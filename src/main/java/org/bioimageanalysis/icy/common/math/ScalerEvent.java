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
package org.bioimageanalysis.icy.common.math;

import org.bioimageanalysis.icy.common.event.CollapsibleEvent;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ScalerEvent implements CollapsibleEvent {
    private final Scaler scaler;

    public ScalerEvent(final Scaler scaler) {
        super();

        this.scaler = scaler;
    }

    /**
     * @return the scaler
     */
    public Scaler getScaler() {
        return scaler;
    }

    @Override
    public boolean collapse(final CollapsibleEvent event) {
        if (equals(event)) {
            // nothing to do here
            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return scaler.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final ScalerEvent e) {
            return (scaler == e.getScaler());
        }

        return super.equals(obj);
    }
}
