package cz.hashiri.harshlands.disease.symptom.builtin;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HungerDrainHandlerTest {
    @Test void drains_by_amount() {
        assertEquals(15, HungerDrainHandler.drained(17, 2));
    }
    @Test void floors_at_zero() {
        assertEquals(0, HungerDrainHandler.drained(1, 5));
    }
    @Test void zero_amount_is_noop() {
        assertEquals(20, HungerDrainHandler.drained(20, 0));
    }
}
