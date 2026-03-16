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
package cz.hashiri.harshlands.dynamicsurroundings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player mutable state for the DynamicSurroundings module.
 * Populated on first use, cleared on PlayerQuitEvent.
 */
public class DSPlayerState {

    private static final Map<UUID, DSPlayerState> states = new ConcurrentHashMap<>();

    /** Horizontal distance accumulated since last footstep sound. */
    double distanceAccumulator;
    /** Player position on last processed block-crossing move event. */
    double lastX, lastZ;
    /** Ground state from the previous movement event. */
    boolean wasOnGround;
    /** Maximum fall distance recorded while airborne; reset on landing. */
    float maxFallDistance;
    /** Running step count used for armor "foot" sounds every 3 steps. */
    int stepCount;
    /** Cooldown tracking for item sounds: key → last-played epoch ms. */
    final Map<String, Long> itemSoundCooldowns = new HashMap<>();
    /** Cooldown tracking for ambient sounds: category → last-played epoch ms. */
    final Map<String, Long> ambientLastPlayed = new HashMap<>();

    static DSPlayerState getOrCreate(UUID uuid) {
        return states.computeIfAbsent(uuid, k -> new DSPlayerState());
    }

    static void remove(UUID uuid) {
        states.remove(uuid);
    }

    static Map<UUID, DSPlayerState> getStates() {
        return states;
    }
}
