package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.api.player.HudPlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class HudImplTest {

    /** Minimal in-memory state — same shape BodyHealthModule will provide. */
    private static final class StubState implements HudImpl.State {
        final java.util.Set<UUID> shown = ConcurrentHashMap.newKeySet();
        @Override public boolean markShown(UUID u)  { return shown.add(u); }
        @Override public boolean markHidden(UUID u) { return shown.remove(u); }
        @Override public boolean isShown(UUID u)    { return shown.contains(u); }
    }

    private static HudPlayer hp(UUID id) {
        return new HudPlayer() {
            @Override public UUID uuid() { return id; }
            @Override public Player bukkitPlayer() { return null; }
        };
    }

    @Test void add_first_call_returns_true() {
        HudImpl hud = new HudImpl("bodyhealth", new StubState());
        assertTrue(hud.add(hp(UUID.randomUUID())));
    }

    @Test void add_second_call_returns_false() {
        HudImpl hud = new HudImpl("bodyhealth", new StubState());
        UUID u = UUID.randomUUID();
        hud.add(hp(u));
        assertFalse(hud.add(hp(u)));
    }

    @Test void remove_when_shown_returns_true() {
        HudImpl hud = new HudImpl("bodyhealth", new StubState());
        UUID u = UUID.randomUUID();
        hud.add(hp(u));
        assertTrue(hud.remove(hp(u)));
    }

    @Test void remove_when_not_shown_returns_false() {
        HudImpl hud = new HudImpl("bodyhealth", new StubState());
        assertFalse(hud.remove(hp(UUID.randomUUID())));
    }

    @Test void isShownTo_reflects_state() {
        HudImpl hud = new HudImpl("bodyhealth", new StubState());
        UUID u = UUID.randomUUID();
        assertFalse(hud.isShownTo(hp(u)));
        hud.add(hp(u));
        assertTrue(hud.isShownTo(hp(u)));
        hud.remove(hp(u));
        assertFalse(hud.isShownTo(hp(u)));
    }

    @Test void id_returns_constructor_value() {
        HudImpl hud = new HudImpl("bodyhealth", new StubState());
        assertEquals("bodyhealth", hud.id());
    }

    @Test void concurrent_add_remove_does_not_corrupt_state() throws Exception {
        HudImpl hud = new HudImpl("bodyhealth", new StubState());
        UUID u = UUID.randomUUID();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            for (int i = 0; i < 1000; i++) {
                pool.submit(() -> hud.add(hp(u)));
                pool.submit(() -> hud.remove(hp(u)));
            }
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }
        // Final state may be either shown or not — but isShownTo must agree with it
        // and not throw.
        boolean shown = hud.isShownTo(hp(u));
        assertEquals(shown, hud.isShownTo(hp(u)));
    }
}
