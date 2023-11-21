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

package icy.plugin.interface_;

/**
 * This <i>ugly hack</i> interface exists to force the Plugin constructor to be done outside the EDT
 * (Event Dispatch Thread).<br>
 * We need it as by default Plugin instance are created in the EDT (for historical reasons then to
 * preserve backward compatibility) and sometime we really want to avoid it as plugin using many
 * others classes make lock the EDT for severals second just with some heavy class loading work.
 *
 * @author Stephane
 * @author Thomas MUSSET
 */
public interface PluginNoEDTConstructor {
}
