package cz.hashiri.harshlands.disease.symptom;

import org.bukkit.configuration.ConfigurationSection;

/** Passed to a handler each tick. params is the symptom's Params section (may be null). */
public record SymptomContext(String diseaseId, int stage, ConfigurationSection params) {}
