package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.api.HarshlandsAPI;
import cz.hashiri.harshlands.api.hud.HudManager;
import cz.hashiri.harshlands.api.player.PlayerManager;

import java.io.File;

final class HarshlandsAPIImpl extends HarshlandsAPI {

    private final PlayerManager playerManager;
    private final HudManager    hudManager;
    private final File          dataFolder;

    HarshlandsAPIImpl(PlayerManager playerManager, HudManager hudManager, File dataFolder) {
        this.playerManager = playerManager;
        this.hudManager    = hudManager;
        this.dataFolder    = dataFolder;
    }

    @Override public PlayerManager getPlayerManager() { return playerManager; }
    @Override public HudManager    getHudManager()    { return hudManager; }
    @Override public File          dataFolder()       { return dataFolder; }
}
