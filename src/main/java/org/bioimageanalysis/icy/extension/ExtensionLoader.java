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

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.common.reflect.ClassUtil;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.abstract_.Plugin;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginDaemon;
import org.bioimageanalysis.icy.io.Loader;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.UserUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.preferences.PluginPreferences;
import org.bioimageanalysis.icy.system.thread.SingleProcessor;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.graph.selector.ExclusionDependencySelector;
import org.eclipse.aether.util.graph.selector.OptionalDependencySelector;
import org.eclipse.aether.util.graph.selector.ScopeDependencySelector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.yaml.snakeyaml.Yaml;

import javax.swing.event.EventListenerList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

/**
 * @author Thomas Musset
 * @since 3.0.0
 */
public final class ExtensionLoader {
    public final static String PLUGINS_PACKAGE = "icy.plugins";
    public final static String OLD_PLUGINS_PACKAGE = "plugins"; // For legacy compatibility
    public final static String EXTENSIONS_PATH = UserUtil.getIcyExtensionsDirectory().getAbsolutePath();

    @Deprecated
    public final static String KERNEL_PLUGINS_PACKAGE = PLUGINS_PACKAGE + ".kernel.";
    @Deprecated
    public final static String OLD_KERNEL_PLUGINS_PACKAGE = OLD_PLUGINS_PACKAGE + ".kernel.";

    public final static String KERNEL_GROUP_ID = "org.bioimageanalysis.icy";
    @Unmodifiable
    public static final List<String> KERNEL_ARTIFACT_IDS = List.of(
            "kernel-extension",
            "icy2-api"
    );

    /**
     * static class
     */
    private static final ExtensionLoader instance = new ExtensionLoader();

    private final RepositorySystem repositorySystem;
    private final DefaultRepositorySystemSession session;

    private final List<RemoteRepository> repositories;
    //private final RemoteRepository central;
    //private final RemoteRepository scijava;
    //private final RemoteRepository ome;
    //private final RemoteRepository jogamp;
    //private final RemoteRepository netbeans;

    /**
     * class loader
     */
    private URLClassLoader ucl;

    private final Set<String> jarSet;

    private final Map<String, Set<String>> extensionsArtifacts;
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

        /*@SuppressWarnings("deprecation")
        final DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(ModelProcessor.class, DefaultModelProcessor.class);
        locator.addService(ModelBuilder.class, DefaultModelBuilder.class);
        locator.addService(DefaultModelBuilderFactory.class, DefaultModelBuilderFactory.class);
        locator.addService(ArtifactDescriptorReader.class, DefaultArtifactDescriptorReader.class);
        locator.addService(VersionResolver.class, DefaultVersionResolver.class);
        locator.addService(VersionRangeResolver.class, DefaultVersionRangeResolver.class);
        locator.addService(MetadataResolver.class, DefaultMetadataResolver.class);
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        //locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(RepositorySystem.class, DefaultRepositorySystem.class);

        @SuppressWarnings("deprecation")
        final DefaultServiceLocator.ErrorHandler errorHandler = new DefaultServiceLocator.ErrorHandler() {
            @Override
            public void serviceCreationFailed(final Class<?> type, final Class<?> impl, final Throwable exception) {
                IcyLogger.error(this.getClass(), exception, "Service creation failed for " + type + " with impl " + impl);
            }
        };
        locator.setErrorHandler(errorHandler);*/

        //repositorySystem = locator.getService(RepositorySystem.class);
        repositorySystem = new RepositorySystemSupplier().get();

        session = MavenRepositorySystemUtils.newSession();
        session.setOffline(Icy.isNetworkDisabled());
        final LocalRepository localRepo = new LocalRepository(System.getProperty("user.home") + "/.m2/repository");
        session.setLocalRepositoryManager(repositorySystem.newLocalRepositoryManager(session, localRepo));
        session.setSystemProperties(System.getProperties());
        session.setDependencySelector(
                new AndDependencySelector(
                        new ExclusionDependencySelector(List.of(new Exclusion("org.bioimageanalysis", null, null, null))),
                        new OptionalDependencySelector(),
                        new ScopeDependencySelector("provided", "test")
                )
        );

        final RemoteRepository central = new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build();
        final RemoteRepository scijava = new RemoteRepository.Builder("scijava", "default", "https://maven.scijava.org/content/groups/public/").build();
        final RemoteRepository ome = new RemoteRepository.Builder("ome", "default", "https://artifacts.openmicroscopy.org/artifactory/ome.releases/").build();
        final RemoteRepository jogamp = new RemoteRepository.Builder("jogamp", "default", "https://jogamp.org/deployment/maven/").build();
        final RemoteRepository netbeans = new RemoteRepository.Builder("netbeans", "default", "https://netbeans.apidesign.org/maven2/").build();
        final RemoteRepository jboss = new RemoteRepository.Builder("jboss", "default", "https://repository.jboss.org/nexus/content/repositories/public/").build();

        repositories = new ArrayList<>();
        repositories.add(central);
        repositories.add(scijava);
        repositories.add(ome);
        repositories.add(jogamp);
        repositories.add(netbeans);
        repositories.add(jboss);

        ucl = new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());

        jarSet = new HashSet<>();
        extensionsArtifacts = new HashMap<>();
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

        jarSet.clear();
        extensionsArtifacts.clear();
        extensions.clear();
        plugins.clear();
        actionablePlugins.clear();
        daemonPlugins.clear();

        final Map<String, Artifact> artifacts = getExtensionsArtifacts();
        IcyLogger.debug(this.getClass(), "Loading " + artifacts.size() + " artifacts from local repository");
        for (final Artifact artifact : artifacts.values()) {
            IcyLogger.debug(this.getClass(), "Found artifact " + artifact);
            loadArtifact(artifact);
        }

        /*final Set<File> jars = getJarList();
        for (final File jar : jars)
            loadJar(jar, false);*/

        loading = false;

        // notify change
        changed();
    }

    private boolean resolveArtifact(final @NotNull String groupId, final @NotNull String artifactId, final @NotNull String version, final @NotNull LinkedHashMap<String, Artifact> artifacts, final @NotNull List<RemoteRepository> repositories) {
        try {
            if (artifacts.containsKey(groupId + ":" + artifactId + ":jar:" + version)) {
                IcyLogger.debug(this.getClass(), "duplicate artifact: " + groupId + ":" + artifactId + ":jar:" + version);
                return true;
            }

            // Resolve artifact
            final Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":jar:" + version);
            final ArtifactRequest artifactRequest = new ArtifactRequest()
                    .setArtifact(artifact)
                    .setRepositories(this.repositories);

            if (!repositories.isEmpty()) {
                for (final RemoteRepository repository : repositories) {
                    artifactRequest.addRepository(repository);
                }
            }

            final ArtifactResult artifactResult = repositorySystem.resolveArtifact(session, artifactRequest);

            if (!artifactResult.isResolved() || artifactResult.getArtifact() == null)
                return false;
            if (artifacts.containsKey(artifactResult.getArtifact().toString()))
                return true;

            // Collect dependencies
            final ArtifactDescriptorRequest artifactDescriptorRequest = new ArtifactDescriptorRequest()
                    .setArtifact(artifactResult.getArtifact())
                    .setRepositories(this.repositories);

            if (!repositories.isEmpty()) {
                for (final RemoteRepository repository : repositories) {
                    artifactDescriptorRequest.addRepository(repository);
                }
            }

            final ArtifactDescriptorResult artifactDescriptorResult = repositorySystem.readArtifactDescriptor(session, artifactDescriptorRequest);
            for (final Dependency dependency : artifactDescriptorResult.getDependencies()) {
                //IcyLogger.debug(this.getClass(), "Resolving dependency: " + dependency.getArtifact().getGroupId() + ":" + dependency.getArtifact().getArtifactId() + ":" + dependency.getScope());
                if (!dependency.getScope().equalsIgnoreCase(JavaScopes.COMPILE) && !dependency.getScope().equalsIgnoreCase(JavaScopes.RUNTIME))
                    continue;
                if (dependency.isOptional())
                    continue;
                final Artifact dependencyArtifact = dependency.getArtifact();
                if (!dependencyArtifact.getExtension().endsWith("jar"))
                    continue;
                //IcyLogger.debug(this.getClass(), "Dependency present: " + artifacts.contains(dependencyArtifact.getGroupId() + ":" + dependencyArtifact.getArtifactId() + ":" + dependencyArtifact.getVersion()));
                if (artifacts.containsKey(dependencyArtifact.toString()))
                    continue;

                final boolean dependencyResolved = resolveArtifact(dependencyArtifact.getGroupId(), dependencyArtifact.getArtifactId(), dependencyArtifact.getVersion(), artifacts, artifactDescriptorResult.getRepositories());
                if (!dependencyResolved)
                    return false;
            }

            artifacts.put(groupId + ":" + artifactId + ":jar:" + version, artifactResult.getArtifact());
            return true;
        }
        catch (final Error | Exception e) {
            IcyLogger.error(this.getClass(), e, "Failed to resolve artifact: " + groupId + ":" + artifactId + ":" + version);
            return false;
        }
    }

    private boolean resolveArtifact(final @NotNull String groupId, final @NotNull String artifactId, final @NotNull String version, final @NotNull LinkedHashMap<String, Artifact> artifacts) {
        return resolveArtifact(groupId, artifactId, version, artifacts, Collections.emptyList());
    }

    private boolean loadArtifact(final @NotNull Artifact artifact) {
        final File jar = artifact.getFile();
        if (jar == null || !jar.exists() || !jar.isFile()) {
            IcyLogger.error(Icy.class, "Jar not found for artifact: " + artifact);
            return false;
        }

        try {
            ucl = new URLClassLoader(
                    Stream.concat(
                            Arrays.stream(ucl.getURLs()),
                            Stream.of(jar.toURI().toURL())
                    ).toArray(URL[]::new),
                    ucl.getParent()
            );

            if (isIcyExtension(artifact)) {
                final Set<ExtensionDescriptor> eds = new HashSet<>();
                final Map<String, PluginDescriptor> pds = new HashMap<>();
                final Map<String, PluginDescriptor> dpds = new HashMap<>();
                final Map<String, PluginDescriptor> apds = new HashMap<>();

                final ExtensionDescriptor ed = new ExtensionDescriptor(artifact);
                if (extensionsArtifacts.containsKey(ed.getGroupId())) {
                    final Set<String> artifactIds = extensionsArtifacts.get(ed.getGroupId());
                    if (artifactIds.contains(ed.getArtifactId())) {
                        IcyLogger.debug(this.getClass(), "Extension " + ed.getName() + " already loaded, skipping it");
                        return true;
                    }
                }

                final Set<String> classes = Set.copyOf(ClassUtil.findClassNamesInJAR(jar.getAbsolutePath()));
                for (final String className : classes) {
                    if (className.startsWith(PLUGINS_PACKAGE) || className.startsWith(OLD_PLUGINS_PACKAGE)) {
                        try {
                            final Class<? extends Plugin> pluginClass = ucl.loadClass(className).asSubclass(Plugin.class);

                            final PluginDescriptor pd = new PluginDescriptor(pluginClass, ed);
                            if (!pd.isAbstract() && !pd.isInterface()) {
                                ed.addPlugin(pd);
                                pds.put(className, pd);
                                if (pd.isDaemon())
                                    dpds.put(className, pd);
                                if (pd.isActionable())
                                    apds.put(className, pd);

                            /*if (className.startsWith(OLD_PLUGINS_PACKAGE) && !ed.isKernel())
                                IcyLogger.warn(this.getClass(), "Old plugins package detected for " + ClassUtil.getSimpleClassName(className));*/
                            }
                        }
                        catch (final Throwable t) {
                            // ignored
                        }
                    }
                }

                extensions.addAll(eds);
                plugins.putAll(pds);
                actionablePlugins.putAll(apds);
                daemonPlugins.putAll(dpds);
            }
        }
        catch (final Throwable t) {
            IcyLogger.error(this.getClass(), t, "Failed to load artifact: " + artifact);
            return false;
        }

        return true;
    }

    private boolean isIcyExtension(final @NotNull Artifact artifact) {
        final File jar = artifact.getFile();
        if (jar == null || !jar.exists() || !jar.isFile()) {
            // Shouldn't happening, but in case of...
            return false;
        }

        final boolean result;
        try (final JarFile jarFile = new JarFile(jar)) {
            result = jarFile.getEntry("META-INF/extension.yaml") != null;
        }
        catch (final IOException e) {
            return false;
        }

        return result;
    }

    /*@Deprecated(forRemoval = true)
    private boolean loadJar(final @NotNull File jar, final boolean external) {
        final String path = jar.getAbsolutePath().toLowerCase(Locale.getDefault());
        // If already loaded, skip it
        if (jarSet.contains(path))
            return true;

        jarSet.add(path);

        if (!jar.exists() || !jar.isFile()) {
            IcyLogger.error(Icy.class, "Jar not found: " + jar.getAbsolutePath());
            return false;
        }

        if (external) {
            try {
                ucl = new URLClassLoader(
                        Stream.concat(
                                Arrays.stream(ucl.getURLs()),
                                Stream.of(jar.toURI().toURL())
                        ).toArray(URL[]::new),
                        ucl.getParent()
                );
            }
            catch (final Throwable t) {
                IcyLogger.error(this.getClass(), t, "Failed to load dependency: " + jar.getAbsolutePath());
                return false;
            }
        }
        else {
            final Set<ExtensionDescriptor> eds = new HashSet<>();
            final Map<String, PluginDescriptor> pds = new HashMap<>();
            final Map<String, PluginDescriptor> dpds = new HashMap<>();
            final Map<String, PluginDescriptor> apds = new HashMap<>();

            try {
                ucl = new URLClassLoader(
                        Stream.concat(
                                Arrays.stream(ucl.getURLs()),
                                Stream.of(jar.toURI().toURL())
                        ).toArray(URL[]::new),
                        ucl.getParent()
                );

                final ExtensionDescriptor ed = new ExtensionDescriptor(jar);
                if (extensionsArtifacts.containsKey(ed.getGroupId())) {
                    final Set<String> artifactIds = extensionsArtifacts.get(ed.getGroupId());
                    if (artifactIds.contains(ed.getArtifactId())) {
                        IcyLogger.debug(this.getClass(), "Extension " + ed.getName() + " already loaded, skipping it");
                        return true;
                    }
                }

                if (!ed.getDependencies().isEmpty()) {
                    if (!loadDependencies(ed)) {
                        IcyLogger.warn(this.getClass(), "Extension " + ed.getName() + " dependencies were not correctly loaded, skipping it");
                        return false;
                    }
                }

                final Set<String> classes = Set.copyOf(ClassUtil.findClassNamesInJAR(jar.getAbsolutePath()));
                for (final String className : classes) {
                    if (className.startsWith(PLUGINS_PACKAGE) || className.startsWith(OLD_PLUGINS_PACKAGE)) {
                        try {
                            final Class<? extends Plugin> pluginClass = ucl.loadClass(className).asSubclass(Plugin.class);

                            final PluginDescriptor pd = new PluginDescriptor(pluginClass, ed);
                            if (!pd.isAbstract() && !pd.isInterface()) {
                                ed.addPlugin(pd);
                                pds.put(className, pd);
                                if (pd.isDaemon())
                                    dpds.put(className, pd);
                                if (pd.isActionable())
                                    apds.put(className, pd);

                            if (className.startsWith(OLD_PLUGINS_PACKAGE) && !ed.isKernel())
                                IcyLogger.warn(this.getClass(), "Old plugins package detected for " + ClassUtil.getSimpleClassName(className));
                            }
                        }
                        catch (final Throwable t) {
                            // ignored
                        }
                    }
                }

                eds.add(ed);
            }
            catch (final Throwable t) {
                IcyLogger.error(this.getClass(), t, "Failed to load extension: " + jar.getAbsolutePath());
                return false;
            }

            extensions.addAll(eds);
            plugins.putAll(pds);
            actionablePlugins.putAll(apds);
            daemonPlugins.putAll(dpds);
        }

        return true;
    }*/

    /*@Deprecated(forRemoval = true)
    private boolean loadDependencies(final @NotNull ExtensionDescriptor descriptor) {
        final Map<String, List<Map<String, Object>>> dependencies = descriptor.getDependencies();
        if (dependencies.isEmpty())
            return true;

        boolean loaded = true;
        if (!dependencies.getOrDefault("external", Collections.emptyList()).isEmpty()) {
            for (final Map<String, Object> ed : dependencies.get("external")) {
                loaded = loadDependency(ed, true);
                if (!loaded)
                    break;
            }
        }

        if (!loaded)
            return false;

        if (!dependencies.getOrDefault("icy", Collections.emptyList()).isEmpty()) {
            for (final Map<String, Object> dependency : dependencies.get("icy")) {
                loaded = loadDependency(dependency, false);
                if (!loaded)
                    break;
            }
        }

        return loaded;
    }*/

    /*@Deprecated(forRemoval = true)
    private boolean loadDependency(final @NotNull Map<String, Object> dependency, final boolean external) {
        final String groupId = (String) dependency.get("groupId");
        final String artifactId = (String) dependency.get("artifactId");
        final String version = (String) dependency.get("version");

        final String m2Path = ".m2" + File.separator + "repository" + File.separator;
        final String jarPath = groupId.replaceAll("\\.", File.separator)
                + File.separator
                + artifactId
                + File.separator
                + version
                + File.separator
                + artifactId + "-" + version + ".jar";
        final File dependencyFile = new File(UserUtil.getUserHome(), m2Path + jarPath);
        //final File dependencyFile = new File(EXTENSIONS_PATH + File.separator + groupId.replaceAll("\\.", File.separator) + File.separator + artifactId, artifactId + ".jar");
        if (!dependencyFile.exists() || !dependencyFile.isFile()) {
            // TODO check version & request a download if missing a dependency
            IcyLogger.warn(this.getClass(), "Dependency not found: " + dependencyFile.getAbsolutePath());
            return false;
        }
        else
            return loadJar(dependencyFile, external);
    }*/

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
    public static ClassLoader getLoader() {
        synchronized (instance.ucl) {
            return instance.ucl;
        }
    }

    public static @NotNull Class<?> loadClass(final String className) throws ClassNotFoundException {
        prepare();

        synchronized (instance.ucl) {
            final Class<?> result = instance.ucl.loadClass(className);

            if (result == null)
                throw new ClassNotFoundException(className);

            // try to load class
            return result;
        }
    }

    @Deprecated(forRemoval = true)
    private @NotNull @Unmodifiable Set<File> getJarList() {
        final File indexFile = new File(UserUtil.getIcyExtensionsDirectory(), "ext.bin");
        if (!indexFile.exists() || !indexFile.isFile() || !indexFile.canRead()) {
            IcyLogger.error(Icy.class, "Cannot find or read extensions config file: " + indexFile.getAbsolutePath());
            return Collections.emptySet();
        }

        try (final InputStream is = new FileInputStream(indexFile)) {
            final byte[] readRawData = is.readAllBytes();
            final byte[] readData = Base64.getDecoder().decode(readRawData);
            final StringBuilder sb = new StringBuilder();
            for (final byte readByte : readData)
                sb.append((char) readByte);

            final Yaml yaml = new Yaml();
            final List<Map<String, Object>> extensionIndex = yaml.loadAs(sb.toString(), List.class);
            if (extensionIndex.isEmpty()) {
                IcyLogger.error(Icy.class, "Extensions config file has no extensions registered");
                return Collections.emptySet();
            }

            final Set<File> files = new HashSet<>(extensionIndex.size());
            for (final Map<String, Object> item : extensionIndex) {
                final String groupId = item.get("groupId").toString();
                final String artifactId = item.get("artifactId").toString();
                final String version = item.get("version").toString();

                try {
                    final Artifact artifact = new DefaultArtifact(groupId + ":" + artifactId + ":jar:" + version);
                    final ArtifactRequest artReq = new ArtifactRequest()
                            .setArtifact(artifact)
                            .setRepositories(repositories);

                    final ArtifactResult artRes = repositorySystem.resolveArtifact(session, artReq);
                    files.add(artRes.getArtifact().getFile());
                }
                catch (final ArtifactResolutionException e) {
                    IcyLogger.error(this.getClass(), e, "Failed to resolve artifact: " + groupId + ":" + artifactId + ":" + version);
                }
            }

            return Collections.unmodifiableSet(files);
        }
        catch (final Throwable t) {
            IcyLogger.fatal(Icy.class, t, "Unbale to read extensions config file");
            return Collections.emptySet();
        }
    }

    private @NotNull @Unmodifiable Map<String, Artifact> getExtensionsArtifacts() {
        final File indexFile = new File(UserUtil.getIcyExtensionsDirectory(), "ext.bin");
        if (!indexFile.exists() || !indexFile.isFile() || !indexFile.canRead()) {
            IcyLogger.error(Icy.class, "Cannot find or read extensions config file: " + indexFile.getAbsolutePath());
            return Collections.emptyMap();
        }

        try (final InputStream is = new FileInputStream(indexFile)) {
            final byte[] readRawData = is.readAllBytes();
            final byte[] readData = Base64.getDecoder().decode(readRawData);
            final StringBuilder sb = new StringBuilder();
            for (final byte readByte : readData)
                sb.append((char) readByte);

            final Yaml yaml = new Yaml();
            final List<Map<String, Object>> extensionIndex = yaml.loadAs(sb.toString(), List.class);
            if (extensionIndex.isEmpty()) {
                IcyLogger.error(Icy.class, "Extensions config file has no extensions registered");
                return Collections.emptyMap();
            }

            final LinkedHashMap<String, Artifact> artifacts = new LinkedHashMap<>();
            for (final Map<String, Object> item : extensionIndex) {
                final String groupId = item.get("groupId").toString();
                final String artifactId = item.get("artifactId").toString();
                final String version = item.get("version").toString();

                final boolean resolved = resolveArtifact(groupId, artifactId, version, artifacts);
                if (!resolved)
                    IcyLogger.error(this.getClass(), "Unable to resolve artifact " + groupId + ":" + artifactId + ":" + version);
            }

            return Collections.unmodifiableMap(artifacts);
        }
        catch (final Throwable t) {
            IcyLogger.fatal(this.getClass(), t, "Unable to read extensions config file");
            return Collections.emptyMap();
        }
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
