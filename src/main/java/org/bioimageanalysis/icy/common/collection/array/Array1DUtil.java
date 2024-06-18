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

package org.bioimageanalysis.icy.common.collection.array;

import org.bioimageanalysis.icy.common.math.MathUtil;
import org.bioimageanalysis.icy.common.string.StringUtil;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.common.type.TypeUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class Array1DUtil {
    /**
     * @param array given array
     * @return Return the total number of element of the specified array
     */
    @Contract(pure = true)
    public static int getTotalLength(final byte[] array) {
        if (array != null)
            return array.length;

        return 0;
    }

    /**
     * @param array given array
     * @return Return the total number of element of the specified array
     */
    @Contract(pure = true)
    public static int getTotalLength(final short[] array) {
        if (array != null)
            return array.length;

        return 0;
    }

    /**
     * @param array given array
     * @return Return the total number of element of the specified array
     */
    @Contract(pure = true)
    public static int getTotalLength(final int[] array) {
        if (array != null)
            return array.length;

        return 0;
    }

    /**
     * @param array given array
     * @return Return the total number of element of the specified array
     */
    @Contract(pure = true)
    public static int getTotalLength(final long[] array) {
        if (array != null)
            return array.length;

        return 0;
    }

    /**
     * @param array given array
     * @return Return the total number of element of the specified array
     */
    @Contract(pure = true)
    public static int getTotalLength(final float[] array) {
        if (array != null)
            return array.length;

        return 0;
    }

    /**
     * @param array given array
     * @return Return the total number of element of the specified array
     */
    @Contract(pure = true)
    public static int getTotalLength(final double[] array) {
        if (array != null)
            return array.length;

        return 0;
    }

    /**
     * @param dataType data
     * @param len      length of Object
     * @return Create a new 1D array with specified data type and length
     */
    @Contract(pure = true)
    public static @Nullable Object createArray(final @NotNull DataType dataType, final int len) {
        return switch (dataType.getJavaType()) {
            case BYTE -> new byte[len];
            case SHORT -> new short[len];
            case INT -> new int[len];
            case LONG -> new long[len];
            case FLOAT -> new float[len];
            case DOUBLE -> new double[len];
            default -> null;
        };
    }

    /**
     * @param dataType data
     * @param len      length of data
     * @param out      Object
     * @return Allocate the specified 1D array if it's defined to null with the specified len
     */
    @Contract(value = "!null, _, _ -> param1", pure = true)
    public static Object allocIfNull(final Object out, final DataType dataType, final int len) {
        if (out == null) {
            switch (dataType.getJavaType()) {
                case BYTE:
                    return new byte[len];
                case SHORT:
                    return new short[len];
                case INT:
                    return new int[len];
                case LONG:
                    return new long[len];
                case FLOAT:
                    return new float[len];
                case DOUBLE:
                    return new double[len];
            }
        }

        return out;
    }

    /**
     * @param len length of array
     * @param out array
     * @return Allocate the specified array if it's defined to null with the specified len
     */
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static boolean @NotNull [] allocIfNull(final boolean[] out, final int len) {
        if (out == null)
            return new boolean[len];

        return out;
    }

    /**
     * @param len length of array
     * @param out array
     * @return Allocate the specified array if it's defined to null with the specified len
     */
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static byte @NotNull [] allocIfNull(final byte[] out, final int len) {
        if (out == null)
            return new byte[len];

        return out;
    }

    /**
     * @param len length of array
     * @param out array
     * @return Allocate the specified array if it's defined to null with the specified len
     */
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static short @NotNull [] allocIfNull(final short[] out, final int len) {
        if (out == null)
            return new short[len];

        return out;
    }

    /**
     * @param len length of array
     * @param out array
     * @return Allocate the specified array if it's defined to null with the specified len
     */
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static int @NotNull [] allocIfNull(final int[] out, final int len) {
        if (out == null)
            return new int[len];

        return out;
    }

    /**
     * @param len length of array
     * @param out array
     * @return Allocate the specified array if it's defined to null with the specified len
     */
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static long @NotNull [] allocIfNull(final long[] out, final int len) {
        if (out == null)
            return new long[len];

        return out;
    }

    /**
     * @param len length of array
     * @param out array
     * @return Allocate the specified array if it's defined to null with the specified len
     */
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static float @NotNull [] allocIfNull(final float[] out, final int len) {
        if (out == null)
            return new float[len];

        return out;
    }

    /**
     * @param len length of array
     * @param out array
     * @return Allocate the specified array if it's defined to null with the specified length
     */
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static double @NotNull [] allocIfNull(final double[] out, final int len) {
        if (out == null)
            return new double[len];

        return out;
    }

    /**
     * @param array array
     * @return Do a copy of the specified array
     */
    public static @Nullable Object copyOf(final Object array) {
        return switch (ArrayUtil.getDataType(array)) {
            case BYTE -> Arrays.copyOf((byte[]) array, ((byte[]) array).length);
            case SHORT -> Arrays.copyOf((short[]) array, ((short[]) array).length);
            case INT -> Arrays.copyOf((int[]) array, ((int[]) array).length);
            case LONG -> Arrays.copyOf((long[]) array, ((long[]) array).length);
            case FLOAT -> Arrays.copyOf((float[]) array, ((float[]) array).length);
            case DOUBLE -> Arrays.copyOf((double[]) array, ((double[]) array).length);
            default -> null;
        };
    }

    /**
     * @param array  array
     * @param offset int
     * @param signed boolean
     * @return Get value as double from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data.
     * Use {@link #getValue(Object, int, DataType)} we you know the DataType as it is faster.
     */
    public static double getValue(final Object array, final int offset, final boolean signed) {
        return getValue(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * @param array    array
     * @param offset   int
     * @param dataType data
     * @return Get value as double from specified 1D array and offset.<br>
     * Use specified DataType to case input array (no type check)
     */
    public static double getValue(final Object array, final int offset, final @NotNull DataType dataType) {
        return switch (dataType) {
            case BYTE -> getValue((byte[]) array, offset, true);
            case UBYTE -> getValue((byte[]) array, offset, false);
            case SHORT -> getValue((short[]) array, offset, true);
            case USHORT -> getValue((short[]) array, offset, false);
            case INT -> getValue((int[]) array, offset, true);
            case UINT -> getValue((int[]) array, offset, false);
            case LONG -> getValue((long[]) array, offset, true);
            case ULONG -> getValue((long[]) array, offset, false);
            case FLOAT -> getValue((float[]) array, offset);
            case DOUBLE -> getValue((double[]) array, offset);
        };
    }

    /**
     * @param array  array
     * @param signed boolean
     * @param offset int
     * @return Get value as float from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     */
    public static float getValueAsFloat(final Object array, final int offset, final boolean signed) {
        return getValueAsFloat(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * @param array    array
     * @param offset   int
     * @param dataType data
     * @return Get value as float from specified 1D array and offset.<br>
     * Use specified DataType to case input array (no type check)
     */
    public static float getValueAsFloat(final Object array, final int offset, final @NotNull DataType dataType) {
        return switch (dataType) {
            case BYTE -> getValueAsFloat((byte[]) array, offset, true);
            case UBYTE -> getValueAsFloat((byte[]) array, offset, false);
            case SHORT -> getValueAsFloat((short[]) array, offset, true);
            case USHORT -> getValueAsFloat((short[]) array, offset, false);
            case INT -> getValueAsFloat((int[]) array, offset, true);
            case UINT -> getValueAsFloat((int[]) array, offset, false);
            case LONG -> getValueAsFloat((long[]) array, offset, true);
            case ULONG -> getValueAsFloat((long[]) array, offset, false);
            case FLOAT -> getValueAsFloat((float[]) array, offset);
            case DOUBLE -> getValueAsFloat((double[]) array, offset);
        };
    }

    /**
     * @param array  array
     * @param offset int
     * @param signed boolean
     * @return Get value as integer from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     */
    public static int getValueAsInt(final Object array, final int offset, final boolean signed) {
        return getValueAsInt(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * @param array    array
     * @param offset   int
     * @param dataType data
     * @return Get value as integer from specified 1D array and offset.<br>
     * Use specified DataType to case input array (no type check)
     */
    public static int getValueAsInt(final Object array, final int offset, final @NotNull DataType dataType) {
        return switch (dataType) {
            case BYTE -> getValueAsInt((byte[]) array, offset, true);
            case UBYTE -> getValueAsInt((byte[]) array, offset, false);
            case SHORT -> getValueAsInt((short[]) array, offset, true);
            case USHORT -> getValueAsInt((short[]) array, offset, false);
            case INT, UINT -> getValueAsInt((int[]) array, offset);
            case LONG, ULONG -> getValueAsInt((long[]) array, offset);
            case FLOAT -> getValueAsInt((float[]) array, offset);
            case DOUBLE -> getValueAsInt((double[]) array, offset);
        };
    }

    /**
     * @param offset int
     * @param signed boolean
     * @param array  array
     * @return Get value as integer from specified 1D array and offset.<br>
     * If signed is true then any integer primitive is considered as signed data
     */
    public static long getValueAsLong(final Object array, final int offset, final boolean signed) {
        return getValueAsLong(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * @param dataType Use specified DataType to case input array (no type check)
     * @param array    array
     * @param offset   int
     * @return Get value as integer from specified 1D array and offset.<br>
     */
    public static long getValueAsLong(final Object array, final int offset, final @NotNull DataType dataType) {
        return switch (dataType) {
            case BYTE -> getValueAsLong((byte[]) array, offset, true);
            case UBYTE -> getValueAsLong((byte[]) array, offset, false);
            case SHORT -> getValueAsLong((short[]) array, offset, true);
            case USHORT -> getValueAsLong((short[]) array, offset, false);
            case INT -> getValueAsLong((int[]) array, offset, true);
            case UINT -> getValueAsLong((int[]) array, offset, false);
            case LONG, ULONG -> getValueAsLong((long[]) array, offset);
            case FLOAT -> getValueAsLong((float[]) array, offset);
            case DOUBLE -> getValueAsLong((double[]) array, offset);
        };
    }

    /**
     * Set value at specified offset as double value.
     *
     * @param array  array
     * @param offset int
     * @param value  double
     */
    public static void setValue(final Object array, final int offset, final double value) {
        setValue(array, offset, ArrayUtil.getDataType(array), value);
    }

    /**
     * Set value at specified offset as double value.
     *
     * @param array    array
     * @param offset   int
     * @param dataType object
     * @param value    double
     */
    public static void setValue(final Object array, final int offset, final @NotNull DataType dataType, final double value) {
        switch (dataType.getJavaType()) {
            case BYTE:
                setValue((byte[]) array, offset, value);
                break;

            case SHORT:
                setValue((short[]) array, offset, value);
                break;

            case INT:
                setValue((int[]) array, offset, value);
                break;

            case LONG:
                setValue((long[]) array, offset, value);
                break;

            case FLOAT:
                setValue((float[]) array, offset, value);
                break;

            case DOUBLE:
                setValue((double[]) array, offset, value);
                break;
        }
    }

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as double from specified byte array and offset.<br>
     */
    @Contract(pure = true)
    public static double getValue(final byte @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toDouble(array[offset], signed);
    }

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as double from specified short array and offset.<br>
     */
    @Contract(pure = true)
    public static double getValue(final short @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toDouble(array[offset], signed);
    }

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as double from specified int array and offset.<br>
     */
    @Contract(pure = true)
    public static double getValue(final int @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toDouble(array[offset], signed);
    }

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as double from specified long array and offset.<br>
     */
    @Contract(pure = true)
    public static double getValue(final long @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toDouble(array[offset], signed);
    }

    /**
     * @param array  array
     * @param offset int
     * @return Get value as double from specified float array and offset.<br>
     */
    @Contract(pure = true)
    public static double getValue(final float @NotNull [] array, final int offset) {
        return array[offset];
    }

    /**
     * @param array  array
     * @param offset int
     * @return Get value as double from specified double array and offset.<br>
     */
    @Contract(pure = true)
    public static double getValue(final double @NotNull [] array, final int offset) {
        return array[offset];
    }

    //

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as float from specified byte array and offset.<br>
     */
    @Contract(pure = true)
    public static float getValueAsFloat(final byte @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toFloat(array[offset], signed);
    }

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as float from specified short array and offset.<br>
     */
    @Contract(pure = true)
    public static float getValueAsFloat(final short @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toFloat(array[offset], signed);
    }

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as float from specified int array and offset.<br>
     */
    @Contract(pure = true)
    public static float getValueAsFloat(final int @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toFloat(array[offset], signed);
    }

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as float from specified long array and offset.<br>
     */
    @Contract(pure = true)
    public static float getValueAsFloat(final long @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toFloat(array[offset], signed);
    }

    /**
     * @param array  array
     * @param offset int
     * @return Get value as float from specified float array and offset.<br>
     */
    @Contract(pure = true)
    public static float getValueAsFloat(final float @NotNull [] array, final int offset) {
        return array[offset];
    }

    /**
     * @param array  array
     * @param offset int
     * @return Get value as float from specified double array and offset.<br>
     */
    @Contract(pure = true)
    public static float getValueAsFloat(final double @NotNull [] array, final int offset) {
        return (float) array[offset];
    }

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as int from specified byte array and offset.<br>
     */
    @Contract(pure = true)
    public static int getValueAsInt(final byte @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toInt(array[offset], signed);
    }

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as int from specified short array and offset.<br>
     */
    @Contract(pure = true)
    public static int getValueAsInt(final short @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toInt(array[offset], signed);
    }

    /**
     * @param array  array
     * @param offset int
     * @return Get value as int from specified int array and offset.<br>
     */
    @Contract(pure = true)
    public static int getValueAsInt(final int @NotNull [] array, final int offset) {
        // can't unsign here
        return array[offset];
    }

    /**
     * @param array  array
     * @param offset int
     * @return Get value as int from specified long array and offset.<br>
     */
    @Contract(pure = true)
    public static int getValueAsInt(final long @NotNull [] array, final int offset) {
        return (int) array[offset];
    }

    /**
     * @param array  array
     * @param offset int
     * @return Get value as int from specified float array and offset.<br>
     */
    @Contract(pure = true)
    public static int getValueAsInt(final float @NotNull [] array, final int offset) {
        return (int) array[offset];
    }

    /**
     * @param array  array
     * @param offset int
     * @return Get value as int from specified double array and offset.<br>
     */
    @Contract(pure = true)
    public static int getValueAsInt(final double @NotNull [] array, final int offset) {
        return (int) array[offset];
    }

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as long from specified byte array and offset.<br>
     */
    @Contract(pure = true)
    public static long getValueAsLong(final byte @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toLong(array[offset], signed);
    }

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as long from specified short array and offset.<br>
     */
    @Contract(pure = true)
    public static long getValueAsLong(final short @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toLong(array[offset], signed);

    }

    /**
     * @param signed If signed is true then we consider data as signed
     * @param array  array
     * @param offset int
     * @return Get value as long from specified int array and offset.<br>
     */
    @Contract(pure = true)
    public static long getValueAsLong(final int @NotNull [] array, final int offset, final boolean signed) {
        return TypeUtil.toLong(array[offset], signed);

    }

    /**
     * @param array  array
     * @param offset int
     * @return Get value as long from specified long array and offset.<br>
     */
    @Contract(pure = true)
    public static long getValueAsLong(final long @NotNull [] array, final int offset) {
        // can't unsign here
        return array[offset];
    }

    /**
     * @param array  array
     * @param offset int
     * @return Get value as long from specified float array and offset.<br>
     */
    @Contract(pure = true)
    public static long getValueAsLong(final float @NotNull [] array, final int offset) {
        return (long) array[offset];
    }

    /**
     * @param array  array
     * @param offset int
     * @return Get value as long from specified double array and offset.<br>
     */
    @Contract(pure = true)
    public static long getValueAsLong(final double @NotNull [] array, final int offset) {
        return (long) array[offset];
    }

    /**
     * Set value at specified offset as double value.
     * @param array array
     * @param offset double
     * @param value double
     */
    public static void setValue(final byte @NotNull [] array, final int offset, final double value) {
        array[offset] = (byte) value;
    }

    /**
     * Set value at specified offset as double value.
     * @param array array
     * @param offset double
     * @param value double
     */
    public static void setValue(final short @NotNull [] array, final int offset, final double value) {
        array[offset] = (short) value;
    }

    /**
     * Set value at specified offset as double value.
     * @param array array
     * @param offset double
     * @param value double
     */
    public static void setValue(final int @NotNull [] array, final int offset, final double value) {
        array[offset] = (int) value;
    }

    /**
     * Set value at specified offset as double value.
     * @param array array
     * @param offset double
     * @param value double
     */
    public static void setValue(final long @NotNull [] array, final int offset, final double value) {
        array[offset] = (long) value;
    }

    /**
     * Set value at specified offset as double value.
     * @param array array
     * @param offset double
     * @param value double
     */
    public static void setValue(final float @NotNull [] array, final int offset, final double value) {
        array[offset] = (float) value;
    }

    /**
     * Set value at specified offset as double value.
     * @param array array
     * @param offset double
     * @param value double
     */
    public static void setValue(final double @NotNull [] array, final int offset, final double value) {
        array[offset] = value;
    }

    /**
     * @param array1 array
     * @param array2 array
     * @return Return true is the specified arrays are equals
     */
    @Contract(value = "null, !null -> false; !null, null -> false", pure = true)
    public static boolean arrayByteCompare(final byte[] array1, final byte[] array2) {
        return Arrays.equals(array1, array2);
    }

    /**
     * @param array1 array
     * @param array2 array
     * @return Return true is the specified arrays are equals
     */
    @Contract(value = "null, !null -> false; !null, null -> false", pure = true)
    public static boolean arrayShortCompare(final short[] array1, final short[] array2) {
        return Arrays.equals(array1, array2);
    }

    /**
     * @param array1 array
     * @param array2 array
     * @return Return true is the specified arrays are equals
     */
    @Contract(value = "null, !null -> false; !null, null -> false", pure = true)
    public static boolean arrayIntCompare(final int[] array1, final int[] array2) {
        return Arrays.equals(array1, array2);
    }

    /**
     * @param array1 array
     * @param array2 array
     * @return Return true is the specified arrays are equals
     */
    @Contract(value = "null, !null -> false; !null, null -> false", pure = true)
    public static boolean arrayLongCompare(final long[] array1, final long[] array2) {
        return Arrays.equals(array1, array2);
    }

    /**
     * @param array1 array
     * @param array2 array
     * @return Return true is the specified arrays are equals
     */
    @Contract(value = "null, !null -> false; !null, null -> false", pure = true)
    public static boolean arrayFloatCompare(final float[] array1, final float[] array2) {
        return Arrays.equals(array1, array2);
    }

    /**
     * @param array1 array
     * @param array2 array
     * @return Return true is the specified arrays are equals
     */
    @Contract(value = "null, !null -> false; !null, null -> false", pure = true)
    public static boolean arrayDoubleCompare(final double[] array1, final double[] array2) {
        return Arrays.equals(array1, array2);
    }

    /**
     * Copy a region of data from <code>src</code> to <code>dst</code>.<br>
     * Both array are 1D but represents 2D data as we have in an image plane.
     *
     * @param src       source data array (should be same type than destination data array)
     * @param srcDim    source rectangular data dimension (array length should be &gt;= (Dimension.width * Dimension.heigth))
     * @param srcRegion source rectangular region to copy (assume the whole data based on srcDim if null)
     * @param dst       destination data array (should be same type than source data array)
     * @param dstDim    destination rectangular data dimension (array length should be &gt;= (Dimension.width * Dimension.heigth))
     * @param dstPt     destination X,Y position (assume [0,0] if null)
     * @param signed    if the source data array should be considered as signed data (meaningful for integer data type only)
     */
    public static void copyRect(final Object src, final Dimension srcDim, final Rectangle srcRegion, final Object dst, final Dimension dstDim, final Point dstPt, final boolean signed) {
        if ((src == null) || (srcDim == null) || (dst == null) || (dstDim == null))
            return;

        // source image region
        Rectangle adjSrcRegion = (srcRegion != null) ? srcRegion : new Rectangle(srcDim);

        // negative destination x position ?
        if ((dstPt != null) && (dstPt.x < 0)) {
            // adjust source rect and width
            adjSrcRegion.x -= dstPt.x;
            adjSrcRegion.width -= -dstPt.x;
        }
        // negative destination y position ?
        if ((dstPt != null) && (dstPt.y < 0)) {
            // adjust source rect and height
            adjSrcRegion.y -= dstPt.y;
            adjSrcRegion.height -= -dstPt.y;
        }

        // limit to source image size
        adjSrcRegion = adjSrcRegion.intersection(new Rectangle(srcDim));

        // destination image region
        Rectangle adjDstRegion = new Rectangle((dstPt != null) ? dstPt : new Point(), adjSrcRegion.getSize());
        // limit to destination image size
        adjDstRegion = adjDstRegion.intersection(new Rectangle(dstDim));

        final int w = Math.min(adjSrcRegion.width, adjDstRegion.width);
        final int h = Math.min(adjSrcRegion.height, adjDstRegion.height);

        // nothing to copy
        if ((w <= 0) || (h <= 0))
            return;

        final int srcSizeX = srcDim.width;
        final int dstSizeX = dstDim.width;

        int srcOffset = adjSrcRegion.x + (adjSrcRegion.y * srcSizeX);
        int dstOffset = adjDstRegion.x + (adjDstRegion.y * dstSizeX);

        for (int y = 0; y < h; y++) {
            // do data copy (and conversion if needed)
            Array1DUtil.arrayToArray(src, srcOffset, dst, dstOffset, w, signed);
            srcOffset += srcSizeX;
            dstOffset += dstSizeX;
        }
    }

    /**
     * Same as Arrays.fill() but applied to Object array from a double value
     * @param array array
     * @param value double
     */
    public static void fill(final Object array, final double value) {
        fill(array, 0, ArrayUtil.getLength(array), value);
    }

    /**
     * Same as Arrays.fill() but applied to Object array from a double value
     * @param array array
     * @param value double
     * @param from int
     * @param to int
     */
    public static void fill(final Object array, final int from, final int to, final double value) {
        switch (ArrayUtil.getDataType(array)) {
            case BYTE:
                fill((byte[]) array, from, to, (byte) value);
                break;

            case SHORT:
                fill((short[]) array, from, to, (short) value);
                break;

            case INT:
                fill((int[]) array, from, to, (int) value);
                break;

            case LONG:
                fill((long[]) array, from, to, (long) value);
                break;

            case FLOAT:
                fill((float[]) array, from, to, (float) value);
                break;

            case DOUBLE:
                fill((double[]) array, from, to, value);
                break;
        }
    }

    /**
     * Same as {@link Arrays#fill(byte[], int, int, byte)}
     * @param array array
     * @param value byte
     * @param from int
     * @param to int
     */
    public static void fill(final byte[] array, final int from, final int to, final byte value) {
        for (int i = from; i < to; i++)
            array[i] = value;
    }

    /**
     * Same as {@link Arrays#fill(short[], int, int, short)}
     * @param array array
     * @param value short
     * @param from int
     * @param to int
     */
    public static void fill(final short[] array, final int from, final int to, final short value) {
        for (int i = from; i < to; i++)
            array[i] = value;
    }

    /**
     * Same as {@link Arrays#fill(int[], int, int, int)}
     * @param array array
     * @param value int
     * @param from int
     * @param to int
     */
    public static void fill(final int[] array, final int from, final int to, final int value) {
        for (int i = from; i < to; i++)
            array[i] = value;
    }

    /**
     * Same as {@link Arrays#fill(long[], int, int, long)}
     * @param array array
     * @param value long
     * @param from int
     * @param to int
     */
    public static void fill(final long[] array, final int from, final int to, final long value) {
        for (int i = from; i < to; i++)
            array[i] = value;
    }

    /**
     * Same as {@link Arrays#fill(float[], int, int, float)}
     * @param array array
     * @param value float
     * @param from int
     * @param to int
     */
    public static void fill(final float[] array, final int from, final int to, final float value) {
        for (int i = from; i < to; i++)
            array[i] = value;
    }

    /**
     * Same as {@link Arrays#fill(double[], int, int, double)}
     * @param array array
     * @param value short
     * @param from int
     * @param to int
     */
    public static void fill(final double[] array, final int from, final int to, final double value) {
        for (int i = from; i < to; i++)
            array[i] = value;
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner.<br>
     * i.e: without overriding any data
     * @param array object
     * @param from int
     * @param to int
     * @param cnt int
     */
    public static void innerCopy(final Object array, final int from, final int to, final int cnt) {
        if (array == null)
            return;

        switch (ArrayUtil.getDataType(array)) {
            case BYTE:
                Array1DUtil.innerCopy((byte[]) array, from, to, cnt);
                return;

            case SHORT:
                Array1DUtil.innerCopy((short[]) array, from, to, cnt);
                return;

            case INT:
                Array1DUtil.innerCopy((int[]) array, from, to, cnt);
                return;

            case LONG:
                Array1DUtil.innerCopy((long[]) array, from, to, cnt);
                return;

            case FLOAT:
                Array1DUtil.innerCopy((float[]) array, from, to, cnt);
                return;

            case DOUBLE:
                Array1DUtil.innerCopy((double[]) array, from, to, cnt);
                return;
        }

        // use generic code
        final int delta = to - from;

        if (delta == 0)
            return;

        final int length = Array.getLength(array);

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0) {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                Array.set(array, to_++, Array.get(array, from_++));
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                Array.set(array, --to_, Array.get(array, --from_));
        }

    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner (no overlap)
     * @param array array
     * @param from int
     * @param to int
     * @param cnt int
     */
    public static void innerCopy(final byte[] array, final int from, final int to, final int cnt) {
        final int delta = to - from;

        if ((array == null) || (delta == 0))
            return;

        final int length = array.length;

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0) {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                array[to_++] = array[from_++];
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                array[--to_] = array[--from_];
        }
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner (no overlap)
     * @param array array
     * @param from int
     * @param to int
     * @param cnt int
     */
    public static void innerCopy(final short[] array, final int from, final int to, final int cnt) {
        final int delta = to - from;

        if ((array == null) || (delta == 0))
            return;

        final int length = array.length;

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0) {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                array[to_++] = array[from_++];
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                array[--to_] = array[--from_];
        }
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner (no overlap)
     * @param array array
     * @param from int
     * @param to int
     * @param cnt int
     */
    public static void innerCopy(final int[] array, final int from, final int to, final int cnt) {
        final int delta = to - from;

        if ((array == null) || (delta == 0))
            return;

        final int length = array.length;

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0) {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                array[to_++] = array[from_++];
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                array[--to_] = array[--from_];
        }
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner (no overlap)
     * @param array array
     * @param from int
     * @param to int
     * @param cnt int
     */
    public static void innerCopy(final long[] array, final int from, final int to, final int cnt) {
        final int delta = to - from;

        if ((array == null) || (delta == 0))
            return;

        final int length = array.length;

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0) {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                array[to_++] = array[from_++];
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                array[--to_] = array[--from_];
        }
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner (no overlap)
     * @param array array
     * @param from int
     * @param to int
     * @param cnt int
     */
    public static void innerCopy(final float[] array, final int from, final int to, final int cnt) {
        final int delta = to - from;

        if ((array == null) || (delta == 0))
            return;

        final int length = array.length;

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0) {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                array[to_++] = array[from_++];
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                array[--to_] = array[--from_];
        }
    }

    /**
     * Copy 'cnt' elements from 'from' index to 'to' index in a safe manner (no overlap)
     * @param array array
     * @param from int
     * @param to int
     * @param cnt int
     */
    public static void innerCopy(final double[] array, final int from, final int to, final int cnt) {
        final int delta = to - from;

        if ((array == null) || (delta == 0))
            return;

        final int length = array.length;

        if ((from < 0) || (to < 0) || (from >= length) || (to >= length))
            return;

        final int adjCnt;

        // forward copy
        if (delta < 0) {
            // adjust copy size
            if ((from + cnt) >= length)
                adjCnt = length - from;
            else
                adjCnt = cnt;

            int to_ = to;
            int from_ = from;
            for (int i = 0; i < adjCnt; i++)
                array[to_++] = array[from_++];
        }
        else
        // backward copy
        {
            // adjust copy size
            if ((to + cnt) >= length)
                adjCnt = length - to;
            else
                adjCnt = cnt;

            int to_ = to + cnt;
            int from_ = from + cnt;
            for (int i = 0; i < adjCnt; i++)
                array[--to_] = array[--from_];
        }
    }

    /**
     * @return Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' at specified offset.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in array
     * @param offset int
     */
    public static byte @NotNull [] toByteArray1D(final byte[] in, final byte[] out, final int offset) {
        final int len = getTotalLength(in);
        final byte[] result = allocIfNull(out, offset + len);

        if (in != null)
            System.arraycopy(in, 0, result, offset, len);

        return result;
    }

    /**
     * @return Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' at specified offset.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in array
     * @param offset int
     */
    public static short @NotNull [] toShortArray1D(final short[] in, final short[] out, final int offset) {
        final int len = getTotalLength(in);
        final short[] result = allocIfNull(out, offset + len);

        if (in != null)
            System.arraycopy(in, 0, result, offset, len);

        return result;
    }

    /**
     * @return Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' at specified offset.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in array
     * @param offset int
     */
    public static int @NotNull [] toIntArray1D(final int[] in, final int[] out, final int offset) {
        final int len = getTotalLength(in);
        final int[] result = allocIfNull(out, offset + len);

        if (in != null)
            System.arraycopy(in, 0, result, offset, len);

        return result;
    }

    /**
     * @return Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' at specified offset.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in array
     * @param offset int
     */
    public static long @NotNull [] toLongArray1D(final long[] in, final long[] out, final int offset) {
        final int len = getTotalLength(in);
        final long[] result = allocIfNull(out, offset + len);

        if (in != null)
            System.arraycopy(in, 0, result, offset, len);

        return result;
    }

    /**
     * @return Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' at specified offset.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in array
     * @param offset int
     */
    public static float @NotNull [] toFloatArray1D(final float[] in, final float[] out, final int offset) {
        final int len = getTotalLength(in);
        final float[] result = allocIfNull(out, offset + len);

        if (in != null)
            System.arraycopy(in, 0, result, offset, len);

        return result;
    }

    /**
     * @return Return the 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' at specified offset.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in array
     * @param offset int
     */
    public static double @NotNull [] toDoubleArray1D(final double[] in, final double[] out, final int offset) {
        final int len = getTotalLength(in);
        final double[] result = allocIfNull(out, offset + len);

        if (in != null)
            System.arraycopy(in, 0, result, offset, len);

        return result;
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @param signed    if input data are integer type then we assume them as signed data
     * @return Convert and return the 'in' 1D array in 'out' 1D array type.<br>
     */
    public static Object arrayToArray(final Object in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT -> shortArrayToArray((short[]) in, inOffset, out, outOffset, length, signed);
            case INT -> intArrayToArray((int[]) in, inOffset, out, outOffset, length, signed);
            case LONG -> longArrayToArray((long[]) in, inOffset, out, outOffset, length, signed);
            case FLOAT -> floatArrayToArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToArray((double[]) in, inOffset, out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @param in     input array
     * @param out    output array which is used to receive result (and so define wanted type)
     * @param signed if input data are integer type then we assume them as signed data
     * @return Convert and return the 'in' 1D array in 'out' 1D array type.
     */
    public static Object arrayToArray(final Object in, final Object out, final boolean signed) {
        return arrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @return Convert and return the 'in' double array in 'out' array type.<br>
     */
    public static Object doubleArrayToArray(final double[] in, final int inOffset, final Object out, final int outOffset, final int length) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> doubleArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT -> doubleArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);
            case INT -> doubleArrayToIntArray(in, inOffset, (int[]) out, outOffset, length);
            case LONG -> doubleArrayToLongArray(in, inOffset, (long[]) out, outOffset, length);
            case FLOAT -> doubleArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length);
            case DOUBLE -> doubleArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @param in  input array
     * @param out output array which is used to receive result (and so define wanted type)
     * @return Convert and return the 'in' double array in 'out' array type.<br>
     */
    public static Object doubleArrayToArray(final double[] in, final Object out) {
        return doubleArrayToArray(in, 0, out, 0, -1);
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @return Convert and return the 'in' float array in 'out' array type.<br>
     */
    public static Object floatArrayToArray(final float[] in, final int inOffset, final Object out, final int outOffset, final int length) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> floatArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT -> floatArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);
            case INT -> floatArrayToIntArray(in, inOffset, (int[]) out, outOffset, length);
            case LONG -> floatArrayToLongArray(in, inOffset, (long[]) out, outOffset, length);
            case FLOAT -> floatArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length);
            case DOUBLE -> floatArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @param in  input array
     * @param out output array which is used to receive result (and so define wanted type)
     * @return Convert and return the 'in' float array in 'out' array type.<br>
     */
    public static Object floatArrayToArray(final float[] in, final Object out) {
        return floatArrayToArray(in, 0, out, 0, -1);
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @param signed    assume input data as signed data
     * @return Convert and return the 'in' long array in 'out' array type.<br>
     */
    public static Object longArrayToArray(final long[] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> longArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT -> longArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);
            case INT -> longArrayToIntArray(in, inOffset, (int[]) out, outOffset, length);
            case LONG -> longArrayToLongArray(in, inOffset, (long[]) out, outOffset, length);
            case FLOAT -> longArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);
            case DOUBLE -> longArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, signed);
            default -> out;
        };
    }

    /**
     * @param in     input array
     * @param out    output array which is used to receive result (and so define wanted type)
     * @param signed assume input data as signed data
     * @return Convert and return the 'in' long array in 'out' array type.<br>
     */
    public static Object longArrayToArray(final long[] in, final Object out, final boolean signed) {
        return longArrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @param signed    assume input data as signed data
     * @return Convert and return the 'in' integer array in 'out' array type.<br>
     */
    public static Object intArrayToArray(final int[] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> intArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT -> intArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);
            case INT -> intArrayToIntArray(in, inOffset, (int[]) out, outOffset, length);
            case LONG -> intArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, signed);
            case FLOAT -> intArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);
            case DOUBLE -> intArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, signed);
            default -> out;
        };
    }

    /**
     * @param in     input array
     * @param out    output array which is used to receive result (and so define wanted type)
     * @param signed assume input data as signed data
     * @return Convert and return the 'in' integer array in 'out' array type.<br>
     */
    public static Object intArrayToArray(final int[] in, final Object out, final boolean signed) {
        return intArrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @param signed    assume input data as signed data
     * @return Convert and return the 'in' short array in 'out' array type.<br>
     */
    public static Object shortArrayToArray(final short[] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> shortArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT -> shortArrayToShortArray(in, inOffset, (short[]) out, outOffset, length);
            case INT -> shortArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, signed);
            case LONG -> shortArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, signed);
            case FLOAT -> shortArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);
            case DOUBLE -> shortArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, signed);
            default -> out;
        };
    }

    /**
     * @param in     input array
     * @param out    output array which is used to receive result (and so define wanted type)
     * @param signed assume input data as signed data
     * @return Convert and return the 'in' short array in 'out' array type.<br>
     */
    public static Object shortArrayToArray(final short[] in, final Object out, final boolean signed) {
        return shortArrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @param signed    assume input data as signed data
     * @return Convert and return the 'in' byte array in 'out' array type.<br>
     */
    public static Object byteArrayToArray(final byte[] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> byteArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT -> byteArrayToShortArray(in, inOffset, (short[]) out, outOffset, length, signed);
            case INT -> byteArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, signed);
            case LONG -> byteArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, signed);
            case FLOAT -> byteArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, signed);
            case DOUBLE -> byteArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, signed);
            default -> out;
        };
    }

    /**
     * @param in     input array
     * @param out    output array which is used to receive result (and so define wanted type)
     * @param signed assume input data as signed data
     * @return Convert and return the 'in' byte array in 'out' array type.<br>
     */
    public static Object byteArrayToArray(final byte[] in, final Object out, final boolean signed) {
        return byteArrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @param signed    assume input data as signed data
     * @return Convert and return the 'in' array in 'out' double array.<br>
     */
    public static double[] arrayToDoubleArray(final Object in, final int inOffset, final double[] out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToDoubleArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT -> shortArrayToDoubleArray((short[]) in, inOffset, out, outOffset, length, signed);
            case INT -> intArrayToDoubleArray((int[]) in, inOffset, out, outOffset, length, signed);
            case LONG -> longArrayToDoubleArray((long[]) in, inOffset, out, outOffset, length, signed);
            case FLOAT -> floatArrayToDoubleArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToDoubleArray((double[]) in, inOffset, out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @param in     input array
     * @param out    output array which is used to receive result (and so define wanted type)
     * @param signed assume input data as signed data
     * @return Convert and return the 'in' array in 'out' double array.<br>
     */
    public static double[] arrayToDoubleArray(final Object in, final double[] out, final boolean signed) {
        return arrayToDoubleArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @param in     input array
     * @param signed assume input data as signed data
     * @return Convert and return the 'in' array as a double array.<br>
     */
    public static double[] arrayToDoubleArray(final Object in, final boolean signed) {
        return arrayToDoubleArray(in, 0, null, 0, -1, signed);
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @param signed    assume input data as signed data
     * @return Convert and return the 'in' array in 'out' float array.<br>
     */
    public static float[] arrayToFloatArray(final Object in, final int inOffset, final float[] out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToFloatArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT -> shortArrayToFloatArray((short[]) in, inOffset, out, outOffset, length, signed);
            case INT -> intArrayToFloatArray((int[]) in, inOffset, out, outOffset, length, signed);
            case LONG -> longArrayToFloatArray((long[]) in, inOffset, out, outOffset, length, signed);
            case FLOAT -> floatArrayToFloatArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToFloatArray((double[]) in, inOffset, out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @param in     input array
     * @param out    output array which is used to receive result (and so define wanted type)
     * @param signed assume input data as signed data
     * @return Convert and return the 'in' array in 'out' float array.<br>
     */
    public static float[] arrayToFloatArray(final Object in, final float[] out, final boolean signed) {
        return arrayToFloatArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @param in     input array
     * @param signed assume input data as signed data
     * @return Convert and return the 'in' array as a float array.<br>
     */
    public static float[] arrayToFloatArray(final Object in, final boolean signed) {
        return arrayToFloatArray(in, 0, null, 0, -1, signed);
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @param signed    assume input data as signed data
     * @return Convert and return the 'in' array in 'out' int array.<br>
     */
    public static int[] arrayToIntArray(final Object in, final int inOffset, final int[] out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToIntArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT -> shortArrayToIntArray((short[]) in, inOffset, out, outOffset, length, signed);
            case INT -> intArrayToIntArray((int[]) in, inOffset, out, outOffset, length);
            case LONG -> longArrayToIntArray((long[]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToIntArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToIntArray((double[]) in, inOffset, out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @param in     input array
     * @param out    output array which is used to receive result (and so define wanted type)
     * @param signed assume input data as signed data
     * @return Convert and return the 'in' array in 'out' int array.<br>
     */
    public static int[] arrayToIntArray(final Object in, final int[] out, final boolean signed) {
        return arrayToIntArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @param in     input array
     * @param signed assume input data as signed data
     * @return Convert and return the 'in' array as a int array.<br>
     */
    public static int[] arrayToIntArray(final Object in, final boolean signed) {
        return arrayToIntArray(in, 0, null, 0, -1, signed);
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @param signed    assume input data as signed data
     * @return Convert and return the 'in' array in 'out' short array.<br>
     */
    public static short[] arrayToShortArray(final Object in, final int inOffset, final short[] out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToShortArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT -> shortArrayToShortArray((short[]) in, inOffset, out, outOffset, length);
            case INT -> intArrayToShortArray((int[]) in, inOffset, out, outOffset, length);
            case LONG -> longArrayToShortArray((long[]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToShortArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToShortArray((double[]) in, inOffset, out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @param in     input array
     * @param out    output array which is used to receive result (and so define wanted type)
     * @param signed assume input data as signed data
     * @return Convert and return the 'in' array in 'out' short array.<br>
     */
    public static short[] arrayToShortArray(final Object in, final short[] out, final boolean signed) {
        return arrayToShortArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @param in     input array
     * @param signed assume input data as signed data
     * @return Convert and return the 'in' array as a short array.<br>
     */
    public static short[] arrayToShortArray(final Object in, final boolean signed) {
        return arrayToShortArray(in, 0, null, 0, -1, signed);
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @return Convert and return the 'in' array in 'out' byte array.<br>
     */
    public static byte[] arrayToByteArray(final Object in, final int inOffset, final byte[] out, final int outOffset, final int length) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToByteArray((byte[]) in, inOffset, out, outOffset, length);
            case SHORT -> shortArrayToByteArray((short[]) in, inOffset, out, outOffset, length);
            case INT -> intArrayToByteArray((int[]) in, inOffset, out, outOffset, length);
            case LONG -> longArrayToByteArray((long[]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToByteArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToByteArray((double[]) in, inOffset, out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @param in  input array
     * @param out output array which is used to receive result (and so define wanted type)
     * @return Convert and return the 'in' array in 'out' byte array.<br>
     */
    public static byte[] arrayToByteArray(final Object in, final byte[] out) {
        return arrayToByteArray(in, 0, out, 0, -1);
    }

    /**
     * @param in input array
     * @return Convert and return the 'in' array as a byte array.<br>
     */
    public static byte[] arrayToByteArray(final Object in) {
        return arrayToByteArray(in, 0, null, 0, -1);
    }

    public static double @NotNull [] doubleArrayToDoubleArray(final double[] in, final int inOffset, final double[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[] result = allocIfNull(out, outOffset + len);

        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    public static float @NotNull [] doubleArrayToFloatArray(final double[] in, final int inOffset, final float[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (float) in[i + inOffset];

        return result;
    }

    public static long @NotNull [] doubleArrayToLongArray(final double[] in, final int inOffset, final long[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = TypeUtil.toLong(in[i + inOffset]);

        return result;
    }

    public static int @NotNull [] doubleArrayToIntArray(final double[] in, final int inOffset, final int[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = TypeUtil.toInt(in[i + inOffset]);

        return result;
    }

    public static short @NotNull [] doubleArrayToShortArray(final double[] in, final int inOffset, final short[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (short) in[i + inOffset];

        return result;
    }

    public static byte @NotNull [] doubleArrayToByteArray(final double[] in, final int inOffset, final byte[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (byte) in[i + inOffset];

        return result;
    }

    public static double @NotNull [] floatArrayToDoubleArray(final float[] in, final int inOffset, final double[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = in[i + inOffset];

        return result;
    }

    public static float @NotNull [] floatArrayToFloatArray(final float[] in, final int inOffset, final float[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[] result = allocIfNull(out, outOffset + len);

        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    public static long @NotNull [] floatArrayToLongArray(final float[] in, final int inOffset, final long[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = TypeUtil.toLong(in[i + inOffset]);

        return result;
    }

    public static int @NotNull [] floatArrayToIntArray(final float[] in, final int inOffset, final int[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = TypeUtil.toInt(in[i + inOffset]);

        return result;
    }

    public static short @NotNull [] floatArrayToShortArray(final float[] in, final int inOffset, final short[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (short) in[i + inOffset];

        return result;
    }

    public static byte @NotNull [] floatArrayToByteArray(final float[] in, final int inOffset, final byte[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (byte) in[i + inOffset];

        return result;
    }

    public static double @NotNull [] longArrayToDoubleArray(final long[] in, final int inOffset, final double[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static float @NotNull [] longArrayToFloatArray(final long[] in, final int inOffset, final float[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsignF(in[i + inOffset]);
        }

        return result;
    }

    public static long @NotNull [] longArrayToLongArray(final long[] in, final int inOffset, final long[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] result = allocIfNull(out, outOffset + len);

        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    public static int @NotNull [] longArrayToIntArray(final long[] in, final int inOffset, final int[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (int) in[i + inOffset];

        return result;
    }

    public static short @NotNull [] longArrayToShortArray(final long[] in, final int inOffset, final short[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (short) in[i + inOffset];

        return result;
    }

    public static byte @NotNull [] longArrayToByteArray(final long[] in, final int inOffset, final byte[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (byte) in[i + inOffset];

        return result;
    }

    public static double @NotNull [] intArrayToDoubleArray(final int[] in, final int inOffset, final double[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static float @NotNull [] intArrayToFloatArray(final int[] in, final int inOffset, final float[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static long @NotNull [] intArrayToLongArray(final int[] in, final int inOffset, final long[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static int @NotNull [] intArrayToIntArray(final int[] in, final int inOffset, final int[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    public static short @NotNull [] intArrayToShortArray(final int[] in, final int inOffset, final short[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (short) in[i + inOffset];

        return result;
    }

    public static byte @NotNull [] intArrayToByteArray(final int[] in, final int inOffset, final byte[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (byte) in[i + inOffset];

        return result;
    }

    public static double @NotNull [] shortArrayToDoubleArray(final short[] in, final int inOffset, final double[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static float @NotNull [] shortArrayToFloatArray(final short[] in, final int inOffset, final float[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static long @NotNull [] shortArrayToLongArray(final short[] in, final int inOffset, final long[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsignL(in[i + inOffset]);
        }

        return result;
    }

    public static int @NotNull [] shortArrayToIntArray(final short[] in, final int inOffset, final int[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static short @NotNull [] shortArrayToShortArray(final short[] in, final int inOffset, final short[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] result = allocIfNull(out, outOffset + len);

        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    public static byte @NotNull [] shortArrayToByteArray(final short[] in, final int inOffset, final byte[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = (byte) in[i + inOffset];

        return result;
    }

    public static double @NotNull [] byteArrayToDoubleArray(final byte[] in, final int inOffset, final double[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static float @NotNull [] byteArrayToFloatArray(final byte[] in, final int inOffset, final float[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static long @NotNull [] byteArrayToLongArray(final byte[] in, final int inOffset, final long[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsignL(in[i + inOffset]);
        }

        return result;
    }

    public static int @NotNull [] byteArrayToIntArray(final byte[] in, final int inOffset, final int[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static short @NotNull [] byteArrayToShortArray(final byte[] in, final int inOffset, final short[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] result = allocIfNull(out, outOffset + len);

        if (signed) {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                result[i + outOffset] = (short) TypeUtil.unsign(in[i + inOffset]);
        }

        return result;
    }

    public static byte @NotNull [] byteArrayToByteArray(final byte[] in, final int inOffset, final byte[] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] result = allocIfNull(out, outOffset + len);

        System.arraycopy(in, inOffset, result, outOffset, len);

        return result;
    }

    public static float @NotNull [] doubleArrayToFloatArray(final double[] array) {
        return doubleArrayToFloatArray(array, 0, null, 0, array.length);
    }

    public static long @NotNull [] doubleArrayToLongArray(final double[] array) {
        return doubleArrayToLongArray(array, 0, null, 0, array.length);
    }

    public static int @NotNull [] doubleArrayToIntArray(final double[] array) {
        return doubleArrayToIntArray(array, 0, null, 0, array.length);
    }

    public static short @NotNull [] doubleArrayToShortArray(final double[] array) {
        return doubleArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte @NotNull [] doubleArrayToByteArray(final double[] array) {
        return doubleArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double @NotNull [] floatArrayToDoubleArray(final float[] array) {
        return floatArrayToDoubleArray(array, 0, null, 0, array.length);
    }

    public static long @NotNull [] floatArrayToLongArray(final float[] array) {
        return floatArrayToLongArray(array, 0, null, 0, array.length);
    }

    public static int @NotNull [] floatArrayToIntArray(final float[] array) {
        return floatArrayToIntArray(array, 0, null, 0, array.length);
    }

    public static short @NotNull [] floatArrayToShortArray(final float[] array) {
        return floatArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte @NotNull [] floatArrayToByteArray(final float[] array) {
        return floatArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double @NotNull [] longArrayToDoubleArray(final long[] array, final boolean signed) {
        return longArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float @NotNull [] longArrayToFloatArray(final long[] array, final boolean signed) {
        return longArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static short @NotNull [] longArrayToShortArray(final long[] array) {
        return longArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte @NotNull [] longArrayToByteArray(final long[] array) {
        return longArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double @NotNull [] intArrayToDoubleArray(final int[] array, final boolean signed) {
        return intArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float @NotNull [] intArrayToFloatArray(final int[] array, final boolean signed) {
        return intArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static long @NotNull [] intArrayToLongArray(final int[] array, final boolean signed) {
        return intArrayToLongArray(array, 0, null, 0, array.length, signed);
    }

    public static short @NotNull [] intArrayToShortArray(final int[] array) {
        return intArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte @NotNull [] intArrayToByteArray(final int[] array) {
        return intArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double @NotNull [] shortArrayToDoubleArray(final short[] array, final boolean signed) {
        return shortArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float @NotNull [] shortArrayToFloatArray(final short[] array, final boolean signed) {
        return shortArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static long @NotNull [] shortArrayToLongArray(final short[] array, final boolean signed) {
        return shortArrayToLongArray(array, 0, null, 0, array.length, signed);
    }

    public static int @NotNull [] shortArrayToIntArray(final short[] array, final boolean signed) {
        return shortArrayToIntArray(array, 0, null, 0, array.length, signed);
    }

    public static byte @NotNull [] shortArrayToByteArray(final short[] array) {
        return shortArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double @NotNull [] byteArrayToDoubleArray(final byte[] array, final boolean signed) {
        return byteArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float @NotNull [] byteArrayToFloatArray(final byte[] array, final boolean signed) {
        return byteArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static long @NotNull [] byteArrayToLongArray(final byte[] array, final boolean signed) {
        return byteArrayToLongArray(array, 0, null, 0, array.length, signed);
    }

    public static int @NotNull [] byteArrayToIntArray(final byte[] array, final boolean signed) {
        return byteArrayToIntArray(array, 0, null, 0, array.length, signed);
    }

    public static short @NotNull [] byteArrayToShortArray(final byte[] array, final boolean signed) {
        return byteArrayToShortArray(array, 0, null, 0, array.length, signed);
    }

    public static Object doubleArrayToSafeArray(final double[] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> doubleArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, signed);
            case SHORT -> doubleArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, signed);
            case INT -> doubleArrayToSafeIntArray(in, inOffset, (int[]) out, outOffset, length, signed);
            case LONG -> doubleArrayToSafeLongArray(in, inOffset, (long[]) out, outOffset, length, signed);
            case FLOAT -> doubleArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length);
            case DOUBLE -> doubleArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length);
            default -> out;
        };
    }

    public static Object doubleArrayToSafeArray(final double[] in, final Object out, final boolean signed) {
        return doubleArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object floatArrayToSafeArray(final float[] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> floatArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, signed);
            case SHORT -> floatArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, signed);
            case INT -> floatArrayToSafeIntArray(in, inOffset, (int[]) out, outOffset, length, signed);
            case LONG -> floatArrayToSafeLongArray(in, inOffset, (long[]) out, outOffset, length, signed);
            case FLOAT -> floatArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length);
            case DOUBLE -> floatArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length);
            default -> out;
        };
    }

    public static Object floatArrayToSafeArray(final float[] in, final Object out, final boolean signed) {
        return floatArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object longArrayToSafeArray(final long[] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> longArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT -> longArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, srcSigned, dstSigned);
            case INT -> longArrayToSafeIntArray(in, inOffset, (int[]) out, outOffset, length, srcSigned, dstSigned);
            case LONG -> longArrayToSafeLongArray(in, inOffset, (long[]) out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> longArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, srcSigned);
            case DOUBLE -> longArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, srcSigned);
            default -> out;
        };
    }

    public static Object longArrayToSafeArray(final long[] in, final Object out, final boolean srcSigned, final boolean dstSigned) {
        return longArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static Object intArrayToSafeArray(final int[] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> intArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT -> intArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, srcSigned, dstSigned);
            case INT -> intArrayToSafeIntArray(in, inOffset, (int[]) out, outOffset, length, srcSigned, dstSigned);
            case LONG -> intArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, srcSigned);
            case FLOAT -> intArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, srcSigned);
            case DOUBLE -> intArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, srcSigned);
            default -> out;
        };
    }

    public static Object intArrayToSafeArray(final int[] in, final Object out, final boolean srcSigned, final boolean dstSigned) {
        return intArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static Object shortArrayToSafeArray(final short[] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> shortArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT -> shortArrayToSafeShortArray(in, inOffset, (short[]) out, outOffset, length, srcSigned, dstSigned);
            case INT -> shortArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, srcSigned);
            case LONG -> shortArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, srcSigned);
            case FLOAT -> shortArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, srcSigned);
            case DOUBLE -> shortArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, srcSigned);
            default -> out;
        };
    }

    public static Object shortArrayToSafeArray(final short[] in, final Object out, final boolean srcSigned, final boolean dstSigned) {
        return shortArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static Object byteArrayToSafeArray(final byte[] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> byteArrayToSafeByteArray(in, inOffset, (byte[]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT -> byteArrayToShortArray(in, inOffset, (short[]) out, outOffset, length, srcSigned);
            case INT -> byteArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, srcSigned);
            case LONG -> byteArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, srcSigned);
            case FLOAT -> byteArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, srcSigned);
            case DOUBLE -> byteArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, srcSigned);
            default -> out;
        };
    }

    public static Object byteArrayToSafeArray(final byte[] in, final Object out, final boolean srcSigned, final boolean dstSigned) {
        return byteArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static long[] arrayToSafeLongArray(final Object in, final int inOffset, final long[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToLongArray((byte[]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT -> shortArrayToLongArray((short[]) in, inOffset, out, outOffset, length, srcSigned);
            case INT -> intArrayToLongArray((int[]) in, inOffset, out, outOffset, length, srcSigned);
            case LONG -> longArrayToSafeLongArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeLongArray((float[]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeLongArray((double[]) in, inOffset, out, outOffset, length, dstSigned);
            default -> out;
        };
    }

    public static long[] arrayToSafeLongArray(final Object in, final long[] out, final boolean srcSigned, final boolean dstSigned) {
        return arrayToSafeLongArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static int[] arrayToSafeIntArray(final Object in, final int inOffset, final int[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToIntArray((byte[]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT -> shortArrayToIntArray((short[]) in, inOffset, out, outOffset, length, srcSigned);
            case INT -> intArrayToSafeIntArray((int[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG -> longArrayToSafeIntArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeIntArray((float[]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeIntArray((double[]) in, inOffset, out, outOffset, length, dstSigned);
            default -> out;
        };
    }

    public static int[] arrayToSafeIntArray(final Object in, final int[] out, final boolean srcSigned, final boolean dstSigned) {
        return arrayToSafeIntArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static short[] arrayToSafeShortArray(final Object in, final int inOffset, final short[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToShortArray((byte[]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT -> shortArrayToSafeShortArray((short[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case INT -> intArrayToSafeShortArray((int[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG -> longArrayToSafeShortArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeShortArray((float[]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeShortArray((double[]) in, inOffset, out, outOffset, length, dstSigned);
            default -> out;
        };
    }

    public static short[] arrayToSafeShortArray(final Object in, final short[] out, final boolean srcSigned, final boolean dstSigned) {
        return arrayToSafeShortArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static byte[] arrayToSafeByteArray(final Object in, final int inOffset, final byte[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToSafeByteArray((byte[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case SHORT -> shortArrayToSafeByteArray((short[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case INT -> intArrayToSafeByteArray((int[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG -> longArrayToSafeByteArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeByteArray((float[]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeByteArray((double[]) in, inOffset, out, outOffset, length, dstSigned);
            default -> out;
        };
    }

    public static byte[] arrayToSafeByteArray(final Object in, final byte[] out, final boolean srcSigned, final boolean dstSigned) {
        return arrayToSafeByteArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static long @NotNull [] doubleArrayToSafeLongArray(final double[] in, final int inOffset, final long[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] outArray = allocIfNull(out, outOffset + len);

        if (signed) {
            // by default value is clamped to [Long.MIN_VALUE..Long.MAX_VALUE] range
            for (int i = 0; i < len; i++)
                outArray[i + outOffset] = (long) in[i + inOffset];
        }
        else {
            final double minValue = 0d;
            final double maxValue = DataType.ULONG_MAX_VALUE;
            final long minValueT = 0L;
            final long maxValueT = 0xFFFFFFFFFFFFFFFFL;

            for (int i = 0; i < len; i++) {
                final double value = in[i + inOffset];
                final long result;

                if (value >= maxValue)
                    result = maxValueT;
                else if (value <= minValue)
                    result = minValueT;
                else
                    result = TypeUtil.toLong(value);

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static int @NotNull [] doubleArrayToSafeIntArray(final double[] in, final int inOffset, final int[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] outArray = allocIfNull(out, outOffset + len);

        if (signed) {
            // by default value is clamped to [Integer.MIN_VALUE..Integer.MAX_VALUE] range
            for (int i = 0; i < len; i++)
                outArray[i + outOffset] = (int) in[i + inOffset];
        }
        else {
            final double minValue = 0d;
            final double maxValue = DataType.UINT_MAX_VALUE;
            final int minValueT = 0;
            final int maxValueT = 0xFFFFFFFF;

            for (int i = 0; i < len; i++) {
                final double value = in[i + inOffset];
                final int result;

                if (value >= maxValue)
                    result = maxValueT;
                else if (value <= minValue)
                    result = minValueT;
                else
                    result = TypeUtil.toInt(value);

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static short @NotNull [] doubleArrayToSafeShortArray(final double[] in, final int inOffset, final short[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] outArray = allocIfNull(out, outOffset + len);

        final double minValue;
        final double maxValue;

        if (signed) {
            minValue = DataType.SHORT.getMinValue();
            maxValue = DataType.SHORT.getMaxValue();
        }
        else {
            minValue = DataType.USHORT.getMinValue();
            maxValue = DataType.USHORT.getMaxValue();
        }

        final short minValueT = (short) minValue;
        final short maxValueT = (short) maxValue;

        for (int i = 0; i < len; i++) {
            final double value = in[i + inOffset];
            final short result;

            if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (short) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static byte @NotNull [] doubleArrayToSafeByteArray(final double[] in, final int inOffset, final byte[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        final double minValue;
        final double maxValue;

        if (signed) {
            minValue = DataType.BYTE.getMinValue();
            maxValue = DataType.BYTE.getMaxValue();
        }
        else {
            minValue = DataType.UBYTE.getMinValue();
            maxValue = DataType.UBYTE.getMaxValue();
        }

        final byte minValueT = (byte) minValue;
        final byte maxValueT = (byte) maxValue;

        for (int i = 0; i < len; i++) {
            final double value = in[i + inOffset];
            final byte result;

            if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (byte) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static long @NotNull [] floatArrayToSafeLongArray(final float[] in, final int inOffset, final long[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] outArray = allocIfNull(out, outOffset + len);

        if (signed) {
            // by default value is clamped to [Long.MIN_VALUE..Long.MAX_VALUE] range
            for (int i = 0; i < len; i++)
                outArray[i + outOffset] = (long) in[i + inOffset];
        }
        else {
            final float minValue = 0f;
            final float maxValue = DataType.ULONG_MAX_VALUE_F;
            final long minValueT = 0L;
            final long maxValueT = 0xFFFFFFFFFFFFFFFFL;

            for (int i = 0; i < len; i++) {
                final float value = in[i + inOffset];
                final long result;

                if (value >= maxValue)
                    result = maxValueT;
                else if (value <= minValue)
                    result = minValueT;
                else
                    result = TypeUtil.toLong(value);

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static int @NotNull [] floatArrayToSafeIntArray(final float[] in, final int inOffset, final int[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] outArray = allocIfNull(out, outOffset + len);

        if (signed) {
            // by default value is clamped to [Integer.MIN_VALUE..Integer.MAX_VALUE] range
            for (int i = 0; i < len; i++)
                outArray[i + outOffset] = (int) in[i + inOffset];
        }
        else {
            final float minValue = 0f;
            final float maxValue = DataType.UINT_MAX_VALUE_F;
            final int minValueT = 0;
            final int maxValueT = 0xFFFFFFFF;

            for (int i = 0; i < len; i++) {
                final float value = in[i + inOffset];
                final int result;

                if (value >= maxValue)
                    result = maxValueT;
                else if (value <= minValue)
                    result = minValueT;
                else
                    result = TypeUtil.toInt(value);

                outArray[i + outOffset] = result;
            }
        }

        return outArray;
    }

    public static short @NotNull [] floatArrayToSafeShortArray(final float[] in, final int inOffset, final short[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] outArray = allocIfNull(out, outOffset + len);

        final float minValue;
        final float maxValue;

        if (signed) {
            minValue = (float) DataType.SHORT.getMinValue();
            maxValue = (float) DataType.SHORT.getMaxValue();
        }
        else {
            minValue = (float) DataType.USHORT.getMinValue();
            maxValue = (float) DataType.USHORT.getMaxValue();
        }

        final short minValueT = (short) minValue;
        final short maxValueT = (short) maxValue;

        for (int i = 0; i < len; i++) {
            final float value = in[i + inOffset];
            final short result;

            if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (short) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static byte @NotNull [] floatArrayToSafeByteArray(final float[] in, final int inOffset, final byte[] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        final float minValue;
        final float maxValue;

        if (signed) {
            minValue = (float) DataType.BYTE.getMinValue();
            maxValue = (float) DataType.BYTE.getMaxValue();
        }
        else {
            minValue = (float) DataType.UBYTE.getMinValue();
            maxValue = (float) DataType.UBYTE.getMaxValue();
        }

        final byte minValueT = (byte) minValue;
        final byte maxValueT = (byte) maxValue;

        for (int i = 0; i < len; i++) {
            final float value = in[i + inOffset];
            final byte result;

            if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (byte) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static long[] longArrayToSafeLongArray(final long[] in, final int inOffset, final long[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        // same sign ?
        if (srcSigned == dstSigned)
            return longArrayToLongArray(in, inOffset, out, outOffset, length);

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[] outArray = allocIfNull(out, outOffset + len);
        final long maxValue = Long.MAX_VALUE;

        for (int i = 0; i < len; i++) {
            long value = in[i + inOffset];

            // signed and unsigned on other side --> need to clamp
            if (value < 0) {
                if (srcSigned)
                    value = 0;
                else
                    value = maxValue;
            }

            outArray[i + outOffset] = value;
        }

        return outArray;
    }

    public static int @NotNull [] longArrayToSafeIntArray(final long[] in, final int inOffset, final int[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] outArray = allocIfNull(out, outOffset + len);

        final long minValue;
        final long maxValue;

        if (dstSigned) {
            minValue = (long) DataType.INT.getMinValue();
            maxValue = (long) DataType.INT.getMaxValue();
        }
        else {
            minValue = (long) DataType.UINT.getMinValue();
            maxValue = (long) DataType.UINT.getMaxValue();
        }

        final int minValueT = (int) minValue;
        final int maxValueT = (int) maxValue;

        for (int i = 0; i < len; i++) {
            final long value = in[i + inOffset];
            final int result;

            if ((!srcSigned) && (value < 0))
                result = maxValueT;
            else if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (int) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static short @NotNull [] longArrayToSafeShortArray(final long[] in, final int inOffset, final short[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] outArray = allocIfNull(out, outOffset + len);

        final long minValue;
        final long maxValue;

        if (dstSigned) {
            minValue = (long) DataType.SHORT.getMinValue();
            maxValue = (long) DataType.SHORT.getMaxValue();
        }
        else {
            minValue = (long) DataType.USHORT.getMinValue();
            maxValue = (long) DataType.USHORT.getMaxValue();
        }

        final short minValueT = (short) minValue;
        final short maxValueT = (short) maxValue;

        for (int i = 0; i < len; i++) {
            final long value = in[i + inOffset];
            final short result;

            if ((!srcSigned) && (value < 0))
                result = maxValueT;
            else if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (short) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static byte @NotNull [] longArrayToSafeByteArray(final long[] in, final int inOffset, final byte[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        final long minValue;
        final long maxValue;

        if (dstSigned) {
            minValue = (long) DataType.BYTE.getMinValue();
            maxValue = (long) DataType.BYTE.getMaxValue();
        }
        else {
            minValue = (long) DataType.UBYTE.getMinValue();
            maxValue = (long) DataType.UBYTE.getMaxValue();
        }

        final byte minValueT = (byte) minValue;
        final byte maxValueT = (byte) maxValue;

        for (int i = 0; i < len; i++) {
            final long value = in[i + inOffset];
            final byte result;

            if ((!srcSigned) && (value < 0))
                result = maxValueT;
            else if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (byte) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static int[] intArrayToSafeIntArray(final int[] in, final int inOffset, final int[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        // same sign ?
        if (srcSigned == dstSigned)
            return intArrayToIntArray(in, inOffset, out, outOffset, length);

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[] outArray = allocIfNull(out, outOffset + len);
        final int maxValue = Integer.MAX_VALUE;

        for (int i = 0; i < len; i++) {
            int value = in[i + inOffset];

            // signed and unsigned on other side --> need to clamp
            if (value < 0) {
                if (srcSigned)
                    value = 0;
                else
                    value = maxValue;
            }

            outArray[i + outOffset] = value;
        }

        return outArray;
    }

    public static short @NotNull [] intArrayToSafeShortArray(final int[] in, final int inOffset, final short[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] outArray = allocIfNull(out, outOffset + len);

        final int minValue;
        final int maxValue;

        if (dstSigned) {
            minValue = (int) DataType.SHORT.getMinValue();
            maxValue = (int) DataType.SHORT.getMaxValue();
        }
        else {
            minValue = (int) DataType.USHORT.getMinValue();
            maxValue = (int) DataType.USHORT.getMaxValue();
        }

        final short minValueT = (short) minValue;
        final short maxValueT = (short) maxValue;

        for (int i = 0; i < len; i++) {
            final int value = in[i + inOffset];
            final short result;

            if ((!srcSigned) && (value < 0))
                result = maxValueT;
            else if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (short) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static byte @NotNull [] intArrayToSafeByteArray(final int[] in, final int inOffset, final byte[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        final int minValue;
        final int maxValue;

        if (dstSigned) {
            minValue = (int) DataType.BYTE.getMinValue();
            maxValue = (int) DataType.BYTE.getMaxValue();
        }
        else {
            minValue = (int) DataType.UBYTE.getMinValue();
            maxValue = (int) DataType.UBYTE.getMaxValue();
        }

        final byte minValueT = (byte) minValue;
        final byte maxValueT = (byte) maxValue;

        for (int i = 0; i < len; i++) {
            final int value = in[i + inOffset];
            final byte result;

            if ((!srcSigned) && (value < 0))
                result = maxValueT;
            else if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (byte) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static short[] shortArrayToSafeShortArray(final short[] in, final int inOffset, final short[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        // same sign ?
        if (srcSigned == dstSigned)
            return shortArrayToShortArray(in, inOffset, out, outOffset, length);

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[] outArray = allocIfNull(out, outOffset + len);
        final short maxValue = Short.MAX_VALUE;

        for (int i = 0; i < len; i++) {
            short value = in[i + inOffset];

            // signed and unsigned on other side --> need to clamp
            if (value < 0) {
                if (srcSigned)
                    value = 0;
                else
                    value = maxValue;
            }

            outArray[i + outOffset] = value;
        }

        return outArray;
    }

    public static byte @NotNull [] shortArrayToSafeByteArray(final short[] in, final int inOffset, final byte[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);

        final short minValue;
        final short maxValue;

        if (dstSigned) {
            minValue = (short) DataType.BYTE.getMinValue();
            maxValue = (short) DataType.BYTE.getMaxValue();
        }
        else {
            minValue = (short) DataType.UBYTE.getMinValue();
            maxValue = (short) DataType.UBYTE.getMaxValue();
        }

        final byte minValueT = (byte) minValue;
        final byte maxValueT = (byte) maxValue;

        for (int i = 0; i < len; i++) {
            final short value = in[i + inOffset];
            final byte result;

            if ((!srcSigned) && (value < 0))
                result = maxValueT;
            else if (value >= maxValue)
                result = maxValueT;
            else if (value <= minValue)
                result = minValueT;
            else
                result = (byte) value;

            outArray[i + outOffset] = result;
        }

        return outArray;
    }

    public static byte[] byteArrayToSafeByteArray(final byte[] in, final int inOffset, final byte[] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        // same sign ?
        if (srcSigned == dstSigned)
            return byteArrayToByteArray(in, inOffset, out, outOffset, length);

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[] outArray = allocIfNull(out, outOffset + len);
        final byte maxValue = Byte.MAX_VALUE;

        for (int i = 0; i < len; i++) {
            byte value = in[i + inOffset];

            // signed and unsigned on other side --> need to clamp
            if (value < 0) {
                if (srcSigned)
                    value = 0;
                else
                    value = maxValue;
            }

            outArray[i + outOffset] = value;
        }

        return outArray;
    }

    public static long @NotNull [] doubleArrayToSafeLongArray(final double[] array, final boolean signed) {
        return doubleArrayToSafeLongArray(array, 0, null, 0, array.length, signed);
    }

    public static int @NotNull [] doubleArrayToSafeIntArray(final double[] array, final boolean signed) {
        return doubleArrayToSafeIntArray(array, 0, null, 0, array.length, signed);
    }

    public static short @NotNull [] doubleArrayToSafeShortArray(final double[] array, final boolean signed) {
        return doubleArrayToSafeShortArray(array, 0, null, 0, array.length, signed);
    }

    public static byte @NotNull [] doubleArrayToSafeByteArray(final double[] array, final boolean signed) {
        return doubleArrayToSafeByteArray(array, 0, null, 0, array.length, signed);
    }

    public static long @NotNull [] floatArrayToSafeLongArray(final float[] array, final boolean signed) {
        return floatArrayToSafeLongArray(array, 0, null, 0, array.length, signed);
    }

    public static int @NotNull [] floatArrayToSafeIntArray(final float[] array, final boolean signed) {
        return floatArrayToSafeIntArray(array, 0, null, 0, array.length, signed);
    }

    public static short @NotNull [] floatArrayToSafeShortArray(final float[] array, final boolean signed) {
        return floatArrayToSafeShortArray(array, 0, null, 0, array.length, signed);
    }

    public static byte @NotNull [] floatArrayToSafeByteArray(final float[] array, final boolean signed) {
        return floatArrayToSafeByteArray(array, 0, null, 0, array.length, signed);
    }

    public static int @NotNull [] longArrayToSafeIntArray(final long[] array, final boolean signed) {
        return longArrayToSafeIntArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static short @NotNull [] longArrayToSafeShortArray(final long[] array, final boolean signed) {
        return longArrayToSafeShortArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static byte @NotNull [] longArrayToSafeByteArray(final long[] array, final boolean signed) {
        return longArrayToSafeByteArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static short @NotNull [] intArrayToSafeShortArray(final int[] array, final boolean signed) {
        return intArrayToSafeShortArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static byte @NotNull [] intArrayToSafeByteArray(final int[] array, final boolean signed) {
        return intArrayToSafeByteArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static byte @NotNull [] shortArrayToSafeByteArray(final short[] array, final boolean signed) {
        return shortArrayToSafeByteArray(array, 0, null, 0, array.length, signed, signed);
    }

    /**
     * @param array 1D array containing values to return as string
     * @return Return the specified 1D array as string<br>
     * Default representation use ':' as separator character<br>
     * <br>
     * ex : [0,1,2,3,4] --&gt; "0:1:2:3:4"<br>
     */
    public static @NotNull String arrayToString(final Object array) {
        return arrayToString(array, false, false, ":", -1);
    }

    /**
     * @param array     1D array containing values to return as string
     * @param signed    input value are signed (only for integer data type)
     * @param hexa      set value in resulting string in hexa decimal format (only for integer data type)
     * @param separator specify the separator to use between each array value in resulting string
     * @param size      specify the number of significant number to display for float value (-1 means all)
     * @return Return the specified 1D array as string<br>
     * ex : [0,1,2,3,4] --&gt; "0:1:2:3:4"<br>
     * ex : [Obj0,Obj1,Obj2,Obj3,Obj4] --&gt; "Obj0:Obj1:Obj2:Obj3:Obj4"<br>
     */
    public static @NotNull String arrayToString(final Object array, final boolean signed, final boolean hexa, final String separator, final int size) {
        final int len = ArrayUtil.getLength(array);
        final DataType dataType = ArrayUtil.getDataType(array, signed);
        final StringBuilder result = new StringBuilder();
        final int base = hexa ? 16 : 10;

        switch (dataType) {
            case UBYTE: {
                final byte[] data = (byte[]) array;

                if (len > 0)
                    result.append(Integer.toString(data[0] & 0xFF, base));
                for (int i = 1; i < len; i++) {
                    result.append(separator);
                    result.append(Integer.toString(data[i] & 0xFF, base));
                }
                break;
            }

            case BYTE: {
                final byte[] data = (byte[]) array;

                if (len > 0)
                    result.append(Integer.toString(data[0], base));
                for (int i = 1; i < len; i++) {
                    result.append(separator);
                    result.append(Integer.toString(data[i], base));
                }
                break;
            }

            case USHORT: {
                final short[] data = (short[]) array;

                if (len > 0)
                    result.append(Integer.toString(data[0] & 0xFFFF, base));
                for (int i = 1; i < len; i++) {
                    result.append(separator);
                    result.append(Integer.toString(data[i] & 0xFFFF, base));
                }
                break;
            }
            case SHORT: {
                final short[] data = (short[]) array;

                if (len > 0)
                    result.append(Integer.toString(data[0], base));
                for (int i = 1; i < len; i++) {
                    result.append(separator);
                    result.append(Integer.toString(data[i], base));
                }
                break;
            }

            case UINT: {
                final int[] data = (int[]) array;

                if (len > 0)
                    result.append(Long.toString(data[0] & 0xFFFFFFFFL, base));
                for (int i = 1; i < len; i++) {
                    result.append(separator);
                    result.append(Long.toString(data[i] & 0xFFFFFFFFL, base));
                }
                break;
            }

            case INT: {
                final int[] data = (int[]) array;

                if (len > 0)
                    result.append(Integer.toString(data[0], base));
                for (int i = 1; i < len; i++) {
                    result.append(separator);
                    result.append(Integer.toString(data[i], base));
                }
                break;
            }

            case ULONG: {
                final long[] data = (long[]) array;

                // we lost highest bit as java doesn't have bigger than long type
                if (len > 0)
                    result.append(Long.toString(data[0] & 0x7FFFFFFFFFFFFFFFL, base));
                for (int i = 1; i < len; i++) {
                    result.append(separator);
                    result.append(Long.toString(data[i] & 0x7FFFFFFFFFFFFFFFL, base));
                }
                break;
            }

            case LONG: {
                final long[] data = (long[]) array;

                if (len > 0)
                    result.append(Long.toString(data[0], base));
                for (int i = 1; i < len; i++) {
                    result.append(separator);
                    result.append(Long.toString(data[i], base));
                }
                break;
            }

            case FLOAT: {
                final float[] data = (float[]) array;

                if (size == -1) {
                    if (len > 0)
                        result.append(data[0]);
                    for (int i = 1; i < len; i++) {
                        result.append(separator);
                        result.append(data[i]);
                    }
                }
                else {
                    if (len > 0)
                        result.append(MathUtil.roundSignificant(data[0], size, true));
                    for (int i = 1; i < len; i++) {
                        result.append(separator);
                        result.append(MathUtil.roundSignificant(data[i], size, true));
                    }
                }
                break;
            }

            case DOUBLE: {
                final double[] data = (double[]) array;

                if (size == -1) {
                    if (len > 0)
                        result.append(data[0]);
                    for (int i = 1; i < len; i++) {
                        result.append(separator);
                        result.append(data[i]);
                    }
                }
                else {
                    if (len > 0)
                        result.append(MathUtil.roundSignificant(data[0], size, true));
                    for (int i = 1; i < len; i++) {
                        result.append(separator);
                        result.append(MathUtil.roundSignificant(data[i], size, true));
                    }
                }
                break;
            }

            // generic method
            default: {
                if (len > 0)
                    result.append(Array.get(array, 0).toString());
                for (int i = 1; i < len; i++) {
                    result.append(separator);
                    result.append(Array.get(array, i).toString());
                }
            }
        }

        return result.toString();
    }

    /**
     * @param value    string containing value to return as 1D array
     * @param dataType specify the values data type and also the output array data type string
     * @return Return the specified string containing separated values as a 1D array<br>
     * By default separator is assumed to be ':' character<br>
     * ex : "0:1:2:3:4" --&gt; [0,1,2,3,4]<br>
     */
    public static Object stringToArray(final String value, final DataType dataType) {
        return stringToArray(value, dataType, false, ":");
    }

    /**
     * @param value     string containing value to return as 1D array
     * @param dataType  specify the values data type and also the output array data type string
     * @param hexa      values in string as stored as hexa values (only for integer data type)
     * @param separator specify the separator used between each value in the input string
     * @return Return the specified string containing separated values as a 1D array<br>
     * ex : "0:1:2:3:4" --&gt; [0,1,2,3,4]<br>
     */
    public static @Nullable Object stringToArray(final String value, final DataType dataType, final boolean hexa, final String separator) {
        if (StringUtil.isEmpty(value))
            return createArray(dataType, 0);

        final String[] values = value.split(separator);
        final int len = values.length;
        final int base = hexa ? 16 : 10;

        switch (dataType.getJavaType()) {
            case BYTE: {
                final byte[] result = new byte[len];

                for (int i = 0; i < len; i++)
                    result[i] = (byte) Integer.parseInt(values[i], base);

                return result;
            }

            case SHORT: {
                final short[] result = new short[len];

                for (int i = 0; i < len; i++)
                    result[i] = (short) Integer.parseInt(values[i], base);

                return result;
            }

            case INT: {
                final int[] result = new int[len];

                for (int i = 0; i < len; i++)
                    result[i] = Integer.parseInt(values[i], base);

                return result;
            }

            case LONG: {
                final long[] result = new long[len];

                for (int i = 0; i < len; i++)
                    result[i] = Long.parseLong(values[i], base);

                return result;
            }

            case FLOAT: {
                final float[] result = new float[len];

                for (int i = 0; i < len; i++)
                    result[i] = Float.parseFloat(values[i]);

                return result;
            }

            case DOUBLE: {
                final double[] result = new double[len];

                for (int i = 0; i < len; i++)
                    result[i] = Double.parseDouble(values[i]);

                return result;
            }
        }

        return null;
    }

    /**
     * @param array array
     * @return Convert a boolean array to a byte array (unpacked form : 1 boolean --&gt; 1 byte)
     */
    @Contract("null -> new")
    public static byte @NotNull [] toByteArray(final boolean[] array) {
        return toByteArray(array, null, 0);
    }

    /**
     * @return Convert a boolean array to a byte array (unpacked form : 1 boolean --&gt; 1 byte)
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in array
     * @param offset int
     */
    @Contract("null, _, _ -> new")
    public static byte @NotNull [] toByteArray(final boolean[] in, final byte[] out, final int offset) {
        if (in == null)
            return new byte[0];

        final int len = in.length;
        final byte[] result = allocIfNull(out, offset + len);

        for (int i = 0; i < len; i++)
            result[i] = (byte) ((in[i]) ? 1 : 0);

        return result;
    }

    /**
     * @param array array
     * @return Convert a byte array (unpacked form : 1 byte --&gt; 1 boolean) to a boolean array
     */
    @Contract("null -> new")
    public static boolean @NotNull [] toBooleanArray(final byte[] array) {
        return toBooleanArray(array, null, 0);
    }

    /**
     * @return Convert a boolean array to a byte array (unpacked form : 1 boolean --&gt; 1 byte)
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * @param out If (out == null) a new array is allocated.
     * @param offset int
     * @param in array
     */
    @Contract("null, _, _ -> new")
    public static boolean @NotNull [] toBooleanArray(final byte[] in, final boolean[] out, final int offset) {
        if (in == null)
            return new boolean[0];

        final int len = in.length;
        final boolean[] result = allocIfNull(out, offset + len);

        for (int i = 0; i < len; i++)
            result[i] = (in[i] != 0);

        return result;
    }

    /**
     * Retrieve interleaved byte data from 'in' array and return the result in the 'out' array.
     *
     * @param in        input byte array containing interleaved data
     * @param inOffset  input array offset
     * @param step      interleave step
     * @param out       output result array. If set to <code>null</code> then a new array is allocated.
     * @param outOffset output array offset
     * @param size      number of byte to retrieve
     * @return byte array containing de-interleaved data.
     */
    public static byte @NotNull [] getInterleavedData(final byte[] in, final int inOffset, final int step, final byte[] out, final int outOffset, final int size) {
        final byte[] result = allocIfNull(out, outOffset + size);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < size; i++) {
            result[outOff] = in[inOff];
            inOff += step;
            outOff++;
        }

        return result;
    }

    /**
     * De interleave data from 'in' array and return the result in the 'out' array.
     *
     * @param in        input byte array containing interleaved data
     * @param inOffset  input array offset
     * @param step      interleave step
     * @param out       output result array. If set to <code>null</code> then a new array is allocated
     * @param outOffset output array offset
     * @param size      number of element to de-interleave
     * @return byte array containing de-interleaved data.
     */
    public static byte @NotNull [] deInterleave(final byte[] in, final int inOffset, final int step, final byte[] out, final int outOffset, final int size) {
        final byte[] result = allocIfNull(out, outOffset + (size * step));

        int inOff1 = inOffset;
        int outOff1 = outOffset;

        for (int j = 0; j < step; j++) {
            int inOff2 = inOff1;
            int outOff2 = outOff1;

            for (int i = 0; i < size; i++) {
                result[outOff2] = in[inOff2];
                inOff2 += step;
                outOff2++;
            }

            inOff1++;
            outOff1 += size;
        }

        return result;
    }
}
