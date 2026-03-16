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
package cz.hashiri.harshlands.dynamicsurroundings;

import org.bukkit.Material;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Handles item equip, swing, bow pull/loose, shield use, and book pageflip sounds.
 */
public class ItemSoundHandler {

    private final FileConfiguration config;
    private final Random random = new Random();

    private final long equipCooldownMs;
    private final long swingCooldownMs;
    private final long bowPullCooldownMs;
    private final long pageflopCooldownMs;
    private final float volume;

    public ItemSoundHandler(DynamicSurroundingsModule module, FileConfiguration config) {
        this.config = config;
        this.equipCooldownMs   = config.getLong("ItemSounds.EquipCooldownMs", 250);
        this.swingCooldownMs   = config.getLong("ItemSounds.SwingCooldownMs", 150);
        this.bowPullCooldownMs = config.getLong("ItemSounds.BowPullCooldownMs", 500);
        this.pageflopCooldownMs = config.getLong("ItemSounds.PageflipCooldownMs", 300);
        this.volume = (float) config.getDouble("ItemSounds.Volume", 0.8);
    }

    // -----------------------------------------------------------------------
    // Public handlers
    // -----------------------------------------------------------------------

    /** Called when player changes hotbar slot. */
    public void handleEquip(Player player, int newSlot) {
        ItemStack item = player.getInventory().getItem(newSlot);
        if (item == null || item.getType() == Material.AIR) return;

        String soundKey = getEquipSound(item.getType());
        if (soundKey == null) return;

        if (checkCooldown(player, "equip." + soundKey, equipCooldownMs)) {
            playSound(player, soundKey, volume, randomPitch());
        }
    }

    /** Called on main-hand arm swing animation. */
    public void handleSwing(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) return;

        String soundKey = getSwingSound(item.getType());
        if (soundKey == null) return;

        if (checkCooldown(player, "swing." + soundKey, swingCooldownMs)) {
            playSound(player, soundKey, volume, randomPitch());
        }
    }

    /** Called when EntityShootBowEvent fires for a player. */
    public void handleBowLoose(Player player) {
        playSound(player, "bow.loose", volume, randomPitch());
    }

    /** Called on PlayerInteractEvent RIGHT_CLICK for bow pull, shield use, pageflip. */
    public void handleInteract(Player player, PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        Material mat = item.getType();

        if ((mat == Material.BOW || mat == Material.CROSSBOW) && event.getHand() == EquipmentSlot.HAND) {
            if (checkCooldown(player, "bow_pull", bowPullCooldownMs)) {
                playSound(player, "bow.pull", volume, randomPitch());
            }
            return;
        }

        if (mat == Material.SHIELD) {
            if (checkCooldown(player, "shield_use", 300)) {
                playSound(player, "shield.use", volume, randomPitch());
            }
            return;
        }

        if (isBook(mat) && event.getHand() == EquipmentSlot.HAND) {
            if (checkCooldown(player, "pageflip", pageflopCooldownMs)) {
                playSound(player, "pageflip", volume, randomPitch());
            }
        }
    }

    // -----------------------------------------------------------------------
    // Classification helpers
    // -----------------------------------------------------------------------

    private String getEquipSound(Material mat) {
        String name = mat.name();
        if (name.endsWith("_SWORD")) return "sword.equip";
        if (name.endsWith("_AXE") || mat == Material.MACE) return "blunt.equip";
        if (mat == Material.BOW || mat == Material.CROSSBOW) return "bow.equip";
        if (name.endsWith("_PICKAXE") || name.endsWith("_HOE") || name.endsWith("_SHOVEL")
                || mat == Material.SHEARS || mat == Material.FLINT_AND_STEEL) return "tool.equip";
        if (mat == Material.SHIELD) return "shield.equip";
        if (mat == Material.POTION || mat == Material.SPLASH_POTION || mat == Material.LINGERING_POTION)
            return "potion.equip";
        if (mat.isItem()) return "utility.equip";
        return null;
    }

    private String getSwingSound(Material mat) {
        String name = mat.name();
        if (name.endsWith("_SWORD")) return "sword.swing";
        if (name.endsWith("_AXE") || mat == Material.MACE) return "blunt.swing";
        if (name.endsWith("_PICKAXE") || name.endsWith("_HOE") || name.endsWith("_SHOVEL")
                || mat == Material.SHEARS || mat == Material.FLINT_AND_STEEL) return "tool.swing";
        return null;
    }

    private boolean isBook(Material mat) {
        return mat == Material.BOOK || mat == Material.WRITTEN_BOOK
                || mat == Material.ENCHANTED_BOOK || mat == Material.WRITABLE_BOOK;
    }

    // -----------------------------------------------------------------------
    // Utility
    // -----------------------------------------------------------------------

    /** Returns true and updates cooldown if the cooldown has elapsed; false otherwise. */
    private boolean checkCooldown(Player player, String key, long cooldownMs) {
        DSPlayerState state = DSPlayerState.getOrCreate(player.getUniqueId());
        long now = System.currentTimeMillis();
        Long last = state.itemSoundCooldowns.get(key);
        if (last != null && now - last < cooldownMs) return false;
        state.itemSoundCooldowns.put(key, now);
        return true;
    }

    private void playSound(Player player, String key, float vol, float pitch) {
        player.playSound(player.getLocation(), "harshlands:" + key, SoundCategory.PLAYERS, vol, pitch);
    }

    private float randomPitch() {
        return 1.0f + (random.nextFloat() - 0.5f) * 0.1f;
    }
}
