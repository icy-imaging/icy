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
package org.bioimageanalysis.icy.common.event;

/**
 * Collapsible interface for collapsible event used by UpdateEventHandler.<br>
 * As we use HashMap to store these events, so we rely on Object.equals(..) and
 * Object.hashcode() implementation for these events.
 * 
 * @author Stephane
 */
public interface CollapsibleEvent
{
    /**
     * Collapse current object/event with specified one.
     * 
     * @return <code>false</code> if collapse operation failed (object are not 'equals')
     * @param event
     *        event
     */
    public boolean collapse(CollapsibleEvent event);

    /**
     * @return Returns <code>true</code> if the current event is equivalent to the specified one.<br>
     *         We want event to override {@link Object#equals(Object)} method as we use an HashMap to store
     *         these event
     *         in the {@link UpdateEventHandler} class.
     * @param event
     *        event
     */
    public boolean equals(Object event);

    /**
     * @return Returns hash code for current event. It should respect the default {@link Object#hashCode()}
     *         contract.
     */
    public int hashCode();
}
