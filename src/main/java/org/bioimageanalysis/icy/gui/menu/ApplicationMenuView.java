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

package org.bioimageanalysis.icy.gui.menu;

import com.formdev.flatlaf.FlatLaf;
import org.bioimageanalysis.icy.gui.action.GeneralActions;
import org.bioimageanalysis.icy.gui.action.WindowActions;
import org.bioimageanalysis.icy.gui.component.menu.IcyCheckBoxMenuItem;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenu;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenuItem;
import org.bioimageanalysis.icy.gui.component.menu.IcyRadioButtonMenuItem;
import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

/**
 * @author Thomas Musset
 */
public final class ApplicationMenuView extends AbstractApplicationMenu {
    private static final @NotNull ApplicationMenuView instance = new ApplicationMenuView();

    public static synchronized @NotNull ApplicationMenuView getInstance() {
        return instance;
    }

    private ApplicationMenuView() {
        super("View");

        final IcyMenu menuAppearance = new IcyMenu("Appearance", SVGIcon.BRIGHTNESS_MEDIUM);
        add(menuAppearance);

        final List<FlatLaf> skins = LookAndFeelUtil.getSkins();
        final String skinName = LookAndFeelUtil.getCurrentSkinName();
        final ButtonGroup groupSkin = new ButtonGroup();
        for (final FlatLaf skin : skins) {
            final SVGIcon svg = (skin.isDark()) ? SVGIcon.DARK_MODE : SVGIcon.LIGHT_MODE;
            final IcyRadioButtonMenuItem itemLaf = new IcyRadioButtonMenuItem(skin.getName(), svg);
            if (skinName.equals(skin.getName()))
                itemLaf.setSelected(true);
            itemLaf.addActionListener(e -> LookAndFeelUtil.setSkin(skin));
            groupSkin.add(itemLaf);
            menuAppearance.add(itemLaf);
        }

        addSeparator();

        // TODO rework detached mode
        final IcyCheckBoxMenuItem checkboxitemDetachedMode = new IcyCheckBoxMenuItem("Detached Mode", SVGIcon.DASHBOARD);
        checkboxitemDetachedMode.setSelected(Icy.getMainInterface().isDetachedMode());
        checkboxitemDetachedMode.addActionListener(GeneralActions.detachedModeAction);
        add(checkboxitemDetachedMode);

        final IcyCheckBoxMenuItem checkboxitemStayOnTop = new IcyCheckBoxMenuItem("Stay on Top", SVGIcon.VERTICAL_ALIGN_TOP);
        checkboxitemStayOnTop.setSelected(Icy.getMainInterface().isAlwaysOnTop());
        checkboxitemStayOnTop.addActionListener(WindowActions.stayOnTopAction);
        add(checkboxitemStayOnTop);

        addSeparator();

        final IcyMenuItem itemSwimmingPoolViewer = new IcyMenuItem("Swimming Pool Viewer...", SVGIcon.GROUP_WORK);
        itemSwimmingPoolViewer.addActionListener(WindowActions.swimmingPoolAction);
        add(itemSwimmingPoolViewer);

        addSeparator();

        final IcyMenu menuOrganizeWindows = new IcyMenu("Organize Windows", SVGIcon.TV_OPTIONS_INPUT_SETTINGS);
        add(menuOrganizeWindows);

        final IcyMenuItem itemOrganizeGrid = new IcyMenuItem("Grid View", SVGIcon.VIEW_MODULE);
        itemOrganizeGrid.setAccelerator(KeyStroke.getKeyStroke("shift G"));
        itemOrganizeGrid.addActionListener(WindowActions.gridTileAction);
        menuOrganizeWindows.add(itemOrganizeGrid);

        final IcyMenuItem itemOrganizeHorizontal = new IcyMenuItem("Horizontal View", SVGIcon.VIEW_STREAM);
        itemOrganizeHorizontal.setAccelerator(KeyStroke.getKeyStroke("shift H"));
        itemOrganizeHorizontal.addActionListener(WindowActions.horizontalTileAction);
        menuOrganizeWindows.add(itemOrganizeHorizontal);

        final IcyMenuItem itemOrganizeVertical = new IcyMenuItem("Vertical View", SVGIcon.VIEW_COLUMN);
        itemOrganizeVertical.setAccelerator(KeyStroke.getKeyStroke("shift V"));
        itemOrganizeVertical.addActionListener(WindowActions.verticalTileAction);
        menuOrganizeWindows.add(itemOrganizeVertical);

        final IcyMenuItem itemOrganizeCascade = new IcyMenuItem("Cascade View", SVGIcon.VIEW_QUILT);
        itemOrganizeCascade.addActionListener(WindowActions.cascadeAction);
        add(itemOrganizeCascade);
    }
}
