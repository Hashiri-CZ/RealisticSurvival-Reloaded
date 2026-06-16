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

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which event-driven special symptoms are currently active for each player.
 * The progression tick calls {@link #mark} each check; Bukkit event callbacks call
 * {@link #isActive}/{@link #chance}. Entries carry a wall-clock expiry (ms) so a missed
 * {@link #clear} self-heals within one check interval. Keyed by handler name; if two
 * diseases drive the same handler in one tick, the last mark wins for that cycle.
 */
public final class SpecialSymptomTracker {

    private record Entry(long expiryMs, double chance) {}

    private final Map<UUID, Map<String, Entry>> active = new ConcurrentHashMap<>();

    public void mark(UUID player, String symptomKey, long expiryMs, double chance) {
        active.computeIfAbsent(player, k -> new ConcurrentHashMap<>())
              .put(symptomKey, new Entry(expiryMs, chance));
    }

    public boolean isActive(UUID player, String symptomKey, long nowMs) {
        Map<String, Entry> byKey = active.get(player);
        if (byKey == null) return false;
        Entry e = byKey.get(symptomKey);
        return e != null && nowMs < e.expiryMs();
    }

    /** The chance value recorded at the last mark, or 0 if inactive. */
    public double chance(UUID player, String symptomKey, long nowMs) {
        Map<String, Entry> byKey = active.get(player);
        if (byKey == null) return 0.0;
        Entry e = byKey.get(symptomKey);
        return (e != null && nowMs < e.expiryMs()) ? e.chance() : 0.0;
    }

    public void clear(UUID player, String symptomKey) {
        Map<String, Entry> byKey = active.get(player);
        if (byKey != null) byKey.remove(symptomKey);
    }

    public void clearPlayer(UUID player) {
        active.remove(player);
    }
}
