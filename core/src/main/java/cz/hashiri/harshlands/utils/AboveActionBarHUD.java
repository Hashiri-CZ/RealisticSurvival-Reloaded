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
package cz.hashiri.harshlands.utils;

import net.kyori.adventure.text.Component;
import java.util.EnumMap;
import java.util.Map;

/**
 * Manages a center-aligned strip of low-macro warning icons rendered just above
 * the action bar via {@link BossbarHUD}. Visible icons are packed left-to-right
 * with no gaps; hidden icons take no space. The group is always centered around
 * {@code centerX}.
 *
 * <p>Y positioning is handled entirely by the resource pack's font ascent +
 * {@code rendertype_text.vsh}'s Bucket C override (bottom-anchored). Java only
 * computes X via {@link BossbarHUD}'s negative-space shifting.</p>
 *
 * <p>The held-food nutrient preview is no longer rendered through this HUD;
 * it is sent directly to the action bar by {@code NutritionPreviewController}.</p>
 */
public final class AboveActionBarHUD {

    private static final String GROUP_ID = "aboveactionbar_group";

    public enum Slot {
        WETNESS (""),
        PROTEIN (""),
        CARBS   (""),
        FAT     ("");

        final String codepoint;
        Slot(String codepoint) { this.codepoint = codepoint; }
    }

    private final BossbarHUD hud;
    private final int centerX;
    private final int iconWidth;
    private final int iconSpacing;
    private final Map<Slot, Boolean> visibility = new EnumMap<>(Slot.class);

    public AboveActionBarHUD(BossbarHUD hud, int centerX, int iconWidth, int iconSpacing) {
        this.hud = hud;
        this.centerX = centerX;
        this.iconWidth = iconWidth;
        this.iconSpacing = iconSpacing;
        for (Slot s : Slot.values()) visibility.put(s, false);
    }

    /** Show or hide a slot. Recalculates all X positions immediately. */
    public void setVisible(Slot slot, boolean visible) {
        if (visibility.get(slot) == visible) return;
        visibility.put(slot, visible);
        relayout();
    }

    private void relayout() {
        Slot[] all = Slot.values();

        java.util.List<Slot> visible = new java.util.ArrayList<>();
        for (Slot s : all) {
            if (visibility.get(s)) visible.add(s);
        }

        int visibleCount = visible.size();
        if (visibleCount == 0) {
            hud.removeElement(GROUP_ID);
            return;
        }

        // Build one content component: icon_1 + shift(spacing) + icon_2 + ... + icon_N.
        Component content = Component.empty();
        for (int i = 0; i < visibleCount; i++) {
            content = content.append(Component.text(visible.get(i).codepoint));
            if (i < visibleCount - 1 && iconSpacing != 0) {
                content = content.append(BossbarHUD.NegativeSpaceHelper.shift(iconSpacing));
            }
        }

        int totalWidth = visibleCount * iconWidth + Math.max(0, visibleCount - 1) * iconSpacing;
        int startX = centerX - totalWidth / 2;
        hud.setElement(GROUP_ID, startX, content, totalWidth);
    }
}
