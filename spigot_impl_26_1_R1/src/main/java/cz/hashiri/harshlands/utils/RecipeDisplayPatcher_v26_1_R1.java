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

import cz.hashiri.harshlands.HLPlugin;
import cz.hashiri.harshlands.utils.recipe.RecipeDisplayRegistry;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class RecipeDisplayPatcher_v26_1_R1 implements Listener {

    private static final String HANDLER_NAME = "harshlands_recipe_display";

    private static final Field CONNECTION_FIELD;
    static {
        try {
            CONNECTION_FIELD = ServerCommonPacketListenerImpl.class.getDeclaredField("connection");
            CONNECTION_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    final HLPlugin plugin;
    final RecipeDisplayRegistry registry;

    private final Set<UUID> warnedPlayers = ConcurrentHashMap.newKeySet();
    final AtomicInteger rewriteFailureCount = new AtomicInteger();

    public RecipeDisplayPatcher_v26_1_R1(HLPlugin plugin, RecipeDisplayRegistry registry) {
        this.plugin = plugin;
        this.registry = registry;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        Player bukkitPlayer = event.getPlayer();
        try {
            Channel channel = resolveChannel(bukkitPlayer);
            if (channel.pipeline().get(HANDLER_NAME) == null) {
                channel.pipeline().addBefore("packet_handler", HANDLER_NAME, new RewriteHandler());
            }
        } catch (Throwable t) {
            UUID uuid = bukkitPlayer.getUniqueId();
            if (warnedPlayers.add(uuid)) {
                plugin.getLogger().log(Level.WARNING,
                        "Recipe display patcher could not be installed for player " + uuid + ".", t);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        try {
            Channel channel = resolveChannel(event.getPlayer());
            if (channel.pipeline().get(HANDLER_NAME) != null) {
                channel.pipeline().remove(HANDLER_NAME);
            }
        } catch (Throwable ignored) {
            // pipeline already torn down by server shutdown — nothing to do
        }
    }

    private Channel resolveChannel(Player bukkitPlayer) throws IllegalAccessException {
        ServerPlayer serverPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
        ServerGamePacketListenerImpl listener = serverPlayer.connection;
        Connection connection = (Connection) CONNECTION_FIELD.get(listener);
        return connection.channel;
    }

    /**
     * Outbound-rewrite handler. Filled in by Task 11.
     */
    private final class RewriteHandler extends ChannelDuplexHandler {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            // Task 11 implements rewrite logic here.
            super.write(ctx, msg, promise);
        }
    }
}
