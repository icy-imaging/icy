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

package fr.icy.gui.menu;

import com.formdev.flatlaf.FlatLaf;
import fr.icy.Icy;
import fr.icy.gui.LookAndFeelUtil;
import fr.icy.gui.action.GeneralActions;
import fr.icy.gui.action.WindowActions;
import fr.icy.gui.component.icon.IcySVG;
import fr.icy.gui.component.menu.IcyCheckBoxMenuItem;
import fr.icy.gui.component.menu.IcyMenu;
import fr.icy.gui.component.menu.IcyMenuItem;
import fr.icy.gui.component.menu.IcyRadioButtonMenuItem;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import java.util.List;

/**
 * @author Thomas Musset
 */
public final class ApplicationMenuView extends AbstractApplicationMenu {
    private static final @NonNull ApplicationMenuView instance = new ApplicationMenuView();

    @Contract(pure = true)
    public static synchronized @NonNull ApplicationMenuView getInstance() {
        return instance;
    }

    private ApplicationMenuView() {
        super("View");

        final IcyMenu menuAppearance = new IcyMenu("Appearance", IcySVG.THEME);
        add(menuAppearance);

        final List<FlatLaf> skins = LookAndFeelUtil.getSkins();
        final String skinName = LookAndFeelUtil.getCurrentSkinName();
        final ButtonGroup groupSkin = new ButtonGroup();
        for (final FlatLaf skin : skins) {
            final IcySVG svg = (skin.isDark()) ? IcySVG.THEME_DARK : IcySVG.THEME_LIGHT;
            final IcyRadioButtonMenuItem itemLaf = new IcyRadioButtonMenuItem(skin.getName(), svg);
            if (skinName.equals(skin.getName()))
                itemLaf.setSelected(true);
            itemLaf.addActionListener(e -> LookAndFeelUtil.setSkin(skin));
            groupSkin.add(itemLaf);
            menuAppearance.add(itemLaf);
        }

        addSeparator();

        // TODO rework detached mode
        final IcyCheckBoxMenuItem checkBoxItemDetachedMode = new IcyCheckBoxMenuItem("Detached Mode", IcySVG.DASHBOARD);
        checkBoxItemDetachedMode.setSelected(Icy.getMainInterface().isDetachedMode());
        checkBoxItemDetachedMode.addActionListener(GeneralActions.detachedModeAction);
        add(checkBoxItemDetachedMode);

        final IcyCheckBoxMenuItem checkBoxItemStayOnTop = new IcyCheckBoxMenuItem("Stay on Top", IcySVG.VERTICAL_ALIGN_TOP);
        checkBoxItemStayOnTop.setSelected(Icy.getMainInterface().isAlwaysOnTop());
        checkBoxItemStayOnTop.addActionListener(WindowActions.stayOnTopAction);
        add(checkBoxItemStayOnTop);

        addSeparator();

        final IcyMenuItem itemSwimmingPoolViewer = new IcyMenuItem("Swimming Pool Viewer…", IcySVG.GROUP_WORK);
        itemSwimmingPoolViewer.addActionListener(WindowActions.swimmingPoolAction);
        add(itemSwimmingPoolViewer);

        addSeparator();

        final IcyMenu menuOrganizeWindows = new IcyMenu("Organize Windows", IcySVG.TV_OPTIONS_INPUT_SETTINGS);
        add(menuOrganizeWindows);

        final IcyMenuItem itemOrganizeGrid = new IcyMenuItem("Grid View", IcySVG.VIEW_MODULE);
        itemOrganizeGrid.setAccelerator(KeyStroke.getKeyStroke("shift G"));
        itemOrganizeGrid.addActionListener(WindowActions.gridTileAction);
        menuOrganizeWindows.add(itemOrganizeGrid);

        final IcyMenuItem itemOrganizeHorizontal = new IcyMenuItem("Horizontal View", IcySVG.VIEW_STREAM);
        itemOrganizeHorizontal.setAccelerator(KeyStroke.getKeyStroke("shift H"));
        itemOrganizeHorizontal.addActionListener(WindowActions.horizontalTileAction);
        menuOrganizeWindows.add(itemOrganizeHorizontal);

        final IcyMenuItem itemOrganizeVertical = new IcyMenuItem("Vertical View", IcySVG.VIEW_COLUMN);
        itemOrganizeVertical.setAccelerator(KeyStroke.getKeyStroke("shift V"));
        itemOrganizeVertical.addActionListener(WindowActions.verticalTileAction);
        menuOrganizeWindows.add(itemOrganizeVertical);

        final IcyMenuItem itemOrganizeCascade = new IcyMenuItem("Cascade View", IcySVG.VIEW_QUILT);
        itemOrganizeCascade.addActionListener(WindowActions.cascadeAction);
        add(itemOrganizeCascade);
    }
}
