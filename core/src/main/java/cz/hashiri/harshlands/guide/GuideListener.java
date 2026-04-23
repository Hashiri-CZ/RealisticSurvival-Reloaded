/*
    Copyright (C) 2026  Hashiri_
    GNU GPL v3.
 */
package cz.hashiri.harshlands.guide;

import cz.hashiri.harshlands.HLPlugin;
import cz.hashiri.harshlands.data.HLPlayer;
import cz.hashiri.harshlands.data.guide.DataModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class GuideListener implements Listener {

    private final HLPlugin plugin;
    private final GuideModule module;

    public GuideListener(HLPlugin plugin, GuideModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Delay a few ticks so HLPlayer + DataModule load first.
        Bukkit.getScheduler().runTaskLater(plugin, () -> tryDeliver(player), 5L);
    }

    private void tryDeliver(Player player) {
        if (!player.isOnline()) return;

        HLPlayer hl = HLPlayer.getPlayers().get(player.getUniqueId());
        if (hl == null) return;
        DataModule data = hl.getGuideDataModule();
        if (data == null || !data.isLoaded()) {
            // DataModule async load still in flight; try again in 20 ticks.
            Bukkit.getScheduler().runTaskLater(plugin, () -> tryDeliver(player), 20L);
            return;
        }

        FileConfiguration cfg = module.getUserConfig().getConfig();
        int currentVersion = module.getGuideVersion();
        int lastSeen = data.getLastSeenVersion();

        if (lastSeen >= currentVersion) return; // already delivered

        boolean isFirstJoin = (lastSeen == 0);
        boolean give = isFirstJoin
            ? cfg.getBoolean("Delivery.GiveItemOnFirstJoin", true)
            : cfg.getBoolean("Delivery.GiveItemOnVersionBump", true);
        boolean open = isFirstJoin
            ? cfg.getBoolean("Delivery.OpenOnFirstJoin", true)
            : cfg.getBoolean("Delivery.OpenOnVersionBump", false);
        boolean dropIfFull = cfg.getBoolean("Delivery.DropIfInventoryFull", true);
        long openDelayTicks = cfg.getLong("Delivery.OpenDelayTicks", 40L);

        ItemStack book = module.buildBook();

        if (give) {
            PlayerInventory inv = player.getInventory();
            if (inv.firstEmpty() == -1) {
                if (dropIfFull) {
                    player.getWorld().dropItemNaturally(player.getLocation(), book);
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&f[&5Harshlands&f] &7» &fThe guide is at your feet — your inventory is full."));
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&f[&5Harshlands&f] &7» &fInventory full — run &e/hl guide give&f when ready."));
                }
            } else {
                inv.addItem(book);
            }
        }

        if (open) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) player.openBook(module.buildBook());
            }, openDelayTicks);
        }

        data.recordDelivery(currentVersion);
    }
}
