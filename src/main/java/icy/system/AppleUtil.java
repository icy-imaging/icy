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

package icy.system;

import icy.gui.dialog.LoaderDialog;
import icy.gui.frame.AboutFrame;
import icy.gui.preferences.GeneralPreferencePanel;
import icy.gui.preferences.PreferenceFrame;
import icy.main.Icy;
import icy.resource.ResourceUtil;
import icy.system.logging.IcyLogger;
import icy.system.thread.ThreadUtil;

import java.awt.*;
import java.beans.PropertyChangeListener;

/**
 * OSX application compatibility class
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class AppleUtil {
    //static final Thread fixThread = new Thread(AppleUtil::appleFixLiveRun, "AppleFix");

    public static void init() {
        // only when we have the GUI
        if (!Icy.getMainInterface().isHeadLess()) {
            try {
                // set quit strategy for OSX
                System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");

                final Desktop desktop = Desktop.getDesktop();

                desktop.setAboutHandler(e -> new AboutFrame());
                desktop.setPreferencesHandler(e -> new PreferenceFrame(GeneralPreferencePanel.NODE_NAME));
                desktop.setQuitHandler((e, r) -> Icy.exit(false));
                desktop.setOpenFileHandler(e -> new LoaderDialog());

                final Taskbar taskbar = Taskbar.getTaskbar();
                taskbar.setIconImage(ResourceUtil.IMAGE_ICY_256);

                // set menu bar name
                SystemUtil.setProperty("com.apple.mrj.application.apple.menu.about.name", "Icy");
                SystemUtil.setProperty("apple.awt.application.name", "Icy");
            }
            catch (final Exception e) {
                IcyLogger.warn(AppleUtil.class, e, "Can't install MacOS application wrapper.");
            }
        }

        // start the fix thread
        //fixThread.start();
    }

    /**
     * Apple fix live run (fixes specific OS X JVM stuff)
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    @SuppressWarnings("unused")
    private static void appleFixLiveRun() {
        while (true) {
            final Toolkit toolkit = Toolkit.getDefaultToolkit();

            // fix memory leak introduced in java 1.6.0_29 in Mac OS X JVM
            // TODO : remove this when issue will be resolved in JVM
            final PropertyChangeListener[] leak = toolkit.getPropertyChangeListeners("apple.awt.contentScaleFactor");

            // remove listener
            for (final PropertyChangeListener propertyChangeListener : leak)
                toolkit.removePropertyChangeListener("apple.awt.contentScaleFactor", propertyChangeListener);

            // no need more...
            ThreadUtil.sleep(500);
        }
    }
}
