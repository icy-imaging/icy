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

import icy.canvas.IcyCanvas;
import icy.gui.viewer.Viewer;
import icy.image.lut.LUT;
import icy.main.Icy;
import icy.resource.icon.IcyIcon;
import icy.sequence.Sequence;
import icy.system.thread.ThreadUtil;
import icy.util.ClassUtil;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import plugins.kernel.canvas.VtkCanvas;

/**
 * Viewer associated actions (Duplicate, externalize...)
 *
 * @author Stephane
 * @author Thomas MUSSET
 */
public class ViewerActions {
    public static IcyAbstractAction duplicateAction = new IcyAbstractAction(
            "Duplicate view",
            (IcyIcon) null,
            "Duplicate view (no data duplication)",
            KeyEvent.VK_F2
    ) {

        @Override
        public boolean doAction(ActionEvent e) {
            ThreadUtil.invokeLater(() -> {
                // so it won't change during process
                final Viewer viewer = Icy.getMainInterface().getActiveViewer();
                final IcyCanvas canvas = (viewer != null) ? viewer.getCanvas() : null;
                final Sequence sequence = (viewer != null) ? viewer.getSequence() : null;

                if ((sequence != null) && (canvas != null)) {
                    final Viewer v = new Viewer(sequence);
                    final LUT oldLut = viewer.getLut();
                    final LUT newLut = v.getLut();

                    // copy LUT
                    if (canvas instanceof VtkCanvas) {
                        // don't copy alpha colormap
                        newLut.setColorMaps(oldLut, false);
                        newLut.setScalers(oldLut);
                    }
                    else
                        newLut.copyFrom(oldLut);
                }
            });

            return true;
        }

        @Override
        public boolean isEnabled() {
            return super.isEnabled() && (Icy.getMainInterface().getActiveViewer() != null);
        }
    };

    /**
     * Return all actions of this class
     */
    public static List<IcyAbstractAction> getAllActions() {
        final List<IcyAbstractAction> result = new ArrayList<>();

        for (Field field : ViewerActions.class.getFields()) {
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
