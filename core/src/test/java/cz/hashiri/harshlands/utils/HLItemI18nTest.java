package cz.hashiri.harshlands.utils;

import cz.hashiri.harshlands.locale.LocaleManager;
import cz.hashiri.harshlands.locale.Messages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HLItemI18nTest {

    @BeforeEach
    void bind(@TempDir Path root) throws IOException {
        Path en = root.resolve("en-US");
        Files.createDirectories(en);
        Files.writeString(en.resolve("items.yml"), """
                items:
                  test:
                    netherite_rapier:
                      display_name: "&fNetherite Rapier"
                """);
        Messages.bind(new LocaleManager(root, "en-US"));
        Messages.reload();
    }

    @AfterEach
    void tearDown() {
        Messages.reset();
    }

    @Test
    void literal_string_returned_as_is() {
        assertEquals("&fRegular Item", HLItem.resolveI18n("&fRegular Item"));
    }

    @Test
    void null_returned_as_null() {
        assertEquals(null, HLItem.resolveI18n(null));
    }

    @Test
    void i18n_prefix_resolves_to_translated_value() {
        // Messages.get runs ChatColor.translateAlternateColorCodes('&', ...) — expect section-sign form in result
        assertEquals("\u00A7fNetherite Rapier",
                HLItem.resolveI18n("i18n:items.test.netherite_rapier.display_name"));
    }

    @Test
    void i18n_prefix_with_missing_key_returns_bracketed_key() {
        // LocaleManager.get returns "[key]" for missing keys
        assertEquals("[items.test.unknown]",
                HLItem.resolveI18n("i18n:items.test.unknown"));
    }

    @Test
    void i18n_prefix_with_list_value_resolves_to_full_list(@TempDir Path root) throws IOException {
        Path en = root.resolve("en-US");
        Files.createDirectories(en);
        Files.writeString(en.resolve("items.yml"), """
                items:
                  test:
                    sample_item:
                      lore:
                        - "&7First line"
                        - "&7Second line"
                        - ""
                        - "&6Footer"
                """);
        Messages.bind(new LocaleManager(root, "en-US"));
        Messages.reload();

        // Messages.getList runs ChatColor.translateAlternateColorCodes('&', ...) on each entry.
        java.util.List<String> resolved = cz.hashiri.harshlands.locale.Messages.getList(
                "items.test.sample_item.lore");

        assertEquals(4, resolved.size());
        assertEquals("\u00A77First line", resolved.get(0));
        assertEquals("\u00A77Second line", resolved.get(1));
        assertEquals("", resolved.get(2));
        assertEquals("\u00A76Footer", resolved.get(3));
    }
}
