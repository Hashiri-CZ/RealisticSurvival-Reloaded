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
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

/**
 * "Jitters" (Battle Trauma stage 1): on a per-check roll, applies a brief NAUSEA pulse — the
 * closest vanilla approximation of a disorienting screen-shake, no client API needed. {@code Chance}
 * defaults to 0.3 (occasional), {@code DurationTicks} to 60. Apply-only ({@code SymptomHandler.clear()}
 * is a no-op default; the short NAUSEA fades on its own).
 */
public final class JittersHandler implements SymptomHandler {

    private final Random random = new Random();

    @Override
    public void apply(Player player, SymptomContext ctx) {
        double chance = ctx.params() != null ? ctx.params().getDouble("Chance", 0.3) : 0.3;
        if (!shouldJitter(chance, random.nextDouble())) return;
        int duration = ctx.params() != null ? ctx.params().getInt("DurationTicks", 60) : 60;
        player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, duration, 0, false, false));
    }

    /** Pure: should a jitter fire, given the active chance and a roll in [0,1)? */
    public static boolean shouldJitter(double chance, double roll) {
        return chance > 0 && roll < chance;
    }
}
