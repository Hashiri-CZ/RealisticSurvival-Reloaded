package cz.hashiri.harshlands.utils;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PendingReshowTest {

    @Test void mark_returns_true_when_first_marked() {
        PendingReshow state = new PendingReshow();
        UUID p = UUID.randomUUID();
        assertTrue(state.markDirty(p));
    }

    @Test void mark_returns_false_when_already_marked() {
        PendingReshow state = new PendingReshow();
        UUID p = UUID.randomUUID();
        state.markDirty(p);
        assertFalse(state.markDirty(p));
    }

    @Test void clear_resets_to_clean() {
        PendingReshow state = new PendingReshow();
        UUID p = UUID.randomUUID();
        state.markDirty(p);
        state.clear(p);
        assertTrue(state.markDirty(p));
    }

    @Test void distinct_players_are_independent() {
        PendingReshow state = new PendingReshow();
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        assertTrue(state.markDirty(a));
        assertTrue(state.markDirty(b));
    }
}
