/*
    Copyright (C) 2025  Hashiri_
    GPL-3.0-or-later — see InternalsProvider.java for full text.
 */
package cz.hashiri.harshlands.utils;

import cz.hashiri.harshlands.HLPlugin;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

    private static final Class<?> OPERATION_ENUM_CLASS;
    private static final Object OP_ADD;

    static {
        Class<?> opClass = null;
        Object addOp = null;
        for (Class<?> inner : ClientboundBossEventPacket.class.getDeclaredClasses()) {
            if (inner.isEnum()) {
                opClass = inner;
                addOp = inner.getEnumConstants()[0]; // ordinal 0 = ADD
                break;
            }
        }
        if (opClass == null) throw new IllegalStateException(
                "Could not locate ClientboundBossEventPacket Operation enum");
        OPERATION_ENUM_CLASS = opClass;
        OP_ADD = addOp;
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

    private void inspect(ClientboundBossEventPacket pkt) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        try {
            ClientboundBossEventPacket.STREAM_CODEC.encode(buf, pkt);
            buf.readerIndex(0);
            UUID uuid = buf.readUUID();
            Object op = buf.readEnum(OPERATION_ENUM_CLASS.asSubclass(Enum.class));
            if (op != OP_ADD) return;

            AnchorRegistry registry = plugin.getAnchorRegistry();
            if (registry.tryConsumeMarker(playerUuid, uuid)) return; // our own anchor's first ADD
            if (registry.isAnchor(playerUuid, uuid)) return;          // our anchor re-adding (after reshow)

            plugin.getBossbarReorderScheduler().requestReshow(playerUuid);
        } finally {
            buf.release();
        }
    }
}
