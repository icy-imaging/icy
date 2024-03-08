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

package icy.gui.frame;

import icy.action.IcyAbstractAction;
import icy.common.MenuCallback;
import icy.resource.ResourceUtil;
import icy.system.SystemUtil;
import icy.system.logging.IcyLogger;
import icy.system.thread.ThreadUtil;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;

/**
 * @author Stephane
 * @author Thomas Musset
 */
public class IcyInternalFrame extends JInternalFrame {
    private class CloseAction extends IcyAbstractAction {
        public CloseAction() {
            super(
                    "Close",
                    "Close window",
                    KeyEvent.VK_F4,
                    SystemUtil.getMenuCtrlMaskEx()
            );
        }

        @Override
        public boolean doAction(final ActionEvent e) {
            close(false);
            return true;
        }
    }

    /**
     * internals
     */
    //SubstanceInternalFrameTitlePane titlePane = null;
    // JMenu systemMenu;
    MenuCallback systemMenuCallback;
    private boolean titleBarVisible;
    private boolean closeItemVisible;
    private final boolean initialized;

    public IcyInternalFrame(final String title, final boolean resizable, final boolean closable, final boolean maximizable, final boolean iconifiable) {
        super(title, resizable, closable, maximizable, iconifiable);

        addPropertyChangeListener("titlePane", evt -> {
            // invoke later so the titlePane variable is up to date
            ThreadUtil.invokeLater(this::updateTitlePane);
        });

        addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(final InternalFrameEvent e) {
                // release the system menu callback as it can lead to some memory leak
                // (cycling reference)
                systemMenuCallback = null;
            }
        });

        setFrameIcon(ResourceUtil.ICON_ICY_20);
        setVisible(false);

        systemMenuCallback = null;
        closeItemVisible = closable;
        //updateTitlePane(LookAndFeelUtil.getTitlePane(this));
        updateSystemMenu();

        titleBarVisible = true;
        initialized = true;
    }

    /*
     * update internals informations linked to title pane with specified pane
     */
    /*protected void updateTitlePane(final SubstanceInternalFrameTitlePane pane)
    {
        // update pane save
        if (pane != null)
            titlePane = pane;
        // update menu
        // if (titlePane != null)
        // systemMenuBar = titlePane.getMenuBar();
        // refresh system menu whatever
        updateSystemMenu();
    }*/

    /**
     * update internals informations linked to title pane and title pane state
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
        if (/*(titlePane != null) && */!isClosed()) {
            final JMenu menu;

            if (systemMenuCallback != null)
                menu = systemMenuCallback.getMenu();
            else
                menu = getDefaultSystemMenu();

            // ensure compatibility with heavyweight component
            menu.getPopupMenu().setLightWeightPopupEnabled(false);

            // rebuild menu
            //titlePane.setSystemMenu(menu);
            // systemMenuBar.removeAll();
            // systemMenuBar.add(menu);
            // systemMenuBar.validate();
        }
    }

    /**
     * Close the frame.
     *
     * @param force if <code>true</code> the frame is close even if {@link #isClosable()} return <code>false</code>
     */
    public void close(final boolean force) {
        if (force || isClosable())
            doDefaultCloseAction();
    }

    /**
     * Implement isMinimized method
     */
    public boolean isMinimized() {
        return isIcon();
    }

    /**
     * Implement isMaximized method
     */
    public boolean isMaximized() {
        return isMaximum();
    }

    /**
     * Implement setMinimized method
     */
    public void setMinimized(final boolean value) {
        // only relevant if state changed
        if (isMinimized() ^ value) {
            try {
                setIcon(value);
            }
            catch (final PropertyVetoException e) {
                IcyLogger.error(IcyInternalFrame.class, e, e.getLocalizedMessage());
            }
        }
    }

    /**
     * Implement setMaximized method
     */
    public void setMaximized(final boolean value) {
        // have to check that else we obtain a null pointer exception
        if (getParent() == null)
            return;

        // only relevant if state changed
        if (isMaximized() ^ value) {
            try {
                // have to check for parent non null
                setMaximum(value);
            }
            catch (final PropertyVetoException e) {
                IcyLogger.error(IcyInternalFrame.class, e, e.getLocalizedMessage());
            }
        }
    }

    /**
     * @return the titleBarVisible
     */
    public boolean isTitleBarVisible() {
        return titleBarVisible;
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

    @Override
    public void setClosable(final boolean b) {
        super.setClosable(b);

        if (!b)
            setCloseItemVisible(false);
    }

    /**
     * @param value the titleBarVisible to set
     */
    public void setTitleBarVisible(final boolean value) {
        /*if (value)
            LookAndFeelUtil.setTitlePane(this, titlePane);
        else
            LookAndFeelUtil.setTitlePane(this, null);*/

        revalidate();

        titleBarVisible = value;
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
}
