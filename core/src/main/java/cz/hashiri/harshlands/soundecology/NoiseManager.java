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
import cz.hashiri.harshlands.fear.FearModule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NoiseManager {

    private final List<NoiseEvent> activeEvents = new ArrayList<>();
    private final ConfigurationSection config;
    private final FearModule fearModule;

    public NoiseManager(ConfigurationSection config, @Nonnull FearModule fearModule) {
        this.config = config;
        this.fearModule = fearModule;
    }

    public void createNoise(Location location, double baseRadius, @Nullable UUID player, String type) {
        int maxEvents = config.getInt("MaxActiveNoiseEvents", 500);
        while (activeEvents.size() >= maxEvents) {
            activeEvents.removeFirst();
        }

        double effectiveRadius = baseRadius;
        World world = location.getWorld();
        if (world == null) return;

        // Cave check: below highest block or in Nether/End
        boolean enclosed = location.getBlockY() < world.getHighestBlockYAt(
                location.getBlockX(), location.getBlockZ());
        if (enclosed || world.getEnvironment() == World.Environment.NETHER
                     || world.getEnvironment() == World.Environment.THE_END) {
            effectiveRadius *= config.getDouble("Environment.CaveBonusMultiplier", 1.25);
        }

        // Wool dampening: scan 7x7x7 cube around source (skip for quiet noises)
        double woolDampeningFactor = config.getDouble("Environment.WoolDampeningFactor", 0.5);
        double woolMinRadius = config.getDouble("Environment.WoolMinRadius", 16);
        if (baseRadius >= woolMinRadius) {
            int woolCount = 0;
            int threshold = config.getInt("Environment.WoolThreshold", 3);
            Block center = location.getBlock();
            for (int dx = -3; dx <= 3 && woolCount < threshold; dx++) {
                for (int dy = -3; dy <= 3 && woolCount < threshold; dy++) {
                    for (int dz = -3; dz <= 3 && woolCount < threshold; dz++) {
                        Material mat = center.getRelative(dx, dy, dz).getType();
                        if (isDampeningMaterial(mat)) {
                            woolCount++;
                        }
                    }
                }
            }
            if (woolCount >= threshold) {
                effectiveRadius *= woolDampeningFactor;
            }
        }

        // Fear amplification
        if (config.getBoolean("Integration.FearAmplification.Enabled", true) && player != null) {
            HLPlayer hlPlayer = HLPlayer.getPlayers().get(player);
            if (hlPlayer != null) {
                cz.hashiri.harshlands.data.fear.DataModule fearDm = hlPlayer.getFearDataModule();
                if (fearDm != null) {
                    double fearLevel = fearDm.getFearLevel();
                    double minFear = config.getDouble("Integration.FearAmplification.MinFear", 50);
                    double maxMultiplier = config.getDouble("Integration.FearAmplification.MaxMultiplier", 1.5);
                    if (fearLevel > minFear) {
                        double multiplier = 1.0 + (fearLevel - minFear) / 100.0;
                        multiplier = Math.min(multiplier, maxMultiplier);
                        effectiveRadius *= multiplier;
                    }
                }
            }
        }

        int decayTicks = config.getInt("Decay.Seconds", 4) * 20;
        long expiration = world.getFullTime() + decayTicks;
        activeEvents.add(new NoiseEvent(location, baseRadius, effectiveRadius, expiration, player, type));
    }

    public void pruneExpired(long currentTick) {
        activeEvents.removeIf(e -> e.isExpired(currentTick) || e.getLocation().getWorld() == null);
    }

    public List<NoiseEvent> getActiveEvents() {
        return Collections.unmodifiableList(activeEvents);
    }

    public void clear() {
        activeEvents.clear();
    }

    private static boolean isDampeningMaterial(Material mat) {
        if (Tag.WOOL.isTagged(mat) || Tag.WOOL_CARPETS.isTagged(mat)) {
            return true;
        }
        return mat == Material.SNOW || mat == Material.SNOW_BLOCK || mat == Material.POWDER_SNOW;
    }
}
