package cz.hashiri.harshlands.utils;

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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that addGearStats pulls its attribute lines from the locale
 * system rather than hardcoded English. We bypass the Bukkit Attribute
 * parameter by calling Messages directly with the same keys the impl
 * uses and checking the formatted output contains the translated label.
 */
class LorePresetsI18nTest {

    @AfterEach
    void tearDown() {
        Messages.reset();
    }

    @Test
    void attack_damage_resolves_from_locale(@TempDir Path root) throws IOException {
        Path zh = root.resolve("zh-CN");
        Files.createDirectories(zh);
        Files.writeString(zh.resolve("item_stats.yml"), """
                item_stats:
                  attack_damage: "&2 %VALUE% \u653B\u51FB\u4F24\u5BB3"
                """);
        Messages.bind(new LocaleManager(root, "zh-CN"));
        Messages.reload();

        String line = Messages.get("item_stats.attack_damage", java.util.Map.of("VALUE", "7"));
        assertTrue(line.contains("7"), "expected formatted value: " + line);
        assertTrue(line.contains("\u653B\u51FB\u4F24\u5BB3"),
                "expected Chinese 'attack damage' in line: " + line);
    }

    @Test
    void all_four_stat_keys_present_in_enUS_file() throws IOException, URISyntaxException {
        // Load via classpath so the test is independent of JVM working directory.
        java.net.URL url = getClass().getClassLoader().getResource("Translations/en-US/item_stats.yml");
        assertTrue(url != null, "item_stats.yml must ship with en-US (not found on test classpath)");
        String contents = Files.readString(Path.of(url.toURI()));
        for (String key : List.of("attack_damage:", "attack_speed:", "armor:", "armor_toughness:")) {
            assertTrue(contents.contains(key), "missing key " + key + " in " + url);
        }
    }

    @Test
    void every_preset_key_exists_in_enUS_file() throws java.net.URISyntaxException, IOException {
        java.net.URL url = getClass().getClassLoader().getResource("Translations/en-US/lore_presets.yml");
        assertTrue(url != null, "lore_presets.yml must ship with en-US (not found on test classpath)");
        String contents = Files.readString(Path.of(url.toURI()));
        for (String preset : List.of(
                "rapier", "katana", "greatsword", "longsword", "spear", "saber",
                "boomerang", "dagger", "glaive", "halberd", "hammer", "javelin",
                "lance", "mace", "pike", "quarterstaff", "tomahawk", "throwing_knife",
                "warhammer", "battleaxe", "club", "cestus", "crossbow", "longbow",
                "flamed_dragonbone", "iced_dragonbone", "lightning_dragonbone",
                "fire_dragonsteel", "ice_dragonsteel", "lightning_dragonsteel",
                "blue_dragonscale", "bronze_dragonscale", "gray_dragonscale",
                "green_dragonscale", "red_dragonscale", "sapphire_dragonscale",
                "silver_dragonscale", "white_dragonscale", "amethyst_dragonscale",
                "black_dragonscale", "copper_dragonscale", "electric_dragonscale",
                "blue_sea_serpent_scale", "bronze_sea_serpent_scale",
                "deepblue_sea_serpent_scale", "green_sea_serpent_scale",
                "purple_sea_serpent_scale", "red_sea_serpent_scale",
                "teal_sea_serpent_scale", "dragon_protection", "tide_guardian_breathing",
                "legendary_weapon", "flamed_extra_damage", "iced_extra_damage",
                "lightning_extra_damage", "dragon_skull", "weapon", "helmet",
                "chestplate", "leggings", "boots")) {
            assertTrue(contents.contains("  " + preset + ":"),
                    "missing preset '" + preset + "' in lore_presets.yml");
        }
    }
}
