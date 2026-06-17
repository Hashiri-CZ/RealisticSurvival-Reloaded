package cz.hashiri.harshlands.disease.trigger;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SybokAccumulationTriggerTest {
    private SybokAccumulationTrigger trigger() {
        return new SybokAccumulationTrigger("sybok", 8L, 0.05);
    }
    @Test void accumulated_predicate() {
        assertTrue(SybokAccumulationTrigger.accumulated(8L, 8L));
        assertTrue(SybokAccumulationTrigger.accumulated(9L, 8L));
        assertFalse(SybokAccumulationTrigger.accumulated(7L, 8L));
    }
    @Test void chance_zero_below_threshold() {
        assertEquals(0.0, trigger().chanceFor(7L), 1e-9);
    }
    @Test void chance_value_at_or_above_threshold() {
        assertEquals(0.05, trigger().chanceFor(8L), 1e-9);
        assertEquals(0.05, trigger().chanceFor(20L), 1e-9);
    }
    @Test void disease_id_reported() {
        assertEquals("sybok", trigger().diseaseId());
    }
}
