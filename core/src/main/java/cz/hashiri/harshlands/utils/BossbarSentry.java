/*
    Copyright (C) 2025  Hashiri_
    GPL-3.0-or-later — see InternalsProvider.java for full text.
 */
package cz.hashiri.harshlands.utils;

import cz.hashiri.harshlands.HLPlugin;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.util.UUID;

/**
 * Read-only outbound packet observer. Non-version-specific. Subclasses
 * supply {@link #parseAddUuid} which extracts the UUID from a bossbar
 * ADD packet (or returns null if the packet is not a bossbar ADD).
 *
 * <p>For each ADD whose UUID is not the player's anchor, this base
 * schedules a debounced reshow via {@link BossbarReorderScheduler}.
 * Never modifies any packet.</p>
 */
public abstract class BossbarSentry extends ChannelDuplexHandler {

    private final HLPlugin plugin;
    private final UUID playerUuid;

    protected BossbarSentry(HLPlugin plugin, UUID playerUuid) {
        this.plugin = plugin;
        this.playerUuid = playerUuid;
    }

    @Override
    public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        try {
            UUID addUuid = parseAddUuid(msg);
            if (addUuid != null) handleAdd(addUuid);
        } catch (Throwable t) {
            plugin.getLogger().fine("Bossbar sentry inspection failed: " + t);
        }
        super.write(ctx, msg, promise);
    }

    /**
     * @param msg the outbound packet object passed to the Netty pipeline
     * @return UUID of the bossbar ADD, or {@code null} if {@code msg} is not
     *         an outbound bossbar ADD packet
     */
    protected abstract UUID parseAddUuid(Object msg);

    private void handleAdd(UUID uuid) {
        AnchorRegistry registry = plugin.getAnchorRegistry();
        if (registry.tryConsumeMarker(playerUuid, uuid)) return;
        if (registry.isAnchor(playerUuid, uuid)) return;
        plugin.getBossbarReorderScheduler().requestReshow(playerUuid);
    }
}
