package cz.hashiri.harshlands.bodyhealth;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BodyHealthRenderStateTest {

    @Test void all_full_uses_lowest_codepoints() {
        Map<BodyPart, BodyPartState> states = new EnumMap<>(BodyPart.class);
        for (BodyPart p : BodyPart.values()) states.put(p, BodyPartState.FULL);

        Component c = BodyHealthRenderState.compose(states);
        String text = PlainTextComponentSerializer.plainText().serialize(c);
        // All 8 FULL codepoints (HEAD..FOOT_RIGHT @ FULL = first column of the table).
        assertEquals(
                "",
                text);
    }

    @Test void single_damaged_picks_offset_codepoint() {
        Map<BodyPart, BodyPartState> states = new EnumMap<>(BodyPart.class);
        for (BodyPart p : BodyPart.values()) states.put(p, BodyPartState.FULL);
        states.put(BodyPart.HEAD, BodyPartState.DAMAGED);

        Component c = BodyHealthRenderState.compose(states);
        String text = PlainTextComponentSerializer.plainText().serialize(c);
        // HEAD codepoint shifts +3 (FULL=0, NEARLY_FULL=1, INTERMEDIATE=2, DAMAGED=3, BROKEN=4)
        assertEquals(
                "",
                text);
    }

    @Test void component_uses_bodyhealth_font() {
        Map<BodyPart, BodyPartState> states = new EnumMap<>(BodyPart.class);
        for (BodyPart p : BodyPart.values()) states.put(p, BodyPartState.FULL);
        Component c = BodyHealthRenderState.compose(states);
        // Walk children: font should be harshlands:bodyhealth on every text segment.
        Key expected = Key.key("harshlands", "bodyhealth");
        c.children().forEach(child -> assertEquals(expected, child.style().font()));
    }

    @Test void totalAdvance_matches_sum_of_part_widths() {
        // 8 parts × default advance (set in font/bodyhealth.json) = 8 × PART_WIDTH_PX
        int adv = BodyHealthRenderState.totalAdvance();
        assertEquals(BodyHealthRenderState.PART_WIDTH_PX * 8, adv);
    }
}
