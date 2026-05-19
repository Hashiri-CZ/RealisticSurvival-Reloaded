package cz.hashiri.harshlands.bodyhealth;

import java.util.Locale;

/**
 * The eight body parts BodyHealth tracks. Iteration order matches the
 * vertical render order of the silhouette (top to bottom, left then right).
 *
 * <p>BetterHud-mirror layout: every part anchors at the same X. Per-part
 * positioning inside the 32×64 silhouette is baked into each PNG canvas
 * (transparent padding around the body part). Y comes from a single shader
 * bucket E that pins {@code ui.y-76 → ui.y-12} (see
 * {@code docs/resource_pack/SHADER_BUCKETS.md}).
 */
public enum BodyPart {
    HEAD,
    TORSO,
    ARM_LEFT,
    ARM_RIGHT,
    LEG_LEFT,
    LEG_RIGHT,
    FOOT_LEFT,
    FOOT_RIGHT;

    /** Lowercase suffix used in PlaceholderAPI placeholder names (e.g. arm_left). */
    public String placeholderSuffix() {
        return name().toLowerCase(Locale.ROOT);
    }
}
