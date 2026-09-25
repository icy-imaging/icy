/*
 * Copyright (c) 2010-2026. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package fr.icy.gui.component.icon;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

/**
 * SVG icons pack for Icy GUI components.
 *
 * @author Thomas Musset
 */
public final class IcyIconPack {
    private final IcySVG def, dis, sel, dis_sel;

    private IcySVGIcon.Badge badge = IcySVGIcon.NONE;

    public IcyIconPack(final @NonNull IcySVG def, final @NonNull IcySVG dis, final @NonNull IcySVG sel, final @NonNull IcySVG dis_sel) {
        this.def = def;
        this.dis = dis;
        this.sel = sel;
        this.dis_sel = dis_sel;
    }

    public IcyIconPack(final @NonNull IcySVG def, final @NonNull IcySVG sel) {
        this.def = def;
        this.dis = def;
        this.sel = sel;
        this.dis_sel = sel;
    }

    public IcyIconPack(final @NonNull IcySVG def) {
        this.def = def;
        this.dis = def;
        this.sel = def;
        this.dis_sel = def;
    }

    public IcyIconPack(final @NonNull IcySVG def, final IcySVGIcon.Badge badge) {
        this.def = def;
        this.dis = def;
        this.sel = def;
        this.dis_sel = def;
        this.badge = badge;
    }

    @Contract(pure = true)
    public @NonNull IcySVG getDefaultIcon() {
        return def;
    }

    @Contract(pure = true)
    public @NonNull IcySVG getDisabledIcon() {
        return dis;
    }

    @Contract(pure = true)
    public @NonNull IcySVG getSelectedIcon() {
        return sel;
    }

    @Contract(pure = true)
    public @NonNull IcySVG getDisabledSelectedIcon() {
        return dis_sel;
    }

    @Contract(pure = true)
    public IcySVGIcon.Badge getBadge() {
        return badge;
    }
}
