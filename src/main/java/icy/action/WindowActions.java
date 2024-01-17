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
package icy.action;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import icy.gui.main.MainFrame;
import icy.main.Icy;
import icy.preferences.GeneralPreferences;
import icy.swimmingPool.SwimmingPoolViewer;
import icy.util.ClassUtil;

/**
 * @author Stephane
 * @author Thomas Musset
 */
public final class WindowActions {
    public static final IcyAbstractAction stayOnTopAction = new IcyAbstractAction(
            "Stay on top",
            //new IcyIcon(ResourceUtil.ICON_PIN),
            "Keep window on top",
            "Icy window always stays above other windows."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final boolean value = !Icy.getMainInterface().isAlwaysOnTop();

            // set "always on top" state
            Icy.getMainInterface().setAlwaysOnTop(value);
            // and save state
            GeneralPreferences.setAlwaysOnTop(value);

            return true;
        }
    };

    public static final IcyAbstractAction swimmingPoolAction = new IcyAbstractAction(
            "Swimming Pool Viewer",
            //new IcyIcon(ResourceUtil.ICON_BOX),
            "Show the swimming pool",
            "Show and edit the swimming pool objects"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new SwimmingPoolViewer();

            return true;
        }
    };

    public static final IcyAbstractAction gridTileAction = new IcyAbstractAction(
            "Grid (Shift+G)",
            //new IcyIcon("2x2_grid"),
            "Grid tile arrangement",
            "Reorganise all opened windows in grid tile.",
            KeyEvent.VK_G,
            //InputEvent.SHIFT_MASK // TODO: 27/01/2023 SHIFT_MASK deprecated, use SHIFT_DOWN_MASK instead
            InputEvent.SHIFT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

            if (mainFrame != null) {
                mainFrame.organizeTile(MainFrame.TILE_GRID);
                return true;
            }

            return false;
        }
    };

    public static final IcyAbstractAction horizontalTileAction = new IcyAbstractAction(
            "Horizontal (Shift+H)",
            //new IcyIcon("tile_horizontal"),
            "Horizontal tile arrangement",
            "Reorganise all opened windows in horizontal tile.",
            KeyEvent.VK_H,
            //InputEvent.SHIFT_MASK // TODO: 27/01/2023 SHIFT_MASK deprecated, use SHIFT_DOWN_MASK instead
            InputEvent.SHIFT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

            if (mainFrame != null) {
                mainFrame.organizeTile(MainFrame.TILE_HORIZONTAL);
                return true;
            }

            return false;
        }
    };

    public static final IcyAbstractAction verticalTileAction = new IcyAbstractAction(
            "Vertical (Shift+V)",
            //new IcyIcon("tile_vertical"),
            "Vertical tile arrangement",
            "Reorganise all opened windows in vertical tile.",
            KeyEvent.VK_V,
            //InputEvent.SHIFT_MASK // TODO: 27/01/2023 SHIFT_MASK deprecated, use SHIFT_DOWN_MASK instead
            InputEvent.SHIFT_DOWN_MASK
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

            if (mainFrame != null) {
                mainFrame.organizeTile(MainFrame.TILE_VERTICAL);
                return true;
            }

            return false;
        }
    };

    public static final IcyAbstractAction cascadeAction = new IcyAbstractAction(
            "Cascade",
            //new IcyIcon("cascade"),
            "Cascade arrangement",
            "Reorganise all opened windows in cascade."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

            if (mainFrame != null) {
                mainFrame.organizeCascade();
                return true;
            }

            return false;
        }
    };

    /**
     * Return all actions of this class
     */
    public static List<IcyAbstractAction> getAllActions() {
        final List<IcyAbstractAction> result = new ArrayList<>();

        for (final Field field : WindowActions.class.getFields()) {
            final Class<?> type = field.getType();

            try {
                if (ClassUtil.isSubClass(type, IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (ClassUtil.isSubClass(type, IcyAbstractAction.class))
                    result.add((IcyAbstractAction) field.get(null));
            }
            catch (Exception e) {
                // ignore
            }
        }

        return result;
    }
}
