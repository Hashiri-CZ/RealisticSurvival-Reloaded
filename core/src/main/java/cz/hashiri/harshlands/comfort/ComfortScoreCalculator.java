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
package cz.hashiri.harshlands.comfort;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.block.data.type.Candle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import javax.annotation.Nonnull;
import java.util.*;

public class ComfortScoreCalculator {

    private final Map<Material, String> materialToCategory = new HashMap<>();
    private final Map<String, Integer> categoryPoints = new HashMap<>();
    private final Set<String> requireLitCategories = new HashSet<>();
    private final Map<EntityType, String> entityToCategory = new HashMap<>();
    private final List<String> prefixMatchCategories = new ArrayList<>();
    private final Map<String, String> prefixToCategory = new HashMap<>();
    private final int searchRadius;
    private final FileConfiguration config;

    public ComfortScoreCalculator(@Nonnull FileConfiguration config) {
        this.config = config;
        this.searchRadius = config.getInt("SearchRadius", 10);
        loadCategories();
    }

    private void loadCategories() {
        ConfigurationSection categoriesSection = config.getConfigurationSection("Categories");
        if (categoriesSection == null) {
            return;
        }

        for (String categoryName : categoriesSection.getKeys(false)) {
            ConfigurationSection cat = categoriesSection.getConfigurationSection(categoryName);
            if (cat == null) {
                continue;
            }

            int points = cat.getInt("Points", 1);
            categoryPoints.put(categoryName, points);

            if (cat.getBoolean("RequireLit", false)) {
                requireLitCategories.add(categoryName);
            }

            // Prefix match (e.g. FlowerPot -> POTTED_)
            if (cat.contains("PrefixMatch")) {
                String prefix = cat.getString("PrefixMatch");
                if (prefix != null && !prefix.isEmpty()) {
                    prefixMatchCategories.add(prefix);
                    prefixToCategory.put(prefix, categoryName);
                }
            }

            // Block materials
            List<String> materials = cat.getStringList("Materials");
            for (String matName : materials) {
                Material mat = Material.matchMaterial(matName);
                if (mat != null) {
                    materialToCategory.put(mat, categoryName);
                }
            }

            // Entity types
            List<String> entityTypes = cat.getStringList("EntityTypes");
            for (String etName : entityTypes) {
                try {
                    EntityType et = EntityType.valueOf(etName);
                    entityToCategory.put(et, categoryName);
                } catch (IllegalArgumentException ignored) {
                    // Unknown entity type — skip
                }
            }
        }
    }

    /**
     * Calculate the comfort score at a given center location.
     *
     * @param center the center location (typically a bed)
     * @return the computed comfort result
     */
    @Nonnull
    public ComfortResult calculate(@Nonnull Location center) {
        World world = center.getWorld();
        if (world == null) {
            return new ComfortResult(0, ComfortTier.NONE, Collections.emptySet());
        }

        Set<String> foundCategories = new HashSet<>();
        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // Scan blocks in a cube
        for (int x = cx - searchRadius; x <= cx + searchRadius; x++) {
            for (int y = cy - searchRadius; y <= cy + searchRadius; y++) {
                for (int z = cz - searchRadius; z <= cz + searchRadius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material mat = block.getType();

                    // Check prefix matches (e.g. POTTED_*)
                    for (String prefix : prefixMatchCategories) {
                        if (mat.name().startsWith(prefix)) {
                            foundCategories.add(prefixToCategory.get(prefix));
                            break;
                        }
                    }

                    // Check direct material mapping
                    String category = materialToCategory.get(mat);
                    if (category != null) {
                        if (requireLitCategories.contains(category)) {
                            if (!isBlockLit(block)) {
                                continue;
                            }
                        }
                        foundCategories.add(category);
                    }
                }
            }
        }

        // Scan nearby entities
        double radius = searchRadius;
        Collection<Entity> entities = world.getNearbyEntities(center, radius, radius, radius);
        for (Entity entity : entities) {
            String category = entityToCategory.get(entity.getType());
            if (category != null) {
                foundCategories.add(category);
            }
        }

        // Sum points from found categories
        int score = 0;
        for (String cat : foundCategories) {
            score += categoryPoints.getOrDefault(cat, 0);
        }

        // Determine tier
        ComfortTier tier = resolveTier(score);

        return new ComfortResult(score, tier, foundCategories);
    }

    private boolean isBlockLit(@Nonnull Block block) {
        BlockData data = block.getBlockData();
        if (data instanceof Campfire campfire) {
            return campfire.isLit();
        }
        if (data instanceof Candle candle) {
            return candle.isLit();
        }
        return false;
    }

    @Nonnull
    private ComfortTier resolveTier(int score) {
        if (score <= 0) {
            return ComfortTier.NONE;
        }

        ConfigurationSection tiersSection = config.getConfigurationSection("Tiers");
        if (tiersSection == null) {
            return ComfortTier.NONE;
        }

        for (String tierKey : tiersSection.getKeys(false)) {
            ConfigurationSection tierSec = tiersSection.getConfigurationSection(tierKey);
            if (tierSec == null) {
                continue;
            }
            int min = tierSec.getInt("MinScore", 0);
            int max = tierSec.getInt("MaxScore", Integer.MAX_VALUE);
            if (score >= min && score <= max) {
                try {
                    return ComfortTier.valueOf(tierKey);
                } catch (IllegalArgumentException ignored) {
                    // Unknown tier key in config
                }
            }
        }

        return ComfortTier.NONE;
    }

    /**
     * Result of a comfort score calculation.
     */
    public static class ComfortResult {
        private final int score;
        @Nonnull
        private final ComfortTier tier;
        @Nonnull
        private final Set<String> foundCategories;

        public ComfortResult(int score, @Nonnull ComfortTier tier, @Nonnull Set<String> foundCategories) {
            this.score = score;
            this.tier = tier;
            this.foundCategories = foundCategories;
        }

        public int getScore() {
            return score;
        }

        @Nonnull
        public ComfortTier getTier() {
            return tier;
        }

        @Nonnull
        public Set<String> getFoundCategories() {
            return foundCategories;
        }
    }
}
