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

import cz.hashiri.harshlands.bodyhealth.BodyPartState;
import org.bukkit.entity.Player;

/**
 * Supplies the worst (most severe) BodyHealth limb state for a player. Production reads
 * PlaceholderAPI ({@link PapiLimbStateReader}); unit tests stub it with a lambda.
 */
public interface LimbStateReader {
    BodyPartState worst(Player player);
}
