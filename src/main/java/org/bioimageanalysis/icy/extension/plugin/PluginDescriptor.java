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

package org.bioimageanalysis.icy.extension.plugin;

import org.bioimageanalysis.icy.common.Version;
import org.bioimageanalysis.icy.common.reflect.ClassUtil;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.extension.ExtensionDescriptor;
import org.bioimageanalysis.icy.extension.ExtensionLoader;
import org.bioimageanalysis.icy.extension.plugin.abstract_.Plugin;
import org.bioimageanalysis.icy.extension.plugin.abstract_.PluginActionable;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyPluginDescription;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyPluginIcon;
import org.bioimageanalysis.icy.extension.plugin.annotation_.IcyPluginName;
import org.bioimageanalysis.icy.extension.plugin.interface_.PluginDaemon;
import org.bioimageanalysis.icy.gui.component.icon.IcySVGImageIcon;
import org.bioimageanalysis.icy.gui.component.icon.SVGIcon;
import org.bioimageanalysis.icy.io.FileUtil;
import org.bioimageanalysis.icy.io.jar.JarUtil;
import org.bioimageanalysis.icy.model.image.ImageUtil;
import org.bioimageanalysis.icy.network.NetworkUtil;
import org.bioimageanalysis.icy.network.URLUtil;
import org.bioimageanalysis.icy.system.preferences.RepositoryPreferences.RepositoryInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * <br>
 * The plugin descriptor contains all the data needed to launch a plugin. <br>
 *
 * @author Fabrice de Chaumont
 * @author Stephane Dallongeville
 * @author Thomas Musset
 * @see PluginLauncher
 */
public final class PluginDescriptor {
    public static final int ICON_SIZE = 32;
    public static final int IMAGE_SIZE = 256;

    public static final ImageIcon DEFAULT_ICON = new IcySVGImageIcon(SVGIcon.INDETERMINATE_QUESTION);
    public static final Image DEFAULT_IMAGE = new IcySVGImageIcon(SVGIcon.INDETERMINATE_QUESTION).getImage();

    private Class<? extends Plugin> pluginClass;
    private ExtensionDescriptor extension;

    private ImageIcon icon;
    private Image image;
    private SVGIcon svgIcon;

    private String name;
    private String shortDescription;
    private String jarUrl;
    private String imageUrl;
    private String iconUrl;
    private String author;
    private String web;
    private String email;
    private String desc;
    private String changeLog;

    private boolean enabled;
    private boolean descriptorLoaded;
    private boolean iconLoaded;
    private boolean imageLoaded;
    private boolean changeLogLoaded;
    private boolean iconMono;

    // only for online descriptor
    private RepositoryInfo repository;

    public PluginDescriptor() {
        super();

        extension = null;
        pluginClass = null;

        icon = DEFAULT_ICON;
        image = DEFAULT_IMAGE;
        svgIcon = null;

        name = "";
        shortDescription = "";
        jarUrl = "";
        imageUrl = "";
        iconUrl = "";
        author = "";
        web = "";
        email = "";
        desc = "";
        changeLog = "";

        repository = null;

        // default
        enabled = true;
        descriptorLoaded = true;
        changeLogLoaded = true;
        iconLoaded = true;
        imageLoaded = true;
        iconMono = false;
    }

    /**
     * Create from class, used for local plugin.
     */
    public PluginDescriptor(final @NotNull Class<? extends Plugin> clazz) {
        this();

        this.pluginClass = clazz;

        final String baseResourceName;
        final String baseLocalName;

        String magicName = "";
        String magicShortDescription = "";
        String magicLongDescription = "";
        String magicIcon = "";
        boolean magicIconMono = false;

        if (clazz.isAnnotationPresent(IcyPluginName.class)) {
            final IcyPluginName annotation = clazz.getAnnotation(IcyPluginName.class);
            magicName = annotation.value();
        }
        if (clazz.isAnnotationPresent(IcyPluginDescription.class)) {
            final IcyPluginDescription annotation = clazz.getAnnotation(IcyPluginDescription.class);
            magicShortDescription = annotation.shortDesc();
            magicLongDescription = annotation.longDesc();
        }
        if (clazz.isAnnotationPresent(IcyPluginIcon.class)) {
            final IcyPluginIcon annotation = clazz.getAnnotation(IcyPluginIcon.class);
            magicIcon = annotation.path();
            magicIconMono = annotation.monochrome();
        }

        // TODO check if it's working
        // bundled plugin ?
        /*if (bundled) {
            // find original JAR file
            final String jarPath = getPluginJarPath();

            // get base resource and local name from it
            baseResourceName = FileUtil.getFileName(jarPath, false);
            baseLocalName = FileUtil.setExtension(jarPath, "");
        }
        else {*/
        baseResourceName = clazz.getSimpleName();
        baseLocalName = ClassUtil.getPathFromQualifiedName(clazz.getName());
        //}

        // load icon
        URL iconUrl;
        if (magicIcon.isEmpty())
            //iconUrl = clazz.getResource(baseResourceName + getIconExtension());
            iconUrl = clazz.getClassLoader().getResource("META-INF/icon.svg");
        else
            iconUrl = clazz.getResource(magicIcon);
        if (iconUrl == null)
            iconUrl = URLUtil.getURL(baseLocalName + getIconExtension());
        // loadIcon(url);

        // load image
        URL imageUrl = clazz.getResource(baseResourceName + getImageExtension());
        if (imageUrl == null)
            imageUrl = URLUtil.getURL(baseLocalName + getImageExtension());
        // loadImage(url);

        // set default informations
        name = pluginClass.getSimpleName();
        desc = name + " plugin";

        if (!magicName.isBlank())
            name = magicName;
        if (!magicShortDescription.isBlank())
            shortDescription = magicShortDescription;

        // overwrite image, icon url with their local equivalent
        this.iconUrl = iconUrl.toString();
        this.imageUrl = imageUrl.toString();

        // only descriptor is loaded here
        descriptorLoaded = true;
        changeLogLoaded = false;
        iconLoaded = false;
        imageLoaded = false;
        iconMono = magicIconMono;
    }

    /**
     * Load change log field (xmlUrl field should be correctly filled)
     */
    public boolean loadChangeLog() {
        // already loaded ?
        if (changeLogLoaded)
            return true;

        // just to avoid retry indefinitely if it fails
        changeLogLoaded = true;

        //IcyLogger.error(PluginDescriptor.class, "Can't load XML file from '" + xmlUrl + "' for plugin class '" + ident.getClassName() + "'");

        return false;
    }

    /**
     * Load 32x32 icon (icon url field should be correctly filled)
     */
    public boolean loadIcon() {
        // already loaded ?
        if (iconLoaded)
            return true;

        // just to avoid retry indefinitely if it fails
        iconLoaded = true;

        // load icon
        return loadIcon(URLUtil.getURL(iconUrl));
    }

    /**
     * Load 256x256 image (image url field should be correctly filled)
     */
    public boolean loadImage() {
        // already loaded ?
        if (imageLoaded)
            return true;

        // just to avoid retry indefinitely if it fails
        imageLoaded = true;

        // load image
        return loadImage(URLUtil.getURL(imageUrl));
    }

    /**
     * Load icon and image (both icon and image url fields should be correctly filled)
     */
    public boolean loadImages() {
        return loadIcon() & loadImage();
    }

    /**
     * Load descriptor and images if not already done
     */
    public boolean loadAll() {
        return loadChangeLog() & loadImages();
    }

    /**
     * Check if the plugin class is an instance of (or subclass of) the specified class.
     */
    public boolean isInstanceOf(final Class<?> baseClazz) {
        return ClassUtil.isSubClass(pluginClass, baseClazz);
    }

    /**
     * Check if the plugin class is an instance of (or subclass of) the specified class.
     */
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
    public boolean isInterface() {
        return pluginClass.isInterface();
    }

    /**
     * return true if the plugin has an action which can be started from menu
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
            if (!acceptInterface && isInterface())
                return false;

            return true;
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
        return getClassName().startsWith(ExtensionLoader.KERNEL_PLUGINS_PACKAGE + ".") || getClassName().startsWith(ExtensionLoader.OLD_KERNEL_PLUGINS_PACKAGE + ".");
    }

    boolean loadIcon(final @Nullable URL url) {
        if (url == null) {
            icon = null; //DEFAULT_ICON;
            return false;
        }

        // load icon
        if (url.toString().toLowerCase(Locale.getDefault()).endsWith(".svg")) {
            svgIcon = new SVGIcon(url, !iconMono);
            icon = new IcySVGImageIcon(svgIcon);
        }
        else {
            final Image img = ImageUtil.scale(ImageUtil.load(NetworkUtil.getInputStream(url, (repository != null) ? repository.getAuthenticationInfo() : null, true, false), false), ICON_SIZE, ICON_SIZE);
            if (img != null)
                icon = new ImageIcon(img);
            else
                icon = null;
        }

        // get default icon
        if (icon == null) {
            icon = null; //DEFAULT_ICON;
            return false;
        }

        return true;
    }

    boolean loadImage(final URL url) {
        // load image
        if (url != null)
            image = ImageUtil.scale(
                    ImageUtil.load(
                            NetworkUtil.getInputStream(
                                    url,
                                    (repository != null) ? repository.getAuthenticationInfo() : null,
                                    true,
                                    false
                            ),
                            false
                    ),
                    IMAGE_SIZE,
                    IMAGE_SIZE
            );

        // get default image
        if (image == null) {
            image = DEFAULT_IMAGE;
            return false;
        }

        return true;
    }

    /**
     * Returns the plugin class name.<br>
     * Ex: "plugins.tutorial.Example1"
     */
    public String getClassName() {
        return pluginClass.getName();
    }

    public String getSimpleClassName() {
        return ClassUtil.getSimpleClassName(pluginClass.getName());
    }

    /**
     * Returns the package name of the plugin class.
     */
    public String getPackageName() {
        return ClassUtil.getPackageName(pluginClass.getName());
    }

    /**
     * Returns the minimum package name (remove "icy" or/and "plugin" header)<br>
     */
    public String getSimplePackageName() {
        String result = getPackageName();

        if (result.startsWith("icy."))
            result = result.substring("icy.".length());
        if (result.startsWith("plugins."))
            result = result.substring("plugins.".length());

        return result;
    }

    /**
     * Returns the author package name (first part of simple package name)
     */
    @Deprecated(since = "3.0.0-a.5", forRemoval = true)
    public String getAuthorPackageName() {
        return "";
    }

    /**
     * @return the pluginClass
     */
    public Class<? extends Plugin> getPluginClass() {
        return pluginClass;
    }

    /**
     * @return the JAR file hosting this plugin (returns <code>null</code> if the plugin is not installed).<br>
     */
    public String getPluginJarPath() {
        if (pluginClass != null)
            return ClassUtil.getJarPath(pluginClass);

        return null;
    }

    /**
     * return associated filename
     */
    public String getFilename() {
        return ClassUtil.getPathFromQualifiedName(getClassName());
    }

    /**
     * return xml filename (local XML file)
     */
    @Deprecated(since = "3.0.0-a.5", forRemoval = true)
    public String getXMLFilename() {
        return "";
    }

    /**
     * return icon extension
     */
    public String getIconExtension() {
        return "_icon.png";
    }

    /**
     * return icon filename
     */
    public String getIconFilename() {
        return getFilename() + getIconExtension();
    }

    /**
     * return image extension
     */
    public String getImageExtension() {
        return ".png";
    }

    /**
     * return image filename
     */
    public String getImageFilename() {
        return getFilename() + getImageExtension();
    }

    /**
     * return jar filename
     */
    public String getJarFilename() {
        return getFilename() + JarUtil.FILE_DOT_EXTENSION;
    }

    /**
     * @return the icon
     */
    public ImageIcon getIcon() {
        loadIcon();
        return icon;
    }

    /**
     * @return the icon as image
     */
    public Image getIconAsImage() {
        final ImageIcon i = getIcon();

        if (i != null)
            return i.getImage();

        return null;
    }

    /**
     * @return the image
     */
    public Image getImage() {
        loadImage();
        return image;
    }

    /**
     * @return the plugin icon as SVG. Can be null.
     */
    public @Nullable SVGIcon getSVGIcon() {
        loadIcon();
        return svgIcon;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the short description.
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * @return the version
     */
    public Version getVersion() {
        return extension.getVersion();
    }

    /**
     * @return the url
     */
    public String getUrl() {
        // url is default XML url
        return "";
    }

    public void setExtension(final ExtensionDescriptor extension) {
        this.extension = extension;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return desc;
    }

    /**
     * @param repository the repository to set
     */
    public void setRepository(final RepositoryInfo repository) {
        this.repository = repository;
    }

    /**
     * @return the jarUrl
     */
    public String getJarUrl() {
        return jarUrl;
    }

    /**
     * @param jarUrl the jarUrl to set
     */
    public void setJarUrl(final String jarUrl) {
        this.jarUrl = jarUrl;
    }

    /**
     * @return the imageUrl
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * @param imageUrl the imageUrl to set
     */
    public void setImageUrl(final String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * @return the iconUrl
     */
    public String getIconUrl() {
        return iconUrl;
    }

    /**
     * @param iconUrl the iconUrl to set
     */
    public void setIconUrl(final String iconUrl) {
        this.iconUrl = iconUrl;
    }

    /**
     * Returns the author's plugin name.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns the website url of this plugin.
     */
    public String getWeb() {
        return web;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return the changeLog
     */
    public String getChangeLog() {
        return changeLog;
    }

    /**
     * @return the requiredKernelVersion
     */
    public Version getRequiredKernelVersion() {
        return extension.getKernelVersion();
    }

    /**
     * Returns true if descriptor is loaded.
     */
    public boolean isDescriptorLoaded() {
        return descriptorLoaded;
    }

    /**
     * Returns true if change log is loaded.
     */
    public boolean isChangeLogLoaded() {
        return changeLogLoaded;
    }

    /**
     * Returns true if icon is loaded.
     */
    public boolean isIconLoaded() {
        return iconLoaded;
    }

    /**
     * Returns true if image is loaded.
     */
    public boolean isImageLoaded() {
        return imageLoaded;
    }

    /**
     * Returns true if image and icon are loaded.
     */
    public boolean isImagesLoaded() {
        return iconLoaded && imageLoaded;
    }

    /**
     * Returns true if both descriptor and images are loaded.
     */
    public boolean isAllLoaded() {
        return descriptorLoaded && changeLogLoaded && iconLoaded && imageLoaded;
    }

    public RepositoryInfo getRepository() {
        return repository;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Return true if plugin is installed (corresponding JAR file exist)
     */
    public boolean isInstalled() {
        return FileUtil.exists(getJarFilename());
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(final String author) {
        this.author = author;
    }

    /**
     * @param web the web to set
     */
    public void setWeb(final String web) {
        this.web = web;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(final String email) {
        this.email = email;
    }

    /**
     * @param desc the description to set
     */
    public void setDescription(final String desc) {
        this.desc = desc;
    }

    /**
     * @param value the changeLog to set
     */
    public void setChangeLog(final String value) {
        this.changeLog = value;
    }

    public boolean isLowerOrEqual(final PluginDescriptor plugin) {
        return extension.getVersion().isLowerOrEqual(plugin.extension.getVersion());
    }

    public boolean isLower(final PluginDescriptor plugin) {
        return extension.getVersion().isLower(plugin.extension.getVersion());
    }

    public boolean isGreaterOrEqual(final PluginDescriptor plugin) {
        return extension.getVersion().isGreaterOrEqual(plugin.extension.getVersion());
    }

    public boolean isGreater(final PluginDescriptor plugin) {
        return extension.getVersion().isGreater(plugin.extension.getVersion());
    }

    @Override
    public String toString() {
        return getName();
    }

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
     * Sort plugins on name with plugins.kernel plugins appearing first.
     */
    public static class PluginKernelNameSorter implements Comparator<PluginDescriptor> {
        // static class
        public static PluginKernelNameSorter instance = new PluginKernelNameSorter();

        // static class
        private PluginKernelNameSorter() {
            super();
        }

        @Override
        public int compare(final PluginDescriptor o1, final PluginDescriptor o2) {
            final String packageName1 = o1.getPackageName();
            final String packageName2 = o2.getPackageName();

            if (packageName1.startsWith(ExtensionLoader.KERNEL_PLUGINS_PACKAGE)) {
                if (!packageName2.startsWith(ExtensionLoader.KERNEL_PLUGINS_PACKAGE))
                    return -1;
            }
            else if (packageName2.startsWith(ExtensionLoader.KERNEL_PLUGINS_PACKAGE))
                return 1;

            return o1.toString().compareToIgnoreCase(o2.toString());
        }
    }

    /**
     * Sort plugins on name.
     */
    public static class PluginNameSorter implements Comparator<PluginDescriptor> {
        // static class
        public static PluginNameSorter instance = new PluginNameSorter();

        // static class
        private PluginNameSorter() {
            super();
        }

        @Override
        public int compare(final PluginDescriptor o1, final PluginDescriptor o2) {
            return o1.toString().compareToIgnoreCase(o2.toString());
        }
    }

    /**
     * Sort plugins on class name.
     */
    public static class PluginClassNameSorter implements Comparator<PluginDescriptor> {
        // static class
        public static PluginClassNameSorter instance = new PluginClassNameSorter();

        // static class
        private PluginClassNameSorter() {
            super();
        }

        @Override
        public int compare(final PluginDescriptor o1, final PluginDescriptor o2) {
            return o1.getClassName().compareToIgnoreCase(o2.getClassName());
        }
    }

}
