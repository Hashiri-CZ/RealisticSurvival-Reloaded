/*
    Copyright (C) 2026  Hashiri_

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cz.hashiri.harshlands.disease;

import cz.hashiri.harshlands.HLPlugin;
import cz.hashiri.harshlands.data.HLConfig;
import cz.hashiri.harshlands.data.HLModule;
import cz.hashiri.harshlands.data.HLPlayer;
import cz.hashiri.harshlands.data.ModuleItems;
import cz.hashiri.harshlands.data.ModuleRecipes;
import cz.hashiri.harshlands.data.disease.DataModule;
import cz.hashiri.harshlands.disease.mitigation.Mitigation;
import cz.hashiri.harshlands.disease.mitigation.TanWarmDryMitigation;
import cz.hashiri.harshlands.disease.model.Disease;
import cz.hashiri.harshlands.disease.model.DiseaseStage;
import cz.hashiri.harshlands.disease.model.SymptomSpec;
import cz.hashiri.harshlands.disease.symptom.SymptomContext;
import cz.hashiri.harshlands.disease.symptom.SymptomHandler;
import cz.hashiri.harshlands.disease.symptom.SymptomHandlers;
import cz.hashiri.harshlands.disease.symptom.builtin.DamageOverTimeHandler;
import cz.hashiri.harshlands.disease.symptom.builtin.PlaySoundHandler;
import cz.hashiri.harshlands.disease.symptom.builtin.PotionEffectHandler;
import cz.hashiri.harshlands.disease.trigger.ColdExposureTrigger;
import cz.hashiri.harshlands.disease.trigger.DiseaseTrigger;
import cz.hashiri.harshlands.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DiseaseModule extends HLModule {

    public static final String NAME = "Disease";

    private final HLPlugin plugin;
    private final DiseaseRegistry registry = new DiseaseRegistry();
    private final SymptomHandlers handlers = new SymptomHandlers();
    private final List<DiseaseTrigger> triggers = new ArrayList<>();
    private final Map<String, Mitigation> mitigations = new HashMap<>();

    private BukkitTask progressionTask;
    private BukkitTask autosaveTask;
    private DiseaseEvents events;

    public DiseaseModule(HLPlugin plugin) {
        super(NAME, plugin, Map.of(), Map.of());
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        setUserConfig(new HLConfig(plugin, "Settings/disease.yml"));
        setItemConfig(new HLConfig(plugin, "Items/disease/items.yml"));
        setRecipeConfig(new HLConfig(plugin, "Items/disease/recipes.yml"));
        setModuleItems(new ModuleItems(this));
        setModuleRecipes(new ModuleRecipes(this, plugin));

        FileConfiguration cfg = getUserConfig().getConfig();
        if (cfg.getBoolean("Initialize.Enabled")) {
            Utils.logModuleInit("disease", NAME);
        }

        getModuleItems().initialize();
        getModuleRecipes().initialize();

        registry.load(cfg.getConfigurationSection("Diseases"));

        handlers.register("PotionEffect", new PotionEffectHandler());
        handlers.register("PlaySound", new PlaySoundHandler());
        handlers.register("DamageOverTime", new DamageOverTimeHandler());

        ConfigurationSection diseases = cfg.getConfigurationSection("Diseases");
        if (diseases != null) {
            for (String id : diseases.getKeys(false)) {
                String type = diseases.getString(id + ".Mitigation.Type", "");
                if ("TAN_WARM_DRY".equals(type)) {
                    double atLeast = diseases.getDouble(id + ".Mitigation.TemperatureAtLeast", 12.0);
                    // NOTE: mitigations are keyed by type, not disease id. Diseases sharing a
                    // mitigation type currently share one threshold (last-loaded wins). Revisit
                    // when a second disease uses TAN_WARM_DRY with a different threshold.
                    mitigations.put(type, new TanWarmDryMitigation(atLeast));
                }
            }
        }

        ConfigurationSection cold = cfg.getConfigurationSection("Triggers.ColdExposure");
        if (cold != null) {
            triggers.add(new ColdExposureTrigger(
                cold.getString("Disease", "respiratory_infection"),
                cold.getDouble("TemperatureBelow", 4.0),
                cold.getBoolean("RequireWet", true),
                cold.getDouble("ChancePerCheck", 0.02)));
        }

        long interval = cfg.getLong("CheckIntervalTicks", 60L);
        DiseaseProgressionTask task = new DiseaseProgressionTask(this, registry, handlers, interval);
        progressionTask = Bukkit.getScheduler().runTaskTimer(plugin, task, interval, interval);

        autosaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (HLPlayer hp : new ArrayList<>(HLPlayer.getPlayers().values())) {
                DataModule dm = hp.getDiseaseDataModule();
                if (dm != null && dm.isDirty()) dm.saveData();
            }
        }, 6000L, 6000L);

        events = new DiseaseEvents(this, registry);
        Bukkit.getPluginManager().registerEvents(events, plugin);

        plugin.getDebugManager().registerProvider(new cz.hashiri.harshlands.debug.DebugProvider() {
            @Override public String getModuleName() { return NAME; }
            @Override public java.util.Collection<String> getSubsystems() {
                return java.util.List.of("Progression", "Triggers", "Symptoms", "Diagnosis");
            }
        });
    }

    @Override
    public void shutdown() {
        if (getUserConfig() != null && getUserConfig().getConfig().getBoolean("Shutdown.Enabled")) {
            Utils.logModuleShutdown("disease", NAME);
        }
        if (progressionTask != null) { progressionTask.cancel(); progressionTask = null; }
        if (autosaveTask != null) { autosaveTask.cancel(); autosaveTask = null; }
        if (events != null) { HandlerList.unregisterAll(events); events = null; }
        triggers.clear();
        mitigations.clear();
    }

    public List<DiseaseTrigger> getTriggers() { return triggers; }

    public DiseaseRegistry getRegistry() { return registry; }

    public boolean mitigationActive(String mitigationType, Player player) {
        if (mitigationType == null || mitigationType.isEmpty()) return false;
        Mitigation m = mitigations.get(mitigationType);
        return m != null && m.isActive(player);
    }

    /** Clears the symptom effects of a disease's current stage (used on item cure). */
    public void clearAllSymptoms(Player player, Disease disease) {
        HLPlayer hp = HLPlayer.getPlayers().get(player.getUniqueId());
        DataModule dm = hp != null ? hp.getDiseaseDataModule() : null;
        int stage = (dm != null && dm.getInfection(disease.id()) != null)
            ? dm.getInfection(disease.id()).getStage() : disease.maxStage();
        DiseaseStage def = disease.stage(Math.max(1, stage));
        if (def == null) return;
        for (SymptomSpec spec : def.symptoms()) {
            SymptomHandler h = handlers.get(spec.handlerName());
            if (h != null) {
                h.clear(player, new SymptomContext(disease.id(), stage, spec.params()));
            }
        }
    }
}
