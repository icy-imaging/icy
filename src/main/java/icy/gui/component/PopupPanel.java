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

package icy.gui.component;

import icy.gui.component.button.IcyToggleButton;
import icy.gui.util.ComponentUtil;
import icy.resource.icon.SVGIcon;
import icy.resource.icon.SVGIconPack;
import icy.util.GraphicsUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class PopupPanel extends JPanel {
    public final class PopupTitlePanel extends IcyToggleButton {
        public PopupTitlePanel(final String text) {
            super(text, new SVGIconPack(SVGIcon.ARROW_DROP_DOWN, SVGIcon.ARROW_DROP_UP));

            setHorizontalAlignment(SwingConstants.LEADING);
            setFocusPainted(false);

            if (subPopupPanel)
                ComponentUtil.setFixedHeight(this, getTextSize().height);
            else
                ComponentUtil.setFontBold(this);

            addActionListener(e -> refresh());
        }

        @Override
        public void setText(final String text) {
            super.setText(text);

            updateIconTextGap();
        }

        @Override
        public void setIcon(final Icon defaultIcon) {
            super.setIcon(defaultIcon);

            updateIconTextGap();
        }

        @Override
        public void setBounds(final int x, final int y, final int width, final int height) {
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
    public PopupPanel(final String title, final int panelHeight, final boolean subPopupPanel) {
        this(title, subPopupPanel);
    }

    /**
     * @deprecated Use {@link #PopupPanel(String, boolean)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public PopupPanel(final String title, final int panelHeight) {
        this(title, false);
    }

    /**
     * Create a new popup panel with specified title.
     *
     * @param title    Panel title
     * @param panel    internal panel
     * @param subPanel Determine if this is an embedded popup panel or a normal one.
     */
    public PopupPanel(final String title, final JPanel panel, final boolean subPanel) {
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
    public PopupPanel(final String title, final JPanel panel) {
        this(title, panel, false);
    }

    /**
     * Create a new popup panel with specified title.
     *
     * @param title    Panel title
     * @param subPanel Determine if this is an embedded popup panel or a normal one.
     */
    public PopupPanel(final String title, final boolean subPanel) {
        this(title, new JPanel(), subPanel);
    }

    /**
     * Create a new popup panel with specified title.
     */
    public PopupPanel(final String title) {
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

    public void setTitle(final String value) {
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
    public void setExpanded(final boolean value) {
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
