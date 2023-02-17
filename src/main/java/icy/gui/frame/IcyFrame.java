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
package icy.gui.frame;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.ImageObserver;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.EventListenerList;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import icy.action.IcyAbstractAction;
import icy.common.MenuCallback;
import icy.gui.main.MainFrame;
import icy.gui.util.ComponentUtil;
import icy.main.Icy;
import icy.system.thread.ThreadUtil;
import icy.util.StringUtil;

/**
 * This class behave either as a JFrame or a JInternalFrame.<br>
 * IcyFrame should be 100% AWT safe
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas MUSSET
 */
public class IcyFrame implements InternalFrameListener, WindowListener, ImageObserver, PropertyChangeListener {
    private class SwitchStateAction extends IcyAbstractAction {
        public SwitchStateAction() {
            super("");

            setAccelerator(KeyEvent.VK_F3);

            refreshState();
        }

        @Override
        public boolean doAction(ActionEvent e) {
            switchState();
            return true;
        }

        void refreshState() {
            if (isInternalized()) {
                setName("Detach");
                setDescription("Externalize the window");
            }
            else {
                setName("Attach");
                setDescription("Internalize the window");
            }
        }
    }

    /**
     * list containing all active frames
     */
    static ArrayList<IcyFrame> frames = new ArrayList<>();

    /**
     * @return Return all active (not closed) IcyFrame
     */
    public static ArrayList<IcyFrame> getAllFrames() {
        synchronized (frames) {
            return new ArrayList<>(frames);
        }
    }

    /**
     * @param frameClass class frame
     * @return Return all active IcyFrame which derive from the specified class
     */
    public static ArrayList<IcyFrame> getAllFrames(Class<?> frameClass) {
        final ArrayList<IcyFrame> result = new ArrayList<>();

        if (frameClass != null) {
            synchronized (frames) {
                for (IcyFrame frame : frames)
                    if (frameClass.isInstance(frame))
                        result.add(frame);

            }
        }

        return result;
    }

    /**
     * @param frame internal frame
     * @return Find IcyFrame corresponding to the specified JInternalFrame
     */
    public static IcyFrame findIcyFrame(JInternalFrame frame) {
        synchronized (frames) {
            for (IcyFrame f : frames)
                if (f.getInternalFrame() == frame)
                    return f;

            return null;
        }
    }

    public enum IcyFrameState {
        INTERNALIZED, EXTERNALIZED
    }

    protected IcyExternalFrame externalFrame;
    protected IcyInternalFrame internalFrame;

    /**
     * frame state (internal / external)
     */
    protected IcyFrameState state;

    /**
     * sync flag for AWT thread process
     */
    protected boolean syncProcess;

    /**
     * listeners
     */
    protected EventListenerList frameEventListeners;

    /**
     * internals
     */
    protected MenuCallback defaultSystemMenuCallback;
    protected SwitchStateAction switchStateAction;
    protected boolean switchStateItemVisible;
    protected IcyFrameState previousState;
    protected final boolean headless;

    public IcyFrame() {
        this("", false, true, false, false, true);
    }

    public IcyFrame(String title) {
        this(title, false, true, false, false, true);
    }

    public IcyFrame(String title, boolean resizable) {
        this(title, resizable, true, false, false, true);
    }

    public IcyFrame(String title, boolean resizable, boolean closable) {
        this(title, resizable, closable, false, false, true);
    }

    public IcyFrame(String title, boolean resizable, boolean closable, boolean maximizable) {
        this(title, resizable, closable, maximizable, false, true);
    }

    public IcyFrame(final String title, final boolean resizable, final boolean closable, final boolean maximizable,
                    final boolean iconifiable) {
        this(title, resizable, closable, maximizable, iconifiable, true);
    }

    public IcyFrame(final String title, final boolean resizable, final boolean closable, final boolean maximizable,
                    final boolean iconifiable, final boolean waitCreate) {
        super();

        headless = Icy.getMainInterface().isHeadLess();

        // don't try to go further
        if (headless)
            return;

        frameEventListeners = new EventListenerList();
        defaultSystemMenuCallback = this::getDefaultSystemMenu;

        syncProcess = false;

        // set default state
        if (canBeInternalized())
            state = IcyFrameState.INTERNALIZED;
        else
            state = IcyFrameState.EXTERNALIZED;

        switchStateItemVisible = true;
        // wanted default state
        previousState = IcyFrameState.INTERNALIZED;

        // create action after state has been set
        switchStateAction = new SwitchStateAction();
        switchStateAction.setEnabled(canBeInternalized());

        ThreadUtil.invoke(() -> {
            externalFrame = createExternalFrame(title);
            // redirect frame / window events
            externalFrame.addWindowListener(IcyFrame.this);
            externalFrame.setLocationRelativeTo(null);
            externalFrame.setResizable(resizable);
            externalFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            // default size
            externalFrame.setSize(480, 400);

            internalFrame = createInternalFrame(title, resizable, closable, maximizable, iconifiable);
            // redirect frame / window events
            internalFrame.addInternalFrameListener(IcyFrame.this);
            // default size
            internalFrame.setSize(480, 400);

            // default system menu callback
            externalFrame.setSystemMenuCallback(defaultSystemMenuCallback);
            internalFrame.setSystemMenuCallback(defaultSystemMenuCallback);

            // register to the list
            synchronized (frames) {
                frames.add(IcyFrame.this);
            }
        }, waitCreate);

        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
        // listen main frame mode change
        if (mainFrame != null)
            mainFrame.addPropertyChangeListener(MainFrame.PROPERTY_DETACHEDMODE, this);
    }

    /**
     * @param title string
     * @return Permit IcyExternalFrame overriding
     */
    protected IcyExternalFrame createExternalFrame(String title) {
        return new IcyExternalFrame(title);
    }

    /**
     * @param title       string
     * @param resizable   boolean
     * @param iconifiable boolean
     * @param maximizable boolean
     * @param closable    boolean
     * @return Permit IcyInternalFrame overriding
     */
    protected IcyInternalFrame createInternalFrame(String title, boolean resizable, boolean closable,
                                                   boolean maximizable, boolean iconifiable) {
        return new IcyInternalFrame(title, resizable, closable, maximizable, iconifiable);
    }

    /**
     * @return Return true if the frame can be internalized
     */
    protected boolean canBeInternalized() {
        final MainFrame frame = Icy.getMainInterface().getMainFrame();

        // internalization possible only in single window mode
        if (frame != null)
            return !frame.isDetachedMode();

        return false;
    }

    /**
     * Refresh system menu
     */
    public void updateSystemMenu() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.updateSystemMenu();
            else
                externalFrame.updateSystemMenu();
        }, syncProcess);
    }

    /**
     * Close frame (send closing event)
     */
    public void close() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            internalFrame.close(true);
            externalFrame.close();
        }, syncProcess);
    }

    /**
     * Dispose frame (send closed event)
     */
    public void dispose() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.dispose();
            else
                externalFrame.dispose();
        }, syncProcess);
    }

    /**
     * go from detached to attached and opposite
     */
    public void switchState() {
        if (isInternalized())
            detach();
        else
            attach();
    }

    /**
     * set the frame to be an inner frame on the desktop pane
     */
    public void internalize() {
        if (isExternalized())
            attach();
    }

    /**
     * the frame becomes detached in an independent frame
     */
    public void externalize() {
        if (isInternalized())
            detach();
    }

    /**
     * Set the frame to be an inner frame on the desktop pane
     */
    public void attach() {
        // don't try to go further
        if (headless)
            return;

        if (isInternalized() || !canBeInternalized())
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            // save current visible state
            final boolean visible = externalFrame.isVisible();

            // hide external frame
            if (visible)
                externalFrame.setVisible(false);

            final JMenuBar menuBar = externalFrame.getJMenuBar();
            final Container content = externalFrame.getContentPane();

            // remove components from external frame
            externalFrame.setJMenuBar(null);
            externalFrame.setContentPane(new JPanel());
            externalFrame.validate();

            internalFrame.setJMenuBar(menuBar);
            internalFrame.setContentPane(content);
            internalFrame.validate();

            // show internal frame
            if (visible) {
                internalFrame.setVisible(true);
                try {
                    internalFrame.setSelected(true);
                }
                catch (PropertyVetoException e) {
                    // ignore
                }
            }

            state = IcyFrameState.INTERNALIZED;

            // notify state change
            stateChanged();
        }, syncProcess);
    }

    /**
     * Set the frame to be detached in an independent frame
     */
    public void detach() {
        // don't try to go further
        if (headless)
            return;

        if (isExternalized())
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            // save current visible state
            final boolean visible = internalFrame.isVisible();

            // hide internal frame
            if (visible)
                internalFrame.setVisible(false);

            final JMenuBar menuBar = internalFrame.getJMenuBar();
            final Container content = internalFrame.getContentPane();

            // remove components from internal frame
            internalFrame.setJMenuBar(null);
            internalFrame.setContentPane(new JPanel());
            internalFrame.validate();

            externalFrame.setJMenuBar(menuBar);
            externalFrame.setContentPane(content);
            externalFrame.validate();

            // show external frame
            if (visible) {
                externalFrame.setVisible(true);
                externalFrame.requestFocus();
            }

            // TODO : we have to force a refresh with resizing or we get a refresh bug on
            // scrollbar (OSX only ?)
            // externalFrame.setSize(externalFrame.getWidth(), externalFrame.getHeight() - 1);
            // externalFrame.setSize(externalFrame.getWidth(), externalFrame.getHeight() + 1);

            state = IcyFrameState.EXTERNALIZED;

            // notify state change
            stateChanged();
        }, syncProcess);
    }

    /**
     * Called on state (internalized / externalized) change
     */
    public void stateChanged() {
        // don't try to go further
        if (headless)
            return;

        // refresh switch action state
        switchStateAction.refreshState();

        // refresh system menu
        updateSystemMenu();

        // fire event
        if (isInternalized())
            fireFrameInternalized(new IcyFrameEvent(IcyFrame.this, null, null));
        else
            fireFrameExternalized(new IcyFrameEvent(IcyFrame.this, null, null));
    }

    /**
     * Center frame on the desktop
     */
    public void center() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                ComponentUtil.center(internalFrame);
            else
                ComponentUtil.center(externalFrame);

        }, syncProcess);
    }

    /**
     * @param c Add to the container c
     */
    public void addTo(final Container c) {
        // don't try to go further
        if (headless)
            return;

        if (isInternalized()) {
            // AWT safe
            ThreadUtil.invoke(() -> c.add(internalFrame), syncProcess);
        }
    }

    /**
     * @param c     Add to the container c
     * @param index int
     */
    public void addTo(final Container c, final int index) {
        // don't try to go further
        if (headless)
            return;

        if (isInternalized()) {
            // AWT safe
            ThreadUtil.invoke(() -> c.add(internalFrame, index), syncProcess);
        }
    }

    /**
     * @param c           container
     * @param constraints object
     * @deprecated Use {@link #addToDesktopPane()} instead.
     */
    @Deprecated
    public void addTo(final Container c, final Object constraints) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> c.add(internalFrame, constraints), syncProcess);
    }

    /**
     * @param constraints Add the frame to the Icy desktop pane with specified constraint.
     */
    public void addToDesktopPane(final Object constraints) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            final JDesktopPane desktop = Icy.getMainInterface().getDesktopPane();

            if (desktop != null)
                desktop.add(internalFrame, constraints);
        }, syncProcess);
    }

    /**
     * Add the frame to the Icy desktop pane
     */
    public void addToDesktopPane() {
        // don't try to go further
        if (headless)
            return;

        addToDesktopPane(JLayeredPane.DEFAULT_LAYER);
    }

    /**
     * @deprecated Use {@link #addToDesktopPane()} instead.
     */
    @Deprecated
    public void addToMainDesktopPane() {
        addToDesktopPane();
    }

    /**
     * Implement add method
     *
     * @param comp component
     */
    public void add(final Component comp) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.add(comp);
            else
                externalFrame.add(comp);
        }, syncProcess);
    }

    /**
     * Implement add method
     *
     * @param constraints object
     * @param comp        component
     */
    public void add(final Component comp, final Object constraints) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.add(comp, constraints);
            else
                externalFrame.add(comp, constraints);
        }, syncProcess);
    }

    /**
     * Implement add method
     *
     * @param comp component
     * @param name string
     */
    public void add(final String name, final Component comp) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.add(name, comp);
            else
                externalFrame.add(name, comp);
        }, syncProcess);
    }

    /**
     * @param c Remove from the container
     */
    public void removeFrom(final Container c) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> c.remove(internalFrame), syncProcess);
    }

    /**
     * Implement removeAll method
     */
    public void removeAll() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.removeAll();
            else
                externalFrame.removeAll();
        }, syncProcess);
    }

    /**
     * @param comp Implement remove method
     */
    public void remove(final Component comp) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.remove(comp);
            else
                externalFrame.remove(comp);
        }, syncProcess);
    }

    /**
     * Remove the frame from the main pane of ICY
     */
    public void removeFromMainDesktopPane() {
        // don't try to go further
        if (headless)
            return;

        removeFrom(Icy.getMainInterface().getDesktopPane());
    }

    /**
     * Implement toFront method
     */
    public void toFront() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.toFront();
            else
                externalFrame.toFront();
        }, syncProcess);
    }

    /**
     * Implement toBack method
     */
    public void toBack() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.toBack();
            else
                externalFrame.toBack();
        }, syncProcess);
    }

    /**
     * Implement pack method
     */
    public void pack() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.pack();
            else
                externalFrame.pack();
        }, syncProcess);
    }

    public Container getFrame() {
        // don't try to go further
        if (headless)
            return null;

        if (isInternalized())
            return internalFrame;

        return externalFrame;
    }

    public IcyInternalFrame getIcyInternalFrame() {
        return internalFrame;
    }

    public IcyExternalFrame getIcyExternalFrame() {
        return externalFrame;
    }

    public JInternalFrame getInternalFrame() {
        return internalFrame;
    }

    public JFrame getExternalFrame() {
        return externalFrame;
    }

    /**
     * @param value Indicate if system menu show display item to switch frame state (internal / external)
     */
    public void setSwitchStateItemVisible(boolean value) {
        // don't try to go further
        if (headless)
            return;

        if (switchStateItemVisible != value) {
            switchStateItemVisible = value;
            switchStateAction.setEnabled(value);
            updateSystemMenu();
        }
    }

    /**
     * @return the systemMenuCallback
     */
    public MenuCallback getSystemMenuCallback() {
        // don't try to go further
        if (headless)
            return null;

        // always have same callback on each frame
        final MenuCallback result = internalFrame.getSystemMenuCallback();

        // default callback ? this means we set it to null
        if (result == defaultSystemMenuCallback)
            return null;

        return result;
    }

    /**
     * Set the system menu callback (this allow modification of system menu)
     *
     * @param value the systemMenuCallback to set
     */
    public void setSystemMenuCallback(MenuCallback value) {
        // don't try to go further
        if (headless)
            return;

        if (value != null) {
            internalFrame.setSystemMenuCallback(value);
            externalFrame.setSystemMenuCallback(value);
        }
        else {
            internalFrame.setSystemMenuCallback(defaultSystemMenuCallback);
            externalFrame.setSystemMenuCallback(defaultSystemMenuCallback);
        }
    }

    /**
     * @return Return the default system menu
     */
    public JMenu getDefaultSystemMenu() {
        // don't try to go further
        if (headless)
            return null;

        final JMenu result;

        if (isInternalized())
            result = internalFrame.getDefaultSystemMenu();
        else
            result = externalFrame.getDefaultSystemMenu();

        if (switchStateItemVisible) {
            result.insert(switchStateAction, 0);
            if (result.getMenuComponentCount() > 1)
                result.insertSeparator(1);
        }

        return result;
    }

    /**
     * @return Implement getParent
     */
    public Container getParent() {
        // don't try to go further
        if (headless)
            return null;

        if (isInternalized())
            return internalFrame.getParent();

        return externalFrame.getParent();
    }

    /**
     * @return Implement getContentPane method
     */
    public Container getContentPane() {
        // don't try to go further
        if (headless)
            return null;

        if (isInternalized())
            return internalFrame.getContentPane();

        return externalFrame.getContentPane();
    }

    /**
     * @return Implement getRootPane method
     */
    public JRootPane getRootPane() {
        // don't try to go further
        if (headless)
            return null;

        if (isInternalized())
            return internalFrame.getRootPane();

        return externalFrame.getRootPane();
    }

    public Border getBorder() {
        // don't try to go further
        if (headless)
            return null;

        if (isInternalized())
            return internalFrame.getBorder();

        return null;
    }

    /**
     * @return the switchStateAction
     */
    public SwitchStateAction getSwitchStateAction() {
        return switchStateAction;
    }

    /**
     * @return Implement getMinimumSize method
     */
    public Dimension getMinimumSize() {
        // don't try to go further
        if (headless)
            return new Dimension();

        if (isInternalized())
            return internalFrame.getMinimumSize();

        return externalFrame.getMinimumSize();
    }

    /**
     * @return Implement getMinimumSize method for internal frame only
     */
    public Dimension getMinimumSizeInternal() {
        // don't try to go further
        if (headless)
            return new Dimension();

        return internalFrame.getMinimumSize();
    }

    /**
     * @return Implement getMinimumSize method for external frame only
     */
    public Dimension getMinimumSizeExternal() {
        // don't try to go further
        if (headless)
            return new Dimension();

        return externalFrame.getMinimumSize();
    }

    /**
     * @return Implement getMaximumSize method
     */
    public Dimension getMaximumSize() {
        // don't try to go further
        if (headless)
            return new Dimension();

        if (isInternalized())
            return internalFrame.getMaximumSize();

        return externalFrame.getMaximumSize();
    }

    /**
     * @return Implement getMaximumSize method for internal frame only
     */
    public Dimension getMaximumSizeInternal() {
        // don't try to go further
        if (headless)
            return new Dimension();

        return internalFrame.getMaximumSize();
    }

    /**
     * @return Implement getMaximumSize method for external frame only
     */
    public Dimension getMaximumSizeExternal() {
        // don't try to go further
        if (headless)
            return new Dimension();

        return externalFrame.getMaximumSize();
    }

    /**
     * @return Implement getPreferredSize method
     */
    public Dimension getPreferredSize() {
        // don't try to go further
        if (headless)
            return new Dimension();

        if (isInternalized())
            return internalFrame.getPreferredSize();

        return externalFrame.getPreferredSize();
    }

    /**
     * @return Implement getPreferredSize method for internal frame only
     */
    public Dimension getPreferredSizeInternal() {
        // don't try to go further
        if (headless)
            return new Dimension();

        return internalFrame.getPreferredSize();
    }

    /**
     * @return Implement getPreferredSize method for external frame only
     */
    public Dimension getPreferredSizeExternal() {
        // don't try to go further
        if (headless)
            return new Dimension();

        return externalFrame.getPreferredSize();
    }

    /**
     * @return Implement getSize method
     */
    public Dimension getSize() {
        // don't try to go further
        if (headless)
            return new Dimension();

        if (isInternalized())
            return internalFrame.getSize();

        return externalFrame.getSize();
    }

    /**
     * @return Implement getSize method for internal frame only
     */
    public Dimension getSizeInternal() {
        // don't try to go further
        if (headless)
            return new Dimension();

        return internalFrame.getSize();
    }

    /**
     * @return Implement getSize method for external frame only
     */
    public Dimension getSizeExternal() {
        // don't try to go further
        if (headless)
            return new Dimension();

        return externalFrame.getSize();
    }

    /**
     * @return Implement getHeight method
     */
    public int getHeight() {
        // don't try to go further
        if (headless)
            return 0;

        if (isInternalized())
            return internalFrame.getHeight();

        return externalFrame.getHeight();
    }

    /**
     * @return Implement getHeight method for internal frame only
     */
    public int getHeightInternal() {
        // don't try to go further
        if (headless)
            return 0;

        return internalFrame.getHeight();
    }

    /**
     * @return Implement getHeight method for external frame only
     */
    public int getHeightExternal() {
        // don't try to go further
        if (headless)
            return 0;

        return externalFrame.getHeight();
    }

    /**
     * @return Implement getWidth method
     */
    public int getWidth() {
        // don't try to go further
        if (headless)
            return 0;

        if (isInternalized())
            return internalFrame.getWidth();

        return externalFrame.getWidth();
    }

    /**
     * @return Implement getWidth method for internal frame only
     */
    public int getWidthInternal() {
        // don't try to go further
        if (headless)
            return 0;

        return internalFrame.getWidth();
    }

    /**
     * @return Implement getWidth method for external frame only
     */
    public int getWidthExternal() {
        // don't try to go further
        if (headless)
            return 0;

        return externalFrame.getWidth();
    }

    /**
     * @return Implement getX method
     */
    public int getX() {
        // don't try to go further
        if (headless)
            return 0;

        if (isInternalized())
            return internalFrame.getX();

        return externalFrame.getX();
    }

    /**
     * @return Implement getX method for internal frame only
     */
    public int getXInternal() {
        // don't try to go further
        if (headless)
            return 0;

        return internalFrame.getX();
    }

    /**
     * @return Implement getX method for external frame only
     */
    public int getXExternal() {
        // don't try to go further
        if (headless)
            return 0;

        return externalFrame.getX();
    }

    /**
     * @return Implement getY method
     */
    public int getY() {
        // don't try to go further
        if (headless)
            return 0;

        if (isInternalized())
            return internalFrame.getY();

        return externalFrame.getY();
    }

    /**
     * @return Implement getY method for internal frame only
     */
    public int getYInternal() {
        // don't try to go further
        if (headless)
            return 0;

        return internalFrame.getY();
    }

    /**
     * @return Implement getY method for external frame only
     */
    public int getYExternal() {
        // don't try to go further
        if (headless)
            return 0;

        return externalFrame.getY();
    }

    /**
     * @return Implement getLocation method
     */
    public Point getLocation() {
        // don't try to go further
        if (headless)
            return new Point();

        if (isInternalized())
            return internalFrame.getLocation();

        return externalFrame.getLocation();
    }

    /**
     * @return Implement getLocation method
     */
    public Point getLocationInternal() {
        // don't try to go further
        if (headless)
            return new Point();

        return internalFrame.getLocation();
    }

    /**
     * @return Implement getLocation method for external frame only
     */
    public Point getLocationExternal() {
        // don't try to go further
        if (headless)
            return new Point();

        return externalFrame.getLocation();
    }

    /**
     * @return Implement getBounds method
     */
    public Rectangle getBounds() {
        // don't try to go further
        if (headless)
            return new Rectangle();

        if (isInternalized())
            return internalFrame.getBounds();

        return externalFrame.getBounds();
    }

    /**
     * @return Implement getBounds method for internal frame only
     */
    public Rectangle getBoundsInternal() {
        // don't try to go further
        if (headless)
            return new Rectangle();

        return internalFrame.getBounds();
    }

    /**
     * @return Implement getBounds method for external frame only
     */
    public Rectangle getBoundsExternal() {
        // don't try to go further
        if (headless)
            return new Rectangle();

        return externalFrame.getBounds();
    }

    /**
     * @return Implement getBounds method for external frame only
     */
    public Rectangle getVisibleRect() {
        // don't try to go further
        if (headless)
            return new Rectangle();

        if (isInternalized())
            return internalFrame.getVisibleRect();

        // not supported on external frame
        if (externalFrame.isVisible())
            return externalFrame.getBounds();

        return new Rectangle();
    }

    /**
     * @return Implement getJMenuBar method
     */
    public JMenuBar getJMenuBar() {
        // don't try to go further
        if (headless)
            return null;

        if (isInternalized())
            return internalFrame.getJMenuBar();

        return externalFrame.getJMenuBar();
    }

    /**
     * @param condition int
     * @return Returns the content pane InputMap
     */
    public InputMap getInputMap(int condition) {
        // don't try to go further
        if (headless)
            return null;

        if (isInternalized())
            return ((JPanel) internalFrame.getContentPane()).getInputMap(condition);

        return ((JPanel) externalFrame.getContentPane()).getInputMap(condition);
    }

    /**
     * @return Returns the content pane InputMap
     */
    public ActionMap getActionMap() {
        // don't try to go further
        if (headless)
            return null;

        if (isInternalized())
            return ((JPanel) internalFrame.getContentPane()).getActionMap();

        return ((JPanel) externalFrame.getContentPane()).getActionMap();
    }

    /**
     * @return Implement getToolkit method
     */
    public Toolkit getToolkit() {
        // don't try to go further
        if (headless)
            return null;

        if (isInternalized())
            return internalFrame.getToolkit();

        return externalFrame.getToolkit();
    }

    /**
     * @return Implement setTitle method
     */
    public String getTitle() {
        // don't try to go further
        if (headless)
            return "";

        if (isInternalized())
            return internalFrame.getTitle();

        return externalFrame.getTitle();
    }

    /**
     * @return Return true if title bar is visible
     */
    public boolean getTitleBarVisible() {
        // don't try to go further
        if (headless)
            return false;

        if (isInternalized())
            return internalFrame.isTitleBarVisible();

        return externalFrame.isTitleBarVisible();
    }

    /**
     * @return the displaySwitchStateItem
     */
    public boolean isSwitchStateItemVisible() {
        // don't try to go further
        if (headless)
            return false;

        return switchStateItemVisible;
    }

    /**
     * @return Implement getMousePosition method
     */
    public Point getMousePosition() {
        // don't try to go further
        if (headless)
            return new Point();

        if (isInternalized())
            return internalFrame.getMousePosition();

        return externalFrame.getMousePosition();
    }

    /**
     * @return Implement isMinimized method
     */
    public boolean isMinimized() {
        // don't try to go further
        if (headless)
            return false;

        if (isInternalized())
            return internalFrame.isIcon();

        return ComponentUtil.isMinimized(externalFrame);
    }

    /**
     * @return Implement isMinimized method for internal frame only
     */
    public boolean isMinimizedInternal() {
        // don't try to go further
        if (headless)
            return false;

        return internalFrame.isIcon();
    }

    /**
     * @return Implement isMinimized method for external frame only
     */
    public boolean isMinimizedExternal() {
        // don't try to go further
        if (headless)
            return false;

        return ComponentUtil.isMinimized(externalFrame);
    }

    /**
     * @return Implement isMaximized method
     */
    public boolean isMaximized() {
        // don't try to go further
        if (headless)
            return false;

        if (isInternalized())
            return internalFrame.isMaximum();

        return ComponentUtil.isMaximized(externalFrame);
    }

    /**
     * @return Implement isMaximized method for internal frame only
     */
    public boolean isMaximizedInternal() {
        // don't try to go further
        if (headless)
            return false;

        return internalFrame.isMaximum();
    }

    /**
     * @return Implement isMaximized method for external frame only
     */
    public boolean isMaximizedExternal() {
        // don't try to go further
        if (headless)
            return false;

        return ComponentUtil.isMaximized(externalFrame);
    }

    /**
     * @return Implement isVisible method
     */
    public boolean isVisible() {
        // don't try to go further
        if (headless)
            return false;

        if (isInternalized())
            return internalFrame.isVisible();

        return externalFrame.isVisible();
    }

    /**
     * @return Implement isResizable method
     */
    public boolean isResizable() {
        // don't try to go further
        if (headless)
            return false;

        if (isInternalized())
            return internalFrame.isResizable();

        return externalFrame.isResizable();
    }

    /**
     * @return Implement isClosable method
     */
    public boolean isClosable() {
        // don't try to go further
        if (headless)
            return false;

        if (isInternalized())
            return internalFrame.isClosable();

        // external frame is always closable
        return true;
    }

    /**
     * @return return true if frame is in internalized state
     */
    public boolean isInternalized() {
        return (state == IcyFrameState.INTERNALIZED);
    }

    /**
     * @return return true if frame is in externalized state
     */
    public boolean isExternalized() {
        return (state == IcyFrameState.EXTERNALIZED);
    }

    /**
     * @return return true if frame is active
     */
    public boolean isActive() {
        // don't try to go further
        if (headless)
            return false;

        if (isInternalized())
            return internalFrame.isSelected();

        return externalFrame.isActive();
    }

    /**
     * @return Implement isAlwaysOnTop method (only for externalized frame)
     */
    public boolean isAlwaysOnTop() {
        // don't try to go further
        if (headless)
            return false;

        return externalFrame.isAlwaysOnTop();
    }

    /**
     * @return Implement hasFocus method
     */
    public boolean hasFocus() {
        // don't try to go further
        if (headless)
            return false;

        if (isInternalized())
            return internalFrame.hasFocus();

        return externalFrame.hasFocus();
    }

    /**
     * Implement setTitle method
     *
     * @param title string
     */
    public void setTitle(final String title) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            internalFrame.setTitle(title);
            externalFrame.setTitle(title);
        }, syncProcess);
    }

    /**
     * Implement setToolTipText method (only for internalized frame)
     *
     * @param text string
     */
    public void setToolTipText(final String text) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            // only internal frame support it
            internalFrame.setToolTipText(text);
            // externalFrame.setToolTipText(text);
        }, syncProcess);
    }

    /**
     * Implement setBackground method
     *
     * @param value color
     */
    public void setBackground(final Color value) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            internalFrame.setBackground(value);
            externalFrame.setBackground(value);
        }, syncProcess);
    }

    /**
     * Implement setForeground method
     *
     * @param value color
     */
    public void setForeground(final Color value) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            internalFrame.setForeground(value);
            externalFrame.setForeground(value);
        }, syncProcess);
    }

    /**
     * Implement setResizable method
     *
     * @param value boolean
     */
    public void setResizable(final boolean value) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            internalFrame.setResizable(value);
            externalFrame.setResizable(value);
        }, syncProcess);

    }

    /**
     * Implement setLocation method
     *
     * @param p point
     */
    public void setLocation(final Point p) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.setLocation(p);
            else
                externalFrame.setLocation(p);
        }, syncProcess);
    }

    /**
     * Implement setLocation method
     *
     * @param x int
     * @param y int
     */
    public void setLocation(final int x, final int y) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.setLocation(x, y);
            else
                externalFrame.setLocation(x, y);
        }, syncProcess);
    }

    /**
     * Implement setLocation method for internal frame only
     *
     * @param p point
     */
    public void setLocationInternal(final Point p) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> internalFrame.setLocation(p), syncProcess);
    }

    /**
     * Implement setLocation method for internal frame only
     *
     * @param x int
     * @param y int
     */
    public void setLocationInternal(final int x, final int y) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> internalFrame.setLocation(x, y), syncProcess);
    }

    /**
     * Implement setLocation method for external frame only
     *
     * @param p point
     */
    public void setLocationExternal(final Point p) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> externalFrame.setLocation(p), syncProcess);
    }

    /**
     * Implement setLocation method for external frame only
     *
     * @param x int
     * @param y int
     */
    public void setLocationExternal(final int x, final int y) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> externalFrame.setLocation(x, y), syncProcess);
    }

    /**
     * Implement setSize method
     *
     * @param d dimension
     */
    public void setSize(final Dimension d) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.setSize(d);
            else
                externalFrame.setSize(d);
        }, syncProcess);
    }

    /**
     * Implement setSize method
     *
     * @param width  int
     * @param height int
     */
    public void setSize(final int width, final int height) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.setSize(width, height);
            else
                externalFrame.setSize(width, height);
        }, syncProcess);
    }

    /**
     * Implement setSize method for internal frame only
     *
     * @param d dimension
     */
    public void setSizeInternal(final Dimension d) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> internalFrame.setSize(d), syncProcess);
    }

    /**
     * Implement setSize method for internal frame only
     *
     * @param width  int
     * @param height int
     */
    public void setSizeInternal(final int width, final int height) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> internalFrame.setSize(width, height), syncProcess);
    }

    /**
     * Implement setSize method for external frame only
     *
     * @param d diemension
     */
    public void setSizeExternal(final Dimension d) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> externalFrame.setSize(d), syncProcess);
    }

    /**
     * Implement setSize method for external frame only
     *
     * @param width  int
     * @param height int
     */
    public void setSizeExternal(final int width, final int height) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> externalFrame.setSize(width, height), syncProcess);
    }

    /**
     * Implement setPreferredSize method
     *
     * @param d dimension
     */
    public void setPreferredSize(final Dimension d) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.setPreferredSize(d);
            else
                externalFrame.setPreferredSize(d);
        }, syncProcess);
    }

    /**
     * Implement setPreferredSize method for internal frame only
     *
     * @param d dimension
     */
    public void setPreferredSizeInternal(final Dimension d) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> internalFrame.setPreferredSize(d), syncProcess);
    }

    /**
     * Implement setPreferredSize method for external frame only
     *
     * @param d dimension
     */
    public void setPreferredSizeExternal(final Dimension d) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> externalFrame.setPreferredSize(d), syncProcess);
    }

    /**
     * Implement setMinimumSize method
     *
     * @param d dimension
     */
    public void setMinimumSize(final Dimension d) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.setMinimumSize(d);
            else
                externalFrame.setMinimumSize(d);
        }, syncProcess);
    }

    /**
     * Implement setMaximumSize method
     *
     * @param d dimension
     */
    public void setMaximumSize(final Dimension d) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.setMaximumSize(d);
            else
                externalFrame.setMaximumSize(d);
        }, syncProcess);
    }

    /**
     * Implement setMinimumSize method for internal frame only
     *
     * @param d diemension
     */
    public void setMinimumSizeInternal(final Dimension d) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> internalFrame.setMinimumSize(d), syncProcess);
    }

    /**
     * Implement setMaximumSize method for internal frame only
     *
     * @param d diemension
     */
    public void setMaximumSizeInternal(final Dimension d) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> internalFrame.setMaximumSize(d), syncProcess);
    }

    /**
     * Implement setMinimumSize method for external frame only
     *
     * @param d dimension
     */
    public void setMinimumSizeExternal(final Dimension d) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> externalFrame.setMinimumSize(d), syncProcess);
    }

    /**
     * Implement setMaximumSize method for external frame only
     *
     * @param d dimension
     */
    public void setMaximumSizeExternal(final Dimension d) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> externalFrame.setMaximumSize(d), syncProcess);
    }

    /**
     * Implement setBounds method
     *
     * @param r rectangle
     */
    public void setBounds(final Rectangle r) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.setBounds(r);
            else
                externalFrame.setBounds(r);
        }, syncProcess);

    }

    /**
     * Implement setMaximisable method
     *
     * @param value boolean
     */
    public void setMaximisable(final boolean value) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            // only for internal frame
            internalFrame.setMaximizable(value);
        }, syncProcess);
    }

    /**
     * Implement setMinimized method
     *
     * @param value boolean
     */
    public void setMinimized(final boolean value) {
        // don't try to go further
        if (headless)
            return;

        // only relevant if state changed
        if (isMinimized() ^ value) {
            // AWT safe
            ThreadUtil.invoke(() -> {
                if (isInternalized())
                    internalFrame.setMinimized(value);
                else
                    externalFrame.setMinimized(value);
            }, syncProcess);
        }
    }

    /**
     * Implement setMinimized method for internal frame only
     *
     * @param value boolean
     */
    public void setMinimizedInternal(final boolean value) {
        // don't try to go further
        if (headless)
            return;

        // only relevant if state changed
        if (internalFrame.isMinimized() ^ value) {
            // AWT safe
            ThreadUtil.invoke(() -> internalFrame.setMinimized(value), syncProcess);
        }
    }

    /**
     * Implement setMinimized method for external frame only
     *
     * @param value boolean
     */
    public void setMinimizedExternal(final boolean value) {
        // don't try to go further
        if (headless)
            return;

        // only relevant if state changed
        if (externalFrame.isMinimized() ^ value) {
            // AWT safe
            ThreadUtil.invoke(() -> externalFrame.setMinimized(value), syncProcess);
        }
    }

    /**
     * Implement setMaximized method
     *
     * @param value boolean
     */
    public void setMaximized(final boolean value) {
        // don't try to go further
        if (headless)
            return;

        // only relevant if state changed
        if (isMaximized() ^ value) {
            // AWT safe
            ThreadUtil.invoke(() -> {
                if (isInternalized())
                    internalFrame.setMaximized(value);
                else
                    externalFrame.setMaximized(value);
            }, syncProcess);
        }
    }

    /**
     * Implement setMaximized method for internal frame only
     *
     * @param value boolean
     */
    public void setMaximizedInternal(final boolean value) {
        // don't try to go further
        if (headless)
            return;

        // only relevant if state changed
        if (internalFrame.isMaximized() ^ value) {
            // AWT safe
            ThreadUtil.invoke(() -> internalFrame.setMaximized(value), syncProcess);
        }
    }

    /**
     * Implement setMaximized method for external frame only
     *
     * @param value boolean
     */
    public void setMaximizedExternal(final boolean value) {
        // don't try to go further
        if (headless)
            return;

        // only relevant if state changed
        if (externalFrame.isMaximized() ^ value) {
            // AWT safe
            ThreadUtil.invoke(() -> externalFrame.setMaximized(value), syncProcess);
        }
    }

    /**
     * Implement setClosable method
     *
     * @param value boolean
     */
    public void setClosable(final boolean value) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> internalFrame.setClosable(value), syncProcess);
    }

    /**
     * Implement setDefaultCloseOperation method
     *
     * @param operation int
     * @see JFrame#setDefaultCloseOperation(int)
     */
    public void setDefaultCloseOperation(final int operation) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            internalFrame.setDefaultCloseOperation(operation);
            externalFrame.setDefaultCloseOperation(operation);
        }, syncProcess);
    }

    /**
     * Implement setFocusable method
     *
     * @param value boolean
     */
    public void setFocusable(final boolean value) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            internalFrame.setFocusable(value);
            externalFrame.setFocusable(value);
        }, syncProcess);
    }

    /**
     * Implement setVisible method
     *
     * @param value boolean
     */
    public void setVisible(final boolean value) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.setVisible(value);
            else
                externalFrame.setVisible(value);
        }, syncProcess);
    }

    /**
     * Implement setAlwaysOnTop method (only for externalized frame)
     *
     * @param alwaysOnTop boolean
     */
    public void setAlwaysOnTop(final boolean alwaysOnTop) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> externalFrame.setAlwaysOnTop(alwaysOnTop), syncProcess);
    }

    /**
     * Implement setJMenuBar method
     *
     * @param m JmenuBar
     */
    public void setJMenuBar(final JMenuBar m) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.setJMenuBar(m);
            else
                externalFrame.setJMenuBar(m);
        }, syncProcess);
    }

    /**
     * Hide or show the title bar (frame should not be displayable when you set this property)
     *
     * @param value boolean
     */
    public void setTitleBarVisible(final boolean value) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            internalFrame.setTitleBarVisible(value);
            externalFrame.setTitleBarVisible(value);
        }, syncProcess);
    }

    /**
     * Implement setLayout method
     *
     * @param layout Layout manager
     */
    public void setLayout(final LayoutManager layout) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.setLayout(layout);
            else
                externalFrame.setLayout(layout);
        }, syncProcess);
    }

    /**
     * Implement setBorder method (only for internal frame)
     *
     * @param border border
     */
    public void setBorder(final Border border) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> internalFrame.setBorder(border), syncProcess);
    }

    /**
     * Implement setContentPane method
     *
     * @param value container
     */
    public void setContentPane(final Container value) {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.setContentPane(value);
            else
                externalFrame.setContentPane(value);
        }, syncProcess);
    }

    /**
     * @return the syncProcess
     */
    public boolean isSyncProcess() {
        return syncProcess;
    }

    /**
     * By default IcyFrame does asych processing, you can force sync processing<br>
     * with this property
     *
     * @param syncProcess the syncProcess to set
     */
    public void setSyncProcess(boolean syncProcess) {
        this.syncProcess = syncProcess;
    }

    /**
     * Frame becomes the active/focused frame
     */
    public void requestFocus() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized()) {
                try {
                    internalFrame.setSelected(true);
                }
                catch (PropertyVetoException e) {
                    // ignore
                }
            }
            else
                externalFrame.requestFocus();
        }, syncProcess);
    }

    /**
     * Implement validate
     */
    public void validate() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.validate();
            else
                externalFrame.validate();
        }, syncProcess);
    }

    /**
     * Implement revalidate
     */
    public void revalidate() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.revalidate();
            else {
                externalFrame.invalidate();
                externalFrame.repaint();
            }
        }, syncProcess);
    }

    /**
     * Implement repaint
     */
    public void repaint() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> {
            if (isInternalized())
                internalFrame.repaint();
            else
                externalFrame.repaint();
        }, syncProcess);
    }

    /**
     * Implement updateUI
     */
    public void updateUI() {
        // don't try to go further
        if (headless)
            return;

        // AWT safe
        ThreadUtil.invoke(() -> internalFrame.updateUI(), syncProcess);
    }

    /**
     * Fire frame activated event
     *
     * @param e Icy frame event
     */
    private void fireFrameActivated(IcyFrameEvent e) {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameActivated(e);
    }

    /**
     * Fire frame deactivated event
     *
     * @param e Icy frame event
     */
    private void fireFrameDeactivated(IcyFrameEvent e) {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameDeactivated(e);
    }

    /**
     * Fire frame closing event
     *
     * @param e Icy frame event
     */
    private void fireFrameClosing(IcyFrameEvent e) {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameClosing(e);
    }

    /**
     * Fire frame closed event
     *
     * @param e Icy frame event
     */
    private void fireFrameClosed(IcyFrameEvent e) {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameClosed(e);
    }

    /**
     * Fire frame iconified event
     *
     * @param e Icy frame event
     */
    private void fireFrameIconified(IcyFrameEvent e) {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameIconified(e);
    }

    /**
     * Fire frame deiconified event
     *
     * @param e Icy frame event
     */
    private void fireFrameDeiconified(IcyFrameEvent e) {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameDeiconified(e);
    }

    /**
     * Fire frame opened event
     *
     * @param e Icy frame event
     */
    private void fireFrameOpened(IcyFrameEvent e) {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameOpened(e);
    }

    /**
     * Fire frame internalized event
     *
     * @param e Icy frame event
     */
    void fireFrameInternalized(IcyFrameEvent e) {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameInternalized(e);
    }

    /**
     * Fire frame externalized event
     *
     * @param e Icy frame event
     */
    void fireFrameExternalized(IcyFrameEvent e) {
        for (IcyFrameListener l : frameEventListeners.getListeners(IcyFrameListener.class))
            l.icyFrameExternalized(e);
    }

    /**
     * Implement addFrameListener method
     *
     * @param l Icy frame listener
     */
    public void addFrameListener(IcyFrameListener l) {
        // don't try to go further
        if (headless)
            return;

        frameEventListeners.add(IcyFrameListener.class, l);
    }

    /**
     * Implement removeFrameListener method
     *
     * @param l Icy frame listener
     */
    public void removeFrameListener(IcyFrameListener l) {
        // don't try to go further
        if (headless)
            return;

        frameEventListeners.remove(IcyFrameListener.class, l);
    }

    /**
     * Implement addComponentListener method
     *
     * @param l Icy frame listener
     */
    public void addComponentListener(ComponentListener l) {
        // don't try to go further
        if (headless)
            return;

        internalFrame.addComponentListener(l);
        externalFrame.addComponentListener(l);
    }

    /**
     * Implement removeComponentListener method
     *
     * @param l Icy frame listener
     */
    public void removeComponentListener(ComponentListener l) {
        // don't try to go further
        if (headless)
            return;

        internalFrame.removeComponentListener(l);
        externalFrame.removeComponentListener(l);
    }

    /**
     * Implement addKeyListener method
     *
     * @param l Icy frame listener
     */
    public void addKeyListener(KeyListener l) {
        // don't try to go further
        if (headless)
            return;

        internalFrame.addKeyListener(l);
        externalFrame.addKeyListener(l);
    }

    /**
     * Implement addKeyListener method
     *
     * @param l Icy frame listener
     */
    public void removeKeyListener(KeyListener l) {
        // don't try to go further
        if (headless)
            return;

        internalFrame.removeKeyListener(l);
        externalFrame.removeKeyListener(l);
    }

    /**
     * internal close stuff
     *
     * @param e event
     */
    public void frameClosed(AWTEvent e) {
        // don't try to go further
        if (headless)
            return;

        final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();
        // remove listener on main frame mode change
        if (mainFrame != null)
            mainFrame.removePropertyChangeListener(MainFrame.PROPERTY_DETACHEDMODE, this);
        // remove others listeners
        externalFrame.removeWindowListener(IcyFrame.this);
        internalFrame.removeInternalFrameListener(IcyFrame.this);

        if (e instanceof InternalFrameEvent) {
            fireFrameClosed(new IcyFrameEvent(this, (InternalFrameEvent) e, null));
            // don't forget to close external frame
            externalFrame.dispose();
        }
        else if (e instanceof WindowEvent) {
            fireFrameClosed(new IcyFrameEvent(this, null, (WindowEvent) e));
            // don't forget to close internal frame
            internalFrame.dispose();
        }

        // easy onClosed handling
        onClosed();
    }

    /**
     * easy onClosed job
     */
    public void onClosed() {
        // don't try to go further
        if (headless)
            return;

        // unregister from list
        synchronized (frames) {
            frames.remove(this);
        }

        // release some stuff else we have cycling reference
        externalFrame.systemMenuCallback = null;
        internalFrame.systemMenuCallback = null;
        switchStateAction = null;
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {
        // don't try to go further
        if (headless)
            return;

        fireFrameActivated(new IcyFrameEvent(this, e, null));
    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
        // don't try to go further
        if (headless)
            return;

        frameClosed(e);
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        // don't try to go further
        if (headless)
            return;

        fireFrameClosing(new IcyFrameEvent(this, e, null));
    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
        // don't try to go further
        if (headless)
            return;

        fireFrameDeactivated(new IcyFrameEvent(this, e, null));
    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {
        // don't try to go further
        if (headless)
            return;

        fireFrameDeiconified(new IcyFrameEvent(this, e, null));
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {
        // don't try to go further
        if (headless)
            return;

        fireFrameIconified(new IcyFrameEvent(this, e, null));
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
        // don't try to go further
        if (headless)
            return;

        fireFrameOpened(new IcyFrameEvent(this, e, null));
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // don't try to go further
        if (headless)
            return;

        fireFrameActivated(new IcyFrameEvent(this, null, e));
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // don't try to go further
        if (headless)
            return;

        frameClosed(e);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        // don't try to go further
        if (headless)
            return;

        fireFrameClosing(new IcyFrameEvent(this, null, e));
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // don't try to go further
        if (headless)
            return;

        fireFrameDeactivated(new IcyFrameEvent(this, null, e));
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // don't try to go further
        if (headless)
            return;

        fireFrameDeiconified(new IcyFrameEvent(this, null, e));
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // don't try to go further
        if (headless)
            return;

        fireFrameIconified(new IcyFrameEvent(this, null, e));
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // don't try to go further
        if (headless)
            return;

        fireFrameOpened(new IcyFrameEvent(this, null, e));
    }

    @Override
    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        // don't try to go further
        if (headless)
            return false;

        if (isInternalized())
            return internalFrame.imageUpdate(img, infoflags, x, y, width, height);

        return externalFrame.imageUpdate(img, infoflags, x, y, width, height);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // don't try to go further
        if (headless)
            return;

        if (StringUtil.equals(evt.getPropertyName(), MainFrame.PROPERTY_DETACHEDMODE)) {
            // window mode has been changed
            final boolean detachedMode = ((Boolean) evt.getNewValue()).booleanValue();

            // detached mode set --> externalize
            if (detachedMode) {
                // save previous state
                previousState = state;
                externalize();
                // disable switch state item
                if (switchStateAction != null)
                    switchStateAction.setEnabled(false);
            }
            else {
                // restore previous state
                if (previousState == IcyFrameState.INTERNALIZED)
                    internalize();
                // enable switch state item
                if (switchStateAction != null)
                    switchStateAction.setEnabled(true);
            }
        }
    }
}
