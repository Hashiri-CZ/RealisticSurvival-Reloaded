package cz.hashiri.harshlands.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AnchorRegistryTest {

    private AnchorRegistry registry;
    private UUID player;
    private UUID inflightUuid;

    @BeforeEach
    void setUp() {
        registry = new AnchorRegistry();
        player = UUID.randomUUID();
        inflightUuid = UUID.randomUUID();
    }

    @Nested
    class GetAnchor {
        @Test void unknown_player_returns_empty() {
            assertTrue(registry.getAnchor(player).isEmpty());
        }
    }

    @Nested
    class MarkPending {
        @Test void no_capture_until_first_consume() {
            registry.markPending(player);
            assertTrue(registry.getAnchor(player).isEmpty());
        }
    }

    @Nested
    class TryConsumeMarker {
        @Test void returns_true_and_stores_uuid_when_marker_pending() {
            registry.markPending(player);
            assertTrue(registry.tryConsumeMarker(player, inflightUuid));
            assertEquals(inflightUuid, registry.getAnchor(player).orElseThrow());
        }

        @Test void returns_false_when_no_marker_pending() {
            assertFalse(registry.tryConsumeMarker(player, inflightUuid));
            assertTrue(registry.getAnchor(player).isEmpty());
        }

        @Test void second_consume_returns_false() {
            registry.markPending(player);
            assertTrue(registry.tryConsumeMarker(player, inflightUuid));
            UUID secondAttempt = UUID.randomUUID();
            assertFalse(registry.tryConsumeMarker(player, secondAttempt));
            assertEquals(inflightUuid, registry.getAnchor(player).orElseThrow());
        }
    }

    @Nested
    class IsAnchor {
        @Test void true_for_stored_anchor() {
            registry.markPending(player);
            registry.tryConsumeMarker(player, inflightUuid);
            assertTrue(registry.isAnchor(player, inflightUuid));
        }

        @Test void false_for_unknown_uuid() {
            registry.markPending(player);
            registry.tryConsumeMarker(player, inflightUuid);
            assertFalse(registry.isAnchor(player, UUID.randomUUID()));
        }

        @Test void false_when_no_anchor_recorded() {
            assertFalse(registry.isAnchor(player, inflightUuid));
        }
    }

    @Nested
    class Clear {
        @Test void removes_anchor_and_marker() {
            registry.markPending(player);
            registry.tryConsumeMarker(player, inflightUuid);
            registry.clear(player);
            assertTrue(registry.getAnchor(player).isEmpty());
            assertFalse(registry.tryConsumeMarker(player, UUID.randomUUID()));
        }
    }
}
