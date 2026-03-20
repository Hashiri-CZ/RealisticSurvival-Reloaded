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
package cz.hashiri.harshlands.data.cabinfever;

import cz.hashiri.harshlands.data.HLDataModule;
import cz.hashiri.harshlands.data.db.HLDatabase;
import cz.hashiri.harshlands.rsv.HLPlugin;

import java.util.UUID;

public class DataModule implements HLDataModule {

    private final UUID id;
    private final HLDatabase database;

    private long indoorTicks = 0;
    private long outdoorTicks = 0;
    private boolean cabinFeverActive = false;
    private String lastComfortTier = "NONE";
    private volatile boolean dirty = false;

    public DataModule(UUID uuid) {
        this.id = uuid;
        this.database = HLPlugin.getPlugin().getDatabase();
    }

    public long getIndoorTicks() {
        return indoorTicks;
    }

    public void setIndoorTicks(long indoorTicks) {
        this.indoorTicks = indoorTicks;
        this.dirty = true;
    }

    public long getOutdoorTicks() {
        return outdoorTicks;
    }

    public void setOutdoorTicks(long outdoorTicks) {
        this.outdoorTicks = outdoorTicks;
        this.dirty = true;
    }

    public boolean isCabinFeverActive() {
        return cabinFeverActive;
    }

    public void setCabinFeverActive(boolean cabinFeverActive) {
        this.cabinFeverActive = cabinFeverActive;
        this.dirty = true;
    }

    public String getLastComfortTier() {
        return lastComfortTier;
    }

    public void setLastComfortTier(String lastComfortTier) {
        this.lastComfortTier = lastComfortTier;
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void retrieveData() {
        database.loadCabinFeverData(id).thenAccept(optional -> {
            if (optional.isPresent()) {
                HLDatabase.CabinFeverDataRow row = optional.get();
                this.indoorTicks = row.indoorTicks();
                this.outdoorTicks = row.outdoorTicks();
                this.cabinFeverActive = row.cabinFeverActive();
                this.lastComfortTier = row.lastComfortTier();
            } else {
                saveData();
            }
            this.dirty = false;
        });
    }

    @Override
    public void saveData() {
        dirty = false;
        database.saveCabinFeverData(id, new HLDatabase.CabinFeverDataRow(
            indoorTicks, outdoorTicks, cabinFeverActive, lastComfortTier
        ));
    }
}
