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

package icy.gui.component.menu;

import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.resource.icon.SVGIcon;
import icy.resource.icon.SVGIconPack;
import icy.system.thread.ThreadUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Create a menu item that can launch a plugin on click
 *
 * @author Thomas Musset
 */
public class IcyPluginMenuItem extends IcyMenuItem implements ActionListener {
    private final PluginDescriptor descriptor;
    private static final int SIZE = 32;

    public IcyPluginMenuItem(final PluginDescriptor descriptor, final String text) {
        super(text, new SVGIconPack(SVGIcon.INDETERMINATE_QUESTION), SIZE);
        this.descriptor = descriptor;


        // do it in background as loading icon can take sometime
        ThreadUtil.bgRun(() -> {
            final ImageIcon imgIcon = descriptor.getIcon();
            if (imgIcon != null) {
                final ImageIcon icon = new ImageIcon(imgIcon.getImage().getScaledInstance(SIZE, SIZE, Image.SCALE_SMOOTH));
                setIcons(icon);
            }
        });

        addActionListener(this);
    }

    public IcyPluginMenuItem(final PluginDescriptor descriptor) {
        this(descriptor, descriptor.getName());
    }

    @Override
    @SuppressWarnings("resource")
    public void actionPerformed(final ActionEvent e) {
        PluginLauncher.start(descriptor);
    }
}
