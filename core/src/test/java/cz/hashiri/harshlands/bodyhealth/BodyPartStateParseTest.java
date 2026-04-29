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
}
