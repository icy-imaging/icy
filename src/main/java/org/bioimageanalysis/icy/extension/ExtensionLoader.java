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

package org.bioimageanalysis.icy.extension;

import org.bioimageanalysis.icy.common.reflect.ClassUtil;
import org.bioimageanalysis.icy.extension.abstract_.Extension;
import org.bioimageanalysis.icy.extension.plugin.PluginInstaller;
import org.bioimageanalysis.icy.extension.plugin.PluginLoader;
import org.bioimageanalysis.icy.extension.plugin.classloader.JarClassLoader;
import org.bioimageanalysis.icy.io.Loader;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.thread.SingleProcessor;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.EventListenerList;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class ExtensionLoader {
    public final static String PLUGIN_PACKAGE = "plugins";
    //public final static String PLUGIN_KERNEL_PACKAGE = "plugins.kernel";
    public final static String PLUGIN_KERNEL_PACKAGE = "org.bioimageanalysis.extension.kernel";
    public final static String PLUGIN_PATH = "plugins";

    // used to identify java version problem
    public final static String NEWER_JAVA_REQUIRED = "Newer java version required";

    /**
     * static class
     */
    private static final ExtensionLoader instance = new ExtensionLoader();

    /**
     * class loader
     */
    private ClassLoader loader;
    /**
     * Loaded extensions list
     */
    private final List<ExtensionDescriptor> extensions;

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
    private ExtensionLoader() {
        super();

        // default class loader
        loader = new PluginLoader.PluginClassLoader();

        JCLDisabled = false;
        initialized = false;
        loading = false;
        // needReload = false;
        // logError = true;

        extensions = new ArrayList<>();
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

    /**
     * Reload the list of installed plugins (in "plugins" directory)
     */
    void reloadInternal() {
        // needReload = false;
        loading = true;

        // reset plugins and loader
        final List<ExtensionDescriptor> newExtensions = new ArrayList<>();
        final ClassLoader newLoader;

        // special case where JCL is disabled
        if (JCLDisabled)
            newLoader = ExtensionLoader.class.getClassLoader();
        else {
            newLoader = new ExtensionLoader.ExtensionClassLoader();

            // reload plugins directory to search path
            ((ExtensionLoader.ExtensionClassLoader) newLoader).add(PLUGIN_PATH);
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
            IcyLogger.error(this.getClass(), e, "Error loading extensions.");
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
                final Class<? extends Extension> extensionClass = newLoader.loadClass(className).asSubclass(Extension.class);
                // add to list
                newExtensions.add(new ExtensionDescriptor(extensionClass));
            }
            catch (final NoClassDefFoundError e) {
                // fatal error
                final String[] messages = new String[]{
                        "Class '" + className + "' cannot be loaded :",
                        "Required class '" + ClassUtil.getQualifiedNameFromPath(e.getLocalizedMessage()) + "' not found."
                };
                IcyLogger.fatal(this.getClass(), e, messages);
            }
            catch (final UnsupportedClassVersionError e) {
                // java version error (here we just notify in the console)
                IcyLogger.warn(this.getClass(), e, NEWER_JAVA_REQUIRED + " for class '" + className + "' (discarded).");
            }
            catch (final ClassCastException e) {
                // ignore ClassCastException (for classes which doesn't extend Plugin)
            }
            catch (final ClassNotFoundException e) {
                // ignore ClassNotFoundException (for no public classes)
            }
            catch (final Error | Exception e) {
                // fatal error
                IcyLogger.fatal(this.getClass(), e,"Class '" + className + "' is discarded.");
            }
        }

        // sort list
        //newExtensions.sort(ExtensionDescriptor.PluginKernelNameSorter.instance);

        // release loaded resources
        if (loader instanceof JarClassLoader)
            ((JarClassLoader) loader).unloadAll();

        loader = newLoader;
        extensions.clear();
        extensions.addAll(newExtensions);
        //plugins = newPlugins;

        loading = false;

        // notify change
        changed();
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
    public static ArrayList<ExtensionDescriptor> getExtensions() {
        prepare();

        final ArrayList<ExtensionDescriptor> result;

        // better to return a copy as we have async list loading
        synchronized (instance.extensions) {
            result = new ArrayList<>(instance.extensions);
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
     * Called when class loader changed
     */
    protected void changed() {
        // check for missing or mis-installed plugins on first start
        if (!initialized) {
            initialized = true;
            checkExtensions(false);
        }

        // notify listener we have changed
        fireEvent(new ExtensionLoader.ExtensionLoaderEvent());

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
    public static void checkExtensions(final boolean showProgress) {
        final List<ExtensionDescriptor> extensions = getExtensions();
        final List<ExtensionDescriptor> required = new ArrayList<>();
        final List<ExtensionDescriptor> missings = new ArrayList<>();
        final List<ExtensionDescriptor> faulties = new ArrayList<>();

        /*if (NetworkUtil.hasInternetAccess()) {
            final ProgressFrame pf;

            if (showProgress) {
                pf = new ProgressFrame("Checking extensions...");
                pf.setLength(extensions.size());
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
        }*/
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

    public static class ExtensionClassLoader extends JarClassLoader {
        public ExtensionClassLoader() {
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
}
