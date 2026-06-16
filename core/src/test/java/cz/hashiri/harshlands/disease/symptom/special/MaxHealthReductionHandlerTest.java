package cz.hashiri.harshlands.disease.symptom.special;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MaxHealthReductionHandlerTest {
    @Test void two_hearts_is_minus_four_hp() {
        assertEquals(-4.0, MaxHealthReductionHandler.modifierAmount(2.0), 1e-9);
    }
    @Test void amount_is_always_negative() {
        assertEquals(-6.0, MaxHealthReductionHandler.modifierAmount(-3.0), 1e-9);
    }
    @Test void zero_hearts_is_zero() {
        assertEquals(0.0, MaxHealthReductionHandler.modifierAmount(0.0), 1e-9);
    }
}
