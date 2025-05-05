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

package org.bioimageanalysis.icy.gui.component.button;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;

/**
 * Color button used to select a specific color.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ColorChooserButton extends JButton implements ActionListener {
    public interface ColorChangeListener extends EventListener {
        void colorChanged(ColorChooserButton source);
    }

    private String colorChooseText;

    public ColorChooserButton() {
        this(Color.black);
    }

    /**
     * @param color default color
     */
    public ColorChooserButton(final Color color) {
        super();

        // setBorderPainted(false);
        setFocusPainted(false);

        final Dimension dim = new Dimension(24, 18);
        add(new Box.Filler(dim, dim, dim));

        // save color information in background color
        setBackground(color);
        colorChooseText = "Choose color";

        addActionListener(this);
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return getBackground();
    }

    /**
     * @param color the color to set
     */
    public void setColor(final Color color) {
        if (getColor() != color) {
            setBackground(color);
            // notify about color change
            fireColorChanged();
        }
    }

    /**
     * @return the colorChooseText
     */
    public String getColorChooseText() {
        return colorChooseText;
    }

    /**
     * @param colorChooseText the colorChooseText to set
     */
    public void setColorChooseText(final String colorChooseText) {
        this.colorChooseText = colorChooseText;
    }

    protected void fireColorChanged() {
        for (final ColorChangeListener listener : listenerList.getListeners(ColorChangeListener.class))
            listener.colorChanged(this);
    }

    /**
     * Adds a <code>ColorChangeListener</code> to the button.
     *
     * @param l the listener to be added
     */
    public void addColorChangeListener(final ColorChangeListener l) {
        listenerList.add(ColorChangeListener.class, l);
    }

    /**
     * Removes a ColorChangeListener from the button.
     *
     * @param l the listener to be removed
     */
    public void removeColorChangeListener(final ColorChangeListener l) {
        listenerList.remove(ColorChangeListener.class, l);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final Color c = JColorChooser.showDialog(this, colorChooseText, getColor());

        if (c != null)
            setColor(c);
    }

}
