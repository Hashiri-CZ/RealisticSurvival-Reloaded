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

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-player registry pairing an Adventure-internal bossbar UUID with our
 * anchor instance. Used by the bossbar Sentry to distinguish our anchor's
 * outbound ADD packet from foreign plugins' bossbar packets.
 *
 * <p>Workflow: {@link #markPending} is called on the Bukkit main thread just
 * before {@code audience.showBossBar(anchor)}. The Sentry, on the next ADD it
 * observes for that player on the Netty thread, calls {@link #tryConsumeMarker};
 * the first call after a {@code markPending} captures the inflight UUID and
 * stores it as the player's anchor.</p>
 *
 * <p>Thread-safe via {@link ConcurrentHashMap}.</p>
 */
public class AnchorRegistry {

    private final ConcurrentHashMap<UUID, UUID> anchorByPlayer = new ConcurrentHashMap<>();
    private final Set<UUID> pendingMarkers = ConcurrentHashMap.newKeySet();

    /** Mark that the next ADD packet observed for this player should be paired with our anchor. */
    public void markPending(UUID playerUuid) {
        pendingMarkers.add(playerUuid);
    }

    /**
     * Atomically consume the pending marker (if any) and store {@code inflightUuid}
     * as the player's anchor.
     *
     * @return true if a marker was pending and the UUID was stored
     */
    public boolean tryConsumeMarker(UUID playerUuid, UUID inflightUuid) {
        if (!pendingMarkers.remove(playerUuid)) return false;
        anchorByPlayer.put(playerUuid, inflightUuid);
        return true;
    }

    /** @return the captured anchor UUID for this player, or empty if none. */
    public Optional<UUID> getAnchor(UUID playerUuid) {
        return Optional.ofNullable(anchorByPlayer.get(playerUuid));
    }

    /** Convenience: true if {@code uuid} is the player's recorded anchor. */
    public boolean isAnchor(UUID playerUuid, UUID uuid) {
        UUID anchor = anchorByPlayer.get(playerUuid);
        return anchor != null && anchor.equals(uuid);
    }

    /** Drop both anchor and marker for a player (e.g. on quit). */
    public void clear(UUID playerUuid) {
        anchorByPlayer.remove(playerUuid);
        pendingMarkers.remove(playerUuid);
    }
}
