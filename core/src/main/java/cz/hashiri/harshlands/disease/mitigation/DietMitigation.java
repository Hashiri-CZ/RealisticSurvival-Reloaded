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
package cz.hashiri.harshlands.disease.mitigation;

import cz.hashiri.harshlands.disease.trigger.DietTrigger;
import cz.hashiri.harshlands.disease.trigger.NutrientTierReader;
import cz.hashiri.harshlands.foodexpansion.NutrientTier;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Malnutrition mitigation: active (holds / regresses the disease) while the player is NOT
 * malnourished — i.e. eating a balanced diet. Shares the malnourished predicate and the tier
 * read with {@link DietTrigger} so trigger and mitigation can never disagree.
 */
public final class DietMitigation implements Mitigation {

    private final Set<NutrientTier> malnourishedTiers;
    private final NutrientTierReader reader;

    public DietMitigation(Set<NutrientTier> malnourishedTiers, NutrientTierReader reader) {
        this.malnourishedTiers = malnourishedTiers;
        this.reader = reader;
    }

    @Override
    public boolean isActive(Player player) {
        return !DietTrigger.malnourished(reader.tier(player), malnourishedTiers);
    }
}
