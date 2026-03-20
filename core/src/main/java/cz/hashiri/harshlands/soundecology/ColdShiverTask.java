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
package cz.hashiri.harshlands.soundecology;

import cz.hashiri.harshlands.data.HLPlayer;
import cz.hashiri.harshlands.data.toughasnails.DataModule;
import cz.hashiri.harshlands.fear.FearModule;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ColdShiverTask implements Runnable {

    private final NoiseManager noiseManager;
    private final ConfigurationSection config;
    private final FearModule fearModule;

    public ColdShiverTask(NoiseManager noiseManager, ConfigurationSection config, FearModule fearModule) {
        this.noiseManager = noiseManager;
        this.config = config;
        this.fearModule = fearModule;
    }

    @Override
    public void run() {
        double threshold = config.getDouble("Integration.ColdShivering.TemperatureThreshold", 4.0);
        double chance = config.getDouble("Integration.ColdShivering.Chance", 0.3);
        double radius = config.getDouble("NoiseLevels.Shivering", 6);

        for (HLPlayer hlPlayer : new ArrayList<>(HLPlayer.getPlayers().values())) {
            Player player = hlPlayer.getPlayer();
            if (player == null || !player.isOnline()) continue;
            if (player.getGameMode() == org.bukkit.GameMode.CREATIVE || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
            if (!fearModule.isEnabled(player.getWorld())) continue;

            DataModule tanDm = hlPlayer.getTanDataModule();
            if (tanDm == null) continue;

            if (tanDm.getTemperature() < threshold) {
                if (ThreadLocalRandom.current().nextDouble() < chance) {
                    noiseManager.createNoise(player.getLocation(), radius, player.getUniqueId(), "SHIVERING");
                }
            }
        }
    }
}
