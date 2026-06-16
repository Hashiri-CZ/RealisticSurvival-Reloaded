package cz.hashiri.harshlands.disease.engine;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DoseMathTest {
    @Test void first_dose_applies_when_no_previous_dose() {
        DoseMath.DoseOutcome o = DoseMath.applyDose(3, 0L, 1000L, 50_000L);
        assertFalse(o.onCooldown());
        assertEquals(2, o.newStage());
        assertFalse(o.cured());
    }
    @Test void dose_within_cooldown_is_rejected() {
        DoseMath.DoseOutcome o = DoseMath.applyDose(3, 10_000L, 1000L, 10_500L);
        assertTrue(o.onCooldown());
        assertEquals(3, o.newStage());
        assertFalse(o.cured());
    }
    @Test void dose_at_or_after_cooldown_regresses_one_stage() {
        DoseMath.DoseOutcome o = DoseMath.applyDose(3, 10_000L, 1000L, 11_000L);
        assertFalse(o.onCooldown());
        assertEquals(2, o.newStage());
        assertFalse(o.cured());
    }
    @Test void regressing_from_stage_one_cures() {
        DoseMath.DoseOutcome o = DoseMath.applyDose(1, 0L, 1000L, 50_000L);
        assertFalse(o.onCooldown());
        assertEquals(0, o.newStage());
        assertTrue(o.cured());
    }
    @Test void incubating_stage_zero_dose_cures_early() {
        DoseMath.DoseOutcome o = DoseMath.applyDose(0, 0L, 1000L, 50_000L);
        assertTrue(o.cured());
        assertEquals(0, o.newStage());
    }
    @Test void zero_cooldown_allows_back_to_back_doses() {
        DoseMath.DoseOutcome o = DoseMath.applyDose(3, 49_999L, 0L, 50_000L);
        assertFalse(o.onCooldown());
        assertEquals(2, o.newStage());
    }
}
