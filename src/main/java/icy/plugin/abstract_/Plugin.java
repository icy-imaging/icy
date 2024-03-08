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

package icy.plugin.abstract_;

import icy.file.FileUtil;
import icy.gui.frame.IcyFrame;
import icy.gui.viewer.Viewer;
import icy.image.IcyBufferedImage;
import icy.image.ImageUtil;
import icy.main.Icy;
import icy.network.NetworkUtil;
import icy.plugin.PluginDescriptor;
import icy.plugin.PluginLauncher;
import icy.plugin.PluginLoader;
import icy.plugin.interface_.PluginBundled;
import icy.plugin.interface_.PluginThreaded;
import icy.preferences.PluginsPreferences;
import icy.preferences.XMLPreferences;
import icy.resource.ResourceUtil;
import icy.sequence.Sequence;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.system.audit.Audit;
import icy.system.logging.IcyLogger;
import icy.util.ClassUtil;
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
    @Nullable
    public static Plugin getPlugin(@NotNull final List<Plugin> list, @Nullable final String className) {
        for (final Plugin plugin : list)
            if (plugin.getClass().getName().equals(className))
                return plugin;

        return null;
    }

    @NotNull
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

    @Deprecated(forRemoval = true)
    @Override
    protected void finalize() throws Throwable {
        // unregister plugin (weak reference so we can do it here)
        Icy.getMainInterface().unRegisterPlugin(this);

        super.finalize();
    }

    @Override
    public void close() throws Exception {
        // unregister plugin (weak reference so we can do it here)
        Icy.getMainInterface().unRegisterPlugin(this);
    }

    /**
     * @return the descriptor
     */
    @NotNull
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * @return the plugin name (from its descriptor)
     */
    @NotNull
    public String getName() {
        return descriptor.getName();
    }

    /**
     * @return <code>true</code> if this is a bundled plugin (see {@link PluginBundled}).
     */
    public boolean isBundled() {
        return this instanceof PluginBundled;
    }

    /**
     * @return the class name of the plugin owner.<br>
     * If this Plugin is not bundled (see {@link PluginBundled}) then it just returns the
     * current class name otherwise it will returns the plugin owner class name.
     */
    @NotNull
    public String getOwnerClassName() {
        if (isBundled())
            return ((PluginBundled) this).getMainPluginClassName();

        return getClass().getName();
    }

    /**
     * @return the folder where the plugin is installed (or should be installed).
     */
    @NotNull
    public String getInstallFolder() {
        return ClassUtil.getPathFromQualifiedName(ClassUtil.getPackageName(getClass().getName()));
    }

    /**
     * @return current active viewer
     */
    @Nullable
    public Viewer getActiveViewer() {
        return Icy.getMainInterface().getActiveViewer();
    }

    /**
     * @return current active sequence
     */
    @Nullable
    public Sequence getActiveSequence() {
        return Icy.getMainInterface().getActiveSequence();
    }

    /**
     * @return current active image
     */
    @Nullable
    public IcyBufferedImage getActiveImage() {
        return Icy.getMainInterface().getActiveImage();
    }

    /**
     * @return Viewer
     * @deprecated Use {@link #getActiveViewer()} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    @Nullable
    public Viewer getFocusedViewer() {
        return getActiveViewer();
    }

    /**
     * @return Sequence
     * @deprecated Use {@link #getActiveSequence()} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    @Nullable
    public Sequence getFocusedSequence() {
        return getActiveSequence();
    }

    /**
     * @return Buffered image
     * @deprecated Use {@link #getActiveImage()} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    @Nullable
    public IcyBufferedImage getFocusedImage() {
        return getActiveImage();
    }

    /**
     * Add a new frame to Icy desktop
     *
     * @param frame the frame to add
     */
    public void addIcyFrame(@NotNull final IcyFrame frame) {
        frame.addToDesktopPane();
    }

    /**
     * Display a new sequence
     *
     * @param sequence the sequence to dispay
     */
    public void addSequence(@Nullable final Sequence sequence) {
        Icy.getMainInterface().addSequence(sequence);
    }

    /**
     * Close / hide a sequence
     *
     * @param sequence the sequence to close
     */
    public void removeSequence(@NotNull final Sequence sequence) {
        sequence.closeSequence();
    }

    /**
     * @return the list of all opened/dispayed Sequence
     */
    @NotNull
    public ArrayList<Sequence> getSequences() {
        return Icy.getMainInterface().getSequences();
    }

    /**
     * @param name resource name
     * @return Return the resource URL from given resource name.<br>
     * Ex: <code>getResource("plugins/author/resources/def.xml");</code>
     */
    @Nullable
    public URL getResource(@NotNull final String name) {
        return getClass().getClassLoader().getResource(name);
    }

    /**
     * @param name resource name
     * @return Return resources corresponding to given resource name.<br>
     * Ex: <code>getResources("plugins/author/resources/def.xml");</code>
     * @throws IOException ioexception
     */
    @NotNull
    public Enumeration<URL> getResources(@NotNull final String name) throws IOException {
        return getClass().getClassLoader().getResources(name);
    }

    /**
     * @param name resource name
     * @return Return the resource as data stream from given resource name.<br>
     * Ex: <code>getResourceAsStream("plugins/author/resources/def.xml");</code>
     */
    @Nullable
    public InputStream getResourceAsStream(@NotNull final String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    /**
     * @param resourceName resource name
     * @return Return the image resource from given resource name
     * Ex: <code>getResourceAsStream("plugins/author/resources/image.png");</code>
     */
    @Nullable
    public BufferedImage getImageResource(@NotNull final String resourceName) {
        return ImageUtil.load(getResourceAsStream(resourceName));
    }

    /**
     * @param resourceName resource name
     * @return Return the icon resource from given resource name
     * Ex: <code>getResourceAsStream("plugins/author/resources/icon.png");</code>
     */
    @Nullable
    public ImageIcon getIconResource(@NotNull final String resourceName) {
        return new ImageIcon(Objects.requireNonNull(getImageResource(resourceName)));
    }

    /**
     * @return Retrieve the preferences root for this plugin.<br>
     */
    @NotNull
    public XMLPreferences getPreferencesRoot() {
        return PluginsPreferences.root(this);
    }

    /**
     * @param name string
     * @return Retrieve the plugin preferences node for specified name.<br>
     * i.e : getPreferences("window") will return node
     * "plugins.[authorPackage].[pluginClass].window"
     */
    @Nullable
    public XMLPreferences getPreferences(@NotNull final String name) {
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
    @NotNull
    protected static String getResourceNativeLibraryPath() {
        return "lib" + FileUtil.separator + SystemUtil.getOSArchIdString();
    }

    /**
     * @param name  resource name
     * @param clazz class
     * @return Return the resource URL from given resource name and class instance.<br>
     * Ex: <code>getResource(Plugin.class, "plugins/author/resources/def.xml");</code>
     */
    @Nullable
    public static URL getResource(@NotNull final Class<?> clazz, @NotNull final String name) {
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
    public static boolean loadLibrary(@NotNull final Class<?> clazz, @NotNull final String libName) {
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
    @Nullable
    public static File prepareLibrary(@NotNull final Class<?> clazz, @NotNull final String libName) {
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
    @NotNull
    protected static File extractResourceTo(@NotNull final String outputPath, @NotNull final URL resource) throws IOException {
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
     * @return string
     * @deprecated Use {@link #getResourceNativeLibraryPath()} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    @NotNull
    protected String getResourceLibraryPath() {
        return getResourceNativeLibraryPath();
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
    public boolean loadLibrary(@NotNull final String libName) {
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
    @Nullable
    public File prepareLibrary(@NotNull final String libName) {
        return prepareLibrary(getClass(), libName);
    }

    /**
     * @param outputPath string
     * @param resource   url
     * @return file
     * @deprecated Use {@link #extractResourceTo(String, URL)}
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    @NotNull
    protected File extractResource(final String outputPath, final URL resource) throws IOException {
        return extractResourceTo(outputPath, resource);
    }

    /**
     * @param errorLog Report an error log for this plugin (reported to Icy web site which report then to the
     *                 author of the plugin).
     * @see IcyExceptionHandler#report(PluginDescriptor, String)
     */
    public void report(@NotNull final String errorLog) {
        IcyExceptionHandler.report(descriptor, errorLog);
    }

    @Override
    @NotNull
    public String toString() {
        return getDescriptor().getName();
    }
}
