/*
    Copyright (C) 2025  Hashiri_

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
package cz.hashiri.harshlands.utils;

import java.util.List;

/**
 * Decomposed translation template used to match locale-rendered lore lines.
 *
 * <p>Produced by {@link Utils#valueTemplateParts(String, String...)}. For a
 * template like {@code "&7Durability: %CURRENT%/%MAX%"} with placeholder
 * names {@code CURRENT, MAX}, segments are {@code ["§7Durability: ", "/", ""]}
 * — prefix, the literal text between placeholders, and trailing tail.</p>
 *
 * <p>Single-placeholder templates yield two segments (prefix + suffix), which
 * is the same shape the older {@code String[]}-returning helper produced.</p>
 */
public record TemplateParts(List<String> segments) {

    public TemplateParts {
        segments = List.copyOf(segments);
    }

    /**
     * Returns true if {@code line} could have been produced by rendering this
     * template with some placeholder values — i.e. the prefix, all middle
     * segments (in order), and the suffix all appear in {@code line}, with
     * arbitrary text between them.
     */
    public boolean matches(String line) {
        if (line == null) return false;
        if (segments.isEmpty()) return false;
        if (segments.size() == 1) return line.equals(segments.get(0));

        String prefix = segments.get(0);
        String suffix = segments.get(segments.size() - 1);
        if (!line.startsWith(prefix)) return false;
        if (!line.endsWith(suffix)) return false;
        // Need: prefix length + suffix length <= line length. Otherwise
        // the same indices would overlap (false positive on short input).
        if (prefix.length() + suffix.length() > line.length()) return false;

        int searchStart = prefix.length();
        int searchEnd = line.length() - suffix.length();
        for (int i = 1; i < segments.size() - 1; i++) {
            String mid = segments.get(i);
            int found = line.indexOf(mid, searchStart);
            if (found < 0 || found > searchEnd - mid.length()) return false;
            searchStart = found + mid.length();
        }
        return true;
    }
}
