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

package org.bioimageanalysis.icy.gui.toolbar.button;

import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyROIPlugin;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginROI;
import org.bioimageanalysis.icy.gui.component.button.IcyToggleButton;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.gui.component.icon.SVGIconPack;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * @author Thomas Musset
 */
public class ROIDrawButton extends IcyToggleButton {
    private static final int SIZE = 32;

    private final PluginDescriptor descriptor;

    public ROIDrawButton(final @NotNull PluginDescriptor descriptor) {
        super(SVGIcon.INDETERMINATE_QUESTION);

        this.descriptor = descriptor;

        if (!this.descriptor.isInstanceOf(PluginROI.class))
            throw new IllegalArgumentException("PluginROI must be instance of " + PluginROI.class.getName());

        final String type;
        final IcyROIPlugin annotation = this.descriptor.getPluginClass().getAnnotation(IcyROIPlugin.class);
        switch (annotation.type()) {
            case ROI2D -> type = "2D ";
            case ROI3D -> type = "3D ";
            default -> type = "";
        }

        setToolTipText(type + this.descriptor.getName());

        // do it in background as loading icon can take sometime
        ThreadUtil.bgRun(() -> {
            // Try with SVG at first
            final SVGIcon svgIcon = descriptor.getSVGIcon();
            if (svgIcon != null)
                setSVGIconPack(new SVGIconPack(svgIcon));
            else {
                // Try with classic PNG/JPG icon
                final ImageIcon imgIcon = descriptor.getIcon();

                if (imgIcon != null) {
                    final ImageIcon icon = new ImageIcon(imgIcon.getImage().getScaledInstance(SIZE, SIZE, Image.SCALE_SMOOTH));
                    setIcons(icon);
                }
            }
        });
    }

    @Contract(pure = true)
    public final PluginDescriptor getPluginROI() {
        return descriptor;
    }
}
