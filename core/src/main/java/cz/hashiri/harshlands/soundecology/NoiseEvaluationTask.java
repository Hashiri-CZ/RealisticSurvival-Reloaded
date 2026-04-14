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

import cz.hashiri.harshlands.utils.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class NoiseEvaluationTask implements Runnable {

    private static final double MAX_SCAN_RADIUS = 48.0;

    private final NoiseManager noiseManager;
    private final Map<UUID, Long> recentlyAssigned = new HashMap<>();
    private final Map<UUID, Long> recentlyTargeted = new HashMap<>();

    private final double baseChance;
    private final int maxPerEvent;
    private final int maxPerCycle;
    private final int cooldownTicks;
    private final double verticalRadius;

    public NoiseEvaluationTask(NoiseManager noiseManager, ConfigurationSection config) {
        this.noiseManager = noiseManager;
        this.baseChance = config.getDouble("MobResponse.BaseChance", 0.3);
        this.maxPerEvent = config.getInt("MobResponse.MaxMobsPerEvent", 3);
        this.maxPerCycle = config.getInt("MobResponse.MaxMobsPerCycle", 20);
        this.cooldownTicks = config.getInt("MobResponse.CooldownTicks", 100);
        this.verticalRadius = config.getDouble("MobResponse.MaxVerticalScanRadius", 24.0);
    }

    @Override
    public void run() {
        if (noiseManager.getActiveEvents().isEmpty()) return;

        World firstWorld = null;
        for (NoiseEvent event : noiseManager.getActiveEvents()) {
            if (event.getLocation().getWorld() != null) {
                firstWorld = event.getLocation().getWorld();
                break;
            }
        }
        long currentTick = firstWorld != null ? firstWorld.getFullTime() : 0;
        noiseManager.pruneExpired(currentTick);

        // Prune expired cooldowns
        recentlyAssigned.entrySet().removeIf(e -> e.getValue() <= currentTick);
        recentlyTargeted.entrySet().removeIf(e -> e.getValue() <= currentTick);

        int totalAssigned = 0;

        for (NoiseEvent event : noiseManager.getActiveEvents()) {
            if (totalAssigned >= maxPerCycle) break;

            Location loc = event.getLocation();
            World world = loc.getWorld();
            if (world == null) continue;

            double radius = event.getEffectiveRadius();
            double scanRadius = Math.min(radius, MAX_SCAN_RADIUS);
            double yRadius = Math.min(scanRadius, verticalRadius);
            Collection<Entity> nearby = world.getNearbyEntities(loc, scanRadius, yRadius, scanRadius,
                    entity -> entity instanceof Monster);

            int assignedForEvent = 0;
            for (Entity entity : nearby) {
                if (totalAssigned >= maxPerCycle || assignedForEvent >= maxPerEvent) break;
                Mob mob = (Mob) entity;
                if (mob.getTarget() != null) {
                    recentlyTargeted.put(mob.getUniqueId(), currentTick + cooldownTicks);
                    continue;
                }
                if (recentlyTargeted.containsKey(mob.getUniqueId())) continue;
                if (recentlyAssigned.containsKey(mob.getUniqueId())) continue;

                double distanceSq = mob.getLocation().distanceSquared(loc);
                double radiusSq = radius * radius;
                if (distanceSq > radiusSq) continue;

                double distance = Math.sqrt(distanceSq);
                double probability = baseChance * (1.0 - distance / radius);
                if (ThreadLocalRandom.current().nextDouble() >= probability) continue;

                if (Utils.assignInvestigateNoiseGoal(mob, loc)) {
                    recentlyAssigned.put(mob.getUniqueId(), currentTick + cooldownTicks);
                    assignedForEvent++;
                    totalAssigned++;
                }
            }

            // Particle feedback to source player when mobs were attracted
            if (assignedForEvent > 0 && event.getSourcePlayer() != null) {
                Player source = Bukkit.getPlayer(event.getSourcePlayer());
                if (source != null && source.isOnline()) {
                    source.spawnParticle(Particle.NOTE, loc.getX(), loc.getY() + 1.5, loc.getZ(), 2, 0.3, 0.2, 0.3);
                }

                // Debug instrumentation
                cz.hashiri.harshlands.debug.DebugManager debugMgr = cz.hashiri.harshlands.HLPlugin.getPlugin().getDebugManager();
                if (debugMgr.isActive("Fear", "SoundEcology", event.getSourcePlayer())) {
                    String chatLine = "§d[Noise] §f" + assignedForEvent + " mobs attracted to noise";
                    String consoleLine = "mobsAssigned=" + assignedForEvent + " radius=" + String.format("%.0f", radius)
                            + " loc=" + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ()
                            + " hostileNearby=" + nearby.size();
                    debugMgr.send("Fear", "SoundEcology", event.getSourcePlayer(), chatLine, consoleLine);
                }
            }
        }
    }
}
