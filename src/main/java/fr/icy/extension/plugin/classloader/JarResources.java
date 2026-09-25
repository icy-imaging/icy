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

package fr.icy.extension.plugin.classloader;

import fr.icy.extension.plugin.classloader.exception.JclException;
import fr.icy.network.NetworkUtil;
import fr.icy.network.URLUtil;
import org.jspecify.annotations.NonNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * JarResources reads jar files and loads the class content/bytes in a HashMap
 *
 * @author Kamran Zafar
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class JarResources {
    private static final Logger LOGGER = Logger.getLogger(JarResources.class.getName());

    // <resourceName, content> map
    protected Map<String, byte[]> entryContents;
    // <resourceName, fileName> map
    protected Map<String, URL> entryUrls;

    protected boolean collisionAllowed;
    // keep trace of loaded resource size
    protected int loadedSize;

    /**
     * Default constructor
     */
    public JarResources() {
        entryContents = new HashMap<>();
        entryUrls = new HashMap<>();
        collisionAllowed = Configuration.suppressCollisionException();
        loadedSize = 0;
    }

    public URL getResource(final String name) {
        return entryUrls.get(name);
    }

    public byte[] getResourceContent(final String name) throws IOException {
        byte[] content = entryContents.get(name);

        // we load the content
        if (content == null) {
            final URL url = entryUrls.get(name);

            if (url != null) {
                // load content and return it
                loadContent(name, url);
                content = entryContents.get(name);

                // try
                // {
                // // load content and return it
                // loadContent(name, url);
                // content = entryContents.get(name);
                // }
                // catch (IOException e)
                // {
                // // content cannot be loaded, remove the URL entry
                // // better to not remove it after all, so we know why it failed next time
                // // entryUrls.remove(name);
                //
                // // and throw exception
                // throw e;
                // }
            }
        }

        return content;
    }

    protected void loadContent(final String name, final URL url) throws IOException {
        // only support JAR resource here
        final byte[] content = loadJarContent(url);
        setResourceContent(name, content);
    }

    /**
     * Returns an immutable Set of all resources names
     */
    public Set<String> getResourcesName() {
        return Collections.unmodifiableSet(entryUrls.keySet());
    }

    /**
     * Returns an immutable Map of all resources
     */
    public Map<String, URL> getResources() {
        return Collections.unmodifiableMap(entryUrls);
    }

    /**
     * Returns an immutable Map of all loaded jar resources
     */
    public Map<String, byte[]> getLoadedResources() {
        return Collections.unmodifiableMap(entryContents);
    }

    /**
     * Loads the jar file from a specified File.<br>
     * This method actually stores all contained URL in the specified JAR file.
     */
    public void loadJar(final @NonNull File file) throws IOException {
        final String filePath = file.getAbsolutePath();
        final String urlPrefix = "jar:" + file.toURI() + "!/";

        if (LOGGER.isLoggable(Level.FINEST))
            LOGGER.finest("Loading jar: " + filePath);

        // we don't care about JAR specific information so just use ZipFile here
        final ZipFile zipFile = new ZipFile(file);

        try {
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();

                // ignore folder entries
                if (entry.isDirectory())
                    continue;

                final String name = entry.getName();

                // ignore manifest file
                if (name.equals("META-INF/MANIFEST.MF"))
                    continue;

                if (entryUrls.containsKey(name)) {
                    if (!collisionAllowed)
                        throw new JclException("Class/Resource " + name + " already loaded");

                    if (LOGGER.isLoggable(Level.FINEST))
                        LOGGER.finest("Class/Resource " + name + " already loaded; ignoring entry…");
                    continue;
                }

                // add to internal resource HashMap
                try {
                    // Recreate an URI with the resource : jar:file:/path/to/jar!/path/to/resource
                    final URI uri = new URI("jar:file", null, file.toURI().getPath() + "!/" + name, null, null);
                    entryUrls.put(name, uri.toURL());
                }
                catch (final URISyntaxException e) {
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(Level.WARNING, "Cannot load resource with URI: " + name + ". Trying with URL…", e);
                    // Trying with direct URL (DEPRECATED SINCE JDK 20)
                    final URL url = new URL(urlPrefix + name);
                    entryUrls.put(name, url);
                }
            }
        }
        /*catch (final URISyntaxException e) {
            IcyExceptionHandler.showErrorMessage(e, false);
        }*/
        finally {
            try {
                zipFile.close();
            }
            catch (final IOException e) {
                // not important
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING, "JarResources.loadJar(" + filePath + ") error.", e);
            }
        }

    }

    /**
     * Loads the jar file from a specified URL.<br>
     * This method actually stores all contained URL in the specified JAR archive.
     */
    public void loadJar(final URL url) throws IOException {
        // use file loading if possible
        if (URLUtil.isFileURL(url)) {
            File f;

            try {
                f = new File(url.toURI());
            }
            catch (final URISyntaxException e) {
                f = new File(url.getPath());
            }

            loadJar(f);
            return;
        }

        if (LOGGER.isLoggable(Level.FINEST))
            LOGGER.finest("Loading jar: " + url.toString());

        final String urlPrefix = "jar:" + url.toString() + "!/";
        BufferedInputStream bis = null;
        // we don't care about JAR specific information so just use ZipInputStream here
        ZipInputStream zis = null;

        try {
            final InputStream in = url.openStream();

            if (in instanceof BufferedInputStream)
                bis = (BufferedInputStream) in;
            else
                bis = new BufferedInputStream(in);

            zis = new ZipInputStream(bis);

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (LOGGER.isLoggable(Level.FINEST))
                    LOGGER.finest(dump(entry));

                if (entry.isDirectory())
                    continue;

                final String name = entry.getName();

                // ignore manifest file
                if (name.equals("META-INF/MANIFEST.MF"))
                    continue;

                if (entryUrls.containsKey(name)) {
                    if (!collisionAllowed)
                        throw new JclException("Class/Resource " + name + " already loaded");

                    if (LOGGER.isLoggable(Level.FINEST))
                        LOGGER.finest("Class/Resource " + name + " already loaded; ignoring entry…");
                    continue;
                }

                // add to internal resource HashMap
                //entryUrls.put(name, new URL(urlPrefix + name));
                entryUrls.put(name, new URI(urlPrefix + name).toURL());
            }
        }
        // catch (NullPointerException e)
        // {
        // if (logger.isLoggable(Level.FINEST))
        // logger.finest("Done loading.");
        // }
        catch (final URISyntaxException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
        finally {
            if (zis != null) {
                try {
                    zis.close();
                }
                catch (final IOException e) {
                    // not important
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(Level.WARNING, "JarResources.loadJar(" + url + ") error.", e);
                }
            }

            if (bis != null) {
                try {
                    bis.close();
                }
                catch (final IOException e) {
                    // not important
                    if (LOGGER.isLoggable(Level.WARNING))
                        LOGGER.log(Level.WARNING, "JarResources.loadJar(" + url + ") error.", e);
                }
            }
        }
    }

    /**
     * Load the jar contents from InputStream
     */
    protected byte[] loadJarContent(final @NonNull URL url) throws IOException {
        final JarURLConnection uc = (JarURLConnection) url.openConnection();
        final JarEntry jarEntry = uc.getJarEntry();

        if (jarEntry != null) {
            if (LOGGER.isLoggable(Level.FINEST))
                LOGGER.finest(dump(jarEntry));

            return NetworkUtil.download(uc.getInputStream(), jarEntry.getSize(), null);
        }

        throw new IOException("JarResources.loadJarContent(" + url + ") error:\nEntry not found !");
    }

    protected void setResourceContent(final String name, final byte[] content) {
        if (entryContents.containsKey(name)) {
            if (!collisionAllowed)
                throw new JclException("Class/Resource " + name + " already loaded");

            if (LOGGER.isLoggable(Level.FINEST))
                LOGGER.finest("Class/Resource " + name + " already loaded; ignoring entry…");
            return;
        }

        if (LOGGER.isLoggable(Level.FINEST))
            LOGGER.finest("Entry Name: " + name + ", " + "Entry Size: " + content.length);

        // add to internal resource HashMap
        entryContents.put(name, content);
    }

    /**
     * For debugging
     */
    private @NonNull String dump(final @NonNull ZipEntry ze) {
        final StringBuilder sb = new StringBuilder();
        if (ze.isDirectory())
            sb.append("d ");
        else
            sb.append("f ");

        if (ze.getMethod() == ZipEntry.STORED)
            sb.append("stored   ");
        else
            sb.append("deflated ");

        sb.append(ze.getName());
        sb.append("\t");
        sb.append(ze.getSize());
        if (ze.getMethod() == ZipEntry.DEFLATED)
            sb.append("/").append(ze.getCompressedSize());

        return (sb.toString());
    }
}
