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

package org.bioimageanalysis.icy.extension.plugin;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor.PluginIdent;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor.PluginNameSorter;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor.PluginOnlineIdent;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.network.NetworkUtil;
import org.bioimageanalysis.icy.network.URLUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.preferences.PluginPreferences;
import org.bioimageanalysis.icy.system.preferences.RepositoryPreferences;
import org.bioimageanalysis.icy.system.preferences.RepositoryPreferences.RepositoryInfo;
import org.bioimageanalysis.icy.system.thread.SingleProcessor;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.swing.event.EventListenerList;
import java.util.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class PluginRepositoryLoader {
    public interface PluginRepositoryLoaderListener extends EventListener {
        void pluginRepositeryLoaderChanged(PluginDescriptor plugin);
    }

    private class Loader implements Runnable {
        public Loader() {
            super();
        }

        @Override
        public void run() {
            final List<PluginDescriptor> newPlugins = new ArrayList<>();

            try {
                final List<RepositoryInfo> repositories = RepositoryPreferences.getRepositeries();

                // load online plugins from all active repositories
                for (final RepositoryInfo repoInfo : repositories) {
                    // reload requested --> stop current loading
                    if (processor.hasWaitingTasks())
                        return;

                    if (repoInfo.isEnabled()) {
                        final List<PluginDescriptor> pluginsRepos = loadInternal(repoInfo);

                        if (pluginsRepos == null) {
                            failed = true;
                            return;
                        }

                        newPlugins.addAll(pluginsRepos);
                    }
                }

                // sort list on plugin class name
                newPlugins.sort(PluginNameSorter.instance);

                plugins.clear();
                plugins.addAll(newPlugins);
                //plugins = newPlugins;
            }
            catch (final Exception e) {
                IcyLogger.error(PluginRepositoryLoader.class, e, e.getLocalizedMessage());
                failed = true;
                return;
            }

            // notify basic data has been loaded
            loaded = true;
            changed(null);
        }
    }

    private static final String ID_ROOT = "plugins";
    private static final String ID_PLUGIN = "plugin";
    // private static final String ID_PATH = "path";

    /**
     * static class
     */
    private static final PluginRepositoryLoader instance = new PluginRepositoryLoader();

    /**
     * Online plugin list
     */
    final List<PluginDescriptor> plugins;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * internals
     */
    boolean loaded;
    boolean failed;

    private final Loader loader;
    final SingleProcessor processor;

    /**
     * static class
     */
    private PluginRepositoryLoader() {
        super();

        plugins = new ArrayList<>();
        listeners = new EventListenerList();

        loader = new Loader();
        processor = new SingleProcessor(true, "Online Plugin Loader");

        loaded = false;
        // initial loading
        load();
    }

    /**
     * Return the plugins identifier list from a repository URL
     */
    public static List<PluginOnlineIdent> getPluginIdents(final RepositoryInfo repos) {
        String address = repos.getLocation();
        final boolean networkAddr = URLUtil.isNetworkURL(address);
        final boolean betaAllowed = PluginPreferences.getAllowBeta();

        if (networkAddr && repos.getSupportParam()) {
            // prepare parameters for plugin list request
            final Map<String, String> values = new HashMap<>();

            // add plugins.kernel information parameter
            values.put(NetworkUtil.ID_KERNELVERSION, Icy.VERSION.toString());
            // add beta allowed information parameter
            values.put(NetworkUtil.ID_BETAALLOWED, Boolean.toString(betaAllowed));
            // concat to address
            address += "?" + NetworkUtil.getContentString(values);
        }

        // load the XML file
        final Document document = XMLUtil.loadDocument(address, repos.getAuthenticationInfo(), false);

        // error
        if (document == null) {
            if (networkAddr && !NetworkUtil.hasInternetAccess())
                IcyLogger.warn(PluginRepositoryLoader.class, "You are not connected to internet.");

            return null;
        }

        final List<PluginOnlineIdent> result = new ArrayList<>();
        // get plugins node
        final Node pluginsNode = XMLUtil.getElement(document.getDocumentElement(), ID_ROOT);

        // plugins node found
        if (pluginsNode != null) {
            // ident nodes
            final List<Node> nodes = XMLUtil.getChildren(pluginsNode, ID_PLUGIN);

            for (final Node node : nodes) {
                final PluginOnlineIdent ident = new PluginOnlineIdent();

                ident.loadFromXML(node);

                // accept only if not empty
                if (!ident.isEmpty()) {
                    // accept only if required plugins.kernel version is ok and beta accepted
                    if (ident.getRequiredKernelVersion().isLowerOrEqual(Icy.VERSION)
                            && (betaAllowed || (!ident.getVersion().isNotRelease()))) {
                        // check if we have several version of the same plugin
                        final int ind = PluginIdent.getIndex(result, ident.getClassName());
                        // other version found ?
                        if (ind != -1) {
                            // replace old version if needed
                            if (result.get(ind).isLowerOrEqual(ident))
                                result.set(ind, ident);
                        }
                        else
                            result.add(ident);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Do loading process.
     */
    private void load() {
        loaded = false;
        failed = false;

        processor.submit(loader);
    }

    /**
     * Reload all plugins from all active repositories (old list is cleared).<br>
     * Asynchronous process, use {@link #waitLoaded()} method to wait for basic data to be loaded.
     */
    public static synchronized void reload() {
        instance.load();
    }

    /**
     * Load and return the list of online plugins located at specified repository
     */
    static List<PluginDescriptor> loadInternal(final RepositoryInfo repos) {
        // we start by loading only identifier part
        final List<PluginOnlineIdent> idents = getPluginIdents(repos);

        // error while retrieving identifiers ?
        if (idents == null) {
            IcyLogger.error(PluginRepositoryLoader.class, "Can't access repository '" + repos.getName() + "'");
            return null;
        }

        final List<PluginDescriptor> result = new ArrayList<>();

        for (final PluginOnlineIdent ident : idents) {
            try {
                result.add(new PluginDescriptor(ident, repos));
            }
            catch (final Exception e) {
                IcyLogger.error(PluginRepositoryLoader.class, e, "PluginRepositoryLoader.load('" + repos.getLocation() + "') error.");
            }
        }

        return result;
    }

    /**
     * @return the pluginList
     */
    public static ArrayList<PluginDescriptor> getPlugins() {
        synchronized (instance.plugins) {
            return new ArrayList<>(instance.plugins);
        }
    }

    public static PluginDescriptor getPlugin(final String className) {
        synchronized (instance.plugins) {
            return PluginDescriptor.getPlugin(instance.plugins, className);
        }
    }

    public static List<PluginDescriptor> getPlugins(final String className) {
        synchronized (instance.plugins) {
            return PluginDescriptor.getPlugins(instance.plugins, className);
        }
    }

    /**
     * Return the plugins list from the specified repository
     */
    public static List<PluginDescriptor> getPlugins(final RepositoryInfo repos) {
        final List<PluginDescriptor> result = new ArrayList<>();

        synchronized (instance.plugins) {
            for (final PluginDescriptor plugin : instance.plugins)
                if (plugin.getRepository().equals(repos))
                    result.add(plugin);
        }

        return result;
    }

    /**
     * @return true if loader is loading the basic informations
     */
    public static boolean isLoading() {
        return instance.processor.isProcessing();
    }

    /**
     * @return true if basic informations (class names, versions...) are loaded.
     */
    public static boolean isLoaded() {
        return instance.failed || instance.loaded;
    }

    /**
     * Wait until basic informations are loaded.
     */
    public static void waitLoaded() {
        while (!isLoaded())
            ThreadUtil.sleep(10);
    }

    /**
     * Returns true if an error occurred during the plugin loading process.
     */
    public static boolean failed() {
        return instance.failed;
    }

    /**
     * Plugin list has changed
     */
    void changed(final PluginDescriptor plugin) {
        fireEvent(plugin);
    }

    /**
     * Add a listener
     */
    public static void addListener(final PluginRepositoryLoaderListener listener) {
        synchronized (instance.listeners) {
            instance.listeners.add(PluginRepositoryLoaderListener.class, listener);
        }
    }

    /**
     * Remove a listener
     */
    public static void removeListener(final PluginRepositoryLoaderListener listener) {
        synchronized (instance.listeners) {
            instance.listeners.remove(PluginRepositoryLoaderListener.class, listener);
        }
    }

    /**
     * fire event
     */
    private void fireEvent(final PluginDescriptor plugin) {
        for (final PluginRepositoryLoaderListener listener : listeners.getListeners(PluginRepositoryLoaderListener.class))
            listener.pluginRepositeryLoaderChanged(plugin);
    }
}
