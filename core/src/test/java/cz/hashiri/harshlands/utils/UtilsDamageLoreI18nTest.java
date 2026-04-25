package cz.hashiri.harshlands.utils;

import be.seeseemelk.mockbukkit.MockBukkit;
import cz.hashiri.harshlands.locale.LocaleManager;
import cz.hashiri.harshlands.locale.Messages;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for the cross-locale damage-line lookup used by Sharpness
 * recompute. The implementation in Utils derives prefix/suffix from the
 * translation template; this test exercises that derivation in two locales
 * by reflectively invoking the private helper.
 *
 * <p>Bukkit must be mocked because Utils has a static initializer that calls
 * {@link org.bukkit.Bukkit#getServer()} to load the NMS internals provider.
 * MockBukkit gives us a non-null server so class init can complete; the NMS
 * lookup itself fails harmlessly (logged, internals stays null) — that is fine
 * for this test which only invokes a pure helper.</p>
 */
class UtilsDamageLoreI18nTest {

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
    void prefix_and_suffix_split_correctly_for_english(@TempDir Path root) throws Exception {
        Path en = root.resolve("en-US");
        Files.createDirectories(en);
        Files.writeString(en.resolve("item_stats.yml"), """
                item_stats:
                  attack_damage: "&2 %VALUE% Attack Damage"
                """);
        Messages.bind(new LocaleManager(root, "en-US"));
        Messages.reload();

        String[] parts = invokeTemplateParts("item_stats.attack_damage");
        assertEquals("\u00A72 ", parts[0]);
        assertEquals(" Attack Damage", parts[1]);
    }

    @Test
    void prefix_and_suffix_split_correctly_for_chinese(@TempDir Path root) throws Exception {
        Path zh = root.resolve("zh-CN");
        Files.createDirectories(zh);
        Files.writeString(zh.resolve("item_stats.yml"), """
                item_stats:
                  attack_damage: "&2 %VALUE% \u653B\u51FB\u4F24\u5BB3"
                """);
        Messages.bind(new LocaleManager(root, "zh-CN"));
        Messages.reload();

        String[] parts = invokeTemplateParts("item_stats.attack_damage");
        assertEquals("\u00A72 ", parts[0]);
        assertEquals(" \u653B\u51FB\u4F24\u5BB3", parts[1]);
        // Sanity: the Chinese suffix must NOT match the English literal — the bug we're guarding against.
        assertNotEquals(" Attack Damage", parts[1]);
        assertTrue(parts[1].contains("\u653B\u51FB"));
    }

    /** Reflectively invokes the private static {@code Utils.valueTemplateParts(String)}. */
    private static String[] invokeTemplateParts(String key) throws Exception {
        Method m = Utils.class.getDeclaredMethod("valueTemplateParts", String.class);
        m.setAccessible(true);
        return (String[]) m.invoke(null, key);
    }
}
