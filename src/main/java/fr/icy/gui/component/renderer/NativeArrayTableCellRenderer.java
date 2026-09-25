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

package fr.icy.gui.component.renderer;

import fr.icy.common.collection.array.Array1DUtil;
import fr.icy.common.collection.array.ArrayUtil;
import fr.icy.common.math.MathUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class NativeArrayTableCellRenderer extends DefaultTableCellRenderer {
    private final boolean signed;

    public NativeArrayTableCellRenderer(final boolean signed) {
        super();

        this.signed = signed;

        setHorizontalAlignment(SwingConstants.TRAILING);
    }

    public NativeArrayTableCellRenderer() {
        this(true);
    }

    @Override
    protected void setValue(final Object value) {
        if ((value != null) && (ArrayUtil.getDim(value) == 1)) {
            final int len = ArrayUtil.getLength(value);

            final StringBuilder s;

            if (len == 0)
                s = new StringBuilder();
            else if (len == 1)
                s = new StringBuilder(Double.toString(MathUtil.roundSignificant(Array1DUtil.getValueAsDouble(value, 0, signed), 5)));
            else {
                s = new StringBuilder("[" + MathUtil.roundSignificant(Array1DUtil.getValueAsDouble(value, 0, signed), 5));
                for (int i = 1; i < len; i++)
                    s.append(" ").append(MathUtil.roundSignificant(Array1DUtil.getValueAsDouble(value, i, signed), 5));
                s.append("]");
            }

            setText(s.toString());
        }
        else
            super.setValue(value);
    }
}
