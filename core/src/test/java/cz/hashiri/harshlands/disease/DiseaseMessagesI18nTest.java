package cz.hashiri.harshlands.disease;

import cz.hashiri.harshlands.locale.LocaleManager;
import cz.hashiri.harshlands.locale.Messages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the Disease module's player-facing strings resolve through the
 * locale system (Messages/LocaleManager) rather than being hardcoded English
 * with literal legacy color codes, and that interpolated values (disease
 * display name, stage number) go through the placeholder mechanism so a
 * translator can reorder them.
 *
 * <p>Mirrors the pattern used by {@code LorePresetsI18nTest} / {@code
 * CanteenLoreI18nTest}: fake-locale tests exercise the mechanics, and a
 * final test binds directly against the real shipped
 * {@code Translations/en-US} resources to catch a key that was used in code
 * but never actually added to the shipped file (which would otherwise
 * silently render as the "[key]" missing-translation fallback in-game).</p>
 */
class DiseaseMessagesI18nTest {

    @AfterEach
    void tearDown() {
        Messages.reset();
    }

    @Test
    void no_infection_message_interpolates_disease_name(@TempDir Path root) throws IOException {
        bindFakeLocale(root, """
                disease:
                  messages:
                    no_infection: "&7Nothing happens — you don't have %disease%."
                """);

        String out = Messages.of("disease.messages.no_infection").with("disease", "Sybok").build();
        assertEquals("§7Nothing happens — you don't have Sybok.", out);
    }

    @Test
    void too_advanced_message_interpolates_disease_name(@TempDir Path root) throws IOException {
        bindFakeLocale(root, """
                disease:
                  messages:
                    too_advanced: "&7The %disease% is too advanced — the treatment can't cure it now."
                """);

        String out = Messages.of("disease.messages.too_advanced").with("disease", "Battle Trauma").build();
        assertEquals("§7The Battle Trauma is too advanced — the treatment can't cure it now.", out);
    }

    @Test
    void dose_on_cooldown_message_has_no_placeholders(@TempDir Path root) throws IOException {
        bindFakeLocale(root, """
                disease:
                  messages:
                    dose_on_cooldown: "&7The treatment hasn't taken hold yet — wait before the next dose."
                """);

        String out = Messages.of("disease.messages.dose_on_cooldown").build();
        assertEquals("§7The treatment hasn't taken hold yet — wait before the next dose.", out);
    }

    @Test
    void treated_and_recovery_messages_interpolate_disease_name(@TempDir Path root) throws IOException {
        bindFakeLocale(root, """
                disease:
                  messages:
                    treated: "&aYou treated your %disease%."
                    fully_recovered: "&aYou have fully recovered from %disease%."
                    regressed_stage: "&aThe regimen pushes your %disease% back a stage."
                """);

        assertEquals("§aYou treated your Rust Fever.",
                Messages.of("disease.messages.treated").with("disease", "Rust Fever").build());
        assertEquals("§aYou have fully recovered from Rust Fever.",
                Messages.of("disease.messages.fully_recovered").with("disease", "Rust Fever").build());
        assertEquals("§aThe regimen pushes your Rust Fever back a stage.",
                Messages.of("disease.messages.regressed_stage").with("disease", "Rust Fever").build());
    }

    @Test
    void diagnosis_messages_interpolate_disease_name_and_stage(@TempDir Path root) throws IOException {
        bindFakeLocale(root, """
                disease:
                  diagnosis:
                    healthy: "&7You appear to be in good health."
                    header: "&6Diagnosis:"
                    incubating: "&7- %disease% &8(incubating)"
                    active: "&c- %disease% &7(stage %stage%)"
                """);

        assertEquals("§7You appear to be in good health.", Messages.of("disease.diagnosis.healthy").build());
        assertEquals("§6Diagnosis:", Messages.of("disease.diagnosis.header").build());
        assertEquals("§7- Jitters §8(incubating)",
                Messages.of("disease.diagnosis.incubating").with("disease", "Jitters").build());
        assertEquals("§c- Jitters §7(stage 3)",
                Messages.of("disease.diagnosis.active").with("disease", "Jitters").with("stage", 3).build());
    }

    @Test
    void item_use_failure_action_bar_resolves(@TempDir Path root) throws IOException {
        bindFakeLocale(root, """
                disease:
                  symptom:
                    item_use_failure:
                      action_bar: "&5Your hands won't cooperate..."
                """);

        assertEquals("§5Your hands won't cooperate...",
                Messages.get("disease.symptom.item_use_failure.action_bar"));
    }

    /**
     * Guards against forgetting to ship a key: binds directly against the real
     * production {@code Translations/en-US} directory (not a fake temp
     * fixture) and asserts every key used by the Disease module resolves to
     * real translated text, not the "[key]" bracket fallback that
     * {@link LocaleManager#get(String)} returns for a missing key.
     */
    @Test
    void all_disease_keys_resolve_against_shipped_enUS_resources() throws IOException, URISyntaxException {
        java.net.URL url = getClass().getClassLoader().getResource("Translations/en-US/disease.yml");
        assertTrue(url != null, "disease.yml must ship with en-US (not found on test classpath)");
        Path diseaseYml = Path.of(url.toURI());
        Path enUSDir = diseaseYml.getParent();
        Path translationsRoot = enUSDir.getParent();

        Messages.bind(new LocaleManager(translationsRoot, enUSDir.getFileName().toString()));
        Messages.reload();

        List<String> keys = List.of(
                "disease.messages.no_infection",
                "disease.messages.too_advanced",
                "disease.messages.dose_on_cooldown",
                "disease.messages.treated",
                "disease.messages.fully_recovered",
                "disease.messages.regressed_stage",
                "disease.diagnosis.healthy",
                "disease.diagnosis.header",
                "disease.diagnosis.incubating",
                "disease.diagnosis.active",
                "disease.symptom.item_use_failure.action_bar");

        for (String key : keys) {
            String resolved = Messages.get(key);
            assertFalse(resolved.equals("[" + key + "]"),
                    "key " + key + " is missing from the shipped en-US disease.yml (resolved to fallback)");
        }

        // Placeholder-bearing keys must still contain interpolated values, not raw %tokens%.
        assertEquals("§7Nothing happens — you don't have Sybok.",
                Messages.of("disease.messages.no_infection").with("disease", "Sybok").build());
        assertEquals("§c- Jitters §7(stage 2)",
                Messages.of("disease.diagnosis.active").with("disease", "Jitters").with("stage", 2).build());
    }

    private static void bindFakeLocale(Path root, String yaml) throws IOException {
        Path dir = root.resolve("en-US");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve("disease.yml"), yaml);
        Messages.bind(new LocaleManager(root, "en-US"));
        Messages.reload();
    }
}
