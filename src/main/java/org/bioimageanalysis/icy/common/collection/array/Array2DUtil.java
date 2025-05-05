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
import org.jetbrains.annotations.NotNull;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class Array2DUtil {
    /**
     * @param array 2D array
     * @return Return the total number of element of the specified array
     */
    public static int getTotalLength(final byte[][] array) {
        int result = 0;

        if (array != null) {
            for (final byte[] bytes : array)
                result += Array1DUtil.getTotalLength(bytes);
        }

        return result;
    }

    /**
     * @param array 2D array
     * @return Return the total number of element of the specified array
     */
    public static int getTotalLength(final short[][] array) {
        int result = 0;

        if (array != null) {
            for (final short[] shorts : array)
                result += Array1DUtil.getTotalLength(shorts);
        }

        return result;
    }

    /**
     * @param array 2D array
     * @return Return the total number of element of the specified array
     */
    public static int getTotalLength(final int[][] array) {
        int result = 0;

        if (array != null) {
            for (final int[] ints : array)
                result += Array1DUtil.getTotalLength(ints);
        }

        return result;
    }

    /**
     * @param array 2D array
     * @return Return the total number of element of the specified array
     */
    public static int getTotalLength(final long[][] array) {
        int result = 0;

        if (array != null) {
            for (final long[] longs : array)
                result += Array1DUtil.getTotalLength(longs);
        }

        return result;
    }

    /**
     * @param array 2D array
     * @return Return the total number of element of the specified array
     */
    public static int getTotalLength(final float[][] array) {
        int result = 0;

        if (array != null) {
            for (final float[] floats : array)
                result += Array1DUtil.getTotalLength(floats);

        }

        return result;
    }

    /**
     * @param array 2D array
     * @return Return the total number of element of the specified array
     */
    public static int getTotalLength(final double[][] array) {
        int result = 0;

        if (array != null) {
            for (final double[] doubles : array)
                result += Array1DUtil.getTotalLength(doubles);
        }

        return result;
    }

    /**
     * @param dataType object
     * @param len int
     * @return Create a new 2D array with specified data type and length
     */
    public static Object[] createArray(final @NotNull DataType dataType, final int len) {
        return switch (dataType.getJavaType()) {
            case BYTE -> new byte[len][];
            case SHORT -> new short[len][];
            case INT -> new int[len][];
            case LONG -> new long[len][];
            case FLOAT -> new float[len][];
            case DOUBLE -> new double[len][];
            default -> null;
        };
    }

    /**
     * @param out 2D array
     * @param len int
     * @return Allocate the specified 2D array if it's defined to null with the specified len
     */
    public static byte[][] allocIfNull(final byte[][] out, final int len) {
        if (out == null)
            return new byte[len][];

        return out;
    }

    /**
     * @param out 2D array
     * @param len int
     * @return Allocate the specified 2D array if it's defined to null with the specified len
     */
    public static short[][] allocIfNull(final short[][] out, final int len) {
        if (out == null)
            return new short[len][];

        return out;
    }

    /**
     * @param out 2D array
     * @param len int
     * @return Allocate the specified 2D array if it's defined to null with the specified len
     */
    public static int[][] allocIfNull(final int[][] out, final int len) {
        if (out == null)
            return new int[len][];

        return out;
    }

    /**
     * @param out 2D array
     * @param len int
     * @return Allocate the specified 2D array if it's defined to null with the specified len
     */
    public static long[][] allocIfNull(final long[][] out, final int len) {
        if (out == null)
            return new long[len][];

        return out;
    }

    /**
     * @param out 2D array
     * @param len int
     * @return Allocate the specified 2D array if it's defined to null with the specified len
     */
    public static float[][] allocIfNull(final float[][] out, final int len) {
        if (out == null)
            return new float[len][];

        return out;
    }

    /**
     * @param out 2D array
     * @param len int
     * @return Allocate the specified 2D array if it's defined to null with the specified len
     */
    public static double[][] allocIfNull(final double[][] out, final int len) {
        if (out == null)
            return new double[len][];

        return out;
    }

    /**
     * @param array1 2D array
     * @param array2 2D array
     * @return Return true is the specified arrays are equals
     */
    public static boolean arrayByteCompare(final byte[][] array1, final byte[][] array2) {
        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Array1DUtil.arrayByteCompare(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * @param array1 2D array
     * @param array2 2D array
     * @return Return true is the specified arrays are equals
     */
    public static boolean arrayShortCompare(final short[][] array1, final short[][] array2) {
        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Array1DUtil.arrayShortCompare(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * @param array1 2D array
     * @param array2 2D array
     * @return Return true is the specified arrays are equals
     */
    public static boolean arrayIntCompare(final int[][] array1, final int[][] array2) {
        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Array1DUtil.arrayIntCompare(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * @param array1 2D array
     * @param array2 2D array
     * @return Return true is the specified arrays are equals
     */
    public static boolean arrayLongCompare(final long[][] array1, final long[][] array2) {
        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Array1DUtil.arrayLongCompare(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * @param array1 2D array
     * @param array2 2D array
     * @return Return true is the specified arrays are equals
     */
    public static boolean arrayFloatCompare(final float[][] array1, final float[][] array2) {
        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Array1DUtil.arrayFloatCompare(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * @param array1 2D array
     * @param array2 2D array
     * @return Return true is the specified arrays are equals
     */
    public static boolean arrayDoubleCompare(final double[][] array1, final double[][] array2) {
        final int len = array1.length;

        if (len != array2.length)
            return false;

        for (int i = 0; i < len; i++)
            if (!Array1DUtil.arrayDoubleCompare(array1[i], array2[i]))
                return false;

        return true;
    }

    /**
     * @param in 2D array
     * @return Return the multi dimension 'in' array as a single dimension byte array.
     */
    public static byte[] toByteArray1D(final byte[][] in) {
        return toByteArray1D(in, null, 0);
    }

    /**
     * @param in 2D array
     * @return Return the multi dimension 'in' array as a single dimension short array.
     */
    public static short[] toShortArray1D(final short[][] in) {
        return toShortArray1D(in, null, 0);
    }

    /**
     * @param in 2D array
     * @return Return the multi dimension 'in' array as a single dimension int array.
     */
    public static int[] toIntArray1D(final int[][] in) {
        return toIntArray1D(in, null, 0);
    }

    /**
     * @param in 2D array
     * @return Return the multi dimension 'in' array as a single dimension float array.
     */
    public static float[] toFloatArray1D(final float[][] in) {
        return toFloatArray1D(in, null, 0);
    }

    /**
     * @param in 2D array
     * @return Return the multi dimension 'in' array as a single dimension double array.
     */
    public static double[] toDoubleArray1D(final double[][] in) {
        return toDoubleArray1D(in, null, 0);
    }

    /**
     * @return Return the 2 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in 2D array
     * @param offset int
     */
    public static byte[] toByteArray1D(final byte[][] in, final byte[] out, final int offset) {
        final byte[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null) {
            int off = offset;
            for (final byte[] s_in : in) {
                if (s_in != null) {
                    Array1DUtil.toByteArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * @return Return the 2 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in 2D array
     * @param offset int
     */
    public static short[] toShortArray1D(final short[][] in, final short[] out, final int offset) {
        final short[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null) {
            int off = offset;
            for (final short[] s_in : in) {
                if (s_in != null) {
                    Array1DUtil.toShortArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * @return Return the 2 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in 2D array
     * @param offset int
     */
    public static int[] toIntArray1D(final int[][] in, final int[] out, final int offset) {
        final int[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null) {
            int off = offset;
            for (final int[] s_in : in) {
                if (s_in != null) {
                    Array1DUtil.toIntArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * @return Return the 2 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in 2D array
     * @param offset int
     */
    public static long[] toLongArray1D(final long[][] in, final long[] out, final int offset) {
        final long[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null) {
            int off = offset;
            for (final long[] s_in : in) {
                if (s_in != null) {
                    Array1DUtil.toLongArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * @return Return the 2 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in 2D array
     * @param offset int
     */
    public static float[] toFloatArray1D(final float[][] in, final float[] out, final int offset) {
        final float[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null) {
            int off = offset;
            for (final float[] s_in : in) {
                if (s_in != null) {
                    Array1DUtil.toFloatArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * @return Return the 2 dimensions 'in' array as a single dimension array.<br>
     * The resulting array is returned in 'out' and from the specified if any.<br>
     * @param out If (out == null) a new array is allocated.
     * @param in 2D array
     * @param offset int
     */
    public static double[] toDoubleArray1D(final double[][] in, final double[] out, final int offset) {
        final double[] result = Array1DUtil.allocIfNull(out, offset + getTotalLength(in));

        if (in != null) {
            int off = offset;
            for (final double[] s_in : in) {
                if (s_in != null) {
                    Array1DUtil.toDoubleArray1D(s_in, result, off);
                    off += s_in.length;
                }
            }
        }

        return result;
    }

    /**
     * @return Convert and return the 'in' 2D array in 'out' 2D array type.
     *
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        if input data are integer type then we assume them as signed data
     */
    public static Object arrayToArray(final Object in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT -> shortArrayToArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT -> intArrayToArray((int[][]) in, inOffset, out, outOffset, length, signed);
            case LONG -> longArrayToArray((long[][]) in, inOffset, out, outOffset, length, signed);
            case FLOAT -> floatArrayToArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToArray((double[][]) in, inOffset, out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @return Convert and return the 'in' 2D array in 'out' 2D array type.
     *
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        if input data are integer type then we assume them as signed data
     */
    public static Object arrayToArray(final Object in, final Object out, final boolean signed) {
        return arrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' double array in 'out' array type.<br>
     *
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     */
    public static Object doubleArrayToArray(final double[][] in, final int inOffset, final Object out, final int outOffset, final int length) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> doubleArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT -> doubleArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT -> doubleArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);
            case LONG -> doubleArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length);
            case FLOAT -> doubleArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);
            case DOUBLE -> doubleArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @return Convert and return the 'in' double array in 'out' array type.<br>
     *
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     */
    public static Object doubleArrayToArray(final double[][] in, final Object out) {
        return doubleArrayToArray(in, 0, out, 0, -1);
    }

    /**
     * @return Convert and return the 'in' float array in 'out' array type.<br>
     *
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     */
    public static Object floatArrayToArray(final float[][] in, final int inOffset, final Object out, final int outOffset, final int length) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> floatArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT -> floatArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT -> floatArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);
            case LONG -> floatArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length);
            case FLOAT -> floatArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length);
            case DOUBLE -> floatArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @return Convert and return the 'in' float array in 'out' array type.<br>
     *
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     */
    public static Object floatArrayToArray(final float[][] in, final Object out) {
        return floatArrayToArray(in, 0, out, 0, -1);
    }

    /**
     * @return Convert and return the 'in' long array in 'out' array type.<br>
     *
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static Object longArrayToArray(final long[][] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> longArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT -> longArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT -> longArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);
            case LONG -> longArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length);
            case FLOAT -> longArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);
            case DOUBLE -> longArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);
            default -> out;
        };
    }

    /**
     * @return Convert and return the 'in' long array in 'out' array type.<br>
     *
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static Object longArrayToArray(final long[][] in, final Object out, final boolean signed) {
        return longArrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' integer array in 'out' array type.<br>
     *
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static Object intArrayToArray(final int[][] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> intArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT -> intArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT -> intArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length);
            case LONG -> intArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);
            case FLOAT -> intArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);
            case DOUBLE -> intArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);
            default -> out;
        };
    }

    /**
     * @return Convert and return the 'in' integer array in 'out' array type.<br>
     *
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static Object intArrayToArray(final int[][] in, final Object out, final boolean signed) {
        return intArrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' short array in 'out' array type.<br>
     *
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static Object shortArrayToArray(final short[][] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> shortArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT -> shortArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length);
            case INT -> shortArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);
            case LONG -> shortArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);
            case FLOAT -> shortArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);
            case DOUBLE -> shortArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);
            default -> out;
        };
    }

    /**
     * @return Convert and return the 'in' short array in 'out' array type.<br>
     *
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static Object shortArrayToArray(final short[][] in, final Object out, final boolean signed) {
        return shortArrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' byte array in 'out' array type.<br>
     *
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static Object byteArrayToArray(final byte[][] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> byteArrayToByteArray(in, inOffset, (byte[][]) out, outOffset, length);
            case SHORT -> byteArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length, signed);
            case INT -> byteArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, signed);
            case LONG -> byteArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, signed);
            case FLOAT -> byteArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, signed);
            case DOUBLE -> byteArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, signed);
            default -> out;
        };
    }

    /**
     * @return Convert and return the 'in' byte array in 'out' array type.<br>
     *
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static Object byteArrayToArray(final byte[][] in, final Object out, final boolean signed) {
        return byteArrayToArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' array in 'out' double array.<br>
     *
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static double[][] arrayToDoubleArray(final Object in, final int inOffset, final double[][] out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToDoubleArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT -> shortArrayToDoubleArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT -> intArrayToDoubleArray((int[][]) in, inOffset, out, outOffset, length, signed);
            case LONG -> longArrayToDoubleArray((long[][]) in, inOffset, out, outOffset, length, signed);
            case FLOAT -> floatArrayToDoubleArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToDoubleArray((double[][]) in, inOffset, out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @return Convert and return the 'in' array in 'out' double array.<br>
     *
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static double[][] arrayToDoubleArray(final Object in, final double[][] out, final boolean signed) {
        return arrayToDoubleArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' array as a double array.<br>
     *
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     */
    public static double[][] arrayToDoubleArray(final Object in, final boolean signed) {
        return arrayToDoubleArray(in, 0, null, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' array in 'out' float array.<br>
     *
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static float[][] arrayToFloatArray(final Object in, final int inOffset, final float[][] out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToFloatArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT -> shortArrayToFloatArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT -> intArrayToFloatArray((int[][]) in, inOffset, out, outOffset, length, signed);
            case LONG -> longArrayToFloatArray((long[][]) in, inOffset, out, outOffset, length, signed);
            case FLOAT -> floatArrayToFloatArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToFloatArray((double[][]) in, inOffset, out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @return Convert and return the 'in' array in 'out' float array.<br>
     *
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static float[][] arrayToFloatArray(final Object in, final float[][] out, final boolean signed) {
        return arrayToFloatArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' array as a float array.<br>
     *
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     */
    public static float[][] arrayToFloatArray(final Object in, final boolean signed) {
        return arrayToFloatArray(in, 0, null, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' array in 'out' int array.<br>
     *
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static int[][] arrayToIntArray(final Object in, final int inOffset, final int[][] out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToIntArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT -> shortArrayToIntArray((short[][]) in, inOffset, out, outOffset, length, signed);
            case INT -> intArrayToIntArray((int[][]) in, inOffset, out, outOffset, length);
            case LONG -> longArrayToIntArray((long[][]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToIntArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToIntArray((double[][]) in, inOffset, out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @return Convert and return the 'in' array in 'out' int array.<br>
     *
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static int[][] arrayToIntArray(final Object in, final int[][] out, final boolean signed) {
        return arrayToIntArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' array as a int array.<br>
     *
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     */
    public static int[][] arrayToIntArray(final Object in, final boolean signed) {
        return arrayToIntArray(in, 0, null, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' array in 'out' short array.<br>
     *
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     * @param signed
     *        assume input data as signed data
     */
    public static short[][] arrayToShortArray(final Object in, final int inOffset, final short[][] out, final int outOffset, final int length, final boolean signed) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToShortArray((byte[][]) in, inOffset, out, outOffset, length, signed);
            case SHORT -> shortArrayToShortArray((short[][]) in, inOffset, out, outOffset, length);
            case INT -> intArrayToShortArray((int[][]) in, inOffset, out, outOffset, length);
            case LONG -> longArrayToShortArray((long[][]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToShortArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToShortArray((double[][]) in, inOffset, out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @return Convert and return the 'in' array in 'out' short array.<br>
     *
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param signed
     *        assume input data as signed data
     */
    public static short[][] arrayToShortArray(final Object in, final short[][] out, final boolean signed) {
        return arrayToShortArray(in, 0, out, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' array as a short array.<br>
     *
     * @param in
     *        input array
     * @param signed
     *        assume input data as signed data
     */
    public static short[][] arrayToShortArray(final Object in, final boolean signed) {
        return arrayToShortArray(in, 0, null, 0, -1, signed);
    }

    /**
     * @return Convert and return the 'in' array in 'out' byte array.<br>
     *
     * @param in
     *        input array
     * @param inOffset
     *        position where we start read data from
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     * @param outOffset
     *        position where we start to write data to
     * @param length
     *        number of value to convert (-1 means we will use the maximum possible length)
     */
    public static byte[][] arrayToByteArray(final Object in, final int inOffset, final byte[][] out, final int outOffset, final int length) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToByteArray((byte[][]) in, inOffset, out, outOffset, length);
            case SHORT -> shortArrayToByteArray((short[][]) in, inOffset, out, outOffset, length);
            case INT -> intArrayToByteArray((int[][]) in, inOffset, out, outOffset, length);
            case LONG -> longArrayToByteArray((long[][]) in, inOffset, out, outOffset, length);
            case FLOAT -> floatArrayToByteArray((float[][]) in, inOffset, out, outOffset, length);
            case DOUBLE -> doubleArrayToByteArray((double[][]) in, inOffset, out, outOffset, length);
            default -> out;
        };
    }

    /**
     * @return Convert and return the 'in' array in 'out' byte array.<br>
     *
     * @param in
     *        input array
     * @param out
     *        output array which is used to receive result (and so define wanted type)
     */
    public static byte[][] arrayToByteArray(final Object in, final byte[][] out) {
        return arrayToByteArray(in, 0, out, 0, -1);
    }

    /**
     * @return Convert and return the 'in' array as a byte array.<br>
     *
     * @param in
     *        input array
     */
    public static byte[][] arrayToByteArray(final Object in) {
        return arrayToByteArray(in, 0, null, 0, -1);
    }

    public static double[][] doubleArrayToDoubleArray(final double[][] in, final int inOffset, final double[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.doubleArrayToDoubleArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static float[][] doubleArrayToFloatArray(final double[][] in, final int inOffset, final float[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.doubleArrayToFloatArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static long[][] doubleArrayToLongArray(final double[][] in, final int inOffset, final long[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.doubleArrayToLongArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static int[][] doubleArrayToIntArray(final double[][] in, final int inOffset, final int[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.doubleArrayToIntArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static short[][] doubleArrayToShortArray(final double[][] in, final int inOffset, final short[][] out, final int outOffset,
                                                    final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.doubleArrayToShortArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static byte[][] doubleArrayToByteArray(final double[][] in, final int inOffset, final byte[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.doubleArrayToByteArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static double[][] floatArrayToDoubleArray(final float[][] in, final int inOffset, final double[][] out, final int outOffset,
                                                     final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.floatArrayToDoubleArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static float[][] floatArrayToFloatArray(final float[][] in, final int inOffset, final float[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.floatArrayToFloatArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static long[][] floatArrayToLongArray(final float[][] in, final int inOffset, final long[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.floatArrayToLongArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static int[][] floatArrayToIntArray(final float[][] in, final int inOffset, final int[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.floatArrayToIntArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static short[][] floatArrayToShortArray(final float[][] in, final int inOffset, final short[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.floatArrayToShortArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static byte[][] floatArrayToByteArray(final float[][] in, final int inOffset, final byte[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.floatArrayToByteArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static double[][] longArrayToDoubleArray(final long[][] in, final int inOffset, final double[][] out, final int outOffset,
                                                    final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.longArrayToDoubleArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static float[][] longArrayToFloatArray(final long[][] in, final int inOffset, final float[][] out, final int outOffset, final int length,
                                                  final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.longArrayToFloatArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static long[][] longArrayToLongArray(final long[][] in, final int inOffset, final long[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.longArrayToLongArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static int[][] longArrayToIntArray(final long[][] in, final int inOffset, final int[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.longArrayToIntArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static short[][] longArrayToShortArray(final long[][] in, final int inOffset, final short[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.longArrayToShortArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static byte[][] longArrayToByteArray(final long[][] in, final int inOffset, final byte[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.longArrayToByteArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static double[][] intArrayToDoubleArray(final int[][] in, final int inOffset, final double[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.intArrayToDoubleArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static float[][] intArrayToFloatArray(final int[][] in, final int inOffset, final float[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.intArrayToFloatArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static long[][] intArrayToLongArray(final int[][] in, final int inOffset, final long[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.intArrayToLongArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static int[][] intArrayToIntArray(final int[][] in, final int inOffset, final int[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.intArrayToIntArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static short[][] intArrayToShortArray(final int[][] in, final int inOffset, final short[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.intArrayToShortArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static byte[][] intArrayToByteArray(final int[][] in, final int inOffset, final byte[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.intArrayToByteArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static double[][] shortArrayToDoubleArray(final short[][] in, final int inOffset, final double[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.shortArrayToDoubleArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static float[][] shortArrayToFloatArray(final short[][] in, final int inOffset, final float[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.shortArrayToFloatArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static long[][] shortArrayToLongArray(final short[][] in, final int inOffset, final long[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.shortArrayToLongArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static int[][] shortArrayToIntArray(final short[][] in, final int inOffset, final int[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.shortArrayToIntArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static short[][] shortArrayToShortArray(final short[][] in, final int inOffset, final short[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.shortArrayToShortArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static byte[][] shortArrayToByteArray(final short[][] in, final int inOffset, final byte[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.shortArrayToByteArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1);

        return result;
    }

    public static double[][] byteArrayToDoubleArray(final byte[][] in, final int inOffset, final double[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final double[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.byteArrayToDoubleArray(in[i + inOffset], 0, result[i + outOffset], 0,
                    -1, signed);

        return result;
    }

    public static float[][] byteArrayToFloatArray(final byte[][] in, final int inOffset, final float[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final float[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.byteArrayToFloatArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static long[][] byteArrayToLongArray(final byte[][] in, final int inOffset, final long[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.byteArrayToLongArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static int[][] byteArrayToIntArray(final byte[][] in, final int inOffset, final int[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.byteArrayToIntArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static short[][] byteArrayToShortArray(final byte[][] in, final int inOffset, final short[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.byteArrayToShortArray(in[i + inOffset], 0, result[i + outOffset], 0, -1,
                    signed);

        return result;
    }

    public static byte[][] byteArrayToByteArray(final byte[][] in, final int inOffset, final byte[][] out, final int outOffset, final int length) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] result = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            result[i + outOffset] = Array1DUtil.byteArrayToByteArray(in[i + inOffset], 0, result[i + outOffset], 0, -1);

        return result;
    }

    public static float[][] doubleArrayToFloatArray(final double[][] array) {
        return doubleArrayToFloatArray(array, 0, null, 0, array.length);
    }

    public static int[][] doubleArrayToIntArray(final double[][] array) {
        return doubleArrayToIntArray(array, 0, null, 0, array.length);
    }

    public static short[][] doubleArrayToShortArray(final double[][] array) {
        return doubleArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte[][] doubleArrayToByteArray(final double[][] array) {
        return doubleArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[][] floatArrayToDoubleArray(final float[][] array) {
        return floatArrayToDoubleArray(array, 0, null, 0, array.length);
    }

    public static int[][] floatArrayToIntArray(final float[][] array) {
        return floatArrayToIntArray(array, 0, null, 0, array.length);
    }

    public static short[][] floatArrayToShortArray(final float[][] array) {
        return floatArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte[][] floatArrayToByteArray(final float[][] array) {
        return floatArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[][] intArrayToDoubleArray(final int[][] array, final boolean signed) {
        return intArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float[][] intArrayToFloatArray(final int[][] array, final boolean signed) {
        return intArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static short[][] intArrayToShortArray(final int[][] array) {
        return intArrayToShortArray(array, 0, null, 0, array.length);
    }

    public static byte[][] intArrayToByteArray(final int[][] array) {
        return intArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[][] shortArrayToDoubleArray(final short[][] array, final boolean signed) {
        return shortArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float[][] shortArrayToFloatArray(final short[][] array, final boolean signed) {
        return shortArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static int[][] shortArrayToIntArray(final short[][] array, final boolean signed) {
        return shortArrayToIntArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[][] shortArrayToByteArray(final short[][] array) {
        return shortArrayToByteArray(array, 0, null, 0, array.length);
    }

    public static double[][] byteArrayToDoubleArray(final byte[][] array, final boolean signed) {
        return byteArrayToDoubleArray(array, 0, null, 0, array.length, signed);
    }

    public static float[][] byteArrayToFloatArray(final byte[][] array, final boolean signed) {
        return byteArrayToFloatArray(array, 0, null, 0, array.length, signed);
    }

    public static int[][] byteArrayToIntArray(final byte[][] array, final boolean signed) {
        return byteArrayToIntArray(array, 0, null, 0, array.length, signed);
    }

    public static short[][] byteArrayToShortArray(final byte[][] array, final boolean signed) {
        return byteArrayToShortArray(array, 0, null, 0, array.length, signed);
    }

    public static Object doubleArrayToSafeArray(final double[][] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
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

    public static Object doubleArrayToSafeArray(final double[][] in, final Object out, final boolean signed) {
        return doubleArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object floatArrayToSafeArray(final float[][] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean signed) {
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

    public static Object floatArrayToSafeArray(final float[][] in, final Object out, final boolean signed) {
        return floatArrayToSafeArray(in, 0, out, 0, -1, signed);
    }

    public static Object longArrayToSafeArray(final long[][] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
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

    public static Object longArrayToSafeArray(final long[][] in, final Object out, final boolean srcSigned, final boolean dstSigned) {
        return longArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static Object intArrayToSafeArray(final int[][] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
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

    public static Object intArrayToSafeArray(final int[][] in, final Object out, final boolean srcSigned, final boolean dstSigned) {
        return intArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static Object shortArrayToSafeArray(final short[][] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
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

    public static Object shortArrayToSafeArray(final short[][] in, final Object out, final boolean srcSigned, final boolean dstSigned) {
        return shortArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static Object byteArrayToSafeArray(final byte[][] in, final int inOffset, final Object out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(out)) {
            case BYTE -> byteArrayToSafeByteArray(in, inOffset, (byte[][]) out, outOffset, length, srcSigned, dstSigned);
            case SHORT -> byteArrayToShortArray(in, inOffset, (short[][]) out, outOffset, length, srcSigned);
            case INT -> byteArrayToIntArray(in, inOffset, (int[][]) out, outOffset, length, srcSigned);
            case LONG -> byteArrayToLongArray(in, inOffset, (long[][]) out, outOffset, length, srcSigned);
            case FLOAT -> byteArrayToFloatArray(in, inOffset, (float[][]) out, outOffset, length, srcSigned);
            case DOUBLE -> byteArrayToDoubleArray(in, inOffset, (double[][]) out, outOffset, length, srcSigned);
            default -> out;
        };
    }

    public static Object byteArrayToSafeArray(final byte[][] in, final Object out, final boolean srcSigned, final boolean dstSigned) {
        return byteArrayToSafeArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static long[][] arrayToSafeLongArray(final Object in, final int inOffset, final long[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToLongArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT -> shortArrayToLongArray((short[][]) in, inOffset, out, outOffset, length, srcSigned);
            case INT -> intArrayToLongArray((int[][]) in, inOffset, out, outOffset, length, srcSigned);
            case LONG -> longArrayToSafeLongArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeLongArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeLongArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
            default -> out;
        };
    }

    public static long[][] arrayToSafeLongArray(final Object in, final long[][] out, final boolean srcSigned, final boolean dstSigned) {
        return arrayToSafeLongArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static int[][] arrayToSafeIntArray(final Object in, final int inOffset, final int[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToIntArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT -> shortArrayToIntArray((short[][]) in, inOffset, out, outOffset, length, srcSigned);
            case INT -> intArrayToSafeIntArray((int[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG -> longArrayToSafeIntArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeIntArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeIntArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
            default -> out;
        };
    }

    public static int[][] arrayToSafeIntArray(final Object in, final int[][] out, final boolean srcSigned, final boolean dstSigned) {
        return arrayToSafeIntArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static short[][] arrayToSafeShortArray(final Object in, final int inOffset, final short[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToShortArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned);
            case SHORT -> shortArrayToSafeShortArray((short[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case INT -> intArrayToSafeShortArray((int[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG -> longArrayToSafeShortArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeShortArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeShortArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
            default -> out;
        };
    }

    public static short[][] arrayToSafeShortArray(final Object in, final short[][] out, final boolean srcSigned, final boolean dstSigned) {
        return arrayToSafeShortArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static byte[][] arrayToSafeByteArray(final Object in, final int inOffset, final byte[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        return switch (ArrayUtil.getDataType(in)) {
            case BYTE -> byteArrayToSafeByteArray((byte[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case SHORT -> shortArrayToSafeByteArray((short[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case INT -> intArrayToSafeByteArray((int[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case LONG -> longArrayToSafeByteArray((long[][]) in, inOffset, out, outOffset, length, srcSigned, dstSigned);
            case FLOAT -> floatArrayToSafeByteArray((float[][]) in, inOffset, out, outOffset, length, dstSigned);
            case DOUBLE -> doubleArrayToSafeByteArray((double[][]) in, inOffset, out, outOffset, length, dstSigned);
            default -> out;
        };
    }

    public static byte[][] arrayToSafeByteArray(final Object in, final byte[][] out, final boolean srcSigned, final boolean dstSigned) {
        return arrayToSafeByteArray(in, 0, out, 0, -1, srcSigned, dstSigned);
    }

    public static long[][] doubleArrayToSafeLongArray(final double[][] in, final int inOffset, final long[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.doubleArrayToSafeLongArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

        return outArray;
    }

    public static int[][] doubleArrayToSafeIntArray(final double[][] in, final int inOffset, final int[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.doubleArrayToSafeIntArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

        return outArray;
    }

    public static short[][] doubleArrayToSafeShortArray(final double[][] in, final int inOffset, final short[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.doubleArrayToSafeShortArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

        return outArray;
    }

    public static byte[][] doubleArrayToSafeByteArray(final double[][] in, final int inOffset, final byte[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.doubleArrayToSafeByteArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

        return outArray;
    }

    public static long[][] floatArrayToSafeLongArray(final float[][] in, final int inOffset, final long[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.floatArrayToSafeLongArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

        return outArray;
    }

    public static int[][] floatArrayToSafeIntArray(final float[][] in, final int inOffset, final int[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.floatArrayToSafeIntArray(in[i + inOffset], 0, outArray[i + outOffset],
                    0, -1, signed);

        return outArray;
    }

    public static short[][] floatArrayToSafeShortArray(final float[][] in, final int inOffset, final short[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.floatArrayToSafeShortArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

        return outArray;
    }

    public static byte[][] floatArrayToSafeByteArray(final float[][] in, final int inOffset, final byte[][] out, final int outOffset, final int length, final boolean signed) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.floatArrayToSafeByteArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, signed);

        return outArray;
    }

    public static long[][] longArrayToSafeLongArray(final long[][] in, final int inOffset, final long[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final long[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.longArrayToSafeLongArray(in[i + inOffset], 0, outArray[i + outOffset],
                    0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static int[][] longArrayToSafeIntArray(final long[][] in, final int inOffset, final int[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.longArrayToSafeIntArray(in[i + inOffset], 0, outArray[i + outOffset],
                    0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static short[][] longArrayToSafeShortArray(final long[][] in, final int inOffset, final short[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.longArrayToSafeShortArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static byte[][] longArrayToSafeByteArray(final long[][] in, final int inOffset, final byte[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.longArrayToSafeByteArray(in[i + inOffset], 0, outArray[i + outOffset],
                    0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static int[][] intArrayToSafeIntArray(final int[][] in, final int inOffset, final int[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final int[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.intArrayToSafeIntArray(in[i + inOffset], 0, outArray[i + outOffset],
                    0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static short[][] intArrayToSafeShortArray(final int[][] in, final int inOffset, final short[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.intArrayToSafeShortArray(in[i + inOffset], 0, outArray[i + outOffset],
                    0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static byte[][] intArrayToSafeByteArray(final int[][] in, final int inOffset, final byte[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.intArrayToSafeByteArray(in[i + inOffset], 0, outArray[i + outOffset],
                    0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static short[][] shortArrayToSafeShortArray(final short[][] in, final int inOffset, final short[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final short[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.shortArrayToSafeShortArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static byte[][] shortArrayToSafeByteArray(final short[][] in, final int inOffset, final byte[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.shortArrayToSafeByteArray(in[i + inOffset], 0,
                    outArray[i + outOffset], 0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static byte[][] byteArrayToSafeByteArray(final byte[][] in, final int inOffset, final byte[][] out, final int outOffset, final int length, final boolean srcSigned, final boolean dstSigned) {
        final int len = ArrayUtil.getCopyLength(in, inOffset, out, outOffset, length);
        final byte[][] outArray = allocIfNull(out, outOffset + len);

        for (int i = 0; i < len; i++)
            outArray[i + outOffset] = Array1DUtil.byteArrayToSafeByteArray(in[i + inOffset], 0, outArray[i + outOffset],
                    0, -1, srcSigned, dstSigned);

        return outArray;
    }

    public static int[][] doubleArrayToSafeIntArray(final double[][] array, final boolean signed) {
        return doubleArrayToSafeIntArray(array, 0, null, 0, array.length, signed);
    }

    public static short[][] doubleArrayToSafeShortArray(final double[][] array, final boolean signed) {
        return doubleArrayToSafeShortArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[][] doubleArrayToSafeByteArray(final double[][] array, final boolean signed) {
        return doubleArrayToSafeByteArray(array, 0, null, 0, array.length, signed);
    }

    public static int[][] floatArrayToSafeIntArray(final float[][] array, final boolean signed) {
        return floatArrayToSafeIntArray(array, 0, null, 0, array.length, signed);
    }

    public static short[][] floatArrayToSafeShortArray(final float[][] array, final boolean signed) {
        return floatArrayToSafeShortArray(array, 0, null, 0, array.length, signed);
    }

    public static byte[][] floatArrayToSafeByteArray(final float[][] array, final boolean signed) {
        return floatArrayToSafeByteArray(array, 0, null, 0, array.length, signed);
    }

    public static short[][] intArrayToSafeShortArray(final int[][] array, final boolean signed) {
        return intArrayToSafeShortArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static byte[][] intArrayToSafeByteArray(final int[][] array, final boolean signed) {
        return intArrayToSafeByteArray(array, 0, null, 0, array.length, signed, signed);
    }

    public static byte[][] shortArrayToSafeByteArray(final short[][] array, final boolean signed) {
        return shortArrayToSafeByteArray(array, 0, null, 0, array.length, signed, signed);
    }
}
