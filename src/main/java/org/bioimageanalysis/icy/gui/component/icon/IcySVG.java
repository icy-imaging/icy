/*
 * Copyright (c) 2010-2025. Institut Pasteur.
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

import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @author Thomas Musset
 *
 * Wrapper for SVG images / icons.
 */
public final class IcySVG {
    private final byte @NotNull [] data;
    /**
     * Only for Icon
     */
    private final boolean monochrome;

    public IcySVG(@NotNull final SVGResource svgIcon) {
        data = svgIcon.toByteArray().clone();
        monochrome = !svgIcon.isColored();
    }

    public IcySVG(final byte @NotNull [] data, final boolean monochrome) {
        this.data = data.clone();
        this.monochrome = monochrome;
    }

    public IcySVG(final byte @NotNull [] data) {
        this(data, false);
    }

    // SVG Icon
    @NotNull
    public IcySVGIcon getIcon(final int width, final int height) {
        return IcySVGIcon.fromBytes(data, width, height);
    }

    @NotNull
    public IcySVGIcon getIcon(final int size) {
        return getIcon(size, size);
    }

    @NotNull
    public IcySVGIcon getIcon(final int width, final int height, final @NotNull Color color) {
        if (monochrome)
            return IcySVGIcon.fromBytes(data, width, height, color);
        else
            return getIcon(width, height);
    }

    @NotNull
    public IcySVGIcon getIcon(final int size, final @NotNull Color color) {
        return getIcon(size, size, color);
    }

    @NotNull
    public IcySVGIcon getIcon(final int width, final int height, final @NotNull LookAndFeelUtil.ColorType colorType) {
        if (monochrome)
            return IcySVGIcon.fromBytes(data, width, height, colorType);
        else
            return getIcon(width, height);
    }

    @NotNull
    public IcySVGIcon getIcon(final int size, final @NotNull LookAndFeelUtil.ColorType colorType) {
        return getIcon(size, size, colorType);
    }

    // SVG Image
    @NotNull
    public IcySVGImage getImage(final int width, final int height) {
        return IcySVGImage.fromBytes(data, width, height);
    }

    @NotNull
    public IcySVGImage getImage(final int size) {
        return getImage(size, size);
    }

    @NotNull
    public IcySVGImage getImage(final int width, final int height, final @NotNull Color color) {
        return IcySVGImage.fromBytes(data, width, height, color);
    }

    @NotNull
    public IcySVGImage getImage(final int size, final @NotNull Color color) {
        return getImage(size, size, color);
    }
}
