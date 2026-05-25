/*
    Copyright (C) 2025  Hashiri_

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
package cz.hashiri.harshlands.firstaid;

import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Reflection wrapper around bodyhealth.api.BodyHealthAPI. Harshlands does not
 * compile against BodyHealth (it's an optional softdep), so all access goes
 * through this class. When BodyHealth is not installed, every method short-circuits.
 */
public class BodyHealthBridge {

    private final boolean available;
    private final @Nullable Object apiInstance;
    private final @Nullable Method mHealPart;
    private final @Nullable Method mGetHealth;
    private final @Nullable Method mGetMaxHealth;
    private final @Nullable Method mIsSystemEnabled;
    private final @Nullable Class<?> partEnum;

    public BodyHealthBridge() {
        Object inst = null;
        Method heal = null, get = null, max = null, enabled = null;
        Class<?> part = null;
        boolean ok = false;
        try {
            Class<?> apiClass = Class.forName("bodyhealth.api.BodyHealthAPI");
            part = Class.forName("bodyhealth.core.BodyPart");
            inst = apiClass.getMethod("getInstance").invoke(null);
            heal = apiClass.getMethod("healPlayer", Player.class, part, double.class);
            get  = apiClass.getMethod("getHealth", Player.class, part);
            max  = apiClass.getMethod("getMaxPartHealth", Player.class, part);
            enabled = apiClass.getMethod("isSystemEnabled", Player.class);
            ok = true;
        } catch (ReflectiveOperationException ignored) {
            // BodyHealth not on the classpath, or API shape changed. Bridge stays unavailable.
        }
        this.apiInstance      = inst;
        this.mHealPart        = heal;
        this.mGetHealth       = get;
        this.mGetMaxHealth    = max;
        this.mIsSystemEnabled = enabled;
        this.partEnum         = part;
        this.available        = ok;
    }

    public boolean isAvailable() {
        return available;
    }

    /** Returns false when unavailable, when the part name is invalid, or when the underlying call throws. */
    public boolean heal(@Nullable Player player, String partName, double amount) {
        if (!available || player == null) return false;
        Object partValue = partValueOf(partName);
        if (partValue == null) return false;
        if (!isSystemEnabledFor(player)) return false;
        try {
            mHealPart.invoke(apiInstance, player, partValue, amount);
            return true;
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    /** Returns the part's current HP percent (0-100), or -1 if unavailable / invalid. */
    public double getHealth(@Nullable Player player, String partName) {
        if (!available || player == null) return -1;
        Object partValue = partValueOf(partName);
        if (partValue == null) return -1;
        try {
            return (double) mGetHealth.invoke(apiInstance, player, partValue);
        } catch (ReflectiveOperationException ex) {
            return -1;
        }
    }

    /** Returns the part's max HP, or -1 if unavailable / invalid. */
    public double getMaxHealth(@Nullable Player player, String partName) {
        if (!available || player == null) return -1;
        Object partValue = partValueOf(partName);
        if (partValue == null) return -1;
        try {
            return (double) mGetMaxHealth.invoke(apiInstance, player, partValue);
        } catch (ReflectiveOperationException ex) {
            return -1;
        }
    }

    private boolean isSystemEnabledFor(Player player) {
        if (mIsSystemEnabled == null) return true; // older BodyHealth without the gate — assume enabled
        try {
            return (boolean) mIsSystemEnabled.invoke(apiInstance, player);
        } catch (ReflectiveOperationException ex) {
            return false;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private @Nullable Object partValueOf(String name) {
        if (partEnum == null || name == null) return null;
        try {
            return Enum.valueOf((Class<Enum>) partEnum, name);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
