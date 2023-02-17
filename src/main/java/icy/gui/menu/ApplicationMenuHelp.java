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
import icy.network.NetworkUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * @author Thomas MUSSET
 */
public final class ApplicationMenuHelp extends AbstractApplicationMenu {
    public ApplicationMenuHelp() {
        super("Help");

        final IcyMenuItem itemHelp = new IcyMenuItem("Get Help", GoogleMaterialDesignIcons.HELP_OUTLINE);
        itemHelp.addActionListener(e -> NetworkUtil.openBrowser(NetworkUtil.IMAGE_SC_ICY_URL));
        itemHelp.setAccelerator(KeyStroke.getKeyStroke("F1"));
        add(itemHelp);

        final IcyMenuItem itemGettingStarted = new IcyMenuItem("Getting Started", GoogleMaterialDesignIcons.FLAG);
        itemGettingStarted.addActionListener(e -> NetworkUtil.openBrowser(NetworkUtil.WEBSITE_URL + "trainings/"));
        add(itemGettingStarted);

        // TODO change action to use Icy's internal bug report system ?
        final IcyMenuItem itemSubmitBug = new IcyMenuItem("Submit a Bug Report", GoogleMaterialDesignIcons.BUG_REPORT);
        itemSubmitBug.addActionListener(e -> NetworkUtil.openBrowser("https://gitlab.pasteur.fr/bia/icy/-/issues"));
        add(itemSubmitBug);

        final IcyMenuItem itemShowLog = new IcyMenuItem("Show Log", GoogleMaterialDesignIcons.DESCRIPTION);
        itemShowLog.addActionListener(e -> {
            try {
                final File log = new File("./icy.log");
                if (log.isFile())
                    Desktop.getDesktop().open(log);
            }
            catch (IOException ex) {
                System.err.println("An error occured while opening log file");
            }
        });
        add(itemShowLog);
    }
}
