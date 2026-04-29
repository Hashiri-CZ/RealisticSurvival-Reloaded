package cz.hashiri.harshlands.api;

import cz.hashiri.harshlands.api.hud.HudManager;
import cz.hashiri.harshlands.api.player.PlayerManager;

import java.io.File;

/**
 * Singleton entry point to the Harshlands HUD API. Mirrors BetterHud's
 * {@code kr.toxicity.hud.api.BetterHudAPI}.
 *
 * <p>Third-party plugins call {@link #inst()} to obtain the active instance.
 * Returns {@code null} if Harshlands is not loaded or the relevant module is
 * disabled in config — consumers MUST null-check.</p>
 *
 * <p><b>Lifecycle:</b> the instance returned by {@link #inst()} is replaced or
 * cleared on plugin reload and shutdown. Do NOT cache the result across a
 * {@link cz.hashiri.harshlands.api.bukkit.event.PluginReloadedEvent} — re-fetch
 * after each reload. A non-null return is valid for the current tick.</p>
 *
 * <p>Harshlands core calls {@link #register(HarshlandsAPI)} once during
 * {@code onEnable()}. Internal use only.</p>
 */
public abstract class HarshlandsAPI {

    private static volatile HarshlandsAPI instance;

    /**
     * @return the currently registered API implementation, or {@code null} if
     *         Harshlands is not loaded or the BodyHealth module is disabled.
     *         Consumers MUST null-check before chained calls.
     */
    public static HarshlandsAPI inst() {
        return instance;
    }

    /** Internal — called by Harshlands core. Pass {@code null} on shutdown. */
    public static void register(HarshlandsAPI impl) {
        instance = impl;
    }

    public abstract PlayerManager getPlayerManager();
    public abstract HudManager    getHudManager();

    /**
     * Harshlands' plugin data folder. Provided for parity with BetterHud's
     * {@code BetterHudAPI.inst().bootstrap().dataFolder()}; not used by the
     * stock BodyHealth integration (Harshlands ships its glyphs natively).
     */
    public abstract File dataFolder();
}
