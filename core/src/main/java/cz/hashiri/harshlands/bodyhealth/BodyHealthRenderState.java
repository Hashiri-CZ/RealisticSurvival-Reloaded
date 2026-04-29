package cz.hashiri.harshlands.bodyhealth;

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
 * <p>Each glyph in {@code font/bodyhealth.json} carries its own X advance and Y
 * ascent. The composer just emits the 8 codepoints in iteration order; cursor
 * positioning between parts is handled entirely by the font.</p>
 */
final class BodyHealthRenderState {

    /** First codepoint in the bodyhealth glyph block (HEAD/FULL). */
    static final char BASE_CODEPOINT = '';

    /** Width per part (and sprite advance) in pixels. Mirrors font/bodyhealth.json. */
    static final int PART_WIDTH_PX = 32;

    private static final Key FONT = Key.key("harshlands", "bodyhealth");
    private static final Style STYLE = Style.style().font(FONT).build();

    private BodyHealthRenderState() {}

    /**
     * Build the silhouette Component. The returned component has one text child
     * per body part (8 children), each styled with {@code font=harshlands:bodyhealth}.
     */
    static Component compose(Map<BodyPart, BodyPartState> states) {
        Component result = Component.empty();
        for (BodyPart part : BodyPart.values()) {
            BodyPartState st = states.getOrDefault(part, BodyPartState.FULL);
            int offset = part.ordinal() * BodyPartState.values().length + st.ordinal();
            char cp = (char) (BASE_CODEPOINT + offset);
            result = result.append(Component.text(String.valueOf(cp)).style(STYLE));
        }
        return result;
    }

    /** Total horizontal advance the silhouette occupies, for BossbarHUD bookkeeping. */
    static int totalAdvance() {
        return PART_WIDTH_PX * BodyPart.values().length;
    }
}
