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

import org.bioimageanalysis.icy.common.type.DataType;

import java.lang.reflect.Array;

/**
 * General array utilities :<br>
 * Basic array manipulation, conversion and tools.<br>
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 * @see Array1DUtil
 * @see Array2DUtil
 * @see Array3DUtil
 * @see ByteArrayConvert
 */
public class ArrayUtil {
    /**
     * @param array object
     * @return Returns the {@link ArrayType} of the specified array.
     */
    public static ArrayType getArrayType(final Object array) {
        int dim = 0;

        Class<?> arrayClass = array.getClass();
        while (arrayClass.isArray()) {
            dim++;
            arrayClass = arrayClass.getComponentType();
        }

        return new ArrayType(DataType.getDataType(arrayClass), dim);
    }

    /**
     * @param array object
     * @return Return the number of dimension of the specified array
     */
    public static int getDim(final Object array) {
        int result = 0;

        Class<?> arrayClass = array.getClass();
        while (arrayClass.isArray()) {
            result++;
            arrayClass = arrayClass.getComponentType();
        }

        return result;
    }

    /**
     * @param array object
     * @return Return the DataType (java type only) of the specified array.
     * @see DataType
     */
    public static DataType getDataType(final Object array) {
        Class<?> arrayClass = array.getClass();
        while (arrayClass.isArray())
            arrayClass = arrayClass.getComponentType();

        return DataType.getDataType(arrayClass);
    }

    /**
     * @param array  object
     * @param signed boolean
     * @return Return the DataType of the specified array
     */
    public static DataType getDataType(final Object array, final boolean signed) {
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
     * @param array object
     * @return Return the number of element of the specified array
     */
    public static int getLength(final Object array) {
        if (array != null)
            return Array.getLength(array);

        // null array
        return 0;
    }

    /**
     * @param array object
     * @return Return the total number of element of the specified array
     */
    public static int getTotalLength(final Object array) {
        int result = 1;
        Object subArray = array;

        Class<?> arrayClass = array.getClass();
        while (arrayClass.isArray()) {
            result *= Array.getLength(subArray);

            arrayClass = arrayClass.getComponentType();
            if (result > 0)
                subArray = Array.get(array, 0);
        }

        return result;
    }

    /**
     * @param dataType array data type
     * @param dim      number of dimension of the allocated array
     * @param len      size of first dimension
     * @return Allocate the specified array data type with specified number of dimension.<br>
     */
    public static Object createArray(final DataType dataType, final int dim, final int len) {
        final int[] dims = new int[dim];
        dims[0] = len;
        return Array.newInstance(dataType.toPrimitiveClass(), dims);
    }

    /**
     * @param arrayType array object
     * @param len       int
     * @return Allocate the specified array data type with specified len for the first dimension
     */
    public static Object createArray(final ArrayType arrayType, final int len) {
        return createArray(arrayType.getDataType(), arrayType.getDim(), len);
    }

    /**
     * @param array     object
     * @param arrayType object
     * @param len       int
     * @return Allocate the specified array if it's defined to null with the specified len
     */
    public static Object allocIfNull(final Object array, final ArrayType arrayType, final int len) {
        if (array == null)
            return createArray(arrayType, len);

        return array;
    }

    /**
     * @param array object
     * @return Encapsulate the specified array with a single cell array of the same type.
     */
    public static Object[] encapsulate(final Object array) {
        final ArrayType type = getArrayType(array);

        // increase dim
        type.setDim(type.getDim() + 1);

        final Object[] result = (Object[]) createArray(type, 1);
        // encapsulate
        result[0] = array;

        return result;
    }

    /**
     * @param array1 object
     * @param array2 object
     * @return Return true if the specified array has the same data type<br>
     * and the same number of dimension.
     */
    public static boolean arrayTypeCompare(final Object array1, final Object array2) {
        return getArrayType(array1).equals(getArrayType(array2));
    }

    /**
     * @param array1 object
     * @param array2 object
     * @return Return true if the specified array are equals (same type, dimension and data).<br>
     */
    public static boolean arrayCompare(final Object array1, final Object array2) {
        if (array1 == array2)
            return true;

        if (array1 == null || array2 == null)
            return false;

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

        // single dimension array
        switch (type.getDataType().getJavaType()) {
            case BYTE:
                switch (dim) {
                    case 1:
                        return Array1DUtil.arrayByteCompare((byte[]) array1, (byte[]) array2);
                    case 2:
                        return Array2DUtil.arrayByteCompare((byte[][]) array1, (byte[][]) array2);
                }
                break;

            case SHORT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.arrayShortCompare((short[]) array1, (short[]) array2);
                    case 2:
                        return Array2DUtil.arrayShortCompare((short[][]) array1, (short[][]) array2);
                }
                break;

            case INT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.arrayIntCompare((int[]) array1, (int[]) array2);
                    case 2:
                        return Array2DUtil.arrayIntCompare((int[][]) array1, (int[][]) array2);
                }
                break;

            case LONG:
                switch (dim) {
                    case 1:
                        return Array1DUtil.arrayLongCompare((long[]) array1, (long[]) array2);
                    case 2:
                        return Array2DUtil.arrayLongCompare((long[][]) array1, (long[][]) array2);
                }
                break;

            case FLOAT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.arrayFloatCompare((float[]) array1, (float[]) array2);
                    case 2:
                        return Array2DUtil.arrayFloatCompare((float[][]) array1, (float[][]) array2);
                }
                break;

            case DOUBLE:
                switch (dim) {
                    case 1:
                        return Array1DUtil.arrayDoubleCompare((double[]) array1, (double[]) array2);
                    case 2:
                        return Array2DUtil.arrayDoubleCompare((double[][]) array1, (double[][]) array2);
                }
                break;
        }

        return false;
    }

    /**
     * @param out    If (out == null) a new array is allocated.
     * @param in     object
     * @param offset int
     * @return Transform the multi dimension 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     */
    public static Object toArray1D(final Object in, final Object out, final int offset) {
        final ArrayType type = getArrayType(in);
        final DataType dataType = type.getDataType();
        final int dim = type.getDim();

        // more than 3 dimensions --> use generic code
        if (dim > 3) {
            final Object result = Array1DUtil.allocIfNull(out, dataType, offset + getTotalLength(in));

            //if (in != null) {
            final int len = Array.getLength(in);

            int off = offset;
            for (int i = 0; i < len; i++) {
                final Object s_in = Array.get(in, i);

                if (s_in != null) {
                    toArray1D(s_in, result, off);
                    off += Array.getLength(s_in);
                }
            }
            //}
        }

        switch (dataType.getJavaType()) {
            case BYTE:
                switch (dim) {
                    case 1:
                        return Array1DUtil.toByteArray1D((byte[]) in, (byte[]) out, offset);
                    case 2:
                        return Array2DUtil.toByteArray1D((byte[][]) in, (byte[]) out, offset);
                    case 3:
                        return Array3DUtil.toByteArray1D((byte[][][]) in, (byte[]) out, offset);
                }
                break;

            case SHORT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.toShortArray1D((short[]) in, (short[]) out, offset);
                    case 2:
                        return Array2DUtil.toShortArray1D((short[][]) in, (short[]) out, offset);
                    case 3:
                        return Array3DUtil.toShortArray1D((short[][][]) in, (short[]) out, offset);
                }
                break;

            case INT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.toIntArray1D((int[]) in, (int[]) out, offset);
                    case 2:
                        return Array2DUtil.toIntArray1D((int[][]) in, (int[]) out, offset);
                    case 3:
                        return Array3DUtil.toIntArray1D((int[][][]) in, (int[]) out, offset);
                }
                break;

            case LONG:
                switch (dim) {
                    case 1:
                        return Array1DUtil.toLongArray1D((long[]) in, (long[]) out, offset);
                    case 2:
                        return Array2DUtil.toLongArray1D((long[][]) in, (long[]) out, offset);
                    case 3:
                        return Array3DUtil.toLongArray1D((long[][][]) in, (long[]) out, offset);
                }
                break;

            case FLOAT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.toFloatArray1D((float[]) in, (float[]) out, offset);
                    case 2:
                        return Array2DUtil.toFloatArray1D((float[][]) in, (float[]) out, offset);
                    case 3:
                        return Array3DUtil.toFloatArray1D((float[][][]) in, (float[]) out, offset);
                }
                break;

            case DOUBLE:
                switch (dim) {
                    case 1:
                        return Array1DUtil.toDoubleArray1D((double[]) in, (double[]) out, offset);
                    case 2:
                        return Array2DUtil.toDoubleArray1D((double[][]) in, (double[]) out, offset);
                    case 3:
                        return Array3DUtil.toDoubleArray1D((double[][][]) in, (double[]) out, offset);
                }
                break;
        }

        return out;
    }

    /**
     * @param length    If specified length != -1 then the value is directly returned.
     * @param in        object
     * @param inOffset  int
     * @param out       object
     * @param outOffset int
     * @return Get maximum length for a copy from in to out with specified offset.<br>
     */
    static int getCopyLength(final Object in, final int inOffset, final Object out, final int outOffset, final int length) {
        if (length == -1)
            return getCopyLength(in, inOffset, out, outOffset);

        return length;
    }

    /**
     * @param in        object
     * @param out       object
     * @param inOffset  int
     * @param outOffset int
     * @return Get maximum length for a copy from in to out with specified offset.
     */
    public static int getCopyLength(final Object in, final int inOffset, final Object out, final int outOffset) {
        // 'in' object can't be null !
        final int len = getCopyLength(in, inOffset);

        if (out == null)
            return len;

        return Math.min(len, getCopyLength(out, outOffset));
    }

    /**
     * @param array  object
     * @param offset int
     * @return Get length for a copy from or to the specified array with specified offset
     */
    public static int getCopyLength(final Object array, final int offset) {
        return getLength(array) - offset;
    }

    /**
     * @param in        input array
     * @param inOffset  position where we start read data from
     * @param out       output array which is used to receive result (and so define wanted type)
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @param signed    if input data are integer type then we assume them as signed data
     * @return Convert and return the 'in' array in 'out' array type.<br>
     */
    public static Object arrayToArray(final Object in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        final ArrayType type = getArrayType(in);
        final int dim = type.getDim();

        // more than 2 dimensions --> use generic code
        if (dim > 2) {
            final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
            final Object result = allocIfNull(out, type, outOffset + len);

            for (int i = 0; i < len; i++)
                Array.set(result, i + outOffset,
                        arrayToArray(Array.get(in, i + inOffset), 0, Array.get(result, i + outOffset), 0, -1, signed));

            return result;
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
     * @param in     input array
     * @param out    output array which is used to receive result (and so define wanted type)
     * @param signed if input data are integer type then we assume them as signed data
     * @return Convert and return the 'in' array in 'out' array type.<br>
     */
    public static Object arrayToArray(final Object in, final Object out, final boolean signed) {
        return arrayToArray(in, 0, out, 0, -1, signed);
    }

    public static Object arrayToDoubleArray(final Object array, final boolean signed) {
        if (array == null)
            return null;

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

    public static Object arrayToFloatArray(final Object array, final boolean signed) {
        if (array == null)
            return null;

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

    public static Object arrayToLongArray(final Object array, final boolean signed) {
        if (array == null)
            return null;

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

    public static Object arrayToIntArray(final Object array, final boolean signed) {
        if (array == null)
            return null;

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

    public static Object arrayToShortArray(final Object array, final boolean signed) {
        if (array == null)
            return null;

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

    public static Object arrayToByteArray(final Object array) {
        if (array == null)
            return null;

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
     * @param in        input array we want to convert
     * @param inOffset  position where we start read data from
     * @param out       output array which receive result and define the type in which we want to convert the
     *                  input array
     * @param outOffset position where we start to write data to
     * @param length    number of value to convert (-1 means we will use the maximum possible length)
     * @param srcSigned considers value from input array as signed (meaningful only for integer type array:
     *                  <code>byte, short, int, long</code>)
     * @param dstSigned considers output value as signed (meaningful only for integer type array:
     *                  <code>byte, short, int, long</code>)
     * @return Safely converts the input array in the output array data type.<br>
     * Output value is limited to output type limit :<br>
     * unsigned int input = 1000 --&gt; unsigned byte output = 255<br>
     * int input = -1000 --&gt; byte output = -128<br>
     */
    public static Object arrayToSafeArray(final Object in, final int inOffset, final Object out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final ArrayType type = getArrayType(in);
        final int dim = type.getDim();

        // more than 2 dimensions --> use generic code
        if (dim > 2) {
            final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
            final Object result = allocIfNull(out, type, outOffset + len);

            for (int i = 0; i < len; i++)
                Array.set(
                        result,
                        i + outOffset,
                        arrayToSafeArray(Array.get(in, i + inOffset), 0, Array.get(result, i + outOffset), 0, -1,
                                srcSigned, dstSigned));

            return result;
        }

        switch (type.getDataType().getJavaType()) {
            case BYTE:
                switch (dim) {
                    case 1:
                        return Array1DUtil.byteArrayToSafeArray((byte[]) in, inOffset, out, outOffset, length,
                                srcSigned, dstSigned);

                    case 2:
                        return Array2DUtil.byteArrayToSafeArray((byte[][]) in, inOffset, out, outOffset, length,
                                srcSigned, dstSigned);
                }
                break;

            case SHORT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.shortArrayToSafeArray((short[]) in, inOffset, out, outOffset, length,
                                srcSigned, dstSigned);

                    case 2:
                        return Array2DUtil.shortArrayToSafeArray((short[][]) in, inOffset, out, outOffset, length,
                                srcSigned, dstSigned);
                }
                break;

            case INT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.intArrayToSafeArray((int[]) in, inOffset, out, outOffset, length, srcSigned,
                                dstSigned);

                    case 2:
                        return Array2DUtil.intArrayToSafeArray((int[][]) in, inOffset, out, outOffset, length,
                                srcSigned, dstSigned);
                }
                break;

            case LONG:
                switch (dim) {
                    case 1:
                        return Array1DUtil.longArrayToSafeArray((long[]) in, inOffset, out, outOffset, length,
                                srcSigned, dstSigned);

                    case 2:
                        return Array2DUtil.longArrayToSafeArray((long[][]) in, inOffset, out, outOffset, length,
                                srcSigned, dstSigned);
                }
                break;

            case FLOAT:
                switch (dim) {
                    case 1:
                        return Array1DUtil.floatArrayToSafeArray((float[]) in, inOffset, out, outOffset, length,
                                dstSigned);

                    case 2:
                        return Array2DUtil.floatArrayToSafeArray((float[][]) in, inOffset, out, outOffset, length,
                                dstSigned);
                }
                break;

            case DOUBLE:
                switch (dim) {
                    case 1:
                        return Array1DUtil.doubleArrayToSafeArray((double[]) in, inOffset, out, outOffset, length,
                                dstSigned);

                    case 2:
                        return Array2DUtil.doubleArrayToSafeArray((double[][]) in, inOffset, out, outOffset, length,
                                dstSigned);
                }
                break;
        }

        return out;
    }

    /**
     * @param in        input array we want to convert
     * @param out       output array which receive result and define the type in which we want to convert the
     *                  input array
     * @param srcSigned considers value from input array as (un)signed (meaningful only for integer type
     *                  array: <code>byte, short, int, long</code>)
     * @param dstSigned considers output value as (un)signed (meaningful only for integer type array:
     *                  <code>byte, short, int, long</code>)
     * @return Safely converts the input array in the output array data type.
     */
    public static Object arrayToSafeArray(final Object in, final Object out, final boolean srcSigned, final boolean dstSigned) {
        return arrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static Object arrayToSafeLongArray(final Object array, final boolean signed) {
        if (array == null)
            return null;

        final int dim = getDim(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToSafeLongArray(array, 0, null, 0, -1, signed, signed);

            case 2:
                return Array2DUtil.arrayToSafeLongArray(array, 0, null, 0, -1, signed, signed);

            default:
                // use generic code
                final int len = Array.getLength(array);
                final Object result = createArray(DataType.LONG, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToSafeLongArray(Array.get(array, i), signed));

                return result;
        }
    }

    public static Object arrayToSafeIntArray(final Object array, final boolean signed) {
        if (array == null)
            return null;

        final int dim = getDim(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToSafeIntArray(array, 0, null, 0, -1, signed, signed);

            case 2:
                return Array2DUtil.arrayToSafeIntArray(array, 0, null, 0, -1, signed, signed);

            default:
                // use generic code
                final int len = Array.getLength(array);
                final Object result = createArray(DataType.INT, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToSafeIntArray(Array.get(array, i), signed));

                return result;
        }
    }

    public static Object arrayToSafeShortArray(final Object array, final boolean signed) {
        if (array == null)
            return null;

        final int dim = getDim(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToSafeShortArray(array, 0, null, 0, -1, signed, signed);

            case 2:
                return Array2DUtil.arrayToSafeShortArray(array, 0, null, 0, -1, signed, signed);

            default:
                // use generic code
                final int len = Array.getLength(array);
                final Object result = createArray(DataType.SHORT, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToSafeIntArray(Array.get(array, i), signed));

                return result;
        }
    }

    public static Object arrayToSafeByteArray(final Object array, final boolean signed) {
        if (array == null)
            return null;

        final int dim = getDim(array);

        switch (dim) {
            case 1:
                return Array1DUtil.arrayToSafeByteArray(array, 0, null, 0, -1, signed, signed);

            case 2:
                return Array2DUtil.arrayToSafeByteArray(array, 0, null, 0, -1, signed, signed);

            default:
                // use generic code
                final int len = Array.getLength(array);
                final Object result = createArray(DataType.BYTE, dim, len);

                for (int i = 0; i < len; i++)
                    Array.set(result, i, arrayToSafeIntArray(Array.get(array, i), signed));

                return result;
        }
    }

    /**
     * @param array 1D array containing values to return as string
     * @return Return the specified array as string<br>
     * Default representation use ':' as separator character<br>
     * <br>
     * ex : [0,1,2,3,4] --&gt; "0:1:2:3:4"<br>
     */
    public static String arrayToString(final Object array) {
        if (array == null)
            return null;

        final int dim = getDim(array);

        return switch (dim) {
            case 1 -> Array1DUtil.arrayToString(array);
            default ->
                // not yet implemented
                    null;
        };
    }

    /**
     * @param array     1D array containing values to return as string
     * @param signed    input value are signed (only for integer data type)
     * @param hexa      set value in resulting string in hexa decimal format (only for integer data type)
     * @param separator specify the separator to use between each array value in resulting string
     * @param size      specify the number of significant number to display for float value (-1 means all)
     * @return Return the specified 1D array as string<br>
     * ex : [0,1,2,3,4] --&gt; "0:1:2:3:4"
     */
    public static String array1DToString(final Object array, final boolean signed, final boolean hexa, final String separator, final int size) {
        if (array == null)
            return null;

        final int dim = getDim(array);

        return switch (dim) {
            case 1 -> Array1DUtil.arrayToString(array, signed, hexa, separator, size);
            default ->
                // not yet implemented
                    null;
        };

    }

    /**
     * @param value    string containing value to return as 1D array
     * @param dataType specify the values data type and also the output array data type string
     * @return Return the specified string containing separated values as a 1D array<br>
     * By default separator is assumed to be ':' character<br>
     * ex : "0:1:2:3:4" --&gt; [0,1,2,3,4]<br>
     */
    public static Object stringToArray1D(final String value, final DataType dataType) {
        return Array1DUtil.stringToArray(value, dataType);
    }

    /**
     * @param value     string containing value to return as 1D array
     * @param dataType  specify the values data type and also the output array data type string
     * @param hexa      values in string as stored as hexa values (only for integer data type)
     * @param separator specify the separator used between each value in the input string
     * @return Return the specified string containing separated values as a 1D array<br>
     * ex : "0:1:2:3:4" --&gt; [0,1,2,3,4]<br>
     */
    public static Object stringToArray1D(final String value, final DataType dataType, final boolean hexa, final String separator) {
        return Array1DUtil.stringToArray(value, dataType, hexa, separator);
    }

    /**
     * @param size         the size of the array to create
     * @param initialValue the initial value (i.e. the first element of the array, boolean signed)
     * @param step         the step between consecutive array values
     * @return Creates a linear array with specified size, initial value and step. <br>
     * Example: to create the array [1,3,5,7] call createLinearArray(4,1,2)
     */
    public static int[] createLinearIntArray(final int size, final int initialValue, final int step) {
        final int[] array = new int[size];

        int value = initialValue;
        for (int i = 0; i < size; i++) {
            array[i] = value;
            value += step;
        }

        return array;
    }

    /**
     * @param size         the size of the array to create
     * @param initialValue the initial value (i.e. the first element of the array)
     * @param step         the step between consecutive array values
     * @return Creates a linear array with specified size, initial value and step. <br>
     * Example: to create the array [1,3,5,7] call createLinearArray(4,1,2)
     */
    public static double[] createLinearDoubleArray(final int size, final double initialValue, final double step) {
        final double[] array = new double[size];

        double value = initialValue;
        for (int i = 0; i < size; i++) {
            array[i] = value;
            value += step;
        }

        return array;
    }
}
