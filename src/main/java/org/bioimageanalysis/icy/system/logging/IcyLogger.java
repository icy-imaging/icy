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

package org.bioimageanalysis.icy.system.logging;

import org.bioimageanalysis.icy.gui.frame.progress.AnnounceFrame;
import org.bioimageanalysis.icy.gui.frame.progress.FailedAnnounceFrame;
import org.bioimageanalysis.icy.gui.frame.progress.SuccessfullAnnounceFrame;
import org.bioimageanalysis.icy.gui.frame.progress.WarningAnnounceFrame;
import org.bioimageanalysis.icy.Icy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    @MagicConstant(intValues = {TRACE, DEBUG, INFO, WARN, ERROR, FATAL})
    public @interface LogLevel {
    }

    public static final int TRACE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;
    public static final int FATAL = 5;

    @LogLevel
    private static int CONSOLE_LEVEL = TRACE;
    @LogLevel
    private static int GUI_LEVEL = ERROR;

    /**
     * Define the console log level.
     *
     * @param level Must be one of these: {@link #TRACE}, {@link #DEBUG}, {@link #INFO}, {@link #WARN} or {@link #ERROR}<br>
     *              ({@link #ERROR} and {@link #FATAL} are always printed in console and GUI)
     */
    public static void setConsoleLevel(final @LogLevel int level) {
        if (level < TRACE || level > ERROR)
            warn(IcyLogger.class, String.format("Console log level must be between 0 and 4, %d given.", level));
        else CONSOLE_LEVEL = level;
    }

    /**
     * Define the GUI log level.
     *
     * @param level Must be one of these: {@link #INFO}, {@link #WARN} or {@link #ERROR}<br>
     *              ({@link #ERROR} and {@link #FATAL} are always printed in console and GUI)
     */
    public static void setGUILevel(final @LogLevel int level) {
        if (level < INFO || level > ERROR)
            warn(IcyLogger.class, String.format("GUI log level must be between 2 and 4, %d given.", level));
        else GUI_LEVEL = level;
    }

    /**
     * Print a trace message in the console and show a {@link AnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void trace(final @NotNull Class<?> clazz, final @NotNull String... messages) {
        if (CONSOLE_LEVEL <= TRACE) {
            final Logger logger = LogManager.getLogger(clazz);
            for (final String message : messages)
                logger.trace(message);
        }

        if (!Icy.getMainInterface().isHeadLess() && GUI_LEVEL <= TRACE)
            new AnnounceFrame(String.join("<br>", messages));
    }

    /**
     * Print a debug message in the console and show a {@link AnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param cause The exception to print.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void trace(final @NotNull Class<?> clazz, final @NotNull Throwable cause, final @NotNull String... messages) {
        if (CONSOLE_LEVEL <= TRACE) {
            final Logger logger = LogManager.getLogger(clazz);
            for (int i = 0; i < messages.length; i++) {
                if (i == messages.length - 1)
                    logger.trace(messages[i], cause);
                else
                    logger.trace(messages[i]);
            }
        }

        if (!Icy.getMainInterface().isHeadLess() && GUI_LEVEL <= TRACE)
            new AnnounceFrame(String.join("<br>", messages) + "<br><i>See the console for more information.</i>");
    }

    /**
     * Print a debug message in the console and show a {@link AnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void debug(final @NotNull Class<?> clazz, final @NotNull String... messages) {
        if (CONSOLE_LEVEL <= DEBUG) {
            final Logger logger = LogManager.getLogger(clazz);
            for (final String message : messages)
                logger.debug(message);
        }

        if (!Icy.getMainInterface().isHeadLess() && GUI_LEVEL <= DEBUG)
            new AnnounceFrame(String.join("<br>", messages));
    }

    /**
     * Print a debug message in the console and show a {@link AnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param cause The exception to print.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void debug(final @NotNull Class<?> clazz, final @NotNull Throwable cause, final @NotNull String... messages) {
        if (CONSOLE_LEVEL <= DEBUG) {
            final Logger logger = LogManager.getLogger(clazz);
            for (int i = 0; i < messages.length; i++) {
                if (i == messages.length - 1)
                    logger.debug(messages[i], cause);
                else
                    logger.debug(messages[i]);
            }
        }

        if (!Icy.getMainInterface().isHeadLess() && GUI_LEVEL <= DEBUG)
            new AnnounceFrame(String.join("<br>", messages) + "<br><i>See the console for more information.</i>");
    }

    /**
     * Print an information message in the console and show a {@link AnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void info(final @NotNull Class<?> clazz, final @NotNull String... messages) {
        if (CONSOLE_LEVEL <= INFO) {
            final Logger logger = LogManager.getLogger(clazz);
            for (final String message : messages)
                logger.info(message);
        }

        if (!Icy.getMainInterface().isHeadLess() && GUI_LEVEL <= INFO)
            new AnnounceFrame(String.join("<br>", messages));
    }

    /**
     * Print an information message in the console and show a {@link AnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param cause The exception to print.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void info(final @NotNull Class<?> clazz, final @NotNull Throwable cause, final @NotNull String... messages) {
        if (CONSOLE_LEVEL <= INFO) {
            final Logger logger = LogManager.getLogger(clazz);
            for (int i = 0; i < messages.length; i++) {
                if (i == messages.length - 1)
                    logger.info(messages[i], cause);
                else
                    logger.info(messages[i]);
            }
        }

        if (!Icy.getMainInterface().isHeadLess() && GUI_LEVEL <= INFO)
            new AnnounceFrame(String.join("<br>", messages) + "<br><i>See the console for more information.</i>");
    }

    /**
     * Print a warning message in the console and show a {@link FailedAnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void warn(final @NotNull Class<?> clazz, final @NotNull String... messages) {
        if (CONSOLE_LEVEL <= WARN) {
            final Logger logger = LogManager.getLogger(clazz);
            for (final String message : messages)
                logger.warn(message);
        }

        if (!Icy.getMainInterface().isHeadLess() && GUI_LEVEL <= WARN)
            new WarningAnnounceFrame(String.join("<br>", messages));
    }

    /**
     * Print a warning message in the console and show a {@link FailedAnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param cause The exception to print.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void warn(final @NotNull Class<?> clazz, final @NotNull Throwable cause, final @NotNull String... messages) {
        if (CONSOLE_LEVEL <= WARN) {
            final Logger logger = LogManager.getLogger(clazz);
            for (int i = 0; i < messages.length; i++) {
                if (i == messages.length - 1)
                    logger.warn(messages[i], cause);
                else
                    logger.warn(messages[i]);
            }
        }

        if (!Icy.getMainInterface().isHeadLess() && GUI_LEVEL <= WARN)
            new WarningAnnounceFrame(String.join("<br>", messages) + "<br><i>See the console for more information.</i>");
    }

    /**
     * Print an error message in the console and show a {@link FailedAnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void error(final @NotNull Class<?> clazz, final @NotNull String... messages) {
        final Logger logger = LogManager.getLogger(clazz);
        for (final String message : messages)
            logger.error(message);

        if (!Icy.getMainInterface().isHeadLess())
            new FailedAnnounceFrame(String.join("<br>", messages));
    }

    /**
     * Print an error message in the console and show a {@link FailedAnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param cause The exception to print.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void error(final @NotNull Class<?> clazz, final @NotNull Throwable cause, final @NotNull String... messages) {
        final Logger logger = LogManager.getLogger(clazz);
        for (int i = 0; i < messages.length; i++) {
            if (i == messages.length - 1)
                logger.error(messages[i], cause);
            else
                logger.error(messages[i]);
        }

        if (!Icy.getMainInterface().isHeadLess())
            new FailedAnnounceFrame(String.join("<br>", messages) + "<br><i>See the console for more information.</i>");
    }

    /**
     * Print a fatal error message in the console and show a {@link FailedAnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void fatal(final @NotNull Class<?> clazz, final @NotNull String... messages) {
        final Logger logger = LogManager.getLogger(clazz);
        for (final String message : messages)
            logger.fatal(message);

        if (!Icy.getMainInterface().isHeadLess())
            new FailedAnnounceFrame(String.join("<br>", messages));
    }

    /**
     * Print a fatal error message in the console and show a {@link FailedAnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param cause The exception to print.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void fatal(final @NotNull Class<?> clazz, final @NotNull Throwable cause, final @NotNull String... messages) {
        final Logger logger = LogManager.getLogger(clazz);
        for (int i = 0; i < messages.length; i++) {
            if (i == messages.length - 1)
                logger.fatal(messages[i], cause);
            else
                logger.fatal(messages[i]);
        }

        if (!Icy.getMainInterface().isHeadLess())
            new FailedAnnounceFrame(String.join("<br>", messages) + "<br><i>See the console for more information.</i>");
    }

    /**
     * Print a success message in the console and show a {@link SuccessfullAnnounceFrame} on the GUI.
     *
     * @param clazz The class where the log comes from.
     * @param messages The text to print in the console and on the GUI depending on your log level settings.
     */
    public static void success(final @NotNull Class<?> clazz, final @NotNull String... messages) {
        final Logger logger = LogManager.getLogger(clazz);
        for (final String message : messages)
            logger.info(message);

        if (!Icy.getMainInterface().isHeadLess())
            new SuccessfullAnnounceFrame(String.join("<br>", messages));
    }
}
