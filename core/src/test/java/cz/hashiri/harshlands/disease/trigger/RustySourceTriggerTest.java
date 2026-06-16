package cz.hashiri.harshlands.disease.trigger;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class RustySourceTriggerTest {
    private static final Set<String> CAUSES = Set.of("CONTACT");
    private static final Set<Material> MATS = EnumSet.of(Material.IRON_BARS, Material.ANVIL);

    // --- pure predicate ---
    @Test void rusty_when_cause_matches() {
        assertTrue(RustySourceTrigger.isRustySource("CONTACT", null, CAUSES, MATS));
    }
    @Test void rusty_when_material_matches() {
        assertTrue(RustySourceTrigger.isRustySource("ENTITY_ATTACK", Material.IRON_BARS, CAUSES, MATS));
    }
    @Test void not_rusty_when_neither_matches() {
        assertFalse(RustySourceTrigger.isRustySource("FALL", Material.DIRT, CAUSES, MATS));
    }
    @Test void not_rusty_with_nulls() {
        assertFalse(RustySourceTrigger.isRustySource(null, null, CAUSES, MATS));
    }

    // --- pending mechanics (one-shot + TTL), mirroring InfectedItemTrigger ---
    private RustySourceTrigger trigger() {
        return new RustySourceTrigger("tetanus", CAUSES, MATS, 0.3, 1000L);
    }
    @Test void pending_taken_once_then_gone() {
        RustySourceTrigger t = trigger();
        UUID p = UUID.randomUUID();
        t.markPending(p, 1000L);
        assertTrue(t.takePending(p, 500L));
        assertFalse(t.takePending(p, 500L));
    }
    @Test void pending_expired_not_taken() {
        RustySourceTrigger t = trigger();
        UUID p = UUID.randomUUID();
        t.markPending(p, 1000L);
        assertFalse(t.takePending(p, 1000L));
    }
    @Test void disease_id_reported() {
        assertEquals("tetanus", trigger().diseaseId());
    }
}
