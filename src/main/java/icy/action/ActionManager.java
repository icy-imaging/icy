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

import icy.gui.main.ActiveSequenceListener;
import icy.main.Icy;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stephane
 * @author Thomas Musset
 */
public final class ActionManager {
    /**
     * All registered action
     */
    public static List<IcyAbstractAction> actions = null;

    public static synchronized void init() {
        // init actions
        if (actions == null) {
            actions = new ArrayList<>();

            // add all kernels actions
            actions.addAll(FileActions.getAllActions());
            actions.addAll(GeneralActions.getAllActions());
            actions.addAll(PreferencesActions.getAllActions());
            actions.addAll(SequenceOperationActions.getAllActions());
            actions.addAll(RoiActions.getAllActions());
            actions.addAll(CanvasActions.getAllActions());
            actions.addAll(ViewerActions.getAllActions());
            actions.addAll(WindowActions.getAllActions());

            final ActiveSequenceListener activeSequenceListener = new ActiveSequenceListener() {
                @Override
                public void sequenceDeactivated(Sequence sequence) {
                    // nothing here
                }

                @Override
                public void sequenceActivated(Sequence sequence) {
                    // force action components refresh
                    for (IcyAbstractAction action : actions)
                        action.enabledChanged();
                }

                @Override
                public void activeSequenceChanged(SequenceEvent event) {
                    // nothing here
                }
            };

            // listen these event
            Icy.getMainInterface().addActiveSequenceListener(activeSequenceListener);
        }
    }
}
