/*
    Copyright (C) 2026  Hashiri_
    GNU GPL v3.
 */
package cz.hashiri.harshlands.guide;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookLineMetricsUnifontTest {

    @Test
    void cjk_glyph_uses_unifont_width() {
        // A single CJK character must measure wider than 6 (the ASCII default)
        // because vanilla renders it via unifont (~12 px advance including spacing).
        // Without the fix this returned 6 and silently allowed overflow.
        int oneCjk = BookLineMetrics.pixelWidth("蛋"); // U+86CB
        assertEquals(12, oneCjk, "single CJK char should be 12 px (unifont)");
    }

    @Test
    void all_cjk_string_overflows_book_line() {
        // 10 CJK chars at 12 px each = 120 px > 114 px book line.
        String tenCjk = "蛋".repeat(10);
        assertTrue(BookLineMetrics.exceedsBookLine(tenCjk),
                "10 CJK chars should exceed 114 px book line");
    }

    @Test
    void mixed_ascii_and_cjk() {
        // ASCII path must still work alongside the new CJK branch.
        int width = BookLineMetrics.pixelWidth("Hi");
        assertTrue(width >= 6 && width <= 14, "ASCII width unchanged: " + width);
    }

    @Test
    void latin1_chars_do_not_crash() {
        // Latin-1 supplement chars (128..255) — '«' (0xAB), '»' (0xBB), accented
        // letters used in Czech/French/etc. — must NOT throw
        // ArrayIndexOutOfBoundsException. The ASCII_WIDTHS table is sized 128,
        // so any path that indexes it with a Latin-1 codepoint blows up at
        // plugin startup during guide validation. Single-char widths are
        // 6 px (default glyph) — the trailing 1-px spacing is subtracted.
        assertEquals(6, BookLineMetrics.pixelWidth("«"));
        assertEquals(6, BookLineMetrics.pixelWidth("é"));
        assertEquals(6, BookLineMetrics.pixelWidth("ÿ")); // boundary: U+00FF
    }

    @Test
    void mixed_ascii_and_latin1_string() {
        // A plausible localized phrase mixing ASCII and Latin-1 must measure
        // without throwing.
        int width = BookLineMetrics.pixelWidth("«hello»");
        assertTrue(width > 0);
    }
}
