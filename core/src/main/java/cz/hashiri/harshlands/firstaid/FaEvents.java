/*
    Copyright (C) 2025  Hashiri_

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
package cz.hashiri.harshlands.firstaid;

import cz.hashiri.harshlands.HLPlugin;
import cz.hashiri.harshlands.data.ModuleEvents;
import cz.hashiri.harshlands.locale.Messages;
import cz.hashiri.harshlands.utils.HLItem;
import cz.hashiri.harshlands.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class FaEvents extends ModuleEvents implements Listener {

    private final HLPlugin plugin;
    private final FaModule module;
    private FaHealService healService;

    public FaEvents(FaModule module, HLPlugin plugin) {
        super(module, plugin);
        this.module = module;
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.healService = buildHealService();
    }

    private FaHealService buildHealService() {
        Map<String, FaHealItem> items = new HashMap<>();
        FileConfiguration cfg = module.getUserConfig().getConfig();
        ConfigurationSection itemsSection = cfg.getConfigurationSection("Items");
        if (itemsSection != null) {
            for (String name : itemsSection.getKeys(false)) {
                FaHealItem parsed = FaHealItem.parse(name, itemsSection.getConfigurationSection(name));
                if (parsed != null) items.put(name, parsed);
            }
        }
        return new FaHealService(new BodyHealthBridge(), items);
    }

    @EventHandler
    public void onHealItemUse(PlayerInteractEvent event) {
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;

        // Avoid double-firing for both hands on the same physical click.
        if (event.getHand() != EquipmentSlot.HAND && event.getHand() != EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        GameMode mode = player.getGameMode();
        if (mode != GameMode.SURVIVAL && mode != GameMode.ADVENTURE) return;

        ItemStack item = event.getItem();
        if (!HLItem.isHLItem(item)) return;
        String name = HLItem.getNameFromItem(item);
        if (name == null) return;
        if (!name.equals("bandage") && !name.equals("splint") && !name.equals("medical_kit")) return;

        // Cancel the event so the item isn't also fed to e.g. a campfire on the same click.
        event.setCancelled(true);

        FaHealService.Outcome outcome = healService.useWithOutcome(player, name);
        switch (outcome.result()) {
            case HEALED -> {
                consumeOne(player, name);
                String soundKey = soundFor(name);
                if (soundKey != null && !soundKey.isEmpty()) {
                    try {
                        player.getWorld().playSound(player.getLocation(), Sound.valueOf(soundKey), 1.0f, 1.0f);
                    } catch (IllegalArgumentException ignored) { /* bad sound name in YAML — silent */ }
                }
                player.sendMessage(Messages.get("firstaid.use.patch_success"));
            }
            case NO_INJURY               -> player.sendMessage(Messages.get("firstaid.use.no_injury"));
            case BODYHEALTH_UNAVAILABLE  -> player.sendMessage(Messages.get("firstaid.use.bodyhealth_unavailable"));
            case UNKNOWN_ITEM            -> { /* unreachable — guarded above */ }
        }
    }

    private @Nullable String soundFor(String itemName) {
        ConfigurationSection sect = module.getUserConfig().getConfig()
                .getConfigurationSection("Items." + itemName);
        return sect == null ? null : sect.getString("Sound", "");
    }

    private void consumeOne(Player player, String itemName) {
        EquipmentSlot hand = Utils.getSlotContainingHLItem(player, itemName);
        if (hand == null) return;
        ItemStack stack = (hand == EquipmentSlot.HAND)
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();
        if (stack == null || stack.getAmount() <= 0) return;
        stack.setAmount(stack.getAmount() - 1);
        if (hand == EquipmentSlot.HAND) player.getInventory().setItemInMainHand(stack);
        else                              player.getInventory().setItemInOffHand(stack);
    }
}
