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
package cz.hashiri.harshlands.disease.symptom.builtin;

import cz.hashiri.harshlands.disease.symptom.SymptomContext;
import cz.hashiri.harshlands.disease.symptom.SymptomHandler;
import org.bukkit.entity.Player;

/**
 * Periodic hunger loss (Dysentery cramps). Each check drains the player's food level by a
 * config {@code Amount}, floored at 0; {@code Amount} defaults to 1 when absent (and the handler
 * is a no-op for {@code Amount <= 0}). No clear() — natural eating restores it; follows
 * {@code DamageOverTimeHandler}'s apply-only shape (SymptomHandler.clear() is a no-op default).
 */
public final class HungerDrainHandler implements SymptomHandler {
    @Override
    public void apply(Player player, SymptomContext ctx) {
        int amount = ctx.params() != null ? ctx.params().getInt("Amount", 1) : 1;
        if (amount <= 0) return;
        player.setFoodLevel(drained(player.getFoodLevel(), amount));
    }

    /** Pure: food level after draining {@code amount}, floored at 0. */
    public static int drained(int current, int amount) {
        return Math.max(0, current - amount);
    }
}
