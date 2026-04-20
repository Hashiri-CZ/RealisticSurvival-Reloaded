/*
    Copyright (C) 2026  Hashiri_
    GNU GPL v3.
 */
package cz.hashiri.harshlands.hints;

import cz.hashiri.harshlands.HLPlugin;
import cz.hashiri.harshlands.data.HLPlayer;
import cz.hashiri.harshlands.locale.Messages;
import cz.hashiri.harshlands.utils.HLItem;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class HintSender {

    private final HLPlugin plugin;
    private final HintsModule module;

    public HintSender(HLPlugin plugin, HintsModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    public void send(Player player, HintKey key) {
        if (player == null || !player.isOnline()) return;

        HLPlayer hlPlayer = HLPlayer.getPlayers().get(player.getUniqueId());
        if (hlPlayer == null) return;
        cz.hashiri.harshlands.data.hints.DataModule data = hlPlayer.getHintsDataModule();
        if (data == null || !data.isLoaded()) return;

        FileConfiguration cfg = module.getUserConfig().getConfig();
        if (!cfg.getBoolean("Hints." + key.name() + ".Enabled", true)) return;

        if (!key.isRepeating() && data.hasSeen(key)) return;

        long cooldownMs = key.isRepeating()
            ? cfg.getLong("Hints." + key.name() + ".CooldownSeconds", key.defaultCooldownMs() / 1000L) * 1000L
            : 0L;
        if (key.isRepeating() && data.isOnCooldown(key, cooldownMs)) return;

        String prefix = Messages.get("hints.Prefix");
        String body = Messages.get("hints.Hints." + key.translationKey(), resolvePlaceholders(key));
        player.sendMessage(prefix + body);

        String soundName = cfg.getString("Hints." + key.name() + ".Sound", key.defaultSound().name());
        Sound sound = parseSound(soundName, key.defaultSound());
        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);

        if (key.isRepeating()) {
            data.stampCooldown(key);
        } else {
            data.markSeen(key);
        }
    }

    private Map<String, Object> resolvePlaceholders(HintKey key) {
        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("item_flint_hatchet", displayName("flint_hatchet", "Flint Hatchet"));
        placeholders.put("item_saw", displayName("iron_saw", "Saw"));
        return placeholders;
    }

    private String displayName(String hlItemId, String fallback) {
        ItemStack stack = HLItem.getItem(hlItemId);
        if (stack == null) return ChatColor.WHITE + fallback;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return ChatColor.WHITE + fallback;
        return meta.getDisplayName();
    }

    private Sound parseSound(String name, Sound fallback) {
        if (name == null || name.isEmpty()) return fallback;
        try {
            return Sound.valueOf(name);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[Hints] Unknown Sound in hints.yml: " + name + " — using " + fallback.name());
            return fallback;
        }
    }
}
