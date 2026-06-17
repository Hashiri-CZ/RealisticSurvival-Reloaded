package cz.hashiri.harshlands.disease.mitigation;

import cz.hashiri.harshlands.disease.trigger.NutrientTierReader;
import cz.hashiri.harshlands.foodexpansion.NutrientTier;
import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class DietMitigationTest {
    private static final Set<NutrientTier> BAD =
        EnumSet.of(NutrientTier.STARVING, NutrientTier.SEVERELY_MALNOURISHED, NutrientTier.MALNOURISHED);

    @Test void mitigating_when_well_fed() {
        NutrientTierReader fed = p -> NutrientTier.WELL_NOURISHED;
        assertTrue(new DietMitigation(BAD, fed).isActive(null));
    }
    @Test void mitigating_when_normal() {
        // NORMAL is also the value NutritionTierReader returns when FoodExpansion is absent,
        // so this also locks in the graceful-degradation contract: no FoodExpansion -> well-fed -> mitigating.
        NutrientTierReader normal = p -> NutrientTier.NORMAL;
        assertTrue(new DietMitigation(BAD, normal).isActive(null));
    }
    @Test void not_mitigating_when_malnourished() {
        NutrientTierReader hungry = p -> NutrientTier.MALNOURISHED;
        assertFalse(new DietMitigation(BAD, hungry).isActive(null));
    }
}
