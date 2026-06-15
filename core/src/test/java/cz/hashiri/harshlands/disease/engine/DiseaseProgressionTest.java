package cz.hashiri.harshlands.disease.engine;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiseaseProgressionTest {

    private static final long PER = 60, DUR = 120;
    private static final int MAX = 3;

    @Nested
    class Incubation {
        @Test void decrement_floors_at_zero() {
            assertEquals(0, DiseaseProgression.decrementIncubation(40, PER));
        }
        @Test void decrement_subtracts_one_check() {
            assertEquals(60, DiseaseProgression.decrementIncubation(120, PER));
        }
        @Test void complete_when_reaches_zero() {
            assertTrue(DiseaseProgression.incubationComplete(60, PER));
        }
        @Test void not_complete_when_remaining() {
            assertFalse(DiseaseProgression.incubationComplete(120, PER));
        }
    }

    @Nested
    class Untreated {
        @Test void accumulates_below_duration_holds_stage() {
            DiseaseProgression.StageResult r =
                DiseaseProgression.progressStage(1, 0, PER, DUR, MAX, false);
            assertEquals(1, r.stage());
            assertEquals(60, r.ticksInStage());
            assertFalse(r.cured());
        }
        @Test void advances_when_duration_reached() {
            DiseaseProgression.StageResult r =
                DiseaseProgression.progressStage(1, 60, PER, DUR, MAX, false);
            assertEquals(2, r.stage());
            assertEquals(0, r.ticksInStage());
        }
        @Test void terminal_stage_caps_and_never_advances() {
            DiseaseProgression.StageResult r =
                DiseaseProgression.progressStage(MAX, 120, PER, DUR, MAX, false);
            assertEquals(MAX, r.stage());
            assertEquals(DUR, r.ticksInStage());
            assertFalse(r.cured());
        }
    }

    @Nested
    class Mitigated {
        @Test void regresses_to_previous_stage_when_ticks_underflow() {
            DiseaseProgression.StageResult r =
                DiseaseProgression.progressStage(2, 0, PER, DUR, MAX, true);
            assertEquals(1, r.stage());
            assertEquals(DUR, r.ticksInStage());
            assertFalse(r.cured());
        }
        @Test void regress_below_stage_one_is_cured() {
            DiseaseProgression.StageResult r =
                DiseaseProgression.progressStage(1, 0, PER, DUR, MAX, true);
            assertTrue(r.cured());
            assertEquals(0, r.stage());
        }
        @Test void mitigation_within_stage_just_reduces_ticks() {
            DiseaseProgression.StageResult r =
                DiseaseProgression.progressStage(2, 120, PER, DUR, MAX, true);
            assertEquals(2, r.stage());
            assertEquals(60, r.ticksInStage());
        }
    }
}
