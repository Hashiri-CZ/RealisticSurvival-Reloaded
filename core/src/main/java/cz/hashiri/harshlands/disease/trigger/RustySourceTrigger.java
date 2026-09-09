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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tetanus: contraction from being hurt by a "rusty/jagged" source. A damage event counts when
 * its {@code DamageCause} name is in the config cause list (e.g. {@code CONTACT} for cactus /
 * sweet-berry / dripstone) OR the damaging block / damager's held item is in the config material
 * list. Like {@link InfectedItemTrigger}, exposure is an event: the damage callback marks a
 * one-shot pending exposure (with a TTL) that {@link #chance(Player)} consumes on the next
 * progression check — keeping the immunity check + immune-suppression multiplier in
 * {@code DiseaseProgressionTask.totalChance} in the loop.
 */
public final class RustySourceTrigger implements DiseaseTrigger, Listener, PlayerStateCleanup {

    private final String diseaseId;
    private final Set<String> rustyCauses;
    private final Set<Material> rustyMaterials;
    private final double chancePerCheck;
    private final long ttlMs;

    // player -> wall-clock ms at which the pending exposure expires.
    private final Map<UUID, Long> pending = new ConcurrentHashMap<>();

    public RustySourceTrigger(String diseaseId, Set<String> rustyCauses, Set<Material> rustyMaterials,
                              double chancePerCheck, long ttlMs) {
        this.diseaseId = diseaseId;
        this.rustyCauses = normalizeCauses(rustyCauses);
        this.rustyMaterials = rustyMaterials;
        this.chancePerCheck = chancePerCheck;
        this.ttlMs = ttlMs;
    }

    /** Normalize config cause names to upper case so they match {@code DamageCause.name()}. */
    private static Set<String> normalizeCauses(Set<String> causes) {
        Set<String> out = new HashSet<>();
        if (causes != null) {
            for (String c : causes) {
                if (c != null) out.add(c.trim().toUpperCase(Locale.ROOT));
            }
        }
        return out;
    }

    @Override public String diseaseId() { return diseaseId; }

    /** Pure: does this damage event count as a rusty/jagged source? */
    public static boolean isRustySource(String causeName, Material sourceMaterial,
                                        Set<String> rustyCauses, Set<Material> rustyMaterials) {
        if (causeName != null && rustyCauses.contains(causeName)) return true;
        return sourceMaterial != null && rustyMaterials.contains(sourceMaterial);
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

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        String cause = event.getCause() != null ? event.getCause().name() : null;
        Material source = sourceMaterial(event);
        if (isRustySource(cause, source, rustyCauses, rustyMaterials)) {
            markPending(player.getUniqueId(), System.currentTimeMillis() + ttlMs);
        }
    }

    /** Best-effort source material: the damaging block, or the damager's held item. */
    private static Material sourceMaterial(EntityDamageEvent event) {
        if (event instanceof EntityDamageByBlockEvent be) {
            Block damager = be.getDamager();
            if (damager != null) return damager.getType();
        }
        if (event instanceof EntityDamageByEntityEvent ee && ee.getDamager() instanceof LivingEntity le) {
            EntityEquipment eq = le.getEquipment();
            if (eq != null) {
                ItemStack weapon = eq.getItemInMainHand();
                if (weapon != null && weapon.getType() != Material.AIR) return weapon.getType();
            }
        }
        return null;
    }

    /** Drop this player's pending exposure (quit cleanup — see {@link PlayerStateCleanup}). */
    @Override
    public void clearPlayer(UUID uuid) {
        pending.remove(uuid);
    }
}
