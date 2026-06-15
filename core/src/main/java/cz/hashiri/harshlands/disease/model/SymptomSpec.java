package cz.hashiri.harshlands.disease.model;

import org.bukkit.configuration.ConfigurationSection;

/** One symptom: which handler to run and its params (a raw config section). */
public record SymptomSpec(String handlerName, ConfigurationSection params) {}
