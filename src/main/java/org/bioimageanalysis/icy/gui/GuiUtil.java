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

package org.bioimageanalysis.icy.gui;

import org.bioimageanalysis.icy.gui.component.ComponentUtil;
import org.bioimageanalysis.icy.gui.frame.IcyFrame;
import org.bioimageanalysis.icy.gui.frame.IcyFrameAdapter;
import org.bioimageanalysis.icy.gui.frame.IcyFrameEvent;
import org.bioimageanalysis.icy.gui.frame.TitledFrame;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Objects;

/**
 * This class is a toolbox with many simple GUI routines.
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class GuiUtil {
    public static JPanel createLoweredPanel(final Component comp) {
        final JPanel result = new JPanel();

        result.setBorder(BorderFactory.createLoweredBevelBorder());
        if (comp != null) {
            result.setLayout(new BorderLayout());
            result.add(comp, BorderLayout.CENTER);
        }
        result.validate();

        return result;
    }

    public static JPanel createRaisedPanel(final Component comp) {
        final JPanel result = new JPanel();

        result.setBorder(BorderFactory.createRaisedBevelBorder());
        if (comp != null) {
            result.setLayout(new BorderLayout());
            result.add(comp, BorderLayout.CENTER);
        }
        result.validate();

        return result;
    }

    public static JLabel createBoldLabel(final String text) {
        final JLabel label = new JLabel(text);

        ComponentUtil.setFontBold(label);

        return label;
    }

    public static JLabel createBigBoldLabel(final String text, final int incSize) {
        final JLabel label = createBoldLabel(text);

        ComponentUtil.increaseFontSize(label, incSize);

        return label;
    }

    public static JPanel createCenteredLabel(final String text) {
        return createCenteredLabel(new JLabel(text));
    }

    public static JPanel createCenteredLabel(final JLabel label) {
        return createLineBoxPanel(Box.createHorizontalGlue(), label, Box.createHorizontalGlue());
    }

    public static JPanel createCenteredBoldLabel(final String text) {
        return createCenteredLabel(createBoldLabel(text));
    }

    public static JLabel createFixedWidthLabel(final String text, final int w) {
        final JLabel result = new JLabel(text);

        ComponentUtil.setFixedWidth(result, w);

        return result;
    }

    public static JLabel createFixedWidthBoldLabel(final String text, final int w) {
        final JLabel result = createBoldLabel(text);

        ComponentUtil.setFixedWidth(result, w);

        return result;
    }

    public static JLabel createFixedWidthRightAlignedLabel(final String text, final int w) {
        final JLabel result = new JLabel(text);

        ComponentUtil.setFixedWidth(result, w);
        result.setHorizontalAlignment(SwingConstants.RIGHT);

        return result;
    }

    public static JPanel createTabLabel(final String text, final int width) {
        return createTabLabel(new JLabel(text), width);
    }

    public static JPanel createTabLabel(final JLabel label, final int width) {
        label.setVerticalTextPosition(SwingConstants.TOP);
        label.setHorizontalTextPosition(SwingConstants.LEADING);

        final JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        if (width > 0)
            panel.add(Box.createHorizontalStrut(width));
        panel.add(label);
        panel.add(Box.createHorizontalGlue());
        panel.validate();

        return panel;
    }

    public static JPanel createTabBoldLabel(final String text, final int width) {
        return createTabLabel(createBoldLabel(text), width);
    }

    public static JPanel createTabArea(final String text, final int width) {
        return createTabArea(new JTextArea(text), width);
    }

    public static JPanel createTabArea(final JTextArea area, final int width) {
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);

        final JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        if (width > 0)
            panel.add(Box.createHorizontalStrut(width));
        panel.add(area);
        panel.validate();

        return panel;
    }

    public static JPanel createTabArea(final String text, final int width, final int height) {
        return createTabArea(new JTextArea(text), width, height);
    }

    public static JPanel createTabArea(final JTextArea area, final int width, final int height) {
        area.setEditable(false);
        area.setOpaque(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);

        final JScrollPane scrollArea = new JScrollPane(area);
        scrollArea.setPreferredSize(new Dimension(320, height));
        scrollArea.setBorder(null);

        final JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        if (width > 0)
            panel.add(Box.createHorizontalStrut(width));
        panel.add(scrollArea);
        panel.validate();

        return panel;
    }

    public static JPanel createLineBoxPanel(final Component... componentArray) {
        final JPanel result = new JPanel();

        result.setLayout(new BoxLayout(result, BoxLayout.LINE_AXIS));
        for (final Component c : componentArray)
            result.add(c);
        result.validate();

        return result;
    }

    public static JPanel createPageBoxPanel(final Component... componentArray) {
        final JPanel result = new JPanel();

        result.setLayout(new BoxLayout(result, BoxLayout.PAGE_AXIS));
        for (final Component c : componentArray)
            result.add(c);
        result.validate();

        return result;
    }

    /**
     * Creates a jpanel with a gridlayout of 1,2 with the given arguments, and
     * force the width of the secon columns. Should be use for list of label
     * beside parameters
     */
    public static JPanel besidesPanel(final Component jc1, final Component jc2, final int widthOfSecondComponent) {
        final JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());
        panel.add(jc1, BorderLayout.CENTER);
        panel.add(jc2, BorderLayout.EAST);
        jc2.setPreferredSize(new Dimension(widthOfSecondComponent, jc2.getPreferredSize().height));
        panel.validate();

        return panel;
    }

    /**
     * Creates a jpanel with a gridlayout of 1,2 with the given arguments.
     */
    public static JPanel besidesPanel(final Component... componentArray) {
        final JPanel panel = new JPanel();

        panel.setLayout(new GridLayout(1, componentArray.length));
        for (final Component component : componentArray)
            panel.add(component);
        panel.validate();

        return panel;
    }

    /**
     * This generate a panel with an empty border on the side, so that it is
     * quite pretty. Generated with a boxLayout
     *
     * @return a JPanel
     */
    public static JPanel generatePanel() {
        final JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        return panel;
    }

    public static JPanel generatePanel(final String string) {
        final JPanel panel = generatePanel();

        panel.setBorder(new TitledBorder(string));
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        return panel;
    }

    public static JPanel generatePanelWithoutBorder() {
        final JPanel panel = new JPanel();

        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        return panel;
    }

    public static TitledFrame generateTitleFrame(final String title, final JPanel panel, final Dimension titleDimension, final boolean resizable, final boolean closable, final boolean maximizable, final boolean iconifiable) {
        final Dimension dim;

        dim = Objects.requireNonNullElseGet(titleDimension, () -> new Dimension(400, 70));

        final TitledFrame result = new TitledFrame(title, dim, resizable, closable, maximizable, iconifiable);

        result.getMainPanel().add(panel);
        result.setVisible(true);

        return result;
    }

    /**
     * @param window
     *        the window to convert in IcyFrame
     * @return an IcyFrame with the content of specified window (same properties and components)<br>
     *         The returned frame windows event (opened, closing, closed) are forwarded to the original window to
     *         maintain original event behaviors<br>
     *         Only the <code>closed</code> event is listened from the original window which will automatically call the
     *         close() method of the returned IcyFrame.
     */
    public static IcyFrame createIcyFrameFromWindow(final Window window) {
        final String title;
        final Component content;
        final JMenuBar menuBar;

        if (window instanceof final Frame f) {
            title = f.getTitle();

            if (f instanceof JFrame) {
                content = ((JFrame) f).getContentPane();
                menuBar = ((JFrame) f).getJMenuBar();
            }
            else {
                content = f.getComponent(0);
                menuBar = SwingUtil.getJMenuBar(f.getMenuBar(), false);
            }
        }
        else if (window instanceof final Dialog d) {
            title = d.getTitle();

            if (d instanceof JDialog) {
                content = ((JDialog) d).getContentPane();
                menuBar = ((JDialog) d).getJMenuBar();
            }
            else {
                content = d.getComponent(0);
                menuBar = null;
            }
        }
        else {
            title = window.getName();
            content = window.getComponent(0);
            menuBar = null;
        }

        final IcyFrame frame = new IcyFrame(title, true, true, false, false);
        frame.setLayout(new BorderLayout());
        frame.add(content, BorderLayout.CENTER);
        frame.setJMenuBar(menuBar);

        // keep this property
        if (window instanceof JFrame)
            frame.setDefaultCloseOperation(((JFrame) window).getDefaultCloseOperation());
        else if (window instanceof JDialog)
            frame.setDefaultCloseOperation(((JDialog) window).getDefaultCloseOperation());

        frame.pack();
        frame.getIcyExternalFrame().setSize(window.getSize());
        frame.getIcyInternalFrame().setSize(window.getSize());
        frame.center();

        frame.setFocusable(window.isFocusable());
        frame.setResizable(false);

        frame.addFrameListener(new IcyFrameAdapter() {
            @Override
            public void icyFrameOpened(final IcyFrameEvent e) {
                for (final WindowListener l : window.getWindowListeners())
                    l.windowOpened(new WindowEvent(window, e.getEvent().getID()));
            }

            @Override
            public void icyFrameClosing(final IcyFrameEvent e) {
                // ensure we are not doing recursing 'close' calls
                if (window.isVisible()) {
                    window.setLocation(frame.getLocation());
                    for (final WindowListener l : window.getWindowListeners())
                        l.windowClosing(new WindowEvent(window, e.getEvent().getID()));
                }
            }

            @Override
            public void icyFrameClosed(final IcyFrameEvent e) {
                // ensure we are not doing recursing 'close' calls
                if (window.isVisible()) {
                    for (final WindowListener l : window.getWindowListeners())
                        l.windowClosed(new WindowEvent(window, e.getEvent().getID()));
                }
            }
        });

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(final WindowEvent e) {
                super.windowClosed(e);
                frame.close();
            }
        });

        return frame;
    }

    public static void setCursor(final Component c, final int cursor) {
        if (c == null)
            return;

        if (c.getCursor().getType() != cursor)
            c.setCursor(Cursor.getPredefinedCursor(cursor));
    }
}
