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
package cz.hashiri.harshlands.disease;

import cz.hashiri.harshlands.data.HLPlayer;
import cz.hashiri.harshlands.data.disease.ActiveInfection;
import cz.hashiri.harshlands.data.disease.DataModule;
import cz.hashiri.harshlands.disease.engine.DoseMath;
import cz.hashiri.harshlands.disease.model.CureMode;
import cz.hashiri.harshlands.disease.model.Disease;
import cz.hashiri.harshlands.utils.HLItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class DiseaseEvents implements Listener {

    public static final String DIAGNOSTIC_ITEM_ID = "medical_kit";

    private final DiseaseModule module;
    private final DiseaseRegistry registry;
    private final DiagnosisService diagnosisService;

    public DiseaseEvents(DiseaseModule module, DiseaseRegistry registry) {
        this.module = module;
        this.registry = registry;
        this.diagnosisService = new DiagnosisService(registry);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
            && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        ItemStack item = event.getItem();
        if (item == null || !HLItem.isHLItem(item)) return;
        if (!module.isEnabled(event.getPlayer().getWorld())) return;

        String id = HLItem.getNameFromItem(item);
        if (id == null) return;
        Player player = event.getPlayer();

        if (DIAGNOSTIC_ITEM_ID.equals(id)) {
            diagnosisService.diagnose(player);
            event.setCancelled(true);
            return;
        }

        Disease cured = registry.byCureItem(id);
        if (cured == null) return;

        HLPlayer hlPlayer = HLPlayer.getPlayers().get(player.getUniqueId());
        DataModule dm = hlPlayer != null ? hlPlayer.getDiseaseDataModule() : null;
        if (dm == null || !dm.hasInfection(cured.id())) {
            // Don't cancel — let normal block interaction through when there's nothing to cure.
            player.sendMessage("§7Nothing happens — you don't have " + cured.displayName() + ".");
            return;
        }
        event.setCancelled(true);
        if (cured.cureMode() == CureMode.REGRESS_ONE_STAGE) {
            applyRegressDose(player, item, dm, cured);
            return;
        }
        // CLEAR (default): one use fully clears the infection.
        module.clearAllSymptoms(player, cured);
        dm.removeInfection(cured.id());
        if (cured.immunityDurationTicks() > 0) {
            // Mirror the natural-cure immunity grant in DiseaseProgressionTask (ticks→ms).
            dm.grantImmunity(cured.id(), System.currentTimeMillis() + cured.immunityDurationTicks() * 50L);
        }
        consumeOne(player, item);
        player.sendMessage("§aYou treated your " + cured.displayName() + ".");
    }

    /** REGRESS_ONE_STAGE cure: each off-cooldown dose drops the infection one stage. */
    private void applyRegressDose(Player player, ItemStack item, DataModule dm, Disease disease) {
        ActiveInfection inf = dm.getInfection(disease.id());
        if (inf == null) return; // guarded by hasInfection() in the caller; stay defensive
        long now = System.currentTimeMillis();
        long cooldownMs = disease.cureDoseCooldownTicks() * 50L;
        DoseMath.DoseOutcome outcome =
            DoseMath.applyDose(inf.getStage(), dm.getLastDoseMs(disease.id()), cooldownMs, now);
        if (outcome.onCooldown()) {
            player.sendMessage("§7The treatment hasn't taken hold yet — wait before the next dose.");
            return;
        }
        module.clearAllSymptoms(player, disease);
        dm.setLastDoseMs(disease.id(), now);
        consumeOne(player, item);
        if (outcome.cured()) {
            dm.removeInfection(disease.id());
            if (disease.immunityDurationTicks() > 0) {
                dm.grantImmunity(disease.id(), now + disease.immunityDurationTicks() * 50L);
            }
            player.sendMessage("§aYou have fully recovered from " + disease.displayName() + ".");
        } else {
            inf.setStage(outcome.newStage());
            inf.setTicksInStage(0L);
            dm.markDirty();
            player.sendMessage("§aThe regimen pushes your " + disease.displayName() + " back a stage.");
        }
    }

    private void consumeOne(Player player, ItemStack item) {
        int amount = item.getAmount();
        if (amount <= 1) {
            player.getInventory().setItemInMainHand(null);
        } else {
            item.setAmount(amount - 1);
        }
    }
}
