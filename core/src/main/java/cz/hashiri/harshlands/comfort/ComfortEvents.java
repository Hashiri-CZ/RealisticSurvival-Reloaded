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
import java.util.Map;

public class ComfortEvents implements Listener {

    @Nonnull
    private final ComfortModule module;
    @Nonnull
    private final HLPlugin plugin;
    @Nonnull
    private final ComfortScoreCalculator calculator;
    @Nonnull
    private final FileConfiguration config;

    // Cached effect types for removal on death
    private static final PotionEffectType REGENERATION = Registry.EFFECT.get(NamespacedKey.minecraft("regeneration"));
    private static final PotionEffectType RESISTANCE = Registry.EFFECT.get(NamespacedKey.minecraft("resistance"));
    private static final PotionEffectType SATURATION = Registry.EFFECT.get(NamespacedKey.minecraft("saturation"));

    public ComfortEvents(@Nonnull ComfortModule module, @Nonnull HLPlugin plugin,
                         @Nonnull ComfortScoreCalculator calculator, @Nonnull FileConfiguration config) {
        this.module = module;
        this.plugin = plugin;
        this.calculator = calculator;
        this.config = config;
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

        // Remove existing comfort-related effects before applying new ones
        removeComfortEffects(player);

        // Look up tier config for duration and effects
        ConfigurationSection tierSection = config.getConfigurationSection("Tiers." + tier.name());
        if (tierSection == null) {
            return;
        }

        int durationTicks = tierSection.getInt("Duration", 6000);
        int durationMinutes = durationTicks / 1200; // 1200 ticks = 1 minute

        ConfigurationSection effectsSection = tierSection.getConfigurationSection("Effects");
        if (effectsSection != null) {
            for (String effectKey : effectsSection.getKeys(false)) {
                int amplifier = effectsSection.getInt(effectKey, 0);
                PotionEffectType type = Registry.EFFECT.get(NamespacedKey.minecraft(effectKey.toLowerCase()));
                if (type != null) {
                    player.addPotionEffect(new PotionEffect(type, durationTicks, amplifier, true, true, true));
                }
            }
        }

        // Send buff applied message
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

        removeComfortEffects(player);
    }

    @EventHandler
    public void onQuit(@Nonnull PlayerQuitEvent event) {
        module.clearCacheFor(event.getPlayer().getUniqueId());
    }

    private void removeComfortEffects(@Nonnull Player player) {
        if (REGENERATION != null) {
            player.removePotionEffect(REGENERATION);
        }
        if (RESISTANCE != null) {
            player.removePotionEffect(RESISTANCE);
        }
        if (SATURATION != null) {
            player.removePotionEffect(SATURATION);
        }
    }
}
