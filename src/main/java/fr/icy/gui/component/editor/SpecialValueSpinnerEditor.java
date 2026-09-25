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

package fr.icy.gui.component.editor;

import fr.icy.gui.component.model.SpecialValueSpinnerModel;
import fr.icy.gui.component.spinner.SpecialValueSpinner;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class SpecialValueSpinnerEditor extends DefaultEditor {
    private static class NumberEditorFormatter extends NumberFormatter {
        private final SpecialValueSpinnerModel model;

        NumberEditorFormatter(final @NonNull SpecialValueSpinnerModel model, final NumberFormat format) {
            super(format);
            this.model = model;
            setValueClass(model.getValue().getClass());
        }

        @Override
        public void setMinimum(final Comparable<?> min) {
            model.setMinimum(min);
        }

        @Override
        public Comparable<?> getMinimum() {
            return model.getMinimum();
        }

        @Override
        public void setMaximum(final Comparable<?> max) {
            model.setMaximum(max);
        }

        @Override
        public Comparable<?> getMaximum() {
            return model.getMaximum();
        }

        @Override
        public Object stringToValue(final String text) throws ParseException {
            if ((text != null) && text.equalsIgnoreCase(model.getSpecialText()))
                return model.getSpecialValue();

            return super.stringToValue(text);
        }

        @Override
        public String valueToString(final Object value) throws ParseException {
            if ((value != null) && value.equals(model.getSpecialValue()))
                return model.getSpecialText();

            return super.valueToString(value);
        }
    }

    public SpecialValueSpinnerEditor(final SpecialValueSpinner spinner) {
        this(spinner, (DecimalFormat) NumberFormat.getInstance(spinner.getLocale()));
    }

    public SpecialValueSpinnerEditor(final SpecialValueSpinner spinner, final String decimalFormatPattern) {
        this(spinner, new DecimalFormat(decimalFormatPattern));
    }

    private SpecialValueSpinnerEditor(final SpecialValueSpinner spinner, final DecimalFormat format) {
        super(spinner);

        if (!(spinner.getModel() instanceof final SpecialValueSpinnerModel model))
            throw new IllegalArgumentException("model not a SpecialValueSpinnerModel");

        final NumberFormatter formatter = new NumberEditorFormatter(model, format);
        final DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
        final JFormattedTextField ftf = getTextField();
        ftf.setEditable(true);
        ftf.setFormatterFactory(factory);
        ftf.setHorizontalAlignment(SwingConstants.RIGHT);

        /*
         * TBD - initializing the column width of the text field
         * is imprecise and doing it here is tricky because
         * the developer may configure the formatter later.
         */
        try {
            final String maxString = formatter.valueToString(model.getMinimum());
            final String minString = formatter.valueToString(model.getMaximum());
            ftf.setColumns(Math.max(maxString.length(), minString.length()));
        }
        catch (final ParseException e) {
            // TBD should throw a chained error here
        }

    }

    public DecimalFormat getFormat() {
        return (DecimalFormat) ((NumberFormatter) (getTextField().getFormatter())).getFormat();
    }

    public SpecialValueSpinnerModel getModel() {
        return (SpecialValueSpinnerModel) (getSpinner().getModel());
    }
}
