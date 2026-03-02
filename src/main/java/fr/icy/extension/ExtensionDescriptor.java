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

import fr.icy.common.Version;
import fr.icy.extension.plugin.PluginDescriptor;
import fr.icy.gui.component.icon.IcySVG;
import fr.icy.gui.component.icon.SVGResource;
import org.eclipse.aether.artifact.Artifact;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Represents a descriptor for an extension, encapsulating metadata and related information
 * about the extension, such as its name, version, description, associated plugins, and more.
 * The descriptor is created by analyzing the contents of the extension's JAR file and extracting
 * relevant metadata stored in pre-defined locations (e.g., `META-INF/extension.yaml`).
 * <p>
 * This class is immutable except for the plugin list, which can be modified in a thread-safe manner.
 *
 * @author Thomas Musset
 * @version 3.0.0-a.5
 */
public final class ExtensionDescriptor {
    private final Artifact artifact;
    private final String name;
    private final Version version;
    private final String description;
    private final Version kernelVersion;

    //private final ExtensionConfig config;

    private final @NotNull IcySVG svg;

    private final List<PluginDescriptor> plugins;

    //private final Map<String, List<Map<String, Object>>> dependencies;

    /**
     * Constructs an instance of {@code ExtensionDescriptor} by extracting and initializing
     * details such as name, version, description, kernel version, and icon from the provided
     * artifact's JAR file. If the artifact file is not a valid, resolved JAR file or does not
     * contain required metadata, an exception will be thrown.
     *
     * @param artifact The artifact representing the JAR file containing extension metadata.
     *                 Must not be null, must resolve to an existing and valid JAR file named
     *                 with a ".jar" extension.
     * @throws IOException If an I/O error occurs while reading the JAR file contents.
     * @throws IllegalArgumentException If the artifact is not a valid JAR file or is unresolved.
     */
    public ExtensionDescriptor(final @NotNull Artifact artifact) throws IOException, IllegalArgumentException {
        this.artifact = artifact;
        final File jar = artifact.getFile();
        if (jar == null || !jar.exists() || !jar.isFile() || !jar.getName().endsWith(".jar"))
            throw new IllegalArgumentException("Artifact is not a jar or is not resolved");

        plugins = new ArrayList<>();
        //dependencies = new HashMap<>();

        final Yaml yaml = new Yaml();
        try (final JarFile jarFile = new JarFile(jar)) {
            final ZipEntry extensionEntry =  jarFile.getEntry("META-INF/extension.yaml");
            //final ZipEntry dependenciesEntry =  jarFile.getEntry("META-INF/dependencies.yaml");
            final ZipEntry iconEntry =  jarFile.getEntry("META-INF/icon.svg");

            try (final InputStream is = jarFile.getInputStream(extensionEntry)) {
                final Map<String, Object> properties = yaml.load(is);
                name = (String) properties.get("name");
                version = Version.fromString((String) properties.get("version"));
                description = (String) properties.get("description");
                kernelVersion = Version.fromString((String) properties.get("kernelVersion"));
            }

            /*try (final InputStream is = jarFile.getInputStream(dependenciesEntry)) {
                final Map<String, List<Map<String, Object>>> dependencies = yaml.load(is);
                this.dependencies.putAll(dependencies);
            }*/

            if (iconEntry != null) {
                try (final InputStream is = jarFile.getInputStream(iconEntry)) {
                    if (is != null)
                        svg = new IcySVG(is.readAllBytes());
                    else
                        svg = new IcySVG(SVGResource.EXTENSION_DEFAULT);
                }
            }
            else
                svg = new IcySVG(SVGResource.EXTENSION_DEFAULT);
        }

        //final File configFile = new File(jar.getParentFile(), "config.yaml");
        //config = new ExtensionConfig(configFile);
    }

    /**
     * Adds a plugin to the collection of plugins associated with the extension descriptor.
     * This method is thread-safe.
     *
     * @param pluginDescriptor The plugin descriptor to be added. Must not be null.
     */
    void addPlugin(final @NotNull PluginDescriptor pluginDescriptor) {
        synchronized (plugins) {
            plugins.add(pluginDescriptor);
        }
    }

    /**
     * Retrieves an immutable list of plugin descriptors associated with the extension descriptor.
     * This method is thread-safe and ensures that the underlying collection cannot be modified.
     *
     * @return A non-null, unmodifiable list of {@code PluginDescriptor} instances representing
     *         the plugins associated with the extension descriptor.
     */
    @NotNull
    @Unmodifiable
    public List<PluginDescriptor> getPlugins() {
        synchronized (plugins) {
            return List.copyOf(plugins);
        }
    }

    /*@NotNull
    @Unmodifiable
    Map<String, List<Map<String, Object>>> getDependencies() {
        synchronized (dependencies) {
            return Map.copyOf(dependencies);
        }
    }*/

    /**
     * Retrieves the file associated with the artifact represented by this extension descriptor.
     *
     * @return A non-null {@code File} object representing the resolved artifact's file.
     *         The file is guaranteed to exist and represents a valid location.
     */
    public @NotNull File getFile() {
        return artifact.getFile();
    }

    /**
     * Retrieves the artifact ID associated with this extension descriptor.
     *
     * @return A non-null {@code String} representing the artifact ID. The artifact ID
     *         is a unique identifier used to distinguish this artifact within its group.
     */
    public @NotNull String getArtifactId() {
        return artifact.getArtifactId();
    }

    /**
     * Retrieves the group ID associated with this extension descriptor.
     * The group ID is a unique identifier that categorizes the artifact,
     * typically based on an organization's package naming structure.
     *
     * @return A non-null {@code String} representing the group ID of the artifact.
     */
    public @NotNull String getGroupId() {
        return artifact.getGroupId();
    }

    /**
     * Retrieves the name of the extension descriptor.
     *
     * @return A non-null {@code String} representing the name of the extension.
     *         The name is used to identify the extension descriptor uniquely.
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Retrieves the description of the extension descriptor.
     *
     * @return A non-null {@code String} representing the description of the extension.
     */
    public @NotNull String getDescription() {
        return description;
    }

    /**
     * Retrieves the version of the extension descriptor.
     *
     * @return A non-null {@code Version} object representing the version of the extension.
     */
    public @NotNull Version getVersion() {
        return version;
    }

    /**
     * Retrieves the kernel version associated with the extension descriptor.
     *
     * @return A non-null {@code Version} object representing the kernel version
     *         required or supported by this extension descriptor.
     */
    public @NotNull Version getKernelVersion() {
        return kernelVersion;
    }

    /*public @NotNull ExtensionConfig getConfig() {
        return config;
    }*/

    /**
     * Retrieves the SVG associated with this extension descriptor.
     *
     * @return A non-null {@code IcySVG} object representing the SVG icon or image
     *         linked to this descriptor. The returned object provides access to
     *         renderable SVG representations.
     */
    public @NotNull IcySVG getSVG() {
        return svg;
    }

    /**
     * Retrieves the {@code IcySVG} object associated with the specified name by extracting
     * it from the artifact's JAR file. The method attempts to locate and load an SVG file
     * from the path {@code META-INF/icons/<name>.svg} within the JAR file.
     *
     * @param name The name of the SVG file to retrieve, excluding the file extension. Must not be null.
     * @return An {@code IcySVG} object representing the loaded SVG file, or {@code null}
     *         if the file is not found or an error occurs during retrieval.
     */
    @Nullable
    public IcySVG getSVG(@NotNull final String name) {
        try (final JarFile jarFile = new JarFile(getFile())) {
            final ZipEntry entry = jarFile.getEntry("META-INF/icons/" + name + ".svg");
            if (entry != null) {
                try (final InputStream is = jarFile.getInputStream(entry)) {
                    if (is == null)
                        return null;

                    return new IcySVG(is.readAllBytes());
                }
            }
            return null;
        }
        catch (final Throwable t) {
            return null;
        }
    }

    /**
     * Determines whether the artifact associated with this extension descriptor
     * matches the criteria for being a kernel artifact. This is determined by
     * checking if the artifact's group ID matches the predefined kernel group ID
     * and if the artifact's artifact ID is included in the predefined collection
     * of kernel artifact IDs.
     *
     * @return {@code true} if the artifact is identified as a kernel artifact
     *         based on its group ID and artifact ID; {@code false} otherwise.
     */
    public boolean isKernel() {
        return (artifact.getGroupId().equals(ExtensionLoader.KERNEL_GROUP_ID) && ExtensionLoader.KERNEL_ARTIFACT_IDS.contains(artifact.getArtifactId()));
    }

    /**
     * Returns a string representation of the extension descriptor.
     * The generated string includes the name and short version of the extension,
     * formatted as "<code>name</code> v<code>short-version</code>".
     *
     * @return A non-null {@code String} representing the extension descriptor's name
     *         followed by its version in a short string format.
     */
    @Override
    public String toString() {
        return getName() + " v" + getVersion().toShortString();
    }
}
