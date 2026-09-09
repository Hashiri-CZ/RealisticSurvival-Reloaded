package cz.hashiri.harshlands.disease;

import cz.hashiri.harshlands.disease.trigger.DiseaseTrigger;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DiseaseProgressionTask#contractionChance}. The fakes ignore the
 * {@link Player} argument entirely, so a null player is enough — no Bukkit mocking needed.
 */
class ContractionChanceTest {

    /** Constant-risk trigger that counts how many times it was polled. */
    private static final class CountingTrigger implements DiseaseTrigger {
        private final String id;
        private final double value;
        int calls;
        CountingTrigger(String id, double value) { this.id = id; this.value = value; }
        @Override public String diseaseId() { return id; }
        @Override public double chance(Player player) { calls++; return value; }
    }

    /**
     * Mirrors the event-driven triggers (unpurified water / infected item / rusty source):
     * a pending exposure is a one-shot flag that {@code chance()} CONSUMES when read.
     */
    private static final class PendingExposureTrigger implements DiseaseTrigger {
        private final String id;
        private final double value;
        private boolean pending;
        PendingExposureTrigger(String id, double value) { this.id = id; this.value = value; }
        void markPending() { pending = true; }
        boolean hasPending() { return pending; }
        @Override public String diseaseId() { return id; }
        @Override public double chance(Player player) {
            if (!pending) return 0.0;
            pending = false;
            return value;
        }
    }

    private static final Player NO_PLAYER = null;

    @Nested
    class NotImmune {
        @Test void sums_matching_triggers_and_applies_the_multiplier() {
            List<DiseaseTrigger> triggers = List.of(
                new CountingTrigger("dysentery", 0.2), new CountingTrigger("dysentery", 0.3));
            assertEquals(0.25, DiseaseProgressionTask.contractionChance(
                triggers, "dysentery", NO_PLAYER, false, 0.5), 1e-9);
        }
        @Test void ignores_triggers_for_other_diseases() {
            List<DiseaseTrigger> triggers = List.of(
                new CountingTrigger("dysentery", 0.2), new CountingTrigger("crimson", 0.9));
            assertEquals(0.2, DiseaseProgressionTask.contractionChance(
                triggers, "dysentery", NO_PLAYER, false, 1.0), 1e-9);
        }
        @Test void other_disease_triggers_are_not_polled_at_all() {
            CountingTrigger other = new CountingTrigger("crimson", 0.9);
            DiseaseProgressionTask.contractionChance(
                List.of(new CountingTrigger("dysentery", 0.2), other), "dysentery", NO_PLAYER, false, 1.0);
            assertEquals(0, other.calls);
        }
        @Test void negative_trigger_chances_are_floored_at_zero() {
            List<DiseaseTrigger> triggers = List.of(
                new CountingTrigger("dysentery", -5.0), new CountingTrigger("dysentery", 0.4));
            assertEquals(0.4, DiseaseProgressionTask.contractionChance(
                triggers, "dysentery", NO_PLAYER, false, 1.0), 1e-9);
        }
        @Test void result_is_clamped_to_one() {
            List<DiseaseTrigger> triggers = List.of(
                new CountingTrigger("dysentery", 0.8), new CountingTrigger("dysentery", 0.8));
            assertEquals(1.0, DiseaseProgressionTask.contractionChance(
                triggers, "dysentery", NO_PLAYER, false, 2.0), 1e-9);
        }
        @Test void no_matching_triggers_is_zero() {
            assertEquals(0.0, DiseaseProgressionTask.contractionChance(
                List.of(new CountingTrigger("crimson", 0.9)), "dysentery", NO_PLAYER, false, 1.0), 1e-9);
        }
    }

    @Nested
    class Immune {
        @Test void never_reports_a_contraction_chance() {
            List<DiseaseTrigger> triggers = List.of(new CountingTrigger("dysentery", 1.0));
            assertEquals(0.0, DiseaseProgressionTask.contractionChance(
                triggers, "dysentery", NO_PLAYER, true, 1.0), 1e-9);
        }
        @Test void still_polls_matching_triggers_so_pending_exposures_are_drained() {
            CountingTrigger t = new CountingTrigger("dysentery", 1.0);
            DiseaseProgressionTask.contractionChance(List.of(t), "dysentery", NO_PLAYER, true, 1.0);
            assertEquals(1, t.calls, "immune players must still drain their disease's triggers");
        }
        @Test void does_not_poll_other_diseases_triggers() {
            CountingTrigger other = new CountingTrigger("crimson", 1.0);
            DiseaseProgressionTask.contractionChance(
                List.of(other), "dysentery", NO_PLAYER, true, 1.0);
            assertEquals(0, other.calls);
        }
        @Test void pending_exposure_marked_while_immune_is_consumed() {
            PendingExposureTrigger t = new PendingExposureTrigger("dysentery", 0.5);
            t.markPending();
            DiseaseProgressionTask.contractionChance(List.of(t), "dysentery", NO_PLAYER, true, 1.0);
            assertFalse(t.hasPending(), "the pending exposure must be consumed while immune");
        }
        @Test void pending_exposure_does_not_leak_into_the_first_check_after_immunity() {
            PendingExposureTrigger t = new PendingExposureTrigger("dysentery", 0.5);
            t.markPending();
            // Check while still immune: drains the exposure, contracts nothing.
            assertEquals(0.0, DiseaseProgressionTask.contractionChance(
                List.of(t), "dysentery", NO_PLAYER, true, 1.0), 1e-9);
            // Next check, immunity has lapsed: the old exposure must NOT be attributed here.
            assertEquals(0.0, DiseaseProgressionTask.contractionChance(
                List.of(t), "dysentery", NO_PLAYER, false, 1.0), 1e-9);
        }
        @Test void a_fresh_exposure_after_immunity_still_counts() {
            PendingExposureTrigger t = new PendingExposureTrigger("dysentery", 0.5);
            t.markPending();
            DiseaseProgressionTask.contractionChance(List.of(t), "dysentery", NO_PLAYER, true, 1.0);
            t.markPending(); // new exposure, immunity now lapsed
            assertEquals(0.5, DiseaseProgressionTask.contractionChance(
                List.of(t), "dysentery", NO_PLAYER, false, 1.0), 1e-9);
        }
    }
}
