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
package cz.hashiri.harshlands.disease.symptom.builtin;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PotionEffectsTest {
    @Test void maps_legacy_slow_to_slowness() {
        assertEquals("slowness", PotionEffects.normalizeName("SLOW"));
    }
    @Test void maps_legacy_slow_digging_to_mining_fatigue() {
        assertEquals("mining_fatigue", PotionEffects.normalizeName("SLOW_DIGGING"));
    }
    @Test void maps_legacy_confusion_to_nausea() {
        assertEquals("nausea", PotionEffects.normalizeName("CONFUSION"));
    }
    @Test void is_case_insensitive() {
        assertEquals("slowness", PotionEffects.normalizeName("slow"));
    }
    @Test void modern_name_passes_through_lowercased() {
        assertEquals("weakness", PotionEffects.normalizeName("WEAKNESS"));
        assertEquals("mining_fatigue", PotionEffects.normalizeName("mining_fatigue"));
    }
    @Test void null_or_blank_yields_empty() {
        assertEquals("", PotionEffects.normalizeName(null));
        assertEquals("", PotionEffects.normalizeName("   "));
    }
}
