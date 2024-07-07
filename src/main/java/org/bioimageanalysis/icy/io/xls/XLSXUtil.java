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

package org.bioimageanalysis.icy.io.xls;

import org.bioimageanalysis.icy.system.logging.IcyLogger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;

/**
 * XLSX (excel) utilities class (create and write XLSX documents).
 *
 * @author Thomas Musset
 */
public final class XLSXUtil {
    public static final String FILE_EXTENSION = "xlsx";
    public static final String FILE_DOT_EXTENSION = "." + FILE_EXTENSION;

    /**
     * Creates and returns a new Workbook.<br>
     * WARNING: do not forget to end by {@link #saveAndClose(Workbook, File)}
     * or {@link #saveAndClose(Workbook, String)}
     */
    public static @NotNull Workbook createWorkbook() throws IOException {
        return WorkbookFactory.create(true);
    }

    /**
     * Loads and returns Workbook from an existing file (for read operations only).
     *
     * @throws FileNotFoundException if the file does not exist.
     */
    public static @NotNull Workbook loadWorkbookForRead(final @NotNull File file) throws IOException {
        return WorkbookFactory.create(file, null, true);
    }

    /**
     * Loads and returns Workbook from an existing file (for write operations).
     * WARNING: do not forget to end by {@link #saveAndClose(Workbook, File)}
     * or {@link #saveAndClose(Workbook, String)} even if you don't
     * change the Workbook else you lost all previous data already present.
     *
     * @throws FileNotFoundException if the file does not exist.
     */
    public static @NotNull Workbook loadWorkbookForWrite(final @NotNull File file) throws IOException {
        return WorkbookFactory.create(new FileInputStream(file), null);
    }

    /**
     * Saves and closes the specified Workbook.<br>
     * Create the file if does not exists.
     */
    public static void saveAndClose(final @NotNull Workbook workbook, final @NotNull String path) throws IOException {
        final FileOutputStream os = new FileOutputStream(path);
        workbook.write(os);
        os.close();
        workbook.close();
    }

    /**
     * Saves and closes the specified Workbook.<br>
     * Create the file if does not exists.
     */
    public static void saveAndClose(final @NotNull Workbook workbook, final @NotNull File file) throws IOException {
        if (!file.getName().endsWith(FILE_DOT_EXTENSION))
            IcyLogger.warn(XLSXUtil.class, "Saving workbook in non-xlsx file.");
        final FileOutputStream os = new FileOutputStream(file);
        workbook.write(os);
        os.close();
        workbook.close();
    }

    /**
     * Searches for the specified page in workbook and returns it.<br>
     * If the page does not exists it creates and returns a new page.<br>
     *
     * @see #createNewPage(Workbook, String)
     */
    public static @NotNull Sheet getPage(final @NotNull Workbook workbook, final @NotNull String title) {
        Sheet result = workbook.getSheet(title);

        if (result == null)
            result = workbook.createSheet(title);

        return result;
    }

    /**
     * Creates and returns a new page for the specified workbook.<br>
     * If the page already exists, add an incremented number for distinction.
     *
     * @see #getPage(Workbook, String)
     */
    public static @NotNull Sheet createNewPage(final @NotNull Workbook workbook, final @NotNull String title) {
        if (workbook.getSheet(title) == null)
            return workbook.createSheet(title);

        int counter = 2;
        while (true) {
            final String pageName = title + " " + counter;

            if (workbook.getSheet(pageName) == null)
                return workbook.createSheet(pageName);

            counter++;
        }
    }

    /**
     * Clear the specified workbook (remove all pages).
     */
    public static void clear(final @NotNull Workbook workbook) {
        for (final Sheet sheet : workbook)
            workbook.removeSheetAt(workbook.getSheetIndex(sheet));
    }

    /**
     * Clear the specified page (remove all rows).
     */
    public static void clearPage(final @NotNull Sheet sheet) {
        for (final Row row : sheet)
            sheet.removeRow(row);
    }

    /**
     * Sets name of specified Sheet.
     */
    public static void setPageName(final @NotNull Sheet sheet, final @NotNull String name) {
        final Workbook workbook = sheet.getWorkbook();
        workbook.setSheetName(workbook.getSheetIndex(sheet), name);
    }

    /**
     * Get an existing {@link Cell} or create it if does not exist yet.
     *
     * @return the {@link Cell} or <code>null</code> if it fails creating it.
     */
    private static @Nullable Cell getOrCreateCell(final @NotNull Sheet sheet, final int x, final int y) {
        Row row = sheet.getRow(y);
        Cell cell;
        if (row != null) {
            cell = row.getCell(x);
            if (cell == null)
                cell = row.createCell(x);
        }
        else {
            row = sheet.createRow(y);
            cell = row.createCell(x);
        }

        return cell;
    }

    /**
     * Change the width of the given column.
     */
    public static void setColumnWidth(final @NotNull Sheet sheet, final int col, final int width) {
        sheet.setColumnWidth(col, width);
    }

    /**
     * Make the width automatic of the given column.
     */
    public static void setColumnAutoWidth(final @NotNull Sheet sheet, final int col) {
        sheet.autoSizeColumn(col);
    }

    /**
     * Merge the given cells coordinates (y1, y2, x1, x2).
     */
    public static void mergeCells(final @NotNull Sheet sheet, final int firstRow, final int lastRow, final int firstCol, final int lastCol) {
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }

    /**
     * Change the background color for the given cell.
     *
     * @param background Apply default style if set to null.
     */
    private static void applyBackground(final @NotNull Cell cell, final @Nullable Color background) {
        if (background != null) {
            final CellStyle style = cell.getCellStyle();
            style.setFillBackgroundColor(background);
            cell.setCellStyle(style);
        }
        else
            cell.setCellStyle(null);
    }

    /**
     * Sets cell content in string format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellString(final @NotNull Sheet sheet, final int x, final int y, final @NotNull String value, final @Nullable Color background) {
        final Cell cell = getOrCreateCell(sheet, x, y);
        if (cell != null) {
            cell.setCellValue(value);
            applyBackground(cell, background);
            return true;
        }

        return false;
    }

    /**
     * Sets cell content in string format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellString(final @NotNull Sheet sheet, final int x, final int y, final @NotNull String value) {
        return setCellString(sheet, x, y, value, null);
    }

    /**
     * Sets cell content in long format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellNumber(final @NotNull Sheet sheet, final int x, final int y, final @NotNull Long value, final @Nullable Color background) {
        final Cell cell = getOrCreateCell(sheet, x, y);
        if (cell != null) {
            cell.setCellValue(value);
            applyBackground(cell, background);
            return true;
        }

        return false;
    }

    /**
     * Sets cell content in long format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellNumber(final @NotNull Sheet sheet, final int x, final int y, final @NotNull Long value) {
        return setCellNumber(sheet, x, y, value, null);
    }

    /**
     * Sets cell content in integer format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellNumber(final @NotNull Sheet sheet, final int x, final int y, final @NotNull Integer value, final @Nullable Color background) {
        final Cell cell = getOrCreateCell(sheet, x, y);
        if (cell != null) {
            cell.setCellValue(value);
            applyBackground(cell, background);
            return true;
        }

        return false;
    }

    /**
     * Sets cell content in integer format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellNumber(final @NotNull Sheet sheet, final int x, final int y, final @NotNull Integer value) {
        return setCellNumber(sheet, x, y, value, null);
    }

    /**
     * Sets cell content in short format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellNumber(final @NotNull Sheet sheet, final int x, final int y, final @NotNull Short value, final @Nullable Color background) {
        final Cell cell = getOrCreateCell(sheet, x, y);
        if (cell != null) {
            cell.setCellValue(value);
            applyBackground(cell, background);
            return true;
        }

        return false;
    }

    /**
     * Sets cell content in short format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellNumber(final @NotNull Sheet sheet, final int x, final int y, final @NotNull Short value) {
        return setCellNumber(sheet, x, y, value, null);
    }

    /**
     * Sets cell content in double format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellNumber(final @NotNull Sheet sheet, final int x, final int y, final @NotNull Double value, final @Nullable Color background) {
        final Cell cell = getOrCreateCell(sheet, x, y);
        if (cell != null) {
            cell.setCellValue(value);
            applyBackground(cell, background);
            return true;
        }

        return false;
    }

    /**
     * Sets cell content in double format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellNumber(final @NotNull Sheet sheet, final int x, final int y, final @NotNull Double value) {
        return setCellNumber(sheet, x, y, value, null);
    }

    /**
     * Sets cell content in float format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellNumber(final @NotNull Sheet sheet, final int x, final int y, final @NotNull Float value, final @Nullable Color background) {
        final Cell cell = getOrCreateCell(sheet, x, y);
        if (cell != null) {
            cell.setCellValue(value);
            applyBackground(cell, background);
            return true;
        }

        return false;
    }

    /**
     * Sets cell content in float format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellNumber(final @NotNull Sheet sheet, final int x, final int y, final @NotNull Float value) {
        return setCellNumber(sheet, x, y, value, null);
    }

    /**
     * Sets cell content in date format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellDate(final @NotNull Sheet sheet, final int x, final int y, final @NotNull Date value, final @Nullable Color background) {
        final Cell cell = getOrCreateCell(sheet, x, y);
        if (cell != null) {
            cell.setCellValue(value);
            applyBackground(cell, background);
            return true;
        }

        return false;
    }

    /**
     * Sets cell content in date format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellDate(final @NotNull Sheet sheet, final int x, final int y, final @NotNull Date value) {
        return setCellDate(sheet, x, y, value, null);
    }

    /**
     * Sets cell content in date format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellFormula(final @NotNull Sheet sheet, final int x, final int y, final @NotNull String value, final @Nullable Color background) {
        final Cell cell = getOrCreateCell(sheet, x, y);
        if (cell != null) {
            cell.setCellFormula(value);
            applyBackground(cell, background);
            return true;
        }

        return false;
    }

    /**
     * Sets cell content in date format of specified Sheet.<br>
     * Returns <code>false</code> if the operation failed.
     */
    public static boolean setCellFormula(final @NotNull Sheet sheet, final int x, final int y, final @NotNull String value) {
        return setCellFormula(sheet, x, y, value, null);
    }

    /**
     * Reads lines from CSV reader and inject it inside an XLSX sheet.
     *
     * @return <code>true</code> if the operation succeed.
     */
    private static boolean readCSVLines(final @NotNull Sheet sheet, final @NotNull BufferedReader reader, final @NotNull String separator) throws InterruptedException {
        try {
            String line;
            int y = 0;
            while ((line = reader.readLine()) != null) {
                int x = 0;

                // use tab as separator
                for (final String col : line.split(separator)) {
                    if (!setCellString(sheet, x, y, col))
                        IcyLogger.warn(XLSXUtil.class, String.format("Cannot write in XLSX cell at position (%d, %d)", x, y));
                    x++;
                }

                y++;
                // check for interruption from time to time as this can be a long process
                if (((y & 0xFF) == 0xFF) && Thread.interrupted())
                    throw new InterruptedException("CSV to XLSX conversion process interrupted.");
            }

            return true;
        }
        catch (final IOException e) {
            IcyLogger.error(XLSXUtil.class, e, e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Fill sheet content from CSV text.
     *
     * @param separator Must be a regex string, like "\t" for tabulation.
     * @return <code>true</code> if the operation succeed.
     */
    public static boolean setFromCSV(final @NotNull Sheet sheet, final @NotNull String csvContent, final @NotNull String separator) throws InterruptedException {
        return readCSVLines(sheet, new BufferedReader(new StringReader(csvContent)), separator);
    }

    /**
     * Fill sheet content from CSV text. Uses tabulation as separator.
     *
     * @return <code>true</code> if the operation succeed.
     */
    public static boolean setFromCSV(final @NotNull Sheet sheet, final @NotNull String csvContent) throws InterruptedException {
        return setFromCSV(sheet, csvContent, "\t");
    }

    /**
     * Fill sheet content from CSV file.
     *
     * @param separator Must be a regex string, like "\t" for tabulation.
     * @return <code>true</code> if the operation succeed.
     */
    public static boolean setFromCSVFile(final @NotNull Sheet sheet, final @NotNull File file, final @NotNull String separator) throws InterruptedException {
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            IcyLogger.error(XLSXUtil.class, String.format("Cannot read CSV file: %s", file.getAbsolutePath()));
            return false;
        }

        try (final InputStream stream = Files.newInputStream(file.toPath());
             final InputStreamReader streamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             final BufferedReader reader = new BufferedReader(streamReader)) {
            return readCSVLines(sheet, reader, separator);
        }
        catch (final IOException e) {
            IcyLogger.error(XLSXUtil.class, e, e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * Fill sheet content from CSV file. Uses tabulation as separator.
     *
     * @return <code>true</code> if the operation succeed.
     */
    public static boolean setFromCSVFile(final @NotNull Sheet sheet, final @NotNull File file) throws InterruptedException {
        return setFromCSVFile(sheet, file, "\t");
    }
}
