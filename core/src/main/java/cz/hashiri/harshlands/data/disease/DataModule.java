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

    public DataModule(org.bukkit.entity.Player player) {
        this.id = player.getUniqueId();
        this.database = HLPlugin.getPlugin().getDatabase();
    }

    public UUID getId() { return id; }

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

    @Override
    public void retrieveData() {
        database.loadDiseaseInfections(id).thenAccept(rows -> {
            infections.clear();
            for (HLDatabase.DiseaseInfectionRow r : rows) {
                infections.put(r.diseaseId(), new ActiveInfection(
                    r.diseaseId(), r.stage(), r.ticksInStage(), r.incubationLeft(), r.contractedAt()));
            }
            dirty = false;
        });
        database.loadDiseaseImmunity(id).thenAccept(map -> {
            immunity.clear();
            immunity.putAll(map);
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
