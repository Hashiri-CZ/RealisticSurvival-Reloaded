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
import cz.hashiri.harshlands.disease.model.Disease;
import org.bukkit.entity.Player;

public final class DiagnosisService {
    private final DiseaseRegistry registry;

    public DiagnosisService(DiseaseRegistry registry) {
        this.registry = registry;
    }

    public void diagnose(Player player) {
        HLPlayer hlPlayer = HLPlayer.getPlayers().get(player.getUniqueId());
        DataModule dm = hlPlayer != null ? hlPlayer.getDiseaseDataModule() : null;
        if (dm == null || dm.getActiveInfections().isEmpty()) {
            player.sendMessage("§7You appear to be in good health.");
            return;
        }
        player.sendMessage("§6Diagnosis:");
        for (ActiveInfection inf : dm.getActiveInfections()) {
            Disease d = registry.get(inf.diseaseId());
            String name = d != null ? d.displayName() : inf.diseaseId();
            if (inf.isIncubating()) {
                player.sendMessage("§7- " + name + " §8(incubating)");
            } else {
                player.sendMessage("§c- " + name + " §7(stage " + inf.getStage() + ")");
            }
        }
    }
}
