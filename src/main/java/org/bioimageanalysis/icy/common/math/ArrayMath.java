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
package org.bioimageanalysis.icy.common.math;

import org.bioimageanalysis.icy.common.collection.array.Array1DUtil;
import org.bioimageanalysis.icy.common.collection.array.ArrayUtil;
import org.bioimageanalysis.icy.common.type.TypeUtil;

/**
 * Class defining basic arithmetic and statistic operations on 1D double arrays.
 *
 * @author Alexandre Dufour
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ArrayMath {
    /**
     * Element-wise addition of two arrays
     *
     * @param out
     *        the array receiving the result
     */
    public static Object add(final Object a1, final Object a2, final Object out) {
        return switch (ArrayUtil.getDataType(a1)) {
            case BYTE -> add((byte[]) a1, (byte[]) a2, (byte[]) out);
            case SHORT -> add((short[]) a1, (short[]) a2, (short[]) out);
            case INT -> add((int[]) a1, (int[]) a2, (int[]) out);
            case LONG -> add((long[]) a1, (long[]) a2, (long[]) out);
            case FLOAT -> add((float[]) a1, (float[]) a2, (float[]) out);
            case DOUBLE -> add((double[]) a1, (double[]) a2, (double[]) out);
            default -> null;
        };
    }

    /**
     * Element-wise addition of two arrays
     */
    public static Object add(final Object a1, final Object a2) {
        return add(a1, a2, null);
    }

    /**
     * Element-wise addition of two double arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static double[] add(final double[] a1, final double[] a2, final double[] out) {
        final double[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] + a2[i];

        return result;
    }

    /**
     * Element-wise addition of two double arrays
     */
    public static double[] add(final double[] a1, final double[] a2) {
        return add(a1, a2, null);
    }

    /**
     * Element-wise addition of two float arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static float[] add(final float[] a1, final float[] a2, final float[] out) {
        final float[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] + a2[i];

        return result;
    }

    /**
     * Element-wise addition of two float arrays
     */
    public static float[] add(final float[] a1, final float[] a2) {
        return add(a1, a2, null);
    }

    /**
     * Element-wise addition of two long arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static long[] add(final long[] a1, final long[] a2, final long[] out) {
        final long[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] + a2[i];

        return result;
    }

    /**
     * Element-wise addition of two long arrays
     */
    public static long[] add(final long[] a1, final long[] a2) {
        return add(a1, a2, null);
    }

    /**
     * Element-wise addition of two int arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static int[] add(final int[] a1, final int[] a2, final int[] out) {
        final int[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] + a2[i];

        return result;
    }

    /**
     * Element-wise addition of two int arrays
     */
    public static int[] add(final int[] a1, final int[] a2) {
        return add(a1, a2, null);
    }

    /**
     * Element-wise addition of two short arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static short[] add(final short[] a1, final short[] a2, final short[] out) {
        final short[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (short) (a1[i] + a2[i]);

        return result;
    }

    /**
     * Element-wise addition of two short arrays
     */
    public static short[] add(final short[] a1, final short[] a2) {
        return add(a1, a2, null);
    }

    /**
     * Element-wise addition of two byte arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static byte[] add(final byte[] a1, final byte[] a2, final byte[] out) {
        final byte[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (byte) (a1[i] + a2[i]);

        return result;
    }

    /**
     * Element-wise addition of two byte arrays
     */
    public static byte[] add(final byte[] a1, final byte[] a2) {
        return add(a1, a2, null);
    }

    /**
     * Adds a value to all elements of the given array
     *
     * @param out
     *        the array receiving the result
     */
    public static Object add(final Object array, final Number value, final Object out) {
        return switch (ArrayUtil.getDataType(array)) {
            case BYTE -> add((byte[]) array, value.byteValue(), (byte[]) out);
            case SHORT -> add((short[]) array, value.shortValue(), (short[]) out);
            case INT -> add((int[]) array, value.intValue(), (int[]) out);
            case LONG -> add((long[]) array, value.longValue(), (long[]) out);
            case FLOAT -> add((float[]) array, value.floatValue(), (float[]) out);
            case DOUBLE -> add((double[]) array, value.doubleValue(), (double[]) out);
            default -> null;
        };
    }

    /**
     * Adds a value to all elements of the given array
     */
    public static Object add(final Object array, final Number value) {
        return add(array, value, null);
    }

    /**
     * Adds a value to all elements of the given double array
     *
     * @param out
     *        the array receiving the result
     */
    public static double[] add(final double[] array, final double value, final double[] out) {
        final double[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] + value;

        return result;
    }

    /**
     * Adds a value to all elements of the given double array
     */
    public static double[] add(final double[] array, final double value) {
        return add(array, value, null);
    }

    /**
     * Adds a value to all elements of the given float array
     *
     * @param out
     *        the array receiving the result
     */
    public static float[] add(final float[] array, final float value, final float[] out) {
        final float[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] + value;

        return result;
    }

    /**
     * Adds a value to all elements of the float given array
     */
    public static float[] add(final float[] array, final float value) {
        return add(array, value, null);
    }

    /**
     * Adds a value to all elements of the given long array
     *
     * @param out
     *        the array receiving the result
     */
    public static long[] add(final long[] array, final long value, final long[] out) {
        final long[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] + value;

        return result;
    }

    /**
     * Adds a value to all elements of the given long array
     */
    public static long[] add(final long[] array, final long value) {
        return add(array, value, null);
    }

    /**
     * Adds a value to all elements of the given int array
     *
     * @param out
     *        the array receiving the result
     */
    public static int[] add(final int[] array, final int value, final int[] out) {
        final int[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] + value;

        return result;
    }

    /**
     * Adds a value to all elements of the given int array
     */
    public static int[] add(final int[] array, final int value) {
        return add(array, value, null);
    }

    /**
     * Adds a value to all elements of the given short array
     *
     * @param out
     *        the array receiving the result
     */
    public static short[] add(final short[] array, final short value, final short[] out) {
        final short[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (short) (array[i] + value);

        return result;
    }

    /**
     * Adds a value to all elements of the given short array
     */
    public static short[] add(final short[] array, final short value) {
        return add(array, value, null);
    }

    /**
     * Adds a value to all elements of the given byte array
     *
     * @param out
     *        the array receiving the result
     */
    public static byte[] add(final byte[] array, final byte value, final byte[] out) {
        final byte[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (byte) (array[i] + value);

        return result;
    }

    /**
     * Adds a value to all elements of the given byte array
     */
    public static byte[] add(final byte[] array, final byte value) {
        return add(array, value, null);
    }

    /**
     * Element-wise subtraction of two arrays
     *
     * @param out
     *        the array receiving the result
     */
    public static Object subtract(final Object a1, final Object a2, final Object out) {
        return switch (ArrayUtil.getDataType(a1)) {
            case BYTE -> subtract((byte[]) a1, (byte[]) a2, (byte[]) out);
            case SHORT -> subtract((short[]) a1, (short[]) a2, (short[]) out);
            case INT -> subtract((int[]) a1, (int[]) a2, (int[]) out);
            case LONG -> subtract((long[]) a1, (long[]) a2, (long[]) out);
            case FLOAT -> subtract((float[]) a1, (float[]) a2, (float[]) out);
            case DOUBLE -> subtract((double[]) a1, (double[]) a2, (double[]) out);
            default -> null;
        };
    }

    /**
     * Element-wise subtraction of two arrays
     */
    public static Object subtract(final Object a1, final Object a2) {
        return subtract(a1, a2, null);
    }

    /**
     * Element-wise subtraction of two double arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static double[] subtract(final double[] a1, final double[] a2, final double[] out) {
        final double[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] - a2[i];

        return result;
    }

    /**
     * Element-wise subtraction of two double arrays
     */
    public static double[] subtract(final double[] a1, final double[] a2) {
        return subtract(a1, a2, null);
    }

    /**
     * Element-wise subtraction of two float arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static float[] subtract(final float[] a1, final float[] a2, final float[] out) {
        final float[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] - a2[i];

        return result;
    }

    /**
     * Element-wise subtraction of two float arrays
     */
    public static float[] subtract(final float[] a1, final float[] a2) {
        return subtract(a1, a2, null);
    }

    /**
     * Element-wise subtraction of two long arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static long[] subtract(final long[] a1, final long[] a2, final long[] out) {
        final long[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] - a2[i];

        return result;
    }

    /**
     * Element-wise subtraction of two long arrays
     */
    public static long[] subtract(final long[] a1, final long[] a2) {
        return subtract(a1, a2, null);
    }

    /**
     * Element-wise subtraction of two int arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static int[] subtract(final int[] a1, final int[] a2, final int[] out) {
        final int[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] - a2[i];

        return result;
    }

    /**
     * Element-wise subtraction of two int arrays
     */
    public static int[] subtract(final int[] a1, final int[] a2) {
        return subtract(a1, a2, null);
    }

    /**
     * Element-wise subtraction of two short arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static short[] subtract(final short[] a1, final short[] a2, final short[] out) {
        final short[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (short) (a1[i] - a2[i]);

        return result;
    }

    /**
     * Element-wise subtraction of two short arrays
     */
    public static short[] subtract(final short[] a1, final short[] a2) {
        return subtract(a1, a2, null);
    }

    /**
     * Element-wise subtraction of two byte arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static byte[] subtract(final byte[] a1, final byte[] a2, final byte[] out) {
        final byte[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (byte) (a1[i] - a2[i]);

        return result;
    }

    /**
     * Element-wise subtraction of two byte arrays
     */
    public static byte[] subtract(final byte[] a1, final byte[] a2) {
        return subtract(a1, a2, null);
    }

    /**
     * Subtracts a value to all elements of the given array
     *
     * @param out
     *        the array receiving the result
     */
    public static Object subtract(final Object array, final Number value, final Object out) {
        return switch (ArrayUtil.getDataType(array)) {
            case BYTE -> subtract((byte[]) array, value.byteValue(), (byte[]) out);
            case SHORT -> subtract((short[]) array, value.shortValue(), (short[]) out);
            case INT -> subtract((int[]) array, value.intValue(), (int[]) out);
            case LONG -> subtract((long[]) array, value.longValue(), (long[]) out);
            case FLOAT -> subtract((float[]) array, value.floatValue(), (float[]) out);
            case DOUBLE -> subtract((double[]) array, value.doubleValue(), (double[]) out);
            default -> null;
        };
    }

    /**
     * Subtracts a value to all elements of the given array
     */
    public static Object subtract(final Object array, final Number value) {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value to all elements of the given double array
     *
     * @param out
     *        the array receiving the result
     */
    public static double[] subtract(final double[] array, final double value, final double[] out) {
        final double[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] - value;

        return result;
    }

    /**
     * Subtracts a value to all elements of the given double array
     */
    public static double[] subtract(final double[] array, final double value) {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value to all elements of the given float array
     *
     * @param out
     *        the array receiving the result
     */
    public static float[] subtract(final float[] array, final float value, final float[] out) {
        final float[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] - value;

        return result;
    }

    /**
     * Subtracts a value to all elements of the float given array
     */
    public static float[] subtract(final float[] array, final float value) {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value to all elements of the given long array
     *
     * @param out
     *        the array receiving the result
     */
    public static long[] subtract(final long[] array, final long value, final long[] out) {
        final long[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] - value;

        return result;
    }

    /**
     * Subtracts a value to all elements of the given long array
     */
    public static long[] subtract(final long[] array, final long value) {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value to all elements of the given int array
     *
     * @param out
     *        the array receiving the result
     */
    public static int[] subtract(final int[] array, final int value, final int[] out) {
        final int[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] - value;

        return result;
    }

    /**
     * Subtracts a value to all elements of the given int array
     */
    public static int[] subtract(final int[] array, final int value) {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value to all elements of the given short array
     *
     * @param out
     *        the array receiving the result
     */
    public static short[] subtract(final short[] array, final short value, final short[] out) {
        final short[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (short) (array[i] - value);

        return result;
    }

    /**
     * Subtracts a value to all elements of the given short array
     */
    public static short[] subtract(final short[] array, final short value) {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value to all elements of the given byte array
     *
     * @param out
     *        the array receiving the result
     */
    public static byte[] subtract(final byte[] array, final byte value, final byte[] out) {
        final byte[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (byte) (array[i] - value);

        return result;
    }

    /**
     * Subtracts a value to all elements of the given byte array
     */
    public static byte[] subtract(final byte[] array, final byte value) {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value by all elements of the given array
     *
     * @param out
     *        the array receiving the result
     */
    public static Object subtract(final Number value, final Object array, final Object out) {
        return switch (ArrayUtil.getDataType(array)) {
            case BYTE -> subtract(value.byteValue(), (byte[]) array, (byte[]) out);
            case SHORT -> subtract(value.shortValue(), (short[]) array, (short[]) out);
            case INT -> subtract(value.intValue(), (int[]) array, (int[]) out);
            case LONG -> subtract(value.longValue(), (long[]) array, (long[]) out);
            case FLOAT -> subtract(value.floatValue(), (float[]) array, (float[]) out);
            case DOUBLE -> subtract(value.doubleValue(), (double[]) array, (double[]) out);
            default -> null;
        };
    }

    /**
     * Subtracts a value by all elements of the given array
     */
    public static Object subtract(final Number value, final Object array) {
        return subtract(value, array, null);
    }

    /**
     * Subtracts a value by all elements of the given double array
     *
     * @param out
     *        the array receiving the result
     */
    public static double[] subtract(final double value, final double[] array, final double[] out) {
        final double[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value - array[i];

        return result;
    }

    /**
     * Subtracts a value by all elements of the given double array
     */
    public static double[] subtract(final double value, final double[] array) {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value by all elements of the given float array
     *
     * @param out
     *        the array receiving the result
     */
    public static float[] subtract(final float value, final float[] array, final float[] out) {
        final float[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value - array[i];

        return result;
    }

    /**
     * Subtracts a value by all elements of the float given array
     */
    public static float[] subtract(final float value, final float[] array) {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value by all elements of the given long array
     *
     * @param out
     *        the array receiving the result
     */
    public static long[] subtract(final long value, final long[] array, final long[] out) {
        final long[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value - array[i];

        return result;
    }

    /**
     * Subtracts a value by all elements of the given long array
     */
    public static long[] subtract(final long value, final long[] array) {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value by all elements of the given int array
     *
     * @param out
     *        the array receiving the result
     */
    public static int[] subtract(final int value, final int[] array, final int[] out) {
        final int[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value - array[i];

        return result;
    }

    /**
     * Subtracts a value by all elements of the given int array
     */
    public static int[] subtract(final int value, final int[] array) {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value by all elements of the given short array
     *
     * @param out
     *        the array receiving the result
     */
    public static short[] subtract(final short value, final short[] array, final short[] out) {
        final short[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (short) (value - array[i]);

        return result;
    }

    /**
     * Subtracts a value by all elements of the given short array
     */
    public static short[] subtract(final short value, final short[] array) {
        return subtract(array, value, null);
    }

    /**
     * Subtracts a value by all elements of the given byte array
     *
     * @param out
     *        the array receiving the result
     */
    public static byte[] subtract(final byte value, final byte[] array, final byte[] out) {
        final byte[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (byte) (value - array[i]);

        return result;
    }

    /**
     * Subtracts a value by all elements of the given byte array
     */
    public static byte[] subtract(final byte value, final byte[] array) {
        return subtract(array, value, null);
    }

    /**
     * Element-wise multiplication of two arrays
     *
     * @param out
     *        the array receiving the result
     */
    public static Object multiply(final Object a1, final Object a2, final Object out) {
        return switch (ArrayUtil.getDataType(a1)) {
            case BYTE -> multiply((byte[]) a1, (byte[]) a2, (byte[]) out);
            case SHORT -> multiply((short[]) a1, (short[]) a2, (short[]) out);
            case INT -> multiply((int[]) a1, (int[]) a2, (int[]) out);
            case LONG -> multiply((long[]) a1, (long[]) a2, (long[]) out);
            case FLOAT -> multiply((float[]) a1, (float[]) a2, (float[]) out);
            case DOUBLE -> multiply((double[]) a1, (double[]) a2, (double[]) out);
            default -> null;
        };
    }

    /**
     * Element-wise multiplication of two arrays
     */
    public static Object multiply(final Object a1, final Object a2) {
        return multiply(a1, a2, null);
    }

    /**
     * Element-wise multiplication of two double arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static double[] multiply(final double[] a1, final double[] a2, final double[] out) {
        final double[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] * a2[i];

        return result;
    }

    /**
     * Element-wise multiplication of two double arrays
     */
    public static double[] multiply(final double[] a1, final double[] a2) {
        return multiply(a1, a2, null);
    }

    /**
     * Element-wise multiplication of two float arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static float[] multiply(final float[] a1, final float[] a2, final float[] out) {
        final float[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] * a2[i];

        return result;
    }

    /**
     * Element-wise multiplication of two float arrays
     */
    public static float[] multiply(final float[] a1, final float[] a2) {
        return multiply(a1, a2, null);
    }

    /**
     * Element-wise multiplication of two long arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static long[] multiply(final long[] a1, final long[] a2, final long[] out) {
        final long[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] * a2[i];

        return result;
    }

    /**
     * Element-wise multiplication of two long arrays
     */
    public static long[] multiply(final long[] a1, final long[] a2) {
        return multiply(a1, a2, null);
    }

    /**
     * Element-wise multiplication of two int arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static int[] multiply(final int[] a1, final int[] a2, final int[] out) {
        final int[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] * a2[i];

        return result;
    }

    /**
     * Element-wise multiplication of two int arrays
     */
    public static int[] multiply(final int[] a1, final int[] a2) {
        return multiply(a1, a2, null);
    }

    /**
     * Element-wise multiplication of two short arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static short[] multiply(final short[] a1, final short[] a2, final short[] out) {
        final short[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (short) (a1[i] * a2[i]);

        return result;
    }

    /**
     * Element-wise multiplication of two short arrays
     */
    public static short[] multiply(final short[] a1, final short[] a2) {
        return multiply(a1, a2, null);
    }

    /**
     * Element-wise multiplication of two byte arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static byte[] multiply(final byte[] a1, final byte[] a2, final byte[] out) {
        final byte[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (byte) (a1[i] * a2[i]);

        return result;
    }

    /**
     * Element-wise multiplication of two byte arrays
     */
    public static byte[] multiply(final byte[] a1, final byte[] a2) {
        return multiply(a1, a2, null);
    }

    /**
     * Multiplies a value to all elements of the given array
     *
     * @param out
     *        the array receiving the result
     */
    public static Object multiply(final Object array, final Number value, final Object out) {
        return switch (ArrayUtil.getDataType(array)) {
            case BYTE -> multiply((byte[]) array, value.byteValue(), (byte[]) out);
            case SHORT -> multiply((short[]) array, value.shortValue(), (short[]) out);
            case INT -> multiply((int[]) array, value.intValue(), (int[]) out);
            case LONG -> multiply((long[]) array, value.longValue(), (long[]) out);
            case FLOAT -> multiply((float[]) array, value.floatValue(), (float[]) out);
            case DOUBLE -> multiply((double[]) array, value.doubleValue(), (double[]) out);
            default -> null;
        };
    }

    /**
     * Multiplies a value to all elements of the given array
     */
    public static Object multiply(final Object array, final Number value) {
        return multiply(array, value, null);
    }

    /**
     * Multiplies a value to all elements of the given double array
     *
     * @param out
     *        the array receiving the result
     */
    public static double[] multiply(final double[] array, final double value, final double[] out) {
        final double[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] * value;

        return result;
    }

    /**
     * Multiplies a value to all elements of the given double array
     */
    public static double[] multiply(final double[] array, final double value) {
        return multiply(array, value, null);
    }

    /**
     * Multiplies a value to all elements of the given float array
     *
     * @param out
     *        the array receiving the result
     */
    public static float[] multiply(final float[] array, final float value, final float[] out) {
        final float[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] * value;

        return result;
    }

    /**
     * Multiplies a value to all elements of the float given array
     */
    public static float[] multiply(final float[] array, final float value) {
        return multiply(array, value, null);
    }

    /**
     * Multiplies a value to all elements of the given long array
     *
     * @param out
     *        the array receiving the result
     */
    public static long[] multiply(final long[] array, final long value, final long[] out) {
        final long[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] * value;

        return result;
    }

    /**
     * Multiplies a value to all elements of the given long array
     */
    public static long[] multiply(final long[] array, final long value) {
        return multiply(array, value, null);
    }

    /**
     * Multiplies a value to all elements of the given int array
     *
     * @param out
     *        the array receiving the result
     */
    public static int[] multiply(final int[] array, final int value, final int[] out) {
        final int[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] * value;

        return result;
    }

    /**
     * Multiplies a value to all elements of the given int array
     */
    public static int[] multiply(final int[] array, final int value) {
        return multiply(array, value, null);
    }

    /**
     * Multiplies a value to all elements of the given short array
     *
     * @param out
     *        the array receiving the result
     */
    public static short[] multiply(final short[] array, final short value, final short[] out) {
        final short[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (short) (array[i] * value);

        return result;
    }

    /**
     * Multiplies a value to all elements of the given short array
     */
    public static short[] multiply(final short[] array, final short value) {
        return multiply(array, value, null);
    }

    /**
     * Multiplies a value to all elements of the given byte array
     *
     * @param out
     *        the array receiving the result
     */
    public static byte[] multiply(final byte[] array, final byte value, final byte[] out) {
        final byte[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (byte) (array[i] * value);

        return result;
    }

    /**
     * Multiplies a value to all elements of the given byte array
     */
    public static byte[] multiply(final byte[] array, final byte value) {
        return multiply(array, value, null);
    }

    /**
     * Element-wise division of two arrays
     *
     * @param out
     *        the array receiving the result
     */
    public static Object divide(final Object a1, final Object a2, final Object out) {
        return switch (ArrayUtil.getDataType(a1)) {
            case BYTE -> divide((byte[]) a1, (byte[]) a2, (byte[]) out);
            case SHORT -> divide((short[]) a1, (short[]) a2, (short[]) out);
            case INT -> divide((int[]) a1, (int[]) a2, (int[]) out);
            case LONG -> divide((long[]) a1, (long[]) a2, (long[]) out);
            case FLOAT -> divide((float[]) a1, (float[]) a2, (float[]) out);
            case DOUBLE -> divide((double[]) a1, (double[]) a2, (double[]) out);
            default -> null;
        };
    }

    /**
     * Element-wise division of two arrays
     */
    public static Object divide(final Object a1, final Object a2) {
        return divide(a1, a2, null);
    }

    /**
     * Element-wise division of two double arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static double[] divide(final double[] a1, final double[] a2, final double[] out) {
        final double[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] / a2[i];

        return result;
    }

    /**
     * Element-wise division of two double arrays
     */
    public static double[] divide(final double[] a1, final double[] a2) {
        return divide(a1, a2, null);
    }

    /**
     * Element-wise division of two float arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static float[] divide(final float[] a1, final float[] a2, final float[] out) {
        final float[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] / a2[i];

        return result;
    }

    /**
     * Element-wise division of two float arrays
     */
    public static float[] divide(final float[] a1, final float[] a2) {
        return divide(a1, a2, null);
    }

    /**
     * Element-wise division of two long arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static long[] divide(final long[] a1, final long[] a2, final long[] out) {
        final long[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] / a2[i];

        return result;
    }

    /**
     * Element-wise division of two long arrays
     */
    public static long[] divide(final long[] a1, final long[] a2) {
        return divide(a1, a2, null);
    }

    /**
     * Element-wise division of two int arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static int[] divide(final int[] a1, final int[] a2, final int[] out) {
        final int[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = a1[i] / a2[i];

        return result;
    }

    /**
     * Element-wise division of two int arrays
     */
    public static int[] divide(final int[] a1, final int[] a2) {
        return divide(a1, a2, null);
    }

    /**
     * Element-wise division of two short arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static short[] divide(final short[] a1, final short[] a2, final short[] out) {
        final short[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (short) (a1[i] / a2[i]);

        return result;
    }

    /**
     * Element-wise division of two short arrays
     */
    public static short[] divide(final short[] a1, final short[] a2) {
        return divide(a1, a2, null);
    }

    /**
     * Element-wise division of two byte arrays (result in output if defined)
     *
     * @param out
     *        the array receiving the result
     */
    public static byte[] divide(final byte[] a1, final byte[] a2, final byte[] out) {
        final byte[] result = Array1DUtil.allocIfNull(out, a1.length);

        for (int i = 0; i < a1.length; i++)
            result[i] = (byte) (a1[i] / a2[i]);

        return result;
    }

    /**
     * Element-wise division of two byte arrays
     */
    public static byte[] divide(final byte[] a1, final byte[] a2) {
        return divide(a1, a2, null);
    }

    /**
     * Divides a value to all elements of the given array
     *
     * @param out
     *        the array receiving the result
     */
    public static Object divide(final Object array, final Number value, final Object out) {
        return switch (ArrayUtil.getDataType(array)) {
            case BYTE -> divide((byte[]) array, value.byteValue(), (byte[]) out);
            case SHORT -> divide((short[]) array, value.shortValue(), (short[]) out);
            case INT -> divide((int[]) array, value.intValue(), (int[]) out);
            case LONG -> divide((long[]) array, value.longValue(), (long[]) out);
            case FLOAT -> divide((float[]) array, value.floatValue(), (float[]) out);
            case DOUBLE -> divide((double[]) array, value.doubleValue(), (double[]) out);
            default -> null;
        };
    }

    /**
     * Divides a value to all elements of the given array
     */
    public static Object divide(final Object array, final Number value) {
        return divide(array, value, null);
    }

    /**
     * Divides a value to all elements of the given double array
     *
     * @param out
     *        the array receiving the result
     */
    public static double[] divide(final double[] array, final double value, final double[] out) {
        final double[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] / value;

        return result;
    }

    /**
     * Divides a value to all elements of the given double array
     */
    public static double[] divide(final double[] array, final double value) {
        return divide(array, value, null);
    }

    /**
     * Divides a value to all elements of the given float array
     *
     * @param out
     *        the array receiving the result
     */
    public static float[] divide(final float[] array, final float value, final float[] out) {
        final float[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] / value;

        return result;
    }

    /**
     * Divides a value to all elements of the float given array
     */
    public static float[] divide(final float[] array, final float value) {
        return divide(array, value, null);
    }

    /**
     * Divides a value to all elements of the given long array
     *
     * @param out
     *        the array receiving the result
     */
    public static long[] divide(final long[] array, final long value, final long[] out) {
        final long[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] / value;

        return result;
    }

    /**
     * Divides a value to all elements of the given long array
     */
    public static long[] divide(final long[] array, final long value) {
        return divide(array, value, null);
    }

    /**
     * Divides a value to all elements of the given int array
     *
     * @param out
     *        the array receiving the result
     */
    public static int[] divide(final int[] array, final int value, final int[] out) {
        final int[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = array[i] / value;

        return result;
    }

    /**
     * Divides a value to all elements of the given int array
     */
    public static int[] divide(final int[] array, final int value) {
        return divide(array, value, null);
    }

    /**
     * Divides a value to all elements of the given short array
     *
     * @param out
     *        the array receiving the result
     */
    public static short[] divide(final short[] array, final short value, final short[] out) {
        final short[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (short) (array[i] / value);

        return result;
    }

    /**
     * Divides a value to all elements of the given short array
     */
    public static short[] divide(final short[] array, final short value) {
        return divide(array, value, null);
    }

    /**
     * Divides a value to all elements of the given byte array
     *
     * @param out
     *        the array receiving the result
     */
    public static byte[] divide(final byte[] array, final byte value, final byte[] out) {
        final byte[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (byte) (array[i] / value);

        return result;
    }

    /**
     * Divides a value to all elements of the given byte array
     */
    public static byte[] divide(final byte[] array, final byte value) {
        return divide(array, value, null);
    }

    /**
     * Divides a value by all elements of the given array
     *
     * @param out
     *        the array receiving the result
     */
    public static Object divide(final Number value, final Object array, final Object out) {
        return switch (ArrayUtil.getDataType(array)) {
            case BYTE -> divide(value.byteValue(), (byte[]) array, (byte[]) out);
            case SHORT -> divide(value.shortValue(), (short[]) array, (short[]) out);
            case INT -> divide(value.intValue(), (int[]) array, (int[]) out);
            case LONG -> divide(value.longValue(), (long[]) array, (long[]) out);
            case FLOAT -> divide(value.floatValue(), (float[]) array, (float[]) out);
            case DOUBLE -> divide(value.doubleValue(), (double[]) array, (double[]) out);
            default -> null;
        };
    }

    /**
     * Divides a value by all elements of the given array
     */
    public static Object divide(final Number value, final Object array) {
        return divide(value, array, null);
    }

    /**
     * Subtracts a value by all elements of the given double array
     *
     * @param out
     *        the array receiving the result
     */
    public static double[] divide(final double value, final double[] array, final double[] out) {
        final double[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value / array[i];

        return result;
    }

    /**
     * Divides a value by all elements of the given double array
     */
    public static double[] divide(final double value, final double[] array) {
        return divide(array, value, null);
    }

    /**
     * Divides a value by all elements of the given float array
     *
     * @param out
     *        the array receiving the result
     */
    public static float[] divide(final float value, final float[] array, final float[] out) {
        final float[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value / array[i];

        return result;
    }

    /**
     * Divides a value by all elements of the float given array
     */
    public static float[] divide(final float value, final float[] array) {
        return divide(array, value, null);
    }

    /**
     * Divides a value by all elements of the given long array
     *
     * @param out
     *        the array receiving the result
     */
    public static long[] divide(final long value, final long[] array, final long[] out) {
        final long[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value / array[i];

        return result;
    }

    /**
     * Divides a value by all elements of the given long array
     */
    public static long[] divide(final long value, final long[] array) {
        return divide(array, value, null);
    }

    /**
     * Divides a value by all elements of the given int array
     *
     * @param out
     *        the array receiving the result
     */
    public static int[] divide(final int value, final int[] array, final int[] out) {
        final int[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = value / array[i];

        return result;
    }

    /**
     * Divides a value by all elements of the given int array
     */
    public static int[] divide(final int value, final int[] array) {
        return divide(array, value, null);
    }

    /**
     * Divides a value by all elements of the given short array
     *
     * @param out
     *        the array receiving the result
     */
    public static short[] divide(final short value, final short[] array, final short[] out) {
        final short[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (short) (value / array[i]);

        return result;
    }

    /**
     * Divides a value by all elements of the given short array
     */
    public static short[] divide(final short value, final short[] array) {
        return divide(array, value, null);
    }

    /**
     * Divides a value by all elements of the given byte array
     *
     * @param out
     *        the array receiving the result
     */
    public static byte[] divide(final byte value, final byte[] array, final byte[] out) {
        final byte[] result = Array1DUtil.allocIfNull(out, array.length);

        for (int i = 0; i < array.length; i++)
            result[i] = (byte) (value / array[i]);

        return result;
    }

    /**
     * Divides a value by all elements of the given byte array
     */
    public static byte[] divide(final byte value, final byte[] array) {
        return divide(array, value, null);
    }

    /**
     * Computes the absolute value of each value of the given array
     *
     * @param overwrite
     *        true : overwrites the input data<br>
     *        false: returns the result in a new array
     */
    public static Object abs(final Object array, final boolean overwrite) {
        return switch (ArrayUtil.getDataType(array)) {
            case BYTE -> abs((byte[]) array, overwrite);
            case SHORT -> abs((short[]) array, overwrite);
            case INT -> abs((int[]) array, overwrite);
            case LONG -> abs((long[]) array, overwrite);
            case FLOAT -> abs((float[]) array, overwrite);
            case DOUBLE -> abs((double[]) array, overwrite);
            default -> null;
        };
    }

    /**
     * Computes the absolute value of each value of the given double array
     *
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static double[] abs(final double[] input, final boolean overwrite) {
        final double[] result = overwrite ? input : new double[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = Math.abs(input[i]);

        return result;
    }

    /**
     * Computes the absolute value of each value of the given float array
     *
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static float[] abs(final float[] input, final boolean overwrite) {
        final float[] result = overwrite ? input : new float[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = Math.abs(input[i]);

        return result;
    }

    /**
     * Computes the absolute value of each value of the given long array
     *
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static long[] abs(final long[] input, final boolean overwrite) {
        final long[] result = overwrite ? input : new long[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = Math.abs(input[i]);

        return result;
    }

    /**
     * Computes the absolute value of each value of the given int array
     *
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static int[] abs(final int[] input, final boolean overwrite) {
        final int[] result = overwrite ? input : new int[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = Math.abs(input[i]);

        return result;
    }

    /**
     * Computes the absolute value of each value of the given short array
     *
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static short[] abs(final short[] input, final boolean overwrite) {
        final short[] result = overwrite ? input : new short[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = (short) Math.abs(input[i]);

        return result;
    }

    /**
     * Computes the absolute value of each value of the given byte array
     *
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static byte[] abs(final byte[] input, final boolean overwrite) {
        final byte[] result = overwrite ? input : new byte[input.length];

        for (int i = 0; i < input.length; i++)
            result[i] = (byte) Math.abs(input[i]);

        return result;
    }

    /**
     * Find the minimum value of a generic array
     *
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the min value of the array
     */
    public static double min(final Object array, final boolean signed) {
        return switch (ArrayUtil.getDataType(array)) {
            case BYTE -> min((byte[]) array, signed);
            case SHORT -> min((short[]) array, signed);
            case INT -> min((int[]) array, signed);
            case LONG -> min((long[]) array, signed);
            case FLOAT -> min((float[]) array);
            case DOUBLE -> min((double[]) array);
            default -> 0;
        };
    }

    /**
     * Find the minimum value of an array
     *
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the min value of the array
     */
    public static int min(final byte[] array, final boolean signed) {
        if (signed) {
            byte min = Byte.MAX_VALUE;

            for (final byte v : array)
                if (v < min)
                    min = v;

            return min;
        }

        int min = Integer.MAX_VALUE;

        for (final byte b : array) {
            final int v = TypeUtil.unsign(b);
            if (v < min)
                min = v;
        }

        return min;
    }

    /**
     * Find the minimum value of an array
     *
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the min value of the array
     */
    public static int min(final short[] array, final boolean signed) {
        if (signed) {
            short min = Short.MAX_VALUE;

            for (final short v : array)
                if (v < min)
                    min = v;

            return min;
        }

        int min = Integer.MAX_VALUE;

        for (final short value : array) {
            final int v = TypeUtil.unsign(value);
            if (v < min)
                min = v;
        }

        return min;
    }

    /**
     * Find the minimum value of an array
     *
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the min value of the array
     */
    public static long min(final int[] array, final boolean signed) {
        if (signed) {
            int min = Integer.MAX_VALUE;

            for (final int v : array)
                if (v < min)
                    min = v;

            return min;
        }

        long min = Long.MAX_VALUE;

        for (final int j : array) {
            final long v = TypeUtil.unsign(j);
            if (v < min)
                min = v;
        }

        return min;
    }

    /**
     * Find the minimum value of an array
     *
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the min value of the array
     */
    public static long min(final long[] array, final boolean signed) {
        if (signed) {
            long min = Integer.MAX_VALUE;

            for (final long v : array)
                if (v < min)
                    min = v;

            return min;
        }

        double min = Long.MAX_VALUE;

        for (final long l : array) {
            final double v = TypeUtil.unsign(l);
            // need to compare in double
            if (v < min)
                min = v;
        }

        // convert back to long (need to be interpreted as unsigned)
        return TypeUtil.toLong(min);
    }

    /**
     * Find the minimum value of an array
     *
     * @param array
     *        an array
     * @return the min value of the array
     */
    public static float min(final float[] array) {
        float min = Float.MAX_VALUE;

        for (final float v : array)
            if (v < min)
                min = v;

        return min;
    }

    /**
     * Find the minimum value of an array
     *
     * @param array
     *        an array
     * @return the min value of the array
     */
    public static double min(final double[] array) {
        double min = Double.MAX_VALUE;

        for (final double v : array)
            if (v < min)
                min = v;

        return min;
    }

    /**
     * Find the maximum value of a generic array
     *
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the max value of the array
     */
    public static double max(final Object array, final boolean signed) {
        return switch (ArrayUtil.getDataType(array)) {
            case BYTE -> max((byte[]) array, signed);
            case SHORT -> max((short[]) array, signed);
            case INT -> max((int[]) array, signed);
            case LONG -> max((long[]) array, signed);
            case FLOAT -> max((float[]) array);
            case DOUBLE -> max((double[]) array);
            default -> 0;
        };
    }

    /**
     * Find the maximum value of an array
     *
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the max value of the array
     */
    public static int max(final byte[] array, final boolean signed) {
        if (signed) {
            byte max = Byte.MIN_VALUE;

            for (final byte v : array)
                if (v > max)
                    max = v;

            return max;
        }

        int max = Integer.MIN_VALUE;

        for (final byte b : array) {
            final int v = TypeUtil.unsign(b);
            if (v > max)
                max = v;
        }

        return max;
    }

    /**
     * Find the maximum value of an array
     *
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the max value of the array
     */
    public static int max(final short[] array, final boolean signed) {
        if (signed) {
            short max = Short.MIN_VALUE;

            for (final short v : array)
                if (v > max)
                    max = v;

            return max;
        }

        int max = Integer.MIN_VALUE;

        for (final short value : array) {
            final int v = TypeUtil.unsign(value);
            if (v > max)
                max = v;
        }

        return max;
    }

    /**
     * Find the maximum value of an array
     *
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the max value of the array
     */
    public static long max(final int[] array, final boolean signed) {
        if (signed) {
            int max = Integer.MIN_VALUE;

            for (final int v : array)
                if (v > max)
                    max = v;

            return max;
        }

        long max = Long.MIN_VALUE;

        for (final int j : array) {
            final long v = TypeUtil.unsign(j);
            if (v > max)
                max = v;
        }

        return max;
    }

    /**
     * Find the maximum value of an array
     *
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the max value of the array
     */
    public static long max(final long[] array, final boolean signed) {
        if (signed) {
            long max = Integer.MIN_VALUE;

            for (final long v : array)
                if (v > max)
                    max = v;

            return max;
        }

        double max = Long.MIN_VALUE;

        for (final long l : array) {
            final double v = TypeUtil.unsign(l);
            // need to compare in double
            if (v > max)
                max = v;
        }

        // convert back to long (need to be interpreted as unsigned)
        return TypeUtil.toLong(max);
    }

    /**
     * Find the maximum value of an array
     *
     * @param array
     *        an array
     * @return the max value of the array
     */
    public static float max(final float[] array) {
        float max = -Float.MAX_VALUE;

        for (final float v : array)
            if (v > max)
                max = v;

        return max;
    }

    /**
     * Find the maximum value of an array
     *
     * @param array
     *        an array
     * @return the max value of the array
     */
    public static double max(final double[] array) {
        double max = -Double.MAX_VALUE;

        for (final double v : array)
            if (v > max)
                max = v;

        return max;
    }

    /**
     * Element-wise minimum of two arrays
     *
     * @param a1
     *        =input1
     * @param a2
     *        =input2
     * @param output
     *        - the array of min values
     */
    public static void min(final double[] a1, final double[] a2, final double[] output) {
        for (int i = 0; i < a1.length; i++)
            output[i] = Math.min(a1[i], a2[i]);
    }

    /**
     * Element-wise minimum of two arrays
     *
     * @param a1
     *        =input1
     * @param a2
     *        =input2
     * @return the array of min values
     */
    public static double[] min(final double[] a1, final double[] a2) {
        final double[] result = new double[a1.length];
        min(a1, a2, result);
        return result;
    }

    /**
     * Element-wise maximum of two arrays
     *
     * @param a1
     *        =input1
     * @param a2
     *        =input2
     * @param output
     *        - the array of max values
     */
    public static void max(final double[] a1, final double[] a2, final double[] output) {
        for (int i = 0; i < a1.length; i++)
            output[i] = Math.max(a1[i], a2[i]);
    }

    /**
     * Element-wise maximum of two arrays
     *
     * @param a1
     *        =input1
     * @param a2
     *        =input2
     * @return the array of max values
     */
    public static double[] max(final double[] a1, final double[] a2) {
        final double[] result = new double[a1.length];
        max(a1, a2, result);
        return result;

    }

    /**
     * Reorders the given array to compute its median value
     *
     * @param preserveData
     *        set to true if the given array should not be changed (a copy will be made)
     */
    public static double median(final double[] input, final boolean preserveData) {
        return select(input.length / 2, preserveData ? input.clone() : input);
    }

    /**
     * Computes the Maximum Absolute Deviation aka MAD of the given array
     *
     * @param normalPopulation
     *        normalizes the population by 1.4826
     */
    public static double mad(final double[] input, final boolean normalPopulation) {
        final double[] temp = new double[input.length];
        final double median = median(input, true);

        if (normalPopulation)
            for (int i = 0; i < input.length; i++)
                temp[i] = 1.4826f * (input[i] - median);
        else
            for (int i = 0; i < input.length; i++)
                temp[i] = (input[i] - median);

        abs(temp, true);

        return median(temp, false);
    }

    /**
     * (routine ported from 'Numerical Recipes in C 2nd ed.')<br>
     * Computes the k-th smallest value in the input array and rearranges the array such that the
     * wanted value is located at data[k-1], Lower values
     * are stored in arbitrary order in data[0 .. k-2] Higher values will be stored in arbitrary
     * order in data[k .. end]
     *
     * @return the k-th smallest value in the array
     */
    public static double select(final int k, final double[] data) {
        int i, ir, j, l, mid;
        double a, temp;
        l = 1;
        ir = data.length;
        while (true) {
            if (ir <= l + 1) {
                if (ir == l + 1 && data[ir - 1] < data[l - 1]) {
                    temp = data[l - 1];
                    data[l - 1] = data[ir - 1];
                    data[ir - 1] = temp;
                }
                return data[k - 1];
            }

            mid = (l + ir) >> 1;
            temp = data[mid - 1];
            data[mid - 1] = data[l];
            data[l] = temp;

            if (data[l] > data[ir - 1]) {
                temp = data[l + 1 - 1];
                data[l] = data[ir - 1];
                data[ir - 1] = temp;
            }

            if (data[l - 1] > data[ir - 1]) {
                temp = data[l - 1];
                data[l - 1] = data[ir - 1];
                data[ir - 1] = temp;
            }

            if (data[l] > data[l - 1]) {
                temp = data[l];
                data[l] = data[l - 1];
                data[l - 1] = temp;
            }

            i = l + 1;
            j = ir;
            a = data[l - 1];

            while (true) {

                do
                    i++;
                while (data[i - 1] < a);
                do
                    j--;
                while (data[j - 1] > a);
                if (j < i)
                    break;
                temp = data[i - 1];
                data[i - 1] = data[j - 1];
                data[j - 1] = temp;
            }

            data[l - 1] = data[j - 1];
            data[j - 1] = a;

            if (j >= k)
                ir = j - 1;
            if (j <= k)
                l = i;
        }
    }

    /**
     * Computes the sum of all values from the specified input array.
     *
     * @param array
     *        an array
     * @param signed
     *        signed / unsigned flag
     * @return the sum of all values from the array
     */
    public static double sum(final Object array, final boolean signed) {
        return switch (ArrayUtil.getDataType(array)) {
            case BYTE -> sum((byte[]) array, signed);
            case SHORT -> sum((short[]) array, signed);
            case INT -> sum((int[]) array, signed);
            case LONG -> sum((long[]) array, signed);
            case FLOAT -> sum((float[]) array);
            case DOUBLE -> sum((double[]) array);
            default -> 0d;
        };
    }

    /**
     * Computes the sum of all values in the input array
     *
     * @param input
     *        the array to sum up
     * @param signed
     *        signed / unsigned flag
     */
    public static double sum(final byte[] input, final boolean signed) {
        double sum = 0;

        if (signed) {
            for (final byte b : input)
                sum += b;

        }
        else {
            for (final byte b : input)
                sum += TypeUtil.unsign(b);
        }

        return sum;
    }

    /**
     * Computes the sum of all values in the input array
     *
     * @param input
     *        the array to sum up
     * @param signed
     *        signed / unsigned flag
     */
    public static double sum(final short[] input, final boolean signed) {
        double sum = 0;

        if (signed) {
            for (final short s : input)
                sum += s;

        }
        else {
            for (final short s : input)
                sum += TypeUtil.unsign(s);
        }

        return sum;
    }

    /**
     * Computes the sum of all values in the input array
     *
     * @param input
     *        the array to sum up
     * @param signed
     *        signed / unsigned flag
     */
    public static double sum(final int[] input, final boolean signed) {
        double sum = 0;

        if (signed) {
            for (final int i : input)
                sum += i;

        }
        else {
            for (final int i : input)
                sum += TypeUtil.unsign(i);
        }

        return sum;
    }

    /**
     * Computes the sum of all values in the input array
     *
     * @param input
     *        the array to sum up
     * @param signed
     *        signed / unsigned flag
     */
    public static double sum(final long[] input, final boolean signed) {
        double sum = 0;

        if (signed) {
            for (final long l : input)
                sum += l;

        }
        else {
            for (final long l : input)
                sum += TypeUtil.unsign(l);
        }

        return sum;
    }

    /**
     * Computes the sum of all values in the input array
     *
     * @param input
     *        the array to sum up
     */
    public static double sum(final float[] input) {
        double sum = 0;

        for (final float f : input)
            sum += f;

        return sum;
    }

    /**
     * Computes the sum of all values in the input array
     *
     * @param input
     *        the array to sum up
     */
    public static double sum(final double[] input) {
        double sum = 0;
        for (final double d : input)
            sum += d;
        return sum;
    }

    /**
     * Computes the mean value of the given array
     */
    public static double mean(final double[] input) {
        return sum(input) / input.length;
    }

    /**
     * Computes the unbiased variance of the given array
     *
     * @param unbiased
     *        set to true if the result should be normalized by the population size minus 1
     */
    public static double var(final double[] input, final boolean unbiased) {
        double var = 0;
        final double mean = mean(input);
        for (final double f : input)
            var += (f - mean) * (f - mean);
        return var / (unbiased ? input.length - 1 : input.length);
    }

    /**
     * Computes the standard deviation of the given array (the variance square root)
     *
     * @param unbiased
     *        set to true if the variance should be unbiased
     * @return the square root of the variance
     */
    public static double std(final double[] input, final boolean unbiased) {
        return Math.sqrt(var(input, unbiased));
    }

    /**
     * Rescales the given array to [newMin,newMax]. Nothing is done if the input is constant or if
     * the new bounds equal the old ones.
     *
     * @param input
     *        the input array
     * @param newMin
     *        the new min bound
     * @param newMax
     *        the new max bound
     * @param overwrite
     *        true overwrites the input data, false returns the result in a new structure
     */
    public static double[] rescale(final double[] input, final double newMin, final double newMax, final boolean overwrite) {
        final double min = min(input);
        final double max = max(input);

        if (min == max || (min == newMin && max == newMax))
            return input;

        final double[] result = overwrite ? input : new double[input.length];

        final double ratio = (newMax - newMin) / (max - min);
        final double base = newMin - (min * ratio);

        for (int i = 0; i < input.length; i++)
            result[i] = base + input[i] * ratio;

        return result;
    }

    /**
     * Standardize the input data by subtracting the mean value and dividing by the standard
     * deviation
     *
     * @param input
     *        the data to standardize
     * @param overwrite
     *        true if the output should overwrite the input, false if a new array should be returned
     * @return the standardized data
     */
    public static double[] standardize(final double[] input, final boolean overwrite) {
        final double[] output = overwrite ? input : new double[input.length];

        subtract(input, mean(input), output);
        divide(output, std(output, true), output);

        return output;
    }

    /**
     * Computes the classical correlation coefficient between 2 populations.<br>
     * This coefficient is given by:<br>
     *
     * <pre>
     *                       sum(a[i] * b[i])
     * r(a,b) = -------------------------------------------
     *          sqrt( sum(a[i] * a[i]) * sum(b[i] * b[i]) )
     * </pre>
     *
     * @param a
     *        a population
     * @param b
     *        a population
     * @return the correlation coefficient between a and b.
     * @throws IllegalArgumentException
     *         if the two input population have different sizes
     */
    public static double correlation(final double[] a, final double[] b) throws IllegalArgumentException {
        if (a.length != b.length)
            throw new IllegalArgumentException("Populations must have same size");

        double sum = 0, sqsum_a = 0, sqsum_b = 0;

        double ai, bi;
        for (int i = 0; i < a.length; i++) {
            ai = a[i];
            bi = b[i];
            sum += ai * bi;
            sqsum_a += ai * ai;
            sqsum_b += bi * bi;
        }

        return sum / Math.sqrt(sqsum_a * sqsum_b);
    }

    /**
     * Computes the Pearson correlation coefficient between two populations of same size N. <br>
     * This coefficient is computed by: <br>
     *
     * <pre>
     *          sum(a[i] * b[i]) - N * mean(a) * mean(b)
     * r(a,b) = ----------------------------------------
     *                  (N-1) * std(a) * std(b)
     * </pre>
     *
     * @param a
     *        a population
     * @param b
     *        a population
     * @return the Pearson correlation measure between a and b.
     * @throws IllegalArgumentException
     *         if the two input population have different sizes
     */
    public static double correlationPearson(final double[] a, final double[] b) throws IllegalArgumentException {
        if (a.length != b.length)
            throw new IllegalArgumentException("Populations must have same size");

        double sum = 0;
        for (int i = 0; i < a.length; i++)
            sum += a[i] * b[i];

        return (sum - a.length * mean(a) * mean(b)) / ((a.length - 1) * std(a, true) * std(b, true));
    }
}
