package cz.hashiri.harshlands.bodyhealth;

import cz.hashiri.harshlands.utils.BossbarHUD;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;

import java.util.Map;

/**
 * Pure composer: turns a per-body-part state map into the {@link Component}
 * Harshlands sets on its BossbarHUD.
 *
 * <p>Codepoint table is row-major (part × state):
 *   HEAD       → U+E000 (FULL) .. U+E004 (BROKEN)
 *   TORSO      → U+E005 .. U+E009
 *   ARM_LEFT   → U+E00A .. U+E00E
 *   ARM_RIGHT  → U+E00F .. U+E013
 *   LEG_LEFT   → U+E014 .. U+E018
 *   LEG_RIGHT  → U+E019 .. U+E01D
 *   FOOT_LEFT  → U+E01E .. U+E022
 *   FOOT_RIGHT → U+E023 .. U+E027
 * </p>
 *
 * <p>Each of the 8 part PNGs is a 32×64 sprite whose body part is drawn at the
 * correct absolute position within a shared canvas (head in the top-center,
 * torso just below it, arms at the canvas left/right edges, legs/feet in the
 * lower half). The intended visual is to <b>overlay all 8 glyphs at the same
 * on-screen X</b> — Minecraft's font cursor would otherwise advance after each
 * glyph and spread them across the screen as a diagonal strip. After each
 * glyph we therefore append a negative-space shift of {@code -PART_WIDTH_PX}
 * (using the {@code harshlands:negative_space} font) to return the cursor to
 * where the glyph started. Per-part vertical positioning continues to come
 * from each glyph's {@code ascent} in {@code font/bodyhealth.json}.</p>
 */
final class BodyHealthRenderState {

    /** First codepoint in the bodyhealth glyph block (HEAD/FULL). */
    static final char BASE_CODEPOINT = '';

    /**
     * Rendered glyph width in pixels. The PNG is 32×64; at the matching font
     * {@code height: 64} the rendered glyph is 32 px wide. This is the value
     * we shift back by between glyphs so all 8 parts overlay at the same X.
     */
    static final int PART_WIDTH_PX = 32;

    private static final Key FONT = Key.key("harshlands", "bodyhealth");
    private static final Style STYLE = Style.style().font(FONT).build();

    private BodyHealthRenderState() {}

    /**
     * Build the silhouette Component. The returned component has one text
     * child per body part (8 children, each styled with
     * {@code font=harshlands:bodyhealth}) interleaved with 7 negative-space
     * shift children that return the cursor to x=0 between parts, so all 8
     * part PNGs render at the same on-screen X.
     */
    static Component compose(Map<BodyPart, BodyPartState> states) {
        Component result = Component.empty();
        BodyPart[] parts = BodyPart.values();
        for (int i = 0; i < parts.length; i++) {
            BodyPart part = parts[i];
            BodyPartState st = states.getOrDefault(part, BodyPartState.FULL);
            int offset = part.ordinal() * BodyPartState.values().length + st.ordinal();
            char cp = (char) (BASE_CODEPOINT + offset);
            result = result.append(Component.text(String.valueOf(cp)).style(STYLE));
            if (i < parts.length - 1) {
                // Return cursor to the glyph's start position so the next part
                // renders on top of this one, not 32 px to its right.
                result = result.append(BossbarHUD.NegativeSpaceHelper.shift(-PART_WIDTH_PX));
            }
        }
        return result;
    }

    /**
     * Total horizontal advance the silhouette occupies, for BossbarHUD
     * bookkeeping. Since all 8 parts overlay at the same X, the silhouette is
     * just one glyph wide.
     */
    static int totalAdvance() {
        return PART_WIDTH_PX;
    }
}
