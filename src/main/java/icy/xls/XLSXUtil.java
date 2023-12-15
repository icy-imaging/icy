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

package icy.xls;

import icy.file.FileUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Thomas MUSSET
 */
public final class XLSXUtil {
    public static final String FILE_EXTENSION = "xlsx";
    public static final String FILE_DOT_EXTENSION = "." + FILE_EXTENSION;

    /**
     * Creates and returns a new Workbook file.<br>
     * Previous existing file is overwritten.
     */
    @NotNull
    public static Workbook createWorkbook(@NotNull final File file) throws IOException {
        if (file.exists())
            return loadWorkbookForWrite(file);

        final String ext = FileUtil.getFileExtension(file.getAbsolutePath(), false);
        if (!ext.equalsIgnoreCase("xlsx"))
            throw new IOException("Can't open workbook - unsupported file type: " + ext);

        FileUtil.createFile(file);
        return WorkbookFactory.create(file, null, false);
    }

    /**
     * Creates and returns a new Workbook file.<br>
     * Previous existing file is overwritten.
     */
    @NotNull
    public static Workbook createWorkbook(@NotNull final String filename) throws IOException {
        return createWorkbook(new File(filename));
    }

    /**
     * Loads and returns Workbook from an existing file (read operation only)
     */
    @NotNull
    public static Workbook loadWorkbookForRead(@NotNull final File file) throws IOException {
        if (!file.exists())
            throw new FileNotFoundException("Can't open workbook - file not found: " + file.getName());

        return WorkbookFactory.create(file, null, true);
    }

    /**
     * Loads and returns Workbook from an existing file (for write operation).<br>
     * Throws IOException if the file does not exist.<br>
     * <br>
     * WARNING: don't forget to end by {@link #saveAndClose(Workbook)} even if you don't
     * change the Workbook else you lost all previous data already present.
     */
    @NotNull
    public static Workbook loadWorkbookForWrite(@NotNull final File file) throws IOException {
        if (!file.exists())
            throw new FileNotFoundException("Can't open workbook - file not found: " + file.getName());

        return WorkbookFactory.create(file, null, false);
    }
}
