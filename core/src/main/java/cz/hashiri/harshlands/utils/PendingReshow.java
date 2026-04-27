/*
    Copyright (C) 2025  Hashiri_
    GPL-3.0-or-later — see InternalsProvider.java for full text.
 */
package cz.hashiri.harshlands.utils;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe set of player UUIDs flagged as needing a bossbar reshow.
 * Used by {@link BossbarReorderScheduler} to coalesce multiple within-tick
 * dirty signals into a single reshow at end-of-tick.
 */
public final class PendingReshow {

    private final ConcurrentHashMap<UUID, Boolean> dirty = new ConcurrentHashMap<>();

    /** @return true if this is the first dirty mark for this player (caller should schedule). */
    public boolean markDirty(UUID playerUuid) {
        return dirty.putIfAbsent(playerUuid, Boolean.TRUE) == null;
    }

    /** Reset state for a player after the reshow has been performed. */
    public void clear(UUID playerUuid) {
        dirty.remove(playerUuid);
    }
}
