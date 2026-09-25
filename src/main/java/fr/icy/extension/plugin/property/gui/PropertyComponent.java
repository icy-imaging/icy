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

package fr.icy.extension.plugin.property.gui;

import fr.icy.extension.plugin.property.Property;
import fr.icy.gui.component.button.IcyButton;
import fr.icy.gui.component.icon.IcySVG;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Abstract class representing a component that interacts with a {@link Property}.
 * This is a generic class that bridges the gap between a UI component and the underlying data model
 * by providing functionality to display, edit, reset, and save property values.
 *
 * @param <C> the type of {@link JComponent} used to edit the property value.
 * @param <V> the type of the property value.
 * @author Thomas Musset
 */
public abstract class PropertyComponent<C extends JComponent, V> extends JPanel implements ActionListener {
    /**
     * Represents the {@link Property} instance associated with this component.
     * This property serves as the core data model, holding the value being edited, displayed,
     * or interacted with by the component. It provides essential metadata such as the property name
     * and description, as well as access to the current, default, and modified values.
     *
     * <p>The property is final and immutable to ensure it remains consistent throughout the component's
     * lifecycle. All operations related to retrieving or updating values are facilitated through this
     * property.
     */
    protected final @NonNull Property<V> property;
    /**
     * Stores the previous value of the property managed by the component. This value
     * is used for operations such as resetting the property to its prior state or
     * comparing changes in the property's value.
     * <p>
     * The value is always non-null and represents a snapshot of the last known
     * state of the property. It is updated as necessary when interactions with
     * the component require tracking or restoring the original property value.
     */
    protected final @NonNull V oldValue;

    /**
     * The graphical component used for rendering and managing interactions
     * with the property value. It is created and configured by the
     * {@code createComponent()} method of the extending class, and it
     * reflects the current state of the associated property.
     * <p>
     * This field is immutable and must not be null.
     * It provides the primary interface for users to view and modify
     * the value of the property in the UI.
     */
    protected final @NonNull C component;

    /**
     * Button component for resetting a property to its original or default value.
     * This button is intended to provide a user interface element that enables
     * resetting configurable properties within the associated PropertyComponent.
     * <p>
     * The button triggers its functionality upon user interaction, typically
     * through an action event, and invokes the necessary methods to restore the
     * corresponding property's value.
     * <p>
     * The resetButton is a final field to ensure its immutability and is also
     * annotated with @NonNull to indicate that it should never be null during
     * runtime.
     */
    protected final @NonNull IcyButton resetButton;
    /**
     * A protected, non-null, and final button instance used to represent or trigger a default action
     * associated with the {@code PropertyComponent}.
     * This button is intended to restore or reset the associated property to its default value
     * when triggered.
     */
    protected final @NonNull IcyButton defaultButton;

    /**
     * Constructs a new PropertyComponent configured with the specified property.
     * The component is initialized with a user interface for interacting with the property,
     * including labels, buttons for resetting the value, or restoring the default value.
     *
     * @param property the property object used to initialize the component. Must not be null.
     */
    PropertyComponent(final @NonNull Property<V> property) {
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
        resetButton = new IcyButton(IcySVG.HISTORY);
        resetButton.addActionListener(this);
        //resetButton.setEnabled(false);
        add(resetButton, gbc);

        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        defaultButton = new IcyButton(IcySVG.DELETE);
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
    public final void actionPerformed(final @NonNull ActionEvent e) {
        if (e.getSource().equals(resetButton))
            resetValue();
        else if (e.getSource().equals(defaultButton))
            resetToDefault();
    }

    /**
     * Saves the current value of the component into the associated property.
     * <p>
     * This method retrieves the current value of the component by invoking the {@code getValue()}
     * method and updates the associated property using {@code property.setValue(V value)}.
     * The value stored in the property reflects the user’s current input or selection.
     */
    public void save() {
        final V value = getValue();
        property.setValue(value);
    }

    /**
     * Creates and initializes the component associated with a property.
     * This method is intended to be implemented by subclasses to provide
     * the specific graphical component (e.g., text field, checkbox, spinner)
     * used to represent and manipulate the property value.
     *
     * @return a non-null instance of the component representing the property.
     */
    protected abstract @NonNull C createComponent();

    /**
     * Retrieves the current value from the component associated with the property.
     * This method is intended to be implemented by subclasses to provide the
     * specific logic for extracting the current user input or selected value
     * from the graphical component linked to the property.
     *
     * @return the current value of the component, or {@code null} if the value
     * cannot be determined or no suitable value exists.
     */
    protected abstract @Nullable V getValue();

    /**
     * Resets the value of the associated property component to its last saved state.
     * <p>
     * This method is meant to be implemented by subclasses to define the logic for
     * reverting the graphical component's value to the corresponding {@code oldValue}
     * stored in the property.
     * <p>
     * Typical implementations would update the UI component to reflect the previously
     * stored value without impacting the underlying property's current state.
     */
    protected abstract void resetValue();

    /**
     * Resets the property component to its default value.
     * <p>
     * This method is intended to restore the associated property's value
     * to the predefined default state. Subclasses should implement the
     * behavior to ensure the graphical component reflects this default
     * value, typically by updating its UI component.
     * <p>
     * The default value is typically provided by the property instance
     * associated with the component.
     */
    protected abstract void resetToDefault();
}
