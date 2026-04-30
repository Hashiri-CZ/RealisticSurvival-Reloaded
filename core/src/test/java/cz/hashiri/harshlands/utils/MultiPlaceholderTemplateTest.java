package cz.hashiri.harshlands.utils;

import be.seeseemelk.mockbukkit.MockBukkit;
import cz.hashiri.harshlands.locale.LocaleManager;
import cz.hashiri.harshlands.locale.Messages;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for the multi-placeholder generalisation of valueTemplateParts.
 * The single-placeholder case is also exercised here as a regression guard
 * for the existing Attack Damage call site at Utils.java:495.
 *
 * <p>Bukkit must be mocked because Utils has a static initializer that calls
 * {@link org.bukkit.Bukkit#getServer()} to load the NMS internals provider.</p>
 */
class MultiPlaceholderTemplateTest {

    @BeforeAll
    static void mockServer() {
        MockBukkit.mock();
    }

    @AfterAll
    static void unmockServer() {
        MockBukkit.unmock();
    }

    @AfterEach
    void tearDown() {
        Messages.reset();
    }

    @Test
    void single_placeholder_splits_into_prefix_and_suffix(@TempDir Path root) throws IOException {
        bindLocale(root, "en-US", """
                item_stats:
                  attack_damage: "&2 %VALUE% Attack Damage"
                """);

        TemplateParts parts = Utils.valueTemplateParts("item_stats.attack_damage", "VALUE");

        assertEquals(2, parts.segments().size());
        assertEquals("§2 ", parts.segments().get(0));
        assertEquals(" Attack Damage", parts.segments().get(1));
    }

    @Test
    void two_placeholders_split_into_three_segments(@TempDir Path root) throws IOException {
        bindLocale(root, "en-US", """
                items:
                  toughasnails:
                    canteen:
                      durability_line: "&7Durability: %CURRENT%/%MAX%"
                """);

        TemplateParts parts = Utils.valueTemplateParts(
                "items.toughasnails.canteen.durability_line", "CURRENT", "MAX");

        assertEquals(3, parts.segments().size());
        assertEquals("§7Durability: ", parts.segments().get(0));
        assertEquals("/", parts.segments().get(1));
        assertEquals("", parts.segments().get(2));
    }

    @Test
    void matches_returns_true_for_a_real_translated_line(@TempDir Path root) throws IOException {
        bindLocale(root, "en-US", """
                items:
                  toughasnails:
                    canteen:
                      durability_line: "&7Durability: %CURRENT%/%MAX%"
                """);

        TemplateParts parts = Utils.valueTemplateParts(
                "items.toughasnails.canteen.durability_line", "CURRENT", "MAX");

        assertTrue(parts.matches("§7Durability: 3/5"));
        assertTrue(parts.matches("§7Durability: 0/5"));
    }

    @Test
    void matches_returns_false_for_unrelated_text(@TempDir Path root) throws IOException {
        bindLocale(root, "en-US", """
                items:
                  toughasnails:
                    canteen:
                      durability_line: "&7Durability: %CURRENT%/%MAX%"
                """);

        TemplateParts parts = Utils.valueTemplateParts(
                "items.toughasnails.canteen.durability_line", "CURRENT", "MAX");

        assertFalse(parts.matches("§7Drink: None"));
        assertFalse(parts.matches("not a canteen line"));
        // Different prefix (e.g. another locale's translation) — must miss gracefully.
        assertFalse(parts.matches("§7耐久度: 3/5"));
    }

    @Test
    void chinese_locale_yields_chinese_prefix(@TempDir Path root) throws IOException {
        bindLocale(root, "zh-CN", """
                items:
                  toughasnails:
                    canteen:
                      drink_line: "&7饮品: %DRINK%"
                """);

        TemplateParts parts = Utils.valueTemplateParts(
                "items.toughasnails.canteen.drink_line", "DRINK");

        assertEquals(2, parts.segments().size());
        assertEquals("§7饮品: ", parts.segments().get(0));
        assertEquals("", parts.segments().get(1));
        assertTrue(parts.matches("§7饮品: 未净化水"));
        assertFalse(parts.matches("§7Drink: Unpurified Water"));
    }

    @Test
    void translator_dropping_a_placeholder_handled_defensively(@TempDir Path root) throws IOException {
        // Translator forgot %MAX% — the rendered template only has %CURRENT%.
        bindLocale(root, "en-US", """
                items:
                  toughasnails:
                    canteen:
                      durability_line: "&7Durability: %CURRENT%"
                """);

        TemplateParts parts = Utils.valueTemplateParts(
                "items.toughasnails.canteen.durability_line", "CURRENT", "MAX");

        // The dropped %MAX% sentinel never appears in the rendered string;
        // its segment becomes empty and the remainder of the rendered text
        // becomes the tail. Caller should still get a usable TemplateParts
        // (no exception); matches() should not crash.
        assertTrue(parts.segments().size() >= 2,
                "expected at least prefix + tail segments, got " + parts.segments());
        // Doesn't crash:
        parts.matches("§7Durability: 3");
    }

    private static void bindLocale(Path root, String locale, String yaml) throws IOException {
        Path dir = root.resolve(locale);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("test.yml"), yaml);
        Messages.bind(new LocaleManager(root, locale));
        Messages.reload();
    }
}
