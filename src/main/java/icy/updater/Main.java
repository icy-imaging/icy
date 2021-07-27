/**
 * 
 */
package icy.updater;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import icy.common.Version;
import icy.file.FileUtil;
import icy.network.NetworkUtil;
import icy.preferences.ApplicationPreferences;
import icy.preferences.IcyPreferences;
import icy.system.SystemUtil;
import icy.system.thread.ThreadUtil;
import icy.update.ElementDescriptor;
import icy.update.Updater;
import icy.util.StringUtil;

/**
 * @author stephane
 */
public class Main
{
    // private static final String ICY_MAINCLASS = "icy.main.Icy";

    static class OutPrintStream extends PrintStream
    {
        boolean isStdErr;

        public OutPrintStream(PrintStream out, boolean isStdErr)
        {
            super(out);

            this.isStdErr = isStdErr;
        }

        @Override
        public void write(byte[] buf, int off, int len)
        {
            super.write(buf, off, len);

            if (buf == null)
            {
                throw new NullPointerException();
            }
            else if ((off < 0) || (off > buf.length) || (len < 0) || ((off + len) > buf.length) || ((off + len) < 0))
            {
                throw new IndexOutOfBoundsException();
            }
            else if (len == 0)
            {
                return;
            }

            final String str = new String(buf, off, len);

            strLog += str;
            if (frame != null)
                frame.addMessage(str, isStdErr);
        }
    }

    /**
     * Keep for older version compatibility
     * 
     * @deprecated
     */
    @Deprecated
    private static final String ARG_NOUPDATE = "-noupdate";
    /**
     * Keep for older version compatibility
     * 
     * @deprecated
     */
    @Deprecated
    private static final String ARG_RESTART = "-restart";

    private static final String ICY_JARNAME = "icy.jar";
    private static final String ICY_FOLDER_OSX = "Icy.app";

    private static final String PARAM_MAX_MEMORY = "-Xmx";
    private static final String PARAM_STACK_SIZE = "-Xss";

    /**
     * Updater Version
     */
    public static Version version = new Version("2.1.2.0");

    static final OutPrintStream stdStream = new OutPrintStream(System.out, false);
    static final OutPrintStream errStream = new OutPrintStream(System.err, true);

    static UpdateFrame frame = null;
    static String extraArgs = "";

    static String strLog = "";

    /**
     * @param args
     *        Received from the command line.
     */
    public static void main(String[] args)
    {
        boolean oldVersion = false;
        boolean start = true;
        boolean update;

        // load preferences
        IcyPreferences.init();
        // update proxy setting
        NetworkUtil.updateNetworkSetting();

        // detect if old ICY request update
        for (String arg : args)
            if (arg.equals(ARG_RESTART))
                oldVersion = true;

        if (oldVersion)
        {
            update = true;

            for (String arg : args)
                if (arg.equals(ARG_NOUPDATE))
                    update = false;
        }
        else
        {
            update = false;

            for (String arg : args)
            {
                if (arg.equals(Updater.ARG_UPDATE))
                    update = true;
                else if (arg.equals(Updater.ARG_NOSTART))
                    start = false;
            }
        }

        // keep trace of others arguments
        for (String arg : args)
        {
            if (!(arg.equals(Updater.ARG_UPDATE) || arg.equals(Updater.ARG_NOSTART) || arg.equals(ARG_NOUPDATE)
                    || arg.equals(ARG_RESTART)))
                extraArgs = extraArgs + " " + arg;
        }

        // redirect stdOut and errOut for update
        System.setOut(stdStream);
        System.setErr(errStream);

        // no error --> we can exit
        if (process(update, start))
        {
            if (frame != null)
            {
                // we got some error messages on starting so we wait for 5 seconds before closing frame
                if (!update && frame.isVisible())
                    ThreadUtil.sleep(5000);
                frame.dispose();
            }

            System.exit(0);
        }
        else
        {
            if (frame != null) frame.setCanClose(true);
            else System.exit(1);
        }
    }

    private static boolean process(final boolean update, final boolean start)
    {
        // prepare GUI
        ThreadUtil.invokeNow(new Runnable()
        {
            @Override
            public void run()
            {
                if (SystemUtil.isHeadLess())
                    frame = null;
                else
                {
                    frame = new UpdateFrame("Icy Updater");
                    // update --> show progress frame immediately
                    if (update)
                        frame.setVisible(true);
                }
            }
        });

        // get Icy directory and path
        final String directory = FileUtil.getApplicationDirectory();
        final String icyJarPath = directory + FileUtil.separatorChar + ICY_JARNAME;

        // wait for lock
        if (!waitForLock(icyJarPath))
        {
            System.err.println("File " + icyJarPath + " is locked, aborting udpate...");

            // send report of the error
            report(strLog);

            return false;
        }

        if (update)
        {
            // do update
            if (!doUpdate())
                return false;

            // update successful ? check we are on OSX
            if (SystemUtil.isMac())
            {
                // we may want to rename Icy folder
                final File oldFile = new File("").getAbsoluteFile();
                final File parentFile = oldFile.getParentFile();
                final File newFile = new File(parentFile, ICY_FOLDER_OSX).getAbsoluteFile();

                // not yet done ?
                if (!newFile.exists())
                {
                    // try to rename Icy folder
                    if (!doOSXFolderNameUpdate(parentFile, oldFile, newFile))
                        return false;

                    // we cannot restart Icy the classic way so do it via the OSX app open
                    if (start)
                        return startICY_OSX(newFile.getAbsolutePath(), parentFile.getAbsolutePath());

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
     * Working directory should be the Icy directory else update won't work.
     */
    public static boolean doUpdate()
    {
        setState("Checking java version", 1);

        if (!checkMinimumJavaVersion(8))
        {
            System.err.println("New version of Icy requires Java 8 or above, please update your java version.");
            System.err.println();
            System.err.println("You can download Java 8 here:");
            System.err.println("http://icy.bioimageanalysis.org/download/");

            return false;
        }

        ArrayList<ElementDescriptor> localElements = Updater.getLocalElements();
        final ArrayList<ElementDescriptor> updateElements = Updater.getUpdateElements(localElements);
        boolean result = true;

        // get list of element to update
        for (ElementDescriptor updateElement : updateElements)
        {
            final String updateElementName = updateElement.getName();

            // can't update updater here (it should be already updated)
            if (updateElementName.equals(Updater.ICYUPDATER_NAME))
            {
                // just update local element with update element info
                Updater.updateElementInfos(updateElement, localElements);
                continue;
            }

            setState("Updating : " + updateElementName,
                    (updateElements.indexOf(updateElement) * 60) / updateElements.size());

            // update element
            if (!Updater.udpateElement(updateElement, localElements))
            {
                // an error happened --> take back current local elements
                localElements = Updater.getLocalElements();
                // remove the faulty element informations, this will force
                // update next time.
                Updater.clearElementInfos(updateElement, localElements);

                // error while updating, no need to go further...
                result = false;
                break;
            }
        }

        // some files hasn't be updated ?
        setState("Checking...", 60);

        if (!result)
        {
            System.err.println("Update processing has failed.");

            // delete update directory to restart update from scratch
            FileUtil.delete(Updater.UPDATE_DIRECTORY, true);

            // restore backup
            if (Updater.restore())
            {
                System.out.println("");
                System.out.println("Files correctly restored.");
                // delete backup directory as we don't need it anymore
                FileUtil.delete(Updater.BACKUP_DIRECTORY, true);
            }
            else
            {
                System.err.println("");
                System.err.println(
                        "Some files cannot be restored, try to restore them manually from 'backup' directory.");
                System.err.println("If Icy doesn't start anymore you may need to reinstall the application.");
            }

            // validate elements
            Updater.validateElements(localElements);
            // and save them
            if (!Updater.saveElementsToXML(localElements, Updater.VERSION_NAME, false))
                System.err.println("Error while saving " + Updater.VERSION_NAME + " file.");

            // send report of the error
            report(strLog);
        }
        else
        {
            // delete obsolete files
            setState("Deleting obsoletes...", 60);
            Updater.deleteObsoletes();

            // cleanup
            setState("Cleaning...", 70);
            FileUtil.delete(Updater.UPDATE_DIRECTORY, true);
            FileUtil.delete(Updater.BACKUP_DIRECTORY, true);

            if (updateElements.size() == 0)
                System.out.println("Nothing to update.");
            else
            {
                // update XML version file
                setState("Updating XML...", 90);

                // validate elements (this actually remove obsoletes files)
                Updater.validateElements(localElements);

                if (!Updater.saveElementsToXML(localElements, Updater.VERSION_NAME, false))
                {
                    System.out.println("Error while saving " + Updater.VERSION_NAME + " file.");
                    System.out.println("The new version is correctly installed but version number informations");
                    System.out.println("will stay outdated until the next update.");
                }
                else
                    System.out.println("Update succefully completed.");
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
    private static boolean doOSXFolderNameUpdate(File parentFile, File oldFile, File newFile)
    {
        final String parentFolder = FileUtil.getGenericPath(parentFile.getAbsolutePath());
        final String cmd = "mv " + oldFile.getAbsolutePath() + " " + newFile.getAbsolutePath();

        try
        {
            Process p;

            System.out.print("Renaming app: " + cmd);
            // execute it from parent folder for safety
            p = SystemUtil.exec(cmd, parentFolder);
            if (p == null)
                return false;

            // wait a bit
            Thread.sleep(1000);
            // output process stream
            outputStreams(p);
            // get error code
            System.out.println(" - exit code = " + p.waitFor());
            // wait a bit
            Thread.sleep(1000);

            System.out.print("Removing security check...");
            // remove quarantine attribut from the new created icy.app
            p = SystemUtil.exec("xattr -dr com.apple.quarantine " + newFile.getAbsolutePath(), parentFolder);
            if (p == null)
                return false;

            // wait a bit
            Thread.sleep(1000);
            // output process stream
            outputStreams(p);
            // get error code
            System.out.println(" exit code = " + p.waitFor());
            // wait a bit
            Thread.sleep(1000);

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    private static boolean checkMinimumJavaVersion(double value)
    {
        return SystemUtil.getJavaVersionAsNumber() >= value;
    }

    private static String getVMParams()
    {
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
        // Java >= 16 ? --> add permited illegal access
        if (checkMinimumJavaVersion(16))
            result += " --illegal-access=permit";
        if (!StringUtil.isEmpty(osVmParams))
            result += " " + osVmParams;

        return result;
    }

    private static String getAppParams()
    {
        // get app parameters stored in preferences
        return ApplicationPreferences.getAppParams();
    }

    public static boolean startICY(String directory)
    {
        setState("Launching Icy...", 0);
        if (frame != null)
            frame.setProgressVisible(false);

        // start icy
        final Process process = SystemUtil.execJAR(ICY_JARNAME, getVMParams(), getAppParams() + extraArgs, directory);

        // process not even created --> critical error
        if (process == null)
        {
            System.err.println("Can't launch execJAR(" + ICY_JARNAME + ", " + getVMParams() + ", " + getAppParams()
                    + extraArgs + "," + directory + ")");
            return false;
        }

        try
        {
            // wait a bit that streams has been filled
            ThreadUtil.sleep(2000);
            // flush stream so process correctly exit on error
            outputStreams(process);

            // check if we got an error
            if (process.exitValue() != 0)
            {
                try
                {
                    setState("Error while launching Icy", 0);
                    System.err.println("Can't launch execJAR(" + ICY_JARNAME + ", " + getVMParams() + ", "
                            + getAppParams() + extraArgs + "," + directory + ")");

                    System.out.println();
                    System.out.println("Trying to launch without specific parameters...");
                    System.out.println();
                }
                catch (Exception e)
                {
                    // ignore
                }

                return startICYSafeMode(directory);
            }
        }
        catch (IllegalThreadStateException e)
        {
            // thread still active --> means Icy properly launched !
        }
        catch (Exception e)
        {
            System.err.println("Error while launching Icy: " + e);
            e.printStackTrace();
            
            return false;
        }

        return true;
    }

    public static boolean startICYSafeMode(String directory)
    {
        setState("Launching Icy (safe mode)...", 0);
        if (frame != null)
            frame.setProgressVisible(false);

        // start icy in safe mode (no parameters)
        final Process process = SystemUtil.execJAR(ICY_JARNAME, "", "", directory);

        // process not even created --> critical error
        if (process == null)
        {
            System.err.println("Can't launch execJAR(" + ICY_JARNAME + ", \"\", \"\", " + directory + ")");
            System.out.println();
            System.out.println("Try to manually launch the following command :");
            System.out.println("java -jar updater.jar");
            return false;
        }

        try
        {
            // we cannot use process.waitFor() here as it hangs forever
            ThreadUtil.sleep(2000);
            // output process stream
            outputStreams(process);

            // got an error ?
            if (process.exitValue() != 0)
            {
                try
                {
                    setState("Error while launching Icy (safe mode)", 0);
                    System.err.println("Can't launch execJAR(" + ICY_JARNAME + ", \"\", \"\", " + directory + ")");

                    System.out.println();
                    System.out.println("Try to manually launch the following command :");
                    System.out.println("java -jar updater.jar");
                }
                catch (Exception e)
                {
                    // ignore

                }

                return false;
            }
        }
        catch (IllegalThreadStateException e)
        {
            // thread still active --> means Icy properly launched !
        }
        catch (Exception e)
        {
            System.err.println("Error while launching Icy: " + e);
            e.printStackTrace();
            
            return false;
        }

        return true;
    }

    public static boolean startICY_OSX(String appPackage, String directory)
    {
        setState("Launching Icy (OSX mode)...", 0);
        if (frame != null)
            frame.setProgressVisible(false);

        // start icy in safe mode (no parameters)
        final Process process = SystemUtil.exec("open -a " + appPackage, directory);

        // process not even created --> critical error
        if (process == null)
        {
            System.err.println("Can't launch Icy..");
            System.out.println();
            System.out.println("Try to launch it manually.");
            return false;
        }

        try
        {
            // wait a bit that streams has been filled
            ThreadUtil.sleep(2000);
            // flush stream so process correctly exit on error
            outputStreams(process);

            // check if we got an error
            if (process.exitValue() != 0)
            {
                try
                {
                    setState("Error while launching Icy", 0);
                    System.err.println("Can't launch Icy..");

                    System.out.println();
                    System.out.println("Try to launch it manually.");
                }
                catch (Exception e)
                {
                    // ignore

                }

                return false;
            }
        }
        catch (IllegalThreadStateException e)
        {
            // thread still active --> means Icy properly launched !
        }
        catch (Exception e)
        {
            System.err.println("Error while launching Icy: " + e);
            e.printStackTrace();
            
            return false;
        }
        
        return true;
    }

    private static void setState(final String state, final int progress)
    {
        ThreadUtil.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                if (frame != null)
                {
                    frame.setTitle(state);
                    frame.setProgress(progress);
                }
            }
        });
    }

    private static boolean waitForLock(String lockName)
    {
        final long start = System.currentTimeMillis();
        final File f = new File(lockName);

        // may help
        if (!f.setWritable(true, false))
            f.setWritable(true, true);

        // ensure lock exist
        if (f.exists())
        {
            // wait while file is lock (we wait 15 seconds at max)
            while ((!f.canWrite()) && ((System.currentTimeMillis() - start) < (15 * 1000)))
            {
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
     * Report an error log to the Icy web site.
     * 
     * @param errorLog
     *        Error log to report.
     */
    private static void report(String errorLog)
    {
        final Map<String, String> values = new HashMap<String, String>();

        values.put(NetworkUtil.ID_PLUGINCLASSNAME, "");
        values.put(NetworkUtil.ID_ERRORLOG, "Updater version " + version + "\n\n" + errorLog);

        // send report
        NetworkUtil.report(values);
    }

    private static void outputStreams(final Process process) throws IOException
    {
        final BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        final OutputStream outStr = process.getOutputStream();
        final BufferedReader inReader=  new BufferedReader(new InputStreamReader(process.getInputStream()));

        try
        {
            outStr.write((int) ' ');
            while (errReader.ready())
                System.err.println(errReader.readLine());
            while (inReader.ready())
                System.out.println(inReader.readLine());
        }
        catch (Exception e)
        {
            System.err.println(e);
        }
    }
}
