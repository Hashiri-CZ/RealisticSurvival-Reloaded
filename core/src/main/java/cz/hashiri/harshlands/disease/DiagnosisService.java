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
import cz.hashiri.harshlands.locale.Messages;
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
            Messages.of("disease.diagnosis.healthy").send(player);
            return;
        }
        Messages.of("disease.diagnosis.header").send(player);
        for (ActiveInfection inf : dm.getActiveInfections()) {
            Disease d = registry.get(inf.diseaseId());
            String name = d != null ? d.displayName() : inf.diseaseId();
            if (inf.isIncubating()) {
                Messages.of("disease.diagnosis.incubating").with("disease", name).send(player);
            } else {
                Messages.of("disease.diagnosis.active")
                        .with("disease", name)
                        .with("stage", inf.getStage())
                        .send(player);
            }
        }
    }
}
