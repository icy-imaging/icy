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

import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExtensionConfig {
    private static final Yaml YAML = new Yaml();

    private final File configFile;
    private final Map<String, Object> config;

    ExtensionConfig(final @NotNull File configFile) throws IOException {
        this.configFile = configFile;
        if (!this.configFile.exists()) {
            if (this.configFile.createNewFile())
                config = new HashMap<>();
            else
                throw new IOException("Could not create config file: " + configFile.getAbsolutePath());
        }
        else
            config = YAML.load(new BufferedReader(new FileReader(configFile)));
    }

    public boolean save() {
        try {
            YAML.dump(config, new BufferedWriter(new FileWriter(configFile)));
        }
        catch (final IOException e) {
            IcyLogger.error(this.getClass(), e, "Could not save config file: " + configFile.getAbsolutePath());
            return false;
        }

        return true;
    }

    @Contract(pure = true)
    public @NotNull @Unmodifiable Map<String, Object> getConfig() {
        return Map.copyOf(config);
    }

    private @Nullable Object getValue(final @NotNull String key) {
        if (config.containsKey(key))
            return config.get(key);

        return null;
    }

    public @Nullable String getString(final @NotNull String key) {
        final Object value = getValue(key);
        if (value instanceof String)
            return (String) value;

        return null;
    }

    public @NotNull String getStringOrDefault(final @NotNull String key, final @NotNull String defaultValue) {
        final Object value = getValue(key);
        if (value instanceof String)
            return (String) value;

        return defaultValue;
    }

    public @Nullable Integer getInteger(final @NotNull String key) {
        final Object value = getValue(key);
        if (value instanceof Integer)
            return (Integer) value;

        return null;
    }

    public @NotNull Integer getIntegerOrDefault(final @NotNull String key, final @NotNull Integer defaultValue) {
        final Object value = getValue(key);
        if (value instanceof Integer)
            return (Integer) value;

        return defaultValue;
    }

    public @Nullable Float getFloat(final @NotNull String key) {
        final Object value = getValue(key);
        if (value instanceof Float)
            return (Float) value;

        return null;
    }

    public @NotNull Float getFloatOrDefault(final @NotNull String key, final @NotNull Float defaultValue) {
        final Object value = getValue(key);
        if (value instanceof Float)
            return (Float) value;

        return defaultValue;
    }

    public @Nullable Boolean getBoolean(final @NotNull String key) {
        final Object value = getValue(key);
        if (value instanceof Boolean)
            return (Boolean) value;

        return null;
    }

    public @NotNull Boolean getBooleanOrDefault(final @NotNull String key, final @NotNull Boolean defaultValue) {
        final Object value = getValue(key);
        if (value instanceof Boolean)
            return (Boolean) value;

        return defaultValue;
    }

    public @Nullable List<?> getList(final @NotNull String key) {
        final Object value = getValue(key);
        if (value instanceof List<?>)
            return (List<?>) value;

        return null;
    }

    public @NotNull List<?> getListOrDefault(final @NotNull String key, final @NotNull List<?> defaultValue) {
        final Object value = getValue(key);
        if (value instanceof List<?>)
            return (List<?>) value;

        return defaultValue;
    }

    public void setValue(final @NotNull String key, final @NotNull String value) {
        config.put(key, value);
    }

    public void setValue(final @NotNull String key, final @NotNull Integer value) {
        config.put(key, value);
    }

    public void setValue(final @NotNull String key, final @NotNull Float value) {
        config.put(key, value);
    }

    public void setValue(final @NotNull String key, final @NotNull Boolean value) {
        config.put(key, value);
    }

    public void setValue(final @NotNull String key, final @NotNull List<?> value) {
        config.put(key, value);
    }

    /*public static  @NotNull ExtensionConfig getConfig(final @NotNull ExtensionDescriptor descriptor) {
        return descriptor.getConfig();
    }*/

    /*public static  @NotNull ExtensionConfig getConfig(final @NotNull PluginDescriptor descriptor) {
        return descriptor.getExtension().getConfig();
    }*/
}
