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

/**
 * 
 */
package org.bioimageanalysis.icy.gui.listener;

import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.model.sequence.SequenceEvent;

import java.util.EventListener;

/**
 * Listener interface for the current active {@link Sequence}.
 * 
 * @author Stephane
 */
public interface ActiveSequenceListener extends EventListener
{
    /**
     * Sequence just get the active state.<br>
     * This event is generally preceded by a {@link #sequenceDeactivated(Sequence)} event describing
     * the sequence which actually lose activation.
     */
    public void sequenceActivated(Sequence sequence);

    /**
     * Sequence just lost the active state.<br>
     * This event is always followed by a {@link #sequenceActivated(Sequence)} event describing the
     * new activated sequence.
     */
    public void sequenceDeactivated(Sequence sequence);

    /**
     * The current active sequence has changed.
     */
    public void activeSequenceChanged(SequenceEvent event);
}
