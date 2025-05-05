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

package org.bioimageanalysis.icy.io.jar;

import org.bioimageanalysis.icy.network.NetworkUtil;
import org.bioimageanalysis.icy.network.URLUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * JAR utilities class
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class JarUtil {
    public static final String FILE_EXTENSION = "jar";
    public static final String FILE_DOT_EXTENSION = "." + FILE_EXTENSION;

    /**
     * Return true if specified URL is a JAR url
     */
    public static boolean isJarURL(final String path) {
        return (path != null) && path.toUpperCase().startsWith("JAR:") && URLUtil.isURL(path.substring(4));
    }

    /**
     * Return a JAR URL from the specified path
     */
    public static URL getJarURL(final String path) {
        if (path == null)
            return null;

        if (path.toUpperCase().startsWith("JAR:"))
            return URLUtil.getURL(path.substring(4));

        return URLUtil.getURL("jar:" + URLUtil.getURL(path) + "!/");
    }

    /**
     * Return a JAR URL from the specified JAR path and JAR entry
     */
    public static URL getJarURL(final String jarPath, final JarEntry entry) {
        return URLUtil.getURL(getJarURL(jarPath) + entry.getName());
    }

    /**
     * Return a JAR File from the specified path
     */
    public static JarFile getJarFile(final String path) {
        try {
            if (isJarURL(path))
                return ((JarURLConnection) NetworkUtil.openConnection(getJarURL(path), false, true)).getJarFile();

            return new JarFile(path);
        }
        catch (final IOException e) {
            IcyLogger.error(JarUtil.class, e, "Cannot open " + path + ".");
            return null;
        }
    }

    /**
     * Find a class entry in the specified JAR file
     */
    public static JarEntry getJarClassEntry(final JarFile file, final String className) {
        return file.getJarEntry(className.replace('.', '/') + ".class");
    }

    /**
     * Find the specified entry in the specified JAR file
     */
    public static JarEntry getJarEntry(final JarFile file, final String entryName) {
        return file.getJarEntry(entryName);
    }

    /**
     * Returns all files contained in the specified JAR file.
     *
     * @param includeFolderEntry
     *        if <code>true</code> all folder entry are also included
     * @param includeHidden
     *        if <code>true</code> all hidden files (starting by '.' character) are also included
     */
    public static void getAllFiles(final String fileName, final boolean includeFolderEntry, final boolean includeHidden, final List<String> result) {
        final JarFile jarFile = getJarFile(fileName);

        if (jarFile == null)
            return;

        final Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            final JarEntry jarEntry = entries.nextElement();

            if (jarEntry.isDirectory() && !includeFolderEntry)
                continue;

            final String name = jarEntry.getName();

            if (includeHidden || !name.startsWith("."))
                result.add(jarEntry.getName());
        }

        try {
            jarFile.close();
        }
        catch (final IOException e) {
            // ignore
        }
    }

    /**
     * Returns all files contained in the specified JAR file.
     *
     * @param includeFolderEntry
     *        if <code>true</code> all folder entry are also included
     * @param includeHidden
     *        if <code>true</code> all hidden files (starting by '.' character) are also included
     */
    public static List<String> getAllFiles(final String fileName, final boolean includeFolderEntry, final boolean includeHidden) {
        final List<String> result = new ArrayList<>();

        getAllFiles(fileName, includeFolderEntry, includeHidden, result);

        return result;
    }
}
