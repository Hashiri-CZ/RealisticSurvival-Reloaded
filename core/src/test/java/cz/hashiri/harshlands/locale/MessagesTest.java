package cz.hashiri.harshlands.locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MessagesTest {

    @BeforeEach
    void reset() {
        Messages.reset();
    }

    @Test
    void get_delegates_to_bound_locale_manager(@TempDir Path root) throws IOException {
        Path enUS = root.resolve("en-US");
        Files.createDirectories(enUS);
        Files.writeString(enUS.resolve("x.yml"), "hello: \"world\"\n");
        Messages.bind(new LocaleManager(root, "en-US"));
        Messages.reload();

        assertEquals("world", Messages.get("hello"));
    }

    @Test
    void get_with_placeholders_substitutes(@TempDir Path root) throws IOException {
        Path enUS = root.resolve("en-US");
        Files.createDirectories(enUS);
        Files.writeString(enUS.resolve("x.yml"), "greet: \"Hello %player%, you have %count% items\"\n");
        Messages.bind(new LocaleManager(root, "en-US"));
        Messages.reload();

        String out = Messages.get("greet", Map.of("player", "Steve", "count", 3));
        assertEquals("Hello Steve, you have 3 items", out);
    }

    @Test
    void get_list_delegates(@TempDir Path root) throws IOException {
        Path enUS = root.resolve("en-US");
        Files.createDirectories(enUS);
        Files.writeString(enUS.resolve("x.yml"), """
                lore:
                  - "a"
                  - "b"
                """);
        Messages.bind(new LocaleManager(root, "en-US"));
        Messages.reload();

        assertEquals(List.of("a", "b"), Messages.getList("lore"));
    }

    @Test
    void builder_with_chain_substitutes_placeholders(@TempDir Path root) throws IOException {
        Path enUS = root.resolve("en-US");
        Files.createDirectories(enUS);
        Files.writeString(enUS.resolve("x.yml"), "give: \"Gave %amount% %item% to %player%\"\n");
        Messages.bind(new LocaleManager(root, "en-US"));
        Messages.reload();

        String out = Messages.of("give")
                .with("amount", 5)
                .with("item", "canteen")
                .with("player", "Steve")
                .build();

        assertEquals("Gave 5 canteen to Steve", out);
    }

    @Test
    void builder_send_on_null_recipient_throws() {
        assertThrows(NullPointerException.class, () -> Messages.of("any.key").send(null));
    }

    @Test
    void missing_key_without_bound_manager_returns_bracket_key() {
        Messages.reset();
        assertEquals("[x.y.z]", Messages.get("x.y.z"));
    }

    @Test
    void placeholder_substitution_is_case_insensitive(@TempDir Path root) throws IOException {
        // Legacy translation files may carry upper- or mixed-case %NAME% tokens
        // (inherited from pre-1.3.0 YAMLs). .with("name", ...) is lowercase.
        // All forms must resolve against the single lowercase placeholder key.
        Path enUS = root.resolve("en-US");
        Files.createDirectories(enUS);
        Files.writeString(enUS.resolve("x.yml"), """
                caps: "Initializing %NAME% module"
                mixed: "Initializing %Name% module"
                lower: "Initializing %name% module"
                """);
        Messages.bind(new LocaleManager(root, "en-US"));
        Messages.reload();

        assertEquals("Initializing Fear module", Messages.get("caps", Map.of("name", "Fear")));
        assertEquals("Initializing Fear module", Messages.get("mixed", Map.of("name", "Fear")));
        assertEquals("Initializing Fear module", Messages.get("lower", Map.of("name", "Fear")));
    }

    @Test
    void placeholder_replacement_handles_special_regex_chars(@TempDir Path root) throws IOException {
        // Values containing $ or \ must be inserted literally, not interpreted
        // as regex back-references when .replaceAll is used internally.
        Path enUS = root.resolve("en-US");
        Files.createDirectories(enUS);
        Files.writeString(enUS.resolve("x.yml"), "line: \"cost %amount%\"\n");
        Messages.bind(new LocaleManager(root, "en-US"));
        Messages.reload();

        assertEquals("cost $5.00", Messages.get("line", Map.of("amount", "$5.00")));
        assertEquals("cost a\\b", Messages.get("line", Map.of("amount", "a\\b")));
    }

    @Test
    void get_keys_returns_empty_when_not_bound() {
        Messages.reset();
        assertEquals(java.util.Set.of(), Messages.getKeys("hints.Obtain"));
    }

    @Test
    void get_keys_delegates_to_bound_locale_manager(@TempDir Path root) throws IOException {
        Path enUS = root.resolve("en-US");
        Files.createDirectories(enUS);
        Files.writeString(enUS.resolve("hints.yml"), """
                hints:
                  Obtain:
                    axe:
                      Name: "Axe"
                    saw:
                      Name: "Saw"
                """);
        Messages.bind(new LocaleManager(root, "en-US"));
        Messages.reload();

        assertEquals(java.util.Set.of("axe", "saw"), Messages.getKeys("hints.Obtain"));
    }
}
