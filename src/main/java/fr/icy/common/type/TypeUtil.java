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

package fr.icy.common.type;

import fr.icy.common.math.MathUtil;
import loci.formats.FormatTools;
import ome.xml.model.enums.PixelType;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.DataBuffer;

/**
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class TypeUtil {
    @Contract(pure = true)
    public static @NonNull String toString(final boolean signed) {
        if (signed)
            return "signed";

        return "unsigned";
    }

    /**
     * Return true if the specified DataBuffer type is considered as a signed type
     */
    @Contract(pure = true)
    public static boolean isSignedDataBufferType(final @MagicConstant(intValues = {DataBuffer.TYPE_BYTE, DataBuffer.TYPE_SHORT, DataBuffer.TYPE_USHORT, DataBuffer.TYPE_INT, DataBuffer.TYPE_FLOAT, DataBuffer.TYPE_DOUBLE, DataBuffer.TYPE_UNDEFINED}) int type) {
        return switch (type) {
            //case DataBuffer.TYPE_BYTE, DataBuffer.TYPE_USHORT, DataBuffer.TYPE_INT -> false;
            case DataBuffer.TYPE_SHORT, DataBuffer.TYPE_FLOAT, DataBuffer.TYPE_DOUBLE -> true;
            default -> false; // assume DataBuffer.TYPE_BYTE, DataBuffer.TYPE_USHORT and DataBuffer.TYPE_INT are unsigned
        };
    }

    /**
     * Return true if the specified FormatTools type is a signed type
     */
    @Contract(pure = true)
    public static boolean isSignedFormatToolsType(final @MagicConstant(intValues = {FormatTools.INT8, FormatTools.UINT8, FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32, FormatTools.UINT32, FormatTools.FLOAT, FormatTools.DOUBLE}) int type) {
        return switch (type) {
            case FormatTools.INT8, FormatTools.INT16, FormatTools.INT32, FormatTools.FLOAT, FormatTools.DOUBLE -> true;
            //case FormatTools.UINT8, FormatTools.UINT16, FormatTools.UINT32 -> false;
            default -> false;
        };
    }

    /**
     * Return true if the specified PixelType is signed
     */
    @Contract(pure = true)
    public static boolean isSignedPixelType(final @NonNull PixelType type) {
        return switch (type) {
            case INT8, INT16, INT32, FLOAT, DOUBLE -> true;
            //case UINT8, UINT16, UINT32 -> false;
            default -> false;
        };
    }

    /**
     * Unsign the specified byte value and return it as int
     */
    @Contract(pure = true)
    public static int unsign(final byte value) {
        return value & 0xFF;
    }

    /**
     * Unsign the specified short value and return it as int
     */
    @Contract(pure = true)
    public static int unsign(final short value) {
        return value & 0xFFFF;
    }

    /**
     * Unsign the specified byte value and return it as long
     */
    @Contract(pure = true)
    public static long unsignL(final byte value) {
        return value & 0xFFL;
    }

    /**
     * Unsign the specified short value and return it as long
     */
    @Contract(pure = true)
    public static long unsignL(final short value) {
        return value & 0xFFFFL;
    }

    /**
     * Unsign the specified int value and return it as long
     */
    @Contract(pure = true)
    public static long unsign(final int value) {
        return value & 0xFFFFFFFFL;
    }

    /**
     * Unsign the specified long value and return it as double (possible information loss)
     */
    @Contract(pure = true)
    public static double unsign(final long value) {
        if ((double) value < 0d)
            return MathUtil.POW2_64_DOUBLE + (double) value;

        return (double) value;
    }

    /**
     * Unsign the specified long value and return it as float (possible information loss)
     */
    @Contract(pure = true)
    public static float unsignF(final long value) {
        if ((float) value < 0f)
            return MathUtil.POW2_64_FLOAT + (float) value;

        return (float) value;
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static int toShort(final byte value, final boolean signed) {
        if (signed)
            return value;

        return unsign(value);
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static int toInt(final byte value, final boolean signed) {
        if (signed)
            return value;

        return unsign(value);
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static int toInt(final short value, final boolean signed) {
        if (signed)
            return value;

        return unsign(value);
    }

    @Contract(pure = true)
    public static int toInt(final float value) {
        // we have to cast to long before else value is limited to
        // [Integer.MIN_VALUE, Integer.MAX_VALUE] range
        return (int) (long) value;
    }

    @Contract(pure = true)
    public static int toInt(final double value) {
        // we have to cast to long before else value is limited to
        // [Integer.MIN_VALUE, Integer.MAX_VALUE] range
        return (int) (long) value;
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static long toLong(final byte value, final boolean signed) {
        if (signed)
            return value;

        return unsignL(value);
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static long toLong(final short value, final boolean signed) {
        if (signed)
            return value;

        return unsignL(value);
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static long toLong(final int value, final boolean signed) {
        if (signed)
            return value;

        return unsign(value);
    }

    @Contract(pure = true)
    public static long toLong(final float value) {
        // handle unsigned long type (else value is clamped to Long.MAX_VALUE)
        if (value > DataType.LONG_MAX_VALUE_F)
            return ((long) (value - DataType.LONG_MAX_VALUE_F)) + 0x8000000000000000L;

        return (long) value;
    }

    @Contract(pure = true)
    public static long toLong(final double value) {
        // handle unsigned long type (else value is clamped to Long.MAX_VALUE)
        if (value > DataType.LONG_MAX_VALUE)
            return ((long) (value - DataType.LONG_MAX_VALUE)) + 0x8000000000000000L;

        return (long) value;
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static float toFloat(final byte value, final boolean signed) {
        if (signed)
            return value;

        return unsign(value);
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static float toFloat(final short value, final boolean signed) {
        if (signed)
            return value;

        return unsign(value);
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static float toFloat(final int value, final boolean signed) {
        if (signed)
            return value;

        return unsign(value);
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static float toFloat(final long value, final boolean signed) {
        if (signed)
            return value;

        return unsignF(value);
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static double toDouble(final byte value, final boolean signed) {
        if (signed)
            return value;

        return unsign(value);
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static double toDouble(final short value, final boolean signed) {
        if (signed)
            return value;

        return unsign(value);
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static double toDouble(final int value, final boolean signed) {
        if (signed)
            return value;

        return unsign(value);
    }

    @Contract(value = "_, true -> param1", pure = true)
    public static double toDouble(final long value, final boolean signed) {
        if (signed)
            return value;

        return unsign(value);
    }

    /**
     * Safe integer evaluation from an Integer object.<br>
     * Return <code>defaultValue</code> if the specified object is null.
     */
    @Contract(value = "null, _ -> param2; !null, _ -> param1", pure = true)
    public static int getInt(final @Nullable Integer obj, final int defaultValue) {
        if (obj == null)
            return defaultValue;

        return obj;
    }

    /**
     * Safe float evaluation from a Float object.<br>
     * Return <code>defaultValue</code> if the specified object is null.
     */
    @Contract(value = "null, _ -> param2; !null, _ -> param1", pure = true)
    public static float getFloat(final @Nullable Float obj, final float defaultValue) {
        if (obj == null)
            return defaultValue;

        return obj;
    }

    /**
     * Safe double evaluation from a Double object.<br>
     * Return <code>defaultValue</code> if <code>obj</code> is null or equal to infinite with
     * <code>allowInfinite</code> set to false.
     */
    @Contract(value = "null, _, _ -> param2", pure = true)
    public static double getDouble(final @Nullable Double obj, final double defaultValue, final boolean allowInfinite) {
        if (obj == null)
            return defaultValue;

        final double result = obj;

        if ((!allowInfinite) && Double.isInfinite(result))
            return defaultValue;

        return result;
    }

    /**
     * Safe double evaluation from a Double object.<br>
     * Return <code>defaultValue</code> if the specified object is null.
     */
    @Contract(value = "null, _ -> param2", pure = true)
    public static double getDouble(final @Nullable Double obj, final double defaultValue) {
        return getDouble(obj, defaultValue, true);
    }

    @Contract("_ -> new")
    public static @NonNull Point toPoint(final @NonNull Point2D p) {
        return new Point((int) p.getX(), (int) p.getY());
    }

    @Contract(value = "_ -> new", pure = true)
    public static Point2D.@NonNull Double toPoint2D(final @NonNull Point p) {
        return new Point2D.Double(p.x, p.y);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Point toPoint(final @NonNull Dimension d) {
        return new Point(d.width, d.height);
    }

    @Contract(value = "_ -> new", pure = true)
    public static Point2D.@NonNull Double toPoint2D(final @NonNull Dimension d) {
        return new Point2D.Double(d.width, d.height);
    }

    @Contract(value = "_ -> new", pure = true)
    public static @NonNull Dimension toDimension(final @NonNull Point p) {
        return new Dimension(p.x, p.y);
    }

    /**
     * Create an array of Point from the input integer array.<br>
     * <br>
     * The format of the input array should be as follows:<br>
     * <code>input.lenght</code> = number of point * 2.<br>
     * <code>input[(pt * 2) + 0]</code> = X coordinate for point <i>pt</i><br>
     * <code>input[(pt * 2) + 1]</code> = Y coordinate for point <i>pt</i><br>
     */
    @Contract(pure = true)
    public static Point @NonNull [] toPoint(final int @NonNull [] input) {
        final Point[] result = new Point[input.length / 2];

        int pt = 0;
        for (int i = 0; i < input.length; i += 2)
            result[pt++] = new Point(input[i/* + 0*/], input[i + 1]);

        return result;
    }
}
