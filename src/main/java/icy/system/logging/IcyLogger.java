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

package icy.system.logging;

import icy.gui.frame.progress.AnnounceFrame;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.gui.frame.progress.SuccessfullAnnounceFrame;
import icy.main.Icy;
import icy.util.DateUtil;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

/**
 * Centralized logging system for Icy.
 * <br>
 * Supports both <code>GUI</code> and <code>Headless</code> mode.
 *
 * @author Thomas Musset
 */
public final class IcyLogger {
    @MagicConstant(intValues = {DEBUG, INFO, WARN, ERROR})
    public @interface LogLevel {}

    public static final int DEBUG = 0;
    public static final int INFO = 1;
    public static final int WARN = 2;
    public static final int ERROR = 3;

    @LogLevel private static int CONSOLE_LEVEL = DEBUG;
    @LogLevel private static int GUI_LEVEL = ERROR;

    /**
     * Define the console log level.
     *
     * @param level Must be one of these: {@link #DEBUG}, {@link #INFO}, {@link #WARN} or {@link #ERROR}
     */
    public static void setConsoleLevel(@LogLevel final int level) {
        if (level < DEBUG || level > ERROR)
            warn(String.format("Console log level must be between 0 and 3, %d given.", level));
        else CONSOLE_LEVEL = level;
    }

    /**
     * Define the GUI log level.
     *
     * @param level Must be one of these: {@link #DEBUG}, {@link #INFO}, {@link #WARN} or {@link #ERROR}
     */
    public static void setGUILevel(@LogLevel final int level) {
        if (level < DEBUG || level > ERROR)
            warn(String.format("GUI log level must be between 0 and 3, %d given.", level));
        else GUI_LEVEL = level;
    }

    /**
     * Print a debug message and show a {@link AnnounceFrame} on the GUI.
     *
     * @param message The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void debug(@NotNull final String message) {
        final String consoleMSG = String.format("[DEBUG] %s - %s", DateUtil.now("yyyy-MM-dd H:mm:ss"), message);

        if (CONSOLE_LEVEL <= DEBUG)
            System.out.println(consoleMSG);

        if (!Icy.getMainInterface().isHeadLess() && GUI_LEVEL <= DEBUG)
            new AnnounceFrame(message);
    }

    /**
     * Print an information message and show a {@link AnnounceFrame} on the GUI.
     *
     * @param message The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void info(@NotNull final String message) {
        final String consoleMSG = String.format("[INFO] %s - %s", DateUtil.now("yyyy-MM-dd H:mm:ss"), message);

        if (CONSOLE_LEVEL <= INFO)
            System.out.println(consoleMSG);

        if (!Icy.getMainInterface().isHeadLess() && GUI_LEVEL <= INFO)
            new AnnounceFrame(message);
    }

    /**
     * Print a warning message and show a {@link FailedAnnounceFrame} on the GUI.
     *
     * @param message The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void warn(@NotNull final String message) {
        final String consoleMSG = String.format("[WARNING] %s - %s", DateUtil.now("yyyy-MM-dd H:mm:ss"), message);

        if (CONSOLE_LEVEL <= WARN)
            System.err.println(consoleMSG);

        if (!Icy.getMainInterface().isHeadLess() && GUI_LEVEL <= WARN)
            new FailedAnnounceFrame(message);
    }

    /**
     * Print an warning message and show a {@link FailedAnnounceFrame} on the GUI.
     *
     * @param message The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void warn(@NotNull final String message, @NotNull final Throwable cause) {
        final String date = DateUtil.now("yyyy-MM-dd H:mm:ss");
        final String consoleMSG1 = String.format("[WARNING] %s - %s", date, message);
        final String consoleMSG2 = String.format("[WARNING] %s - Caused by: %s", date, cause.getLocalizedMessage());

        if (CONSOLE_LEVEL <= WARN)
            System.err.printf("%s%n%s%n", consoleMSG1, consoleMSG2);

        if (!Icy.getMainInterface().isHeadLess() && GUI_LEVEL <= WARN)
            new FailedAnnounceFrame(message);
    }

    /**
     * Print an error message.
     *
     * @param message The text print in the console and on the GUI.
     */
    public static void error(@NotNull final String message) {
        final String consoleMSG = String.format("[ERROR] %s - %s", DateUtil.now("yyyy-MM-dd H:mm:ss"), message);

        System.err.println(consoleMSG);

        if (!Icy.getMainInterface().isHeadLess())
            new FailedAnnounceFrame(message);
    }

    /**
     * Print an error message. Always show a {@link FailedAnnounceFrame} on the GUI.
     *
     * @param message The text print in the console and on the GUI.
     */
    @Deprecated(forRemoval = true)
    public static void error(@NotNull final String message, @NotNull final Throwable cause) {
        final String date = DateUtil.now("yyyy-MM-dd H:mm:ss");
        final String consoleMSG1 = String.format("[ERROR] %s - %s", date, message);
        final String consoleMSG2 = String.format("[ERROR] %s - Caused by: %s", date, cause.getLocalizedMessage());

        System.err.printf("%s%n%s%n", consoleMSG1, consoleMSG2);

        if (!Icy.getMainInterface().isHeadLess())
            new FailedAnnounceFrame(message);
    }

    /**
     * Print a fatal error message. Always show a {@link FailedAnnounceFrame} on the GUI.
     *
     * @param message The text print in the console and on the GUI.
     */
    public static void fatal(@NotNull final String message) {
        final String consoleMSG = String.format("[FATAL] %s - %s", DateUtil.now("yyyy-MM-dd H:mm:ss"), message);

        System.err.println(consoleMSG);

        if (!Icy.getMainInterface().isHeadLess())
            new FailedAnnounceFrame(message);
    }

    /**
     * Print a fatal error message. Always show a {@link FailedAnnounceFrame} on the GUI.
     *
     * @param message The text print in the console and on the GUI.
     */
    @Deprecated(forRemoval = true)
    public static void fatal(@NotNull final String message, @NotNull final Throwable cause) {
        final String date = DateUtil.now("yyyy-MM-dd H:mm:ss");
        final String consoleMSG1 = String.format("[FATAL] %s - %s", date, message);
        final String consoleMSG2 = String.format("[FATAL] %s - Caused by: %s", date, cause.getLocalizedMessage());

        System.err.printf("%s%n%s%n", consoleMSG1, consoleMSG2);

        if (!Icy.getMainInterface().isHeadLess())
            new FailedAnnounceFrame(message);
    }

    /**
     * Print a success message. Always show a {@link SuccessfullAnnounceFrame} on the GUI.
     *
     * @param message The text print in the console and on the GUI.
     */
    public static void success(@NotNull final String message) {
        final String consoleMSG = String.format("[SUCCESS] %s - %s", DateUtil.now("yyyy-MM-dd H:mm:ss"), message);

        System.out.println(consoleMSG);

        if (!Icy.getMainInterface().isHeadLess())
            new SuccessfullAnnounceFrame(message);
    }
}
