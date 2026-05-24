package cz.hashiri.harshlands.utils;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BossbarHUDReturnValuesTest {

    @Test void setElement_new_id_returns_ADD() {
        BossbarHUD hud = new BossbarHUD(Audience.empty());
        BossbarHUD.SetElementOutcome out = hud.setElement("a", 0, Component.text("X"), 6);
        assertEquals(BossbarHUD.SetElementOutcome.ADD, out);
        assertEquals(1, hud.elementCount());
    }

    @Test void setElement_identical_content_returns_KEEP_EQUAL() {
        BossbarHUD hud = new BossbarHUD(Audience.empty());
        hud.setElement("a", 0, Component.text("X"), 6);
        BossbarHUD.SetElementOutcome out = hud.setElement("a", 0, Component.text("X"), 6);
        assertEquals(BossbarHUD.SetElementOutcome.KEEP_EQUAL, out);
        assertEquals(1, hud.elementCount());
    }

    @Test void setElement_changed_content_returns_UPDATE() {
        BossbarHUD hud = new BossbarHUD(Audience.empty());
        hud.setElement("a", 0, Component.text("X"), 6);
        BossbarHUD.SetElementOutcome out = hud.setElement("a", 0, Component.text("Y"), 6);
        assertEquals(BossbarHUD.SetElementOutcome.UPDATE, out);
        assertEquals(1, hud.elementCount());
    }

    @Test void setElement_changed_x_returns_UPDATE() {
        BossbarHUD hud = new BossbarHUD(Audience.empty());
        hud.setElement("a", 0, Component.text("X"), 6);
        BossbarHUD.SetElementOutcome out = hud.setElement("a", 10, Component.text("X"), 6);
        assertEquals(BossbarHUD.SetElementOutcome.UPDATE, out);
    }

    @Test void removeElement_present_returns_true() {
        BossbarHUD hud = new BossbarHUD(Audience.empty());
        hud.setElement("a", 0, Component.text("X"), 6);
        assertTrue(hud.removeElement("a"));
        assertEquals(0, hud.elementCount());
    }

    @Test void removeElement_absent_returns_false() {
        BossbarHUD hud = new BossbarHUD(Audience.empty());
        assertFalse(hud.removeElement("missing"));
        assertEquals(0, hud.elementCount());
    }

    @Test void elementCount_tracks_adds_and_removes() {
        BossbarHUD hud = new BossbarHUD(Audience.empty());
        assertEquals(0, hud.elementCount());
        hud.setElement("a", 0, Component.text("X"), 6);
        hud.setElement("b", 10, Component.text("Y"), 6);
        assertEquals(2, hud.elementCount());
        hud.removeElement("a");
        assertEquals(1, hud.elementCount());
    }
}
