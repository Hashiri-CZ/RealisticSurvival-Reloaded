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

import cz.hashiri.harshlands.rsv.HLPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class DynamicSurroundingsEvents implements Listener {

    // Stable UUID for the Dynamic Surroundings sounds pack slot
    private static final UUID PACK_UUID = UUID.fromString("8f4e3d2c-1b0a-9f8e-7d6c-5b4a3f2e1d0c");

    private final DynamicSurroundingsModule module;
    private final HLPlugin plugin;

    public DynamicSurroundingsEvents(DynamicSurroundingsModule module, HLPlugin plugin) {
        this.module = module;
        this.plugin = plugin;
    }

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

        player.addResourcePack(PACK_UUID, url, new byte[0], "", false);
        plugin.getLogger().info("Sent Dynamic Surroundings resource pack to player " + player.getName() + ".");
    }
}
