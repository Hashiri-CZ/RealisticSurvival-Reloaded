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
import cz.hashiri.harshlands.tan.RawWaterDrinkEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dysentery: contraction from drinking raw/unpurified water. Listens for TAN's
 * {@link RawWaterDrinkEvent} and marks a one-shot pending exposure (with a TTL that self-heals
 * a missed read) that {@link #chance(Player)} consumes on the next progression check — keeping
 * the immunity check + immune-suppression multiplier in {@code DiseaseProgressionTask.totalChance}
 * in the loop. Mirrors {@code InfectedItemTrigger}.
 */
public final class UnpurifiedWaterTrigger implements DiseaseTrigger, Listener, PlayerStateCleanup {

    private final String diseaseId;
    private final double chancePerCheck;
    private final long ttlMs;

    // player -> wall-clock ms at which the pending exposure expires.
    private final Map<UUID, Long> pending = new ConcurrentHashMap<>();

    public UnpurifiedWaterTrigger(String diseaseId, double chancePerCheck, long ttlMs) {
        this.diseaseId = diseaseId;
        this.chancePerCheck = chancePerCheck;
        this.ttlMs = ttlMs;
    }

    @Override public String diseaseId() { return diseaseId; }

    /** Record a one-shot pending exposure for the player, expiring at {@code expiryMs}. */
    public void markPending(UUID player, long expiryMs) {
        pending.put(player, expiryMs);
    }

    /** True (and clears the entry) iff a non-expired pending exposure exists at {@code nowMs}. */
    public boolean takePending(UUID player, long nowMs) {
        Long expiry = pending.remove(player);
        return expiry != null && nowMs < expiry;
    }

    @Override
    public double chance(Player player) {
        return takePending(player.getUniqueId(), System.currentTimeMillis()) ? chancePerCheck : 0.0;
    }

    @EventHandler
    public void onRawWaterDrink(RawWaterDrinkEvent event) {
        Player player = event.getPlayer();
        if (player != null) {
            markPending(player.getUniqueId(), System.currentTimeMillis() + ttlMs);
        }
    }

    /** Drop this player's pending exposure (quit cleanup — see {@link PlayerStateCleanup}). */
    @Override
    public void clearPlayer(UUID uuid) {
        pending.remove(uuid);
    }
}
