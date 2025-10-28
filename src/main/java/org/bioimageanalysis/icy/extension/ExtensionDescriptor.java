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
 */
public final class ExtensionDescriptor {
    private final File jar;
    private final String artifactId;
    private final String groupId;
    private final String name;
    private final Version version;
    private final String description;
    private final Version kernelVersion;

    //private final ExtensionConfig config;

    private final @NotNull IcySVG svg;

    private final List<PluginDescriptor> plugins;

    private final List<Map<String, Object>> dependencies;

    public ExtensionDescriptor(final @NotNull File jar) throws IOException {
        this.jar = jar;
        plugins = new ArrayList<>();
        dependencies = new ArrayList<>();

        final Yaml yaml = new Yaml();
        try (final JarFile jarFile = new JarFile(jar)) {
            final ZipEntry extensionEntry =  jarFile.getEntry("META-INF/extension.yaml");
            final ZipEntry dependenciesEntry =  jarFile.getEntry("META-INF/dependencies.yaml");
            final ZipEntry iconEntry =  jarFile.getEntry("META-INF/icon.svg");

            try (final InputStream is = jarFile.getInputStream(extensionEntry)) {
                final Map<String, Object> properties = yaml.load(is);
                artifactId = (String) properties.get("artifactId");
                groupId = (String) properties.get("groupId");
                name = (String) properties.get("name");
                version = Version.fromString((String) properties.get("version"));
                description = (String) properties.get("description");
                kernelVersion = Version.fromString((String) properties.get("kernelVersion"));
            }

            try (final InputStream is = jarFile.getInputStream(dependenciesEntry)) {
                final List<Map<String, Object>> dependencies = yaml.load(is);
                this.dependencies.addAll(dependencies);
            }

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

    @NotNull
    @Unmodifiable
    List<Map<String, Object>> getDependencies() {
        synchronized (dependencies) {
            return List.copyOf(dependencies);
        }
    }

    public @NotNull File getJar() {
        return jar;
    }

    public @NotNull String getArtifactId() {
        return artifactId;
    }

    public @NotNull String getGroupId() {
        return groupId;
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
        try (final JarFile jarFile = new JarFile(jar)) {
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
        return (groupId.equals(ExtensionLoader.KERNEL_GROUP_ID) && ExtensionLoader.KERNEL_ARTIFACT_IDS.contains(artifactId));
    }

    @Override
    public String toString() {
        return getName() + " v" + getVersion().toShortString();
    }
}
