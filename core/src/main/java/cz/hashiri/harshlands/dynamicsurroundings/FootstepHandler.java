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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.EnumMap;
import java.util.Random;

/**
 * Handles footstep sounds: surface detection, step accumulation, landing, and armor overlay.
 */
public class FootstepHandler {

    // -----------------------------------------------------------------------
    // Surface types
    // -----------------------------------------------------------------------

    private enum SurfaceType {
        GRASS, DIRT, STONE, SAND, GRAVEL, SNOW, WOOD, LOG,
        CONCRETE, GLASS, METALBAR, METALBOX, MUD, WEAK_ICE,
        MARBLE, WATER, LEAVES, BRUSH, DEFAULT
    }

    private record SoundSet(String walk, String run, String sneak, String land) {
        String getVariant(boolean sprinting, boolean sneaking) {
            if (sprinting && run != null) return run;
            if (sneaking && sneak != null) return sneak;
            return walk;
        }
    }

    private enum ArmorTier { NONE, LIGHT, MEDIUM, HEAVY }

    // -----------------------------------------------------------------------
    // Static lookup tables (built once)
    // -----------------------------------------------------------------------

    private static final EnumMap<Material, SurfaceType> MATERIAL_SURFACE_MAP;
    private static final EnumMap<SurfaceType, SoundSet> SOUNDS;

    private static void map(EnumMap<Material, SurfaceType> m, SurfaceType type, Material... materials) {
        for (Material mat : materials) m.put(mat, type);
    }

    static {
        MATERIAL_SURFACE_MAP = new EnumMap<>(Material.class);

        // GRASS
        map(MATERIAL_SURFACE_MAP, SurfaceType.GRASS,
                Material.GRASS_BLOCK, Material.MYCELIUM, Material.PODZOL, Material.DIRT_PATH,
                Material.MOSS_BLOCK, Material.HAY_BLOCK, Material.TARGET,
                Material.SPONGE, Material.WET_SPONGE);

        // DIRT
        map(MATERIAL_SURFACE_MAP, SurfaceType.DIRT,
                Material.DIRT, Material.ROOTED_DIRT, Material.COARSE_DIRT, Material.FARMLAND,
                Material.PACKED_MUD, Material.SOUL_SOIL, Material.DRIED_KELP_BLOCK);

        // STONE
        map(MATERIAL_SURFACE_MAP, SurfaceType.STONE,
                Material.STONE, Material.STONE_SLAB, Material.STONE_STAIRS,
                Material.SMOOTH_STONE, Material.SMOOTH_STONE_SLAB,
                Material.COBBLESTONE, Material.COBBLESTONE_SLAB, Material.COBBLESTONE_STAIRS, Material.COBBLESTONE_WALL,
                Material.MOSSY_COBBLESTONE, Material.MOSSY_COBBLESTONE_SLAB, Material.MOSSY_COBBLESTONE_STAIRS, Material.MOSSY_COBBLESTONE_WALL,
                Material.DEEPSLATE, Material.COBBLED_DEEPSLATE, Material.COBBLED_DEEPSLATE_SLAB, Material.COBBLED_DEEPSLATE_STAIRS, Material.COBBLED_DEEPSLATE_WALL,
                Material.POLISHED_DEEPSLATE, Material.POLISHED_DEEPSLATE_SLAB, Material.POLISHED_DEEPSLATE_STAIRS, Material.POLISHED_DEEPSLATE_WALL,
                Material.DEEPSLATE_BRICKS, Material.DEEPSLATE_BRICK_SLAB, Material.DEEPSLATE_BRICK_STAIRS, Material.DEEPSLATE_BRICK_WALL,
                Material.DEEPSLATE_TILES, Material.DEEPSLATE_TILE_SLAB, Material.DEEPSLATE_TILE_STAIRS, Material.DEEPSLATE_TILE_WALL,
                Material.CHISELED_DEEPSLATE, Material.REINFORCED_DEEPSLATE,
                Material.BLACKSTONE, Material.BLACKSTONE_SLAB, Material.BLACKSTONE_STAIRS, Material.BLACKSTONE_WALL,
                Material.POLISHED_BLACKSTONE, Material.POLISHED_BLACKSTONE_SLAB, Material.POLISHED_BLACKSTONE_STAIRS, Material.POLISHED_BLACKSTONE_WALL,
                Material.POLISHED_BLACKSTONE_BRICKS, Material.POLISHED_BLACKSTONE_BRICK_SLAB, Material.POLISHED_BLACKSTONE_BRICK_STAIRS, Material.POLISHED_BLACKSTONE_BRICK_WALL,
                Material.CHISELED_POLISHED_BLACKSTONE, Material.GILDED_BLACKSTONE,
                Material.BASALT, Material.SMOOTH_BASALT, Material.POLISHED_BASALT,
                Material.ANDESITE, Material.ANDESITE_SLAB, Material.ANDESITE_STAIRS, Material.ANDESITE_WALL,
                Material.POLISHED_ANDESITE, Material.POLISHED_ANDESITE_SLAB, Material.POLISHED_ANDESITE_STAIRS,
                Material.DIORITE, Material.DIORITE_SLAB, Material.DIORITE_STAIRS, Material.DIORITE_WALL,
                Material.POLISHED_DIORITE, Material.POLISHED_DIORITE_SLAB, Material.POLISHED_DIORITE_STAIRS,
                Material.GRANITE, Material.GRANITE_SLAB, Material.GRANITE_STAIRS, Material.GRANITE_WALL,
                Material.POLISHED_GRANITE, Material.POLISHED_GRANITE_SLAB, Material.POLISHED_GRANITE_STAIRS,
                Material.NETHERRACK,
                Material.NETHER_BRICKS, Material.NETHER_BRICK_SLAB, Material.NETHER_BRICK_STAIRS, Material.NETHER_BRICK_WALL, Material.NETHER_BRICK_FENCE,
                Material.CRACKED_NETHER_BRICKS, Material.CHISELED_NETHER_BRICKS,
                Material.RED_NETHER_BRICKS, Material.RED_NETHER_BRICK_SLAB, Material.RED_NETHER_BRICK_STAIRS, Material.RED_NETHER_BRICK_WALL,
                Material.END_STONE, Material.END_STONE_BRICKS, Material.END_STONE_BRICK_SLAB, Material.END_STONE_BRICK_STAIRS, Material.END_STONE_BRICK_WALL,
                Material.TERRACOTTA,
                Material.WHITE_TERRACOTTA, Material.ORANGE_TERRACOTTA, Material.MAGENTA_TERRACOTTA, Material.LIGHT_BLUE_TERRACOTTA,
                Material.YELLOW_TERRACOTTA, Material.LIME_TERRACOTTA, Material.PINK_TERRACOTTA, Material.GRAY_TERRACOTTA,
                Material.LIGHT_GRAY_TERRACOTTA, Material.CYAN_TERRACOTTA, Material.PURPLE_TERRACOTTA, Material.BLUE_TERRACOTTA,
                Material.BROWN_TERRACOTTA, Material.GREEN_TERRACOTTA, Material.RED_TERRACOTTA, Material.BLACK_TERRACOTTA,
                Material.TUFF, Material.TUFF_SLAB, Material.TUFF_STAIRS, Material.TUFF_WALL,
                Material.POLISHED_TUFF, Material.POLISHED_TUFF_SLAB, Material.POLISHED_TUFF_STAIRS, Material.POLISHED_TUFF_WALL,
                Material.TUFF_BRICKS, Material.TUFF_BRICK_SLAB, Material.TUFF_BRICK_STAIRS, Material.TUFF_BRICK_WALL,
                Material.CHISELED_TUFF, Material.CHISELED_TUFF_BRICKS,
                Material.DRIPSTONE_BLOCK,
                Material.STONE_BRICKS, Material.STONE_BRICK_SLAB, Material.STONE_BRICK_STAIRS, Material.STONE_BRICK_WALL,
                Material.CRACKED_STONE_BRICKS, Material.MOSSY_STONE_BRICKS,
                Material.MOSSY_STONE_BRICK_SLAB, Material.MOSSY_STONE_BRICK_STAIRS, Material.MOSSY_STONE_BRICK_WALL,
                Material.CHISELED_STONE_BRICKS,
                Material.QUARTZ_BLOCK, Material.QUARTZ_SLAB, Material.QUARTZ_STAIRS, Material.QUARTZ_PILLAR,
                Material.CHISELED_QUARTZ_BLOCK, Material.SMOOTH_QUARTZ, Material.SMOOTH_QUARTZ_SLAB, Material.SMOOTH_QUARTZ_STAIRS,
                Material.PURPUR_BLOCK, Material.PURPUR_SLAB, Material.PURPUR_STAIRS, Material.PURPUR_PILLAR,
                Material.PRISMARINE, Material.PRISMARINE_SLAB, Material.PRISMARINE_STAIRS, Material.PRISMARINE_WALL,
                Material.PRISMARINE_BRICKS, Material.PRISMARINE_BRICK_SLAB, Material.PRISMARINE_BRICK_STAIRS,
                Material.DARK_PRISMARINE, Material.DARK_PRISMARINE_SLAB, Material.DARK_PRISMARINE_STAIRS,
                Material.MUD_BRICKS, Material.MUD_BRICK_SLAB, Material.MUD_BRICK_STAIRS, Material.MUD_BRICK_WALL,
                Material.OBSIDIAN, Material.CRYING_OBSIDIAN,
                Material.MAGMA_BLOCK, Material.BONE_BLOCK);

        // SAND
        map(MATERIAL_SURFACE_MAP, SurfaceType.SAND,
                Material.SAND, Material.RED_SAND, Material.SUSPICIOUS_SAND, Material.SOUL_SAND,
                Material.WHITE_CONCRETE_POWDER, Material.ORANGE_CONCRETE_POWDER, Material.MAGENTA_CONCRETE_POWDER,
                Material.LIGHT_BLUE_CONCRETE_POWDER, Material.YELLOW_CONCRETE_POWDER, Material.LIME_CONCRETE_POWDER,
                Material.PINK_CONCRETE_POWDER, Material.GRAY_CONCRETE_POWDER, Material.LIGHT_GRAY_CONCRETE_POWDER,
                Material.CYAN_CONCRETE_POWDER, Material.PURPLE_CONCRETE_POWDER, Material.BLUE_CONCRETE_POWDER,
                Material.BROWN_CONCRETE_POWDER, Material.GREEN_CONCRETE_POWDER, Material.RED_CONCRETE_POWDER,
                Material.BLACK_CONCRETE_POWDER);

        // GRAVEL
        map(MATERIAL_SURFACE_MAP, SurfaceType.GRAVEL,
                Material.GRAVEL, Material.SUSPICIOUS_GRAVEL);

        // SNOW
        map(MATERIAL_SURFACE_MAP, SurfaceType.SNOW,
                Material.SNOW_BLOCK, Material.POWDER_SNOW, Material.SNOW);

        // WOOD planks, slabs, stairs
        map(MATERIAL_SURFACE_MAP, SurfaceType.WOOD,
                Material.OAK_PLANKS, Material.SPRUCE_PLANKS, Material.BIRCH_PLANKS, Material.JUNGLE_PLANKS,
                Material.ACACIA_PLANKS, Material.DARK_OAK_PLANKS, Material.MANGROVE_PLANKS,
                Material.CHERRY_PLANKS, Material.BAMBOO_PLANKS, Material.BAMBOO_MOSAIC,
                Material.CRIMSON_PLANKS, Material.WARPED_PLANKS,
                Material.OAK_SLAB, Material.SPRUCE_SLAB, Material.BIRCH_SLAB, Material.JUNGLE_SLAB,
                Material.ACACIA_SLAB, Material.DARK_OAK_SLAB, Material.MANGROVE_SLAB,
                Material.CHERRY_SLAB, Material.BAMBOO_SLAB, Material.BAMBOO_MOSAIC_SLAB,
                Material.CRIMSON_SLAB, Material.WARPED_SLAB,
                Material.OAK_STAIRS, Material.SPRUCE_STAIRS, Material.BIRCH_STAIRS, Material.JUNGLE_STAIRS,
                Material.ACACIA_STAIRS, Material.DARK_OAK_STAIRS, Material.MANGROVE_STAIRS,
                Material.CHERRY_STAIRS, Material.BAMBOO_STAIRS, Material.BAMBOO_MOSAIC_STAIRS,
                Material.CRIMSON_STAIRS, Material.WARPED_STAIRS,
                Material.OAK_PRESSURE_PLATE, Material.SPRUCE_PRESSURE_PLATE, Material.BIRCH_PRESSURE_PLATE,
                Material.JUNGLE_PRESSURE_PLATE, Material.ACACIA_PRESSURE_PLATE, Material.DARK_OAK_PRESSURE_PLATE,
                Material.MANGROVE_PRESSURE_PLATE, Material.CRIMSON_PRESSURE_PLATE, Material.WARPED_PRESSURE_PLATE,
                Material.CRAFTING_TABLE, Material.BARREL, Material.CHEST, Material.TRAPPED_CHEST,
                Material.BOOKSHELF, Material.LECTERN);

        // LOG: logs, wood, stems, stripped variants
        map(MATERIAL_SURFACE_MAP, SurfaceType.LOG,
                Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG,
                Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.MANGROVE_LOG, Material.CHERRY_LOG,
                Material.BAMBOO_BLOCK, Material.CRIMSON_STEM, Material.WARPED_STEM,
                Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.BIRCH_WOOD, Material.JUNGLE_WOOD,
                Material.ACACIA_WOOD, Material.DARK_OAK_WOOD, Material.MANGROVE_WOOD, Material.CHERRY_WOOD,
                Material.CRIMSON_HYPHAE, Material.WARPED_HYPHAE,
                Material.STRIPPED_OAK_LOG, Material.STRIPPED_SPRUCE_LOG, Material.STRIPPED_BIRCH_LOG,
                Material.STRIPPED_JUNGLE_LOG, Material.STRIPPED_ACACIA_LOG, Material.STRIPPED_DARK_OAK_LOG,
                Material.STRIPPED_MANGROVE_LOG, Material.STRIPPED_CHERRY_LOG, Material.STRIPPED_BAMBOO_BLOCK,
                Material.STRIPPED_CRIMSON_STEM, Material.STRIPPED_WARPED_STEM,
                Material.STRIPPED_OAK_WOOD, Material.STRIPPED_SPRUCE_WOOD, Material.STRIPPED_BIRCH_WOOD,
                Material.STRIPPED_JUNGLE_WOOD, Material.STRIPPED_ACACIA_WOOD, Material.STRIPPED_DARK_OAK_WOOD,
                Material.STRIPPED_MANGROVE_WOOD, Material.STRIPPED_CHERRY_WOOD,
                Material.STRIPPED_CRIMSON_HYPHAE, Material.STRIPPED_WARPED_HYPHAE);

        // CONCRETE (+ sculk blocks)
        map(MATERIAL_SURFACE_MAP, SurfaceType.CONCRETE,
                Material.WHITE_CONCRETE, Material.ORANGE_CONCRETE, Material.MAGENTA_CONCRETE,
                Material.LIGHT_BLUE_CONCRETE, Material.YELLOW_CONCRETE, Material.LIME_CONCRETE,
                Material.PINK_CONCRETE, Material.GRAY_CONCRETE, Material.LIGHT_GRAY_CONCRETE,
                Material.CYAN_CONCRETE, Material.PURPLE_CONCRETE, Material.BLUE_CONCRETE,
                Material.BROWN_CONCRETE, Material.GREEN_CONCRETE, Material.RED_CONCRETE, Material.BLACK_CONCRETE,
                Material.SCULK, Material.SCULK_CATALYST, Material.SCULK_SENSOR, Material.CALIBRATED_SCULK_SENSOR,
                Material.SCULK_VEIN, Material.SCULK_SHRIEKER);

        // GLASS
        map(MATERIAL_SURFACE_MAP, SurfaceType.GLASS,
                Material.GLASS, Material.GLASS_PANE,
                Material.WHITE_STAINED_GLASS, Material.ORANGE_STAINED_GLASS, Material.MAGENTA_STAINED_GLASS,
                Material.LIGHT_BLUE_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, Material.LIME_STAINED_GLASS,
                Material.PINK_STAINED_GLASS, Material.GRAY_STAINED_GLASS, Material.LIGHT_GRAY_STAINED_GLASS,
                Material.CYAN_STAINED_GLASS, Material.PURPLE_STAINED_GLASS, Material.BLUE_STAINED_GLASS,
                Material.BROWN_STAINED_GLASS, Material.GREEN_STAINED_GLASS, Material.RED_STAINED_GLASS, Material.BLACK_STAINED_GLASS,
                Material.WHITE_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE,
                Material.LIGHT_BLUE_STAINED_GLASS_PANE, Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE,
                Material.PINK_STAINED_GLASS_PANE, Material.GRAY_STAINED_GLASS_PANE, Material.LIGHT_GRAY_STAINED_GLASS_PANE,
                Material.CYAN_STAINED_GLASS_PANE, Material.PURPLE_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE,
                Material.BROWN_STAINED_GLASS_PANE, Material.GREEN_STAINED_GLASS_PANE, Material.RED_STAINED_GLASS_PANE, Material.BLACK_STAINED_GLASS_PANE);

        // MUD
        map(MATERIAL_SURFACE_MAP, SurfaceType.MUD,
                Material.MUD, Material.MUDDY_MANGROVE_ROOTS);

        // METALBAR
        map(MATERIAL_SURFACE_MAP, SurfaceType.METALBAR,
                Material.IRON_BARS, Material.LIGHTNING_ROD);

        // METALBOX
        map(MATERIAL_SURFACE_MAP, SurfaceType.METALBOX,
                Material.IRON_BLOCK, Material.IRON_TRAPDOOR,
                Material.GOLD_BLOCK, Material.NETHERITE_BLOCK,
                Material.COPPER_BLOCK, Material.EXPOSED_COPPER, Material.WEATHERED_COPPER, Material.OXIDIZED_COPPER,
                Material.WAXED_COPPER_BLOCK, Material.WAXED_EXPOSED_COPPER, Material.WAXED_WEATHERED_COPPER, Material.WAXED_OXIDIZED_COPPER,
                Material.CUT_COPPER, Material.CUT_COPPER_SLAB, Material.CUT_COPPER_STAIRS,
                Material.EXPOSED_CUT_COPPER, Material.EXPOSED_CUT_COPPER_SLAB, Material.EXPOSED_CUT_COPPER_STAIRS,
                Material.WEATHERED_CUT_COPPER, Material.WEATHERED_CUT_COPPER_SLAB, Material.WEATHERED_CUT_COPPER_STAIRS,
                Material.OXIDIZED_CUT_COPPER, Material.OXIDIZED_CUT_COPPER_SLAB, Material.OXIDIZED_CUT_COPPER_STAIRS,
                Material.WAXED_CUT_COPPER, Material.WAXED_CUT_COPPER_SLAB, Material.WAXED_CUT_COPPER_STAIRS,
                Material.WAXED_EXPOSED_CUT_COPPER, Material.WAXED_EXPOSED_CUT_COPPER_SLAB, Material.WAXED_EXPOSED_CUT_COPPER_STAIRS,
                Material.WAXED_WEATHERED_CUT_COPPER, Material.WAXED_WEATHERED_CUT_COPPER_SLAB, Material.WAXED_WEATHERED_CUT_COPPER_STAIRS,
                Material.WAXED_OXIDIZED_CUT_COPPER, Material.WAXED_OXIDIZED_CUT_COPPER_SLAB, Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS,
                Material.ANVIL, Material.CHIPPED_ANVIL, Material.DAMAGED_ANVIL, Material.HOPPER);

        // WEAK_ICE
        map(MATERIAL_SURFACE_MAP, SurfaceType.WEAK_ICE,
                Material.ICE, Material.FROSTED_ICE, Material.PACKED_ICE, Material.BLUE_ICE);

        // MARBLE (calcite, amethyst)
        map(MATERIAL_SURFACE_MAP, SurfaceType.MARBLE,
                Material.CALCITE, Material.AMETHYST_BLOCK, Material.BUDDING_AMETHYST);

        // WATER (at-feet level, walking through)
        map(MATERIAL_SURFACE_MAP, SurfaceType.WATER, Material.WATER);

        // LEAVES
        map(MATERIAL_SURFACE_MAP, SurfaceType.LEAVES,
                Material.OAK_LEAVES, Material.SPRUCE_LEAVES, Material.BIRCH_LEAVES, Material.JUNGLE_LEAVES,
                Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES, Material.MANGROVE_LEAVES,
                Material.CHERRY_LEAVES, Material.AZALEA_LEAVES, Material.FLOWERING_AZALEA_LEAVES);

        // BRUSH (passthrough plants at feet level)
        map(MATERIAL_SURFACE_MAP, SurfaceType.BRUSH,
                Material.SHORT_GRASS, Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN,
                Material.DEAD_BUSH, Material.NETHER_SPROUTS, Material.VINE,
                Material.CAVE_VINES, Material.CAVE_VINES_PLANT, Material.MOSS_CARPET,
                Material.WARPED_ROOTS, Material.CRIMSON_ROOTS, Material.GLOW_LICHEN);

        // Sound sets per surface
        SOUNDS = new EnumMap<>(SurfaceType.class);
        SOUNDS.put(SurfaceType.GRASS,    new SoundSet("grass.grass_walk",         "grass.grass_run",            "grass.grass_wander",   "dirt.dirt_land"));
        SOUNDS.put(SurfaceType.DIRT,     new SoundSet("dirt.dirt_walk",           "dirt.dirt_run",              "dirt.dirt_wander",     "dirt.dirt_land"));
        SOUNDS.put(SurfaceType.STONE,    new SoundSet("stone.stone_walk",         "stone.stone_run",            null,                   null));
        SOUNDS.put(SurfaceType.SAND,     new SoundSet("sand.sand_walk",           "sand.sand_run",              null,                   null));
        SOUNDS.put(SurfaceType.GRAVEL,   new SoundSet("gravel.gravel_walk",       "gravel.gravel_run",          "gravel.gravel_wander", "gravel.gravel_land"));
        SOUNDS.put(SurfaceType.SNOW,     new SoundSet("snow.snow_walk",           "snow.snow_run",              "snow.snow_wander",     null));
        SOUNDS.put(SurfaceType.WOOD,     new SoundSet("wood.wood_walk",           null,                         null,                   null));
        SOUNDS.put(SurfaceType.LOG,      new SoundSet("wood.log_walk",            null,                         null,                   null));
        SOUNDS.put(SurfaceType.CONCRETE, new SoundSet("concrete.concrete_walk",   "concrete.concrete_run",      "concrete.concrete_wander", null));
        SOUNDS.put(SurfaceType.GLASS,    new SoundSet("glass.glass_hard",         null,                         null,                   null));
        SOUNDS.put(SurfaceType.METALBAR, new SoundSet("metalbar.metalbar_walk",   "metalbar.metalbar_run",      "metalbar.metalbar_wander", "metalbar.metalbar_land"));
        SOUNDS.put(SurfaceType.METALBOX, new SoundSet("metalbox.metalbox_walk",   "metalbox.metalbox_run",      "metalbox.metalbox_wander", null));
        SOUNDS.put(SurfaceType.MUD,      new SoundSet("mud.mud_walk",             null,                         "mud.mud_wander",       null));
        SOUNDS.put(SurfaceType.WEAK_ICE, new SoundSet("weakice.weakice_walk",     "muffledice.muffledice_walk", null,                   null));
        SOUNDS.put(SurfaceType.MARBLE,   new SoundSet("marble.marble_walk",       "marble.marble_run",          "marble.marble_wander", null));
        SOUNDS.put(SurfaceType.WATER,    new SoundSet("water.water_through",      null,                         "water.water_wander",   null));
        SOUNDS.put(SurfaceType.LEAVES,   new SoundSet("leaves.leaves_through",    null,                         null,                   null));
        SOUNDS.put(SurfaceType.BRUSH,    new SoundSet("brush.brush_through",      null,                         null,                   null));
        SOUNDS.put(SurfaceType.DEFAULT,  new SoundSet("stone.stone_walk",         "stone.stone_run",            null,                   null));
    }

    // -----------------------------------------------------------------------
    // Instance fields
    // -----------------------------------------------------------------------

    private final FileConfiguration config;
    private final Random random = new Random();
    private final double stepThreshold;
    private final float footstepVolume;
    private final float pitchVariance;
    private final float armorOverlayVolume;

    public FootstepHandler(DynamicSurroundingsModule module, FileConfiguration config) {
        this.config = config;
        this.stepThreshold = config.getDouble("Footsteps.Threshold", 1.5);
        this.footstepVolume = (float) config.getDouble("Footsteps.Volume", 0.8);
        this.pitchVariance = (float) config.getDouble("Footsteps.PitchVariance", 0.1);
        this.armorOverlayVolume = (float) config.getDouble("Footsteps.ArmorOverlay.Volume", 0.3);
    }

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /**
     * Called from PlayerMoveEvent. Handles landing detection and footstep accumulation.
     */
    public void handleMove(Player player, Location from, Location to) {
        DSPlayerState state = DSPlayerState.getOrCreate(player.getUniqueId());
        boolean onGround = player.isOnGround();

        // Update fall tracking and detect landing before the hot-path exit
        if (!onGround) {
            state.maxFallDistance = Math.max(state.maxFallDistance, player.getFallDistance());
        } else if (!state.wasOnGround && state.maxFallDistance > 0.5f) {
            playLandSound(player, to);
            state.maxFallDistance = 0f;
        }
        state.wasOnGround = onGround;

        // Hot-path: skip footstep accumulation for pure vertical/rotational moves
        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        if (!onGround) {
            // Airborne — still update last position so delta is correct after landing
            state.lastX = to.getX();
            state.lastZ = to.getZ();
            return;
        }

        double dx = to.getX() - state.lastX;
        double dz = to.getZ() - state.lastZ;
        state.lastX = to.getX();
        state.lastZ = to.getZ();

        state.distanceAccumulator += Math.sqrt(dx * dx + dz * dz);

        double threshold = stepThreshold;
        if (state.distanceAccumulator < threshold) {
            return;
        }
        state.distanceAccumulator = 0;
        state.stepCount++;

        SurfaceType surface = getSurface(player, to);
        playFootstepSound(player, to, surface);
        playArmorOverlay(player, to, surface, state);

        // Debug instrumentation
        cz.hashiri.harshlands.debug.DebugManager debugMgr = cz.hashiri.harshlands.rsv.HLPlugin.getPlugin().getDebugManager();
        if (debugMgr.isActive("DynamicSurroundings", "Footsteps", player.getUniqueId())) {
            SoundSet sounds = SOUNDS.getOrDefault(surface, SOUNDS.get(SurfaceType.DEFAULT));
            String key = sounds.getVariant(player.isSprinting(), player.isSneaking());
            String consoleLine = "surface=" + surface + " sound=" + key
                    + " sprint=" + player.isSprinting() + " sneak=" + player.isSneaking()
                    + " armor=" + getArmorTier(player) + " step=" + state.stepCount;
            debugMgr.send("DynamicSurroundings", "Footsteps", player.getUniqueId(), "", consoleLine);
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void playFootstepSound(Player player, Location loc, SurfaceType surface) {
        SoundSet sounds = SOUNDS.getOrDefault(surface, SOUNDS.get(SurfaceType.DEFAULT));
        String key = sounds.getVariant(player.isSprinting(), player.isSneaking());
        if (key == null) return;

        float vol = footstepVolume;
        float pitch = 1.0f + (random.nextFloat() - 0.5f) * this.pitchVariance;

        playSound(player, loc, key, vol, pitch);

        // WOOD: 5% chance of floor squeak
        if (surface == SurfaceType.WOOD && random.nextFloat() < 0.05f) {
            playSound(player, loc, "floorsqueak", vol * 0.5f, pitch);
        }
    }

    private void playLandSound(Player player, Location loc) {
        SurfaceType surface = getSurface(player, loc);
        SoundSet sounds = SOUNDS.getOrDefault(surface, SOUNDS.get(SurfaceType.DEFAULT));
        if (sounds.land() == null) return;

        float vol = footstepVolume * 1.2f;
        float pitch = 0.95f + (random.nextFloat() - 0.5f) * 0.1f;
        playSound(player, loc, sounds.land(), vol, pitch);
    }

    private void playArmorOverlay(Player player, Location loc, SurfaceType surface, DSPlayerState state) {
        ArmorTier tier = getArmorTier(player);
        if (tier == ArmorTier.NONE) return;

        float vol = armorOverlayVolume;
        float pitch = 1.0f + (random.nextFloat() - 0.5f) * 0.1f;
        boolean sprinting = player.isSprinting();

        switch (tier) {
            case LIGHT -> playSound(player, loc, sprinting ? "armor.light_run" : "armor.light_walk", vol, pitch);
            case MEDIUM -> {
                playSound(player, loc, sprinting ? "armor.medium_run" : "armor.medium_walk", vol, pitch);
                if (state.stepCount % 3 == 0) playSound(player, loc, "armor.medium_foot", vol, pitch);
            }
            case HEAVY -> {
                playSound(player, loc, sprinting ? "armor.heavy_run" : "armor.heavy_walk", vol, pitch);
                if (state.stepCount % 3 == 0) playSound(player, loc, "armor.heavy_foot", vol, pitch);
            }
        }
    }

    private SurfaceType getSurface(Player player, Location to) {
        Block feetBlock = to.getBlock();
        Material feetMat = feetBlock.getType();

        // Check feet-level for passthrough surfaces (brush, wading water)
        SurfaceType feetSurface = MATERIAL_SURFACE_MAP.get(feetMat);
        if (feetSurface == SurfaceType.BRUSH || feetSurface == SurfaceType.WATER) {
            return feetSurface;
        }

        // Ground block (Y-1) determines the walking surface
        Block groundBlock = feetBlock.getRelative(BlockFace.DOWN);
        return MATERIAL_SURFACE_MAP.getOrDefault(groundBlock.getType(), SurfaceType.DEFAULT);
    }

    private ArmorTier getArmorTier(Player player) {
        int heavy = 0, medium = 0, light = 0;
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (ItemStack item : armor) {
            if (item == null || item.getType() == Material.AIR) continue;
            String name = item.getType().name();
            if (name.contains("DIAMOND") || name.contains("NETHERITE")) heavy++;
            else if (name.contains("CHAINMAIL") || name.contains("IRON")) medium++;
            else if (name.contains("LEATHER") || name.contains("GOLD")) light++;
        }
        if (heavy >= 3) return ArmorTier.HEAVY;
        if (medium >= 3) return ArmorTier.MEDIUM;
        if (light >= 3) return ArmorTier.LIGHT;
        return ArmorTier.NONE;
    }

    private void playSound(Player player, Location loc, String key, float volume, float pitch) {
        player.playSound(loc, "harshlands:" + key, SoundCategory.PLAYERS, volume, pitch);
    }
}
