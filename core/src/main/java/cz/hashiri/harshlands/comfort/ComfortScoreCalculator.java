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
import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Logger;

public class ComfortScoreCalculator {

    private final Map<Material, String> materialToCategory = new HashMap<>();
    private final Map<String, Integer> categoryPoints = new HashMap<>();
    private final Set<String> requireLitCategories = new HashSet<>();
    private final Map<EntityType, String> entityToCategory = new HashMap<>();
    private final int searchRadius;

    // Diminishing returns config
    private final boolean diminishingEnabled;
    private final double diminishingFactor;
    private final double diminishingCap;

    private record TierRange(int minScore, int maxScore, ComfortTier tier) {}
    private final List<TierRange> tierRanges;

    private static final Set<Material> AIR_TYPES = Set.of(Material.AIR, Material.CAVE_AIR, Material.VOID_AIR);

    public ComfortScoreCalculator(@Nonnull FileConfiguration config, @Nonnull Logger logger) {
        this.searchRadius = config.getInt("SearchRadius", 8);
        loadCategories(config, logger);

        // Load diminishing returns config
        ConfigurationSection dr = config.getConfigurationSection("DiminishingReturns");
        if (dr != null) {
            this.diminishingEnabled = dr.getBoolean("Enabled", false);
            this.diminishingFactor = dr.getDouble("Factor", 1.0);
            this.diminishingCap = dr.getDouble("Cap", 2.0);
        } else {
            this.diminishingEnabled = false;
            this.diminishingFactor = 1.0;
            this.diminishingCap = 2.0;
        }

        List<TierRange> ranges = new ArrayList<>();
        ConfigurationSection tiersSection = config.getConfigurationSection("Tiers");
        if (tiersSection != null) {
            for (String tierKey : tiersSection.getKeys(false)) {
                ConfigurationSection tierSec = tiersSection.getConfigurationSection(tierKey);
                if (tierSec == null) continue;
                try {
                    ranges.add(new TierRange(
                        tierSec.getInt("MinScore", 0),
                        tierSec.getInt("MaxScore", Integer.MAX_VALUE),
                        ComfortTier.valueOf(tierKey)
                    ));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        // Sort by minScore ascending so we can iterate in order for next-tier lookup
        ranges.sort(Comparator.comparingInt(r -> r.minScore()));
        this.tierRanges = List.copyOf(ranges);
    }

    private void loadCategories(@Nonnull FileConfiguration config, @Nonnull Logger logger) {
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

            // Prefix match (e.g. FlowerPot -> POTTED_): pre-compute all matching materials
            if (cat.contains("PrefixMatch")) {
                String prefix = cat.getString("PrefixMatch");
                if (prefix != null && !prefix.isEmpty()) {
                    for (Material mat : Material.values()) {
                        if (mat.name().startsWith(prefix)) {
                            materialToCategory.put(mat, categoryName);
                        }
                    }
                }
            }

            // Block materials
            List<String> materials = cat.getStringList("Materials");
            for (String matName : materials) {
                Material mat = Material.matchMaterial(matName);
                if (mat != null) {
                    materialToCategory.put(mat, categoryName);
                } else {
                    logger.warning("[Comfort] Unknown material in config: " + matName);
                }
            }

            // Entity types
            List<String> entityTypes = cat.getStringList("EntityTypes");
            for (String etName : entityTypes) {
                try {
                    EntityType et = EntityType.valueOf(etName);
                    entityToCategory.put(et, categoryName);
                } catch (IllegalArgumentException e) {
                    logger.warning("[Comfort] Unknown entity type in config: " + etName);
                }
            }
        }
    }

    @Nonnull
    public ComfortResult calculate(@Nonnull Location center) {
        World world = center.getWorld();
        if (world == null) {
            return new ComfortResult(0, ComfortTier.NONE, Collections.emptySet());
        }

        // Count matches per category (not just presence)
        Map<String, Integer> matchCounts = new HashMap<>();

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        // Scan blocks in a cube
        for (int x = cx - searchRadius; x <= cx + searchRadius; x++) {
            for (int y = cy - searchRadius; y <= cy + searchRadius; y++) {
                for (int z = cz - searchRadius; z <= cz + searchRadius; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material mat = block.getType();

                    // Skip air blocks (majority of scan volume)
                    if (AIR_TYPES.contains(mat)) {
                        continue;
                    }

                    // Check direct material mapping (includes pre-computed POTTED_* materials)
                    String category = materialToCategory.get(mat);
                    if (category != null) {
                        if (requireLitCategories.contains(category)) {
                            if (!isBlockLit(block)) {
                                continue;
                            }
                        }
                        matchCounts.merge(category, 1, Integer::sum);
                    }
                }
            }
        }

        // Scan nearby entities
        if (!entityToCategory.isEmpty()) {
            double radius = searchRadius;
            Collection<Entity> entities = world.getNearbyEntities(center, radius, radius, radius);
            for (Entity entity : entities) {
                String category = entityToCategory.get(entity.getType());
                if (category != null) {
                    matchCounts.merge(category, 1, Integer::sum);
                }
            }
        }

        // Sum points from found categories, applying diminishing returns
        double scoreDouble = 0.0;
        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            String cat = entry.getKey();
            int count = entry.getValue();
            int basePoints = categoryPoints.getOrDefault(cat, 0);
            scoreDouble += computeCategoryContribution(basePoints, count);
        }

        int score = (int) scoreDouble; // floor to nearest integer

        // Build the set of found categories (categories with at least one match)
        Set<String> foundCategories = Collections.unmodifiableSet(matchCounts.keySet());

        ComfortTier tier = resolveTier(score);
        return new ComfortResult(score, tier, foundCategories);
    }

    /**
     * Computes the score contribution from a single category given its base point value
     * and the number of matching blocks/entities found.
     *
     * <p>When DiminishingReturns is disabled: returns basePoints (old behaviour — one match
     * worth of points regardless of count).</p>
     *
     * <p>When DiminishingReturns is enabled: computes
     * {@code basePoints * (1 + factor + factor^2 + ... + factor^(count-1))} capped at
     * {@code Cap * basePoints}.</p>
     */
    private double computeCategoryContribution(int basePoints, int count) {
        if (!diminishingEnabled || count <= 0) {
            // Disabled: original behaviour — one category, full points once
            return count > 0 ? basePoints : 0;
        }

        double contribution = 0.0;
        double multiplier = 1.0;
        double capValue = diminishingCap * basePoints;

        for (int i = 0; i < count; i++) {
            contribution += basePoints * multiplier;
            if (contribution >= capValue) {
                return capValue;
            }
            multiplier *= diminishingFactor;
        }
        return Math.min(contribution, capValue);
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
        if (score <= 0) return ComfortTier.NONE;
        for (TierRange range : tierRanges) {
            if (score >= range.minScore() && score <= range.maxScore()) return range.tier();
        }
        return ComfortTier.NONE;
    }

    /**
     * Returns the next tier above the given score, or {@code null} if the score is
     * already in or above the highest tier (LUXURY).
     *
     * <p>If the score is below all tiers, returns info for the lowest tier.</p>
     *
     * @return array of [nextTierDisplayName, pointsNeeded] as Object[2], or null if at max tier
     */
    @Nullable
    public NextTierInfo getNextTierInfo(int score) {
        // tierRanges is sorted ascending by minScore
        // Find current tier range
        TierRange current = null;
        for (TierRange range : tierRanges) {
            if (score >= range.minScore() && score <= range.maxScore()) {
                current = range;
                break;
            }
        }

        if (current != null && current.tier() == ComfortTier.LUXURY) {
            // Already at max tier
            return null;
        }

        // Find the first tier whose minScore is greater than current score
        for (TierRange range : tierRanges) {
            if (range.minScore() > score) {
                int pointsNeeded = range.minScore() - score;
                return new NextTierInfo(range.tier().getDisplayName(), pointsNeeded);
            }
        }

        // Score is beyond all tier ranges but wasn't matched as LUXURY — edge case
        return null;
    }

    public record NextTierInfo(String tierDisplayName, int pointsNeeded) {}

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
