/*
    Copyright (C) 2025  Hashiri_

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
package cz.hashiri.harshlands.foodexpansion;

import cz.hashiri.harshlands.data.HLModule;
import cz.hashiri.harshlands.data.HLPlayer;
import cz.hashiri.harshlands.foodexpansion.items.CustomFoodRegistry;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Single source of truth for "should the held-food nutrient preview be shown
 * for this player right now". Both NutritionPreviewController (which writes
 * the preview to the action bar) and DisplayTask (which suppresses its own
 * action-bar send when the preview is active) consult this predicate to avoid
 * both tasks writing the action bar in the same tick.
 */
public final class FoodPreviewState {

    private FoodPreviewState() {}

    /**
     * @return true iff the player is currently holding an edible item with a
     *         resolvable {@link NutrientProfile}, has loaded nutrition data,
     *         is in survival/adventure, and is not mid-eating.
     */
    public static boolean isActive(Player player) {
        if (player == null || !player.isOnline()) return false;

        FoodExpansionModule module = (FoodExpansionModule) HLModule.getModule(FoodExpansionModule.NAME);
        if (module == null || !module.isEnabled(player)) return false;

        GameMode mode = player.getGameMode();
        if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) return false;

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand == null || mainHand.getType() == Material.AIR) return false;

        // Hide during the right-click-hold eating animation. Comparing to the
        // active item avoids also hiding when the player is, say, raising a
        // shield in the off-hand.
        if (player.isHandRaised() && player.getItemInUse() != null
                && player.getItemInUse().isSimilar(mainHand)) {
            return false;
        }

        CustomFoodRegistry cfRegistry = module.getCustomFoodRegistry();
        boolean isEdible = mainHand.getType().isEdible()
                || (cfRegistry != null && cfRegistry.isCustomFood(mainHand));
        if (!isEdible) return false;

        String itemKey;
        if (cfRegistry != null && cfRegistry.isCustomFood(mainHand)) {
            itemKey = cfRegistry.getFoodId(mainHand);
        } else {
            itemKey = mainHand.getType().name();
        }
        if (module.getNutrientProfile(itemKey) == null) return false;

        HLPlayer hl = HLPlayer.getPlayers().get(player.getUniqueId());
        if (hl == null) return false;
        cz.hashiri.harshlands.data.foodexpansion.DataModule dm = hl.getNutritionDataModule();
        if (dm == null || dm.getData() == null) return false;

        return true;
    }
}
