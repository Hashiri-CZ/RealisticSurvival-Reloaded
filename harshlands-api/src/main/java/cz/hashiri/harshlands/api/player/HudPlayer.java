package cz.hashiri.harshlands.api.player;

import org.bukkit.entity.Player;
import java.util.UUID;

/**
 * A wrapper around a Bukkit {@link Player} used by HUD operations.
 * Mirrors the role of BetterHud's {@code kr.toxicity.hud.api.player.HudPlayer}.
 *
 * <p>Obtain instances via {@link PlayerManager#getHudPlayer(UUID)}.</p>
 */
public interface HudPlayer {
    UUID uuid();
    Player bukkitPlayer();
}
