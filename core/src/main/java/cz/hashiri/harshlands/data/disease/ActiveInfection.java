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
package cz.hashiri.harshlands.data.disease;

/**
 * Mutable per-player runtime state for one active infection.
 * stage 0 = incubating; stages 1..n are active.
 *
 * Accessor convention: immutable identity fields use record-style accessors
 * ({@link #diseaseId()}, {@link #contractedAt()}); mutable progression fields use
 * JavaBean getters/setters ({@link #getStage()} etc.).
 */
public final class ActiveInfection {
    private final String diseaseId;
    private int stage;
    private long ticksInStage;
    private long incubationLeft;
    private final long contractedAt;

    public ActiveInfection(String diseaseId, int stage, long ticksInStage,
                           long incubationLeft, long contractedAt) {
        this.diseaseId = diseaseId;
        this.stage = stage;
        this.ticksInStage = ticksInStage;
        this.incubationLeft = incubationLeft;
        this.contractedAt = contractedAt;
    }

    public String diseaseId()       { return diseaseId; }
    public int getStage()           { return stage; }
    public long getTicksInStage()   { return ticksInStage; }
    public long getIncubationLeft() { return incubationLeft; }
    public long contractedAt()      { return contractedAt; }

    public void setStage(int stage)                { this.stage = stage; }
    public void setTicksInStage(long ticks)        { this.ticksInStage = ticks; }
    public void setIncubationLeft(long incubation) { this.incubationLeft = incubation; }

    public boolean isIncubating() { return stage == 0; }
}
