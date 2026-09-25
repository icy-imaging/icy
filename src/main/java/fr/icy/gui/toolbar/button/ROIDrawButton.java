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

package fr.icy.gui.toolbar.button;

import fr.icy.extension.plugin.PluginDescriptor;
import fr.icy.extension.plugin.annotation_.IcyROIPlugin;
import fr.icy.extension.plugin.interface_.PluginROI;
import fr.icy.gui.LookAndFeelUtil;
import fr.icy.gui.component.button.IcyToggleButton;
import fr.icy.gui.component.icon.IcyIconPack;
import fr.icy.gui.component.icon.IcySVG;
import fr.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

/**
 * A specialized toggle button designed for ROI (Region of Interest) drawing tools,
 * built on top of the {@link IcyToggleButton} class. This button is linked to a
 * specific {@link PluginDescriptor}, ensuring that the plugin is a subclass of
 * {@link PluginROI}.
 * <p>
 * This button dynamically sets its tooltip and icon based on the type of ROI (2D or 3D)
 * specified by the plugin and loads the associated SVG icon in the background
 * for smoother UI performance.
 *
 * @author Thomas Musset
 */
public class ROIDrawButton extends IcyToggleButton {
    private static final int SIZE = 24;

    private final PluginDescriptor descriptor;

    /**
     * Constructs an instance of the {@code ROIDrawButton}, a specialized toggle button
     * for ROI (Region of Interest) drawing tools. The button is initialized with the
     * provided {@link PluginDescriptor} and verifies that the associated plugin is an
     * instance of {@link PluginROI}. It dynamically configures its tooltip and asynchronously
     * loads its icon based on the type of ROI specified by the plugin's annotation.
     *
     * @param descriptor the {@link PluginDescriptor} associated with the ROI drawing tool.
     *                   This descriptor provides metadata, annotations, and icon resources
     *                   for the associated plugin. Must be non-null and linked to an instance
     *                   of {@link PluginROI}.
     * @throws IllegalArgumentException if the provided {@code descriptor} is not an instance
     *                                  of {@link PluginROI}.
     */
    public ROIDrawButton(final @NonNull PluginDescriptor descriptor) throws IllegalArgumentException {
        super(IcySVG.INDETERMINATE_QUESTION, SIZE);
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
                final IcySVG svg = descriptor.getSVG();
                setSVGIconPack(new IcyIconPack(svg));
                setIcons(svg.getIcon(SIZE, LookAndFeelUtil.ColorType.TOGGLEBUTTON_DEFAULT));
            }
            catch (final Throwable t) {
                //
            }
        });
    }

    /**
     * Retrieves the {@link PluginDescriptor} associated with this ROI (Region of Interest) drawing button.
     * The descriptor provides metadata and resources for the linked ROI plugin.
     *
     * @return the {@link PluginDescriptor} associated with this ROI drawing button.
     */
    @Contract(pure = true)
    public final PluginDescriptor getPluginROI() {
        return descriptor;
    }
}
