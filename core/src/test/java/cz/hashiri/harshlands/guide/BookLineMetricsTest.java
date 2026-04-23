/*
    Copyright (C) 2026  Hashiri_
    GNU GPL v3.
 */
package cz.hashiri.harshlands.guide;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookLineMetricsTest {

    @Test
    void emptyStringHasZeroWidth() {
        assertEquals(0, BookLineMetrics.pixelWidth(""));
    }

    @Test
    void narrowCharactersAreCountedNarrow() {
        // 'i' is 1 pixel + 1 spacing = 2 pixels in vanilla
        int w = BookLineMetrics.pixelWidth("i");
        assertTrue(w <= 3, "expected 'i' <= 3 pixels, got " + w);
    }

    @Test
    void wideCharactersAreCountedWide() {
        // 'W' is 5 pixels + 1 spacing = 6 pixels
        int w = BookLineMetrics.pixelWidth("W");
        assertTrue(w >= 5, "expected 'W' >= 5 pixels, got " + w);
    }

    @Test
    void legacyColorCodesDoNotCountTowardWidth() {
        int plain = BookLineMetrics.pixelWidth("hello");
        int colored = BookLineMetrics.pixelWidth("&0&lhello");
        assertEquals(plain, colored);
    }

    @Test
    void exceedsLineWidthReturnsTrueForWidePhrase() {
        // 114-pixel book line. A long string of 'W's certainly overflows.
        String overflow = "WWWWWWWWWWWWWWWWWWWWWWWW"; // 24 W's ≈ 144 px
        assertTrue(BookLineMetrics.exceedsBookLine(overflow));
    }

    @Test
    void shortLineDoesNotExceed() {
        assertFalse(BookLineMetrics.exceedsBookLine("hi"));
    }
}
