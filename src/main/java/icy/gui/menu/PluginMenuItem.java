/*
 * Copyright 2010-2023 Institut Pasteur.
 *
 * This file is part of Icy.
 *
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
package icy.gui.menu;

import icy.gui.component.menu.IcyMenuItem;
import icy.gui.util.LookAndFeelUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.system.thread.ThreadUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

/**
 * This class represent a MenuItem which launch a plugin when pressed.
 */
public class PluginMenuItem extends IcyMenuItem implements ActionListener {
    private final PluginDescriptor pluginDescriptor;

    public PluginMenuItem(final PluginDescriptor pluginDescriptor) {
        super(pluginDescriptor.getSimpleClassName());

        this.pluginDescriptor = pluginDescriptor;
        final int size = LookAndFeelUtil.getDefaultIconSizeAsInt();

        // do it in background as loading icon can take sometime
        ThreadUtil.bgRun(() -> {
            final ImageIcon icon = pluginDescriptor.getIcon();
            if (icon != null)
                setIcon(new ImageIcon(icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH)));
        });

        addActionListener(this);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        PluginLauncher.start(pluginDescriptor);
    }
}
