package cz.hashiri.harshlands.firstaid;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FaHealServiceTest {

    /** In-memory fake of the bridge — captures heal() calls and serves stubbed HP values. */
    private static final class FakeBridge extends BodyHealthBridge {
        boolean available = true;
        final Map<String, Double> currentPercent = new HashMap<>();  // part -> percent (0..100)
        final Map<String, Double> maxHp = new HashMap<>();           // part -> max HP units
        final List<Heal> healed = new ArrayList<>();

        record Heal(String part, double amount) {}

        @Override public boolean isAvailable() { return available; }
        @Override public double getHealth(org.bukkit.entity.Player p, String part) {
            return currentPercent.getOrDefault(part, 100.0);
        }
        @Override public double getMaxHealth(org.bukkit.entity.Player p, String part) {
            return maxHp.getOrDefault(part, 6.0);
        }
        @Override public boolean heal(org.bukkit.entity.Player p, String part, double amount) {
            healed.add(new Heal(part, amount));
            // mutate the fake state so subsequent reads see the heal
            double max = maxHp.getOrDefault(part, 6.0);
            double curHp = currentPercent.getOrDefault(part, 100.0) / 100.0 * max;
            double newHp = Math.min(max, curHp + amount);
            currentPercent.put(part, newHp / max * 100.0);
            return true;
        }
    }

    private static FaHealItem bandage() {
        return new FaHealItem("bandage", 2.0,
                List.of("TORSO", "ARM_LEFT", "ARM_RIGHT", "LEG_LEFT", "LEG_RIGHT", "FOOT_LEFT", "FOOT_RIGHT"),
                "ITEM_ARMOR_EQUIP_LEATHER");
    }

    private static FaHealItem splint() {
        return new FaHealItem("splint", 4.0,
                List.of("LEG_LEFT", "LEG_RIGHT", "FOOT_LEFT", "FOOT_RIGHT"),
                "ITEM_ARMOR_EQUIP_LEATHER");
    }

    private static FaHealItem medicalKit() {
        return new FaHealItem("medical_kit", 6.0,
                List.of("HEAD", "TORSO", "ARM_LEFT", "ARM_RIGHT",
                        "LEG_LEFT", "LEG_RIGHT", "FOOT_LEFT", "FOOT_RIGHT"),
                "ITEM_ARMOR_EQUIP_LEATHER");
    }

    private static FaHealService service(BodyHealthBridge bridge, FaHealItem... items) {
        Map<String, FaHealItem> map = new HashMap<>();
        for (FaHealItem i : items) map.put(i.name(), i);
        return new FaHealService(bridge, map);
    }

    @Test
    void returns_unavailable_when_bridge_is_unavailable() {
        FakeBridge bridge = new FakeBridge();
        bridge.available = false;

        FaHealService svc = service(bridge, bandage());

        assertEquals(FaHealService.Result.BODYHEALTH_UNAVAILABLE, svc.use(null, "bandage"));
        assertTrue(bridge.healed.isEmpty());
    }

    @Test
    void returns_unknown_item_when_name_not_registered() {
        FakeBridge bridge = new FakeBridge();
        FaHealService svc = service(bridge, bandage());

        assertEquals(FaHealService.Result.UNKNOWN_ITEM, svc.use(null, "not_a_real_item"));
        assertTrue(bridge.healed.isEmpty());
    }

    @Test
    void returns_no_injury_when_all_allowed_parts_are_full() {
        FakeBridge bridge = new FakeBridge();
        // All defaults are 100% — nothing to heal.

        FaHealService svc = service(bridge, bandage());

        assertEquals(FaHealService.Result.NO_INJURY, svc.use(null, "bandage"));
        assertTrue(bridge.healed.isEmpty());
    }

    @Test
    void heals_the_most_injured_allowed_part() {
        FakeBridge bridge = new FakeBridge();
        bridge.maxHp.put("TORSO", 6.0);
        bridge.maxHp.put("ARM_LEFT", 6.0);
        bridge.currentPercent.put("TORSO", 50.0);     // 3 HP missing
        bridge.currentPercent.put("ARM_LEFT", 16.0);  // ~5 HP missing — more injured

        FaHealService svc = service(bridge, bandage());

        assertEquals(FaHealService.Result.HEALED, svc.use(null, "bandage"));
        assertEquals(1, bridge.healed.size());
        assertEquals("ARM_LEFT", bridge.healed.get(0).part());
        assertEquals(2.0, bridge.healed.get(0).amount());
    }

    @Test
    void splint_skips_torso_even_if_torso_is_most_injured() {
        FakeBridge bridge = new FakeBridge();
        bridge.maxHp.put("TORSO", 6.0);
        bridge.maxHp.put("LEG_LEFT", 6.0);
        bridge.currentPercent.put("TORSO", 16.0);     // most injured but TORSO not in splint allowlist
        bridge.currentPercent.put("LEG_LEFT", 50.0);  // splint should pick this

        FaHealService svc = service(bridge, splint());

        assertEquals(FaHealService.Result.HEALED, svc.use(null, "splint"));
        assertEquals("LEG_LEFT", bridge.healed.get(0).part());
    }

    @Test
    void tie_break_uses_affects_parts_declaration_order() {
        FakeBridge bridge = new FakeBridge();
        bridge.maxHp.put("LEG_LEFT", 6.0);
        bridge.maxHp.put("LEG_RIGHT", 6.0);
        bridge.currentPercent.put("LEG_LEFT", 50.0);
        bridge.currentPercent.put("LEG_RIGHT", 50.0);

        FaHealService svc = service(bridge, splint()); // splint declares LEG_LEFT before LEG_RIGHT

        svc.use(null, "splint");
        assertEquals("LEG_LEFT", bridge.healed.get(0).part());
    }

    @Test
    void heal_amount_is_capped_at_remaining_missing_hp() {
        FakeBridge bridge = new FakeBridge();
        bridge.maxHp.put("TORSO", 6.0);
        bridge.currentPercent.put("TORSO", 83.33); // missing ~1 HP

        FaHealService svc = service(bridge, medicalKit()); // would heal 6.0 raw

        svc.use(null, "medical_kit");
        // Service requested only what was missing — not the full 6.0
        assertEquals(1, bridge.healed.size());
        assertTrue(bridge.healed.get(0).amount() <= 1.01,
                "expected capped heal ~1.0 HP, got " + bridge.healed.get(0).amount());
    }

    @Test
    void result_carries_the_part_that_was_healed() {
        FakeBridge bridge = new FakeBridge();
        bridge.maxHp.put("TORSO", 6.0);
        bridge.currentPercent.put("TORSO", 50.0);

        FaHealService svc = service(bridge, bandage());
        FaHealService.Outcome out = svc.useWithOutcome(null, "bandage");

        assertEquals(FaHealService.Result.HEALED, out.result());
        assertEquals("TORSO", out.healedPart());
    }
}
