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

import org.bioimageanalysis.icy.common.Version;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.gui.component.icon.IcySVG;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
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
 * @author Thomas Musset
 * @since 3.O.0
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

    void addPlugin(final @NotNull PluginDescriptor pluginDescriptor) {
        synchronized (plugins) {
            plugins.add(pluginDescriptor);
        }
    }

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

    public @NotNull File getFile() {
        return artifact.getFile();
    }

    public @NotNull String getArtifactId() {
        return artifact.getArtifactId();
    }

    public @NotNull String getGroupId() {
        return artifact.getGroupId();
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public @NotNull Version getVersion() {
        return version;
    }

    public @NotNull Version getKernelVersion() {
        return kernelVersion;
    }

    /*public @NotNull ExtensionConfig getConfig() {
        return config;
    }*/

    /**
     * Returns default extension's SVG
     */
    public @NotNull IcySVG getSVG() {
        return svg;
    }

    /**
     * Returns SVG by it's name in extension's jar (META-INF/icon/...). Can be null.
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

    public boolean isKernel() {
        return (artifact.getGroupId().equals(ExtensionLoader.KERNEL_GROUP_ID) && ExtensionLoader.KERNEL_ARTIFACT_IDS.contains(artifact.getArtifactId()));
    }

    @Override
    public String toString() {
        return getName() + " v" + getVersion().toShortString();
    }
}
