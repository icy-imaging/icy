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

package org.bioimageanalysis.icy.gui.action;

import org.bioimageanalysis.icy.gui.clipboard.Clipboard;
import org.bioimageanalysis.icy.gui.clipboard.TransferableImage;
import org.bioimageanalysis.icy.gui.frame.AboutFrame;
import org.bioimageanalysis.icy.gui.viewer.Viewer;
import org.bioimageanalysis.icy.model.image.ImageUtil;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.network.NetworkUtil;
import org.bioimageanalysis.icy.extension.plugin.PluginUpdater;
import org.bioimageanalysis.icy.system.preferences.GeneralPreferences;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.network.update.IcyUpdater;
import org.bioimageanalysis.icy.common.reflect.ClassUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * General actions.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class GeneralActions {
    public static final IcyAbstractAction searchAction = new IcyAbstractAction(
            "Search",
            "Application search tool", KeyEvent.VK_F,
            SystemUtil.getMenuCtrlMaskEx()
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            //final MainFrame mf = Icy.getMainInterface().getMainFrame();

            // TODO replace search bar
            /*if (mf != null) {
                final SearchBar sb = mf.getSearchBar();

                if (sb != null) {
                    sb.setFocus();
                    return true;
                }
            }*/
            return false;
        }
    };

    public static final IcyAbstractAction exitApplicationAction = new IcyAbstractAction("Quit Icy") {
        @Override
        public boolean doAction(final ActionEvent e) {
            Icy.exit(false);
            return true;
        }
    };

    @Deprecated(since = "3.0.0", forRemoval = true)
    public static final IcyAbstractAction detachedModeAction = new IcyAbstractAction(
            "Detached Mode",
            "Detached mode ON/OFF",
            "Switch application to detached / attached mode"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final boolean value = !Icy.getMainInterface().isDetachedMode();

            // set detached mode
            Icy.getMainInterface().setDetachedMode(value);
            // and save state
            GeneralPreferences.setMultiWindowMode(value);

            return true;
        }

        @Override
        public boolean isEnabled() {
            return false; // TODO disable for now...
        }
    };

    public static final IcyAbstractAction copyImageAction = new IcyAbstractAction(
            "Copy image",
            "Copy image to clipboard",
            "Copy the active image to the system clipboard.",
            KeyEvent.VK_C,
            SystemUtil.getMenuCtrlMaskEx(),
            true,
            "Copying image to the clipboard..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final Viewer viewer = Icy.getMainInterface().getActiveViewer();

            if (viewer != null) {
                final Sequence seq = viewer.getSequence();

                if (seq != null) {
                    try {
                        final BufferedImage img = viewer.getRenderedImage(viewer.getPositionT(), viewer.getPositionZ(), viewer.getPositionC(), false);

                        // put image in system clipboard
                        Clipboard.putSystem(new TransferableImage(img), null);
                        // clear content of Icy clipboard
                        Clipboard.clear();

                        return true;
                    }
                    catch (final Throwable e1) {
                        IcyLogger.error(GeneralActions.class, e1, "Can't copy image to clipboard.");
                    }
                }
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveSequence() != null);
        }
    };

    public static final IcyAbstractAction pasteImageAction = new IcyAbstractAction(
            "Paste image",
            "Paste image from clipboard",
            "Paste image from the system clipboard in a new sequence.",
            KeyEvent.VK_V,
            SystemUtil.getMenuCtrlMaskEx(),
            true,
            "Creating new sequence from clipboard image..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            try {
                if (Clipboard.hasTypeSystem(DataFlavor.imageFlavor)) {
                    final Image img = (Image) Clipboard.getSystem(DataFlavor.imageFlavor);
                    Icy.getMainInterface().addSequence(new Sequence("Clipboard image", ImageUtil.toBufferedImage(img)));
                    return true;
                }
            }
            catch (final Throwable e1) {
                IcyLogger.error(GeneralActions.class, e1, "Can't paste image from clipboard.");
            }

            return false;
        }

        @Override
        public boolean isEnabled() {
            try {
                return super.isEnabled() && Clipboard.hasTypeSystem(DataFlavor.imageFlavor);
            }
            catch (final Throwable e) {
                return false;
            }
        }
    };

    public static final IcyAbstractAction onlineHelpAction = new IcyAbstractAction(
            "Get Help",
            "Open a browser and display support forum",
            KeyEvent.VK_F1
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            // open browser on help page
            NetworkUtil.openBrowser(NetworkUtil.IMAGE_SC_ICY_URL);
            return true;
        }
    };

    public static final IcyAbstractAction websiteAction = new IcyAbstractAction("Go to Website") {
        @Override
        public boolean doAction(final ActionEvent e) {
            // open browser on help page
            NetworkUtil.openBrowser(NetworkUtil.WEBSITE_URL);
            return true;
        }
    };

    // TODO: uncomment when done !
    // public static IcyAbstractAction linkAction = new IcyAbstractAction("Link", new IcyIcon(ResourceUtil.ICON_LINK),
    // "Link / unlink online user account",
    // "Link / unlink with online user account.\nGive access to extra features as plugin rating")
    // {
    //
    // /**
    // *
    // */
    // private static final long serialVersionUID = 3449298011169150396L;
    //
    // @Override
    // public boolean doAction(ActionEvent e)
    // {
    // if (Audit.isUserLinked())
    // {
    // // ask for confirmation
    // if (!Icy.getMainInterface().isHeadLess()
    // && !ConfirmDialog.confirm("Do you want to unlink user account ?"))
    // return false;
    //
    // // unlink user
    // Audit.unlinkUser();
    // }
    // else
    // {
    // // update link first
    // Audit.updateUserLink();
    //
    // // still not linked --> link user
    // if (!Audit.isUserLinked())
    // Audit.linkUser();
    // }
    //
    // // refresh user infos (in title)
    // final MainFrame frame = Icy.getMainInterface().getMainFrame();
    // if (frame != null)
    // frame.refreshTitle();
    //
    // return true;
    // }
    // };

    public static final IcyAbstractAction checkUpdateAction = new IcyAbstractAction(
            "Check for Updates",
            "Check for updates",
            "Search updates for application and plugins in all referenced repositories."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            // check core update
            if (!IcyUpdater.isCheckingForUpdate())
                IcyUpdater.checkUpdate(false);
            // check plugin update
            /*if (!PluginUpdater.isCheckingForUpdate()) // TODO uncomment this
                PluginUpdater.checkUpdate(false);*/

            return true;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && !(IcyUpdater.isCheckingForUpdate() || PluginUpdater.isCheckingForUpdate());
        }

    };

    public static final IcyAbstractAction aboutAction = new IcyAbstractAction(
            "About Icy",
            "About Icy",
            "Information about ICY's authors, license and copyrights."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new AboutFrame(0);
            return true;
        }
    };

    public static final IcyAbstractAction changeLogAction = new IcyAbstractAction(
            "See Changelog",
            "See changelog",
            "See the changelog informations."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new AboutFrame(1);
            return true;
        }
    };

    /**
     * Return all actions of this class
     */
    @Deprecated(forRemoval = true)
    public static @NotNull List<IcyAbstractAction> getAllActions() {
        final List<IcyAbstractAction> result = new ArrayList<>();

        for (final Field field : GeneralActions.class.getFields()) {
            final Class<?> type = field.getType();

            try {
                if (ClassUtil.isSubClass(type, IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (ClassUtil.isSubClass(type, IcyAbstractAction.class))
                    result.add((IcyAbstractAction) field.get(null));
            }
            catch (final Exception e) {
                // ignore
            }
        }

        return result;
    }
}
