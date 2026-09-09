package cz.hashiri.harshlands.disease.trigger;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

    // --- interact-vector gating (Bug: Septicemia caught by merely right-clicking while
    // holding an infected material, e.g. opening a chest with raw chicken in hand, without
    // ever eating it) ---
    private ServerMock server;
    private PlayerMock player;

    @BeforeEach void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
    }

    @AfterEach void tearDown() {
        MockBukkit.unmock();
    }

    @Test void material_mode_interact_never_marks_pending_even_for_infected_material() {
        InfectedItemTrigger t = new InfectedItemTrigger("septicemia", SPOILED, false, 0.5, 1000L);
        ItemStack rawChicken = new ItemStack(Material.CHICKEN);
        PlayerInteractEvent event = new PlayerInteractEvent(
            player, Action.RIGHT_CLICK_BLOCK, rawChicken, null, null, EquipmentSlot.HAND);

        t.onInteract(event);

        assertFalse(t.takePending(player.getUniqueId(), Long.MAX_VALUE),
            "right-clicking while holding spoiled food must not roll Septicemia; only eating it should");
    }

    @Test void tag_mode_interact_off_hand_does_not_mark_pending() {
        InfectedItemTrigger t = new InfectedItemTrigger("wasting_blight", Set.of(), true, 0.25, 1000L);
        ItemStack item = new ItemStack(Material.STICK);
        PlayerInteractEvent event = new PlayerInteractEvent(
            player, Action.RIGHT_CLICK_AIR, item, null, null, EquipmentSlot.OFF_HAND);

        t.onInteract(event);

        assertFalse(t.takePending(player.getUniqueId(), Long.MAX_VALUE),
            "PlayerInteractEvent fires once per hand; the off-hand copy must not mark exposure");
    }

    @Test void tag_mode_interact_wrong_action_does_not_mark_pending() {
        InfectedItemTrigger t = new InfectedItemTrigger("wasting_blight", Set.of(), true, 0.25, 1000L);
        ItemStack item = new ItemStack(Material.STICK);
        PlayerInteractEvent event = new PlayerInteractEvent(
            player, Action.LEFT_CLICK_AIR, item, null, null, EquipmentSlot.HAND);

        t.onInteract(event);

        assertFalse(t.takePending(player.getUniqueId(), Long.MAX_VALUE));
    }
}
