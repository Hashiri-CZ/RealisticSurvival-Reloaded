/*
    Copyright (C) 2026  Hashiri_
    GNU GPL v3.
 */
package cz.hashiri.harshlands.guide;

public final class BookLineMetrics {

    public static final int BOOK_PAGE_WIDTH_PX = 114;

    private BookLineMetrics() {}

    // Vanilla per-glyph pixel widths (width of the glyph itself; we add 1 pixel of
    // spacing after each glyph). Only ASCII is tabulated — non-ASCII characters
    // default to 6 pixels which matches vanilla's typical glyph.
    private static final int[] ASCII_WIDTHS = new int[128];

    static {
        // Default everything to 6 (most common vanilla width).
        for (int i = 0; i < ASCII_WIDTHS.length; i++) ASCII_WIDTHS[i] = 6;
        // Narrow glyphs.
        int[] ones = { '!', ',', '.', ':', ';', 'i', '|', '\'' };
        for (int c : ones) ASCII_WIDTHS[c] = 2;
        int[] twos = { '`', 'l' };
        for (int c : twos) ASCII_WIDTHS[c] = 3;
        int[] threes = { ' ', 'I', 't', '(', ')', '[', ']', '*', '"', '<', '>', '{', '}' };
        for (int c : threes) ASCII_WIDTHS[c] = 4;
        int[] fours = { 'f', 'k', 'r', 's' };
        for (int c : fours) ASCII_WIDTHS[c] = 5;
        // Wide glyphs.
        int[] sevens = { '@', '~' };
        for (int c : sevens) ASCII_WIDTHS[c] = 7;
    }

    /** Width in pixels of {@code text} when rendered in a vanilla book. Legacy color codes are stripped first. */
    public static int pixelWidth(String text) {
        if (text == null || text.isEmpty()) return 0;
        String stripped = stripLegacyColors(text);
        int width = 0;
        for (int i = 0; i < stripped.length(); i++) {
            char c = stripped.charAt(i);
            if (c > 'ÿ') {
                // Vanilla renders non-Latin codepoints via unifont. Use the same
                // 12-px advance that NutritionPreviewLayout.measureTextAdvance
                // applies, which over-estimates slightly to keep the book-line
                // validator on the safe side (better a false warn than overflow).
                width += 12; // unifont advance already includes its trailing space
            } else {
                width += ASCII_WIDTHS[c] + 1; // +1 spacing after each ASCII glyph
            }
        }
        // The last ASCII glyph's trailing spacing doesn't render; subtract it,
        // but only if the string ended on an ASCII char.
        if (!stripped.isEmpty() && stripped.charAt(stripped.length() - 1) <= 'ÿ') {
            width = Math.max(0, width - 1);
        }
        return width;
    }

    public static boolean exceedsBookLine(String text) {
        return pixelWidth(text) > BOOK_PAGE_WIDTH_PX;
    }

    private static String stripLegacyColors(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c == '&' || c == '\u00A7') && i + 1 < s.length()) {
                i++; // skip the following format char
                continue;
            }
            out.append(c);
        }
        return out.toString();
    }
}
