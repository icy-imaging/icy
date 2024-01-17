/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

package icy.plugin.classloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * @author Kamran Zafar
 * @author Thomas Musset
 */
public abstract class ProxyClassLoader implements Comparable<ProxyClassLoader> {
    protected int order;
    protected boolean enabled;

    public ProxyClassLoader(final int order) {
        super();

        // Default order
        this.order = order;
        // Enabled by default
        enabled = true;
    }

    public int getOrder() {
        return order;
    }

    /**
     * Returns the internal {@link ClassLoader} object used to load class.
     */
    public abstract ClassLoader getLoader();

    /**
     * Loads the class and returns it.
     */
    public abstract Class<?> loadClass(String className, boolean resolveIt) throws ClassNotFoundException;

    /**
     * Loads the resource and returns an input stream for reading it.
     */
    public abstract InputStream getResourceAsStream(String name);

    /**
     * Find the resource and returns it as a URL (if it exists).
     */
    public abstract URL getResource(String name);

    /**
     * Finds all resources with the given name and returns them as a URL Enumeration.
     */
    public abstract Enumeration<URL> getResources(String name) throws IOException;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int compareTo(final ProxyClassLoader o) {
        return order - o.getOrder();
    }
}
