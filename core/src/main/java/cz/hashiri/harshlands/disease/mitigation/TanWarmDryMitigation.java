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

import cz.hashiri.harshlands.data.HLPlayer;
import org.bukkit.entity.Player;

public final class TanWarmDryMitigation implements Mitigation {
    private final double temperatureAtLeast;

    public TanWarmDryMitigation(double temperatureAtLeast) {
        this.temperatureAtLeast = temperatureAtLeast;
    }

    /** Pure decision, unit-tested. */
    public static boolean isMitigating(double temperature, boolean wet, double temperatureAtLeast) {
        return !wet && temperature >= temperatureAtLeast;
    }

    @Override
    public boolean isActive(Player player) {
        HLPlayer hlPlayer = HLPlayer.getPlayers().get(player.getUniqueId());
        if (hlPlayer == null || hlPlayer.getTanDataModule() == null) {
            return false; // no TAN data -> cannot mitigate via warmth
        }
        double temperature = hlPlayer.getTanDataModule().getTemperature();
        boolean wet = isWet(player);
        return isMitigating(temperature, wet, temperatureAtLeast);
    }

    /** "Wet" = in water, or standing in rain under open sky. */
    private static boolean isWet(Player player) {
        if (player.isInWater()) return true;
        if (!player.getWorld().hasStorm()) return false;
        // exposed to sky at the player's position (rain only falls on the highest blocks)
        return player.getWorld().getHighestBlockYAt(player.getLocation()) <= player.getLocation().getBlockY();
    }
}
