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
package cz.hashiri.harshlands.disease.symptom.special;

import cz.hashiri.harshlands.data.HLPlayer;
import cz.hashiri.harshlands.data.disease.DataModule;
import cz.hashiri.harshlands.disease.symptom.SymptomContext;
import cz.hashiri.harshlands.disease.symptom.SymptomHandler;
import org.bukkit.entity.Player;

/** Multiplies the player's contraction chance for OTHER diseases while active (Wasting Blight). */
public final class ImmuneSuppressionHandler implements SymptomHandler {

    private final long ttlMs;

    public ImmuneSuppressionHandler(long ttlMs) {
        this.ttlMs = ttlMs;
    }

    @Override
    public void apply(Player player, SymptomContext ctx) {
        double multiplier = ctx.params() != null ? ctx.params().getDouble("Multiplier", 1.5) : 1.5;
        DataModule dm = dataModule(player);
        if (dm != null) dm.setContractionMultiplier(multiplier, System.currentTimeMillis() + ttlMs);
    }

    @Override
    public void clear(Player player, SymptomContext ctx) {
        DataModule dm = dataModule(player);
        if (dm != null) dm.setContractionMultiplier(1.0, 0L);
    }

    private static DataModule dataModule(Player player) {
        HLPlayer hp = HLPlayer.getPlayers().get(player.getUniqueId());
        return hp != null ? hp.getDiseaseDataModule() : null;
    }
}
