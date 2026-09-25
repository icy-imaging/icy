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

package fr.icy.common.collection.array;

import fr.icy.common.type.DataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

import static fr.icy.common.EnforcerUtil.enforceGreaterOrEqual;
import static fr.icy.common.EnforcerUtil.enforceRange;

/**
 * Utility class for various operations on 2D arrays.
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class Array2DUtil {
    /**
     * Computes the total length of all byte arrays contained in the given two-dimensional byte array.
     *
     * @param array a non-null two-dimensional array of non-null byte arrays.
     * @return the total length of all byte arrays contained in the two-dimensional array.
     * @throws NullPointerException if the provided array or any of its elements is {@code null}.
     */
    @Contract(pure = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final byte @NonNull [] @NonNull [] array) throws NullPointerException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        for (final byte[] bytes : array)
            result += bytes.length;

        return result;
    }

    /**
     * Computes the total length of all byte arrays contained in the given two-dimensional short array.
     *
     * @param array a non-null two-dimensional array of non-null short arrays.
     * @return the total length of all short arrays contained in the two-dimensional array.
     * @throws NullPointerException if the provided array or any of its elements is {@code null}.
     */
    @Contract(pure = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final short @NonNull [] @NonNull [] array) throws NullPointerException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        for (final short[] shorts : array)
            result += shorts.length;

        return result;
    }

    /**
     * Computes the total length of all byte arrays contained in the given two-dimensional int array.
     *
     * @param array a non-null two-dimensional array of non-null int arrays.
     * @return the total length of all int arrays contained in the two-dimensional array.
     * @throws NullPointerException if the provided array or any of its elements is {@code null}.
     */
    @Contract(pure = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final int @NonNull [] @NonNull [] array) throws NullPointerException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        for (final int[] ints : array)
            result += ints.length;

        return result;
    }

    /**
     * Computes the total length of all long arrays contained in the given two-dimensional long array.
     *
     * @param array a non-null two-dimensional array of non-null long arrays.
     * @return the total length of all long arrays contained in the two-dimensional array.
     * @throws NullPointerException if the provided array or any of its elements is null.
     */
    @Contract(pure = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final long @NonNull [] @NonNull [] array) throws NullPointerException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        for (final long[] longs : array)
            result += longs.length;

        return result;
    }

    /**
     * Computes the total length of all float arrays contained in the given two-dimensional float array.
     *
     * @param array a non-null two-dimensional array of non-null float arrays.
     * @return the total length of all float arrays contained in the two-dimensional array.
     * @throws NullPointerException if the provided array or any of its elements is {@code null}.
     */
    @Contract(pure = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final float @NonNull [] @NonNull [] array) throws NullPointerException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        for (final float[] floats : array)
            result += floats.length;

        return result;
    }

    /**
     * Computes the total length of all double arrays contained in the given two-dimensional double array.
     *
     * @param array a non-null two-dimensional array of non-null double arrays.
     * @return the total length of all double arrays contained in the two-dimensional array.
     * @throws NullPointerException if the provided array or any of its elements is {@code null}.
     */
    @Contract(pure = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final double @NonNull [] @NonNull [] array) throws NullPointerException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        for (final double[] doubles : array)
            result += doubles.length;

        return result;
    }

    /**
     * Creates an array of a specified data type and length.
     *
     * @param dataType the data type of the elements in the array; must not be {@code null}.
     * @param len      the length of the array; must be a non-negative integer.
     * @return a new array of the specified data type and length.
     * @throws NullPointerException      if the data type is {@code null}.
     * @throws IndexOutOfBoundsException if the length is negative.
     */
    @Contract(pure = true)
    public static @NonNull Object @NonNull [] createArray(final @NonNull DataType dataType, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(dataType, "Data type cannot be null.");
        enforceGreaterOrEqual(len, 0, "Length cannot be negative.");
        return switch (dataType.getJavaType()) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> new byte[len][];
            case SHORT, USHORT -> new short[len][];
            case INT, UINT -> new int[len][];
            case LONG, ULONG -> new long[len][];
            case FLOAT -> new float[len][];
            case DOUBLE -> new double[len][];
            //default -> null;
        };
    }

    /**
     * Compares two 2D byte arrays for equality.
     * Returns {@code true} if both arrays are non-null and contain the same
     * bytes in the same order. Returns {@code false} if either an array is {@code null}
     * or if the arrays are not equal.
     *
     * @param array1 the first 2D byte array to compare, may be {@code null}.
     * @param array2 the second 2D byte array to compare, may be {@code null}.
     * @return {@code true} if the arrays are equal, {@code false} otherwise.
     */
    @Contract(pure = true)
    public static boolean arrayByteCompare(final byte @Nullable [] @Nullable [] array1, final byte @Nullable [] @Nullable [] array2) {
        if (array1 == null || array2 == null)
            return false;

        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++) {
            if (array1[i] == null || array2[i] == null)
                return false;
            if (!Arrays.equals(array1[i], array2[i]))
                return false;
        }

        return true;
    }

    /**
     * Compares two 2D short arrays for equality.
     * Returns {@code true} if both arrays are non-null and contain the same
     * bytes in the same order. Returns {@code false} if either an array is {@code null}
     * or if the arrays are not equal.
     *
     * @param array1 the first 2D short array to compare, may be {@code null}.
     * @param array2 the second 2D short array to compare, may be {@code null}.
     * @return {@code true} if the arrays are equal, {@code false} otherwise.
     */
    @Contract(pure = true)
    public static boolean arrayShortCompare(final short @Nullable [] @Nullable [] array1, final short @Nullable [] @Nullable [] array2) {
        if (array1 == null || array2 == null)
            return false;

        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++) {
            if (array1[i] == null || array2[i] == null)
                return false;
            if (!Arrays.equals(array1[i], array2[i]))
                return false;
        }

        return true;
    }

    /**
     * Compares two 2D int arrays for equality.
     * Returns {@code true} if both arrays are non-null and contain the same
     * bytes in the same order. Returns {@code false} if either an array is {@code null}
     * or if the arrays are not equal.
     *
     * @param array1 the first 2D int array to compare, may be {@code null}.
     * @param array2 the second 2D int array to compare, may be {@code null}.
     * @return {@code true} if the arrays are equal, {@code false} otherwise.
     */
    @Contract(pure = true)
    public static boolean arrayIntCompare(final int @Nullable [] @Nullable [] array1, final int @Nullable [] @Nullable [] array2) {
        if (array1 == null || array2 == null)
            return false;

        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++) {
            if (array1[i] == null || array2[i] == null)
                return false;
            if (!Arrays.equals(array1[i], array2[i]))
                return false;
        }

        return true;
    }

    /**
     * Compares two 2D long arrays for equality.
     * Returns {@code true} if both arrays are non-null and contain the same
     * bytes in the same order. Returns {@code false} if either an array is {@code null}
     * or if the arrays are not equal.
     *
     * @param array1 the first 2D long array to compare, may be {@code null}.
     * @param array2 the second 2D long array to compare, may be {@code null}.
     * @return {@code true} if the arrays are equal, {@code false} otherwise.
     */
    @Contract(pure = true)
    public static boolean arrayLongCompare(final long @Nullable [] @Nullable [] array1, final long @Nullable [] @Nullable [] array2) {
        if (array1 == null || array2 == null)
            return false;

        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++) {
            if (array1[i] == null || array2[i] == null)
                return false;
            if (!Arrays.equals(array1[i], array2[i]))
                return false;
        }

        return true;
    }

    /**
     * Compares two 2D float arrays for equality.
     * Returns {@code true} if both arrays are non-null and contain the same
     * bytes in the same order. Returns {@code false} if either an array is {@code null}
     * or if the arrays are not equal.
     *
     * @param array1 the first 2D float array to compare, may be {@code null}.
     * @param array2 the second 2D float array to compare, may be {@code null}.
     * @return {@code true} if the arrays are equal, {@code false} otherwise.
     */
    @Contract(pure = true)
    public static boolean arrayFloatCompare(final float @Nullable [] @Nullable [] array1, final float @Nullable [] @Nullable [] array2) {
        if (array1 == null || array2 == null)
            return false;

        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Arrays.equals(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * Compares two 2D double arrays for equality.
     * Returns {@code true} if both arrays are non-null and contain the same
     * bytes in the same order. Returns {@code false} if either an array is {@code null}
     * or if the arrays are not equal.
     *
     * @param array1 the first 2D double array to compare, may be {@code null}.
     * @param array2 the second 2D double array to compare, may be {@code null}.
     * @return {@code true} if the arrays are equal, {@code false} otherwise.
     */
    @Contract(pure = true)
    public static boolean arrayDoubleCompare(final double @Nullable [] @Nullable [] array1, final double @Nullable [] @Nullable [] array2) {
        if (array1 == null || array2 == null)
            return false;

        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++) {
            if (array1[i] == null || array2[i] == null)
                return false;
            if (!Arrays.equals(array1[i], array2[i]))
                return false;
        }

        return true;
    }

    /**
     * Converts a two-dimensional byte array into a one-dimensional byte array.
     * The method supports writing the resulting one-dimensional array to a pre-allocated
     * output array, starting at a specified offset.
     *
     * @param in     A non-null two-dimensional byte array (source data).
     * @param out    An optional pre-allocated byte array where the results will be written.
     * @param offset An integer value indicating the starting position in the output array
     *               where data from the input array should be written. Must be non-negative.
     * @return A one-dimensional byte array containing all elements from the input
     * two-dimensional array flattened into a single dimension.
     * @throws NullPointerException      If the input array or the output array is null.
     * @throws IndexOutOfBoundsException If the offset is invalid.
     */
    @Contract("_, _, _ -> param2")
    public static byte @NonNull [] toByteArray1D(final byte @NonNull [] @NonNull [] in, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");
        enforceGreaterOrEqual(out.length, offset + getTotalLength(in), "Offset is out of bounds, or the output array is too small.");

        int off = offset;
        for (final byte[] s_in : in) {
            System.arraycopy(s_in, 0, out, off, s_in.length);
            off += s_in.length;
        }

        return out;
    }

    /**
     * Converts a two-dimensional byte array into a one-dimensional byte array.
     *
     * @param array A non-null two-dimensional byte array (source data).
     * @return A one-dimensional byte array containing all elements from the input
     * two-dimensional array flattened into a single dimension.
     * @throws NullPointerException If the input data is {@code null}.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] toByteArray1D(final byte @NonNull [] @NonNull [] array) throws NullPointerException {
        return toByteArray1D(array, new byte[getTotalLength(array)], 0);
    }

    /**
     * Converts a two-dimensional short array into a one-dimensional short array.
     * The method supports writing the resulting one-dimensional array to a pre-allocated
     * output array, starting at a specified offset.
     *
     * @param in     A non-null two-dimensional short array (source data).
     * @param out    An optional pre-allocated short array where the results will be written.
     * @param offset An integer value indicating the starting position in the output array
     *               where data from the input array should be written. Must be non-negative.
     * @return A one-dimensional short array containing all elements from the input
     * two-dimensional array flattened into a single dimension.
     * @throws NullPointerException      If the input data is {@code null}.
     * @throws IndexOutOfBoundsException If the offset is invalid.
     */
    @Contract("_, _, _ -> param2")
    public static short @NonNull [] toShortArray1D(final short @NonNull [] @NonNull [] in, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input data cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");
        enforceGreaterOrEqual(out.length, offset, "Offset is out of bounds, or the output array is too small.");

        int off = offset;
        for (final short[] s_in : in) {
            System.arraycopy(s_in, 0, out, off, s_in.length);
            off += s_in.length;
        }

        return out;
    }

    /**
     * Converts a two-dimensional short array into a one-dimensional short array.
     *
     * @param array A non-null two-dimensional short array (source data).
     * @return A one-dimensional short array containing all elements from the input
     * two-dimensional array flattened into a single dimension.
     * @throws NullPointerException If the input data is {@code null}.
     */
    @Contract("_ -> new")
    public static short @NonNull [] toShortArray1D(final short @NonNull [] @NonNull [] array) throws NullPointerException {
        return toShortArray1D(array, new short[getTotalLength(array)], 0);
    }

    /**
     * Converts a two-dimensional int array into a one-dimensional int array.
     * The method supports writing the resulting one-dimensional array to a pre-allocated
     * output array, starting at a specified offset.
     *
     * @param in     A non-null two-dimensional int array (source data).
     * @param out    An optional pre-allocated int array where the results will be written.
     * @param offset An integer value indicating the starting position in the output array
     *               where data from the input array should be written. Must be non-negative.
     * @return A one-dimensional int array containing all elements from the input
     * two-dimensional array flattened into a single dimension.
     * @throws NullPointerException      If the input data is {@code null}.
     * @throws IndexOutOfBoundsException If the offset is invalid.
     */
    @Contract("_, _, _ -> param2")
    public static int @NonNull [] toIntArray1D(final int @NonNull [] @NonNull [] in, final int @Nullable [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input data cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");
        enforceGreaterOrEqual(out.length, offset + getTotalLength(in), "Offset is out of bounds or the output array is too small.");

        int off = offset;
        for (final int[] s_in : in) {
            System.arraycopy(s_in, 0, out, off, s_in.length);
            off += s_in.length;
        }

        return out;
    }

    /**
     * Converts a two-dimensional int array into a one-dimensional int array.
     *
     * @param array A non-null two-dimensional int array (source data).
     * @return A one-dimensional int array containing all elements from the input
     * two-dimensional array flattened into a single dimension.
     * @throws NullPointerException If the input data is {@code null}.
     */
    @Contract("_ -> new")
    public static int @NonNull [] toIntArray1D(final int @NonNull [] @NonNull [] array) throws NullPointerException {
        return toIntArray1D(array, new int[getTotalLength(array)], 0);
    }

    /**
     * Converts a two-dimensional long array into a one-dimensional long array.
     * The method supports writing the resulting one-dimensional array to a pre-allocated
     * output array, starting at a specified offset.
     *
     * @param in     A non-null two-dimensional long array (source data).
     * @param out    An optional pre-allocated long array where the results will be written.
     * @param offset An integer value indicating the starting position in the output array
     *               where data from the input array should be written. Must be non-negative.
     * @return A one-dimensional long array containing all elements from the input
     * two-dimensional array flattened into a single dimension.
     * @throws NullPointerException      If the input data is {@code null}.
     * @throws IndexOutOfBoundsException If the offset is invalid.
     */
    @Contract("_, _, _ -> param2")
    public static long @NonNull [] toLongArray1D(final long @NonNull [] @NonNull [] in, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input data cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");
        enforceGreaterOrEqual(out.length, offset + getTotalLength(in), "Offset is out of bounds or the output array is too small.");

        int off = offset;
        for (final long[] s_in : in) {
            System.arraycopy(s_in, 0, out, off, s_in.length);
            off += s_in.length;
        }

        return out;
    }

    /**
     * Converts a two-dimensional long array into a one-dimensional long array.
     *
     * @param array A non-null two-dimensional long array (source data).
     * @return A one-dimensional long array containing all elements from the input
     * two-dimensional array flattened into a single dimension.
     * @throws NullPointerException If the input data is {@code null}.
     */
    @Contract("_ -> new")
    public static long @NonNull [] toLongArray1D(final long @NonNull [] @NonNull [] array) throws NullPointerException {
        return toLongArray1D(array, new long[getTotalLength(array)], 0);
    }

    /**
     * Converts a two-dimensional float array into a one-dimensional float array.
     * The method supports writing the resulting one-dimensional array to a pre-allocated
     * output array, starting at a specified offset.
     *
     * @param in     A non-null two-dimensional float array (source data).
     * @param out    An optional pre-allocated float array where the results will be written.
     * @param offset An integer value indicating the starting position in the output array
     *               where data from the input array should be written. Must be non-negative.
     * @return A one-dimensional float array containing all elements from the input
     * two-dimensional array flattened into a single dimension.
     * @throws NullPointerException      If the input array is null.
     * @throws IndexOutOfBoundsException If the offset is invalid.
     */
    @Contract("_, _, _ -> param2")
    public static float @NonNull [] toFloatArray1D(final float @NonNull [] @NonNull [] in, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");
        enforceGreaterOrEqual(out.length, offset + getTotalLength(in), "Offset is out of bounds or the output array is too small.");

        int off = offset;
        for (final float[] s_in : in) {
            System.arraycopy(s_in, 0, out, off, s_in.length);
            off += s_in.length;
        }

        return out;
    }

    /**
     * Converts a two-dimensional float array into a one-dimensional float array.
     *
     * @param array A non-null two-dimensional float array (source data).
     * @return A one-dimensional float array containing all elements from the input
     * two-dimensional array flattened into a single dimension.
     * @throws NullPointerException If the input data is {@code null}.
     */
    @Contract("_ -> new")
    public static float @NonNull [] toFloatArray1D(final float @NonNull [] @NonNull [] array) throws NullPointerException {
        return toFloatArray1D(array, new float[getTotalLength(array)], 0);
    }

    /**
     * Converts a two-dimensional double array into a one-dimensional double array.
     * The method supports writing the resulting one-dimensional array to a pre-allocated
     * output array, starting at a specified offset.
     *
     * @param in     A non-null two-dimensional double array (source data).
     * @param out    An optional pre-allocated double array where the results will be written.
     * @param offset An integer value indicating the starting position in the output array
     *               where data from the input array should be written. Must be non-negative.
     * @return A one-dimensional double array containing all elements from the input
     * two-dimensional array flattened into a single dimension.
     * @throws NullPointerException      If the input data is {@code null}.
     * @throws IndexOutOfBoundsException If the offset is invalid.
     */
    @Contract("_, _, _ -> param2")
    public static double @NonNull [] toDoubleArray1D(final double @NonNull [] @NonNull [] in, final double @Nullable [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");
        enforceGreaterOrEqual(out.length, offset + getTotalLength(in), "Offset is out of bounds or the output array is too small.");

        int off = offset;
        for (final double[] s_in : in) {
            System.arraycopy(s_in, 0, out, off, s_in.length);
            off += s_in.length;
        }

        return out;
    }

    /**
     * Converts a two-dimensional double array into a one-dimensional double array.
     *
     * @param array A non-null two-dimensional double array (source data).
     * @return A one-dimensional double array containing all elements from the input
     * two-dimensional array flattened into a single dimension.
     * @throws NullPointerException If the input data is {@code null}.
     */
    @Contract("_ -> new")
    public static double @NonNull [] toDoubleArray1D(final double @NonNull [] @NonNull [] array) throws NullPointerException {
        return toDoubleArray1D(array, new double[getTotalLength(array)], 0);
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the input or output objects are not arrays.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are out of bounds for the input or output arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object arrayToArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT, USHORT -> shortArrayToArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT, UINT -> intArrayToArray((int[][]) in, inOffset, out, outOffset, length, signed);
            case LONG, ULONG -> longArrayToArray((long[][]) in, inOffset, out, outOffset, length, signed);
            case FLOAT -> floatArrayToArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToArray((double[][]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in     The input multidimensional array. Must not be {@code null}.
     * @param out    The output multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the input or output objects are not arrays.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object arrayToArray(final @NonNull Object in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToArray(in, 0, out, 0, Array.getLength(in), signed);
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the input or output objects are not arrays.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are out of bounds for the input or output arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] arrayToByteArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToByteArray((byte[][]) in, inOffset, out, outOffset, length);
            case SHORT, USHORT -> shortArrayToByteArray((short[][]) in, inOffset, out, outOffset, length);
            case INT, UINT -> intArrayToByteArray((int[][]) in, inOffset, out, outOffset, length);
            case LONG, ULONG -> longArrayToByteArray((long[][]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToByteArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToByteArray((double[][]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in  The input multidimensional array. Must not be {@code null}.
     * @param out The output multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the input or output objects are not arrays.
     */
    @Contract("_, _ -> param2")
    public static byte @NonNull [] @NonNull [] arrayToByteArray(final @NonNull Object in, final byte @NonNull [] @NonNull [] out) throws NullPointerException, IllegalArgumentException {
        return arrayToByteArray(in, 0, out, 0, Array.getLength(in));
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input array is {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] @NonNull [] arrayToByteArray(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToByteArray(array, 0, new byte[length][], 0, length);
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the input object is not an array.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] arrayToShortArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToShortArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT, USHORT -> shortArrayToShortArray((short[][]) in, inOffset, out, outOffset, length);
            case INT, UINT -> intArrayToShortArray((int[][]) in, inOffset, out, outOffset, length);
            case LONG, ULONG -> longArrayToShortArray((long[][]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToShortArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToShortArray((double[][]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in     The input multidimensional array. Must not be {@code null}.
     * @param out    The output multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static short @NonNull [] @NonNull [] arrayToShortArray(final @NonNull Object in, final short @NonNull [] @NonNull [] out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToShortArray(in, 0, out, 0, Array.getLength(in), signed);
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input array is {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] @NonNull [] arrayToShortArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToShortArray(array, 0, new short[length][], 0, length, signed);
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the input object is not an array.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] @NonNull [] arrayToIntArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToIntArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT, USHORT -> shortArrayToIntArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT, UINT -> intArrayToIntArray((int[][]) in, inOffset, out, outOffset, length);
            case LONG, ULONG -> longArrayToIntArray((long[][]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToIntArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToIntArray((double[][]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in     The input multidimensional array. Must not be {@code null}.
     * @param out    The output multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static int @NonNull [] @NonNull [] arrayToIntArray(final @NonNull Object in, final int @NonNull [] @NonNull [] out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToIntArray(in, 0, out, 0, Array.getLength(in), signed);
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] @NonNull [] arrayToIntArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToIntArray(array, 0, new int[length][], 0, length, signed);
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the input object is not an array.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] @NonNull [] arrayToLongArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToLongArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT, USHORT -> shortArrayToLongArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT, UINT -> intArrayToLongArray((int[][]) in, inOffset, out, outOffset, length, signed);
            case LONG, ULONG -> longArrayToLongArray((long[][]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToLongArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToLongArray((double[][]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in     The input multidimensional array. Must not be {@code null}.
     * @param out    The output multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static long @NonNull [] @NonNull [] arrayToLongArray(final @NonNull Object in, final long @NonNull [] @NonNull [] out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToLongArray(in, 0, out, 0, Array.getLength(in), signed);
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input array is {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    public static long @NonNull [] @NonNull [] arrayToLongArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToLongArray(array, 0, new long[length][], 0, length, signed);
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the input object is not an array.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static float @NonNull [] @NonNull [] arrayToFloatArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToFloatArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT, USHORT -> shortArrayToFloatArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT, UINT -> intArrayToFloatArray((int[][]) in, inOffset, out, outOffset, length, signed);
            case LONG, ULONG -> longArrayToFloatArray((long[][]) in, inOffset, out, outOffset, length, signed);
            case FLOAT -> floatArrayToFloatArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToFloatArray((double[][]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in     The input multidimensional array. Must not be {@code null}.
     * @param out    The output multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static float @NonNull [] @NonNull [] arrayToFloatArray(final @NonNull Object in, final float @NonNull [] @NonNull [] out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToFloatArray(in, 0, out, 0, Array.getLength(in), signed);
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input array is {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _ -> new")
    public static float @NonNull [] @NonNull [] arrayToFloatArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToFloatArray(array, 0, new float[length][], 0, length, signed);
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the input object is not an array.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static double @NonNull [] @NonNull [] arrayToDoubleArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToDoubleArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT, USHORT -> shortArrayToDoubleArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT, UINT -> intArrayToDoubleArray((int[][]) in, inOffset, out, outOffset, length, signed);
            case LONG, ULONG -> longArrayToDoubleArray((long[][]) in, inOffset, out, outOffset, length, signed);
            case FLOAT -> floatArrayToDoubleArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToDoubleArray((double[][]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in     The input multidimensional array. Must not be {@code null}.
     * @param out    The output multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static double @NonNull [] @NonNull [] arrayToDoubleArray(final @NonNull Object in, final double @NonNull [] @NonNull [] out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToDoubleArray(in, 0, out, 0, Array.getLength(in), signed);
    }

    /**
     * Transfers elements from an input multidimensional array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input array is {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _ -> new")
    public static double @NonNull [] @NonNull [] arrayToDoubleArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToDoubleArray(array, 0, new double[length][], 0, length, signed);
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the output object is not an array.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object byteArrayToArray(final byte @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT, USHORT -> byteArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length, signed);
            case INT, UINT -> byteArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);
            case LONG, ULONG -> byteArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);
            case FLOAT -> byteArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);
            case DOUBLE -> byteArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in     The input multidimensional array. Must not be {@code null}.
     * @param out    The output multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the output object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object byteArrayToArray(final byte @NonNull [] @NonNull [] in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return byteArrayToArray(in, 0, out, 0, in.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] byteArrayToByteArray(final byte @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.byteArrayToByteArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] @NonNull [] byteArrayToByteArray(final byte @NonNull [] @NonNull [] array) throws NullPointerException {
        return byteArrayToByteArray(array, 0, new byte[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] byteArrayToShortArray(final byte @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.byteArrayToShortArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] @NonNull [] byteArrayToShortArray(final byte @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return byteArrayToShortArray(array, 0, new short[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. If set to -1, the maximum possible elements are transferred.
     *                  Must be -1 or non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] @NonNull [] byteArrayToIntArray(final byte @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.byteArrayToIntArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] @NonNull [] byteArrayToIntArray(final byte @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return byteArrayToIntArray(array, 0, new int[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input array is {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] @NonNull [] byteArrayToLongArray(final byte @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.byteArrayToLongArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] @NonNull [] byteArrayToLongArray(final byte @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return byteArrayToLongArray(array, 0, new long[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. If set to -1, the maximum possible elements are transferred.
     *                  Must be -1 or non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static float @NonNull [] @NonNull [] byteArrayToFloatArray(final byte @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.byteArrayToFloatArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }


    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static float @NonNull [] @NonNull [] byteArrayToFloatArray(final byte @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return byteArrayToFloatArray(array, 0, new float[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input array is {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static double @NonNull [] @NonNull [] byteArrayToDoubleArray(final byte @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.byteArrayToDoubleArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional byte array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static double @NonNull [] @NonNull [] byteArrayToDoubleArray(final byte @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return byteArrayToDoubleArray(array, 0, new double[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     * @throws IllegalArgumentException  If the output object is not an array.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object shortArrayToArray(final short @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> shortArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT, USHORT -> shortArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT, UINT -> shortArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);
            case LONG, ULONG -> shortArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);
            case FLOAT -> shortArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);
            case DOUBLE -> shortArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in     The input multidimensional array. Must not be {@code null}.
     * @param out    The output multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input array is {@code null}.
     * @throws IllegalArgumentException If the output object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object shortArrayToArray(final short @NonNull [] @NonNull [] in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return shortArrayToArray(in, 0, out, 0, in.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. If set to -1, the maximum possible elements are transferred.
     *                  Must be -1 or non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] shortArrayToByteArray(final short @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.shortArrayToByteArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] @NonNull [] shortArrayToByteArray(final short @NonNull [] @NonNull [] array) throws NullPointerException {
        return shortArrayToByteArray(array, 0, new byte[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] shortArrayToShortArray(final short @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.shortArrayToShortArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static short @NonNull [] @NonNull [] shortArrayToShortArray(final short @NonNull [] @NonNull [] array) throws NullPointerException {
        return shortArrayToShortArray(array, 0, new short[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] @NonNull [] shortArrayToIntArray(final short @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.shortArrayToIntArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] @NonNull [] shortArrayToIntArray(final short @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return shortArrayToIntArray(array, 0, new int[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] @NonNull [] shortArrayToLongArray(final short @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.shortArrayToLongArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] @NonNull [] shortArrayToLongArray(final short @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return shortArrayToLongArray(array, 0, new long[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static float @NonNull [] @NonNull [] shortArrayToFloatArray(final short @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.shortArrayToFloatArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static float @NonNull [] @NonNull [] shortArrayToFloatArray(final short @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return shortArrayToFloatArray(array, 0, new float[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code  null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static double @NonNull [] @NonNull [] shortArrayToDoubleArray(final short @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.shortArrayToDoubleArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional short array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static double @NonNull [] @NonNull [] shortArrayToDoubleArray(final short @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return shortArrayToDoubleArray(array, 0, new double[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     * @throws IllegalArgumentException  If the output object is not an array.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object intArrayToArray(final int @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> intArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT, USHORT -> intArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT, UINT -> intArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);
            case LONG, ULONG -> intArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);
            case FLOAT -> intArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);
            case DOUBLE -> intArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in     The input multidimensional array. Must not be {@code null}.
     * @param out    The output multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the output object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object intArrayToArray(final int @NonNull [] @NonNull [] in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return intArrayToArray(in, 0, out, 0, in.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] intArrayToByteArray(final int @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.intArrayToByteArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] @NonNull [] intArrayToByteArray(final int @NonNull [] @NonNull [] array) throws NullPointerException {
        return intArrayToByteArray(array, 0, new byte[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] intArrayToShortArray(final int @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.intArrayToShortArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static short @NonNull [] @NonNull [] intArrayToShortArray(final int @NonNull [] @NonNull [] array) throws NullPointerException {
        return intArrayToShortArray(array, 0, new short[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static int @NonNull [] @NonNull [] intArrayToIntArray(final int @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.intArrayToIntArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static int @NonNull [] @NonNull [] intArrayToIntArray(final int @NonNull [] @NonNull [] array) throws NullPointerException {
        return intArrayToIntArray(array, 0, new int[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. If set to -1, the maximum possible elements are transferred.
     *                  Must be -1 or non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] @NonNull [] intArrayToLongArray(final int @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.intArrayToLongArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] @NonNull [] intArrayToLongArray(final int @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return intArrayToLongArray(array, 0, new long[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static float @NonNull [] @NonNull [] intArrayToFloatArray(final int @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.intArrayToFloatArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static float @NonNull [] @NonNull [] intArrayToFloatArray(final int @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return intArrayToFloatArray(array, 0, new float[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static double @NonNull [] @NonNull [] intArrayToDoubleArray(final int @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.intArrayToDoubleArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional int array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static double @NonNull [] @NonNull [] intArrayToDoubleArray(final int @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return intArrayToDoubleArray(array, 0, new double[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input array is {@code null}.
     * @throws IllegalArgumentException  If the output object is not an array.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object longArrayToArray(final long @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> longArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT, USHORT -> longArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT, UINT -> longArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);
            case LONG, ULONG -> longArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length);
            case FLOAT -> longArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);
            case DOUBLE -> longArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in     The input multidimensional array. Must not be {@code null}.
     * @param out    The output multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the output object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object longArrayToArray(final long @NonNull [] @NonNull [] in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return longArrayToArray(in, 0, out, 0, in.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] longArrayToByteArray(final long @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.longArrayToByteArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] @NonNull [] longArrayToByteArray(final long @NonNull [] @NonNull [] array) throws NullPointerException {
        return longArrayToByteArray(array, 0, new byte[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] longArrayToShortArray(final long @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.longArrayToShortArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static short @NonNull [] @NonNull [] longArrayToShortArray(final long @NonNull [] @NonNull [] array) throws NullPointerException {
        return longArrayToShortArray(array, 0, new short[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static int @NonNull [] @NonNull [] longArrayToIntArray(final long @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.longArrayToIntArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static int @NonNull [] @NonNull [] longArrayToIntArray(final long @NonNull [] @NonNull [] array) throws NullPointerException {
        return longArrayToIntArray(array, 0, new int[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static long @NonNull [] @NonNull [] longArrayToLongArray(final long @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.longArrayToLongArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static long @NonNull [] @NonNull [] longArrayToLongArray(final long @NonNull [] @NonNull [] array) throws NullPointerException {
        return longArrayToLongArray(array, 0, new long[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static float @NonNull [] @NonNull [] longArrayToFloatArray(final long @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.longArrayToFloatArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static float @NonNull [] @NonNull [] longArrayToFloatArray(final long @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return longArrayToFloatArray(array, 0, new float[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @param signed    Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static double @NonNull [] @NonNull [] longArrayToDoubleArray(final long @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.longArrayToDoubleArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional long array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed Whether the transfer should account for signedness in the data type conversion.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static double @NonNull [] @NonNull [] longArrayToDoubleArray(final long @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return longArrayToDoubleArray(array, 0, new double[array.length][], 0, array.length, signed);
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the output object is not an array.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static @NonNull Object floatArrayToArray(final float @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> floatArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT, USHORT -> floatArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT, UINT -> floatArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);
            case LONG, ULONG -> floatArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length);
            case FLOAT -> floatArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);
            case DOUBLE -> floatArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in  The input multidimensional array. Must not be {@code null}.
     * @param out The output multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the output object is not an array.
     */
    @Contract("_, _ -> param2")
    public static @NonNull Object floatArrayToArray(final float @NonNull [] @NonNull [] in, final @NonNull Object out) throws NullPointerException, IllegalArgumentException {
        return floatArrayToArray(in, 0, out, 0, in.length);
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] floatArrayToByteArray(final float @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.floatArrayToByteArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] @NonNull [] floatArrayToByteArray(final float @NonNull [] @NonNull [] array) throws NullPointerException {
        return floatArrayToByteArray(array, 0, new byte[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] floatArrayToShortArray(final float @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.floatArrayToShortArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static short @NonNull [] @NonNull [] floatArrayToShortArray(final float @NonNull [] @NonNull [] array) throws NullPointerException {
        return floatArrayToShortArray(array, 0, new short[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static int @NonNull [] @NonNull [] floatArrayToIntArray(final float @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.floatArrayToIntArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static int @NonNull [] @NonNull [] floatArrayToIntArray(final float @NonNull [] @NonNull [] array) throws NullPointerException {
        return floatArrayToIntArray(array, 0, new int[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static long @NonNull [] @NonNull [] floatArrayToLongArray(final float @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.floatArrayToLongArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static long @NonNull [] @NonNull [] floatArrayToLongArray(final float @NonNull [] @NonNull [] array) throws NullPointerException {
        return floatArrayToLongArray(array, 0, new long[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static float @NonNull [] @NonNull [] floatArrayToFloatArray(final float @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.floatArrayToFloatArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static float @NonNull [] @NonNull [] floatArrayToFloatArray(final float @NonNull [] @NonNull [] array) throws NullPointerException {
        return floatArrayToFloatArray(array, 0, new float[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are invalid.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static double @NonNull [] @NonNull [] floatArrayToDoubleArray(final float @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.floatArrayToDoubleArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional float array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static double @NonNull [] @NonNull [] floatArrayToDoubleArray(final float @NonNull [] @NonNull [] array) throws NullPointerException {
        return floatArrayToDoubleArray(array, 0, new double[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the output object is not an array.
     * @throws IndexOutOfBoundsException If the offsets or length parameters are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static @NonNull Object doubleArrayToArray(final double @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> doubleArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT, USHORT -> doubleArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT, UINT -> doubleArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);
            case LONG, ULONG -> doubleArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length);
            case FLOAT -> doubleArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);
            case DOUBLE -> doubleArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in  The input multidimensional array. Must not be {@code null}.
     * @param out The output multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the output object is not an array.
     */
    @Contract("_, _ -> param2")
    public static @NonNull Object doubleArrayToArray(final double @NonNull [] @NonNull [] in, final @NonNull Object out) throws NullPointerException, IllegalArgumentException {
        return doubleArrayToArray(in, 0, out, 0, in.length);
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] doubleArrayToByteArray(final double @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.doubleArrayToByteArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional byte array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] @NonNull [] doubleArrayToByteArray(final double @NonNull [] @NonNull [] array) throws NullPointerException {
        return doubleArrayToByteArray(array, 0, new byte[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] doubleArrayToShortArray(final double @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.doubleArrayToShortArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional short array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static short @NonNull [] @NonNull [] doubleArrayToShortArray(final double @NonNull [] @NonNull [] array) throws NullPointerException {
        return doubleArrayToShortArray(array, 0, new short[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static int @NonNull [] @NonNull [] doubleArrayToIntArray(final double @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.doubleArrayToIntArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional int array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static int @NonNull [] @NonNull [] doubleArrayToIntArray(final double @NonNull [] @NonNull [] array) throws NullPointerException {
        return doubleArrayToIntArray(array, 0, new int[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static long @NonNull [] @NonNull [] doubleArrayToLongArray(final double @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.doubleArrayToLongArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional long array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static long @NonNull [] @NonNull [] doubleArrayToLongArray(final double @NonNull [] @NonNull [] array) throws NullPointerException {
        return doubleArrayToLongArray(array, 0, new long[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static float @NonNull [] @NonNull [] doubleArrayToFloatArray(final double @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.doubleArrayToFloatArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional float array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static float @NonNull [] @NonNull [] doubleArrayToFloatArray(final double @NonNull [] @NonNull [] array) throws NullPointerException {
        return doubleArrayToFloatArray(array, 0, new float[array.length][], 0, array.length);
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array from which elements are read. Must be non-negative.
     * @param out       The output multidimensional array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where elements are written. Must be non-negative.
     * @param length    The number of elements to transfer. Must be non-negative.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offsets are out of bounds.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static double @NonNull [] @NonNull [] doubleArrayToDoubleArray(final double @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.doubleArrayToDoubleArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length);

        return out;
    }

    /**
     * Transfers elements from an input multidimensional double array to an output multidimensional double array.
     * The operation may involve type conversion and signedness handling based on the provided data type.
     *
     * @param array The input multidimensional array. Must not be {@code null}.
     * @return The updated output array after elements have been transferred.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static double @NonNull [] @NonNull [] doubleArrayToDoubleArray(final double @NonNull [] @NonNull [] array) throws NullPointerException {
        return doubleArrayToDoubleArray(array, 0, new double[array.length][], 0, array.length);
    }

    /**
     * Converts a multidimensional array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param in        The input array, which may be of type byte[][], short[][], int[][], long[][], float[][], or double[][].
     *                  Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the input object is not an array.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] arrayToSafeByteArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToSafeByteArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case SHORT, USHORT -> shortArrayToSafeByteArray((short[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case INT, UINT -> intArrayToSafeByteArray((int[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG, ULONG -> longArrayToSafeByteArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeByteArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeByteArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
            //default -> out;
        };
    }

    /**
     * Converts a multidimensional array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param in        The input array, which may be of type byte[][], short[][], int[][], long[][], float[][], or double[][].
     *                  Must not be {@code null}.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static byte @NonNull [] @NonNull [] arrayToSafeByteArray(final @NonNull Object in, final byte @NonNull [] @NonNull [] out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return arrayToSafeByteArray(in, 0, out, 0, Array.getLength(in), srcSigned, dstSigned);
    }

    /**
     * Converts a multidimensional array of various numerical data types into a safe two-dimensional short array.
     *
     * @param in        The input array, which may be of type byte[][], short[][], int[][], long[][], float[][], or double[][].
     *                  Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional short array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the input object is not an array.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] arrayToSafeShortArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToShortArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT, USHORT -> shortArrayToSafeShortArray((short[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case INT, UINT -> intArrayToSafeShortArray((int[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG, ULONG -> longArrayToSafeShortArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeShortArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeShortArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
            //default -> out;
        };
    }

    /**
     * Converts a multidimensional array of various numerical data types into a safe two-dimensional short array.
     *
     * @param in        The input array, which may be of type byte[][], short[][], int[][], long[][], float[][], or double[][].
     *                  Must not be {@code null}.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional short array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static short @NonNull [] @NonNull [] arrayToSafeShortArray(final @NonNull Object in, final short @NonNull [] @NonNull [] out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return arrayToSafeShortArray(in, 0, out, 0, Array.getLength(in), srcSigned, dstSigned);
    }

    /**
     * Converts a multidimensional array of various numerical data types into a safe two-dimensional int array.
     *
     * @param in        The input array, which may be of type byte[][], short[][], int[][], long[][], float[][], or double[][].
     *                  Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional int array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the input object is not an array.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3 ")
    public static int @NonNull [] @NonNull [] arrayToSafeIntArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToIntArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT, USHORT -> shortArrayToIntArray((short[][]) in, inOffset, out, outOffset, length, srcSigned);
            case INT, UINT -> intArrayToSafeIntArray((int[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG, ULONG -> longArrayToSafeIntArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeIntArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeIntArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
            //default -> out;
        };
    }

    /**
     * Converts a multidimensional array of various numerical data types into a safe two-dimensional int array.
     *
     * @param in        The input array, which may be of type byte[][], short[][], int[][], long[][], float[][], or double[][].
     *                  Must not be {@code null}.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional int array representing the converted data. Will never be null.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static int @NonNull [] @NonNull [] arrayToSafeIntArray(final @NonNull Object in, final int @NonNull [] @NonNull [] out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return arrayToSafeIntArray(in, 0, out, 0, Array.getLength(in), srcSigned, dstSigned);
    }

    /**
     * Converts a multidimensional array of various numerical data types into a safe two-dimensional long array.
     *
     * @param in        The input array, which may be of type byte[][], short[][], int[][], long[][], float[][], or double[][].
     *                  Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional long array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the input object is not an array.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static long @NonNull [] @NonNull [] arrayToSafeLongArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToLongArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT, USHORT -> shortArrayToLongArray((short[][]) in, inOffset, out, outOffset, length, srcSigned);
            case INT, UINT -> intArrayToLongArray((int[][]) in, inOffset, out, outOffset, length, srcSigned);
            case LONG, ULONG -> longArrayToSafeLongArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeLongArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeLongArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
            //default -> out;
        };
    }

    /**
     * Converts a multidimensional array of various numerical data types into a safe two-dimensional long array.
     *
     * @param in        The input array, which may be of type byte[][], short[][], int[][], long[][], float[][], or double[][].
     *                  Must not be {@code null}.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional long array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the input object is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static long @NonNull [] @NonNull [] arrayToSafeLongArray(final @NonNull Object in, final long @NonNull [] @NonNull [] out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return arrayToSafeLongArray(in, 0, out, 0, Array.getLength(in), srcSigned, dstSigned);
    }

    /**
     * Converts a multidimensional byte array of various numerical data types into a safe two-dimensional array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the out object is not an array.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static @NonNull Object byteArrayToSafeArray(final byte @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT, USHORT -> byteArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length, srcSigned);
            case INT, UINT -> byteArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, srcSigned);
            case LONG, ULONG -> byteArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, srcSigned);
            case FLOAT -> byteArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, srcSigned);
            case DOUBLE -> byteArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, srcSigned);
            //default -> out;
        };
    }

    /**
     * Converts a multidimensional byte array of various numerical data types into a safe two-dimensional array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the output object is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static @NonNull Object byteArrayToSafeArray(final byte @NonNull [] @NonNull [] in, final @NonNull Object out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return byteArrayToSafeArray(in, 0, out, 0, in.length, srcSigned, dstSigned);
    }

    /**
     * Converts a multidimensional byte array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] byteArrayToSafeByteArray(final byte @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.byteArrayToSafeByteArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, srcSigned, dstSigned);

        return out;
    }

    /**
     * Converts a multidimensional byte array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the provided input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static byte @NonNull [] @NonNull [] byteArrayToSafeByteArray(final byte @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return byteArrayToSafeByteArray(array, 0, new byte[array.length][], 0, array.length, signed, signed);
    }

    /**
     * Converts a multidimensional short array of various numerical data types into a safe two-dimensional array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the output object is not an array.
     * @throws IndexOutOfBoundsException If the provided offsets or length are out of bounds.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static @NonNull Object shortArrayToSafeArray(final short @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> shortArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT -> shortArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, srcSigned, dstSigned);
            case INT -> shortArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, srcSigned);
            case LONG -> shortArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, srcSigned);
            case FLOAT -> shortArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, srcSigned);
            case DOUBLE -> shortArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, srcSigned);
            default -> out;
        };
    }

    /**
     * Converts a multidimensional short array of various numerical data types into a safe two-dimensional array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the output object is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static @NonNull Object shortArrayToSafeArray(final short @NonNull [] @NonNull [] in, final @NonNull Object out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return shortArrayToSafeArray(in, 0, out, 0, in.length, srcSigned, dstSigned);
    }

    /**
     * Converts a multidimensional short array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] shortArrayToSafeByteArray(final short @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.shortArrayToSafeByteArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, srcSigned, dstSigned);

        return out;
    }

    /**
     * Converts a multidimensional short array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static byte @NonNull [] @NonNull [] shortArrayToSafeByteArray(final short @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return shortArrayToSafeByteArray(array, 0, new byte[array.length][], 0, array.length, signed, signed);
    }

    /**
     * Converts a multidimensional short array of various numerical data types into a safe two-dimensional short array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional short array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] shortArrayToSafeShortArray(final short @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.shortArrayToSafeShortArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, srcSigned, dstSigned);

        return out;
    }

    /**
     * Converts a multidimensional short array of various numerical data types into a safe two-dimensional short array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional short array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] @NonNull [] shortArrayToSafeShortArray(final short @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return shortArrayToSafeShortArray(array, 0, new short[array.length][], 0, array.length, signed, signed);
    }

    /**
     * Converts a multidimensional int array of various numerical data types into a safe two-dimensional array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the output object is not an array.
     * @throws IndexOutOfBoundsException If the provided offsets or length are out of bounds.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static @NonNull Object intArrayToSafeArray(final int @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> intArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT -> intArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, srcSigned, dstSigned);
            case INT -> intArrayToSafeIntArray(in, inOffset, (int[][]) out, outOffset, length, srcSigned, dstSigned);
            case LONG -> intArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, srcSigned);
            case FLOAT -> intArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, srcSigned);
            case DOUBLE -> intArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, srcSigned);
            default -> out;
        };
    }

    /**
     * Converts a multidimensional int array of various numerical data types into a safe two-dimensional array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the output object is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static @NonNull Object intArrayToSafeArray(final int @NonNull [] @NonNull [] in, final @NonNull Object out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return intArrayToSafeArray(in, 0, out, 0, in.length, srcSigned, dstSigned);
    }

    /**
     * Converts a multidimensional int array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Can be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] intArrayToSafeByteArray(final int @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.intArrayToSafeByteArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, srcSigned, dstSigned);

        return out;
    }

    /**
     * Converts a multidimensional int array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static byte @NonNull [] @NonNull [] intArrayToSafeByteArray(final int @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return intArrayToSafeByteArray(array, 0, new byte[array.length][], 0, array.length, signed, signed);
    }

    /**
     * Converts a multidimensional int array of various numerical data types into a safe two-dimensional short array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional short array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] intArrayToSafeShortArray(final int @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.intArrayToSafeShortArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, srcSigned, dstSigned);

        return out;
    }

    /**
     * Converts a multidimensional int array of various numerical data types into a safe two-dimensional short array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional short array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] @NonNull [] intArrayToSafeShortArray(final int @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return intArrayToSafeShortArray(array, 0, new short[array.length][], 0, array.length, signed, signed);
    }

    /**
     * Converts a multidimensional int array of various numerical data types into a safe two-dimensional int array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional int array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static int @NonNull [] @NonNull [] intArrayToSafeIntArray(final int @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.intArrayToSafeIntArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, srcSigned, dstSigned);

        return out;
    }

    /**
     * Converts a multidimensional int array of various numerical data types into a safe two-dimensional int array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional int array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] @NonNull [] intArrayToSafeIntArray(final int @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return intArrayToSafeIntArray(array, 0, new int[array.length][], 0, array.length, signed, signed);
    }

    /**
     * Converts a multidimensional long array of various numerical data types into a safe two-dimensional array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the output object is not an array.
     * @throws IndexOutOfBoundsException If the provided offsets or length are out of bounds.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static @NonNull Object longArrayToSafeArray(final long @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> longArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT -> longArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, srcSigned, dstSigned);
            case INT -> longArrayToSafeIntArray(in, inOffset, (int[][]) out, outOffset, length, srcSigned, dstSigned);
            case LONG -> longArrayToSafeLongArray(in, inOffset, (long[][]) out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> longArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, srcSigned);
            case DOUBLE -> longArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, srcSigned);
            default -> out;
        };
    }

    /**
     * Converts a multidimensional long array of various numerical data types into a safe two-dimensional array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the output object is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static @NonNull Object longArrayToSafeArray(final long @NonNull [] @NonNull [] in, final @NonNull Object out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return longArrayToSafeArray(in, 0, out, 0, in.length, srcSigned, dstSigned);
    }

    /**
     * Converts a multidimensional long array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Can be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] longArrayToSafeByteArray(final long @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.longArrayToSafeByteArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, srcSigned, dstSigned);

        return out;
    }

    /**
     * Converts a multidimensional long array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static byte @NonNull [] @NonNull [] longArrayToSafeByteArray(final long @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return longArrayToSafeByteArray(array, 0, new byte[array.length][], 0, array.length, signed, signed);
    }

    /**
     * Converts a multidimensional long array of various numerical data types into a safe two-dimensional short array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional short array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] longArrayToSafeShortArray(final long @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.longArrayToSafeShortArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, srcSigned, dstSigned);

        return out;
    }

    /**
     * Converts a multidimensional long array of various numerical data types into a safe two-dimensional short array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional short array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] @NonNull [] longArrayToSafeShortArray(final long @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return longArrayToSafeShortArray(array, 0, new short[array.length][], 0, array.length, signed, signed);
    }

    /**
     * Converts a multidimensional long array of various numerical data types into a safe two-dimensional int array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional int array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static int @NonNull [] @NonNull [] longArrayToSafeIntArray(final long @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.longArrayToSafeIntArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, srcSigned, dstSigned);

        return out;
    }

    /**
     * Converts a multidimensional long array of various numerical data types into a safe two-dimensional int array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional int array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] @NonNull [] longArrayToSafeIntArray(final long @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return longArrayToSafeIntArray(array, 0, new int[array.length][], 0, array.length, signed, signed);
    }

    /**
     * Converts a multidimensional long array of various numerical data types into a safe two-dimensional long array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param srcSigned A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @param dstSigned A flag indicating whether the resulting byte array should represent data as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional long array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static long @NonNull [] @NonNull [] longArrayToSafeLongArray(final long @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.longArrayToSafeLongArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, srcSigned, dstSigned);

        return out;
    }

    /**
     * Converts a multidimensional long array of various numerical data types into a safe two-dimensional long array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional long array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] @NonNull [] longArrayToSafeLongArray(final long @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return longArrayToSafeLongArray(array, 0, new long[array.length][], 0, array.length, signed, signed);
    }

    /**
     * Converts a multidimensional float array of various numerical data types into a safe two-dimensional array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param signed    A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the output object is not an array.
     * @throws IndexOutOfBoundsException If the provided offsets or length are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object floatArrayToSafeArray(final float @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> floatArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, signed);
            case SHORT -> floatArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, signed);
            case INT -> floatArrayToSafeIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);
            case LONG -> floatArrayToSafeLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);
            case FLOAT -> floatArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);
            case DOUBLE -> floatArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length);
            default -> out;
        };
    }

    /**
     * Converts a multidimensional float array of various numerical data types into a safe two-dimensional array.
     *
     * @param in     The input multidimensional array. Must not be {@code null}.
     * @param out    The output byte array to be written to. Must not be {@code null}.
     * @param signed A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the output object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object floatArrayToSafeArray(final float @NonNull [] @NonNull [] in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return floatArrayToSafeArray(in, 0, out, 0, in.length, signed);
    }

    /**
     * Converts a multidimensional float array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param signed    A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] floatArrayToSafeByteArray(final float @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.floatArrayToSafeByteArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Converts a multidimensional float array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static byte @NonNull [] @NonNull [] floatArrayToSafeByteArray(final float @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return floatArrayToSafeByteArray(array, 0, new byte[array.length][], 0, array.length, signed);
    }

    /**
     * Converts a multidimensional float array of various numerical data types into a safe two-dimensional short array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param signed    A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional short array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] floatArrayToSafeShortArray(final float @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.floatArrayToSafeShortArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Converts a multidimensional float array of various numerical data types into a safe two-dimensional short array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional short array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] @NonNull [] floatArrayToSafeShortArray(final float @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return floatArrayToSafeShortArray(array, 0, new short[array.length][], 0, array.length, signed);
    }

    /**
     * Converts a multidimensional float array of various numerical data types into a safe two-dimensional int array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param signed    A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional int array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] @NonNull [] floatArrayToSafeIntArray(final float @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.floatArrayToSafeIntArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Converts a multidimensional float array of various numerical data types into a safe two-dimensional int array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional int array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] @NonNull [] floatArrayToSafeIntArray(final float @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return floatArrayToSafeIntArray(array, 0, new int[array.length][], 0, array.length, signed);
    }

    /**
     * Converts a multidimensional float array of various numerical data types into a safe two-dimensional long array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param signed    A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional long array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] @NonNull [] floatArrayToSafeLongArray(final float @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.floatArrayToSafeLongArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Converts a multidimensional float array of various numerical data types into a safe two-dimensional long array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional long array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] @NonNull [] floatArrayToSafeLongArray(final float @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return floatArrayToSafeLongArray(array, 0, new long[array.length][], 0, array.length, signed);
    }

    /**
     * Converts a multidimensional double array of various numerical data types into a safe two-dimensional array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param signed    A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException  If the output object is not an array.
     * @throws IndexOutOfBoundsException If the provided offsets or length are out of bounds.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object doubleArrayToSafeArray(final double @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> doubleArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, signed);
            case SHORT -> doubleArrayToSafeShortArray(in, inOffset, (short[][]) out, outOffset, length, signed);
            case INT -> doubleArrayToSafeIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);
            case LONG -> doubleArrayToSafeLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);
            case FLOAT -> doubleArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);
            case DOUBLE -> doubleArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length);
            default -> out;
        };
    }

    /**
     * Converts a multidimensional double array of various numerical data types into a safe two-dimensional array.
     *
     * @param in     The input multidimensional array. Must not be {@code null}.
     * @param out    The output byte array to be written to. Must not be {@code null}.
     * @param signed A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException     If the input or output arrays are {@code null}.
     * @throws IllegalArgumentException If the output object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object doubleArrayToSafeArray(final double @NonNull [] @NonNull [] in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return doubleArrayToSafeArray(in, 0, out, 0, in.length, signed);
    }

    /**
     * Converts a multidimensional double array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param signed    A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] @NonNull [] doubleArrayToSafeByteArray(final double @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.doubleArrayToSafeByteArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Converts a multidimensional double array of various numerical data types into a safe two-dimensional byte array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional byte array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static byte @NonNull [] @NonNull [] doubleArrayToSafeByteArray(final double @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return doubleArrayToSafeByteArray(array, 0, new byte[array.length][], 0, array.length, signed);
    }

    /**
     * Converts a multidimensional double array of various numerical data types into a safe two-dimensional short array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param signed    A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional short array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static short @NonNull [] @NonNull [] doubleArrayToSafeShortArray(final double @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.doubleArrayToSafeShortArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Converts a multidimensional double array of various numerical data types into a safe two-dimensional short array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional short array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] @NonNull [] doubleArrayToSafeShortArray(final double @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return doubleArrayToSafeShortArray(array, 0, new short[array.length][], 0, array.length, signed);
    }

    /**
     * Converts a multidimensional double array of various numerical data types into a safe two-dimensional int array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param signed    A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional int array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] @NonNull [] doubleArrayToSafeIntArray(final double @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.doubleArrayToSafeIntArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Converts a multidimensional double array of various numerical data types into a safe two-dimensional int array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional int array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] @NonNull [] doubleArrayToSafeIntArray(final double @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return doubleArrayToSafeIntArray(array, 0, new int[array.length][], 0, array.length, signed);
    }

    /**
     * Converts a multidimensional double array of various numerical data types into a safe two-dimensional long array.
     *
     * @param in        The input multidimensional array. Must not be {@code null}.
     * @param inOffset  The starting offset within the input array. Must be a non-negative integer.
     * @param out       The output byte array to be written to. Must not be {@code null}.
     * @param outOffset The starting offset within the output array. Must be a non-negative integer.
     * @param length    The number of elements to be processed. Must be non-negative.
     * @param signed    A flag indicating whether the source data should be interpreted as signed ({@code true}) or unsigned ({@code false}).
     * @return A two-dimensional long array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException      If the input or output arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the length or offsets are invalid.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] @NonNull [] doubleArrayToSafeLongArray(final double @NonNull [] @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = Array1DUtil.doubleArrayToSafeLongArray(in[i + inOffset], 0, out[i + outOffset], 0, in[i + inOffset].length, signed);

        return out;
    }

    /**
     * Converts a multidimensional double array of various numerical data types into a safe two-dimensional long array.
     *
     * @param array  The input multidimensional array. Must not be {@code null}.
     * @param signed A flag indicating whether the source data and the output data should be interpreted as signed ({@code true})
     *               or unsigned ({@code false}).
     * @return A two-dimensional long array representing the converted data. Will never be {@code null}.
     * @throws NullPointerException If the input or output arrays are {@code null}.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] @NonNull [] doubleArrayToSafeLongArray(final double @NonNull [] @NonNull [] array, final boolean signed) throws NullPointerException {
        return doubleArrayToSafeLongArray(array, 0, new long[array.length][], 0, array.length, signed);
    }
}
