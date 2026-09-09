package cz.hashiri.harshlands.disease.trigger;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import cz.hashiri.harshlands.disease.PlayerStateCleanup;
import org.bukkit.event.player.PlayerQuitEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression coverage for the "no player-quit cleanup anywhere in the disease package" bug:
 * every registered {@link PlayerStateCleanup} target must have {@code clearPlayer} invoked with
 * the quitting player's UUID, and only that player's UUID.
 */
class PlayerQuitCleanupListenerTest {

    private static final class RecordingCleanup implements PlayerStateCleanup {
        final List<UUID> cleared = new ArrayList<>();
        @Override public void clearPlayer(UUID uuid) { cleared.add(uuid); }
    }

    private ServerMock server;
    private PlayerMock player;

    @BeforeEach void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
    }

    @AfterEach void tearDown() {
        MockBukkit.unmock();
    }

    @Test void quit_clears_every_registered_target_with_the_quitting_players_uuid() {
        RecordingCleanup a = new RecordingCleanup();
        RecordingCleanup b = new RecordingCleanup();
        PlayerQuitCleanupListener listener = new PlayerQuitCleanupListener(List.of(a, b));

        listener.onQuit(new PlayerQuitEvent(player, (String) null));

        assertEquals(List.of(player.getUniqueId()), a.cleared);
        assertEquals(List.of(player.getUniqueId()), b.cleared);
    }

    @Test void no_registered_targets_is_a_no_op() {
        PlayerQuitCleanupListener listener = new PlayerQuitCleanupListener(List.of());
        assertDoesNotThrow(() -> listener.onQuit(new PlayerQuitEvent(player, (String) null)));
    }
}
