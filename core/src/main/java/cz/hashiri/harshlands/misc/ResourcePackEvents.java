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
package cz.hashiri.harshlands.misc;

import cz.hashiri.harshlands.rsv.HLPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import java.util.UUID;
import java.util.logging.Logger;

public class ResourcePackEvents implements Listener {

    // Stable UUID for the main Harshlands resource pack slot
    private static final UUID MAIN_PACK_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    private final HLPlugin plugin;

    public ResourcePackEvents(HLPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Logger logger = plugin.getLogger();

        String packUrl = plugin.getConfig().getString("ResourcePack.Url");
        if (packUrl == null || packUrl.isBlank()) {
            logger.warning("ResourcePack.Enabled is true but ResourcePack.Url is empty. Skipping resource pack send.");
            return;
        }

        // Use addResourcePack so multiple packs (e.g. DynamicSurroundings) can coexist.
        // setResourcePack() removes all previously queued packs before adding the new one.
        player.addResourcePack(MAIN_PACK_UUID, packUrl, null, null, false);
        logger.info("Sent resource pack URL to player " + player.getName() + ".");
    }
}

