package cz.hashiri.harshlands.api.player;

import java.util.UUID;

/**
 * Resolves Bukkit player UUIDs to {@link HudPlayer} wrappers.
 * Mirrors the role of BetterHud's {@code kr.toxicity.hud.api.player.PlayerManager}.
 */
public interface PlayerManager {
    /**
     * @return the {@link HudPlayer} for {@code uuid}, or {@code null} if the player
     *         is offline or otherwise not tracked.
     */
    HudPlayer getHudPlayer(UUID uuid);
}
