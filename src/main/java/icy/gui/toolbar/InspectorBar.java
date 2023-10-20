/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

package icy.gui.toolbar;

import icy.gui.component.button.IcyToggleButton;
import icy.gui.toolbar.button.*;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.Enumeration;

/**
 * @author Thomas MUSSET
 */
public final class InspectorBar extends IcyToolbar {
    private IcyToggleButton selected = null;

    /**
     * Creates a new tool bar; orientation defaults to <code>VERTICAL</code>.
     */
    public InspectorBar() {
        super(VERTICAL, false);

        final ButtonGroup group = new ButtonGroup();

        final SequenceButton sequenceButton = new SequenceButton();
        group.add(sequenceButton);
        add(sequenceButton);

        final ROIButton ROIButton = new ROIButton();
        group.add(ROIButton);
        add(ROIButton);

        final LayersButton layersButton = new LayersButton();
        group.add(layersButton);
        add(layersButton);

        final HistoryButton historyButton = new HistoryButton();
        group.add(historyButton);
        add(historyButton);

        add(Box.createVerticalGlue());

        final ConsoleButton consoleButton = new ConsoleButton();
        add(consoleButton);

        add(new EHCacheButton());

        final ActionListener openClose = e -> {
            if (e.getSource() instanceof IcyToggleButton) {
                if (selected == null || !selected.equals(e.getSource()))
                    selected = (IcyToggleButton) e.getSource();
                else if (selected.equals(e.getSource())) {
                    group.clearSelection();
                    selected = null;
                }
            }
        };

        final Enumeration<AbstractButton> buttons = group.getElements();
        while (buttons.hasMoreElements()) {
            final AbstractButton b = buttons.nextElement();
            if (b instanceof IcyToggleButton)
                b.addActionListener(openClose);
        }
    }
}
