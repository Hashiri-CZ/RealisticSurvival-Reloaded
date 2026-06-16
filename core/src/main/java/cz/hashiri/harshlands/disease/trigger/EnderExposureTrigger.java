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

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

/** Contraction from handling ender items or being in the End (Endersion). Soft: 0 if no match. */
public final class EnderExposureTrigger implements DiseaseTrigger {

    private final String diseaseId;
    private final Set<Material> enderItems;
    private final boolean theEndCounts;
    private final double chancePerCheck;

    public EnderExposureTrigger(String diseaseId, Set<Material> enderItems,
                                boolean theEndCounts, double chancePerCheck) {
        this.diseaseId = diseaseId;
        this.enderItems = enderItems;
        this.theEndCounts = theEndCounts;
        this.chancePerCheck = chancePerCheck;
    }

    @Override public String diseaseId() { return diseaseId; }

    /** Pure: is the player exposed given the item in hand and whether they're in the End. */
    public static boolean exposed(Material inHand, boolean inTheEnd, Set<Material> enderItems) {
        if (inTheEnd) return true;
        return inHand != null && enderItems.contains(inHand);
    }

    @Override
    public double chance(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();
        Material mainType = main != null ? main.getType() : null;
        Material offType = off != null ? off.getType() : null;
        boolean inEnd = theEndCounts
            && player.getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END;
        boolean exposed = exposed(mainType, inEnd, enderItems) || exposed(offType, inEnd, enderItems);
        return exposed ? chancePerCheck : 0.0;
    }
}
