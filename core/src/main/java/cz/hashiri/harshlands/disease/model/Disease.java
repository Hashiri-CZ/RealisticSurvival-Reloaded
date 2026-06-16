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
package cz.hashiri.harshlands.disease.model;

import java.util.List;

public record Disease(
    String id,
    String displayName,
    boolean enabled,
    long incubationTicks,
    long immunityDurationTicks,   // 0 = no immunity after cure
    String cureItemId,            // null/empty = no item cure
    String mitigationType,        // null/empty = no behavioral mitigation
    List<DiseaseStage> stages
) {
    public int maxStage() { return stages.size(); }

    /** stage index is 1-based; returns the stage definition or null if out of range. */
    public DiseaseStage stage(int oneBasedStage) {
        if (oneBasedStage < 1 || oneBasedStage > stages.size()) return null;
        return stages.get(oneBasedStage - 1);
    }
}
