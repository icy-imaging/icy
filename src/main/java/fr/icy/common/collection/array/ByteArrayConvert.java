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

import java.lang.reflect.Array;
import java.util.Objects;

import static fr.icy.common.EnforcerUtil.enforceGreaterOrEqual;
import static fr.icy.common.EnforcerUtil.enforceRange;

/**
 * This utility class provides methods to perform various operations on byte arrays, such as
 * calculating copy lengths and reading data in different formats (e.g., primitive types like int, float, etc.)
 * from specified offsets. The supported operations include handling endianness for multibyte types.
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public class ByteArrayConvert {
    /**
     * Calculates the length in bytes for a copy operation on the given array, starting from the specified offset.
     *
     * @param array  the input object, which must be an array.
     * @param offset the starting position in the array; must be within the valid range of the array.
     * @return the length in bytes for a copy operation starting from the given offset.
     * @throws NullPointerException      if the supplied array is {@code null}.
     * @throws IllegalArgumentException  if the supplied object is invalid.
     * @throws IndexOutOfBoundsException if the supplied offset is out of bounds for the array.
     */
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getCopyLengthInBytes(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return ArrayUtil.getCopyLength(array, offset) * ArrayUtil.getDataType(array).getSize();
    }

    /**
     * Calculates the length in bytes available for copying between two objects
     * based on their respective offsets. The method returns the minimum copyable
     * length between the input and output objects.
     *
     * @param in        the source object from which bytes are copied; must not be {@code null}.
     * @param inOffset  the starting offset in the source object; must be non-negative.
     * @param out       the destination object to which bytes are copied; must not be {@code null}.
     * @param outOffset the starting offset in the destination object; must be non-negative.
     * @return the length in bytes that can be safely copied between the source
     * and destination objects.
     * @throws NullPointerException      if any of the supplied objects are {@code null}.
     * @throws IllegalArgumentException  if the objects specified are invalid.
     * @throws IndexOutOfBoundsException if the supplied offset is out of bounds for the array.
     */
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getCopyLengthInBytes(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return Math.min(getCopyLengthInBytes(in, inOffset), getCopyLengthInBytes(out, outOffset));
    }

    /**
     * Determines the length, in bytes, that can be copied based on the given input and output
     * objects, offsets, and length constraints. If the length is specified as -1, the method
     * calculates the copy length using alternative logic.
     *
     * @param in        The input object from which the data is to be copied. Must not be {@code null}.
     * @param inOffset  The offset, in bytes, within the input object where the copy begins. Must be greater than or equal to 0.
     * @param out       The output object to which the data is to be copied. Must not be {@code null}.
     * @param outOffset The offset, in bytes, within the output object where the copy begins. Must be greater than or equal to 0.
     * @param length    The number of bytes to copy. Must be greater than or equal to 0.
     * @return The determined copy length in bytes. Will be greater than or equal to 0.
     * @throws NullPointerException      if any of the supplied objects are {@code null}.
     * @throws IllegalArgumentException  if the objects specified are invalid.
     * @throws IndexOutOfBoundsException if the supplied offset or length is out of bounds for the array.
     */
    static @Range(from = 0, to = Integer.MAX_VALUE) int getCopyLengthInBytes(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        enforceGreaterOrEqual(length, 0, "Length cannot be less than 0.");
        return Math.min(getCopyLengthInBytes(in, inOffset, out, outOffset), length);
    }

    /**
     * Reads a byte value from the specified array at the given offset.
     *
     * @param array  the array to read the byte from; must not be null.
     * @param offset the position in the array from which to read the byte; must be within the valid range of the array indices.
     * @return the byte value at the specified array offset.
     * @throws NullPointerException      if the array is null.
     * @throws IndexOutOfBoundsException if the offset is out of bounds.
     * @deprecated Use {@link java.lang.reflect.Array#getByte(Object, int)} or {@code array[offset]} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static byte readByte(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        enforceRange(offset, 0, array.length - 1, "Offset must be between 0 and " + (array.length - 1));
        return array[offset];
    }

    /**
     * Reads a 16-bit short value from the specified byte array at the given offset,
     * interpreting the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param array        the byte array containing the data to be read; must not be {@code null}.
     * @param offset       the starting position in the array from which to read; must be within the valid range of the array.
     * @param littleEndian a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return the 16-bit short value read from the byte array.
     * @throws NullPointerException      if the array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset is out of bounds.
     */
    public static short readShort(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final boolean littleEndian) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        enforceRange(offset, 0, array.length - 1, "Offset must be between 0 and " + (array.length - 1));
        if (littleEndian)
            return (short) (((array[offset/* + 0*/] & 0xFF)/* << 0*/) + ((array[offset + 1] & 0xFF) << 8));

        return (short) (((array[offset/* + 0*/] & 0xFF) << 8) + ((array[offset + 1] & 0xFF)/* << 0*/));
    }

    /**
     * Reads a 32-bit int value from the specified byte array at the given offset,
     * interpreting the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param array        the byte array containing the data to be read; must not be {@code null}.
     * @param offset       the starting position in the array from which to read; must be within the valid range of the array.
     * @param littleEndian a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return the 32-bit int value read from the byte array.
     * @throws NullPointerException      if the array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset is out of bounds.
     */
    public static int readInt(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final boolean littleEndian) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        enforceRange(offset, 0, array.length - 1, "Offset must be between 0 and " + (array.length - 1));
        if (littleEndian)
            return ((array[offset/* + 0*/] & 0xFF)/* << 0*/) + ((array[offset + 1] & 0xFF) << 8)
                    + ((array[offset + 2] & 0xFF) << 16) + ((array[offset + 3] & 0xFF) << 24);

        return ((array[offset/* + 0*/] & 0xFF) << 24) + ((array[offset + 1] & 0xFF) << 16)
                + ((array[offset + 2] & 0xFF) << 8) + ((array[offset + 3] & 0xFF)/* << 0*/);
    }

    /**
     * Reads a 64-bit long value from the specified byte array at the given offset,
     * interpreting the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param array        the byte array containing the data to be read; must not be {@code null}.
     * @param offset       the starting position in the array from which to read; must be within the valid range of the array.
     * @param littleEndian a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return the 64-bit long value read from the byte array.
     * @throws NullPointerException      if the array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset is out of bounds.
     */
    public static long readLong(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final boolean littleEndian) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        enforceRange(offset, 0, array.length - 1, "Offset must be between 0 and " + (array.length - 1));
        if (littleEndian) {
            final int v1 = ((array[offset/* + 0*/] & 0xFF)/* << 0*/) + ((array[offset + 1] & 0xFF) << 8)
                    + ((array[offset + 2] & 0xFF) << 16) + ((array[offset + 3] & 0xFF) << 24);
            final int v2 = ((array[offset + 4] & 0xFF)/* << 0*/) + ((array[offset + 5] & 0xFF) << 8)
                    + ((array[offset + 6] & 0xFF) << 16) + ((array[offset + 7] & 0xFF) << 24);
            return ((v1 & 0xFFFFFFFFL)/* << 0*/) + ((v2 & 0xFFFFFFFFL) << 32);
        }

        final int v1 = ((array[offset/* + 0*/] & 0xFF) << 24) + ((array[offset + 1] & 0xFF) << 16)
                + ((array[offset + 2] & 0xFF) << 8) + ((array[offset + 3] & 0xFF)/* << 0*/);
        final int v2 = ((array[offset + 4] & 0xFF) << 24) + ((array[offset + 5] & 0xFF) << 16)
                + ((array[offset + 6] & 0xFF) << 8) + ((array[offset + 7] & 0xFF)/* << 0*/);
        return ((v1 & 0xFFFFFFFFL) << 32) + ((v2 & 0xFFFFFFFFL)/* << 0*/);
    }

    /**
     * Reads a 32-bit float value from the specified byte array at the given offset,
     * interpreting the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param array        the byte array containing the data to be read; must not be {@code null}.
     * @param offset       the starting position in the array from which to read; must be within the valid range of the array.
     * @param littleEndian a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return the 32-bit float value read from the byte array.
     * @throws NullPointerException      if the array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset is out of bounds.
     */
    public static float readFloat(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final boolean littleEndian) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        enforceRange(offset, 0, array.length - 1, "Offset must be between 0 and " + (array.length - 1));
        return Float.intBitsToFloat(readInt(array, offset, littleEndian));
    }

    /**
     * Reads a 64-bit double value from the specified byte array at the given offset,
     * interpreting the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param array        the byte array containing the data to be read; must not be {@code null}.
     * @param offset       the starting position in the array from which to read; must be within the valid range of the array.
     * @param littleEndian a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return the 64-bit double value read from the byte array.
     * @throws NullPointerException      if the array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset is out of bounds.
     */
    public static double readDouble(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final boolean littleEndian) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        enforceRange(offset, 0, array.length - 1, "Offset must be between 0 and " + (array.length - 1));
        return Double.longBitsToDouble(readLong(array, offset, littleEndian));
    }

    /**
     * Writes a byte value to the specified array at the given offset.
     *
     * @param array  the byte array where the value should be written; must not be {@code null}.
     * @param offset the position in the array where the value will be written;
     *               must be within the valid range of the array.
     * @param value  the byte value to be written to the array.
     * @throws NullPointerException      if the array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset is out of range.
     * @deprecated Use {@link java.lang.reflect.Array#setByte(Object, int, byte)} or {@code array[offset] = value} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static void writeByte(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final byte value) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        enforceRange(offset, 0, array.length - 1, "Offset must be between 0 and " + (array.length - 1));
        array[offset] = value;
    }

    /**
     * Writes a 16-bit short value into a byte array at the specified offset.
     * The byte order can be configured as little-endian or big-endian.
     *
     * @param array        the byte array where the short value will be written.
     * @param offset       the position in the array where the short value begins.
     * @param value        the 16-bit short value to write to the array.
     * @param littleEndian if true, the value is written in little-endian byte order; otherwise, big-endian.
     * @throws NullPointerException      if the array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset is out of the valid range.
     */
    public static void writeShort(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final short value, final boolean littleEndian) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        enforceRange(offset, 0, array.length - 1, "Offset must be between 0 and " + (array.length - 1));
        if (littleEndian) {
            array[offset/* + 0*/] = (byte) (value/* >> 0*/);
            array[offset + 1] = (byte) (value >> 8);
        }
        else {
            array[offset/* + 0*/] = (byte) (value >> 8);
            array[offset + 1] = (byte) (value/* >> 0*/);
        }
    }

    /**
     * Writes a 32-bit int value into a byte array at the specified offset.
     * The byte order can be configured as little-endian or big-endian.
     *
     * @param array        the byte array where the int value will be written.
     * @param offset       the position in the array where the int value begins.
     * @param value        the 32-bit int value to write to the array.
     * @param littleEndian if true, the value is written in little-endian byte order; otherwise, big-endian.
     * @throws NullPointerException      if the array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset is out of the valid range.
     */
    public static void writeInt(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final int value, final boolean littleEndian) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        enforceRange(offset, 0, array.length - 1, "Offset must be between 0 and " + (array.length - 1));
        if (littleEndian) {
            array[offset/* + 0*/] = (byte) (value/* >> 0*/);
            array[offset + 1] = (byte) (value >> 8);
            array[offset + 2] = (byte) (value >> 16);
            array[offset + 3] = (byte) (value >> 24);
        }
        else {
            array[offset/* + 0*/] = (byte) (value >> 24);
            array[offset + 1] = (byte) (value >> 16);
            array[offset + 2] = (byte) (value >> 8);
            array[offset + 3] = (byte) (value/* >> 0*/);
        }
    }

    /**
     * Writes a 64-bit long value into a byte array at the specified offset.
     * The byte order can be configured as little-endian or big-endian.
     *
     * @param array        the byte array where the long value will be written.
     * @param offset       the position in the array where the long value begins.
     * @param value        the 64-bit long value to write to the array.
     * @param littleEndian if true, the value is written in little-endian byte order; otherwise, big-endian.
     * @throws NullPointerException      if the array is {@code null}, or if the offset is out of the valid range.
     * @throws IndexOutOfBoundsException if the array is null, or if the offset is out of the valid range.
     */
    public static void writeLong(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final long value, final boolean littleEndian) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        enforceRange(offset, 0, array.length - 1, "Offset must be between 0 and " + (array.length - 1));

        int v;

        if (littleEndian) {
            v = (int) (value/* >> 0*/);
            array[offset/* + 0*/] = (byte) (v/* >> 0*/);
            array[offset + 1] = (byte) (v >> 8);
            array[offset + 2] = (byte) (v >> 16);
            array[offset + 3] = (byte) (v >> 24);
            v = (int) (value >> 32);
            array[offset + 4] = (byte) (v/* >> 0*/);
            array[offset + 5] = (byte) (v >> 8);
            array[offset + 6] = (byte) (v >> 16);
            array[offset + 7] = (byte) (v >> 24);
        }
        else {
            v = (int) (value >> 32);
            array[offset/* + 0*/] = (byte) (v >> 24);
            array[offset + 1] = (byte) (v >> 16);
            array[offset + 2] = (byte) (v >> 8);
            array[offset + 3] = (byte) (v/* >> 0*/);
            v = (int) (value/* >> 0*/);
            array[offset + 4] = (byte) (v >> 24);
            array[offset + 5] = (byte) (v >> 16);
            array[offset + 6] = (byte) (v >> 8);
            array[offset + 7] = (byte) (v/* >> 0*/);
        }
    }

    /**
     * Writes a 32-bit float value into a byte array at the specified offset.
     * The byte order can be configured as little-endian or big-endian.
     *
     * @param array        the byte array where the float value will be written.
     * @param offset       the position in the array where the float value begins.
     * @param value        the 32-bit float value to write to the array.
     * @param littleEndian if true, the value is written in little-endian byte order; otherwise, big-endian.
     * @throws NullPointerException      if the array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset is out of the valid range.
     */
    public static void writeFloat(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final float value, final boolean littleEndian) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        enforceRange(offset, 0, array.length - 1, "Offset must be between 0 and " + (array.length - 1));
        writeInt(array, offset, Float.floatToRawIntBits(value), littleEndian);
    }

    /**
     * Writes a 64-bit double value into a byte array at the specified offset.
     * The byte order can be configured as little-endian or big-endian.
     *
     * @param array        the byte array where the double value will be written.
     * @param offset       the position in the array where the double value begins.
     * @param value        the 64-bit double value to write to the array.
     * @param littleEndian if true, the value is written in little-endian byte order; otherwise, big-endian.
     * @throws NullPointerException      if the array is {@code null}.
     * @throws IndexOutOfBoundsException if the offset is out of the valid range.
     */
    public static void writeDouble(final byte @NonNull [] array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset, final double value, final boolean littleEndian) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        enforceRange(offset, 0, array.length - 1, "Offset must be between 0 and " + (array.length - 1));
        writeLong(array, offset, Double.doubleToRawLongBits(value), littleEndian);
    }

    /**
     * Copies a segment of a byte array to another byte array.
     *
     * @param in        The input byte array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be in the input array bounds.
     * @param out       The output array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array. Must be in the output array bounds.
     * @param length    The number of bytes to copy. Must be non-negative.
     * @return A byte array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException      If the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the input or output offset, or the length is out of bounds.
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

        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length);

        System.arraycopy(in, inOffset, out, outOffset, len);

        return out;
    }

    /**
     * Copies a segment of a byte array to another byte array.
     *
     * @param in  The input byte array. Must not be {@code null}.
     * @param out The output array. Must not be {@code null}.
     * @return A byte array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException If the input or output array is {@code null}.
     */
    @Contract("_, _ -> param2")
    public static byte @NonNull [] byteArrayToByteArray(final byte @NonNull [] in, final byte @NonNull [] out) throws NullPointerException {
        return byteArrayToByteArray(in, 0, out, 0, in.length);
    }

    /**
     * Copies a segment of a byte array to a newly created byte array.
     *
     * @param in The input byte array. Must not be {@code null}.
     * @return A byte array containing the copied bytes. Returns a newly allocated array.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_ -> new")
    public static byte @NonNull [] byteArrayToByteArray(final byte @NonNull [] in) throws NullPointerException {
        return byteArrayToByteArray(in, 0, new byte[in.length], 0, in.length);
    }

    /**
     * Copies a segment of a byte array to another short array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in        The input byte array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be in the input array bounds.
     * @param out       The output array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array. Must be in the output array bounds.
     * @param length    The number of bytes to copy. Must be non-negative.
     * @param little    a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A short array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException      If the input array or the output array is {@code null}.
     * @throws IndexOutOfBoundsException If the input offset is out of bounds or the length is negative.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static short @NonNull [] byteArrayToShortArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final short @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length) / 2;
        //final int[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len; i++) {
            out[outOff++] = readShort(in, inOff, little);
            inOff += 2;
        }

        return out;
    }

    /**
     * Copies a segment of a byte array to another short array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in     The input byte array. Must not be {@code null}.
     * @param out    The output array. Must not be {@code null}.
     * @param little a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A short array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException If the input array or the output array is {@code null}.
     */
    @Contract("_, _, _ -> param2")
    public static short @NonNull [] byteArrayToShortArray(final byte @NonNull [] in, final short @NonNull [] out, final boolean little) throws NullPointerException {
        return byteArrayToShortArray(in, 0, out, 0, in.length, little);
    }

    /**
     * Copies a segment of a byte array to a newly created short array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in     The input byte array. Must not be {@code null}.
     * @param little a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A short array containing the copied bytes. Returns a newly allocated array.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static short @NonNull [] byteArrayToShortArray(final byte @NonNull [] in, final boolean little) throws NullPointerException {
        return byteArrayToShortArray(in, 0, new short[in.length], 0, in.length, little); // TODO: check if size is correct for the new array. (may be divided by 2 ?)
    }

    /**
     * Copies a segment of a byte array to another int array or creates a new array if the output array is null.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in        The input byte array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be in the input array bounds.
     * @param out       The output array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array. Must be in the output array bounds.
     * @param length    The number of bytes to copy. Must be non-negative.
     * @param little    a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return An int array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException      If the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offsets are out of bounds or the length is negative.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static int @NonNull [] byteArrayToIntArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final int @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length) / 4;
        //final int[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len; i++) {
            out[outOff++] = readInt(in, inOff, little);
            inOff += 4;
        }

        return out;
    }

    /**
     * Copies a segment of a byte array to another int array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in     The input byte array. Must not be {@code null}.
     * @param out    The output array. Must not be {@code null}.
     * @param little a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return An int array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException If either the input or output array is {@code null}.
     */
    @Contract("_, _, _ -> param2")
    public static int @NonNull [] byteArrayToIntArray(final byte @NonNull [] in, final int @NonNull [] out, final boolean little) throws NullPointerException {
        return byteArrayToIntArray(in, 0, out, 0, in.length, little);
    }

    /**
     * Copies a segment of a byte array to a newly created int array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in     The input byte array. Must not be {@code null}.
     * @param little a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return An int array containing the copied bytes. Returns a newly allocated array.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static int @NonNull [] byteArrayToIntArray(final byte @NonNull [] in, final boolean little) throws NullPointerException {
        return byteArrayToIntArray(in, 0, new int[in.length], 0, in.length, little); // TODO: check if size is correct for the new array. (may be divided by 4 ?)
    }

    /**
     * Copies a segment of a byte array to another long array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in        The input byte array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be in the input array bounds.
     * @param out       The output array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array. Must be in the output array bounds.
     * @param length    The number of bytes to copy. Must be non-negative.
     * @param little    a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A long array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException      If either the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offsets are out of bounds or the length is negative.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static long @NonNull [] byteArrayToLongArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final long @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length) / 8;
        //final long[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len; i++) {
            out[outOff++] = readLong(in, inOff, little);
            inOff += 8;
        }

        return out;
    }

    /**
     * Copies a segment of a byte array to another long array or creates a new array if the output array is null.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in     The input byte array. Must not be {@code null}.
     * @param out    The output array. Must not be {@code null}.
     * @param little a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A long array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException If either the input or output array is {@code null}.
     */
    @Contract("_, _, _ -> param2")
    public static long @NonNull [] byteArrayToLongArray(final byte @NonNull [] in, final long @NonNull [] out, final boolean little) throws NullPointerException {
        return byteArrayToLongArray(in, 0, out, 0, in.length, little);
    }

    /**
     * Copies a segment of a byte array to a newly created long array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in     The input byte array. Must not be {@code null}.
     * @param little a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A long array containing the copied bytes. Returns a newly allocated array.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static long @NonNull [] byteArrayToLongArray(final byte @NonNull [] in, final boolean little) throws NullPointerException {
        return byteArrayToLongArray(in, 0, new long[in.length], 0, in.length, little); // TODO: check if size is correct for the new array. (may be divided by 8 ?)
    }

    /**
     * Copies a segment of a byte array to another float array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in        The input byte array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be in the input array bounds.
     * @param out       The output array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array. Must be in the output array bounds.
     * @param length    The number of bytes to copy. Must be non-negative.
     * @param little    a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A float array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException      If either the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offsets are out of bounds or the length is negative.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static float @NonNull [] byteArrayToFloatArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final float @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length) / 4;
        //final float[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len; i++) {
            out[outOff++] = readFloat(in, inOff, little);
            inOff += 4;
        }

        return out;
    }

    /**
     * Copies a segment of a byte array to another float array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in     The input byte array. Must not be {@code null}.
     * @param out    The output array. Must not be {@code null}.
     * @param little a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A float array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException If either the input or output array is {@code null}.
     */
    @Contract("_, _, _ -> param2")
    public static float @NonNull [] byteArrayToFloatArray(final byte @NonNull [] in, final float @NonNull [] out, final boolean little) throws NullPointerException {
        return byteArrayToFloatArray(in, 0, out, 0, in.length, little);
    }

    /**
     * Copies a segment of a byte array to a newly created float array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in     The input byte array. Must not be {@code null}.
     * @param little a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A float array containing the copied bytes. Returns a newly allocated array.
     * @throws NullPointerException If the input array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static float @NonNull [] byteArrayToFloatArray(final byte @NonNull [] in, final boolean little) throws NullPointerException {
        return byteArrayToFloatArray(in, 0, new float[in.length], 0, in.length, little); // TODO: check if size is correct for the new array. (may be divided by 4 ?)
    }

    /**
     * Copies a segment of a byte array to another double array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in        The input byte array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be in the input array bounds.
     * @param out       The output array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array. Must be in the output array bounds.
     * @param length    The number of bytes to copy. Must be non-negative.
     * @param little    a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A double array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException      If either the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offsets are out of bounds or the length is negative.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static double @NonNull [] byteArrayToDoubleArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final double @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length) / 8;
        //final double[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len; i++) {
            out[outOff++] = readDouble(in, inOff, little);
            inOff += 8;
        }

        return out;
    }

    /**
     * Copies a segment of a byte array to another double array or creates a new array if the output array is null.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in     The input byte array. Must not be {@code null}.
     * @param out    The output array. Must not be {@code null}.
     * @param little a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A double array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException If either the input or output array is {@code null}.
     */
    @Contract("_, _, _ -> param2")
    public static double @NonNull [] byteArrayToDoubleArray(final byte @NonNull [] in, final double @NonNull [] out, final boolean little) throws NullPointerException {
        return byteArrayToDoubleArray(in, 0, out, 0, in.length, little);
    }

    /**
     * Copies a segment of a byte array to a newly created double array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in     The input byte array. Must not be {@code null}.
     * @param little a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A double array containing the copied bytes. Returns a newly allocated array.
     * @throws NullPointerException If either the input or output array is {@code null}.
     */
    @Contract("_, _ -> new")
    public static double @NonNull [] byteArrayToDoubleArray(final byte @NonNull [] in, final boolean little) throws NullPointerException {
        return byteArrayToDoubleArray(in, 0, new double[in.length], 0, in.length, little); // TODO: check if size is correct for the new array. (may be divided by 4 ?)
    }

    /**
     * Copies a segment of a short array to another byte array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in        The input array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be in the input array bounds.
     * @param out       The output array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array. Must be in the output array bounds.
     * @param length    The number of bytes to copy. Must be non-negative.
     * @param little    a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A byte array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException      If either the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offsets are out of bounds or the length is negative.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] shortArrayToByteArray(final short @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length);
        //final byte[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len / 2; i++) {
            writeShort(out, outOff, in[inOff++], little);
            outOff += 2;
        }

        return out;
    }

    /**
     * Copies a segment of an int array to another byte array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in        The input array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be in the input array bounds.
     * @param out       The output array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array. Must be in the output array bounds.
     * @param length    The number of bytes to copy. Must be non-negative.
     * @param little    a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A byte array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException      If either the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offsets are out of bounds or the length is negative.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] intArrayToByteArray(final int @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length);
        //final byte[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len / 4; i++) {
            writeInt(out, outOff, in[inOff++], little);
            outOff += 4;
        }

        return out;
    }

    /**
     * Copies a segment of a long array to another byte array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in        The input array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be in the input array bounds.
     * @param out       The output array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array. Must be in the output array bounds.
     * @param length    The number of bytes to copy. Must be non-negative.
     * @param little    a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A byte array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException      If either the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offsets are out of bounds or the length is negative.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] longArrayToByteArray(final long @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length);
        //final byte[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len / 8; i++) {
            writeLong(out, outOff, in[inOff++], little);
            outOff += 8;
        }

        return out;
    }

    /**
     * Copies a segment of a float array to another byte array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in        The input array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be in the input array bounds.
     * @param out       The output array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array. Must be in the output array bounds.
     * @param length    The number of bytes to copy. Must be on-negative.
     * @param little    a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A byte array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException      If either the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offsets are out of bounds or the length is negative.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] floatArrayToByteArray(final float @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length);
        //final byte[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len / 4; i++) {
            writeFloat(out, outOff, in[inOff++], little);
            outOff += 4;
        }

        return out;
    }

    /**
     * Copies a segment of a double array to another byte array.
     * This method interprets the bytes as either little-endian or big-endian based on the provided flag.
     *
     * @param in        The input array. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be in the input array bounds.
     * @param out       The output array. Must not be {@code null}.
     * @param outOffset The starting offset in the output array. Must be in the output array bounds.
     * @param length    The number of bytes to copy. Must be non-negative.
     * @param little    a flag indicating whether the bytes should be interpreted in little-endian order; if false, big-endian is used.
     * @return A byte array containing the copied bytes. Returns the provided output array.
     * @throws NullPointerException      If either the input or output array is {@code null}.
     * @throws IndexOutOfBoundsException If the offsets are out of bounds or the length is negative.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static byte @NonNull [] doubleArrayToByteArray(final double @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array cannot be null.");
        Objects.requireNonNull(out, "Output array cannot be null.");
        final int inLen = in.length;
        final int outLen = out.length;
        enforceRange(inOffset, 0, inLen - 1, "Input offset must be between 0 and " + (inLen - 1) + ".");
        enforceRange(length, 0, inLen - inOffset, "Length must be between 0 and " + (inLen - inOffset) + ".");
        enforceRange(outOffset, 0, outLen - 1, "Output offset must be between 0 and " + (outLen - 1) + ".");

        final int len = getCopyLengthInBytes(in, inOffset, out, outOffset, length);
        //final byte[] result = Array1DUtil.allocIfNull(out, outOffset + len);

        int inOff = inOffset;
        int outOff = outOffset;

        for (int i = 0; i < len / 8; i++) {
            writeDouble(out, outOff, in[inOff++], little);
            outOff += 8;
        }

        return out;
    }

    /**
     * Converts a byte array into another array of a specified type, supporting multiple data types.
     * The method supports conversion to arrays of primitive types such as byte, short, int, long, float, and double.
     * It also allows specifying offsets in both the input and output arrays, as well as the byte order for multibyte types.
     *
     * @param in        The input byte array to be converted.
     * @param inOffset  The starting offset in the input array from which to begin reading data.
     * @param out       The output array where the converted data will be written. Its type determines the resulting data type.
     * @param outOffset The starting offset in the output array where the converted data will be written.
     * @param length    The number of elements to convert. Must be non-negative.
     * @param little    A flag indicating whether the input data should be interpreted in little-endian byte order. If {@code false}, big-endian order is used.
     * @return The same output array passed as the {@code out} parameter, containing the converted data.
     * @throws NullPointerException      If either the input or output array is {@code null}.
     * @throws IllegalArgumentException  If the provided objects are invalid.
     * @throws IndexOutOfBoundsException If the offsets are out of bounds or the length is negative.
     */
    public static @NonNull Object byteArrayToArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(out).getJavaType()) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToByteArray(in, inOffset, (byte[]) out, outOffset, length);
            case SHORT, USHORT -> byteArrayToShortArray(in, inOffset, (short[]) out, outOffset, length, little);
            case INT, UINT -> byteArrayToIntArray(in, inOffset, (int[]) out, outOffset, length, little);
            case LONG, ULONG -> byteArrayToLongArray(in, inOffset, (long[]) out, outOffset, length, little);
            case FLOAT -> byteArrayToFloatArray(in, inOffset, (float[]) out, outOffset, length, little);
            case DOUBLE -> byteArrayToDoubleArray(in, inOffset, (double[]) out, outOffset, length, little);
            //default -> null;
        };
    }

    /**
     * Converts a byte array into another array of a specified type, supporting multiple data types.
     * The method supports conversion to arrays of primitive types such as byte, short, int, long, float, and double.
     *
     * @param in     The input byte array to be converted.
     * @param out    The output array where the converted data will be written. Its type determines the resulting data type.
     * @param little A flag indicating whether the input data should be interpreted in little-endian byte order. If {@code false}, big-endian order is used.
     * @return The same output array passed as the {@code out} parameter, containing the converted data.
     * @throws NullPointerException     If either the input or output array is {@code null}.
     * @throws IllegalArgumentException If the provided objects are invalid.
     */
    public static @NonNull Object byteArrayToArray(final byte @NonNull [] in, final @NonNull Object out, final boolean little) throws NullPointerException, IllegalArgumentException {
        return byteArrayToArray(in, 0, out, 0, in.length, little);
    }

    /**
     * Converts a byte array into an array of the specified data type.
     *
     * @param in          The input byte array to be converted. Must not be {@code null}.
     * @param inOffset    The starting offset in the input byte array. Must be a non-negative value.
     * @param outDataType The data type of the output array. Must not be {@code null}.
     * @param outOffset   The starting offset for the output array. Must be a non-negative value.
     * @param length      The number of elements to convert. Must be non-negative.
     * @param little      Indicates whether the input byte array is in little-endian format.
     * @return An array of the specified data type containing the converted values.
     * @throws NullPointerException      If either the input byte array or the output data type is {@code null}.
     * @throws IndexOutOfBoundsException If the offsets are out of bounds or the length is negative.
     */
    public static @NonNull Object byteArrayToArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull DataType outDataType, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(outDataType, "Datatype cannot be null.");
        enforceRange(length, 0, in.length, "Length is out of bounds.");
        return switch (outDataType.getJavaType()) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToByteArray(in, inOffset, new byte[length + outOffset], outOffset, length);
            case SHORT, USHORT -> byteArrayToShortArray(in, inOffset, new short[length + outOffset], outOffset, length, little);
            case INT, UINT -> byteArrayToIntArray(in, inOffset, new int[length + outOffset], outOffset, length, little);
            case LONG, ULONG -> byteArrayToLongArray(in, inOffset, new long[length + outOffset], outOffset, length, little);
            case FLOAT -> byteArrayToFloatArray(in, inOffset, new float[length + outOffset], outOffset, length, little);
            case DOUBLE -> byteArrayToDoubleArray(in, inOffset, new double[length + outOffset], outOffset, length, little);
            //default -> null;
        };
    }

    /**
     * Converts a byte array into an array of the specified data type.
     *
     * @param in          The input byte array to be converted. Must not be {@code null}.
     * @param inOffset    The starting offset in the input byte array. Must be a non-negative value.
     * @param outDataType The data type of the output array. Must not be {@code null}.
     * @param length      The number of elements to convert. Must be non-negative.
     * @param little      Indicates whether the input byte array is in little-endian format.
     * @return An array of the specified data type containing the converted values.
     * @throws NullPointerException      If either the input byte array or the output data type is {@code null}.
     * @throws IndexOutOfBoundsException If the offset is out of bounds or the length is negative.
     */
    public static @NonNull Object byteArrayToArray(final byte @NonNull [] in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull DataType outDataType, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IndexOutOfBoundsException {
        return byteArrayToArray(in, inOffset, outDataType, 0, length, little);
    }

    /**
     * Converts a byte array into an array of the specified data type.
     *
     * @param in          The input byte array to be converted. Must not be {@code null}.
     * @param outDataType The data type of the output array. Must not be {@code null}.
     * @param little      Indicates whether the input byte array is in little-endian format.
     * @return An array of the specified data type containing the converted values.
     * @throws NullPointerException If either the input byte array or the output data type is {@code null}.
     */
    public static @NonNull Object byteArrayToArray(final byte @NonNull [] in, final @NonNull DataType outDataType, final boolean little) throws NullPointerException {
        return byteArrayToArray(in, 0, outDataType, 0, in.length, little);
    }

    /**
     * Converts an input array of primitive data types into a byte array with configurable options.
     *
     * @param in        the input object, which must be an array of a supported primitive data type
     *                  (e.g., byte[], short[], int[], long[], float[], double[]).
     * @param inOffset  the starting position in the input array from which data will be read.
     * @param out       a pre-allocated output byte array where the converted data will be stored.
     * @param outOffset the starting position in the output array where the converted data will be written.
     * @param length    the number of elements to convert. Must be non-negative.
     * @param little    a boolean specifying the endianness. If {@code true}, the data will be converted using little-endian order;
     *                  otherwise, big-endian order will be used.
     * @return a byte array that contains the converted data.
     * @throws NullPointerException      if either the input array or the output array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is invalid.
     * @throws IndexOutOfBoundsException if the offsets are out of bounds or if the length is negative.
     */
    public static byte @NonNull [] toByteArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return switch (ArrayUtil.getDataType(in)) { // FIXME: Remove unsigned cases
            case BYTE, UBYTE -> byteArrayToByteArray((byte[]) in, inOffset, out, outOffset, length);
            case SHORT, USHORT -> shortArrayToByteArray((short[]) in, inOffset, out, outOffset, length, little);
            case INT, UINT -> intArrayToByteArray((int[]) in, inOffset, out, outOffset, length, little);
            case LONG, ULONG -> longArrayToByteArray((long[]) in, inOffset, out, outOffset, length, little);
            case FLOAT -> floatArrayToByteArray((float[]) in, inOffset, out, outOffset, length, little);
            case DOUBLE -> doubleArrayToByteArray((double[]) in, inOffset, out, outOffset, length, little);
            //default -> out;
        };
    }

    /**
     * Converts an input array of primitive data types into a byte array with configurable options.
     *
     * @param in        the input object, which must be an array of a supported primitive data type
     *                  (e.g., byte[], short[], int[], long[], float[], double[]).
     * @param inOffset  the starting position in the input array from which data will be read.
     * @param out       a pre-allocated output byte array where the converted data will be stored.
     * @param outOffset the starting position in the output array where the converted data will be written.
     * @param little    a boolean specifying the endianness. If {@code true}, the data will be converted using little-endian order;
     *                  otherwise, big-endian order will be used.
     * @return a byte array that contains the converted data.
     * @throws NullPointerException      if either the input array or the output array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is invalid.
     * @throws IndexOutOfBoundsException if the offsets are out of bounds.
     */
    public static byte @NonNull [] toByteArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final byte @NonNull [] out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final boolean little) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return toByteArray(in, inOffset, out, outOffset, Array.getLength(in), little);
    }

    /**
     * Converts an input array of primitive data types into a byte array with configurable options.
     *
     * @param in       the input object, which must be an array of a supported primitive data type
     *                 (e.g., byte[], short[], int[], long[], float[], double[]).
     * @param inOffset the starting position in the input array from which data will be read.
     * @param length   the number of elements to convert. Must be non-negative.
     * @param little   a boolean specifying the endianness. If {@code true}, the data will be converted using little-endian order;
     *                 otherwise, big-endian order will be used.
     * @return a byte array that contains the converted data.
     * @throws NullPointerException      if either the input array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is invalid.
     * @throws IndexOutOfBoundsException if the offsets are out of bounds or if the length is negative.
     */
    public static byte @NonNull [] toByteArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean little) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        return toByteArray(in, inOffset, new byte[length], 0, length, little);
    }

    /**
     * Converts an input array of primitive data types into a byte array with configurable options.
     *
     * @param in     the input object, which must be an array of a supported primitive data type
     *               (e.g., byte[], short[], int[], long[], float[], double[]).
     * @param out    an optional pre-allocated output byte array where the converted data will be stored.
     * @param little a boolean specifying the endianness. If {@code true}, the data will be converted using little-endian order;
     *               otherwise, big-endian order will be used.
     * @return a byte array that contains the converted data.
     * @throws NullPointerException     if either the input array or the output array is {@code null}.
     * @throws IllegalArgumentException if the provided object is invalid.
     */
    public static byte @NonNull [] toByteArray(final @NonNull Object in, final byte @NonNull [] out, final boolean little) throws NullPointerException, IllegalArgumentException {
        return toByteArray(in, 0, out, 0, Array.getLength(in), little);
    }

    /**
     * Converts an input array of primitive data types into a byte array with configurable options.
     *
     * @param in     the input object, which must be an array of a supported primitive data type
     *               (e.g., byte[], short[], int[], long[], float[], double[]).
     * @param little a boolean specifying the endianness. If {@code true}, the data will be converted using little-endian order;
     *               otherwise, big-endian order will be used.
     * @return a byte array that contains the converted data.
     * @throws NullPointerException     if the input array is {@code null}.
     * @throws IllegalArgumentException if the provided object is invalid.
     */
    public static byte @NonNull [] toByteArray(final @NonNull Object in, final boolean little) throws NullPointerException, IllegalArgumentException {
        final int length = Array.getLength(in);
        return toByteArray(in, 0, new byte[length], 0, length, little);
    }
}
