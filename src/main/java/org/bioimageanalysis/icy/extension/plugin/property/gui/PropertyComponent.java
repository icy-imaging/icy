/*
 * Copyright (c) 2010-2025. Institut Pasteur.
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

package org.bioimageanalysis.icy.extension.plugin.property.gui;

import org.bioimageanalysis.icy.extension.plugin.property.Property;
import org.bioimageanalysis.icy.gui.component.button.IcyButton;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class PropertyComponent<C extends JComponent, V> extends JPanel implements ActionListener {
    @NotNull
    protected final Property<V> property;

    @NotNull
    protected final C component;
    @NotNull
    protected final V oldValue;

    @NotNull
    protected final IcyButton resetButton;
    @NotNull
    protected final IcyButton defaultButton;

    PropertyComponent(@NotNull final Property<V> property) {
        super(new GridBagLayout());

        this.property = property;
        this.oldValue = property.getValue();

        setToolTipText(property.getDescription());

        final GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(new JLabel(property.getName()), gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        component = createComponent();
        add(component, gbc);


        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        resetButton = new IcyButton(SVGResource.HISTORY);
        resetButton.addActionListener(this);
        //resetButton.setEnabled(false);
        add(resetButton, gbc);

        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        defaultButton = new IcyButton(SVGResource.DELETE);
        defaultButton.addActionListener(this);
        /*if (oldValue == property.getDefaultValue())
            defaultButton.setEnabled(false);*/
        add(defaultButton, gbc);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public final void actionPerformed(@NotNull final ActionEvent e) {
        if (e.getSource().equals(resetButton))
            resetValue();
        else if (e.getSource().equals(defaultButton))
            resetToDefault();
    }

    public void save() {
        final V value = getValue();
        property.setValue(value);
    }

    @NotNull
    protected abstract C createComponent();

    @Nullable
    protected abstract V getValue();

    protected abstract void resetValue();

    protected abstract void resetToDefault();
}
