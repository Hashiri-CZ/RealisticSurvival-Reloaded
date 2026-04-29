package cz.hashiri.harshlands.locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MessagesPlaceholderTest {

    @AfterEach
    void tearDown() {
        Messages.reset();
    }

    private static void bind(Path root, String key, String raw, String locale) throws IOException {
        Path dir = root.resolve(locale);
        Files.createDirectories(dir);
        Files.write(dir.resolve("x.yml"),
                (key + ": \"" + raw + "\"\n").getBytes(StandardCharsets.UTF_8));
        Messages.bind(new LocaleManager(root, locale));
        Messages.reload();
    }

    @Test
    void single_placeholder_replaced(@TempDir Path root) throws IOException {
        bind(root, "msg", "hello %NAME%!", "en-US");
        assertEquals("hello world!", Messages.get("msg", Map.of("NAME", "world")));
    }

    @Test
    void repeated_placeholder_replaced_each_occurrence(@TempDir Path root) throws IOException {
        bind(root, "msg", "%X% then %X% again", "en-US");
        assertEquals("a then a again", Messages.get("msg", Map.of("X", "a")));
    }

    @Test
    void multiple_distinct_placeholders(@TempDir Path root) throws IOException {
        bind(root, "msg", "%A%-%B%-%C%", "en-US");
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("A", "1"); p.put("B", "2"); p.put("C", "3");
        assertEquals("1-2-3", Messages.get("msg", p));
    }

    @Test
    void unknown_placeholder_left_intact(@TempDir Path root) throws IOException {
        bind(root, "msg", "x %UNKNOWN% y", "en-US");
        assertEquals("x %UNKNOWN% y", Messages.get("msg", Map.of("OTHER", "z")));
    }

    @Test
    void replacement_with_special_chars_not_interpreted(@TempDir Path root) throws IOException {
        bind(root, "msg", "result=%V%", "en-US");
        // $1 must be a literal in the result, not a regex backreference.
        assertEquals("result=$1\\foo", Messages.get("msg", Map.of("V", "$1\\foo")));
    }

    @Test
    void empty_placeholders_returns_raw(@TempDir Path root) throws IOException {
        bind(root, "msg", "no replacements here", "en-US");
        assertEquals("no replacements here", Messages.get("msg", Map.of()));
    }
}
