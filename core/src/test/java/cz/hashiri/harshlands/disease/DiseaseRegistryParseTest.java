package cz.hashiri.harshlands.disease;

import cz.hashiri.harshlands.disease.model.Disease;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiseaseRegistryParseTest {

    private static final String YAML = String.join("\n",
        "Diseases:",
        "  respiratory_infection:",
        "    Enabled: true",
        "    DisplayName: 'Respiratory Infection'",
        "    IncubationTicks: 1200",
        "    Immunity:",
        "      DurationTicks: 0",
        "    Cure:",
        "      ItemId: cold_remedy",
        "    Mitigation:",
        "      Type: TAN_WARM_DRY",
        "    Stages:",
        "      - DurationTicks: 2400",
        "        Symptoms:",
        "          - Handler: PlaySound",
        "            Params: { Sound: ENTITY_PLAYER_HURT, Volume: 0.6, Pitch: 1.2 }",
        "      - DurationTicks: 2400",
        "        Symptoms:",
        "          - Handler: PotionEffect",
        "            Params: { Effect: SLOW, Amplifier: 0, DurationTicks: 80 }",
        "      - DurationTicks: 0",
        "        Symptoms:",
        "          - Handler: DamageOverTime",
        "            Params: { Amount: 1.0 }",
        "  disabled_one:",
        "    Enabled: false",
        "    DisplayName: 'Nope'",
        "    Stages: []");

    private static YamlConfiguration cfg() {
        return YamlConfiguration.loadConfiguration(new StringReader(YAML));
    }

    @Test void parses_only_enabled_diseases() {
        List<Disease> diseases = DiseaseRegistry.parse(cfg().getConfigurationSection("Diseases"));
        assertEquals(1, diseases.size());
        assertEquals("respiratory_infection", diseases.get(0).id());
    }

    @Test void parses_core_fields() {
        Disease d = DiseaseRegistry.parse(cfg().getConfigurationSection("Diseases")).get(0);
        assertEquals("Respiratory Infection", d.displayName());
        assertEquals(1200, d.incubationTicks());
        assertEquals(0, d.immunityDurationTicks());
        assertEquals("cold_remedy", d.cureItemId());
        assertEquals("TAN_WARM_DRY", d.mitigationType());
    }

    @Test void parses_stages_and_symptoms_in_order() {
        Disease d = DiseaseRegistry.parse(cfg().getConfigurationSection("Diseases")).get(0);
        assertEquals(3, d.maxStage());
        assertEquals(2400, d.stage(1).durationTicks());
        assertEquals("PlaySound", d.stage(1).symptoms().get(0).handlerName());
        assertEquals("ENTITY_PLAYER_HURT",
            d.stage(1).symptoms().get(0).params().getString("Sound"));
        assertEquals("PotionEffect", d.stage(2).symptoms().get(0).handlerName());
        assertEquals(0, d.stage(3).durationTicks());
        assertEquals("DamageOverTime", d.stage(3).symptoms().get(0).handlerName());
    }

    @Test void null_section_yields_empty_list() {
        assertTrue(DiseaseRegistry.parse(null).isEmpty());
    }

    @Test void empty_stages_yields_empty_unmodifiable_list() {
        String yaml = String.join("\n",
            "Diseases:",
            "  bare:",
            "    Enabled: true",
            "    DisplayName: Bare",
            "    Stages: []");
        Disease d = DiseaseRegistry.parse(
            YamlConfiguration.loadConfiguration(new StringReader(yaml)).getConfigurationSection("Diseases")).get(0);
        assertEquals(0, d.maxStage());
        assertTrue(d.stage(1) == null);
    }

    @Test void missing_cure_section_defaults_to_empty_string() {
        String yaml = String.join("\n",
            "Diseases:",
            "  nocure:",
            "    Enabled: true",
            "    DisplayName: NoCure",
            "    Stages: []");
        Disease d = DiseaseRegistry.parse(
            YamlConfiguration.loadConfiguration(new StringReader(yaml)).getConfigurationSection("Diseases")).get(0);
        assertEquals("", d.cureItemId());
    }
}
