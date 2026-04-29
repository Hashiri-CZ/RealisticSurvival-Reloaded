package cz.hashiri.harshlands.bodyhealth;

import java.util.Locale;

/**
 * Health states a body part can be in, mirroring BodyHealth's BodyPartState enum.
 * NEARLYFULL is mapped to NEARLY_FULL for Java-style naming.
 */
public enum BodyPartState {
    FULL,
    NEARLY_FULL,
    INTERMEDIATE,
    DAMAGED,
    BROKEN;

    /** Lowercase token used in glyph filenames (e.g. nearly_full). */
    public String spriteToken() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Parse the value returned by {@code %bodyhealth_state_<part>%}.
     * Recognized: FULL, NEARLYFULL, INTERMEDIATE, DAMAGED, BROKEN (case-insensitive, trimmed).
     * Everything else (null, empty, unknown) returns FULL — avoids partial silhouettes
     * during PAPI reload windows.
     */
    public static BodyPartState fromPlaceholder(String raw) {
        if (raw == null) return FULL;
        String s = raw.trim().toUpperCase(Locale.ROOT);
        if (s.isEmpty()) return FULL;
        return switch (s) {
            case "FULL"         -> FULL;
            case "NEARLYFULL"   -> NEARLY_FULL;
            case "INTERMEDIATE" -> INTERMEDIATE;
            case "DAMAGED"      -> DAMAGED;
            case "BROKEN"       -> BROKEN;
            default             -> FULL;
        };
    }
}
