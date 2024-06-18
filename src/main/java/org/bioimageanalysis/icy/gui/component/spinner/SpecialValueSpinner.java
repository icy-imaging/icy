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

package org.bioimageanalysis.icy.gui.component.spinner;

import org.bioimageanalysis.icy.gui.component.editor.SpecialValueSpinnerEditor;
import org.bioimageanalysis.icy.gui.component.model.SpecialValueSpinnerModel;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;

/**
 * JSpinner component using a special value for a specific state.
 * 
 * @author Stephane
 */
public class SpecialValueSpinner extends JSpinner
{
    /**
     * 
     */
    private static final long serialVersionUID = 1858500300780069742L;

    /**
     * Create a new IcySpinner
     */
    public SpecialValueSpinner()
    {
        this(new SpecialValueSpinnerModel());
    }

    /**
     * @param model
     */
    public SpecialValueSpinner(SpecialValueSpinnerModel model)
    {
        super(model);
    }

    @Override
    protected JComponent createEditor(SpinnerModel model)
    {
        if (model instanceof SpecialValueSpinnerModel)
            return new SpecialValueSpinnerEditor(this);

        return super.createEditor(model);
    }

}
