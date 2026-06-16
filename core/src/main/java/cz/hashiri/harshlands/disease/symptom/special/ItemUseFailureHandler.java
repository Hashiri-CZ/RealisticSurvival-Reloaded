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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

import java.util.Random;

/** Chance to cancel item use while active (Endersion). Tick marks; events enforce. */
public final class ItemUseFailureHandler implements SymptomHandler, Listener {

    public static final String SYMPTOM_KEY = "ItemUseFailure";

    private final SpecialSymptomTracker tracker;
    private final long ttlMs;
    private final Random random = new Random();

    public ItemUseFailureHandler(SpecialSymptomTracker tracker, long ttlMs) {
        this.tracker = tracker;
        this.ttlMs = ttlMs;
    }

    @Override
    public void apply(Player player, SymptomContext ctx) {
        double chance = ctx.params() != null ? ctx.params().getDouble("Chance", 0.25) : 0.25;
        tracker.mark(player.getUniqueId(), SYMPTOM_KEY, System.currentTimeMillis() + ttlMs, chance);
    }

    @Override
    public void clear(Player player, SymptomContext ctx) {
        tracker.clear(player.getUniqueId(), SYMPTOM_KEY);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null) return;
        if (rollFails(event.getPlayer())) {
            event.setCancelled(true);
            feedback(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (rollFails(event.getPlayer())) {
            event.setCancelled(true);
            feedback(event.getPlayer());
        }
    }

    private boolean rollFails(Player player) {
        long now = System.currentTimeMillis();
        double chance = tracker.chance(player.getUniqueId(), SYMPTOM_KEY, now);
        return chance > 0 && random.nextDouble() < chance;
    }

    private void feedback(Player player) {
        ((net.kyori.adventure.audience.Audience) player).sendActionBar(
            net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                .deserialize("§5Your hands won't cooperate..."));
    }
}
