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
package org.bioimageanalysis.icy.gui.component.model;

import javax.swing.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class SpecialValueSpinnerModel extends SpinnerNumberModel {
    private final Number special;
    private final String specialText;

    public SpecialValueSpinnerModel() {
        this(Integer.valueOf(0), null, null, Integer.valueOf(1), Integer.valueOf(0), null);
    }

    public SpecialValueSpinnerModel(final int special, final String specialText) {
        this(Integer.valueOf(0), null, null, Integer.valueOf(1), Integer.valueOf(special), specialText);
    }

    public SpecialValueSpinnerModel(final double value, final double minimum, final double maximum, final double stepSize, final double special, final String specialText) {
        this(Double.valueOf(value), Double.valueOf(minimum), Double.valueOf(maximum), Double.valueOf(stepSize), Double.valueOf(special), specialText);
    }

    public SpecialValueSpinnerModel(final int value, final int minimum, final int maximum, final int stepSize, final int special, final String specialText) {
        this(Integer.valueOf(value), Integer.valueOf(minimum), Integer.valueOf(maximum), Integer.valueOf(stepSize), Integer.valueOf(special), specialText);
    }

    public SpecialValueSpinnerModel(final Number value, final Comparable minimum, final Comparable maximum, final Number stepSize, final Number special, final String specialText) {
        super(value, minimum, maximum, stepSize);

        this.special = special;
        this.specialText = specialText;
    }

    /**
     * Returns the special value which is used to display special text.
     *
     * @see #getSpecialText()
     */
    public Number getSpecialValue() {
        return special;
    }

    /**
     * Returns the special text which is display when special value is selected.
     *
     * @see #getSpecialValue()
     */
    public String getSpecialText() {
        return specialText;
    }
}
