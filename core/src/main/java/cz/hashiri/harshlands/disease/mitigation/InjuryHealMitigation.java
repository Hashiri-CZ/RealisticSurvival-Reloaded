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
package cz.hashiri.harshlands.disease.mitigation;

import cz.hashiri.harshlands.bodyhealth.BodyPartState;
import cz.hashiri.harshlands.disease.trigger.LimbInjuryTrigger;
import cz.hashiri.harshlands.disease.trigger.LimbStateReader;
import org.bukkit.entity.Player;

import java.util.Set;

/**
 * Festering Wound mitigation: active (holds / regresses the disease) while no limb is in an
 * "injured" state — i.e. the wound has been treated and healed. Shares the limb-state read and
 * the {@code injured} predicate with {@link LimbInjuryTrigger} so trigger and mitigation can
 * never disagree about what counts as injured.
 */
public final class InjuryHealMitigation implements Mitigation {

    private final Set<BodyPartState> injuredStates;
    private final LimbStateReader reader;

    public InjuryHealMitigation(Set<BodyPartState> injuredStates, LimbStateReader reader) {
        this.injuredStates = injuredStates;
        this.reader = reader;
    }

    @Override
    public boolean isActive(Player player) {
        BodyPartState worst = reader.worst(player);
        return !LimbInjuryTrigger.injured(worst, injuredStates);
    }
}
