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
package org.bioimageanalysis.icy.gui.component.editor;

import org.bioimageanalysis.icy.gui.component.spinner.SpecialValueSpinner;
import org.bioimageanalysis.icy.gui.component.model.SpecialValueSpinnerModel;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 * @author Stephane
 */
public class SpecialValueSpinnerEditor extends DefaultEditor
{
    /**
     * 
     */
    private static final long serialVersionUID = -2378027484728815432L;

    private static class NumberEditorFormatter extends NumberFormatter
    {
        /**
         * 
         */
        private static final long serialVersionUID = 723313251195326099L;

        private final SpecialValueSpinnerModel model;

        NumberEditorFormatter(SpecialValueSpinnerModel model, NumberFormat format)
        {
            super(format);
            this.model = model;
            setValueClass(model.getValue().getClass());
        }

        @Override
        public void setMinimum(Comparable min)
        {
            model.setMinimum(min);
        }

        @Override
        public Comparable getMinimum()
        {
            return model.getMinimum();
        }

        @Override
        public void setMaximum(Comparable max)
        {
            model.setMaximum(max);
        }

        @Override
        public Comparable getMaximum()
        {
            return model.getMaximum();
        }

        @Override
        public Object stringToValue(String text) throws ParseException
        {
            if ((text != null) && text.equalsIgnoreCase(model.getSpecialText()))
                return model.getSpecialValue();

            return super.stringToValue(text);
        }

        @Override
        public String valueToString(Object value) throws ParseException
        {
            if ((value != null) && value.equals(model.getSpecialValue()))
                return model.getSpecialText();

            return super.valueToString(value);
        }
    }

    public SpecialValueSpinnerEditor(SpecialValueSpinner spinner)
    {
        this(spinner, (DecimalFormat) NumberFormat.getInstance(spinner.getLocale()));
    }

    public SpecialValueSpinnerEditor(SpecialValueSpinner spinner, String decimalFormatPattern)
    {
        this(spinner, new DecimalFormat(decimalFormatPattern));
    }

    private SpecialValueSpinnerEditor(SpecialValueSpinner spinner, DecimalFormat format)
    {
        super(spinner);

        if (!(spinner.getModel() instanceof SpecialValueSpinnerModel))
            throw new IllegalArgumentException("model not a SpecialValueSpinnerModel");

        SpecialValueSpinnerModel model = (SpecialValueSpinnerModel) spinner.getModel();
        NumberFormatter formatter = new NumberEditorFormatter(model, format);
        DefaultFormatterFactory factory = new DefaultFormatterFactory(formatter);
        JFormattedTextField ftf = getTextField();
        ftf.setEditable(true);
        ftf.setFormatterFactory(factory);
        ftf.setHorizontalAlignment(SwingConstants.RIGHT);

        /*
         * TBD - initializing the column width of the text field
         * is imprecise and doing it here is tricky because
         * the developer may configure the formatter later.
         */
        try
        {
            String maxString = formatter.valueToString(model.getMinimum());
            String minString = formatter.valueToString(model.getMaximum());
            ftf.setColumns(Math.max(maxString.length(), minString.length()));
        }
        catch (ParseException e)
        {
            // TBD should throw a chained error here
        }

    }

    public DecimalFormat getFormat()
    {
        return (DecimalFormat) ((NumberFormatter) (getTextField().getFormatter())).getFormat();
    }

    public SpecialValueSpinnerModel getModel()
    {
        return (SpecialValueSpinnerModel) (getSpinner().getModel());
    }
}
