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

import icy.plugin.classloader.exception.JclException;
import icy.plugin.classloader.exception.ResourceNotFoundException;
import icy.system.IcyExceptionHandler;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads the class bytes from jar files and other resources using
 * ClasspathResources
 *
 * @author Kamran Zafar
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class JarClassLoader extends AbstractClassLoader {
    /**
     * Class cache
     */
    protected final Map<String, Class<?>> loadedClasses;

    protected final ClasspathResources classpathResources;
    private char classNameReplacementChar;
    private final ProxyClassLoader localLoader = new LocalLoader();

    private static final Logger logger = Logger.getLogger(JarClassLoader.class.getName());

    public JarClassLoader(final ClassLoader parent) {
        super(parent);

        classpathResources = new ClasspathResources();
        loadedClasses = Collections.synchronizedMap(new HashMap<>());

        addLoader(localLoader);
    }

    public JarClassLoader() {
        this(getSystemClassLoader());
    }

    /**
     * Loads classes from different sources
     */
    public JarClassLoader(final Object[] sources) {
        this();

        addAll(sources);
    }

    /**
     * Loads classes from different sources
     */
    public JarClassLoader(final List<Object> sources) {
        this();

        addAll(sources);
    }

    /**
     * Add all jar/class sources
     */
    public void addAll(final Object[] sources) {
        for (final Object source : sources)
            add(source);
    }

    /**
     * Add all jar/class sources
     */
    public void addAll(final List<Object> sources) {
        for (final Object source : sources)
            add(source);
    }

    /**
     * Loads local/remote source
     */
    public void add(final Object source) {
        switch (source) {
            case final InputStream ignored -> throw new JclException("Unsupported resource type");
            case final URL url -> add(url);
            case final String s -> add(s);
            case null, default -> throw new JclException("Unknown Resource type");
        }

    }

    /**
     * Loads local/remote resource
     */
    public void add(final String resourceName) {
        classpathResources.loadResource(resourceName);
    }

    /**
     * Loads classes from InputStream.
     *
     * @deprecated Not anymore supported (we need URL for getResource(..) method)
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public void add(final InputStream jarStream) {
        // classpathResources.loadJar(jarStream);
    }

    /**
     * Loads local/remote resource
     */
    public void add(final URL url) {
        classpathResources.loadResource(url);
    }

    /**
     * Release all loaded resources and classes.
     * The ClassLoader cannot be used anymore to load any new resource.
     */
    public void unloadAll() {
        // unload resources
        classpathResources.entryContents.clear();
        // unload classes
        loadedClasses.clear();
    }

    /**
     * Reads the class bytes from different local and remote resources using
     * ClasspathResources
     */
    protected byte[] getClassBytes(final String className) throws IOException {
        return classpathResources.getResourceContent(formatClassName(className));
    }

    /**
     * Attempts to unload class, it only unloads the locally loaded classes by
     * JCL
     */
    public void unloadClass(final String className) {
        if (logger.isLoggable(Level.FINEST))
            logger.finest("Unloading class " + className);

        if (loadedClasses.containsKey(className)) {
            if (logger.isLoggable(Level.FINEST))
                logger.finest("Removing loaded class " + className);
            loadedClasses.remove(className);
            try {
                classpathResources.unload(formatClassName(className));
            }
            catch (final ResourceNotFoundException e) {
                throw new JclException("Something is very wrong!!!"
                        + "The locally loaded classes must be in synch with ClasspathResources", e);
            }
        }
        else {
            try {
                classpathResources.unload(formatClassName(className));
            }
            catch (final ResourceNotFoundException e) {
                throw new JclException("Class could not be unloaded "
                        + "[Possible reason: Class belongs to the system]", e);
            }
        }
    }

    protected String formatClassName(@NotNull final String className) {
        String cname = className.replace('/', '~');

        if (classNameReplacementChar == '\u0000')
            // '/' is used to map the package to the path
            cname = cname.replace('.', '/') + ".class";
        else
            // Replace '.' with custom char, such as '_'
            cname = cname.replace('.', classNameReplacementChar) + ".class";

        return cname.replace('~', '/');
    }

    /**
     * Local class loader
     */
    class LocalLoader extends ProxyClassLoader {
        private final Logger logger = Logger.getLogger(LocalLoader.class.getName());

        public LocalLoader() {
            super(50);

            enabled = Configuration.isLocalLoaderEnabled();
        }

        @Override
        public ClassLoader getLoader() {
            return JarClassLoader.this;
        }

        @Override
        public Class<?> loadClass(final String className, final boolean resolveIt) throws ClassNotFoundException, ClassFormatError {
            Class<?> result;
            final byte[] classBytes;

            result = loadedClasses.get(className);
            if (result != null) {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning local loaded class [" + className + "] from cache");
                return result;
            }

            // try to find from already loaded class (by other method)
            result = findLoadedClass(className);
            // not loaded ?
            if (result == null) {
                try {
                    classBytes = getClassBytes(className);
                }
                catch (final IOException e) {
                    // we got a severe error here --> throw an exception
                    throw new ClassNotFoundException(className, e);
                }

                if (classBytes == null)
                    return null;

                result = defineClass(className, classBytes, 0, classBytes.length);

                if (result == null)
                    return null;
            }

            /*
             * Preserve package name.
             */
            if (result.getPackage() == null) {
                final int lastDotIndex = className.lastIndexOf('.');
                final String packageName = (lastDotIndex >= 0) ? className.substring(0, lastDotIndex) : "";
                definePackage(packageName, null, null, null, null, null, null, null);
            }

            if (resolveIt)
                resolveClass(result);

            loadedClasses.put(className, result);
            if (logger.isLoggable(Level.FINEST))
                logger.finest("Return new local loaded class " + className);

            return result;
        }

        @Override
        public InputStream getResourceAsStream(final String name) {
            try {
                final byte[] arr = classpathResources.getResourceContent(name);

                if (arr != null) {
                    if (logger.isLoggable(Level.FINEST))
                        logger.finest("Returning newly loaded resource " + name);

                    return new ByteArrayInputStream(arr);
                }
            }
            catch (final IOException e) {
                IcyExceptionHandler.showErrorMessage(e, false, true);
            }

            return null;
        }

        @Override
        public URL getResource(final String name) {
            final URL url = classpathResources.getResource(name);

            if (url != null) {
                if (logger.isLoggable(Level.FINEST))
                    logger.finest("Returning newly loaded resource " + name);

                return url;
            }

            return null;
        }

        @Override
        public Enumeration<URL> getResources(final String name) {
            final URL url = getResource(name);

            return new Enumeration<>() {
                boolean hasMore = (url != null);

                @Override
                public boolean hasMoreElements() {
                    return hasMore;
                }

                @Override
                public URL nextElement() {
                    if (hasMore) {
                        hasMore = false;
                        return url;
                    }

                    return null;
                }
            };
        }
    }

    public char getClassNameReplacementChar() {
        return classNameReplacementChar;
    }

    public void setClassNameReplacementChar(final char classNameReplacementChar) {
        this.classNameReplacementChar = classNameReplacementChar;
    }

    /**
     * Returns an immutable Set of all resources name
     */
    public Set<String> getResourcesName() {
        return classpathResources.getResourcesName();
    }

    /**
     * Returns an immutable Map of all resources
     */
    public Map<String, URL> getResources() {
        return classpathResources.getResources();
    }

    /**
     * Returns all currently loaded classes and resources.
     */
    public Map<String, byte[]> getLoadedResources() {
        return classpathResources.getLoadedResources();
    }

    /**
     * @return Local JCL ProxyClassLoader
     */
    public ProxyClassLoader getLocalLoader() {
        return localLoader;
    }

    /**
     * Returns all JCL-loaded classes as an immutable Map
     *
     * @return Map
     */
    public Map<String, Class<?>> getLoadedClasses() {
        return Collections.unmodifiableMap(loadedClasses);
    }
}
