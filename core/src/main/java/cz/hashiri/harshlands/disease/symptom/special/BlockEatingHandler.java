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
package cz.hashiri.harshlands.disease.symptom.special;

import cz.hashiri.harshlands.disease.symptom.SymptomContext;
import cz.hashiri.harshlands.disease.symptom.SymptomHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.Random;

/**
 * Blocks eating/drinking while active (Tetanus stage 2 "locked jaw"). The progression tick
 * {@link #apply marks} the symptom each check (with a TTL that self-heals a missed clear);
 * the consume event enforces it. {@code Chance} param defaults to 1.0 (a full lock).
 */
public final class BlockEatingHandler implements SymptomHandler, Listener {

    public static final String SYMPTOM_KEY = "BlockEating";

    private final SpecialSymptomTracker tracker;
    private final long ttlMs;
    private final Random random = new Random();

    public BlockEatingHandler(SpecialSymptomTracker tracker, long ttlMs) {
        this.tracker = tracker;
        this.ttlMs = ttlMs;
    }

    @Override
    public void apply(Player player, SymptomContext ctx) {
        double chance = ctx.params() != null ? ctx.params().getDouble("Chance", 1.0) : 1.0;
        tracker.mark(player.getUniqueId(), SYMPTOM_KEY, System.currentTimeMillis() + ttlMs, chance);
    }

    @Override
    public void clear(Player player, SymptomContext ctx) {
        tracker.clear(player.getUniqueId(), SYMPTOM_KEY);
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        double chance = tracker.chance(event.getPlayer().getUniqueId(), SYMPTOM_KEY, System.currentTimeMillis());
        if (shouldBlock(chance, random.nextDouble())) {
            event.setCancelled(true);
            feedback(event.getPlayer());
        }
    }

    /** Pure: should this consume attempt be blocked, given the active chance and a roll in [0,1)? */
    public static boolean shouldBlock(double chance, double roll) {
        return chance > 0 && roll < chance;
    }

    private void feedback(Player player) {
        ((net.kyori.adventure.audience.Audience) player).sendActionBar(
            net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                .deserialize("§eYour jaw locks up — you can't eat."));
    }
}
