package cz.hashiri.harshlands.bodyhealth;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BodyHealthRenderStateTest {

    private static final Key BODYHEALTH_FONT  = Key.key("harshlands", "bodyhealth");
    private static final Key NEGATIVE_SPACE   = Key.key("harshlands", "negative_space");

    /**
     * Build the expected bodyhealth-glyph string for a given per-part state map by
     * mirroring the same codepoint math {@link BodyHealthRenderState#compose} uses.
     * Authored via explicit char casts (not literal Private-Use-Area chars in source)
     * to keep the test file ASCII-safe across editor/encoding pipelines.
     */
    private static String expectedGlyphs(Map<BodyPart, BodyPartState> states) {
        StringBuilder sb = new StringBuilder();
        for (BodyPart part : BodyPart.values()) {
            BodyPartState st = states.getOrDefault(part, BodyPartState.FULL);
            int offset = part.ordinal() * BodyPartState.values().length + st.ordinal();
            sb.append((char) (BodyHealthRenderState.BASE_CODEPOINT + offset));
        }
        return sb.toString();
    }

    private static String bodyhealthChildrenText(Component c) {
        StringBuilder sb = new StringBuilder();
        for (Component child : c.children()) {
            if (BODYHEALTH_FONT.equals(child.style().font())) {
                sb.append(PlainTextComponentSerializer.plainText().serialize(child));
            }
        }
        return sb.toString();
    }

    @Test void all_full_emits_eight_full_codepoints() {
        Map<BodyPart, BodyPartState> states = new EnumMap<>(BodyPart.class);
        for (BodyPart p : BodyPart.values()) states.put(p, BodyPartState.FULL);

        Component c = BodyHealthRenderState.compose(states);
        // compose() interleaves negative-space shifts between body-part glyphs, so
        // the bodyhealth glyphs themselves are non-adjacent children. Filter to the
        // bodyhealth-font children to get just the 8 FULL codepoints.
        assertEquals(expectedGlyphs(states), bodyhealthChildrenText(c));
    }

    @Test void single_damaged_picks_offset_codepoint() {
        Map<BodyPart, BodyPartState> states = new EnumMap<>(BodyPart.class);
        for (BodyPart p : BodyPart.values()) states.put(p, BodyPartState.FULL);
        states.put(BodyPart.HEAD, BodyPartState.DAMAGED);

        Component c = BodyHealthRenderState.compose(states);
        // HEAD codepoint shifts +3 (FULL=0, NEARLY_FULL=1, INTERMEDIATE=2, DAMAGED=3, BROKEN=4);
        // other 7 parts remain at their FULL codepoint.
        assertEquals(expectedGlyphs(states), bodyhealthChildrenText(c));
    }

    @Test void glyph_children_use_bodyhealth_font_shifts_use_negative_space() {
        Map<BodyPart, BodyPartState> states = new EnumMap<>(BodyPart.class);
        for (BodyPart p : BodyPart.values()) states.put(p, BodyPartState.FULL);

        Component c = BodyHealthRenderState.compose(states);
        int glyphCount = 0;
        int shiftCount = 0;
        for (Component child : c.children()) {
            Key font = child.style().font();
            if (BODYHEALTH_FONT.equals(font)) {
                glyphCount++;
            } else if (NEGATIVE_SPACE.equals(font)) {
                shiftCount++;
            } else {
                fail("Unexpected font on child: " + font);
            }
        }
        assertEquals(BodyPart.values().length, glyphCount, "one bodyhealth glyph per part");
        assertTrue(shiftCount >= BodyPart.values().length - 1,
                "expected at least one negative-space shift between adjacent glyphs");
    }

    @Test void totalAdvance_is_single_glyph_wide() {
        // All 8 parts overlay at the same X via the in-component shifts, so the
        // silhouette's effective width is one glyph (PART_WIDTH_PX), not 8.
        assertEquals(BodyHealthRenderState.PART_WIDTH_PX, BodyHealthRenderState.totalAdvance());
    }
}
