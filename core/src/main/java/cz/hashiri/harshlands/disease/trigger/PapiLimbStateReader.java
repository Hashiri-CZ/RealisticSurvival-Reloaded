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

import cz.hashiri.harshlands.bodyhealth.BodyPart;
import cz.hashiri.harshlands.bodyhealth.BodyPartState;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Production {@link LimbStateReader}: reads {@code %bodyhealth_state_<part>%} from the external
 * BodyHealth plugin via PlaceholderAPI — exactly as {@code BodyHealthRenderTask} does — and
 * returns the worst limb state. Returns {@code FULL} when PlaceholderAPI / BodyHealth is
 * unavailable or a placeholder is unresolved, so a missing BodyHealth means "no injury".
 * Must be called on the main thread (the progression task is).
 */
public final class PapiLimbStateReader implements LimbStateReader {

    @Override
    public BodyPartState worst(Player player) {
        if (player == null) return BodyPartState.FULL;
        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (papi == null || !papi.isEnabled()) return BodyPartState.FULL;
        List<BodyPartState> states = new ArrayList<>();
        for (BodyPart part : BodyPart.values()) {
            String key = "%bodyhealth_state_" + part.placeholderSuffix() + "%";
            String raw = PlaceholderAPI.setPlaceholders(player, key);
            if (raw == null || raw.startsWith("%")) {
                states.add(BodyPartState.FULL);
            } else {
                states.add(BodyPartState.fromPlaceholder(raw));
            }
        }
        return LimbInjuryTrigger.worstSeverity(states);
    }
}
