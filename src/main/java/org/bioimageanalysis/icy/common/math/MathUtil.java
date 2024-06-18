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

/**
 * Math utilities class.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class MathUtil {
    public static final String INFINITE_STRING = "∞";

    public static final double POW2_8_DOUBLE = Math.pow(2, 8);
    public static final float POW2_8_FLOAT = (float) POW2_8_DOUBLE;
    public static final double POW2_16_DOUBLE = Math.pow(2, 16);
    public static final float POW2_16_FLOAT = (float) POW2_16_DOUBLE;
    public static final double POW2_32_DOUBLE = Math.pow(2, 32);
    public static final float POW2_32_FLOAT = (float) POW2_32_DOUBLE;
    public static final double POW2_64_DOUBLE = Math.pow(2, 64);
    public static final float POW2_64_FLOAT = (float) POW2_64_DOUBLE;

    public static double frac(final double value) {
        return value - Math.floor(value);
    }

    /**
     * Normalize an array
     *
     * @param array
     *        elements to normalize
     */
    public static void normalize(final float[] array) {
        final float max = ArrayMath.max(array);

        if (max != 0)
            divide(array, max);
        else {
            final float min = ArrayMath.min(array);
            if (min != 0)
                divide(array, min);
        }
    }

    /**
     * Normalize an array
     *
     * @param array
     *        elements to normalize
     */
    public static void normalize(final double[] array) {
        final double max = ArrayMath.max(array);

        if (max != 0)
            divide(array, max);
        else {
            final double min = ArrayMath.min(array);
            if (min != 0)
                divide(array, min);
        }
    }

    /**
     * Replace all values in the array by their logarithm<br>
     * Be careful, all values should be &gt; 0 values
     *
     * @param array
     *        elements to logarithm
     */
    public static void log(final double[] array) {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = Math.log(array[i]);
    }

    /**
     * Replace all values in the array by their logarithm<br>
     * Be careful, all values should be &gt; 0 values
     *
     * @param array
     *        elements to logarithm
     */
    public static void log(final float[] array) {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = (float) Math.log(array[i]);
    }

    /**
     * Add the the specified value to all elements in an array
     *
     * @param array
     *        elements to modify
     */
    public static void add(final double[] array, final double value) {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = array[i] + value;
    }

    /**
     * Add the the specified value to all elements in an array
     *
     * @param array
     *        elements to modify
     */
    public static void add(final float[] array, final float value) {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = array[i] + value;
    }

    /**
     * Multiply and add all elements in an array by the specified values
     *
     * @param array
     *        elements to modify
     */
    public static void madd(final double[] array, final double mulValue, final double addValue) {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = (array[i] * mulValue) + addValue;
    }

    /**
     * Multiply and add all elements in an array by the specified values
     *
     * @param array
     *        elements to modify
     */
    public static void madd(final float[] array, final float mulValue, final float addValue) {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = (array[i] * mulValue) + addValue;
    }

    /**
     * Multiply all elements in an array by the specified value
     *
     * @param array
     *        elements to modify
     * @param value
     *        value to multiply by
     */
    public static void mul(final double[] array, final double value) {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = array[i] * value;
    }

    /**
     * Multiply all elements in an array by the specified value
     *
     * @param array
     *        elements to modify
     * @param value
     *        value to multiply by
     */
    public static void mul(final float[] array, final float value) {
        final int len = array.length;

        for (int i = 0; i < len; i++)
            array[i] = array[i] * value;
    }

    /**
     * Divides all elements in an array by the specified value
     *
     * @param array
     *        elements to modify
     * @param value
     *        value used as divisor
     */
    public static void divide(final double[] array, final double value) {
        if (value != 0d) {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                array[i] = array[i] / value;
        }
    }

    /**
     * Divides all elements in an array by the specified value
     *
     * @param array
     *        elements to modify
     * @param value
     *        value used as divisor
     */
    public static void divide(final float[] array, final float value) {
        if (value != 0d) {
            final int len = array.length;

            for (int i = 0; i < len; i++)
                array[i] = array[i] / value;
        }
    }

    /**
     * Round specified value to specified number of significant digit.<br>
     * If keepInteger is true then integer part of number is entirely conserved.<br>
     * If <i>numDigit</i> is &lt;= 0 then the value stay unchanged.
     */
    public static double roundSignificant(final double d, final int numDigit, final boolean keepInteger) {
        if ((numDigit <= 0) || (d == 0d))
            return d;

        final double digit = Math.ceil(Math.log10(Math.abs(d)));
        if ((digit >= numDigit) && keepInteger)
            return Math.round(d);

        return round(d, numDigit - (int) digit);
    }

    /**
     * Round specified value to specified number of significant digit.
     */
    public static double roundSignificant(final double d, final int numDigit) {
        return roundSignificant(d, numDigit, false);
    }

    /**
     * Round specified value to specified number of decimal.
     */
    public static double round(final double d, final int numDecimal) {
        final double pow = Math.pow(10, numDecimal);
        return Math.round(d * pow) / pow;
    }

    /**
     * Return the previous multiple of "mul" for the specified value
     * <ul>
     * <li>prevMultiple(200, 64) = 192</li>
     * </ul>
     */
    public static double prevMultiple(final double value, final double mul) {
        if (mul == 0)
            return 0d;

        return Math.floor(value / mul) * mul;
    }

    /**
     * Return the next multiple of "mul" for the specified value
     * <ul>
     * <li>nextMultiple(200, 64) = 256</li>
     * </ul>
     */
    public static double nextMultiple(final double value, final double mul) {
        if (mul == 0)
            return 0d;

        return Math.ceil(value / mul) * mul;
    }

    /**
     * Return the next power of 2 for the specified value
     * <ul>
     * <li>nextPow2(17) = 32</li>
     * <li>nextPow2(16) = 32</li>
     * <li>nextPow2(-12) = -8</li>
     * <li>nextPow2(-8) = -4</li>
     * </ul>
     *
     * @return next power of 2
     */
    public static long nextPow2(final long value) {
        long result;

        if (value < 0) {
            result = -1;
            while (result > value)
                result <<= 1;
            result >>= 1;
        }
        else {
            result = 1;
            while (result <= value)
                result <<= 1;
        }

        return result;
    }

    /**
     * Return the next power of 2 mask for the specified value
     * <ul>
     * <li>nextPow2Mask(17) = 31</li>
     * <li>nextPow2Mask(16) = 31</li>
     * <li>nextPow2Mask(-12) = -8</li>
     * <li>nextPow2Mask(-8) = -4</li>
     * </ul>
     *
     * @return next power of 2 mask
     */
    public static long nextPow2Mask(final long value) {
        final long result = nextPow2(value);
        if (value > 0)
            return result - 1;

        return result;
    }

    /**
     * Return the previous power of 2 for the specified value
     * <ul>
     * <li>prevPow2(17) = 16</li>
     * <li>prevPow2(16) = 8</li>
     * <li>prevPow2(-12) = -16</li>
     * <li>prevPow2(-8) = -16</li>
     * </ul>
     *
     * @return previous power of 2
     */
    public static long prevPow2(final long value) {
        long result;

        if (value < 0) {
            result = -1;
            while (result >= value)
                result <<= 1;
        }
        else {
            result = 1;
            while (result < value)
                result <<= 1;
            result >>= 1;
        }

        return result;
    }

    /**
     * Return the next power of 10 for the specified value
     * <ul>
     * <li>nextPow10(0.0067) = 0.01</li>
     * <li>nextPow10(-28.7) = -10</li>
     * </ul>
     */
    public static double nextPow10(final double value) {
        if (value == 0)
            return 0;
        else if (value < 0)
            return -Math.pow(10, Math.floor(Math.log10(-value)));
        else
            return Math.pow(10, Math.ceil(Math.log10(value)));
    }

    /**
     * Return the previous power of 10 for the specified value
     * <ul>
     * <li>prevPow10(0.0067) = 0.001</li>
     * <li>prevPow10(-28.7) = -100</li>
     * </ul>
     */
    public static double prevPow10(final double value) {
        if (value == 0)
            return 0;
        else if (value < 0)
            return -Math.pow(10, Math.ceil(Math.log10(-value)));
        else
            return Math.pow(10, Math.floor(Math.log10(value)));
    }

    /**
     * Format the specified degree angle to stay in [0..360[ range
     */
    public static double formatDegreeAngle(final double angle) {
        final double res = angle % 360d;

        if (res < 0)
            return 360d + res;

        return res;
    }

    /**
     * Format the specified degree angle to stay in [-180..180] range
     */
    public static double formatDegreeAngle2(final double angle) {
        final double res = angle % 360d;

        if (res < -180d)
            return 360d + res;
        if (res > 180d)
            return res - 360d;

        return res;
    }

    /**
     * Format the specified degree angle to stay in [0..2PI[ range
     */
    public static double formatRadianAngle(final double angle) {
        final double res = angle % (2 * Math.PI);

        if (res < 0)
            return (2 * Math.PI) + res;

        return res;
    }

    /**
     * Format the specified degree angle to stay in [-PI..PI] range
     */
    public static double formatRadianAngle2(final double angle) {
        final double res = angle % (2 * Math.PI);

        if (res < -Math.PI)
            return (2 * Math.PI) + res;
        if (res > Math.PI)
            return res - (2 * Math.PI);

        return res;
    }

    /**
     * Return the value in <code>source</code> which is the closest to <code>value</code> Returns
     * <code>0</code> if source is null or empty.
     */
    public static double closest(final double value, final double[] source) {
        if ((source == null) || (source.length == 0))
            return 0d;

        double result = source[0];
        double minDelta = Math.abs(value - result);

        for (final double d : source) {
            final double delta = Math.abs(value - d);

            if (delta < minDelta) {
                result = d;
                minDelta = delta;
            }
        }

        return result;
    }

    /**
     * Calculate the cubic root of the specified value.
     */
    public static double cubicRoot(final double value) {
        return Math.pow(value, 1d / 3d);
    }
}
