package cz.hashiri.harshlands.foodexpansion;

/**
 * Pure, testable helpers for building the nutrient preview HUD strip.
 * No Bukkit dependencies; all methods are static and side-effect-free.
 */
public final class NutritionPreviewLayout {

    private NutritionPreviewLayout() {}

    /**
     * Computes the actual macro gain a player would receive by eating the held food right now.
     *
     * @param raw            base macro value from the food's {@link NutrientProfile}
     * @param current        player's current value for this macro
     * @param comfortMult    comfort absorption multiplier (1.0 when no bonus)
     * @return gain in [0.0, 100.0]; 0 when already at cap or beyond
     */
    public static double computeDelta(double raw, double current, double comfortMult) {
        double projected = Math.min(100.0, current + raw * comfortMult);
        return Math.max(0.0, projected - current);
    }
}
