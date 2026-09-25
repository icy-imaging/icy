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

package fr.icy.gui.menu;

import fr.icy.gui.action.GeneralActions;
import fr.icy.gui.component.icon.IcySVG;
import fr.icy.gui.component.menu.IcyMenuItem;
import fr.icy.network.NetworkUtil;
import fr.icy.system.UserUtil;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Thomas Musset
 */
public final class ApplicationMenuHelp extends AbstractApplicationMenu {
    private static final Logger LOGGER = Logger.getLogger(ApplicationMenuHelp.class.getName());

    private static final @NonNull ApplicationMenuHelp instance = new ApplicationMenuHelp();

    @Contract(pure = true)
    public static synchronized @NonNull ApplicationMenuHelp getInstance() {
        return instance;
    }

    private ApplicationMenuHelp() {
        super("Help");

        final IcyMenuItem itemHelp = new IcyMenuItem("Get Help", IcySVG.HELP);
        itemHelp.addActionListener(e -> NetworkUtil.openBrowser(NetworkUtil.IMAGE_SC_ICY_URL));
        itemHelp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        add(itemHelp);

        final IcyMenuItem itemGettingStarted = new IcyMenuItem("Getting Started", IcySVG.FLAG);
        itemGettingStarted.addActionListener(e -> NetworkUtil.openBrowser(NetworkUtil.WEBSITE_URL + "trainings/"));
        itemGettingStarted.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        add(itemGettingStarted);

        // TODO make shortcuts visible
        final IcyMenuItem itemShortcuts = new IcyMenuItem("See Shortcuts", IcySVG.KEYBOARD);
        itemShortcuts.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
        itemShortcuts.setEnabled(false);
        add(itemShortcuts);

        // TODO change action to use Icy's internal bug report system ?
        final IcyMenuItem itemSubmitBug = new IcyMenuItem("Submit a Bug Report", IcySVG.BUG_REPORT);
        itemSubmitBug.addActionListener(e -> NetworkUtil.openBrowser("https://gitlab.pasteur.fr/bia/icy/-/issues"));
        itemSubmitBug.setEnabled(false);
        add(itemSubmitBug);

        final IcyMenuItem itemShowLog = new IcyMenuItem("Show Log", IcySVG.DESCRIPTION);
        itemShowLog.addActionListener(e -> {
            try {
                final File log = new File(UserUtil.getIcyHomeDirectory(), "icy.log");
                if (log.isFile())
                    Desktop.getDesktop().open(log);
            }
            catch (final IOException ex) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, "An error occurred while opening log file.", ex);
            }
        });
        add(itemShowLog);

        addSeparator();

        final IcyMenuItem itemUpdate = new IcyMenuItem(GeneralActions.checkUpdateAction, IcySVG.UPDATE);
        add(itemUpdate);
    }
}
