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

package org.bioimageanalysis.icy.extension;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.common.reflect.ClassUtil;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.abstract_.Plugin;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginDaemon;
import org.bioimageanalysis.icy.gui.dialog.ExtensionInstallerDialog;
import org.bioimageanalysis.icy.io.Loader;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.UserUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.preferences.PluginPreferences;
import org.bioimageanalysis.icy.system.thread.SingleProcessor;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.xeustechnologies.jcl.JarClassLoader;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.io.*;
import java.util.*;

public final class ExtensionLoader {
    public final static String PLUGINS_PACKAGE = "icy.plugins";
    public final static String OLD_PLUGINS_PACKAGE = "plugins"; // For legacy compatibility
    public final static String EXTENSIONS_PATH = UserUtil.getIcyExtensionsDirectory().getAbsolutePath();

    public final static String KERNEL_PLUGINS_PACKAGE = PLUGINS_PACKAGE + ".kernel.";
    public final static String OLD_KERNEL_PLUGINS_PACKAGE = OLD_PLUGINS_PACKAGE + ".kernel.";

    /**
     * static class
     */
    private static final ExtensionLoader instance = new ExtensionLoader();

    /**
     * class loader
     */
    private final List<JarClassLoader> loaders = new ArrayList<>();
    /**
     * Loaded extensions set
     */
    private final Set<ExtensionDescriptor> extensions;
    /**
     * Loaded plugins map
     */
    private final Map<String, PluginDescriptor> plugins;
    /**
     * Loaded actionable plugins map
     */
    private final Map<String, PluginDescriptor> actionablePlugins;
    /**
     * Loaded daemons plugins map
     */
    private final Map<String, PluginDescriptor> daemonPlugins;
    /**
     * active daemons plugins
     */
    private final Set<PluginDaemon> activeDaemons;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /*
     * internals
     */
    private final Runnable reloader;
    final SingleProcessor processor;

    private boolean initialized;
    private boolean loading;

    /**
     * static class
     */
    private ExtensionLoader() {
        super();

        initialized = false;
        loading = false;
        // needReload = false;
        // logError = true;

        extensions = new HashSet<>();
        plugins = new HashMap<>();
        actionablePlugins = new HashMap<>();
        daemonPlugins = new HashMap<>();
        activeDaemons = new HashSet<>();
        listeners = new EventListenerList();

        // reloader
        reloader = this::reloadInternal;

        processor = new SingleProcessor(true, "Local Extension Loader");

        // don't load by default as we need Preferences to be ready first
    }

    static void prepare() {
        if (!instance.initialized) {
            if (isLoading())
                waitWhileLoading();
            else
                reload();
        }
    }

    /**
     * Reload the list of installed plugins (asynchronous version).
     */
    public static void reloadAsynch() {
        instance.processor.submit(instance.reloader);
    }

    /**
     * Reload the list of installed plugins (wait for completion).
     */
    public static void reload() {
        instance.processor.submit(instance.reloader);
        // ensure we don't miss the reloading
        ThreadUtil.sleep(500);
        waitWhileLoading();
    }

    void reloadInternal() {
        loading = true;

        stopDaemons();

        // no need to complete loading...
        if (processor.hasWaitingTasks())
            return;

        final Set<ExtensionDescriptor> eds = new HashSet<>();
        final Map<String, PluginDescriptor> pds = new HashMap<>();
        final Map<String, PluginDescriptor> dpds = new HashMap<>();
        final Map<String, PluginDescriptor> apds = new HashMap<>();
        final Set<File> jars = getJarList();
        final List<JarClassLoader> jcls = new ArrayList<>();

        for (final File jar : jars) {
            try {
                final JarClassLoader jcl = new JarClassLoader();
                jcl.add(jar.toURI().toURL());

                final ExtensionDescriptor ed = new ExtensionDescriptor(jcl, jar);
                eds.add(ed);

                if (!ed.getDependencies().isEmpty())
                    loadDependencies(ed);

                final Set<String> classes = Set.copyOf(ClassUtil.findClassNamesInJAR(jar.getAbsolutePath()));
                for (final String className : classes) {
                    if (className.startsWith(PLUGINS_PACKAGE) || className.startsWith(OLD_PLUGINS_PACKAGE)) {
                        try {
                            @SuppressWarnings("unchecked")
                            final Class<? extends Plugin> pluginClass = jcl.loadClass(className, true).asSubclass(Plugin.class);

                            final PluginDescriptor pd = new PluginDescriptor(pluginClass);
                            pd.setExtension(ed);
                            pds.put(className, pd);
                            if (pd.isDaemon())
                                dpds.put(className, pd);
                            if (pd.isActionable())
                                apds.put(className, pd);

                            if (className.startsWith(OLD_PLUGINS_PACKAGE) && !pd.isKernelPlugin())
                                IcyLogger.warn(this.getClass(), "Old plugins package detected for " + ClassUtil.getSimpleClassName(className));
                        }
                        catch (final Throwable e) {
                            // ignored
                        }
                    }
                }

                jcls.add(jcl);
            }
            catch (final Throwable e) {
                IcyLogger.error(this.getClass(), e, "Failed to load extension: " + jar.getAbsolutePath());
            }
        }

        // release loaded resources
        for (final JarClassLoader loader : loaders) {
            final Set<String> key = loader.getLoadedClasses().keySet();
            for (final String className : key) {
                loader.unloadClass(className);
            }
        }

        loaders.clear();
        loaders.addAll(jcls);

        extensions.clear();
        extensions.addAll(eds);

        plugins.clear();
        plugins.putAll(pds);

        actionablePlugins.clear();
        actionablePlugins.putAll(apds);

        daemonPlugins.clear();
        daemonPlugins.putAll(dpds);

        loading = false;

        // notify change
        changed();
    }

    private void loadDependencies(final @NotNull ExtensionDescriptor descriptor) throws IOException {
        final List<Map<String, Object>> dependencies = descriptor.getDependencies();
        for (final Map<String, Object> dependency : dependencies) {
            loadDependency(dependency, descriptor.getJCL());
        }
    }

    private void loadDependency(final @NotNull Map<String, Object> dependency, final JarClassLoader jcl) throws IOException {
        final String groupId = (String) dependency.get("groupId");
        final String artifactId = (String) dependency.get("artifactId");
        final String version = (String) dependency.get("version");

        final File dependencyFile = new File(EXTENSIONS_PATH + File.separator + groupId.replaceAll("\\.", File.separator) + File.separator + artifactId + File.separator + version, artifactId + ".jar");
        // TODO request a download if missing a dependency
        if (dependencyFile.exists() && dependencyFile.isFile()) {
            jcl.add(dependencyFile.toURI().toURL());
            try (final InputStream is = jcl.getResourceAsStream("META-INF/" + groupId + "." + artifactId + "/dependencies.yaml")) {
                final Yaml yaml = new Yaml();
                final List<Map<String, Object>> dependencies = yaml.load(is);
                for (final Map<String, Object> dep : dependencies) {
                    loadDependency(dep, jcl);
                }
            }
        }
        else
            throw new FileNotFoundException("Dependency " + dependencyFile.getAbsolutePath() + " not found");
    }

    /**
     * Stop and restart all daemons plugins.
     */
    public static synchronized void resetDaemons() {
        // reset will be done later
        if (isLoading())
            return;

        stopDaemons();
        startDaemons();
    }

    /**
     * Start daemons plugins.
     */
    static synchronized void startDaemons() {
        // at this point active daemons should be empty !
        synchronized (instance.activeDaemons) {
            if (!instance.activeDaemons.isEmpty())
                stopDaemons();

            final List<String> inactives = PluginPreferences.getInactiveDaemons();
            final Set<PluginDaemon> newDaemons = new HashSet<>();

            for (final PluginDescriptor pluginDesc : getDaemonPlugins()) {
                // not found in inactives ?
                if (!inactives.contains(pluginDesc.getClassName())) {
                    try {
                        final PluginDaemon plugin = (PluginDaemon) pluginDesc.getPluginClass().getDeclaredConstructor().newInstance();
                        final Thread thread = new Thread(plugin, pluginDesc.getName());

                        thread.setName(pluginDesc.getName());
                        // so icy can exit even with running daemon plugin
                        thread.setDaemon(true);

                        // init daemon
                        plugin.init();
                        // start daemon
                        thread.start();
                        // register daemon plugin (so we can stop it later)
                        Icy.getMainInterface().registerPlugin((Plugin) plugin);

                        // add daemon plugin to list
                        newDaemons.add(plugin);
                    }
                    catch (final Throwable t) {
                        IcyExceptionHandler.handleException(pluginDesc, t, true);
                    }
                }
            }

            instance.activeDaemons.clear();
            instance.activeDaemons.addAll(newDaemons);
        }
    }

    /**
     * Stop daemons plugins.
     */
    public synchronized static void stopDaemons() {
        synchronized (instance.activeDaemons) {
            for (final PluginDaemon pluginDaemon : getActiveDaemons()) {
                try {
                    // stop the daemon
                    pluginDaemon.stop();
                }
                catch (final Throwable t) {
                    IcyExceptionHandler.handleException(((Plugin) pluginDaemon).getDescriptor(), t, true);
                }
            }

            // no more active daemons
            instance.activeDaemons.clear();
        }
    }

    /**
     * Return the list of loaded plugins.
     */
    public static @NotNull @Unmodifiable Set<ExtensionDescriptor> getExtensions() {
        prepare();

        // better to return a copy as we have async list loading
        synchronized (instance.extensions) {
            return Set.copyOf(instance.extensions);
        }
    }

    /**
     * @return the loading
     */
    public static boolean isLoading() {
        return instance.processor.hasWaitingTasks() || instance.loading;
    }

    /**
     * wait until loading completed
     */
    public static void waitWhileLoading() {
        while (isLoading())
            ThreadUtil.sleep(100);
    }

    /**
     * Returns <code>true</code> if the specified extension exists in the {@link ExtensionLoader}.
     *
     * @param extension   the extension we are looking for.
     * @param acceptNewer allow newer version of the extension
     */
    public static boolean isLoaded(final @NotNull ExtensionDescriptor extension, final boolean acceptNewer) {
        //return (getExtensions(plugin.getIdent(), acceptNewer) != null);
        boolean loaded = false;

        synchronized (instance.extensions) {
            loaded = instance.extensions.contains(extension);
        }

        return loaded;
    }

    /**
     * Called when class loader changed
     */
    private void changed() {
        // check for missing or mis-installed plugins on first start
        if (!initialized) {
            initialized = true;
            checkExtensions(false);
        }

        startDaemons();
        // notify listener we have changed
        fireEvent(new ExtensionLoader.ExtensionLoaderEvent());

        ThreadUtil.bgRun(() -> {
            // we are still installing / removing plugins or reloading plugin list --> cancel
            /*if (!PluginInstaller.getInstallFIFO().isEmpty() || !PluginInstaller.getRemoveFIFO().isEmpty() || isLoading())
                return;*/

            // pre load the importers classes as they can be heavy
            Loader.getSequenceFileImporters();
            Loader.getFileImporters();
            Loader.getImporters();
        });
    }

    /**
     * Check for missing plugins and install them if needed.
     */
    public static void checkExtensions(final boolean showProgress) {
        final Set<ExtensionDescriptor> extensions = getExtensions();
        final List<ExtensionDescriptor> required = new ArrayList<>();
        final List<ExtensionDescriptor> missings = new ArrayList<>();
        final List<ExtensionDescriptor> faulties = new ArrayList<>();

        //for (final ExtensionDescriptor extension : extensions) {
            // ignore
        //}
    }

    /**
     * Add a listener
     */
    public static void addListener(final ExtensionLoader.ExtensionLoaderListener listener) {
        synchronized (instance.listeners) {
            instance.listeners.add(ExtensionLoader.ExtensionLoaderListener.class, listener);
        }
    }

    /**
     * Remove a listener
     */
    public static void removeListener(final ExtensionLoader.ExtensionLoaderListener listener) {
        synchronized (instance.listeners) {
            instance.listeners.remove(ExtensionLoader.ExtensionLoaderListener.class, listener);
        }
    }

    /**
     * fire event
     */
    void fireEvent(final ExtensionLoader.ExtensionLoaderEvent e) {
        synchronized (instance.listeners) {
            for (final ExtensionLoader.ExtensionLoaderListener listener : instance.listeners.getListeners(ExtensionLoader.ExtensionLoaderListener.class)) {
                listener.extensionLoaderChanged(e);
            }
        }
    }

    public interface ExtensionLoaderListener extends EventListener {
        void extensionLoaderChanged(ExtensionLoader.ExtensionLoaderEvent e);
    }

    public static class ExtensionLoaderEvent {
        @Contract(pure = true)
        public ExtensionLoaderEvent() {
            super();
        }

        @Contract(value = "null -> false", pure = true)
        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof ExtensionLoader.ExtensionLoaderEvent)
                return true;

            return super.equals(obj);
        }
    }

    /**
     * Return the loaders list
     */
    public static List<JarClassLoader> getLoaders() {
        synchronized (instance.loaders) {
            return instance.loaders;
        }
    }

    public static @NotNull Class<?> loadClass(final String className) throws ClassNotFoundException {
        prepare();

        synchronized (instance.loaders) {
            Class<?> result = null;
            for (final JarClassLoader jcl : instance.loaders) {
                try {
                    result = jcl.loadClass(className);
                }
                catch (final ClassNotFoundException e) {
                    // ignore
                }
            }

            if (result == null)
                throw new ClassNotFoundException(className);

            // try to load class
            return result;
        }
    }

    private @NotNull @Unmodifiable Set<File> getJarList() {
        final File indexFile = new File(UserUtil.getIcyExtensionsDirectory(), "extensions.yaml");
        if (!indexFile.exists() || !indexFile.isFile() || !indexFile.canRead())
            return Collections.emptySet();

        final Yaml yaml = new Yaml();
        final List<Map<String, Object>> extensionIndex;
        try {
            extensionIndex = yaml.load(new FileReader(indexFile));
        }
        catch (final FileNotFoundException e) {
            return Collections.emptySet();
        }

        if (extensionIndex == null)
            return Collections.emptySet();

        final Set<File> files = new HashSet<>();
        for (final Map<String, Object> item : extensionIndex) {
            final File jarFile = new File(UserUtil.getIcyExtensionsDirectory(), item.get("path").toString());
            if (jarFile.isFile()) {
                files.add(jarFile);
            }
        }

        return Set.copyOf(files);
    }

    /**
     * Returns the list of daemon type plugins.
     */
    public static @NotNull @Unmodifiable Set<PluginDescriptor> getDaemonPlugins() {
        synchronized (instance.daemonPlugins) {
            return Set.copyOf(instance.daemonPlugins.values());
        }
    }

    /**
     * Returns the list of active daemon plugins.
     */
    @Contract(pure = true)
    public static @NotNull @Unmodifiable Set<PluginDaemon> getActiveDaemons() {
        synchronized (instance.activeDaemons) {
            return Set.copyOf(instance.activeDaemons);
        }
    }

    /**
     * Return the list of loaded plugins.
     */
    public static @NotNull @Unmodifiable Set<PluginDescriptor> getPlugins() {
        prepare();

        // better to return a copy as we have async list loading
        synchronized (instance.plugins) {
            return Set.copyOf(instance.plugins.values());
        }
    }

    /**
     * Return the list of loaded plugins which derive from the specified class.
     *
     * @param clazz The class object defining the class we want plugin derive from.
     */
    public static @NotNull @Unmodifiable Set<PluginDescriptor> getPlugins(final @NotNull Class<?> clazz) {
        return getPlugins(clazz, false, false);
    }

    /**
     * Return the list of loaded plugins which derive from the specified class.
     *
     * @param clazz         The class object defining the class we want plugin derive from.
     * @param wantAbstract  specify if we also want abstract classes
     * @param wantInterface specify if we also want interfaces
     */
    public static @NotNull @Unmodifiable Set<PluginDescriptor> getPlugins(final @NotNull Class<?> clazz, final boolean wantAbstract, final boolean wantInterface) {
        prepare();

        final Set<PluginDescriptor> result = new HashSet<>();

        synchronized (instance.plugins) {
            for (final PluginDescriptor pluginDescriptor : instance.plugins.values()) {
                if (pluginDescriptor.isInstanceOf(clazz)) {
                    // accept class ?
                    if ((wantAbstract || !pluginDescriptor.isAbstract()) && (wantInterface || !pluginDescriptor.isInterface()))
                        result.add(pluginDescriptor);
                }
            }
        }

        return Set.copyOf(result);
    }

    /**
     * Return the list of "actionable" plugins (mean we can launch them from GUI).
     */
    public static @NotNull @Unmodifiable Set<PluginDescriptor> getActionablePlugins() {
        prepare();

        synchronized (instance.actionablePlugins) {
            return Set.copyOf(instance.actionablePlugins.values());
        }
    }

    /**
     * Returns the plugin corresponding to the specified plugin class name.<br>
     * Returns <code>null</code> if the plugin does not exists in any loaders.
     *
     * @param className class name of the plugin we are looking for.
     */
    public static @Nullable PluginDescriptor getPlugin(final String className) {
        prepare();

        synchronized (instance.plugins) {
            return instance.plugins.getOrDefault(className, null);
        }
    }
}
