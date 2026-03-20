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
package cz.hashiri.harshlands.comfort;

import javax.annotation.Nonnull;

public enum ComfortTier {

    NONE("None"),
    SHELTER("Shelter"),
    HOME("Home"),
    COZY("Cozy Home"),
    LUXURY("Luxury");

    @Nonnull
    private final String displayName;

    ComfortTier(@Nonnull String displayName) {
        this.displayName = displayName;
    }

    @Nonnull
    public String getDisplayName() {
        return displayName;
    }
}
