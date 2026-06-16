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
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class SpecialSymptomTrackerTest {
    private final UUID p = UUID.randomUUID();

    @Test void marked_symptom_is_active_before_expiry() {
        SpecialSymptomTracker t = new SpecialSymptomTracker();
        t.mark(p, "ItemUseFailure", 1000L /*expiry*/, 0.5);
        assertTrue(t.isActive(p, "ItemUseFailure", 999L));
    }
    @Test void symptom_inactive_at_or_after_expiry() {
        SpecialSymptomTracker t = new SpecialSymptomTracker();
        t.mark(p, "ItemUseFailure", 1000L, 0.5);
        assertFalse(t.isActive(p, "ItemUseFailure", 1000L));
        assertFalse(t.isActive(p, "ItemUseFailure", 1500L));
    }
    @Test void unknown_symptom_is_inactive() {
        SpecialSymptomTracker t = new SpecialSymptomTracker();
        assertFalse(t.isActive(p, "Nope", 0L));
    }
    @Test void chance_returns_marked_value_then_zero_after_clear() {
        SpecialSymptomTracker t = new SpecialSymptomTracker();
        t.mark(p, "ItemUseFailure", 1000L, 0.75);
        assertEquals(0.75, t.chance(p, "ItemUseFailure", 0L), 1e-9);
        t.clear(p, "ItemUseFailure");
        assertFalse(t.isActive(p, "ItemUseFailure", 0L));
        assertEquals(0.0, t.chance(p, "ItemUseFailure", 0L), 1e-9);
    }
    @Test void remark_extends_expiry() {
        SpecialSymptomTracker t = new SpecialSymptomTracker();
        t.mark(p, "BlockNaturalRegen", 1000L, 0.0);
        t.mark(p, "BlockNaturalRegen", 2000L, 0.0);
        assertTrue(t.isActive(p, "BlockNaturalRegen", 1500L));
    }
}
