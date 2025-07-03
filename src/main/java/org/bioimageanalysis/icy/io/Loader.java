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

package org.bioimageanalysis.icy.io;

import loci.formats.ome.OMEXMLMetadataImpl;
import ome.xml.meta.OMEXMLMetadata;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.common.collection.CollectionUtil;
import org.bioimageanalysis.icy.common.exception.UnsupportedFormatException;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.extension.ExtensionLoader;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.PluginLauncher;
import org.bioimageanalysis.icy.gui.dialog.ImporterSelectionDialog;
import org.bioimageanalysis.icy.gui.dialog.SeriesSelectionDialog;
import org.bioimageanalysis.icy.gui.frame.progress.FileFrame;
import org.bioimageanalysis.icy.gui.menu.ApplicationMenuFile;
import org.bioimageanalysis.icy.io.SequenceFileSticher.SequenceFileGroup;
import org.bioimageanalysis.icy.io.SequenceFileSticher.SequencePosition;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.model.OMEUtil;
import org.bioimageanalysis.icy.model.cache.ImageCache;
import org.bioimageanalysis.icy.model.image.IcyBufferedImage;
import org.bioimageanalysis.icy.model.image.ImageProvider;
import org.bioimageanalysis.icy.model.sequence.*;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.preferences.GeneralPreferences;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

/**
 * Sequence / Image loader class.
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class Loader {
    /**
     * XML, XLS and TXT file can be image metadata files used to open the whole image, accept it !
     */
    private final static Set<String> nonImageExtensions = new HashSet<>(CollectionUtil.asList("pdf", "doc", "docx", "rtf", "exe", "wav", "mp3", "app"));

    private final static Set<String> nonMetaExtensions = new HashSet<>(CollectionUtil.asList("jpg", "png", "bmp", "avi"));

    // keep trace of reported / warned plugin
    private static final Set<String> reportedImporterPlugins = new HashSet<>();
    private static final Set<String> warnedImporterPlugins = new HashSet<>();

    private static void handleImporterError(final PluginDescriptor plugin, final Throwable t) {
        final String pluginId = plugin.getName() + " " + plugin.getVersion();

        if (t instanceof UnsupportedClassVersionError) {
            if (!warnedImporterPlugins.contains(pluginId)) {
                // show a specific message in the output console
                IcyLogger.warn(
                        Loader.class,
                        t,
                        "Plugin '" + plugin.getName() + "' " + plugin.getVersion() + " is not compatible with java " + ((int) Math.floor(SystemUtil.getJavaVersionAsNumber())),
                        "You need to install a newer version of java to use it."
                );

                // add to the list of warned plugins
                warnedImporterPlugins.add(pluginId);
            }
        }
        else {
            if (!reportedImporterPlugins.contains(pluginId)) {
                // show a message in the output console
                IcyLogger.error(Loader.class, t, t.getLocalizedMessage());
                // and send an error report (silent as we don't want a dialog appearing here)
                IcyExceptionHandler.report(plugin, IcyExceptionHandler.getErrorMessage(t, true));

                // add to the list of warned plugins
                reportedImporterPlugins.add(pluginId);
            }
        }
    }

    /**
     * Returns all available resource importer.
     */
    public static @NotNull List<Importer> getImporters() {
        final Set<PluginDescriptor> plugins = ExtensionLoader.getPlugins(Importer.class);
        final List<Importer> result = new ArrayList<>();

        for (final PluginDescriptor plugin : plugins) {
            try {
                // add the importer
                result.add((Importer) PluginLauncher.create(plugin));
            }
            catch (final Throwable t) {
                handleImporterError(plugin, t);
            }
        }

        return result;
    }

    /**
     * Returns all available resource (non image) importer which take file as input.
     */
    public static @NotNull List<FileImporter> getFileImporters() {
        final Set<PluginDescriptor> plugins = ExtensionLoader.getPlugins(FileImporter.class);
        final List<FileImporter> result = new ArrayList<>();

        for (final PluginDescriptor plugin : plugins) {
            try {
                // add the importer
                result.add((FileImporter) PluginLauncher.create(plugin));
            }
            catch (final Throwable t) {
                handleImporterError(plugin, t);
            }
        }

        return result;
    }

    /**
     * Returns a Map containing the appropriate file importer for the specified file.<br>
     * A file can be absent from the returned Map when no importer support it.<br>
     *
     * @param importers     the base list of importer we want to test to open file.
     * @param paths         the list of file we want to retrieve importer for.
     * @param useFirstFound if set to <code>true</code> then the first matching importer is automatically selected
     *                      otherwise a dialog appears to let the user to choose the correct importer when
     *                      severals importers match for a file.
     */
    public static @NotNull Map<FileImporter, List<String>> getFileImporters(final @NotNull List<FileImporter> importers, final @NotNull List<String> paths, final boolean useFirstFound) {
        final Map<FileImporter, List<String>> result = new HashMap<>(importers.size());
        final Map<String, FileImporter> extensionImporters = new HashMap<>(importers.size());

        for (final String path : paths) {
            final String ext = FileUtil.getFileExtension(path, false);
            FileImporter imp;

            // try to get importer from extension first
            imp = extensionImporters.get(ext);

            // do not exist yet
            if (imp == null) {
                // find it
                imp = getFileImporter(importers, path, useFirstFound);
                // set the importer for this extension
                if (imp != null)
                    extensionImporters.put(ext, imp);
            }

            // importer found for this path ?
            if (imp != null) {
                // retrieve current list of path for this importer
                // do not exist yet --> create it
                // set the list for this importer
                final List<String> list = result.computeIfAbsent(imp, k -> new ArrayList<>());

                // add path to the list
                list.add(path);
            }
        }

        return result;
    }

    /**
     * Returns a Map containing the appropriate file importer for the specified file.<br>
     * A file can be absent from the returned Map when no importer support it.<br>
     *
     * @param paths         the list of file we want to retrieve importer for.
     * @param useFirstFound if set to <code>true</code> then the first matching importer is automatically selected
     *                      otherwise a dialog appears to let the user to choose the correct importer when
     *                      severals importers match for a file.
     */
    public static Map<FileImporter, List<String>> getFileImporters(final List<String> paths, final boolean useFirstFound) {
        return getFileImporters(getFileImporters(), paths, useFirstFound);
    }

    /**
     * Returns all file importer which can open the specified file.
     */
    public static List<FileImporter> getFileImporters(final List<FileImporter> importers, final String path) {
        final List<FileImporter> result = new ArrayList<>(importers.size());

        for (final FileImporter importer : importers)
            if (importer.acceptFile(path))
                result.add(importer);

        return result;
    }

    /**
     * Returns all file importer which can open the specified file.
     */
    public static List<FileImporter> getFileImporters(final String path) {
        return getFileImporters(getFileImporters(), path);
    }

    /**
     * Returns the appropriate file importer for the specified file.<br>
     * Returns <code>null</code> if no importer can open the file.
     *
     * @param importers     the base list of importer we want to test to open file.
     * @param path          the file we want to retrieve importer for.
     * @param useFirstFound if set to <code>true</code> then the first matching importer is automatically selected
     *                      otherwise a dialog appears to let the user to choose the correct importer when
     *                      severals importers match.
     * @see #getFileImporters(List, String)
     */
    public static FileImporter getFileImporter(final @NotNull List<FileImporter> importers, final String path, final boolean useFirstFound) {
        final List<FileImporter> result = new ArrayList<>(importers.size());

        for (final FileImporter importer : importers) {
            if (importer.acceptFile(path)) {
                if (useFirstFound)
                    return importer;

                result.add(importer);
            }
        }

        // let user select the good importer
        return selectFileImporter(result, path);
    }

    /**
     * Returns the appropriate file importer for the specified file.<br>
     * Returns <code>null</code> if no importer can open the file.
     *
     * @param path          the file we want to retrieve importer for.
     * @param useFirstFound if set to <code>true</code> then the first matching importer is automatically selected
     *                      otherwise a dialog appears to let the user to choose the correct importer when
     *                      severals importers match.
     * @see #getFileImporters(String)
     */
    public static FileImporter getFileImporter(final String path, final boolean useFirstFound) {
        return getFileImporter(getFileImporters(), path, useFirstFound);
    }

    /**
     * Display a dialog to let the user select the appropriate file importer for the specified file.
     */
    public static FileImporter selectFileImporter(final List<FileImporter> importers, final String path) {
        if (importers.size() == 0)
            return null;
        if (importers.size() == 1)
            return importers.get(0);

        if (Icy.getMainInterface().isHeadLess())
            return importers.get(0);

        final Object[] result = new Object[1];

        // use invokeNow carefully !
        ThreadUtil.invokeNow(() -> {
            // get importer
            result[0] = new ImporterSelectionDialog(importers, path).getSelectedImporter();
        });

        return (FileImporter) result[0];
    }

    /**
     * Returns all available sequence importer (different from {@link SequenceIdImporter} or {@link SequenceFileImporter}).
     */
    public static @NotNull List<SequenceImporter> getSequenceImporters() {
        final Set<PluginDescriptor> plugins = ExtensionLoader.getPlugins(SequenceImporter.class);
        final List<SequenceImporter> result = new ArrayList<>();

        for (final PluginDescriptor plugin : plugins) {
            try {
                // add the importer
                result.add((SequenceImporter) PluginLauncher.create(plugin));
            }
            catch (final Throwable t) {
                handleImporterError(plugin, t);
            }
        }

        return result;
    }

    /**
     * Returns all available sequence importer which take path as input.<br>
     * If you want to get specifically importer which use path as input, then you need to use {@link #getSequenceFileImporters()} instead.
     *
     * @see #getSequenceFileImporters()
     */
    public static @NotNull List<SequenceIdImporter> getSequenceIdImporters() {
        final Set<PluginDescriptor> plugins = ExtensionLoader.getPlugins(SequenceIdImporter.class);
        final List<SequenceIdImporter> result = new ArrayList<>();

        for (final PluginDescriptor plugin : plugins) {
            try {
                // add the importer
                result.add((SequenceIdImporter) PluginLauncher.create(plugin));
            }
            catch (final Throwable t) {
                handleImporterError(plugin, t);
            }
        }

        return result;
    }

    /**
     * Returns all available sequence importer which take file as input.
     *
     * @see #getSequenceIdImporters()
     */
    public static @NotNull List<SequenceFileImporter> getSequenceFileImporters() {
        final Set<PluginDescriptor> plugins = ExtensionLoader.getPlugins(SequenceFileImporter.class);
        final List<SequenceFileImporter> result = new ArrayList<>();

        for (final PluginDescriptor plugin : plugins) {
            try {
                // add the importer
                result.add((SequenceFileImporter) PluginLauncher.create(plugin));
            }
            catch (final Throwable t) {
                handleImporterError(plugin, t);
            }
        }

        return result;
    }

    /**
     * Returns a Map containing the appropriate sequence file importer for the specified list of file path.<br>
     * A file can be absent from the returned Map when no importer support it.<br>
     *
     * @param importers     the base list of importer we want to test to open file.
     * @param paths         the list of path we want to find importer for.
     * @param useFirstFound if set to <code>true</code> then the first matching importer is automatically selected
     *                      otherwise a dialog appears to let the user to choose the correct importer when
     *                      severals importers match for a path.
     */
    public static <T extends SequenceFileImporter> @NotNull Map<T, List<String>> getSequenceFileImporters(final @NotNull List<T> importers, final @NotNull List<String> paths, final boolean useFirstFound) {
        final Map<T, List<String>> result = new HashMap<>(importers.size());
        final Map<String, T> extensionImporters = new HashMap<>(importers.size());
        T imp = null;

        for (final String path : paths) {
            // get path extension (useful for path path type)
            final String ext = FileUtil.getFileExtension(path, false);

            // have an extension ? --> try to get importer from extension first
            if (!StringUtil.isEmpty(ext))
                imp = extensionImporters.get(ext);

            // have an importer for this path extension ? --> test it
            if ((imp != null) && !imp.acceptFile(path))
                imp = null;

            // do not exist yet
            if (imp == null) {
                // find it
                imp = getSequenceFileImporter(importers, path, useFirstFound);
                // set the importer for this extension
                if ((imp != null) && !StringUtil.isEmpty(ext))
                    extensionImporters.put(ext, imp);
            }

            // importer found for this path ?
            if (imp != null) {
                // retrieve current list of path for this importer
                // do not exist yet --> create it
                // set the list for this importer
                final List<String> list = result.computeIfAbsent(imp, k -> new ArrayList<>());


                // add path to the list
                list.add(path);
            }
        }

        return result;
    }

    /**
     * Returns a Map containing the appropriate sequence file importer for the specified file.<br>
     * A file can be absent from the returned Map when no importer support it.<br>
     *
     * @param paths         the list of file we want to retrieve importer for.
     * @param grouped       if set to <code>true</code> then we want to group the files so we only need to find the first appropriate importer
     *                      and don't test for other files (only 1 importer will be returned for all files).
     * @param useFirstFound if set to <code>true</code> then the first matching importer is automatically selected
     *                      otherwise a dialog appears to let the user to choose the correct importer when severals importers match for a file.
     */
    public static Map<SequenceFileImporter, List<String>> getSequenceFileImporters(final SequenceFileImporter defaultImporter, final List<String> paths, final boolean grouped, final boolean useFirstFound) {
        if (paths.isEmpty())
            return new HashMap<>();

        final Map<SequenceFileImporter, List<String>> result;

        // have a default importer specified ? --> use it for all files
        if (defaultImporter != null) {
            result = new HashMap<>();
            result.put(defaultImporter, paths);
            return result;
        }

        // grouped ? --> find the first valid importer
        if (grouped) {
            result = new HashMap<>();

            for (final String path : paths) {
                final SequenceFileImporter imp = getSequenceFileImporter(path, useFirstFound);

                if (imp != null) {
                    // use first valid importer for all files
                    result.put(imp, paths);
                    break;
                }
            }

            return result;
        }

        // get importers for each path
        return getSequenceFileImporters(paths, useFirstFound);
    }

    /**
     * Returns a Map containing the appropriate sequence file importer for the specified list of file path.<br>
     * A path can be absent from the returned Map when no importer support it.<br>
     *
     * @param paths         the list of path we want to retrieve importer for.
     * @param useFirstFound if set to <code>true</code> then the first matching importer is automatically selected
     *                      otherwise a dialog appears to let the user to choose the correct importer when
     *                      severals importers match for a file.
     */
    public static Map<SequenceFileImporter, List<String>> getSequenceFileImporters(final List<String> paths, final boolean useFirstFound) {
        return getSequenceFileImporters(getSequenceFileImporters(), paths, useFirstFound);
    }

    /**
     * Returns all sequence file importer which can open the specified path.
     */
    public static <T extends SequenceFileImporter> List<T> getSequenceFileImporters(final List<T> importers, final String path) {
        final List<T> result = new ArrayList<>(importers.size());

        for (final T importer : importers)
            if (importer.acceptFile(path))
                result.add(importer);

        return result;
    }

    /**
     * Returns all sequence file importer which can open the specified file path.
     */
    public static List<SequenceFileImporter> getSequenceFileImporters(final String path) {
        return getSequenceFileImporters(getSequenceFileImporters(), path);
    }

    /**
     * Returns the appropriate sequence file importer for the specified path.<br>
     * Depending the parameters it will open a dialog to let the user choose the importer to use
     * when severals match.<br>
     * Returns <code>null</code> if no importer can open the specified path.
     *
     * @param importers     the base list of importer we want to test to open file path.
     * @param path          the path we want to retrieve importer for.
     * @param useFirstFound if set to <code>true</code> then the first matching importer is automatically selected
     *                      otherwise a dialog appears to let the user to choose the correct importer when
     *                      severals importers match.
     * @see #getSequenceFileImporters(List, String)
     */
    public static <T extends SequenceFileImporter> T getSequenceFileImporter(final List<T> importers, final String path, final boolean useFirstFound) {
        final List<T> result = new ArrayList<>(importers.size());

        for (final T importer : importers) {
            if (importer.acceptFile(path)) {
                if (useFirstFound)
                    return importer;

                result.add(importer);
            }
        }

        // let user select the good importer
        return selectSequenceFileImporter(result, path);
    }

    /**
     * Returns the appropriate sequence file importer for the specified file path.<br>
     * Depending the parameters it will open a dialog to let the user choose the importer to use
     * when severals match.<br>
     * Returns <code>null</code> if no importer can open the specified file path.
     *
     * @param path          the file path we want to retrieve importer for.
     * @param useFirstFound if set to <code>true</code> then the first matching importer is automatically selected
     *                      otherwise a dialog appears to let the user to choose the correct importer when
     *                      severals importers match.
     * @see #getSequenceFileImporters(String)
     */
    public static SequenceFileImporter getSequenceFileImporter(final String path, final boolean useFirstFound) {
        return getSequenceFileImporter(getSequenceFileImporters(), path, useFirstFound);
    }

    /**
     * Display a dialog to let the user select the appropriate sequence file importer for the given file path.
     */
    @SuppressWarnings("unchecked")
    public static <T extends SequenceFileImporter> T selectSequenceFileImporter(final List<T> importers, final String path) {
        if (importers.size() == 0)
            return null;
        if (importers.size() == 1)
            return importers.get(0);

        // no choice, take first one
        if (Icy.getMainInterface().isHeadLess())
            return importers.get(0);

        final Object[] result = new Object[1];

        // use invokeNow carefully !
        ThreadUtil.invokeNow(() -> {
            // get importer
            final ImporterSelectionDialog selectionDialog = new ImporterSelectionDialog(importers, path);

            if (!selectionDialog.isCanceled())
                result[0] = selectionDialog.getSelectedImporter();
            else
                result[0] = null;
        });

        return (T) result[0];
    }

    public static <T extends SequenceFileImporter> T cloneSequenceFileImporter(final T importer) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        if (importer == null)
            return null;

        @SuppressWarnings("unchecked") final T result = (T) importer.getClass().getDeclaredConstructor().newInstance();

        result.setGroupFiles(importer.isGroupFiles());
        result.setReadOriginalMetadata(importer.getReadOriginalMetadata());

        return result;
    }

    static <T extends SequenceFileImporter> T cloneAndOpenSequenceFileImporter(final T importer) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException, UnsupportedFormatException, InterruptedException {
        final T result = cloneSequenceFileImporter(importer);

        if (result != null)
            if (!result.open(importer.getOpened(), 0))
                throw new UnsupportedFormatException("Image file '" + importer.getOpened() + "' could not be opened by " + result + " importer.");

        return result;
    }

    /**
     * Returns <code>true</code> if the specified path describes a file type (from extension) which
     * is well known to
     * not be an image file.<br>
     * For instance <i>.exe</i>, <i>.wav</i> or <i>.doc</i> file cannot specify an image file so we
     * can quickly discard them (extension based exclusion)
     */
    public static boolean canDiscardImageFile(final String path) {
        final String ext = FileUtil.getFileExtension(path, false).toLowerCase();

        return nonImageExtensions.contains(ext);
    }

    /**
     * Returns true if the specified file is a supported image file.
     */
    public static boolean isSupportedImageFile(final String path) {
        return getSequenceFileImporter(path, true) != null;
    }

    /**
     * @return <code>true</code> if the specified path describes an image file type (from extension) which
     * is well known to store usable metadata for image processing (image source, pixel size...)<br>
     * For instance <i>jpg</i>, <i>png</i> or <i>avi</i> files doesn't not store useful metadata while <i>tif</i> does.
     */
    public static boolean hasMetadata(final String path) {
        final String ext = FileUtil.getFileExtension(path, false).toLowerCase();

        return !nonImageExtensions.contains(ext) && !nonMetaExtensions.contains(ext);
    }

    /**
     * Returns path which are supported by the specified imported for the given list of paths.
     */
    public static List<String> getSupportedFiles(final SequenceFileImporter importer, final List<String> paths) {
        final List<String> result = new ArrayList<>();

        for (final String path : paths) {
            if (importer.acceptFile(path))
                result.add(path);
        }

        return result;
    }

    /**
     * Check if we can open the given image plane resolution (XY size &lt; 2^31).<br>
     * If the image plane is too large the method throw an exception with an informative error
     * message about the encountered limitation.
     *
     * @param resolution    wanted image resolution: a value of <code>0</code> means full resolution of the
     *                      original image while value <code>1</code> correspond to the resolution / 2.<br>
     *                      Formula: <code>resolution / 2^value</code><br>
     * @param sizeX         width of the image region we want to load
     * @param sizeY         height of the image region we want to load
     * @param messageSuffix message suffix for the exception if wanted
     * @return the number of pixels of the image plane
     * @throws UnsupportedOperationException if the XY plane size is &gt;= 2^31 pixels
     */
    public static long checkOpeningPlane(final int resolution, final int sizeX, final int sizeY, final String messageSuffix) throws UnsupportedOperationException {
        // size of XY plane
        long sizeXY = (long) sizeX * (long) sizeY;
        // wanted resolution
        sizeXY /= Math.pow(4, resolution);

        // we can't handle that plane size
        if (sizeXY > Integer.MAX_VALUE)
            throw new UnsupportedOperationException("Cannot open image with a XY plane size >= 2^31." + ((messageSuffix != null) ? messageSuffix : ""));

        return sizeXY;
    }

    /**
     * Check if we have enough resource to open the image defined by the given size information and
     * wanted resolution.<br>
     * If the image is too large to be displayed at full resolution (XY plane size &gt; 2^31) or if we
     * don't have enough
     * memory to store the whole image the method throw an exception with an informative error
     * message about the
     * encountered limitation.
     *
     * @param resolution    wanted image resolution: a value of <code>0</code> means full resolution of the
     *                      original image while value <code>1</code> correspond to the resolution / 2.<br>
     *                      Formula: <code>resolution / 2^value</code><br>
     * @param sizeX         width of the image region we want to load
     * @param sizeY         height of the image region we want to load
     * @param sizeC         number of channel we want to load
     * @param sizeZ         number of slice we want to load (can be different from original image sizeZ)
     * @param sizeT         number of frame we want to load (can be different from original image sizeT)
     * @param dataType      pixel data type of the image we want to load
     * @param messageSuffix message suffix for the exception if wanted
     * @throws UnsupportedOperationException if the XY plane size is &gt;= 2^31 pixels
     * @throws OutOfMemoryError              if there is not enough memory to open the image
     */
    public static void checkOpening(final int resolution, final int sizeX, final int sizeY, final int sizeC, final int sizeZ, final int sizeT, final DataType dataType, final String messageSuffix) throws UnsupportedOperationException, OutOfMemoryError {
        final long sizeXY = checkOpeningPlane(resolution, sizeX, sizeY, messageSuffix);

        // get free memory
        long freeInByte = SystemUtil.getJavaFreeMemory() - (16 * 1024 * 1024);
        // check that we have enough memory for the whole image and for the ARGB image used for
        // display (sizeXY * 4)
        final long sizeInByte = (sizeXY * sizeC * sizeZ * sizeT * dataType.getSize()) + (sizeXY * 4);

        // not enough memory to store the whole image ?
        if (sizeInByte > freeInByte) {
            // try to release some memory
            System.gc();
            // get updated free memory
            freeInByte = SystemUtil.getJavaFreeMemory() - (16 * 1024 * 1024);
        }

        // still not enough memory ?
        if (sizeInByte > freeInByte)
            throw new OutOfMemoryError("Not enough memory to open the wanted image resolution." + ((messageSuffix != null) ? messageSuffix : ""));
    }

    /**
     * Check if we have enough resource to open the image defined by the given metadata information, series index and
     * wanted resolution.<br>
     * If the image is too large to be displayed at full resolution (XY plane size &gt; 2^31) or if we* don't have enough
     * memory to store the whole image the method throw an exception with an informative error message about the
     * encountered limitation.
     *
     * @param meta          metadata of the image
     * @param series        series index
     * @param resolution    wanted image resolution: a value of <code>0</code> means full resolution of the
     *                      original image while value
     *                      <code>1</code> correspond to the resolution / 2.<br>
     *                      Formula: <code>resolution / 2^value</code><br>
     * @param sizeZ         number of slice we want to load (can be different from original image sizeZ)
     * @param sizeT         number of frame we want to load (can be different from original image sizeT)
     * @param messageSuffix message suffix for the exception if wanted
     * @throws UnsupportedOperationException if the XY plane size is &gt;= 2^31 pixels
     * @throws OutOfMemoryError              if there is not enough memory to open the image
     */
    public static void checkOpening(final OMEXMLMetadata meta, final int series, final int resolution, final int sizeZ, final int sizeT, final String messageSuffix) throws UnsupportedOperationException, OutOfMemoryError {
        checkOpening(resolution, MetaDataUtil.getSizeX(meta, series), MetaDataUtil.getSizeY(meta, series), MetaDataUtil.getSizeC(meta, series), sizeZ, sizeT, MetaDataUtil.getDataType(meta, series), messageSuffix);
    }

    /**
     * Check if we have enough resource to open the image defined by the given metadata information, series index and
     * wanted resolution.<br>
     * If the image is too large to be displayed at full resolution (XY plane size &gt; 2^31) or if we don't have enough
     * memory to store the whole image the method throw an exception with an informative error message about the
     * encountered limitation.
     *
     * @param meta          metadata of the image
     * @param series        series index
     * @param resolution    wanted image resolution: a value of <code>0</code> means full resolution of the
     *                      original image while value
     *                      <code>1</code> correspond to the resolution / 2.<br>
     *                      Formula: <code>resolution / 2^value</code><br>
     * @param messageSuffix message suffix for the exception if wanted
     * @throws UnsupportedOperationException if the XY plane size is &gt;= 2^31 pixels
     * @throws OutOfMemoryError              if there is not enough memory to open the image
     */
    public static void checkOpening(final OMEXMLMetadata meta, final int series, final int resolution, final String messageSuffix) throws UnsupportedOperationException, OutOfMemoryError {
        checkOpening(meta, series, resolution, MetaDataUtil.getSizeZ(meta, series), MetaDataUtil.getSizeT(meta, series), messageSuffix);
    }

    /**
     * Loads and returns metadata of the specified image file with given importer.<br>
     * It can returns <code>null</code> if the specified file is not a valid or supported) image
     * file.
     */
    public static OMEXMLMetadata getOMEXMLMetaData(final SequenceFileImporter importer, final String path) throws Exception {
        if (importer.open(path, 0)) {
            try (importer) {
                return importer.getOMEXMLMetaData();
            }
        }

        return null;
    }

    /**
     * Loads and returns metadata of the specified image file.<br>
     * It can returns <code>null</code> if the specified file is not a valid or supported) image
     * file.
     */
    public static OMEXMLMetadata getOMEXMLMetaData(final String path) throws Exception {
        OMEXMLMetadata result;
        UnsupportedFormatException lastError = null;

        for (final SequenceFileImporter importer : getSequenceFileImporters(path)) {
            try {
                result = getOMEXMLMetaData(importer, path);

                if (result != null)
                    return result;
            }
            catch (final UnsupportedFormatException e) {
                lastError = e;
            }
        }

        throw new UnsupportedFormatException("Image file '" + path + "' is not supported :\n", lastError);
    }

    /**
     * Returns a thumbnail of the specified image file path.<br>
     * It can return <code>null</code> if the specified file is not a valid or supported image file.
     *
     * @param importer Importer used to open and load the thumbnail from the image file.
     * @param path     image file path.
     * @param series   Series index we want to retrieve thumbnail from (for multi series image).<br>
     *                 Set to 0 if unsure.
     */
    public static IcyBufferedImage loadThumbnail(final SequenceFileImporter importer, final String path, final int series) throws Exception {
        if (importer.open(path, 0)) {
            try (importer) {
                return importer.getThumbnail(series);
            }
        }

        return null;
    }

    /**
     * Returns a thumbnail of the specified image file path.
     *
     * @param path   image file path.
     * @param series Series index we want to retrieve thumbnail from (for multi series image).<br>
     *               Set to 0 if unsure.
     */
    public static IcyBufferedImage loadThumbnail(final String path, final int series) throws Exception {
        IcyBufferedImage result;
        UnsupportedFormatException lastError = null;

        for (final SequenceFileImporter importer : getSequenceFileImporters(path)) {
            try {
                result = loadThumbnail(importer, path, series);

                if (result != null)
                    return result;
            }
            catch (final UnsupportedFormatException e) {
                lastError = e;
            }
        }

        throw new UnsupportedFormatException("Image file '" + path + "' is not supported :\n", lastError);
    }

    /**
     * Load and return the image at given position from the specified file path.<br>
     * For lower image level access, you can use importer methods.
     *
     * @param importer Importer used to open and load the image file.
     * @param path     image file path.
     * @param series   Series index we want to retrieve image from (for multi series image).<br>
     *                 Set to 0 if unsure (default).
     * @param z        Z position of the image to open.
     * @param t        T position of the image to open.
     */
    public static IcyBufferedImage loadImage(final SequenceFileImporter importer, final String path, final int series, final int z, final int t) throws Exception {
        if ((importer == null) || !importer.open(path, 0))
            throw new UnsupportedFormatException("Image file '" + path + "' is not supported !");

        try (importer) {
            return importer.getImage(series, z, t);
        }
    }

    /**
     * Load and return the image at given position from the specified file path.<br>
     * For lower image level access, you can use {@link #getSequenceFileImporter(String, boolean)}
     * method and
     * directly work through the returned {@link ImageProvider} interface.
     *
     * @param path   image file path.
     * @param series Series index we want to retrieve image from (for multi series image).<br>
     *               Set to 0 if unsure (default).
     * @param z      Z position of the image to open.
     * @param t      T position of the image to open.
     */
    public static IcyBufferedImage loadImage(final String path, final int series, final int z, final int t) throws Exception {
        return loadImage(getSequenceFileImporter(path, true), path, series, z, t);
    }

    /**
     * Load and return a single image from the specified file path.<br>
     * If the specified file contains severals image the first image is returned.
     */
    public static IcyBufferedImage loadImage(final String path) throws Exception {
        return loadImage(path, 0, 0, 0);
    }

    /**
     * Load a list of sequence from the specified list of file with the given {@link SequenceFileImporter} and returns them.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The method returns an empty array if an error occurred or if no file could be opened (not supported).<br>
     * If the user cancelled the action (series selection dialog) then it returns <code>null</code>.
     *
     * @param importer      Importer used to open and load image files.<br>
     *                      If set to <code>null</code> the loader will search for a compatible importer and if
     *                      several importers match the user will have to select the appropriate one from a
     *                      selection dialog.
     * @param paths         List of image file to load.
     * @param series        Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *                      -1 is a special value so it gives a chance to the user<br>
     *                      to select the series to open from a series selector dialog.
     * @param forceVolatile If set to <code>true</code> then image data is forced to volatile (see {@link IcyBufferedImage#isVolatile()}).<br>
     *                      Note that if you don't have enough memory to load the whole Sequence into memory then image data are always made volatile.
     * @param separate      Force image to be loaded in separate sequence.
     * @param autoOrder     Try to order image in sequence from their filename
     * @param addToRecent   If set to true the files list will be traced in recent opened sequence.
     * @param showProgress  Show progression of loading process.
     */
    public static List<Sequence> loadSequences(final SequenceFileImporter importer, final List<String> paths, final int series, final boolean forceVolatile, final boolean separate, final boolean autoOrder, final boolean addToRecent, final boolean showProgress) {
        final List<Sequence> result = new ArrayList<>();

        // detect if this is a complete folder load
        final boolean directory = (paths.size() == 1) && new File(paths.get(0)).isDirectory();
        // explode path list
        final List<String> singlePaths = cleanNonImageFile(explode(paths));
        final boolean grouped = (singlePaths.size() > 1) && !separate;
        // get the sequence importers first
        final Map<SequenceFileImporter, List<String>> sequenceFileImporters = getSequenceFileImporters(importer, singlePaths, grouped, false);

        for (final Entry<SequenceFileImporter, List<String>> entry : sequenceFileImporters.entrySet()) {
            final SequenceFileImporter imp = entry.getKey();
            final List<String> currPaths = entry.getValue();
            final boolean dir = directory && (sequenceFileImporters.size() == 1) && (currPaths.size() == singlePaths.size());

            // load sequence
            result.addAll(loadSequences(imp, currPaths, series, forceVolatile, separate, autoOrder, dir, addToRecent, showProgress));

            // remove loaded files
            singlePaths.removeAll(currPaths);
        }

        // remove remaining XML persistence files...
        for (int i = singlePaths.size() - 1; i >= 0; i--)
            if (SequencePersistent.isValidXMLPersitence(singlePaths.get(i)))
                singlePaths.remove(i);

        // remaining files ?
        if (singlePaths.size() > 0) {
            // get first found importer for remaining files
            final Map<SequenceFileImporter, List<String>> importers = getSequenceFileImporters(singlePaths, true);

            // user canceled action for these paths so we remove them
            for (final List<String> values : importers.values())
                singlePaths.removeAll(values);

            if (singlePaths.size() > 0) {
                // just log in console
                final ArrayList<String> messages = new ArrayList<>();
                messages.add("No compatible importer found for the following files:");
                messages.addAll(singlePaths);
                IcyLogger.error(Loader.class, messages.toArray(new String[0]));
            }
        }

        // return sequences
        return result;
    }

    /**
     * Load a list of sequence from the specified list of file with the given {@link SequenceFileImporter} and returns them.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The method returns an empty array if an error occurred or if no file could be opened (not supported).<br>
     * If the user cancelled the action (series selection dialog) then it returns <code>null</code>.
     *
     * @param importer     Importer used to open and load image files.<br>
     *                     If set to <code>null</code> the loader will search for a compatible importer and if
     *                     several importers match the user will have to select the appropriate one from a
     *                     selection dialog.
     * @param paths        List of image file to load.
     * @param series       Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *                     -1 is a special value so it gives a chance to the user<br>
     *                     to select the series to open from a series selector dialog.
     * @param separate     Force image to be loaded in separate sequence.
     * @param autoOrder    Try to order image in sequence from their filename
     * @param addToRecent  If set to true the files list will be traced in recent opened sequence.
     * @param showProgress Show progression of loading process.
     */
    public static List<Sequence> loadSequences(final SequenceFileImporter importer, final List<String> paths, final int series, final boolean separate, final boolean autoOrder, final boolean addToRecent, final boolean showProgress) {
        return loadSequences(importer, paths, series, false, separate, autoOrder, addToRecent, showProgress);
    }

    /**
     * Loads a list of sequence from the specified list of file and returns them.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The method returns an empty array if an error occurred or if no file could not be opened (not
     * supported).<br>
     * If several importers match to open a file the user will have to select the appropriate one
     * from a selection dialog.
     *
     * @param paths        List of image file to load.
     * @param series       Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *                     -1 is a special value so it gives a chance to the user<br>
     *                     to select the series to open from a series selector dialog.
     * @param separate     Force image to be loaded in separate sequence.
     * @param autoOrder    Try to order image in sequence from their filename
     * @param addToRecent  If set to true the files list will be traced in recent opened sequence.
     * @param showProgress Show progression of loading process.
     */
    public static List<Sequence> loadSequences(final List<String> paths, final int series, final boolean separate, final boolean autoOrder, final boolean addToRecent, final boolean showProgress) {
        return loadSequences(null, paths, series, separate, autoOrder, addToRecent, showProgress);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * The function try to guess image ordering from file name and metadata.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     *
     * @param importer     Importer used to load the image file (can be null for automatic selection).
     * @param paths        List of image file to load.
     * @param addToRecent  If set to true the files list will be traced in recent opened sequence.
     * @param showProgress Show progression of loading process.
     * @see #getSequenceFileImporter(String, boolean)
     */
    public static Sequence loadSequence(final SequenceFileImporter importer, final List<String> paths, final boolean addToRecent, final boolean showProgress) {
        final ApplicationMenuFile mainMenu;
        final FileFrame loadingFrame;
        Sequence result = null;

        if (addToRecent)
            mainMenu = ApplicationMenuFile.getInstance();
        else
            mainMenu = null;
        if (showProgress && !Icy.getMainInterface().isHeadLess())
            loadingFrame = new FileFrame("Loading", null);
        else
            loadingFrame = null;

        try {
            // we want only one group
            final SequenceFileGroup group = SequenceFileSticher.groupFiles(importer, cleanNonImageFile(paths), true, loadingFrame);
            // do group loading
            result = internalLoadGroup(group, false, false, mainMenu, loadingFrame);
            // load sequence XML data
            if (GeneralPreferences.getSequencePersistence())
                result.loadXMLData();
        }
        catch (final Throwable t) {
            // just show the error
            IcyLogger.error(Loader.class, t, "Failed to open file(s).");
        }
        finally {
            if (loadingFrame != null)
                loadingFrame.close();
        }

        return result;
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.
     *
     * @param importer     Importer used to load the image file (can be null for automatic selection).
     * @param paths        List of image file to load.
     * @param showProgress Show progression of loading process.
     * @see #getSequenceFileImporter(String, boolean)
     */
    public static Sequence loadSequence(final SequenceFileImporter importer, final List<String> paths, final boolean showProgress) {
        return loadSequence(importer, paths, false, showProgress);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.<br>
     * If several importers match to open a file the user will have to select the appropriate one
     * from a selection dialog.
     *
     * @param files        List of image file to load (can be String or File object).
     * @param showProgress Show progression of loading process.
     */
    @SuppressWarnings("unchecked")
    public static Sequence loadSequence(final List<?> files, final boolean showProgress) {
        if (files.size() == 0)
            return null;

        final List<String> paths;

        if (files.get(0) instanceof File)
            paths = FileUtil.toPaths((List<File>) files);
        else
            paths = (List<String>) files;

        return loadSequence(null, paths, showProgress);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * The function can return null if no sequence can be loaded from the specified files.<br>
     * If several importers match to open a file the user will have to select the appropriate one
     * from a selection dialog.
     *
     * @param files List of image file to load (can be String or File object).
     */
    public static Sequence loadSequence(final List<?> files) {
        return loadSequence(files, true);
    }

    /**
     * Loads the specified image file and return it as a Sequence, it can return <code>null</code>
     * if an error occured.<br>
     * As this method can take sometime, you should not call it from the EDT.
     *
     * @param importer     Importer used to load the image file.<br>
     *                     If set to <code>null</code> the loader will search for a compatible importer and if
     *                     several importers
     *                     match the user will have to select the appropriate one from a selection dialog if
     *                     <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *                     compatible importer will be automatically used.
     * @param path         Image file to load.
     * @param series       Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *                     -1 is a special value so it gives a chance to the user to select series to open from a
     *                     series selector dialog.
     * @param addToRecent  If set to true the path will be traced in recent opened sequence.
     * @param showProgress Show progression of loading process.
     */
    public static Sequence loadSequence(final SequenceFileImporter importer, final String path, final int series, final boolean addToRecent, final boolean showProgress) {
        return loadSequence(importer, path, series, 0, null, -1, -1, -1, -1, -1, addToRecent, showProgress);
    }

    /**
     * Load a sequence from the specified file.<br>
     * As the function can take sometime you should not call it from the AWT EDT.
     *
     * @param importer     Importer used to load the image file.<br>
     *                     If set to <code>null</code> the loader will search for a compatible importer and if
     *                     several importers
     *                     match the user will have to select the appropriate one from a selection dialog if
     *                     <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *                     compatible importer will be automatically used.
     * @param path         Image file to load.
     * @param series       Series index to load (for multi series sequence), set to 0 if unsure (default).
     * @param showProgress Show progression of loading process.
     */
    public static Sequence loadSequence(final SequenceFileImporter importer, final String path, final int series, final boolean showProgress) {
        return loadSequence(importer, path, series, false, showProgress);
    }

    /**
     * Load a sequence from the specified file.<br>
     * As the function can take sometime you should not call it from the AWT EDT.<br>
     * If several importers match to open the file the user will have to select the appropriate one
     * from a selection dialog if <code>showProgress</code> parameter is set to <code>true</code>
     * otherwise the first
     * compatible importer is automatically used.
     *
     * @param path         Image file to load.
     * @param series       Series index to load (for multi series sequence), set to 0 if unsure (default).
     * @param showProgress Show progression of loading process.
     */
    public static Sequence loadSequence(final String path, final int series, final boolean showProgress) {
        return loadSequence(null, path, series, showProgress);
    }

    /**
     * Load the specified files with the given {@link FileImporter}.<br>
     * The loading process is asynchronous.<br>
     * The FileImporter is responsible to make the loaded files available in the application.<br>
     * This method should be used only for non image file.
     *
     * @param importer     Importer used to open and load files.<br>
     *                     If set to <code>null</code> the loader will search for a compatible importer and if
     *                     several importers match the user will have to select the appropriate one from a
     *                     selection dialog.
     * @param paths        list of file to load
     * @param showProgress Show progression in loading process
     */
    public static void load(final FileImporter importer, final List<String> paths, final boolean showProgress) {
        // asynchronous call
        ThreadUtil.bgRun(() -> {
            // explode path list
            final List<String> singlePaths = explode(paths);

            if (singlePaths.size() > 0) {
                // get the file importer now for remaining file
                final Map<FileImporter, List<String>> fileImporters;

                // importer not defined --> find the appropriate importers
                if (importer == null)
                    fileImporters = getFileImporters(singlePaths, false);
                else {
                    fileImporters = new HashMap<>(1);
                    fileImporters.put(importer, new ArrayList<>(singlePaths));
                }

                for (final Entry<FileImporter, List<String>> entry : fileImporters.entrySet()) {
                    final FileImporter importer1 = entry.getKey();
                    final List<String> currPaths = entry.getValue();

                    // load files
                    loadFiles(importer1, paths, true, showProgress);

                    // remove loaded files
                    singlePaths.removeAll(currPaths);
                }
            }

            // remaining files ?
            if (singlePaths.size() > 0) {
                // get first found importer for remaining files
                final Map<FileImporter, List<String>> importers = getFileImporters(singlePaths, true);

                // user canceled action for these paths so we remove them
                for (final List<String> values : importers.values())
                    singlePaths.removeAll(values);

                if (singlePaths.size() > 0) {
                    // just log in console
                    final ArrayList<String> messages = new ArrayList<>();
                    messages.add("No compatible importer found for the following files:");
                    messages.addAll(singlePaths);
                    IcyLogger.error(Loader.class, messages.toArray(new String[0]));
                }
            }
        });
    }

    /**
     * Load the specified files with the given {@link FileImporter}.<br>
     * The FileImporter is responsible to make the loaded files available in the application.<br>
     * This method should be used only for non image file.
     *
     * @param importer     Importer used to open and load image files.
     * @param paths        list of file to load
     * @param addToRecent  If set to true the files list will be traced in recent opened files.
     * @param showProgress Show progression in loading process
     */
    public static void loadFiles(final FileImporter importer, final List<String> paths, final boolean addToRecent, final boolean showProgress) {
        final ApplicationMenuFile mainMenu;
        final FileFrame loadingFrame;

        if (addToRecent)
            mainMenu = ApplicationMenuFile.getInstance();
        else
            mainMenu = null;
        if (showProgress && !Icy.getMainInterface().isHeadLess()) {
            loadingFrame = new FileFrame("Loading", null);
            loadingFrame.setLength(paths.size());
            loadingFrame.setPosition(0);
        }
        else
            loadingFrame = null;

        try {
            // load each file in a separate sequence
            for (final String path : paths) {
                if (loadingFrame != null)
                    loadingFrame.incPosition();

                // load current file
                importer.load(path, loadingFrame);

                // add as separate item to recent file list
                if (mainMenu != null)
                    mainMenu.addRecentLoadedFile(new File(FileUtil.getGenericPath(path)));
            }
        }
        catch (final Throwable t) {
            // just show the error
            IcyLogger.error(Loader.class, t, "Failed to open file(s).");
        }
        finally {
            if (loadingFrame != null)
                loadingFrame.close();
        }
    }

    /**
     * Load the specified image file with the given {@link SequenceFileImporter}.<br>
     * The loading process is asynchronous.<br>
     * The resulting sequence is automatically displayed when the process complete.
     *
     * @param importer      Importer used to load the image file.<br>
     *                      If set to <code>null</code> the loader will search for a compatible importer and if
     *                      several importers match the user will have to select the appropriate one from a selection dialog if
     *                      <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *                      compatible importer will be automatically used.
     * @param path          image file to load
     * @param series        Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *                      -1 is a special value so it gives a chance to the user to select series to open from a
     *                      series selector dialog.
     * @param resolution    Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *                      The retrieved image resolution is equal to
     *                      <code>image.resolution / (2^resolution)</code><br>
     *                      So for instance level 0 is the default/full image resolution while level 1 is base
     *                      image
     *                      resolution / 2 and so on...
     * @param region        The 2D region of the image we want to retrieve.<br>
     *                      If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ          the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole stack.
     * @param maxZ          the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole stack.
     * @param minT          the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole timelaps.
     * @param maxT          the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole timelaps.
     * @param channel       C position of the image (channel) we want retrieve (-1 means all channel).
     * @param forceVolatile If set to <code>true</code> then image data is forced to volatile (see {@link IcyBufferedImage#isVolatile()}).<br>
     *                      Note that if you don't have enough memory to load the whole Sequence into memory then image data are always made volatile.
     * @param separate      Force image to be loaded in separate sequence if possible (disable stitching if any)
     * @param addToRecent   If set to true the files list will be traced in recent opened sequence.
     * @param showProgress  Show progression in loading process
     */
    public static void load(
            final SequenceFileImporter importer, final String path, final int series,
            final int resolution, final Rectangle region, final int minZ,
            final int maxZ, final int minT, final int maxT,
            final int channel, final boolean forceVolatile, final boolean separate,
            final boolean addToRecent, final boolean showProgress
    ) {
        // asynchronous call
        ThreadUtil.bgRun(() -> {
            // normal opening operation ?
            if ((resolution == 0) && (region == null) && (minZ <= 0) && (maxZ == -1) && (minT <= 0) && (maxT == -1) && (channel == -1)) {
                // load sequence (we use this method to allow multi series opening)
                final List<Sequence> sequences = loadSequences((importer == null) ? getSequenceFileImporter(path, !showProgress) : importer,
                        CollectionUtil.asList(path), series, forceVolatile, separate, false, false, addToRecent, showProgress);

                // and display them
                for (final Sequence sequence : sequences)
                    if (sequence != null)
                        Icy.getMainInterface().addSequence(sequence);
            }
            else {
                // load sequence
                final Sequence sequence = loadSequence(importer, path, series, resolution, region, minZ, maxZ, minT, maxT, channel, forceVolatile,
                        addToRecent, showProgress);

                // and display it
                if (sequence != null)
                    Icy.getMainInterface().addSequence(sequence);
            }
        });
    }

    /**
     * Load the specified image file with the given {@link SequenceFileImporter}.<br>
     * The loading process is asynchronous.<br>
     * The resulting sequence is automatically displayed when the process complete.
     *
     * @param importer     Importer used to load the image file.<br>
     *                     If set to <code>null</code> the loader will search for a compatible importer and if
     *                     several importers match the user will have to select the appropriate one from a selection dialog if
     *                     <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *                     compatible importer will be automatically used.
     * @param path         image file to load
     * @param series       Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *                     -1 is a special value so it gives a chance to the user to select series to open from a
     *                     series selector dialog.
     * @param resolution   Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *                     The retrieved image resolution is equal to
     *                     <code>image.resolution / (2^resolution)</code><br>
     *                     So for instance level 0 is the default/full image resolution while level 1 is base
     *                     image
     *                     resolution / 2 and so on...
     * @param region       The 2D region of the image we want to retrieve.<br>
     *                     If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ         the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole stack.
     * @param maxZ         the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole stack.
     * @param minT         the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole timelaps.
     * @param maxT         the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole timelaps.
     * @param channel      C position of the image (channel) we want retrieve (-1 means all channel).
     * @param separate     Force image to be loaded in separate sequence if possible (disable stitching if any)
     * @param addToRecent  If set to true the files list will be traced in recent opened sequence.
     * @param showProgress Show progression in loading process
     */
    public static void load(
            final SequenceFileImporter importer, final String path, final int series,
            final int resolution, final Rectangle region, final int minZ,
            final int maxZ, final int minT, final int maxT,
            final int channel, final boolean separate, final boolean addToRecent,
            final boolean showProgress
    ) {
        load(importer, path, series, resolution, region, minZ, maxZ, minT, maxT, channel, false, separate, addToRecent, showProgress);
    }

    /**
     * Load the specified image file list (try to group them as much as possible).<br>
     * The loading process is asynchronous.<br>
     * The resulting sequence is automatically displayed when the process complete.
     *
     * @param importer     Importer used to open and load image files.<br>
     *                     If set to <code>null</code> the loader will search for a compatible importer and if
     *                     several importers match the user will have to select the appropriate one from a
     *                     selection dialog.
     * @param paths        list of file we want to group to build a to load
     * @param resolution   Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *                     The retrieved image resolution is equal to
     *                     <code>image.resolution / (2^resolution)</code><br>
     *                     So for instance level 0 is the default/full image resolution while level 1 is base
     *                     image
     *                     resolution / 2 and so on...
     * @param region       The 2D region of the image we want to retrieve.<br>
     *                     If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ         the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole stack.
     * @param maxZ         the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole stack.
     * @param minT         the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole timelaps.
     * @param maxT         the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole timelaps.
     * @param channel      C position of the image (channel) we want retrieve (-1 means all channel).
     * @param directory    specify is the source is a single complete directory
     * @param addToRecent  If set to true the files list will be traced in recent opened sequence.
     * @param showProgress Show progression in loading process
     */
    public static void load(
            final SequenceFileImporter importer, final List<String> paths, final boolean autoOrder,
            final int resolution, final Rectangle region, final int minZ,
            final int maxZ, final int minT, final int maxT,
            final int channel, final boolean directory, final boolean addToRecent,
            final boolean showProgress
    ) {
        // asynchronous call
        ThreadUtil.bgRun(() -> {
            final ApplicationMenuFile mainMenu;
            final FileFrame loadingFrame;

            if (addToRecent)
                mainMenu = ApplicationMenuFile.getInstance();
            else
                mainMenu = null;
            if (showProgress && !Icy.getMainInterface().isHeadLess())
                loadingFrame = new FileFrame("Loading", null);
            else
                loadingFrame = null;

            try {
                // load sequence from group with advanced options
                for (final SequenceFileGroup group : SequenceFileSticher.groupAllFiles(importer, cleanNonImageFile(paths), autoOrder, loadingFrame)) {
                    final Sequence sequence = internalLoadGroup(group, resolution, region, minZ, maxZ, minT, maxT, channel, false, directory, mainMenu,
                            loadingFrame);

                    if (sequence != null) {
                        // load sequence XML data
                        if (GeneralPreferences.getSequencePersistence())
                            sequence.loadXMLData();
                        // and display it
                        Icy.getMainInterface().addSequence(sequence);
                    }
                }
            }
            catch (final Throwable t) {
                // just show the error
                IcyLogger.error(Loader.class, t, "Failed to open file(s).");
            }
            finally {
                if (loadingFrame != null)
                    loadingFrame.close();
            }
        });
    }

    /**
     * Load the specified image file list (try to group them as much as possible).<br>
     * The loading process is asynchronous.<br>
     * The resulting sequence is automatically displayed when the process complete.
     *
     * @param paths        list of file we want to group to build a to load
     * @param resolution   Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *                     The retrieved image resolution is equal to
     *                     <code>image.resolution / (2^resolution)</code><br>
     *                     So for instance level 0 is the default/full image resolution while level 1 is base
     *                     image
     *                     resolution / 2 and so on...
     * @param region       The 2D region of the image we want to retrieve.<br>
     *                     If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ         the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole stack.
     * @param maxZ         the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole stack.
     * @param minT         the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole timelaps.
     * @param maxT         the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole timelaps.
     * @param channel      C position of the image (channel) we want retrieve (-1 means all channel).
     * @param directory    specify is the source is a single complete directory
     * @param addToRecent  If set to true the files list will be traced in recent opened sequence.
     * @param showProgress Show progression in loading process
     */
    public static void load(
            final List<String> paths, final int resolution, final Rectangle region,
            final int minZ, final int maxZ, final int minT,
            final int maxT, final int channel, final boolean directory,
            final boolean addToRecent, final boolean showProgress
    ) {
        load(null, paths, true, resolution, region, minZ, maxZ, minT, maxT, channel, directory, addToRecent, showProgress);
    }

    /**
     * Load the specified image file group (built using SequenceFileSticher class).<br>
     * The loading process is asynchronous.<br>
     * The resulting sequence is automatically displayed when the process complete.
     *
     * @param group        Sequence file group built using {@link SequenceFileSticher}.
     * @param resolution   Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *                     The retrieved image resolution is equal to
     *                     <code>image.resolution / (2^resolution)</code><br>
     *                     So for instance level 0 is the default/full image resolution while level 1 is base
     *                     image
     *                     resolution / 2 and so on...
     * @param region       The 2D region of the image we want to retrieve.<br>
     *                     If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ         the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole stack.
     * @param maxZ         the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole stack.
     * @param minT         the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole timelaps.
     * @param maxT         the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole timelaps.
     * @param channel      C position of the image (channel) we want retrieve (-1 means all channel).
     * @param directory    specify is the source is a single complete directory
     * @param addToRecent  If set to true the files list will be traced in recent opened sequence.
     * @param showProgress Show progression in loading process
     */
    public static void load(
            final SequenceFileGroup group, final int resolution, final Rectangle region,
            final int minZ, final int maxZ, final int minT,
            final int maxT, final int channel, final boolean directory,
            final boolean addToRecent, final boolean showProgress
    ) {
        // asynchronous call
        ThreadUtil.bgRun(() -> {
            final ApplicationMenuFile mainMenu;
            final FileFrame loadingFrame;

            if (addToRecent)
                mainMenu = ApplicationMenuFile.getInstance();
            else
                mainMenu = null;
            if (showProgress && !Icy.getMainInterface().isHeadLess())
                loadingFrame = new FileFrame("Loading", null);
            else
                loadingFrame = null;

            try {
                // load sequence from group with advanced options
                final Sequence sequence = internalLoadGroup(group, resolution, region, minZ, maxZ, minT, maxT, channel, false, directory, mainMenu,
                        loadingFrame);

                if (sequence != null) {
                    // load sequence XML data
                    if (GeneralPreferences.getSequencePersistence())
                        sequence.loadXMLData();
                    // and display it
                    Icy.getMainInterface().addSequence(sequence);
                }
            }
            catch (final Throwable t) {
                // just show the error
                IcyLogger.error(Loader.class, t, "Failed to open file(s).");
            }
            finally {
                if (loadingFrame != null)
                    loadingFrame.close();
            }
        });
    }

    /**
     * Load the specified image files with the given {@link SequenceFileImporter}.<br>
     * The loading process is asynchronous.<br>
     * If <i>separate</i> is false the loader try to set image in the same sequence.<br>
     * If <i>separate</i> is true each image is loaded in a separate sequence.<br>
     * The resulting sequences are automatically displayed when the process complete.
     *
     * @param importer     Importer used to open and load image files.<br>
     *                     If set to <code>null</code> the loader will search for a compatible importer and if
     *                     several importers match the user will have to select the appropriate one from a
     *                     selection dialog.
     * @param paths        list of image file to load
     * @param separate     Force image to be loaded in separate sequence
     * @param autoOrder    Try to order image in sequence from their filename
     * @param showProgress Show progression in loading process
     */
    public static void load(final SequenceFileImporter importer, final List<String> paths, final boolean separate, final boolean autoOrder, final boolean showProgress) {
        // asynchronous call
        ThreadUtil.bgRun(() -> {
            // load sequence
            final List<Sequence> sequences = loadSequences(importer, paths, -1, separate, autoOrder, true, showProgress);
            // and display them
            for (final Sequence seq : sequences)
                Icy.getMainInterface().addSequence(seq);
        });
    }

    /**
     * Load the specified files (asynchronous process) by using automatically the appropriate
     * {@link FileImporter} or {@link SequenceFileImporter}. If several importers match to open the
     * file the user will have to select the appropriate one from a selection dialog.<br>
     * <br>
     * If the specified files are image files:<br>
     * When <i>separate</i> is <code>false</code> the loader try to set image in the same
     * sequence.<br>
     * When <i>separate</i> is <code>true</code> each image is loaded in a separate sequence.<br>
     * The resulting sequences are automatically displayed when the process complete.
     *
     * @param paths         list of file to load
     * @param forceVolatile If set to <code>true</code> then image data is forced to volatile (see {@link IcyBufferedImage#isVolatile()}).<br>
     *                      Note that if you don't have enough memory to load the whole Sequence into memory then image data are always made volatile.
     * @param separate      Force image to be loaded in separate sequence (image files only)
     * @param autoOrder     Try to order image in sequence from their filename (image files only)
     * @param showProgress  Show progression in loading process
     */
    public static void load(final List<String> paths, final boolean forceVolatile, final boolean separate, final boolean autoOrder, final boolean showProgress) {
        // asynchronous call
        ThreadUtil.bgRun(() -> {
            // detect if this is a complete folder load
            final boolean directory = (paths.size() == 1) && new File(paths.get(0)).isDirectory();
            // explode path list
            final List<String> singlePaths = cleanNonImageFile(explode(paths));
            final boolean grouped = (singlePaths.size() > 1) && !separate;
            final Map<SequenceFileImporter, List<String>> sequenceFileImporters = getSequenceFileImporters(null, singlePaths, grouped, false);

            for (final Entry<SequenceFileImporter, List<String>> entry : sequenceFileImporters.entrySet()) {
                final SequenceFileImporter importer = entry.getKey();
                final List<String> currPaths = entry.getValue();
                final boolean dir = directory && (sequenceFileImporters.size() == 1) && (currPaths.size() == singlePaths.size());

                // load sequence
                final List<Sequence> sequences = loadSequences(importer, currPaths, -1, forceVolatile, separate, autoOrder, dir, true, showProgress);
                // and display them
                for (final Sequence seq : sequences)
                    Icy.getMainInterface().addSequence(seq);

                // remove loaded files
                singlePaths.removeAll(currPaths);
            }

            if (singlePaths.size() > 0) {
                // get the file importer now for remaining file
                final Map<FileImporter, List<String>> fileImporters = getFileImporters(singlePaths, false);

                for (final Entry<FileImporter, List<String>> entry : fileImporters.entrySet()) {
                    final FileImporter importer = entry.getKey();
                    final List<String> currPaths = entry.getValue();

                    // load files
                    loadFiles(importer, paths, true, showProgress);

                    // remove loaded files
                    singlePaths.removeAll(currPaths);
                }
            }

            // remaining files ?
            if (singlePaths.size() > 0) {
                // get first found importer for remaining files
                final Map<SequenceFileImporter, List<String>> importers = getSequenceFileImporters(singlePaths, true);

                // user canceled action for these paths so we remove them
                for (final List<String> values : importers.values())
                    singlePaths.removeAll(values);

                if (singlePaths.size() > 0) {
                    // just log in console
                    final ArrayList<String> messages = new ArrayList<>();
                    messages.add("No compatible importer found for the following files:");
                    messages.addAll(singlePaths);
                    IcyLogger.error(Loader.class, messages.toArray(new String[0]));
                }
            }
        });
    }

    /**
     * Load the specified files (asynchronous process) by using automatically the appropriate
     * {@link FileImporter} or {@link SequenceFileImporter}. If several importers match to open the
     * file the user will have to select the appropriate one from a selection dialog.<br>
     * <br>
     * If the specified files are image files:<br>
     * When <i>separate</i> is <code>false</code> the loader try to set image in the same
     * sequence.<br>
     * When <i>separate</i> is <code>true</code> each image is loaded in a separate sequence.<br>
     * The resulting sequences are automatically displayed when the process complete.
     *
     * @param paths        list of file to load
     * @param separate     Force image to be loaded in separate sequence (image files only)
     * @param autoOrder    Try to order image in sequence from their filename (image files only)
     * @param showProgress Show progression in loading process
     */
    public static void load(final List<String> paths, final boolean separate, final boolean autoOrder, final boolean showProgress) {
        load(paths, false, separate, autoOrder, showProgress);
    }

    /**
     * Load the specified file (asynchronous process) by using automatically the appropriate
     * {@link FileImporter} or
     * {@link SequenceFileImporter}. If several importers match to open the
     * file the user will have to select the appropriate one from a selection dialog.<br>
     * <br>
     * If the specified file is an image file, the resulting sequence is automatically displayed
     * when process complete.
     *
     * @param path         file to load
     * @param showProgress Show progression of loading process.
     */
    public static void load(final String path, final boolean showProgress) {
        load(CollectionUtil.createArrayList(path), false, false, showProgress);
    }

    /**
     * Loads the specified image file and return it as a Sequence, it can return <code>null</code>
     * if an error occured.<br>
     * As this method can take sometime, you should not call it from the EDT.
     *
     * @param importer      Importer used to load the image file.<br>
     *                      If set to <code>null</code> the loader will search for a compatible importer and if
     *                      several importers
     *                      match the user will have to select the appropriate one from a selection dialog if
     *                      <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *                      compatible importer will be automatically used.
     * @param path          image file to load
     * @param series        Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *                      -1 is a special value so it gives a chance to the user to select series to open from a
     *                      series selector dialog.
     * @param resolution    Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *                      The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
     *                      So for instance level 0 is the default/full image resolution while level 1 is base image resolution / 2
     *                      and so on...
     * @param region        The 2D region of the image we want to retrieve (in full image resolution).<br>
     *                      If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ          the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole stack.
     * @param maxZ          the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole stack.
     * @param minT          the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole timelaps.
     * @param maxT          the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole timelaps.
     * @param channel       C position of the image (channel) we want retrieve (-1 means all channel).
     * @param forceVolatile If set to <code>true</code> then image data is forced to volatile (see {@link IcyBufferedImage#isVolatile()}).<br>
     *                      Note that if you don't have enough memory to load the whole Sequence into memory then image data are always made volatile.
     * @param addToRecent   If set to true the files list will be traced in recent opened sequence.
     * @param showProgress  Show progression in loading process
     */
    public static Sequence loadSequence(final SequenceFileImporter importer, final String path, final int series, final int resolution, final Rectangle region, final int minZ, final int maxZ, final int minT, final int maxT, final int channel, final boolean forceVolatile, final boolean addToRecent, final boolean showProgress) {
        final ApplicationMenuFile mainMenu;
        final FileFrame loadingFrame;
        final Sequence result;

        if (addToRecent)
            mainMenu = ApplicationMenuFile.getInstance();
        else
            mainMenu = null;
        if (showProgress && !Icy.getMainInterface().isHeadLess())
            loadingFrame = new FileFrame("Loading", path);
        else
            loadingFrame = null;

        // importer is not specified ? --> get a compatible one.
        final SequenceFileImporter imp = (importer == null) ? getSequenceFileImporter(path, !showProgress) : importer;

        try {
            // open image
            imp.open(path, 0);

            // get metadata
            final OMEXMLMetadata meta = imp.getOMEXMLMetaData();
            // clean the metadata
            MetaDataUtil.clean(meta);

            // series selection
            int selectedSerie;

            // give the opportunity to select the series (single one) to open ?
            if (series == -1) {
                try {
                    // series selection (create a new importer instance as selectSerie(..) does async processes)
                    selectedSerie = selectSerie(cloneSequenceFileImporter(imp), path, meta, 0);
                }
                catch (final Throwable t) {
                    IcyLogger.error(Loader.class, t, "Opening first series by default...");
                    selectedSerie = 0;
                }

                // user cancelled action in the series selection ? null = cancel
                if (selectedSerie == -1)
                    return null;
            }
            else
                selectedSerie = series;

            // load the image
            result = internalLoadSingle(imp, meta, selectedSerie, resolution, region, minZ, maxZ, minT, maxT, channel, forceVolatile, loadingFrame);

            // Don't close importer on success ! we want to keep it inside the sequence.
            // We will close it when finalizing the sequence...
            // imp.close();

            // add as separate item to recent file list
            if (mainMenu != null)
                mainMenu.addRecentLoadedFile(new File(FileUtil.getGenericPath(path)));

            // set default colormaps if none defined
            SequenceUtil.setDefaultColormaps(result, false);

            // load sequence XML data
            if (GeneralPreferences.getSequencePersistence())
                result.loadXMLData();
        }
        catch (final Throwable t) {
            try {
                // close importer when error happen (not stored in Sequence so we need to close it manually)
                imp.close();
            }
            catch (final Exception e) {
                // ignore
            }

            // just show the error
            IcyLogger.error(Loader.class, t, "Failed to open file(s).");

            return null;
        }
        finally {
            if (loadingFrame != null)
                loadingFrame.close();
        }

        return result;
    }

    /**
     * Loads the specified image file and return it as a Sequence, it can return <code>null</code>
     * if an error occured.<br>
     * As this method can take sometime, you should not call it from the EDT.
     *
     * @param importer     Importer used to load the image file.<br>
     *                     If set to <code>null</code> the loader will search for a compatible importer and if
     *                     several importers
     *                     match the user will have to select the appropriate one from a selection dialog if
     *                     <code>showProgress</code> parameter is set to <code>true</code> otherwise the first
     *                     compatible importer will be automatically used.
     * @param path         image file to load
     * @param series       Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *                     -1 is a special value so it gives a chance to the user to select series to open from a
     *                     series selector dialog.
     * @param resolution   Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *                     The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
     *                     So for instance level 0 is the default/full image resolution while level 1 is base image resolution / 2
     *                     and so on...
     * @param region       The 2D region of the image we want to retrieve (in full image resolution).<br>
     *                     If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ         the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole stack.
     * @param maxZ         the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole stack.
     * @param minT         the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole timelaps.
     * @param maxT         the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole timelaps.
     * @param channel      C position of the image (channel) we want retrieve (-1 means all channel).
     * @param addToRecent  If set to true the files list will be traced in recent opened sequence.
     * @param showProgress Show progression in loading process
     */
    public static Sequence loadSequence(
            final SequenceFileImporter importer, final String path, final int series,
            final int resolution, final Rectangle region, final int minZ,
            final int maxZ, final int minT, final int maxT,
            final int channel, final boolean addToRecent, final boolean showProgress
    ) {
        return loadSequence(importer, path, series, resolution, region, minZ, maxZ, minT, maxT, channel, false, addToRecent, showProgress);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * The function try to guess image ordering from file name and metadata.<br>
     *
     * @param importer      Importer used to load the image files.<br>
     *                      If set to <code>null</code> the loader will take the first compatible importer found.
     * @param paths         list of file where to load image from
     * @param resolution    Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *                      The retrieved image resolution is equal to
     *                      <code>image.resolution / (2^resolution)</code><br>
     *                      So for instance level 0 is the default/full image resolution while level 1 is base
     *                      image
     *                      resolution / 2 and so on...
     * @param region        The 2D region of the image we want to retrieve.<br>
     *                      If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ          the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole stack.
     * @param maxZ          the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole stack.
     * @param minT          the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole timelaps.
     * @param maxT          the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole timelaps.
     * @param channel       C position of the image (channel) we want retrieve (-1 means all channel).
     * @param forceVolatile If set to <code>true</code> then image data is forced to volatile (see {@link IcyBufferedImage#isVolatile()}).<br>
     *                      Note that if you don't have enough memory to load the whole Sequence into memory then image data are always made volatile.
     * @param directory     specify is the source is a single complete directory
     * @param addToRecent   If set to true the files list will be traced in recent opened sequence.
     * @param showProgress  Show progression in loading process
     * @see #getSequenceFileImporter(String, boolean)
     */
    public static Sequence loadSequence(
            final SequenceFileImporter importer, final List<String> paths, final int resolution,
            final Rectangle region, final int minZ, final int maxZ,
            final int minT, final int maxT, final int channel,
            final boolean forceVolatile, final boolean directory, final boolean addToRecent,
            final boolean showProgress
    ) {
        final ApplicationMenuFile mainMenu;
        final FileFrame loadingFrame;
        Sequence result = null;

        if (addToRecent)
            mainMenu = ApplicationMenuFile.getInstance();
        else
            mainMenu = null;
        if (showProgress && !Icy.getMainInterface().isHeadLess())
            loadingFrame = new FileFrame("Loading", null);
        else
            loadingFrame = null;

        try {
            // we want only one group
            final SequenceFileGroup group = SequenceFileSticher.groupFiles(importer, cleanNonImageFile(paths), true, loadingFrame);
            // do group loading
            result = internalLoadGroup(group, resolution, region, minZ, maxZ, minT, maxT, channel, forceVolatile, directory, mainMenu, loadingFrame);
            // load sequence XML data
            if (GeneralPreferences.getSequencePersistence())
                result.loadXMLData();
        }
        catch (final Throwable t) {
            // just show the error
            IcyLogger.error(Loader.class, t, "Failed to open file(s).");
        }
        finally {
            if (loadingFrame != null)
                loadingFrame.close();
        }

        return result;
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * The function try to guess image ordering from file name and metadata.<br>
     *
     * @param paths         list of file where to load image from
     * @param resolution    Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *                      The retrieved image resolution is equal to
     *                      <code>image.resolution / (2^resolution)</code><br>
     *                      So for instance level 0 is the default/full image resolution while level 1 is base
     *                      image
     *                      resolution / 2 and so on...
     * @param region        The 2D region of the image we want to retrieve.<br>
     *                      If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ          the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole stack.
     * @param maxZ          the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole stack.
     * @param minT          the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole timelaps.
     * @param maxT          the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole timelaps.
     * @param channel       C position of the image (channel) we want retrieve (-1 means all channel).
     * @param forceVolatile If set to <code>true</code> then image data is forced to volatile (see {@link IcyBufferedImage#isVolatile()}).<br>
     *                      Note that if you don't have enough memory to load the whole Sequence into memory then image data are always made volatile.
     * @param directory     specify is the source is a single complete directory
     * @param addToRecent   If set to true the files list will be traced in recent opened sequence.
     * @param showProgress  Show progression in loading process
     * @see #getSequenceFileImporter(String, boolean)
     */
    public static Sequence loadSequence(
            final List<String> paths, final int resolution, final Rectangle region,
            final int minZ, final int maxZ, final int minT,
            final int maxT, final int channel, final boolean forceVolatile,
            final boolean directory, final boolean addToRecent, final boolean showProgress
    ) {
        return loadSequence(null, paths, resolution, region, minZ, maxZ, minT, maxT, channel, forceVolatile, directory, addToRecent, showProgress);
    }

    /**
     * Load a sequence from the specified list of file and returns it.<br>
     * The function try to guess image ordering from file name and metadata.<br>
     *
     * @param paths        list of file where to load image from
     * @param resolution   Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *                     The retrieved image resolution is equal to
     *                     <code>image.resolution / (2^resolution)</code><br>
     *                     So for instance level 0 is the default/full image resolution while level 1 is base
     *                     image
     *                     resolution / 2 and so on...
     * @param region       The 2D region of the image we want to retrieve.<br>
     *                     If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ         the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole stack.
     * @param maxZ         the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole stack.
     * @param minT         the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole timelaps.
     * @param maxT         the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *                     Set to -1 to retrieve the whole timelaps.
     * @param channel      C position of the image (channel) we want retrieve (-1 means all channel).
     * @param directory    specify is the source is a single complete directory
     * @param addToRecent  If set to true the files list will be traced in recent opened sequence.
     * @param showProgress Show progression in loading process
     * @see #getSequenceFileImporter(String, boolean)
     */
    public static Sequence loadSequence(
            final List<String> paths, final int resolution, final Rectangle region,
            final int minZ, final int maxZ, final int minT,
            final int maxT, final int channel, final boolean directory,
            final boolean addToRecent, final boolean showProgress
    ) {
        return loadSequence(null, paths, resolution, region, minZ, maxZ, minT, maxT, channel, false, directory, addToRecent, showProgress);
    }

    /**
     * Loads the specified image files and return them as list of sequence.<br>
     * If 'separate' is false the loader try to set images in the same sequence.<br>
     * If separate is true each image is loaded in a separate sequence.<br>
     * As this method can take sometime, you should not call it from the EDT.<br>
     *
     * @param importer      Importer used to open and load images (cannot be <code>null</code> here) except when separate is false
     * @param paths         list of image file to load
     * @param series        Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *                      -1 is a special value so it gives a chance to the user to select series to open from a
     *                      series selector dialog.
     * @param forceVolatile If set to <code>true</code> then image data is forced to volatile (see {@link IcyBufferedImage#isVolatile()}).<br>
     *                      Note that if you don't have enough memory to load the whole Sequence into memory then image data are always made volatile.
     * @param separate      Force image to be loaded in separate sequence (also disable stitching if possible)
     * @param autoOrder     If set to true then images are automatically orderer from their filename.
     * @param directory     Specify is the source is a single complete directory
     * @param addToRecent   If set to true the files list will be traced in recent opened sequence.
     * @param showProgress  Show progression in loading process
     */
    public static List<Sequence> loadSequences(
            final SequenceFileImporter importer, final List<String> paths, final int series,
            final boolean forceVolatile, final boolean separate, final boolean autoOrder,
            final boolean directory, final boolean addToRecent, final boolean showProgress
    ) {
        final List<Sequence> result = new ArrayList<>();

        // nothing to load
        if (paths.size() <= 0)
            return result;

        final ApplicationMenuFile mainMenu;
        final FileFrame loadingFrame;

        if (addToRecent)
            mainMenu = ApplicationMenuFile.getInstance();
        else
            mainMenu = null;
        if (showProgress && !Icy.getMainInterface().isHeadLess())
            loadingFrame = new FileFrame("Loading", null);
        else
            loadingFrame = null;

        final List<String> cleanedPaths = cleanNonImageFile(explode(paths));

        try {
            final List<String> remainingFiles = new ArrayList<>(cleanedPaths);

            // load each file in a separate sequence
            if (separate || (cleanedPaths.size() <= 1)) {
                if (loadingFrame != null) {
                    // each file can contains several image so we use 100 "inter-step"
                    loadingFrame.setLength(cleanedPaths.size() * 100d);
                    loadingFrame.setPosition(0d);
                }

                // force un-grouping when 'separate' is true
                if (separate)
                    importer.setGroupFiles(false);

                // load each file in a separate sequence
                for (final String path : cleanedPaths) {
                    // load the file (need to clone the importer so each sequence has its own importer)
                    final List<Sequence> sequences = internalLoadSingle(cloneSequenceFileImporter(importer), path, series, forceVolatile, !separate, loadingFrame);

                    // special case where loading was interrupted
                    if (sequences == null) {
                        // no error
                        remainingFiles.clear();
                        break;
                    }

                    if (sequences.size() > 0) {
                        // add sequences to result
                        result.addAll(sequences);
                        // remove path from remaining
                        remainingFiles.remove(path);
                        // add as separate item to recent file list
                        if (mainMenu != null)
                            mainMenu.addRecentLoadedFile(new File(FileUtil.getGenericPath(path)));
                    }

                    // interrupt loading
                    if ((loadingFrame != null) && loadingFrame.isCancelRequested()) {
                        // no error
                        remainingFiles.clear();
                        break;
                    }
                }
            }
            // grouped loading
            else {
                // get file group
                final Collection<SequenceFileGroup> groups = SequenceFileSticher.groupAllFiles(importer, cleanedPaths, autoOrder, loadingFrame);

                for (final SequenceFileGroup group : groups) {
                    final Sequence sequence = internalLoadGroup(group, forceVolatile, directory, mainMenu, loadingFrame);

                    // loading interrupted ?
                    if ((loadingFrame != null) && loadingFrame.isCancelRequested()) {
                        // no error
                        remainingFiles.clear();
                        break;
                    }

                    // remove all paths from the group in remaining list
                    for (final SequencePosition pos : group.positions)
                        remainingFiles.remove(pos.getPath());

                    // add to result
                    result.add(sequence);
                }
            }

            if (remainingFiles.size() > 0) {
                final ArrayList<String> messages = new ArrayList<>();
                messages.add("Cannot open the following file(s) (format not supported):");
                messages.addAll(remainingFiles);

                IcyLogger.error(Loader.class, messages.toArray(new String[0]));
            }

            // set default colormaps if none defined
            for (final Sequence sequence : result)
                SequenceUtil.setDefaultColormaps(sequence, false);

            // load sequence XML data
            if (GeneralPreferences.getSequencePersistence()) {
                for (final Sequence seq : result)
                    seq.loadXMLData();
            }
        }
        catch (final Throwable t) {
            // just show the error
            IcyLogger.error(Loader.class, t, "Failed to open file(s).");
        }
        finally {
            if (loadingFrame != null)
                loadingFrame.close();
        }

        return result;
    }

    /**
     * Concatenate the <i>src</i> sequence to the <i>dest</i> one.
     */
    static Sequence concatenateSequence(final Sequence dest, final Sequence src, final boolean onT, final boolean onZ) {
        if (dest == null)
            return src;
        if (src == null)
            return dest;

        final int dst;
        final int dsz;
        final int sst = src.getSizeT();
        final int ssz = src.getSizeZ();

        if (onT) {
            if (onZ) {
                dst = dest.getSizeT() - 1;
                dsz = dest.getSizeZ(dst);
            }
            else {
                dst = dest.getSizeT();
                dsz = 0;
            }
        }
        else {
            dst = 0;
            dsz = onZ ? dest.getSizeZ() : 0;
        }

        dest.beginUpdate();
        try {
            for (int t = 0; t < sst; t++)
                for (int z = 0; z < ssz; z++)
                    dest.setImage(t + dst, z + dsz, src.getImage(t, z));
        }
        finally {
            dest.endUpdate();
        }

        return dest;
    }

    @SuppressWarnings("unused")
    static void addSequences(final List<Sequence> result, final TreeMap<Integer, Sequence> map) {
        if (!map.isEmpty()) {
            // get all sequence from the map orderer by channel
            final Collection<Sequence> sequencesC = map.values();

            // remove update state
            for (final Sequence seq : sequencesC)
                if (seq.isUpdating())
                    seq.endUpdate();

            final Sequence[] sequences = sequencesC.toArray(new Sequence[0]);

            // several sequences ?
            if (sequences.length > 1) {
                // concatenate sequences on C dimension
                final Sequence merged = SequenceUtil.concatC(sequences);
                // better to keep name from first image
                merged.setName(sequences[0].getName());
                // then add the result to the list
                result.add(merged);
            }
            else
                result.add(sequences[0]);

            // better to not merge the C channel after all
            // result.addAll(sequencesC);

            // clear the map
            map.clear();
        }
    }

    /**
     * <b>Internal use only !</b><br>
     * Load image(s) from the given importer and parameters and return the result as a Sequence.<br>
     * If <i>loadingFrame</i> is not <code>null</code> then it has 100 steps allocated to the
     * loading of current path.
     *
     * @param importer      Opened importer used to load the image file (cannot be <code>null</code> here)
     * @param metadata      Metadata of the image
     * @param series        Series index to load (for multi series sequence), set to 0 if unsure (default).
     * @param resolution    Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *                      The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
     *                      So for instance level 0 is the default/full image resolution while level 1 is base image resolution / 2
     *                      and so on...
     * @param region        The 2D region of the image we want to retrieve (in full image resolution).<br>
     *                      If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ          the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole stack.
     * @param maxZ          the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole stack.
     * @param minT          the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole timelaps.
     * @param maxT          the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole timelaps.
     * @param channel       C position of the image (channel) we want retrieve (-1 means all channel).
     * @param forceVolatile If set to <code>true</code> then image data is forced to volatile (see {@link IcyBufferedImage#isVolatile()}).<br>
     *                      Note that if you don't have enough memory to load the whole Sequence into memory then image data are always made volatile.
     * @param loadingFrame  the loading frame used to display progress of the operation (can be null).<br>
     *                      Caller should allocate 100 positions for the internal single load process.
     * @return the Sequence object or <code>null</code>
     */
    public static Sequence internalLoadSingle(
            final SequenceIdImporter importer, final OMEXMLMetadata metadata, final int series,
            final int resolution, final Rectangle region, final int minZ,
            final int maxZ, final int minT, final int maxT,
            final int channel, final boolean forceVolatile, final FileFrame loadingFrame
    ) throws Exception, OutOfMemoryError {
        final int imgSizeX = MetaDataUtil.getSizeX(metadata, series);
        final int imgSizeY = MetaDataUtil.getSizeY(metadata, series);

        final Rectangle adjRegion;

        if (region != null)
            adjRegion = new Rectangle(0, 0, imgSizeX, imgSizeY).intersection(region);
        else
            adjRegion = null;

        final int sizeX = (adjRegion == null) ? imgSizeX : adjRegion.width;
        final int sizeY = (adjRegion == null) ? imgSizeY : adjRegion.height;
        final int sizeZ = MetaDataUtil.getSizeZ(metadata, series);
        final int sizeT = MetaDataUtil.getSizeT(metadata, series);
        final int sizeC = MetaDataUtil.getSizeC(metadata, series);
        final DataType dataType = MetaDataUtil.getDataType(metadata, series);

        final int adjMinZ, adjMaxZ;
        final int adjMinT, adjMaxT;

        if (minZ < 0)
            adjMinZ = 0;
        else
            adjMinZ = Math.min(minZ, sizeZ);
        if (maxZ < 0)
            adjMaxZ = sizeZ - 1;
        else
            adjMaxZ = Math.min(maxZ, sizeZ - 1);
        if (minT < 0)
            adjMinT = 0;
        else
            adjMinT = Math.min(minT, sizeT);
        if (maxT < 0)
            adjMaxT = sizeT - 1;
        else
            adjMaxT = Math.min(maxT, sizeT - 1);

        // we want volatile image
        boolean volatileImage = (forceVolatile && ImageCache.isInit()) || GeneralPreferences.getVirtualMode();

        try {
            // volatile image ? --> we just need to check the plane size
            if (volatileImage)
                checkOpeningPlane(resolution, sizeX, sizeY, " Try to open a sub resolution or sub part of the image only.");
            else
                // check that can open the image
                checkOpening(resolution, sizeX, sizeY, (channel == -1) ? sizeC : 1, (adjMaxZ - adjMinZ) + 1, (adjMaxT - adjMinT) + 1, dataType,
                        " Try to open a sub resolution or sub part of the image only.");
        }
        catch (final OutOfMemoryError e) {
            if (!ImageCache.isInit())
                throw new OutOfMemoryError("Not enough memory to load the dataset, enable the Virtual Mode and retry.");

            // force volatile image if we don't have enough memory to open the image
            volatileImage = true;
        }

        // create result sequence with desired series metadata
        final Sequence result = new Sequence(OMEUtil.createOMEXMLMetadata(metadata, series));

        // setup sequence properties and metadata from the opening setting
        setupSequence(result, importer, MetaDataUtil.getNumSeries(metadata) > 1, series, adjRegion, resolution, sizeZ, sizeT, sizeC, adjMinZ, adjMaxZ, adjMinT,
                adjMaxT, channel);

        // number of image to process
        final int numImage = ((adjMaxZ - adjMinZ) + 1) * ((adjMaxT - adjMinT) + 1);

        if (numImage > 0) {
            // set local length for loader frame
            final double progressStep = 100d / numImage;
            double progress = 0d;
            boolean first = true;
            final int resDivisor = Math.max(1, (int) Math.pow(2, resolution));
            final int adjSizeX = sizeX / resDivisor;
            final int adjSizeY = sizeY / resDivisor;

            if (loadingFrame != null)
                progress = loadingFrame.getPosition();

            result.beginUpdate();
            try {
                for (int t = adjMinT; t <= adjMaxT; t++) {
                    for (int z = adjMinZ; z <= adjMaxZ; z++) {
                        if (loadingFrame != null) {
                            // cancel requested ? --> stop loading here...
                            if (loadingFrame.isCancelRequested())
                                return result;

                            // special group importer ? --> use internal file path
                            if (importer instanceof SequenceFileGroupImporter)
                                loadingFrame.setFilename(((SequenceFileGroupImporter) importer).getPath(z, t, (channel != -1) ? channel : 0));
                        }

                        final IcyBufferedImage image;

                        // need to load one image at least to get the colormap information (stored in image colormodel)
                        if (first) {
                            // load image now
                            if (channel == -1)
                                image = importer.getImage(series, resolution, adjRegion, z, t);
                            else
                                image = importer.getImage(series, resolution, adjRegion, z, t, channel);

                            // don't forget to set volatile state
                            image.setVolatile(volatileImage);
                            // first image has been loaded now
                            first = false;
                        }
                        else
                            // use empty image for now (lazy loading)
                            image = new IcyBufferedImage(adjSizeX, adjSizeY, sizeC, dataType, volatileImage);

                        // set image source information for delayed image data loading
                        image.setImageSourceInfo(importer, series, resolution, adjRegion, t, z, channel);
                        // set image into the sequence
                        result.setImage(t - adjMinT, z - adjMinZ, image);

                        progress += progressStep;

                        // notify progress to loader frame
                        if (loadingFrame != null)
                            loadingFrame.setPosition(progress);
                    }
                }
            }
            finally {
                result.endUpdate();
            }
        }

        return result;
    }

    /**
     * <b>Internal use only !</b><br>
     * Load a single file and return result as Sequence list (for multi series).<br>
     * If <i>loadingFrame</i> is not <code>null</code> then it has 100 steps allocated to the
     * loading of current path.
     *
     * @param importer      Importer to use to open and load images (cannot be <code>null</code> here)
     * @param path          image file to load
     * @param series        Series index to load (for multi series sequence), set to 0 if unsure (default).<br>
     *                      -1 is a special value so it gives a chance to the user to select series to open from a
     *                      series selector dialog.
     * @param forceVolatile If set to <code>true</code> then image data is forced to volatile (see {@link IcyBufferedImage#isVolatile()}).<br>
     *                      Note that if you don't have enough memory to load the whole Sequence into memory then image data are always made volatile.
     * @param groupSeries   Enable series grouping into the same sequence (appended in T dimension), only meaningful if <code>series</code> = -1
     * @param loadingFrame  the loading frame used to display progress of the operation (can be null)
     */
    public static List<Sequence> internalLoadSingle(
            final SequenceFileImporter importer, final String path, final int series,
            final boolean forceVolatile, final boolean groupSeries, final FileFrame loadingFrame
    ) throws Exception, OutOfMemoryError {
        final double endStep;

        if (loadingFrame != null) {
            loadingFrame.setFilename(path);
            // 100 step reserved to load this image
            endStep = loadingFrame.getPosition() + 100d;
        }
        else
            endStep = 0d;

        final List<Sequence> result = new ArrayList<>();

        try {
            // prepare image loading for this file
            if (!importer.open(path, 0))
                throw new UnsupportedFormatException("Image file '" + path + "' is not supported by " + importer + " importer.");

            // get metadata
            final OMEXMLMetadata meta = importer.getOMEXMLMetaData();
            // clean the metadata
            MetaDataUtil.clean(meta);

            // series selection
            int[] selectedSeries;

            // give the opportunity to select the series(s) to open ?
            if (series == -1) {
                try {
                    // try to group series
                    if (groupSeries)
                        selectedSeries = groupSeries(meta);
                        // series selection (create a new importer instance as selectSerie(..) does async processes)
                    else
                        selectedSeries = selectSeries(cloneSequenceFileImporter(importer), path, meta, 0, false);
                }
                catch (final Throwable t) {
                    IcyLogger.error(Loader.class, t, "Opening first series by default...");
                    selectedSeries = new int[]{0};
                }

                // user cancelled action in the series selection ? null = cancel
                if (selectedSeries.length == 0)
                    return null;
            }
            else
                selectedSeries = new int[]{series};

            // add sequence to result
            for (final int s : selectedSeries) {
                final SequenceFileImporter imp;

                // opening several series ?
                if ((selectedSeries.length > 1) && !result.isEmpty()) {
                    try {
                        // duplicate the importer so each Sequence has its own importer instance
                        // otherwise closing a Sequence will close importer for everyone
                        imp = cloneAndOpenSequenceFileImporter(importer);
                    }
                    catch (final IOException | UnsupportedFormatException e) {
                        throw e;
                    }
                    catch (final Throwable t) {
                        IcyLogger.error(Loader.class, t, "Couln't open, ignoring this series...");
                        continue;
                    }
                }
                else
                    imp = importer;

                final Sequence seq = internalLoadSingle(imp, meta, s, 0, null, -1, -1, -1, -1, -1, forceVolatile, loadingFrame);

                // group series together
                if ((result.size() > 0) && groupSeries)
                    concatenateSequence(result.get(0), seq, true, false);
                else
                    // just add to list
                    result.add(seq);
            }

            // grouped series ?
            if (groupSeries && (selectedSeries.length > 1)) {
                final Sequence seq = result.get(0);
                final String imageName = MetaDataUtil.getName(meta, 0);

                // reset to metadata name
                if (StringUtil.isEmpty(imageName))
                    seq.setName(Sequence.DEFAULT_NAME + StringUtil.toString(seq.getId(), 3));
                else
                    seq.setName(imageName);
            }

            // Don't close importer on success ! we want to keep it inside the sequence.
            // We will close it when finalizing the sequence...
            // importer.close();
        }
        catch (final Throwable t) {
            // close importer when error happen (not stored in Sequence so we need to close it manually)
            importer.close();

            // the importer is supposed to support this file --> re throw the exception
            if (importer.acceptFile(path))
                throw t;
        }
        finally {
            if (loadingFrame != null)
                loadingFrame.setPosition(endStep);
        }

        return result;
    }

    /**
     * <b>Internal use only !</b><br>
     * Load the specified image file group and return a single Sequence from it.<br>
     * As this method can take sometime, you should not call it from the EDT.<br>
     *
     * @param group         image path group we want to load
     * @param resolution    Wanted resolution level for the image (use 0 if unsure), useful for large image<br>
     *                      The retrieved image resolution is equal to <code>image.resolution / (2^resolution)</code><br>
     *                      So for instance level 0 is the default/full image resolution while level 1 is base image resolution / 2
     *                      and so on...
     * @param region        The 2D region of the image we want to retrieve (in full image resolution).<br>
     *                      If set to <code>null</code> then the whole XY plane of the image is returned.
     * @param minZ          the minimum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole stack.
     * @param maxZ          the maximum Z position of the image (slice) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole stack.
     * @param minT          the minimum T position of the image (frame) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole timelaps.
     * @param maxT          the maximum T position of the image (frame) we want retrieve (inclusive).<br>
     *                      Set to -1 to retrieve the whole timelaps.
     * @param channel       C position of the image (channel) we want retrieve (-1 means all channel).
     * @param forceVolatile If set to <code>true</code> then image data is forced to volatile (see {@link IcyBufferedImage#isVolatile()}).<br>
     *                      Note that if you don't have enough memory to load the whole Sequence into memory then image data are always made volatile.
     * @param directory     specify is the source is a single complete directory
     * @param mainMenu      menu object used to store recent file (can be null)
     * @param loadingFrame  the loading frame used to cancel / display progress of the operation (can be null)
     * @return the loaded Sequence (or <code>null<code> if loading was canceled)
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static Sequence internalLoadGroup(
            final SequenceFileGroup group, final int resolution, final Rectangle region,
            final int minZ, final int maxZ, final int minT,
            final int maxT, final int channel, final boolean forceVolatile,
            final boolean directory, final ApplicationMenuFile mainMenu, final FileFrame loadingFrame
    ) throws OutOfMemoryError, Exception {
        final double endStep;
        Sequence result;

        if (loadingFrame != null) {
            loadingFrame.setFilename(group.ident.base);
            loadingFrame.setAction("Loading image group");
            // 100 step reserved to load this image
            endStep = loadingFrame.getPosition() + 100d;
        }
        else
            endStep = 0d;

        // use the special group importer
        final SequenceFileGroupImporter groupImporter = new SequenceFileGroupImporter();

        try {
            // open image group (can't fail)
            groupImporter.open(group, 0);

            // get metadata
            final OMEXMLMetadata meta = groupImporter.getOMEXMLMetaData();
            // clean the metadata
            MetaDataUtil.clean(meta);

            result = internalLoadSingle(groupImporter, meta, 0, resolution, region, minZ, maxZ, minT, maxT, channel, forceVolatile, loadingFrame);

            // directory load ?
            if (directory) {
                // get directory without last separator
                final String fileDir = FileUtil.getDirectory(group.getBasePath(), false);

                // set sequence name and filename to directory
                result.setName(FileUtil.getFileName(fileDir, false));
                result.setFilename(fileDir);

                // add as one item to recent file list
                if (mainMenu != null)
                    mainMenu.addRecentFile(fileDir);
            }
            // normal file loading
            else {
                if (mainMenu != null)
                    mainMenu.addRecentFile(group.getPaths());
            }

            // Don't close importer on success ! we want to keep it inside the sequence.
            // We will close it when finalizing the sequence...
            // groupImporter.close();
        }
        catch (final Throwable t) {
            // close importer when error happen (not stored in Sequence so we need to close it manually)
            groupImporter.close();

            // re throw
            throw t;
        }
        finally {
            if (loadingFrame != null)
                loadingFrame.setPosition(endStep);
        }

        return result;
    }

    /**
     * <b>Internal use only !</b><br>
     * Load the specified image file group and return a single Sequence from it.<br>
     * As this method can take sometime, you should not call it from the EDT.<br>
     *
     * @param group         image path group we want to load
     * @param directory     specify is the source is a single complete directory
     * @param mainMenu      menu object used to store recent file (can be null)
     * @param forceVolatile If set to <code>true</code> then image data is forced to volatile (see {@link IcyBufferedImage#isVolatile()}).<br>
     *                      Note that if you don't have enough memory to load the whole Sequence into memory then image data are always made volatile.
     * @param loadingFrame  the loading frame used to cancel / display progress of the operation (can be null)
     * @return the loaded Sequence (or <code>null<code> if loading was canceled)
     */
    public static Sequence internalLoadGroup(final SequenceFileGroup group, final boolean forceVolatile, final boolean directory, final ApplicationMenuFile mainMenu, final FileFrame loadingFrame) throws Exception {
        return internalLoadGroup(group, 0, null, -1, -1, -1, -1, -1, forceVolatile, directory, mainMenu, loadingFrame);
    }

    public static String getSequenceName(final Sequence sequence, final String path, final boolean multiSerie, final int series) {
        // default name
        String name = FileUtil.getFileName(path, false);

        // default name used --> use better name
        if (sequence.isDefaultName()) {
            // multi series image --> add series info
            if (multiSerie)
                name += " - series " + StringUtil.toString(series);
        }
        else {
            // multi series image --> adjust name to keep file name info
            if (multiSerie)
                name += " - " + sequence.getName();
            else
                // just use sequence metadata name
                name = sequence.getName();
        }

        return name;
    }

    /**
     * Setup the specified sequence object given the different opening informations
     *
     * @param sequence   sequence to adjust properties
     * @param importer   image path
     * @param multiSerie <code>true</code> if this Sequence comes from a multi series dataset
     * @param series     series index
     * @param region     Rectangle region we want to load from original image (full resolution)
     * @param resolution Resolution level to open
     * @param sizeZ      original image sizeZ
     * @param sizeT      original image sizeT
     * @param sizeC      original image sizeC
     * @param minZ       minimum Z slice wanted
     * @param maxZ       maximum Z slice wanted
     * @param minT       minimum T frame wanted
     * @param maxT       maximum T frame wanted
     * @param channel    channel we want to load (-1 for all)
     */
    public static void setupSequence(
            final Sequence sequence, final SequenceIdImporter importer, final boolean multiSerie,
            final int series, final Rectangle region, final int resolution,
            final int sizeZ, final int sizeT, final int sizeC,
            final int minZ, final int maxZ, final int minT,
            final int maxT, final int channel
    ) {
        final String path = FileUtil.getGenericPath(importer.getOpened());
        // get default name
        String name = getSequenceName(sequence, path, multiSerie, series);

        // original pixel size
        final double psx = sequence.getPixelSizeX();
        final double psy = sequence.getPixelSizeY();
        final double psz = sequence.getPixelSizeZ();
        // original position
        final double posX = sequence.getPositionX();
        final double posY = sequence.getPositionY();
        final double posZ = sequence.getPositionZ();
        // original time stamp (in ms)
        final long posT = sequence.getPositionT();

        // get sequence metadata
        final OMEXMLMetadata metadata = sequence.getOMEXMLMetadata();

        // cleanup planes
        for (int t = sizeT - 1; t >= 0; t--) {
            for (int z = sizeZ - 1; z >= 0; z--) {
                for (int c = 0; c < sizeC; c++) {
                    if ((t < minT) || (t > maxT) || (z < minZ) || (z > maxZ))
                        MetaDataUtil.removePlane(metadata, 0, maxT, maxZ, c);
                }
            }
        }

        // single channel extraction ?
        if (channel != -1) {
            // adjust origin channel
            sequence.setOriginChannel(channel);

            // clean channels and remaining planes
            for (int c = 0; c < sizeC; c++) {
                if (c != channel) {
                    MetaDataUtil.removePlanes(metadata, 0, -1, -1, c);
                    MetaDataUtil.removeChannel(metadata, 0, c);
                }
            }
        }

        // adjust position X,Y
        if (region != null) {
            // set origin region
            sequence.setOriginXYRegion(region);
            // adjust position
            sequence.setPositionX(posX + (region.x * psx));
            sequence.setPositionY(posY + (region.y * psy));
        }
        // adjust position Z
        if (minZ > 0)
            sequence.setPositionZ(posZ + (minZ * psz));
        // adjust position T
        if (minT > 0) {
            final double startTOffset = sequence.getPositionTOffset(0, 0, 0);
            final double curTOffset = sequence.getPositionTOffset(minT, minZ, Math.max(0, channel));

            sequence.setTimeStamp(posT + (long) ((curTOffset - startTOffset) * 1000d));
        }

        // using sub resolution ?
        if (resolution > 0) {
            final int divider = (int) Math.pow(2, resolution);

            // adjust origin resolution
            sequence.setOriginResolution(resolution);
            // adjust pixel size
            sequence.setPixelSizeX(psx * divider);
            sequence.setPixelSizeY(psy * divider);

            // adjust name
            name += " - binning=" + StringUtil.toString(resolution);
        }

        // adjust Z Range
        if ((minZ > 0) || (maxZ < (sizeZ - 1))) {
            sequence.setOriginZMin(minZ);
            sequence.setOriginZMax(maxZ);

            // adjust name
            if (minZ == maxZ)
                name += " - Z" + StringUtil.toString(minZ);
            else
                name += " - Z[" + StringUtil.toString(minZ) + "-" + StringUtil.toString(maxZ) + "]";
        }
        // adjust T Range
        if ((minT > 0) || (maxT < (sizeT - 1))) {
            sequence.setOriginTMin(minT);
            sequence.setOriginTMax(maxT);

            // adjust name
            if (minT == maxT)
                name += " - T" + StringUtil.toString(minT);
            else
                name += " - T[" + StringUtil.toString(minT) + "-" + StringUtil.toString(maxT) + "]";
        }

        // need to adjust name for channel ?
        if (channel != -1)
            name += " - C" + StringUtil.toString(channel);

        // set final name and filename
        sequence.setName(name);
        sequence.setFilename(path);

        // set importer (for caching / delayed loading...)
        sequence.setImageProvider(importer);
    }

    /**
     * Display the Series Selection frame for the given image and returns selected series(s).<br>
     * Returns a 0 length array if user canceled series selection.
     */
    public static int[] selectSeries(
            final SequenceIdImporter importer, final String path, final OMEXMLMetadata meta,
            final int defaultSerie, final boolean singleSelection
    ) throws UnsupportedFormatException, IOException {
        final int serieCount = MetaDataUtil.getNumSeries(meta);
        final int[] tmp = new int[serieCount + 1];

        if (serieCount > 0) {
            tmp[0] = 1;

            // multi series, display selection dialog
            if (serieCount > 1) {
                // allow user to select series to open
                if (!Icy.getMainInterface().isHeadLess()) {
                    final Exception[] exception = new Exception[1];
                    exception[0] = null;

                    // use invokeNow carefully !
                    ThreadUtil.invokeNow(() -> {
                        try {
                            final int[] series = new SeriesSelectionDialog(importer, path, meta, singleSelection).getSelectedSeries();
                            // get result
                            tmp[0] = series.length;
                            System.arraycopy(series, 0, tmp, 1, series.length);
                        }
                        catch (final Exception e) {
                            exception[0] = e;
                        }
                    });

                    // propagate exception
                    if (exception[0] instanceof UnsupportedFormatException)
                        throw (UnsupportedFormatException) exception[0];
                    else if (exception[0] instanceof IOException)
                        throw (IOException) exception[0];
                }
                // use the pre selected series
                else
                    tmp[1] = defaultSerie;
            }
            // only 1 series so open it
            else
                tmp[1] = 0;
        }

        // copy back result to adjusted array
        final int[] result = new int[tmp[0]];

        System.arraycopy(tmp, 1, result, 0, result.length);

        return result;
    }

    /**
     * Try to group series with similar images properties (XYZC dimension) starting from first image and return the list of grouped series index.
     */
    public static int[] groupSeries(final OMEXMLMetadata meta) {
        final List<Integer> result = new ArrayList<>();

        final int sizeS = MetaDataUtil.getNumSeries(meta);
        if (sizeS > 0) {
            final int sizeX = MetaDataUtil.getSizeX(meta, 0);
            final int sizeY = MetaDataUtil.getSizeY(meta, 0);
            final int sizeZ = MetaDataUtil.getSizeZ(meta, 0);
            final int sizeC = MetaDataUtil.getSizeC(meta, 0);
            final DataType dataType = MetaDataUtil.getDataType(meta, 0);

            result.add(Integer.valueOf(0));

            // we can group series only if size T == 1 (as we group on T dimension)
            if (MetaDataUtil.getSizeT(meta, 0) == 1) {
                for (int s = 1; s < sizeS; s++) {
                    final int sx = MetaDataUtil.getSizeX(meta, s);
                    final int sy = MetaDataUtil.getSizeY(meta, s);
                    final int sz = MetaDataUtil.getSizeZ(meta, s);
                    final int sc = MetaDataUtil.getSizeC(meta, s);
                    final DataType dt = MetaDataUtil.getDataType(meta, s);

                    if ((sx == sizeX) && (sy == sizeY) && (sz == sizeZ) && (sc == sizeC) && (dt == dataType) && (MetaDataUtil.getSizeT(meta, s) == 1))
                        result.add(Integer.valueOf(s));
                }
            }
        }

        final int[] ires = new int[result.size()];

        for (int i = 0; i < ires.length; i++)
            ires[i] = result.get(i).intValue();

        return ires;
    }

    /**
     * Display the Series Selection frame for the given image and returns selected series(s).<br>
     * Returns a 0 length array if user canceled series selection.
     */
    public static int[] selectSeries(final SequenceFileImporter importer, final String path, final OMEXMLMetadata meta, final int defaultSerie, final boolean singleSelection) throws UnsupportedFormatException, IOException {
        return selectSeries((SequenceIdImporter) importer, path, meta, defaultSerie, singleSelection);
    }

    /**
     * Display the Series Selection frame for the given image and returns selected series(s).<br>
     * Returns a 0 length array if user canceled series selection.
     */
    public static int[] selectSeries(final SequenceFileImporter importer, final String path, final OMEXMLMetadataImpl meta, final int defaultSerie, final boolean singleSelection) throws UnsupportedFormatException, IOException {
        return selectSeries(importer, path, (OMEXMLMetadata) meta, defaultSerie, singleSelection);
    }

    /**
     * Display the Series Selection frame for the given image and return the selected series (single selection).<br>
     * Returns <code>-1</code> if user canceled series selection.
     */
    public static int selectSerie(final SequenceFileImporter importer, final String path, final OMEXMLMetadata meta, final int defaultSerie) throws UnsupportedFormatException, IOException {
        final int[] selected = selectSeries(importer, path, meta, defaultSerie, true);

        if (selected.length > 0)
            return selected[0];

        return -1;
    }

    public static List<String> explode(final List<String> paths) {
        return FileUtil.toPaths(FileUtil.explode(FileUtil.toFiles(paths), null, true, false));
    }

    /**
     * Remove invalid image files from the list of files
     */
    public static List<String> cleanNonImageFile(final List<String> paths) {
        final List<String> result = new ArrayList<>();

        // extensions based exclusion
        for (final String path : paths) {
            // no image file or XML persistence --> ignore
            if (canDiscardImageFile(path))
                continue;

            // XML file ?
            if (FileUtil.getFileExtension(path, false).equalsIgnoreCase(XMLUtil.FILE_EXTENSION)) {
                // ignore persistence files
                if (SequencePersistent.isValidXMLPersitence(path))
                    continue;
            }

            result.add(path);
        }

        return result;
    }
}
