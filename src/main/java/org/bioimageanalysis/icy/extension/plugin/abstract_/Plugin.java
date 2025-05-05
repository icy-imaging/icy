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

package org.bioimageanalysis.icy.extension.plugin.abstract_;

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.common.reflect.ClassUtil;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.PluginLauncher;
import org.bioimageanalysis.icy.extension.plugin.PluginLoader;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginThreaded;
import org.bioimageanalysis.icy.gui.frame.IcyFrame;
import org.bioimageanalysis.icy.gui.viewer.Viewer;
import org.bioimageanalysis.icy.io.FileUtil;
import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.bioimageanalysis.icy.model.image.ImageUtil;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.network.NetworkUtil;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.audit.Audit;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.preferences.PluginsPreferences;
import org.bioimageanalysis.icy.system.preferences.XMLPreferences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

/**
 * Base class for Plugin, provide some helper methods.<br>
 * By default the constructor of a Plugin class is called in the EDT (Event Dispatch Thread).<br>
 * If the plugin implements the {@link PluginThreaded} there is no more guarantee that is the case.
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public abstract class Plugin implements AutoCloseable {
    public static @Nullable Plugin getPlugin(final @NotNull List<Plugin> list, final String className) {
        for (final Plugin plugin : list)
            if (plugin.getClass().getName().equals(className))
                return plugin;

        return null;
    }

    private PluginDescriptor descriptor;

    /**
     * Default Plugin constructor.<br>
     * The {@link PluginLauncher} is normally responsible of Plugin class instantiation.
     */
    public Plugin() {
        super();

        // get descriptor from loader
        descriptor = PluginLoader.getPlugin(getClass().getName());

        if (descriptor == null) {
            // descriptor not found (don't check for anonymous plugin class) ?
            if (!getClass().isAnonymousClass()) {
                final String[] messages = new String[]{
                        "Plugin '" + getClass().getName() + "' started but not found in PluginLoader !",
                        "Local XML plugin description file is probably incorrect."
                };
                IcyLogger.warn(Plugin.class, messages);
            }

            // create dummy descriptor
            descriptor = new PluginDescriptor(this.getClass());
            descriptor.setName(getClass().getSimpleName());
        }

        // audit
        Audit.pluginInstanced(this);
    }

    @Override
    public void close() throws Exception {
        // unregister plugin (weak reference so we can do it here)
        Icy.getMainInterface().unRegisterPlugin(this);
    }

    /**
     * @return the descriptor
     */
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * @return the plugin name (from its descriptor)
     */
    public String getName() {
        return descriptor.getName();
    }

    /**
     * @return the folder where the plugin is installed (or should be installed).
     */
    public String getInstallFolder() {
        return ClassUtil.getPathFromQualifiedName(ClassUtil.getPackageName(getClass().getName()));
    }

    /**
     * @return current active viewer
     */
    public Viewer getActiveViewer() {
        return Icy.getMainInterface().getActiveViewer();
    }

    /**
     * @return current active sequence
     */
    public Sequence getActiveSequence() {
        return Icy.getMainInterface().getActiveSequence();
    }

    /**
     * @return current active image
     */
    public IcyBufferedImage getActiveImage() {
        return Icy.getMainInterface().getActiveImage();
    }

    /**
     * Add a new frame to Icy desktop
     *
     * @param frame the frame to add
     */
    public void addIcyFrame(final @NotNull IcyFrame frame) {
        frame.addToDesktopPane();
    }

    /**
     * Display a new sequence
     *
     * @param sequence the sequence to dispay
     */
    public void addSequence(final Sequence sequence) {
        Icy.getMainInterface().addSequence(sequence);
    }

    /**
     * Close / hide a sequence
     *
     * @param sequence the sequence to close
     */
    public void removeSequence(final @NotNull Sequence sequence) {
        sequence.closeSequence();
    }

    /**
     * @return the list of all opened/dispayed Sequence
     */
    public ArrayList<Sequence> getSequences() {
        return Icy.getMainInterface().getSequences();
    }

    /**
     * @param name resource name
     * @return Return the resource URL from given resource name.<br>
     * Ex: <code>getResource("plugins/author/resources/def.xml");</code>
     */
    public URL getResource(final String name) {
        return getClass().getClassLoader().getResource(name);
    }

    /**
     * @param name resource name
     * @return Return resources corresponding to given resource name.<br>
     * Ex: <code>getResources("plugins/author/resources/def.xml");</code>
     * @throws IOException ioexception
     */
    public Enumeration<URL> getResources(final String name) throws IOException {
        return getClass().getClassLoader().getResources(name);
    }

    /**
     * @param name resource name
     * @return Return the resource as data stream from given resource name.<br>
     * Ex: <code>getResourceAsStream("plugins/author/resources/def.xml");</code>
     */
    public InputStream getResourceAsStream(final String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    /**
     * @param resourceName resource name
     * @return Return the image resource from given resource name
     * Ex: <code>getResourceAsStream("plugins/author/resources/image.png");</code>
     */
    public BufferedImage getImageResource(final String resourceName) {
        return ImageUtil.load(getResourceAsStream(resourceName));
    }

    /**
     * @param resourceName resource name
     * @return Return the icon resource from given resource name
     * Ex: <code>getResourceAsStream("plugins/author/resources/icon.png");</code>
     */
    public ImageIcon getIconResource(final String resourceName) {
        return new ImageIcon(Objects.requireNonNull(getImageResource(resourceName)));
    }

    /**
     * @return Retrieve the preferences root for this plugin.<br>
     */
    public XMLPreferences getPreferencesRoot() {
        return PluginsPreferences.root(this);
    }

    /**
     * @param name string
     * @return Retrieve the plugin preferences node for specified name.<br>
     * i.e : getPreferences("window") will return node
     * "plugins.[authorPackage].[pluginClass].window"
     */
    public XMLPreferences getPreferences(final String name) {
        return getPreferencesRoot().node(name);
    }

    /**
     * @return Returns the base resource path for plugin native libraries.<br>
     * Depending the Operating System it can returns these values:
     * <ul>
     * <li>lib/unix32</li>
     * <li>lib/unix64</li>
     * <li>lib/mac32</li>
     * <li>lib/mac64</li>
     * <li>lib/win32</li>
     * <li>lib/win64</li>
     * </ul>
     */
    protected static @NotNull String getResourceNativeLibraryPath() {
        return "lib" + FileUtil.separator + SystemUtil.getOSArchIdString();
    }

    /**
     * @param name  resource name
     * @param clazz class
     * @return Return the resource URL from given resource name and class instance.<br>
     * Ex: <code>getResource(Plugin.class, "plugins/author/resources/def.xml");</code>
     */
    public static URL getResource(final @NotNull Class<?> clazz, final String name) {
        return clazz.getClassLoader().getResource(name);
    }

    /**
     * Load a packed native library from the JAR file.<br>
     * Native libraries should be packaged with the following directory &amp; file structure:
     *
     * <pre>
     * /lib/unix32
     *   libxxx.so
     * /lib/unix64
     *   libxxx.so
     * /lib/mac32
     *   libxxx.dylib
     * /lib/mac64
     *   libxxx.dylib
     * /lib/win32
     *   xxx.dll
     * /lib/win64
     *   xxx.dll
     * /plugins/myname/mypackage
     *   MyPlugin.class
     *   ....
     * </pre>
     * <p>
     * Here "xxx" is the name of the native library.<br>
     * Current approach is to unpack the native library into a temporary file and load from there.
     *
     * @param libName string
     * @param clazz   class
     * @return true if the library was correctly loaded.
     * @see #prepareLibrary(String)
     */
    public static boolean loadLibrary(final Class<?> clazz, final String libName) {
        final File file = prepareLibrary(clazz, libName);

        if (file == null)
            return false;

        // and load it from absolute path
        System.load(file.getAbsolutePath());

        return true;
    }

    /**
     * Extract a packed native library from the JAR file to a temporary native library folder so it can be easily loaded
     * later.<br>
     * Native libraries should be packaged with the following directory &amp; file structure:
     *
     * <pre>
     * /lib/unix32
     *   libxxx.so
     * /lib/unix64
     *   libxxx.so
     * /lib/mac32
     *   libxxx.dylib
     * /lib/mac64
     *   libxxx.dylib
     * /lib/win32
     *   xxx.dll
     * /lib/win64
     *   xxx.dll
     * /plugins/myname/mypackage
     *   MyPlugin.class
     *   ....
     * </pre>
     * <p>
     * Here "xxx" is the name of the native library.<br>
     *
     * @param libName string
     * @param clazz   class
     * @return the extracted native library file.
     * @see #loadLibrary(String)
     */
    public static @Nullable File prepareLibrary(final Class<?> clazz, final String libName) {
        try {
            // get mapped library name
            String mappedlibName = System.mapLibraryName(libName);
            // get base resource path for native library
            final String basePath = getResourceNativeLibraryPath() + FileUtil.separator;

            // search for library in resource
            URL libUrl = getResource(clazz, basePath + mappedlibName);

            // not found ?
            if (libUrl == null) {
                // jnilib extension may not work, try with "dylib" extension instead
                if (mappedlibName.endsWith(".jnilib")) {
                    mappedlibName = mappedlibName.substring(0, mappedlibName.length() - 7) + ".dylib";
                    libUrl = getResource(clazz, basePath + mappedlibName);
                }
                // do the contrary in case we have an old "jnilib" file and system use "dylib" by default
                else if (mappedlibName.endsWith(".dylib")) {
                    mappedlibName = mappedlibName.substring(0, mappedlibName.length() - 6) + ".jnilib";
                    libUrl = getResource(clazz, basePath + mappedlibName);
                }
            }

            // resource not found --> error
            if (libUrl == null)
                throw new IOException("Couldn't find resource " + basePath + mappedlibName);

            // extract resource
            return extractResourceTo(SystemUtil.getTempLibraryDirectory() + FileUtil.separator + mappedlibName, libUrl);
        }
        catch (final IOException e) {
            IcyLogger.error(Plugin.class, e, "Error while extracting packed library " + libName);
        }

        return null;
    }

    /**
     * Extract a resource to the specified path
     *
     * @param outputPath the file to extract the resource to
     * @param resource   the resource URL
     * @return the extracted file
     * @throws IOException io exception
     */
    protected static @NotNull File extractResourceTo(final String outputPath, final @NotNull URL resource) throws IOException {
        // open resource stream
        final InputStream in = resource.openStream();
        // create output file
        final File result = new File(outputPath);
        final byte[] data;

        try {
            // load resource
            data = NetworkUtil.download(in);
        }
        finally {
            in.close();
        }

        // file already exist ??
        if (result.exists()) {
            // same size --> assume it's the same
            if (result.length() == data.length)
                return result;

            if (!FileUtil.delete(result, false))
                throw new IOException("Cannot overwrite " + result + " file !");
        }

        // save resource to file
        FileUtil.save(result, data, true);

        return result;
    }

    /**
     * Load a packed native library from the JAR file.<br>
     * Native libraries should be packaged with the following directory &amp; file structure:
     *
     * <pre>
     * /lib/unix32
     *   libxxx.so
     * /lib/unix64
     *   libxxx.so
     * /lib/mac32
     *   libxxx.dylib
     * /lib/mac64
     *   libxxx.dylib
     * /lib/win32
     *   xxx.dll
     * /lib/win64
     *   xxx.dll
     * /plugins/myname/mypackage
     *   MyPlugin.class
     *   ....
     * </pre>
     * <p>
     * Here "xxx" is the name of the native library.<br>
     * Current approach is to unpack the native library into a temporary file and load from there.
     *
     * @param libName string
     * @return true if the library was correctly loaded.
     * @see #prepareLibrary(String)
     */
    public boolean loadLibrary(final String libName) {
        return loadLibrary(getClass(), libName);
    }

    /**
     * Extract a packed native library from the JAR file to a temporary native library folder so it can be easily loaded
     * later.<br>
     * Native libraries should be packaged with the following directory &amp; file structure:
     *
     * <pre>
     * /lib/unix32
     *   libxxx.so
     * /lib/unix64
     *   libxxx.so
     * /lib/mac32
     *   libxxx.dylib
     * /lib/mac64
     *   libxxx.dylib
     * /lib/win32
     *   xxx.dll
     * /lib/win64
     *   xxx.dll
     * /plugins/myname/mypackage
     *   MyPlugin.class
     *   ....
     * </pre>
     * <p>
     * Here "xxx" is the name of the native library.<br>
     *
     * @param libName string
     * @return the extracted native library file.
     * @see #loadLibrary(String)
     */
    public File prepareLibrary(final String libName) {
        return prepareLibrary(getClass(), libName);
    }

    /**
     * @param errorLog Report an error log for this plugin (reported to Icy web site which report then to the
     *                 author of the plugin).
     * @see IcyExceptionHandler#report(PluginDescriptor, String)
     */
    public void report(final String errorLog) {
        IcyExceptionHandler.report(descriptor, errorLog);
    }

    @Override
    public String toString() {
        return getDescriptor().getName();
    }
}
