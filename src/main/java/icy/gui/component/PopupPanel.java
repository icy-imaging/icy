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

import icy.gui.component.button.IcyToggleButton;
import icy.gui.util.ComponentUtil;
import icy.gui.util.LookAndFeelUtil;
import icy.resource.icon.IcyIcon;
import icy.util.GraphicsUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.geom.Rectangle2D;

import javax.swing.*;

/**
 * @author Stephane
 * @author Thomas MUSSET
 */
public class PopupPanel extends JPanel {
    private class PopupTitlePanel extends IcyToggleButton {
        @Deprecated(since = "3.0.0", forRemoval = true)
        public PopupTitlePanel(String text, Image image) {
            super(text, new IcyIcon(image, LookAndFeelUtil.getDefaultIconSizeAsInt()));
            setHorizontalAlignment(SwingConstants.LEADING);
            setFocusPainted(false);

            if (subPopupPanel)
                ComponentUtil.setFixedHeight(this, getTextSize().height);
            else
                ComponentUtil.setFontBold(this);

            addActionListener(e -> refresh());
        }

        public PopupTitlePanel(final String text) {
            super(text, GoogleMaterialDesignIcons.ARROW_DROP_DOWN, GoogleMaterialDesignIcons.ARROW_DROP_UP);

            setHorizontalAlignment(SwingConstants.LEADING);
            setFocusPainted(false);

            if (subPopupPanel)
                ComponentUtil.setFixedHeight(this, getTextSize().height);
            else
                ComponentUtil.setFontBold(this);

            addActionListener(e -> refresh());
        }

        @Override
        public void setText(String text) {
            super.setText(text);

            updateIconTextGap();
        }

        @Override
        public void setIcon(Icon defaultIcon) {
            super.setIcon(defaultIcon);

            updateIconTextGap();
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height);

            updateIconTextGap();
        }

        private Dimension getTextSize() {
            final String text = getText();

            if (text != null) {
                final Rectangle2D r = GraphicsUtil.getStringBounds(this, text);
                return new Dimension((int) r.getWidth(), (int) r.getHeight());
            }

            return new Dimension(0, 0);
        }

        private void updateIconTextGap() {
            final int width = getWidth();
            final Icon icon = getIcon();

            if ((width != 0) && (icon != null)) {
                // adjust icon gap to new width
                int iconTextGap = (width - getTextSize().width) / 2;
                iconTextGap -= (icon.getIconWidth() + 10);
                setIconTextGap(iconTextGap);
            }
        }
    }

    protected final PopupTitlePanel topPanel;
    protected final JPanel mainPanel;

    protected final boolean subPopupPanel;

    /**
     * @deprecated Use {@link #PopupPanel(String, boolean)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public PopupPanel(String title, int panelHeight, boolean subPopupPanel) {
        this(title, subPopupPanel);
    }

    /**
     * @deprecated Use {@link #PopupPanel(String, boolean)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public PopupPanel(String title, int panelHeight) {
        this(title, false);
    }

    /**
     * Create a new popup panel with specified title.
     *
     * @param title    Panel title
     * @param panel    internal panel
     * @param subPanel Determine if this is an embedded popup panel or a normal one.
     */
    public PopupPanel(String title, JPanel panel, boolean subPanel) {
        super();

        //topPanel = new PopupTitlePanel(title, ResourceUtil.ICON_PANEL_COLLAPSE);
        topPanel = new PopupTitlePanel(title);
        mainPanel = panel;
        // if (panelHeight != -1)
        // ComponentUtil.setFixedHeight(mainPanel, panelHeight);
        subPopupPanel = subPanel;

        //setBorder(BorderFactory.createRaisedBevelBorder());
        setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        setLayout(new BorderLayout());

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        refresh();
    }

    /**
     * Create a new popup panel with specified title.
     *
     * @param title Panel title
     * @param panel internal panel
     */
    public PopupPanel(String title, JPanel panel) {
        this(title, panel, false);
    }

    /**
     * Create a new popup panel with specified title.
     *
     * @param title    Panel title
     * @param subPanel Determine if this is an embedded popup panel or a normal one.
     */
    public PopupPanel(String title, boolean subPanel) {
        this(title, new JPanel(), subPanel);
    }

    /**
     * Create a new popup panel with specified title.
     */
    public PopupPanel(String title) {
        this(title, false);
    }

    /**
     * @deprecated Use {@link #PopupPanel(String)} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public PopupPanel() {
        this("no title", false);
    }

    public String getTitle() {
        return topPanel.getText();
    }

    public void setTitle(String value) {
        topPanel.setText(value);
    }

    /**
     * @return the title panel
     */
    public PopupTitlePanel getTitlePanel() {
        return topPanel;
    }

    /**
     * @return the mainPanel
     */
    public JPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * @return the collapsed
     */
    public boolean isCollapsed() {
        return !isExpanded();
    }

    /**
     * @return the collapsed
     */
    public boolean isExpanded() {
        return topPanel.isSelected();
    }

    /**
     * @param value the collapsed to set
     */
    public void setExpanded(boolean value) {
        if (topPanel.isSelected() != value) {
            topPanel.setSelected(value);
            refresh();
        }
    }

    /**
     * @return the subPopupPanel
     */
    public boolean isSubPopupPanel() {
        return subPopupPanel;
    }

    public void expand() {
        setExpanded(true);
    }

    public void collapse() {
        setExpanded(false);
    }

    void refresh() {
        mainPanel.setVisible(topPanel.isSelected());
    }
}
