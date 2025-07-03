/*
 * Copyright (c) 2010-2025. Institut Pasteur.
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

import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.extension.ExtensionLoader;
import org.bioimageanalysis.icy.gui.dialog.ConfirmDialog;
import org.bioimageanalysis.icy.gui.frame.progress.*;
import org.bioimageanalysis.icy.io.FileUtil;
import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.bioimageanalysis.icy.io.zip.ZipUtil;
import org.bioimageanalysis.icy.network.NetworkUtil;
import org.bioimageanalysis.icy.network.URLUtil;
import org.bioimageanalysis.icy.network.update.Updater;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.preferences.RepositoryPreferences.RepositoryInfo;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.EventListenerList;
import java.net.URL;
import java.util.*;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
@Deprecated(since = "3.0.0-a.5", forRemoval = true)
public class PluginInstaller implements Runnable {
    public interface PluginInstallerListener extends EventListener {
        void pluginInstalled(PluginDescriptor plugin, boolean success);

        void pluginRemoved(PluginDescriptor plugin, boolean success);
    }

    public record PluginInstallInfo(PluginDescriptor plugin, boolean showProgress) {
        @Contract(value = "null -> false", pure = true)
        @Override
        public boolean equals(@Nullable final Object obj) {
            if (obj instanceof PluginInstallInfo)
                return ((PluginInstallInfo) obj).plugin.equals(plugin);

            return false;
        }

        @Override
        public int hashCode() {
            return plugin.hashCode();
        }
    }

    private static final String ERROR_DOWNLOAD = "Error while downloading ";
    private static final String ERROR_SAVE = "Error while saving";
    // private static final String INSTALL_CANCELED = "Plugin installation canceled by user.";

    /**
     * static class
     */
    private static final PluginInstaller instance = new PluginInstaller();

    /**
     * plugin(s) to install FIFO
     */
    private final List<PluginInstallInfo> installFIFO;
    /**
     * plugin(s) to delete FIFO
     */
    private final List<PluginInstallInfo> removeFIFO;

    /**
     * listeners
     */
    private final EventListenerList listeners;

    /*
     * internals
     */
    private final List<PluginDescriptor> installingPlugins;
    private final List<PluginDescriptor> desinstallingPlugin;

    /**
     * static class
     */
    private PluginInstaller() {
        super();

        installFIFO = new ArrayList<>();
        removeFIFO = new ArrayList<>();

        listeners = new EventListenerList();

        installingPlugins = new ArrayList<>();
        desinstallingPlugin = new ArrayList<>();

        // launch installer thread
        new Thread(this, "Plugin installer").start();
    }

    /**
     * Return true if install or desinstall is possible
     */
    @Deprecated(since = "3.0.0-a.5", forRemoval = true)
    @Contract(pure = true)
    private static boolean isEnabled() {
        return true;
    }

    /**
     * Install a plugin (asynchronous)
     *
     * @param plugin       the plugin to install
     * @param showProgress show a progress frame during process
     */
    public static void install(final PluginDescriptor plugin, final boolean showProgress) {
        if ((plugin != null) && isEnabled()) {
            if (!NetworkUtil.hasInternetAccess()) {
                IcyLogger.error(PluginInstaller.class, "Cannot install '" + plugin.getName() + "' plugin : you are not connected to Internet.");

                return;
            }

            synchronized (instance.installFIFO) {
                instance.installFIFO.add(new PluginInstallInfo(plugin, showProgress));
            }
        }
    }

    /**
     * return true if PluginInstaller is processing
     */
    public static boolean isProcessing() {
        return isInstalling() || isDesinstalling();
    }

    /**
     * return a copy of the install FIFO
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull ArrayList<PluginInstallInfo> getInstallFIFO() {
        synchronized (instance.installFIFO) {
            return new ArrayList<>(instance.installFIFO);
        }
    }

    /**
     * Wait while installer is installing plugin.
     */
    public static void waitInstall() {
        while (isInstalling())
            ThreadUtil.sleep(100);
    }

    /**
     * return true if PluginInstaller is installing plugin(s)
     */
    public static boolean isInstalling() {
        return !instance.installFIFO.isEmpty() || !instance.installingPlugins.isEmpty();
    }

    /**
     * return true if 'plugin' is in the install FIFO
     */
    @Contract(pure = true)
    public static boolean isWaitingForInstall(final PluginDescriptor plugin) {
        synchronized (instance.installFIFO) {
            for (final PluginInstallInfo info : instance.installFIFO)
                if (plugin == info.plugin)
                    return true;
        }

        return false;
    }

    /**
     * return true if specified plugin is currently being installed or will be installed
     */
    public static boolean isInstallingPlugin(final PluginDescriptor plugin) {
        return instance.installingPlugins.contains(plugin) || isWaitingForInstall(plugin);
    }

    /**
     * Uninstall a plugin (asynchronous)
     *
     * @param plugin       the plugin to uninstall
     * @param showConfirm  show a confirmation dialog
     * @param showProgress show a progress frame during process
     */
    public static void desinstall(final PluginDescriptor plugin, final boolean showConfirm, final boolean showProgress) {
        if ((plugin != null) && isEnabled()) {
            if (showConfirm) {
                // get local plugins which depend from the plugin we want to delete
                final List<PluginDescriptor> dependants = getLocalDependenciesFrom(plugin);

                final StringBuilder message = new StringBuilder("<html>");

                if (!dependants.isEmpty()) {
                    message.append("The following plugin(s) won't work anymore :<br>");

                    for (final PluginDescriptor depPlug : dependants)
                        message.append(depPlug.getName()).append(" ").append(depPlug.getVersion()).append("<br>");

                    message.append("<br>");
                }

                message.append("Are you sure you want to remove '").append(plugin.getName()).append(" ").append(plugin.getVersion()).append("' ?</html>");

                if (ConfirmDialog.confirm(message.toString())) {
                    synchronized (instance.removeFIFO) {
                        instance.removeFIFO.add(new PluginInstallInfo(plugin, showConfirm));
                    }
                }
            }
            else {
                synchronized (instance.removeFIFO) {
                    instance.removeFIFO.add(new PluginInstallInfo(plugin, showProgress));
                }
            }
        }
    }

    /**
     * return a copy of the remove FIFO
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull ArrayList<PluginInstallInfo> getRemoveFIFO() {
        synchronized (instance.removeFIFO) {
            return new ArrayList<>(instance.removeFIFO);
        }
    }

    /**
     * Wait while installer is removing plugin.
     */
    public static void waitDesinstall() {
        while (isDesinstalling())
            ThreadUtil.sleep(100);
    }

    /**
     * return true if PluginInstaller is desinstalling plugin(s)
     */
    public static boolean isDesinstalling() {
        return !instance.removeFIFO.isEmpty() || !instance.desinstallingPlugin.isEmpty();
    }

    /**
     * return true if 'plugin' is in the remove FIFO
     */
    @Contract(pure = true)
    public static boolean isWaitingForDesinstall(final PluginDescriptor plugin) {
        synchronized (instance.removeFIFO) {
            for (final PluginInstallInfo info : instance.removeFIFO)
                if (plugin == info.plugin)
                    return true;
        }

        return false;
    }

    /**
     * return true if specified plugin is currently being desinstalled or will be desinstalled
     */
    public static boolean isDesinstallingPlugin(final PluginDescriptor plugin) {
        return instance.desinstallingPlugin.contains(plugin) || isWaitingForDesinstall(plugin);
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            // process installations
            if (!installFIFO.isEmpty()) {
                // so list has sometime to fill-up
                ThreadUtil.sleep(200);

                do
                    installInternal();
                while (!installFIFO.isEmpty());
            }

            // process deletions
            while (!removeFIFO.isEmpty()) {
                // so list has sometime to fill-up
                ThreadUtil.sleep(200);

                do
                    desinstallInternal();
                while (!removeFIFO.isEmpty());
            }

            ThreadUtil.sleep(100);
        }
    }

    /**
     * Backup specified plugin if it already exists.<br>
     * Return an empty string if no error else return error message
     */
    private static @NotNull String backup(final @NotNull PluginDescriptor plugin) {
        final boolean ok;

        // backup JAR and image files
        ok = Updater.backup(plugin.getJarFilename())
                && Updater.backup(plugin.getIconFilename())
                && Updater.backup(plugin.getImageFilename());

        if (!ok)
            return "Can't backup plugin '" + plugin.getName() + "'";

        return "";
    }

    /**
     * Return an empty string if no error else return error message
     */
    private static String downloadAndSavePlugin(final PluginDescriptor plugin, final DownloadFrame taskFrame) {
        String result;

        if (taskFrame != null)
            taskFrame.setMessage("Downloading " + plugin);

        final RepositoryInfo repos = plugin.getRepository();
        final String login;
        final String pass;

        // use authentication (repos should not be null at this point)
        if (repos.isAuthenticationEnabled()) {
            login = repos.getLogin();
            pass = repos.getPassword();
        }
        else {
            login = null;
            pass = null;
        }

        // try to build the final path using base repository address and plugin relative address
        // (useful for local repository)
        URL url;
        final String basePath = FileUtil.getDirectory(repos.getLocation());
        // download and save JAR file
        url = URLUtil.buildURL(basePath, plugin.getJarUrl());
        result = downloadAndSave(url, plugin.getJarFilename(), login, pass, true, taskFrame);
        if (!StringUtil.isEmpty(result))
            return result;

        // verify JAR file is not corrupted
        if (!ZipUtil.isValid(plugin.getJarFilename(), false))
            return "Downloaded JAR file '" + plugin.getJarFilename() + "' is corrupted !";

        // download and save XML file
        url = URLUtil.buildURL(basePath, plugin.getUrl());
        //result = downloadAndSave(url, plugin.getXMLFilename(), login, pass, true, taskFrame);
        if (!StringUtil.isEmpty(result))
            return result;

        // download and save icon & image files
        if (!StringUtil.isEmpty(plugin.getIconUrl())) {
            url = URLUtil.buildURL(basePath, plugin.getIconUrl());
            downloadAndSave(url, plugin.getIconFilename(), login, pass, false, taskFrame);
        }
        if (!StringUtil.isEmpty(plugin.getImageUrl())) {
            url = URLUtil.buildURL(basePath, plugin.getImageUrl());
            downloadAndSave(url, plugin.getImageFilename(), login, pass, false, taskFrame);
        }

        return "";
    }

    /**
     * Return an empty string if no error else return error message
     */
    private static @Nullable String downloadAndSave(final URL downloadPath, final String savePath, final String login, final String pass, final boolean displayError, final DownloadFrame downloadFrame) {
        if (downloadFrame != null)
            downloadFrame.setPath(FileUtil.getFileName(savePath));

        // load data
        final byte[] data = NetworkUtil.download(downloadPath, login, pass, downloadFrame, displayError);
        if (data == null)
            return ERROR_DOWNLOAD + downloadPath.toString();

        // save data
        if (!FileUtil.save(savePath, data, displayError)) {
            final String[] messages = new String[]{
                    "Can't write '" + savePath + "' !",
                    "File may be locked or you don't own the rights to write files here."
            };
            IcyLogger.error(PluginInstaller.class, messages);
            return ERROR_SAVE + savePath;
        }

        return null;
    }

    private static boolean deletePlugin(final @NotNull PluginDescriptor plugin) {
        if (!FileUtil.delete(plugin.getJarFilename(), false)) {
            IcyLogger.error(PluginInstaller.class, "Can't delete '" + plugin.getJarFilename() + "' file !");
            // fatal error
            return false;
        }

        FileUtil.delete(plugin.getImageFilename(), false);
        FileUtil.delete(plugin.getIconFilename(), false);

        return true;
    }

    /**
     * Fill list with local dependencies (plugins) of specified plugin
     */
    public static void getLocalDependenciesOf(final List<PluginDescriptor> result, final @NotNull PluginDescriptor plugin) {
        //
    }

    /**
     * Return local plugins list which depend from the specified list of plugins.
     */
    public static @NotNull List<PluginDescriptor> getLocalDependenciesFrom(final @NotNull List<PluginDescriptor> plugins) {
        final List<PluginDescriptor> result = new ArrayList<>();

        for (final PluginDescriptor plugin : plugins)
            getLocalDependenciesFrom(plugin, result);

        return result;
    }

    /**
     * Return local plugins list which depend from the specified plugin.
     */
    public static @NotNull List<PluginDescriptor> getLocalDependenciesFrom(final PluginDescriptor plugin) {
        final List<PluginDescriptor> result = new ArrayList<>();

        getLocalDependenciesFrom(plugin, result);

        return result;
    }

    /**
     * Return local plugins list which depend from the specified plugin.
     */
    private static void getLocalDependenciesFrom(final PluginDescriptor plugin, final List<PluginDescriptor> result) {
        //for (final PluginDescriptor curPlug : ExtensionLoader.getPlugins())
            // require specified plugin ?
            //if (curPlug.requires(plugin))
                //PluginDescriptor.addToList(result, curPlug);
    }

    /**
     * Fill list with 'sources' dependencies of specified plugin
     */
    private static void getLocalDependenciesOf(final List<PluginDescriptor> result, final List<PluginDescriptor> sources, final @NotNull PluginDescriptor plugin) {
        //
    }

    /**
     * Reorder the list so needed dependencies comes first in list
     */
    public static @NotNull List<PluginDescriptor> orderDependencies(final List<PluginDescriptor> plugins) {
        final List<PluginDescriptor> sources = new ArrayList<>(plugins);
        final List<PluginDescriptor> result = new ArrayList<>();

        while (sources.size() > 0) {
            final List<PluginDescriptor> deps = new ArrayList<>();

            getLocalDependenciesOf(result, sources, sources.get(0));

            // remove tested plugin and its dependencies from source
            sources.removeAll(result);
        }

        return result;
    }

    /**
     * Resolve dependencies for specified plugin
     */
    public static boolean getDependencies(final @NotNull PluginDescriptor plugin, final List<PluginDescriptor> pluginsToInstall, final CancelableProgressFrame taskFrame, final boolean showError) {
        //

        return true;
    }

    private void installInternal() {
        DownloadFrame taskFrame = null;

        try {
            final List<PluginInstallInfo> infos;
            boolean showProgress;

            synchronized (installFIFO) {
                infos = new ArrayList<>(installFIFO);

                showProgress = false;
                for (int i = infos.size() - 1; i >= 0; i--) {
                    final PluginInstallInfo info = infos.get(i);

                    showProgress |= info.showProgress;
                }

                installFIFO.clear();
            }

            if (showProgress && !Icy.getMainInterface().isHeadLess()) {
                taskFrame = new DownloadFrame();
                taskFrame.setMessage("Initializing...");
            }

            List<PluginDescriptor> dependencies = new ArrayList<>();
            final Set<PluginDescriptor> pluginsOk = new HashSet<>();
            final Set<PluginDescriptor> pluginsNOk = new HashSet<>();
            final Set<PluginDescriptor> pluginsNewJava = new HashSet<>();

            // get dependencies
            for (int i = installingPlugins.size() - 1; i >= 0; i--) {
                final PluginDescriptor plugin = installingPlugins.get(i);
                final String plugDesc = plugin.getName() + " " + plugin.getVersion();

                if (taskFrame != null) {
                    // cancel requested ?
                    if (taskFrame.isCancelRequested())
                        return;

                    taskFrame.setMessage("Checking dependencies for '" + plugDesc + "' ...");
                }

                // check dependencies
                if (!getDependencies(plugin, dependencies, taskFrame, true)) {
                    // can't resolve dependencies for this plugin
                    pluginsNOk.add(plugin);
                    installingPlugins.remove(i);
                }
            }

            // nothing to install
            if (installingPlugins.isEmpty())
                return;

            // order dependencies
            dependencies = orderDependencies(dependencies);

            String error;

            // clear backup folder
            FileUtil.delete(Updater.BACKUP_DIRECTORY, true);

            // now we can proceed the installation itself
            for (final PluginDescriptor plugin : installingPlugins) {
                final String plugDesc = plugin.getName() + " " + plugin.getVersion();

                if (taskFrame != null) {
                    // cancel requested ? --> interrupt installation
                    if (taskFrame.isCancelRequested())
                        break;

                    taskFrame.setMessage("Installing " + plugDesc + "...");
                }

                try {
                    // backup plugin
                    error = backup(plugin);

                    // backup ok --> install plugin
                    if (StringUtil.isEmpty(error)) {
                        error = downloadAndSavePlugin(plugin, taskFrame);

                        // an error occurred ? --> restore
                        if (!StringUtil.isEmpty(error))
                            Updater.restore();
                    }
                }
                finally {
                    // delete backup
                    FileUtil.delete(Updater.BACKUP_DIRECTORY, true);
                }

                if (StringUtil.isEmpty(error))
                    pluginsOk.add(plugin);
                else {
                    pluginsNOk.add(plugin);
                    // print error
                    IcyLogger.error(PluginInstaller.class, error);
                }
            }

            // verify installed plugins
            if (taskFrame != null)
                taskFrame.setMessage("Verifying plugins...");

            // reload plugin list
            ExtensionLoader.reload();

            for (final PluginDescriptor plugin : pluginsOk) {
                error = PluginLoader.verifyPlugin(plugin);

                // send report when we have verification error
                if (!StringUtil.isEmpty(error)) {
                    final String[] messages = new String[]{
                            "Fatal error while loading '" + plugin.getClassName() + "' class from " + plugin.getJarFilename() + " :",
                            error
                    };

                    // new java version required ?
                    if (error.contains(PluginLoader.NEWER_JAVA_REQUIRED)) {
                        // print error in console
                        IcyLogger.fatal(PluginInstaller.class, messages);
                        // add to list
                        pluginsNewJava.add(plugin);
                    }
                    else {
                        // report error to developer
                        IcyExceptionHandler.report(plugin, "An error occured while installing the plugin :\n" + error);
                        // print error
                        IcyLogger.fatal(PluginInstaller.class, messages);
                        // add to list
                        pluginsNOk.add(plugin);
                    }
                }
            }

            // remove all plugins which failed or require new version of Java from OK list
            pluginsOk.removeAll(pluginsNOk);
            pluginsOk.removeAll(pluginsNewJava);

            if (!pluginsNOk.isEmpty()) {
                final ArrayList<String> messages = new ArrayList<>();
                messages.add("Installation of the following plugin(s) failed:");
                for (final PluginDescriptor plugin : pluginsNOk) {
                    messages.add(plugin.getName() + " " + plugin.getVersion());
                    // notify about installation fails
                    fireInstalledEvent(plugin, false);
                }
                IcyLogger.error(PluginInstaller.class, messages.toArray(new String[0]));
            }

            if (!pluginsOk.isEmpty()) {
                final ArrayList<String> messages = new ArrayList<>();
                messages.add("The following plugin(s) has been correctly installed:");
                for (final PluginDescriptor plugin : pluginsOk) {
                    messages.add(plugin.getName() + " " + plugin.getVersion());
                    // notify about installation successes
                    fireInstalledEvent(plugin, true);
                }
                IcyLogger.info(PluginInstaller.class, messages.toArray(new String[0]));
            }

            if (!pluginsNewJava.isEmpty()) {
                final ArrayList<String> messages = new ArrayList<>();
                messages.add("The following plugin(s) require a newer version of java:");
                for (final PluginDescriptor plugin : pluginsNewJava) {
                    messages.add(plugin.getName() + " " + plugin.getVersion());
                    // notify about installation even fails
                    fireInstalledEvent(plugin, false);
                }
                IcyLogger.error(PluginInstaller.class, messages.toArray(new String[0]));
            }

            if (showProgress && !Icy.getMainInterface().isHeadLess()) {
                // no plugin success ?
                if (pluginsOk.isEmpty())
                    new FailedAnnounceFrame("Plugin(s) installation failed !", 10);
                    // no plugin fail ?
                else if (pluginsNOk.isEmpty() && pluginsNewJava.isEmpty())
                    new SuccessfullAnnounceFrame("Plugin(s) installation was successful !", 10);
                else if (!pluginsNOk.isEmpty())
                    new FailedAnnounceFrame("Some plugin(s) installation failed (looks at the output console for detail) !", 10);

                // notify about new java version required
                for (final PluginDescriptor plugin : pluginsNewJava)
                    new AnnounceFrame("Plugin '" + plugin.getName() + " requires a new version of Java !", 10);
            }
        }
        finally {
            // installation end
            installingPlugins.clear();
            if (taskFrame != null)
                taskFrame.close();
        }
    }

    private void desinstallInternal() {
        CancelableProgressFrame taskFrame = null;

        try {
            final List<PluginInstallInfo> infos;
            boolean showProgress;

            synchronized (removeFIFO) {
                infos = new ArrayList<>(removeFIFO);

                // determine if we should display the progress bar
                showProgress = false;
                for (int i = infos.size() - 1; i >= 0; i--) {
                    final PluginInstallInfo info = infos.get(i);

                    desinstallingPlugin.add(info.plugin);
                    showProgress |= info.showProgress;
                }

                removeFIFO.clear();
            }

            if (showProgress && !Icy.getMainInterface().isHeadLess())
                taskFrame = new CancelableProgressFrame("Initializing...");

            // now we can proceed remove
            for (final PluginDescriptor plugin : desinstallingPlugin) {
                final String plugDesc = plugin.getName() + " " + plugin.getVersion();
                final boolean result;

                if (taskFrame != null) {
                    // cancel requested ?
                    if (taskFrame.isCancelRequested())
                        return;

                    taskFrame.setMessage("Removing plugin '" + plugDesc + "'...");
                }

                result = deletePlugin(plugin);

                // notify plugin deletion
                fireRemovedEvent(plugin, result);

                if (showProgress && !Icy.getMainInterface().isHeadLess()) {
                    if (!result)
                        new FailedAnnounceFrame("Plugin '" + plugDesc + "' delete operation failed !");
                }

                if (result)
                    IcyLogger.info(PluginInstaller.class, "Plugin '" + plugDesc + "' correctly removed.");
                else
                    IcyLogger.error(PluginInstaller.class, "Plugin '" + plugDesc + "' delete operation failed !");
            }
        }
        finally {
            if (taskFrame != null)
                taskFrame.close();
            // removing end
            desinstallingPlugin.clear();
        }

        // reload plugin list
        PluginLoader.reload();
    }

    /**
     * Add a listener
     */
    public static void addListener(final PluginInstallerListener listener) {
        synchronized (instance.listeners) {
            instance.listeners.add(PluginInstallerListener.class, listener);
        }
    }

    /**
     * Remove a listener
     */
    public static void removeListener(final PluginInstallerListener listener) {
        synchronized (instance.listeners) {
            instance.listeners.remove(PluginInstallerListener.class, listener);
        }
    }

    /**
     * fire plugin installed event
     */
    private void fireInstalledEvent(final PluginDescriptor plugin, final boolean success) {
        synchronized (listeners) {
            for (final PluginInstallerListener listener : listeners.getListeners(PluginInstallerListener.class))
                listener.pluginInstalled(plugin, success);
        }
    }

    /**
     * fire plugin removed event
     */
    private void fireRemovedEvent(final PluginDescriptor plugin, final boolean success) {
        synchronized (listeners) {
            for (final PluginInstallerListener listener : listeners.getListeners(PluginInstallerListener.class))
                listener.pluginRemoved(plugin, success);
        }
    }
}
