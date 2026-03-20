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

import cz.hashiri.harshlands.fear.FearModule;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoundEcologyEvents implements Listener {

    private final FearModule fearModule;
    private final NoiseManager noiseManager;
    private final ConfigurationSection config;
    private final Map<UUID, Long> lastMoveNoiseTick = new HashMap<>();

    public SoundEcologyEvents(FearModule fearModule, ConfigurationSection config, NoiseManager noiseManager) {
        this.fearModule = fearModule;
        this.noiseManager = noiseManager;
        this.config = config;
    }

    // -----------------------------------------------------------------------
    // Mining
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (!fearModule.isEnabled(player.getWorld())) return;
        double radius = config.getDouble("NoiseLevels.Mining", 32);
        Location loc = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
        noiseManager.createNoise(loc, radius, player.getUniqueId(), "MINING");
        emitFeedback(player, loc, radius);
    }

    // -----------------------------------------------------------------------
    // Placing
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (!fearModule.isEnabled(player.getWorld())) return;
        double radius = config.getDouble("NoiseLevels.Placing", 12);
        Location loc = event.getBlock().getLocation().add(0.5, 0.5, 0.5);
        noiseManager.createNoise(loc, radius, player.getUniqueId(), "PLACING");
        emitFeedback(player, loc, radius);
    }

    // -----------------------------------------------------------------------
    // Combat (melee + projectile)
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Player player = null;
        String type = "COMBAT";
        double radius = config.getDouble("NoiseLevels.Combat", 48);

        if (damager instanceof Player p) {
            player = p;
        } else if (damager instanceof Arrow arrow && arrow.getShooter() instanceof Player p) {
            player = p;
            type = "BOW";
            radius = config.getDouble("NoiseLevels.Bow", 40);
        } else if (damager instanceof Trident trident && trident.getShooter() instanceof Player p) {
            player = p;
            type = "BOW";
            radius = config.getDouble("NoiseLevels.Bow", 40);
        }

        if (player == null) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (!fearModule.isEnabled(player.getWorld())) return;

        Location loc = event.getEntity().getLocation();
        noiseManager.createNoise(loc, radius, player.getUniqueId(), type);
        emitFeedback(player, loc, radius);
    }

    // -----------------------------------------------------------------------
    // Chest / Door interaction
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (!fearModule.isEnabled(player.getWorld())) return;

        Material mat = event.getClickedBlock().getType();
        BlockState state = event.getClickedBlock().getState();
        boolean isContainer = state instanceof Container;
        boolean isDoor = Tag.DOORS.isTagged(mat) || Tag.TRAPDOORS.isTagged(mat)
                || Tag.FENCE_GATES.isTagged(mat);

        if (!isContainer && !isDoor) return;

        double radius = config.getDouble("NoiseLevels.ChestDoor", 20);
        Location loc = event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5);
        noiseManager.createNoise(loc, radius, player.getUniqueId(), "CHEST_DOOR");
        emitFeedback(player, loc, radius);
    }

    // -----------------------------------------------------------------------
    // Explosions
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!fearModule.isEnabled(event.getEntity().getWorld())) return;
        double radius = config.getDouble("NoiseLevels.Explosion", 80);
        Location loc = event.getLocation();
        noiseManager.createNoise(loc, radius, null, "EXPLOSION");
    }

    // -----------------------------------------------------------------------
    // Movement (walk / sprint / sneak)
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (!fearModule.isEnabled(player.getWorld())) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;

        // Only fire when block position changes
        if (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;

        // Throttle: at least 20 ticks between move noises per player
        long currentTick = player.getWorld().getFullTime();
        Long lastTick = lastMoveNoiseTick.get(player.getUniqueId());
        if (lastTick != null && currentTick - lastTick < 20) return;
        lastMoveNoiseTick.put(player.getUniqueId(), currentTick);

        double radius;
        String type;
        if (player.isSneaking()) {
            radius = config.getDouble("NoiseLevels.Sneaking", 2);
            type = "SNEAK";
        } else if (player.isSprinting()) {
            radius = config.getDouble("NoiseLevels.Sprinting", 16);
            type = "SPRINT";
        } else {
            radius = config.getDouble("NoiseLevels.Walking", 8);
            type = "WALK";
        }

        noiseManager.createNoise(to, radius, player.getUniqueId(), type);
        emitFeedback(player, to, radius);
    }

    // -----------------------------------------------------------------------
    // Cleanup
    // -----------------------------------------------------------------------

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        lastMoveNoiseTick.remove(event.getPlayer().getUniqueId());
    }

    // -----------------------------------------------------------------------
    // Particle feedback
    // -----------------------------------------------------------------------

    private void emitFeedback(Player source, Location loc, double radius) {
        if (!config.getBoolean("Feedback.Particles.Enabled", true)) return;
        double minRadius = config.getDouble("Feedback.Particles.MinRadiusToShow", 16);
        if (radius < minRadius) return;

        double scanRadius = config.getDouble("Feedback.Particles.ScanRadius", 20);
        Location particleLoc = loc.clone().add(0, 1.5, 0);
        for (Entity entity : loc.getWorld().getNearbyEntities(loc, scanRadius, scanRadius, scanRadius)) {
            if (!(entity instanceof Player nearby)) continue;
            if (nearby.getUniqueId().equals(source.getUniqueId())) continue;
            nearby.spawnParticle(Particle.NOTE, particleLoc, 3, 0.5, 0.3, 0.5, 0);
        }
    }
}
