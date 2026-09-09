package cz.hashiri.harshlands.disease.trigger;

import cz.hashiri.harshlands.foodexpansion.NutrientTier;
import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DietTriggerTest {
    private static final Set<NutrientTier> BAD =
        EnumSet.of(NutrientTier.STARVING, NutrientTier.SEVERELY_MALNOURISHED, NutrientTier.MALNOURISHED);

    @Test void malnourished_true_for_listed_tier() {
        assertTrue(DietTrigger.malnourished(NutrientTier.MALNOURISHED, BAD));
    }
    @Test void malnourished_false_for_normal() {
        assertFalse(DietTrigger.malnourished(NutrientTier.NORMAL, BAD));
    }
    @Test void malnourished_false_for_null() {
        assertFalse(DietTrigger.malnourished(null, BAD));
    }
    @Test void parse_tiers_maps_known_and_skips_unknown() {
        Set<NutrientTier> s = DietTrigger.parseTiers(List.of("malnourished", "starving", "bogus"));
        assertEquals(EnumSet.of(NutrientTier.MALNOURISHED, NutrientTier.STARVING), s);
    }
    @Test void chance_zero_until_sustain_threshold_then_fires() {
        DietTrigger t = new DietTrigger("malnutrition", BAD, 3, 0.05, p -> NutrientTier.NORMAL);
        UUID u = UUID.randomUUID();
        assertEquals(0.0, t.chanceFor(u, NutrientTier.MALNOURISHED), 1e-9); // 1
        assertEquals(0.0, t.chanceFor(u, NutrientTier.MALNOURISHED), 1e-9); // 2
        assertEquals(0.05, t.chanceFor(u, NutrientTier.MALNOURISHED), 1e-9); // 3 -> threshold
        assertEquals(0.05, t.chanceFor(u, NutrientTier.STARVING), 1e-9);     // 4 stays
    }
    @Test void chance_resets_when_nourished() {
        DietTrigger t = new DietTrigger("malnutrition", BAD, 2, 0.05, p -> NutrientTier.NORMAL);
        UUID u = UUID.randomUUID();
        t.chanceFor(u, NutrientTier.MALNOURISHED);                  // 1
        assertEquals(0.0, t.chanceFor(u, NutrientTier.NORMAL), 1e-9); // reset
        assertEquals(0.0, t.chanceFor(u, NutrientTier.MALNOURISHED), 1e-9); // back to 1
    }
    @Test void disease_id_reported() {
        assertEquals("malnutrition",
            new DietTrigger("malnutrition", BAD, 3, 0.05, p -> NutrientTier.NORMAL).diseaseId());
    }

    // --- quit cleanup (PlayerStateCleanup): a departing player's consecutive-malnourished
    // counter must not linger in the map forever ---
    @Test void clear_player_drops_consecutive_counter() {
        DietTrigger t = new DietTrigger("malnutrition", BAD, 3, 0.05, p -> NutrientTier.NORMAL);
        UUID u = UUID.randomUUID();
        t.chanceFor(u, NutrientTier.MALNOURISHED); // 1
        t.chanceFor(u, NutrientTier.MALNOURISHED); // 2

        t.clearPlayer(u);

        // Counter reset to 0, not just "2" retained: two more checks must not yet reach the
        // sustain threshold of 3.
        assertEquals(0.0, t.chanceFor(u, NutrientTier.MALNOURISHED), 1e-9); // back to 1
        assertEquals(0.0, t.chanceFor(u, NutrientTier.MALNOURISHED), 1e-9); // back to 2
    }
}
