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
package cz.hashiri.harshlands.disease.mitigation;

import cz.hashiri.harshlands.disease.trigger.DiseaseTrigger;
import org.bukkit.entity.Player;

import java.util.List;

/** Mitigating (hold/regress) when none of the disease's triggers report exposure right now. */
public final class NoExposureMitigation implements Mitigation {

    private final List<DiseaseTrigger> diseaseTriggers;

    public NoExposureMitigation(List<DiseaseTrigger> diseaseTriggers) {
        this.diseaseTriggers = diseaseTriggers;
    }

    /** Pure: mitigating when current total exposure chance is non-positive. */
    public static boolean mitigating(double totalChance) {
        return totalChance <= 0.0;
    }

    @Override
    public boolean isActive(Player player) {
        double sum = 0.0;
        for (DiseaseTrigger t : diseaseTriggers) {
            sum += Math.max(0.0, t.chance(player));
        }
        return mitigating(sum);
    }
}
