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
import org.bioimageanalysis.icy.common.reflect.ClassUtil;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor.PluginKernelNameSorter;
import org.bioimageanalysis.icy.extension.plugin.abstract_.Plugin;
import org.bioimageanalysis.icy.extension.plugin.classloader.JarClassLoader;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginDaemon;
import org.bioimageanalysis.icy.gui.frame.progress.ProgressFrame;
import org.bioimageanalysis.icy.io.Loader;
import org.bioimageanalysis.icy.network.NetworkUtil;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.preferences.PluginPreferences;
import org.bioimageanalysis.icy.system.thread.SingleProcessor;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;

/**
 * Plugin Loader class.<br>
 * This class is used to load plugins from "plugins" package and "plugins" directory
 *
 * @author Stephane Dallongeville
 * @author Thommas Musset
 * @deprecated Use {@link org.bioimageanalysis.icy.extension.ExtensionLoader} instead.
 */
@Deprecated(since = "3.0.0-a.5", forRemoval = true)
public class PluginLoader {
    public final static String PLUGIN_PACKAGE = "plugins";
    //public final static String PLUGIN_KERNEL_PACKAGE = "plugins.kernel";
    public final static String PLUGIN_KERNEL_PACKAGE = "org.bioimageanalysis.extension.kernel";
    public final static String PLUGIN_PATH = "plugins";

    // used to identify java version problem
    public final static String NEWER_JAVA_REQUIRED = "Newer java version required";

    /**
     * static class
     */
    private static final PluginLoader instance = new PluginLoader();

    /**
     * class loader
     */
    private ClassLoader loader;
    /**
     * active daemons plugins
     */
    private final List<PluginDaemon> activeDaemons;
    /**
     * Loaded plugin list
     */
    private final List<PluginDescriptor> plugins;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /**
     * JAR Class Loader disabled flag
     */
    protected boolean JCLDisabled;

    /*
     * internals
     */
    private final Runnable reloader;
    final SingleProcessor processor;

    private boolean initialized;
    private boolean loading;

    // private boolean logError;

    /**
     * static class
     */
    private PluginLoader() {
        super();

        // default class loader
        loader = new PluginClassLoader();
        // active daemons
        activeDaemons = new ArrayList<>();

        JCLDisabled = false;
        initialized = false;
        loading = false;
        // needReload = false;
        // logError = true;

        plugins = new ArrayList<>();
        listeners = new EventListenerList();

        // reloader
        reloader = this::reloadInternal;

        processor = new SingleProcessor(true, "Local Plugin Loader");

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
     * Reload the list of installed plugins (in "plugins" directory)
     */
    void reloadInternal() {
        // needReload = false;
        loading = true;

        // stop daemon plugins
        stopDaemons();

        // reset plugins and loader
        final List<PluginDescriptor> newPlugins = new ArrayList<>();
        final ClassLoader newLoader;

        // special case where JCL is disabled
        if (JCLDisabled)
            newLoader = PluginLoader.class.getClassLoader();
        else {
            newLoader = new PluginClassLoader();

            // reload plugins directory to search path
            ((PluginClassLoader) newLoader).add(PLUGIN_PATH);
        }

        // no need to complete loading...
        if (processor.hasWaitingTasks())
            return;

        final Set<String> classes = new HashSet<>();

        try {
            // search all classes in "Plugins" package (needed when working from JAR archive)
            ClassUtil.findClassNamesInPackage(PLUGIN_PACKAGE, true, classes);
            // search all classes in "Plugins" directory with default plugin package name
            ClassUtil.findClassNamesInPath(PLUGIN_PATH, PLUGIN_PACKAGE, true, classes);
        }
        catch (final IOException e) {
            IcyLogger.error(PluginLoader.class, e, "Error loading plugins.");
        }

        for (final String className : classes) {
            // we only want to load classes from 'plugins' package
            if (!className.startsWith(PLUGIN_PACKAGE))
                continue;
            // by-pass jython wrapper classes and some specific MM classes that can't be loaded at this point
            if (className.endsWith("$py") || className.endsWith("$ImageAnalyser") || className.endsWith("$CustomEventCallback"))
                continue;

            // no need to complete loading...
            if (processor.hasWaitingTasks())
                return;

            try {
                // try to load class and check we have a Plugin class at same time
                final Class<? extends Plugin> pluginClass = newLoader.loadClass(className).asSubclass(Plugin.class);
                // add to list
                newPlugins.add(new PluginDescriptor(pluginClass));
            }
            catch (final NoClassDefFoundError e) {
                // fatal error
                final String[] messages = new String[]{
                        "Class '" + className + "' cannot be loaded :",
                        "Required class '" + ClassUtil.getQualifiedNameFromPath(e.getLocalizedMessage()) + "' not found."
                };
                IcyLogger.fatal(PluginLoader.class, e, messages);
            }
            catch (final UnsupportedClassVersionError e) {
                // java version error (here we just notify in the console)
                IcyLogger.warn(PluginLoader.class, e, NEWER_JAVA_REQUIRED + " for class '" + className + "' (discarded).");
            }
            catch (final ClassCastException e) {
                // ignore ClassCastException (for classes which doesn't extend Plugin)
            }
            catch (final ClassNotFoundException e) {
                // ignore ClassNotFoundException (for no public classes)
            }
            catch (final Error | Exception e) {
                // fatal error
                IcyLogger.fatal(PluginLoader.class, "Class '" + className + "' is discarded.");
            }
        }

        // sort list
        newPlugins.sort(PluginKernelNameSorter.instance);

        // release loaded resources
        if (loader instanceof JarClassLoader)
            ((JarClassLoader) loader).unloadAll();

        loader = newLoader;
        plugins.clear();
        plugins.addAll(newPlugins);
        //plugins = newPlugins;

        loading = false;

        // notify change
        changed();
    }

    /**
     * Returns the list of daemon type plugins.
     */
    public static @NotNull ArrayList<PluginDescriptor> getDaemonPlugins() {
        final ArrayList<PluginDescriptor> result = new ArrayList<>();

        synchronized (instance.plugins) {
            for (final PluginDescriptor pluginDescriptor : instance.plugins) {
                if (pluginDescriptor.isInstanceOf(PluginDaemon.class)) {
                    // accept class ?
                    if (!pluginDescriptor.isAbstract() && !pluginDescriptor.isInterface())
                        result.add(pluginDescriptor);
                }
            }
        }

        return result;
    }

    /**
     * Returns the list of active daemon plugins.
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull ArrayList<PluginDaemon> getActiveDaemons() {
        synchronized (instance.activeDaemons) {
            return new ArrayList<>(instance.activeDaemons);
        }
    }

    /**
     * Start daemons plugins.
     */
    static synchronized void startDaemons() {
        // at this point active daemons should be empty !
        if (!instance.activeDaemons.isEmpty())
            stopDaemons();

        final List<String> inactives = PluginPreferences.getInactiveDaemons();
        final List<PluginDaemon> newDaemons = new ArrayList<>();

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
        //instance.activeDaemons = newDaemons;
    }

    /**
     * Stop daemons plugins.
     */
    public synchronized static void stopDaemons() {
        for (final PluginDaemon daemonPlug : getActiveDaemons()) {
            try {
                // stop the daemon
                daemonPlug.stop();
            }
            catch (final Throwable t) {
                IcyExceptionHandler.handleException(((Plugin) daemonPlug).getDescriptor(), t, true);
            }
        }

        // no more active daemons
        instance.activeDaemons.clear();
        //instance.activeDaemons = new ArrayList<>();
    }

    /**
     * Return the loader
     */
    @Contract(pure = true)
    public static ClassLoader getLoader() {
        return instance.loader;
    }

    /**
     * Return all resources present in the Plugin class loader.
     */
    public static Map<String, URL> getAllResources() {
        prepare();

        //synchronized (instance.loader) {
        if (instance.loader instanceof JarClassLoader)
            return ((JarClassLoader) instance.loader).getResources();
        //}

        return new HashMap<>();
    }

    /**
     * Return content of all loaded resources.
     */
    public static Map<String, byte[]> getLoadedResources() {
        prepare();

        //synchronized (instance.loader) {
        if (instance.loader instanceof JarClassLoader)
            return ((JarClassLoader) instance.loader).getLoadedResources();
        //}

        return new HashMap<>();
    }

    /**
     * Return all loaded classes.
     */
    @Contract(" -> new")
    public static @NotNull Map<String, Class<?>> getLoadedClasses() {
        prepare();

        //synchronized (instance.loader) {
        if (instance.loader instanceof JarClassLoader) {
            final Map<String, Class<?>> classes = ((JarClassLoader) instance.loader).getLoadedClasses();

            return new HashMap<>(classes);
        }
        //}

        return new HashMap<>();
    }

    /**
     * Return a resource as data stream from given resource name
     *
     * @param name resource name
     */
    public static InputStream getResourceAsStream(final String name) {
        prepare();

        //synchronized (instance.loader) {
        return instance.loader.getResourceAsStream(name);
        //}
    }

    /**
     * Return the list of loaded plugins.
     */
    public static ArrayList<PluginDescriptor> getPlugins() {
        prepare();

        final ArrayList<PluginDescriptor> result;

        // better to return a copy as we have async list loading
        synchronized (instance.plugins) {
            result = new ArrayList<>(instance.plugins);
        }

        return result;
    }

    /**
     * Return the list of loaded plugins which derive from the specified class.
     *
     * @param clazz The class object defining the class we want plugin derive from.
     */
    public static @NotNull ArrayList<PluginDescriptor> getPlugins(final Class<?> clazz) {
        return getPlugins(clazz, false, false);
    }

    /**
     * Return the list of loaded plugins which derive from the specified class.
     *
     * @param clazz         The class object defining the class we want plugin derive from.
     * @param wantAbstract  specify if we also want abstract classes
     * @param wantInterface specify if we also want interfaces
     */
    public static @NotNull ArrayList<PluginDescriptor> getPlugins(final Class<?> clazz, final boolean wantAbstract, final boolean wantInterface) {
        prepare();

        final ArrayList<PluginDescriptor> result = new ArrayList<>();

        if (clazz != null) {
            synchronized (instance.plugins) {
                for (final PluginDescriptor pluginDescriptor : instance.plugins) {
                    if (pluginDescriptor.isInstanceOf(clazz)) {
                        // accept class ?
                        if ((wantAbstract || !pluginDescriptor.isAbstract()) && (wantInterface || !pluginDescriptor.isInterface()))
                            result.add(pluginDescriptor);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Return the list of loaded plugins which annotated with the specified annotation.
     *
     * @param annotation The class object defining the annotation we want.
     */
    public static @NotNull ArrayList<PluginDescriptor> getAnnotatedPlugins(final Class<? extends Annotation> annotation) {
        return getAnnotatedPlugins(annotation, false, false);
    }

    /**
     * Return the list of loaded plugins which annotated with the specified annotation.
     *
     * @param annotation    The class object defining the annotation we want.
     * @param wantAbstract  specify if we also want abstract classes
     * @param wantInterface specify if we also want interfaces
     */
    public static @NotNull ArrayList<PluginDescriptor> getAnnotatedPlugins(final @NotNull Class<? extends Annotation> annotation, final boolean wantAbstract, final boolean wantInterface) {
        prepare();

        final ArrayList<PluginDescriptor> result = new ArrayList<>();
        synchronized (instance.plugins) {
            for (final PluginDescriptor pluginDescriptor : instance.plugins) {
                if (pluginDescriptor.isAnnotated(annotation)) {
                    // accept class ?
                    if ((wantAbstract || !pluginDescriptor.isAbstract()) && (wantInterface || !pluginDescriptor.isInterface()))
                        result.add(pluginDescriptor);
                }
            }
        }

        return result;
    }

    /**
     * Return the list of "actionable" plugins (mean we can launch them from GUI).
     */
    public static @NotNull ArrayList<PluginDescriptor> getActionablePlugins() {
        prepare();

        final ArrayList<PluginDescriptor> result = new ArrayList<>();

        synchronized (instance.plugins) {
            for (final PluginDescriptor pluginDescriptor : instance.plugins) {
                if (pluginDescriptor.isActionable())
                    result.add(pluginDescriptor);
            }
        }

        return result;
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
     * Try to load and returns the specified class from the {@link PluginLoader}.<br>
     * This method is equivalent to call {@link #getLoader()} then call
     * <code>loadClass(String)</code> method from it.
     *
     * @param className class name of the class we want to load.
     */
    public static Class<?> loadClass(final String className) throws ClassNotFoundException {
        prepare();

        //synchronized (instance.loader) {
        // try to load class
        return instance.loader.loadClass(className);
        //}
    }

    /**
     * Verify the specified plugin is correctly installed.<br>
     * Returns an empty string if the plugin is valid otherwise it returns the error message.
     */
    public static String verifyPlugin(final PluginDescriptor plugin) {
        //synchronized (instance.loader) {
        try {
            // then try to load the plugin class as Plugin class
            instance.loader.loadClass(plugin.getClassName()).asSubclass(Plugin.class);
        }
        catch (final UnsupportedClassVersionError e) {
            return NEWER_JAVA_REQUIRED + ".";
        }
        catch (final Error e) {
            return e.toString();
        }
        catch (final ClassCastException e) {
            return IcyExceptionHandler.getErrorMessage(e, false)
                    + "Your plugin class should extends 'icy.plugin.abstract_.Plugin' class.";
        }
        catch (final ClassNotFoundException e) {
            return IcyExceptionHandler.getErrorMessage(e, false)
                    + "Verify you correctly set the class name in your plugin description.";
        }
        catch (final Exception e) {
            return IcyExceptionHandler.getErrorMessage(e, false);
        }
        //}

        return "";
    }

    @Contract(pure = true)
    public static boolean isJCLDisabled() {
        return instance.JCLDisabled;
    }

    public static void setJCLDisabled(final boolean value) {
        instance.JCLDisabled = value;
    }

    /**
     * Called when class loader changed
     */
    protected void changed() {
        // check for missing or mis-installed plugins on first start
        if (!initialized) {
            initialized = true;
            checkPlugins(false);
        }

        // start daemon plugins
        startDaemons();
        // notify listener we have changed
        fireEvent(new PluginLoaderEvent());

        ThreadUtil.bgRun(() -> {
            // we are still installing / removing plugins or reloading plugin list --> cancel
            if (!PluginInstaller.getInstallFIFO().isEmpty() || !PluginInstaller.getRemoveFIFO().isEmpty() || isLoading())
                return;

            // pre load the importers classes as they can be heavy
            Loader.getSequenceFileImporters();
            Loader.getFileImporters();
            Loader.getImporters();
        });
    }

    /**
     * Check for missing plugins and install them if needed.
     */
    public static void checkPlugins(final boolean showProgress) {
        final List<PluginDescriptor> plugins = getPlugins();
        final List<PluginDescriptor> required = new ArrayList<>();
        final List<PluginDescriptor> missings = new ArrayList<>();
        final List<PluginDescriptor> faulties = new ArrayList<>();

        if (NetworkUtil.hasInternetAccess()) {
            final ProgressFrame pf;

            if (showProgress) {
                pf = new ProgressFrame("Checking plugins...");
                pf.setLength(plugins.size());
                pf.setPosition(0);
            }
            else
                pf = null;

            PluginRepositoryLoader.waitLoaded();

            // get list of required and faulty plugins
            for (final PluginDescriptor plugin : plugins) {
                // get dependencies
                if (!PluginInstaller.getDependencies(plugin, required, null, false))
                    // error in dependencies --> try to reinstall the plugin
                    faulties.add(PluginRepositoryLoader.getPlugin(plugin.getClassName()));

                if (pf != null)
                    pf.incPosition();
            }

            if (pf != null)
                pf.setLength(required.size());

            // check for missing plugins
            for (final PluginDescriptor plugin : required) {
                // dependency missing ? --> try to reinstall the plugin
                if (!plugin.isInstalled()) {
                    final PluginDescriptor toInstall = PluginRepositoryLoader.getPlugin(plugin.getClassName());
                    if (toInstall != null)
                        missings.add(toInstall);
                }

                if (pf != null)
                    pf.incPosition();
            }

            if ((faulties.size() > 0) || (missings.size() > 0)) {
                if (pf != null) {
                    pf.setMessage("Installing missing plugins...");
                    pf.setPosition(0);
                    pf.setLength(faulties.size() + missings.size());
                }

                // remove faulty plugins
                // for (PluginDescriptor plugin : faulties)
                // PluginInstaller.desinstall(plugin, false, false);
                // PluginInstaller.waitDesinstall();

                // install missing plugins
                for (final PluginDescriptor plugin : missings) {
                    PluginInstaller.install(plugin, true);
                    if (pf != null)
                        pf.incPosition();
                }
                // and reinstall faulty plugins
                for (final PluginDescriptor plugin : faulties) {
                    PluginInstaller.install(plugin, true);
                    if (pf != null)
                        pf.incPosition();
                }
            }
        }
    }

    /**
     * Add a listener
     */
    public static void addListener(final PluginLoaderListener listener) {
        synchronized (instance.listeners) {
            instance.listeners.add(PluginLoaderListener.class, listener);
        }
    }

    /**
     * Remove a listener
     */
    public static void removeListener(final PluginLoaderListener listener) {
        synchronized (instance.listeners) {
            instance.listeners.remove(PluginLoaderListener.class, listener);
        }
    }

    /**
     * fire event
     */
    void fireEvent(final PluginLoaderEvent e) {
        synchronized (instance.listeners) {
            for (final PluginLoaderListener listener : instance.listeners.getListeners(PluginLoaderListener.class)) {
                listener.pluginLoaderChanged(e);
            }
        }
    }

    public static class PluginClassLoader extends JarClassLoader {
        public PluginClassLoader() {
            super();
        }

        /**
         * Give access to this method
         */
        public Class<?> getLoadedClass(final String name) {
            return super.findLoadedClass(name);
        }

        /**
         * Give access to this method
         */
        public boolean isLoadedClass(final String name) {
            return getLoadedClass(name) != null;
        }
    }

    public interface PluginLoaderListener extends EventListener {
        void pluginLoaderChanged(PluginLoaderEvent e);
    }

    public static class PluginLoaderEvent {
        @Contract(pure = true)
        public PluginLoaderEvent() {
            super();
        }

        @Contract(value = "null -> false", pure = true)
        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof PluginLoaderEvent)
                return true;

            return super.equals(obj);
        }
    }
}
