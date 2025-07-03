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

import org.bioimageanalysis.icy.extension.ExtensionLoader;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.gui.action.GeneralActions;
import org.bioimageanalysis.icy.gui.action.PreferencesActions;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenu;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenuItem;
import org.bioimageanalysis.icy.gui.component.menu.IcyPluginMenuItem;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.*;

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
    private final IcyMenuItem itemCatalog;
    //@NotNull
    //private final IcyTextFieldHint itemPluginSearch;

    private ApplicationMenuPlugins() {
        super("Plugins");

        itemPluginsSettings = new IcyMenuItem(PreferencesActions.pluginPreferencesAction, SVGIcon.SETTINGS);
        itemCatalog = new IcyMenuItem(GeneralActions.catalogAction, SVGIcon.WIDGETS);

        //itemPluginSearch = new IcyTextFieldHint(SVGIcon.SEARCH, "Search Plugin...");
        //itemPluginSearch.setEnabled(false);

        // wait for plugin reload
        setEnabled(false);

        addPluginLoaderListener();

        if (!ExtensionLoader.isLoading())
            reloadPluginsMenu();
    }

    private void reloadPluginsMenu() {
        setEnabled(false);
        SwingUtilities.invokeLater(() -> {
            removeAll();

            add(itemPluginsSettings);
            //add(itemCatalog);
            //add(itemPluginSearch);

            final Set<PluginDescriptor> plugins = ExtensionLoader.getActionablePlugins();

            addSeparator();

            if (plugins.isEmpty()) {
                final JMenuItem noPluginsMenuItem = new JMenuItem("No plugin installed");
                noPluginsMenuItem.setEnabled(false);
                noPluginsMenuItem.setFocusable(false);
                add(noPluginsMenuItem);
                setEnabled(true);
                return;
            }

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
    public void extensionLoaderChanged(final ExtensionLoader.ExtensionLoaderEvent e) {
        reloadPluginsMenu();
    }
}
