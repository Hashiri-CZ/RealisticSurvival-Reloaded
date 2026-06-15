package cz.hashiri.harshlands.disease.mitigation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TanWarmDryMitigationTest {
    @Test void warm_and_dry_is_mitigating() {
        assertTrue(TanWarmDryMitigation.isMitigating(15.0, false, 12.0));
    }
    @Test void warm_but_wet_is_not_mitigating() {
        assertFalse(TanWarmDryMitigation.isMitigating(15.0, true, 12.0));
    }
    @Test void cold_and_dry_is_not_mitigating() {
        assertFalse(TanWarmDryMitigation.isMitigating(5.0, false, 12.0));
    }
    @Test void exactly_at_threshold_counts_as_warm() {
        assertTrue(TanWarmDryMitigation.isMitigating(12.0, false, 12.0));
    }
}
