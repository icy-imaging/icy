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
package icy.gui.inspector;

import icy.gui.frame.ActionFrame;
import icy.preferences.XMLPreferences;

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Frame to change ROI table settings.
 *
 * @author Stephane
 * @author Thomas Musset
 */
public class RoiSettingFrame extends ActionFrame {
    final RoiSettingPanel settingPanel;

    public RoiSettingFrame(XMLPreferences viewPreferences, XMLPreferences exportPreferences, final Runnable onValidate) {
        super("ROI table setting", true);

        settingPanel = new RoiSettingPanel(viewPreferences, exportPreferences);
        getMainPanel().add(settingPanel, BorderLayout.CENTER);

        setPreferredSize(new Dimension(520, 480));
        setOkAction(e -> {
            // save setting
            settingPanel.save();
            // call callback
            if (onValidate != null)
                onValidate.run();
        });

        pack();
        addToDesktopPane();
        setVisible(true);
        center();
        requestFocus();
    }
}
