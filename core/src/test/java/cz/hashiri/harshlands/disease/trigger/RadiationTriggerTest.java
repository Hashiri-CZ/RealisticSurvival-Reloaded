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

class RadiationTriggerTest {
    private static final Set<Material> RAD = EnumSet.of(Material.MAGMA_BLOCK, Material.SCULK);

    @Test void exposed_when_a_nearby_block_is_irradiated() {
        assertTrue(RadiationTrigger.exposed(Set.of(Material.DIRT, Material.MAGMA_BLOCK), null, RAD));
    }
    @Test void exposed_when_holding_an_irradiated_item() {
        assertTrue(RadiationTrigger.exposed(Set.of(Material.DIRT), Material.SCULK, RAD));
    }
    @Test void not_exposed_when_nothing_matches() {
        assertFalse(RadiationTrigger.exposed(Set.of(Material.DIRT, Material.STONE), Material.APPLE, RAD));
    }
    @Test void not_exposed_with_empty_surroundings_and_no_item() {
        assertFalse(RadiationTrigger.exposed(Set.of(), null, RAD));
    }
}
