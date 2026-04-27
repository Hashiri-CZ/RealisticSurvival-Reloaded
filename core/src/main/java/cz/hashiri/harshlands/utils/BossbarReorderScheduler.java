/*
    Copyright (C) 2025  Hashiri_
    GPL-3.0-or-later — see InternalsProvider.java for full text.
 */
package cz.hashiri.harshlands.utils;

import cz.hashiri.harshlands.HLPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Coalesces foreign-bossbar-detected signals from the Sentry (Netty thread)
 * into a single Bukkit-main-thread {@code hide(anchor) + show(anchor)} per
 * player per tick. Net effect: anchor is repeatedly pushed to the bottom
 * of the client's bossbar stack so foreign bars render at slot 0..N-1 in
 * their natural order.
 */
public final class BossbarReorderScheduler {

    private final HLPlugin plugin;
    private final PendingReshow pending = new PendingReshow();
    private long lastWarnNanos = 0L;
    private int observedReshowsThisWindow = 0;
    private long windowStartNanos = 0L;

    public BossbarReorderScheduler(HLPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Called by the Sentry on the Netty thread when a foreign ADD is observed.
     * Schedules a debounced reshow on the Bukkit main thread.
     */
    public void requestReshow(UUID playerUuid) {
        if (!pending.markDirty(playerUuid)) return; // already queued this tick
        Bukkit.getScheduler().runTask(plugin, () -> performReshow(playerUuid));
    }

    private void performReshow(UUID playerUuid) {
        try {
            Player player = Bukkit.getPlayer(playerUuid);
            if (player == null || !player.isOnline()) return;
            DisplayTask task = DisplayTask.getTasks().get(playerUuid);
            if (task == null) return;
            BossbarHUD hud = task.getBossbarHud();
            if (hud == null) return;
            hud.hide();
            hud.show();
            recordReshowForRateWarning();
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "BossbarReorderScheduler reshow failed", t);
        } finally {
            pending.clear(playerUuid);
        }
    }

    /** Emits a WARN log at most once per minute if reshow rate exceeds 5/sec sustained. */
    private void recordReshowForRateWarning() {
        long now = System.nanoTime();
        if (windowStartNanos == 0L || now - windowStartNanos > 10_000_000_000L) {
            windowStartNanos = now;
            observedReshowsThisWindow = 1;
            return;
        }
        observedReshowsThisWindow++;
        if (observedReshowsThisWindow > 50 && now - lastWarnNanos > 60_000_000_000L) {
            plugin.getLogger().warning(
                "Bossbar reshow rate exceeded 5/sec over the last 10s. "
                + "Likely a third-party plugin re-creating its bossbar instead of "
                + "calling setProgress(...) — investigate.");
            lastWarnNanos = now;
        }
    }
}
