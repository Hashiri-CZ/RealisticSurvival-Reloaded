package cz.hashiri.harshlands.disease;

import cz.hashiri.harshlands.disease.model.CureMode;
import cz.hashiri.harshlands.disease.model.Disease;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiseasePlan3dParseTest {

    private static final String YAML = String.join("\n",
        "Diseases:",
        "  battle_trauma:",
        "    Enabled: true",
        "    DisplayName: 'Battle Trauma'",
        "    IncubationTicks: 600",
        "    Mitigation:",
        "      Type: NO_EXPOSURE",
        "    Stages:",
        "      - DurationTicks: 4800",
        "        Symptoms:",
        "          - Handler: Jitters",
        "            Params: { Chance: 0.3, DurationTicks: 60 }",
        "      - DurationTicks: 4800",
        "        Symptoms:",
        "          - Handler: PotionEffect",
        "            Params: { Effect: WEAKNESS, Amplifier: 0, DurationTicks: 120 }",
        "      - DurationTicks: 0",
        "        Symptoms:",
        "          - Handler: PotionEffect",
        "            Params: { Effect: MINING_FATIGUE, Amplifier: 0, DurationTicks: 120 }",
        "  sybok:",
        "    Enabled: true",
        "    DisplayName: 'Sybok'",
        "    IncubationTicks: 2400",
        "    Immunity:",
        "      DurationTicks: 24000",
        "    Cure:",
        "      ItemId: experimental_treatment",
        "      Mode: CLEAR_BEFORE_TERMINAL",
        "    Stages:",
        "      - DurationTicks: 9600",
        "        Symptoms: []",
        "      - DurationTicks: 9600",
        "        Symptoms:",
        "          - Handler: PotionEffect",
        "            Params: { Effect: WEAKNESS, Amplifier: 0, DurationTicks: 120 }",
        "      - DurationTicks: 0",
        "        Symptoms:",
        "          - Handler: DamageOverTime",
        "            Params: { Amount: 1.0 }");

    private static List<Disease> parsed() {
        return DiseaseRegistry.parse(
            YamlConfiguration.loadConfiguration(new StringReader(YAML)).getConfigurationSection("Diseases"));
    }

    private static Disease byId(String id) {
        return parsed().stream().filter(d -> d.id().equals(id)).findFirst().orElseThrow();
    }

    @Test void both_diseases_parse() {
        assertEquals(2, parsed().size());
    }

    @Test void battle_trauma_fields() {
        Disease d = byId("battle_trauma");
        assertEquals("Battle Trauma", d.displayName());
        assertEquals("", d.cureItemId());
        assertEquals("NO_EXPOSURE", d.mitigationType());
        assertEquals(3, d.maxStage());
        assertEquals("Jitters", d.stage(1).symptoms().get(0).handlerName());
    }

    @Test void sybok_fields() {
        Disease d = byId("sybok");
        assertEquals("experimental_treatment", d.cureItemId());
        assertEquals(CureMode.CLEAR_BEFORE_TERMINAL, d.cureMode());
        assertEquals(24000L, d.immunityDurationTicks());
        assertEquals(0, d.stage(1).symptoms().size());
        assertEquals("DamageOverTime", d.stage(3).symptoms().get(0).handlerName());
    }
}
