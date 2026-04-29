package cz.hashiri.harshlands.bodyhealth;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import cz.hashiri.harshlands.HLPlugin;
import cz.hashiri.harshlands.api.HarshlandsAPI;
import cz.hashiri.harshlands.api.hud.Hud;
import cz.hashiri.harshlands.api.player.HudPlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration smoke. Disabled by default because HLPlugin.onEnable boots the
 * full module graph (database, recipes, etc.) which MockBukkit does not fully
 * support out of the box; flip @Disabled off locally to run interactively.
 */
@Disabled("Requires full HLPlugin enable; run manually after smoke testing locally")
class BodyHealthModuleLifecycleTest {

    private ServerMock server;
    private HLPlugin plugin;

    @BeforeEach void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(HLPlugin.class);
    }

    @AfterEach void tearDown() {
        MockBukkit.unmock();
    }

    @Test void api_is_registered_after_enable() {
        assertNotNull(HarshlandsAPI.inst());
        assertNotNull(HarshlandsAPI.inst().getHudManager().getHud("bodyhealth"));
    }

    @Test void hud_add_then_remove_toggles_state() {
        Hud hud = HarshlandsAPI.inst().getHudManager().getHud("bodyhealth");
        PlayerMock p = server.addPlayer();
        HudPlayer hp = HarshlandsAPI.inst().getPlayerManager().getHudPlayer(p.getUniqueId());
        assertNotNull(hp);
        assertTrue(hud.add(hp));
        assertTrue(hud.isShownTo(hp));
        assertTrue(hud.remove(hp));
        assertFalse(hud.isShownTo(hp));
    }

    @Test void disable_clears_singleton() {
        plugin.onDisable();
        assertNull(HarshlandsAPI.inst());
    }
}
