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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ExtensionConfig {
    private static final Logger LOGGER = Logger.getLogger(ExtensionConfig.class.getName());

    private static final Yaml YAML = new Yaml();

    private final File configFile;
    private final Map<String, Object> config;

    ExtensionConfig(final @NonNull File configFile) throws IOException {
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
            LOGGER.log(Level.SEVERE, "Could not save config file: " + configFile.getAbsolutePath() + ".", e);
            return false;
        }

        return true;
    }

    @Contract(pure = true)
    public @NonNull @Unmodifiable Map<String, Object> getConfig() {
        return Map.copyOf(config);
    }

    private @Nullable Object getValue(final @NonNull String key) {
        if (config.containsKey(key))
            return config.get(key);

        return null;
    }

    public @Nullable String getString(final @NonNull String key) {
        final Object value = getValue(key);
        if (value instanceof String)
            return (String) value;

        return null;
    }

    public @NonNull String getStringOrDefault(final @NonNull String key, final @NonNull String defaultValue) {
        final Object value = getValue(key);
        if (value instanceof String)
            return (String) value;

        return defaultValue;
    }

    public @Nullable Integer getInteger(final @NonNull String key) {
        final Object value = getValue(key);
        if (value instanceof Integer)
            return (Integer) value;

        return null;
    }

    public @NonNull Integer getIntegerOrDefault(final @NonNull String key, final @NonNull Integer defaultValue) {
        final Object value = getValue(key);
        if (value instanceof Integer)
            return (Integer) value;

        return defaultValue;
    }

    public @Nullable Float getFloat(final @NonNull String key) {
        final Object value = getValue(key);
        if (value instanceof Float)
            return (Float) value;

        return null;
    }

    public @NonNull Float getFloatOrDefault(final @NonNull String key, final @NonNull Float defaultValue) {
        final Object value = getValue(key);
        if (value instanceof Float)
            return (Float) value;

        return defaultValue;
    }

    public @Nullable Boolean getBoolean(final @NonNull String key) {
        final Object value = getValue(key);
        if (value instanceof Boolean)
            return (Boolean) value;

        return null;
    }

    public @NonNull Boolean getBooleanOrDefault(final @NonNull String key, final @NonNull Boolean defaultValue) {
        final Object value = getValue(key);
        if (value instanceof Boolean)
            return (Boolean) value;

        return defaultValue;
    }

    public @Nullable List<?> getList(final @NonNull String key) {
        final Object value = getValue(key);
        if (value instanceof List<?>)
            return (List<?>) value;

        return null;
    }

    public @NonNull List<?> getListOrDefault(final @NonNull String key, final @NonNull List<?> defaultValue) {
        final Object value = getValue(key);
        if (value instanceof List<?>)
            return (List<?>) value;

        return defaultValue;
    }

    public void setValue(final @NonNull String key, final @NonNull String value) {
        config.put(key, value);
    }

    public void setValue(final @NonNull String key, final @NonNull Integer value) {
        config.put(key, value);
    }

    public void setValue(final @NonNull String key, final @NonNull Float value) {
        config.put(key, value);
    }

    public void setValue(final @NonNull String key, final @NonNull Boolean value) {
        config.put(key, value);
    }

    public void setValue(final @NonNull String key, final @NonNull List<?> value) {
        config.put(key, value);
    }

    /*public static @NonNull ExtensionConfig getConfig(final @NonNull ExtensionDescriptor descriptor) {
        return descriptor.getConfig();
    }*/

    /*public static @NonNull ExtensionConfig getConfig(final @NonNull PluginDescriptor descriptor) {
        return descriptor.getExtension().getConfig();
    }*/
}
