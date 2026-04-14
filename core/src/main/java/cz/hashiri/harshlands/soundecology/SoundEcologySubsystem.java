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
package cz.hashiri.harshlands.soundecology;

import cz.hashiri.harshlands.data.HLModule;
import cz.hashiri.harshlands.fear.FearModule;
import cz.hashiri.harshlands.HLPlugin;
import cz.hashiri.harshlands.tan.TanModule;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitTask;

public class SoundEcologySubsystem {

    private final FearModule fearModule;
    private final HLPlugin plugin;
    private NoiseManager noiseManager;
    private BukkitTask evaluationTask;
    private BukkitTask shiverTask;

    public SoundEcologySubsystem(FearModule fearModule, HLPlugin plugin) {
        this.fearModule = fearModule;
        this.plugin = plugin;
    }

    public void initialize(ConfigurationSection config) {
        // Resolve optional TAN cross-module reference
        HLModule tanMod = HLModule.getModule(TanModule.NAME);
        TanModule tanModule = (tanMod instanceof TanModule tm && tm.isGloballyEnabled()) ? tm : null;

        noiseManager = new NoiseManager(config, fearModule);

        // Register event listeners
        plugin.getServer().getPluginManager().registerEvents(
                new SoundEcologyEvents(fearModule, config, noiseManager), plugin);

        // Schedule noise evaluation task
        long evalInterval = config.getLong("EvaluationIntervalTicks", 10);
        evaluationTask = Bukkit.getScheduler().runTaskTimer(plugin,
                new NoiseEvaluationTask(noiseManager, config), evalInterval, evalInterval);

        // Schedule cold shiver task if TAN is available
        if (tanModule != null && config.getBoolean("Integration.ColdShivering.Enabled", true)) {
            shiverTask = Bukkit.getScheduler().runTaskTimer(plugin,
                    new ColdShiverTask(noiseManager, config, fearModule), 100L, 100L);
        }
    }

    public void shutdown() {
        if (evaluationTask != null) {
            evaluationTask.cancel();
            evaluationTask = null;
        }
        if (shiverTask != null) {
            shiverTask.cancel();
            shiverTask = null;
        }
        if (noiseManager != null) {
            noiseManager.clear();
        }
    }

    public NoiseManager getNoiseManager() {
        return noiseManager;
    }
}
