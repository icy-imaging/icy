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

import java.util.Objects;

import static fr.icy.common.EnforcerUtil.enforceGreaterOrEqual;

/**
 * Utility class for various operations on 3D arrays.
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class Array3DUtil {
    /**
     * Computes the total length of all byte arrays contained in the given three-dimensional byte array.
     *
     * @param array a non-null three-dimensional array of non-null byte arrays.
     * @return the total length of all byte arrays contained in the three-dimensional array.
     * @throws NullPointerException if the provided array or any of its elements is {@code null}.
     */
    @Contract(pure = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final byte @NonNull [] @NonNull [] @NonNull [] array) throws NullPointerException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        for (final byte[][] bytes : array)
            result += Array2DUtil.getTotalLength(bytes);

        return result;
    }

    /**
     * Computes the total length of all short arrays contained in the given three-dimensional short array.
     *
     * @param array a non-null three-dimensional array of non-null short arrays.
     * @return the total length of all short arrays contained in the three-dimensional array.
     * @throws NullPointerException if the provided array or any of its elements is {@code null}.
     */
    @Contract(pure = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final short @NonNull [] @NonNull [] @NonNull [] array) throws NullPointerException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        for (final short[][] shorts : array)
            result += Array2DUtil.getTotalLength(shorts);

        return result;
    }

    /**
     * Computes the total length of all int arrays contained in the given three-dimensional int array.
     *
     * @param array a non-null three-dimensional array of non-null int arrays.
     * @return the total length of all int arrays contained in the three-dimensional array.
     * @throws NullPointerException if the provided array or any of its elements is {@code null}.
     */
    @Contract(pure = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final int @NonNull [] @NonNull [] @NonNull [] array) throws NullPointerException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        for (final int[][] ints : array)
            result += Array2DUtil.getTotalLength(ints);

        return result;
    }

    /**
     * Computes the total length of all long arrays contained in the given three-dimensional long array.
     *
     * @param array a non-null three-dimensional array of non-null long arrays.
     * @return the total length of all long arrays contained in the three-dimensional array.
     * @throws NullPointerException if the provided array or any of its elements is {@code null}.
     */
    @Contract(pure = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final long @NonNull [] @NonNull [] @NonNull [] array) throws NullPointerException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        for (final long[][] longs : array)
            result += Array2DUtil.getTotalLength(longs);

        return result;
    }

    /**
     * Computes the total length of all float arrays contained in the given three-dimensional float array.
     *
     * @param array a non-null three-dimensional array of non-null float arrays.
     * @return the total length of all float arrays contained in the three-dimensional array.
     * @throws NullPointerException if the provided array or any of its elements is {@code null}.
     */
    @Contract(pure = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final float @NonNull [] @NonNull [] @NonNull [] array) throws NullPointerException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        for (final float[][] floats : array)
            result += Array2DUtil.getTotalLength(floats);

        return result;
    }

    /**
     * Computes the total length of all double arrays contained in the given three-dimensional double array.
     *
     * @param array a non-null three-dimensional array of non-null double arrays.
     * @return the total length of all double arrays contained in the three-dimensional array.
     * @throws NullPointerException if the provided array or any of its elements is {@code null}.
     */
    @Contract(pure = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final double @NonNull [] @NonNull [] @NonNull [] array) throws NullPointerException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        for (final double[][] doubles : array)
            result += Array2DUtil.getTotalLength(doubles);

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
    public static Object @Nullable [][] createArray(final @NonNull DataType dataType, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(dataType, "Data type cannot be null.");
        enforceGreaterOrEqual(len, 0, "Length cannot be negative.");

        return switch (dataType.getJavaType()) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> new byte[len][][];
            case SHORT, USHORT -> new short[len][][];
            //case INT, LONG -> new int[len][][]; // TODO: check if long array is supported
            case INT, UINT -> new int[len][][];
            case LONG, ULONG -> new long[len][][];
            case FLOAT -> new float[len][][];
            case DOUBLE -> new double[len][][];
            //default -> null;
        };
    }

    /**
     * Converts a three-dimensional byte array into a one-dimensional byte array.
     * The method supports writing the resulting one-dimensional array to a pre-allocated
     * output array, starting at a specified offset.
     *
     * @param in     A three-dimensional byte array. Must not be {@code null}.
     * @param out    A pre-allocated byte array where the results will be written. Must not be {@code null}.
     * @param offset An integer value indicating the starting position in the output array
     *               where data from the input array should be written. Must be non-negative.
     * @return A one-dimensional byte array containing all elements from the input
     * three-dimensional array flattened into a single dimension.
     * @throws NullPointerException      If the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offset is invalid.
     */
    @Contract("_, _, _ -> param2")
    public static byte @NonNull [] toByteArray1D(final byte @NonNull [] @NonNull [] @NonNull [] in, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");
        enforceGreaterOrEqual(out.length, offset + getTotalLength(in), "Offset is out of bounds, or the output array is too small.");

        int off = offset;
        for (final byte[][] s_in : in) {
            Array2DUtil.toByteArray1D(s_in, out, off);
            off += s_in.length;
        }

        return out;
    }

    /**
     * Converts a three-dimensional byte array into a one-dimensional byte array.
     *
     * @param array A three-dimensional byte array. Must not be {@code null}.
     * @return A one-dimensional byte array containing all elements from the input
     * three-dimensional array flattened into a single dimension.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] toByteArray1D(final byte @NonNull [] @NonNull [] @NonNull [] array) throws NullPointerException {
        return toByteArray1D(array, new byte[getTotalLength(array)], 0);
    }

    /**
     * Converts a three-dimensional short array into a one-dimensional short array.
     * The method supports writing the resulting one-dimensional array to a pre-allocated
     * output array, starting at a specified offset.
     *
     * @param in     A three-dimensional short array. Must not be {@code null}.
     * @param out    A pre-allocated short array where the results will be written. Must not be {@code null}.
     * @param offset An integer value indicating the starting position in the output array
     *               where data from the input array should be written. Must be non-negative.
     * @return A one-dimensional short array containing all elements from the input
     * three-dimensional array flattened into a single dimension.
     * @throws NullPointerException      If the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offset is invalid.
     */
    @Contract("_, _, _ -> param2")
    public static short @NonNull [] toShortArray1D(final short @NonNull [] @NonNull [] @NonNull [] in, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");
        enforceGreaterOrEqual(out.length, offset + getTotalLength(in), "Offset is out of bounds, or the output array is too small.");

        int off = offset;
        for (final short[][] s_in : in) {
            Array2DUtil.toShortArray1D(s_in, out, off);
            off += s_in.length;
        }

        return out;
    }

    /**
     * Converts a three-dimensional short array into a one-dimensional short array.
     *
     * @param array A three-dimensional short array. Must not be {@code null}.
     * @return A one-dimensional short array containing all elements from the input
     * three-dimensional array flattened into a single dimension.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static short @NonNull [] toShortArray1D(final short @NonNull [] @NonNull [] @NonNull [] array) throws NullPointerException {
        return toShortArray1D(array, new short[getTotalLength(array)], 0);
    }

    /**
     * Converts a three-dimensional int array into a one-dimensional int array.
     * The method supports writing the resulting one-dimensional array to a pre-allocated
     * output array, starting at a specified offset.
     *
     * @param in     A three-dimensional int array. Must not be {@code null}.
     * @param out    A pre-allocated int array where the results will be written. Must not be {@code null}.
     * @param offset An integer value indicating the starting position in the output array
     *               where data from the input array should be written. Must be non-negative.
     * @return A one-dimensional int array containing all elements from the input
     * three-dimensional array flattened into a single dimension.
     * @throws NullPointerException      If the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offset is invalid.
     */
    @Contract("_, _, _ -> param2")
    public static int @NonNull [] toIntArray1D(final int @NonNull [] @NonNull [] @NonNull [] in, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");
        enforceGreaterOrEqual(out.length, offset + getTotalLength(in), "Offset is out of bounds, or the output array is too small.");

        int off = offset;
        for (final int[][] s_in : in) {
            Array2DUtil.toIntArray1D(s_in, out, off);
            off += s_in.length;
        }

        return out;
    }

    /**
     * Converts a three-dimensional int array into a one-dimensional int array.
     *
     * @param array A three-dimensional int array. Must not be {@code null}.
     * @return A one-dimensional int array containing all elements from the input
     * three-dimensional array flattened into a single dimension.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static int @NonNull [] toIntArray1D(final int @NonNull [] @NonNull [] @NonNull [] array) throws NullPointerException {
        return toIntArray1D(array, new int[getTotalLength(array)], 0);
    }

    /**
     * Converts a three-dimensional long array into a one-dimensional long array.
     * The method supports writing the resulting one-dimensional array to a pre-allocated
     * output array, starting at a specified offset.
     *
     * @param in     A three-dimensional long array. Must not be {@code null}.
     * @param out    A pre-allocated long array where the results will be written. Must not be {@code null}.
     * @param offset An integer value indicating the starting position in the output array
     *               where data from the input array should be written. Must be non-negative.
     * @return A one-dimensional long array containing all elements from the input
     * three-dimensional array flattened into a single dimension.
     * @throws NullPointerException      If the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offset is invalid.
     */
    @Contract("_, _, _ -> param2")
    public static long @NonNull [] toLongArray1D(final long @NonNull [] @NonNull [] @NonNull [] in, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");
        enforceGreaterOrEqual(out.length, offset + getTotalLength(in), "Offset is out of bounds, or the output array is too small.");

        int off = offset;
        for (final long[][] s_in : in) {
            Array2DUtil.toLongArray1D(s_in, out, off);
            off += s_in.length;
        }

        return out;
    }

    /**
     * Converts a three-dimensional long array into a one-dimensional long array.
     *
     * @param array A three-dimensional long array. Must not be {@code null}.
     * @return A one-dimensional long array containing all elements from the input
     * three-dimensional array flattened into a single dimension.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static long @NonNull [] toLongArray1D(final long @NonNull [] @NonNull [] @NonNull [] array) throws NullPointerException {
        return toLongArray1D(array, new long[getTotalLength(array)], 0);
    }

    /**
     * Converts a three-dimensional float array into a one-dimensional float array.
     * The method supports writing the resulting one-dimensional array to a pre-allocated
     * output array, starting at a specified offset.
     *
     * @param in     A three-dimensional float array. Must not be {@code null}.
     * @param out    A pre-allocated float array where the results will be written. Must not be {@code null}.
     * @param offset An integer value indicating the starting position in the output array
     *               where data from the input array should be written. Must be non-negative.
     * @return A one-dimensional float array containing all elements from the input
     * three-dimensional array flattened into a single dimension.
     * @throws NullPointerException      If the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offset is invalid.
     */
    @Contract("_, _, _ -> param2")
    public static float @NonNull [] toFloatArray1D(final float @NonNull [] @NonNull [] @NonNull [] in, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");
        enforceGreaterOrEqual(out.length, offset + getTotalLength(in), "Offset is out of bounds, or the output array is too small.");

        int off = offset;
        for (final float[][] s_in : in) {
            Array2DUtil.toFloatArray1D(s_in, out, off);
            off += s_in.length;
        }

        return out;
    }

    /**
     * Converts a three-dimensional float array into a one-dimensional float array.
     *
     * @param array A three-dimensional float array. Must not be {@code null}.
     * @return A one-dimensional float array containing all elements from the input
     * three-dimensional array flattened into a single dimension.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static float @NonNull [] toFloatArray1D(final float @NonNull [] @NonNull [] @NonNull [] array) throws NullPointerException {
        return toFloatArray1D(array, new float[getTotalLength(array)], 0);
    }

    /**
     * Converts a three-dimensional double array into a one-dimensional double array.
     * The method supports writing the resulting one-dimensional array to a pre-allocated
     * output array, starting at a specified offset.
     *
     * @param in     A three-dimensional double array. Must not be {@code null}.
     * @param out    A pre-allocated double array where the results will be written. Must not be {@code null}.
     * @param offset An integer value indicating the starting position in the output array
     *               where data from the input array should be written. Must be non-negative.
     * @return A one-dimensional double array containing all elements from the input
     * three-dimensional array flattened into a single dimension.
     * @throws NullPointerException      If the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offset is invalid.
     */
    @Contract("_, _, _ -> param2")
    public static double @NonNull [] toDoubleArray1D(final double @NonNull [] @NonNull [] @NonNull [] in, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset cannot be negative.");
        enforceGreaterOrEqual(out.length, offset + getTotalLength(in), "Offset is out of bounds, or the output array is too small.");

        int off = offset;
        for (final double[][] s_in : in) {
            Array2DUtil.toDoubleArray1D(s_in, out, off);
            off += s_in.length;
        }

        return out;
    }

    /**
     * Converts a three-dimensional double array into a one-dimensional double array.
     *
     * @param array A three-dimensional double array. Must not be {@code null}.
     * @return A one-dimensional double array containing all elements from the input
     * three-dimensional array flattened into a single dimension.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static double @NonNull [] toDoubleArray1D(final double @NonNull [] @NonNull [] @NonNull [] array) throws NullPointerException {
        return toDoubleArray1D(array, new double[getTotalLength(array)], 0);
    }
}
