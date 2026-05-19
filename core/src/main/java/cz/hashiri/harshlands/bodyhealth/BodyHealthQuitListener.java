package cz.hashiri.harshlands.bodyhealth;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Lifecycle listener: clears module state on quit, and on respawn nudges the
 * render task to re-emit the bossbar title (the Mojang client clears bossbars
 * client-side on respawn, but the render task would otherwise skip re-sending
 * because the cached state map is still equal).
 */
final class BodyHealthQuitListener implements Listener {

    private final Consumer<UUID> onQuit;
    private final Consumer<UUID> onRespawn;

    BodyHealthQuitListener(Consumer<UUID> onQuit, Consumer<UUID> onRespawn) {
        this.onQuit = onQuit;
        this.onRespawn = onRespawn;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        onQuit.accept(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {
        onRespawn.accept(e.getPlayer().getUniqueId());
    }
}
