package cz.hashiri.harshlands.firstaid;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FaHealItemTest {

    @Test
    void parses_full_block() {
        String yaml = """
                bandage:
                  RestoreAmount: 2.0
                  AffectsParts:
                    - TORSO
                    - ARM_LEFT
                  Sound: "ITEM_ARMOR_EQUIP_LEATHER"
                """;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new StringReader(yaml));

        FaHealItem item = FaHealItem.parse("bandage", cfg.getConfigurationSection("bandage"));

        assertEquals("bandage", item.name());
        assertEquals(2.0, item.restoreAmount());
        assertEquals(List.of("TORSO", "ARM_LEFT"), item.affectsParts());
        assertEquals("ITEM_ARMOR_EQUIP_LEATHER", item.sound());
    }

    @Test
    void parse_returns_null_for_missing_restore_amount() {
        String yaml = """
                splint:
                  AffectsParts: [LEG_LEFT]
                """;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new StringReader(yaml));

        assertNull(FaHealItem.parse("splint", cfg.getConfigurationSection("splint")));
    }

    @Test
    void parse_returns_null_for_empty_affects_parts() {
        String yaml = """
                splint:
                  RestoreAmount: 4.0
                  AffectsParts: []
                """;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new StringReader(yaml));

        assertNull(FaHealItem.parse("splint", cfg.getConfigurationSection("splint")));
    }

    @Test
    void parse_returns_null_for_null_section() {
        assertNull(FaHealItem.parse("ghost", null));
    }

    @Test
    void empty_sound_means_silent() {
        String yaml = """
                medical_kit:
                  RestoreAmount: 6.0
                  AffectsParts: [HEAD]
                  Sound: ""
                """;
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new StringReader(yaml));

        FaHealItem item = FaHealItem.parse("medical_kit", cfg.getConfigurationSection("medical_kit"));

        assertEquals("", item.sound());
    }
}
