/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

package icy.gui.menu;

import icy.gui.component.menu.IcyPluginMenuItem;
import icy.plugin.PluginDescriptor;

/**
 * This class represent a MenuItem which launch a plugin when pressed
 * @deprecated USe {@link IcyPluginMenuItem} instead
 */
@Deprecated(since = "3.0.0", forRemoval = true)
public class PluginMenuItem extends IcyPluginMenuItem {
    public PluginMenuItem(final PluginDescriptor pluginDescriptor) {
        super(pluginDescriptor);
    }
}
