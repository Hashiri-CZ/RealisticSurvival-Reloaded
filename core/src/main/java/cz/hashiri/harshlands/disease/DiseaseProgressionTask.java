/*
    Copyright (C) 2026  Hashiri_

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cz.hashiri.harshlands.disease;

import cz.hashiri.harshlands.data.HLPlayer;
import cz.hashiri.harshlands.data.disease.ActiveInfection;
import cz.hashiri.harshlands.data.disease.DataModule;
import cz.hashiri.harshlands.disease.engine.DiseaseProgression;
import cz.hashiri.harshlands.disease.model.Disease;
import cz.hashiri.harshlands.disease.model.DiseaseStage;
import cz.hashiri.harshlands.disease.model.SymptomSpec;
import cz.hashiri.harshlands.disease.symptom.SymptomContext;
import cz.hashiri.harshlands.disease.symptom.SymptomHandler;
import cz.hashiri.harshlands.disease.symptom.SymptomHandlers;
import cz.hashiri.harshlands.disease.trigger.DiseaseTrigger;
import cz.hashiri.harshlands.data.HLModule;
import cz.hashiri.harshlands.hints.HintKey;
import cz.hashiri.harshlands.hints.HintsModule;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class DiseaseProgressionTask implements Runnable {

    private final DiseaseModule module;
    private final DiseaseRegistry registry;
    private final SymptomHandlers handlers;
    private final long ticksPerCheck;
    private final Random random = new Random();

    public DiseaseProgressionTask(DiseaseModule module, DiseaseRegistry registry,
                                  SymptomHandlers handlers, long ticksPerCheck) {
        this.module = module;
        this.registry = registry;
        this.handlers = handlers;
        this.ticksPerCheck = ticksPerCheck;
    }

    @Override
    public void run() {
        // Wall-clock millis: immunity expiry must survive /time set and be world-agnostic.
        long now = System.currentTimeMillis();
        for (HLPlayer hlPlayer : new ArrayList<>(HLPlayer.getPlayers().values())) {
            Player p = hlPlayer.getPlayer();
            if (p == null || !p.isOnline() || p.isDead()) continue;
            if (!module.isEnabled(p.getWorld())) continue;
            DataModule dm = hlPlayer.getDiseaseDataModule();
            if (dm == null) continue;

            for (Disease disease : registry.all()) {
                tickDisease(p, dm, disease, now);
            }
        }
    }

    private void tickDisease(Player p, DataModule dm, Disease disease, long now) {
        ActiveInfection inf = dm.getInfection(disease.id());

        if (inf == null) {
            // Triggers are polled even while immune, on purpose: several of them are event-driven
            // and hold a short-lived one-shot "pending exposure" that chance() CONSUMES on read.
            // Returning early on immunity would leave such an exposure sitting in its TTL, ready to
            // be picked up by the first check after immunity lapses — attributing an exposure that
            // happened during immunity to a later moment. contractionChance() drains those reads and
            // still returns 0 while immune, so an immune player can never contract the disease.
            double chance = contractionChance(module.getTriggers(), disease.id(), p,
                dm.isImmune(disease.id(), now), dm.getContractionMultiplier(now));
            if (chance > 0 && random.nextDouble() < chance) {
                dm.contract(disease.id(), disease.incubationTicks(), now);
                dm.incrementContractionCount();
                module.getNotifier().notifyContracted(p);
            }
            return;
        }

        if (inf.isIncubating()) {
            if (DiseaseProgression.incubationComplete(inf.getIncubationLeft(), ticksPerCheck)) {
                inf.setStage(1);
                inf.setTicksInStage(0);
                inf.setIncubationLeft(0);
                module.getNotifier().notifyStageChange(p, 0, 1);
                sendHint(p, HintKey.FIRST_SICKNESS_ONSET);
            } else {
                inf.setIncubationLeft(
                    DiseaseProgression.decrementIncubation(inf.getIncubationLeft(), ticksPerCheck));
            }
            dm.markDirty();
            return;
        }

        DiseaseStage stageDef = disease.stage(inf.getStage());
        if (stageDef == null) { dm.removeInfection(disease.id()); return; }

        boolean mitigating = module.mitigationActive(disease, p);
        DiseaseProgression.StageResult r = DiseaseProgression.progressStage(
            inf.getStage(), inf.getTicksInStage(), ticksPerCheck,
            stageDef.durationTicks(), prevStageDuration(disease, inf.getStage()),
            disease.maxStage(), mitigating);

        if (r.cured()) {
            clearSymptoms(p, disease, inf.getStage());
            dm.removeInfection(disease.id());
            if (disease.immunityDurationTicks() > 0) {
                // immunityDurationTicks is in game ticks; convert to ms to match the wall clock.
                dm.grantImmunity(disease.id(), now + disease.immunityDurationTicks() * 50L);
            }
            module.getNotifier().notifyNaturalCure(p);
            sendHint(p, HintKey.FIRST_SICKNESS_CURED);
            return;
        }

        if (r.stage() != inf.getStage()) {
            clearSymptoms(p, disease, inf.getStage());
            module.getNotifier().notifyStageChange(p, inf.getStage(), r.stage());
            if (r.stage() > inf.getStage()) {
                // Terminal is the lesson worth its own hint: it is the only stage that
                // kills, and it is the last point at which curing still helps.
                sendHint(p, r.stage() == disease.maxStage()
                    ? HintKey.FIRST_SICKNESS_TERMINAL
                    : HintKey.FIRST_SICKNESS_WORSENED);
            }
        }
        inf.setStage(r.stage());
        inf.setTicksInStage(r.ticksInStage());
        dm.markDirty();

        applySymptoms(p, disease, inf.getStage());
    }

    /**
     * Duration of the stage one below {@code stage} — the stage a mitigated regression enters.
     * 0 when there is none (stage 1 regresses to a cure, not to a stage).
     */
    private static long prevStageDuration(Disease disease, int stage) {
        DiseaseStage prev = disease.stage(stage - 1);
        return prev != null ? prev.durationTicks() : 0L;
    }

    /**
     * Contraction chance for a player with no active infection of {@code diseaseId}.
     *
     * <p>Every matching trigger is polled exactly once, immune or not. {@code chance()} is a
     * side-effecting read for the event-driven triggers (it consumes a one-shot pending exposure),
     * so polling while immune is what DRAINS an exposure recorded during immunity instead of letting
     * it survive its TTL into the first post-immunity check. An immune player always gets 0.0 back
     * and therefore can never contract the disease.
     *
     * <p>Package-private and static so it can be unit-tested with fake triggers and no Bukkit.
     */
    static double contractionChance(List<DiseaseTrigger> triggers, String diseaseId,
                                    Player p, boolean immune, double contractionMultiplier) {
        double sum = 0.0;
        for (DiseaseTrigger trigger : triggers) {
            if (trigger.diseaseId().equals(diseaseId)) {
                sum += Math.max(0.0, trigger.chance(p));
            }
        }
        if (immune) return 0.0; // drained above, but immunity blocks contraction outright
        double multiplied = sum * contractionMultiplier;
        return cz.hashiri.harshlands.disease.symptom.special.ContractionMath.clampChance(multiplied);
    }

    /**
     * Fires a one-time teaching hint, if the Hints module is loaded and enabled.
     *
     * <p>Looked up per call rather than cached: HintsModule initialises after DiseaseModule
     * (HLPlugin.onEnable), so a reference captured at construction would always be null.
     */
    private void sendHint(Player p, HintKey key) {
        HLModule hints = HLModule.getModule(HintsModule.NAME);
        if (hints instanceof HintsModule hintsModule && hints.isGloballyEnabled()) {
            hintsModule.sendHint(p, key);
        }
    }

    private void applySymptoms(Player p, Disease disease, int stage) {
        DiseaseStage def = disease.stage(stage);
        if (def == null) return;
        for (SymptomSpec spec : def.symptoms()) {
            SymptomHandler h = handlers.get(spec.handlerName());
            if (h != null) h.apply(p, new SymptomContext(disease.id(), stage, spec.params()));
        }
    }

    private void clearSymptoms(Player p, Disease disease, int stage) {
        DiseaseStage def = disease.stage(stage);
        if (def == null) return;
        for (SymptomSpec spec : def.symptoms()) {
            SymptomHandler h = handlers.get(spec.handlerName());
            if (h != null) h.clear(p, new SymptomContext(disease.id(), stage, spec.params()));
        }
    }
}
