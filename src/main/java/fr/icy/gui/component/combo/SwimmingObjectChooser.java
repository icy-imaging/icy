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

package fr.icy.gui.component.combo;

import fr.icy.Icy;
import fr.icy.common.string.StringUtil;
import fr.icy.model.swimmingPool.SwimmingObject;
import fr.icy.model.swimmingPool.SwimmingPoolEvent;
import fr.icy.model.swimmingPool.SwimmingPoolListener;
import fr.icy.model.swimmingPool.WeakSwimmingPoolListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas Chenouard
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class SwimmingObjectChooser extends JComboBox<Object> implements SwimmingPoolListener {
    @FunctionalInterface
    public interface SwimmingObjectChooserListener {
        void objectChanged(Object object);
    }

    private final List<SwimmingObjectChooserListener> listeners;
    private final Class<?> itemClass;

    public SwimmingObjectChooser(final Class<?> itemClass) {
        this(itemClass, 50, "No valid object to display in SwimmingPool");
    }

    public SwimmingObjectChooser(final Class<?> itemClass, final int maxSize, final String defaultMessage) {
        super();

        this.itemClass = itemClass;
        this.listeners = new ArrayList<>();

        Icy.getMainInterface().getSwimmingPool().addListener(new WeakSwimmingPoolListener(this));

        this.setRenderer((list, value, index, isSelected, cellHasFocus) -> {
            if (value == null)
                return new JLabel(defaultMessage);

            if (value instanceof SwimmingObject) {
                final JLabel label = new JLabel(StringUtil.limit(((SwimmingObject) value).getName(), maxSize));
                label.setToolTipText(((SwimmingObject) value).getName());
                return label;
            }

            return new JLabel(value.toString());
        });
    }

    public Object getSelectedObject() {
        final Object o = getSelectedItem();

        if (o != null)
            return ((SwimmingObject) o).getObject();

        return null;
    }

    @Override
    public void swimmingPoolChangeEvent(final SwimmingPoolEvent event) {
        SwingUtilities.invokeLater(() -> {
            refreshList();

            final SwimmingObject swimObj = event.getResult();

            if (swimObj != null) {
                final Object obj = swimObj.getObject();

                // Select the last entry computed
                if (obj != null)
                    setSelectedItem(obj);
            }
        });

    }

    void refreshList() {
        // save old selection
        final Object oldSelected = getSelectedItem();
        // rebuild model
        setModel(new DefaultComboBoxModel<>(getSwimmingObjects()));
        // restore selection
        setSelectedItem(oldSelected);
    }

    Object[] getSwimmingObjects() {
        final List<Object> objectList = new ArrayList<>();
        final List<SwimmingObject> objects = Icy.getMainInterface().getSwimmingPool().getObjects();

        for (final SwimmingObject so : objects)
            if (itemClass.isInstance(so.getObject()))
                objectList.add(so);

        return objectList.toArray();
    }

    public void addListener(final SwimmingObjectChooserListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(final SwimmingObjectChooserListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void fireItemStateChanged(final ItemEvent e) {
        for (final SwimmingObjectChooserListener listener : listeners)
            listener.objectChanged(getSelectedObject());

        super.fireItemStateChanged(e);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        for (final SwimmingObjectChooserListener listener : listeners)
            listener.objectChanged(getSelectedObject());

        super.actionPerformed(e);
    }
}
