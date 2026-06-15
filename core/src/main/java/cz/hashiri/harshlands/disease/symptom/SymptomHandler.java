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
package cz.hashiri.harshlands.disease.symptom;

import org.bukkit.entity.Player;

public interface SymptomHandler {
    /** Applied once per progression check while the stage is active. */
    void apply(Player player, SymptomContext ctx);

    /** Called when the stage is left or the disease is cured. Default: nothing to undo. */
    default void clear(Player player, SymptomContext ctx) {}
}
