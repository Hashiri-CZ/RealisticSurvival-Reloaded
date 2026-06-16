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

/** Pure helpers for contraction-chance modification (immune suppression). */
public final class ContractionMath {

    private ContractionMath() {}

    /** Returns {@code multiplier} while not expired and positive; otherwise 1.0 (no effect). */
    public static double effectiveMultiplier(double multiplier, long expiryMs, long nowMs) {
        if (multiplier <= 0.0) return 1.0;
        return nowMs < expiryMs ? multiplier : 1.0;
    }

    /** Clamp a probability into [0, 1]. */
    public static double clampChance(double chance) {
        if (chance < 0.0) return 0.0;
        if (chance > 1.0) return 1.0;
        return chance;
    }
}
