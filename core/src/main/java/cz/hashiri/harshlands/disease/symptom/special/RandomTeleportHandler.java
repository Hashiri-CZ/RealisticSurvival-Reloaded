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
package cz.hashiri.harshlands.disease.symptom.special;

import cz.hashiri.harshlands.disease.symptom.SymptomContext;
import cz.hashiri.harshlands.disease.symptom.SymptomHandler;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Random;

/** On a per-check roll, teleports the player a short random horizontal offset (Endersion). */
public final class RandomTeleportHandler implements SymptomHandler {

    private final Random random = new Random();

    @Override
    public void apply(Player player, SymptomContext ctx) {
        if (ctx.params() == null) return;
        double chance = ctx.params().getDouble("Chance", 0.15);
        if (chance <= 0 || random.nextDouble() >= chance) return;
        int radius = ctx.params().getInt("MaxRadius", 6);
        int[] off = pickOffset(random, radius);
        if (off[0] == 0 && off[1] == 0) return;

        Location base = player.getLocation();
        int targetX = base.getBlockX() + off[0];
        int targetZ = base.getBlockZ() + off[1];
        int targetY = player.getWorld().getHighestBlockYAt(targetX, targetZ) + 1;
        Location dest = new Location(player.getWorld(), targetX + 0.5, targetY, targetZ + 0.5,
            base.getYaw(), base.getPitch());
        player.teleport(dest);
        player.getWorld().playSound(dest, org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
    }

    /** Pure: a random offset in [-radius, radius] on each of x/z. Returns {dx, dz}. */
    public static int[] pickOffset(Random random, int radius) {
        if (radius <= 0) return new int[] {0, 0};
        int dx = random.nextInt(radius * 2 + 1) - radius;
        int dz = random.nextInt(radius * 2 + 1) - radius;
        return new int[] {dx, dz};
    }
}
