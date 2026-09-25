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

package fr.icy.io.yaml;

import org.jspecify.annotations.NonNull;
import org.yaml.snakeyaml.Yaml;

import java.io.*;

/**
 * WIP
 */
public class YAMLFile implements Closeable {
    private final Yaml yaml;

    public YAMLFile() {
        yaml = new Yaml();
    }

    public YAMLFile(final @NonNull String yamlFile) {
        this();

        yaml.load(yamlFile);
    }

    public YAMLFile(final @NonNull InputStream inputStream) {
        this();

        yaml.load(inputStream);
    }

    public YAMLFile(final @NonNull File file) throws FileNotFoundException {
        this();

        yaml.load(new FileInputStream(file));
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {

    }
}
