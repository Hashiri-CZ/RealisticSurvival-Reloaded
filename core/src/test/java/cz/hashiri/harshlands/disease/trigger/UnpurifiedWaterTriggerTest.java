package cz.hashiri.harshlands.disease.trigger;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class UnpurifiedWaterTriggerTest {
    private UnpurifiedWaterTrigger trigger() {
        return new UnpurifiedWaterTrigger("dysentery", 0.25, 1000L);
    }
    @Test void pending_taken_once_then_gone() {
        UnpurifiedWaterTrigger t = trigger();
        UUID p = UUID.randomUUID();
        t.markPending(p, 1000L);
        assertTrue(t.takePending(p, 500L));
        assertFalse(t.takePending(p, 500L));
    }
    @Test void pending_expired_not_taken() {
        UnpurifiedWaterTrigger t = trigger();
        UUID p = UUID.randomUUID();
        t.markPending(p, 1000L);
        assertFalse(t.takePending(p, 1000L));
    }
    @Test void unknown_player_has_no_pending() {
        assertFalse(trigger().takePending(UUID.randomUUID(), 0L));
    }
    @Test void disease_id_reported() {
        assertEquals("dysentery", trigger().diseaseId());
    }
}
