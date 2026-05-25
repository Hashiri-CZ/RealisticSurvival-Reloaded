package cz.hashiri.harshlands.firstaid;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BodyHealthBridgeTest {

    @Test
    void isAvailable_returns_false_when_api_class_missing() {
        // BodyHealth is not on the test classpath, so reflection lookup must fail gracefully.
        BodyHealthBridge bridge = new BodyHealthBridge();
        assertFalse(bridge.isAvailable());
    }

    @Test
    void heal_returns_false_when_unavailable() {
        BodyHealthBridge bridge = new BodyHealthBridge();
        // Player parameter is null on purpose — bridge must short-circuit on availability.
        assertFalse(bridge.heal(null, "TORSO", 2.0));
    }

    @Test
    void getHealth_returns_negative_when_unavailable() {
        BodyHealthBridge bridge = new BodyHealthBridge();
        assertTrue(bridge.getHealth(null, "TORSO") < 0);
    }

    @Test
    void getMaxHealth_returns_negative_when_unavailable() {
        BodyHealthBridge bridge = new BodyHealthBridge();
        assertTrue(bridge.getMaxHealth(null, "TORSO") < 0);
    }
}
