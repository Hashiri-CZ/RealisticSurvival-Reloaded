package cz.hashiri.harshlands.bodyhealth;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;

/**
 * Per-body-part glyph builder for the BodyHealth HUD.
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
 *
 * <p>Each glyph is a depth-1 leaf: {@code Component.text(cp).font(harshlands:bodyhealth)}.
 * BossbarHUD preserves the font. Every part PNG is 32×64 with its visible
 * pixels at the natural position inside the silhouette; all eight elements
 * anchor at the same X and overlay, with transparent pixels compositing them
 * into one silhouette.
 */
final class BodyHealthRenderState {

    /** First codepoint in the bodyhealth glyph block (HEAD/FULL). U+E000 in the Private Use Area. */
    static final char BASE_CODEPOINT = '';

    /** Width of the silhouette canvas in pixels — also the per-element advance. */
    static final int CANVAS_WIDTH_PX = 32;

    /** BossbarHUD element id prefix; per-part ids are this + "_" + part.name(). */
    static final String ELEMENT_ID_PREFIX = "bodyhealth";

    private static final Key BODYHEALTH_FONT = Key.key("harshlands", "bodyhealth");
    private static final Style STYLE = Style.style().font(BODYHEALTH_FONT).build();

    private BodyHealthRenderState() {}

    /**
     * Build a single-glyph Component for one body part. Depth-1 leaf:
     * {@code Component.text(cp).style(font=harshlands:bodyhealth)}. BossbarHUD
     * preserves the font.
     */
    static Component glyphFor(BodyPart part, BodyPartState state) {
        int offset = part.ordinal() * BodyPartState.values().length + state.ordinal();
        char cp = (char) (BASE_CODEPOINT + offset);
        return Component.text(String.valueOf(cp)).style(STYLE);
    }

    /** Stable element id for a body part — used by BossbarHUD.setElement / removeElement. */
    static String elementId(BodyPart part) {
        return ELEMENT_ID_PREFIX + "_" + part.name();
    }
}
