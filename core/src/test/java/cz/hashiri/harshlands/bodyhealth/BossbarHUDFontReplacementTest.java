package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.utils.BossbarHUD;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins the contract that {@link BossbarHUD#rebuildTitle()} preserves the
 * font of an element whose content already has one explicitly set. A
 * depth-1 leaf {@code Component.text(cp).font(harshlands:bodyhealth)}
 * arrives at the client intact.
 */
class BossbarHUDFontReplacementTest {

    @Test void bossbar_title_preserves_explicit_element_font() {
        BossbarHUD hud = new BossbarHUD(Audience.empty());
        Component glyph = BodyHealthRenderState.glyphFor(BodyPart.ARM_LEFT, BodyPartState.FULL);
        // X is irrelevant to this contract; pin advance to the canvas width so
        // the test doesn't drift if BossbarHUD's cursor arithmetic ever changes.
        hud.setElement(BodyHealthRenderState.elementId(BodyPart.ARM_LEFT),
                0, glyph, BodyHealthRenderState.CANVAS_WIDTH_PX);

        String json = GsonComponentSerializer.gson().serialize(hud.currentTitle());

        assertTrue(json.contains("harshlands:bodyhealth"),
                "wire JSON must still reference harshlands:bodyhealth. Got: " + json);
        assertFalse(json.contains("\"font\":\"minecraft:default\""),
                "wire JSON should NOT contain minecraft:default (no element opted in). Got: " + json);
    }

    @Test void bossbar_title_still_defaults_font_for_unstyled_elements() {
        BossbarHUD hud = new BossbarHUD(Audience.empty());
        Component plain = Component.text("X");
        hud.setElement("plain", 0, plain, 6);

        String json = GsonComponentSerializer.gson().serialize(hud.currentTitle());

        assertTrue(json.contains("minecraft:default"),
                "unstyled element should be wrapped with minecraft:default. Got: " + json);
    }
}
