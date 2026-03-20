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
package cz.hashiri.harshlands.dynamicsurroundings;

import org.bukkit.block.Biome;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.Set;

/**
 * Evaluates ambient sound categories per-player and plays the appropriate sounds.
 * Called every {@code Ambient.CheckIntervalTicks} ticks by {@link AmbientTask}.
 */
public class AmbientSoundHandler {

    // -----------------------------------------------------------------------
    // Biome sets
    // -----------------------------------------------------------------------

    private static final Set<Biome> COYOTE_BIOMES = Set.of(
            Biome.PLAINS, Biome.SUNFLOWER_PLAINS,
            Biome.SAVANNA, Biome.SAVANNA_PLATEAU, Biome.WINDSWEPT_SAVANNA,
            Biome.DESERT,
            Biome.BADLANDS, Biome.ERODED_BADLANDS, Biome.WOODED_BADLANDS);

    private static final Set<Biome> WOLF_BIOMES = Set.of(
            Biome.TAIGA, Biome.OLD_GROWTH_PINE_TAIGA, Biome.OLD_GROWTH_SPRUCE_TAIGA,
            Biome.GROVE, Biome.SNOWY_TAIGA);

    private static final Set<Biome> SEAGULL_BIOMES = Set.of(
            Biome.BEACH, Biome.STONY_SHORE,
            Biome.OCEAN, Biome.DEEP_OCEAN,
            Biome.COLD_OCEAN, Biome.DEEP_COLD_OCEAN,
            Biome.WARM_OCEAN, Biome.LUKEWARM_OCEAN, Biome.DEEP_LUKEWARM_OCEAN,
            Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN);

    private static final Set<Biome> WHALE_BIOMES = Set.of(
            Biome.OCEAN, Biome.DEEP_OCEAN,
            Biome.COLD_OCEAN, Biome.DEEP_COLD_OCEAN,
            Biome.WARM_OCEAN, Biome.LUKEWARM_OCEAN, Biome.DEEP_LUKEWARM_OCEAN,
            Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN);

    private static final Set<Biome> ICE_BIOMES = Set.of(
            Biome.FROZEN_OCEAN, Biome.DEEP_FROZEN_OCEAN,
            Biome.FROZEN_PEAKS, Biome.SNOWY_SLOPES,
            Biome.ICE_SPIKES, Biome.JAGGED_PEAKS);

    // -----------------------------------------------------------------------

    private final DynamicSurroundingsModule module;
    private final FileConfiguration config;
    private final Random random = new Random();

    public AmbientSoundHandler(DynamicSurroundingsModule module, FileConfiguration config) {
        this.module = module;
        this.config = config;
    }

    /**
     * Evaluates all ambient categories for one player and fires eligible sounds.
     */
    public void evaluate(Player player) {
        if (!module.isEnabled(player.getWorld())) return;

        World world = player.getWorld();
        Location loc = player.getLocation();
        World.Environment env = world.getEnvironment();
        DSPlayerState state = DSPlayerState.getOrCreate(player.getUniqueId());

        // Steve (stomach grumbles / heartbeat) — fires anywhere, 2% chance
        tryAmbient(player, state, "steve", loc);

        // ── Dimension-specific ─────────────────────────────────────────────
        if (env == World.Environment.NETHER) {
            tryAmbient(player, state, "dimension", loc);
            Biome biome = world.getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            if (biome == Biome.SOUL_SAND_VALLEY) {
                tryAmbient(player, state, "soulsand", loc);
            }
            return;
        }

        if (env == World.Environment.THE_END) {
            tryAmbient(player, state, "end", loc);
            return;
        }

        // ── Underwater ─────────────────────────────────────────────────────
        boolean underwater = player.isSwimming()
                || player.getEyeLocation().getBlock().getType() == Material.WATER;
        if (underwater) {
            tryAmbient(player, state, "underwater", loc);
            return;
        }

        // ── Underground ────────────────────────────────────────────────────
        int skyLight = loc.getBlock().getLightFromSky();
        boolean underground = skyLight == 0 && loc.getY() < 40;
        if (underground) {
            tryAmbient(player, state, "underground", loc);
            // Droplets: air at eye level with water/lava within 4 blocks
            if (isAirAtEye(player) && hasFluidNearby(player, 4)) {
                tryAmbient(player, state, "droplets", loc);
            }
            // Miscblocks: solid block directly above the player's head
            Block aboveHead = player.getEyeLocation().getBlock().getRelative(BlockFace.UP);
            if (aboveHead.getType().isSolid()) {
                tryAmbient(player, state, "miscblocks", loc);
            }
            return;
        }

        // ── Outdoor surface (NORMAL world) ─────────────────────────────────
        long time = world.getTime();
        boolean daytime  = time < 12000;
        boolean nighttime = time >= 13000 && time < 23000;
        boolean storming = world.hasStorm();

        Biome biome = world.getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

        if (world.isThundering()) {
            tryAmbient(player, state, "weather", loc);
        }

        boolean firedSpecific = false;

        // Birds: daytime, no storm, sky light ≥ 10
        if (!firedSpecific && daytime && !storming && skyLight >= 10) {
            if (tryAmbient(player, state, "birds", loc)) firedSpecific = true;
        }
        // Owl: nighttime, sky light ≥ 7
        if (!firedSpecific && nighttime && skyLight >= 7) {
            if (tryAmbient(player, state, "owl", loc)) firedSpecific = true;
        }
        // Insects: daytime, warm biome, sky light ≥ 8
        if (!firedSpecific && daytime && skyLight >= 8
                && world.getTemperature(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()) > 0.5) {
            if (tryAmbient(player, state, "insects", loc)) firedSpecific = true;
        }
        // Coyote: nighttime, plains/savanna/desert/badlands
        if (!firedSpecific && nighttime && COYOTE_BIOMES.contains(biome)) {
            if (tryAmbient(player, state, "coyote", loc)) firedSpecific = true;
        }
        // Wolf: nighttime, taiga
        if (!firedSpecific && nighttime && WOLF_BIOMES.contains(biome)) {
            if (tryAmbient(player, state, "wolf", loc)) firedSpecific = true;
        }

        // Seagulls / whale: ocean/beach biomes
        if (SEAGULL_BIOMES.contains(biome)) {
            tryAmbient(player, state, "seagulls", loc);
        }
        if (WHALE_BIOMES.contains(biome)) {
            tryAmbient(player, state, "whale", loc);
        }

        // Ice biomes
        if (ICE_BIOMES.contains(biome)) {
            tryAmbient(player, state, "ice", loc);
        }

        // Stream: Y > 50, water in 3×3 horizontal column, NOT in ocean/beach biomes
        if (loc.getY() > 50 && hasWaterColumnNearby(player, 1) && !SEAGULL_BIOMES.contains(biome)) {
            tryAmbient(player, state, "stream", loc);
        }

        // Village: ≥3 villagers within 32 blocks
        if (hasNearbyVillagers(player, 32, 3)) {
            tryAmbient(player, state, "village", loc);
        }

        // Book: near a bookshelf
        if (hasNearbyBookshelf(player, 3)) {
            tryAmbient(player, state, "book", loc);
        }

        // Outside: fallback when no specific outdoor category fired
        if (!firedSpecific && skyLight >= 8) {
            tryAmbient(player, state, "outside", loc);
        }
    }

    // -----------------------------------------------------------------------
    // Core sound dispatch
    // -----------------------------------------------------------------------

    /**
     * Attempts to play an ambient category sound.
     *
     * @return true if the sound was actually played
     */
    private boolean tryAmbient(Player player, DSPlayerState state, String category, Location loc) {
        ConfigurationSection cat = config.getConfigurationSection("Ambient.Categories." + category);
        if (cat == null || !cat.getBoolean("Enabled", true)) return false;

        double chance = cat.getDouble("Chance", 0.2);
        if (random.nextDouble() >= chance) return false;

        long cooldownMs = cat.getLong("MinCooldownMs", 15000);
        long now = System.currentTimeMillis();
        Long last = state.ambientLastPlayed.get(category);
        if (last != null && now - last < cooldownMs) return false;

        state.ambientLastPlayed.put(category, now);

        String soundKey = cat.getString("Sound", "dynamicsurroundings.ambient." + category);
        float volume = (float) cat.getDouble("Volume", 0.7);
        float pitch  = 0.9f + random.nextFloat() * 0.2f;

        player.playSound(loc, "harshlands:" + soundKey, SoundCategory.AMBIENT, volume, pitch);

        // Debug instrumentation
        cz.hashiri.harshlands.debug.DebugManager debugMgr = cz.hashiri.harshlands.rsv.HLPlugin.getPlugin().getDebugManager();
        if (debugMgr.isActive("DynamicSurroundings", "Ambient", player.getUniqueId())) {
            String consoleLine = "category=" + category + " sound=" + soundKey
                    + " vol=" + String.format("%.1f", volume) + " pitch=" + String.format("%.2f", pitch)
                    + " biome=" + loc.getWorld().getBiome(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
            debugMgr.send("DynamicSurroundings", "Ambient", player.getUniqueId(), "", consoleLine);
        }

        return true;
    }

    // -----------------------------------------------------------------------
    // World-scan helpers
    // -----------------------------------------------------------------------

    private boolean isAirAtEye(Player player) {
        Material mat = player.getEyeLocation().getBlock().getType();
        return mat == Material.AIR || mat == Material.CAVE_AIR;
    }

    private boolean hasFluidNearby(Player player, int radius) {
        Location eye = player.getEyeLocation();
        World world = eye.getWorld();
        int ex = eye.getBlockX(), ey = eye.getBlockY(), ez = eye.getBlockZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    Material mat = world.getBlockAt(ex + dx, ey + dy, ez + dz).getType();
                    if (mat == Material.WATER || mat == Material.LAVA) return true;
                }
            }
        }
        return false;
    }

    private boolean hasWaterColumnNearby(Player player, int radius) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        int px = loc.getBlockX(), py = loc.getBlockY(), pz = loc.getBlockZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (world.getBlockAt(px + dx, py, pz + dz).getType() == Material.WATER) return true;
            }
        }
        return false;
    }

    private boolean hasNearbyVillagers(Player player, double radius, int minCount) {
        long count = player.getWorld()
                .getNearbyEntities(player.getLocation(), radius, radius, radius,
                        e -> e.getType() == EntityType.VILLAGER)
                .stream().limit(minCount).count();
        return count >= minCount;
    }

    private boolean hasNearbyBookshelf(Player player, int radius) {
        Location loc = player.getLocation();
        World world = loc.getWorld();
        int px = loc.getBlockX(), py = loc.getBlockY(), pz = loc.getBlockZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (world.getBlockAt(px + dx, py + dy, pz + dz).getType() == Material.BOOKSHELF) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
