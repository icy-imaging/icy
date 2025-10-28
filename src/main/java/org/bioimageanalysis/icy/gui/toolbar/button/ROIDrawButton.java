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

package org.bioimageanalysis.icy.gui.toolbar.button;

import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyROIPlugin;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginROI;
import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.gui.component.button.IcyToggleButton;
import org.bioimageanalysis.icy.gui.component.icon.IcySVG;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author Thomas Musset
 */
public class ROIDrawButton extends IcyToggleButton {
    private static final int SIZE = 24;

    private final PluginDescriptor descriptor;

    public ROIDrawButton(final @NotNull PluginDescriptor descriptor) {
        super(SVGResource.INDETERMINATE_QUESTION, SIZE);
        setText(null);

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
            try {
                final IcySVG icons = descriptor.getSVG();
                if (icons != null) {
                    setIcons(icons.getIcon(SIZE, LookAndFeelUtil.ColorType.TOGGLEBUTTON_DEFAULT));
                }
            }
            catch (final Throwable t) {
                //
            }
        });
    }

    @Contract(pure = true)
    public final PluginDescriptor getPluginROI() {
        return descriptor;
    }
}
