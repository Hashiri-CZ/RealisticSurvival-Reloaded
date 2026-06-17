package cz.hashiri.harshlands.disease.symptom.special;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JittersHandlerTest {
    @Test void zero_chance_never_jitters() {
        assertFalse(JittersHandler.shouldJitter(0.0, 0.0));
    }
    @Test void full_chance_always_jitters() {
        assertTrue(JittersHandler.shouldJitter(1.0, 0.99));
    }
    @Test void jitters_when_roll_below_chance() {
        assertTrue(JittersHandler.shouldJitter(0.3, 0.29));
    }
    @Test void no_jitter_when_roll_at_or_above_chance() {
        assertFalse(JittersHandler.shouldJitter(0.3, 0.3));
        assertFalse(JittersHandler.shouldJitter(0.3, 0.5));
    }
}
