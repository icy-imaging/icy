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

import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public final class UserUtil {
    /**
     * Get user home directory.<br>
     * For example in macOS : /User/username/
     *
     * @return the user's home directory.
     */
    @Contract(" -> new")
    public static @NotNull File getUserHome() {
        return new File(System.getProperty("user.home"));
    }

    /**
     * Get '.icy' folder in user home directory.<br>
     * For example in macOS : /Users/username/.icy/
     *
     * @return the Icy's home folder.
     */
    public static @NotNull File getIcyHomeDirectory() {
        final File folder = new File(getUserHome(), ".icy");
        if (!folder.exists())
            if (folder.mkdirs())
                IcyLogger.info(UserUtil.class, "Created Icy's home directory: " + folder.getAbsolutePath());
            else
                IcyLogger.error(UserUtil.class, "Failed to create Icy's home directory: " + folder.getAbsolutePath());

        return folder;
    }

    public static @NotNull File getIcyLibrariesDirectory() {
        final File folder = new File(getIcyHomeDirectory(), "libraries");
        if (!folder.exists())
            if (folder.mkdirs())
                IcyLogger.info(UserUtil.class, "Created Icy's libraries directory: " + folder.getAbsolutePath());
            else
                IcyLogger.error(UserUtil.class, "Failed to create Icy's libraries directory: " + folder.getAbsolutePath());

        return folder;
    }

    public static @NotNull File getIcyExtensionsDirectory() {
        final File folder = new File(getIcyHomeDirectory(), "extensions");
        if (!folder.exists())
            if (folder.mkdirs())
                IcyLogger.info(UserUtil.class, "Created Icy's extension directory: " + folder.getAbsolutePath());
            else
                IcyLogger.error(UserUtil.class, "Failed to create Icy's extensions directory: " + folder.getAbsolutePath());

        return folder;
    }

    public static @NotNull File getIcyConfigDirectory() {
        final File folder = new File(getIcyHomeDirectory(), "config");
        if (!folder.exists())
            if (folder.mkdirs())
                IcyLogger.info(UserUtil.class, "Created Icy's config directory: " + folder.getAbsolutePath());
            else
                IcyLogger.error(UserUtil.class, "Failed to create Icy's config directory: " + folder.getAbsolutePath());

        return folder;
    }

    public static @NotNull File getIcyScriptsDirectory() {
        final File folder = new File(getIcyHomeDirectory(), "scripts");
        if (!folder.exists())
            if (folder.mkdirs())
                IcyLogger.info(UserUtil.class, "Created Icy's scripts directory: " + folder.getAbsolutePath());
            else
                IcyLogger.error(UserUtil.class, "Failed to create Icy's scripts directory: " + folder.getAbsolutePath());

        return folder;
    }
}
