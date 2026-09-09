package cz.hashiri.harshlands.disease.engine;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiseaseProgressionTest {

    private static final long TICKS_PER_CHECK = 60;
    // Deliberately DISTINCT per-stage durations: a uniform duration makes "current stage duration"
    // and "entered stage duration" indistinguishable, which hides regression bugs.
    private static final long STAGE_1_DURATION = 120;
    private static final long STAGE_2_DURATION = 240;
    private static final long STAGE_3_DURATION = 360;
    private static final long NO_PREV_STAGE = 0;
    private static final int MAX_STAGE = 3;

    @Nested
    class Incubation {
        @Test void decrement_floors_at_zero() {
            assertEquals(0, DiseaseProgression.decrementIncubation(40, TICKS_PER_CHECK));
        }
        @Test void decrement_subtracts_one_check() {
            assertEquals(60, DiseaseProgression.decrementIncubation(120, TICKS_PER_CHECK));
        }
        @Test void complete_when_reaches_zero() {
            assertTrue(DiseaseProgression.incubationComplete(60, TICKS_PER_CHECK));
        }
        @Test void not_complete_when_remaining() {
            assertFalse(DiseaseProgression.incubationComplete(120, TICKS_PER_CHECK));
        }
        @Test void decrement_floors_at_exactly_zero() {
            assertEquals(0, DiseaseProgression.decrementIncubation(60, TICKS_PER_CHECK));
        }
    }

    @Nested
    class Untreated {
        @Test void accumulates_below_duration_holds_stage() {
            DiseaseProgression.StageResult r = DiseaseProgression.progressStage(
                1, 0, TICKS_PER_CHECK, STAGE_1_DURATION, NO_PREV_STAGE, MAX_STAGE, false);
            assertEquals(1, r.stage());
            assertEquals(60, r.ticksInStage());
            assertFalse(r.cured());
        }
        @Test void advances_when_duration_reached() {
            DiseaseProgression.StageResult r = DiseaseProgression.progressStage(
                1, 60, TICKS_PER_CHECK, STAGE_1_DURATION, NO_PREV_STAGE, MAX_STAGE, false);
            assertEquals(2, r.stage());
            assertEquals(0, r.ticksInStage());
            assertFalse(r.cured());
        }
        @Test void terminal_stage_caps_and_never_advances() {
            DiseaseProgression.StageResult r = DiseaseProgression.progressStage(
                MAX_STAGE, STAGE_3_DURATION, TICKS_PER_CHECK,
                STAGE_3_DURATION, STAGE_2_DURATION, MAX_STAGE, false);
            assertEquals(MAX_STAGE, r.stage());
            assertEquals(STAGE_3_DURATION, r.ticksInStage());
            assertFalse(r.cured());
        }
        @Test void previous_stage_duration_is_ignored_while_progressing() {
            DiseaseProgression.StageResult r = DiseaseProgression.progressStage(
                2, 0, TICKS_PER_CHECK, STAGE_2_DURATION, STAGE_1_DURATION, MAX_STAGE, false);
            assertEquals(2, r.stage());
            assertEquals(60, r.ticksInStage());
        }
    }

    @Nested
    class Mitigated {
        @Test void regresses_to_previous_stage_when_ticks_underflow() {
            DiseaseProgression.StageResult r = DiseaseProgression.progressStage(
                2, 0, TICKS_PER_CHECK, STAGE_2_DURATION, STAGE_1_DURATION, MAX_STAGE, true);
            assertEquals(1, r.stage());
            assertFalse(r.cured());
            // Must carry the ENTERED stage's duration, not the stage just left.
            assertEquals(STAGE_1_DURATION, r.ticksInStage());
            assertNotEquals(STAGE_2_DURATION, r.ticksInStage());
        }
        @Test void regress_below_stage_one_is_cured() {
            DiseaseProgression.StageResult r = DiseaseProgression.progressStage(
                1, 0, TICKS_PER_CHECK, STAGE_1_DURATION, NO_PREV_STAGE, MAX_STAGE, true);
            assertTrue(r.cured());
            assertEquals(0, r.stage());
        }
        @Test void mitigation_within_stage_just_reduces_ticks() {
            DiseaseProgression.StageResult r = DiseaseProgression.progressStage(
                2, STAGE_2_DURATION, TICKS_PER_CHECK,
                STAGE_2_DURATION, STAGE_1_DURATION, MAX_STAGE, true);
            assertEquals(2, r.stage());
            assertEquals(STAGE_2_DURATION - TICKS_PER_CHECK, r.ticksInStage());
            assertFalse(r.cured());
        }
        @Test void mitigating_inside_the_terminal_stage_only_reduces_ticks() {
            DiseaseProgression.StageResult r = DiseaseProgression.progressStage(
                MAX_STAGE, STAGE_3_DURATION, TICKS_PER_CHECK,
                STAGE_3_DURATION, STAGE_2_DURATION, MAX_STAGE, true);
            assertEquals(MAX_STAGE, r.stage());
            assertEquals(STAGE_3_DURATION - TICKS_PER_CHECK, r.ticksInStage());
        }
    }

    /**
     * Reproduces the shipped NO_EXPOSURE configs (endersion / crimson / dysentery / battle_trauma):
     * stage durations [2400, 2400, 0] with the terminal stage held indefinitely at ticksInStage 0.
     * Before the fix, regressing out of the terminal stage carried the TERMINAL stage's duration (0),
     * so the very next check underflowed again and stage 2 was skipped entirely — terminal to stage 1
     * in two checks (~6 s).
     */
    @Nested
    class ShippedTerminalRegression {
        private static final long[] DURATIONS = {2400, 2400, 0}; // 1-based stages 1..3
        private static final int MAX = 3;

        private static long durationOf(int stage) { return DURATIONS[stage - 1]; }
        private static long prevDurationOf(int stage) { return stage <= 1 ? 0 : DURATIONS[stage - 2]; }

        private static DiseaseProgression.StageResult tick(int stage, long ticksInStage) {
            return DiseaseProgression.progressStage(
                stage, ticksInStage, TICKS_PER_CHECK,
                durationOf(stage), prevDurationOf(stage), MAX, true);
        }

        @Test void terminal_regression_does_not_skip_stage_two() {
            // Terminal hold state: stage 3, ticksInStage 0 (stage 3 DurationTicks is 0).
            DiseaseProgression.StageResult first = tick(MAX, 0);
            assertEquals(2, first.stage());
            assertEquals(2400, first.ticksInStage(), "must enter stage 2 with stage 2's duration");

            // The very next mitigating check must STAY in stage 2, not cascade down to stage 1.
            DiseaseProgression.StageResult second = tick(first.stage(), first.ticksInStage());
            assertEquals(2, second.stage(), "stage 2 must not be skipped");
            assertEquals(2340, second.ticksInStage());
        }

        @Test void stage_two_takes_a_full_duration_of_mitigation_to_leave() {
            int stage = MAX;
            long ticks = 0;
            int checks = 0;
            while (stage == MAX || stage == 2) {
                DiseaseProgression.StageResult r = tick(stage, ticks);
                assertFalse(r.cured());
                stage = r.stage();
                ticks = r.ticksInStage();
                checks++;
                assertTrue(checks < 1000, "regression must terminate");
            }
            assertEquals(1, stage);
            // 1 check to leave the terminal stage, 2400/60 = 40 checks to burn stage 2 down to 0,
            // then 1 more check to underflow out of stage 2 = 42.
            assertEquals(42, checks);
        }

        @Test void full_mitigated_recovery_from_terminal_reaches_cure() {
            int stage = MAX;
            long ticks = 0;
            int checks = 0;
            DiseaseProgression.StageResult r;
            do {
                r = tick(stage, ticks);
                stage = r.stage();
                ticks = r.ticksInStage();
                checks++;
                assertTrue(checks < 1000, "regression must terminate");
            } while (!r.cured());
            // 1 (leave terminal) + 41 (stage 2) + 40 (stage 1) + 1 (cure) = 83 checks,
            // i.e. ~4 minutes of mitigation rather than the ~6 seconds the bug allowed.
            assertEquals(83, checks);
        }
    }

    @Nested
    class Guards {
        @Test void progress_stage_rejects_incubating_stage() {
            assertThrows(IllegalArgumentException.class, () -> DiseaseProgression.progressStage(
                0, 0, TICKS_PER_CHECK, STAGE_1_DURATION, NO_PREV_STAGE, MAX_STAGE, false));
        }
    }
}
