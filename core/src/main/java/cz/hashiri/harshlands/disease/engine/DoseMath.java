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
package cz.hashiri.harshlands.disease.engine;

/** Pure decisions for multi-dose (REGRESS_ONE_STAGE) cures. No Bukkit, no state. */
public final class DoseMath {

    private DoseMath() {}

    /**
     * @param onCooldown true if the dose was rejected because the cooldown has not elapsed
     * @param newStage   the stage after this dose (unchanged if on cooldown; 0 if cured)
     * @param cured      true if the infection should be removed entirely
     */
    public record DoseOutcome(boolean onCooldown, int newStage, boolean cured) {}

    /**
     * Decide the result of applying one cure dose.
     *
     * @param currentStage current infection stage (0 = incubating, 1..n active)
     * @param lastDoseMs   wall-clock ms of the last effective dose (0 = none yet)
     * @param cooldownMs   minimum ms between effective doses (0 = no cooldown)
     * @param nowMs        current wall-clock ms
     */
    public static DoseOutcome applyDose(int currentStage, long lastDoseMs, long cooldownMs, long nowMs) {
        if (cooldownMs > 0L && nowMs < lastDoseMs + cooldownMs) {
            return new DoseOutcome(true, currentStage, false);
        }
        int newStage = currentStage - 1;
        if (newStage <= 0) {
            return new DoseOutcome(false, 0, true);
        }
        return new DoseOutcome(false, newStage, false);
    }
}
