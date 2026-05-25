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

import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.List;

/**
 * One heal-item entry parsed from Settings/firstaid.yml under Items.<name>.
 * AffectsParts strings must match bodyhealth.core.BodyPart enum names; they are
 * compared as strings so this class has no compile-time dependency on BodyHealth.
 */
public record FaHealItem(String name, double restoreAmount, List<String> affectsParts, String sound) {

    public static @Nullable FaHealItem parse(String name, @Nullable ConfigurationSection section) {
        if (section == null) return null;
        if (!section.isSet("RestoreAmount")) return null;
        double amount = section.getDouble("RestoreAmount");
        List<String> parts = section.getStringList("AffectsParts");
        if (parts.isEmpty()) return null;
        String sound = section.getString("Sound", "");
        return new FaHealItem(name, amount, List.copyOf(parts), sound);
    }
}
