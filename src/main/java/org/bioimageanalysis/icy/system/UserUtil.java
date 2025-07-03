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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

public final class UserUtil {
    private UserUtil() {
        //
    }

    public static void init() throws IOException {
        new File(getUserHome(), ".icy").mkdirs();
        new File(getIcyHomeDirectory(), "libraries").mkdirs();
        new File(getIcyLibrariesDirectory(), "natives").mkdirs();
        new File(getIcyHomeDirectory(), "extensions").mkdirs();
        new File(getIcyHomeDirectory(), "config").mkdirs();
        new File(getIcyHomeDirectory(), "scripts").mkdirs();
    }

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
    public static @Nullable File getIcyHomeDirectory() {
        final File folder = new File(getUserHome(), ".icy");
        if (!folder.exists() || !folder.isDirectory())
            return null;

        return folder;
    }

    public static @Nullable File getIcyLibrariesDirectory() {
        final File folder = new File(getIcyHomeDirectory(), "libraries");
        if (!folder.exists() || !folder.isDirectory())
            return null;

        return folder;
    }

    public static @Nullable File getIcyNativesDirectory() {
        final File folder = new File(getIcyLibrariesDirectory(), "natives");
        if (!folder.exists() || !folder.isDirectory())
            return null;

        return folder;
    }

    public static @Nullable File getIcyExtensionsDirectory() {
        final File folder = new File(getIcyHomeDirectory(), "extensions");
        if (!folder.exists() || !folder.isDirectory())
            return null;

        return folder;
    }

    public static @Nullable File getIcyConfigDirectory() {
        final File folder = new File(getIcyHomeDirectory(), "config");
        if (!folder.exists() || !folder.isDirectory())
            return null;

        return folder;
    }

    public static @Nullable File getIcyScriptsDirectory() {
        final File folder = new File(getIcyHomeDirectory(), "scripts");
        if (!folder.exists() || !folder.isDirectory())
            return null;

        return folder;
    }
}
