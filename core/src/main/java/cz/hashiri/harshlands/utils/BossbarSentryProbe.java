/*
    Copyright (C) 2025  Hashiri_

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cz.hashiri.harshlands.utils;

import org.bukkit.Bukkit;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Decides whether to install the bossbar Sentry on a given server.
 *
 * <p>Pure-decision API ({@link #evaluate}) is unit-tested; the convenience
 * {@link #evaluateLive(Mode)} delegates to {@code Bukkit.getPluginManager()}
 * and Folia detection.</p>
 */
public final class BossbarSentryProbe {

    private static final List<String> BLOCKERS = List.of("BetterHud", "Nova", "NovaFramework");
    private static final String FOLIA_CLASS = "io.papermc.paper.threadedregions.RegionizedServer";

    private BossbarSentryProbe() {}

    public enum Mode {
        AUTO, ENABLED, DISABLED;

        public static Mode parse(String raw) {
            if (raw == null) return AUTO;
            try { return Mode.valueOf(raw.trim().toUpperCase()); }
            catch (IllegalArgumentException e) { return AUTO; }
        }
    }

    public record Decision(boolean shouldInstall, String skipReason) {
        public static Decision install() { return new Decision(true, null); }
        public static Decision skip(String reason) { return new Decision(false, reason); }
    }

    /**
     * Pure decision logic. Inject {@code pluginEnabled} and {@code foliaPresent}
     * for testing.
     */
    public static Decision evaluate(Mode mode,
                                    Predicate<String> pluginEnabled,
                                    BooleanSupplier foliaPresent) {
        switch (mode) {
            case DISABLED: return Decision.skip("config");
            case ENABLED:  return Decision.install();
            case AUTO:
            default:
                for (String name : BLOCKERS) {
                    if (pluginEnabled.test(name)) return Decision.skip(name);
                }
                if (foliaPresent.getAsBoolean()) return Decision.skip("Folia");
                return Decision.install();
        }
    }

    /** Live evaluation against the running server. */
    public static Decision evaluateLive(Mode mode) {
        return evaluate(
                mode,
                name -> Bukkit.getPluginManager().isPluginEnabled(name),
                () -> {
                    try { Class.forName(FOLIA_CLASS); return true; }
                    catch (ClassNotFoundException e) { return false; }
                });
    }
}
