package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.api.hud.Hud;
import cz.hashiri.harshlands.api.player.HudPlayer;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HudManagerImplTest {

    private static Hud stubHud(String id) {
        return new Hud() {
            @Override public String  id()                       { return id; }
            @Override public boolean add(HudPlayer p)           { return false; }
            @Override public boolean remove(HudPlayer p)        { return false; }
            @Override public boolean isShownTo(HudPlayer p)     { return false; }
        };
    }

    @Test void getHud_returns_registered_hud() {
        Hud bodyhealth = stubHud("bodyhealth");
        HudManagerImpl mgr = new HudManagerImpl(Map.of("bodyhealth", bodyhealth));
        assertSame(bodyhealth, mgr.getHud("bodyhealth"));
    }

    @Test void getHud_unknown_id_returns_null() {
        HudManagerImpl mgr = new HudManagerImpl(Map.of("bodyhealth", stubHud("bodyhealth")));
        assertNull(mgr.getHud("unknown"));
    }

    @Test void getHud_null_id_returns_null_not_throws() {
        HudManagerImpl mgr = new HudManagerImpl(Map.of("bodyhealth", stubHud("bodyhealth")));
        assertNull(mgr.getHud(null));
    }

    @Test void getHuds_does_not_track_caller_mutation_of_source_map() {
        Map<String, Hud> source = new HashMap<>();
        source.put("bodyhealth", stubHud("bodyhealth"));
        HudManagerImpl mgr = new HudManagerImpl(source);

        // Mutate the source map AFTER construction.
        source.put("late_addition", stubHud("late_addition"));

        // Snapshot contract: the manager should not surface the late addition.
        assertEquals(1, mgr.getHuds().size());
        assertNull(mgr.getHud("late_addition"));
    }

    @Test void getHuds_snapshot_is_unmodifiable() {
        HudManagerImpl mgr = new HudManagerImpl(Map.of("bodyhealth", stubHud("bodyhealth")));
        assertThrows(UnsupportedOperationException.class, () -> mgr.getHuds().clear());
    }
}
