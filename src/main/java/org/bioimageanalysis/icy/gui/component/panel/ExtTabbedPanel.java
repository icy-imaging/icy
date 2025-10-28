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

package org.bioimageanalysis.icy.gui.component.panel;

import org.bioimageanalysis.icy.gui.component.button.IcyButton;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Basically a JTabbedPane which can handle ExternalizablePanel.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
@Deprecated(since = "3.0.0", forRemoval = true)
public class ExtTabbedPanel extends JTabbedPane {
    public static class TabComponent extends JPanel {
        final ExternalizablePanel extPanel;
        final private IcyButton externButton;
        final JLabel label;

        /**
         * needed for data save
         */
        final int index;
        final String tip;

        public TabComponent(final String title, final Icon icon, final ExternalizablePanel panel, final String tip, final int index) {
            super();

            setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
            setBorder(BorderFactory.createEmptyBorder());
            setOpaque(false);

            this.index = index;
            this.tip = tip;
            extPanel = panel;

            label = new JLabel(title + " ", icon, SwingConstants.CENTER);
            label.setOpaque(false);

            externButton = new IcyButton(SVGResource.OPEN_IN_NEW);
            externButton.setOpaque(false);
            externButton.setContentAreaFilled(false);
            externButton.setToolTipText("Externalize panel");
            externButton.addActionListener(e -> {
                // externalize panel
                extPanel.externalize();
            });

            add(label);
            add(externButton);

            validate();
        }

        public String getTitle() {
            return label.getText().trim();
        }

        public Icon getIcon() {
            return label.getIcon();
        }

        public void setTitle(final String title) {
            label.setText(title + " ");
        }

        public void setIcon(final Icon icon) {
            label.setIcon(icon);
        }

        public void setDisabledIcon(final Icon disabledIcon) {
            label.setDisabledIcon(disabledIcon);
        }

        public void setBackgroundAll(final Color background) {
            externButton.setBackground(background);
            label.setBackground(background);
        }

        public void setForegroundAll(final Color foreground) {
            externButton.setForeground(foreground);
            label.setForeground(foreground);
        }
    }

    final private ArrayList<TabComponent> tabComponents;

    public ExtTabbedPanel() {
        this(TOP, WRAP_TAB_LAYOUT);
    }

    public ExtTabbedPanel(final int tabPlacement) {
        this(tabPlacement, WRAP_TAB_LAYOUT);
    }

    public ExtTabbedPanel(final int tabPlacement, final int tabLayoutPolicy) {
        super(tabPlacement, tabLayoutPolicy);

        tabComponents = new ArrayList<>();
    }

    /**
     * Find the ExtTabComponent attached to the specified ExternalizablePanel.
     */
    protected TabComponent getTabComponent(final ExternalizablePanel panel) {
        for (final TabComponent extTabComp : tabComponents)
            if (extTabComp.extPanel == panel)
                return extTabComp;

        return null;
    }

    @Override
    public Component add(final Component component) {
        // special case of externalizable panel
        if (component instanceof ExternalizablePanel) {
            final TabComponent tabComp = getTabComponent((ExternalizablePanel) component);

            // already existing ?
            if (tabComp != null) {
                // use its parameter
                insertTab(tabComp.getTitle(), tabComp.getIcon(), component, tabComp.tip,
                        Math.min(tabComp.index, getTabCount()));
                return component;
            }
        }

        return super.add(component);
    }

    @Override
    public void setIconAt(final int index, final Icon icon) {
        super.setIconAt(index, icon);

        final Component comp = getTabComponentAt(index);
        if (comp instanceof TabComponent)
            ((TabComponent) comp).setIcon(icon);
    }

    @Override
    public void setDisabledIconAt(final int index, final Icon disabledIcon) {
        super.setDisabledIconAt(index, disabledIcon);

        final Component comp = getTabComponentAt(index);
        if (comp instanceof TabComponent)
            ((TabComponent) comp).setDisabledIcon(disabledIcon);
    }

    @Override
    public void setBackgroundAt(final int index, final Color background) {
        super.setBackgroundAt(index, background);

        final Component comp = getTabComponentAt(index);
        if (comp instanceof TabComponent)
            ((TabComponent) comp).setBackgroundAll(background);
    }

    @Override
    public void setForegroundAt(final int index, final Color foreground) {
        super.setForegroundAt(index, foreground);

        final Component comp = getTabComponentAt(index);
        if (comp instanceof TabComponent)
            ((TabComponent) comp).setForegroundAll(foreground);
    }

    @Override
    public void setTitleAt(final int index, final String title) {
        super.setTitleAt(index, title);

        final Component comp = getTabComponentAt(index);
        if (comp instanceof TabComponent)
            ((TabComponent) comp).setTitle(title);
    }

    @Override
    public void insertTab(final String title, final Icon icon, final Component component, final String tip, final int index) {
        TabComponent tabComp;

        if (component instanceof final ExternalizablePanel panel) {
            tabComp = getTabComponent(panel);

            // not existing ?
            if (tabComp == null) {
                // create the associated tab component
                tabComp = new TabComponent(title, icon, panel, tip, index);
                // and save it in the list to keep a reference
                tabComponents.add(tabComp);
            }

            // externalized ?
            if (panel.isExternalized()) {
                // manually set parent and exit
                panel.setParent(this);
                return;
            }
        }
        else
            tabComp = null;

        super.insertTab(title, icon, component, tip, index);

        // use custom panel for externalizable panel
        if (tabComp != null)
            setTabComponentAt(index, tabComp);
    }
}
