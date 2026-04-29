package cz.hashiri.harshlands.api.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired after {@code /hl reload} completes. Mirrors BetterHud's
 * {@code kr.toxicity.hud.api.bukkit.event.PluginReloadedEvent}.
 *
 * <p>Consumers should use this signal to re-issue any {@code Hud.add()} calls
 * for players that should remain visible — Harshlands' internal "shown" set
 * may have been reset during reload.</p>
 */
public class PluginReloadedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public PluginReloadedEvent() {
        super(false); // synchronous
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
