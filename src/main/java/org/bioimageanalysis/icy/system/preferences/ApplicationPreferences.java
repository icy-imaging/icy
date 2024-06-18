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

package org.bioimageanalysis.icy.system.preferences;

import org.bioimageanalysis.icy.common.Version;
import org.bioimageanalysis.icy.io.FileUtil;
import org.bioimageanalysis.icy.common.math.MathUtil;
import org.bioimageanalysis.icy.network.NetworkUtil;
import org.bioimageanalysis.icy.system.SystemUtil;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ApplicationPreferences {
    /**
     * id
     */
    private static final String PREF_ID = "icy";

    public static final String ID_ICY_ID = "id";
    public static final String ID_OS = "os";
    public static final String ID_UPDATE_REPOSITORY_BASE = "updateRepositoryBase";
    public static final String ID_UPDATE_REPOSITORY_FILE = "updateRepositoryFile";
    public static final String ID_MAX_MEMORY = "maxMemory";
    public static final String ID_STACK_SIZE = "stackSize";
    public static final String ID_CACHE_MEMORY_PERCENT = "cacheMemoryPercent";
    public static final String ID_CACHE_PATH = "cacheMemoryPath";
    public static final String ID_EXTRA_VMPARAMS = "extraVMParams";
    public static final String ID_OS_EXTRA_VMPARAMS = "osExtraVMParams";
    public static final String ID_APP_FOLDER = "appFolder";
    public static final String ID_APP_PARAMS = "appParams";
    public static final String ID_VERSION = "version";
    public static final String ID_SINGLE_INSTANCE = "singleInstance";

    public final static String DEFAULT_UPDATE_REPOSITORY_BASE = NetworkUtil.WEBSITE_URL + "update/";
    public final static String BETA_UPDATE_REPOSITORY_BASE = NetworkUtil.WEBSITE_URL + "update_test/";
    // private final static String DEFAULT_UPDATE_REPOSITORY_BASE = "https://icy.yhello.co/update/";
    private final static String DEFAULT_UPDATE_REPOSITORY_FILE = "update.php";

    /**
     * preferences
     */
    private static XMLPreferences preferences;

    public static void load() {
        // load preference
        preferences = IcyPreferences.root().node(PREF_ID);

        // set here settings which need to be initialized
        setMaxMemoryMB(getMaxMemoryMB());
    }

    /**
     * @return the preferences
     */
    public static XMLPreferences getPreferences() {
        return preferences;
    }

    public static String getOs() {
        return preferences.get(ID_OS, "");
    }

    public static void setOs(final String value) {
        preferences.put(ID_OS, value);
    }

    /**
     * @return Return Icy unique Id (-1 if not yet set)
     */
    public static int getId() {
        return preferences.getInt(ID_ICY_ID, -1);
    }

    public static void setId(final int value) {
        preferences.putInt(ID_ICY_ID, value);
    }

    // TODO Enable get from preference when Icy 3 is ready
    public static String getUpdateRepositoryBase() {
        //return preferences.get(ID_UPDATE_REPOSITORY_BASE, DEFAULT_UPDATE_REPOSITORY_BASE);
        return "https://icy.bioimageanalysis.org/update_test_icy3/";
    }

    public static void setUpdateRepositoryBase(final String value) {
        preferences.put(ID_UPDATE_REPOSITORY_BASE, value);
    }

    public static String getUpdateRepositoryFile() {
        return preferences.get(ID_UPDATE_REPOSITORY_FILE, DEFAULT_UPDATE_REPOSITORY_FILE);
    }

    public static void setUpdateRepositoryFile(final String value) {
        preferences.put(ID_UPDATE_REPOSITORY_FILE, value);
    }

    static int memoryAlign(final int memMB) {
        // arrange to get multiple of 32 MB
        return (int) MathUtil.prevMultiple(memMB, 32);
    }

    static int checkMem(final int memMB) {
        // check we can allocate that much
        return Math.min(getMaxMemoryMBLimit(), memoryAlign(memMB));
    }

    /**
     * @return Get max memory (in MB)
     */
    public static int getMaxMemoryMB() {
        int result = preferences.getInt(ID_MAX_MEMORY, -1);

        // no value ?
        if (result == -1)
            result = getDefaultMemoryMB();

        // arrange to get multiple of 32 MB
        return checkMem(result);
    }

    public static int getDefaultMemoryMB() {
        final long freeMemory = SystemUtil.getFreeMemory();

        // take system total memory / 2
        long calculatedMaxMem = SystemUtil.getTotalMemory() / 2;
        // current available memory is low ?
        if (calculatedMaxMem > freeMemory)
            // adjust max memory
            calculatedMaxMem -= (calculatedMaxMem - freeMemory) / 2;

        // get max memory in MB
        return checkMem((int) (calculatedMaxMem / (1024 * 1000)));
    }

    public static int getMaxMemoryMBLimit() {
        final int result = (int) (SystemUtil.getTotalMemory() / (1024 * 1000));

        return memoryAlign(result);
    }

    /**
     * @return Get stack size (in KB)
     */
    public static int getStackSizeKB() {
        // 2MB by default for VTK
        return preferences.getInt(ID_STACK_SIZE, 2048);
    }

    /**
     * @return Get cache reserved memory (in % of max memory)
     */
    public static int getCacheMemoryPercent() {
        return preferences.getInt(ID_CACHE_MEMORY_PERCENT, 40);
    }

    /**
     * @return Get cache reserved memory (in MB)
     */
    public static int getCacheMemoryMB() {
        return (int) (((SystemUtil.getJavaMaxMemory() / (1024 * 1024)) * getCacheMemoryPercent()) / 100L);
    }

    /**
     * @return Get cache path (folder where to create cache data, better to use fast storage)
     */
    public static String getCachePath() {
        final String result = preferences.get(ID_CACHE_PATH, SystemUtil.getTempDirectory());
        // doesn't exist ? --> use default folder as config may have changed
        if (!FileUtil.exists(result))
            return SystemUtil.getTempDirectory();

        return result;
    }

    /**
     * @return Get extra JVM parameters string
     */
    public static String getExtraVMParams() {
//        return preferences.get(ID_EXTRA_VMPARAMS, "-XX:+UseG1GC -XX:MaxGCPauseMillis=100");
        return preferences.get(ID_EXTRA_VMPARAMS, "");
    }

    /**
     * @return Get OS specific extra JVM parameters string
     */
    public static String getOSExtraVMParams() {
        final String os = SystemUtil.getOSNameId();

        // we have different default extra VM parameters depending OS
        if (os.equals(SystemUtil.SYSTEM_WINDOWS))
            return preferences.get(ID_OS_EXTRA_VMPARAMS + SystemUtil.SYSTEM_WINDOWS, "");
        if (os.equals(SystemUtil.SYSTEM_MAC_OS))
            return preferences.get(ID_OS_EXTRA_VMPARAMS + SystemUtil.SYSTEM_MAC_OS, "-Xdock:name=Icy");
        if (os.equals(SystemUtil.SYSTEM_UNIX))
            return preferences.get(ID_OS_EXTRA_VMPARAMS + SystemUtil.SYSTEM_UNIX, "");

        return "";
    }

    /**
     * @return Get Icy application folder
     */
    public static String getAppFolder() {
        return preferences.get(ID_APP_FOLDER, "");
    }

    /**
     * @return Get Icy application parameters string
     */
    public static String getAppParams() {
        return preferences.get(ID_APP_PARAMS, "");
    }

    /**
     * @return Get the stored version number (used to detect new installed version).
     */
    public static Version getVersion() {
        return Version.fromString(preferences.get(ID_VERSION, "1.0.0"));
    }

    /**
     * @param value
     *        Set max memory (in MB)
     */
    public static void setMaxMemoryMB(final int value) {
        preferences.putInt(ID_MAX_MEMORY, Math.min(getMaxMemoryMBLimit(), value));
    }

    /**
     * @param value
     *        Set stack size (in KB)
     */
    public static void setStackSizeKB(final int value) {
        preferences.putInt(ID_STACK_SIZE, value);
    }

    /**
     * @param value
     *        Set cache reserved memory (in % of max memory)
     */
    public static void setCacheMemoryPercent(final int value) {
        // 10 <= value <= 80
        preferences.putInt(ID_CACHE_MEMORY_PERCENT, Math.min(80, Math.max(10, value)));
    }

    /**
     * @param value
     *        Set cache path (folder where to create cache data, better to use fast storage)
     */
    public static void setCachePath(final String value) {
        preferences.put(ID_CACHE_PATH, value);
    }

    /**
     * @param value
     *        Set extra JVM parameters string
     */
    public static void setExtraVMParams(final String value) {
        preferences.put(ID_EXTRA_VMPARAMS, value);
    }

    /**
     * @param value
     *        Set OS specific extra JVM parameters string
     */
    public static void setOSExtraVMParams(final String value) {
        preferences.put(ID_OS_EXTRA_VMPARAMS + SystemUtil.getOSNameId(), value);
    }

    /**
     * @param value
     *        Set Icy application folder
     */
    public static void setAppFolder(final String value) {
        preferences.put(ID_APP_FOLDER, value);
    }

    /**
     * @param value
     *        Set ICY application parameters string
     */
    public static void setAppParams(final String value) {
        preferences.put(ID_APP_PARAMS, value);
    }

    /**
     * @param value
     *        Set the stored version number (used to detect new installed version)
     */
    public static void setVersion(final Version value) {
        preferences.put(ID_VERSION, value.toString());
    }

}
