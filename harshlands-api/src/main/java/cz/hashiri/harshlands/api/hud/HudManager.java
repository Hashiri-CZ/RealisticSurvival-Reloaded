package cz.hashiri.harshlands.api.hud;

import java.util.Collection;

/**
 * Looks up registered {@link Hud} instances by id.
 * Mirrors BetterHud's {@code kr.toxicity.hud.api.hud.HudManager}.
 */
public interface HudManager {
    /** @return the hud registered under {@code id}, or {@code null}. */
    Hud getHud(String id);

    /**
     * @return an unmodifiable snapshot of all currently-registered huds.
     *         Safe to iterate without external synchronization; reflects the
     *         registration state at call time and does not update if huds
     *         are added or removed afterwards.
     */
    Collection<Hud> getHuds();
}
