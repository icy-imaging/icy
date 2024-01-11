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

package icy.main;

import icy.action.ActionManager;
import icy.common.Version;
import icy.file.FileUtil;
import icy.file.Loader;
import icy.gui.dialog.ConfirmDialog;
import icy.gui.dialog.IdConfirmDialog;
import icy.gui.dialog.IdConfirmDialog.Confirmer;
import icy.gui.frame.ExitFrame;
import icy.gui.frame.IcyExternalFrame;
import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.frame.progress.ToolTipFrame;
import icy.gui.inspector.InspectorPanel;
import icy.gui.main.MainFrame;
import icy.gui.main.MainInterface;
import icy.gui.main.MainInterfaceBatch;
import icy.gui.main.MainInterfaceGui;
import icy.gui.system.NewVersionFrame;
import icy.gui.util.LookAndFeelUtil;
import icy.image.cache.ImageCache;
import icy.math.UnitUtil;
import icy.network.NetworkUtil;
import icy.plugin.*;
import icy.plugin.abstract_.Plugin;
import icy.preferences.ApplicationPreferences;
import icy.preferences.GeneralPreferences;
import icy.preferences.IcyPreferences;
import icy.preferences.PluginPreferences;
import icy.sequence.Sequence;
import icy.sequence.SequencePrefetcher;
import icy.system.AppleUtil;
import icy.system.IcyExceptionHandler;
import icy.system.SingleInstanceCheck;
import icy.system.SystemUtil;
import icy.system.audit.Audit;
import icy.system.logging.IcyLogger;
import icy.system.thread.ThreadUtil;
import icy.type.collection.CollectionUtil;
import icy.update.IcyUpdater;
import icy.util.StringUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vtk.vtkNativeLibrary;
import vtk.vtkVersion;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyVetoException;
import java.io.File;
import java.nio.channels.FileLock;
import java.util.List;
import java.util.*;

/**
 * Entry point for Icy.
 *
 * @author Stephane
 * @author Thomas MUSSET
 */
public class Icy {
    public static final String LIB_PATH = "lib";
    public static final int EXIT_FORCE_DELAY = 3000;

    /**
     * Icy Version
     */
    public static final Version VERSION = new Version(3, 0, 0, Version.Snapshot.ALPHA);

    /**
     * Main interface
     */
    private static MainInterface mainInterface = null;

    /**
     * Unique instance checker
     */
    static FileLock lock = null;

    // TODO remove splashscreen ?
    /*
     * private splash for initial loading
     */
    //static SplashScreenFrame splashScreen = null;

    /**
     * VTK library loaded flag
     */
    static boolean vtkLibraryLoaded = false;

    /**
     * ITK library loaded flag
     */
    static boolean itkLibraryLoaded = false;

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
    @NotNull
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
    public static void main(@NotNull final String[] args) {
        boolean headless = false;

        IcyLogger.setConsoleLevel(IcyLogger.DEBUG);
        IcyLogger.setGUILevel(IcyLogger.ERROR);

        try {
            IcyLogger.debug("Initializing...");

            // handle arguments (must be the first thing to do)
            headless = handleAppArgs(args);

            // force headless if we have a headless system
            if (!headless && GraphicsEnvironment.isHeadless())
                headless = true;

            // initialize preferences
            IcyPreferences.init();

            // check if Icy is already running
            lock = SingleInstanceCheck.lock("icy");
            if (lock == null) {
                // we always accept multi instance in headless mode
                if (!headless) {
                    // we need to use our custom ConfirmDialog as
                    // Icy.getMainInterface().isHeadless() will return false here
                    final Confirmer confirmer = new Confirmer(
                            "Confirmation",
                            "Icy is already running on this computer. Start anyway ?",
                            JOptionPane.YES_NO_OPTION,
                            ApplicationPreferences.ID_SINGLE_INSTANCE
                    );
                    ThreadUtil.invokeNow(confirmer);

                    if (!confirmer.getResult()) {
                        IcyLogger.info("Exiting...");
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
            loci.common.DebugTools.enableLogging("ERROR");

            /*if (!headless && !noSplash) {
                // prepare splashScreen (ok to create it here as we are not yet in substance laf)
                splashScreen = new SplashScreenFrame();

                // It's important to initialize AWT now (with InvokeNow(...) for instance) to avoid
                // the JVM deadlock bug (id: 5104239). It happen when the AWT thread is initialized
                // while others threads load some new library with ClassLoader.loadLibrary

                // display splash NOW (don't use ThreadUtil as headless is still false here)
                EventQueue.invokeAndWait(() -> {
                    // display splash screen
                    splashScreen.setVisible(true);
                });
            }*/

            // fast start
            // force image cache initialization so GUI won't wait after it (need preferences init)
            new Thread(ImageCache::isEnabled, "Initializer: Cache").start();

            // initialize network (need preferences init)
            new Thread(NetworkUtil::init, "Initializer: Network").start();

            // load plugins classes (need preferences init)
            new Thread(PluginLoader::reloadAsynch, "Initializer: Plugin").start();

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
            // do it on AWT thread NOW as this is what we want first
            ThreadUtil.invokeNow(() -> {
                try {
                    // init IconFontSwing with GoogleMaterialIcons
                    IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
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
        }
        else {
            // simple main interface init
            getMainInterface().init();
        }

        // splash screen initialized --> hide it
        /*if (splashScreen != null) {
            // then do less important stuff later
            ThreadUtil.invokeLater(() -> {
                // we can now hide splash as we have interface
                splashScreen.dispose();
                splashScreen = null;
            });
        }*/

        // show general informations
        IcyLogger.info(String.format("%s %s (%d bit)", SystemUtil.getJavaName(), SystemUtil.getJavaVersion(), SystemUtil.getJavaArchDataModel()));
        IcyLogger.info(String.format("Running on %s %s (%s)", SystemUtil.getOSName(), SystemUtil.getOSVersion(), SystemUtil.getOSArch()));
        IcyLogger.info(String.format("System total memory: %s", UnitUtil.getBytesString(SystemUtil.getTotalMemory())));
        IcyLogger.info(String.format("System available memory: %s", UnitUtil.getBytesString(SystemUtil.getFreeMemory())));
        IcyLogger.info(String.format("Max Java memory: %s", UnitUtil.getBytesString(SystemUtil.getJavaMaxMemory())));

        // image cache disabled from command line ?
        // TODO: 16/10/2023 Replace this with new Inspector mode
        if (isCacheDisabled()) {
            IcyLogger.info("Image cache is disabled.");

            // disable virtual mode button from inspector
            final InspectorPanel inspector = getMainInterface().getInspector();
            if (inspector != null)
                inspector.imageCacheDisabled();
        }
        // virtual mode enabled ? --> initialize image cache
        else if (InspectorPanel.getVirtualMode())
            ImageCache.init(ApplicationPreferences.getCacheMemoryMB(), ApplicationPreferences.getCachePath());

        if (headless) {
            IcyLogger.info("Headless mode.");
        }

        // initialize OSX specific GUI stuff
        if (!headless && SystemUtil.isMac())
            AppleUtil.init();
        // initialize exception handler
        IcyExceptionHandler.init();
        // initialize action manager
        if (!headless)
            ActionManager.init();
        // prepare native library files (need preferences init)
        nativeLibrariesInit();

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
            if (GeneralPreferences.getAutomaticUpdate())
                IcyUpdater.checkUpdate(true);
            // check for plugin update
            if (PluginPreferences.getAutomaticUpdate())
                PluginUpdater.checkUpdate(true);

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

        IcyLogger.info(String.format("Icy v%s started.", VERSION.toShortString()));

        checkParameters();

        // handle startup arguments
        if (startupImage != null && !startupImage.isEmpty() && !startupImage.isBlank())
            Icy.getMainInterface().addSequence(Loader.loadSequence(FileUtil.getGenericPath(startupImage), 0, false));

        // wait while updates are occurring before starting command line plugin...
        while (PluginUpdater.isCheckingForUpdate() || PluginInstaller.isProcessing())
            ThreadUtil.sleep(1);

        if (startupPluginName != null) {
            PluginLoader.waitWhileLoading();

            final PluginDescriptor plugin = PluginLoader.getPlugin(startupPluginName);

            if (plugin == null) {
                IcyLogger.error(String.format("Could not launch plugin '%s': the plugin was not found.", startupPluginName));
                IcyLogger.info("Be sure you correctly wrote the complete class name and respected the case.");
                IcyLogger.info("Ex: plugins.mydevid.analysis.MyPluginClass");
            }
            else
                startupPlugin = PluginLauncher.start(plugin);
        }

        // headless mode ? we can exit now...
        if (headless && !noHLExit)
            exit(false);
    }

    private static boolean handleAppArgs(@NotNull final String[] args) {
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
                // special flag to disabled JCL (needed for development)
            else if (arg.equalsIgnoreCase("--disableJCL") || arg.equalsIgnoreCase("-dJCL"))
                PluginLoader.setJCLDisabled(true);
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

            IcyLogger.warn(text);

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

    static void fatalError(@NotNull final Throwable t, final boolean headless) {
        // hide splashScreen if needed
        /*if ((splashScreen != null) && (splashScreen.isVisible()))
            splashScreen.dispose();*/

        // show error in console
        IcyExceptionHandler.showErrorMessage(t, true);
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
    public static void announceRestart(@Nullable final String message) {
        final String mess;

        if (StringUtil.isEmpty(message))
            mess = "Application need to be restarted so changes can take effet.";
        else
            mess = message;

        if (Icy.getMainInterface().isHeadLess()) {
            // just display this message
            IcyLogger.info(mess);
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

            IcyLogger.info("Exiting...");

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
            PluginLoader.stopDaemons();
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

            IcyLogger.info("Done.");

            // good exit
            System.exit(0);
        });

        terminer.setName("Icy Shutdown");
        terminer.start();

        return true;
    }

    /**
     * @deprecated use <code>exit(boolean)</code> instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static boolean exit(final boolean restart, final boolean force) {
        return exit(restart);
    }

    /**
     * Return true is VTK library loaded.
     */
    public static boolean isVtkLibraryLoaded() {
        return vtkLibraryLoaded;
    }

    /**
     * Return true is VTK library loaded.
     */
    public static boolean isItkLibraryLoaded() {
        return itkLibraryLoaded;
    }

    /**
     * @deprecated Use {@link MainInterface#isHeadLess()} instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static boolean isHeadLess() {
        return getMainInterface().isHeadLess();
    }

    /**
     * @deprecated Use {@link #isCacheDisabled()} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static boolean isCacheEnabled() {
        return !isCacheDisabled();
    }

    /**
     * @deprecated Use {@link #isNetworkDisabled()} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static boolean isNetworkEnabled() {
        return !isNetworkDisabled();
    }

    /**
     * @return {@code true} if the cache module is disabled (not loaded).
     */
    public static boolean isCacheDisabled() {
        return disableCache;
    }

    /**
     * @return {@code true} if the network module is disabled (not loaded).
     */
    public static boolean isNetworkDisabled() {
        return disableNetwork;
    }

    /**
     * Return true is the application is currently exiting.
     */
    public static boolean isExiting() {
        return exiting;
    }

    /**
     * Return the main Icy interface.
     */
    @NotNull
    public static MainInterface getMainInterface() {
        // batch mode
        if (mainInterface == null)
            mainInterface = new MainInterfaceBatch();

        return mainInterface;
    }

    /**
     * Returns the command line arguments
     */
    @NotNull
    public static String[] getCommandLineArgs() {
        return args;
    }

    /**
     * Returns the plugin command line arguments
     */
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
    public static Plugin getStartupPlugin() {
        return startupPlugin;
    }

    /**
     * Return content of the <code>CHANGELOG</code> file
     */
    public static String getChangeLog() {
        if (FileUtil.exists("CHANGELOG.md"))
            return new String(FileUtil.load("CHANGELOG.md", false));

        return "";
    }

    /**
     * Return content of the <code>LICENSE</code> file
     */
    public static String getLicense() {
        if (FileUtil.exists("LICENSE"))
            return new String(FileUtil.load("LICENSE", false));

        return "";
    }

    /**
     * Return content of the <code>README</code> file
     */
    public static String getReadMe() {
        if (FileUtil.exists("README.md"))
            return new String(FileUtil.load("README.md", false));

        return "";
    }

    /**
     * @deprecated Uses <code>Icy.getMainInterface().addSequence(Sequence)</code> instead.
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static void addSequence(final Sequence sequence) {
        Icy.getMainInterface().addSequence(sequence);
    }

    static void nativeLibrariesInit() {
        // build the local native library path
        final String libPath = LIB_PATH + FileUtil.separator + SystemUtil.getOSArchIdString();
        final File libPathFile = new File(libPath);

        // load native libraries
        loadVtkLibrary(libPathFile);

        // disable native lib support for JAI as we don't provide them
        SystemUtil.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }

    private static void loadVtkLibrary(final File libPathFile) {
        final String vtkLibPath = FileUtil.getGenericPath(new File(libPathFile, "vtk").getAbsolutePath());

        // we load it directly from inner lib path if possible
        System.setProperty("vtk.lib.dir", vtkLibPath);

        vtkLibraryLoaded = false;
        try {
            final Set<String> nativeLibraries = new HashSet<>(CollectionUtil.asList(FileUtil.getFiles(vtkLibPath, null, false, false)));

            final Set<String> filesToRemove = new HashSet<>();
            for (final String path : nativeLibraries) {
                final String[] split = path.split("\\.");
                final String extension = split[split.length - 1].toLowerCase(Locale.ROOT);
                if (!(extension.equals("dylib") || extension.equals("jnilib") || extension.equals("so") || extension.equals("dll") || path.contains(".so."))) {
                    IcyLogger.warn(String.format("Wrong file format for a native library: %s", path));
                    filesToRemove.add(path);
                }
            }

            if (!filesToRemove.isEmpty())
                nativeLibraries.removeAll(filesToRemove);

            final int numFile = nativeLibraries.size();
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
                    }
                }
            }

            // still some remaining files not loaded ? --> display a warning
            if (!nativeLibraries.isEmpty())
                for (final String lib : nativeLibraries)
                    IcyLogger.warn(String.format("This VTK library file couldn't be loaded: %s", FileUtil.getFileName(lib)));

            // at least one file was correctly loaded ? --> the library certainly loaded correctly then
            if (nativeLibraries.size() < numFile)
                vtkLibraryLoaded = true;
        }
        catch (final Throwable e1) {
            IcyExceptionHandler.showErrorMessage(e1, false, false);
        }

        if (vtkLibraryLoaded) {
            vtkNativeLibrary.DisableOutputWindow(new File("vtk.log"));
            final String vv = new vtkVersion().GetVTKVersion();

            final String message = String.format("VTK %s library successfully loaded.", vv);
            IcyLogger.success(message);
        }
        else {
            final String message = "Cannot load VTK library.";
            IcyLogger.error(message);
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
