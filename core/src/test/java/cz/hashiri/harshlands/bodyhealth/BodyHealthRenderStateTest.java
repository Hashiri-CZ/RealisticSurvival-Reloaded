package cz.hashiri.harshlands.bodyhealth;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BodyHealthRenderStateTest {

    private static final Key BODYHEALTH_FONT = Key.key("harshlands", "bodyhealth");

    @Test void glyphFor_is_a_depth_1_leaf() {
        // Shape contract: a flat TextComponent with no children. Font is
        // pinned exhaustively by all_glyphs_share_the_same_font below.
        Component c = BodyHealthRenderState.glyphFor(BodyPart.HEAD, BodyPartState.FULL);
        assertEquals(0, c.children().size(), "glyph should have no children (depth-1 leaf)");
        assertInstanceOf(TextComponent.class, c);
    }

    @Test void glyphFor_codepoint_matches_row_major_table() {
        for (BodyPart part : BodyPart.values()) {
            for (BodyPartState state : BodyPartState.values()) {
                int expectedOffset = part.ordinal() * BodyPartState.values().length + state.ordinal();
                char expectedCp = (char) (BodyHealthRenderState.BASE_CODEPOINT + expectedOffset);
                Component c = BodyHealthRenderState.glyphFor(part, state);
                String actual = PlainTextComponentSerializer.plainText().serialize(c);
                assertEquals(String.valueOf(expectedCp), actual,
                        () -> part + "/" + state + " should map to codepoint U+"
                              + String.format("%04X", (int) expectedCp));
            }
        }
    }

    @Test void elementId_is_stable_and_unique_per_part() {
        Set<String> ids = new HashSet<>();
        for (BodyPart part : BodyPart.values()) {
            String id = BodyHealthRenderState.elementId(part);
            assertTrue(id.startsWith(BodyHealthRenderState.ELEMENT_ID_PREFIX),
                    () -> "element id should start with prefix: " + id);
            assertTrue(id.contains(part.name()),
                    () -> "element id should mention the part: " + id);
            assertTrue(ids.add(id), () -> "duplicate element id across parts: " + id);
        }
        assertEquals(BodyPart.values().length, ids.size());
    }

    @Test void bodyPart_has_no_canvas_offset_api() {
        // BetterHud-mirror invariant: every part draws at the same anchor X,
        // so BodyPart no longer exposes per-part X/width offsets.
        for (java.lang.reflect.Method m : BodyPart.class.getDeclaredMethods()) {
            String n = m.getName();
            assertNotEquals("canvasX", n, "canvasX should be removed");
            assertNotEquals("canvasWidth", n, "canvasWidth should be removed");
        }
    }

    @Test void all_glyphs_share_the_same_font() {
        // Exhaustive: every (part, state) pair builds a glyph in the
        // harshlands:bodyhealth font.
        for (BodyPart part : BodyPart.values()) {
            for (BodyPartState st : BodyPartState.values()) {
                assertEquals(BODYHEALTH_FONT,
                        BodyHealthRenderState.glyphFor(part, st).style().font(),
                        () -> "glyph " + part + "/" + st + " font mismatch");
            }
        }
    }
}
