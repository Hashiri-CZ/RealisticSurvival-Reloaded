package cz.hashiri.harshlands.disease.engine;

/** Pure progression decisions. No Bukkit, no state — fully unit-testable. */
public final class DiseaseProgression {

    private DiseaseProgression() {}

    /** Result of one stage tick. {@code cured} means the infection should be removed. */
    public record StageResult(int stage, long ticksInStage, boolean cured) {}

    public static long decrementIncubation(long incubationLeft, long ticksPerCheck) {
        return Math.max(0L, incubationLeft - ticksPerCheck);
    }

    public static boolean incubationComplete(long incubationLeft, long ticksPerCheck) {
        return incubationLeft - ticksPerCheck <= 0L;
    }

    /**
     * Advance or regress one active stage by a single check interval.
     *
     * @param stage              current stage (>= 1)
     * @param ticksInStage       ticks accumulated in this stage
     * @param ticksPerCheck      ticks between progression checks
     * @param stageDurationTicks ticks the current stage must accumulate to advance
     * @param maxStage           highest (terminal) stage index
     * @param mitigationActive   true if the player meets the disease's behavioral mitigation
     */
    public static StageResult progressStage(int stage, long ticksInStage, long ticksPerCheck,
                                            long stageDurationTicks, int maxStage,
                                            boolean mitigationActive) {
        long delta = mitigationActive ? -ticksPerCheck : ticksPerCheck;
        long t = ticksInStage + delta;

        if (mitigationActive && t < 0L) {
            if (stage <= 1) {
                return new StageResult(0, 0L, true); // recovered
            }
            return new StageResult(stage - 1, stageDurationTicks, false);
        }

        if (!mitigationActive && t >= stageDurationTicks) {
            if (stage >= maxStage) {
                return new StageResult(maxStage, stageDurationTicks, false); // terminal: hold
            }
            return new StageResult(stage + 1, 0L, false);
        }

        return new StageResult(stage, Math.max(0L, t), false);
    }
}
