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

package fr.icy.extension.plugin;

import fr.icy.common.Version;
import fr.icy.common.reflect.ClassUtil;
import fr.icy.extension.ExtensionDescriptor;
import fr.icy.extension.plugin.abstract_.Plugin;
import fr.icy.extension.plugin.abstract_.PluginActionable;
import fr.icy.extension.plugin.annotation_.IcyPluginDescription;
import fr.icy.extension.plugin.annotation_.IcyPluginIcon;
import fr.icy.extension.plugin.annotation_.IcyPluginName;
import fr.icy.extension.plugin.interface_.PluginDaemon;
import fr.icy.gui.component.icon.IcySVG;
import fr.icy.io.FileUtil;
import fr.icy.io.jar.JarUtil;
import fr.icy.shared.logging.CustomLevel;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * <br>
 * The plugin descriptor contains all the data needed to launch a plugin. <br>
 *
 * @author Fabrice de Chaumont
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 * @see PluginLauncher
 */
public final class PluginDescriptor {
    private static final Logger LOGGER = Logger.getLogger(PluginDescriptor.class.getName());

    public static final int ICON_SIZE = 32;

    private final Class<? extends Plugin> pluginClass;
    private final ExtensionDescriptor extension;

    private IcySVG svg;

    private final String name;
    private final String shortDescription;
    private final String svgPath;
    private final String desc;

    private boolean enabled;
    private boolean descriptorLoaded;
    private boolean iconLoaded;
    private boolean iconMono;

    public PluginDescriptor(final @NonNull Class<? extends Plugin> clazz, final @NonNull ExtensionDescriptor extension) {
        this.pluginClass = clazz;
        this.extension = extension;

        svg = IcySVG.EXTENSION_DEFAULT;

        enabled = true;
        descriptorLoaded = false;
        iconLoaded = true;
        iconMono = false;

        String magicName = "";
        String magicDescription = "";
        String magicIcon = "";
        boolean magicIconMono = false;

        if (clazz.isAnnotationPresent(IcyPluginName.class)) {
            final IcyPluginName annotation = clazz.getAnnotation(IcyPluginName.class);
            magicName = annotation.value();
        }
        if (clazz.isAnnotationPresent(IcyPluginDescription.class)) {
            final IcyPluginDescription annotation = clazz.getAnnotation(IcyPluginDescription.class);
            magicDescription = annotation.value();
        }
        if (clazz.isAnnotationPresent(IcyPluginIcon.class)) {
            final IcyPluginIcon annotation = clazz.getAnnotation(IcyPluginIcon.class);
            magicIcon = annotation.value();
            magicIconMono = annotation.monochrome();
        }

        // load icon
        if (!magicIcon.isBlank())
            svgPath = magicIcon;
        else
            svgPath = "";

        if (!magicName.isBlank())
            name = magicName;
        else
            name = pluginClass.getSimpleName();

        desc = name + " plugin";

        if (!magicDescription.isBlank())
            shortDescription = magicDescription;
        else
            shortDescription = "";

        // only descriptor is loaded here
        descriptorLoaded = true;
        iconLoaded = false;
        iconMono = magicIconMono;
    }

    public boolean loadSVG() {
        if (iconLoaded)
            return true;

        // just to avoid retry indefinitely if it fails
        iconLoaded = true;

        // load icon
        return loadSVGIcon();
    }

    /**
     * Load descriptor and images if not already done
     */
    public boolean loadAll() {
        return /*loadChangeLog() &*/ loadSVG();
    }

    /**
     * Check if the plugin class is an instance of (or subclass of) the specified class.
     */
    @Contract(pure = true)
    public boolean isInstanceOf(final Class<?> baseClazz) {
        return ClassUtil.isSubClass(pluginClass, baseClazz);
    }

    /**
     * Check if the plugin class is an instance of (or subclass of) the specified class.
     */
    @Contract(pure = true)
    public boolean isAnnotated(final Class<? extends Annotation> annotation) {
        return pluginClass.isAnnotationPresent(annotation);
    }

    /**
     * Return true if the plugin class is abstract
     */
    public boolean isAbstract() {
        return ClassUtil.isAbstract(pluginClass);
    }

    /**
     * Return true if the plugin class is private
     */
    public boolean isPrivate() {
        return ClassUtil.isPrivate(pluginClass);
    }

    /**
     * Return true if the plugin class is an interface
     */
    @Contract(pure = true)
    public boolean isInterface() {
        return pluginClass.isInterface();
    }

    /**
     * return true if the plugin has an action which can be started from the menu
     */
    public boolean isActionable() {
        return !isPrivate() && !isAbstract() && !isInterface() && isInstanceOf(PluginActionable.class);
    }

    public boolean isDaemon() {
        return isDaemon(false, false);
    }

    public boolean isDaemon(final boolean acceptAbstract, final boolean acceptInterface) {
        if (isInstanceOf(PluginDaemon.class)) {
            if (!acceptAbstract && isAbstract())
                return false;

            return acceptInterface || !isInterface();
        }
        else
            return false;
    }

    public boolean isNotRelease() {
        return getVersion().isNotRelease();
    }

    /**
     * Return true if this plugin is a system application plugin (declared in plugins.plugins.kernel
     * package).
     */
    public boolean isKernelPlugin() {
        return extension.isKernel();
    }

    boolean loadSVGIcon() {
        if (svgPath.isBlank())
            svg = extension.getSVG();
        else {
            if (LOGGER.isLoggable(CustomLevel.DEBUG))
                LOGGER.log(CustomLevel.DEBUG, "Loading SVG : " + svgPath + " (mono = " + iconMono + ")");
            svg = Objects.requireNonNullElseGet(extension.getSVG(svgPath, iconMono), extension::getSVG);
        }

        return true;
    }

    /**
     * Returns the plugin class name.<br>
     * Ex: "plugins.tutorial.Example1"
     */
    @Contract(pure = true)
    public @NonNull String getClassName() {
        return pluginClass.getName();
    }

    public @NonNull String getSimpleClassName() {
        return ClassUtil.getSimpleClassName(pluginClass.getName());
    }

    /**
     * Returns the package name of the plugin class.
     */
    public @NonNull String getPackageName() {
        return ClassUtil.getPackageName(pluginClass.getName());
    }

    /**
     * Returns the minimum package name (remove "icy" or/and "plugin" header)<br>
     */
    public @NonNull String getSimplePackageName() {
        String result = getPackageName();

        if (result.startsWith("icy."))
            result = result.substring("icy.".length());
        else if (result.startsWith("fr.icy."))
            result = result.substring("fr.icy.".length());
        if (result.startsWith("plugins."))
            result = result.substring("plugins.".length());

        return result;
    }

    @Contract(pure = true)
    public ExtensionDescriptor getExtension() {
        return extension;
    }

    /**
     * @return the pluginClass
     */
    @Contract(pure = true)
    public Class<? extends Plugin> getPluginClass() {
        return pluginClass;
    }

    /**
     * @return the JAR file hosting this plugin (returns <code>null</code> if the plugin is not installed).<br>
     */
    public @Nullable String getPluginJarPath() {
        if (pluginClass != null)
            return ClassUtil.getJarPath(pluginClass);

        return null;
    }

    /**
     * return the associated filename
     */
    public @NonNull String getFilename() {
        return ClassUtil.getPathFromQualifiedName(getClassName());
    }

    /**
     * return jar filename
     */
    public @NonNull String getJarFilename() {
        return getFilename() + JarUtil.FILE_DOT_EXTENSION;
    }

    public @NonNull IcySVG getSVG() {
        loadSVG();
        return svg;
    }

    /**
     * @return the name
     */
    @Contract(pure = true)
    public String getName() {
        return name;
    }

    /**
     * @return the short description.
     */
    @Contract(pure = true)
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * @return the version
     */
    @Contract(pure = true)
    public @NonNull Version getVersion() {
        return extension.getVersion();
    }

    /**
     * @return the description
     */
    @Contract(pure = true)
    public String getDescription() {
        return desc;
    }

    /**
     * @return the requiredKernelVersion
     */
    @Contract(pure = true)
    public @NonNull Version getRequiredKernelVersion() {
        return extension.getKernelVersion();
    }

    /**
     * Returns true if the descriptor is loaded.
     */
    @Contract(pure = true)
    public boolean isDescriptorLoaded() {
        return descriptorLoaded;
    }

    /**
     * Returns true if the icon is loaded.
     */
    @Contract(pure = true)
    public boolean isIconLoaded() {
        return iconLoaded;
    }

    /**
     * Returns true if image and icon are loaded.
     */
    @Deprecated(forRemoval = true, since = "3.0.0-a.8")
    @Contract(pure = true)
    public boolean isImagesLoaded() {
        return iconLoaded; // && imageLoaded;
    }

    /**
     * Returns true if both descriptor and images are loaded.
     */
    @Contract(pure = true)
    public boolean isAllLoaded() {
        return descriptorLoaded /*&& changeLogLoaded*/ && iconLoaded;
    }

    /**
     * @return the enabled
     */
    @Contract(pure = true)
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    @Contract(mutates = "this")
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Return true if the plugin is installed (the corresponding JAR file exists)
     */
    public boolean isInstalled() {
        return FileUtil.exists(getJarFilename());
    }

    @Deprecated(forRemoval = true, since = "3.0.0-a.8")
    public boolean isLowerOrEqual(final @NonNull PluginDescriptor plugin) {
        return extension.getVersion().isLowerOrEqual(plugin.extension.getVersion());
    }

    @Deprecated(forRemoval = true, since = "3.0.0-a.8")
    public boolean isLower(final @NonNull PluginDescriptor plugin) {
        return extension.getVersion().isLower(plugin.extension.getVersion());
    }

    @Deprecated(forRemoval = true, since = "3.0.0-a.8")
    public boolean isGreaterOrEqual(final @NonNull PluginDescriptor plugin) {
        return extension.getVersion().isGreaterOrEqual(plugin.extension.getVersion());
    }

    @Deprecated(forRemoval = true, since = "3.0.0-a.8")
    public boolean isGreater(final @NonNull PluginDescriptor plugin) {
        return extension.getVersion().isGreater(plugin.extension.getVersion());
    }

    @Contract(pure = true)
    @Override
    public String toString() {
        return getName();
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof final PluginDescriptor plug)
            return getClassName().equals(plug.getClassName()) && getVersion().equals(plug.getVersion());

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getClassName().hashCode() ^ getVersion().hashCode();
    }

    /**
     * Sort plugins to make kernel plugins appears first.
     */
    public static final class PluginKernelNameSorter implements Comparator<PluginDescriptor> {
        public static PluginKernelNameSorter instance = new PluginKernelNameSorter();

        @Contract(pure = true)
        private PluginKernelNameSorter() {
            //
        }

        @Override
        public int compare(final @NonNull PluginDescriptor o1, final PluginDescriptor o2) {
            if (o1.extension.isKernel()) {
                if (!o2.extension.isKernel())
                    return -1;
            }
            else if (o2.extension.isKernel())
                return 1;

            return o1.toString().compareToIgnoreCase(o2.toString());
        }
    }

    /**
     * Sort plugins by name.
     */
    public static final class PluginNameSorter implements Comparator<PluginDescriptor> {
        public static PluginNameSorter instance = new PluginNameSorter();

        @Contract(pure = true)
        private PluginNameSorter() {
            //
        }

        @Override
        public int compare(final @NonNull PluginDescriptor o1, final @NonNull PluginDescriptor o2) {
            return o1.toString().compareToIgnoreCase(o2.toString());
        }
    }

    /**
     * Sort plugins on class name.
     */
    public static final class PluginClassNameSorter implements Comparator<PluginDescriptor> {
        public static PluginClassNameSorter instance = new PluginClassNameSorter();

        @Contract(pure = true)
        private PluginClassNameSorter() {
            //
        }

        @Override
        public int compare(final @NonNull PluginDescriptor o1, final @NonNull PluginDescriptor o2) {
            return o1.getClassName().compareToIgnoreCase(o2.getClassName());
        }
    }
}
