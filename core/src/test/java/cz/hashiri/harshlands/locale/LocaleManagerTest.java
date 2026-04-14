package cz.hashiri.harshlands.locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocaleManagerTest {

    @Test
    void loads_flat_keys_from_nested_yaml(@TempDir Path translationsRoot) throws IOException {
        Path enUS = translationsRoot.resolve("en-US");
        Files.createDirectories(enUS);
        Files.writeString(enUS.resolve("commands.yml"), """
                commands:
                  give:
                    success: "Gave item to player"
                  no_permission: "No permission"
                """);

        LocaleManager mgr = new LocaleManager(translationsRoot, "en-US");
        mgr.load();

        assertEquals("Gave item to player", mgr.get("commands.give.success"));
        assertEquals("No permission", mgr.get("commands.no_permission"));
    }
}
