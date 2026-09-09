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
package cz.hashiri.harshlands.disease.engine;

/** Pure progression decisions. No Bukkit, no state — fully unit-testable. */
public final class DiseaseProgression {

    private DiseaseProgression() {}

    /**
     * Result of one stage tick.
     * @param stage        the new stage; 0 only when {@code cured} is true
     * @param ticksInStage accumulated ticks in the new stage
     * @param cured        true if the infection should be removed from the player entirely
     */
    public record StageResult(int stage, long ticksInStage, boolean cured) {}

    /** Decrement remaining incubation by one check interval, floored at zero. */
    public static long decrementIncubation(long incubationLeft, long ticksPerCheck) {
        return Math.max(0L, incubationLeft - ticksPerCheck);
    }

    /**
     * True when this check interval brings incubation to (or below) zero.
     * Pass the value BEFORE decrementing — i.e. the currently-stored incubationLeft.
     * Callers must only invoke this while the infection is still incubating (stage 0).
     */
    public static boolean incubationComplete(long incubationLeft, long ticksPerCheck) {
        return incubationLeft - ticksPerCheck <= 0L;
    }

    /**
     * Advance or regress one active stage by a single check interval.
     *
     * <p>Regression re-enters {@code stage - 1} with that stage's OWN duration, so the player must
     * mitigate for the full length of the stage they fall back into before regressing further.
     * Carrying the current stage's duration instead would let the terminal stage — whose configured
     * duration is 0 in every shipped disease — cascade straight through the intermediate stages.
     *
     * @param stage                  current stage (>= 1)
     * @param ticksInStage           ticks accumulated in this stage
     * @param ticksPerCheck          ticks between progression checks
     * @param stageDurationTicks     ticks the current stage must accumulate to advance
     * @param prevStageDurationTicks duration of {@code stage - 1}, i.e. the stage regression enters;
     *                               ignored when {@code stage == 1} (regressing there cures instead)
     * @param maxStage               highest (terminal) stage index
     * @param mitigationActive       true if the player meets the disease's behavioral mitigation
     */
    public static StageResult progressStage(int stage, long ticksInStage, long ticksPerCheck,
                                            long stageDurationTicks, long prevStageDurationTicks,
                                            int maxStage, boolean mitigationActive) {
        if (stage < 1) {
            throw new IllegalArgumentException("progressStage requires stage >= 1, got " + stage);
        }
        long delta = mitigationActive ? -ticksPerCheck : ticksPerCheck;
        long t = ticksInStage + delta;

        if (mitigationActive && t < 0L) {
            if (stage <= 1) {
                return new StageResult(0, 0L, true); // recovered
            }
            // Enter the previous stage "full": its own duration must be burned down next.
            return new StageResult(stage - 1, Math.max(0L, prevStageDurationTicks), false);
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
