package cz.hashiri.harshlands.disease.model;

import java.util.List;

public record Disease(
    String id,
    String displayName,
    boolean enabled,
    long incubationTicks,
    long immunityDurationTicks,   // 0 = no immunity after cure
    String cureItemId,            // null/empty = no item cure
    String mitigationType,        // null/empty = no behavioral mitigation
    List<DiseaseStage> stages
) {
    public int maxStage() { return stages.size(); }

    /** stage index is 1-based; returns the stage definition or null if out of range. */
    public DiseaseStage stage(int oneBasedStage) {
        if (oneBasedStage < 1 || oneBasedStage > stages.size()) return null;
        return stages.get(oneBasedStage - 1);
    }
}
