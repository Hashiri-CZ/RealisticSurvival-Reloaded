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
package cz.hashiri.harshlands.comfort;

import cz.hashiri.harshlands.data.HLConfig;
import cz.hashiri.harshlands.data.HLModule;
import cz.hashiri.harshlands.rsv.HLPlugin;
import cz.hashiri.harshlands.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ComfortModule extends HLModule {

    public static final String NAME = "Comfort";

    @Nonnull
    private final HLPlugin plugin;

    @Nullable
    private ComfortScoreCalculator calculator;

    @Nullable
    private ComfortEvents events;

    private final Map<UUID, CachedComfortResult> papiCache = new ConcurrentHashMap<>();

    public ComfortModule(@Nonnull HLPlugin plugin) {
        super(NAME, plugin, Map.of(), Map.of());
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        setUserConfig(new HLConfig(plugin, "comfort.yml"));
        FileConfiguration config = getUserConfig().getConfig();

        if (config.getBoolean("Initialize.Enabled")) {
            Utils.logModuleLifecycle("Initializing", NAME);
        }

        calculator = new ComfortScoreCalculator(config);
        events = new ComfortEvents(this, plugin, calculator, config);
        Bukkit.getPluginManager().registerEvents(events, plugin);
    }

    @Override
    public void shutdown() {
        FileConfiguration config = getUserConfig() != null ? getUserConfig().getConfig() : null;
        if (config != null && config.getBoolean("Shutdown.Enabled")) {
            Utils.logModuleLifecycle("Shutting down", NAME);
        }

        if (events != null) {
            HandlerList.unregisterAll(events);
            events = null;
        }

        papiCache.clear();
        calculator = null;
    }

    /**
     * Returns the score calculator for use by commands and PAPI.
     */
    @Nullable
    public ComfortScoreCalculator getCalculator() {
        return calculator;
    }

    /**
     * Returns a cached comfort result for PAPI performance.
     * If the cache is stale or missing, a fresh calculation is performed.
     *
     * @param player       the player whose location to scan
     * @param cacheSeconds maximum cache age in seconds
     * @return the comfort result, or null if calculator is unavailable
     */
    @Nullable
    public ComfortScoreCalculator.ComfortResult getCachedResult(@Nonnull Player player, int cacheSeconds) {
        if (calculator == null) {
            return null;
        }

        UUID uuid = player.getUniqueId();
        CachedComfortResult cached = papiCache.get(uuid);
        long now = System.currentTimeMillis();

        if (cached != null && (now - cached.timestampMillis) < (cacheSeconds * 1000L)) {
            return cached.result;
        }

        ComfortScoreCalculator.ComfortResult result = calculator.calculate(player.getLocation());
        papiCache.put(uuid, new CachedComfortResult(result, now));
        return result;
    }

    /**
     * Removes the PAPI cache entry for a player (called on quit).
     */
    public void clearCacheFor(@Nonnull UUID uuid) {
        papiCache.remove(uuid);
    }

    /**
     * Simple holder for a cached comfort result with a timestamp.
     */
    private static class CachedComfortResult {
        final ComfortScoreCalculator.ComfortResult result;
        final long timestampMillis;

        CachedComfortResult(@Nonnull ComfortScoreCalculator.ComfortResult result, long timestampMillis) {
            this.result = result;
            this.timestampMillis = timestampMillis;
        }
    }
}
