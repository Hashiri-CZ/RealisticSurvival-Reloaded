package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.api.HarshlandsAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class HarshlandsAPISingletonTest {

    @BeforeEach @AfterEach void clearSingleton() {
        HarshlandsAPI.register(null);
    }

    @Test void inst_returns_null_before_register() {
        assertNull(HarshlandsAPI.inst());
    }

    @Test void inst_returns_registered_instance() {
        HarshlandsAPIImpl impl = new HarshlandsAPIImpl(
                new PlayerManagerImpl(),
                new HudManagerImpl(new HashMap<>()),
                new File("."));
        HarshlandsAPI.register(impl);
        assertSame(impl, HarshlandsAPI.inst());
    }

    @Test void register_null_clears_instance() {
        HarshlandsAPIImpl impl = new HarshlandsAPIImpl(
                new PlayerManagerImpl(),
                new HudManagerImpl(new HashMap<>()),
                new File("."));
        HarshlandsAPI.register(impl);
        HarshlandsAPI.register(null);
        assertNull(HarshlandsAPI.inst());
    }

    @Test void getters_return_constructor_args() {
        PlayerManagerImpl pm = new PlayerManagerImpl();
        HudManagerImpl hm = new HudManagerImpl(new HashMap<>());
        File df = new File("/tmp/hl");
        HarshlandsAPIImpl impl = new HarshlandsAPIImpl(pm, hm, df);
        assertSame(pm, impl.getPlayerManager());
        assertSame(hm, impl.getHudManager());
        assertSame(df, impl.dataFolder());
    }
}
