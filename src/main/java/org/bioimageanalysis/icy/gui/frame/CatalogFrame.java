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

package org.bioimageanalysis.icy.gui.frame;

import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class CatalogFrame extends IcyFrame {
    private final JTabbedPane tabbedPane;
    private final JScrollPane descriptionPane;

    public CatalogFrame() {
        super("Extensions", false, true, false, false);

        setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));

        tabbedPane = new JTabbedPane();
        tabbedPane.add("Online", new JScrollPane());
        tabbedPane.add("Installed", new JScrollPane());
        tabbedPane.setSelectedIndex(0);
        add(tabbedPane);

        descriptionPane = new JScrollPane(new JEditorPane("text/html", ""));
        add(descriptionPane);

        setSize(680, 480);
        setVisible(true);
        addToDesktopPane();
        center();
        requestFocus();
    }
}
