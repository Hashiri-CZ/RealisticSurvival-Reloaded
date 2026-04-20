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

import cz.hashiri.harshlands.HLPlugin;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class DynamicSurroundingsEvents implements Listener {

    // Stable UUID for the Dynamic Surroundings sounds pack slot
    private static final UUID PACK_UUID = UUID.fromString("8f4e3d2c-1b0a-9f8e-7d6c-5b4a3f2e1d0c");

    private final DynamicSurroundingsModule module;
    private final HLPlugin plugin;
    private final FootstepHandler footstepHandler;
    private final ItemSoundHandler itemSoundHandler;

    public DynamicSurroundingsEvents(DynamicSurroundingsModule module, HLPlugin plugin,
                                     FootstepHandler footstepHandler, ItemSoundHandler itemSoundHandler) {
        this.module = module;
        this.plugin = plugin;
        this.footstepHandler = footstepHandler;
        this.itemSoundHandler = itemSoundHandler;
    }

    // -----------------------------------------------------------------------
    // Resource pack delivery
    // -----------------------------------------------------------------------

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!module.isEnabled(player.getWorld())) {
            return;
        }

        String url = module.getPackUrl();
        if (url == null || url.isBlank()) {
            return;
        }

        player.addResourcePack(PACK_UUID, url, null, null, false);
        plugin.getLogger().info("Sent Dynamic Surroundings resource pack to player " + player.getName() + ".");
    }

    // -----------------------------------------------------------------------
    // Footstep sounds
    // -----------------------------------------------------------------------

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (footstepHandler == null) return;
        Player player = event.getPlayer();
        if (!module.isEnabled(player.getWorld())) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        footstepHandler.handleMove(player, event.getFrom(), event.getTo());
    }

    // -----------------------------------------------------------------------
    // Item sounds
    // -----------------------------------------------------------------------

    @EventHandler(ignoreCancelled = true)
    public void onItemHeld(PlayerItemHeldEvent event) {
        if (itemSoundHandler == null) return;
        Player player = event.getPlayer();
        if (!module.isEnabled(player.getWorld())) return;
        itemSoundHandler.handleEquip(player, event.getNewSlot());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (itemSoundHandler == null) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!module.isEnabled(player.getWorld())) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        itemSoundHandler.handleSwing(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event) {
        if (itemSoundHandler == null) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!module.isEnabled(player.getWorld())) return;
        itemSoundHandler.handleBowLoose(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (itemSoundHandler == null) return;
        Player player = event.getPlayer();
        if (!module.isEnabled(player.getWorld())) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            itemSoundHandler.handleSwing(player);
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            itemSoundHandler.handleInteract(player, event);
        }
    }

    // -----------------------------------------------------------------------
    // Cleanup
    // -----------------------------------------------------------------------

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        DSPlayerState.remove(event.getPlayer().getUniqueId());
    }
}
