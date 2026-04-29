package cz.hashiri.harshlands.bodyhealth;

/**
 * The eight body parts BodyHealth tracks. Iteration order matches the
 * vertical render order of the silhouette (top to bottom, left then right).
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
        return name().toLowerCase(java.util.Locale.ROOT);
    }
}
