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
import org.bukkit.entity.Player;

import java.util.ArrayList;
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
            if (dm.isImmune(disease.id(), now)) return;
            double chance = totalChance(p, dm, disease.id(), now);
            if (chance > 0 && random.nextDouble() < chance) {
                dm.contract(disease.id(), disease.incubationTicks(), now);
                dm.incrementContractionCount();
            }
            return;
        }

        if (inf.isIncubating()) {
            if (DiseaseProgression.incubationComplete(inf.getIncubationLeft(), ticksPerCheck)) {
                inf.setStage(1);
                inf.setTicksInStage(0);
                inf.setIncubationLeft(0);
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
            stageDef.durationTicks(), disease.maxStage(), mitigating);

        if (r.cured()) {
            clearSymptoms(p, disease, inf.getStage());
            dm.removeInfection(disease.id());
            if (disease.immunityDurationTicks() > 0) {
                // immunityDurationTicks is in game ticks; convert to ms to match the wall clock.
                dm.grantImmunity(disease.id(), now + disease.immunityDurationTicks() * 50L);
            }
            return;
        }

        if (r.stage() != inf.getStage()) {
            clearSymptoms(p, disease, inf.getStage());
        }
        inf.setStage(r.stage());
        inf.setTicksInStage(r.ticksInStage());
        dm.markDirty();

        applySymptoms(p, disease, inf.getStage());
    }

    private double totalChance(Player p, DataModule dm, String diseaseId, long now) {
        double sum = 0.0;
        for (DiseaseTrigger trigger : module.getTriggers()) {
            if (trigger.diseaseId().equals(diseaseId)) {
                sum += Math.max(0.0, trigger.chance(p));
            }
        }
        double multiplied = sum * dm.getContractionMultiplier(now);
        return cz.hashiri.harshlands.disease.symptom.special.ContractionMath.clampChance(multiplied);
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
