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

package org.bioimageanalysis.icy.gui.toolbar;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.extension.ExtensionLoader;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyROIPlugin;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginROI;
import org.bioimageanalysis.icy.gui.component.button.IcyToggleButton;
import org.bioimageanalysis.icy.gui.toolbar.button.ROIDrawButton;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Set;

/**
 * @author Thomas Musset
 */
public final class ROIBar extends IcyToolbar implements ExtensionLoader.ExtensionLoaderListener {
    private @Nullable ROIDrawButton selected = null;
    private final ButtonGroup roiGroup = new ButtonGroup();
    private final ActionListener openClose;

    private final ArrayList<PluginDescriptor> roi2d = new ArrayList<>();
    private final ArrayList<PluginDescriptor> roi3d = new ArrayList<>();
    private final ArrayList<PluginDescriptor> roiTools = new ArrayList<>();

    private final Comparator<PluginDescriptor> comparator = (o1, o2) -> {
        final IcyROIPlugin ao1 = o1.getPluginClass().getAnnotation(IcyROIPlugin.class);
        final IcyROIPlugin ao2 = o2.getPluginClass().getAnnotation(IcyROIPlugin.class);

        if (ao1 == null || ao2 == null)
            return 0;

        return Integer.compare(ao1.nbPoints().ordinal(), ao2.nbPoints().ordinal());
    };

    /**
     * Creates a new tool bar; orientation defaults to <code>VERTICAL</code>.
     */
    @SuppressWarnings("unchecked")
    public ROIBar() {
        super(VERTICAL, false);

        openClose = e -> {
            if (e.getSource() instanceof IcyToggleButton) {
                if (selected == null || !selected.equals(e.getSource())) {
                    selected = (ROIDrawButton) e.getSource();
                    if (selected != null)
                        Icy.getMainInterface().changeROITool((Class<? extends PluginROI>) selected.getPluginROI().getPluginClass());
                }
                else if (selected.equals(e.getSource())) {
                    roiGroup.clearSelection();
                    selected = null;
                    Icy.getMainInterface().changeROITool(null);
                }
            }
        };

        if (!ExtensionLoader.isLoading())
            reloadPlugins();

        ExtensionLoader.addListener(this);
    }

    private void reloadPlugins() {
        this.invalidate();

        ThreadUtil.invokeLater(() -> {
            roiGroup.clearSelection();
            Enumeration<AbstractButton> buttons = roiGroup.getElements();
            while (buttons.hasMoreElements())
                roiGroup.remove(buttons.nextElement());
            ROIBar.this.removeAll();

            roi2d.clear();
            roi3d.clear();
            roiTools.clear();

            final Set<PluginDescriptor> plugins = ExtensionLoader.getPlugins(PluginROI.class);
            for (final PluginDescriptor plugin : plugins) {
                if (!(plugin.isAnnotated(IcyROIPlugin.class))) {
                    IcyLogger.warn(this.getClass(), "Plugin ROI [" + plugin.getClassName() + "] doesn't have 'IcyROIPlugin' annotation.");
                    continue;
                }

                final IcyROIPlugin icyROIPlugin = plugin.getPluginClass().getAnnotation(IcyROIPlugin.class);
                if (icyROIPlugin == null)
                    continue;
                switch (icyROIPlugin.type()) {
                    case TOOL -> roiTools.add(plugin);
                    case ROI2D -> roi2d.add(plugin);
                    case ROI3D -> roi3d.add(plugin);
                }
            }

            if (!roi2d.isEmpty()) {
                roi2d.sort(comparator);

                ROIBar.this.add(new JLabel(" 2D"));

                for (final PluginDescriptor pl : roi2d) {
                    final ROIDrawButton drawButton = new ROIDrawButton(pl);
                    roiGroup.add(drawButton);
                    ROIBar.this.add(drawButton);
                }

                ROIBar.this.addSeparator();
            }

            if (!roi3d.isEmpty()) {
                roi3d.sort(comparator);

                ROIBar.this.add(new JLabel(" 3D"));

                for (final PluginDescriptor pl : roi3d) {
                    final ROIDrawButton drawButton = new ROIDrawButton(pl);
                    roiGroup.add(drawButton);
                    ROIBar.this.add(drawButton);
                }

                ROIBar.this.addSeparator();
            }

            if (!roiTools.isEmpty()) {
                for (final PluginDescriptor pl : roiTools) {
                    final ROIDrawButton drawButton = new ROIDrawButton(pl);
                    roiGroup.add(drawButton);
                    ROIBar.this.add(drawButton);
                }
            }

            buttons = roiGroup.getElements();
            while (buttons.hasMoreElements()) {
                final AbstractButton b = buttons.nextElement();
                if (b instanceof IcyToggleButton)
                    b.addActionListener(openClose);
            }

            ROIBar.this.revalidate();
        });
    }

    @Override
    public void extensionLoaderChanged(final ExtensionLoader.ExtensionLoaderEvent e) {
        reloadPlugins();
    }
}
