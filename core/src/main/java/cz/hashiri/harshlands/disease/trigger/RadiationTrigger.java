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

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/** Contraction from nearby irradiated blocks or held irradiated items (Crimson). Soft: 0 if none. */
public final class RadiationTrigger implements DiseaseTrigger {

    private final String diseaseId;
    private final Set<Material> radioactive;
    private final int scanRadius;
    private final double chancePerCheck;

    public RadiationTrigger(String diseaseId, Set<Material> radioactive,
                            int scanRadius, double chancePerCheck) {
        this.diseaseId = diseaseId;
        this.radioactive = radioactive;
        this.scanRadius = scanRadius;
        this.chancePerCheck = chancePerCheck;
    }

    @Override public String diseaseId() { return diseaseId; }

    /** Pure: exposed if any nearby block type or the held item type is radioactive. */
    public static boolean exposed(Set<Material> nearbyBlocks, Material inHand, Set<Material> radioactive) {
        if (inHand != null && radioactive.contains(inHand)) return true;
        for (Material m : nearbyBlocks) {
            if (radioactive.contains(m)) return true;
        }
        return false;
    }

    @Override
    public double chance(Player player) {
        Set<Material> nearby = scanNearby(player.getLocation());
        ItemStack main = player.getInventory().getItemInMainHand();
        Material inHand = main != null ? main.getType() : null;
        return exposed(nearby, inHand, radioactive) ? chancePerCheck : 0.0;
    }

    private Set<Material> scanNearby(Location center) {
        Set<Material> found = new HashSet<>();
        int r = scanRadius;
        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    Material type = center.getWorld()
                        .getBlockAt(center.getBlockX() + dx, center.getBlockY() + dy, center.getBlockZ() + dz)
                        .getType();
                    if (radioactive.contains(type)) { found.add(type); return found; }
                }
            }
        }
        return found;
    }
}
