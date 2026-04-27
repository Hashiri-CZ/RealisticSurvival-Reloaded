package cz.hashiri.harshlands.utils;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BossbarSentryProbeTest {

    @Test void no_blockers_returns_install() {
        BossbarSentryProbe.Decision d = BossbarSentryProbe.evaluate(
                BossbarSentryProbe.Mode.AUTO, name -> false, () -> false);
        assertTrue(d.shouldInstall());
        assertNull(d.skipReason());
    }

    @Test void betterhud_present_skips_in_auto() {
        BossbarSentryProbe.Decision d = BossbarSentryProbe.evaluate(
                BossbarSentryProbe.Mode.AUTO, "BetterHud"::equals, () -> false);
        assertFalse(d.shouldInstall());
        assertEquals("BetterHud", d.skipReason());
    }

    @Test void nova_present_skips_in_auto() {
        BossbarSentryProbe.Decision d = BossbarSentryProbe.evaluate(
                BossbarSentryProbe.Mode.AUTO, Set.of("Nova")::contains, () -> false);
        assertFalse(d.shouldInstall());
        assertEquals("Nova", d.skipReason());
    }

    @Test void nova_framework_present_skips_in_auto() {
        BossbarSentryProbe.Decision d = BossbarSentryProbe.evaluate(
                BossbarSentryProbe.Mode.AUTO, Set.of("NovaFramework")::contains, () -> false);
        assertFalse(d.shouldInstall());
        assertEquals("NovaFramework", d.skipReason());
    }

    @Test void folia_skips_in_auto() {
        BossbarSentryProbe.Decision d = BossbarSentryProbe.evaluate(
                BossbarSentryProbe.Mode.AUTO, name -> false, () -> true);
        assertFalse(d.shouldInstall());
        assertEquals("Folia", d.skipReason());
    }

    @Test void enabled_overrides_blockers() {
        BossbarSentryProbe.Decision d = BossbarSentryProbe.evaluate(
                BossbarSentryProbe.Mode.ENABLED, "BetterHud"::equals, () -> true);
        assertTrue(d.shouldInstall());
    }

    @Test void disabled_skips_unconditionally() {
        BossbarSentryProbe.Decision d = BossbarSentryProbe.evaluate(
                BossbarSentryProbe.Mode.DISABLED, name -> false, () -> false);
        assertFalse(d.shouldInstall());
        assertEquals("config", d.skipReason());
    }

    @Test void mode_parses_case_insensitive() {
        assertEquals(BossbarSentryProbe.Mode.AUTO, BossbarSentryProbe.Mode.parse("auto"));
        assertEquals(BossbarSentryProbe.Mode.ENABLED, BossbarSentryProbe.Mode.parse("ENABLED"));
        assertEquals(BossbarSentryProbe.Mode.DISABLED, BossbarSentryProbe.Mode.parse("Disabled"));
    }

    @Test void mode_parse_falls_back_to_auto_on_garbage() {
        assertEquals(BossbarSentryProbe.Mode.AUTO, BossbarSentryProbe.Mode.parse("garbage"));
        assertEquals(BossbarSentryProbe.Mode.AUTO, BossbarSentryProbe.Mode.parse(null));
    }
}
