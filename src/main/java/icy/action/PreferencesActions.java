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

package icy.action;

import icy.gui.preferences.*;
import icy.util.ClassUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Preferences actions.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public final class PreferencesActions {
    public static final @NotNull IcyAbstractAction preferencesAction = new IcyAbstractAction(
            "Preferences...",
            "Show the preferences window",
            "Setup Icy preferences"
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new PreferenceFrame(GeneralPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static final @NotNull IcyAbstractAction generalPreferencesAction = new IcyAbstractAction(
            "General Preferences...",
            "Show the general preferences window",
            "Setup general setting as font size, automatic update, maximum memory..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new PreferenceFrame(GeneralPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static final @NotNull IcyAbstractAction canvasPreferencesAction = new IcyAbstractAction(
            "Canvas Preferences...",
            "Show the canvas preferences window",
            "Setup canvas setting as filtering, mouse wheel sensivity and reverse mouse axis..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new PreferenceFrame(GUICanvasPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static final @NotNull IcyAbstractAction magicWandPreferencesAction = new IcyAbstractAction(
            "Magic Wand Preferences...",
            "Show the Magic Wand preferences window",
            "Setup Magic Wand advanced setting as connectivity, gradient tolerance..."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new PreferenceFrame(MagicWandPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static final @NotNull IcyAbstractAction networkPreferencesAction = new IcyAbstractAction(
            "Network Preferences...",
            "Show the network preferences window",
            "Setup network setting as proxy server."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new PreferenceFrame(NetworkPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static final @NotNull IcyAbstractAction pluginPreferencesAction = new IcyAbstractAction(
            "Plugin Preferences...",
            "Show the plugin preferences window",
            "Setup plugin setting as automatic update and enable beta version."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new PreferenceFrame(PluginPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static final @NotNull IcyAbstractAction localPluginPreferencesAction = new IcyAbstractAction(
            "Local Plugin Preferences...",
            "Show the local plugin window",
            "Browse, remove, update and show informations about installed plugin."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new PreferenceFrame(PluginLocalPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static final @NotNull IcyAbstractAction onlinePluginPreferencesAction = new IcyAbstractAction(
            "Online Plugin Preferences...",
            "Show the online plugin window",
            "Browse online plugins and install them."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new PreferenceFrame(PluginOnlinePreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static final @NotNull IcyAbstractAction startupPluginPreferencesAction = new IcyAbstractAction(
            "Startup Plugin Preferences...",
            "Show the startup plugin window",
            "Enable / disable startup plugins."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new PreferenceFrame(PluginStartupPreferencePanel.NODE_NAME);
            return true;
        }
    };

    public static final @NotNull IcyAbstractAction repositoryPreferencesAction = new IcyAbstractAction(
            "Repository Preferences...",
            "Show the repository preferences window",
            "Add, edit or remove repository address."
    ) {
        @Override
        public boolean doAction(final ActionEvent e) {
            new PreferenceFrame(RepositoryPreferencePanel.NODE_NAME);
            return true;
        }
    };

    /**
     * Return all actions of this class
     */
    @Deprecated(forRemoval = true)
    public static @NotNull List<IcyAbstractAction> getAllActions() {
        final List<IcyAbstractAction> result = new ArrayList<>();

        for (final Field field : PreferencesActions.class.getFields()) {
            final Class<?> type = field.getType();

            try {
                if (ClassUtil.isSubClass(type, IcyAbstractAction[].class))
                    result.addAll(Arrays.asList(((IcyAbstractAction[]) field.get(null))));
                else if (ClassUtil.isSubClass(type, IcyAbstractAction.class))
                    result.add((IcyAbstractAction) field.get(null));
            }
            catch (final Exception e) {
                // ignore
            }
        }

        return result;
    }
}
