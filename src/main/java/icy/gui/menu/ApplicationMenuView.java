/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

package icy.gui.menu;

import com.formdev.flatlaf.FlatLaf;
import icy.action.GeneralActions;
import icy.action.WindowActions;
import icy.gui.component.menu.IcyCheckBoxMenuItem;
import icy.gui.component.menu.IcyMenu;
import icy.gui.component.menu.IcyMenuItem;
import icy.gui.component.menu.IcyRadioButtonMenuItem;
import icy.gui.util.LookAndFeelUtil;
import icy.main.Icy;
import jiconfont.IconCode;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import javax.swing.*;
import java.util.List;

/**
 * @author Thomas Musset
 */
public final class ApplicationMenuView extends AbstractApplicationMenu {

    private static ApplicationMenuView instance = null;

    public static synchronized ApplicationMenuView getInstance() {
        if (instance == null)
            instance = new ApplicationMenuView();

        return instance;
    }

    private ApplicationMenuView() {
        super("View");

        final IcyMenu menuAppearance = new IcyMenu("Appearance", GoogleMaterialDesignIcons.BRIGHTNESS_MEDIUM);
        add(menuAppearance);

        final List<FlatLaf> skins = LookAndFeelUtil.getSkins();
        final String skinName = LookAndFeelUtil.getCurrentSkinName();
        final ButtonGroup groupSkin = new ButtonGroup();
        for (final FlatLaf skin : skins) {
            final IconCode iconCode = (skin.isDark()) ? GoogleMaterialDesignIcons.BRIGHTNESS_2 : GoogleMaterialDesignIcons.WB_SUNNY;
            final IcyRadioButtonMenuItem itemLaf = new IcyRadioButtonMenuItem(skin.getName(), iconCode);
            if (skinName.equals(skin.getName()))
                itemLaf.setSelected(true);
            itemLaf.addActionListener(e -> LookAndFeelUtil.setSkin(skin));
            groupSkin.add(itemLaf);
            menuAppearance.add(itemLaf);
        }

        addSeparator();

        final IcyCheckBoxMenuItem checkboxitemDetachedMode = new IcyCheckBoxMenuItem("Detached Mode", GoogleMaterialDesignIcons.DASHBOARD);
        checkboxitemDetachedMode.setSelected(Icy.getMainInterface().isDetachedMode());
        checkboxitemDetachedMode.addActionListener(GeneralActions.detachedModeAction);
        add(checkboxitemDetachedMode);

        final IcyCheckBoxMenuItem checkboxitemStayOnTop = new IcyCheckBoxMenuItem("Stay on Top", GoogleMaterialDesignIcons.VERTICAL_ALIGN_TOP);
        checkboxitemStayOnTop.setSelected(Icy.getMainInterface().isAlwaysOnTop());
        checkboxitemStayOnTop.addActionListener(WindowActions.stayOnTopAction);
        add(checkboxitemStayOnTop);

        addSeparator();

        final IcyMenuItem itemSwimmingPoolViewer = new IcyMenuItem("Swimming Pool Viewer...", GoogleMaterialDesignIcons.GROUP_WORK);
        itemSwimmingPoolViewer.addActionListener(WindowActions.swimmingPoolAction);
        add(itemSwimmingPoolViewer);

        addSeparator();

        final IcyMenu menuOrganizeWindows = new IcyMenu("Organize Windows", GoogleMaterialDesignIcons.WEB);
        add(menuOrganizeWindows);

        final IcyMenuItem itemOrganizeGrid = new IcyMenuItem("Grid View", GoogleMaterialDesignIcons.VIEW_MODULE);
        itemOrganizeGrid.setAccelerator(KeyStroke.getKeyStroke("shift G"));
        itemOrganizeGrid.addActionListener(WindowActions.gridTileAction);
        menuOrganizeWindows.add(itemOrganizeGrid);

        final IcyMenuItem itemOrganizeHorizontal = new IcyMenuItem("Horizontal View", GoogleMaterialDesignIcons.VIEW_AGENDA);
        itemOrganizeHorizontal.setAccelerator(KeyStroke.getKeyStroke("shift H"));
        itemOrganizeHorizontal.addActionListener(WindowActions.horizontalTileAction);
        menuOrganizeWindows.add(itemOrganizeHorizontal);

        final IcyMenuItem itemOrganizeVertical = new IcyMenuItem("Vertical View", GoogleMaterialDesignIcons.VIEW_COLUMN);
        itemOrganizeVertical.setAccelerator(KeyStroke.getKeyStroke("shift V"));
        itemOrganizeVertical.addActionListener(WindowActions.verticalTileAction);
        menuOrganizeWindows.add(itemOrganizeVertical);

        final IcyMenuItem itemOrganizeCascade = new IcyMenuItem("Cascade View", GoogleMaterialDesignIcons.VIEW_QUILT);
        itemOrganizeCascade.addActionListener(WindowActions.cascadeAction);
        add(itemOrganizeCascade);
    }
}
