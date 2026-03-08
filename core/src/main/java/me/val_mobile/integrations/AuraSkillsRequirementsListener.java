/*
    Copyright (C) 2026  Val_Mobile

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
package me.val_mobile.integrations;

import me.val_mobile.rsv.RSVPlugin;
import me.val_mobile.utils.RSVItem;
import me.val_mobile.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class AuraSkillsRequirementsListener implements Listener {

    private final RSVPlugin plugin;

    public AuraSkillsRequirementsListener(@Nonnull RSVPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String identifier = toIdentifier(player.getInventory().getItemInMainHand());

        RequirementFailure failure = firstMissingRequirement(player, identifier, "using");
        if (failure != null) {
            event.setCancelled(true);
            player.sendMessage(buildFailureMessage("use", identifier, failure));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        String identifier = toIdentifier(player.getInventory().getItemInMainHand());
        RequirementFailure failure = firstMissingRequirement(player, identifier, "using");
        if (failure != null) {
            event.setCancelled(true);
            player.sendMessage(buildFailureMessage("use", identifier, failure));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        if (!(event.getView().getPlayer() instanceof Player player)) {
            return;
        }

        Recipe recipe = event.getRecipe();
        if (recipe == null) {
            return;
        }

        String identifier = toIdentifier(recipe.getResult());
        RequirementFailure failure = firstMissingRequirement(player, identifier, "crafting");
        if (failure != null) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        String identifier = toIdentifier(event.getCurrentItem());
        RequirementFailure failure = firstMissingRequirement(player, identifier, "crafting");
        if (failure != null) {
            event.setCancelled(true);
            player.updateInventory();
            player.sendMessage(buildFailureMessage("craft", identifier, failure));
        }
    }

    @Nullable
    private RequirementFailure firstMissingRequirement(@Nonnull Player player, @Nullable String identifier, @Nonnull String action) {
        if (identifier == null || identifier.isEmpty()) {
            return null;
        }

        Map<String, Integer> requirements = getRequirements(identifier, action);
        for (Map.Entry<String, Integer> entry : requirements.entrySet()) {
            String skill = entry.getKey();
            int requiredLevel = entry.getValue();

            if (!Utils.hasAuraSkillLevel(player, skill, requiredLevel)) {
                int current = Utils.getAuraSkillLevel(player, skill);
                return new RequirementFailure(skill, requiredLevel, current);
            }
        }

        return null;
    }

    @Nonnull
    private Map<String, Integer> getRequirements(@Nonnull String identifier, @Nonnull String action) {
        FileConfiguration config = plugin.getAuraSkillsRequirementsConfig();
        Map<String, Integer> requirements = new LinkedHashMap<>();

        String normalizedAction = action.toLowerCase(Locale.ROOT);

        // New format:
        // identifier:
        //   crafting:
        //     FARMING: 5
        //   using:
        //     FARMING: 0
        ConfigurationSection section = config.getConfigurationSection(identifier + "." + normalizedAction);

        // Legacy format fallback:
        // Crafting:
        //   identifier:
        //     FARMING: 5
        if (section == null) {
            String legacyRoot = normalizedAction.equals("crafting") ? "Crafting" : "Using";
            section = config.getConfigurationSection(legacyRoot + "." + identifier);
        }

        if (section == null) {
            return requirements;
        }

        for (String skill : section.getKeys(false)) {
            requirements.put(skill, section.getInt(skill));
        }

        return requirements;
    }

    @Nullable
    private String toIdentifier(@Nullable ItemStack item) {
        if (!Utils.isItemReal(item)) {
            return null;
        }

        if (RSVItem.isRSVItem(item)) {
            return RSVItem.getNameFromItem(item);
        }

        return item.getType().name().toLowerCase(Locale.ROOT);
    }

    @Nonnull
    private String buildFailureMessage(@Nonnull String action, @Nonnull String identifier, @Nonnull RequirementFailure failure) {
        return ChatColor.RED + "You need " + failure.skill + " level " + failure.requiredLevel
                + " to " + action + " " + identifier + ". Current level: " + failure.currentLevel + ".";
    }

    private record RequirementFailure(String skill, int requiredLevel, int currentLevel) { }
}
