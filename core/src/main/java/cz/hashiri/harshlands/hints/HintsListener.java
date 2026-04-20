/*
    Copyright (C) 2026  Hashiri_
    GNU GPL v3.
 */
package cz.hashiri.harshlands.hints;

import cz.hashiri.harshlands.misc.PlayerItemAcquireEvent;
import cz.hashiri.harshlands.utils.HLItem;
import cz.hashiri.harshlands.utils.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class HintsListener implements Listener {

    private final HintsModule module;
    private final HintSender sender;

    public HintsListener(HintsModule module, HintSender sender) {
        this.module = module;
        this.sender = sender;
    }

    private boolean shouldRun(Player player) {
        if (player == null) return false;
        if (!module.isEnabled(player.getWorld())) return false;
        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return false;
        return true;
    }

    // 1. FIST_ON_LOG — MONITOR so NtpEvents (HIGHEST) has already decided drops
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (!shouldRun(player)) return;

        Block block = event.getBlock();
        Material mat = block.getType();
        if (!Tag.LOGS.isTagged(mat)) return;
        if (event.isDropItems()) return; // drops allowed → not a fist-on-log scenario
        if (Utils.isHoldingAxe(player)) return; // safety — NtpEvents should have allowed drops

        sender.send(player, HintKey.FIST_ON_LOG);
    }

    // 2, 4, 5. Acquire-based triggers
    @EventHandler
    public void onAcquire(PlayerItemAcquireEvent event) {
        if (!acceptableCause(event.getCause())) return;

        Player player = event.getPlayer();
        if (!shouldRun(player)) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() == Material.AIR) return;

        // 2. First flint shard
        if (HLItem.isHLItem(item) && "flint_shard".equals(HLItem.getNameFromItem(item))) {
            sender.send(player, HintKey.FIRST_FLINT_SHARD);
        }

        Material mat = item.getType();

        // 4. First log
        if (Tag.LOGS.isTagged(mat)) {
            sender.send(player, HintKey.FIRST_LOG);
        }

        // 5. First plank + first stick together
        if (Tag.PLANKS.isTagged(mat) || mat == Material.STICK) {
            if (playerHasBothPlankAndStick(player, item)) {
                sender.send(player, HintKey.FIRST_PLANK_AND_STICK);
            }
        }
    }

    // 3, 6. Craft-based triggers
    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!shouldRun(player)) return;

        ItemStack result = event.getRecipe().getResult();
        if (!HLItem.isHLItem(result)) return;
        String hlName = HLItem.getNameFromItem(result);
        if (hlName == null) return;

        if ("flint_hatchet".equals(hlName)) {
            sender.send(player, HintKey.FIRST_FLINT_HATCHET_CRAFTED);
        } else if (hlName.contains("saw")) {
            sender.send(player, HintKey.FIRST_SAW_CRAFTED);
        }
    }

    private boolean acceptableCause(cz.hashiri.harshlands.misc.EntityItemAcquireEvent.ItemAcquireCause cause) {
        return switch (cause) {
            case ITEM_PICKUP, INVENTORY_CLICK, DRAG_CLICK, PLAYER_JOIN, PLAYER_RESPAWN, GIVE_COMMAND -> true;
            default -> false;
        };
    }

    /**
     * Returns true iff the player now has at least one plank AND at least one stick.
     * The {@code justAcquired} stack is pre-seeded because for {@code ITEM_PICKUP}
     * events the item is not yet in the inventory view; for other acquire causes
     * it's already in {@code getContents()} and the pre-seed is a harmless OR.
     */
    private boolean playerHasBothPlankAndStick(Player player, ItemStack justAcquired) {
        boolean hasPlank = false;
        boolean hasStick = false;

        Material acquiredMat = justAcquired.getType();
        if (Tag.PLANKS.isTagged(acquiredMat)) hasPlank = true;
        if (acquiredMat == Material.STICK) hasStick = true;

        PlayerInventory inv = player.getInventory();
        for (ItemStack stack : inv.getContents()) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            Material m = stack.getType();
            if (Tag.PLANKS.isTagged(m)) hasPlank = true;
            if (m == Material.STICK) hasStick = true;
            if (hasPlank && hasStick) return true;
        }
        return hasPlank && hasStick;
    }
}
