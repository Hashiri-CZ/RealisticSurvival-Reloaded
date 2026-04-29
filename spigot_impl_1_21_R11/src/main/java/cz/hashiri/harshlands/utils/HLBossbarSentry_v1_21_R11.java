/*
    Copyright (C) 2025  Hashiri_
    GPL-3.0-or-later — see InternalsProvider.java for full text.
 */
package cz.hashiri.harshlands.utils;

import cz.hashiri.harshlands.HLPlugin;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;

import java.util.UUID;

/**
 * Read-only outbound packet observer. For each ClientboundBossEventPacket
 * with op=ADD whose UUID is not the player's anchor, schedules a debounced
 * reshow via {@link BossbarReorderScheduler}. Never modifies any packet.
 *
 * <p>Pipeline name: {@code harshlands_bossbar_sentry}. Inserted before the
 * Connection handler.</p>
 */
public final class HLBossbarSentry_v1_21_R11 extends ChannelDuplexHandler {

    private static final java.lang.reflect.Field ID_FIELD;
    private static final java.lang.reflect.Field OPERATION_FIELD;

    static {
        java.lang.reflect.Field id = null;
        java.lang.reflect.Field op = null;
        for (java.lang.reflect.Field f : ClientboundBossEventPacket.class.getDeclaredFields()) {
            Class<?> t = f.getType();
            if (t == java.util.UUID.class && id == null) {
                f.setAccessible(true);
                id = f;
            } else if (t.isInterface() && id != null && op == null) {
                // Operation is a sealed interface in current Mojang mappings.
                f.setAccessible(true);
                op = f;
            } else if (t.isEnum() && op == null) {
                // Older mappings: Operation is an inner enum.
                f.setAccessible(true);
                op = f;
            }
        }
        if (id == null || op == null) {
            throw new IllegalStateException(
                    "Could not locate ClientboundBossEventPacket id/operation fields");
        }
        ID_FIELD = id;
        OPERATION_FIELD = op;
    }

    private final UUID playerUuid;
    private final HLPlugin plugin;

    public HLBossbarSentry_v1_21_R11(HLPlugin plugin, UUID playerUuid) {
        this.plugin = plugin;
        this.playerUuid = playerUuid;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ClientboundBossEventPacket pkt) {
            try {
                inspect(pkt);
            } catch (Throwable t) {
                plugin.getLogger().fine("Bossbar sentry inspection failed: " + t);
            }
        }
        super.write(ctx, msg, promise);
    }

    private void inspect(ClientboundBossEventPacket pkt) throws IllegalAccessException {
        Object op = OPERATION_FIELD.get(pkt);
        if (op == null) return;
        // Match by simple name on the operation's class. ADD ops are `AddOperation`
        // (sealed-interface mappings) or the `ADD` enum constant (legacy mappings).
        String opName = op.getClass().getSimpleName();
        boolean isAdd = "AddOperation".equals(opName)
                || (op instanceof Enum<?> e && "ADD".equals(e.name()));
        if (!isAdd) return;

        UUID uuid = (UUID) ID_FIELD.get(pkt);
        if (uuid == null) return;

        AnchorRegistry registry = plugin.getAnchorRegistry();
        if (registry.tryConsumeMarker(playerUuid, uuid)) return;
        if (registry.isAnchor(playerUuid, uuid)) return;

        plugin.getBossbarReorderScheduler().requestReshow(playerUuid);
    }
}
