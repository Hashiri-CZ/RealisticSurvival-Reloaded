package cz.hashiri.harshlands.disease.mitigation;

import cz.hashiri.harshlands.bodyhealth.BodyPartState;
import cz.hashiri.harshlands.disease.trigger.LimbStateReader;
import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class InjuryHealMitigationTest {
    private static final Set<BodyPartState> INJURED =
        EnumSet.of(BodyPartState.DAMAGED, BodyPartState.BROKEN);

    @Test void mitigating_when_limbs_healthy() {
        LimbStateReader healthy = p -> BodyPartState.NEARLY_FULL;
        assertTrue(new InjuryHealMitigation(INJURED, healthy).isActive(null));
    }
    @Test void not_mitigating_when_a_limb_is_injured() {
        LimbStateReader hurt = p -> BodyPartState.DAMAGED;
        assertFalse(new InjuryHealMitigation(INJURED, hurt).isActive(null));
    }
    @Test void mitigating_when_reader_reports_full() {
        // BodyHealth/PAPI absent -> reader returns FULL -> treated -> mitigating.
        LimbStateReader absent = p -> BodyPartState.FULL;
        assertTrue(new InjuryHealMitigation(INJURED, absent).isActive(null));
    }
}
