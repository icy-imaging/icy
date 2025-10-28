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

package org.bioimageanalysis.icy.gui.component.menu;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginROI;
import org.bioimageanalysis.icy.gui.component.icon.IcySVG;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@Deprecated(since = "3.0.0", forRemoval = true)
public class IcyROIMenuItem extends IcyRadioButtonMenuItem {
    private static final int SIZE = 32;

    @SuppressWarnings("unchecked")
    public IcyROIMenuItem(final @NotNull PluginDescriptor descriptor, final ButtonGroup group) {
        super("Unknown ROI", SVGResource.INDETERMINATE_QUESTION);

        if (!descriptor.isInstanceOf(PluginROI.class))
            throw new IllegalArgumentException("PluginROI must be instance of " + PluginROI.class.getName());

        setText(descriptor.getName());

        // do it in background as loading icon can take sometime
        ThreadUtil.bgRun(() -> {
            final IcySVG icons = descriptor.getSVG();
            setIcons(icons.getIcon(SIZE));

        });

        addActionListener(e -> {
            Icy.getMainInterface().changeROITool((Class<? extends PluginROI>) descriptor.getPluginClass());
        });

        group.add(this);
    }
}
