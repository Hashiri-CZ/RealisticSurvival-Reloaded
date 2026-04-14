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
package cz.hashiri.harshlands.integrations;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import cz.hashiri.harshlands.HLPlugin;
import org.bukkit.Location;

import javax.annotation.Nonnull;

public class WorldGuard extends ProtectionPlugin {

    public static final String NAME = "WorldGuard";

    public WorldGuard(HLPlugin plugin) {
        super(plugin, NAME);

        if (isIntegrated) {
            // respect "pvp" state flag
            FlagRegistry registry = com.sk89q.worldguard.WorldGuard.getInstance().getFlagRegistry();
            try {
                StateFlag flag = new StateFlag("hl-throwing", true);
                registry.register(flag);
            } catch (FlagConflictException e) {
                Flag<?> existing = registry.get("hl-throwing");
                if (existing instanceof StateFlag) {
                    // use the existing flag if it matches
                }
            }
        }
    }

    @Override
    public boolean otherLoadOptions() {
        return true;
    }

    @Override
    public boolean isPvpAllowed(@Nonnull Location loc) {
        RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));

        return set.testState(null, Flags.PVP);
    }

    @Override
    public boolean isMobGriefingAllowed(@Nonnull Location loc) {
        RegionContainer container = com.sk89q.worldguard.WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(BukkitAdapter.adapt(loc));

        return set.testState(null, Flags.MOB_DAMAGE);
    }
}

