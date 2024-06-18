/*
 * Copyright (c) 2010-2024. Institut Pasteur.
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

package org.bioimageanalysis.icy.gui.component.icon;

import org.jetbrains.annotations.NotNull;

/**
 * SVG icons pack for Icy GUI components.
 *
 * @author Thomas Musset
 */
public final class SVGIconPack {
    private final SVGIcon def, dis, sel, dis_sel;

    public SVGIconPack(final @NotNull SVGIcon def, final @NotNull SVGIcon dis, final @NotNull SVGIcon sel, final @NotNull SVGIcon dis_sel) {
        this.def = def;
        this.dis = dis;
        this.sel = sel;
        this.dis_sel = dis_sel;
    }

    public SVGIconPack(final @NotNull SVGIcon def, final @NotNull SVGIcon sel) {
        this.def = def;
        this.dis = def;
        this.sel = sel;
        this.dis_sel = sel;
    }

    public SVGIconPack(final @NotNull SVGIcon def) {
        this.def = def;
        this.dis = def;
        this.sel = def;
        this.dis_sel = def;
    }

    public @NotNull SVGIcon getDefaultIcon() {
        return def;
    }

    public @NotNull SVGIcon getDisabledIcon() {
        return dis;
    }

    public @NotNull SVGIcon getSelectedtIcon() {
        return sel;
    }

    public @NotNull SVGIcon getDisabledSelectedIcon() {
        return dis_sel;
    }
}
