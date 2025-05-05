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
package org.bioimageanalysis.icy.model.colorspace;

import org.bioimageanalysis.icy.common.event.CollapsibleEvent;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class IcyColorSpaceEvent implements CollapsibleEvent {
    private final IcyColorSpace colorSpace;
    private int component;

    public IcyColorSpaceEvent(final IcyColorSpace colorSpace, final int component) {
        super();

        this.colorSpace = colorSpace;
        this.component = component;
    }

    /**
     * @return the colorSpace
     */
    public IcyColorSpace getColorSpace() {
        return colorSpace;
    }

    /**
     * @return the component
     */
    public int getComponent() {
        return component;
    }

    @Override
    public boolean collapse(final CollapsibleEvent event) {
        if (equals(event)) {
            final IcyColorSpaceEvent e = (IcyColorSpaceEvent) event;

            // set all component
            if (e.getComponent() != component)
                component = -1;

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return colorSpace.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final IcyColorSpaceEvent e) {
            return (colorSpace == e.getColorSpace());
        }

        return super.equals(obj);
    }
}