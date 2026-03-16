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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Repeating task that evaluates ambient sounds for all online players.
 * Scheduled via {@code runTaskTimer(plugin, 100L, intervalTicks)}.
 */
public class AmbientTask extends BukkitRunnable {

    private final DynamicSurroundingsModule module;
    private final AmbientSoundHandler handler;

    public AmbientTask(DynamicSurroundingsModule module, AmbientSoundHandler handler) {
        this.module = module;
        this.handler = handler;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOnline()) {
                handler.evaluate(player);
            }
        }
    }
}
