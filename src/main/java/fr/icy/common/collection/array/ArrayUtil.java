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
import org.jetbrains.annotations.ApiStatus;
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
 * Utility class that provides methods for working with arrays.
 * Includes methods to retrieve array properties, perform type conversions,
 * and perform operations such as allocation and comparisons on arrays.
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 * @see Array1DUtil
 * @see Array2DUtil
 * @see Array3DUtil
 * @see ByteArrayConvert
 */
public class ArrayUtil {
    /**
     * Analyzes the provided array object and returns a corresponding {@link ArrayType}
     * that describes the type of elements contained in the array and its dimensionality.
     *
     * @param array the array object to analyze; must not be null and must represent an array type.
     * @return a {@link ArrayType} instance representing the element type and dimensionality of the provided array.
     * @throws NullPointerException     if the array is {@code null}.
     * @throws IllegalArgumentException if the provided object is not an array.
     */
    @Contract("_ -> new")
    public static @NonNull ArrayType getArrayType(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int dim = 0;

        Class<?> arrayClass = array.getClass();
        while (arrayClass.isArray()) {
            dim++;
            arrayClass = arrayClass.getComponentType();
        }

        enforceGreaterOrEqual(dim, 1, "Parameter is not an array: " + array.getClass().getName() + ".");

        return new ArrayType(DataType.getDataType(arrayClass), dim);
    }

    /**
     * Determines the number of dimensions of a given array.
     *
     * @param array the array whose dimensions are to be determined;
     *              must not be null and must be an array type.
     * @return the number of dimensions of the specified array,
     * which is always a positive integer.
     * @throws NullPointerException     if the input array is null.
     * @throws IllegalArgumentException if the input is not an array type.
     */
    public static @Range(from = 1, to = Integer.MAX_VALUE) int getDim(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 0;

        Class<?> arrayClass = array.getClass();
        while (arrayClass.isArray()) {
            result++;
            arrayClass = arrayClass.getComponentType();
        }

        enforceGreaterOrEqual(result, 1, "Parameter is not an array: " + array.getClass().getName() + ".");

        return result;
    }

    /**
     * Determines the {@link DataType} of the given array object.
     * Traverses through the dimensions of the array to find the base component type
     * and maps it to a corresponding {@link DataType}.
     *
     * @param array the array object whose base data type is to be determined; must not be null
     * @return the {@link DataType} corresponding to the base component type of the array
     * @throws NullPointerException     if the array object is null
     * @throws IllegalArgumentException if the array object is not an array
     */
    public static @NonNull DataType getDataType(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(array, "Array cannot be null.");
        Class<?> arrayClass = array.getClass();
        if (!arrayClass.isArray())
            throw new IllegalArgumentException("Array must be an array.");

        while (arrayClass.isArray())
            arrayClass = arrayClass.getComponentType();

        return DataType.getDataType(arrayClass);
    }

    /**
     * Determines the {@link DataType} of the given array and modifies it to represent unsigned values
     * if the {@code signed} parameter is set to {@code false}.
     *
     * @param array  the input object representing an array; must not be null
     * @param signed a boolean flag indicating whether the data type should represent signed values
     *               ({@code true} for signed, {@code false} for unsigned)
     * @return the {@link DataType} representation of the input array; if {@code signed} is false,
     * the unsigned equivalent of the {@link DataType} is returned for applicable types
     * @throws NullPointerException     if the {@code array} is null
     * @throws IllegalArgumentException if the {@code array} is not a valid array or if the data type cannot be determined*
     */
    public static @NonNull DataType getDataType(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final DataType result = getDataType(array);

        if (signed)
            return result;

        return switch (result) {
            case BYTE -> DataType.UBYTE;
            case SHORT -> DataType.USHORT;
            case INT -> DataType.UINT;
            case LONG -> DataType.ULONG;
            default -> result;
        };
    }

    /**
     * Returns the length of the specified array.
     *
     * @param array the array whose length is to be determined. Must not be null.
     * @return the length of the specified array.
     * @throws NullPointerException     if the provided array is null.
     * @throws IllegalArgumentException if the provided object is not a valid array.
     * @deprecated Use {@link Array#getLength(Object)}
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getLength(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(array, "Array cannot be null.");
        return Array.getLength(array);
    }

    /**
     * Calculates the total length of a multidimensional array.
     * This method computes the product of lengths across all dimensions of the array.
     *
     * @param array the array object whose total length is to be calculated. Must not be null.
     * @return the total number of elements in the array, calculated by multiplying the sizes of all dimensions.
     * @throws IllegalArgumentException if the provided object is not an array.
     * @throws NullPointerException     if the provided array is null.
     */
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getTotalLength(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(array, "Array cannot be null.");
        int result = 1;
        Object subArray = array;

        Class<?> arrayClass = array.getClass();
        if (!arrayClass.isArray())
            throw new IllegalArgumentException("Provided object is not an array.");
        while (arrayClass.isArray()) {
            result *= Array.getLength(subArray);

            arrayClass = arrayClass.getComponentType();
            if (result > 0)
                subArray = Array.get(array, 0);
        }

        return result;
    }

    /**
     * Creates and returns a multidimensional array of the specified type, dimensions, and length.
     *
     * @param dataType the data type of the array elements. Must not be {@code null}.
     * @param dim      the number of dimensions for the array. Must be 1 or greater.
     * @param len      the size of the first dimension. Must be 0 or greater.
     * @return a multidimensional array of the specified type and dimensions.
     * @throws NullPointerException      if dataType is {@code null}.
     * @throws IndexOutOfBoundsException if dim is less than 1 or len is negative.
     */
    public static @NonNull Object createArray(final @NonNull DataType dataType, final @Range(from = 1, to = Integer.MAX_VALUE) int dim, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(dataType, "DataType cannot be null.");
        enforceGreaterOrEqual(dim, 1, "Dimension cannot be less than 1.");
        enforceGreaterOrEqual(len, 0, "Length cannot be negative.");
        final int[] dims = new int[dim];
        dims[0] = len;
        return Array.newInstance(dataType.toPrimitiveClass(), dims);
    }

    /**
     * Creates and returns a multidimensional array of the specified type and length.
     *
     * @param arrayType the array type of the array elements. Must not be {@code null}.
     * @param len       the size of the first dimension. Must be 0 or greater.
     * @return a multidimensional array of the specified type and length.
     * @throws NullPointerException      if arrayType is {@code null}.
     * @throws IndexOutOfBoundsException if len is negative.
     */
    public static @NonNull Object createArray(final @NonNull ArrayType arrayType, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IndexOutOfBoundsException {
        Objects.requireNonNull(arrayType, "ArrayType cannot be null.");
        return createArray(arrayType.getDataType(), arrayType.getDim(), len);
    }

    /**
     * Returns the given array if it is not null; otherwise, creates and returns a new array
     * of the specified type and length.
     *
     * @param array     the array to check for null; if null, a new array will be allocated.
     * @param arrayType the type of the array to allocate if the input array is {@code null}.
     * @param len       the length of the new array to allocate, if applicable. Must be non-negative.
     * @return the input array if it is not {@code null}, or a newly allocated array based on the specified type and length.
     * @throws NullPointerException      if arrayType is {@code null}.
     * @throws IndexOutOfBoundsException if the length is negative
     * @deprecated Use {@link #createArray(ArrayType, int)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    @Contract("!null, _, _ -> param1")
    public static @NonNull Object allocIfNull(final @Nullable Object array, final @NonNull ArrayType arrayType, final @Range(from = 0, to = Integer.MAX_VALUE) int len) throws NullPointerException, IndexOutOfBoundsException {
        if (array == null)
            return createArray(arrayType, len);

        return array;
    }

    /**
     * Encapsulates the given array into a new array with one additional dimension.
     *
     * @param array the non-null array to encapsulate.
     * @return a new non-null array with one additional dimension containing the given array as its sole element.
     * @throws NullPointerException     if the provided array is {@code null}.
     * @throws IllegalArgumentException if the provided array type is invalid or cannot be processed.
     */
    public static @NonNull Object @NonNull [] encapsulate(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
        final ArrayType type = getArrayType(array);

        // increase dim
        type.setDim(type.getDim() + 1);

        final Object[] result = (Object[]) createArray(type, 1);
        // encapsulate
        result[0] = array;

        return result;
    }

    /**
     * Compares the types of two array objects to determine if they are of the same type.
     *
     * @param array1 the first array object to compare; can be {@code null}.
     * @param array2 the second array object to compare; can be {@code null}.
     * @return {@code true} if both arrays are non-null and have the same type, otherwise {@code false}.
     * @throws IllegalArgumentException if the input objects are not arrays, or if an error occurs during type comparison.
     */
    @Contract("null, _ -> false; !null, null -> false")
    public static boolean arrayTypeCompare(final @Nullable Object array1, final @Nullable Object array2) throws IllegalArgumentException {
        if (array1 == null || array2 == null)
            return false;

        return getArrayType(array1).equals(getArrayType(array2));
    }

    /**
     * Compares two arrays for equality. This method works with single-dimensional and multidimensional arrays
     * of primitive types and their respective boxed types. It performs element-wise comparison for each dimension.
     *
     * @param array1 the first array to compare; can be {@code null}.
     * @param array2 the second array to compare; can be {@code null}.
     * @return true if the arrays are considered equal, false otherwise. Returns false if either input is {@code null},
     * or if the arrays have incompatible types or dimensions.
     * @throws IllegalArgumentException if the provided objects are not arrays.
     */
    @Contract("null, _ -> false; !null, null -> false")
    public static boolean arrayCompare(final @Nullable Object array1, final @Nullable Object array2) throws IllegalArgumentException {
        if (array1 == null || array2 == null)
            return false;

        if (array1 == array2)
            return true;

        final ArrayType type = getArrayType(array1);

        if (!type.equals(getArrayType(array2)))
            return false;

        final int dim = type.getDim();

        // more than 2 dimensions --> use generic code
        if (dim > 2) {
            final int len = Array.getLength(array1);

            if (len != Array.getLength(array2))
                return false;

            for (int i = 0; i < len; i++)
                if (!arrayCompare(Array.get(array1, i), Array.get(array2, i)))
                    return false;

            return true;
        }

        // single-dimension array
        switch (type.getDataType().getJavaType()) {
            case BYTE:
                switch (dim) {
                    case 1:
                        return Arrays.equals((byte[]) array1, (byte[]) array2);
                    case 2:
                        return Array2DUtil.arrayByteCompare((byte[][]) array1, (byte[][]) array2);
                }
                break;

            case SHORT:
                switch (dim) {
                    case 1:
                        return Arrays.equals((short[]) array1, (short[]) array2);
                    case 2:
                        return Array2DUtil.arrayShortCompare((short[][]) array1, (short[][]) array2);
                }
                break;

            case INT:
                switch (dim) {
                    case 1:
                        return Arrays.equals((int[]) array1, (int[]) array2);
                    case 2:
                        return Array2DUtil.arrayIntCompare((int[][]) array1, (int[][]) array2);
                }
                break;

            case LONG:
                switch (dim) {
                    case 1:
                        return Arrays.equals((long[]) array1, (long[]) array2);
                    case 2:
                        return Array2DUtil.arrayLongCompare((long[][]) array1, (long[][]) array2);
                }
                break;

            case FLOAT:
                switch (dim) {
                    case 1:
                        return Arrays.equals((float[]) array1, (float[]) array2);
                    case 2:
                        return Array2DUtil.arrayFloatCompare((float[][]) array1, (float[][]) array2);
                }
                break;

            case DOUBLE:
                switch (dim) {
                    case 1:
                        return Arrays.equals((double[]) array1, (double[]) array2);
                    case 2:
                        return Array2DUtil.arrayDoubleCompare((double[][]) array1, (double[][]) array2);
                }
                break;
        }

        return false;
    }

    /**
     * Converts a multidimensional array into a one-dimensional array. The input array
     * can have up to three dimensions and contains elements of a specific data type.
     * This method recursively processes each dimension and supports various primitive data types.
     *
     * @param in     the input multidimensional array; must not be {@code null}. The type of this array
     *               determines the corresponding one-dimensional array conversion.
     * @param out    the output array into which the elements will be written. Must not be {@code null}.
     * @param offset the starting index in the output array where the elements will be placed; must be
     *               non-negative.
     * @return a one-dimensional array containing all elements of the input array.
     * @throws NullPointerException          if the input array is {@code null}, or if the output array is {@code null}.
     * @throws IllegalArgumentException      if the provided objects are not arrays.
     * @throws IndexOutOfBoundsException     if the offset is invalid.
     * @throws UnsupportedOperationException if the input array cannot be flattened.
     */
    @SuppressWarnings("SuspiciousSystemArraycopy")
    @Contract("_, _, _ -> param2")
    public static @NonNull Object toArray1D(final @NonNull Object in, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException, UnsupportedOperationException {
        Objects.requireNonNull(in, "Input array must not be null.");
        Objects.requireNonNull(out, "Output array must not be null.");
        enforceRange(offset, 0, Array.getLength(out), "Offset is out of bounds.");
        final ArrayType type = getArrayType(in);
        final DataType dataType = type.getDataType();
        final int dim = type.getDim();

        // more than 3 dimensions --> use generic code
        if (dim > 3) {
            //final Object result = Array1DUtil.allocIfNull(out, dataType, offset + getTotalLength(in));

            final int len = Array.getLength(in);

            int off = offset;
            for (int i = 0; i < len; i++) {
                final Object s_in = Array.get(in, i);

                if (s_in != null) {
                    toArray1D(s_in, out, off);
                    off += Array.getLength(s_in);
                }
            }
        }

        switch (dataType.getJavaType()) {
            case BYTE:
                switch (dim) {
                    case 1:
                        System.arraycopy(in, 0, out, offset, Array.getLength(in));
                        return out;
                    //return Array1DUtil.toByteArray1D((byte[]) in, (byte[]) out, offset);
                    case 2:
                        return Array2DUtil.toByteArray1D((byte[][]) in, (byte[]) out, offset);
                    case 3:
                        return Array3DUtil.toByteArray1D((byte[][][]) in, (byte[]) out, offset);
                }
                break;

            case SHORT:
                switch (dim) {
                    case 1:
                        System.arraycopy(in, 0, out, offset, Array.getLength(in));
                        return out;
                    //return Array1DUtil.toShortArray1D((short[]) in, (short[]) out, offset);
                    case 2:
                        return Array2DUtil.toShortArray1D((short[][]) in, (short[]) out, offset);
                    case 3:
                        return Array3DUtil.toShortArray1D((short[][][]) in, (short[]) out, offset);
                }
                break;

            case INT:
                switch (dim) {
                    case 1:
                        System.arraycopy(in, 0, out, offset, Array.getLength(in));
                        return out;
                    //return Array1DUtil.toIntArray1D((int[]) in, (int[]) out, offset);
                    case 2:
                        return Array2DUtil.toIntArray1D((int[][]) in, (int[]) out, offset);
                    case 3:
                        return Array3DUtil.toIntArray1D((int[][][]) in, (int[]) out, offset);
                }
                break;

            case LONG:
                switch (dim) {
                    case 1:
                        System.arraycopy(in, 0, out, offset, Array.getLength(in));
                        return out;
                    //return Array1DUtil.toLongArray1D((long[]) in, (long[]) out, offset);
                    case 2:
                        return Array2DUtil.toLongArray1D((long[][]) in, (long[]) out, offset);
                    case 3:
                        return Array3DUtil.toLongArray1D((long[][][]) in, (long[]) out, offset);
                }
                break;

            case FLOAT:
                switch (dim) {
                    case 1:
                        System.arraycopy(in, 0, out, offset, Array.getLength(in));
                        return out;
                    //return Array1DUtil.toFloatArray1D((float[]) in, (float[]) out, offset);
                    case 2:
                        return Array2DUtil.toFloatArray1D((float[][]) in, (float[]) out, offset);
                    case 3:
                        return Array3DUtil.toFloatArray1D((float[][][]) in, (float[]) out, offset);
                }
                break;

            case DOUBLE:
                switch (dim) {
                    case 1:
                        System.arraycopy(in, 0, out, offset, Array.getLength(in));
                        return out;
                    //return Array1DUtil.toDoubleArray1D((double[]) in, (double[]) out, offset);
                    case 2:
                        return Array2DUtil.toDoubleArray1D((double[][]) in, (double[]) out, offset);
                    case 3:
                        return Array3DUtil.toDoubleArray1D((double[][][]) in, (double[]) out, offset);
                }
                break;
        }

        throw new UnsupportedOperationException("Unable to flatten array.");
        //return out;
    }

    /**
     * Converts a multidimensional array into a one-dimensional array. The input array
     * can have up to three dimensions and contains elements of a specific data type.
     * This method recursively processes each dimension and supports various primitive data types.
     *
     * @param array the input multidimensional array; must not be {@code null}. The type of this array
     *              determines the corresponding one-dimensional array conversion.
     * @return a one-dimensional array containing all elements of the input array.
     * @throws NullPointerException          if the input array is {@code null}, or if the output array is {@code null}.
     * @throws IllegalArgumentException      if the provided objects are not arrays.
     * @throws UnsupportedOperationException if the input array cannot be flattened.
     */
    @Contract("_ -> new")
    public static @NonNull Object toArray1D(final @NonNull Object array) throws NullPointerException, IllegalArgumentException, UnsupportedOperationException {
        return toArray1D(array, createArray(getArrayType(array), getTotalLength(array)), 0);
    }

    /**
     * Calculates the length of a subarray after applying an offset to the original array.
     *
     * @param array  the source array; must not be {@code null}
     * @param offset the starting position of the subarray; must be a non-negative value
     *               and within the bounds of the source array length.
     * @return the length of the subarray after the specified offset.
     * @throws NullPointerException      if the provided array is {@code null}.
     * @throws IllegalArgumentException  if the provided object is not an array.
     * @throws IndexOutOfBoundsException if the offset is out of range for the provided array.
     */
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getCopyLength(final @NonNull Object array, final @Range(from = 0, to = Integer.MAX_VALUE) int offset) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        Objects.requireNonNull(array, "Array cannot be null.");
        final int length = Array.getLength(array);
        enforceRange(offset, 0, length - 1, "Offset must be between 0 and " + (length - 1) + ".");
        return length - offset;
    }

    /**
     * Determines the length of data that can be copied based on the provided input object, input offset,
     * output object, and output offset. The method ensures that the calculated length is valid and
     * constrained by both the input and output properties.
     *
     * @param in        the input object; must not be {@code null}.
     * @param inOffset  the offset within the input object; must be a non-negative integer.
     * @param out       the output object; must not be {@code null}.
     * @param outOffset the offset within the output object; must be a non-negative integer.
     * @return the maximum length of data that can be copied between the input and output.
     * @throws NullPointerException      if the provided arrays are {@code null}.
     * @throws IllegalArgumentException  if the provided objects are not arrays.
     * @throws IndexOutOfBoundsException if the offsets are out of range for the provided arrays.
     */
    public static @Range(from = 0, to = Integer.MAX_VALUE) int getCopyLength(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        final int inLen = getCopyLength(in, inOffset);
        final int outLen = getCopyLength(out, outOffset);

        return Math.min(inLen, outLen);
    }

    /**
     * Calculates the maximum number of elements that can be safely copied between input and output objects,
     * considering the specified offsets and the desired length.
     *
     * @param in        The input object from which elements are to be copied. Must not be {@code null}.
     * @param inOffset  The starting offset in the input object. Must be greater than or equal to {@code 0}.
     * @param out       The output object to which elements shall be copied. Must not be {@code null}.
     * @param outOffset The starting offset in the output object. Must be greater than or equal to {@code 0}.
     * @param length    The desired number of elements to copy. Must not be less than {@code 0}.
     * @return The number of elements that can be safely copied, which is the minimum between the desired length
     * and the maximum possible copyable length based on the input/output objects and their offsets.
     * @throws NullPointerException      If either the input or output object is {@code null}.
     * @throws IllegalArgumentException  If the provided objects are not arrays.
     * @throws IndexOutOfBoundsException If any of the specified offsets are out of range for the input or output objects.
     */
    static @Range(from = 0, to = Integer.MAX_VALUE) int getCopyLength(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        enforceGreaterOrEqual(length, 0, "Length cannot be less than 0.");
        final int cpLen = getCopyLength(in, inOffset, out, outOffset);

        return Math.min(length, cpLen);
    }

    /**
     * Converts an input array to an output array, handling multiple data types and array dimensions.
     *
     * @param in        the input array to be converted; must not be {@code null}.
     * @param inOffset  the starting index in the input array where the conversion begins, must be non-negative.
     * @param out       the output array to store the converted values; must not be {@code null}.
     * @param outOffset the starting index in the output array where the converted values will be placed; must be non-negative
     * @param length    the number of elements to be converted; must be non-negative.
     * @param signed    specifies whether to handle the conversion as signed data when applicable
     * @return the resulting array after conversion.
     * @throws NullPointerException      if {@code in} or {@code out} is {@code null}.
     * @throws IllegalArgumentException  if parameters are invalid or violate constraints.
     * @throws IndexOutOfBoundsException if {@code inOffset} or {@code outOffset} is negative.
     */
    @Contract("_, _, _, _, _, _ -> param3")
    public static @NonNull Object arrayToArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean signed) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array must not be null.");
        Objects.requireNonNull(out, "Output array must not be null.");
        enforceRange(inOffset, 0, Array.getLength(in), "Input offset is out of bounds.");
        enforceRange(outOffset, 0, Array.getLength(out), "Output offset is out of bounds.");
        enforceGreaterOrEqual(length, 0, "Length must be non-negative.");

        final ArrayType type = getArrayType(in);
        final int dim = type.getDim();

        // more than 2 dimensions --> use generic code
        if (dim > 2) {
            final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
            //final Object result = allocIfNull(out, type, outOffset + len);

            for (int i = 0; i < len; i++)
                Array.set(
                        out,
                        i + outOffset,
                        arrayToArray(
                                Array.get(in, i + inOffset),
                                0,
                                Array.get(out, i + outOffset),
                                0,
                                Array.getLength(Array.get(in, i + inOffset)),
                                signed
                        )
                );

            return out;
        }

        switch (type.getDataType().getJavaType()) {
            case BYTE:
                switch (dim) {
                    case 1:
                        return Array1DUtil.byteArrayToArray((byte[]) in, inOffset, out, outOffset, length, signed);
                    case 2:
                        return Array2DUtil.byteArrayToArray((byte[][]) in, inOffset, out, outOffset, length, signed);
                }
                break;

            case SHORT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.shortArrayToArray((short[]) in, inOffset, out, outOffset, length, signed);
                    case 2:
                        return Array2DUtil.shortArrayToArray((short[][]) in, inOffset, out, outOffset, length, signed);
                }
                break;

            case INT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.intArrayToArray((int[]) in, inOffset, out, outOffset, length, signed);
                    case 2:
                        return Array2DUtil.intArrayToArray((int[][]) in, inOffset, out, outOffset, length, signed);
                }
                break;

            case LONG:
                switch (dim) {
                    case 1:
                        return Array1DUtil.longArrayToArray((long[]) in, inOffset, out, outOffset, length, signed);
                    case 2:
                        return Array2DUtil.longArrayToArray((long[][]) in, inOffset, out, outOffset, length, signed);
                }
                break;

            case FLOAT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.floatArrayToArray((float[]) in, inOffset, out, outOffset, length);
                    case 2:
                        return Array2DUtil.floatArrayToArray((float[][]) in, inOffset, out, outOffset, length);
                }
                break;

            case DOUBLE:
                switch (dim) {
                    case 1:
                        return Array1DUtil.doubleArrayToArray((double[]) in, inOffset, out, outOffset, length);
                    case 2:
                        return Array2DUtil.doubleArrayToArray((double[][]) in, inOffset, out, outOffset, length);
                }
                break;
        }

        return out;
    }

    /**
     * Converts an input array to an output array, handling multiple data types and array dimensions.
     *
     * @param in     the input array to be converted; must not be {@code null}.
     * @param out    the output array to store the converted values; must not be {@code null}.
     * @param signed specifies whether to handle the conversion as signed data when applicable
     * @return the resulting array after conversion.
     * @throws NullPointerException      if {@code in} or {@code out} is {@code null}.
     * @throws IllegalArgumentException  if parameters are invalid or violate constraints.
     * @throws IndexOutOfBoundsException if {@code inOffset} or {@code outOffset} is negative.
     */
    @Contract("_, _, _ -> param2")
    public static @NonNull Object arrayToArray(final @NonNull Object in, final @NonNull Object out, final boolean signed) throws NullPointerException, IllegalArgumentException {
        return arrayToArray(in, 0, out, 0, getTotalLength(in), signed);
    }

    /**
     * Converts a multidimensional array of primitive or wrapper types to a multidimensional byte array.
     * The conversion supports 1D, 2D, and higher-dimensional arrays.
     *
     * @param array the input array to be converted; must be a non-null array of a supported type.
     * @return a new byte array with the same structure as the input array.
     * @throws NullPointerException     if {@code array} is {@code null}.
     * @throws IllegalArgumentException if the input object is not valid or its dimensions cannot be determined.
     */
    public static Object arrayToByteArray(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
        final int dim = getDim(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToByteArray(array);

            case 2:
                return Array2DUtil.arrayToByteArray(array);

            default:
                // use generic code
                final int len = Array.getLength(array);
                final Object result = createArray(DataType.BYTE, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToByteArray(Array.get(array, i)));

                return result;
        }
    }

    /**
     * Converts a multidimensional array of primitive or wrapper types to a multidimensional short array.
     * The conversion supports 1D, 2D, and higher-dimensional arrays.
     *
     * @param array  the input array to be converted; must be a non-null array of a supported type.
     * @param signed a flag indicating whether signed conversion should be applied, if applicable.
     * @return a new short array with the same structure as the input array.
     * @throws NullPointerException     if {@code array} is {@code null}.
     * @throws IllegalArgumentException if the input object is not valid or its dimensions cannot be determined.
     */
    public static @NonNull Object arrayToShortArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int dim = getDim(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToShortArray(array, signed);

            case 2:
                return Array2DUtil.arrayToShortArray(array, signed);

            default:
                // use generic code
                final int len = Array.getLength(array);
                final Object result = createArray(DataType.SHORT, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToShortArray(Array.get(array, i), signed));

                return result;

        }
    }

    /**
     * Converts a multidimensional array of primitive or wrapper types to a multidimensional int array.
     * The conversion supports 1D, 2D, and higher-dimensional arrays.
     *
     * @param array  the input array to be converted; must be a non-null array of a supported type.
     * @param signed a flag indicating whether signed conversion should be applied, if applicable.
     * @return a new int array with the same structure as the input array.
     * @throws NullPointerException     if {@code array} is {@code null}.
     * @throws IllegalArgumentException if the input object is not valid or its dimensions cannot be determined.
     */
    public static @NonNull Object arrayToIntArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int dim = getDim(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToIntArray(array, signed);

            case 2:
                return Array2DUtil.arrayToIntArray(array, signed);

            default:
                // use generic code
                final int len = Array.getLength(array);
                final Object result = createArray(DataType.INT, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToIntArray(Array.get(array, i), signed));

                return result;

        }
    }

    /**
     * Converts a multidimensional array of primitive or wrapper types to a multidimensional long array.
     * The conversion supports 1D, 2D, and higher-dimensional arrays.
     *
     * @param array  the input array to be converted; must be a non-null array of a supported type.
     * @param signed a flag indicating whether signed conversion should be applied, if applicable.
     * @return a new long array with the same structure as the input array.
     * @throws NullPointerException     if {@code array} is {@code null}.
     * @throws IllegalArgumentException if the input object is not valid or its dimensions cannot be determined.
     */
    public static @NonNull Object arrayToLongArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int dim = getDim(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToIntArray(array, signed);

            case 2:
                return Array2DUtil.arrayToIntArray(array, signed);

            default:
                // use generic code
                final int len = Array.getLength(array);
                final Object result = createArray(DataType.LONG, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToLongArray(Array.get(array, i), signed));

                return result;
        }
    }

    /**
     * Converts a multidimensional array of primitive or wrapper types to a multidimensional float array.
     * The conversion supports 1D, 2D, and higher-dimensional arrays.
     *
     * @param array  the input array to be converted; must be a non-null array of a supported type.
     * @param signed a flag indicating whether signed conversion should be applied, if applicable.
     * @return a new float array with the same structure as the input array.
     * @throws NullPointerException     if {@code array} is {@code null}.
     * @throws IllegalArgumentException if the input object is not valid or its dimensions cannot be determined.
     */
    public static @NonNull Object arrayToFloatArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int dim = getDim(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToFloatArray(array, signed);

            case 2:
                return Array2DUtil.arrayToFloatArray(array, signed);

            default:
                // use generic code
                final int len = Array.getLength(array);
                final Object result = createArray(DataType.FLOAT, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToFloatArray(Array.get(array, i), signed));

                return result;
        }
    }

    /**
     * Converts a multidimensional array of primitive or wrapper types to a multidimensional double array.
     * The conversion supports 1D, 2D, and higher-dimensional arrays.
     *
     * @param array  the input array to be converted; must be a non-null array of a supported type.
     * @param signed a flag indicating whether signed conversion should be applied, if applicable.
     * @return a new double array with the same structure as the input array.
     * @throws NullPointerException     if {@code array} is {@code null}.
     * @throws IllegalArgumentException if the input object is not valid or its dimensions cannot be determined.
     */
    public static @NonNull Object arrayToDoubleArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int dim = getDim(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToDoubleArray(array, signed);

            case 2:
                return Array2DUtil.arrayToDoubleArray(array, signed);

            default:
                // use generic code
                final int len = Array.getLength(array);
                final Object result = createArray(DataType.DOUBLE, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToDoubleArray(Array.get(array, i), signed));

                return result;
        }
    }

    /**
     * Converts a source array into a "safe" array with controlled data type conversion
     * and data manipulation. Supports multidimensional arrays with various primitive
     * data types and allows truncation or extension of input data based on offsets
     * and lengths.
     *
     * @param in        The input array to be converted. Must not be {@code null}.
     * @param inOffset  The starting offset in the input array. Must be non-negative.
     * @param out       The output array where the result will be stored. Must not be {@code null}.
     * @param outOffset The starting offset in the output array. Must be non-negative.
     * @param length    The number of elements to copy or process. Must be non-negative.
     * @param srcSigned Specifies if the input array values are signed.
     * @param dstSigned Specifies if the output array values should be signed.
     * @return The resulting "safe" array converted and processed based on inputs.
     * @throws NullPointerException      If {@code in} or {@code out} is {@code null}.
     * @throws IllegalArgumentException  If any provided argument is invalid.
     * @throws IndexOutOfBoundsException If {@code inOffset}, {@code outOffset}, or {@code length} is negative.
     */
    @Contract("_, _, _, _, _, _, _ -> param3")
    public static @NonNull Object arrayToSafeArray(final @NonNull Object in, final @Range(from = 0, to = Integer.MAX_VALUE) int inOffset, final @NonNull Object out, final @Range(from = 0, to = Integer.MAX_VALUE) int outOffset, final @Range(from = 0, to = Integer.MAX_VALUE) int length, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException, IndexOutOfBoundsException {
        Objects.requireNonNull(in, "Input array must not be null.");
        Objects.requireNonNull(out, "Output array must not be null.");
        enforceRange(inOffset, 0, Array.getLength(in), "Input offset is out of bounds.");
        enforceRange(outOffset, 0, Array.getLength(out), "Output offset is out of bounds.");
        enforceGreaterOrEqual(length, 0, "Length must be non-negative.");

        final ArrayType type = getArrayType(in);
        final int dim = type.getDim();

        // more than 2 dimensions --> use generic code
        if (dim > 2) {
            final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
            //final Object result = allocIfNull(out, type, outOffset + len);

            for (int i = 0; i < len; i++)
                Array.set(
                        out,
                        i + outOffset,
                        arrayToSafeArray(
                                Array.get(in, i + inOffset),
                                0,
                                Array.get(out, i + outOffset),
                                0,
                                Array.getLength(Array.get(in, i + inOffset)),
                                srcSigned,
                                dstSigned
                        ));

            return out;
        }

        switch (type.getDataType().getJavaType()) {
            case BYTE:
                switch (dim) {
                    case 1:
                        return Array1DUtil.byteArrayToSafeArray((byte[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);

                    case 2:
                        return Array2DUtil.byteArrayToSafeArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
                }
                break;

            case SHORT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.shortArrayToSafeArray((short[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);

                    case 2:
                        return Array2DUtil.shortArrayToSafeArray((short[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
                }
                break;

            case INT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.intArrayToSafeArray((int[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);

                    case 2:
                        return Array2DUtil.intArrayToSafeArray((int[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
                }
                break;

            case LONG:
                switch (dim) {
                    case 1:
                        return Array1DUtil.longArrayToSafeArray((long[]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);

                    case 2:
                        return Array2DUtil.longArrayToSafeArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
                }
                break;

            case FLOAT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.floatArrayToSafeArray((float[]) in, inOffset, out, outOffset, length, dstSigned);

                    case 2:
                        return Array2DUtil.floatArrayToSafeArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
                }
                break;

            case DOUBLE:
                switch (dim) {
                    case 1:
                        return Array1DUtil.doubleArrayToSafeArray((double[]) in, inOffset, out, outOffset, length, dstSigned);

                    case 2:
                        return Array2DUtil.doubleArrayToSafeArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
                }
                break;
        }

        return out;
    }

    /**
     * Converts a source array into a "safe" array with controlled data type conversion
     * and data manipulation. Supports multidimensional arrays with various primitive
     * data types.
     *
     * @param in        The input array to be converted. Must not be {@code null}.
     * @param out       The output array where the result will be stored. Must not be {@code null}.
     * @param srcSigned Specifies if the input array values are signed.
     * @param dstSigned Specifies if the output array values should be signed.
     * @return The resulting "safe" array converted and processed based on inputs.
     * @throws NullPointerException     If {@code in} or {@code out} is {@code null}.
     * @throws IllegalArgumentException If any provided argument is invalid.
     */
    @Contract("_, _, _, _ -> param2")
    public static @NonNull Object arrayToSafeArray(final @NonNull Object in, final @NonNull Object out, final boolean srcSigned, final boolean dstSigned) throws NullPointerException, IllegalArgumentException {
        return arrayToSafeArray(in, 0, out, 0, getTotalLength(in), srcSigned, dstSigned);
    }

    /**
     * Converts the given input array to a safe byte array. The conversion respects
     * the signed or unsigned nature of the input, depending on the provided flag.
     * Supports arrays of multiple dimensions.
     *
     * @param array  The input array to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input array should be treated as signed or unsigned.
     *               If {@code true}, the array is treated as signed; otherwise, it is treated as unsigned.
     * @return A new array (potentially multidimensional) containing safe byte values converted from the input array.
     * @throws NullPointerException     If the input array is {@code null}.
     * @throws IllegalArgumentException If the input array is of an unsupported type or has an invalid structure.
     */
    public static @NonNull Object arrayToSafeByteArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int dim = getDim(array);
        final int len = Array.getLength(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToSafeByteArray(array, 0, new byte[len], 0, len, signed, signed);

            case 2:
                return Array2DUtil.arrayToSafeByteArray(array, 0, new byte[len][], 0, len, signed, signed);

            default:
                // use generic code
                final Object result = createArray(DataType.BYTE, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToSafeIntArray(Array.get(array, i), signed));

                return result;
        }
    }

    /**
     * Converts the given input array to a safe short array. The conversion respects
     * the signed or unsigned nature of the input, depending on the provided flag.
     * Supports arrays of multiple dimensions.
     *
     * @param array  The input array to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input array should be treated as signed or unsigned.
     *               If {@code true}, the array is treated as signed; otherwise, it is treated as unsigned.
     * @return A new array (potentially multidimensional) containing safe short values converted from the input array.
     * @throws NullPointerException     If the input array is {@code null}.
     * @throws IllegalArgumentException If the input array is of an unsupported type or has an invalid structure.
     */
    public static @NonNull Object arrayToSafeShortArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int dim = getDim(array);
        final int len = Array.getLength(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToSafeShortArray(array, 0, new short[len], 0, len, signed, signed);

            case 2:
                return Array2DUtil.arrayToSafeShortArray(array, 0, new short[len][], 0, len, signed, signed);

            default:
                // use generic code
                final Object result = createArray(DataType.SHORT, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToSafeIntArray(Array.get(array, i), signed));

                return result;
        }
    }

    /**
     * Converts the given input array to a safe int array. The conversion respects
     * the signed or unsigned nature of the input, depending on the provided flag.
     * Supports arrays of multiple dimensions.
     *
     * @param array  The input array to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input array should be treated as signed or unsigned.
     *               If {@code true}, the array is treated as signed; otherwise, it is treated as unsigned.
     * @return A new array (potentially multidimensional) containing safe int values converted from the input array.
     * @throws NullPointerException     If the input array is {@code null}.
     * @throws IllegalArgumentException If the input array is of an unsupported type or has an invalid structure.
     */
    public static @NonNull Object arrayToSafeIntArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int dim = getDim(array);
        final int len = Array.getLength(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToSafeIntArray(array, 0, new int[len], 0, len, signed, signed);

            case 2:
                return Array2DUtil.arrayToSafeIntArray(array, 0, new int[len][], 0, len, signed, signed);

            default:
                // use generic code
                final Object result = createArray(DataType.INT, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToSafeIntArray(Array.get(array, i), signed));

                return result;
        }
    }

    /**
     * Converts the given input array to a safe long array. The conversion respects
     * the signed or unsigned nature of the input, depending on the provided flag.
     * Supports arrays of multiple dimensions.
     *
     * @param array  The input array to be converted. Must not be {@code null}.
     * @param signed Indicates whether the input array should be treated as signed or unsigned.
     *               If {@code true}, the array is treated as signed; otherwise, it is treated as unsigned.
     * @return A new array (potentially multidimensional) containing safe long values converted from the input array.
     * @throws NullPointerException     If the input array is {@code null}.
     * @throws IllegalArgumentException If the input array is of an unsupported type or has an invalid structure.
     */
    public static @NonNull Object arrayToSafeLongArray(final @NonNull Object array, final boolean signed) throws NullPointerException, IllegalArgumentException {
        final int dim = getDim(array);
        final int len = Array.getLength(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToSafeLongArray(array, 0, new long[len], 0, len, signed, signed);

            case 2:
                return Array2DUtil.arrayToSafeLongArray(array, 0, new long[len][], 0, len, signed, signed);

            default:
                // use generic code
                final Object result = createArray(DataType.LONG, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToSafeLongArray(Array.get(array, i), signed));

                return result;
        }
    }

    /**
     * Converts an array to its String representation.
     *
     * @param array the input array to be converted to a String; must not be {@code null}.
     * @return the String representation of the one-dimensional array.
     * @throws IllegalArgumentException      if the input is not a valid array.
     * @throws UnsupportedOperationException if the array is multidimensional (WIP) or unsupported.
     */
    @ApiStatus.Experimental
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public static @NonNull String arrayToString(final @NonNull Object array) throws IllegalArgumentException, UnsupportedOperationException {
        final int dim = getDim(array);

        return switch (dim) {
            case 1 -> Array1DUtil.arrayToString(array);
            default -> throw new UnsupportedOperationException("Cannot convert a multidimensional array to String."); // FIXME not yet implemented
        };
    }

    /**
     * Converts a one-dimensional array to a string representation based on the specified parameters.
     *
     * @param array       the one-dimensional array to be converted to a string; must not be {@code null}.
     * @param signed      whether the numeric values in the array should be treated as signed.
     * @param hexadecimal whether the numeric values in the array should be converted to hexadecimal format.
     * @param separator   the string to use as a separator between array elements; must not be {@code null}.
     * @param size        the number of significant figures to retain for floating-point values;
     *                    use {@code -1} to disable rounding.
     * @return the string representation of the one-dimensional array.
     * @throws NullPointerException          if the input array or the separator is {@code null}.
     * @throws IllegalArgumentException      if the input array is invalid.
     * @throws UnsupportedOperationException if the input is not a one-dimensional array (WIP) or if an unsupported feature is encountered
     */
    @ApiStatus.Experimental
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    public static @NonNull String array1DToString(final @NonNull Object array, final boolean signed, final boolean hexadecimal, final @NonNull String separator, final @Range(from = -1, to = Integer.MAX_VALUE) int size) throws NullPointerException, IllegalArgumentException, UnsupportedOperationException {
        final int dim = getDim(array);

        return switch (dim) {
            case 1 -> Array1DUtil.arrayToString(array, signed, hexadecimal, separator, size);
            default -> throw new UnsupportedOperationException("Cannot convert a multidimensional array to String."); // FIXME not yet implemented
        };

    }

    /**
     * Converts a given string representation of a 1D array into an actual 1D array object
     * of the specified data type.
     *
     * @param value    the string representation of the 1D array; must not be {@code null}.
     * @param dataType the data type to which the string should be converted; must not be {@code null}.
     * @return a 1D array object corresponding to the given string representation and data type.
     * @throws NullPointerException     If any of the provided arguments are {@code null}.
     * @throws IllegalArgumentException If the input string is blank or invalid for the
     *                                  specified data type.
     * @throws NumberFormatException    If a value in the input string cannot be parsed as
     *                                  the specified data type.
     * @deprecated Use {@link Array1DUtil#stringToArray(String, DataType)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static @NonNull Object stringToArray1D(final @NonNull String value, final @NonNull DataType dataType) throws NullPointerException, IllegalArgumentException, NumberFormatException {
        return Array1DUtil.stringToArray(value, dataType);
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
     * @deprecated Use {@link Array1DUtil#stringToArray(String, DataType, boolean, String)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static @NonNull Object stringToArray1D(final @NonNull String value, final @NonNull DataType dataType, final boolean hexadecimal, final @NonNull String separator) throws NullPointerException, IllegalArgumentException, NumberFormatException {
        return Array1DUtil.stringToArray(value, dataType, hexadecimal, separator);
    }

    /**
     * Creates a one-dimensional byte array of the specified size, where each element is calculated
     * using the specified initial value and incremental step.
     *
     * @param size         the number of elements in the array; must be non-negative.
     * @param initialValue the starting value for the first element in the array.
     * @param step         the increment applied to calculate the following values in the array.
     * @return a byte array with elements following a linear sequence starting from the initial value.
     * @throws IndexOutOfBoundsException if the size is negative.
     */
    public static byte @NonNull [] createLinearByteArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size, final byte initialValue, final byte step) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
        final byte[] array = new byte[size];

        byte value = initialValue;
        for (int i = 0; i < size; i++) {
            array[i] = value;
            value += step;
        }

        return array;
    }

    /**
     * Creates a one-dimensional short array of the specified size, where each element is calculated
     * using the specified initial value and incremental step.
     *
     * @param size         the number of elements in the array; must be non-negative.
     * @param initialValue the starting value for the first element in the array.
     * @param step         the increment applied to calculate the following values in the array.
     * @return a short array with elements following a linear sequence starting from the initial value.
     * @throws IndexOutOfBoundsException if the size is negative.
     */
    public static short @NonNull [] createLinearShortArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size, final short initialValue, final short step) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
        final short[] array = new short[size];

        short value = initialValue;
        for (int i = 0; i < size; i++) {
            array[i] = value;
            value += step;
        }

        return array;
    }

    /**
     * Creates a one-dimensional int array of the specified size, where each element is calculated
     * using the specified initial value and incremental step.
     *
     * @param size         the number of elements in the array; must be non-negative.
     * @param initialValue the starting value for the first element in the array.
     * @param step         the increment applied to calculate the following values in the array.
     * @return an int array with elements following a linear sequence starting from the initial value.
     * @throws IndexOutOfBoundsException if the size is negative.
     */
    public static int @NonNull [] createLinearIntArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size, final int initialValue, final int step) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
        final int[] array = new int[size];

        int value = initialValue;
        for (int i = 0; i < size; i++) {
            array[i] = value;
            value += step;
        }

        return array;
    }

    /**
     * Creates a one-dimensional long array of the specified size, where each element is calculated
     * using the specified initial value and incremental step.
     *
     * @param size         the number of elements in the array; must be non-negative.
     * @param initialValue the starting value for the first element in the array.
     * @param step         the increment applied to calculate the following values in the array.
     * @return a long array with elements following a linear sequence starting from the initial value.
     * @throws IndexOutOfBoundsException if the size is negative.
     */
    public static long @NonNull [] createLinearLongArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size, final long initialValue, final long step) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
        final long[] array = new long[size];

        long value = initialValue;
        for (int i = 0; i < size; i++) {
            array[i] = value;
            value += step;
        }

        return array;
    }

    /**
     * Creates a one-dimensional float array of the specified size, where each element is calculated
     * using the specified initial value and incremental step.
     *
     * @param size         the number of elements in the array; must be non-negative.
     * @param initialValue the starting value for the first element in the array.
     * @param step         the increment applied to calculate the following values in the array.
     * @return a float array with elements following a linear sequence starting from the initial value.
     * @throws IndexOutOfBoundsException if the size is negative.
     */
    public static float @NonNull [] createLinearFloatArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size, final float initialValue, final float step) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
        final float[] array = new float[size];

        float value = initialValue;
        for (int i = 0; i < size; i++) {
            array[i] = value;
            value += step;
        }

        return array;
    }

    /**
     * Creates a one-dimensional double array of the specified size, where each element is calculated
     * using the specified initial value and incremental step.
     *
     * @param size         the number of elements in the array; must be non-negative.
     * @param initialValue the starting value for the first element in the array.
     * @param step         the increment applied to calculate the following values in the array.
     * @return a double array with elements following a linear sequence starting from the initial value.
     * @throws IndexOutOfBoundsException if the size is negative.
     */
    public static double @NonNull [] createLinearDoubleArray(final @Range(from = 0, to = Integer.MAX_VALUE) int size, final double initialValue, final double step) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(size, 0, "Size cannot be negative.");
        final double[] array = new double[size];

        double value = initialValue;
        for (int i = 0; i < size; i++) {
            array[i] = value;
            value += step;
        }

        return array;
    }
}
