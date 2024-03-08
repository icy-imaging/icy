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

package icy.gui.menu;

import icy.action.GeneralActions;
import icy.gui.component.menu.IcyMenuItem;
import icy.network.NetworkUtil;
import icy.resource.icon.SVGIcon;
import icy.system.logging.IcyLogger;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * @author Thomas Musset
 */
public final class ApplicationMenuHelp extends AbstractApplicationMenu {
    @NotNull
    private static final ApplicationMenuHelp instance = new ApplicationMenuHelp();

    @NotNull
    public static synchronized ApplicationMenuHelp getInstance() {
        return instance;
    }

    private ApplicationMenuHelp() {
        super("Help");

        final IcyMenuItem itemHelp = new IcyMenuItem("Get Help", SVGIcon.HELP);
        itemHelp.addActionListener(e -> NetworkUtil.openBrowser(NetworkUtil.IMAGE_SC_ICY_URL));
        itemHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        add(itemHelp);

        final IcyMenuItem itemGettingStarted = new IcyMenuItem("Getting Started", SVGIcon.FLAG);
        itemGettingStarted.addActionListener(e -> NetworkUtil.openBrowser(NetworkUtil.WEBSITE_URL + "trainings/"));
        itemGettingStarted.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        add(itemGettingStarted);

        // TODO make shortcuts visible
        final IcyMenuItem itemShortcuts = new IcyMenuItem("See Shortcuts", SVGIcon.KEYBOARD);
        itemShortcuts.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        itemShortcuts.setEnabled(false);
        add(itemShortcuts);

        // TODO change action to use Icy's internal bug report system ?
        final IcyMenuItem itemSubmitBug = new IcyMenuItem("Submit a Bug Report", SVGIcon.BUG_REPORT);
        itemSubmitBug.addActionListener(e -> NetworkUtil.openBrowser("https://gitlab.pasteur.fr/bia/icy/-/issues"));
        itemSubmitBug.setEnabled(false);
        add(itemSubmitBug);

        final IcyMenuItem itemShowLog = new IcyMenuItem("Show Log", SVGIcon.DESCRIPTION);
        itemShowLog.addActionListener(e -> {
            try {
                final File log = new File("./icy.log");
                if (log.isFile())
                    Desktop.getDesktop().open(log);
            }
            catch (final IOException ex) {
                IcyLogger.error(ApplicationMenuHelp.class, ex, "An error occured while opening log file.");
            }
        });
        add(itemShowLog);

        addSeparator();

        final IcyMenuItem itemUpdate = new IcyMenuItem("Check for Update", SVGIcon.UPDATE);
        itemUpdate.addActionListener(GeneralActions.checkUpdateAction);
        add(itemUpdate);
    }
}
