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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.xeustechnologies.jcl.JarClassLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public final class ExtensionDescriptor {
    private final JarClassLoader jcl;
    private final File jar;
    private final String artifactId;
    private final String groupId;
    private final String name;
    private final Version version;
    private final String description;
    private final Version kernelVersion;

    private final URL iconURL;
    private final URL darkIconURL;

    private final List<Map<String, Object>> dependencies;

    public ExtensionDescriptor(final @NotNull JarClassLoader jcl, final @NotNull File jar) throws IOException {
        this.jcl = jcl;
        this.jar = jar;
        final Yaml yaml = new Yaml();
        try (final InputStream propertiesIS = jcl.getResourceAsStream("META-INF/extension.yaml")) {
            final Map<String, Object> properties = yaml.load(propertiesIS);
            artifactId = (String) properties.get("artifactId");
            groupId = (String) properties.get("groupId");
            name = (String) properties.get("name");
            version = Version.fromString((String) properties.get("version"));
            description = (String) properties.get("description");
            kernelVersion = Version.fromString((String) properties.get("kernelVersion"));

            dependencies = new ArrayList<>();

            iconURL = jcl.getResource("META-INF/icon.svg");
            darkIconURL = jcl.getResource("META-INF/icon_dark.svg");

            try (final InputStream dependenciesIS = jcl.getResourceAsStream("META-INF/" + properties.get("groupId") + "." + properties.get("artifactId") + "/dependencies.yaml")) {
                final List<Map<String, Object>> dependencies = yaml.load(dependenciesIS);
                this.dependencies.addAll(dependencies);
            }
        }
    }

    @NotNull JarClassLoader getJCL() {
        return jcl;
    }

    @Contract(pure = true)
    @NotNull @Unmodifiable List<Map<String, Object>> getDependencies() {
        return List.copyOf(dependencies);
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

    public @NotNull URL getIconURL() {
        return iconURL;
    }

    public @NotNull URL getDarkIconURL() {
        return darkIconURL;
    }

    public @NotNull Version getKernelVersion() {
        return kernelVersion;
    }

    @Override
    public String toString() {
        return getName() + " v" + getVersion().toShortString();
    }
}
