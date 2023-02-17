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
package icy.gui.component;

import icy.gui.component.button.IcyButtonNew;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import java.awt.Color;
import java.awt.Component;
import java.util.EventListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/**
 * @deprecated Use {@link CloseableTabbedPane} instead
 */
@Deprecated
public class CloseTabbedPane extends JTabbedPane {
    public interface CloseTabbedPaneListener extends EventListener {
        void tabClosed(int index, String title);
    }

    private class CloseTabComponent extends JPanel {
        final private IcyButtonNew closeButton;
        final private JLabel label;
        final private Component sep;

        public CloseTabComponent(String title, Icon icon) {
            super();

            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setBorder(BorderFactory.createEmptyBorder());
            setOpaque(false);

            label = new JLabel(title, icon, SwingConstants.CENTER);
            label.setOpaque(false);

            sep = Box.createHorizontalStrut(6);

            closeButton = new IcyButtonNew(GoogleMaterialDesignIcons.DELETE);
            closeButton.setFlat(true);
            // closeButton.setContentAreaFilled(false);
            closeButton.setToolTipText("close");
            closeButton.setOpaque(false);

            closeButton.addActionListener(actionevent -> {
                final int index = indexOfTabComponent(CloseTabComponent.this);

                if (index != -1) {
                    CloseTabbedPane.this.removeTabAt(index);
                    CloseTabbedPane.this.fireTabClosed(index, getTitle());
                }
            });

            add(label);
            add(sep);
            add(closeButton);

            validate();
        }

        public boolean isClosable() {
            return closeButton.isVisible();
        }

        public void setClosable(boolean value) {
            sep.setVisible(value);
            closeButton.setVisible(value);
        }

        public String getTitle() {
            return label.getText();
        }

        public void setTitle(String title) {
            label.setText(title);
        }

        public void setIcon(Icon icon) {
            label.setIcon(icon);
        }

        public void setDisabledIcon(Icon disabledIcon) {
            label.setDisabledIcon(disabledIcon);
        }

        public void setBackgroundAll(Color background) {
            label.setBackground(background);
            closeButton.setBackground(background);
        }

        public void setForegroundAll(Color foreground) {
            label.setForeground(foreground);
            closeButton.setForeground(foreground);
        }
    }

    /**
     * {@link JTabbedPane}
     */
    public CloseTabbedPane() {
        super();
    }

    /**
     * {@link JTabbedPane}
     */
    public CloseTabbedPane(int tabPlacement) {
        super(tabPlacement);
    }

    /**
     * {@link JTabbedPane}
     */
    public CloseTabbedPane(int tabPlacement, int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);
    }

    /**
     * Returns the 'closable' state of tab component at <code>index</code>.
     *
     * @param index the tab index where the check state is queried
     * @return true if tab component at <code>index</code> can be closed (close button visible).<br>
     * Returns false otherwise
     * @throws IndexOutOfBoundsException if index is out of range (index &lt; 0 || index &gt;= tab count)
     * @see #setTabClosable(int, boolean)
     */
    public boolean isTabClosable(int index) {
        return ((CloseTabComponent) getTabComponentAt(index)).isClosable();
    }

    /**
     * Set the 'closable' state of tab component at <code>index</code>.
     *
     * @param index the tab index we want to set the 'closable' state
     * @param value true if the tab should be 'closable' (close button visible), false otherwise.
     * @throws IndexOutOfBoundsException if index is out of range (index &lt; 0 || index &gt;= tab count)
     * @see #isTabClosable(int)
     */
    public void setTabClosable(int index, boolean value) {
        ((CloseTabComponent) getTabComponentAt(index)).setClosable(value);
    }

    @Override
    public void setIconAt(int index, Icon icon) {
        super.setIconAt(index, icon);

        final CloseTabComponent comp = (CloseTabComponent) getTabComponentAt(index);

        if (comp != null)
            comp.setIcon(icon);
    }

    @Override
    public void setDisabledIconAt(int index, Icon disabledIcon) {
        super.setDisabledIconAt(index, disabledIcon);

        final CloseTabComponent comp = (CloseTabComponent) getTabComponentAt(index);

        if (comp != null)
            comp.setDisabledIcon(disabledIcon);
    }

    @Override
    public void setBackgroundAt(int index, Color background) {
        super.setBackgroundAt(index, background);

        final CloseTabComponent comp = (CloseTabComponent) getTabComponentAt(index);

        if (comp != null)
            comp.setBackgroundAll(background);
    }

    @Override
    public void setForegroundAt(int index, Color foreground) {
        super.setForegroundAt(index, foreground);

        final CloseTabComponent comp = (CloseTabComponent) getTabComponentAt(index);

        if (comp != null)
            comp.setForegroundAll(foreground);
    }

    @Override
    public void setTitleAt(int index, String title) {
        super.setTitleAt(index, title);

        final CloseTabComponent comp = (CloseTabComponent) getTabComponentAt(index);

        if (comp != null)
            comp.setTitle(title);
    }

    @Override
    public void insertTab(String title, Icon icon, Component component, String tip, int index) {
        super.insertTab(title, icon, component, tip, index);

        setTabComponentAt(index, new CloseTabComponent(title, icon));
    }

    protected void fireTabClosed(int index, String text) {
        for (CloseTabbedPaneListener l : listenerList.getListeners(CloseTabbedPaneListener.class))
            l.tabClosed(index, text);

    }

    public void addCloseTabbedPaneListener(CloseTabbedPaneListener l) {
        listenerList.add(CloseTabbedPaneListener.class, l);
    }

    public void removeCloseTabbedPaneListener(CloseTabbedPaneListener l) {
        listenerList.remove(CloseTabbedPaneListener.class, l);
    }
}
