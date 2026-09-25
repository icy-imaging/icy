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

package fr.icy.gui.component.slider;

import fr.icy.gui.component.ComponentUtil;
import org.intellij.lang.annotations.MagicConstant;

import javax.swing.*;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class IcySlider extends JSlider {
    private boolean smartTickMarkers;

    public IcySlider() {
        super();

        smartTickMarkers = true;
    }

    public IcySlider(final BoundedRangeModel brm) {
        super(brm);

        smartTickMarkers = true;
    }

    public IcySlider(final int orientation, final int min, final int max, final int value) {
        super(orientation, min, max, value);

        smartTickMarkers = true;
    }

    public IcySlider(final int min, final int max, final int value) {
        super(min, max, value);

        smartTickMarkers = true;
    }

    public IcySlider(final int min, final int max) {
        super(min, max);

        smartTickMarkers = true;
    }

    public IcySlider(final @MagicConstant(intValues = {SwingConstants.HORIZONTAL, SwingConstants.VERTICAL}) int orientation) {
        super(orientation);

        smartTickMarkers = true;
    }

    /**
     * @return the smartTickMarkers
     */
    public boolean isSmartTickMarkers() {
        return smartTickMarkers;
    }

    /**
     * @param value the smartTickMarkers to set
     */
    public void setSmartTickMarkers(final boolean value) {
        if (smartTickMarkers != value) {
            smartTickMarkers = value;

            updateTicksAndLabels();
        }
    }

    private void updateTicksAndLabels() {
        if (smartTickMarkers && (getPaintTicks() || getPaintLabels()))
            ComponentUtil.setTickMarkers(this);
    }

    @Override
    public void setPaintLabels(final boolean b) {
        super.setPaintLabels(b);

        updateTicksAndLabels();
    }

    @Override
    public void setPaintTicks(final boolean b) {
        super.setPaintTicks(b);

        updateTicksAndLabels();
    }

    @Override
    protected void fireStateChanged() {
        super.fireStateChanged();

        updateTicksAndLabels();
    }
}
