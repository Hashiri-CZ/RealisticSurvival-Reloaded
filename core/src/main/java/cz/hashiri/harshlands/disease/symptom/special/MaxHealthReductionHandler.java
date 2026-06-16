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

import cz.hashiri.harshlands.HLPlugin;
import cz.hashiri.harshlands.disease.symptom.SymptomContext;
import cz.hashiri.harshlands.disease.symptom.SymptomHandler;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.ArrayList;

/**
 * Reduces the player's max health while active (Malnutrition stage 2). Applies a single
 * removable {@link AttributeModifier} keyed by a stable {@link NamespacedKey} on
 * {@code MAX_HEALTH}, so {@link #clear} fully restores max health without needing to remember
 * the original value. {@code apply} is idempotent (removes any prior copy before adding), so
 * re-applying every check never stacks. {@code Hearts} defaults to 2.0 when absent.
 */
public final class MaxHealthReductionHandler implements SymptomHandler {

    private static final String KEY_NAME = "disease_maxhealth_reduction";

    @Override
    public void apply(Player player, SymptomContext ctx) {
        double hearts = ctx.params() != null ? ctx.params().getDouble("Hearts", 2.0) : 2.0;
        AttributeInstance inst = player.getAttribute(Attribute.MAX_HEALTH);
        if (inst == null) return;
        removeOurModifier(inst);
        inst.addModifier(new AttributeModifier(key(), modifierAmount(hearts),
            AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
        double max = inst.getValue();
        if (player.getHealth() > max) player.setHealth(max);
    }

    @Override
    public void clear(Player player, SymptomContext ctx) {
        AttributeInstance inst = player.getAttribute(Attribute.MAX_HEALTH);
        if (inst != null) removeOurModifier(inst);
    }

    /** Pure: the (negative) attribute delta to drop max health by {@code hearts} hearts (2 HP each). */
    public static double modifierAmount(double hearts) {
        return -Math.abs(hearts) * 2.0;
    }

    private static NamespacedKey key() {
        return new NamespacedKey(HLPlugin.getPlugin(), KEY_NAME);
    }

    /** Remove any modifier we previously added (matched by our stable key). */
    private static void removeOurModifier(AttributeInstance inst) {
        NamespacedKey k = key();
        for (AttributeModifier m : new ArrayList<>(inst.getModifiers())) {
            if (k.equals(m.getKey())) inst.removeModifier(m);
        }
    }
}
