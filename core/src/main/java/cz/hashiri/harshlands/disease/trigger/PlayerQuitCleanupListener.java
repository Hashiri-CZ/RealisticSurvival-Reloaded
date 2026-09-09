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
package cz.hashiri.harshlands.disease.trigger;

import cz.hashiri.harshlands.disease.PlayerStateCleanup;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.UUID;

/**
 * Single quit-cleanup path for the disease module's per-player state maps.
 * {@code cz.hashiri.harshlands.disease.DiseaseModule} builds this from every registered
 * {@link DiseaseTrigger}/tracker that implements
 * {@link PlayerStateCleanup} and registers it once; a future stateful trigger opts in just by
 * implementing that interface rather than requiring someone to remember to edit a growing list
 * here or in {@code onDisconnect} handling.
 */
public final class PlayerQuitCleanupListener implements Listener {

    private final List<PlayerStateCleanup> targets;

    public PlayerQuitCleanupListener(List<PlayerStateCleanup> targets) {
        this.targets = targets;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        for (PlayerStateCleanup target : targets) {
            target.clearPlayer(uuid);
        }
    }
}
