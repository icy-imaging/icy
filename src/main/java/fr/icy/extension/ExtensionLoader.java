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

package fr.icy.extension;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import fr.icy.Icy;
import fr.icy.common.reflect.ClassUtil;
import fr.icy.extension.plugin.PluginDescriptor;
import fr.icy.extension.plugin.abstract_.Plugin;
import fr.icy.extension.plugin.interface_.PluginDaemon;
import fr.icy.io.Loader;
import fr.icy.system.IcyExceptionHandler;
import fr.icy.system.UserUtil;
import fr.icy.system.logging.IcyLogger;
import fr.icy.system.preferences.PluginPreferences;
import fr.icy.system.thread.MultipleProcessor;
import fr.icy.system.thread.Processor;
import fr.icy.system.thread.ThreadUtil;
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

/**
 * Handles the initialization and management of extension loading for the application.
 * <p>
 * The `ExtensionLoader` is responsible for managing the repository system, configuring the Maven
 * repository system session, setting up remote repositories, and loading extensions and plugins.
 * It ensures proper setup of dependencies and creates the necessary infrastructure for loading
 * extensions.
 * <p>
 * This class is implemented as a singleton and should not be instantiated directly.
 * <p>
 * Key Features:
 * - Sets up a Maven-like repository system for resolving dependencies.
 * - Pre-configures several remote repositories for dependency resolution.
 * - Initializes an extension and plugin management system.
 * - Registers specific exclusion rules for dependency selection.
 * - Handles the reloading of extensions and plugins through a reloader mechanism.
 *
 * @author Thomas Musset
 * @version 3.0.0-a.5
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
    final Processor processor;

    private boolean initialized;
    private boolean loading;

    /**
     * Handles the initialization and management of extension loading for the application.
     * The `ExtensionLoader` is responsible for managing the repository system,
     * configuring the Maven repository system session, setting up remote repositories,
     * and loading extensions and plugins. It ensures proper setup of dependencies
     * and creates the necessary infrastructure for loading extensions.
     * <p>
     * This class is implemented as a singleton and should not be instantiated directly.
     * <p>
     * Key Features:
     * - Sets up a Maven-like repository system for resolving dependencies.
     * - Pre-configures several remote repositories for dependency resolution.
     * - Initializes an extension and plugin management system.
     * - Registers specific exclusion rules for dependency selection.
     * - Handles the reloading of extensions and plugins through a reloader mechanism.
     * <p>
     * Note:
     * This constructor performs extensive setup and should only be invoked internally
     * within the class to maintain the singleton pattern.
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

        processor = new MultipleProcessor(4, "Local Extension Loader");
        //processor = new SingleProcessor(true, "Local Extension Loader");

        // don't load by default as we need Preferences to be ready first
    }

    /**
     * Prepares the instance for usage by ensuring it is properly initialized.
     * <p>
     * The method performs the following steps:
     * 1. Checks if the instance is not already initialized.
     * 2. If the system is in a loading state, it waits until the loading is complete.
     * 3. If the system is not in a loading state, it triggers a reload operation.
     * <p>
     * This method is intended to be used to ensure the instance is ready before
     * proceeding with operations that depend on its initialization.
     */
    static void prepare() {
        if (!instance.initialized) {
            if (isLoading())
                waitWhileLoading();
            else
                reload();
        }
    }

    /**
     * Submits the reloader task to the processing thread asynchronously.
     * <p>
     * This method is used to initiate the reloading process without blocking the caller thread.
     * The reloader task is submitted to the internal processor, allowing the extension loader
     * to refresh its state asynchronously.
     * <p>
     * Note: If synchronous behavior is required instead, use the {@link #reload()} method
     * to ensure the reloading process is completed before proceeding further.
     * <p>
     * It is recommended to use this method when the caller does not need to wait for the
     * completion of the reloading process and prefers non-blocking execution.
     */
    public static void reloadAsynch() {
        instance.processor.submit(instance.reloader);
    }

    /**
     * Reloads the current state of the extension loader.
     * <p>
     * This method synchronously triggers the reloading process, ensuring the extension loader is
     * refreshed and up-to-date. The reloader task is submitted to the processor, followed by
     * a brief pause to ensure the reloading process is initiated properly. It then waits until
     * the loading process is completed to ensure a consistent state before returning.
     * <p>
     * The method performs the following steps:
     * 1. Submits the reloader task to the processor for execution.
     * 2. Waits briefly to prevent missing the onset of the reloading process.
     * 3. Waits until the loading process is flagged as finished.
     * <p>
     * Note: This is a synchronous method. If asynchronous behavior is required, consider
     * using the {@link #reloadAsynch()} method.
     */
    public static void reload() {
        instance.processor.submit(instance.reloader);
        // ensure we don't miss the reloading
        ThreadUtil.sleep(500);
        waitWhileLoading();
    }

    /**
     * Reloads the internal state of the extension loader. This method is responsible for clearing
     * internal caches, stopping daemon plugins, and loading artifacts related to extensions.
     * The following steps are executed during this process:
     * <p>
     * 1. Sets the loading flag to true to indicate the reloading process has started.
     * 2. Stops all active daemon plugins using the {@link #stopDaemons()} method.
     * 3. Checks if there are any waiting tasks in the processor. If there are, the reload is aborted.
     * 4. Clears all internal sets and collections related to JAR files, artifacts, extensions,
     * and plugins.
     * 5. Retrieves the list of artifacts from the local repository using {@link #getExtensionsArtifacts()}
     * and iterates through each artifact, invoking {@link #loadArtifact(Artifact)} to load it.
     * 6. Resets the loading flag to false once all artifacts have been processed.
     * 7. Triggers the {@link #changed()} method to notify listeners of the state change.
     */
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

        ExtensionLoaderProgressEvent event = new ExtensionLoaderProgressEvent();

        final Map<String, Artifact> artifacts = getExtensionsArtifacts();
        event.total(artifacts.size());
        IcyLogger.debug(this.getClass(), "Loading " + artifacts.size() + " artifacts from local repository");
        for (final Artifact artifact : artifacts.values()) {
            event.name(artifact.getGroupId() + ":" + artifact.getArtifactId());
            event.actual(event.actual + 1);
            updateProgress(event);
            IcyLogger.debug(this.getClass(), "Found artifact " + artifact);
            loadArtifact(artifact);
        }

        // TODO: Add native libs loading here

        /*final Set<File> jars = getJarList();
        for (final File jar : jars)
            loadJar(jar, false);*/

        loading = false;
        event.actual(event.total);
        event.name(null);
        event.finished(true);
        updateProgress(event);

        // notify change
        changed();
    }

    /**
     * Resolves an artifact from the given group ID, artifact ID, version, and repositories.
     * This method also resolves and validates the dependencies of the artifact recursively.
     * If the artifact or its dependencies are already present in the cache, it skips resolution for those.
     *
     * @param groupId      The group ID of the artifact to resolve. Must not be null.
     * @param artifactId   The artifact ID of the artifact to resolve. Must not be null.
     * @param version      The version of the artifact to resolve. Must not be null.
     * @param artifacts    A map containing already resolved artifacts, keyed by their coordinates. Must not be null.
     * @param repositories A list of remote repositories to search for the artifact. Must not be null.
     * @return true if the artifact and all its required dependencies were resolved successfully; false otherwise.
     */
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

    /**
     * Resolves an artifact by searching for it using the provided group ID, artifact ID, and version.
     * This method delegates the resolution process to another overloaded method.
     *
     * @param groupId    the group ID of the artifact to be resolved; must not be null.
     * @param artifactId the artifact ID of the artifact to be resolved; must not be null.
     * @param version    the version of the artifact to be resolved; must not be null.
     * @param artifacts  a map of artifacts where the resolved artifact can be looked up or stored; must not be null.
     * @return {@code true} if the artifact is successfully resolved; {@code false} otherwise.
     */
    private boolean resolveArtifact(final @NotNull String groupId, final @NotNull String artifactId, final @NotNull String version, final @NotNull LinkedHashMap<String, Artifact> artifacts) {
        return resolveArtifact(groupId, artifactId, version, artifacts, Collections.emptyList());
    }

    /**
     * Loads the given artifact and integrates it into the system. This includes updating
     * the class loader and registering plugins if the artifact is identified as an extension.
     *
     * @param artifact the artifact to be loaded, which must not be null
     * @return true if the artifact was successfully loaded; false otherwise
     */
    private boolean loadArtifact(final @NotNull Artifact artifact) {
        final File jar = artifact.getFile();
        if (jar == null || !jar.exists() || !jar.isFile()) {
            IcyLogger.error(Icy.class, "Jar not found for artifact: " + artifact);
            return false;
        }

        try {
            // Creates URLClassLoader with artifact URL and parent
            ucl = new URLClassLoader(
                    Stream.concat(
                            Arrays.stream(ucl.getURLs()),
                            Stream.of(jar.toURI().toURL())
                    ).toArray(URL[]::new),
                    ucl.getParent()
            );

            // Registers plugins from artifact if it's an extension
            if (isIcyExtension(artifact)) {
                final Set<ExtensionDescriptor> eds = new HashSet<>();
                final Map<String, PluginDescriptor> pds = new HashMap<>();
                final Map<String, PluginDescriptor> dpds = new HashMap<>();
                final Map<String, PluginDescriptor> apds = new HashMap<>();

                final ExtensionDescriptor ed = new ExtensionDescriptor(artifact);
                // Skips already loaded extensions
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
                            // Registers concrete plugins; flags daemon and actionable ones
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

    /**
     * Determines whether the given artifact is an "icy extension" by checking
     * for the presence of a specific file ("META-INF/extension.yaml") within the artifact's JAR.
     *
     * @param artifact the artifact to be checked; must not be null
     * @return {@code true} if the artifact is an icy extension, {@code false} otherwise
     */
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
     * Resets the state of all active daemons by stopping and subsequently restarting them.
     * The method ensures thread safety by being synchronized and performs the reset operation
     * only if the system is not in a loading state.
     * <p>
     * The reset process involves:
     * 1. Checking if the system is in a loading state. If it is, the reset operation is skipped.
     * 2. Stopping all currently running daemons using the {@code stopDaemons} method.
     * 3. Starting the daemons again using the {@code startDaemons} method.
     * <p>
     * This method is designed to ensure that the daemons are restarted cleanly without
     * interfering with ongoing loading operations.
     */
    public static synchronized void resetDaemons() {
        // reset will be done later
        if (isLoading())
            return;

        stopDaemons();
        startDaemons();
    }

    /**
     * Starts all available daemon plugins that are not marked as inactive.
     * This method initializes and runs all eligible daemon plugins in separate threads,
     * ensuring that they are properly registered and managed by the system.
     * <p>
     * The method performs the following actions:
     * - Ensures the currently active daemons list is empty. If it is not, any existing daemons are stopped.
     * - Retrieves the list of inactive daemons from plugin preferences.
     * - Iterates through all available daemon plugins:
     * - Skips plugins marked as inactive in the preferences.
     * - Instantiates and initializes the plugin.
     * - Starts the plugin in a new daemon thread, making sure the thread name matches the plugin's name.
     * - Registers the plugin with the application's main interface for management and future cleanup.
     * - Adds the plugin to the set of active daemons.
     * - Updates the active daemons list with the newly started daemons.
     * <p>
     * The method is synchronized to prevent concurrent execution, ensuring thread safety
     * when starting or stopping daemon plugins.
     * <p>
     * This method handles exceptions that may occur during the initialization
     * and execution of plugins, logging them through the system exception handler.
     */
    static synchronized void startDaemons() {
        // at this point active daemons should be empty!
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
     * Stops all currently active daemons. This method ensures that each daemon
     * is properly stopped and any exceptions that occur during the stopping
     * process are handled gracefully.
     * <p>
     * The method operates in a synchronized manner to prevent concurrent modifications
     * of the list of active daemons. Each daemon is iterated, stopped, and any thrown
     * exceptions are managed using a dedicated exception handler.
     * <p>
     * After all daemons are stopped, the internal list of active daemons is cleared.
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
     * Retrieves an unmodifiable set of extension descriptors. This method ensures thread safety
     * and returns a copy of the currently loaded extensions.
     *
     * @return an unmodifiable set of {@code ExtensionDescriptor} representing the loaded extensions
     */
    public static @NotNull @Unmodifiable Set<ExtensionDescriptor> getExtensions() {
        prepare();

        // better to return a copy as we have async list loading
        synchronized (instance.extensions) {
            return Set.copyOf(instance.extensions);
        }
    }

    /**
     * Checks if there are pending tasks being processed or if the system is currently in a loading state.
     *
     * @return true if there are tasks waiting to be processed or if the system is loading; false otherwise.
     */
    public static boolean isLoading() {
        return instance.processor.hasWaitingTasks() || instance.loading;
    }

    /**
     * Continuously waits while a loading process is active.
     * This method periodically pauses the current thread for a fixed interval
     * of 100 milliseconds until the loading condition is no longer true.
     * <p>
     * The loading state is checked using the {@code isLoading()} method, which should
     * return a boolean indicating whether the loading process is still ongoing.
     * <p>
     * Utilizes {@code ThreadUtil.sleep(int milliseconds)} to manage the interval between
     * consecutive checks, ensuring that the thread does not consume excessive resources
     * while waiting.
     * <p>
     * This method is blocking and should only be used when necessary, as it prevents
     * the current thread from proceeding until the loading process completes.
     * <p>
     * Note: The implementation of {@code isLoading()} and {@code ThreadUtil.sleep}
     * is assumed to be thread-safe and efficient.
     */
    public static void waitWhileLoading() {
        while (isLoading())
            ThreadUtil.sleep(100);
    }

    /**
     * Checks whether the specified extension is loaded.
     *
     * @param extension   the extension descriptor to check, must not be null
     * @param acceptNewer a flag indicating whether newer versions of the extension are acceptable
     * @return true if the extension is loaded, otherwise false
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
     * Handles change events or state updates related to plugins or extensions.
     * This method performs the following actions:
     * 1. Checks for missing or mis-installed plugins upon the first start, if not already initialized.
     * 2. Starts necessary background daemon processes.
     * 3. Notifies listeners about the changes by firing an event.
     * 4. Executes a background task to preload importer classes to improve performance during runtime operations.
     * <p>
     * Note: This method ensures that the required state and processes are initialized only once and optimizes
     * performance by preloading resource-heavy components asynchronously.
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
     * Checks and categorizes extension descriptors into required, missing, and faulty lists.
     * This method analyzes the state of extensions and organizes them based on their availability and functionality.
     *
     * @param showProgress a boolean flag indicating whether progress should be displayed during the operation
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
     * Adds a new listener to the ExtensionLoader. The listener will be notified of events
     * related to the extension loading process.
     *
     * @param listener the listener to be added. Must not be null.
     */
    public static void addListener(final ExtensionLoader.ExtensionLoaderListener listener) {
        synchronized (instance.listeners) {
            instance.listeners.add(ExtensionLoader.ExtensionLoaderListener.class, listener);
        }
    }

    public static void addProgressListener(final ExtensionLoader.ExtensionLoaderProgressListener listener) {
        synchronized (instance.listeners) {
            instance.listeners.add(ExtensionLoader.ExtensionLoaderProgressListener.class, listener);
        }
    }

    /**
     * Removes a specified listener from the collection of listeners managed by the ExtensionLoader.
     *
     * @param listener the listener to be removed from the ExtensionLoader's listener list
     */
    public static void removeListener(final ExtensionLoader.ExtensionLoaderListener listener) {
        synchronized (instance.listeners) {
            instance.listeners.remove(ExtensionLoader.ExtensionLoaderListener.class, listener);
        }
    }

    public static void removeProgressListener(final ExtensionLoaderProgressListener listener) {
        synchronized (instance.listeners) {
            instance.listeners.remove(ExtensionLoader.ExtensionLoaderProgressListener.class, listener);
        }
    }

    /**
     * Fires an event to all registered listeners of type ExtensionLoader.ExtensionLoaderListener.
     * The event is handled in a thread-safe manner by synchronizing on the listeners collection.
     *
     * @param e the event to be fired, represented by an instance of ExtensionLoader.ExtensionLoaderEvent
     */
    void fireEvent(final ExtensionLoader.ExtensionLoaderEvent e) {
        synchronized (instance.listeners) {
            for (final ExtensionLoader.ExtensionLoaderListener listener : instance.listeners.getListeners(ExtensionLoader.ExtensionLoaderListener.class)) {
                listener.extensionLoaderChanged(e);
            }
        }
    }

    void updateProgress(final ExtensionLoader.ExtensionLoaderProgressEvent e) {
        synchronized (instance.listeners) {
            for (final ExtensionLoader.ExtensionLoaderProgressListener listener : instance.listeners.getListeners(ExtensionLoader.ExtensionLoaderProgressListener.class)) {
                listener.updateProgress(e);
            }
        }
    }

    /**
     * This interface defines a listener for events related to the extension loader.
     * Implementing classes can use this listener mechanism to respond to changes
     * in the state of the extension loader.
     * <p>
     * The extension loader is responsible for managing and loading extensions,
     * such as plugins or additional components. When the state of the extension
     * loader changes (e.g., an extension is added, removed, or updated), the
     * implementing class can perform appropriate actions in response to those changes.
     */
    public interface ExtensionLoaderListener extends EventListener {
        /**
         * Invoked when the state of the extension loader changes. This method is called
         * to notify the listener about a specific event related to the extension loader,
         * such as the addition, removal, or update of an extension.
         *
         * @param e an {@link ExtensionLoader.ExtensionLoaderEvent} object representing
         *          the event details, including the type of change and the affected extension.
         */
        void extensionLoaderChanged(ExtensionLoader.ExtensionLoaderEvent e);
    }

    /**
     * Represents an event associated with the loading of extensions.
     * This class provides mechanisms to manage and evaluate equality
     * of extension loader events.
     */
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

    public interface ExtensionLoaderProgressListener extends EventListener {
        void updateProgress(ExtensionLoaderProgressEvent e);
    }

    public static final class ExtensionLoaderProgressEvent {
        private int actual;
        private int total;
        private @Nullable String name;

        private boolean finished;

        ExtensionLoaderProgressEvent() {
            actual = 0;
            total = 0;
            name = null;
            finished = false;
        }

        void actual(final int actual) {
            this.actual = actual;
        }

        void total(final int total) {
            this.total = total;
        }

        void name(final @Nullable String name) {
            this.name = name;
        }

        void finished(final boolean finished) {
            this.finished = finished;
        }

        public int actual() {
            return actual;
        }

        public int total() {
            return total;
        }

        public @Nullable String name() {
            return name;
        }

        public boolean finished() {
            return finished;
        }
    }

    /**
     * Retrieves the current class loader used by the instance.
     * <p>
     * The method ensures thread-safe access to the class loader by synchronizing on the `ucl` object of the instance.
     *
     * @return The class loader associated with the instance.
     */
    public static ClassLoader getLoader() {
        synchronized (instance.ucl) {
            return instance.ucl;
        }
    }

    /**
     * Loads a class with the specified fully qualified name using a custom class loader.
     *
     * @param className the fully qualified name of the class to be loaded
     * @return the {@code Class} object representing the loaded class
     * @throws ClassNotFoundException if the class cannot be found using the custom class loader
     */
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

    /**
     * Retrieves a set of JAR files based on an extension configuration index.
     * This method attempts to load and parse the extension index file, resolve
     * artifact dependencies, and collect the corresponding JAR files. If any
     * errors occur during the process, an empty set is returned.
     *
     * @return an unmodifiable set of JAR files representing resolved extension
     * dependencies, or an empty set if the index file is not available,
     * unreadable, or if errors occur during artifact resolution
     */
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

    /**
     * Retrieves the extensions artifacts from the configuration file located in the
     * extensions directory. This method reads and decodes the configuration file,
     * parses it as YAML, and processes the entries to resolve extension artifacts.
     * If the configuration file is missing, unreadable, or incorrectly formatted,
     * an error is logged, and an empty map is returned.
     *
     * @return an unmodifiable map where the key is the artifact identifier as a
     * string, and the value is the corresponding {@link Artifact} object.
     * If no artifacts are found or errors occur, an empty map is returned.
     */
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
     * Retrieves an unmodifiable set of daemon plugins currently registered.
     * <p>
     * This method returns a thread-safe, immutable view of the daemon plugins
     * available in the system. It ensures that the retrieved set is consistent
     * with the current state of the internally maintained daemon plugins.
     *
     * @return an unmodifiable set of {@link PluginDescriptor} representing the daemon plugins.
     */
    public static @NotNull @Unmodifiable Set<PluginDescriptor> getDaemonPlugins() {
        synchronized (instance.daemonPlugins) {
            return Set.copyOf(instance.daemonPlugins.values());
        }
    }

    /**
     * Retrieves an unmodifiable set of active plugin daemons.
     * The returned set reflects the current state of active daemons at the time of invocation.
     *
     * @return an unmodifiable set of active {@link PluginDaemon} instances
     */
    @Contract(pure = true)
    public static @NotNull @Unmodifiable Set<PluginDaemon> getActiveDaemons() {
        synchronized (instance.activeDaemons) {
            return Set.copyOf(instance.activeDaemons);
        }
    }

    /**
     * Retrieves an unmodifiable set of plugin descriptors currently loaded in the system.
     * This method ensures thread-safe access to the underlying plugin list and returns a copy
     * to prevent external modification.
     *
     * @return an unmodifiable set containing the plugin descriptors.
     */
    public static @NotNull @Unmodifiable Set<PluginDescriptor> getPlugins() {
        prepare();

        // better to return a copy as we have async list loading
        synchronized (instance.plugins) {
            return Set.copyOf(instance.plugins.values());
        }
    }

    /**
     * Retrieves a set of plugin descriptors associated with the specified class.
     *
     * @param clazz the class for which plugins are to be retrieved; must not be null
     * @return an unmodifiable set of PluginDescriptor instances associated with the specified class; never null
     */
    public static @NotNull @Unmodifiable Set<PluginDescriptor> getPlugins(final @NotNull Class<?> clazz) {
        return getPlugins(clazz, false, false);
    }

    /**
     * Retrieves a set of plugin descriptors that match the specified criteria.
     *
     * @param clazz         the class type that the plugins must be an instance of; must not be null
     * @param wantAbstract  specifies whether abstract plugins should be included in the results
     * @param wantInterface specifies whether interface plugins should be included in the results
     * @return an unmodifiable set of plugin descriptors matching the provided criteria; never null
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
     * Retrieves an unmodifiable set of actionable plugin descriptors.
     * Actionable plugins are those that are currently available and can be executed or interacted with
     * based on the application's state.
     *
     * @return an unmodifiable set containing the actionable {@link PluginDescriptor} instances.
     */
    public static @NotNull @Unmodifiable Set<PluginDescriptor> getActionablePlugins() {
        prepare();

        synchronized (instance.actionablePlugins) {
            return Set.copyOf(instance.actionablePlugins.values());
        }
    }

    /**
     * Retrieves the plugin descriptor associated with the specified class name.
     *
     * @param className the fully qualified name of the class for which the plugin descriptor is to be retrieved
     * @return the plugin descriptor associated with the given class name, or null if no such plugin exists
     */
    public static @Nullable PluginDescriptor getPlugin(final String className) {
        prepare();

        synchronized (instance.plugins) {
            return instance.plugins.getOrDefault(className, null);
        }
    }
}
