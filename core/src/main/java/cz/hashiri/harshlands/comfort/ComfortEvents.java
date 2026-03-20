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

import cz.hashiri.harshlands.rsv.HLPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public class ComfortEvents implements Listener {

    @Nonnull
    private final ComfortModule module;
    @Nonnull
    private final HLPlugin plugin;
    @Nonnull
    private final ComfortScoreCalculator calculator;
    @Nonnull
    private final FileConfiguration config;

    // All effect types used by any comfort tier — built from config for accurate death cleanup
    private final Set<PotionEffectType> comfortEffectTypes = new HashSet<>();

    public ComfortEvents(@Nonnull ComfortModule module, @Nonnull HLPlugin plugin,
                         @Nonnull ComfortScoreCalculator calculator, @Nonnull FileConfiguration config) {
        this.module = module;
        this.plugin = plugin;
        this.calculator = calculator;
        this.config = config;
        buildComfortEffectTypes();
    }

    private void buildComfortEffectTypes() {
        ConfigurationSection tiersSection = config.getConfigurationSection("Tiers");
        if (tiersSection == null) {
            return;
        }
        for (String tierKey : tiersSection.getKeys(false)) {
            ConfigurationSection tierSec = tiersSection.getConfigurationSection(tierKey);
            if (tierSec == null) continue;
            ConfigurationSection effectsSec = tierSec.getConfigurationSection("Effects");
            if (effectsSec == null) continue;
            for (String effectKey : effectsSec.getKeys(false)) {
                PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(effectKey.toLowerCase()));
                if (type != null) {
                    comfortEffectTypes.add(type);
                } else {
                    plugin.getLogger().warning("[Comfort] Unknown potion effect in tier config: " + effectKey);
                }
            }
        }
    }

    @EventHandler
    public void onBedEnter(@Nonnull PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }

        Player player = event.getPlayer();
        if (!module.isEnabled(player.getWorld())) {
            return;
        }

        ComfortScoreCalculator.ComfortResult result = calculator.calculate(event.getBed().getLocation());

        if (result.getScore() == 0) {
            String noComfortMsg = config.getString("Messages.NoComfort", "§7No comfort nearby.");
            player.sendMessage(noComfortMsg);
            return;
        }

        ComfortTier tier = result.getTier();

        ConfigurationSection tierSection = config.getConfigurationSection("Tiers." + tier.name());
        if (tierSection == null) {
            return;
        }

        int durationTicks = tierSection.getInt("Duration", 6000);
        int durationMinutes = durationTicks / 1200;

        ConfigurationSection effectsSection = tierSection.getConfigurationSection("Effects");
        if (effectsSection != null) {
            for (String effectKey : effectsSection.getKeys(false)) {
                int amplifier = effectsSection.getInt(effectKey, 0);
                PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(effectKey.toLowerCase()));
                if (type != null) {
                    // addPotionEffect replaces existing effects of the same type automatically
                    player.addPotionEffect(new PotionEffect(type, durationTicks, amplifier, true, true, true));
                }
            }
        }

        // Populate PAPI cache from this sync context (avoids unsafe async calculation)
        module.updateCache(player, result);

        String msg = config.getString("Messages.BuffApplied",
                "§aComfort: §f{score} §7({tier}) §a- Resting buff for {minutes}min");
        msg = msg.replace("{score}", String.valueOf(result.getScore()));
        msg = msg.replace("{tier}", tier.getDisplayName());
        msg = msg.replace("{minutes}", String.valueOf(durationMinutes));
        player.sendMessage(msg);
    }

    @EventHandler
    public void onDeath(@Nonnull PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (!module.isEnabled(player.getWorld())) {
            return;
        }

        // Remove only effects that the comfort system can grant (config-driven)
        for (PotionEffectType type : comfortEffectTypes) {
            player.removePotionEffect(type);
        }
    }

    @EventHandler
    public void onQuit(@Nonnull PlayerQuitEvent event) {
        module.clearCacheFor(event.getPlayer().getUniqueId());
    }
}
