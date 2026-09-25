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

import fr.icy.common.math.MathUtil;
import fr.icy.common.string.StringUtil;
import fr.icy.common.type.DataType;
import fr.icy.common.type.TypeUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

import static fr.icy.common.EnforcerUtil.enforceGreaterOrEqual;
import static fr.icy.common.EnforcerUtil.enforceRange;

/**
 * Utility class for various operations on 1D arrays.
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class Array1DUtil {
    /**
     * Creates a new 1D array of the specified data type and length.
     *
     * @param dataType the data type of the array elements; must not be {@code null}.
     * @param len      the length of the array; must be greater than or equal to 0.
     * @return a newly created 1D array of the specified data type and length.
     * @throws NullPointerException      if the data type is {@code null}.
     * @throws IndexOutOfBoundsException if the length is negative.
     */
    public static @NonNull Object createArray(final @NonNull DataType dataType, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(dataType, "Datatype cannot be null.");
        enforceGreaterOrEqual(len, 0, "Length cannot be negative.");
        return switch (dataType.getJavaType()) {
            case BYTE, UBYTE -> new byte[len];
            case SHORT, USHORT -> new short[len];
            case INT, UINT -> new int[len];
            case LONG, ULONG -> new long[len];
            case FLOAT -> new float[len];
            case DOUBLE -> new double[len];
        };
    }

    /**
     * Allocates a new one-dimensional array of the specified data type and length if the input object is null.
     * Otherwise, returns the input object without modification.
     *
     * @param out      an existing object or null. If this parameter is null, a new array will be created.
     * @param dataType the data type of the array to create if out is {@code null}.
     * @param len      the length of the array to create if out is {@code null}. Must be a non-negative integer.
     * @return the original out object if it is not {@code null}, or a newly allocated array of the specified type and length if out is {@code null}.
     * @throws NullPointerException      if the datatype is {@code null}.
     * @throws IndexOutOfBoundsException if len is negative.
     * @deprecated Use {@link #createArray(DataType, int)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    @Contract(value = "!null, _, _ -> param1; null, _, _ -> !null", pure = true)
    public static @NonNull Object allocIfNull(final @Nullable Object out, final @NonNull DataType dataType, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IndexOutOfBoundsException {
        return Objects.requireNonNullElseGet(out, () -> createArray(dataType, len));
    }

    /**
     * Allocates and returns a new byte array of the specified length if the provided array is null.
     * If the provided array is not null, it simply returns the original array.
     *
     * @param out the byte array to check for null. If null, a new array of the specified length is allocated.
     * @param len the length of the new array to be allocated if out is null. Must be non-negative.
     * @return the original array if it is not null, otherwise a newly allocated array of the specified length.
     * @throws IndexOutOfBoundsException if the specified length is negative.
     * @deprecated Use {@code new byte[len]} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static byte @NonNull [] allocIfNull(final byte @Nullable [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws IndexOutOfBoundsException {
        if (out == null) {
            enforceGreaterOrEqual(len, 0, "Length cannot be negative.");
            return new byte[len];
        }

        return out;
    }

    /**
     * Allocates and returns a new short array of the specified length if the provided array is null.
     * If the provided array is not null, it simply returns the original array.
     *
     * @param out the short array to check for null. If null, a new array of the specified length is allocated.
     * @param len the length of the new array to be allocated if out is null. Must be non-negative.
     * @return the original array if it is not null, otherwise a newly allocated array of the specified length.
     * @throws IndexOutOfBoundsException if the specified length is negative.
     * @deprecated Use {@code new short[len]} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static short @NonNull [] allocIfNull(final short @Nullable [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws IndexOutOfBoundsException {
        if (out == null) {
            enforceGreaterOrEqual(len, 0, "Length cannot be negative.");
            return new short[len];
        }

        return out;
    }

    /**
     * Allocates and returns a new int array of the specified length if the provided array is null.
     * If the provided array is not null, it simply returns the original array.
     *
     * @param out the int array to check for null. If null, a new array of the specified length is allocated.
     * @param len the length of the new array to be allocated if out is null. Must be non-negative.
     * @return the original array if it is not null, otherwise a newly allocated array of the specified length.
     * @throws IndexOutOfBoundsException if the specified length is negative.
     * @deprecated Use {@code new int[len]} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static int @NonNull [] allocIfNull(final int @Nullable [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws IndexOutOfBoundsException {
        if (out == null) {
            enforceGreaterOrEqual(len, 0, "Length cannot be negative.");
            return new int[len];
        }

        return out;
    }

    /**
     * Allocates and returns a new long array of the specified length if the provided array is null.
     * If the provided array is not null, it simply returns the original array.
     *
     * @param out the long array to check for null. If null, a new array of the specified length is allocated.
     * @param len the length of the new array to be allocated if out is null. Must be non-negative.
     * @return the original array if it is not null, otherwise a newly allocated array of the specified length.
     * @throws IndexOutOfBoundsException if the specified length is negative.
     * @deprecated Use {@code new long[len]} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static long @NonNull [] allocIfNull(final long @Nullable [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws IndexOutOfBoundsException {
        if (out == null) {
            enforceGreaterOrEqual(len, 0, "Length cannot be negative.");
            return new long[len];
        }

        return out;
    }

    /**
     * Allocates and returns a new float array of the specified length if the provided array is null.
     * If the provided array is not null, it simply returns the original array.
     *
     * @param out the float array to check for null. If null, a new array of the specified length is allocated.
     * @param len the length of the new array to be allocated if out is null. Must be non-negative.
     * @return the original array if it is not null, otherwise a newly allocated array of the specified length.
     * @throws IndexOutOfBoundsException if the specified length is negative.
     * @deprecated Use {@code new float[len]} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static float @NonNull [] allocIfNull(final float @Nullable [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws IndexOutOfBoundsException {
        if (out == null) {
            enforceGreaterOrEqual(len, 0, "Length cannot be negative.");
            return new float[len];
        }

        return out;
    }

    /**
     * Allocates and returns a new double array of the specified length if the provided array is null.
     * If the provided array is not null, it simply returns the original array.
     *
     * @param out the double array to check for null. If null, a new array of the specified length is allocated.
     * @param len the length of the new array to be allocated if out is null. Must be non-negative.
     * @return the original array if it is not null, otherwise a newly allocated array of the specified length.
     * @throws IndexOutOfBoundsException if the specified length is negative.
     * @deprecated Use {@code new double[len]} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    @Contract(value = "null, _ -> new; !null, _ -> param1", pure = true)
    public static double @NonNull [] allocIfNull(final double @Nullable [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws IndexOutOfBoundsException {
        if (out == null) {
            enforceGreaterOrEqual(len, 0, "Length cannot be negative.");
            return new double[len];
        }

        return out;
    }

    /**
     * Creates a new array that is a copy of the specified array.
     *
     * @param array the input array to copy; must not be {@code null}.
     * @return a new array containing the same elements as the input array.
     * @throws NullPointerException     if the input array is {@code null}.
     * @throws IllegalArgumentException if the provided object is not an array.
     */
    public static @NonNull Object copyOf(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(array, "Array cannot be null.");
        final DataType type = ArrayUtil.getDataType(array);
        return switch (type) {
            case BYTE, UBYTE -> Arrays.copyOf((byte[]) array, ((byte[]) array).length);
            case SHORT, USHORT -> Arrays.copyOf((short[]) array, ((short[]) array).length);
            case INT, UINT -> Arrays.copyOf((int[]) array, ((int[]) array).length);
            case LONG, ULONG -> Arrays.copyOf((long[]) array, ((long[]) array).length);
            case FLOAT -> Arrays.copyOf((float[]) array, ((float[]) array).length);
            case DOUBLE -> Arrays.copyOf((double[]) array, ((double[]) array).length);
        };
    }

    /**
     * Retrieves a value from the specified array at the given offset and converts it to byte,
     * based on the provided data type.
     *
     * @param array    the source array from which the value is to be retrieved; must not be {@code null}.
     * @param offset   the zero-based index of the value within the array; must be within the valid range of the array indices.
     * @param dataType the data type of the array elements, which determines how the value is interpreted; must not be {@code null}.
     * @return the value at the specified offset in the array, converted to byte.
     * @throws NullPointerException      if the array or data type is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is outside the bounds of the array.
     */
    public static int getValueAsByte(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final @NonNull DataType dataType) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        final int length = Array.getLength(array);
        enforceRange(offset, 0, length - 1, "Offset must be between 0 and " + (length - 1) + ".");
        Objects.requireNonNull(dataType, "DataType cannot be null.");
        return switch (dataType) {
            case BYTE, UBYTE -> Array.getByte(array, offset);
            case SHORT, USHORT -> (byte) Array.getShort(array, offset);
            case INT, UINT -> (byte) Array.getInt(array, offset);
            case LONG, ULONG -> (byte) Array.getLong(array, offset);
            case FLOAT -> (byte) Array.getFloat(array, offset);
            case DOUBLE -> (byte) Array.getDouble(array, offset);
        };
    }

    /**
     * Retrieves the value at the specified offset from the given array and converts it to byte.
     *
     * @param array  the array to read the value from; must not be {@code null}.
     * @param offset the position within the array from which to retrieve the value; must be within the valid bounds of the array.
     * @param signed a flag indicating whether the value should be treated as signed or unsigned.
     * @return the value at the specified offset, converted to byte.
     * @throws NullPointerException      if the provided array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is outside the bounds of the array.
     */
    public static int getValueAsByte(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return getValueAsByte(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * Retrieves a value from the specified array at the given offset and converts it to short,
     * based on the provided data type.
     *
     * @param array    the source array from which the value is to be retrieved; must not be {@code null}.
     * @param offset   the zero-based index of the value within the array; must be within the valid range of the array indices.
     * @param dataType the data type of the array elements, which determines how the value is interpreted; must not be {@code null}.
     * @return the value at the specified offset in the array, converted to short.
     * @throws NullPointerException      if the array or data type is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is outside the bounds of the array.
     */
    public static int getValueAsShort(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final @NonNull DataType dataType) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        final int length = Array.getLength(array);
        enforceRange(offset, 0, length - 1, "Offset must be between 0 and " + (length - 1) + ".");
        Objects.requireNonNull(dataType, "DataType cannot be null.");
        return switch (dataType) {
            case BYTE -> TypeUtil.toShort(Array.getByte(array, offset), true);
            case UBYTE -> TypeUtil.toShort(Array.getByte(array, offset), false);
            case SHORT, USHORT -> Array.getShort(array, offset);
            case INT, UINT -> (short) Array.getInt(array, offset);
            case LONG, ULONG -> (short) Array.getLong(array, offset);
            case FLOAT -> (short) Array.getFloat(array, offset);
            case DOUBLE -> (short) Array.getDouble(array, offset);
        };
    }

    /**
     * Retrieves the value at the specified offset from the given array and converts it to short.
     *
     * @param array  the array to read the value from; must not be {@code null}.
     * @param offset the position within the array from which to retrieve the value; must be within the valid bounds of the array.
     * @param signed a flag indicating whether the value should be treated as signed or unsigned.
     * @return the value at the specified offset, converted to short.
     * @throws NullPointerException      if the provided array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is outside the bounds of the array.
     */
    public static int getValueAsShort(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return getValueAsShort(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * Retrieves a value from the specified array at the given offset and converts it to int,
     * based on the provided data type.
     *
     * @param array    the source array from which the value is to be retrieved; must not be {@code null}.
     * @param offset   the zero-based index of the value within the array; must be within the valid range of the array indices.
     * @param dataType the data type of the array elements, which determines how the value is interpreted; must not be {@code null}.
     * @return the value at the specified offset in the array, converted to int.
     * @throws NullPointerException      if the array or data type is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is outside the bounds of the array.
     */
    public static int getValueAsInt(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final @NonNull DataType dataType) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        final int length = Array.getLength(array);
        enforceRange(offset, 0, length - 1, "Offset must be between 0 and " + (length - 1) + ".");
        Objects.requireNonNull(dataType, "DataType cannot be null.");
        return switch (dataType) {
            case BYTE -> TypeUtil.toInt(Array.getByte(array, offset), true);
            case UBYTE -> TypeUtil.toInt(Array.getByte(array, offset), false);
            case SHORT -> TypeUtil.toInt(Array.getShort(array, offset), true);
            case USHORT -> TypeUtil.toInt(Array.getShort(array, offset), false);
            case INT, UINT -> Array.getInt(array, offset);
            case LONG, ULONG -> (int) Array.getLong(array, offset);
            case FLOAT -> (int) Array.getFloat(array, offset);
            case DOUBLE -> (int) Array.getDouble(array, offset);
        };
    }

    /**
     * Retrieves the value at the specified offset from the given array and converts it to int.
     *
     * @param array  the array to read the value from; must not be {@code null}.
     * @param offset the position within the array from which to retrieve the value; must be within the valid bounds of the array.
     * @param signed a flag indicating whether the value should be treated as signed or unsigned.
     * @return the value at the specified offset, converted to int.
     * @throws NullPointerException      if the provided array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is outside the bounds of the array.
     */
    public static int getValueAsInt(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return getValueAsInt(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * Retrieves a value from the specified array at the given offset and converts it to long,
     * based on the provided data type.
     *
     * @param array    the source array from which the value is to be retrieved; must not be {@code null}.
     * @param offset   the zero-based index of the value within the array; must be within the valid range of the array indices.
     * @param dataType the data type of the array elements, which determines how the value is interpreted; must not be {@code null}.
     * @return the value at the specified offset in the array, converted to long.
     * @throws NullPointerException      if the array or data type is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is outside the bounds of the array.
     */
    public static long getValueAsLong(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final @NonNull DataType dataType) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        final int length = Array.getLength(array);
        enforceRange(offset, 0, length - 1, "Offset must be between 0 and " + (length - 1) + ".");
        Objects.requireNonNull(dataType, "Datatype cannot be null");
        return switch (dataType) {
            case BYTE -> TypeUtil.toLong(Array.getByte(array, offset), true);
            case UBYTE -> TypeUtil.toLong(Array.getByte(array, offset), false);
            case SHORT -> TypeUtil.toLong(Array.getShort(array, offset), true);
            case USHORT -> TypeUtil.toLong(Array.getShort(array, offset), false);
            case INT -> TypeUtil.toLong(Array.getInt(array, offset), true);
            case UINT -> TypeUtil.toLong(Array.getInt(array, offset), false);
            case LONG, ULONG -> Array.getLong(array, offset);
            case FLOAT -> (long) Array.getFloat(array, offset);
            case DOUBLE -> (long) Array.getDouble(array, offset);
        };
    }

    /**
     * Retrieves the value at the specified offset from the given array and converts it to long.
     *
     * @param array  the array to read the value from; must not be {@code null}.
     * @param offset the position within the array from which to retrieve the value; must be within the valid bounds of the array.
     * @param signed a flag indicating whether the value should be treated as signed or unsigned.
     * @return the value at the specified offset, converted to long.
     * @throws NullPointerException      if the provided array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is outside the bounds of the array.
     */
    public static long getValueAsLong(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return getValueAsLong(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * Retrieves a value from the specified array at the given offset and converts it to float,
     * based on the provided data type.
     *
     * @param array    the source array from which the value is to be retrieved; must not be {@code null}.
     * @param offset   the zero-based index of the value within the array; must be within the valid range of the array indices.
     * @param dataType the data type of the array elements, which determines how the value is interpreted; must not be {@code null}.
     * @return the value at the specified offset in the array, converted to float.
     * @throws NullPointerException      if the array or data type is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is outside the bounds of the array.
     */
    public static float getValueAsFloat(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final @NonNull DataType dataType) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        final int length = Array.getLength(array);
        enforceRange(offset, 0, length - 1, "Offset must be between 0 and " + (length - 1) + ".");
        Objects.requireNonNull(dataType, "DataType cannot be null.");
        return switch (dataType) {
            case BYTE -> TypeUtil.toFloat(Array.getByte(array, offset), true);
            case UBYTE -> TypeUtil.toFloat(Array.getByte(array, offset), false);
            case SHORT -> TypeUtil.toFloat(Array.getShort(array, offset), true);
            case USHORT -> TypeUtil.toFloat(Array.getShort(array, offset), false);
            case INT -> TypeUtil.toFloat(Array.getInt(array, offset), true);
            case UINT -> TypeUtil.toFloat(Array.getInt(array, offset), false);
            case LONG -> TypeUtil.toFloat(Array.getLong(array, offset), true);
            case ULONG -> TypeUtil.toFloat(Array.getLong(array, offset), false);
            case FLOAT -> Array.getFloat(array, offset);
            case DOUBLE -> (float) Array.getDouble(array, offset);
        };
    }

    /**
     * Retrieves the value at the specified offset from the given array and converts it to float.
     *
     * @param array  the array to read the value from; must not be {@code null}.
     * @param offset the position within the array from which to retrieve the value; must be within the valid bounds of the array.
     * @param signed a flag indicating whether the value should be treated as signed or unsigned.
     * @return the value at the specified offset, converted to float.
     * @throws NullPointerException      if the provided array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is outside the bounds of the array.
     */
    public static float getValueAsFloat(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return getValueAsFloat(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * Retrieves a value from the specified array at the given offset and converts it to double,
     * based on the provided data type.
     *
     * @param array    the source array from which the value is to be retrieved; must not be {@code null}.
     * @param offset   the zero-based index of the value within the array; must be within the valid range of the array indices.
     * @param dataType the data type of the array elements, which determines how the value is interpreted; must not be {@code null}.
     * @return the value at the specified offset in the array, converted to double.
     * @throws NullPointerException      if the array or data type is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is outside the bounds of the array.
     */
    public static double getValueAsDouble(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final @NonNull DataType dataType) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        final int length = Array.getLength(array);
        enforceRange(offset, 0, length - 1, "Offset must be between 0 and " + (length - 1) + ".");
        Objects.requireNonNull(dataType, "DataType cannot be null.");
        return switch (dataType) {
            case BYTE -> TypeUtil.toDouble(Array.getByte(array, offset), true);
            case UBYTE -> TypeUtil.toDouble(Array.getByte(array, offset), false);
            case SHORT -> TypeUtil.toDouble(Array.getShort(array, offset), true);
            case USHORT -> TypeUtil.toDouble(Array.getShort(array, offset), false);
            case INT -> TypeUtil.toDouble(Array.getInt(array, offset), true);
            case UINT -> TypeUtil.toDouble(Array.getInt(array, offset), false);
            case LONG -> TypeUtil.toDouble(Array.getLong(array, offset), true);
            case ULONG -> TypeUtil.toDouble(Array.getLong(array, offset), false);
            case FLOAT -> (double) Array.getFloat(array, offset);
            case DOUBLE -> Array.getDouble(array, offset);
        };
    }

    /**
     * Retrieves the value at the specified offset from the given array and converts it to double.
     *
     * @param array  the array to read the value from; must not be {@code null}.
     * @param offset the position within the array from which to retrieve the value; must be within the valid bounds of the array.
     * @param signed a flag indicating whether the value should be treated as signed or unsigned.
     * @return the value at the specified offset, converted to double.
     * @throws NullPointerException      if the provided array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is outside the bounds of the array.
     */
    public static double getValueAsDouble(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return getValueAsDouble(array, offset, ArrayUtil.getDataType(array, signed));
    }

    /**
     * Sets the value of an element in an array at the specified offset according to the given data type.
     *
     * @param array    the array to modify; must not be {@code null}.
     * @param offset   the index in the array where the value will be set; must be within the range of the array's length.
     * @param dataType the data type that determines the Java type for the value; must not be {@code null}.
     * @param value    the value to set at the specified offset.
     * @throws NullPointerException      if the array or the dataType is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is not within the valid range of the array indices.
     */
    public static void setValue(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final @NonNull DataType dataType, final double value) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        final int length = Array.getLength(array);
        enforceRange(offset, 0, length - 1, "Offset must be between 0 and " + (length - 1) + ".");
        Objects.requireNonNull(dataType, "Datatype cannot be null.");
        switch (dataType.getJavaType()) {
            case BYTE:
                Array.setByte(array, offset, (byte) value);
                break;

            case SHORT:
                Array.setShort(array, offset, (short) value);
                break;

            case INT:
                Array.setInt(array, offset, (int) value);
                break;

            case LONG:
                Array.setLong(array, offset, (long) value);
                break;

            case FLOAT:
                Array.setFloat(array, offset, (float) value);
                break;

            case DOUBLE:
                Array.setDouble(array, offset, value);
                break;
        }
    }

    /**
     * Sets the value of an element in an array at the specified offset.
     *
     * @param array  the array to modify; must not be {@code null}.
     * @param offset the index in the array where the value will be set; must be within the range of the array's length.
     * @param value  the value to set at the specified offset.
     * @throws NullPointerException      if the array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is not within the valid range of the array indices.
     */
    public static void setValue(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final double value) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        setValue(array, offset, ArrayUtil.getDataType(array), value);
    }

    /**
     * Copies a rectangular region of data from a source array or buffer to a destination array or buffer.
     * Supports optional region selection in the source, positioning in the destination,
     * and handling signed or unsigned data during the copy process. Throws exceptions if invalid arguments
     * are provided or if the regions exceed the boundaries of the specified dimensions.
     *
     * @param src       The source object containing the data to be copied. Must not be {@code null}.
     * @param srcDim    The dimensions (width and height) of the source object. Must not be {@code null}.
     * @param srcRegion The rectangular region within the source object to be copied.
     *                  If {@code null}, the entire source dimensions are used.
     * @param dst       The destination object to which the data will be copied. Must not be {@code null}.
     * @param dstDim    The dimensions (width and height) of the destination object. Must not be {@code null}.
     * @param dstPt     The point within the destination object at which the data will be copied.
     *                  If null, the copy starts in the top-left corner (0,0).
     * @param signed    A boolean flag indicating whether the data to be copied should be treated
     *                  as signed or unsigned values during the conversion process.
     * @throws NullPointerException      If any of the required arguments (src, srcDim, dst, dstDim) are {@code null}.
     * @throws IllegalArgumentException  If any of the source or destination dimensions are invalid,
     *                                   or if the source region or destination point goes out of bounds.
     * @throws IndexOutOfBoundsException If the specific region to be copied falls outside the
     *                                   boundaries of the source or destination dimensions.
     */
    public static void copyRect(final @NonNull Object src, final @NonNull Dimension srcDim, final @Nullable Rectangle srcRegion, final @NonNull Object dst, final @NonNull Dimension dstDim, final @Nullable Point dstPt, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        Objects.requireNonNull(src, "Source array cannot be null.");
        Objects.requireNonNull(srcDim, "Source dimension cannot be null.");
        Objects.requireNonNull(dst, "Destination array cannot be null.");
        Objects.requireNonNull(dstDim, "Destination dimension cannot be null.");

        // source image region
        Rectangle adjSrcRegion = (srcRegion != null) ? srcRegion : new Rectangle(srcDim);

        // negative destination x position?
        if ((dstPt != null) && (dstPt.x < 0)) {
            // adjust source rect and width
            adjSrcRegion.x -= dstPt.x;
            adjSrcRegion.width -= -dstPt.x;
        }
        // negative destination y position?
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
            arrayToArray(src, srcOffset, dst, dstOffset, w, signed);
            srcOffset += srcSizeX;
            dstOffset += dstSizeX;
        }
    }

    /**
     * Fills an array with the provided double value in a specified range.
     *
     * @param array the array to be filled; must not be {@code null}.
     * @param from  the starting index of the range to fill, inclusive; must be within valid array bounds.
     * @param to    the ending index of the range to fill, exclusive; must be greater than or equal to {@code from} and within valid array bounds.
     * @param value the value to be stored in the specified range of the array.
     * @throws NullPointerException      if the array is {@code null}
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if either {@code from} or {@code to} are outside the bounds of the array, or if the {@code from} parameter is greater than the {@code to} parameter.
     */
    public static void fill(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int from, final @Range(from = 0, to = Integer.MAX_VALUE) int to, final double value) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        final DataType dataType = ArrayUtil.getDataType(array);
        final int length = Array.getLength(array);
        enforceGreaterOrEqual(to, from, "To parameter must be greater than or equal to from parameter.");
        enforceRange(from, 0, length - 1, "From parameter must be between 0 and " + (length - 1) + ".");
        enforceRange(to, 0, length - 1, "To parameter must be between 0 and " + (length - 1) + ".");
        switch (dataType) {
            case BYTE:
                Arrays.fill((byte[]) array, from, to, (byte) value);
                break;

            case SHORT:
                Arrays.fill((short[]) array, from, to, (short) value);
                break;

            case INT:
                Arrays.fill((int[]) array, from, to, (int) value);
                break;

            case LONG:
                Arrays.fill((long[]) array, from, to, (long) value);
                break;

            case FLOAT:
                Arrays.fill((float[]) array, from, to, (float) value);
                break;

            case DOUBLE:
                Arrays.fill((double[]) array, from, to, value);
                break;
        }
    }

    /**
     * Fills an array with the provided double value.
     *
     * @param array the array to be filled; must not be {@code null}.
     * @param value the value to be stored in the array.
     * @throws NullPointerException     if the array is {@code null}
     * @throws IllegalArgumentException if the provided object is not an array.
     */
    public static void fill(final @NonNull Object array, final double value) throws NullPointerException, IllegalArgumentException {
        fill(array, 0, Array.getLength(array), value);
    }

    /**
     * Copies a specified number of elements within the given array. The elements are copied
     * from the range starting at the index specified by {@code from} and moved to the range
     * starting at the index specified by {@code to}. If the range overlaps during the copy
     * operation, the method ensures proper handling of forward or backward copy to avoid
     * overwriting unintended elements.
     *
     * @param array The array in which elements will be copied. Must not be {@code null}.
     * @param from  The starting index of the range to copy from. Must be within the bounds
     *              of the array.
     * @param to    The starting index of the range to copy to. Must be within the bounds
     *              of the array.
     * @param cnt   The number of elements to copy. Must be non-negative. The actual number
     *              of elements copied may be adjusted based on array boundaries.
     * @throws NullPointerException      If the array is {@code null}.
     * @throws IllegalArgumentException  If the object provided is not an array.
     * @throws IndexOutOfBoundsException If {@code from} or {@code to} is outside the valid
     *                                   range of indices for the array.
     */
    public static void innerCopy(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int from, final @Range(from = 0, to = Integer.MAX_VALUE) int to, final @Range(from = 0, to = Integer.MAX_VALUE) int cnt) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        switch (ArrayUtil.getDataType(array)) {
            case BYTE:
                innerCopy((byte[]) array, from, to, cnt);
                return;

            case SHORT:
                innerCopy((short[]) array, from, to, cnt);
                return;

            case INT:
                innerCopy((int[]) array, from, to, cnt);
                return;

            case LONG:
                innerCopy((long[]) array, from, to, cnt);
                return;

            case FLOAT:
                innerCopy((float[]) array, from, to, cnt);
                return;

            case DOUBLE:
                innerCopy((double[]) array, from, to, cnt);
                return;
        }

        final int length = Array.getLength(array);

        enforceRange(from, 0, length - 1, "From parameter must be between 0 and " + (length - 1) + ".");
        enforceRange(to, 0, length - 1, "To parameter must be between 0 and " + (length - 1) + ".");

        // use generic code
        final int delta = to - from;

        if (delta == 0)
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
     * Copies a specified number of elements within the given array. The elements are copied
     * from the range starting at the index specified by {@code from} and moved to the range
     * starting at the index specified by {@code to}. If the range overlaps during the copy
     * operation, the method ensures proper handling of forward or backward copy to avoid
     * overwriting unintended elements.
     *
     * @param array The array of bytes in which elements will be copied. Must not be {@code null}.
     * @param from  The starting index of the range to copy from. Must be within the bounds
     *              of the array.
     * @param to    The starting index of the range to copy to. Must be within the bounds
     *              of the array.
     * @param cnt   The number of elements to copy. Must be non-negative. The actual number
     *              of elements copied may be adjusted based on array boundaries.
     * @throws NullPointerException      If the array is {@code null}.
     * @throws IndexOutOfBoundsException If {@code from} or {@code to} is outside the valid
     *                                   range of indices for the array.
     */
    public static void innerCopy(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int from, final @Range(from = 0, to = Integer.MAX_VALUE) int to, final @Range(from = 0, to = Integer.MAX_VALUE) int cnt) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");

        final int length = array.length;
        enforceRange(from, 0, length - 1, "From parameter must be between 0 and " + (length - 1) + ".");
        enforceRange(to, 0, length - 1, "To parameter must be between 0 and " + (length - 1) + ".");

        final int delta = to - from;

        if (delta == 0)
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
     * Copies a specified number of elements within the given array. The elements are copied
     * from the range starting at the index specified by {@code from} and moved to the range
     * starting at the index specified by {@code to}. If the range overlaps during the copy
     * operation, the method ensures proper handling of forward or backward copy to avoid
     * overwriting unintended elements.
     *
     * @param array The array of shorts in which elements will be copied. Must not be {@code null}.
     * @param from  The starting index of the range to copy from. Must be within the bounds
     *              of the array.
     * @param to    The starting index of the range to copy to. Must be within the bounds
     *              of the array.
     * @param cnt   The number of elements to copy. Must be non-negative. The actual number
     *              of elements copied may be adjusted based on array boundaries.
     * @throws NullPointerException      If the array is {@code null}.
     * @throws IndexOutOfBoundsException If {@code from} or {@code to} is outside the valid
     *                                   range of indices for the array.
     */
    public static void innerCopy(final short @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int from, final @Range(from = 0, to = Integer.MAX_VALUE) int to, final @Range(from = 0, to = Integer.MAX_VALUE) int cnt) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");

        final int length = array.length;
        enforceRange(from, 0, length - 1, "From parameter must be between 0 and " + (length - 1) + ".");
        enforceRange(to, 0, length - 1, "To parameter must be between 0 and " + (length - 1) + ".");

        final int delta = to - from;

        if (delta == 0)
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
     * Copies a specified number of elements within the given array. The elements are copied
     * from the range starting at the index specified by {@code from} and moved to the range
     * starting at the index specified by {@code to}. If the range overlaps during the copy
     * operation, the method ensures proper handling of forward or backward copy to avoid
     * overwriting unintended elements.
     *
     * @param array The array of ints in which elements will be copied. Must not be {@code null}.
     * @param from  The starting index of the range to copy from. Must be within the bounds
     *              of the array.
     * @param to    The starting index of the range to copy to. Must be within the bounds
     *              of the array.
     * @param cnt   The number of elements to copy. Must be non-negative. The actual number
     *              of elements copied may be adjusted based on array boundaries.
     * @throws NullPointerException      If the array is {@code null}.
     * @throws IndexOutOfBoundsException If {@code from} or {@code to} is outside the valid
     *                                   range of indices for the array.
     */
    public static void innerCopy(final int @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int from, final @Range(from = 0, to = Integer.MAX_VALUE) int to, final @Range(from = 0, to = Integer.MAX_VALUE) int cnt) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");

        final int length = array.length;
        enforceRange(from, 0, length - 1, "From parameter must be between 0 and " + (length - 1) + ".");
        enforceRange(to, 0, length - 1, "To parameter must be between 0 and " + (length - 1) + ".");

        final int delta = to - from;

        if (delta == 0)
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
     * Copies a specified number of elements within the given array. The elements are copied
     * from the range starting at the index specified by {@code from} and moved to the range
     * starting at the index specified by {@code to}. If the range overlaps during the copy
     * operation, the method ensures proper handling of forward or backward copy to avoid
     * overwriting unintended elements.
     *
     * @param array The array of longs in which elements will be copied. Must not be {@code null}.
     * @param from  The starting index of the range to copy from. Must be within the bounds
     *              of the array.
     * @param to    The starting index of the range to copy to. Must be within the bounds
     *              of the array.
     * @param cnt   The number of elements to copy. Must be non-negative. The actual number
     *              of elements copied may be adjusted based on array boundaries.
     * @throws NullPointerException      If the array is {@code null}.
     * @throws IndexOutOfBoundsException If {@code from} or {@code to} is outside the valid
     *                                   range of indices for the array.
     */
    public static void innerCopy(final long @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int from, final @Range(from = 0, to = Integer.MAX_VALUE) int to, final @Range(from = 0, to = Integer.MAX_VALUE) int cnt) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");

        final int length = array.length;
        enforceRange(from, 0, length - 1, "From parameter must be between 0 and " + (length - 1) + ".");
        enforceRange(to, 0, length - 1, "To parameter must be between 0 and " + (length - 1) + ".");

        final int delta = to - from;

        if (delta == 0)
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
     * Copies a specified number of elements within the given array. The elements are copied
     * from the range starting at the index specified by {@code from} and moved to the range
     * starting at the index specified by {@code to}. If the range overlaps during the copy
     * operation, the method ensures proper handling of forward or backward copy to avoid
     * overwriting unintended elements.
     *
     * @param array The array of floats in which elements will be copied. Must not be {@code null}.
     * @param from  The starting index of the range to copy from. Must be within the bounds
     *              of the array.
     * @param to    The starting index of the range to copy to. Must be within the bounds
     *              of the array.
     * @param cnt   The number of elements to copy. Must be non-negative. The actual number
     *              of elements copied may be adjusted based on array boundaries.
     * @throws NullPointerException      If the array is {@code null}.
     * @throws IndexOutOfBoundsException If {@code from} or {@code to} is outside the valid
     *                                   range of indices for the array.
     */
    public static void innerCopy(final float @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int from, final @Range(from = 0, to = Integer.MAX_VALUE) int to, final @Range(from = 0, to = Integer.MAX_VALUE) int cnt) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");

        final int length = array.length;
        enforceRange(from, 0, length - 1, "From parameter must be between 0 and " + (length - 1) + ".");
        enforceRange(to, 0, length - 1, "To parameter must be between 0 and " + (length - 1) + ".");

        final int delta = to - from;

        if (delta == 0)
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
     * Copies a specified number of elements within the given array. The elements are copied
     * from the range starting at the index specified by {@code from} and moved to the range
     * starting at the index specified by {@code to}. If the range overlaps during the copy
     * operation, the method ensures proper handling of forward or backward copy to avoid
     * overwriting unintended elements.
     *
     * @param array The array of doubles in which elements will be copied. Must not be {@code null}.
     * @param from  The starting index of the range to copy from. Must be within the bounds
     *              of the array.
     * @param to    The starting index of the range to copy to. Must be within the bounds
     *              of the array.
     * @param cnt   The number of elements to copy. Must be non-negative. The actual number
     *              of elements copied may be adjusted based on array boundaries.
     * @throws NullPointerException      If the array is {@code null}.
     * @throws IndexOutOfBoundsException If {@code from} or {@code to} is outside the valid
     *                                   range of indices for the array.
     */
    public static void innerCopy(final double @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int from, final @Range(from = 0, to = Integer.MAX_VALUE) int to, final @Range(from = 0, to = Integer.MAX_VALUE) int cnt) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");

        final int length = array.length;
        enforceRange(from, 0, length - 1, "From parameter must be between 0 and " + (length - 1) + ".");
        enforceRange(to, 0, length - 1, "To parameter must be between 0 and " + (length - 1) + ".");

        final int delta = to - from;

        if (delta == 0)
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
     * Copies the contents of the input byte array to the specified position in the output byte array.
     *
     * @param in     the input byte array to be copied; must not be null.
     * @param out    the output byte array where data from the input array will be written; must not be null.
     * @param offset the starting position in the output array where the data will be written; must be within the valid range.
     * @return the modified output byte array after copying the contents of the input array.
     * @throws NullPointerException      if the input or output array is null.
     * @throws IndexOutOfBoundsException if the specified offset is out of the valid range for the output array.
     * @deprecated Use {@link System#arraycopy(Object, int, Object, int, int)} instead.
     */
    @Contract("_, _, _ -> param2")
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static byte @NonNull [] toByteArray1D(final byte @NonNull [] in, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceRange(offset, 0, in.length - 1, "Offset must be between 0 and " + (in.length - 1) + ".");
        final int length = in.length;

        System.arraycopy(in, 0, out, offset, length);

        return out;
    }

    /**
     * Copies the contents of the input short array to the specified position in the output short array.
     *
     * @param in     the input short array to be copied; must not be null.
     * @param out    the output short array where data from the input array will be written; must not be null.
     * @param offset the starting position in the output array where the data will be written; must be within the valid range.
     * @return the modified output short array after copying the contents of the input array.
     * @throws NullPointerException      if the input or output array is null.
     * @throws IndexOutOfBoundsException if the specified offset is out of the valid range for the output array.
     * @deprecated Use {@link System#arraycopy(Object, int, Object, int, int)} instead.
     */
    @Contract("_, _, _ -> param2")
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static short @NonNull [] toShortArray1D(final short @NonNull [] in, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceRange(offset, 0, in.length - 1, "Offset must be between 0 and " + (in.length - 1) + ".");
        final int length = in.length;

        System.arraycopy(in, 0, out, offset, length);

        return out;
    }

    /**
     * Copies the contents of the input int array to the specified position in the output int array.
     *
     * @param in     the input int array to be copied; must not be null.
     * @param out    the output int array where data from the input array will be written; must not be null.
     * @param offset the starting position in the output array where the data will be written; must be within the valid range.
     * @return the modified output int array after copying the contents of the input array.
     * @throws NullPointerException      if the input or output array is null.
     * @throws IndexOutOfBoundsException if the specified offset is out of the valid range for the output array.
     * @deprecated Use {@link System#arraycopy(Object, int, Object, int, int)} instead.
     */
    @Contract("_, _, _ -> param2")
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static int @NonNull [] toIntArray1D(final int @NonNull [] in, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceRange(offset, 0, in.length - 1, "Offset must be between 0 and " + (in.length - 1) + ".");
        final int length = in.length;

        System.arraycopy(in, 0, out, offset, length);

        return out;
    }

    /**
     * Copies the contents of the input long array to the specified position in the output long array.
     *
     * @param in     the input long array to be copied; must not be null.
     * @param out    the output long array where data from the input array will be written; must not be null.
     * @param offset the starting position in the output array where the data will be written; must be within the valid range.
     * @return the modified output long array after copying the contents of the input array.
     * @throws NullPointerException      if the input or output array is null.
     * @throws IndexOutOfBoundsException if the specified offset is out of the valid range for the output array.
     * @deprecated Use {@link System#arraycopy(Object, int, Object, int, int)} instead.
     */
    @Contract("_, _, _ -> param2")
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static long @NonNull [] toLongArray1D(final long @NonNull [] in, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceRange(offset, 0, in.length - 1, "Offset must be between 0 and " + (in.length - 1) + ".");
        final int length = in.length;

        System.arraycopy(in, 0, out, offset, length);

        return out;
    }

    /**
     * Copies the contents of the input float array to the specified position in the output float array.
     *
     * @param in     the input float array to be copied; must not be null.
     * @param out    the output float array where data from the input array will be written; must not be null.
     * @param offset the starting position in the output array where the data will be written; must be within the valid range.
     * @return the modified output float array after copying the contents of the input array.
     * @throws NullPointerException      if the input or output array is null.
     * @throws IndexOutOfBoundsException if the specified offset is out of the valid range for the output array.
     * @deprecated Use {@link System#arraycopy(Object, int, Object, int, int)} instead.
     */
    @Contract("_, _, _ -> param2")
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static float @NonNull [] toFloatArray1D(final float @NonNull [] in, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceRange(offset, 0, in.length - 1, "Offset must be between 0 and " + (in.length - 1) + ".");
        final int length = in.length;

        System.arraycopy(in, 0, out, offset, length);

        return out;
    }

    /**
     * Copies the contents of the input double array to the specified position in the output double array.
     *
     * @param in     the input double array to be copied; must not be null.
     * @param out    the output double array where data from the input array will be written; must not be null.
     * @param offset the starting position in the output array where the data will be written; must be within the valid range.
     * @return the modified output double array after copying the contents of the input array.
     * @throws NullPointerException      if the input or output array is null.
     * @throws IndexOutOfBoundsException if the specified offset is out of the valid range for the output array.
     * @deprecated Use {@link System#arraycopy(Object, int, Object, int, int)} instead.
     */
    @Contract("_, _, _ -> param2")
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static double @NonNull [] toDoubleArray1D(final double @NonNull [] in, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceRange(offset, 0, in.length - 1, "Offset must be between 0 and " + (in.length - 1) + ".");
        final int length = in.length;

        System.arraycopy(in, 0, out, offset, length);

        return out;
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object arrayToArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in     The input array containing the source elements. Must not be {@code null}.
     * @param out    The output array where elements will be copied to. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object arrayToArray(final @NonNull Object in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToArray(in, 0, out, 0, Array.getLength(in), signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] arrayToByteArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToByteArray((byte[]) in, inOffset, out, outOffset, length);
            case SHORT, USHORT -> shortArrayToByteArray((short[]) in, inOffset, out, outOffset, length);
            case INT, UINT -> intArrayToByteArray((int[]) in, inOffset, out, outOffset, length);
            case LONG, ULONG -> longArrayToByteArray((long[]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToByteArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToByteArray((double[]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in  The input array containing the source elements. Must not be {@code null}.
     * @param out The output array where elements will be copied to. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _ -> param2")
    public static byte @NonNull [] arrayToByteArray(final @NonNull Object in, final byte @NonNull [] out) throws NullPointerException, IllegalArgumentException {
        return arrayToByteArray(in, 0, out, 0, Array.getLength(in));
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] arrayToByteArray(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToByteArray(array, 0, new byte[length], 0, length);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static short @NonNull [] arrayToShortArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToShortArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT, USHORT -> shortArrayToShortArray((short[]) in, inOffset, out, outOffset, length);
            case INT, UINT -> intArrayToShortArray((int[]) in, inOffset, out, outOffset, length);
            case LONG, ULONG -> longArrayToShortArray((long[]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToShortArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToShortArray((double[]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in     The input array containing the source elements. Must not be {@code null}.
     * @param out    The output array where elements will be copied to. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _, _ -> param2")
    public static short @NonNull [] arrayToShortArray(final @NonNull Object in, final short @NonNull [] out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToShortArray(in, 0, out, 0, Array.getLength(in), signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] arrayToShortArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToShortArray(array, 0, new short[length], 0, length, signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] arrayToIntArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToIntArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT, USHORT -> shortArrayToIntArray((short[]) in, inOffset, out, outOffset, length, signed);
            case INT, UINT -> intArrayToIntArray((int[]) in, inOffset, out, outOffset, length);
            case LONG, ULONG -> longArrayToIntArray((long[]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToIntArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToIntArray((double[]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in     The input array containing the source elements. Must not be {@code null}.
     * @param out    The output array where elements will be copied to. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _, _ -> param2")
    public static int @NonNull [] arrayToIntArray(final @NonNull Object in, final int @NonNull [] out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToIntArray(in, 0, out, 0, Array.getLength(in), signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] arrayToIntArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToIntArray(array, 0, new int[length], 0, length, signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] arrayToLongArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToLongArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT, USHORT -> shortArrayToLongArray((short[]) in, inOffset, out, outOffset, length, signed);
            case INT, UINT -> intArrayToLongArray((int[]) in, inOffset, out, outOffset, length, signed);
            case LONG, ULONG -> longArrayToLongArray((long[]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToLongArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToLongArray((double[]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in     The input array containing the source elements. Must not be {@code null}.
     * @param out    The output array where elements will be copied to. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _, _ -> param2")
    public static long @NonNull [] arrayToLongArray(final @NonNull Object in, final long @NonNull [] out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToLongArray(in, 0, out, 0, Array.getLength(in), signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] arrayToLongArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToLongArray(array, 0, new long[length], 0, length, signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static float @NonNull [] arrayToFloatArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToFloatArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT, USHORT -> shortArrayToFloatArray((short[]) in, inOffset, out, outOffset, length, signed);
            case INT, UINT -> intArrayToFloatArray((int[]) in, inOffset, out, outOffset, length, signed);
            case LONG, ULONG -> longArrayToFloatArray((long[]) in, inOffset, out, outOffset, length, signed);
            case FLOAT -> floatArrayToFloatArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToFloatArray((double[]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in     The input array containing the source elements. Must not be {@code null}.
     * @param out    The output array where elements will be copied to. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _, _ -> param2")
    public static float @NonNull [] arrayToFloatArray(final @NonNull Object in, final float @NonNull [] out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToFloatArray(in, 0, out, 0, Array.getLength(in), signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _ -> new")
    public static float @NonNull [] arrayToFloatArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToFloatArray(array, 0, new float[length], 0, length, signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static double @NonNull [] arrayToDoubleArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToDoubleArray((byte[]) in, inOffset, out, outOffset, length, signed);
            case SHORT, USHORT -> shortArrayToDoubleArray((short[]) in, inOffset, out, outOffset, length, signed);
            case INT, UINT -> intArrayToDoubleArray((int[]) in, inOffset, out, outOffset, length, signed);
            case LONG, ULONG -> longArrayToDoubleArray((long[]) in, inOffset, out, outOffset, length, signed);
            case FLOAT -> floatArrayToDoubleArray((float[]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToDoubleArray((double[]) in, inOffset, out, outOffset, length);
            //default -> out;
        };
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in     The input array containing the source elements. Must not be {@code null}.
     * @param out    The output array where elements will be copied to. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _, _ -> param2")
    public static double @NonNull [] arrayToDoubleArray(final @NonNull Object in, final double @NonNull [] out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToDoubleArray(in, 0, out, 0, Array.getLength(in), signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _ -> new")
    public static double @NonNull [] arrayToDoubleArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToDoubleArray(array, 0, new double[length], 0, length, signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object byteArrayToArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in     The input array containing the source elements. Must not be {@code null}.
     * @param out    The output array where elements will be copied to. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object byteArrayToArray(final byte @NonNull [] in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return byteArrayToArray(in, 0, out, 0, in.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] byteArrayToByteArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        System.arraycopy(in, inOffset, out, outOffset, len);

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static byte @NonNull [] byteArrayToByteArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return byteArrayToByteArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] byteArrayToByteArray(final byte @NonNull [] array) throws NullPointerException {
        return byteArrayToByteArray(array, 0, new byte[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static short @NonNull [] byteArrayToShortArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = (short) TypeUtil.unsign(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] byteArrayToShortArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return byteArrayToShortArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] byteArrayToShortArray(final byte @NonNull [] array, final boolean signed) throws NullPointerException {
        return byteArrayToShortArray(array, 0, new short[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] byteArrayToIntArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static int @NonNull [] byteArrayToIntArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return byteArrayToIntArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] byteArrayToIntArray(final byte @NonNull [] array, final boolean signed) throws NullPointerException {
        return byteArrayToIntArray(array, 0, new int[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] byteArrayToLongArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsignL(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static long @NonNull [] byteArrayToLongArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return byteArrayToLongArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] byteArrayToLongArray(final byte @NonNull [] array, final boolean signed) throws NullPointerException {
        return byteArrayToLongArray(array, 0, new long[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static float @NonNull [] byteArrayToFloatArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static float @NonNull [] byteArrayToFloatArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return byteArrayToFloatArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static float @NonNull [] byteArrayToFloatArray(final byte @NonNull [] array, final boolean signed) throws NullPointerException {
        return byteArrayToFloatArray(array, 0, new float[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static double @NonNull [] byteArrayToDoubleArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static double @NonNull [] byteArrayToDoubleArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return byteArrayToDoubleArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static double @NonNull [] byteArrayToDoubleArray(final byte @NonNull [] array, final boolean signed) throws NullPointerException {
        return byteArrayToDoubleArray(array, 0, new double[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object shortArrayToArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in     The input array containing the source elements. Must not be {@code null}.
     * @param out    The output array where elements will be copied to. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object shortArrayToArray(final short @NonNull [] in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return shortArrayToArray(in, 0, out, 0, in.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] shortArrayToByteArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = (byte) in[i + inOffset];

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static byte @NonNull [] shortArrayToByteArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return shortArrayToByteArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] shortArrayToByteArray(final short @NonNull [] array) throws NullPointerException {
        return shortArrayToByteArray(array, 0, new byte[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] shortArrayToShortArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        System.arraycopy(in, inOffset, out, outOffset, len);

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static short @NonNull [] shortArrayToShortArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return shortArrayToShortArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static short @NonNull [] shortArrayToShortArray(final short @NonNull [] array) throws NullPointerException {
        return shortArrayToShortArray(array, 0, new short[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] shortArrayToIntArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static int @NonNull [] shortArrayToIntArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return shortArrayToIntArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] shortArrayToIntArray(final short @NonNull [] array, final boolean signed) throws NullPointerException {
        return shortArrayToIntArray(array, 0, new int[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] shortArrayToLongArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsignL(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static long @NonNull [] shortArrayToLongArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return shortArrayToLongArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] shortArrayToLongArray(final short @NonNull [] array, final boolean signed) throws NullPointerException {
        return shortArrayToLongArray(array, 0, new long[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static float @NonNull [] shortArrayToFloatArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static float @NonNull [] shortArrayToFloatArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return shortArrayToFloatArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static float @NonNull [] shortArrayToFloatArray(final short @NonNull [] array, final boolean signed) throws NullPointerException {
        return shortArrayToFloatArray(array, 0, new float[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static double @NonNull [] shortArrayToDoubleArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static double @NonNull [] shortArrayToDoubleArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return shortArrayToDoubleArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static double @NonNull [] shortArrayToDoubleArray(final short @NonNull [] array, final boolean signed) throws NullPointerException {
        return shortArrayToDoubleArray(array, 0, new double[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object intArrayToArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in     The input array containing the source elements. Must not be {@code null}.
     * @param out    The output array where elements will be copied to. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object intArrayToArray(final int @NonNull [] in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return intArrayToArray(in, 0, out, 0, in.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] intArrayToByteArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = (byte) in[i + inOffset];

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static byte @NonNull [] intArrayToByteArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return intArrayToByteArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] intArrayToByteArray(final int @NonNull [] array) throws NullPointerException {
        return intArrayToByteArray(array, 0, new byte[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] intArrayToShortArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = (short) in[i + inOffset];

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static short @NonNull [] intArrayToShortArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return intArrayToShortArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static short @NonNull [] intArrayToShortArray(final int @NonNull [] array) throws NullPointerException {
        return intArrayToShortArray(array, 0, new short[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static int @NonNull [] intArrayToIntArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        System.arraycopy(in, inOffset, out, outOffset, len);

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static int @NonNull [] intArrayToIntArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return intArrayToIntArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static int @NonNull [] intArrayToIntArray(final int @NonNull [] array) throws NullPointerException {
        return intArrayToIntArray(array, 0, new int[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] intArrayToLongArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static long @NonNull [] intArrayToDoubleArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return intArrayToLongArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] intArrayToLongArray(final int @NonNull [] array, final boolean signed) throws NullPointerException {
        return intArrayToLongArray(array, 0, new long[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static float @NonNull [] intArrayToFloatArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static float @NonNull [] intArrayToFloatArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return intArrayToFloatArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static float @NonNull [] intArrayToFloatArray(final int @NonNull [] array, final boolean signed) throws NullPointerException {
        return intArrayToFloatArray(array, 0, new float[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static double @NonNull [] intArrayToDoubleArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static double @NonNull [] intArrayToDoubleArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return intArrayToDoubleArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static double @NonNull [] intArrayToDoubleArray(final int @NonNull [] array, final boolean signed) throws NullPointerException {
        return intArrayToDoubleArray(array, 0, new double[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object longArrayToArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in     The input array containing the source elements. Must not be {@code null}.
     * @param out    The output array where elements will be copied to. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object longArrayToArray(final long @NonNull [] in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return longArrayToArray(in, 0, out, 0, in.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] longArrayToByteArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = (byte) in[i + inOffset];

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static byte @NonNull [] longArrayToByteArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return longArrayToByteArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] longArrayToByteArray(final long @NonNull [] array) throws NullPointerException {
        return longArrayToByteArray(array, 0, new byte[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] longArrayToShortArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = (short) in[i + inOffset];

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static short @NonNull [] longArrayToShortArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return longArrayToShortArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static short @NonNull [] longArrayToShortArray(final long @NonNull [] array) throws NullPointerException {
        return longArrayToShortArray(array, 0, new short[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static int @NonNull [] longArrayToIntArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = (int) in[i + inOffset];

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static int @NonNull [] longArrayToIntArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return longArrayToIntArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static int @NonNull [] longArrayToIntArray(final long @NonNull [] array) throws NullPointerException {
        return longArrayToIntArray(array, 0, new int[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static long @NonNull [] longArrayToLongArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        System.arraycopy(in, inOffset, out, outOffset, len);

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static long @NonNull [] longArrayToLongArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return longArrayToLongArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static long @NonNull [] longArrayToLongArray(final long @NonNull [] array) throws NullPointerException {
        return longArrayToLongArray(array, 0, new long[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static float @NonNull [] longArrayToFloatArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsignF(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static float @NonNull [] longArrayToFloatArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return longArrayToFloatArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static float @NonNull [] longArrayToFloatArray(final long @NonNull [] array, final boolean signed) throws NullPointerException {
        return longArrayToFloatArray(array, 0, new float[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static double @NonNull [] longArrayToDoubleArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = in[i + inOffset];
        }
        else {
            for (int i = 0; i < len; i++)
                out[i + outOffset] = TypeUtil.unsign(in[i + inOffset]);
        }

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param signed    Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static double @NonNull [] longArrayToDoubleArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return longArrayToDoubleArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array  The input array containing the source elements. Must not be {@code null}.
     * @param signed Indicates whether the conversion (if applicable) should preserve signedness of numeric values.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_, _ -> new")
    public static double @NonNull [] longArrayToDoubleArray(final long @NonNull [] array, final boolean signed) throws NullPointerException {
        return longArrayToDoubleArray(array, 0, new double[array.length], 0, array.length, signed);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static @NonNull Object floatArrayToArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in  The input array containing the source elements. Must not be {@code null}.
     * @param out The output array where elements will be copied to. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _ -> param2")
    public static @NonNull Object floatArrayToArray(final float @NonNull [] in, final @NonNull Object out) throws NullPointerException, IllegalArgumentException {
        return floatArrayToArray(in, 0, out, 0, in.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] floatArrayToByteArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = (byte) in[i + inOffset];

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static byte @NonNull [] floatArrayToByteArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return floatArrayToByteArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] floatArrayToByteArray(final float @NonNull [] array) throws NullPointerException {
        return floatArrayToByteArray(array, 0, new byte[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] floatArrayToShortArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = (short) in[i + inOffset];

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static short @NonNull [] floatArrayToShortArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return floatArrayToShortArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static short @NonNull [] floatArrayToShortArray(final float @NonNull [] array) throws NullPointerException {
        return floatArrayToShortArray(array, 0, new short[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static int @NonNull [] floatArrayToIntArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = TypeUtil.toInt(in[i + inOffset]);

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static int @NonNull [] floatArrayToIntArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return floatArrayToIntArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static int @NonNull [] floatArrayToIntArray(final float @NonNull [] array) throws NullPointerException {
        return floatArrayToIntArray(array, 0, new int[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static long @NonNull [] floatArrayToLongArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = TypeUtil.toLong(in[i + inOffset]);

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static long @NonNull [] floatArrayToLongArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return floatArrayToLongArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static long @NonNull [] floatArrayToLongArray(final float @NonNull [] array) throws NullPointerException {
        return floatArrayToLongArray(array, 0, new long[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static float @NonNull [] floatArrayToFloatArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        System.arraycopy(in, inOffset, out, outOffset, len);

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static float @NonNull [] floatArrayToFloatArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return floatArrayToFloatArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static float @NonNull [] floatArrayToFloatArray(final float @NonNull [] array) throws NullPointerException {
        return floatArrayToFloatArray(array, 0, new float[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static double @NonNull [] floatArrayToDoubleArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = in[i + inOffset];

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static double @NonNull [] floatArrayToDoubleArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return floatArrayToDoubleArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static double @NonNull [] floatArrayToDoubleArray(final float @NonNull [] array) throws NullPointerException {
        return floatArrayToDoubleArray(array, 0, new double[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IllegalArgumentException  If the input or output array has an unsupported data type.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static @NonNull Object doubleArrayToArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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
     * Copies elements from an input array to an output array, supporting various primitive data types.
     *
     * @param in  The input array containing the source elements. Must not be {@code null}.
     * @param out The output array where elements will be copied to. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException     If the input or output array is null.
     * @throws IllegalArgumentException If the input or output array has an unsupported data type.
     */
    @Contract("_, _ -> param2")
    public static @NonNull Object doubleArrayToArray(final double @NonNull [] in, final @NonNull Object out) throws NullPointerException, IllegalArgumentException {
        return doubleArrayToArray(in, 0, out, 0, in.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] doubleArrayToByteArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = (byte) in[i + inOffset];

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static byte @NonNull [] doubleArrayToByteArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return doubleArrayToByteArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] doubleArrayToByteArray(final double @NonNull [] array) throws NullPointerException {
        return doubleArrayToByteArray(array, 0, new byte[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] doubleArrayToShortArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = (short) in[i + inOffset];

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static short @NonNull [] doubleArrayToShortArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return doubleArrayToShortArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static short @NonNull [] doubleArrayToShortArray(final double @NonNull [] array) throws NullPointerException {
        return doubleArrayToShortArray(array, 0, new short[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static int @NonNull [] doubleArrayToIntArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = TypeUtil.toInt(in[i + inOffset]);

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static int @NonNull [] doubleArrayToIntArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return doubleArrayToIntArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static int @NonNull [] doubleArrayToIntArray(final double @NonNull [] array) throws NullPointerException {
        return doubleArrayToIntArray(array, 0, new int[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static long @NonNull [] doubleArrayToLongArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = TypeUtil.toLong(in[i + inOffset]);

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static long @NonNull [] doubleArrayToLongArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return doubleArrayToLongArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static long @NonNull [] doubleArrayToLongArray(final double @NonNull [] array) throws NullPointerException {
        return doubleArrayToLongArray(array, 0, new long[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static float @NonNull [] doubleArrayToFloatArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        for (int i = 0; i < len; i++)
            out[i + outOffset] = (float) in[i + inOffset];

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static float @NonNull [] doubleArrayToFloatArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return doubleArrayToFloatArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static float @NonNull [] doubleArrayToFloatArray(final double @NonNull [] array) throws NullPointerException {
        return doubleArrayToFloatArray(array, 0, new float[array.length], 0, array.length);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @param length    The number of elements to be copied. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static double @NonNull [] doubleArrayToDoubleArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        System.arraycopy(in, inOffset, out, outOffset, len);

        return out;
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param in        The input array containing the source elements. Must not be {@code null}.
     * @param inOffset  The starting index in the input array. Must be non-negative.
     * @param out       The output array where elements will be copied to. Must not be {@code null}.
     * @param outOffset The starting index in the output array. Must be non-negative.
     * @return The output array containing the copied elements.
     * @throws NullPointerException      If the input or output array is null.
     * @throws IndexOutOfBoundsException If any of the length or the offsets exceed the bounds of the arrays.
     */
    @Contract("_, _, _, _ -> param3")
    public static double @NonNull [] doubleArrayToDoubleArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IndexOutOfBoundsException {
        return doubleArrayToDoubleArray(in, inOffset, out, outOffset, in.length - inOffset);
    }

    /**
     * Copies elements from an input array to an output array.
     *
     * @param array The input array containing the source elements. Must not be {@code null}.
     * @return The output array containing the copied elements.
     * @throws NullPointerException If the input or output array is null.
     */
    @Contract("_ -> new")
    public static double @NonNull [] doubleArrayToDoubleArray(final double @NonNull [] array) throws NullPointerException {
        return doubleArrayToDoubleArray(array, 0, new double[array.length], 0, array.length);
    }

    /**
     * Converts an input array to a safe array of byte values, handling different input data types
     * and ensuring compatibility with the specified signedness. The method processes a portion
     * of the input array starting at the specified offset and writes the result into the
     * specified portion of the output array.
     *
     * @param in        the input array, which must be a supported data type (e.g., byte[], short[], int[], long[], float[], or double[]).
     * @param inOffset  the offset within the input array to start reading data from; must be non-negative.
     * @param out       the output array where the transformed byte values will be stored.
     * @param outOffset the offset within the output array to start writing data to; must be non-negative.
     * @param length    the number of elements to process; must be non-negative and within bounds of both arrays.
     * @param srcSigned a flag indicating whether the input array should be treated as signed.
     * @param dstSigned a flag indicating whether the output array should be treated as signed.
     * @return the output array containing the transformed byte values.
     * @throws NullPointerException      if the input or output array is {@code null}.
     * @throws IllegalArgumentException  if the input type is unsupported or input is not an array.
     * @throws IndexOutOfBoundsException if the offsets or length exceed the bounds of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static byte @NonNull [] arrayToSafeByteArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToSafeByteArray((byte[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case SHORT, USHORT -> shortArrayToSafeByteArray((short[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case INT, UINT -> intArrayToSafeByteArray((int[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG, ULONG -> longArrayToSafeByteArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeByteArray((float[]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeByteArray((double[]) in, inOffset, out, outOffset, length, dstSigned);
            //default -> out;
        };
    }

    /**
     * Converts an input array to a safe array of byte values, handling different input data types
     * and ensuring compatibility with the specified signedness.
     *
     * @param in        the input array, which must be a supported data type (e.g., byte[], short[], int[], long[], float[], or double[]).
     * @param out       the output array where the transformed byte values will be stored.
     * @param srcSigned a flag indicating whether the input array should be treated as signed.
     * @param dstSigned a flag indicating whether the output array should be treated as signed.
     * @return the output array containing the transformed byte values.
     * @throws NullPointerException     if the input or output array is {@code null}.
     * @throws IllegalArgumentException if the input type is unsupported or input is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static byte @NonNull [] arrayToSafeByteArray(final @NonNull Object in, final byte @NonNull [] out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return arrayToSafeByteArray(in, 0, out, 0, Array.getLength(in), srcSigned, dstSigned);
    }

    /**
     * Converts an input array to a safe array of byte values, handling different input data types
     * and ensuring compatibility with the specified signedness.
     *
     * @param array     the input array, which must be a supported data type (e.g., byte[], short[], int[], long[], float[], or double[]).
     * @param srcSigned a flag indicating whether the input array should be treated as signed.
     * @param dstSigned a flag indicating whether the output array should be treated as signed.
     * @return the newly created output array containing the transformed byte values.
     * @throws NullPointerException     if the input or output array is {@code null}.
     * @throws IllegalArgumentException if the input type is unsupported or input is not an array.
     */
    @Contract("_, _, _ -> new")
    public static byte @NonNull [] arrayToSafeByteArray(final @NonNull Object array, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToSafeByteArray(array, 0, new byte[length], 0, length, srcSigned, dstSigned);
    }

    /**
     * Converts an input array to a safe array of short values, handling different input data types
     * and ensuring compatibility with the specified signedness. The method processes a portion
     * of the input array starting at the specified offset and writes the result into the
     * specified portion of the output array.
     *
     * @param in        the input array, which must be a supported data type (e.g., byte[], short[], int[], long[], float[], or double[]).
     * @param inOffset  the offset within the input array to start reading data from; must be non-negative.
     * @param out       the output array where the transformed short values will be stored.
     * @param outOffset the offset within the output array to start writing data to; must be non-negative.
     * @param length    the number of elements to process; must be non-negative and within bounds of both arrays.
     * @param srcSigned a flag indicating whether the input array should be treated as signed.
     * @param dstSigned a flag indicating whether the output array should be treated as signed.
     * @return the output array containing the transformed short values.
     * @throws NullPointerException      if the input or output array is {@code null}.
     * @throws IllegalArgumentException  if the input type is unsupported or input is not an array.
     * @throws IndexOutOfBoundsException if the offsets or length exceed the bounds of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static short @NonNull [] arrayToSafeShortArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToShortArray((byte[]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT, USHORT -> shortArrayToSafeShortArray((short[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case INT, UINT -> intArrayToSafeShortArray((int[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG, ULONG -> longArrayToSafeShortArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeShortArray((float[]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeShortArray((double[]) in, inOffset, out, outOffset, length, dstSigned);
            //default -> out;
        };
    }

    /**
     * Converts an input array to a safe array of short values, handling different input data types
     * and ensuring compatibility with the specified signedness.
     *
     * @param in        the input array, which must be a supported data type (e.g., byte[], short[], int[], long[], float[], or double[]).
     * @param out       the output array where the transformed short values will be stored.
     * @param srcSigned a flag indicating whether the input array should be treated as signed.
     * @param dstSigned a flag indicating whether the output array should be treated as signed.
     * @return the output array containing the transformed short values.
     * @throws NullPointerException     if the input or output array is {@code null}.
     * @throws IllegalArgumentException if the input type is unsupported or input is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static short @NonNull [] arrayToSafeShortArray(final @NonNull Object in, final short @NonNull [] out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return arrayToSafeShortArray(in, 0, out, 0, Array.getLength(in), srcSigned, dstSigned);
    }

    /**
     * Converts an input array to a safe array of short values, handling different input data types
     * and ensuring compatibility with the specified signedness.
     *
     * @param array     the input array, which must be a supported data type (e.g., byte[], short[], int[], long[], float[], or double[]).
     * @param srcSigned a flag indicating whether the input array should be treated as signed.
     * @param dstSigned a flag indicating whether the output array should be treated as signed.
     * @return the newly created output array containing the transformed short values.
     * @throws NullPointerException     if the input or output array is {@code null}.
     * @throws IllegalArgumentException if the input type is unsupported or input is not an array.
     */
    @Contract("_, _, _ -> new")
    public static short @NonNull [] arrayToSafeShortArray(final @NonNull Object array, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToSafeShortArray(array, 0, new short[length], 0, length, srcSigned, dstSigned);
    }

    /**
     * Converts an input array to a safe array of int values, handling different input data types
     * and ensuring compatibility with the specified signedness. The method processes a portion
     * of the input array starting at the specified offset and writes the result into the
     * specified portion of the output array.
     *
     * @param in        the input array, which must be a supported data type (e.g., byte[], short[], int[], long[], float[], or double[]).
     * @param inOffset  the offset within the input array to start reading data from; must be non-negative.
     * @param out       the output array where the transformed int values will be stored.
     * @param outOffset the offset within the output array to start writing data to; must be non-negative.
     * @param length    the number of elements to process; must be non-negative and within bounds of both arrays.
     * @param srcSigned a flag indicating whether the input array should be treated as signed.
     * @param dstSigned a flag indicating whether the output array should be treated as signed.
     * @return the output array containing the transformed int values.
     * @throws NullPointerException      if the input or output array is {@code null}.
     * @throws IllegalArgumentException  if the input type is unsupported or input is not an array.
     * @throws IndexOutOfBoundsException if the offsets or length exceed the bounds of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static int @NonNull [] arrayToSafeIntArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToIntArray((byte[]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT, USHORT -> shortArrayToIntArray((short[]) in, inOffset, out, outOffset, length, srcSigned);
            case INT, UINT -> intArrayToSafeIntArray((int[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG, ULONG -> longArrayToSafeIntArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeIntArray((float[]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeIntArray((double[]) in, inOffset, out, outOffset, length, dstSigned);
            //default -> out;
        };
    }

    /**
     * Converts an input array to a safe array of int values, handling different input data types
     * and ensuring compatibility with the specified signedness.
     *
     * @param in        the input array, which must be a supported data type (e.g., byte[], short[], int[], long[], float[], or double[]).
     * @param out       the output array where the transformed int values will be stored.
     * @param srcSigned a flag indicating whether the input array should be treated as signed.
     * @param dstSigned a flag indicating whether the output array should be treated as signed.
     * @return the output array containing the transformed int values.
     * @throws NullPointerException     if the input or output array is {@code null}.
     * @throws IllegalArgumentException if the input type is unsupported or input is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static int @NonNull [] arrayToSafeIntArray(final @NonNull Object in, final int @NonNull [] out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return arrayToSafeIntArray(in, 0, out, 0, Array.getLength(in), srcSigned, dstSigned);
    }

    /**
     * Converts an input array to a safe array of int values, handling different input data types
     * and ensuring compatibility with the specified signedness.
     *
     * @param array     the input array, which must be a supported data type (e.g., byte[], short[], int[], long[], float[], or double[]).
     * @param srcSigned a flag indicating whether the input array should be treated as signed.
     * @param dstSigned a flag indicating whether the output array should be treated as signed.
     * @return the newly created output array containing the transformed int values.
     * @throws NullPointerException     if the input or output array is {@code null}.
     * @throws IllegalArgumentException if the input type is unsupported or input is not an array.
     */
    @Contract("_, _, _ -> new")
    public static int @NonNull [] arrayToSafeIntArray(final @NonNull Object array, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToSafeIntArray(array, 0, new int[length], 0, length, srcSigned, dstSigned);
    }

    /**
     * Converts an input array to a safe array of long values, handling different input data types
     * and ensuring compatibility with the specified signedness. The method processes a portion
     * of the input array starting at the specified offset and writes the result into the
     * specified portion of the output array.
     *
     * @param in        the input array, which must be a supported data type (e.g., byte[], short[], int[], long[], float[], or double[]).
     * @param inOffset  the offset within the input array to start reading data from; must be non-negative.
     * @param out       the output array where the transformed long values will be stored.
     * @param outOffset the offset within the output array to start writing data to; must be non-negative.
     * @param length    the number of elements to process; must be non-negative and within bounds of both arrays.
     * @param srcSigned a flag indicating whether the input array should be treated as signed.
     * @param dstSigned a flag indicating whether the output array should be treated as signed.
     * @return the output array containing the transformed long values.
     * @throws NullPointerException      if the input or output array is {@code null}.
     * @throws IllegalArgumentException  if the input type is unsupported or input is not an array.
     * @throws IndexOutOfBoundsException if the offsets or length exceed the bounds of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static long @NonNull [] arrayToSafeLongArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToLongArray((byte[]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT, USHORT -> shortArrayToLongArray((short[]) in, inOffset, out, outOffset, length, srcSigned);
            case INT, UINT -> intArrayToLongArray((int[]) in, inOffset, out, outOffset, length, srcSigned);
            case LONG, ULONG -> longArrayToSafeLongArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeLongArray((float[]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeLongArray((double[]) in, inOffset, out, outOffset, length, dstSigned);
            //default -> out;
        };
    }

    /**
     * Converts an input array to a safe array of long values, handling different input data types
     * and ensuring compatibility with the specified signedness.
     *
     * @param in        the input array, which must be a supported data type (e.g., byte[], short[], int[], long[], float[], or double[]).
     * @param out       the output array where the transformed long values will be stored.
     * @param srcSigned a flag indicating whether the input array should be treated as signed.
     * @param dstSigned a flag indicating whether the output array should be treated as signed.
     * @return the output array containing the transformed long values.
     * @throws NullPointerException     if the input or output array is {@code null}.
     * @throws IllegalArgumentException if the input type is unsupported or input is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static long @NonNull [] arrayToSafeLongArray(final @NonNull Object in, final long @NonNull [] out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return arrayToSafeLongArray(in, 0, out, 0, Array.getLength(in), srcSigned, dstSigned);
    }

    /**
     * Converts an input array to a safe array of long values, handling different input data types
     * and ensuring compatibility with the specified signedness.
     *
     * @param array     the input array, which must be a supported data type (e.g., byte[], short[], int[], long[], float[], or double[]).
     * @param srcSigned a flag indicating whether the input array should be treated as signed.
     * @param dstSigned a flag indicating whether the output array should be treated as signed.
     * @return the newly created output array containing the transformed long values.
     * @throws NullPointerException     if the input or output array is {@code null}.
     * @throws IllegalArgumentException if the input type is unsupported or input is not an array.
     */
    @Contract("_, _, _ -> new")
    public static long @NonNull [] arrayToSafeLongArray(final @NonNull Object array, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(array);
        return arrayToSafeLongArray(array, 0, new long[length], 0, length, srcSigned, dstSigned);
    }

    /**
     * Converts a source array of byte values into a destination array of the specified target data type
     * while ensuring data safety and type compatibility. The operation allows specifying the ranges
     * of source and destination arrays to be processed. Depending on the target data type, the method
     * adjusts for signed or unsigned conversion where applicable.
     *
     * @param in        The input array of byte values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param srcSigned Indicates whether the input byte values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted values are to be interpreted as signed.
     * @return The output array containing the converted values. Returns the same reference as the out parameter.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     * @throws IllegalArgumentException  If the provided output object is not an array.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static @NonNull Object byteArrayToSafeArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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

    /**
     * Converts a source array of byte values into a destination array of the specified target data type
     * while ensuring data safety and type compatibility. Depending on the target data type, the method
     * adjusts for signed or unsigned conversion where applicable.
     *
     * @param in        The input array of byte values to be converted. Cannot be {@code null}.
     * @param out       The output array where the converted values will be stored. Cannot be {@code null}.
     * @param srcSigned Indicates whether the input byte values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted values are to be interpreted as signed.
     * @return The output array containing the converted values. Returns the same reference as the out parameter.
     * @throws NullPointerException     If either the input array or output array is {@code null}.
     * @throws IllegalArgumentException If the provided output object is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static @NonNull Object byteArrayToSafeArray(final byte @NonNull [] in, final @NonNull Object out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return byteArrayToSafeArray(in, 0, out, 0, in.length, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of a byte array to a byte array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of byte values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted byte values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param length    The number of elements to convert. Must not exceed the remaining length
     *                  of the input array starting at inOffset.
     * @param srcSigned Indicates whether the input byte values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted byte values are to be interpreted as signed.
     * @return The output array containing the safely converted byte values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static byte @NonNull [] byteArrayToSafeByteArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        // same sign?
        if (srcSigned == dstSigned)
            return byteArrayToByteArray(in, inOffset, out, outOffset, length);

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte maxValue = Byte.MAX_VALUE;

        for (int i = 0; i < len; i++) {
            byte value = in[i + inOffset];

            // signed and unsigned on the other side --> need to clamp
            if (value < 0) {
                if (srcSigned)
                    value = 0;
                else
                    value = maxValue;
            }

            out[i + outOffset] = value;
        }

        return out;
    }

    /**
     * Converts a portion of a byte array to a byte array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of byte values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted byte values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param srcSigned Indicates whether the input byte values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted byte values are to be interpreted as signed.
     * @return The output array containing the safely converted byte values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] byteArrayToSafeByteArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        return byteArrayToSafeByteArray(in, inOffset, out, outOffset, in.length - inOffset, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of a byte array to a byte array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param array  The input array of byte values to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input and the output byte values are to be interpreted as signed.
     * @return The output array containing the safely converted byte values.
     * @throws NullPointerException If either the input array or the output array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static byte @NonNull [] byteArrayToSafeByteArray(final byte @NonNull [] array, final boolean signed) throws NullPointerException {
        return byteArrayToSafeByteArray(array, 0, new byte[array.length], 0, array.length, signed, signed);
    }

    /**
     * Converts a source array of short values into a destination array of the specified target data type
     * while ensuring data safety and type compatibility. The operation allows specifying the ranges
     * of source and destination arrays to be processed. Depending on the target data type, the method
     * adjusts for signed or unsigned conversion where applicable.
     *
     * @param in        The input array of short values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param srcSigned Indicates whether the input short values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted values are to be interpreted as signed.
     * @return The output array containing the converted values. Returns the same reference as the out parameter.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     * @throws IllegalArgumentException  If the provided output object is not an array.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static @NonNull Object shortArrayToSafeArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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

    /**
     * Converts a source array of short values into a destination array of the specified target data type
     * while ensuring data safety and type compatibility. Depending on the target data type, the method
     * adjusts for signed or unsigned conversion where applicable.
     *
     * @param in        The input array of short values to be converted. Cannot be {@code null}.
     * @param out       The output array where the converted values will be stored. Cannot be {@code null}.
     * @param srcSigned Indicates whether the input short values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted values are to be interpreted as signed.
     * @return The output array containing the converted values. Returns the same reference as the out parameter.
     * @throws NullPointerException     If either the input array or output array is {@code null}.
     * @throws IllegalArgumentException If the provided output object is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static @NonNull Object shortArrayToSafeArray(final short @NonNull [] in, final @NonNull Object out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return shortArrayToSafeArray(in, 0, out, 0, in.length, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of a short array to a byte array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of short values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted byte values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param length    The number of elements to convert. Must not exceed the remaining length
     *                  of the input array starting at inOffset.
     * @param srcSigned Indicates whether the input short values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted byte values are to be interpreted as signed.
     * @return The output array containing the safely converted byte values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static byte @NonNull [] shortArrayToSafeByteArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

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

            out[i + outOffset] = result;
        }

        return out;
    }

    /**
     * Converts a portion of a short array to a byte array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of short values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted byte values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param srcSigned Indicates whether the input short values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted byte values are to be interpreted as signed.
     * @return The output array containing the safely converted byte values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] shortArrayToSafeByteArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        return shortArrayToSafeByteArray(in, inOffset, out, outOffset, in.length - inOffset, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of a short array to a byte array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param array  The input array of short values to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input and the output byte values are to be interpreted as signed.
     * @return The output array containing the safely converted byte values.
     * @throws NullPointerException If either the input array or the output array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static byte @NonNull [] shortArrayToSafeByteArray(final short @NonNull [] array, final boolean signed) throws NullPointerException {
        return shortArrayToSafeByteArray(array, 0, new byte[array.length], 0, array.length, signed, signed);
    }

    /**
     * Converts a portion of a short array to a short array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of short values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted short values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param length    The number of elements to convert. Must not exceed the remaining length
     *                  of the input array starting at inOffset.
     * @param srcSigned Indicates whether the input short values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted short values are to be interpreted as signed.
     * @return The output array containing the safely converted short values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static short @NonNull [] shortArrayToSafeShortArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        // same sign?
        if (srcSigned == dstSigned)
            return shortArrayToShortArray(in, inOffset, out, outOffset, length);

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short maxValue = Short.MAX_VALUE;

        for (int i = 0; i < len; i++) {
            short value = in[i + inOffset];

            // signed and unsigned on the other side --> need to clamp
            if (value < 0) {
                if (srcSigned)
                    value = 0;
                else
                    value = maxValue;
            }

            out[i + outOffset] = value;
        }

        return out;
    }

    /**
     * Converts a portion of a short array to a short array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of short values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted short values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param srcSigned Indicates whether the input short values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted short values are to be interpreted as signed.
     * @return The output array containing the safely converted short values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static short @NonNull [] shortArrayToSafeShortArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        return shortArrayToSafeShortArray(in, inOffset, out, outOffset, in.length - inOffset, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of a short array to a short array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param array  The input array of short values to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input and the output values are to be interpreted as signed.
     * @return The output array containing the safely converted short values.
     * @throws NullPointerException If either the input array or the output array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] shortArrayToSafeShortArray(final short @NonNull [] array, final boolean signed) throws NullPointerException {
        return shortArrayToSafeShortArray(array, 0, new short[array.length], 0, array.length, signed, signed);
    }

    /**
     * Converts a source array of int values into a destination array of the specified target data type
     * while ensuring data safety and type compatibility. The operation allows specifying the ranges
     * of source and destination arrays to be processed. Depending on the target data type, the method
     * adjusts for signed or unsigned conversion where applicable.
     *
     * @param in        The input array of int values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param srcSigned Indicates whether the input int values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted values are to be interpreted as signed.
     * @return The output array containing the converted values. Returns the same reference as the out parameter.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     * @throws IllegalArgumentException  If the provided output object is not an array.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static @NonNull Object intArrayToSafeArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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

    /**
     * Converts a source array of int values into a destination array of the specified target data type
     * while ensuring data safety and type compatibility. Depending on the target data type, the method
     * adjusts for signed or unsigned conversion where applicable.
     *
     * @param in        The input array of int values to be converted. Cannot be {@code null}.
     * @param out       The output array where the converted values will be stored. Cannot be {@code null}.
     * @param srcSigned Indicates whether the input int values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted values are to be interpreted as signed.
     * @return The output array containing the converted values. Returns the same reference as the out parameter.
     * @throws NullPointerException     If either the input array or output array is {@code null}.
     * @throws IllegalArgumentException If the provided output object is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static @NonNull Object intArrayToSafeArray(final int @NonNull [] in, final @NonNull Object out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return intArrayToSafeArray(in, 0, out, 0, in.length, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of an int array to a byte array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of int values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted byte values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param length    The number of elements to convert. Must not exceed the remaining length
     *                  of the input array starting at inOffset.
     * @param srcSigned Indicates whether the input int values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted byte values are to be interpreted as signed.
     * @return The output array containing the safely converted byte values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static byte @NonNull [] intArrayToSafeByteArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

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

            out[i + outOffset] = result;
        }

        return out;
    }

    /**
     * Converts a portion of an int array to a byte array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of int values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted byte values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param srcSigned Indicates whether the input int values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted byte values are to be interpreted as signed.
     * @return The output array containing the safely converted byte values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] intArrayToSafeByteArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        return intArrayToSafeByteArray(in, inOffset, out, outOffset, in.length - inOffset, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of an int array to a byte array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param array  The input array of int values to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input and the output values are to be interpreted as signed.
     * @return The output array containing the safely converted byte values.
     * @throws NullPointerException If either the input array or the output array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static byte @NonNull [] intArrayToSafeByteArray(final int @NonNull [] array, final boolean signed) throws NullPointerException {
        return intArrayToSafeByteArray(array, 0, new byte[array.length], 0, array.length, signed, signed);
    }

    /**
     * Converts a portion of an int array to a short array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of int values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted short values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param length    The number of elements to convert. Must not exceed the remaining length
     *                  of the input array starting at inOffset.
     * @param srcSigned Indicates whether the input int values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted short values are to be interpreted as signed.
     * @return The output array containing the safely converted short values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static short @NonNull [] intArrayToSafeShortArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

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

            out[i + outOffset] = result;
        }

        return out;
    }

    /**
     * Converts a portion of an int array to a short array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of int values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted short values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param srcSigned Indicates whether the input int values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted short values are to be interpreted as signed.
     * @return The output array containing the safely converted short values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static short @NonNull [] intArrayToSafeShortArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        return intArrayToSafeShortArray(in, inOffset, out, outOffset, in.length - inOffset, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of an int array to a short array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param array  The input array of int values to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input and the output values are to be interpreted as signed.
     * @return The output array containing the safely converted short values.
     * @throws NullPointerException If either the input array or the output array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] intArrayToSafeShortArray(final int @NonNull [] array, final boolean signed) throws NullPointerException {
        return intArrayToSafeShortArray(array, 0, new short[array.length], 0, array.length, signed, signed);
    }

    /**
     * Converts a portion of an int array to an int array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of int values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted int values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param length    The number of elements to convert. Must not exceed the remaining length
     *                  of the input array starting at inOffset.
     * @param srcSigned Indicates whether the input int values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted int values are to be interpreted as signed.
     * @return The output array containing the safely converted int values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static int @NonNull [] intArrayToSafeIntArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        // same sign?
        if (srcSigned == dstSigned)
            return intArrayToIntArray(in, inOffset, out, outOffset, length);

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int maxValue = Integer.MAX_VALUE;

        for (int i = 0; i < len; i++) {
            int value = in[i + inOffset];

            // signed and unsigned on the other side --> need to clamp
            if (value < 0) {
                if (srcSigned)
                    value = 0;
                else
                    value = maxValue;
            }

            out[i + outOffset] = value;
        }

        return out;
    }

    /**
     * Converts a portion of an int array to an int array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of int values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted int values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param srcSigned Indicates whether the input int values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted int values are to be interpreted as signed.
     * @return The output array containing the safely converted int values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] intArrayToSafeIntArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        return intArrayToSafeIntArray(in, inOffset, out, outOffset, in.length - inOffset, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of an int array to an int array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param array  The input array of int values to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input and the output values are to be interpreted as signed.
     * @return The output array containing the safely converted int values.
     * @throws NullPointerException If either the input array or the output array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] intArrayToSafeIntArray(final int @NonNull [] array, final boolean signed) throws NullPointerException {
        return intArrayToSafeIntArray(array, 0, new int[array.length], 0, array.length, signed, signed);
    }

    /**
     * Converts a source array of long values into a destination array of the specified target data type
     * while ensuring data safety and type compatibility. The operation allows specifying the ranges
     * of source and destination arrays to be processed. Depending on the target data type, the method
     * adjusts for signed or unsigned conversion where applicable.
     *
     * @param in        The input array of long values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param srcSigned Indicates whether the input long values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted values are to be interpreted as signed.
     * @return The output array containing the converted values. Returns the same reference as the out parameter.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     * @throws IllegalArgumentException  If the provided output object is not an array.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static @NonNull Object longArrayToSafeArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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

    /**
     * Converts a source array of long values into a destination array of the specified target data type
     * while ensuring data safety and type compatibility. Depending on the target data type, the method
     * adjusts for signed or unsigned conversion where applicable.
     *
     * @param in        The input array of long values to be converted. Cannot be {@code null}.
     * @param out       The output array where the converted values will be stored. Cannot be {@code null}.
     * @param srcSigned Indicates whether the input long values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted values are to be interpreted as signed.
     * @return The output array containing the converted values. Returns the same reference as the out parameter.
     * @throws NullPointerException     If either the input array or output array is {@code null}.
     * @throws IllegalArgumentException If the provided output object is not an array.
     */
    @Contract("_, _, _, _ -> param2")
    public static @NonNull Object longArrayToSafeArray(final long @NonNull [] in, final @NonNull Object out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return longArrayToSafeArray(in, 0, out, 0, in.length, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of a long array to a byte array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of long values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted int values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param length    The number of elements to convert. Must not exceed the remaining length
     *                  of the input array starting at inOffset.
     * @param srcSigned Indicates whether the input long values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted byte values are to be interpreted as signed.
     * @return The output array containing the safely converted byte values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static byte @NonNull [] longArrayToSafeByteArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

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

            out[i + outOffset] = result;
        }

        return out;
    }

    /**
     * Converts a portion of a long array to a byte array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of long values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted int values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param srcSigned Indicates whether the input long values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted byte values are to be interpreted as signed.
     * @return The output array containing the safely converted byte values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] longArrayToSafeByteArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        return longArrayToSafeByteArray(in, inOffset, out, outOffset, in.length - inOffset, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of a long array to a byte array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param array  The input array of long values to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input and the output values are to be interpreted as signed.
     * @return The output array containing the safely converted byte values.
     * @throws NullPointerException If either the input array or the output array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static byte @NonNull [] longArrayToSafeByteArray(final long @NonNull [] array, final boolean signed) throws NullPointerException {
        return longArrayToSafeByteArray(array, 0, new byte[array.length], 0, array.length, signed, signed);
    }

    /**
     * Converts a portion of a long array to a short array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of long values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted int values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param length    The number of elements to convert. Must not exceed the remaining length
     *                  of the input array starting at inOffset.
     * @param srcSigned Indicates whether the input long values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted short values are to be interpreted as signed.
     * @return The output array containing the safely converted short values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static short @NonNull [] longArrayToSafeShortArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

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

            out[i + outOffset] = result;
        }

        return out;
    }

    /**
     * Converts a portion of a long array to a short array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of long values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted int values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param srcSigned Indicates whether the input long values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted short values are to be interpreted as signed.
     * @return The output array containing the safely converted short values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static short @NonNull [] longArrayToSafeShortArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        return longArrayToSafeShortArray(in, inOffset, out, outOffset, in.length - inOffset, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of a long array to a short array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param array  The input array of long values to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input and the output values are to be interpreted as signed.
     * @return The output array containing the safely converted short values.
     * @throws NullPointerException If either the input array or the output array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] longArrayToSafeShortArray(final long @NonNull [] array, final boolean signed) throws NullPointerException {
        return longArrayToSafeShortArray(array, 0, new short[array.length], 0, array.length, signed, signed);
    }

    /**
     * Converts a portion of a long array to an int array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of long values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted int values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param length    The number of elements to convert. Must not exceed the remaining length
     *                  of the input array starting at inOffset.
     * @param srcSigned Indicates whether the input long values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted int values are to be interpreted as signed.
     * @return The output array containing the safely converted int values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static int @NonNull [] longArrayToSafeIntArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

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

            out[i + outOffset] = result;
        }

        return out;
    }

    /**
     * Converts a portion of a long array to an int array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param in        The input array of long values to be converted. Must not be {@code null}.
     * @param inOffset  The starting index in the input array from where values will be read.
     *                  Must be within the valid range of input array size.
     * @param out       The output array where the converted int values will be written. Must not be {@code null}.
     * @param outOffset The starting index in the output array where values will be written.
     *                  Must be within the valid range of output array size.
     * @param srcSigned Indicates whether the input long values are to be interpreted as signed.
     * @param dstSigned Indicates whether the converted int values are to be interpreted as signed.
     * @return The output array containing the safely converted int values.
     * @throws NullPointerException      If either the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the `inOffset`, `outOffset`, or `length` is out of bounds
     *                                   for the given arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] longArrayToSafeIntArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        return longArrayToSafeIntArray(in, inOffset, out, outOffset, in.length - inOffset, srcSigned, dstSigned);
    }

    /**
     * Converts a portion of a long array to an int array while ensuring the values remain within
     * a safe range determined by specified signedness constraints for both the source and
     * destination.
     *
     * @param array  The input array of long values to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input and the output values are to be interpreted as signed.
     * @return The output array containing the safely converted int values.
     * @throws NullPointerException If either the input array or the output array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] longArrayToSafeIntArray(final long @NonNull [] array, final boolean signed) throws NullPointerException {
        return longArrayToSafeIntArray(array, 0, new int[array.length], 0, array.length, signed, signed);
    }

    /**
     * Converts a specified portion of a long array into another long array in a type-safe manner.
     * This method ensures that values are clamped appropriately when converting between signed and
     * unsigned representations, as specified by the `srcSigned` and `dstSigned` parameters.
     * If the input and output representations are the same (both signed or both unsigned), this method
     * simply copies the array contents without clamping.
     *
     * @param in        The input array of long values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the bounds of the input array.
     * @param out       The output array where the converted long values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where converted values will be stored.
     *                  Must be within the bounds of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the specified
     *                  range for both input and output arrays is valid.
     * @param srcSigned Indicates whether the input array values should be treated as signed.
     *                  If true, values are interpreted as signed.
     * @param dstSigned Indicates whether the output array values should be treated as signed.
     *                  If true, output values are clamped to the signed long range.
     * @return The output array containing the safely converted long values.
     * @throws NullPointerException      If the input array, or the output array is null.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static long @NonNull [] longArrayToSafeLongArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        if (srcSigned == dstSigned)
            return longArrayToLongArray(in, inOffset, out, outOffset, length);

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long maxValue = Long.MAX_VALUE;

        for (int i = 0; i < len; i++) {
            long value = in[i + inOffset];

            // signed and unsigned on the other side --> need to clamp
            if (value < 0) {
                if (srcSigned)
                    value = 0;
                else
                    value = maxValue;
            }

            out[i + outOffset] = value;
        }

        return out;
    }

    /**
     * Converts a specified portion of a long array into another long array in a type-safe manner.
     * This method ensures that values are clamped appropriately when converting between signed and
     * unsigned representations, as specified by the `srcSigned` and `dstSigned` parameters.
     * If the input and output representations are the same (both signed or both unsigned), this method
     * simply copies the array contents without clamping.
     *
     * @param in        The input array of long values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the bounds of the input array.
     * @param out       The output array where the converted long values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where converted values will be stored.
     *                  Must be within the bounds of the output array.
     * @param srcSigned Indicates whether the input array values should be treated as signed.
     *                  If true, values are interpreted as signed.
     * @param dstSigned Indicates whether the output array values should be treated as signed.
     *                  If true, output values are clamped to the signed long range.
     * @return The output array containing the safely converted long values.
     * @throws NullPointerException      If the input array, or the output array is null.
     * @throws IndexOutOfBoundsException If `inOffset`, or `outOffset` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] longArrayToSafeLongArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IndexOutOfBoundsException {
        return longArrayToSafeLongArray(in, inOffset, out, outOffset, in.length - inOffset, srcSigned, dstSigned);
    }

    /**
     * Converts a specified portion of a long array into another long array in a type-safe manner.
     * This method ensures that values are clamped appropriately when converting between signed and
     * unsigned representations, as specified by the `srcSigned` and `dstSigned` parameters.
     * If the input and output representations are the same (both signed or both unsigned), this method
     * simply copies the array contents without clamping.
     *
     * @param array  The input array of long values to be converted. Cannot be {@code null}.
     * @param signed Indicates whether the input array and output array values should be treated as signed.
     *               If true, values are interpreted as signed.
     * @return The output array containing the safely converted long values.
     * @throws NullPointerException If the input array, or the output array is null.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] longArrayToSafeLongArray(final long @NonNull [] array, final boolean signed) throws NullPointerException {
        return longArrayToSafeLongArray(array, 0, new long[array.length], 0, array.length, signed, signed);
    }

    /**
     * Converts a source array of float values into a destination array of the specified target data type
     * while ensuring data safety and type compatibility. The operation allows specifying the ranges
     * of source and destination arrays to be processed. Depending on the target data type, the method
     * adjusts for signed or unsigned conversion where applicable.
     *
     * @param in        The input array of float values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     * @return The output array containing the converted values. Returns the same reference as the out parameter.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     * @throws IllegalArgumentException  If the provided output object is not an array.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object floatArrayToSafeArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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

    /**
     * Converts a source array of float values into a destination array of the specified target data type
     * while ensuring data safety and type compatibility. Depending on the target data type, the method
     * adjusts for signed or unsigned conversion where applicable.
     *
     * @param in     The input array of float values to be converted. Cannot be {@code null}.
     * @param out    The output array where the converted values will be stored. Cannot be {@code null}.
     * @param signed Specifies whether the conversion should be treated as signed or unsigned.
     * @return The output array containing the converted values. Returns the same reference as the out parameter.
     * @throws NullPointerException     If either the input array or output array is {@code null}.
     * @throws IllegalArgumentException If the provided output object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object floatArrayToSafeArray(final float @NonNull [] in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return floatArrayToSafeArray(in, 0, out, 0, in.length, signed);
    }

    /**
     * Converts a specified portion of a float array to a byte array in a type-safe manner.
     * If `signed` is {@code true}, the float values will be clamped to the range of signed byte values
     * ({@link Byte#MIN_VALUE} to {@link Byte#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned byte range ({@code 0} to {@code 0xFF}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of float values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted byte values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed byte values. When {@code false},
     *                  values are clamped to the range of unsigned byte values.
     * @return The output array containing the converted byte values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] floatArrayToSafeByteArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

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

            out[i + outOffset] = result;
        }

        return out;
    }

    /**
     * Converts a specified portion of a float array to a byte array in a type-safe manner.
     * If `signed` is {@code true}, the float values will be clamped to the range of signed byte values
     * ({@link Byte#MIN_VALUE} to {@link Byte#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned byte range ({@code 0} to {@code 0xFF}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of float values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted byte values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed byte values. When {@code false},
     *                  values are clamped to the range of unsigned byte values.
     * @return The output array containing the converted byte values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, or `outOffset` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] floatArrayToSafeByteArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return floatArrayToSafeByteArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Converts a specified portion of a float array to a byte array in a type-safe manner.
     * If `signed` is {@code true}, the float values will be clamped to the range of signed byte values
     * ({@link Byte#MIN_VALUE} to {@link Byte#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned byte range ({@code 0} to {@code 0xFF}). The converted values are written
     * directly into a newly created array.
     *
     * @param array  The input array of float values to be converted. Cannot be {@code null}.
     * @param signed Specifies whether the conversion should be treated as signed or unsigned.
     *               When {@code true}, values are clamped to the range of signed short values. When {@code false},
     *               values are clamped to the range of unsigned short values.
     * @return The output array containing the converted byte values.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static byte @NonNull [] floatArrayToSafeByteArray(final float @NonNull [] array, final boolean signed) throws NullPointerException {
        return floatArrayToSafeByteArray(array, 0, new byte[array.length], 0, array.length, signed);
    }

    /**
     * Converts a specified portion of a float array to a short array in a type-safe manner.
     * If `signed` is {@code true}, the float values will be clamped to the range of signed short values
     * ({@link Short#MIN_VALUE} to {@link Short#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned short range ({@code 0} to {@code 0xFFFF}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of float values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted short values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed short values. When {@code false},
     *                  values are clamped to the range of unsigned short values.
     * @return The output array containing the converted short values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static short @NonNull [] floatArrayToSafeShortArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

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

            out[i + outOffset] = result;
        }

        return out;
    }

    /**
     * Converts a specified portion of a float array to a short array in a type-safe manner.
     * If `signed` is {@code true}, the float values will be clamped to the range of signed short values
     * ({@link Short#MIN_VALUE} to {@link Short#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned short range ({@code 0} to {@code 0xFFFF}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of float values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted short values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed short values. When {@code false},
     *                  values are clamped to the range of unsigned short values.
     * @return The output array containing the converted short values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, or `outOffset` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] floatArrayToSafeShortArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return floatArrayToSafeShortArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Converts a specified portion of a float array to a short array in a type-safe manner.
     * If `signed` is {@code true}, the float values will be clamped to the range of signed short values
     * ({@link Short#MIN_VALUE} to {@link Short#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned short range ({@code 0} to {@code 0xFFFF}). The converted values are written
     * directly into a newly created array.
     *
     * @param array  The input array of float values to be converted. Cannot be {@code null}.
     * @param signed Specifies whether the conversion should be treated as signed or unsigned.
     *               When {@code true}, values are clamped to the range of signed short values. When {@code false},
     *               values are clamped to the range of unsigned short values.
     * @return The output array containing the converted short values.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] floatArrayToSafeShortArray(final float @NonNull [] array, final boolean signed) throws NullPointerException {
        return floatArrayToSafeShortArray(array, 0, new short[array.length], 0, array.length, signed);
    }

    /**
     * Converts a specified portion of a float array to an int array in a type-safe manner.
     * If `signed` is {@code true}, the float values will be clamped to the range of signed int values
     * ({@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned int range ({@code 0} to {@code 0xFFFFFFFF}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of float values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted int values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed int values. When {@code false},
     *                  values are clamped to the range of unsigned int values.
     * @return The output array containing the converted int values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] floatArrayToSafeIntArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            // by default, value is clamped to [Integer.MIN_VALUE, Integer.MAX_VALUE] range
            for (int i = 0; i < len; i++)
                out[i + outOffset] = (int) in[i + inOffset];
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

                out[i + outOffset] = result;
            }
        }

        return out;
    }

    /**
     * Converts a specified portion of a float array to an int array in a type-safe manner.
     * If `signed` is {@code true}, the float values will be clamped to the range of signed int values
     * ({@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned int range ({@code 0} to {@code 0xFFFFFFFF}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of float values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted int values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed int values. When {@code false},
     *                  values are clamped to the range of unsigned int values.
     * @return The output array containing the converted int values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, or `outOffset` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static int @NonNull [] floatArrayToSafeIntArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return floatArrayToSafeIntArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Converts a specified portion of a float array to an int array in a type-safe manner.
     * If `signed` is {@code true}, the float values will be clamped to the range of signed int values
     * ({@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned int range ({@code 0} to {@code 0xFFFFFFFF}). The converted values are written
     * directly into a newly created array.
     *
     * @param array  The input array of float values to be converted. Cannot be {@code null}.
     * @param signed Specifies whether the conversion should be treated as signed or unsigned.
     *               When {@code true}, values are clamped to the range of signed int values. When {@code false},
     *               values are clamped to the range of unsigned int values.
     * @return The output array containing the converted int values.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] floatArrayToSafeIntArray(final float @NonNull [] array, final boolean signed) throws NullPointerException {
        return floatArrayToSafeIntArray(array, 0, new int[array.length], 0, array.length, signed);
    }

    /**
     * Converts a specified portion of a float array to a long array in a type-safe manner.
     * If `signed` is {@code true}, the float values will be clamped to the range of signed long values
     * ({@link Long#MIN_VALUE} to {@link Long#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned long range ({@code 0} to {@code 0xFFFFFFFFFFFFFFFFL}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of float values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted long values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed long values. When {@code false},
     *                  values are clamped to the range of unsigned long values.
     * @return The output array containing the converted long values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] floatArrayToSafeLongArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            // by default, value is clamped to [Long.MIN_VALUE, Long.MAX_VALUE] range
            for (int i = 0; i < len; i++)
                out[i + outOffset] = (long) in[i + inOffset];
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

                out[i + outOffset] = result;
            }
        }

        return out;
    }

    /**
     * Converts a specified portion of a float array to a long array in a type-safe manner.
     * If `signed` is {@code true}, the float values will be clamped to the range of signed long values
     * ({@link Long#MIN_VALUE} to {@link Long#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned long range ({@code 0} to {@code 0xFFFFFFFFFFFFFFFFL}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of float values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted long values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed long values. When {@code false},
     *                  values are clamped to the range of unsigned long values.
     * @return The output array containing the converted long values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, or `outOffset` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static long @NonNull [] floatArrayToSafeLongArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return floatArrayToSafeLongArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Converts a specified portion of a float array to a long array in a type-safe manner.
     * If `signed` is {@code true}, the float values will be clamped to the range of signed long values
     * ({@link Long#MIN_VALUE} to {@link Long#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned long range ({@code 0} to {@code 0xFFFFFFFFFFFFFFFFL}). The converted values are written
     * directly into a newly created array.
     *
     * @param array  The input array of float values to be converted. Cannot be {@code null}.
     * @param signed Specifies whether the conversion should be treated as signed or unsigned.
     *               When {@code true}, values are clamped to the range of signed long values. When {@code false},
     *               values are clamped to the range of unsigned long values.
     * @return The output array containing the converted long values.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] floatArrayToSafeLongArray(final float @NonNull [] array, final boolean signed) throws NullPointerException {
        return floatArrayToSafeLongArray(array, 0, new long[array.length], 0, array.length, signed);
    }

    /**
     * Converts a source array of double values into a destination array of the specified target data type
     * while ensuring data safety and type compatibility. The operation allows specifying the ranges
     * of source and destination arrays to be processed. Depending on the target data type, the method
     * adjusts for signed or unsigned conversion where applicable.
     *
     * @param in        The input array of double values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     * @return The output array containing the converted values. Returns the same reference as the out parameter.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     * @throws IllegalArgumentException  If the provided output object is not an array.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object doubleArrayToSafeArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
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

    /**
     * Converts a source array of double values into a destination array of the specified target data type
     * while ensuring data safety and type compatibility. Depending on the target data type, the method
     * adjusts for signed or unsigned conversion where applicable.
     *
     * @param in     The input array of double values to be converted. Cannot be {@code null}.
     * @param out    The output array where the converted values will be stored. Cannot be {@code null}.
     * @param signed Specifies whether the conversion should be treated as signed or unsigned.
     * @return The output array containing the converted values. Returns the same reference as the out parameter.
     * @throws NullPointerException     If either the input array or output array is {@code null}.
     * @throws IllegalArgumentException If the provided output object is not an array.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object doubleArrayToSafeArray(final double @NonNull [] in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return doubleArrayToSafeArray(in, 0, out, 0, in.length, signed);
    }

    /**
     * Converts a specified portion of a double array to a byte array in a type-safe manner.
     * If `signed` is {@code true}, the double values will be clamped to the range of signed byte values
     * ({@link Byte#MIN_VALUE} to {@link Byte#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned byte range ({@code 0} to {@code 0xFF}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of double values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted byte values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed byte values. When {@code false},
     *                  values are clamped to the range of unsigned byte values.
     * @return The output array containing the converted byte values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] doubleArrayToSafeByteArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

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

            out[i + outOffset] = result;
        }

        return out;
    }

    /**
     * Converts a specified portion of a double array to a byte array in a type-safe manner.
     * If `signed` is {@code true}, the double values will be clamped to the range of signed byte values
     * ({@link Byte#MIN_VALUE} to {@link Byte#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned byte range ({@code 0} to {@code 0xFF}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of double values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted byte values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed byte values. When {@code false},
     *                  values are clamped to the range of unsigned byte values.
     * @return The output array containing the converted byte values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, or `outOffset` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static byte @NonNull [] doubleArrayToSafeByteArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return doubleArrayToSafeByteArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Converts a specified portion of a double array to a byte array in a type-safe manner.
     * If `signed` is {@code true}, the double values will be clamped to the range of signed byte values
     * ({@link Byte#MIN_VALUE} to {@link Byte#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned byte range ({@code 0} to {@code 0xFF}). The converted values are written
     * directly into a newly created array.
     *
     * @param array  The input array of double values to be converted. Cannot be {@code null}.
     * @param signed Specifies whether the conversion should be treated as signed or unsigned.
     *               When {@code true}, values are clamped to the range of signed byte values. When {@code false},
     *               values are clamped to the range of unsigned byte values.
     * @return The output array containing the converted byte values.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static byte @NonNull [] doubleArrayToSafeByteArray(final double @NonNull [] array, final boolean signed) throws NullPointerException {
        return doubleArrayToSafeByteArray(array, 0, new byte[array.length], 0, array.length, signed);
    }

    /**
     * Converts a specified portion of a double array to a short array in a type-safe manner.
     * If `signed` is {@code true}, the double values will be clamped to the range of signed short values
     * ({@link Short#MIN_VALUE} to {@link Short#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned short range ({@code 0} to {@code 0xFFFF}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of double values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted short values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed short values. When {@code false},
     *                  values are clamped to the range of unsigned short values.
     * @return The output array containing the converted short values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static short @NonNull [] doubleArrayToSafeShortArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

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

            out[i + outOffset] = result;
        }

        return out;
    }

    /**
     * Converts a specified portion of a double array to a short array in a type-safe manner.
     * If `signed` is {@code true}, the double values will be clamped to the range of signed short values
     * ({@link Short#MIN_VALUE} to {@link Short#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned short range ({@code 0} to {@code 0xFFFF}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of double values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted short values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed short values. When {@code false},
     *                  values are clamped to the range of unsigned short values.
     * @return The output array containing the converted short values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, or `outOffset` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static short @NonNull [] doubleArrayToSafeShortArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return doubleArrayToSafeShortArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Converts a specified portion of a double array to a short array in a type-safe manner.
     * If `signed` is {@code true}, the double values will be clamped to the range of signed short values
     * ({@link Short#MIN_VALUE} to {@link Short#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned short range ({@code 0} to {@code 0xFFFF}). The converted values are written
     * directly into a newly created array.
     *
     * @param array  The input array of double values to be converted. Cannot be {@code null}.
     * @param signed Specifies whether the conversion should be treated as signed or unsigned.
     *               When {@code true}, values are clamped to the range of signed short values. When {@code false},
     *               values are clamped to the range of unsigned short values.
     * @return The output array containing the converted short values.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] doubleArrayToSafeShortArray(final double @NonNull [] array, final boolean signed) throws NullPointerException {
        return doubleArrayToSafeShortArray(array, 0, new short[array.length], 0, array.length, signed);
    }

    /**
     * Converts a specified portion of a double array to an int array in a type-safe manner.
     * If `signed` is {@code true}, the double values will be clamped to the range of signed int values
     * ({@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned int range ({@code 0} to {@code 0xFFFFFFFF}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of double values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted int values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed int values. When {@code false},
     *                  values are clamped to the range of unsigned int values.
     * @return The output array containing the converted int values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] doubleArrayToSafeIntArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            // by default, value is clamped to [Integer.MIN_VALUE, Integer.MAX_VALUE] range
            for (int i = 0; i < len; i++)
                out[i + outOffset] = (int) in[i + inOffset];
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

                out[i + outOffset] = result;
            }
        }

        return out;
    }

    /**
     * Converts a specified portion of a double array to an int array in a type-safe manner.
     * If `signed` is {@code true}, the double values will be clamped to the range of signed int values
     * ({@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned int range ({@code 0} to {@code 0xFFFFFFFF}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of double values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted int values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed int values. When {@code false},
     *                  values are clamped to the range of unsigned int values.
     * @return The output array containing the converted int values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, or `outOffset` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static int @NonNull [] doubleArrayToSafeIntArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return doubleArrayToSafeIntArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Converts a specified portion of a double array to an int array in a type-safe manner.
     * If `signed` is {@code true}, the double values will be clamped to the range of signed int values
     * ({@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned int range ({@code 0} to {@code 0xFFFFFFFF}). The converted values are written
     * directly into a newly created array.
     *
     * @param array  The input array of double values to be converted. Cannot be {@code null}.
     * @param signed Specifies whether the conversion should be treated as signed or unsigned.
     *               When {@code true}, values are clamped to the range of signed int values. When {@code false},
     *               values are clamped to the range of unsigned int values.
     * @return The output array containing the converted int values.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] doubleArrayToSafeIntArray(final double @NonNull [] array, final boolean signed) throws NullPointerException {
        return doubleArrayToSafeIntArray(array, 0, new int[array.length], 0, array.length, signed);
    }

    /**
     * Converts a specified portion of a double array to a long array in a type-safe manner.
     * If `signed` is {@code true}, the double values will be clamped to the range of signed long values
     * ({@link Long#MIN_VALUE} to {@link Long#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned long range ({@code 0} to {@code 0xFFFFFFFFFFFFFFFFL}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of double values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted long values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param length    The number of elements to convert. Must be non-negative and such that the
     *                  specified portion of the input array and the output array are valid.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed long values. When {@code false},
     *                  values are clamped to the range of unsigned long values.
     * @return The output array containing the converted long values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, `outOffset`, or `length` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] doubleArrayToSafeLongArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);

        if (signed) {
            // by default, value is clamped to [Long.MIN_VALUE, Long.MAX_VALUE] range
            for (int i = 0; i < len; i++)
                out[i + outOffset] = (long) in[i + inOffset];
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

                out[i + outOffset] = result;
            }
        }

        return out;
    }

    /**
     * Converts a specified portion of a double array to a long array in a type-safe manner.
     * If `signed` is {@code true}, the double values will be clamped to the range of signed long values
     * ({@link Long#MIN_VALUE} to {@link Long#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned long range ({@code 0} to {@code 0xFFFFFFFFFFFFFFFFL}). The converted values are written
     * directly into the provided output array.
     *
     * @param in        The input array of double values to be converted. Cannot be {@code null}.
     * @param inOffset  The starting index in the input array from which conversion begins. Must
     *                  be within the valid range of the input array.
     * @param out       The output array where the converted long values will be stored. Cannot be {@code null}.
     * @param outOffset The starting index in the output array where the converted values will be stored.
     *                  Must be within the valid range of the output array.
     * @param signed    Specifies whether the conversion should be treated as signed or unsigned.
     *                  When {@code true}, values are clamped to the range of signed long values. When {@code false},
     *                  values are clamped to the range of unsigned long values.
     * @return The output array containing the converted long values.
     * @throws NullPointerException      If either the input array or output array is {@code null}.
     * @throws IndexOutOfBoundsException If `inOffset`, or `outOffset` exceeds the bounds
     *                                   of the input or output arrays.
     */
    @Contract("_, _, _, _, _ -> param3")
    public static long @NonNull [] doubleArrayToSafeLongArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean signed) throws NullPointerException, IndexOutOfBoundsException {
        return doubleArrayToSafeLongArray(in, inOffset, out, outOffset, in.length - inOffset, signed);
    }

    /**
     * Converts a specified portion of a double array to a long array in a type-safe manner.
     * If `signed` is {@code true}, the double values will be clamped to the range of signed long values
     * ({@link Long#MIN_VALUE} to {@link Long#MAX_VALUE}). If `signed` is {@code false}, values are clamped
     * to unsigned long range ({@code 0} to {@code 0xFFFFFFFFFFFFFFFFL}). The converted values are written
     * directly into a newly created array.
     *
     * @param array  The input array of double values to be converted. Cannot be {@code null}.
     * @param signed Specifies whether the conversion should be treated as signed or unsigned.
     *               When {@code true}, values are clamped to the range of signed long values. When {@code false},
     *               values are clamped to the range of unsigned long values.
     * @return The output array containing the converted long values.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] doubleArrayToSafeLongArray(final double @NonNull [] array, final boolean signed) throws NullPointerException {
        return doubleArrayToSafeLongArray(array, 0, new long[array.length], 0, array.length, signed);
    }

    /**
     * Converts an array of elements into a formatted string representation.
     * The formatting can include options such as signed/unsigned representation,
     * hexadecimal or decimal bases, a custom separator, and significant figure rounding
     * for floating-point values.
     *
     * @param array       the input array to be converted to a string; must not be {@code null}.
     * @param signed      determines if the array elements should be treated as signed
     *                    numbers when applicable.
     * @param hexadecimal if true, formats the numeric values in base 16; otherwise, uses base 10.
     * @param separator   the string used to separate elements in the resulting string; must not be {@code null}.
     * @param size        the number of significant figures to retain for floating-point values;
     *                    use {@code -1} to disable rounding.
     * @return a formatted string representation of the array.
     * @throws NullPointerException     if the input array, or the separator is {@code null}.
     * @throws IllegalArgumentException if the input array is not of a supported type.
     */
    public static @NonNull String arrayToString(final @NonNull Object array, final boolean signed, final boolean hexadecimal, final @NonNull String separator, final @Range(from = -1, to = Integer.MAX_VALUE) int size) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(separator, "Separator cannot be null.");
        enforceGreaterOrEqual(size, -1, "Size cannot be less than -1.");
        final int len = Array.getLength(array);
        final DataType dataType = ArrayUtil.getDataType(array, signed);
        final StringBuilder result = new StringBuilder();
        final int base = hexadecimal ? 16 : 10;

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
     * Converts an array of elements into a formatted string representation.
     *
     * @param array the input array to be converted to a string; must not be {@code null}.
     * @return a formatted string representation of the array.
     * @throws NullPointerException     if the input array is {@code null}.
     * @throws IllegalArgumentException if the input array is not of a supported type.
     */
    public static @NonNull String arrayToString(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
        return arrayToString(array, false, false, ":", -1);
    }

    /**
     * Converts a delimited string into an array of a specified data type.
     *
     * @param value       The input string to be converted. Must not be {@code null} or blank.
     * @param dataType    The target data type for the resulting array. Must not be {@code null}.
     * @param hexadecimal A flag indicating whether the input string values are in
     *                    hexadecimal format. If true, values are interpreted as base-16;
     *                    otherwise, as base-10.
     * @param separator   The delimiter used to split the input string into components.
     *                    Must not be {@code null}.
     * @return An array of the specified data type, parsed from the input string. The
     * length of the array corresponds to the number of values in the input string.
     * @throws NullPointerException     If any of the provided arguments are {@code null}.
     * @throws IllegalArgumentException If the input string is blank.
     * @throws NumberFormatException    If a value in the input string cannot be parsed as
     *                                  the specified data type.
     */
    public static @NonNull Object stringToArray(final @NonNull String value, final @NonNull DataType dataType, final boolean hexadecimal, final @NonNull String separator) throws NullPointerException, IllegalArgumentException, NumberFormatException {
        Objects.requireNonNull(value, "String value cannot be null.");
        if (StringUtil.isBlank(value))
            throw new IllegalArgumentException("String value cannot be blank.");
        //return createArray(dataType, 0);

        Objects.requireNonNull(dataType, "Data type cannot be null.");
        Objects.requireNonNull(separator, "Separator cannot be null.");

        final String[] values = value.split(separator);
        final int len = values.length;
        final int base = hexadecimal ? 16 : 10;

        switch (dataType.getJavaType()) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE: {
                final byte[] result = new byte[len];

                for (int i = 0; i < len; i++)
                    result[i] = (byte) Integer.parseInt(values[i], base);

                return result;
            }

            case SHORT, USHORT: {
                final short[] result = new short[len];

                for (int i = 0; i < len; i++)
                    result[i] = (short) Integer.parseInt(values[i], base);

                return result;
            }

            case INT, UINT: {
                final int[] result = new int[len];

                for (int i = 0; i < len; i++)
                    result[i] = Integer.parseInt(values[i], base);

                return result;
            }

            case LONG, ULONG: {
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

            case DOUBLE:
            default: {
                final double[] result = new double[len];

                for (int i = 0; i < len; i++)
                    result[i] = Double.parseDouble(values[i]);

                return result;
            }
        }

        //return null;
    }

    /**
     * Converts a delimited string into an array of a specified data type.
     *
     * @param value    The input string to be converted. Must not be {@code null} or blank.
     * @param dataType The target data type for the resulting array. Must not be {@code null}.
     * @return An array of the specified data type, parsed from the input string. The
     * length of the array corresponds to the number of values in the input string.
     * @throws NullPointerException     If any of the provided arguments are {@code null}.
     * @throws IllegalArgumentException If the input string is blank or invalid for the
     *                                  specified data type.
     * @throws NumberFormatException    If a value in the input string cannot be parsed as
     *                                  the specified data type.
     */
    public static @NonNull Object stringToArray(final @NonNull String value, final @NonNull DataType dataType) throws NullPointerException, IllegalArgumentException, NumberFormatException {
        return stringToArray(value, dataType, false, ":");
    }

    /**
     * Converts an array of bytes into a boolean array where each byte is
     * interpreted as a boolean value. A byte value of {@code 0} is considered {@code false},
     * and any non-zero byte value is considered {@code true}. The result is stored
     * in the provided output boolean array starting at the specified offset.
     *
     * @param in     the input array of bytes to be converted; must not be {@code null}.
     * @param out    the output array for storing the converted boolean values; must not be {@code null}.
     * @param offset the starting index in the output array where the results should be stored;
     *               must be non-negative.
     * @return the output boolean array containing the converted boolean values.
     * @throws NullPointerException      if the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset is invalid or if the size of the output
     *                                   array is not enough to hold the results starting at
     *                                   the specified offset.
     */
    @Contract("_, _, _ -> param2")
    public static boolean @NonNull [] toBooleanArray(final byte @NonNull [] in, final boolean @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset must be non-negative.");
        final int inLen = in.length;
        final int outLen = out.length;
        if (offset == 0)
            enforceGreaterOrEqual(outLen, inLen, "Output array length must be greater than or equal to input array length (" + inLen + ").");
        else
            enforceGreaterOrEqual(outLen, offset + inLen, "Output array length must be greater than or equal to offset + input array length (" + (offset + inLen) + ").");

        for (int i = 0; i < inLen; i++)
            out[i + offset] = (in[i] != 0);

        return out;
    }

    /**
     * Converts an array of bytes into a boolean array where each byte is
     * interpreted as a boolean value. A byte value of {@code 0} is considered {@code false},
     * and any non-zero byte value is considered {@code true}. The result is stored
     * in a newly created boolean array.
     *
     * @param in the input array of bytes to be converted; must not be {@code null}.
     * @return the output boolean array containing the converted boolean values.
     * @throws NullPointerException if the input or output array is {@code null}.
     */
    @Contract("_ -> new")
    public static boolean @NonNull [] toBooleanArray(final byte @NonNull [] in) throws NullPointerException {
        return toBooleanArray(in, new boolean[in.length], 0);
    }

    /**
     * Converts a boolean array to a byte array, where each boolean value is represented
     * as a byte (1 for {@code true}, 0 for {@code false}).
     *
     * @param in     the boolean array to be converted; must not be {@code null}.
     * @param out    the byte array to store the converted values; must not be {@code null}.
     * @param offset the starting index in the output array where the converted values will be written; must be non-negative.
     * @return the byte array containing the converted values from the boolean array.
     * @throws NullPointerException      if the in or out array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset or array lengths make the operation invalid.
     */
    @Contract("_, _, _ -> param2")
    public static byte @NonNull [] toByteArray(final boolean @NonNull [] in, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceGreaterOrEqual(offset, 0, "Offset must be non-negative.");
        final int inLen = in.length;
        final int outLen = out.length;
        if (offset == 0)
            enforceGreaterOrEqual(outLen, inLen, "Output array length must be greater than or equal to input array length (" + inLen + ").");
        else
            enforceGreaterOrEqual(outLen, offset + inLen, "Output array length must be greater than or equal to offset + input array length (" + (offset + inLen) + ").");

        for (int i = 0; i < inLen; i++)
            out[i + offset] = (byte) ((in[i]) ? 1 : 0);

        return out;
    }

    /**
     * Converts a boolean array to a byte array, where each boolean value is represented
     * as a byte (1 for {@code true}, 0 for {@code false}).
     *
     * @param in the boolean array to be converted; must not be {@code null}.
     * @return the byte array containing the converted values from the boolean array.
     * @throws NullPointerException if the in array is {@code null}.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] toByteArray(final boolean @NonNull [] in) throws NullPointerException {
        return toByteArray(in, new byte[in.length], 0);
    }

    /**
     * Extracts and interleaves data from the input array into the output array.
     * The method takes elements from the input array starting at the specified input offset and copies
     * them into the output array with a step size until the specified size is reached.
     *
     * @param in        the input byte array from which data is extracted; must not be {@code null}.
     * @param inOffset  the offset in the input array from which to start extracting data; must be non-negative.
     * @param step      the step size for data extraction; must be non-negative.
     * @param out       the output byte array to be filled with extracted data; must not be {@code null}.
     * @param outOffset the offset in the output array at which to start storing extracted data; must be non-negative.
     * @param size      the number of elements to extract from the input array and store in the output array; must be non-negative.
     * @return a byte array containing the extracted and interleaved data.
     * @throws NullPointerException      if either the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException if the offsets, step, or size cause invalid access in the input or output arrays.
     */
    @Contract("_, _, _, _, _, _ -> param4")
    public static byte @NonNull [] getInterleavedData(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int step, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        enforceRange(inOffset, 0, in.length - 1, "Input offset must be between 0 and " + (in.length - 1) + ".");
        enforceGreaterOrEqual(step, 0, "Step cannot be negative.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceRange(outOffset, 0, out.length - 1, "Output offset must be between 0 and " + (out.length - 1) + ".");
        enforceGreaterOrEqual(size, 0, "Size cannot be negative.");

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < size; i++) {
            out[outOff] = in[inOff];
            inOff += step;
            outOff++;
        }

        return out;
    }

    /**
     * Extracts and interleaves data from the input array into a newly allocated array.
     * The method takes elements from the input array starting at the specified input offset and copies
     * them into the output array with a step size until the specified size is reached.
     *
     * @param in       the input byte array from which data is extracted; must not be {@code null}.
     * @param inOffset the offset in the input array from which to start extracting data; must be non-negative.
     * @param step     the step size for data extraction; must be non-negative.
     * @param size     the number of elements to extract from the input array and store in the output array; must be non-negative.
     * @return a byte array containing the extracted and interleaved data.
     * @throws NullPointerException      if either the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset, step, or size cause invalid access in the input array.
     */
    @Contract("_, _, _, _ -> new")
    public static byte @NonNull [] getInterleavedData(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int step, final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws NullPointerException, IndexOutOfBoundsException {
        return getInterleavedData(in, inOffset, step, new byte[size], 0, size);
    }

    /**
     * De-interleaves the input byte array into a separate output array based on a specified step size.
     *
     * @param in        The input byte array that contains interleaved data. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be a non-negative integer.
     * @param step      The step size defining how data is de-interleaved from the input array. Must be a non-negative integer.
     * @param out       The output byte array where de-interleaved data will be stored. Must not be {@code null}.
     * @param outOffset The starting offset in the output array where data will be written. Must be a non-negative integer.
     * @param size      The number of elements to de-interleave in each step. Must be a non-negative integer.
     * @return The output byte array containing the de-interleaved data.
     * @throws NullPointerException      If the input or output byte arrays are {@code null}.
     * @throws IndexOutOfBoundsException If the offsets or size are out of the valid range based on the array dimensions.
     */
    @Contract("_, _, _, _, _, _ -> param4")
    public static byte @NonNull [] deInterleave(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int step, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        enforceRange(inOffset, 0, in.length - 1, "Input offset must be between 0 and " + (in.length - 1) + ".");
        enforceGreaterOrEqual(step, 0, "Step cannot be negative.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        enforceRange(outOffset, 0, out.length - 1, "Output offset must be between 0 and " + (out.length - 1) + ".");
        enforceGreaterOrEqual(size, 0, "Size cannot be negative.");

        int inOff1 = inOffset;
        int outOff1 = outOffset;

        for (int j = 0; j < step; j++) {
            int inOff2 = inOff1;
            int outOff2 = outOff1;

            for (int i = 0; i < size; i++) {
                out[outOff2] = in[inOff2];
                inOff2 += step;
                outOff2++;
            }

            inOff1++;
            outOff1 += size;
        }

        return out;
    }

    /**
     * De-interleaves the input byte array into a separate output array based on a specified step size.
     *
     * @param in       The input byte array that contains interleaved data. Must not be {@code null}.
     * @param inOffset The starting offset in the input array. Must be a non-negative integer.
     * @param step     The step size defining how data is de-interleaved from the input array. Must be a non-negative integer.
     * @param size     The number of elements to de-interleave in each step. Must be a non-negative integer.
     * @return The output byte array containing the de-interleaved data.
     * @throws NullPointerException      If the input byte array is {@code null}.
     * @throws IndexOutOfBoundsException If the offset or size are out of the valid range based on the array dimensions.
     */
    @Contract("_, _, _, _ -> new")
    public static byte @NonNull [] deInterleave(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int step, final @Range(from = 0, to = Integer.MAX_VALUE) int size) throws NullPointerException, IndexOutOfBoundsException {
        return deInterleave(in, inOffset, step, new byte[size * step], 0, size);
    }
}
