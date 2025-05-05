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

import org.bioimageanalysis.icy.gui.action.IcyAbstractAction;
import org.bioimageanalysis.icy.gui.component.ComponentUtil;
import org.bioimageanalysis.icy.gui.menu.MenuCallback;
import org.bioimageanalysis.icy.io.ResourceUtil;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class IcyExternalFrame extends JFrame {
    private class CloseAction extends IcyAbstractAction {
        public CloseAction() {
            super("Close", "Close window", KeyEvent.VK_F4, SystemUtil.getMenuCtrlMaskEx());
        }

        @Override
        public boolean doAction(final ActionEvent e) {
            close();
            return true;
        }
    }

    /**
     * internals
     */
    MenuCallback systemMenuCallback;
    private final boolean titleBarVisible;
    private boolean closeItemVisible;
    private final boolean initialized;

    public IcyExternalFrame(final String title) throws HeadlessException {
        super(title);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent e) {
                // release the system menu callback as it can lead to some memory leak
                // (cycling reference)
                systemMenuCallback = null;
            }
        });
        // Hide preview in Mac dock, so we don't set icon image for frame on MacOS
        if (!SystemUtil.isMac())
            setIconImages(ResourceUtil.getIcyIconImages());
        setVisible(false);

        systemMenuCallback = null;
        closeItemVisible = true;
        updateSystemMenu();

        titleBarVisible = true;
        initialized = true;
    }

    /**
     * update internals informations linked to title pane
     */
    protected void updateTitlePane() {
        if (initialized) {
            // title pane can have changed
            //updateTitlePane(LookAndFeelUtil.getTitlePane(this));

            if (!titleBarVisible)
                setTitleBarVisible(false);
        }
    }

    /**
     * Refresh system menu
     */
    public void updateSystemMenu() {
        final JMenu menu;

        if (systemMenuCallback != null)
            menu = systemMenuCallback.getMenu();
        else
            menu = getDefaultSystemMenu();

        // ensure compatibility with heavyweight component
        menu.getPopupMenu().setLightWeightPopupEnabled(false);
    }

    public void setTitleBarVisible(final boolean value) {
        if (value)
            getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
        else
            getRootPane().setWindowDecorationStyle(JRootPane.NONE);

        validate();
    }

    /**
     * close frame
     */
    public void close() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    /**
     * Implement isMinimized method
     */
    public boolean isMinimized() {
        return ComponentUtil.isMinimized(this);
    }

    /**
     * Implement isMaximized method
     */
    public boolean isMaximized() {
        return ComponentUtil.isMaximized(this);
    }

    /**
     * Implement setMinimized method
     */
    public void setMinimized(final boolean value) {
        ComponentUtil.setMinimized(this, value);
    }

    /**
     * Implement setMaximized method
     */
    public void setMaximized(final boolean value) {
        ComponentUtil.setMaximized(this, value);
    }

    /**
     * @return the titleBarVisible
     */
    public boolean isTitleBarVisible() {
        return getRootPane().getWindowDecorationStyle() != JRootPane.NONE;
    }

    /**
     * @return the closeItemVisible
     */
    public boolean isCloseItemVisible() {
        return closeItemVisible;
    }

    /**
     * @param value the closeItemVisible to set
     */
    public void setCloseItemVisible(final boolean value) {
        if (closeItemVisible != value) {
            closeItemVisible = value;

            ThreadUtil.invokeLater(this::updateSystemMenu);
        }
    }

    /**
     * Return the default system menu
     */
    public JMenu getDefaultSystemMenu() {
        final JMenu result = new JMenu();

        if (closeItemVisible)
            result.add(new CloseAction());

        return result;
    }

    /**
     * @return the systemMenuCallback
     */
    public MenuCallback getSystemMenuCallback() {
        return systemMenuCallback;
    }

    /**
     * @param value the systemMenuCallback to set
     */
    public void setSystemMenuCallback(final MenuCallback value) {
        if (systemMenuCallback != value) {
            systemMenuCallback = value;

            ThreadUtil.invokeLater(this::updateSystemMenu);
        }
    }

    @Override
    public void setBounds(final int x, final int y, final int width, final int height) {
        final Rectangle r = new Rectangle(x, y, width, height);

        // prevent to go completely off screen
        ComponentUtil.fixPosition(this, r);

        super.setBounds(r.x, r.y, r.width, r.height);
    }
}
