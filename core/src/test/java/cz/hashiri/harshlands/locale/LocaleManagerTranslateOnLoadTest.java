package cz.hashiri.harshlands.locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class LocaleManagerTranslateOnLoadTest {

    @Test
    void color_codes_translated_at_load_not_per_call(@TempDir Path root) throws IOException {
        Path en = root.resolve("en-US");
        Files.createDirectories(en);
        Files.write(en.resolve("x.yml"),
                "greeting: \"&aHello &bWorld\"\n".getBytes(StandardCharsets.UTF_8));

        LocaleManager m = new LocaleManager(root, "en-US");
        m.load();

        String first = m.get("greeting");
        String second = m.get("greeting");

        // Translation already applied: '&' replaced with section sign.
        assertEquals("§aHello §bWorld", first);
        // Same string instance returned both times — proves no per-call translation.
        assertSame(first, second, "expected cached, pre-translated value");
    }

    @Test
    void list_values_translated_at_load(@TempDir Path root) throws IOException {
        Path en = root.resolve("en-US");
        Files.createDirectories(en);
        Files.write(en.resolve("x.yml"),
                ("lines:\n  - \"&afirst\"\n  - \"&bsecond\"\n").getBytes(StandardCharsets.UTF_8));

        LocaleManager m = new LocaleManager(root, "en-US");
        m.load();

        java.util.List<String> result = m.getList("lines");
        assertEquals(java.util.List.of("§afirst", "§bsecond"), result);
    }
}
