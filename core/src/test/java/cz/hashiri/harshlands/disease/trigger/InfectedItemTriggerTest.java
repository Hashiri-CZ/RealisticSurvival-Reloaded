package cz.hashiri.harshlands.disease.trigger;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class InfectedItemTriggerTest {
    private static final Set<Material> SPOILED =
        EnumSet.of(Material.ROTTEN_FLESH, Material.SPIDER_EYE, Material.PUFFERFISH);

    // --- pure predicate: material-list mode (Septicemia) ---
    @Test void material_mode_infectious_for_listed_material() {
        assertTrue(InfectedItemTrigger.infectious(Material.ROTTEN_FLESH, false, SPOILED, false));
    }
    @Test void material_mode_not_infectious_for_unlisted_material() {
        assertFalse(InfectedItemTrigger.infectious(Material.BREAD, false, SPOILED, false));
    }
    @Test void material_mode_ignores_nbt_tag() {
        assertFalse(InfectedItemTrigger.infectious(Material.BREAD, true, SPOILED, false));
    }
    @Test void material_mode_null_material_not_infectious() {
        assertFalse(InfectedItemTrigger.infectious(null, false, SPOILED, false));
    }

    // --- pure predicate: NBT-tag mode (Wasting Blight) ---
    @Test void tag_mode_infectious_only_when_tagged() {
        assertTrue(InfectedItemTrigger.infectious(Material.BREAD, true, Set.of(), true));
    }
    @Test void tag_mode_not_infectious_for_untagged_listed_material() {
        assertFalse(InfectedItemTrigger.infectious(Material.ROTTEN_FLESH, false, SPOILED, true));
    }

    // --- pending mechanics (one-shot + TTL) ---
    private InfectedItemTrigger trigger() {
        return new InfectedItemTrigger("septicemia", SPOILED, false, 0.5, 1000L);
    }
    @Test void pending_is_taken_once_then_gone() {
        InfectedItemTrigger t = trigger();
        UUID p = UUID.randomUUID();
        t.markPending(p, 1000L);
        assertTrue(t.takePending(p, 500L));
        assertFalse(t.takePending(p, 500L)); // one-shot: already consumed
    }
    @Test void pending_expired_by_ttl_is_not_taken() {
        InfectedItemTrigger t = trigger();
        UUID p = UUID.randomUUID();
        t.markPending(p, 1000L);
        assertFalse(t.takePending(p, 1000L));
        assertFalse(t.takePending(p, 1500L));
    }
    @Test void unknown_player_has_no_pending() {
        assertFalse(trigger().takePending(UUID.randomUUID(), 0L));
    }
    @Test void disease_id_is_reported() {
        assertEquals("septicemia", trigger().diseaseId());
    }
}
