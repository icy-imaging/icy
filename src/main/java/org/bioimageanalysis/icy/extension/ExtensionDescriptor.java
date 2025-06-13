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

import org.bioimageanalysis.icy.common.Version;
import org.bioimageanalysis.icy.extension.abstract_.Extension;
import org.bioimageanalysis.icy.extension.annotation_.IcyExtension;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.abstract_.Plugin;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.io.FileUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ExtensionDescriptor {
    private String name;
    private Version version;
    private String description;
    private Icon icon;
    private SVGIcon svgIcon;

    private Extension extension;
    private final ArrayList<PluginDescriptor> plugins = new ArrayList<>();

    @Contract(pure = true)
    public ExtensionDescriptor() {
        name = "";
        version = new Version();
        description = "";
        icon = null;
        svgIcon = null;
        extension = null;
    }

    public ExtensionDescriptor(final @NotNull Class<? extends Extension> clazz) {
        this();

        try {
            extension = clazz.getDeclaredConstructor().newInstance();
        }
        catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            IcyLogger.error(this.getClass(), e, "Unable to load extension: " + clazz.getSimpleName());
            return;
        }

        if (!readExtensionFile(clazz)) {
            return;
        }

        synchronized (plugins) {
            for (final Class<? extends Plugin> plugin : extension.getPlugins()) {
                plugins.add(new PluginDescriptor(plugin));
            }
        }
    }

    public String getName() {
        return name;
    }

    public Version getVersion() {
        return version;
    }

    public ArrayList<PluginDescriptor> getPlugins() {
        return plugins;
    }

    private boolean readExtensionFile(final @NotNull Class<? extends Extension> clazz) {
        final IcyExtension icyExtension = clazz.getAnnotation(IcyExtension.class);

        if (icyExtension == null) {
            IcyLogger.error(this.getClass(), "Cannot load an extension that is not annotated with @IcyExtension");
            return false;
        }



        final String extensionFilePath = icyExtension.path() + File.separator + "extension.properties";
        if (extensionFilePath != null && !extensionFilePath.isBlank()) {
            final String fileExt = FileUtil.getFileExtension(extensionFilePath, false);
            return switch (fileExt.toLowerCase(Locale.getDefault())) {
                case "yml", "yaml" -> readYAMLFile(extensionFilePath);
                case "json" -> readJSONFile(extensionFilePath);
                case "xml" -> readXMLFile(extensionFilePath);
                case "properties" -> readPropertiesFile(extensionFilePath);
                default -> {
                    IcyLogger.error(this.getClass(), "Unsupported format for extension descriptor: " + clazz.getSimpleName() + " - " + extensionFilePath);
                    yield false;
                }
            };
        }
        else {
            IcyLogger.error(this.getClass(), "Cannot load an extension that is annotated with an empty @IcyExtension");
            return false;
        }
    }

    private boolean readYAMLFile(final String path) {
        IcyLogger.debug(this.getClass(), "Reading YAML extension file: " + path);

        final Yaml yaml = new Yaml();
        try (final InputStream is = extension.getClass().getResourceAsStream(path)) {
            final HashMap<?, ?> map = yaml.load(Objects.requireNonNull(is));
            name = (String) map.get("name");
            version = Version.fromString((String) map.get("version"));
        }
        catch (final IOException | NullPointerException e) {
            IcyLogger.error(this.getClass(), e, "Unable to read YAML extension file: " + path);
            return false;
        }

        return true;
    }

    private boolean readJSONFile(final String path) {
        IcyLogger.debug(this.getClass(), "Reading JSON extension file: " + path);
        return true;
    }

    private boolean readXMLFile(final String path) {
        IcyLogger.debug(this.getClass(), "Reading XML extension file: " + path);
        return true;
    }

    private boolean readPropertiesFile(final String path) {
        IcyLogger.debug(this.getClass(), "Reading Properties extension file: " + path);

        final Properties props = new Properties();
        try(final InputStream is = extension.getClass().getResourceAsStream(path)) {
            props.load(Objects.requireNonNull(is));

            name = props.getProperty("extension.name", "");
            version = Version.fromString(props.getProperty("extension.version", "0.0.0"));
        }
        catch (final IOException | NullPointerException e) {
            IcyLogger.error(this.getClass(), e, "Unable to read Properties extension file: " + path);
            return false;
        }

        return true;
    }
}
