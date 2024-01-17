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

package icy.gui.toolbar.button;

import icy.gui.component.button.IcyToggleButton;
import icy.gui.toolbar.container.InspectorPanel;
import icy.gui.toolbar.panel.SequencePanel;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Thomas Musset
 */
public final class SequenceButton extends IcyToggleButton implements ActionListener {
    public SequenceButton() {
        super(GoogleMaterialDesignIcons.IMAGE);
        setFocusable(false);
        addActionListener(this);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final InspectorPanel panel = InspectorPanel.getInstance();
        if (isSelected())
            panel.show(SequencePanel.getInstance());
        else
            panel.close();
    }
}
