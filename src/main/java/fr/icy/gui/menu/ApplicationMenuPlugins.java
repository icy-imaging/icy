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

package fr.icy.gui.menu;

import fr.icy.extension.ExtensionLoader;
import fr.icy.extension.plugin.PluginDescriptor;
import fr.icy.gui.action.GeneralActions;
import fr.icy.gui.action.PreferencesActions;
import fr.icy.gui.component.icon.IcySVG;
import fr.icy.gui.component.menu.IcyMenu;
import fr.icy.gui.component.menu.IcyMenuItem;
import fr.icy.gui.component.menu.IcyPluginMenuItem;
import org.jetbrains.annotations.Contract;

import javax.swing.*;
import java.util.*;

/**
 * @author Thomas Musset
 */
public final class ApplicationMenuPlugins extends AbstractApplicationMenu {
    private static final ApplicationMenuPlugins instance = new ApplicationMenuPlugins();

    @Contract(pure = true)
    public static synchronized ApplicationMenuPlugins getInstance() {
        return instance;
    }

    private final IcyMenuItem itemPluginsSettings;
    private final IcyMenuItem itemPluginsReload;
    //private final IcyMenuItem itemCatalog;
    //private final IcyTextFieldHint itemPluginSearch;

    private ApplicationMenuPlugins() {
        super("Plugins");

        itemPluginsSettings = new IcyMenuItem(PreferencesActions.pluginPreferencesAction, IcySVG.SETTINGS);
        itemPluginsReload = new IcyMenuItem(GeneralActions.reloadPluginsAction, IcySVG.REPEAT);
        //itemCatalog = new IcyMenuItem(GeneralActions.catalogAction, IcySVG.WIDGETS);

        //itemPluginSearch = new IcyTextFieldHint(SVGIcon.SEARCH, "Search Plugin…");
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
            add(itemPluginsReload);
            //add(itemCatalog);
            //add(itemPluginSearch);

            final Set<PluginDescriptor> plugins = ExtensionLoader.getActionablePlugins();

            addSeparator();

            if (plugins.isEmpty()) {
                final JMenuItem noPluginsMenuItem = new JMenuItem("No extensions found");
                noPluginsMenuItem.setEnabled(false);
                noPluginsMenuItem.setFocusable(false);
                add(noPluginsMenuItem);
                setEnabled(true);
                return;
            }

            final Map<String, List<PluginDescriptor>> categoryListMap = new TreeMap<>();
            for (final PluginDescriptor descriptor : plugins) {
                final String category = descriptor.getCategory();

                if (categoryListMap.containsKey(category)) {
                    final List<PluginDescriptor> pluginList = categoryListMap.get(category);
                    if (!pluginList.contains(descriptor))
                        pluginList.add(descriptor);
                }
                else {
                    final List<PluginDescriptor> pluginList = new ArrayList<>();
                    pluginList.add(descriptor);

                    categoryListMap.put(category, pluginList);
                }
            }

            for (final Map.Entry<String, List<PluginDescriptor>> categoryListEntry : categoryListMap.entrySet()) {
                final IcyMenu menuCategory = new IcyMenu(categoryListEntry.getKey(), IcySVG.FOLDER);
                for (final PluginDescriptor plugin : categoryListEntry.getValue()) {
                    final IcyPluginMenuItem itemPlugin = new IcyPluginMenuItem(plugin);
                    if (!plugin.getDescription().isBlank())
                        itemPlugin.setToolTipText("<html>" + plugin.getDescription() + "</html>");
                    menuCategory.add(itemPlugin);
                }
                add(menuCategory);
            }

            setEnabled(true);
        });
    }

    @Override
    public void extensionLoaderChanged(final ExtensionLoader.ExtensionLoaderEvent e) {
        reloadPluginsMenu();
    }
}
