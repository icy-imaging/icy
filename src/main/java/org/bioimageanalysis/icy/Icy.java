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

package org.bioimageanalysis.icy;

import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.bioimageanalysis.icy.common.Version;
import org.bioimageanalysis.icy.common.collection.CollectionUtil;
import org.bioimageanalysis.icy.common.math.UnitUtil;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.extension.ExtensionLoader;
import org.bioimageanalysis.icy.extension.plugin.PluginDescriptor;
import org.bioimageanalysis.icy.extension.plugin.PluginInstaller;
import org.bioimageanalysis.icy.extension.plugin.PluginLauncher;
import org.bioimageanalysis.icy.extension.plugin.PluginUpdater;
import org.bioimageanalysis.icy.extension.plugin.abstract_.Plugin;
import org.bioimageanalysis.icy.gui.LookAndFeelUtil;
import org.bioimageanalysis.icy.gui.action.ActionManager;
import org.bioimageanalysis.icy.gui.dialog.ConfirmDialog;
import org.bioimageanalysis.icy.gui.dialog.IdConfirmDialog;
import org.bioimageanalysis.icy.gui.dialog.IdConfirmDialog.Confirmer;
import org.bioimageanalysis.icy.gui.frame.ExitFrame;
import org.bioimageanalysis.icy.gui.frame.IcyExternalFrame;
import org.bioimageanalysis.icy.gui.frame.NewVersionFrame;
import org.bioimageanalysis.icy.gui.frame.progress.AnnounceFrame;
import org.bioimageanalysis.icy.gui.frame.progress.ProgressFrame;
import org.bioimageanalysis.icy.gui.frame.progress.ToolTipFrame;
import org.bioimageanalysis.icy.gui.main.MainFrame;
import org.bioimageanalysis.icy.gui.main.MainInterface;
import org.bioimageanalysis.icy.gui.main.MainInterfaceBatch;
import org.bioimageanalysis.icy.gui.main.MainInterfaceGui;
import org.bioimageanalysis.icy.io.FileUtil;
import org.bioimageanalysis.icy.io.Loader;
import org.bioimageanalysis.icy.model.cache.ImageCache;
import org.bioimageanalysis.icy.model.sequence.SequencePrefetcher;
import org.bioimageanalysis.icy.network.NetworkUtil;
import org.bioimageanalysis.icy.network.update.IcyUpdater;
import org.bioimageanalysis.icy.network.update.Updater;
import org.bioimageanalysis.icy.system.IcyExceptionHandler;
import org.bioimageanalysis.icy.system.SingleInstanceCheck;
import org.bioimageanalysis.icy.system.SystemUtil;
import org.bioimageanalysis.icy.system.UserUtil;
import org.bioimageanalysis.icy.system.audit.Audit;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.bioimageanalysis.icy.system.os.AppleUtil;
import org.bioimageanalysis.icy.system.preferences.ApplicationPreferences;
import org.bioimageanalysis.icy.system.preferences.GeneralPreferences;
import org.bioimageanalysis.icy.system.preferences.IcyPreferences;
import org.bioimageanalysis.icy.system.thread.ThreadUtil;
import org.bioimageanalysis.icy.vtk.IcyVTK;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import vtk.vtkNativeLibrary;
import vtk.vtkVersion;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.*;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;

/**
 * Entry point for Icy.
 *
 * @author Stephane
 * @author Thomas Musset
 */
public final class Icy {
    public static final String LIB_PATH = "lib";
    public static final int EXIT_FORCE_DELAY = 3000;

    /**
     * Icy Version
     */
    public static final Version VERSION = new Version(3, 0, 0, Version.DevelopmentStage.ALPHA, 5);

    /**
     * Main interface
     */
    private static MainInterface mainInterface = null;

    /**
     * Unique instance checker
     */
    static FileLock lock = null;

    // TODO : remove this block
    /*
     * private splash for initial loading
     */
    //static SplashScreenFrame splashScreen = null;

    private static boolean firstStart = false;

    /**
     * VTK library loaded flag
     */
    static boolean vtkLibraryLoaded = false;

    /**
     * No splash screen flag (default = false)
     */
    static boolean noSplash = false;
    /**
     * No exit on HeadLess mode (default = false)
     */
    static boolean noHLExit = false;
    /**
     * Exiting flag
     */
    static boolean exiting = false;

    /**
     * Startup parameters
     */
    static String[] args;
    static String[] pluginArgs;
    static String startupPluginName;
    static Plugin startupPlugin;
    static String startupImage;

    /**
     * internals
     */
    static ExitFrame exitFrame = null;
    static Thread terminer = null;

    /**
     * Flag indicating that cache module is disabled
     */
    private static boolean disableCache = false;

    /**
     * Flag indicating that network module is disabled (important to set to true by default for Icy-Updater)
     */
    private static boolean disableNetwork = false;

    /**
     * @param args Received from the command line.
     */
    public static void main(final String[] args) {
        System.setProperty("log4j.skipJansi", "false");

        boolean headless = false;

        Locale.setDefault(Locale.ENGLISH);

        firstStart = UserUtil.getIcyHomeDirectory() == null;

        try {
            if (firstStart)
                UserUtil.init();
        }
        catch (final IOException e) {
            fatalError(e, true);
        }

        // Clear log file
        final File logFile = new File(UserUtil.getIcyHomeDirectory(), "icy.log");
        if (!logFile.exists())
            FileUtil.createFile(logFile);
        else if (logFile.isFile()) {
            FileUtil.delete(logFile, false);
            FileUtil.createFile(logFile);
        }

        IcyLogger.setConsoleLevel(IcyLogger.TRACE);
        IcyLogger.setGUILevel(IcyLogger.ERROR);

        try {
            IcyLogger.info(Icy.class, "Initializing...");

            // handle arguments (must be the first thing to do)
            headless = handleAppArgs(args);

            // force headless if we have a headless system
            if (!headless && GraphicsEnvironment.isHeadless())
                headless = true;

            // initialize preferences
            IcyPreferences.init();

            // check if Icy is already running
            lock = SingleInstanceCheck.lock("icy-3");
            if (lock == null) {
                // we always accept multi instance in headless mode
                if (!headless) {
                    // we need to use our custom ConfirmDialog as
                    // Icy.getMainInterface().isHeadless() will return false here
                    final Confirmer confirmer = new Confirmer(
                            "Confirmation",
                            "Icy 3 is already running on this computer. Start anyway ?",
                            JOptionPane.YES_NO_OPTION,
                            ApplicationPreferences.ID_SINGLE_INSTANCE
                    );
                    ThreadUtil.invokeNow(confirmer);

                    if (!confirmer.getResult()) {
                        IcyLogger.info(Icy.class, "Exiting...");
                        // save preferences
                        IcyPreferences.save();
                        // and quit
                        System.exit(0);
                        return;
                    }
                }
            }

            // fix possible IllegalArgumentException on Swing sorting
            //System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
            // set LOCI debug level (do it immediately as it can quickly show some log messages)
            //loci.common.DebugTools.enableLogging("ERROR");
            loci.common.DebugTools.setRootLevel("ERROR");

            // TODO : remove this block
            //if (!headless && !noSplash) {
            // prepare splashScreen (ok to create it here as we are not yet in substance laf)
            //splashScreen = new SplashScreenFrame();

            // It's important to initialize AWT now (with InvokeNow(...) for instance) to avoid
            // the JVM deadlock bug (id: 5104239). It happen when the AWT thread is initialized
            // while others threads load some new library with ClassLoader.loadLibrary

            // display splash NOW (don't use ThreadUtil as headless is still false here)
            //EventQueue.invokeAndWait(() -> {
            // display splash screen
            //splashScreen.setVisible(true);
            //});
            //}

            // fast start
            // force image cache initialization so GUI won't wait after it (need preferences init)
            // TODO I don't know what is supposed to do, it just check if the cache is null, it does not init EhCache
            //new Thread(ImageCache::isInit, "Initializer: Cache").start();

            // initialize network (need preferences init)
            new Thread(NetworkUtil::init, "Initializer: Network").start();

            new Thread(ExtensionLoader::reloadAsynch, "Initializer: Extensions").start();

            WorkbookFactory.addProvider(new HSSFWorkbookFactory());
            WorkbookFactory.addProvider(new XSSFWorkbookFactory());

            // build main interface
            if (headless)
                mainInterface = new MainInterfaceBatch();
            else
                mainInterface = new MainInterfaceGui();
        }
        catch (final Throwable t) {
            // any error at this point is fatal
            fatalError(t, headless);
        }

        if (!headless) {
            ToolTipManager.sharedInstance().setInitialDelay(10);

            // do it on AWT thread NOW as this is what we want first
            ThreadUtil.invokeNow(() -> {
                try {
                    // init Look And Feel (need mainInterface instance)
                    LookAndFeelUtil.init();

                    // init need "mainInterface" variable to be initialized
                    getMainInterface().init();
                }
                catch (final Throwable t) {
                    // any error here is fatal
                    fatalError(t, false);
                }
            });

            // initialize OSX specific GUI stuff
            if (SystemUtil.isMac())
                AppleUtil.init();
        }
        else {
            if (ExtensionLoader.getExtensions().isEmpty()) {
                IcyLogger.fatal(Icy.class, "No extensions were found. Exiting...");
                exit(false);
            }

            // simple main interface init
            getMainInterface().init();
        }

        // TODO : remove this block
        // splash screen initialized --> hide it
        //if (splashScreen != null) {
        // then do less important stuff later
        //ThreadUtil.invokeLater(() -> {
        // we can now hide splash as we have interface
        //splashScreen.dispose();
        //splashScreen = null;
        //});
        //}

        // initialize exception handler
        IcyExceptionHandler.init();

        // show general informations
        IcyLogger.info(Icy.class, String.format("%s %s (%d bit)", SystemUtil.getJavaName(), SystemUtil.getJavaVersion(), SystemUtil.getJavaArchDataModel()));
        IcyLogger.info(Icy.class, String.format("Running on %s %s (%s)", SystemUtil.getOSName(), SystemUtil.getOSVersion(), SystemUtil.getOSArch()));
        IcyLogger.info(Icy.class, String.format("System total memory: %s", UnitUtil.getBytesString(SystemUtil.getTotalMemory())));
        IcyLogger.info(Icy.class, String.format("System available memory: %s", UnitUtil.getBytesString(SystemUtil.getFreeMemory())));
        IcyLogger.info(Icy.class, String.format("Max Java memory: %s", UnitUtil.getBytesString(SystemUtil.getJavaMaxMemory())));

        if (headless)
            IcyLogger.info(Icy.class, "Headless mode");

        // prepare native library files (need preferences init)
        new Thread(Icy::nativeLibrariesInit, "Initializer: VTK").start();

        // image cache disabled from command line ?
        if (isCacheDisabled())
            IcyLogger.info(Icy.class, "Image cache is disabled.");

            // virtual mode enabled ? --> initialize image cache
        else if (GeneralPreferences.getVirtualMode())
            ImageCache.init(ApplicationPreferences.getCacheMemoryMB(), ApplicationPreferences.getCachePath());

        // initialize action manager
        if (!headless)
            ActionManager.init();

        // changed version ?
        if (!ApplicationPreferences.getVersion().equals(Icy.VERSION)) {
            // not headless ?
            if (!headless) {
                // display the new version information
                final String changeLog = Icy.getChangeLog();

                // show the new version frame
                if (!StringUtil.isEmpty(changeLog)) {
                    ThreadUtil.invokeNow(() -> {
                        new NewVersionFrame(Icy.getChangeLog());
                    });
                }
            }

            // force check update for the new version
            GeneralPreferences.setLastUpdateCheckTime(0);
        }

        final long currentTime = System.currentTimeMillis();
        final long halfDayInterval = 1000 * 60 * 60 * 12;

        // check only once per 12 hours slice
        if (!isNetworkDisabled() && (currentTime > (GeneralPreferences.getLastUpdateCheckTime() + halfDayInterval))) {
            // check for core update
            // TODO : enable this
            /*if (GeneralPreferences.getAutomaticUpdate())
                IcyUpdater.checkUpdate(true);*/
            // check for plugin update
            /*if (PluginPreferences.getAutomaticUpdate())
                PluginUpdater.checkUpdate(true);*/

            // update last update check time
            GeneralPreferences.setLastUpdateCheckTime(currentTime);
        }

        // update version info
        ApplicationPreferences.setVersion(Icy.VERSION);
        // set LOCI debug level
        // loci.common.DebugTools.enableLogging("ERROR");
        // set OGL debug level
        //SystemUtil.setProperty("jogl.verbose", "TRUE");
        //SystemUtil.setProperty("jogl.debug", "TRUE");

        IcyLogger.info(Icy.class, String.format("Icy v%s started", VERSION.toShortString()));

        if (!headless) {
            if (ExtensionLoader.getExtensions().isEmpty()) {
                IcyLogger.error(Icy.class, "No extensions were found. You will not be able to use Icy properly.");
                JOptionPane.showMessageDialog(getMainInterface().getMainFrame(), new String[] {"No extensions were found.", "You will not be able to use Icy properly."}, "No extension found", JOptionPane.ERROR_MESSAGE);
            }
        }

        checkParameters();

        // handle startup arguments
        if (startupImage != null && !startupImage.isBlank())
            Icy.getMainInterface().addSequence(Loader.loadSequence(FileUtil.getGenericPath(startupImage), 0, false));

        // wait while updates are occurring before starting command line plugin...
        while (PluginUpdater.isCheckingForUpdate() || PluginInstaller.isProcessing())
            ThreadUtil.sleep(1);

        if (startupPluginName != null) {
            ExtensionLoader.waitWhileLoading();

            final PluginDescriptor plugin = ExtensionLoader.getPlugin(startupPluginName);

            if (plugin == null) {
                IcyLogger.error(Icy.class, String.format("Could not launch plugin '%s': the plugin was not found.", startupPluginName));
                IcyLogger.info(Icy.class, "Be sure you correctly wrote the complete class name and respected the case.");
                IcyLogger.info(Icy.class, "Ex: plugins.mydevid.analysis.MyPluginClass");
            }
            else
                startupPlugin = PluginLauncher.start(plugin);
        }

        // headless mode ? we can exit now...
        if (headless && !noHLExit)
            exit(false);
    }

    private static boolean handleAppArgs(final String @NotNull [] args) {
        final List<String> pluginArgsList = new ArrayList<>();

        startupImage = null;
        startupPluginName = null;
        startupPlugin = null;
        boolean execute = false;
        boolean headless = false;
        disableCache = false;
        disableNetwork = false;

        // save the base arguments
        Icy.args = args;

        for (final String arg : args) {
            // store plugin arguments
            if (startupPluginName != null)
                pluginArgsList.add(arg);
            else if (execute)
                startupPluginName = arg;
                // headless mode
            else if (arg.equalsIgnoreCase("--headless") || arg.equalsIgnoreCase("-hl"))
                headless = true;
            else if (arg.equalsIgnoreCase("--nocache") || arg.equalsIgnoreCase("-nc"))
                disableCache = true;
            else if (arg.equalsIgnoreCase("--nonetwork") || arg.equalsIgnoreCase("-nnt"))
                disableNetwork = true;
                // disable splash-screen
            else if (arg.equalsIgnoreCase("--nosplash") || arg.equalsIgnoreCase("-ns"))
                noSplash = true;
                // disable default exit operation in headless mode
            else if (arg.equalsIgnoreCase("--noHLexit") || arg.equalsIgnoreCase("-nhle"))
                noHLExit = true;
                // execute plugin
            else if (arg.equalsIgnoreCase("--execute") || arg.equalsIgnoreCase("-x"))
                execute = true;
            else if (arg.trim().equalsIgnoreCase(Updater.ARG_UPDATE) || arg.trim().equalsIgnoreCase(Updater.ARG_NOSTART))
                IcyLogger.error(Icy.class, "Wrong parameter: " + arg);
                // assume image name ?
            else if (!arg.trim().isBlank())
                startupImage = arg;
        }

        // save the plugin arguments
        Icy.pluginArgs = pluginArgsList.toArray(new String[0]);

        return headless;
    }

    static void checkParameters() {
        // detect bad memory setting
        if ((ApplicationPreferences.getMaxMemoryMB() <= 128) && (ApplicationPreferences.getMaxMemoryMBLimit() > 256)) {
            final String text = "Your maximum memory setting is low, you should increase it in Preferences.";

            IcyLogger.warn(Icy.class, text);

            if (!Icy.getMainInterface().isHeadLess())
                new ToolTipFrame("<html>" + text + "</html>", 15, "lowMemoryTip");
        }
        else if (ApplicationPreferences.getMaxMemoryMB() < (ApplicationPreferences.getDefaultMemoryMB() / 2)) {
            if (!Icy.getMainInterface().isHeadLess()) {
                new ToolTipFrame(
                        "<html><b>Tip:</b> you can increase your maximum memory in preferences setting.</html>",
                        15,
                        "maxMemoryTip"
                );
            }
        }

        if (!Icy.getMainInterface().isHeadLess()) {
            ToolTipFrame tooltip;
            String message;

            // TODO: 28/09/2023 Add image for each OS (Windows, macOS & Linux)
            // welcome tip !
            message = String.format(
                    "<html>Access the main menu with the new menu bar<br>" + "<img width=\"400\" src=\"%s\" /></html>",
                    //Objects.requireNonNull(Icy.class.getResource("/image/help/main_menu.png"))
                    Objects.requireNonNull(Icy.class.getResource("/image/help/menubar_macOS.png"))
            );
            tooltip = new ToolTipFrame(message, 30, "menubarTip");
            tooltip.setSize(410, 150);

            // new Image Cache !
            message = String.format(
                    "<html><img src=\"%s\" /><br>"
                            + "This new button allow to enable/disable Icy <b>virtual mode</b>.<br><br>"
                            + "Virtual mode will force all new created images to be in <i>virtual mode</i> which mean their data can be stored on disk to spare memory.<br>"
                            + "Note that <i>virtual mode</i> is still experimental and <b>some plugins don't support it</b>"
                            + " (processed data can be lost) so use it carefully and only if you're running out of memory.<br>"
                            + "<i>You can change the image caching settings in Icy preferences</i></html>",
                    Objects.requireNonNull(Icy.class.getResource("/image/help/virtual_mode.jpg"))
            );
            tooltip = new ToolTipFrame(message, 30, "virtualMode");
            tooltip.setSize(380, 240);

            // new Magic Wand!
            message = String.format(
                    "<html><img src=\"%s\" /><br>" + "<b>Magic Wand</b> is now available in Icy !<br><br>"
                            + "You can access its settings from preferences :<br><img src=\"%s\" /></html>",
                    Objects.requireNonNull(Icy.class.getResource("/image/help/magic_wand.png")),
                    Objects.requireNonNull(Icy.class.getResource("/image/help/icy_prefs.png"))
            );
            tooltip = new ToolTipFrame(message, 30, "magicWand");
            tooltip.setSize(300, 260);
        }
    }

    /**
     * Check if Icy was just installed (no extension, no native libraries)
     */
    static void checkFirstInstall() {

    }

    static void fatalError(final Throwable t, final boolean headless) {
        // TODO remove this block
        // hide splashScreen if needed
        //if ((splashScreen != null) && (splashScreen.isVisible()))
        //splashScreen.dispose();

        // show error in console
        IcyLogger.fatal(Icy.class, t, t.getLocalizedMessage());
        // and show error in dialog if not headless
        if (!headless) {
            JOptionPane.showMessageDialog(
                    null,
                    IcyExceptionHandler.getErrorMessage(t, true),
                    "Fatal error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        // exit with error code 1
        System.exit(1);
    }

    /**
     * Restart application with user confirmation
     */
    public static void confirmRestart() {
        confirmRestart(null);
    }

    /**
     * Restart application with user confirmation (custom message)
     */
    public static void confirmRestart(final String message) {
        final String mess;

        if (StringUtil.isEmpty(message))
            mess = "Application need to be restarted so changes can take effect. Do it now ?";
        else
            mess = message;

        if (ConfirmDialog.confirm(mess))
            // restart application now
            exit(true);
    }

    /**
     * Show announcement to restart application
     */
    public static void announceRestart() {
        announceRestart(null);
    }

    /**
     * Show announcement to restart application (custom message)
     */
    public static void announceRestart(final String message) {
        final String mess;

        if (StringUtil.isEmpty(message))
            mess = "Application need to be restarted so changes can take effet.";
        else
            mess = message;

        if (Icy.getMainInterface().isHeadLess()) {
            // just display this message
            IcyLogger.info(Icy.class, mess);
        }
        else {
            new AnnounceFrame(mess, "Restart Now", () -> {
                // restart application now
                exit(true);
            }, 20);
        }
    }

    /**
     * Returns <code>true</code> if application can exit.<br>
     * Shows a confirmation dialog if setting requires it or if it's unsafe to exit now.
     */
    public static boolean canExit(final boolean showConfirm) {
        // we first check if externals listeners allow existing
        if (!getMainInterface().canExitExternal())
            return false;

        // headless mode --> allow exit
        if (Icy.getMainInterface().isHeadLess())
            return true;

        // PluginInstaller or WorkspaceInstaller not running
        final boolean safeExit = !PluginInstaller.isProcessing();

        // not safe, need confirmation
        if (!safeExit) {
            return ConfirmDialog.confirm(
                    "Quit the application",
                    "Some processes are not yet completed, are you sure you want to quit ?",
                    ConfirmDialog.YES_NO_CANCEL_OPTION
            );
        }
        else if (showConfirm && GeneralPreferences.getExitConfirm()) {
            // we need user confirmation
            return IdConfirmDialog.confirm("Quit the application ?", GeneralPreferences.ID_CONFIRM_EXIT);
        }

        return true;
    }

    /**
     * Exit Icy, returns <code>true</code> if the operation should success.<br>
     * Note that the method is asynchronous so you still have a bit of time to execute some stuff before the application
     * actually exit.
     */
    public static boolean exit(final boolean restart) {
        // check we can exit application
        if (!canExit(!restart))
            return false;

        // already existing
        if (exiting && terminer.isAlive()) {
            // set focus on exit frame
            if (exitFrame != null)
                exitFrame.requestFocus();
            // return true;
            return true;
        }

        // we don't want to be in EDT here and avoid BG runner
        // as we test for BG runner completion
        terminer = new Thread(() -> {
            // mark the application as exiting
            exiting = true;

            IcyLogger.info(Icy.class, "Exiting...");

            // get main frame
            final MainFrame mainFrame = Icy.getMainInterface().getMainFrame();

            // disconnect from chat (not needed but preferred)
            // if (mainFrame != null)
            // mainFrame.getChat().disconnect("Icy closed");

            // close all icyFrames (force wait completion)
            ThreadUtil.invokeNow(() -> {
                // for (IcyFrame frame : IcyFrame.getAllFrames())
                // frame.close();
                // close all JInternalFrames
                final JDesktopPane desktopPane = Icy.getMainInterface().getDesktopPane();

                if (desktopPane != null) {
                    for (final JInternalFrame frame : desktopPane.getAllFrames()) {
                        try {
                            try {
                                frame.setClosed(true);
                            }
                            catch (final PropertyVetoException e) {
                                // if (frame.getDefaultCloseOperation() !=
                                // WindowConstants.DISPOSE_ON_CLOSE)
                                frame.dispose();
                            }
                            catch (final Throwable t) {
                                // error on close ? --> try dispose
                                frame.dispose();
                            }
                        }
                        catch (final Throwable t) {
                            // ignore further error here...
                        }
                    }
                }

                // then close all external frames except main frame
                for (final JFrame frame : Icy.getMainInterface().getExternalFrames()) {
                    if (frame != mainFrame) {
                        if (frame instanceof final IcyExternalFrame iFrame) {
                            iFrame.close();
                            if (iFrame.getDefaultCloseOperation() != WindowConstants.DISPOSE_ON_CLOSE)
                                iFrame.dispose();
                        }
                        else
                            frame.dispose();
                    }
                }
            });

            // stop daemon plugin
            ExtensionLoader.stopDaemons();
            // shutdown background processor after frame close
            ThreadUtil.shutdown();
            // shutdown prefetcher
            SequencePrefetcher.shutdown();

            // headless mode
            if (Icy.getMainInterface().isHeadLess()) {
                // final long start = System.currentTimeMillis();
                // // wait 10s max for background processors completed theirs tasks
                // while (!ThreadUtil.isShutdownAndTerminated() && ((System.currentTimeMillis()
                // - start) < 10 * 1000))
                // ThreadUtil.sleep(1);

                // wait that background processors completed theirs tasks
                while (!ThreadUtil.isShutdownAndTerminated())
                    ThreadUtil.sleep(1);
            }
            else {
                // need to create the exit frame in EDT
                ThreadUtil.invokeNow(() -> {
                    // create and display the exit frame
                    exitFrame = new ExitFrame(EXIT_FORCE_DELAY);
                });

                // wait that background processors completed theirs tasks
                while (!ThreadUtil.isShutdownAndTerminated() && !exitFrame.isForced())
                    ThreadUtil.sleep(1);

                // need to dispose the exit frame in EDT (else we can have deadlock)
                ThreadUtil.invokeNow(() -> {
                    // can close the exit frame now
                    exitFrame.dispose();
                });
            }

            // need to dispose the main frame in EDT (else we can have deadlock)
            ThreadUtil.invokeNow(() -> {
                // finally close the main frame
                if (mainFrame != null)
                    mainFrame.dispose();
            });

            // save preferences
            IcyPreferences.save();
            // save audit data
            Audit.save();
            // cache cleanup
            ImageCache.shutDown();

            // clean up native library files
            // unPrepareNativeLibraries();

            // release lock
            if (lock != null)
                SingleInstanceCheck.release(lock);

            final boolean doUpdate = IcyUpdater.getWantUpdate();

            // launch updater if needed
            if (doUpdate || restart)
                IcyUpdater.launchUpdater(doUpdate, restart);

            IcyLogger.info(Icy.class, "Done.");

            // good exit
            System.exit(0);
        });

        terminer.setName("Icy Shutdown");
        terminer.start();

        return true;
    }

    /**
     * Return true is VTK library loaded.
     */
    @Contract(pure = true)
    public static boolean isVtkLibraryLoaded() {
        return vtkLibraryLoaded;
    }

    /**
     * @return {@code true} if the cache module is disabled (not loaded).
     */
    @Contract(pure = true)
    public static boolean isCacheDisabled() {
        return disableCache;
    }

    /**
     * @return {@code true} if the network module is disabled (not loaded).
     */
    @Contract(pure = true)
    public static boolean isNetworkDisabled() {
        return disableNetwork;
    }

    /**
     * Return true is the application is currently exiting.
     */
    @Contract(pure = true)
    public static boolean isExiting() {
        return exiting;
    }

    /**
     * Return the main Icy interface.
     */
    public static MainInterface getMainInterface() {
        // batch mode
        if (mainInterface == null)
            mainInterface = new MainInterfaceBatch();

        return mainInterface;
    }

    /**
     * Returns the command line arguments
     */
    @Contract(pure = true)
    public static String[] getCommandLineArgs() {
        return args;
    }

    /**
     * Returns the plugin command line arguments
     */
    @Contract(pure = true)
    public static String[] getCommandLinePluginArgs() {
        return pluginArgs;
    }

    /**
     * Clear the plugin command line arguments.<br>
     * This method should be called after the launching plugin actually 'consumed' the startup
     * arguments.
     */
    public static void clearCommandLinePluginArgs() {
        pluginArgs = new String[0];
    }

    /**
     * Returns the startup plugin if any
     */
    @Contract(pure = true)
    public static Plugin getStartupPlugin() {
        return startupPlugin;
    }

    /**
     * Return content of the <code>CHANGELOG</code> file
     */
    public static @NotNull String getChangeLog() {
        if (FileUtil.exists("CHANGELOG.md"))
            return new String(FileUtil.load("CHANGELOG.md", false));

        return "";
    }

    /**
     * Return content of the <code>LICENSE</code> file
     */
    public static @NotNull String getLicense() {
        if (FileUtil.exists("LICENSE"))
            return new String(FileUtil.load("LICENSE", false));

        return "";
    }

    /**
     * Return content of the <code>README</code> file
     */
    public static @NotNull String getReadMe() {
        if (FileUtil.exists("README.md"))
            return new String(FileUtil.load("README.md", false));

        return "";
    }

    // TODO remove this code as it should be managed outside Icy, and natives libraries should not be included with Icy by default
    private static boolean copyLibraries(final @NotNull String libName) {
        final File newFolder = new File(UserUtil.getIcyLibrariesDirectory(), libName);
        final File oldFolder = new File("." + File.separator + "lib" + File.separator + SystemUtil.getOSArchIdString() + File.separator + libName);

        // Checking old folder before coying
        if (!oldFolder.exists()) {
            IcyLogger.error(Icy.class, "Could not find old " + libName + " folder. Abort copying!");
            return false;
        }
        final File[] oldLibs = oldFolder.listFiles();
        if (oldLibs == null || oldLibs.length == 0) {
            IcyLogger.error(Icy.class, "Could not find old " + libName + " libraries. Abort copying!");
            return false;
        }

        // Create new folder
        if (!newFolder.exists()) {
            if (newFolder.mkdirs()) {
                IcyLogger.info(Icy.class, "Created " + libName + " folder. Start copying.");
            }
            else {
                IcyLogger.error(Icy.class, "Could not create " + libName + " folder. Abort loading!");
                return false;
            }
        }

        // Start copying
        for (final File oldLib : oldLibs) {
            IcyLogger.debug(Icy.class, "Copying old " + libName + " library: " + oldLib.getName());
            try {
                // Replace files
                Files.copy(oldLib.toPath(), new File(newFolder, oldLib.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (final IOException e) {
                IcyLogger.error(Icy.class, e, "Could not copy old " + libName + " library: " + oldLib.getName());
                //return false;
            }
        }

        final File[] newLibs = newFolder.listFiles();
        if (newLibs != null && oldLibs.length == newLibs.length) {
            IcyLogger.success(Icy.class, "Successfully copied " + newLibs.length + " " + libName + " library files.");
            return true;
        }
        else {
            IcyLogger.error(Icy.class, "An error occured while copying " + libName + " libraries. Abort loading!");
            return false;
        }
    }

    static void nativeLibrariesInit() {
        // build the local native library path
        //final String libPath = LIB_PATH + FileUtil.separator + SystemUtil.getOSArchIdString();
        //final File libPathFile = new File(libPath);
        final File libPathFile = new File(UserUtil.getIcyHomeDirectory(), "libraries");

        if (!libPathFile.exists()) {
            if (libPathFile.mkdirs())
                IcyLogger.info(Icy.class, "Created libraries directory.");
            else {
                IcyLogger.error(Icy.class, "Could not create libraries directory. Abort loading!");
                return;
            }
        }

        // load native libraries
        loadVtkLibrary(libPathFile);

        // disable native lib support for JAI as we don't provide them
        SystemUtil.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }

    private static void loadVtkLibrary(final File libPathFile) {
        IcyLogger.info(Icy.class, "Loading VTK...");
        final File vtkFolder = new File(libPathFile, "vtk");
        if (!vtkFolder.exists())
            if (!copyLibraries("vtk"))
                return;

        final String vtkLibPath = FileUtil.getGenericPath(vtkFolder.getAbsolutePath());

        // we load it directly from inner lib path if possible
        System.setProperty("vtk.lib.dir", vtkLibPath);

        vtkLibraryLoaded = false;
        try {
            final Set<String> nativeLibraries = new HashSet<>(CollectionUtil.asList(FileUtil.getFiles(vtkLibPath, null, false, true)));

            final Set<String> filesToRemove = new HashSet<>();
            for (final String path : nativeLibraries) {
                final String[] split = path.split("\\.");
                final String extension = split[split.length - 1].toLowerCase(Locale.getDefault());
                if (!(extension.equals("dylib") || extension.equals("jnilib") || extension.equals("so") || extension.equals("dll") || path.contains(".so."))) {
                    IcyLogger.warn(Icy.class, String.format("Wrong file format for a native library: %s", path));
                    filesToRemove.add(path);
                }
            }

            if (!filesToRemove.isEmpty())
                nativeLibraries.removeAll(filesToRemove);

            final int numFile = nativeLibraries.size();

            // Show progress, as VTK may take a while to load
            final ProgressFrame pf = new ProgressFrame("Loading VTK libraries: 0 / " + numFile);
            pf.notifyProgress(0, numFile);

            int i = 0;
            boolean load = true;
            // so we can at least load 1 library at each iteration
            while (load) {
                load = false;

                for (final String lib : new ArrayList<>(nativeLibraries)) {
                    // successfully loaded ? one more iteration
                    if (loadLibrary(lib)) {
                        // done
                        nativeLibraries.remove(lib);
                        // try another iteration
                        load = true;
                        i++;
                        pf.notifyProgress(i, numFile);
                        pf.setMessage("Loading VTK libraries: " + i + " / " + numFile);
                    }
                }
            }

            // Close the progress bar, as we do not need it anymore
            pf.close();

            // still some remaining files not loaded ? --> display a warning and prevent VTK trying to load
            if (!nativeLibraries.isEmpty()) {
                for (final String lib : nativeLibraries)
                    IcyLogger.warn(Icy.class, String.format("This VTK library file couldn't be loaded: %s", FileUtil.getFileName(lib)));
                vtkLibraryLoaded = false;
            }
            else
                vtkLibraryLoaded = true;

            // TODO : fix this ! it consider ready even if there are still libraries that are not loaded !
            // at least one file was correctly loaded ? --> the library certainly loaded correctly then
            //if (nativeLibraries.size() < numFile)
            //vtkLibraryLoaded = true;
        }
        catch (final Throwable e1) {
            IcyLogger.error(Icy.class, e1, e1.getLocalizedMessage());
        }

        if (vtkLibraryLoaded) {
            vtkNativeLibrary.DisableOutputWindow(new File(UserUtil.getIcyHomeDirectory(), "vtk.log"));
            final String vv = new vtkVersion().GetVTKVersion();

            IcyLogger.success(Icy.class, String.format("%s v%s (%s) loaded", IcyVTK.NAME, Version.fromString(IcyVTK.VERSION).toShortString(), vv));
        }
        else {
            IcyLogger.error(Icy.class, String.format("%s v%s not loaded", IcyVTK.NAME, Version.fromString(IcyVTK.VERSION).toShortString()));
        }
    }

    private static boolean loadLibrary(final String path) {
        if (FileUtil.exists(path)) {
            try {
                System.load(path);
                return true;
            }
            catch (final Throwable t) {
                // Ignore
            }
        }

        return false;
    }

    @Deprecated(since = "3.0.0")
    static void nativeLibrariesShutdown() {
        // build the native local library path
        final String path = LIB_PATH + FileUtil.separator + SystemUtil.getOSArchIdString();
        // get file list (we don't want hidden files if any)
        File[] libraryFiles = FileUtil.getFiles(new File(path), null, true, false, false);

        // remove previous copied files
        for (final File libraryFile : libraryFiles) {
            // get file in root directory
            final File file = new File(libraryFile.getName());
            // invoke delete on exit if the file exists
            if (file.exists())
                file.deleteOnExit();
        }

        // get file list from temporary native library path
        libraryFiles = FileUtil.getFiles(new File(SystemUtil.getTempLibraryDirectory()), null, true, false, false);

        // remove previous copied files
        for (final File libraryFile : libraryFiles) {
            // delete file
            if (!FileUtil.delete(libraryFile, false))
                libraryFile.deleteOnExit();
        }
    }
}
