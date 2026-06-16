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
package cz.hashiri.harshlands.disease.mitigation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NoExposureMitigationTest {
    @Test void mitigating_when_total_trigger_chance_is_zero() {
        assertTrue(NoExposureMitigation.mitigating(0.0));
    }
    @Test void not_mitigating_when_any_exposure() {
        assertFalse(NoExposureMitigation.mitigating(0.02));
        assertFalse(NoExposureMitigation.mitigating(1.0));
    }
    @Test void negative_treated_as_no_exposure() {
        assertTrue(NoExposureMitigation.mitigating(-0.1));
    }
}
