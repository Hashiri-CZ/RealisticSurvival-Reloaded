package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.api.player.HudPlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

final class HudPlayerImpl implements HudPlayer {

    private final Player player;

    HudPlayerImpl(Player player) {
        this.player = player;
    }

    @Override public UUID uuid() { return player.getUniqueId(); }
    @Override public Player bukkitPlayer() { return player; }
}
