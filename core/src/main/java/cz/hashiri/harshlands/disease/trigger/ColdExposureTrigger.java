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

import cz.hashiri.harshlands.data.HLPlayer;
import org.bukkit.entity.Player;

public final class ColdExposureTrigger implements DiseaseTrigger {
    private final String diseaseId;
    private final double temperatureBelow;
    private final boolean requireWet;
    private final double chancePerCheck;

    public ColdExposureTrigger(String diseaseId, double temperatureBelow,
                               boolean requireWet, double chancePerCheck) {
        this.diseaseId = diseaseId;
        this.temperatureBelow = temperatureBelow;
        this.requireWet = requireWet;
        this.chancePerCheck = chancePerCheck;
    }

    @Override public String diseaseId() { return diseaseId; }

    @Override
    public double chance(Player player) {
        HLPlayer hlPlayer = HLPlayer.getPlayers().get(player.getUniqueId());
        if (hlPlayer == null || hlPlayer.getTanDataModule() == null) return 0.0;
        double temperature = hlPlayer.getTanDataModule().getTemperature();
        if (temperature >= temperatureBelow) return 0.0;
        if (requireWet && !player.isInWater() && !player.getWorld().hasStorm()) return 0.0;
        return chancePerCheck;
    }
}
