package cz.hashiri.harshlands.foodexpansion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NutritionPreviewLayoutTest {

    @Nested
    class DeltaMath {
        @Test void normal_case_returns_raw_times_multiplier() {
            assertEquals(8.0, NutritionPreviewLayout.computeDelta(8.0, 50.0, 1.0));
        }

        @Test void capped_at_100() {
            assertEquals(5.0, NutritionPreviewLayout.computeDelta(8.0, 95.0, 1.0));
        }

        @Test void at_cap_returns_zero() {
            assertEquals(0.0, NutritionPreviewLayout.computeDelta(8.0, 100.0, 1.0));
        }

        @Test void respects_comfort_multiplier() {
            assertEquals(12.0, NutritionPreviewLayout.computeDelta(8.0, 50.0, 1.5));
        }

        @Test void zero_raw_returns_zero() {
            assertEquals(0.0, NutritionPreviewLayout.computeDelta(0.0, 50.0, 1.0));
        }

        @Test void never_negative_when_already_over_cap() {
            // Defensive: if current somehow exceeds 100, delta should not go negative.
            assertEquals(0.0, NutritionPreviewLayout.computeDelta(8.0, 110.0, 1.0));
        }
    }
}
