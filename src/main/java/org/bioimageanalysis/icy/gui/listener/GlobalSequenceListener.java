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

package org.bioimageanalysis.icy.gui.listener;

import org.bioimageanalysis.icy.model.sequence.Sequence;

import java.util.EventListener;

/**
 * Global {@link Sequence} listener class.
 * Used to listen open, focus and close event for all sequence.
 * 
 * @author Stephane
 */
public interface GlobalSequenceListener extends EventListener
{
    /**
     * Sequence was just opened (first viewer displaying the sequence just opened)
     */
    public void sequenceOpened(Sequence sequence);

    /**
     * Sequence was just closed (last viewer displaying the sequence just closed)
     */
    public void sequenceClosed(Sequence sequence);
}
