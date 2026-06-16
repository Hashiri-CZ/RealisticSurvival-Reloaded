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
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class RandomTeleportHandlerTest {
    @Test void offset_is_within_radius_and_nonzero_horizontal() {
        Random seeded = new Random(42L);
        for (int i = 0; i < 100; i++) {
            int[] off = RandomTeleportHandler.pickOffset(seeded, 8);
            assertTrue(Math.abs(off[0]) <= 8, "dx within radius");
            assertTrue(Math.abs(off[1]) <= 8, "dz within radius");
        }
    }
    @Test void zero_radius_yields_zero_offset() {
        int[] off = RandomTeleportHandler.pickOffset(new Random(1L), 0);
        assertEquals(0, off[0]);
        assertEquals(0, off[1]);
    }
}
