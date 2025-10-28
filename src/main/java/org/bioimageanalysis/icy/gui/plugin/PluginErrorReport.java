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

package org.bioimageanalysis.icy.gui.plugin;

import org.bioimageanalysis.icy.gui.component.icon.IcySVG;
import org.bioimageanalysis.icy.gui.frame.error.ErrorReportFrame;
import org.bioimageanalysis.icy.gui.frame.progress.AnnounceFrame;
import org.bioimageanalysis.icy.gui.frame.progress.CancelableProgressFrame;
import org.bioimageanalysis.icy.gui.frame.progress.ProgressFrame;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.PluginInstaller;
import org.bioimageanalysis.icy.extension.plugin.PluginRepositoryLoader;
import org.bioimageanalysis.icy.extension.plugin.PluginUpdater;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.bioimageanalysis.icy.common.string.StringUtil;

import javax.swing.*;
import javax.swing.text.BadLocationException;

/**
 * This class create a report from a plugin crash and ask the
 * user if he wants to send it to the dev team of the plugin.
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class PluginErrorReport {
    /**
     * Report an error thrown by the specified plugin.
     *
     * @param plugin
     *        {@link PluginDescriptor} of the plugin which thrown the error.
     * @param devId
     *        Plugin developer Id, used only if we do not have plugin descriptor information.
     * @param title
     *        Error title if any
     * @param message
     *        Error message to report
     */
    public static void report(final PluginDescriptor plugin, final String devId, final String title, final String message) {
        // headless mode ?
        if (Icy.getMainInterface().isHeadLess()) {
            // do nothing

            // directly report
            // IcyExceptionHandler.report(plugin, devId, message);

            return;
        }

        // cannot be reported...
        // if ((plugin == null) && StringUtil.isEmpty(devId))
        // return;

        if (ErrorReportFrame.hasErrorFrameOpened())
            return;

        // always do that in background process
        ThreadUtil.bgRun(() -> {
            if (plugin != null) {
                final CancelableProgressFrame info = new CancelableProgressFrame("Plugin '" + plugin.getName() + "' has crashed, searching for update...");

                // wait for online basic info loaded
                PluginRepositoryLoader.waitLoaded();

                PluginDescriptor onlinePlugin = null;

                try {
                    // search for update
                    if (!info.isCancelRequested())
                        onlinePlugin = PluginUpdater.getUpdate(plugin);
                }
                finally {
                    info.close();
                }

                // update found and not canceled
                if (!info.isCancelRequested() && (onlinePlugin != null)) {
                    PluginInstaller.install(onlinePlugin, true);
                    new AnnounceFrame("The plugin crashed but a new version has been found, try it again when installation is done", 10);
                    // don't need to report
                    return;
                }

                // display report as no update were found
                ThreadUtil.invokeLater(() -> doReport(plugin, null, title, message));
            }
            else {
                // directly display report frame
                ThreadUtil.invokeLater(() -> doReport(null, devId, title, message));
            }
        });
    }

    /**
     * Report an error thrown by the specified plugin.
     *
     * @param plugin
     *        {@link PluginDescriptor} of the plugin which thrown the error.
     * @param devId
     *        Plugin developer Id, used only if we do not have plugin descriptor information.
     * @param message
     *        Error message to report
     */
    public static void report(final PluginDescriptor plugin, final String devId, final String message) {
        report(plugin, devId, null, message);
    }

    /**
     * Report an error thrown by the specified plugin.
     *
     * @param plugin
     *        {@link PluginDescriptor} of the plugin which thrown the error.
     * @param message
     *        Error message to report
     */
    public static void report(final PluginDescriptor plugin, final String message) {
        report(plugin, null, null, message);
    }

    // internal use only
    static void doReport(final PluginDescriptor plugin, final String devId, final String title, final String message) {
        // headless mode
        if (Icy.getMainInterface().isHeadLess()) {
            // do not report

            // report directly
            // IcyExceptionHandler.report(plugin, devId, message);

            return;
        }

        String str;
        final Icon icon;

        // build title
        if (plugin != null) {
            str = "<html><br>The plugin named <b>" + plugin.getName() + "</b> has encountered a problem";
            //icon = plugin.getIcon();

            final IcySVG icons = plugin.getSVG();
            if (icons != null) {
                icon = icons.getIcon(32);
            }
            else
                icon = null;
        }
        else if (!StringUtil.isEmpty(devId)) {
            str = "<html><br>The plugin from the developer <b>" + devId + "</b> has encountered a problem";
            icon = null;
        }
        else {
            str = "<html><br>The application has encountered a problem";
            icon = null;
        }

        if (StringUtil.isEmpty(title))
            str += ".<br><br>";
        else
            str += " :<br><i>" + title + "</i><br><br>";

        str += "Reporting this problem is anonymous and will help improving this plugin.<br><br></html>";

        final ErrorReportFrame frame = new ErrorReportFrame(icon, str, message);

        // set specific report action here
        frame.setReportAction(e -> {
            final ProgressFrame progressFrame = new ProgressFrame("Sending report...");

            try {
                IcyExceptionHandler.report(plugin, devId, frame.getReportMessage());
            }
            catch (final BadLocationException ex) {
                IcyLogger.error(PluginErrorReport.class, ex, "Error while reporting error.");
            }
            finally {
                progressFrame.close();
            }
        });
    }
}
