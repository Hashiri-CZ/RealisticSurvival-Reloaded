package cz.hashiri.harshlands.disease;

import cz.hashiri.harshlands.disease.model.CureMode;
import cz.hashiri.harshlands.disease.model.Disease;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiseasePlan3bParseTest {

    private static final String YAML = String.join("\n",
        "Diseases:",
        "  festering_wound:",
        "    Enabled: true",
        "    DisplayName: 'Festering Wound'",
        "    IncubationTicks: 1200",
        "    Immunity:",
        "      DurationTicks: 0",
        "    Cure:",
        "      ItemId: disinfectant_bandage",
        "    Mitigation:",
        "      Type: INJURY_HEAL",
        "      InjuredStates: [DAMAGED, BROKEN]",
        "    Stages:",
        "      - DurationTicks: 2400",
        "        Symptoms:",
        "          - Handler: PotionEffect",
        "            Params: { Effect: SLOWNESS, Amplifier: 0, DurationTicks: 100 }",
        "      - DurationTicks: 2400",
        "        Symptoms:",
        "          - Handler: PotionEffect",
        "            Params: { Effect: POISON, Amplifier: 0, DurationTicks: 100 }",
        "      - DurationTicks: 0",
        "        Symptoms:",
        "          - Handler: DamageOverTime",
        "            Params: { Amount: 1.0 }",
        "  tetanus:",
        "    Enabled: true",
        "    DisplayName: 'Tetanus'",
        "    IncubationTicks: 1200",
        "    Cure:",
        "      ItemId: antitoxin",
        "    Stages:",
        "      - DurationTicks: 2400",
        "        Symptoms:",
        "          - Handler: PotionEffect",
        "            Params: { Effect: SLOWNESS, Amplifier: 0, DurationTicks: 100 }",
        "      - DurationTicks: 2400",
        "        Symptoms:",
        "          - Handler: BlockEating",
        "            Params: { Chance: 1.0 }",
        "      - DurationTicks: 0",
        "        Symptoms:",
        "          - Handler: WEAKNESS_PLACEHOLDER",
        "            Params: {}");

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

    @Test void festering_wound_fields() {
        Disease d = byId("festering_wound");
        assertEquals("Festering Wound", d.displayName());
        assertEquals("disinfectant_bandage", d.cureItemId());
        assertEquals(CureMode.CLEAR, d.cureMode());
        assertEquals("INJURY_HEAL", d.mitigationType());
        assertEquals(3, d.maxStage());
        assertEquals("POISON", d.stage(2).symptoms().get(0).params().getString("Effect"));
        assertEquals("DamageOverTime", d.stage(3).symptoms().get(0).handlerName());
    }

    @Test void tetanus_uses_block_eating_at_stage_two() {
        Disease d = byId("tetanus");
        assertEquals("antitoxin", d.cureItemId());
        assertEquals("BlockEating", d.stage(2).symptoms().get(0).handlerName());
        assertEquals(1.0, d.stage(2).symptoms().get(0).params().getDouble("Chance"), 1e-9);
    }
}
