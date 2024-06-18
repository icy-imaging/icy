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

package org.bioimageanalysis.icy.system;

import com.sun.management.OperatingSystemMXBean;
import org.bioimageanalysis.icy.Icy;
import org.bioimageanalysis.icy.common.Version;
import org.bioimageanalysis.icy.common.collection.CollectionUtil;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.io.FileUtil;
import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.Desktop.Action;
import java.awt.event.InputEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.VolatileImage;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class SystemUtil {
    public static final String SYSTEM_WINDOWS = "win";
    public static final String SYSTEM_MAC_OS = "mac";
    public static final String SYSTEM_UNIX = "unix";

    /**
     * internals
     */
    private static final Properties props = System.getProperties();

    private static long lastNano = 0;
    private static long lastCpu = 0;
    private static int lastCpuLoad = 0;

    /**
     * Launch specified jar file.
     *
     * @param jarPath jar file path.
     * @param vmArgs  arguments for the java virtual machine.
     * @param appArgs arguments for jar application.
     * @param workDir working directory.
     * @deprecated Use {@link #execJAR(String, String[], String, String)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static Process execJAR(final String jarPath, final String vmArgs, final String appArgs, final String workDir) {
        //return exec("java " + vmArgs + " -jar " + jarPath + " " + appArgs, workDir);
        return exec(new String[]{"java", vmArgs, "-jar", jarPath, appArgs}, workDir);
    }

    /**
     * Launch specified jar file.
     *
     * @param jarPath jar file path.
     * @param vmArgs  arguments for the java virtual machine.
     * @param appArgs arguments for jar application.
     * @param workDir working directory.
     */
    public static Process execJAR(final String jarPath, final String @NotNull [] vmArgs, final String appArgs, final String workDir) {
        //return exec("java " + vmArgs + " -jar " + jarPath + " " + appArgs, workDir);
        final List<String> cmdarray = new ArrayList<>();
        cmdarray.add("java");
        if (vmArgs.length > 0)
            cmdarray.addAll(Arrays.asList(vmArgs));
        cmdarray.add("-jar");
        cmdarray.add(jarPath);
        cmdarray.add(appArgs);

        //return exec(new String[]{"java", vmArgs, "-jar", jarPath, appArgs}, workDir);
        return exec(cmdarray.toArray(new String[0]), workDir);
    }

    /**
     * Launch specified jar file.
     *
     * @param jarPath jar file path.
     * @param vmArgs  arguments for the java virtual machine.
     * @param appArgs arguments for jar application.
     * @deprecated Use {@link #execJAR(String, String[], String)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static Process execJAR(final String jarPath, final String vmArgs, final String appArgs) {
        //return exec("java " + vmArgs + " -jar " + jarPath + " " + appArgs);
        return exec(new String[]{"java", vmArgs, "-jar", jarPath, appArgs});
    }

    /**
     * Launch specified jar file.
     *
     * @param jarPath jar file path.
     * @param vmArgs  arguments for the java virtual machine.
     * @param appArgs arguments for jar application.
     */
    public static Process execJAR(final String jarPath, final String @NotNull [] vmArgs, final String appArgs) {
        //return exec("java " + vmArgs + " -jar " + jarPath + " " + appArgs, workDir);
        final List<String> cmdarray = new ArrayList<>();
        cmdarray.add("java");
        if (vmArgs.length > 0)
            cmdarray.addAll(Arrays.asList(vmArgs));
        cmdarray.add("-jar");
        cmdarray.add(jarPath);
        cmdarray.add(appArgs);

        //return exec(new String[]{"java", vmArgs, "-jar", jarPath, appArgs}, workDir);
        return exec(cmdarray.toArray(new String[0]));
    }

    /**
     * Launch specified jar file.
     *
     * @param jarPath jar file path.
     * @param appArgs arguments for jar application.
     */
    public static Process execJAR(final String jarPath, final String appArgs) {
        //return execJAR(jarPath, "", appArgs);
        return execJAR(jarPath, new String[]{}, appArgs);
    }

    /**
     * Launch specified jar file.
     *
     * @param jarPath jar file path.
     */
    public static Process execJAR(final String jarPath) {
        return execJAR(jarPath, "", "");
    }

    /**
     * Execute a system command and return the attached process.
     *
     * @param cmd system command to execute.
     * @deprecated Use {@link #exec(String[])} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static Process exec(final String cmd) {
        return exec(cmd, ".");
    }

    /**
     * Execute a system command and return the attached process.
     *
     * @param cmdarray system commands list to execute.
     */
    public static Process exec(final String[] cmdarray) {
        return exec(cmdarray, ".");
    }

    /**
     * Execute a system command and return the attached process.
     *
     * @param cmd system command to execute.
     * @param dir the working directory of the subprocess, or null if the subprocess should inherit the
     *            working directory of the current process.
     * @deprecated Use {@link #exec(String[], String)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static @Nullable Process exec(final String cmd, final String dir) {
        try {
            return Runtime.getRuntime().exec(cmd, null, new File(dir));
        }
        catch (final Exception e) {
            IcyLogger.error(SystemUtil.class, e, "SystemUtil.exec(" + cmd + ") error.");
            return null;
        }
    }

    /**
     * Execute a system command and return the attached process.
     *
     * @param cmdarray system commands list to execute.
     * @param dir      the working directory of the subprocess, or null if the subprocess should inherit the
     *                 working directory of the current process.
     */
    public static @Nullable Process exec(final String[] cmdarray, final String dir) {
        try {
            return Runtime.getRuntime().exec(cmdarray, null, new File(dir));
        }
        catch (final IOException e) {
            IcyLogger.error(SystemUtil.class, e, "SystemUtil.exec(" + Arrays.toString(cmdarray) + ") error.");
            return null;
        }
    }

    public static BufferedImage createCompatibleImage(final int width, final int height) {
        final GraphicsConfiguration defaultGC = getDefaultGraphicsConfiguration();

        if (defaultGC == null)
            return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        return defaultGC.createCompatibleImage(width, height);
    }

    public static BufferedImage createCompatibleImage(final int width, final int height, final int transparency) {
        final GraphicsConfiguration defaultGC = getDefaultGraphicsConfiguration();

        if (defaultGC == null) {
            if (transparency == Transparency.OPAQUE)
                return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        return defaultGC.createCompatibleImage(width, height, transparency);
    }

    public static @Nullable VolatileImage createCompatibleVolatileImage(final int width, final int height) {
        final GraphicsConfiguration defaultGC = getDefaultGraphicsConfiguration();

        if (defaultGC == null)
            return null;

        return defaultGC.createCompatibleVolatileImage(width, height);
    }

    public static @Nullable VolatileImage createCompatibleVolatileImage(final int width, final int height, final int transparency) {
        final GraphicsConfiguration defaultGC = getDefaultGraphicsConfiguration();

        if (defaultGC == null)
            return null;

        return defaultGC.createCompatibleVolatileImage(width, height, transparency);
    }

    public static @Nullable Desktop getDesktop() {
        if (Desktop.isDesktopSupported())
            return Desktop.getDesktop();

        return null;
    }

    /**
     * Launch the system file manager on specified folder (if supported)
     */
    public static boolean openFolder(final String folder) throws IOException {
        final Desktop desktop = getDesktop();

        if ((desktop != null) && desktop.isSupported(Action.OPEN)) {
            desktop.open(new File(folder));
            return true;
        }

        return false;
    }

    /**
     * @see System#getProperty(String)
     */
    public static String getProperty(final String name) {
        return props.getProperty(name);
    }

    /**
     * @see System#getProperty(String, String)
     */
    public static String getProperty(final String name, final String defaultValue) {
        return props.getProperty(name, defaultValue);
    }

    /**
     * @see System#setProperty(String, String)
     */
    public static String setProperty(final String name, final String value) {
        return (String) props.setProperty(name, value);
    }

    /**
     * Return the CTRL key mask used for Menu shortcut.
     */
    @SuppressWarnings({"MagicConstant", "deprecation"})
    @MagicConstant(flags = {Event.CTRL_MASK})
    public static int getMenuCtrlMask() {
        try {
            return Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        }
        catch (final HeadlessException e) {
            // headless mode, use default Ctrl Mask
            return Event.CTRL_MASK;
        }
    }

    /**
     * Return the CTRL extended key mask used for Menu shortcut.
     */
    @SuppressWarnings("MagicConstant")
    @MagicConstant(flags = {InputEvent.CTRL_DOWN_MASK})
    public static int getMenuCtrlMaskEx() {
        try {
            return Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        }
        catch (final HeadlessException e) {
            // headless mode, use default Ctrl Mask Ex
            return InputEvent.CTRL_DOWN_MASK;
        }
    }

    @Contract(pure = true)
    public static GraphicsEnvironment getLocalGraphicsEnvironment() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment();
    }

    /**
     * Return the default screen device.
     */
    public static @Nullable GraphicsDevice getDefaultScreenDevice() {
        if (Icy.getMainInterface().isHeadLess())
            return null;

        return getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }

    /**
     * Return the default graphics configuration.
     */
    public static @Nullable GraphicsConfiguration getDefaultGraphicsConfiguration() {
        final GraphicsDevice screenDevice = getDefaultScreenDevice();

        if (screenDevice != null)
            return screenDevice.getDefaultConfiguration();

        return null;
    }

    /**
     * Return all available screen devices.
     */
    public static List<GraphicsDevice> getScreenDevices() {
        final List<GraphicsDevice> result = new ArrayList<>();

        if (Icy.getMainInterface().isHeadLess())
            return result;

        try {
            return CollectionUtil.asList(getLocalGraphicsEnvironment().getScreenDevices());
        }
        catch (final HeadlessException e) {
            return result;
        }
    }

    /**
     * Return the number of screen device.
     */
    public static int getScreenDeviceCount() {
        if (Icy.getMainInterface().isHeadLess())
            return 0;

        try {
            return getLocalGraphicsEnvironment().getScreenDevices().length;
        }
        catch (final HeadlessException e) {
            return 0;
        }
    }

    /**
     * Return the screen device corresponding to specified index.
     */
    public static @Nullable GraphicsDevice getScreenDevice(final int index) {
        if (Icy.getMainInterface().isHeadLess())
            return null;

        try {
            return getLocalGraphicsEnvironment().getScreenDevices()[index];
        }
        catch (final HeadlessException e) {
            return null;
        }
    }

    /**
     * Returns all screen device intersecting the given region.<br>
     * Can return an empty list if given region do not intersect any screen device.
     */
    public static @NotNull List<GraphicsDevice> getScreenDevices(final Rectangle region) {
        final List<GraphicsDevice> result = new ArrayList<>();

        if (Icy.getMainInterface().isHeadLess())
            return result;

        for (final GraphicsDevice gd : getLocalGraphicsEnvironment().getScreenDevices())
            if (getScreenBounds(gd, true).intersects(region))
                result.add(gd);

        return result;
    }

    /**
     * Returns the main screen device corresponding to the given region.<br>
     * If the given region intersect multiple screen, it return screen containing the largest area.<br>
     * Can return <code>null</code> if given region do not intersect any screen device.
     */
    public static @Nullable GraphicsDevice getScreenDevice(final Rectangle region) {
        if (Icy.getMainInterface().isHeadLess())
            return null;

        GraphicsDevice result = null;
        Rectangle2D largest = null;

        for (final GraphicsDevice gd : getLocalGraphicsEnvironment().getScreenDevices()) {
            final Rectangle2D intersection = getScreenBounds(gd, true).createIntersection(region);

            if (!intersection.isEmpty()) {
                // bigger intersection ?
                if ((largest == null) || ((intersection.getWidth() * intersection.getHeight()) > (largest.getWidth() * largest.getHeight()))) {
                    largest = intersection;
                    result = gd;
                }
            }
        }

        return result;
    }

    /**
     * Returns the screen device corresponding to the given position.<br>
     * Can return <code>null</code> if given position is not located in any screen device.
     */
    public static @Nullable GraphicsDevice getScreenDevice(final Point position) {
        if (Icy.getMainInterface().isHeadLess())
            return null;

        for (final GraphicsDevice gd : getLocalGraphicsEnvironment().getScreenDevices())
            if (getScreenBounds(gd, false).contains(position))
                return gd;

        return null;
    }

    /**
     * Returns true if current system is "head less" (no screen output device).
     */
    public static boolean isHeadLess() {
        return GraphicsEnvironment.isHeadless();
    }

    public static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static ClassLoader getSystemClassLoader() {
        return ClassLoader.getSystemClassLoader();
    }

    public static @Nullable BufferCapabilities getSystemBufferCapabilities() {
        final GraphicsConfiguration defaultGC = getDefaultGraphicsConfiguration();

        if (defaultGC == null)
            return null;

        return defaultGC.getBufferCapabilities();
    }

    public static @Nullable ImageCapabilities getSystemImageCapabilities() {
        final GraphicsConfiguration defaultGC = getDefaultGraphicsConfiguration();

        if (defaultGC == null)
            return null;

        return defaultGC.getImageCapabilities();
    }

    public static @Nullable ColorModel getSystemColorModel() {
        final GraphicsConfiguration defaultGC = getDefaultGraphicsConfiguration();

        if (defaultGC == null)
            return null;

        return defaultGC.getColorModel();
    }

    public static @Nullable ColorModel getSystemColorModel(final int transparency) {
        final GraphicsConfiguration defaultGC = getDefaultGraphicsConfiguration();

        if (defaultGC == null)
            return null;

        return defaultGC.getColorModel(transparency);
    }

    /**
     * Return bounds for specified screen.
     *
     * @param removeInsets remove any existing taskbars and menubars from the result
     */
    @Contract("null, _ -> new")
    public static @NotNull Rectangle getScreenBounds(final GraphicsDevice graphicsDevice, final boolean removeInsets) {
        if (graphicsDevice == null)
            return new Rectangle();

        final GraphicsConfiguration graphicsConfiguration = graphicsDevice.getDefaultConfiguration();
        final Rectangle bounds = graphicsConfiguration.getBounds();
        final Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(graphicsConfiguration);

        bounds.x += insets.left;
        bounds.y += insets.top;
        bounds.width -= (insets.left + insets.right);
        bounds.height -= (insets.top + insets.bottom);

        return bounds;
    }

    /**
     * Return the entire desktop bounds (take multi screens in account).
     *
     * @param removeInsets remove any existing taskbars and menubars from the result
     */
    public static Rectangle getDesktopBounds(final boolean removeInsets) {
        Rectangle result = new Rectangle();

        if (Icy.getMainInterface().isHeadLess())
            return result;

        final GraphicsDevice[] gs = getLocalGraphicsEnvironment().getScreenDevices();

        for (final GraphicsDevice g : gs)
            result = result.union(getScreenBounds(g, removeInsets));

        return result;
    }

    /**
     * Return the entire desktop bounds (take multi screens in account)
     *
     * @see #getDesktopBounds(boolean)
     */
    public static Rectangle getDesktopBounds() {
        return getDesktopBounds(true);
    }

    /**
     * {@link GraphicsEnvironment#getMaximumWindowBounds()}
     */
    public static Rectangle getMaximumWindowBounds() {
        if (Icy.getMainInterface().isHeadLess())
            return new Rectangle();

        return getLocalGraphicsEnvironment().getMaximumWindowBounds();
    }

    /**
     * {@link GraphicsDevice#getDisplayMode()}
     */
    public static @Nullable DisplayMode getSystemDisplayMode() {
        final GraphicsDevice screenDevice = getDefaultScreenDevice();

        if (screenDevice != null)
            return screenDevice.getDisplayMode();

        return null;
    }

    /**
     * Return total number of processors or cores available to the JVM (same as system)
     */
    public static int getNumberOfCPUs() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Return total amount of free memory available to the JVM (in bytes)
     */
    public static long getJavaFreeMemory() {
        return getJavaMaxMemory() - getJavaUsedMemory();
    }

    /**
     * Return maximum amount of memory the JVM will attempt to use (in bytes)
     */
    public static long getJavaMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Return memory currently allocated by the JVM (in bytes)
     */
    public static long getJavaAllocatedMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * Return actual memory used by the JVM (in bytes)
     */
    public static long getJavaUsedMemory() {
        return getJavaAllocatedMemory() - Runtime.getRuntime().freeMemory();
    }

    private static @Nullable OperatingSystemMXBean getOSMXBean() {
        final java.lang.management.OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();

        if (bean instanceof OperatingSystemMXBean)
            return (OperatingSystemMXBean) bean;

        // for (Method method : bean.getClass().getDeclaredMethods())
        // {
        // final int modifiers=method.getModifiers();
        //
        // method.setAccessible(true);
        // if (method.getName().startsWith("get")
        // && Modifier.isPublic(modifiers)) {
        // Object value;
        // try {
        // value = method.invoke(bean);
        // } catch (Exception e) {
        // value = e;
        // } // try
        // System.out.println(method.getName() + " = " + value);
        // } // if
        // } // for

        return null;
    }

    /**
     * Return total physic memory of system (in bytes)
     */
    public static long getTotalMemory() {
        final OperatingSystemMXBean bean = getOSMXBean();

        if (bean != null)
            return bean.getTotalMemorySize();

        return -1L;
    }

    /**
     * Return free physic memory of system (in bytes)
     */
    public static long getFreeMemory() {
        final OperatingSystemMXBean bean = getOSMXBean();

        if (bean != null)
            return bean.getFreeMemorySize();

        return -1L;
    }

    /**
     * Return system process CPU time
     */
    public static long getProcessCpuTime() {
        final OperatingSystemMXBean bean = getOSMXBean();

        if (bean != null)
            return bean.getProcessCpuTime();

        return -1L;
    }

    /**
     * Return average CPU load of the application processes from the last call<br>
     * (-1 if no available)
     */
    public static int getCpuLoad() {
        final OperatingSystemMXBean bean = getOSMXBean();

        if (bean != null) {
            // first call
            if (lastNano == 0) {
                lastNano = System.nanoTime();
                lastCpu = bean.getProcessCpuTime();
            }
            else {
                final long nanoAfter = System.nanoTime();
                final long cpuAfter = bean.getProcessCpuTime();

                final long dNano = nanoAfter - lastNano;
                final long dCpu = cpuAfter - lastCpu;

                // below 0.5s the reported value isn't very significant
                if (dNano > 500000000L) {
                    lastCpuLoad = (int) ((dCpu * 100L) / (dNano * getNumberOfCPUs()));
                    lastNano = nanoAfter;
                    lastCpu = cpuAfter;
                }
            }

            return lastCpuLoad;
        }

        return -1;
    }

    /**
     * Returns the user name.
     */
    public static String getUserName() {
        return getProperty("user.name");
    }

    /**
     * Returns the JVM name.
     */
    public static String getJavaName() {
        return getProperty("java.runtime.name");
    }

    /**
     * Returns the JVM version.
     */
    public static String getJavaVersion() {
        String result = getProperty("java.runtime.version");

        if (result.equals("unknow"))
            result = getProperty("java.version");

        return result;
    }

    /**
     * Returns the JVM version in number format (ex: 6.091, 7.071, 8.151..)
     */
    public static double getJavaVersionAsNumber() {
        // remove all unwanted characters
        String version = getJavaVersion().replaceAll("[^\\d.]", "");
        // find first digit separator
        int firstSepInd = version.indexOf('.');

        if (firstSepInd >= 0) {
            // version 1.xxx ?
            if (version.substring(0, firstSepInd).equals("1")) {
                // remove "1."
                version = version.substring(firstSepInd + 1);
                // get first "." index
                firstSepInd = version.indexOf('.');
            }

            int lastSepInd = version.lastIndexOf('.');
            while (lastSepInd != firstSepInd) {
                version = version.substring(0, lastSepInd) + version.substring(lastSepInd + 1);
                lastSepInd = version.lastIndexOf('.');
            }

            if (version.charAt(firstSepInd + 1) == '0')
                version = version.substring(0, lastSepInd + 1) + version.substring(lastSepInd + 2);
        }

        return StringUtil.parseDouble(version, 0);
    }

    /**
     * Returns the JVM integer version (ex: 6.0.91, 7.0.71, 8.0.151..)
     */
    public static @NotNull Version getJavaVersionAsVersion() {
        // replace separators by '.'
        String version = getJavaVersion().replaceAll("-", ".");
        version = version.replaceAll("_", ".");
        // then remove all unwanted characters
        version = version.replaceAll("[^\\d.]", "");

        final int firstSepInd = version.indexOf('.');

        if (firstSepInd >= 0) {
            // version 1.xxx ?
            if (version.substring(0, firstSepInd).equals("1")) {
                // remove "1."
                version = version.substring(firstSepInd + 1);
                // get first "." index
                //firstSepInd = version.indexOf('.');
            }
        }

        return Version.fromString(version);
    }

    /**
     * Returns the JVM data architecture model.
     */
    public static int getJavaArchDataModel() {
        return Integer.parseInt(getProperty("sun.arch.data.model"));
    }

    /**
     * Returns the Operating System name.
     */
    public static String getOSName() {
        return getProperty("os.name");
    }

    /**
     * Returns the Operating System architecture name.
     */
    public static String getOSArch() {
        return getProperty("os.arch");
    }

    /**
     * Returns the Operating System version.
     */
    public static String getOSVersion() {
        return getProperty("os.version");
    }

    /**
     * Return an id OS string :<br>
     * <br>
     * Windows system return <code>SystemUtil.SYSTEM_WINDOWS</code><br>
     * MAC OS return <code>SystemUtil.SYSTEM_MAC_OS</code><br>
     * Unix system return <code>SystemUtil.SYSTEM_UNIX</code><br>
     * <br>
     * An empty string is returned is OS is unknown.
     */
    public static String getOSNameId() {
        if (isWindows())
            return SYSTEM_WINDOWS;
        if (isMac())
            return SYSTEM_MAC_OS;
        if (isUnix())
            return SYSTEM_UNIX;

        return "";
    }

    /**
     * Return an id OS architecture string<br>
     * example : "win32", "win64", "mac32", "mac64", "maca64", "unix32", "unix64", "unixa64"<br>
     * The bits number depends only from current installed JVM (32 or 64 bit)
     * and not directly from host OS.<br>
     * An empty string is returned if OS is unknown.
     */
    public static @NotNull String getOSArchIdString() {
        final String javaBit;

        // arm64 architecture ?
        if (StringUtil.equals(getOSArch().toLowerCase(), "aarch64"))
            javaBit = "a64";
        else
            javaBit = Integer.toString(getJavaArchDataModel());

        if (isWindows())
            return SYSTEM_WINDOWS + javaBit;
        else if (isMac())
            return SYSTEM_MAC_OS + javaBit;
        else if (isUnix())
            return SYSTEM_UNIX + javaBit;

        return "";
    }

    /**
     * Returns true is the operating system support link (symbolic or not).
     */
    public static boolean isLinkSupported() {
        return isMac() || isUnix();
    }

    /**
     * Returns true is the JVM is 32 bits.
     *
     * @deprecated Since Java 11, there is no 32bits version of the JVM.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static boolean is32bits() {
        return getJavaArchDataModel() == 32;
    }

    /**
     * Returns true is the JVM is 64 bits.
     *
     * @deprecated Since Java 11, there is only 64bits version of the JVM.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static boolean is64bits() {
        return getJavaArchDataModel() == 64;
    }

    /**
     * Returns true is the Operating System is Windows based.
     */
    public static boolean isWindows() {
        return (getOSName().toLowerCase().contains("win"));
    }

    /**
     * Returns true is the Operating System is Mac OS based.
     */
    public static boolean isMac() {
        return (getOSName().toLowerCase().contains("mac"));
    }

    /**
     * Returns true is the Operating System is Unix / Linux based.
     */
    public static boolean isUnix() {
        final String os = getOSName().toLowerCase();
        return (os.contains("nix")) || (os.contains("nux"));
    }

    /**
     * Returns true is the Operating System is Windows 64 bits whatever is the JVM installed (32 or 64 bits).
     */
    public static boolean isWindows64() {
        if (!isWindows())
            return false;

        final String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
        if ((wow64Arch != null) && wow64Arch.endsWith("64"))
            return true;

        final String arch = System.getenv("PROCESSOR_ARCHITECTURE");
        if ((arch != null) && arch.endsWith("64"))
            return true;

        return false;
    }

    /**
     * Returns default temporary directory.<br>
     * ex:<br>
     * <code>c:/temp</code><br>
     * <code>/tmp</code><br>
     * Same as {@link FileUtil#getTempDirectory()}
     */
    public static @NotNull String getTempDirectory() {
        return FileUtil.getTempDirectory();
    }

    /**
     * Returns temporary native library path (used to load native libraries from plugin)
     */
    public static @NotNull String getTempLibraryDirectory() {
        return FileUtil.getTempDirectory() + "/lib";
    }

    /**
     * Load the specified native library.
     *
     * @param dir  directory from where we want to load the native library.
     * @param name name of the library.<br>
     *             The filename of the library is automatically built depending the operating system.
     */
    public static void loadLibrary(final String dir, final String name) {
        final File libPath = new File(dir, System.mapLibraryName(name));

        if (libPath.exists())
            System.load(libPath.getAbsolutePath());
        else
            System.loadLibrary(name);
    }

    /**
     * Load the specified native library.
     *
     * @param pathname complete path or name of the library we want to load
     */
    public static void loadLibrary(final String pathname) {
        final File file = new File(pathname);

        if (file.exists())
            System.load(file.getAbsolutePath());
        else
            System.loadLibrary(pathname);
    }
}
