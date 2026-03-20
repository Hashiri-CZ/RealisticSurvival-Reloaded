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
package cz.hashiri.harshlands.soundecology;

import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.UUID;

public class NoiseEvent {

    private final Location location;
    private final double baseRadius;
    private final double effectiveRadius;
    private final long expirationTick;
    private final UUID sourcePlayer;
    private final String noiseType;

    public NoiseEvent(Location location, double baseRadius, double effectiveRadius,
                      long expirationTick, @Nullable UUID sourcePlayer, String noiseType) {
        this.location = location;
        this.baseRadius = baseRadius;
        this.effectiveRadius = effectiveRadius;
        this.expirationTick = expirationTick;
        this.sourcePlayer = sourcePlayer;
        this.noiseType = noiseType;
    }

    public Location getLocation() {
        return location;
    }

    public double getBaseRadius() {
        return baseRadius;
    }

    public double getEffectiveRadius() {
        return effectiveRadius;
    }

    public double getEffectiveRadiusSquared() {
        return effectiveRadius * effectiveRadius;
    }

    public long getExpirationTick() {
        return expirationTick;
    }

    @Nullable
    public UUID getSourcePlayer() {
        return sourcePlayer;
    }

    public String getNoiseType() {
        return noiseType;
    }

    public boolean isExpired(long currentTick) {
        return currentTick >= expirationTick;
    }
}
