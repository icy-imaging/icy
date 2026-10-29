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

import fr.icy.Icy;
import fr.icy.common.Version;
import fr.icy.common.reflect.ClassUtil;
import fr.icy.extension.plugin.PluginDescriptor;
import fr.icy.extension.plugin.abstract_.Plugin;
import fr.icy.extension.plugin.interface_.PluginDaemon;
import fr.icy.io.Loader;
import fr.icy.shared.logging.CustomLevel;
import fr.icy.shared.task.Task;
import fr.icy.system.IcyExceptionHandler;
import fr.icy.system.SystemUtil;
import fr.icy.system.UserUtil;
import fr.icy.system.preferences.PluginPreferences;
import fr.icy.system.thread.SingleProcessor;
import fr.icy.system.thread.ThreadUtil;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionChecker;
import org.eclipse.aether.collection.DependencySelector;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.internal.impl.scope.OptionalDependencySelector;
import org.eclipse.aether.internal.impl.scope.ScopeDependencySelector;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.graph.selector.AndDependencySelector;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import javax.swing.event.EventListenerList;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import static org.eclipse.aether.repository.RepositoryPolicy.*;
import static org.eclipse.aether.util.artifact.JavaScopes.*;

/**
 * Handles the loading, management, and lifecycle of plugins and extensions.
 * <br>
 * The {@code ExtensionLoader} class provides static methods for:
 * <ul>
 *     <li>Loading and unloading plugins.</li>
 *     <li>Starting, stopping, and resetting daemon plugins.</li>
 *     <li>Managing plugin listeners and events.</li>
 *     <li>Resolving and loading extension artifacts.</li>
 *     <li>Querying loaded plugins.</li>
 * </ul>
 * This class uses various fields and methods for its internal operations,
 * such as managing plugin dependencies, handling asynchronous operations,
 * and notifying listeners about changes in plugin state.
 *
 * @author Thomas Musset
 */
public final class ExtensionLoader {
    private static final Logger LOGGER = Logger.getLogger(ExtensionLoader.class.getName());

    private static final Integer CURRENT_VERSION = 0;

    public final static String PLUGINS_PACKAGE = "icy.plugins";
    public final static String OLD_PLUGINS_PACKAGE = "plugins"; // For legacy compatibility

    public final static String DEFAULT_GROUP_ID = "fr.icy.extension";
    @Unmodifiable
    public static final List<String> KERNEL_ARTIFACT_IDS = List.of(
            "kernel",
            "icy2-api"
    );
    @Unmodifiable
    private static final List<String> EXCLUSIONS = List.of(
            //"fr.icy:icy",
            "org.jetbrains:annotations",
            "org.jspecify:jspecify"
    );

    private static volatile ExtensionLoader instance;

    private static synchronized ExtensionLoader getInstance() {
        final ExtensionLoader i = instance;
        if (i != null)
            return i;
        synchronized (ExtensionLoader.class) {
            if (instance == null)
                instance = new ExtensionLoader();
            return instance;
        }
    }

    private final RepositorySystem repositorySystem;
    private final RepositorySystemSession session;

    private static final RepositoryPolicy SNAPSHOT_POLICY = new RepositoryPolicy(true, UPDATE_POLICY_NEVER, CHECKSUM_POLICY_IGNORE);
    private static final RepositoryPolicy RELEASE_POLICY = new RepositoryPolicy(true, UPDATE_POLICY_NEVER, CHECKSUM_POLICY_IGNORE);

    private final List<RemoteRepository> repositories;

    /**
     * class loader
     */
    private URLClassLoader ucl;

    private final Map<Artifact, URL> artifactURLMap;

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

        repositorySystem = new RepositorySystemSupplier().get();

        final RepositorySystemSession.SessionBuilder sessionBuilder = repositorySystem.createSessionBuilder();
        final LocalRepository localRepo = new LocalRepository(Path.of(System.getProperty("user.home") + "/.m2/repository/"));
        sessionBuilder.withLocalRepositories(localRepo);
        sessionBuilder.setChecksumPolicy(CHECKSUM_POLICY_IGNORE); // TODO: Investigate checksum policy
        sessionBuilder.setUpdatePolicy(UPDATE_POLICY_NEVER); // TODO: Investigate update policy
        sessionBuilder.setArtifactDescriptorPolicy(new SimpleArtifactDescriptorPolicy(ArtifactDescriptorPolicy.IGNORE_ERRORS));
        sessionBuilder.setConfigProperties(Map.of(
                "aether.dependencyCollector.impl", "bf"
        ));
        sessionBuilder.setSystemProperties(System.getProperties());
        sessionBuilder.setOffline(Icy.isNetworkDisabled());

        final DependencySelector scopeSelector = ScopeDependencySelector.fromRoot(
                List.of(COMPILE, RUNTIME),
                List.of(TEST, PROVIDED, SYSTEM)
        );
        final DependencySelector optionalSelector = OptionalDependencySelector.fromRoot();
        sessionBuilder.setDependencySelector(AndDependencySelector.newInstance(scopeSelector, optionalSelector));

        session = sessionBuilder.build();

        final RemoteRepository central = new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/")
                .setSnapshotPolicy(SNAPSHOT_POLICY)
                .setReleasePolicy(RELEASE_POLICY)
                .build();
        final RemoteRepository scijava_releases = new RemoteRepository.Builder("scijava.releases", "default", "https://maven.scijava.org/content/repositories/releases/")
                .setSnapshotPolicy(SNAPSHOT_POLICY)
                .setReleasePolicy(RELEASE_POLICY)
                .build();
        final RemoteRepository ome = new RemoteRepository.Builder("ome", "default", "https://artifacts.openmicroscopy.org/artifactory/ome.releases/")
                .setSnapshotPolicy(SNAPSHOT_POLICY)
                .setReleasePolicy(RELEASE_POLICY)
                .build();
        final RemoteRepository jogamp = new RemoteRepository.Builder("jogamp", "default", "https://jogamp.org/deployment/maven/")
                .setSnapshotPolicy(SNAPSHOT_POLICY)
                .setReleasePolicy(RELEASE_POLICY)
                .build();
        final RemoteRepository netbeans = new RemoteRepository.Builder("netbeans", "default", "https://netbeans.apidesign.org/maven2/")
                .setSnapshotPolicy(SNAPSHOT_POLICY)
                .setReleasePolicy(RELEASE_POLICY)
                .build();
        final RemoteRepository jboss_releases = new RemoteRepository.Builder("jboss.releases", "default", "https://repository.jboss.org/nexus/repository/releases/")
                .setSnapshotPolicy(SNAPSHOT_POLICY)
                .setReleasePolicy(RELEASE_POLICY)
                .build();

        final RemoteRepository central_snapshots = new RemoteRepository.Builder("central.snapshots", "default", "https://central.sonatype.com/repository/maven-snapshots/")
                .setSnapshotPolicy(SNAPSHOT_POLICY)
                .setReleasePolicy(RELEASE_POLICY)
                .build();

        final RemoteRepository jboss_public = new RemoteRepository.Builder("jboss.public", "default", "https://repository.jboss.org/nexus/content/repositories/public/")
                .setSnapshotPolicy(SNAPSHOT_POLICY)
                .setReleasePolicy(RELEASE_POLICY)
                .build();
        final RemoteRepository scijava_public = new RemoteRepository.Builder("scijava.public", "default", "https://maven.scijava.org/content/groups/public/")
                .setSnapshotPolicy(SNAPSHOT_POLICY)
                .setReleasePolicy(RELEASE_POLICY)
                .build();

        repositories = new LinkedList<>();
        /*repositories.add(central);
        repositories.add(scijava_releases);
        repositories.add(ome);
        repositories.add(jogamp);
        repositories.add(netbeans);
        repositories.add(jboss_releases);
        repositories.add(central_snapshots);*/
        //repositories.add(jboss_public);
        //repositories.add(scijava_public);

        //ucl = new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());
        ucl = new URLClassLoader(new URL[0], SystemUtil.getSystemClassLoader());

        artifactURLMap = new HashMap<>();
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

    private void reloadInternal() {
        if (LOGGER.isLoggable(CustomLevel.INFO))
            LOGGER.info("Loading extensions…");

        loading = true;

        stopDaemons();

        // no need to complete loading…
        if (processor.hasWaitingTasks())
            return;

        artifactURLMap.clear();
        extensionsArtifacts.clear();
        extensions.clear();
        plugins.clear();
        actionablePlugins.clear();
        daemonPlugins.clear();

        final Set<Artifact> coordinates = getExtensionCoordinates();
        /*final LinkedHashMap<String, Artifact> seen = new LinkedHashMap<>();
        for (final Artifact coordinate : coordinates) {
            resolveArtifact(coordinate, seen);
        }*/

        final boolean resolved = resolveExtensions(coordinates);

        // Stop if no Jar found
        if (artifactURLMap.isEmpty() || !resolved) {
            loading = false;
            changed();
            return;
        }

        ucl = new URLClassLoader(artifactURLMap.values().toArray(new URL[0]), SystemUtil.getSystemClassLoader());

        for (final Artifact artifact : artifactURLMap.keySet())
            loadArtifact(artifact);

        loading = false;

        changed();
    }

    @ApiStatus.Experimental
    private synchronized Set<Artifact> getExtensionCoordinates() {
        final File extensionsFile = new File(UserUtil.getIcyHomeDirectory(), "extensions.yaml");
        if (!extensionsFile.exists() || !extensionsFile.isFile() || !extensionsFile.canRead()) {
            LOGGER.log(CustomLevel.ERROR, "Cannot find or read extensions config file: " + extensionsFile.getAbsolutePath());
            return Collections.emptySet();
        }

        final Map<String, Object> content;
        try (final FileInputStream is = new FileInputStream(extensionsFile)) {
            final Yaml yaml = new Yaml();
            final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            content = yaml.loadAs(reader, Map.class);
            reader.close();
        }
        catch (final Throwable t) {
            LOGGER.log(CustomLevel.ERROR, "Failed to read extensions file", t);
            return Collections.emptySet();
        }

        if (content.isEmpty()) {
            LOGGER.log(CustomLevel.ERROR, "Extensions config file is empty");
            return Collections.emptySet();
        }

        final Integer fileVersion = (Integer) content.getOrDefault("version", -1);
        if (fileVersion < 0) {
            LOGGER.log(CustomLevel.ERROR, "Extensions config file is invalid (no version found)");
            return Collections.emptySet();
        }

        if (fileVersion > CURRENT_VERSION) {
            LOGGER.log(CustomLevel.ERROR, "Extensions config file is invalid (unsupported version)");
            return Collections.emptySet();
        }

        if (fileVersion < CURRENT_VERSION) {
            upgradeYaml(content);
        }

        @SuppressWarnings("unchecked")
        final SortedMap<Integer, Map<String, Object>> extensions = new TreeMap<>((Map<Integer, Map<String, Object>>) content.getOrDefault("extensions", new TreeMap<>()));
        if (extensions.isEmpty()) {
            LOGGER.log(CustomLevel.ERROR, "Extensions config file is invalid (no extensions found)");
            return Collections.emptySet();
        }

        final Set<Artifact> coordinates = new LinkedHashSet<>();
        final Set<String> seenRepos = new HashSet<>();
        for (final Map.Entry<Integer, Map<String, Object>> entry : extensions.entrySet()) {
            final String groupId = entry.getValue().get("groupId").toString();
            final String artifactId = entry.getValue().get("artifactId").toString();
            final String version = entry.getValue().get("version").toString();
            @SuppressWarnings("unchecked")
            final Map<String, String> distribution = (Map<String, String>) entry.getValue().getOrDefault("distribution", Collections.emptyMap());
            final Boolean enabled = (Boolean) entry.getValue().getOrDefault("enabled", Boolean.TRUE);

            if (!enabled) {
                if (LOGGER.isLoggable(CustomLevel.INFO))
                    LOGGER.log(CustomLevel.INFO, "Extension " + groupId + ":" + artifactId + ":" + version + " is disabled.");
                continue;
            }

            coordinates.add(new DefaultArtifact(groupId, artifactId, null, "jar", version));

            if (!distribution.isEmpty() && (distribution.containsKey("id") && distribution.containsKey("url")))
                if (seenRepos.add(distribution.get("url")))
                    repositories.add(new RemoteRepository.Builder(distribution.get("id"), "default", distribution.get("url")).setSnapshotPolicy(SNAPSHOT_POLICY).setReleasePolicy(RELEASE_POLICY).build());
        }

        LOGGER.log(CustomLevel.DEBUG, "repositories: " + repositories);

        return Collections.unmodifiableSet(coordinates);
    }

    @ApiStatus.Experimental
    private synchronized boolean resolveExtensions(final @NonNull Set<Artifact> coordinates) {
        final List<Dependency> roots = coordinates.stream().map(coordinate -> new Dependency(coordinate, RUNTIME)).toList();

        final CollectRequest request = new CollectRequest(roots, List.of(), repositories);

        final DependencyFilter filter = DependencyFilterUtils.classpathFilter(COMPILE, RUNTIME);

        try {
            final DependencyResult result = repositorySystem.resolveDependencies(session, new DependencyRequest(request, filter));

            collect(result.getRoot(), new HashSet<>());

            return true;
        }
        catch (final Throwable t) {
            LOGGER.log(CustomLevel.ERROR, "Failed to resolve extensions.", t);
            return false;
        }
    }

    @ApiStatus.Experimental
    private void collect(final @NonNull DependencyNode node, final Set<String> seen) throws Exception {
        final Artifact artifact = node.getArtifact();
        if (artifact == null) { // virtual root node when resolving several coordinates at once
            LOGGER.log(CustomLevel.DEBUG, "Skipping virtual root node.");
            for (final DependencyNode child : node.getChildren()) {
                collect(child, seen);
            }
            return;
        }
        // dedupe (diamonds in the graph) and guard against cycles
        final String key = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
        if (seen.add(key) && artifact.getPath() != null) {
            LOGGER.log(CustomLevel.DEBUG, "Node added to URL map: " + node);
            artifactURLMap.put(artifact, artifact.getPath().toUri().toURL());
            for (final DependencyNode child : node.getChildren()) {
                collect(child, seen);
            }
        }
    }

    @Deprecated
    private boolean resolveArtifact(final @NonNull String groupId, final @NonNull String artifactId, final @Nullable String classifier, final @NonNull String version, final @NonNull LinkedHashMap<String, Artifact> seen, final @NonNull List<RemoteRepository> repositories) {
        final String dashClassifier = classifier == null || classifier.isBlank() ? "" : "-" + classifier;
        if (seen.containsKey(groupId + ":" + artifactId + dashClassifier + ":jar:" + version))
            return true;

        LOGGER.log(CustomLevel.DEBUG, "Resolving artifact: " + groupId + ":" + artifactId + dashClassifier + ":jar:" + version);

        // Resolve artifact
        final Artifact artifact = new DefaultArtifact(groupId, artifactId, classifier, "jar", version);
        final ArtifactRequest artifactRequest = new ArtifactRequest()
                .setArtifact(artifact)
                .setRepositories(this.repositories);

        if (!repositories.isEmpty()) {
            for (final RemoteRepository repository : repositories) {
                if (!this.repositories.contains(repository))
                    artifactRequest.addRepository(repository);
            }
        }

        final ArtifactResult artifactResult;
        try {
            artifactResult = repositorySystem.resolveArtifact(session, artifactRequest);
        }
        catch (final Throwable t) {
            LOGGER.log(CustomLevel.ERROR, "Failed to resolve artifact: " + groupId + ":" + artifactId + dashClassifier + ":jar:" + version + ".", t);
            return false;
        }

        if (!artifactResult.isResolved() || artifactResult.getArtifact() == null)
            return false;
        if (seen.containsKey(artifactResult.getArtifact().toString()))
            return true;

        // Collect dependencies
        final ArtifactDescriptorRequest artifactDescriptorRequest = new ArtifactDescriptorRequest()
                .setArtifact(artifactResult.getArtifact())
                .setRepositories(this.repositories);

        if (!repositories.isEmpty()) {
            for (final RemoteRepository repository : repositories) {
                if (!this.repositories.contains(repository))
                    artifactDescriptorRequest.addRepository(repository);
            }
        }

        final ArtifactDescriptorResult artifactDescriptorResult;
        try {
            artifactDescriptorResult = repositorySystem.readArtifactDescriptor(session, artifactDescriptorRequest);
        }
        catch (final Throwable t) {
            LOGGER.log(CustomLevel.ERROR, "Failed to read artifact descriptor: " + groupId + ":" + artifactId + dashClassifier + ":jar:" + version + ".", t);
            return false;
        }

        for (final Dependency dependency : artifactDescriptorResult.getDependencies()) {
            if (!dependency.getScope().equalsIgnoreCase(JavaScopes.COMPILE) && !dependency.getScope().equalsIgnoreCase(RUNTIME))
                continue;
            if (dependency.isOptional())
                continue;
            final Artifact dependencyArtifact = dependency.getArtifact();
            if (!dependencyArtifact.getExtension().endsWith("jar"))
                continue;
            final String dependencyDashClassifier = dependencyArtifact.getClassifier() == null || dependencyArtifact.getClassifier().isBlank() ? "" : "-" + dependencyArtifact.getClassifier();
            if (seen.containsKey(dependencyArtifact.getGroupId() + ":" + dependencyArtifact.getArtifactId() + dependencyDashClassifier + ":jar:" + dependencyArtifact.getVersion()))
                continue;

            if (EXCLUSIONS.contains(dependencyArtifact.getGroupId() + ":" + dependencyArtifact.getArtifactId() + dependencyDashClassifier))
                continue;

            final boolean dependencyResolved = resolveArtifact(dependencyArtifact.getGroupId(), dependencyArtifact.getArtifactId(), dependencyArtifact.getClassifier(), dependencyArtifact.getVersion(), seen, artifactDescriptorResult.getRepositories());
            if (!dependencyResolved)
                return false;
        }

        final Artifact a = artifactResult.getArtifact();
        seen.put(groupId + ":" + artifactId + dashClassifier + ":jar:" + version, a);
        try {
            final URL url = a.getPath().toUri().toURL();
            if (!artifactURLMap.containsValue(url))
                artifactURLMap.put(a, url);
        }
        catch (final Throwable t) {
            LOGGER.log(CustomLevel.ERROR, "Failed to add artifact to URL set: " + groupId + ":" + artifactId + ":" + version + ".", t);
            return false;
        }
        return true;
    }

    @Deprecated
    private boolean resolveArtifact(final @NonNull String groupId, final @NonNull String artifactId, final @Nullable String classifier, final @NonNull String version, final @NonNull LinkedHashMap<String, Artifact> artifacts) {
        return resolveArtifact(groupId, artifactId, classifier, version, artifacts, Collections.emptyList());
    }

    @Deprecated
    private boolean resolveArtifact(final @NonNull Artifact artifact, final LinkedHashMap<String, Artifact> artifacts) {
        return resolveArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(), artifact.getVersion(), artifacts);
    }

    @ApiStatus.Experimental
    private synchronized boolean loadArtifact(final @NonNull Artifact artifact) {
        if (artifact.getPath() == null) {
            LOGGER.log(CustomLevel.ERROR, "Artifact doesn't have a path: " + artifact);
            return false;
        }

        final File jar = artifact.getPath().toFile();
        if (!jar.exists() || !jar.isFile()) {
            LOGGER.log(CustomLevel.ERROR, "Jar not found for artifact: " + artifact);
            return false;
        }

        try {
            final Map<String, Object> data = getExtensionData(artifact);
            if (!data.isEmpty()) {
                final Map<String, PluginDescriptor> pds = new HashMap<>();
                final Map<String, PluginDescriptor> dpds = new HashMap<>();
                final Map<String, PluginDescriptor> apds = new HashMap<>();

                final ExtensionDescriptor ed = new ExtensionDescriptor(artifact);
                if (extensionsArtifacts.containsKey(ed.getGroupId())) {
                    final Set<String> artifactIds = extensionsArtifacts.get(ed.getGroupId());
                    if (artifactIds.contains(ed.getArtifactId()))
                        return true;
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
                                    if (LOGGER.isLoggable(CustomLevel.WARNING))
                                        LOGGER.log(CustomLevel.WARNING, "Old plugins package detected for " + className);
                            }
                        }
                        catch (final Throwable t) {
                            // ignored
                        }
                    }
                }

                extensions.add(ed);
                plugins.putAll(pds);
                actionablePlugins.putAll(apds);
                daemonPlugins.putAll(dpds);
            }
        }
        catch (final Throwable t) {
            LOGGER.log(CustomLevel.ERROR, "Failed to load artifact: " + artifact, t);
            return false;
        }

        return true;
    }

    private Map<String, Object> getExtensionData(final @NonNull Artifact artifact) {
        final File jar = artifact.getPath().toFile();
        if (!jar.exists() || !jar.isFile()) {
            // Shouldn't happen, but just in case…
            return Collections.emptyMap();
        }

        Map<String, Object> content;
        final ZipEntry ze;
        try (final JarFile jarFile = new JarFile(jar)) {
            ze = jarFile.getEntry("META-INF/extension.yaml");
            if (ze == null)
                return Collections.emptyMap();


            try (final InputStream is = jarFile.getInputStream(ze)) {
                final Yaml yaml = new Yaml();
                content = yaml.loadAs(is, Map.class);
            }
            catch (final Throwable t) {
                content = Collections.emptyMap();
            }
        }
        catch (final Throwable t) {
            content = Collections.emptyMap();
        }

        return content;
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
     * fire event
     */
    private void fireEvent(final ExtensionLoader.ExtensionLoaderEvent e) {
        synchronized (instance.listeners) {
            for (final ExtensionLoader.ExtensionLoaderListener listener : instance.listeners.getListeners(ExtensionLoader.ExtensionLoaderListener.class)) {
                listener.extensionLoaderChanged(e);
            }
        }
    }

    @FunctionalInterface
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

    @Deprecated
    private @NonNull @Unmodifiable Map<String, Artifact> resolveExtensions() {
        final File extensionsFile = new File(UserUtil.getIcyHomeDirectory(), "extensions.yaml");
        if (!extensionsFile.exists() || !extensionsFile.isFile() || !extensionsFile.canRead()) {
            LOGGER.log(CustomLevel.ERROR, "Cannot find or read extensions config file: " + extensionsFile.getAbsolutePath());
            return Collections.emptyMap();
        }

        final Map<String, Object> content;
        try (final FileInputStream is = new FileInputStream(extensionsFile)) {
            final Yaml yaml = new Yaml();
            final Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8);
            content = yaml.loadAs(reader, Map.class);
            reader.close();
        }
        catch (final Throwable t) {
            LOGGER.log(CustomLevel.ERROR, "Failed to read extensions file", t);
            return Collections.emptyMap();
        }

        if (content.isEmpty()) {
            LOGGER.log(CustomLevel.ERROR, "Extensions config file is empty");
            return Collections.emptyMap();
        }

        final Integer fileVersion = (Integer) content.getOrDefault("version", -1);
        if (fileVersion < 0) {
            LOGGER.log(CustomLevel.ERROR, "Extensions config file is invalid (no version found)");
            return Collections.emptyMap();
        }

        if (fileVersion > CURRENT_VERSION) {
            LOGGER.log(CustomLevel.ERROR, "Extensions config file is invalid (unsupported version)");
            return Collections.emptyMap();
        }

        if (fileVersion < CURRENT_VERSION) {
            upgradeYaml(content);
        }

        @SuppressWarnings("unchecked")
        final SortedMap<Integer, Map<String, Object>> extensions = new TreeMap<>((Map<Integer, Map<String, Object>>) content.getOrDefault("extensions", new TreeMap<>()));
        if (extensions.isEmpty()) {
            LOGGER.log(CustomLevel.ERROR, "Extensions config file is invalid (no extensions found)");
            return Collections.emptyMap();
        }

        final LinkedHashMap<String, Artifact> artifacts = new LinkedHashMap<>();
        for (final Map.Entry<Integer, Map<String, Object>> entry : extensions.entrySet()) {
            final String groupId = entry.getValue().get("groupId").toString();
            final String artifactId = entry.getValue().get("artifactId").toString();
            final String classifier = entry.getValue().getOrDefault("classifier", "").toString();
            final String version = entry.getValue().get("version").toString();
            final Boolean enabled = (Boolean) entry.getValue().getOrDefault("enabled", Boolean.TRUE);

            if (!enabled) {
                if (LOGGER.isLoggable(CustomLevel.INFO))
                    LOGGER.log(CustomLevel.INFO, "Extension " + groupId + ":" + artifactId + ":" + version + " is disabled.");
                continue;
            }

            final boolean resolved = resolveArtifact(groupId, artifactId, classifier.isBlank() ? null : classifier, version, artifacts);
            if (!resolved)
                LOGGER.log(CustomLevel.ERROR, "Unable to resolve artifact " + groupId + ":" + artifactId + (classifier.isBlank() ? "" : ":" + classifier) + ":" + version);
        }

        return Collections.unmodifiableMap(artifacts);
    }

    private void upgradeYaml(final Map<String, Object> content) {
        // TODO: implement upgrade function when new version released
    }

    /* === Utilities === */

    private static synchronized void prepare() {
        if (!getInstance().initialized) {
            if (isLoading())
                waitWhileLoading();
            /*else
                reload();*/
        }
    }

    public static class ReloadTask extends Task {
        protected ReloadTask() {
            super(1L, "Reload extension task");
        }

        @Override
        public void execute() throws Exception {
            LOGGER.log(CustomLevel.DEBUG, "Reloading extensions (internal)…");
            getInstance().reloadInternal();
        }
    }

    /**
     * Reload the list of installed plugins (asynchronous version).
     */
    public static synchronized void reloadAsync() {
        /*LOGGER.log(CustomLevel.DEBUG, "Creating pipeline");
        final TaskExecutor executor = TaskExecutor.getInstance();
        final Pipeline pipeline = new Pipeline("Extension reload");
        LOGGER.log(CustomLevel.DEBUG, "Adding task");
        pipeline.addTask(new ReloadTask());

        new Thread(() -> {
            try {
                LOGGER.log(CustomLevel.DEBUG, "Executing pipeline");
                executor.execute(pipeline);
            }
            catch (final TaskExecutionException e) {
                e.getFailedTasks().forEach(task ->
                        task.getError().ifPresent(err -> {
                            if (err instanceof InterruptedException) {
                                if (LOGGER.isLoggable(CustomLevel.WARNING))
                                    LOGGER.log(CustomLevel.WARNING, "Extension loading interrupted");
                            }
                            else
                                LOGGER.log(CustomLevel.ERROR, "Failed to load extensions", err);
                        })
                );
            }
        }).start();*/

        getInstance().processor.submit(getInstance().reloader);
    }

    /**
     * Reload the list of installed plugins (wait for completion).
     */
    public static synchronized void reload() {
        getInstance().processor.submit(getInstance().reloader);
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
     * Start daemons plugins.
     */
    private static synchronized void startDaemons() {
        // at this point active daemons should be empty !
        synchronized (getInstance().activeDaemons) {
            if (!getInstance().activeDaemons.isEmpty())
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

            getInstance().activeDaemons.clear();
            getInstance().activeDaemons.addAll(newDaemons);
        }
    }

    /**
     * Stop daemons plugins.
     */
    public synchronized static void stopDaemons() {
        synchronized (getInstance().activeDaemons) {
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
            getInstance().activeDaemons.clear();
        }
    }

    /**
     * @return the loading
     */
    public static synchronized boolean isLoading() {
        return getInstance().processor.hasWaitingTasks() || getInstance().loading;
    }

    /**
     * wait until loading completed
     */
    public static synchronized void waitWhileLoading() {
        while (isLoading())
            ThreadUtil.sleep(100);
    }

    /**
     * Returns <code>true</code> if the specified extension exists in the {@link ExtensionLoader}.
     *
     * @param extension   the extension we are looking for.
     * @param acceptNewer allow newer version of the extension
     */
    public static synchronized boolean isLoaded(final @NonNull ExtensionDescriptor extension, final boolean acceptNewer) {
        synchronized (getInstance().extensions) {
            final ExtensionDescriptor ed = getInstance().extensions.stream().filter(e -> e.getGroupId().equals(extension.getGroupId()) && e.getArtifactId().equals(extension.getArtifactId())).findFirst().orElse(null);
            if (ed == null)
                return false;

            if (!acceptNewer)
                return ed.getVersion().equals(extension.getVersion());
            else
                return ed.getVersion().compareTo(extension.getVersion()) >= 0;
        }
    }

    public static synchronized boolean isLoaded(final @NonNull ExtensionDescriptor extension) {
        return isLoaded(extension, true);
    }

    public static synchronized boolean isLoaded(final @NonNull String groupId, final @NonNull String artifactId, final @Nullable String version, final boolean acceptNewer) {
        synchronized (getInstance().extensions) {
            final ExtensionDescriptor ed = getInstance().extensions.stream().filter(e -> e.getGroupId().equals(groupId) && e.getArtifactId().equals(artifactId)).findFirst().orElse(null);
            if (ed == null)
                return false;

            if (version == null)
                return true;
            else if (!acceptNewer)
                return ed.getVersion().equals(Version.fromString(version));
            else
                return ed.getVersion().compareTo(Version.fromString(version)) >= 0;
        }
    }

    public static synchronized boolean isLoaded(final @NonNull String groupId, final @NonNull String artifactId, final @Nullable String version) {
        return isLoaded(groupId, artifactId, version, true);
    }

    public static synchronized boolean isLoaded(final @NonNull String groupId, final @NonNull String artifactId) {
        return isLoaded(groupId, artifactId, null);
    }

    /**
     * Check for missing plugins and install them if needed.
     */
    public static synchronized void checkExtensions(final boolean showProgress) {
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
    public static synchronized void addListener(final ExtensionLoader.ExtensionLoaderListener listener) {
        synchronized (getInstance().listeners) {
            getInstance().listeners.add(ExtensionLoader.ExtensionLoaderListener.class, listener);
        }
    }

    /**
     * Remove a listener
     */
    public static synchronized void removeListener(final ExtensionLoader.ExtensionLoaderListener listener) {
        synchronized (getInstance().listeners) {
            getInstance().listeners.remove(ExtensionLoader.ExtensionLoaderListener.class, listener);
        }
    }

    /**
     * Return the loaders list
     */
    public static synchronized ClassLoader getLoader() {
        //synchronized (instance.ucl) {
        synchronized (ExtensionLoader.class) {
            return getInstance().ucl;
        }
    }

    public static synchronized @NonNull Class<?> loadClass(final String className) throws ClassNotFoundException {
        prepare();

        //synchronized (instance.ucl) {
        synchronized (ExtensionLoader.class) {
            final Class<?> result = getInstance().ucl.loadClass(className);

            if (result == null)
                throw new ClassNotFoundException(className);

            // try to load class
            return result;
        }
    }

    /**
     * Return the list of loaded plugins.
     */
    public static synchronized @NonNull @Unmodifiable Set<ExtensionDescriptor> getExtensions() {
        synchronized (getInstance().extensions) {
            return Set.copyOf(getInstance().extensions);
        }
    }

    /**
     * Returns the list of daemon type plugins.
     */
    public static @NonNull @Unmodifiable Set<PluginDescriptor> getDaemonPlugins() {
        synchronized (getInstance().daemonPlugins) {
            return Set.copyOf(getInstance().daemonPlugins.values());
        }
    }

    /**
     * Returns the list of active daemon plugins.
     */
    @Contract(pure = true)
    public static @NonNull @Unmodifiable Set<PluginDaemon> getActiveDaemons() {
        synchronized (getInstance().activeDaemons) {
            return Set.copyOf(getInstance().activeDaemons);
        }
    }

    /**
     * Return the list of "actionable" plugins (mean we can launch them from GUI).
     */
    public static synchronized @NonNull @Unmodifiable Set<PluginDescriptor> getActionablePlugins() {
        prepare();

        synchronized (getInstance().actionablePlugins) {
            return Set.copyOf(getInstance().actionablePlugins.values());
        }
    }

    /**
     * Return the list of loaded plugins.
     */
    public static @NonNull @Unmodifiable Set<PluginDescriptor> getPlugins() {
        prepare();

        // better to return a copy as we have async list loading
        synchronized (getInstance().plugins) {
            return Set.copyOf(getInstance().plugins.values());
        }
    }

    /**
     * Return the list of loaded plugins which derive from the specified class.
     *
     * @param clazz The class object defining the class we want plugins to derive from.
     */
    public static @NonNull @Unmodifiable Set<PluginDescriptor> getPlugins(final @NonNull Class<?> clazz) {
        return getPlugins(clazz, false, false);
    }

    /**
     * Return the list of loaded plugins which derive from the specified class.
     *
     * @param clazz         The class object defining the class we want plugins to derive from.
     * @param wantAbstract  specify if we also want abstract classes
     * @param wantInterface specify if we also want interfaces
     */
    public static synchronized @NonNull @Unmodifiable Set<PluginDescriptor> getPlugins(final @NonNull Class<?> clazz, final boolean wantAbstract, final boolean wantInterface) {
        prepare();

        final Set<PluginDescriptor> result = new HashSet<>();

        synchronized (getInstance().plugins) {
            for (final PluginDescriptor pluginDescriptor : getInstance().plugins.values()) {
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
     * Returns the plugin corresponding to the specified plugin class name.<br>
     * Returns <code>null</code> if the plugin does not exists in any loaders.
     *
     * @param className class name of the plugin we are looking for.
     */
    public static synchronized @Nullable PluginDescriptor getPlugin(final String className) {
        prepare();

        synchronized (getInstance().plugins) {
            return getInstance().plugins.getOrDefault(className, null);
        }
    }
}
