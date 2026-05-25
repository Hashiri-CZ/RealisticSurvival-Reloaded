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
package cz.hashiri.harshlands.firstaid;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Pure-Java orchestrator for the bandage / splint / medical_kit heal action.
 * Selects the most-injured allowed body part and asks BodyHealthBridge to heal it.
 *
 * Side effects (item consumption, sound, chat) are the listener's responsibility.
 */
public class FaHealService {

    public enum Result {
        HEALED,
        NO_INJURY,
        BODYHEALTH_UNAVAILABLE,
        UNKNOWN_ITEM
    }

    public record Outcome(Result result, @Nullable String healedPart, double amount) {}

    private final BodyHealthBridge bridge;
    private final Map<String, FaHealItem> items;

    public FaHealService(BodyHealthBridge bridge, Map<String, FaHealItem> items) {
        this.bridge = bridge;
        this.items = items;
    }

    public Result use(@Nullable Player player, String itemName) {
        return useWithOutcome(player, itemName).result();
    }

    public Outcome useWithOutcome(@Nullable Player player, String itemName) {
        if (!bridge.isAvailable()) {
            return new Outcome(Result.BODYHEALTH_UNAVAILABLE, null, 0);
        }
        FaHealItem item = items.get(itemName);
        if (item == null) {
            return new Outcome(Result.UNKNOWN_ITEM, null, 0);
        }

        String bestPart = null;
        double bestMissingHp = 0;
        for (String partName : item.affectsParts()) {
            double max = bridge.getMaxHealth(player, partName);
            if (max <= 0) continue;
            double curPercent = bridge.getHealth(player, partName);
            if (curPercent < 0 || curPercent >= 100.0) continue;
            double missingHp = (1.0 - curPercent / 100.0) * max;
            if (missingHp > bestMissingHp) {
                bestMissingHp = missingHp;
                bestPart = partName;
            }
        }

        if (bestPart == null) {
            return new Outcome(Result.NO_INJURY, null, 0);
        }

        double amount = Math.min(item.restoreAmount(), bestMissingHp);
        boolean ok = bridge.heal(player, bestPart, amount);
        if (!ok) {
            return new Outcome(Result.BODYHEALTH_UNAVAILABLE, null, 0);
        }
        return new Outcome(Result.HEALED, bestPart, amount);
    }
}
