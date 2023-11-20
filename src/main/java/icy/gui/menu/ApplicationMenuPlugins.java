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

import icy.action.PreferencesActions;
import icy.gui.component.menu.IcyMenu;
import icy.gui.component.menu.IcyMenuItem;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.system.thread.ThreadUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Thomas MUSSET
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
    @NotNull
    private final IcyMenuItem itemPluginSearch;

    private ApplicationMenuPlugins() {
        super("Plugins");

        itemPluginsSettings = new IcyMenuItem("Plugins Settings...", GoogleMaterialDesignIcons.EXTENSION);
        itemPluginsSettings.addActionListener(PreferencesActions.onlinePluginPreferencesAction);

        itemPluginSearch = new IcyMenuItem("Search Plugin...", GoogleMaterialDesignIcons.SEARCH);
        itemPluginSearch.setEnabled(false);

        reloadPluginsMenu();

        addPluginLoaderListener();
    }

    private void reloadPluginsMenu() {
        removeAll();

        add(itemPluginsSettings);
        add(itemPluginSearch);

        addSeparator();

        ThreadUtil.invokeLater(() -> {
            final List<PluginDescriptor> plugins = PluginLoader.getActionablePlugins();

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
                final IcyMenu menuAuthor = new IcyMenu(characterListEntry.getKey().toString());
                for (final PluginDescriptor plugin : characterListEntry.getValue()) {
                    final IcyMenuItem itemPlugin = new IcyMenuItem(plugin.getName());
                    itemPlugin.setIcon(new ImageIcon(plugin.getIcon().getImage().getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
                    itemPlugin.addActionListener(e -> PluginLauncher.start(plugin));
                    menuAuthor.add(itemPlugin);
                }
                add(menuAuthor);
            }
        });
    }

    @Override
    public void pluginLoaderChanged(final PluginLoader.PluginLoaderEvent e) {
        reloadPluginsMenu();
    }
}
