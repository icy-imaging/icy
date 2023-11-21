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

import icy.file.FileUtil;
import icy.network.NetworkUtil;
import icy.plugin.classloader.exception.JclException;
import icy.plugin.classloader.exception.ResourceNotFoundException;
import icy.system.IcyExceptionHandler;
import org.jetbrains.annotations.NotNull;

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
 * @author Stephane Dallongeville
 * @author Thomas MUSSET
 */
public class ClasspathResources extends JarResources {
    private static final Logger logger = Logger.getLogger(ClasspathResources.class.getName());
    private boolean ignoreMissingResources;

    public ClasspathResources() {
        super();
        ignoreMissingResources = Configuration.suppressMissingResourceException();
    }

    /**
     * Attempts to load a remote resource (jars, properties files, etc)
     */
    protected void loadRemoteResource(final URL url) {
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Attempting to load a remote resource.");

        if (url.toString().toLowerCase().endsWith(".jar")) {
            try {
                loadJar(url);
            }
            catch (final IOException e) {
                System.err.println("JarResources.loadJar(" + url + ") error:");
                IcyExceptionHandler.showErrorMessage(e, false, true);
            }
            return;
        }

        if (entryUrls.containsKey(url.toString())) {
            if (!collisionAllowed)
                throw new JclException("Resource " + url + " already loaded");

            if (logger.isLoggable(Level.FINEST))
                logger.finest("Resource " + url + " already loaded; ignoring entry...");
            return;
        }

        if (logger.isLoggable(Level.FINEST))
            logger.finest("Loading remote resource.");

        entryUrls.put(url.toString(), url);
    }

    /**
     * Loads and returns content the remote resource (jars, properties files, etc)
     */
    protected byte[] loadRemoteResourceContent(@NotNull final URL url) throws IOException {
        final byte[] result = NetworkUtil.download(url.openStream());

        if (result != null)
            loadedSize += result.length;

        return result;
    }

    /**
     * Reads local and remote resources
     */
    protected void loadResource(@NotNull final URL url) {
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
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Resource: " + path);

        final File fp = new File(path);

        if (!fp.exists() && !ignoreMissingResources)
            throw new JclException("File/Path does not exist");

        loadResource(fp, FileUtil.getGenericPath(path));
    }

    /**
     * Reads local resources from - Jar files - Class folders - Jar Library
     * folders
     */
    protected void loadResource(@NotNull final File fol, final String packName) {
        // FILE
        if (fol.isFile()) {
            if (fol.getName().toLowerCase().endsWith(".jar")) {
                try {
                    loadJar(fol.toURI().toURL());
                }
                catch (final IOException e) {
                    System.err.println("JarResources.loadJar(" + fol.getAbsolutePath() + ") error:");
                    IcyExceptionHandler.showErrorMessage(e, false, true);
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

                        if (!pn.equals(""))
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
    protected void loadResourceInternal(final File file, @NotNull final String pack) {
        String entryName = "";

        if (pack.length() > 0)
            entryName = pack + "/";
        entryName += file.getName();

        if (entryUrls.containsKey(entryName)) {
            if (!collisionAllowed)
                throw new JclException("Resource " + entryName + " already loaded");

            if (logger.isLoggable(Level.WARNING))
                logger.finest("Resource " + entryName + " already loaded; ignoring entry...");
            return;
        }

        if (logger.isLoggable(Level.FINEST))
            logger.finest("Loading resource: " + entryName);

        try {
            entryUrls.put(entryName, file.toURI().toURL());
        }
        catch (final Exception e) {
            if (logger.isLoggable(Level.SEVERE))
                logger.finest("Error while loading: " + entryName);

            System.err.println("JarResources.loadResourceInternal(" + file.getAbsolutePath() + ") error:");
            IcyExceptionHandler.showErrorMessage(e, false, true);
        }
    }

    @Override
    protected void loadContent(final String name, @NotNull final URL url) throws IOException {
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
    protected byte[] loadResourceContent(@NotNull final URL url) throws IOException {
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
            if (logger.isLoggable(Level.FINEST))
                logger.finest("Removing resource " + resource);
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
