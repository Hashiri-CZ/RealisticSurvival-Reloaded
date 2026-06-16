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

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class EnderExposureTriggerTest {
    private static final Set<Material> ENDER = EnumSet.of(
        Material.ENDER_PEARL, Material.ENDER_EYE, Material.ENDER_CHEST);

    @Test void exposed_when_holding_a_configured_ender_item() {
        assertTrue(EnderExposureTrigger.exposed(Material.ENDER_PEARL, false, ENDER));
    }
    @Test void exposed_when_in_the_end_even_without_items() {
        assertTrue(EnderExposureTrigger.exposed(Material.DIRT, true, ENDER));
    }
    @Test void not_exposed_when_neither() {
        assertFalse(EnderExposureTrigger.exposed(Material.DIRT, false, ENDER));
    }
    @Test void not_exposed_for_unconfigured_item() {
        assertFalse(EnderExposureTrigger.exposed(Material.STONE, false, ENDER));
    }
}
