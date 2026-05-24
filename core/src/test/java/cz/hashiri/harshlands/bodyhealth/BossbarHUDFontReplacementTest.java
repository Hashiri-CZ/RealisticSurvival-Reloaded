package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.utils.BossbarHUD;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pins two BossbarHUD contracts for bodyhealth glyphs:
 * <ul>
 *   <li>the font of an element with an explicit font survives rebuildTitle;</li>
 *   <li>eight same-anchor parts decascade: every inter-element shift is exactly
 *       -GLYPH_ADVANCE_PX, so all eight glyphs render at one anchor X.</li>
 * </ul>
 */
class BossbarHUDFontReplacementTest {

    @Test void bossbar_title_preserves_explicit_element_font() {
        BossbarHUD hud = new BossbarHUD(Audience.empty());
        Component glyph = BodyHealthRenderState.glyphFor(BodyPart.ARM_LEFT, BodyPartState.FULL);
        // X is irrelevant to this contract; pin advance to the glyph advance so
        // the test doesn't drift if BossbarHUD's cursor arithmetic ever changes.
        hud.setElement(BodyHealthRenderState.elementId(BodyPart.ARM_LEFT),
                0, glyph, BodyHealthRenderState.GLYPH_ADVANCE_PX);

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

    /**
     * The eight body parts all anchor at the same X (BetterHud-mirror layout).
     * For them to overlay into one silhouette, every inter-element negative-space
     * shift must cancel the previous glyph's full advance. Mojang's real advance
     * for a 32 px-wide bitmap glyph is 33 (32 drawn + 1 trailing gap), so each
     * inter-element shift must be exactly -33. A shift of -32 (the old bug)
     * under-cancels by 1 px per part and shears the silhouette apart.
     */
    @Test void bossbar_overlays_eight_bodyhealth_parts_at_one_anchor() {
        BossbarHUD hud = new BossbarHUD(Audience.empty());
        int anchorX = 160;
        for (BodyPart part : BodyPart.values()) {
            hud.setElement(
                    BodyHealthRenderState.elementId(part),
                    anchorX,
                    BodyHealthRenderState.glyphFor(part, BodyPartState.FULL),
                    BodyHealthRenderState.GLYPH_ADVANCE_PX);
        }

        List<Component> children = hud.currentTitle().children();
        List<Integer> interElementShifts = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            // a negative-space run sandwiched between two bodyhealth glyphs
            // is an inter-element shift (the leading and trailing shifts are not)
            if (isBodyhealthGlyph(children.get(i))
                    && i + 2 < children.size()
                    && isNegativeSpace(children.get(i + 1))
                    && isBodyhealthGlyph(children.get(i + 2))) {
                interElementShifts.add(decodeShift(((TextComponent) children.get(i + 1)).content()));
            }
        }

        assertEquals(7, interElementShifts.size(),
                "expected 7 shifts between 8 bodyhealth glyphs, got " + interElementShifts);
        for (int shift : interElementShifts) {
            assertEquals(-33, shift,
                    "every inter-element shift must be -33 (cancel a 32 px glyph + Mojang's "
                    + "1 px trailing gap); -32 is the cascade bug. Shifts: " + interElementShifts);
        }
    }

    private static boolean isBodyhealthGlyph(Component c) {
        return c.style().font() != null
                && "harshlands:bodyhealth".equals(c.style().font().asString());
    }

    private static boolean isNegativeSpace(Component c) {
        return c.style().font() != null
                && "harshlands:negative_space".equals(c.style().font().asString());
    }

    /**
     * Decode a harshlands:negative_space run back to its signed pixel shift.
     * Mirrors the codepoint tables in BossbarHUD.NegativeSpaceHelper: the F8xx/F9xx
     * range carries negative advances, the FAxx range carries positive advances.
     */
    private static int decodeShift(String text) {
        int[] powers = {256, 128, 64, 32, 16, 8, 4, 2, 1};
        int[] neg = {0xF900, 0xF880, 0xF840, 0xF820, 0xF810, 0xF808, 0xF804, 0xF802, 0xF801};
        int[] pos = {0xFA00, 0xFA80, 0xFA40, 0xFA20, 0xFA10, 0xFA08, 0xFA04, 0xFA02, 0xFA01};
        int sum = 0;
        for (int idx = 0; idx < text.length(); idx++) {
            int ch = text.charAt(idx);
            for (int i = 0; i < powers.length; i++) {
                if (ch == neg[i]) sum -= powers[i];
                if (ch == pos[i]) sum += powers[i];
            }
        }
        return sum;
    }
}
