package cz.hashiri.harshlands.disease;

import cz.hashiri.harshlands.disease.model.CureMode;
import cz.hashiri.harshlands.disease.model.Disease;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiseasePlan3cParseTest {

    private static final String YAML = String.join("\n",
        "Diseases:",
        "  malnutrition:",
        "    Enabled: true",
        "    DisplayName: 'Malnutrition'",
        "    IncubationTicks: 2400",
        "    Mitigation:",
        "      Type: DIET",
        "      MalnourishedTiers: [STARVING, SEVERELY_MALNOURISHED, MALNOURISHED]",
        "    Stages:",
        "      - DurationTicks: 4800",
        "        Symptoms:",
        "          - Handler: BlockNaturalRegen",
        "            Params: {}",
        "      - DurationTicks: 4800",
        "        Symptoms:",
        "          - Handler: MaxHealthReduction",
        "            Params: { Hearts: 4 }",
        "      - DurationTicks: 0",
        "        Symptoms:",
        "          - Handler: PotionEffect",
        "            Params: { Effect: WEAKNESS, Amplifier: 0, DurationTicks: 120 }",
        "  dysentery:",
        "    Enabled: true",
        "    DisplayName: 'Dysentery'",
        "    IncubationTicks: 600",
        "    Cure:",
        "      ItemId: rehydration_salts",
        "    Mitigation:",
        "      Type: NO_EXPOSURE",
        "    Stages:",
        "      - DurationTicks: 2400",
        "        Symptoms:",
        "          - Handler: HungerDrain",
        "            Params: { Amount: 1 }",
        "      - DurationTicks: 2400",
        "        Symptoms:",
        "          - Handler: HungerDrain",
        "            Params: { Amount: 2 }",
        "          - Handler: PotionEffect",
        "            Params: { Effect: NAUSEA, Amplifier: 0, DurationTicks: 120 }",
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

    @Test void malnutrition_fields() {
        Disease d = byId("malnutrition");
        assertEquals("Malnutrition", d.displayName());
        assertEquals("", d.cureItemId());
        assertEquals("DIET", d.mitigationType());
        assertEquals(3, d.maxStage());
        assertEquals("BlockNaturalRegen", d.stage(1).symptoms().get(0).handlerName());
        assertEquals("MaxHealthReduction", d.stage(2).symptoms().get(0).handlerName());
        assertEquals(4.0, d.stage(2).symptoms().get(0).params().getDouble("Hearts"), 1e-9);
    }

    @Test void dysentery_fields() {
        Disease d = byId("dysentery");
        assertEquals("rehydration_salts", d.cureItemId());
        assertEquals(CureMode.CLEAR, d.cureMode());
        assertEquals("NO_EXPOSURE", d.mitigationType());
        assertEquals("HungerDrain", d.stage(1).symptoms().get(0).handlerName());
        assertEquals("DamageOverTime", d.stage(3).symptoms().get(0).handlerName());
    }
}
