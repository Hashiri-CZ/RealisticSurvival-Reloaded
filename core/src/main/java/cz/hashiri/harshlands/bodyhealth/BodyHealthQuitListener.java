package cz.hashiri.harshlands.bodyhealth;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.function.Consumer;

final class BodyHealthQuitListener implements Listener {

    private final Consumer<UUID> onQuit;

    BodyHealthQuitListener(Consumer<UUID> onQuit) {
        this.onQuit = onQuit;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        onQuit.accept(e.getPlayer().getUniqueId());
    }
}
