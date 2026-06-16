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

public final class DamageOverTimeHandler implements SymptomHandler {
    @Override
    public void apply(Player player, SymptomContext ctx) {
        if (ctx.params() == null) return;
        double amount = ctx.params().getDouble("Amount", 1.0);
        if (amount <= 0) return;
        player.damage(amount);
    }
}
