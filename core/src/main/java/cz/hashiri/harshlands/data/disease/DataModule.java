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
package cz.hashiri.harshlands.data.disease;

import cz.hashiri.harshlands.HLPlugin;
import cz.hashiri.harshlands.data.HLDataModule;
import cz.hashiri.harshlands.data.db.HLDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DataModule implements HLDataModule {

    private final UUID id;
    private final HLDatabase database;
    private final Map<String, ActiveInfection> infections = new ConcurrentHashMap<>();
    private final Map<String, Long> immunity = new ConcurrentHashMap<>();
    private volatile boolean dirty = false;

    // Transient runtime state (NOT persisted): immune-suppression contraction multiplier.
    private volatile double contractionMultiplier = 1.0;
    private volatile long contractionMultiplierExpiry = 0L;

    public DataModule(org.bukkit.entity.Player player) {
        this.id = player.getUniqueId();
        this.database = HLPlugin.getPlugin().getDatabase();
    }

    public java.util.Collection<ActiveInfection> getActiveInfections() { return infections.values(); }

    public ActiveInfection getInfection(String diseaseId) { return infections.get(diseaseId); }

    public boolean hasInfection(String diseaseId) { return infections.containsKey(diseaseId); }

    public void contract(String diseaseId, long incubationTicks, long nowTick) {
        infections.put(diseaseId, new ActiveInfection(diseaseId, 0, 0L, incubationTicks, nowTick));
        dirty = true;
    }

    public void removeInfection(String diseaseId) {
        if (infections.remove(diseaseId) != null) dirty = true;
    }

    public boolean isImmune(String diseaseId, long nowTick) {
        Long until = immunity.get(diseaseId);
        return until != null && until > nowTick;
    }

    public void grantImmunity(String diseaseId, long untilTick) {
        if (untilTick > 0) {
            immunity.put(diseaseId, untilTick);
            dirty = true;
        }
    }

    /** Call after mutating an infection in place (stage/ticks/incubation) so it is persisted. */
    public void markDirty() { dirty = true; }

    public boolean isDirty() { return dirty; }

    /** Record an immune-suppression multiplier active until {@code expiryMs} (wall-clock). */
    public void setContractionMultiplier(double multiplier, long expiryMs) {
        this.contractionMultiplier = multiplier;
        this.contractionMultiplierExpiry = expiryMs;
    }

    /** Current contraction multiplier given {@code nowMs}; 1.0 if none/expired. */
    public double getContractionMultiplier(long nowMs) {
        return cz.hashiri.harshlands.disease.symptom.special.ContractionMath
            .effectiveMultiplier(contractionMultiplier, contractionMultiplierExpiry, nowMs);
    }

    @Override
    public void retrieveData() {
        java.util.concurrent.CompletableFuture<Void> infectionsLoad =
            database.loadDiseaseInfections(id).thenAccept(rows -> {
                Map<String, ActiveInfection> fresh = new HashMap<>();
                for (HLDatabase.DiseaseInfectionRow r : rows) {
                    fresh.put(r.diseaseId(), new ActiveInfection(
                        r.diseaseId(), r.stage(), r.ticksInStage(), r.incubationLeft(), r.contractedAt()));
                }
                // Build into a local map first, then swap in one pass to minimise the window
                // during which a reader could observe a partially-populated map.
                infections.clear();
                infections.putAll(fresh);
            });

        java.util.concurrent.CompletableFuture<Void> immunityLoad =
            database.loadDiseaseImmunity(id).thenAccept(map -> {
                Map<String, Long> fresh = new HashMap<>(map);
                immunity.clear();
                immunity.putAll(fresh);
            });

        java.util.concurrent.CompletableFuture.allOf(infectionsLoad, immunityLoad)
            .thenRun(() -> dirty = false)
            .exceptionally(ex -> {
                HLPlugin.getPlugin().getLogger().warning(
                    "[Disease] Failed to load disease data for " + id + ": " + ex.getMessage());
                return null;
            });
    }

    @Override
    public void saveData() {
        dirty = false;
        List<HLDatabase.DiseaseInfectionRow> rows = new ArrayList<>();
        for (ActiveInfection inf : infections.values()) {
            rows.add(new HLDatabase.DiseaseInfectionRow(
                inf.diseaseId(), inf.getStage(), inf.getTicksInStage(),
                inf.getIncubationLeft(), inf.contractedAt()));
        }
        database.saveDiseaseInfections(id, rows);
        database.saveDiseaseImmunity(id, new HashMap<>(immunity));
    }
}
