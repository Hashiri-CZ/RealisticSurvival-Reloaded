package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.api.player.HudPlayer;
import cz.hashiri.harshlands.api.player.PlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

final class PlayerManagerImpl implements PlayerManager {

    @Override
    public HudPlayer getHudPlayer(UUID uuid) {
        Player p = Bukkit.getPlayer(uuid);
        if (p == null || !p.isOnline()) return null;
        return new HudPlayerImpl(p);
    }
}
