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

package fr.icy.gui.component.panel;

import fr.icy.Icy;
import fr.icy.common.listener.weak.WeakListener;
import fr.icy.common.string.StringUtil;
import fr.icy.gui.WindowPositionSaver;
import fr.icy.gui.frame.IcyFrame;
import fr.icy.gui.frame.IcyFrameAdapter;
import fr.icy.gui.frame.IcyFrameEvent;

import javax.swing.*;
import java.awt.*;
import java.util.EventListener;

/**
 * Externalizable panel component.<br>
 * Basically this is a JPanel you can externalize and display in an IcyFrame.<br>
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
@Deprecated(since = "3.0.0", forRemoval = true)
public class ExternalizablePanel extends JPanel {
    public static class WeakStateListener extends WeakListener<StateListener> implements StateListener {
        public WeakStateListener(final StateListener listener) {
            super(listener);
        }

        @Override
        public void removeListener(final Object source) {
            if (source != null)
                ((ExternalizablePanel) source).removeStateListener(this);
        }

        @Override
        public void stateChanged(final ExternalizablePanel source, final boolean externalized) {
            final StateListener listener = getListener(source);

            if (listener != null)
                listener.stateChanged(source, externalized);
        }
    }

    @FunctionalInterface
    public interface StateListener extends EventListener {
        void stateChanged(ExternalizablePanel source, boolean externalized);
    }

    public class Frame extends IcyFrame {
        public Frame(final String title) throws HeadlessException {
            super(title, true, true, true, true);

            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            addFrameListener(new IcyFrameAdapter() {
                @Override
                public void icyFrameClosing(final IcyFrameEvent e) {
                    super.icyFrameClosing(e);

                    // ignore the event when frame is manually closed or application is exiting
                    if (!(closed || Icy.isExiting())) {
                        if (ExternalizablePanel.this.isExternalized())
                            ExternalizablePanel.this.internalizeInternal();
                    }
                }
            });

            setLayout(new BorderLayout());
            setSize(400, 400);
        }
    }

    /**
     * extern frame
     */
    protected final Frame frame;
    /**
     * parent component
     */
    private Container parent;

    /**
     * internals
     */
    private boolean internalizationAuthorized;
    private boolean externalizationAuthorized;
    boolean closed;

    // we need to keep reference on it as the object only use weak reference
    final WindowPositionSaver positionSaver;

    /**
     * Create a new externalizable panel.
     *
     * @param title  title for the associated frame.
     * @param key    save key, used for WindowPositionSaver.<br>
     *               Set to null or empty string disable parameter saving.
     * @param defLoc the default location for the frame (externalized state)
     * @param defDim the default dimension for the frame (externalized state)
     */
    public ExternalizablePanel(final String title, final String key, final Point defLoc, final Dimension defDim) {
        super();

        frame = new Frame(title);
        parent = null;
        internalizationAuthorized = true;
        externalizationAuthorized = true;
        closed = false;

        // use window position saver with default parameters
        if (!StringUtil.isEmpty(key))
            positionSaver = new WindowPositionSaver(this, "frame/" + key, defLoc, defDim);
        else
            positionSaver = null;
    }

    /**
     * Create a new externalizable panel.
     *
     * @param title title for the associated frame.
     * @param key   save key, used for WindowPositionSaver.<br>
     *              Set to null or empty string disable parameter saving.
     */
    public ExternalizablePanel(final String title, final String key) {
        // default location and dimension for extern frame
        this(title, key, new Point(200, 200), new Dimension(400, 300));
    }

    public ExternalizablePanel(final String title) {
        this(title, null);
    }

    public ExternalizablePanel() {
        this("", null);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        // set parent on first attachment
        if (parent == null) {
            final Container p = getParent();

            if ((p != frame.getInternalFrame().getContentPane()) && (p != frame.getExternalFrame().getContentPane()))
                parent = p;
        }
    }

    /**
     * Close the panel (close and release associated frames and resources).
     */
    public void close() {
        closed = true;
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.close();
    }

    /**
     * Manual parent set
     */
    public void setParent(final Container value) {
        parent = value;
    }

    /**
     * @return the internalizationAuthorized
     */
    public boolean isInternalizationAuthorized() {
        return internalizationAuthorized;
    }

    /**
     * @param internalizationAuthorized the internalizationAuthorized to set
     */
    public void setInternalizationAuthorized(final boolean internalizationAuthorized) {
        this.internalizationAuthorized = internalizationAuthorized;
    }

    /**
     * @return the externalizationAuthorized
     */
    public boolean isExternalizationAuthorized() {
        return externalizationAuthorized;
    }

    /**
     * @param externalizationAuthorized the externalizationAuthorized to set
     */
    public void setExternalizationAuthorized(final boolean externalizationAuthorized) {
        this.externalizationAuthorized = externalizationAuthorized;
    }

    /**
     * Externalize panel in an independent frame
     */
    public void externalize() {
        if (isInternalized())
            externalizeInternal();
    }

    /**
     * Internalize panel (remove from independent frame)
     */
    public void internalize() {
        if (isExternalized())
            internalizeInternal();
    }

    /**
     * Externalize panel (internal method)
     */
    void externalizeInternal() {
        if (!externalizationAuthorized)
            return;

        // externalize
        if (parent != null) {
            parent.remove(this);
            parent.validate();
        }

        frame.add(this, BorderLayout.CENTER);
        frame.validate();
        frame.addToDesktopPane();
        frame.setVisible(true);

        // notify
        fireStateChange(true);
    }

    /**
     * Internalize panel (internal method)
     */
    void internalizeInternal() {
        if (!internalizationAuthorized)
            return;

        // internalize
        frame.setVisible(false);
        frame.remove(this);
        frame.validate();
        frame.removeFromMainDesktopPane();

        if (parent != null) {
            parent.add(this);
            parent.validate();
        }

        // notify
        fireStateChange(false);
    }

    /**
     * Switch from internalized &lt;--&gt; externalized state and vice versa
     */
    public void switchState() {
        if (isExternalized())
            internalizeInternal();
        else
            externalizeInternal();
    }

    public boolean isInternalized() {
        return !frame.isVisible();
    }

    public boolean isExternalized() {
        return frame.isVisible();
    }

    /**
     * @return the frame
     */
    public IcyFrame getFrame() {
        return frame;
    }

    // /**
    // * Implement getMinimumSize method for external frame only
    // */
    // public Dimension getMinimumSizeExternal()
    // {
    // return frame.getMinimumSize();
    // }
    //
    // /**
    // * Implement getMaximumSize method for external frame only
    // */
    // public Dimension getMaximumSizeExternal()
    // {
    // return frame.getMaximumSize();
    // }
    //
    // /**
    // * Implement getPreferredSize method for external frame only
    // */
    // public Dimension getPreferredSizeExternal()
    // {
    // return frame.getPreferredSize();
    // }
    //
    // /**
    // * Implement getSize method for external frame only
    // */
    // public Dimension getSizeExternal()
    // {
    // return frame.getSize();
    // }
    //
    // /**
    // * Implement getHeight method for external frame only
    // */
    // public int getHeightExternal()
    // {
    // return frame.getHeight();
    // }
    //
    // /**
    // * Implement getWidth method for external frame only
    // */
    // public int getWidthExternal()
    // {
    // return frame.getWidth();
    // }
    //
    // /**
    // * Implement getLocation method for external frame only
    // */
    // public Point getLocationExternal()
    // {
    // return frame.getLocation();
    // }
    //
    // /**
    // * Implement getBounds method for external frame only
    // */
    // public Rectangle getBoundsExternal()
    // {
    // return frame.getBounds();
    // }
    //
    // /**
    // * Implement setLocation method for external frame only
    // */
    // public void setLocationExternal(final Point p)
    // {
    // frame.setLocation(p);
    // }
    //
    // /**
    // * Implement setLocation method for external frame only
    // */
    // public void setLocationExternal(final int x, final int y)
    // {
    // frame.setLocation(x, y);
    // }
    //
    // /**
    // * Implement setSize method for external frame only
    // */
    // public void setSizeExternal(final Dimension d)
    // {
    // frame.setSize(d);
    // }
    //
    // /**
    // * Implement setSize method for external frame only
    // */
    // public void setSizeExternal(final int width, final int height)
    // {
    // frame.setSize(width, height);
    // }
    //
    // /**
    // * Implement setPreferredSize method for external frame only
    // */
    // public void setPreferredSizeExternal(final Dimension d)
    // {
    // frame.setPreferredSize(d);
    // }
    //
    // /**
    // * Implement setMinimumSize method for external frame only
    // */
    // public void setMinimumSizeExternal(final Dimension d)
    // {
    // frame.setMinimumSize(d);
    // }
    //
    // /**
    // * Implement setMaximumSize method for external frame only
    // */
    // public void setMaximumSizeExternal(final Dimension d)
    // {
    // frame.setMaximumSize(d);
    // }

    /**
     * Fire state change event
     */
    private void fireStateChange(final boolean externalized) {
        for (StateListener l : listenerList.getListeners(StateListener.class))
            l.stateChanged(this, externalized);
    }

    /**
     * Implement addFrameListener method
     */
    public void addStateListener(final StateListener l) {
        listenerList.add(StateListener.class, l);
    }

    /**
     * Implement removeFrameListener method
     */
    public void removeStateListener(final StateListener l) {
        listenerList.remove(StateListener.class, l);
    }
}
