package cz.hashiri.harshlands.bodyhealth;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BodyPartStateParseTest {

    @Test void parses_each_documented_value() {
        assertEquals(BodyPartState.FULL,         BodyPartState.fromPlaceholder("FULL"));
        assertEquals(BodyPartState.NEARLY_FULL,  BodyPartState.fromPlaceholder("NEARLYFULL"));
        assertEquals(BodyPartState.INTERMEDIATE, BodyPartState.fromPlaceholder("INTERMEDIATE"));
        assertEquals(BodyPartState.DAMAGED,      BodyPartState.fromPlaceholder("DAMAGED"));
        assertEquals(BodyPartState.BROKEN,       BodyPartState.fromPlaceholder("BROKEN"));
    }

    @Test void is_case_insensitive_and_trims() {
        assertEquals(BodyPartState.DAMAGED, BodyPartState.fromPlaceholder("  damaged  "));
    }

    @Test void null_returns_full() {
        assertEquals(BodyPartState.FULL, BodyPartState.fromPlaceholder(null));
    }

    @Test void empty_returns_full() {
        assertEquals(BodyPartState.FULL, BodyPartState.fromPlaceholder(""));
    }

    @Test void garbage_returns_full() {
        assertEquals(BodyPartState.FULL, BodyPartState.fromPlaceholder("WHATEVER"));
    }

    // -------------------------------------------------------------------------
    // Wire-format / filename contract pin tests
    // -------------------------------------------------------------------------

    @Test void spriteToken_is_lowercase_underscore_form() {
        assertEquals("full",         BodyPartState.FULL.spriteToken());
        assertEquals("nearly_full",  BodyPartState.NEARLY_FULL.spriteToken());
        assertEquals("intermediate", BodyPartState.INTERMEDIATE.spriteToken());
        assertEquals("damaged",      BodyPartState.DAMAGED.spriteToken());
        assertEquals("broken",       BodyPartState.BROKEN.spriteToken());
    }

    @Test void bodyPart_placeholderSuffix_is_lowercase_underscore_form() {
        assertEquals("head",       BodyPart.HEAD.placeholderSuffix());
        assertEquals("torso",      BodyPart.TORSO.placeholderSuffix());
        assertEquals("arm_left",   BodyPart.ARM_LEFT.placeholderSuffix());
        assertEquals("arm_right",  BodyPart.ARM_RIGHT.placeholderSuffix());
        assertEquals("leg_left",   BodyPart.LEG_LEFT.placeholderSuffix());
        assertEquals("leg_right",  BodyPart.LEG_RIGHT.placeholderSuffix());
        assertEquals("foot_left",  BodyPart.FOOT_LEFT.placeholderSuffix());
        assertEquals("foot_right", BodyPart.FOOT_RIGHT.placeholderSuffix());
    }
}
