package cz.hashiri.harshlands.disease;

import cz.hashiri.harshlands.disease.model.Disease;
import cz.hashiri.harshlands.disease.model.DiseaseStage;
import cz.hashiri.harshlands.disease.model.SymptomSpec;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DiseaseRegistry {

    private final Map<String, Disease> byId = new ConcurrentHashMap<>();

    public void load(ConfigurationSection diseasesSection) {
        byId.clear();
        for (Disease d : parse(diseasesSection)) {
            byId.put(d.id(), d);
        }
    }

    public Collection<Disease> all() { return byId.values(); }

    public Disease get(String id) { return byId.get(id); }

    public Disease byCureItem(String itemId) {
        if (itemId == null) return null;
        for (Disease d : byId.values()) {
            if (itemId.equals(d.cureItemId())) return d;
        }
        return null;
    }

    /** Pure parse — only enabled diseases are returned. Safe to call with null. */
    public static List<Disease> parse(ConfigurationSection section) {
        List<Disease> result = new ArrayList<>();
        if (section == null) return result;

        for (String id : section.getKeys(false)) {
            ConfigurationSection ds = section.getConfigurationSection(id);
            if (ds == null) continue;
            if (!ds.getBoolean("Enabled", true)) continue;

            String displayName = ds.getString("DisplayName", id);
            long incubation = ds.getLong("IncubationTicks", 0L);
            long immunity = ds.getLong("Immunity.DurationTicks", 0L);
            String cureItem = ds.getString("Cure.ItemId", "");
            String mitigation = ds.getString("Mitigation.Type", "");

            List<DiseaseStage> stages = new ArrayList<>();
            List<?> rawStages = ds.getList("Stages");
            if (rawStages != null) {
                for (Object stageEntry : rawStages) {
                    ConfigurationSection stageSec = sectionFromListEntry(stageEntry);
                    long duration = stageSec.getLong("DurationTicks", 0L);
                    List<SymptomSpec> symptoms = new ArrayList<>();
                    List<?> rawSymptoms = stageSec.getList("Symptoms");
                    if (rawSymptoms != null) {
                        for (Object symEntry : rawSymptoms) {
                            ConfigurationSection symSec = sectionFromListEntry(symEntry);
                            String handler = symSec.getString("Handler", "");
                            // Bukkit may store the Params map as a raw Map rather than
                            // a child ConfigurationSection; materialise it explicitly.
                            ConfigurationSection params = symSec.getConfigurationSection("Params");
                            if (params == null) {
                                Object rawParams = symSec.get("Params");
                                if (rawParams instanceof Map<?, ?> paramsMap) {
                                    params = sectionFromListEntry(paramsMap);
                                }
                            }
                            symptoms.add(new SymptomSpec(handler, params));
                        }
                    }
                    stages.add(new DiseaseStage(duration, Collections.unmodifiableList(symptoms)));
                }
            }

            result.add(new Disease(id, displayName, true, incubation, immunity,
                    cureItem, mitigation, Collections.unmodifiableList(stages)));
        }
        return result;
    }

    /**
     * Bukkit deserialises YAML list entries as Maps, losing ConfigurationSection typing.
     * Wrap one list entry (a Map) into a MemoryConfiguration so nested getList /
     * getConfigurationSection calls behave uniformly. Returns an empty section for
     * non-map entries.
     * <p>
     * Values that are themselves Maps are recursively materialised as child sections so
     * that {@code getConfigurationSection("Params")} works even for inline-map values.
     */
    @SuppressWarnings("unchecked")
    private static ConfigurationSection sectionFromListEntry(Object entry) {
        MemoryConfiguration mem = new MemoryConfiguration();
        if (entry instanceof Map<?, ?> map) {
            populateSection(mem, (Map<String, Object>) map);
        }
        return mem;
    }

    @SuppressWarnings("unchecked")
    private static void populateSection(ConfigurationSection section, Map<String, Object> map) {
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            if (value instanceof Map<?, ?> nested) {
                // Materialise nested maps as child sections rather than storing raw Maps,
                // so getConfigurationSection(key) returns a real section (not null).
                ConfigurationSection child = section.createSection(key);
                populateSection(child, (Map<String, Object>) nested);
            } else {
                section.set(key, value);
            }
        }
    }
}
