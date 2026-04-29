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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Locale-safe canteen lore update. Exercises the pure helper
 * {@link Utils#computeUpdatedCanteenLore} which the live runtime path
 * delegates to, so the test does not need a Bukkit server / NBT-API.
 *
 * <p>Each test binds a fake locale, builds the same lore lines that
 * {@link cz.hashiri.harshlands.utils.HLItem#buildItem} would produce
 * for a freshly-built canteen in that locale, and asserts that the
 * helper finds and replaces the right lines with translated content
 * — never English literal text.</p>
 */
class CanteenLoreI18nTest {

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
    void english_locale_updates_durability_and_drink_lines(@TempDir Path root) throws IOException {
        bindLocale(root, "en-US", """
                items:
                  toughasnails:
                    canteen:
                      drink_line: "&7Drink: %DRINK%"
                      durability_line: "&7Durability: %CURRENT%/%MAX%"
                """);

        // Initial lines as a freshly-built canteen_empty would have them
        // (rendered from canteen_empty.lore translation).
        List<String> initial = new ArrayList<>(List.of(
                "§7Drink: None",
                "§7Durability: 0/5"));

        List<String> updated = Utils.computeUpdatedCanteenLore(
                initial, /*newDur=*/3, /*maxDur=*/5, /*drink=*/"Unpurified Water", /*isJuice=*/true);

        assertEquals(2, updated.size());
        assertEquals("§7Drink: Unpurified Water", updated.get(0));
        assertEquals("§7Durability: 3/5", updated.get(1));
    }

    @Test
    void chinese_locale_replaces_lines_with_chinese_text(@TempDir Path root) throws IOException {
        bindLocale(root, "zh-CN", """
                items:
                  toughasnails:
                    canteen:
                      drink_line: "&7饮品: %DRINK%"
                      durability_line: "&7耐久度: %CURRENT%/%MAX%"
                """);

        // Initial lines as a freshly-built canteen would have them under zh-CN
        // (assumes translator kept canteen_empty.lore in sync with the templates).
        List<String> initial = new ArrayList<>(List.of(
                "§7饮品: 无",
                "§7耐久度: 0/5"));

        List<String> updated = Utils.computeUpdatedCanteenLore(
                initial, /*newDur=*/3, /*maxDur=*/5, /*drink=*/"Unpurified Water", /*isJuice=*/true);

        assertEquals(2, updated.size());
        // Drink label translated; the value (%DRINK%) is the raw NBT string
        // (English drink names are out of scope of this migration).
        assertEquals("§7饮品: Unpurified Water", updated.get(0));
        assertEquals("§7耐久度: 3/5", updated.get(1));
    }

    @Test
    void non_juice_canteen_only_updates_durability(@TempDir Path root) throws IOException {
        bindLocale(root, "en-US", """
                items:
                  toughasnails:
                    canteen:
                      drink_line: "&7Drink: %DRINK%"
                      durability_line: "&7Durability: %CURRENT%/%MAX%"
                """);

        List<String> initial = new ArrayList<>(List.of(
                "§7Drink: None",
                "§7Durability: 5/5"));

        // isJuice=false simulates an item that has durability but no hldrink
        // NBT tag — the drink line should NOT be touched.
        List<String> updated = Utils.computeUpdatedCanteenLore(
                initial, /*newDur=*/4, /*maxDur=*/5, /*drink=*/null, /*isJuice=*/false);

        assertEquals(2, updated.size());
        assertEquals("§7Drink: None", updated.get(0));   // unchanged
        assertEquals("§7Durability: 4/5", updated.get(1));
    }

    @Test
    void mismatched_locale_lines_pass_through_unchanged(@TempDir Path root) throws IOException {
        // Bound locale templates use Chinese; lore lines came from a
        // different locale (English). Helper must miss gracefully — no
        // replacement, no exception.
        bindLocale(root, "zh-CN", """
                items:
                  toughasnails:
                    canteen:
                      drink_line: "&7饮品: %DRINK%"
                      durability_line: "&7耐久度: %CURRENT%/%MAX%"
                """);

        List<String> initial = new ArrayList<>(List.of(
                "§7Drink: None",         // English — won't match Chinese template
                "§7Durability: 0/5"));   // English — won't match Chinese template

        List<String> updated = Utils.computeUpdatedCanteenLore(
                initial, /*newDur=*/3, /*maxDur=*/5, /*drink=*/"Unpurified Water", /*isJuice=*/true);

        assertEquals(2, updated.size());
        assertEquals("§7Drink: None", updated.get(0));
        assertEquals("§7Durability: 0/5", updated.get(1));
    }

    private static void bindLocale(Path root, String locale, String yaml) throws IOException {
        Path dir = root.resolve(locale);
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("items.yml"), yaml);
        Messages.bind(new LocaleManager(root, locale));
        Messages.reload();
    }
}
