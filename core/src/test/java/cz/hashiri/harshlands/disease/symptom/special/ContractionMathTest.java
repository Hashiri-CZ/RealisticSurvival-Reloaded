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

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContractionMathTest {
    @Test void multiplier_applies_before_expiry() {
        assertEquals(2.0, ContractionMath.effectiveMultiplier(2.0, 1000L, 500L), 1e-9);
    }
    @Test void multiplier_reverts_to_one_at_or_after_expiry() {
        assertEquals(1.0, ContractionMath.effectiveMultiplier(2.0, 1000L, 1000L), 1e-9);
        assertEquals(1.0, ContractionMath.effectiveMultiplier(2.0, 1000L, 2000L), 1e-9);
    }
    @Test void nonpositive_multiplier_is_ignored() {
        assertEquals(1.0, ContractionMath.effectiveMultiplier(0.0, 1000L, 0L), 1e-9);
        assertEquals(1.0, ContractionMath.effectiveMultiplier(-3.0, 1000L, 0L), 1e-9);
    }
    @Test void clamp_keeps_chance_in_unit_interval() {
        assertEquals(0.0, ContractionMath.clampChance(-0.5), 1e-9);
        assertEquals(1.0, ContractionMath.clampChance(1.7), 1e-9);
        assertEquals(0.3, ContractionMath.clampChance(0.3), 1e-9);
    }
}
