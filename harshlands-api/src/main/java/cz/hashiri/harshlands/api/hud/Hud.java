package cz.hashiri.harshlands.api.hud;

import cz.hashiri.harshlands.api.player.HudPlayer;

/**
 * A registered HUD identified by a string id. Mirrors BetterHud's
 * {@code kr.toxicity.hud.api.hud.Hud}.
 *
 * <p>Show or hide the HUD per-player via {@link #add(HudPlayer)} and
 * {@link #remove(HudPlayer)}. Both operations are idempotent and return
 * {@code true} only if the player's shown-state actually changed.</p>
 */
public interface Hud {
    String id();
    boolean add(HudPlayer player);
    boolean remove(HudPlayer player);
    boolean isShownTo(HudPlayer player);
}
