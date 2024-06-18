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

package org.bioimageanalysis.icy.gui.menu;

import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.PluginLoader;
import org.bioimageanalysis.icy.gui.action.PreferencesActions;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenu;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenuItem;
import org.bioimageanalysis.icy.gui.component.menu.IcyPluginMenuItem;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Thomas Musset
 */
public final class ApplicationMenuPlugins extends AbstractApplicationMenu {
    @NotNull
    private static final ApplicationMenuPlugins instance = new ApplicationMenuPlugins();

    @NotNull
    public static synchronized ApplicationMenuPlugins getInstance() {
        return instance;
    }

    @NotNull
    private final IcyMenuItem itemPluginsSettings;
    //@NotNull
    //private final IcyTextFieldHint itemPluginSearch;

    private ApplicationMenuPlugins() {
        super("Plugins");

        itemPluginsSettings = new IcyMenuItem(PreferencesActions.pluginPreferencesAction, SVGIcon.SETTINGS);

        //itemPluginSearch = new IcyTextFieldHint(SVGIcon.SEARCH, "Search Plugin...");
        //itemPluginSearch.setEnabled(false);

        // wait for plugin reload
        setEnabled(false);

        addPluginLoaderListener();

        PluginLoader.reloadAsynch();
    }

    private void reloadPluginsMenu() {
        ThreadUtil.invokeLater(() -> {
            removeAll();

            add(itemPluginsSettings);
            //add(itemPluginSearch);

            addSeparator();

            final List<PluginDescriptor> plugins = PluginLoader.getActionablePlugins();
            //final List<PluginDescriptor> plugins = PluginLoader.getPlugins(true);

            final Map<Character, List<PluginDescriptor>> characterListMap = new TreeMap<>();
            for (final PluginDescriptor descriptor : plugins) {
                final String className = descriptor.getName();
                final Character firstChar = className.charAt(0);

                if (characterListMap.containsKey(firstChar)) {
                    final List<PluginDescriptor> pluginList = characterListMap.get(firstChar);
                    if (!pluginList.contains(descriptor))
                        pluginList.add(descriptor);
                }
                else {
                    final List<PluginDescriptor> pluginList = new ArrayList<>();
                    pluginList.add(descriptor);

                    characterListMap.put(firstChar, pluginList);
                }
            }

            for (final Map.Entry<Character, List<PluginDescriptor>> characterListEntry : characterListMap.entrySet()) {
                final IcyMenu menuFirstChar = new IcyMenu(characterListEntry.getKey().toString(), SVGIcon.FOLDER);
                for (final PluginDescriptor plugin : characterListEntry.getValue()) {
                    final IcyPluginMenuItem itemPlugin = new IcyPluginMenuItem(plugin);
                    if (!plugin.getShortDescription().isBlank())
                        itemPlugin.setToolTipText("<html>" + plugin.getShortDescription() + "</html>");
                    menuFirstChar.add(itemPlugin);
                }
                add(menuFirstChar);
            }

            setEnabled(true);
        });
    }

    @Override
    public void pluginLoaderChanged(final PluginLoader.PluginLoaderEvent e) {
        setEnabled(false);
        reloadPluginsMenu();
    }
}
