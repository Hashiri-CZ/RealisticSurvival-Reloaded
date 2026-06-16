package cz.hashiri.harshlands.disease.trigger;

import cz.hashiri.harshlands.bodyhealth.BodyPartState;
import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LimbInjuryTriggerTest {
    private static final Set<BodyPartState> INJURED =
        EnumSet.of(BodyPartState.DAMAGED, BodyPartState.BROKEN);

    @Test void injured_true_for_listed_state() {
        assertTrue(LimbInjuryTrigger.injured(BodyPartState.BROKEN, INJURED));
    }
    @Test void injured_false_for_healthy_state() {
        assertFalse(LimbInjuryTrigger.injured(BodyPartState.NEARLY_FULL, INJURED));
    }
    @Test void injured_false_for_null() {
        assertFalse(LimbInjuryTrigger.injured(null, INJURED));
    }
    @Test void worst_severity_picks_highest_ordinal() {
        assertEquals(BodyPartState.BROKEN, LimbInjuryTrigger.worstSeverity(
            List.of(BodyPartState.FULL, BodyPartState.DAMAGED, BodyPartState.BROKEN)));
    }
    @Test void worst_severity_empty_is_full() {
        assertEquals(BodyPartState.FULL, LimbInjuryTrigger.worstSeverity(List.of()));
    }
    @Test void worst_severity_null_is_full() {
        assertEquals(BodyPartState.FULL, LimbInjuryTrigger.worstSeverity(null));
    }
    @Test void parse_states_maps_known_and_skips_unknown() {
        Set<BodyPartState> s = LimbInjuryTrigger.parseStates(List.of("damaged", "broken", "bogus"));
        assertEquals(EnumSet.of(BodyPartState.DAMAGED, BodyPartState.BROKEN), s);
    }
    @Test void chance_is_value_when_injured_else_zero() {
        LimbInjuryTrigger hurt = new LimbInjuryTrigger("festering_wound", INJURED, 0.05, p -> BodyPartState.BROKEN);
        LimbInjuryTrigger fine = new LimbInjuryTrigger("festering_wound", INJURED, 0.05, p -> BodyPartState.FULL);
        assertEquals(0.05, hurt.chance(null), 1e-9);
        assertEquals(0.0, fine.chance(null), 1e-9);
    }
    @Test void disease_id_reported() {
        assertEquals("festering_wound",
            new LimbInjuryTrigger("festering_wound", INJURED, 0.05, p -> BodyPartState.FULL).diseaseId());
    }
}
