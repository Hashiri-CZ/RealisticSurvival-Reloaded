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

import cz.hashiri.harshlands.data.HLPlayer;
import cz.hashiri.harshlands.data.disease.DataModule;
import org.bukkit.entity.Player;

/**
 * Sybok: per-check contraction risk once a player's cumulative-contraction counter (the count of
 * diseases ever contracted, persisted in the disease DataModule) reaches a threshold — modelling a
 * tumour from long-term repeated illness. Reads the counter from the player's disease DataModule;
 * contributes no risk (count 0) when that data is absent. Sybok's post-cure immunity (config) prevents
 * instant re-contraction from a still-high counter.
 */
public final class SybokAccumulationTrigger implements DiseaseTrigger {

    private final String diseaseId;
    private final long threshold;
    private final double chancePerCheck;

    public SybokAccumulationTrigger(String diseaseId, long threshold, double chancePerCheck) {
        this.diseaseId = diseaseId;
        this.threshold = threshold;
        this.chancePerCheck = chancePerCheck;
    }

    @Override public String diseaseId() { return diseaseId; }

    /** Pure: has the cumulative-contraction count reached the Sybok threshold? */
    public static boolean accumulated(long count, long threshold) {
        return count >= threshold;
    }

    /** Pure: this check's contraction chance for a given cumulative-contraction count. */
    public double chanceFor(long count) {
        return accumulated(count, threshold) ? chancePerCheck : 0.0;
    }

    @Override
    public double chance(Player player) {
        return chanceFor(readCount(player));
    }

    private static long readCount(Player player) {
        HLPlayer hp = HLPlayer.getPlayers().get(player.getUniqueId());
        if (hp == null) return 0L;
        DataModule dm = hp.getDiseaseDataModule();
        return dm != null ? dm.getContractionCount() : 0L;
    }
}
