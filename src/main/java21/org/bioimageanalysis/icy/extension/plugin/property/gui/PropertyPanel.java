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

import org.bioimageanalysis.icy.extension.plugin.property.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

public final class PropertyPanel extends JPanel implements ActionListener {
    private final JButton cancelButton;
    private final JButton applyButton;
    private final JButton okButton;

    public PropertyPanel(@NotNull @Unmodifiable final Set<Property<?>> properties) {
        super(new GridLayout(properties.size() + 1, 1, 5, 5));
        for (@NotNull final Property<?> property : properties) {
            @Nullable
            final PropertyComponent<?, ?> pc;
            switch (property) {
                case @NotNull final SelectionProperty<?> selectionProperty -> pc = new PropertyComboBox<>(selectionProperty);
                case @NotNull final NumberProperty<?> numberProperty -> pc = new PropertyNumberSpinner<>(numberProperty);
                case @NotNull final ColorProperty colorProperty -> pc = new PropertyColorChooser(colorProperty);
                case @NotNull final StringProperty stringProperty -> pc = new PropertyTextField(stringProperty);
                case @NotNull final BooleanProperty booleanProperty -> pc = new PropertyCheckBox(booleanProperty);
                default -> pc = null;
            }
            if (pc != null)
                add(property.getName(), pc);
        }

        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add("Cancel Button", cancelButton);
        applyButton = new JButton("Apply");
        applyButton.addActionListener(this);
        buttonPanel.add("Apply Button", applyButton);
        okButton = new JButton("Ok");
        okButton.addActionListener(this);
        buttonPanel.add("OK Button", okButton);

        add("Button Panel", buttonPanel);
    }

    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(@NotNull final ActionEvent e) {
        if (e.getSource().equals(cancelButton)) {
        }
        else if (e.getSource().equals(applyButton)) {
        }
        else if (e.getSource().equals(okButton)) {

        }
    }
}
