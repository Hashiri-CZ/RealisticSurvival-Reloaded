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
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;

/** Suppresses natural (food-driven) health regen while active (Crimson, later Malnutrition). */
public final class BlockNaturalRegenHandler implements SymptomHandler, Listener {

    public static final String SYMPTOM_KEY = "BlockNaturalRegen";

    private final SpecialSymptomTracker tracker;
    private final long ttlMs;

    public BlockNaturalRegenHandler(SpecialSymptomTracker tracker, long ttlMs) {
        this.tracker = tracker;
        this.ttlMs = ttlMs;
    }

    @Override
    public void apply(Player player, SymptomContext ctx) {
        tracker.mark(player.getUniqueId(), SYMPTOM_KEY, System.currentTimeMillis() + ttlMs, 1.0);
    }

    @Override
    public void clear(Player player, SymptomContext ctx) {
        tracker.clear(player.getUniqueId(), SYMPTOM_KEY);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRegain(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        // Only suppress passive regen, never potion/instant-heal sources.
        if (event.getRegainReason() != RegainReason.SATIATED
            && event.getRegainReason() != RegainReason.REGEN) return;
        if (tracker.isActive(player.getUniqueId(), SYMPTOM_KEY, System.currentTimeMillis())) {
            event.setCancelled(true);
        }
    }
}
