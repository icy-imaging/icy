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

package fr.icy.gui.component.spinner;

import fr.icy.gui.component.editor.SpecialValueSpinnerEditor;
import fr.icy.gui.component.model.SpecialValueSpinnerModel;

import javax.swing.*;

/**
 * JSpinner component using a special value for a specific state.
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class SpecialValueSpinner extends JSpinner {
    public SpecialValueSpinner() {
        this(new SpecialValueSpinnerModel());
    }

    public SpecialValueSpinner(final SpecialValueSpinnerModel model) {
        super(model);
    }

    @Override
    protected JComponent createEditor(final SpinnerModel model) {
        if (model instanceof SpecialValueSpinnerModel)
            return new SpecialValueSpinnerEditor(this);

        return super.createEditor(model);
    }

}
