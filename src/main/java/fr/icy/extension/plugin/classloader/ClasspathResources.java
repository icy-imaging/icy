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

package fr.icy.extension.plugin.classloader;

import fr.icy.extension.plugin.classloader.exception.JclException;
import fr.icy.extension.plugin.classloader.exception.ResourceNotFoundException;
import fr.icy.io.FileUtil;
import fr.icy.network.NetworkUtil;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that builds a local classpath by loading resources from different
 * files/paths
 *
 * @author Kamran Zafar
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class ClasspathResources extends JarResources {
    private static final Logger LOGGER = Logger.getLogger(ClasspathResources.class.getName());
    private boolean ignoreMissingResources;

    public ClasspathResources() {
        super();
        ignoreMissingResources = Configuration.suppressMissingResourceException();
    }

    /**
     * Attempts to load a remote resource (jars, properties files, etc)
     */
    protected void loadRemoteResource(final @NonNull URL url) {
        if (LOGGER.isLoggable(Level.FINEST))
            LOGGER.finest("Attempting to load a remote resource.");

        if (url.toString().toLowerCase().endsWith(".jar")) {
            try {
                loadJar(url);
            }
            catch (final IOException e) {
                if (LOGGER.isLoggable(Level.SEVERE))
                    LOGGER.log(Level.SEVERE, "JarResources.loadJar(" + url + ") error.", e);
            }
            return;
        }

        if (entryUrls.containsKey(url.toString())) {
            if (!collisionAllowed)
                throw new JclException("Resource " + url + " already loaded");

            if (LOGGER.isLoggable(Level.FINEST))
                LOGGER.finest("Resource " + url + " already loaded; ignoring entry…");
            return;
        }

        if (LOGGER.isLoggable(Level.FINEST))
            LOGGER.finest("Loading remote resource.");

        entryUrls.put(url.toString(), url);
    }

    /**
     * Loads and returns content the remote resource (jars, properties files, etc)
     */
    protected byte[] loadRemoteResourceContent(final @NonNull URL url) throws IOException {
        final byte[] result = NetworkUtil.download(url.openStream());

        if (result != null)
            loadedSize += result.length;

        return result;
    }

    /**
     * Reads local and remote resources
     */
    protected void loadResource(final @NonNull URL url) {
        try {
            final File file = new File(url.toURI());
            // Is Local
            loadResource(file, FileUtil.getGenericPath(file.getAbsolutePath()));
        }
        catch (final IllegalArgumentException iae) {
            // Is Remote
            loadRemoteResource(url);
        }
        catch (final URISyntaxException e) {
            throw new JclException("URISyntaxException", e);
        }
    }

    /**
     * Reads local resources from - Jar files - Class folders - Jar Library
     * folders
     */
    protected void loadResource(final String path) {
        if (LOGGER.isLoggable(Level.FINEST))
            LOGGER.finest("Resource: " + path);

        final File fp = new File(path);

        if (!fp.exists() && !ignoreMissingResources)
            throw new JclException("File/Path does not exist");

        loadResource(fp, FileUtil.getGenericPath(path));
    }

    /**
     * Reads local resources from - Jar files - Class folders - Jar Library
     * folders
     */
    protected void loadResource(final @NonNull File fol, final String packName) {
        // FILE
        if (fol.isFile()) {
            if (fol.getName().toLowerCase().endsWith(".jar")) {
                try {
                    loadJar(fol.toURI().toURL());
                }
                catch (final IOException e) {
                    if (LOGGER.isLoggable(Level.SEVERE))
                        LOGGER.log(Level.SEVERE, "JarResources.loadJar(" + fol.getAbsolutePath() + ") error.", e);
                }
            }
            else
                loadResourceInternal(fol, packName);
        }
        // DIRECTORY
        else {
            final String[] folList = fol.list();
            if (folList != null) {
                for (final String f : folList) {
                    final File fl = new File(fol.getAbsolutePath() + "/" + f);

                    String pn = packName;

                    if (fl.isDirectory()) {

                        if (!pn.isEmpty())
                            pn = pn + "/";

                        pn = pn + fl.getName();
                    }

                    loadResource(fl, pn);
                }
            }
        }
    }

    /**
     * Loads the local resource.
     */
    protected void loadResourceInternal(final File file, final @NonNull String pack) {
        String entryName = "";

        if (!pack.isEmpty())
            entryName = pack + "/";
        entryName += file.getName();

        if (entryUrls.containsKey(entryName)) {
            if (!collisionAllowed)
                throw new JclException("Resource " + entryName + " already loaded");

            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning("Resource " + entryName + " already loaded; ignoring entry…");
            return;
        }

        if (LOGGER.isLoggable(Level.FINEST))
            LOGGER.finest("Loading resource: " + entryName);

        try {
            entryUrls.put(entryName, file.toURI().toURL());
        }
        catch (final Exception e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, "Error while loading: " + entryName + ".", e);
        }
    }

    @Override
    protected void loadContent(final String name, final @NonNull URL url) throws IOException {
        // JAR protocol
        if (url.getProtocol().equalsIgnoreCase(("jar")))
            super.loadContent(name, url);
            // FILE protocol
        else if (url.getProtocol().equalsIgnoreCase(("file"))) {
            final byte[] content = loadResourceContent(url);
            setResourceContent(name, content);
        }
        // try remote loading
        else {
            final byte[] content = loadRemoteResourceContent(url);
            setResourceContent(name, content);
        }
    }

    /**
     * Loads and returns the local resource content.
     */
    protected byte[] loadResourceContent(final @NonNull URL url) throws IOException {
        final byte[] result = NetworkUtil.download(url.openStream());

        if (result != null)
            loadedSize += result.length;

        return result;
    }

    /**
     * Removes the loaded resource
     */
    public void unload(final String resource) {
        if (entryContents.containsKey(resource)) {
            if (LOGGER.isLoggable(Level.FINEST))
                LOGGER.finest("Removing resource " + resource);
            entryContents.remove(resource);
        }
        else
            throw new ResourceNotFoundException(resource, "Resource not found in local ClasspathResources");
    }

    public boolean isCollisionAllowed() {
        return collisionAllowed;
    }

    public void setCollisionAllowed(final boolean collisionAllowed) {
        this.collisionAllowed = collisionAllowed;
    }

    public boolean isIgnoreMissingResources() {
        return ignoreMissingResources;
    }

    public void setIgnoreMissingResources(final boolean ignoreMissingResources) {
        this.ignoreMissingResources = ignoreMissingResources;
    }
}
