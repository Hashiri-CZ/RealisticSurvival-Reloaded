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
package cz.hashiri.harshlands.disease.trigger;

import cz.hashiri.harshlands.disease.PlayerStateCleanup;
import cz.hashiri.harshlands.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contraction from eating or using "infected" items. Septicemia uses a config material list
 * (spoiled food); Wasting Blight uses items NBT-tagged {@code hldiseased}. Infected-item
 * exposure is an event, not a per-tick state: the Bukkit callbacks {@link #markPending mark}
 * a one-shot pending exposure (with a TTL that self-heals a missed read), and
 * {@link #chance(Player)} consumes it on the next progression check — which keeps the
 * immunity check and immune-suppression multiplier in
 * {@code DiseaseProgressionTask.contractionChance} in the loop.
 */
public final class InfectedItemTrigger implements DiseaseTrigger, Listener, PlayerStateCleanup {

    public static final String DISEASED_NBT_KEY = "hldiseased";

    private final String diseaseId;
    private final Set<Material> infectedMaterials;
    private final boolean requireNbtTag;
    private final double chancePerCheck;
    private final long ttlMs;

    // player -> wall-clock ms at which the pending exposure expires.
    private final Map<UUID, Long> pending = new ConcurrentHashMap<>();

    public InfectedItemTrigger(String diseaseId, Set<Material> infectedMaterials,
                               boolean requireNbtTag, double chancePerCheck, long ttlMs) {
        this.diseaseId = diseaseId;
        this.infectedMaterials = infectedMaterials;
        this.requireNbtTag = requireNbtTag;
        this.chancePerCheck = chancePerCheck;
        this.ttlMs = ttlMs;
    }

    @Override public String diseaseId() { return diseaseId; }

    /**
     * True when this trigger only fires on items carrying the {@code hldiseased} tag.
     * {@link TaintedItemSource} uses this to work out which diseases are tag-borne, and so
     * which infected players spread contamination through their drops.
     */
    public boolean requiresNbtTag() { return requireNbtTag; }

    /** Pure: is this item infectious for this trigger's mode? */
    public static boolean infectious(Material consumed, boolean hasDiseasedTag,
                                     Set<Material> infectedMaterials, boolean requireNbtTag) {
        if (requireNbtTag) return hasDiseasedTag;
        return consumed != null && infectedMaterials.contains(consumed);
    }

    /** Record a one-shot pending exposure for the player, expiring at {@code expiryMs}. */
    public void markPending(UUID player, long expiryMs) {
        pending.put(player, expiryMs);
    }

    /** True (and clears the entry) iff a non-expired pending exposure exists at {@code nowMs}. */
    public boolean takePending(UUID player, long nowMs) {
        Long expiry = pending.remove(player);
        return expiry != null && nowMs < expiry;
    }

    @Override
    public double chance(Player player) {
        return takePending(player.getUniqueId(), System.currentTimeMillis()) ? chancePerCheck : 0.0;
    }

    /**
     * MONITOR so the exposure is only recorded once the item was actually eaten. At NORMAL this
     * ran before {@code BlockEatingHandler} (Tetanus "locked jaw") cancelled the consume, so a
     * blocked bite still infected the player — the same "caught it without eating" defect this
     * trigger's interact gating fixes. With MONITOR + ignoreCancelled, a cancelled consume is
     * skipped entirely. Observation only: nothing here mutates the event.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        considerItem(event.getPlayer(), event.getItem());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        // Interact/use is only a Wasting-Blight-style vector (handling a contaminated item);
        // material-list triggers such as Septicemia infect only by actually eating the item,
        // via onConsume. Merely right-clicking while holding spoiled food must not roll a check.
        if (!requireNbtTag) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
            && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        // PlayerInteractEvent fires once per hand; only count the main hand so a single
        // player action marks at most one exposure (mirrors DiseaseEvents.onRightClick).
        if (event.getHand() != EquipmentSlot.HAND) return;
        considerItem(event.getPlayer(), event.getItem());
    }

    private void considerItem(Player player, ItemStack item) {
        if (item == null) return;
        boolean tagged = Utils.hasNbtTag(item, DISEASED_NBT_KEY);
        if (infectious(item.getType(), tagged, infectedMaterials, requireNbtTag)) {
            markPending(player.getUniqueId(), System.currentTimeMillis() + ttlMs);
        }
    }

    /** Drop this player's pending exposure (quit cleanup — see {@link PlayerStateCleanup}). */
    @Override
    public void clearPlayer(UUID uuid) {
        pending.remove(uuid);
    }
}
