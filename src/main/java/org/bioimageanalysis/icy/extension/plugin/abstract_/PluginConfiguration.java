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

package org.bioimageanalysis.icy.extension.plugin.abstract_;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.extension.plugin.property.Property;
import org.bioimageanalysis.icy.extension.plugin.property.gui.PropertyPanel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public abstract class PluginConfiguration extends Plugin {
    @NotNull
    private final Set<Property<?>> properties;

    public PluginConfiguration() {
        super();
        this.properties = new HashSet<>();
        setProperties(properties);
    }

    protected abstract void setProperties(@NotNull final Set<Property<?>> properties);

    @NotNull
    public final JPanel createConfigurationPanel() throws HeadlessException {
        if (Icy.getMainInterface().isHeadLess() || properties.isEmpty())
            throw new HeadlessException();

        return new PropertyPanel(properties);
    }
}
