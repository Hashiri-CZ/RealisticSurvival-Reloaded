package cz.hashiri.harshlands.disease.symptom.special;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BlockEatingHandlerTest {
    @Test void inactive_chance_never_blocks() {
        assertFalse(BlockEatingHandler.shouldBlock(0.0, 0.0));
    }
    @Test void full_chance_always_blocks() {
        assertTrue(BlockEatingHandler.shouldBlock(1.0, 0.99));
    }
    @Test void partial_chance_blocks_when_roll_below() {
        assertTrue(BlockEatingHandler.shouldBlock(0.5, 0.49));
    }
    @Test void partial_chance_passes_when_roll_at_or_above() {
        assertFalse(BlockEatingHandler.shouldBlock(0.5, 0.5));
        assertFalse(BlockEatingHandler.shouldBlock(0.5, 0.6));
    }
}
