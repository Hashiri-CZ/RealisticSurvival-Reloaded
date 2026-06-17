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

import java.util.Locale;

/** How a cure item resolves an infection. */
public enum CureMode {
    /** One use fully clears the infection (default; existing behaviour). */
    CLEAR,
    /** Each use regresses the infection by one stage, gated by a per-dose cooldown. */
    REGRESS_ONE_STAGE,
    /** One use clears the infection, but only before its terminal stage (Sybok). */
    CLEAR_BEFORE_TERMINAL;

    /** Parse a config string; unknown/blank/null falls back to {@link #CLEAR}. */
    public static CureMode fromConfig(String s) {
        if (s == null) return CLEAR;
        try {
            return CureMode.valueOf(s.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return CLEAR;
        }
    }
}
