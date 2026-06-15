package cz.hashiri.harshlands.disease.engine;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiseaseProgressionTest {

    private static final long TICKS_PER_CHECK = 60, STAGE_DURATION = 120;
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
            DiseaseProgression.StageResult r =
                DiseaseProgression.progressStage(1, 0, TICKS_PER_CHECK, STAGE_DURATION, MAX_STAGE, false);
            assertEquals(1, r.stage());
            assertEquals(60, r.ticksInStage());
            assertFalse(r.cured());
        }
        @Test void advances_when_duration_reached() {
            DiseaseProgression.StageResult r =
                DiseaseProgression.progressStage(1, 60, TICKS_PER_CHECK, STAGE_DURATION, MAX_STAGE, false);
            assertEquals(2, r.stage());
            assertEquals(0, r.ticksInStage());
            assertFalse(r.cured());
        }
        @Test void terminal_stage_caps_and_never_advances() {
            DiseaseProgression.StageResult r =
                DiseaseProgression.progressStage(MAX_STAGE, 120, TICKS_PER_CHECK, STAGE_DURATION, MAX_STAGE, false);
            assertEquals(MAX_STAGE, r.stage());
            assertEquals(STAGE_DURATION, r.ticksInStage());
            assertFalse(r.cured());
        }
    }

    @Nested
    class Mitigated {
        @Test void regresses_to_previous_stage_when_ticks_underflow() {
            DiseaseProgression.StageResult r =
                DiseaseProgression.progressStage(2, 0, TICKS_PER_CHECK, STAGE_DURATION, MAX_STAGE, true);
            assertEquals(1, r.stage());
            assertEquals(STAGE_DURATION, r.ticksInStage());
            assertFalse(r.cured());
        }
        @Test void regress_below_stage_one_is_cured() {
            DiseaseProgression.StageResult r =
                DiseaseProgression.progressStage(1, 0, TICKS_PER_CHECK, STAGE_DURATION, MAX_STAGE, true);
            assertTrue(r.cured());
            assertEquals(0, r.stage());
        }
        @Test void mitigation_within_stage_just_reduces_ticks() {
            DiseaseProgression.StageResult r =
                DiseaseProgression.progressStage(2, 120, TICKS_PER_CHECK, STAGE_DURATION, MAX_STAGE, true);
            assertEquals(2, r.stage());
            assertEquals(60, r.ticksInStage());
            assertFalse(r.cured());
        }
    }

    @Nested
    class Guards {
        @Test void progress_stage_rejects_incubating_stage() {
            assertThrows(IllegalArgumentException.class,
                () -> DiseaseProgression.progressStage(0, 0, TICKS_PER_CHECK, STAGE_DURATION, MAX_STAGE, false));
        }
    }
}
