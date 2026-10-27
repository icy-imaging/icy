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

package fr.icy.extension.plugin.interface_;

/**
 * Plugin Threaded interface.<br>
 * <br>
 * By default, a plugin is launched on the AWT Event Dispatch Thread, so GUI creation
 * can be done directly without {@code invokeLater} calls.
 * <p>
 * A common problem is that long processes will actually lock the EDT and make GUI not respond.
 * <p>
 * A plugin implementing this interface will have both its {@code run()} methods called
 * in a separate thread, but the developer has to use {@code invokeLater} method
 * for GUI creation / modification. Also, there is no more guarantee that the plugin constructor is called
 * in the EDT (which is the case for the default plugin).
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
@Deprecated(forRemoval = true)
public interface PluginThreaded extends Runnable {
}
