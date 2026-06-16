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
package cz.hashiri.harshlands.tan;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * Fired when a player drinks raw (unpurified) water directly from a world source block via the
 * TAN drink action. Consumed by the disease module's UnpurifiedWaterTrigger (Dysentery).
 */
public class RawWaterDrinkEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final UUID id;

    public RawWaterDrinkEvent(Player player) {
        this.id = player.getUniqueId();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(id);
    }

    @Nonnull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
