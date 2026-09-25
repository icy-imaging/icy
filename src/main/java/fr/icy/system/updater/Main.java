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

package fr.icy.system.updater;

import com.formdev.flatlaf.FlatLightLaf;
import fr.icy.common.Version;
import fr.icy.common.string.StringUtil;
import fr.icy.io.FileUtil;
import fr.icy.network.NetworkUtil;
import fr.icy.network.WebInterface;
import fr.icy.network.update.ElementDescriptor;
import fr.icy.network.update.Updater;
import fr.icy.shared.logging.CustomLevel;
import fr.icy.shared.logging.LogConfig;
import fr.icy.shared.logging.LogManager;
import fr.icy.system.SystemUtil;
import fr.icy.system.preferences.ApplicationPreferences;
import fr.icy.system.preferences.IcyPreferences;
import fr.icy.system.thread.ThreadUtil;
import org.jspecify.annotations.NonNull;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    static class OutPrintStream extends PrintStream {
        boolean isStdErr;

        public OutPrintStream(final PrintStream out, final boolean isStdErr) {
            super(out);

            this.isStdErr = isStdErr;
        }

        @Override
        public void write(final byte @NonNull [] buf, final int off, final int len) {
            super.write(buf, off, len);

            if ((off < 0) || (off > buf.length) || (len < 0) || ((off + len) > buf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            }
            else if (len == 0) {
                return;
            }

            final String str = new String(buf, off, len);

            strLog += str;
            if (frame != null)
                frame.addMessage(str, isStdErr);
        }
    }

    private static final String ICY_JAR_NAME = "icy.jar";
    private static final String ICY_FOLDER_OSX = "Icy.app";

    private static final String PARAM_MAX_MEMORY = "-Xmx";
    private static final String PARAM_STACK_SIZE = "-Xss";

    /**
     * Min java target to execute Icy
     */
    private static final short MIN_JAVA_TARGET = 21;

    /**
     * Updater Version
     */
    public static final Version VERSION = new Version(3, 0, 0, Version.DevelopmentStage.ALPHA);

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    static final OutPrintStream stdStream = new OutPrintStream(System.out, false);
    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    static final OutPrintStream errStream = new OutPrintStream(System.err, true);

    static UpdateFrame frame = null;
    static String extraArgs = "";

    static String strLog = "";

    /**
     * @param args Received from the command line.
     */
    public static void main(final String @NonNull [] args) throws IOException {
        LogManager.init(LogConfig.builder()
                .consoleEnabled(true)
                .consoleLevel(CustomLevel.CONFIG)
                .logFile("%h/.icy/icy_updater_%g.log")
                .fileLevel(Level.WARNING)
                .maxFileBytes(10 * 1024 * 1024)
                .fileCount(5)
                .build()
        );

        boolean start = true;
        boolean update = false;

        // load preferences
        IcyPreferences.init();
        // update proxy setting
        NetworkUtil.updateNetworkSetting();

        for (final String arg : args) {
            if (arg.trim().equalsIgnoreCase(Updater.ARG_UPDATE))
                update = true;
            else if (arg.trim().equalsIgnoreCase(Updater.ARG_NOSTART))
                start = false;
        }

        // keep trace of other arguments
        for (final String arg : args) {
            if (!(arg.trim().equalsIgnoreCase(Updater.ARG_UPDATE) || arg.trim().equalsIgnoreCase(Updater.ARG_NOSTART)))
                extraArgs = extraArgs.concat(" ").concat(arg);
        }

        // redirect stdOut and errOut for update
        System.setOut(stdStream);
        System.setErr(errStream);

        // no error --> we can exit
        if (process(update, start)) {
            if (frame != null) {
                // we got some error messages on starting, so we wait for 5 seconds before closing frame
                if (!update && frame.isVisible())
                    ThreadUtil.sleep(5000);
                frame.dispose();
            }

            System.exit(0);
        }
        else {
            if (frame != null) frame.setCanClose(true);
            else System.exit(1);
        }
    }

    private static boolean process(final boolean update, final boolean start) {
        // prepare GUI
        ThreadUtil.invokeNow(() -> {
            if (GraphicsEnvironment.isHeadless())
                frame = null;
            else {
                try {
                    UIManager.setLookAndFeel(new FlatLightLaf());
                }
                catch (final UnsupportedLookAndFeelException e) {
                    // ignore
                }
                frame = new UpdateFrame("Icy Updater");
                // update --> show progress frame immediately
                if (update)
                    frame.setVisible(true);
            }
        });

        // get Icy directory and path
        final String directory = FileUtil.getApplicationDirectory();

        if (update) {
            final String icyJarPath = directory + FileUtil.separatorChar + ICY_JAR_NAME;

            // wait for lock
            if (!waitForLock(icyJarPath)) {
                LOGGER.severe(String.format("File %s is locked, aborting update…", icyJarPath));

                // send report of the error
                report(strLog);

                return false;
            }

            // do update
            if (!doUpdate())
                return false;

            // update successful ? check we are on OSX
            if (SystemUtil.isMac()) {
                // we may want to rename Icy folder to .app
                final File oldFile = new File("").getAbsoluteFile();
                final File parentFile = oldFile.getParentFile();

                // not a macOS application? --> rename folder to make a proper app
                if (!oldFile.getAbsolutePath().toLowerCase().endsWith(".app")) {
                    final File newFile = new File(parentFile, ICY_FOLDER_OSX).getAbsoluteFile();

                    // do not already exist? --> can rename
                    if (!newFile.exists()) {
                        // try to rename Icy folder
                        if (!doOSXFolderNameUpdate(parentFile, oldFile, newFile))
                            return false;

                        // we cannot restart Icy the classic way, so do it via the OSX app open
                        if (start)
                            return startICY_OSX(newFile.getAbsolutePath(), parentFile.getAbsolutePath());

                        return true;
                    }
                    // cannot rename
                    else {
                        return false;
                    }
                }
                else {
                    // we cannot restart Icy the classic way, so do it via the OSX app open
                    if (start)
                        return startICY_OSX(oldFile.getAbsolutePath(), parentFile.getAbsolutePath());

                    return true;
                }
            }
        }

        // start ICY
        if (start)
            return startICY(directory);

        return true;
    }

    /**
     * Process the update.<br>
     * Working directory should be the Icy directory, else the update won't work.
     */
    public static boolean doUpdate() {
        setState("Checking java version", 1);

        if (!checkMinimumJavaVersion()) {
            LOGGER.severe(String.format("Icy %s requires Java %d or above, please update your java version.", VERSION.toShortString(), MIN_JAVA_TARGET));
            LOGGER.severe("You can download Java here: https://icy.bioimageanalysis.org/download/");
            return false;
        }

        ArrayList<ElementDescriptor> localElements = Updater.getLocalElements();
        final ArrayList<ElementDescriptor> updateElements = Updater.getUpdateElements(localElements);
        boolean result = true;

        // get list of element to update
        for (final ElementDescriptor updateElement : updateElements) {
            final String updateElementName = updateElement.getName();

            // can't update updater here (it should be already updated)
            if (updateElementName.equals(Updater.ICYUPDATER_NAME)) {
                // just update local element with update element info
                Updater.updateElementInfos(updateElement, localElements);
                continue;
            }

            setState("Updating : " + updateElementName, (updateElements.indexOf(updateElement) * 60) / updateElements.size());

            try {
                // update element
                if (!Updater.udpateElement(updateElement, localElements)) {
                    // an error happened --> take back current local elements
                    localElements = Updater.getLocalElements();
                    // remove the faulty element information, this will force
                    // update next time.
                    Updater.clearElementInfos(updateElement, localElements);

                    // error while updating, no need to go further…
                    result = false;
                    break;
                }
            }
            catch (final InterruptedException exc) {
                LOGGER.warning("Process interrupted !");
                result = false;
            }
        }

        // some files haven't been updated?
        setState("Checking…", 60);

        if (!result) {
            LOGGER.severe("Update processing has failed.");

            // delete update directory to restart update from scratch
            FileUtil.delete(Updater.UPDATE_DIRECTORY, true);

            // restore backup
            if (Updater.restore()) {
                LOGGER.info("Files correctly restored.");
                // delete backup directory as we don't need it anymore
                FileUtil.delete(Updater.BACKUP_DIRECTORY, true);
            }
            else {
                LOGGER.severe("Some files cannot be restored, try to restore them manually from 'backup' directory.");
                LOGGER.severe("If Icy doesn't start anymore you may need to reinstall the application.");
            }

            // validate elements
            Updater.validateElements(localElements);
            // and save them
            if (!Updater.saveElementsToXML(localElements, Updater.VERSION_NAME, false))
                LOGGER.severe(String.format("Error while saving %s file.", Updater.VERSION_NAME));

            // send report of the error
            report(strLog);
        }
        else {
            // delete obsolete files
            setState("Deleting obsoletes…", 60);
            Updater.deleteObsoletes();

            // cleanup
            setState("Cleaning…", 70);
            FileUtil.delete(Updater.UPDATE_DIRECTORY, true);
            FileUtil.delete(Updater.BACKUP_DIRECTORY, true);

            if (updateElements.isEmpty())
                LOGGER.info("Nothing to update.");
            else {
                // update XML version file
                setState("Updating XML…", 90);

                // validate elements (this actually removes obsoletes files)
                Updater.validateElements(localElements);

                if (!Updater.saveElementsToXML(localElements, Updater.VERSION_NAME, false)) {
                    LOGGER.warning(String.format("Error while saving %s file.", Updater.VERSION_NAME));
                    LOGGER.warning("The new version is correctly installed but version number information will stay outdated until the next update.");
                }
                else
                    LOGGER.info("Update successfully completed.");
            }
        }

        if (result)
            setState("Succeed !", 100);
        else
            setState("Failed !", 100);

        return result;
    }

    /**
     * Rename 'icy' folder to 'icy.app' for OSX if needed
     */
    private static boolean doOSXFolderNameUpdate(final @NonNull File parentFile, final @NonNull File oldFile, final @NonNull File newFile) {
        final String parentFolder = FileUtil.getGenericPath(parentFile.getAbsolutePath());
        //final String cmd = "mv " + oldFile.getAbsolutePath() + " " + newFile.getAbsolutePath();
        final String[] cmdArray = {"mv", oldFile.getAbsolutePath(), newFile.getAbsolutePath()};

        try {
            Process p;

            LOGGER.info(String.format("Renaming app: %s", String.join(" ", cmdArray)));
            // execute it from parent folder for safety
            p = SystemUtil.exec(cmdArray, parentFolder);
            if (p == null)
                return false;

            // wait a bit
            Thread.sleep(1000);
            // output process stream
            outputStreams(p);
            // get error code
            LOGGER.config(String.format("exit code = %d", p.waitFor()));
            // wait a bit
            Thread.sleep(1000);

            LOGGER.info("Removing security check…");
            // remove quarantine attribute from the new-created icy.app
            //p = SystemUtil.exec("xattr -dr com.apple.quarantine " + newFile.getAbsolutePath(), parentFolder);
            p = SystemUtil.exec(new String[]{"xattr", "-dr", "com.apple.quarantine", newFile.getAbsolutePath()}, parentFolder);
            if (p == null)
                return false;

            // wait a bit
            Thread.sleep(1000);
            // output process stream
            outputStreams(p);
            // get error code
            LOGGER.config(String.format("exit code = %d", p.waitFor()));
            // wait a bit
            Thread.sleep(1000);

            return true;
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }

        return false;
    }

    private static boolean checkMinimumJavaVersion() {
        return SystemUtil.getJavaVersionAsNumber() >= (double) Main.MIN_JAVA_TARGET;
    }

    @Deprecated(since = "3.0.0", forRemoval = true)
    private static String getVMParams() {
        // get JVM parameters stored in preferences
        final int maxMemory = ApplicationPreferences.getMaxMemoryMB();
        final int stackSize = ApplicationPreferences.getStackSizeKB();
        final String vmParams = ApplicationPreferences.getExtraVMParams();
        final String osVmParams = ApplicationPreferences.getOSExtraVMParams();

        String result = "";

        if (maxMemory != -1)
            result += " " + PARAM_MAX_MEMORY + maxMemory + "m";
        if (stackSize != -1)
            result += " " + PARAM_STACK_SIZE + stackSize + "k";
        if (!StringUtil.isEmpty(vmParams))
            result += " " + vmParams;
        if (!StringUtil.isEmpty(osVmParams))
            result += " " + osVmParams;

        return result;
    }

    private static String @NonNull [] getVMParamsArray() {
        // get JVM parameters stored in preferences
        final int maxMemory = ApplicationPreferences.getMaxMemoryMB();
        final int stackSize = ApplicationPreferences.getStackSizeKB();
        final String vmParams = ApplicationPreferences.getExtraVMParams();
        final String osVmParams = ApplicationPreferences.getOSExtraVMParams();

        final List<String> result = new ArrayList<>();

        if (maxMemory != -1) {
            result.add(PARAM_MAX_MEMORY.concat(String.valueOf(maxMemory).concat("m")));
        }
        if (stackSize != -1) {
            result.add(PARAM_STACK_SIZE.concat(String.valueOf(stackSize).concat("k")));
        }
        if (!StringUtil.isEmpty(vmParams))
            result.add(vmParams);
        if (!StringUtil.isEmpty(osVmParams))
            result.add(osVmParams);

        return result.toArray(new String[0]);
    }

    private static String getAppParams() {
        // get app parameters stored in preferences
        return ApplicationPreferences.getAppParams();
    }

    public static boolean startICY(final String directory) {
        setState("Launching Icy…", 0);
        if (frame != null)
            frame.setProgressVisible(false);

        // start icy
        final Process process = SystemUtil.execJAR(ICY_JAR_NAME, getVMParamsArray(), getAppParams() + extraArgs, directory);

        // process isn't even created --> critical error
        if (process == null) {
            LOGGER.severe(String.format("Can't launch execJAR(%s, %s, %s, %s, %s)", ICY_JAR_NAME, Arrays.toString(getVMParamsArray()), getAppParams(), extraArgs, directory));
            return false;
        }

        try {
            // wait a bit that stream has been filled
            ThreadUtil.sleep(2000);
            // flush stream so process correctly exit on error
            outputStreams(process);

            // check if we got an error
            if (process.exitValue() != 0) {
                try {
                    setState("Error while launching Icy", 0);

                    LOGGER.severe(String.format("Can't launch execJAR(%s, %s, %s, %s, %s)", ICY_JAR_NAME, Arrays.toString(getVMParamsArray()), getAppParams(), extraArgs, directory));
                    LOGGER.severe("Trying to launch without specific parameters…");
                }
                catch (final Exception e) {
                    // ignore
                }

                return startICYSafeMode(directory);
            }
        }
        catch (final IllegalThreadStateException e) {
            // thread still active --> means Icy properly launched!
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error while launching Icy.", e);
            return false;
        }

        return true;
    }

    public static boolean startICYSafeMode(final String directory) {
        setState("Launching Icy (safe mode)…", 0);
        if (frame != null)
            frame.setProgressVisible(false);

        // start icy in safe mode (no parameters)
        final Process process = SystemUtil.execJAR(ICY_JAR_NAME, new String[0], "", directory);

        // process isn't even created --> critical error
        if (process == null) {
            LOGGER.severe(String.format("Can't launch execJAR(%s, \"\", \"\", %s)", ICY_JAR_NAME, directory));
            LOGGER.severe("Try to manually launch the following command : java -jar updater.jar");
            return false;
        }

        try {
            // we cannot use process.waitFor() here as it hangs forever
            ThreadUtil.sleep(2000);
            // output process stream
            outputStreams(process);

            // got an error?
            if (process.exitValue() != 0) {
                try {
                    setState("Error while launching Icy (safe mode)", 0);
                    LOGGER.severe(String.format("Can't launch execJAR(%s, \"\", \"\", %s)", ICY_JAR_NAME, directory));
                    LOGGER.severe("Try to manually launch the following command : java -jar updater.jar");
                }
                catch (final Exception e) {
                    // ignore
                }

                return false;
            }
        }
        catch (final IllegalThreadStateException e) {
            // thread still active --> means Icy properly launched!
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error while launching Icy", e);
            return false;
        }

        return true;
    }

    public static boolean startICY_OSX(final String appPackage, final String directory) {
        setState("Launching Icy (OSX mode)…", 0);
        if (frame != null)
            frame.setProgressVisible(false);

        // start icy in safe mode (no parameters)
        //final Process process = SystemUtil.exec("open -a " + appPackage, directory);
        final Process process = SystemUtil.exec(new String[]{"open", "-a", appPackage}, directory);

        // process isn't even created --> critical error
        if (process == null) {
            LOGGER.severe("Can't launch Icy..");
            LOGGER.severe("Try to launch it manually.");
            return false;
        }

        try {
            // wait a bit that stream has been filled
            ThreadUtil.sleep(2000);
            // flush stream so process correctly exit on error
            outputStreams(process);

            // check if we got an error
            if (process.exitValue() != 0) {
                try {
                    setState("Error while launching Icy", 0);
                    LOGGER.severe("Can't launch Icy..");
                    LOGGER.severe("Try to launch it manually.");
                }
                catch (final Exception e) {
                    // ignore
                }

                return false;
            }
        }
        catch (final IllegalThreadStateException e) {
            // thread still active --> means Icy properly launched!
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error while launching Icy", e);
            return false;
        }

        return true;
    }

    private static void setState(final String state, final int progress) {
        ThreadUtil.invokeLater(() -> {
            if (frame != null) {
                frame.setTitle(state);
                frame.setProgress(progress);
            }
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean waitForLock(final String lockName) {
        final long start = System.currentTimeMillis();
        final File f = new File(lockName);

        // may help
        if (!f.setWritable(true, false))
            f.setWritable(true, true);

        // ensure lock exist
        if (f.exists()) {
            // wait while file is lock (we wait 15 seconds at max)
            while ((!f.canWrite()) && ((System.currentTimeMillis() - start) < (15 * 1000))) {
                System.gc();
                ThreadUtil.sleep(100);

                // may help
                if (!f.setWritable(true, false))
                    f.setWritable(true, true);
            }

            return f.canWrite();
        }

        return true;
    }

    /**
     * Report an error log to the Icy website.
     *
     * @param errorLog Error log to report.
     */
    private static void report(final String errorLog) {
        final Map<String, String> values = new HashMap<>();

        values.put(NetworkUtil.ID_PLUGINCLASSNAME, "");
        values.put(NetworkUtil.ID_ERRORLOG, "Updater version " + VERSION + "\n\n" + errorLog);

        // send report
        try {
            NetworkUtil.postData(WebInterface.BASE_URL, values);
        }
        catch (final IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to send report.", e);
        }
    }

    private static void outputStreams(final @NonNull Process process) {
        final BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        final OutputStream outStr = process.getOutputStream();
        final BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        try {
            outStr.write(' ');
            while (errReader.ready())
                LOGGER.severe(errReader.readLine());
            while (inReader.ready())
                LOGGER.info(inReader.readLine());
        }
        catch (final Exception e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
    }
}
