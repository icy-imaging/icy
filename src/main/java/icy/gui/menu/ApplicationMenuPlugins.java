package icy.gui.menu;

import icy.action.PreferencesActions;
//import icy.common.Version;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.system.thread.ThreadUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class ApplicationMenuPlugins extends AbstractApplicationMenu {
    private final JMenuItem itemPluginsSettings;

    public ApplicationMenuPlugins() {
        super("Plugins");

        itemPluginsSettings = new JMenuItem("Plugins Settings...");
        setIcon(itemPluginsSettings, GoogleMaterialDesignIcons.EXTENSION);
        itemPluginsSettings.addActionListener(PreferencesActions.onlinePluginPreferencesAction);

        reloadPluginsMenu();

        addSkinChangeListener();
        addPluginLoaderListener();
    }

    private void reloadPluginsMenu() {
        removeAll();

        add(itemPluginsSettings);

        addSeparator();

        ThreadUtil.invokeLater(() -> {
            final Map<String, Map<String, Map<String, PluginDescriptor>>> authorMap = new TreeMap<>();
            final List<PluginDescriptor> plugins = PluginLoader.getActionablePlugins();
            for (final PluginDescriptor descriptor : plugins) {

                String authorName = descriptor.getAuthor();
                String className = descriptor.getSimpleClassName();
                //Version version = descriptor.getVersion();
                final String pluginName = descriptor.getName();

                // Check if author is empty
                if (authorName.isEmpty()) {
                    // Get JAR path
                    final String pathToJAR = descriptor.getPluginJarPath();
                    final String JARExtension = descriptor.getJarExtension();
                    final int indexOfExtension = pathToJAR.lastIndexOf(JARExtension);
                    // Change JAR path to XML path (replace .jar with .xml)
                    final String pathToXML = pathToJAR.substring(0, indexOfExtension) + ".xml";
                    final File XMLFile = new File(pathToXML);
                    // Check if XML file exists and is not a folder
                    if (XMLFile.isFile()) {
                        // Create a temp pluginDescriptor that contains XML data
                        final PluginDescriptor tempDescriptor = new PluginDescriptor();
                        try {
                            // Attempt to load from XML
                            tempDescriptor.loadFromXML(XMLFile.toURI().toURL());
                            // Replace wrong data from actual plugin with XML data
                            descriptor.setIconUrl(tempDescriptor.getIconUrl());
                            authorName = tempDescriptor.getAuthor();
                            className = tempDescriptor.getSimpleClassName();
                            //version = tempDescriptor.getVersion();
                        }
                        catch (IOException ex) {
                            authorName = "???";
                        }
                    }
                    if (authorName.isEmpty())
                        authorName = "???";
                }

                // Reformat author name (remove username and " - ")
                if (!authorName.equals("???")) {
                    final String emDash = " — ";
                    final String enDash = " – ";
                    final String hyphen = " - ";

                    if (authorName.contains(emDash))
                        authorName = authorName.substring(authorName.indexOf(emDash)).substring(3);
                    else if (authorName.contains(enDash))
                        authorName = authorName.substring(authorName.indexOf(enDash)).substring(3);
                    else if (authorName.contains(hyphen))
                        authorName = authorName.substring(authorName.indexOf(hyphen)).substring(3);
                }

                // Concat version with classname (may be useless)
                //final String finalClassName = className + " (v" + version + ")";
                final String finalClassName = className;

                if (authorMap.containsKey(authorName)) {
                    final Map<String, Map<String, PluginDescriptor>> classMap = authorMap.get(authorName);

                    if (classMap.containsKey(finalClassName)) {
                        final Map<String, PluginDescriptor> pluginList = classMap.get(finalClassName);

                        if (!pluginList.containsKey(pluginName))
                            pluginList.put(pluginName, descriptor);
                    }
                    else {
                        final Map<String, PluginDescriptor> pluginList = new TreeMap<>();

                        pluginList.put(pluginName, descriptor);
                        classMap.put(finalClassName, pluginList);
                    }
                }
                else {
                    final Map<String, Map<String, PluginDescriptor>> classMap = new TreeMap<>();
                    final Map<String, PluginDescriptor> pluginList = new TreeMap<>();

                    pluginList.put(pluginName, descriptor);
                    classMap.put(finalClassName, pluginList);
                    authorMap.put(authorName, classMap);
                }
            }

            for (final Map.Entry<String, Map<String, Map<String, PluginDescriptor>>> authorEntry : authorMap.entrySet()) {
                final JMenu menuAuthor = new JMenu(authorEntry.getKey());
                for (final Map.Entry<String, Map<String, PluginDescriptor>> classEntry : authorEntry.getValue().entrySet()) {
                    final JMenu menuClassName = new JMenu(classEntry.getKey());
                    for (final Map.Entry<String, PluginDescriptor> plugin : classEntry.getValue().entrySet()) {
                        if (menuClassName.getIcon() == null)
                            menuClassName.setIcon(new ImageIcon(plugin.getValue().getIcon().getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
                        final JMenuItem itemPlugin = new JMenuItem(plugin.getKey());
                        itemPlugin.addActionListener(e -> PluginLauncher.start(plugin.getValue()));
                        menuClassName.add(itemPlugin);
                    }
                    menuAuthor.add(menuClassName);
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
