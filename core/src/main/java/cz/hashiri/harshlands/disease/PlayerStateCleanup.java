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
package cz.hashiri.harshlands.disease;

import java.util.UUID;

/**
 * Opt-in contract for disease components that hold per-player state in a long-lived map
 * (triggers with a pending-exposure or counter map, the special-symptom tracker, etc.).
 *
 * <p>{@link DiseaseModule} discovers every registered {@link cz.hashiri.harshlands.disease.trigger.DiseaseTrigger}
 * and its {@code SpecialSymptomTracker} that implement this interface and clears them on
 * {@code PlayerQuitEvent}, so a future stateful trigger opts into quit cleanup automatically by
 * implementing {@code clearPlayer} — nobody has to remember to add it to a growing list by hand.
 */
public interface PlayerStateCleanup {
    /** Remove all state held for this player. Safe to call even if the player has no state. */
    void clearPlayer(UUID uuid);
}
