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

import cz.hashiri.harshlands.disease.PlayerStateCleanup;
import cz.hashiri.harshlands.foodexpansion.NutrientTier;
import org.bukkit.entity.Player;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Malnutrition: per-check contraction risk once a player has been in a malnourished
 * {@link NutrientTier} for a sustained number of consecutive checks. Tier is read through an
 * injected {@link NutrientTierReader} (FoodExpansion in production), so the trigger contributes
 * no risk when FoodExpansion is absent — the reader returns {@code NORMAL}.
 *
 * <p>{@code NutrientTier} ordinals are not severity-ordered, so "malnourished" is a configured
 * set of tiers, never an ordinal comparison.
 */
public final class DietTrigger implements DiseaseTrigger, PlayerStateCleanup {

    private final String diseaseId;
    private final Set<NutrientTier> malnourishedTiers;
    private final int sustainChecks;
    private final double chancePerCheck;
    private final NutrientTierReader reader;

    // player -> count of consecutive malnourished checks observed so far.
    private final Map<UUID, Integer> consecutive = new ConcurrentHashMap<>();

    public DietTrigger(String diseaseId, Set<NutrientTier> malnourishedTiers, int sustainChecks,
                       double chancePerCheck, NutrientTierReader reader) {
        this.diseaseId = diseaseId;
        this.malnourishedTiers = malnourishedTiers;
        this.sustainChecks = sustainChecks;
        this.chancePerCheck = chancePerCheck;
        this.reader = reader;
    }

    @Override public String diseaseId() { return diseaseId; }

    /** Pure: is this tier considered malnourished by this config? */
    public static boolean malnourished(NutrientTier tier, Set<NutrientTier> malnourishedTiers) {
        return tier != null && malnourishedTiers.contains(tier);
    }

    /** Parse a config list of tier names into a set; unknown names are skipped. */
    public static Set<NutrientTier> parseTiers(List<String> names) {
        Set<NutrientTier> set = EnumSet.noneOf(NutrientTier.class);
        if (names == null) return set;
        for (String n : names) {
            try {
                set.add(NutrientTier.valueOf(n.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // skip unknown tier name
            }
        }
        return set;
    }

    /**
     * Stateful per-call: advances the player's consecutive-malnourished counter and returns
     * the contraction chance — {@code chancePerCheck} once the sustain threshold is reached,
     * else 0. A nourished tier resets the counter.
     */
    public double chanceFor(UUID uuid, NutrientTier tier) {
        if (malnourished(tier, malnourishedTiers)) {
            int n = consecutive.merge(uuid, 1, Integer::sum);
            return n >= sustainChecks ? chancePerCheck : 0.0;
        }
        consecutive.remove(uuid);
        return 0.0;
    }

    @Override
    public double chance(Player player) {
        return chanceFor(player.getUniqueId(), reader.tier(player));
    }

    /** Drop this player's consecutive-malnourished counter (quit cleanup — see {@link PlayerStateCleanup}). */
    @Override
    public void clearPlayer(UUID uuid) {
        consecutive.remove(uuid);
    }
}
