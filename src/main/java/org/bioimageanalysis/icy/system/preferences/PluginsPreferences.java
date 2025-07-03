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

package org.bioimageanalysis.icy.system.preferences;

import org.bioimageanalysis.icy.common.reflect.ClassUtil;
import org.bioimageanalysis.icy.extension.ExtensionLoader;
import org.bioimageanalysis.icy.extension.plugin.abstract_.Plugin;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class PluginsPreferences {
    /**
     * pref id
     */
    private static final String PREF_ID = "plugins";

    /**
     * preferences
     */
    private static XMLPreferences preferences;

    public static void load() {
        // load preference
        preferences = IcyPreferences.root().node(PREF_ID);
    }

    /**
     * @return the preferences
     */
    public static XMLPreferences getPreferences() {
        return preferences;
    }

    /**
     * @param pluginClass class
     * @return Return root node for specified Plugin class.
     */
    public static XMLPreferences root(final Class<? extends Plugin> pluginClass) {
        if (pluginClass != null) {
            final String className = pluginClass.getName();

            if (className.startsWith(ExtensionLoader.PLUGINS_PACKAGE))
                return preferences.node(ClassUtil.getPathFromQualifiedName(className.substring(ExtensionLoader.PLUGINS_PACKAGE.length() + 1)));
            if (className.startsWith(ExtensionLoader.OLD_PLUGINS_PACKAGE))
                return preferences.node(ClassUtil.getPathFromQualifiedName(className.substring(ExtensionLoader.OLD_PLUGINS_PACKAGE.length() + 1)));
        }

        return null;
    }

    /**
     * @param plugin plugin
     * @return Return root node for specified Plugin
     */
    public static XMLPreferences root(final Plugin plugin) {
        if (plugin != null) {
            final String className = plugin.getClass().getName();

            if (className.startsWith(ExtensionLoader.PLUGINS_PACKAGE))
                return preferences.node(ClassUtil.getPathFromQualifiedName(className.substring(ExtensionLoader.PLUGINS_PACKAGE.length() + 1)));
            if (className.startsWith(ExtensionLoader.OLD_PLUGINS_PACKAGE))
                return preferences.node(ClassUtil.getPathFromQualifiedName(className.substring(ExtensionLoader.OLD_PLUGINS_PACKAGE.length() + 1)));
        }

        return null;
    }
}
