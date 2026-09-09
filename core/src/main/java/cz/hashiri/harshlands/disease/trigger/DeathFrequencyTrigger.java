/*
    Copyright (C) 2026  Hashiri_

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cz.hashiri.harshlands.disease.trigger;

import cz.hashiri.harshlands.disease.PlayerStateCleanup;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Battle Trauma: contraction risk while a player has died at least {@code deathThreshold} times
 * within a rolling {@code windowMs} window. Death timestamps are session-only (a short rolling
 * window — deaths across a restart are not meaningful here). {@code chance()} is a non-destructive
 * read: it prunes only expired timestamps, never the current ones, so pairing this trigger with the
 * {@code NO_EXPOSURE} mitigation is safe (deaths age out → chance drops to 0 → disease regresses).
 */
public final class DeathFrequencyTrigger implements DiseaseTrigger, Listener, PlayerStateCleanup {

    private final String diseaseId;
    private final int deathThreshold;
    private final long windowMs;
    private final double chancePerCheck;

    // Main-thread only: onDeath (sync PlayerDeathEvent) and chanceFor (sync progression task) both
    // run on the server thread, so the inner ArrayLists need no extra synchronization. The
    // ConcurrentHashMap matches the sibling triggers' per-player map convention.
    private final Map<UUID, List<Long>> deaths = new ConcurrentHashMap<>();

    public DeathFrequencyTrigger(String diseaseId, int deathThreshold, long windowMs, double chancePerCheck) {
        this.diseaseId = diseaseId;
        this.deathThreshold = deathThreshold;
        this.windowMs = windowMs;
        this.chancePerCheck = chancePerCheck;
    }

    @Override public String diseaseId() { return diseaseId; }

    /** Pure: are there enough deaths in the window to risk contraction? */
    public static boolean enoughDeaths(int count, int threshold) {
        return count >= threshold;
    }

    /** Record a death for the player at {@code nowMs}. */
    public void recordDeath(UUID player, long nowMs) {
        deaths.computeIfAbsent(player, k -> new ArrayList<>()).add(nowMs);
    }

    /**
     * Prune deaths older than the window and return this check's contraction chance — non-destructive
     * of in-window deaths. {@code chancePerCheck} once at least {@code deathThreshold} remain, else 0.
     */
    public double chanceFor(UUID player, long nowMs) {
        List<Long> times = deaths.get(player);
        if (times == null) return 0.0;
        long cutoff = nowMs - windowMs;
        times.removeIf(t -> t <= cutoff);
        if (times.isEmpty()) {
            deaths.remove(player);
            return 0.0;
        }
        return enoughDeaths(times.size(), deathThreshold) ? chancePerCheck : 0.0;
    }

    @Override
    public double chance(Player player) {
        return chanceFor(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        recordDeath(event.getEntity().getUniqueId(), System.currentTimeMillis());
    }

    /** Drop this player's death history entirely (quit cleanup — see {@link PlayerStateCleanup}). */
    @Override
    public void clearPlayer(UUID uuid) {
        deaths.remove(uuid);
    }
}
