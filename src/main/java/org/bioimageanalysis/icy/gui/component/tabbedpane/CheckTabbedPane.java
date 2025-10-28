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

package org.bioimageanalysis.icy.gui.component.tabbedpane;

import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.gui.component.icon.IcySVG;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;

import javax.swing.*;
import java.awt.*;

/**
 * Basically a JTabbedPane with checkbox in tab.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class CheckTabbedPane extends JTabbedPane {
    private static final int ICON_SIZE = LookAndFeelUtil.getDefaultIconSize();

    private class CheckTabComponent extends JPanel {
        final private JCheckBox checkBox;
        final private JLabel label;

        public CheckTabComponent(final String title, final Icon icon) {
            super();

            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            //setBorder(BorderFactory.createEmptyBorder());
            setOpaque(false);

            checkBox = new JCheckBox(null, null, defaultSelected);
            //checkBox.setBorder(BorderFactory.createEmptyBorder());
            checkBox.setFocusable(false);
            checkBox.setToolTipText("enable / disable");
            checkBox.setOpaque(false);

            checkBox.addActionListener(actionevent -> CheckTabbedPane.this.fireStateChanged());

            //label = new JLabel(" " + title, icon, SwingConstants.CENTER);
            label = new JLabel(title, icon, SwingConstants.CENTER);
            label.setOpaque(false);

            add(checkBox);
            add(Box.createHorizontalStrut(10));
            add(label);
            //final Icon disabledIcon = new IcySVGIcon(SVGIcon.CIRCLE_FILL, Color.GRAY);
            final Icon disabledIcon = new IcySVG(SVGResource.CIRCLE_FILL).getIcon(ICON_SIZE, Color.GRAY);
            setIcon(disabledIcon);
            setDisabledIcon(disabledIcon);

            validate();
        }

        public boolean isSelected() {
            return checkBox.isSelected();
        }

        public void setSelected(final boolean value) {
            checkBox.setSelected(value);
        }

        public void setTitle(final String title) {
            label.setText(" " + title);
        }

        public void setIcon(final Icon icon) {
            label.setIcon(icon);
        }

        public void setDisabledIcon(final Icon disabledIcon) {
            label.setDisabledIcon(disabledIcon);
        }

        public void setBackgroundAll(final Color background) {
            //checkBox.setBackground(background);
            //label.setBackground(background);
            //setIcon(new IcySVGIcon(SVGIcon.CIRCLE_FILL, background));
            setIcon(new IcySVG(SVGResource.CIRCLE_FILL).getIcon(ICON_SIZE, background));
        }

        public void setForegroundAll(final Color foreground) {
            checkBox.setForeground(foreground);
            label.setForeground(foreground);
        }
    }

    /**
     * default checkbox selected state
     */
    boolean defaultSelected;

    /**
     * Constructor.
     *
     * @param defaultSelected by default checkbox is selected
     * @see JTabbedPane
     */
    @SuppressWarnings("MagicConstant")
    public CheckTabbedPane(final int tabPlacement, final boolean defaultSelected) {
        super(tabPlacement);

        this.defaultSelected = defaultSelected;
    }

    public boolean isDefaultSelected() {
        return defaultSelected;
    }

    public void setDefaultSelected(final boolean defaultSelected) {
        this.defaultSelected = defaultSelected;
    }

    @Override
    protected void fireStateChanged() {
        // just to avoid warning
        super.fireStateChanged();
    }

    /**
     * Returns the check state of tab component at <code>index</code>.
     *
     * @param index the tab index where the check state is queried
     * @return true if tab component at <code>index</code> is checked, false
     * otherwise
     * @throws IndexOutOfBoundsException if index is out of range (index &lt; 0 || index &gt;= tab count)
     * @see #setTabChecked(int, boolean)
     */
    public boolean isTabChecked(final int index) {
        return ((CheckTabComponent) getTabComponentAt(index)).isSelected();
    }

    /**
     * Set the check state of tab component at <code>index</code>.
     *
     * @param index the tab index we want to set the check state
     * @param value the check state
     * @throws IndexOutOfBoundsException if index is out of range (index &lt; 0 || index &gt;= tab count)
     * @see #isTabChecked(int)
     */
    public void setTabChecked(final int index, final boolean value) {
        ((CheckTabComponent) getTabComponentAt(index)).setSelected(value);
    }

    @Override
    public void setIconAt(final int index, final Icon icon) {
        super.setIconAt(index, icon);

        ((CheckTabComponent) getTabComponentAt(index)).setIcon(icon);
    }

    @Override
    public void setDisabledIconAt(final int index, final Icon disabledIcon) {
        super.setDisabledIconAt(index, disabledIcon);

        ((CheckTabComponent) getTabComponentAt(index)).setDisabledIcon(disabledIcon);
    }

    @Override
    public void setBackgroundAt(final int index, final Color background) {
        //super.setBackgroundAt(index, background);

        ((CheckTabComponent) getTabComponentAt(index)).setBackgroundAll(background);
    }

    @Override
    public void setForegroundAt(final int index, final Color foreground) {
        super.setForegroundAt(index, foreground);

        ((CheckTabComponent) getTabComponentAt(index)).setForegroundAll(foreground);
    }

    @Override
    public void setTitleAt(final int index, final String title) {
        super.setTitleAt(index, title);

        ((CheckTabComponent) getTabComponentAt(index)).setTitle(title);
    }

    @Override
    public void insertTab(final String title, final Icon icon, final Component component, final String tip, final int index) {
        super.insertTab(title, icon, component, tip, index);

        setTabComponentAt(index, new CheckTabComponent(title, icon));
    }
}
