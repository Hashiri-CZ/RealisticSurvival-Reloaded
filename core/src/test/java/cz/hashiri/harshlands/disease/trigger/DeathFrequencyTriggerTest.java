package cz.hashiri.harshlands.disease.trigger;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DeathFrequencyTriggerTest {
    // diseaseId, deathThreshold, windowMs, chancePerCheck
    private DeathFrequencyTrigger trigger() {
        return new DeathFrequencyTrigger("battle_trauma", 3, 1000L, 0.5);
    }

    @Test void enough_deaths_predicate() {
        assertTrue(DeathFrequencyTrigger.enoughDeaths(3, 3));
        assertTrue(DeathFrequencyTrigger.enoughDeaths(4, 3));
        assertFalse(DeathFrequencyTrigger.enoughDeaths(2, 3));
    }
    @Test void fires_once_threshold_deaths_within_window() {
        DeathFrequencyTrigger t = trigger();
        UUID p = UUID.randomUUID();
        t.recordDeath(p, 0L);
        t.recordDeath(p, 100L);
        assertEquals(0.0, t.chanceFor(p, 200L), 1e-9); // only 2 deaths
        t.recordDeath(p, 200L);
        assertEquals(0.5, t.chanceFor(p, 250L), 1e-9); // 3 deaths within window
    }
    @Test void deaths_age_out_of_window() {
        DeathFrequencyTrigger t = trigger();
        UUID p = UUID.randomUUID();
        t.recordDeath(p, 0L);
        t.recordDeath(p, 1L);
        t.recordDeath(p, 2L);
        assertEquals(0.5, t.chanceFor(p, 2L), 1e-9);      // 3 within window
        assertEquals(0.0, t.chanceFor(p, 1003L), 1e-9);   // all older than now-window(=3) -> pruned
    }
    @Test void unknown_player_zero() {
        assertEquals(0.0, trigger().chanceFor(UUID.randomUUID(), 0L), 1e-9);
    }
    @Test void disease_id_reported() {
        assertEquals("battle_trauma", trigger().diseaseId());
    }

    // --- quit cleanup (PlayerStateCleanup): a departing player's death history must not
    // linger in the map forever ---
    @Test void clear_player_drops_death_history() {
        DeathFrequencyTrigger t = trigger();
        UUID p = UUID.randomUUID();
        t.recordDeath(p, 0L);
        t.recordDeath(p, 100L);
        t.recordDeath(p, 200L);
        assertEquals(0.5, t.chanceFor(p, 250L), 1e-9); // sanity: threshold reached

        t.clearPlayer(p);

        assertEquals(0.0, t.chanceFor(p, 250L), 1e-9); // history gone, not just aged out
    }
}
