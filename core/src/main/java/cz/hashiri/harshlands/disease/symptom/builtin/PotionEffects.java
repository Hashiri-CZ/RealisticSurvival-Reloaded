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
package cz.hashiri.harshlands.disease.symptom.builtin;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

/** Resolves potion-effect names robustly across legacy spigot names and modern registry keys. */
public final class PotionEffects {

    private PotionEffects() {}

    // Legacy spigot constant name (upper) -> modern registry key (lower).
    private static final Map<String, String> LEGACY = Map.ofEntries(
        Map.entry("SLOW", "slowness"),
        Map.entry("SLOW_DIGGING", "mining_fatigue"),
        Map.entry("FAST_DIGGING", "haste"),
        Map.entry("INCREASE_DAMAGE", "strength"),
        Map.entry("HEAL", "instant_health"),
        Map.entry("HARM", "instant_damage"),
        Map.entry("JUMP", "jump_boost"),
        Map.entry("CONFUSION", "nausea"),
        Map.entry("DAMAGE_RESISTANCE", "resistance")
    );

    /** Pure: legacy alias (any case) -> modern lowercase key; unknown names pass through lowercased.
     *  Null/blank -> "". */
    public static String normalizeName(String name) {
        if (name == null) return "";
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return "";
        String upper = trimmed.toUpperCase(java.util.Locale.ROOT);
        String mapped = LEGACY.get(upper);
        return mapped != null ? mapped : trimmed.toLowerCase(java.util.Locale.ROOT);
    }

    /** Resolve to a live PotionEffectType, or null if unknown. Touches the Bukkit registry. */
    public static PotionEffectType resolve(String name) {
        String key = normalizeName(name);
        if (key.isEmpty()) return null;
        PotionEffectType byKey = Registry.EFFECT.get(NamespacedKey.minecraft(key));
        if (byKey != null) return byKey;
        // Fallback for any name the registry path missed.
        return PotionEffectType.getByName(name.trim().toUpperCase(java.util.Locale.ROOT));
    }
}
