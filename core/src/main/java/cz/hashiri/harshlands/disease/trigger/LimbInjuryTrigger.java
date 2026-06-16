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

import cz.hashiri.harshlands.bodyhealth.BodyPartState;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Festering Wound: per-check contraction risk while a BodyHealth limb is at or beyond an
 * "injured" state. Limb state is read through an injected {@link LimbStateReader}
 * (PlaceholderAPI in production), so the trigger contributes no risk when BodyHealth / PAPI
 * is absent — the reader returns {@code FULL}.
 */
public final class LimbInjuryTrigger implements DiseaseTrigger {

    private final String diseaseId;
    private final Set<BodyPartState> injuredStates;
    private final double chancePerCheck;
    private final LimbStateReader reader;

    public LimbInjuryTrigger(String diseaseId, Set<BodyPartState> injuredStates,
                             double chancePerCheck, LimbStateReader reader) {
        this.diseaseId = diseaseId;
        this.injuredStates = injuredStates;
        this.chancePerCheck = chancePerCheck;
        this.reader = reader;
    }

    @Override public String diseaseId() { return diseaseId; }

    /** Pure: is the worst limb state considered "injured" by this config? */
    public static boolean injured(BodyPartState worst, Set<BodyPartState> injuredStates) {
        return worst != null && injuredStates.contains(worst);
    }

    /** Pure: the most severe (highest-ordinal) state in the collection; FULL if empty/null. */
    public static BodyPartState worstSeverity(Collection<BodyPartState> states) {
        BodyPartState worst = BodyPartState.FULL;
        if (states == null) return worst;
        for (BodyPartState s : states) {
            if (s != null && s.ordinal() > worst.ordinal()) worst = s;
        }
        return worst;
    }

    /** Parse a config list of state names into a set; unknown names are skipped. */
    public static Set<BodyPartState> parseStates(List<String> names) {
        Set<BodyPartState> set = EnumSet.noneOf(BodyPartState.class);
        if (names == null) return set;
        for (String n : names) {
            try {
                set.add(BodyPartState.valueOf(n.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // skip unknown state name
            }
        }
        return set;
    }

    @Override
    public double chance(Player player) {
        BodyPartState worst = reader.worst(player);
        return injured(worst, injuredStates) ? chancePerCheck : 0.0;
    }
}
