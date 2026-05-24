package cz.hashiri.harshlands.bodyhealth;

import java.util.logging.Logger;

/**
 * Gated `[BHD]`-prefixed debug logger for the BodyHealth render path.
 *
 * <p>Off by default; flip on via the `BodyHealth.Debug.Render` config flag in
 * `Settings/bodyhealth.yml`. {@link BodyHealthModule#initialize()} calls
 * {@link #enable(boolean, Logger)} once at startup.
 *
 * <p>When disabled, every public log method is a cheap no-op that does not even
 * format its arguments — safe to leave in hot paths.
 */
public final class BHDLogger {

    private static volatile boolean enabled = false;
    private static volatile Logger logger = null;

    private BHDLogger() {}

    /** Enable or disable the logger and install the target Logger. */
    public static void enable(boolean on, Logger l) {
        enabled = on;
        logger = on ? l : null;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    /** Emit a `[BHD] msg` line. No-op when disabled. */
    public static void log(String msg) {
        if (enabled && logger != null) {
            logger.info("[BHD] " + msg);
        }
    }

    /** Emit a formatted `[BHD] ...` line. No-op when disabled (args are not formatted). */
    public static void logf(String fmt, Object... args) {
        if (enabled && logger != null) {
            logger.info("[BHD] " + String.format(fmt, args));
        }
    }
}
