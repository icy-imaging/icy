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

package fr.icy.common;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

/**
 * Utility class for enforcing conditions on primitive numeric values such as {@code byte}, {@code short},
 * {@code int}, {@code long}, and {@code float}. This class provides a set of static methods for
 * validating equality, inequality, range, and comparison of values. If the specified condition is violated,
 * the respective exception is thrown.
 *
 * <p>This class is designed for pure utility purposes and cannot be instantiated.
 *
 * @author Thomas Musset
 */
public final class EnforcerUtil {
    @Contract(pure = true)
    private EnforcerUtil() {
        //
    }

    // ==== Byte ==== //

    /**
     * Ensures that the two provided byte values are equal.
     * If the values are not equal, an {@link IllegalArgumentException} is thrown with the specified message
     * or a default message if no message is provided.
     *
     * @param v1      the first byte value to compare.
     * @param v2      the second byte value to compare against the first.
     * @param message the custom exception message to use if the values are not equal; may be null or blank.
     * @throws IllegalArgumentException if the two values are not equal.
     */
    public static void enforceEqual(final byte v1, final byte v2, final @Nullable String message) throws IllegalArgumentException {
        if (v1 != v2)
            if (message != null && !message.isBlank())
                throw new IllegalArgumentException(message);
            else
                throw new IllegalArgumentException("Value " + v1 + " is not equal to " + v2 + ".");
    }


    /**
     * Ensures that the two provided byte values are equal.
     * If the values are not equal, an {@link IllegalArgumentException} is thrown with the default message.
     *
     * @param v1 the first byte value to compare.
     * @param v2 the second byte value to compare against the first.
     * @throws IllegalArgumentException if the two values are not equal.
     */
    public static void enforceEqual(final byte v1, final byte v2) throws IllegalArgumentException {
        enforceEqual(v1, v2, null);
    }

    /**
     * Ensures that the two provided byte values are not equal.
     * If the values are equal, an {@link IllegalArgumentException} is thrown with the specified message
     * or a default message if no message is provided.
     *
     * @param v1      the first byte value to compare.
     * @param v2      the second byte value to compare against the first.
     * @param message the custom exception message to use if the values are equal; may be null or blank.
     * @throws IllegalArgumentException if the two values are equal.
     */
    public static void enforceNotEqual(final byte v1, final byte v2, final @Nullable String message) throws IllegalArgumentException {
        if (v1 == v2)
            if (message != null && !message.isBlank())
                throw new IllegalArgumentException(message);
            else
                throw new IllegalArgumentException("Value " + v1 + " must not be equal to " + v2 + ".");
    }

    /**
     * Ensures that the two provided byte values are not equal.
     * If the values are equal, an {@link IllegalArgumentException} is thrown with the default message.
     *
     * @param v1 the first byte value to compare.
     * @param v2 the second byte value to compare against the first.
     * @throws IllegalArgumentException if the two values are equal.
     */
    public static void enforceNotEqual(final byte v1, final byte v2) throws IllegalArgumentException {
        enforceNotEqual(v1, v2, null);
    }

    /**
     * Ensures that a given byte value falls within the specified range.
     * If the value is outside the range, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   The value to be checked.
     * @param min     The minimum acceptable value (inclusive).
     * @param max     The maximum acceptable value (inclusive).
     * @param message An optional custom error message to be used if the value is out of range.
     *                If null or blank, a default error message is generated.
     * @throws IndexOutOfBoundsException If the value is outside the specified range.
     */
    public static void enforceRange(final byte value, final byte min, final byte max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value < min || value > max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " is out of range [" + min + ", " + max + "].");
    }

    /**
     * Ensures that a given byte value falls within the specified range.
     * If the value is outside the range, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value The value to be checked.
     * @param min   The minimum acceptable value (inclusive).
     * @param max   The maximum acceptable value (inclusive).
     * @throws IndexOutOfBoundsException If the value is outside the specified range.
     */
    public static void enforceRange(final byte value, final byte min, final byte max) throws IndexOutOfBoundsException {
        enforceRange(value, min, max, null);
    }

    /**
     * Ensures that a specified byte value is less than or equal to a maximum allowed value.
     * If the specified value is greater than the maximum, an {@link IndexOutOfBoundsException}
     * is thrown, optionally with a custom error message.
     *
     * @param value   the byte value to be checked.
     * @param max     the maximum allowed byte value.
     * @param message an optional custom error message that will be included in the exception
     *                if the check fails; may be {@code null} or blank.
     * @throws IndexOutOfBoundsException if {@code value} is greater than {@code max}.
     */
    public static void enforceLowerOrEqual(final byte value, final byte max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value > max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be lower or equal to " + max + ".");
    }

    /**
     * Ensures that a specified byte value is less than or equal to a maximum allowed value.
     * If the specified value is greater than the maximum, an {@link IndexOutOfBoundsException}
     * is thrown.
     *
     * @param value the byte value to be checked.
     * @param max   the maximum allowed byte value.
     * @throws IndexOutOfBoundsException if {@code value} is greater than {@code max}.
     */
    public static void enforceLowerOrEqual(final byte value, final byte max) throws IndexOutOfBoundsException {
        enforceLowerOrEqual(value, max, null);
    }

    /**
     * Ensures that the specified byte value is less than the provided maximum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   the byte value to be checked.
     * @param max     the maximum byte value that the input value must be less than.
     * @param message an optional custom message for the exception; if null or blank, a default message is used.
     * @throws IndexOutOfBoundsException if the provided value is not less than the specified maximum.
     */
    public static void enforceLowerThan(final byte value, final byte max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value >= max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be lower than " + max + ".");
    }

    /**
     * Ensures that the specified byte value is less than the provided maximum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value the byte value to be checked.
     * @param max   the maximum byte value that the input value must be less than.
     * @throws IndexOutOfBoundsException if the provided value is not less than the specified maximum.
     */
    public static void enforceLowerThan(final byte value, final byte max) throws IndexOutOfBoundsException {
        enforceLowerThan(value, max, null);
    }

    /**
     * Ensures that a specified byte value is greater than or equal to a minimum allowed value.
     * If the specified value is less than the minimum, an {@link IndexOutOfBoundsException}
     * is thrown, optionally with a custom error message.
     *
     * @param value   the byte value to be checked.
     * @param min     the minimum allowed byte value.
     * @param message an optional custom error message that will be included in the exception
     *                if the check fails; may be {@code null} or blank.
     * @throws IndexOutOfBoundsException if {@code value} is less than {@code min}.
     */
    public static void enforceGreaterOrEqual(final byte value, final byte min, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value < min)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be greater or equal to " + min + ".");
    }

    /**
     * Ensures that a specified byte value is greater than or equal to a minimum allowed value.
     * If the specified value is less than the minimum, an {@link IndexOutOfBoundsException}
     * is thrown.
     *
     * @param value the byte value to be checked.
     * @param min   the minimum allowed byte value.
     * @throws IndexOutOfBoundsException if {@code value} is less than {@code min}.
     */
    public static void enforceGreaterOrEqual(final byte value, final byte min) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(value, min, null);
    }

    /**
     * Ensures that the specified byte value is greater than the provided minimum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   the byte value to be checked.
     * @param min     the minimum byte value that the input value must be greater than.
     * @param message an optional custom message for the exception; if null or blank, a default message is used.
     * @throws IndexOutOfBoundsException if the provided value is not greater than the specified maximum.
     */
    public static void enforceGreaterThan(final byte value, final byte min, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value <= min)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be greater than " + min + ".");
    }

    /**
     * Ensures that the specified byte value is greater than the provided minimum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value the byte value to be checked.
     * @param min   the minimum byte value that the input value must be greater than.
     * @throws IndexOutOfBoundsException if the provided value is not greater than the specified maximum.
     */
    public static void enforceGreaterThan(final byte value, final byte min) throws IndexOutOfBoundsException {
        enforceGreaterThan(value, min, null);
    }

    // ==== Short ==== //

    /**
     * Ensures that the two provided short values are equal.
     * If the values are not equal, an {@link IllegalArgumentException} is thrown with the specified message
     * or a default message if no message is provided.
     *
     * @param v1      the first short value to compare.
     * @param v2      the second short value to compare against the first.
     * @param message the custom exception message to use if the values are not equal; may be null or blank.
     * @throws IllegalArgumentException if the two values are not equal.
     */
    public static void enforceEqual(final short v1, final short v2, final @Nullable String message) throws IllegalArgumentException {
        if (v1 != v2)
            if (message != null && !message.isBlank())
                throw new IllegalArgumentException(message);
            else
                throw new IllegalArgumentException("Value " + v1 + " is not equal to " + v2 + ".");
    }


    /**
     * Ensures that the two provided short values are equal.
     * If the values are not equal, an {@link IllegalArgumentException} is thrown with the default message.
     *
     * @param v1 the first short value to compare.
     * @param v2 the second short value to compare against the first.
     * @throws IllegalArgumentException if the two values are not equal.
     */
    public static void enforceEqual(final short v1, final short v2) throws IllegalArgumentException {
        enforceEqual(v1, v2, null);
    }

    /**
     * Ensures that the two provided short values are not equal.
     * If the values are equal, an {@link IllegalArgumentException} is thrown with the specified message
     * or a default message if no message is provided.
     *
     * @param v1      the first short value to compare.
     * @param v2      the second short value to compare against the first.
     * @param message the custom exception message to use if the values are equal; may be null or blank.
     * @throws IllegalArgumentException if the two values are equal.
     */
    public static void enforceNotEqual(final short v1, final short v2, final @Nullable String message) throws IllegalArgumentException {
        if (v1 == v2)
            if (message != null && !message.isBlank())
                throw new IllegalArgumentException(message);
            else
                throw new IllegalArgumentException("Value " + v1 + " must not be equal to " + v2 + ".");
    }

    /**
     * Ensures that the two provided short values are not equal.
     * If the values are equal, an {@link IllegalArgumentException} is thrown with the default message.
     *
     * @param v1 the first short value to compare.
     * @param v2 the second short value to compare against the first.
     * @throws IllegalArgumentException if the two values are equal.
     */
    public static void enforceNotEqual(final short v1, final short v2) throws IllegalArgumentException {
        enforceNotEqual(v1, v2, null);
    }

    /**
     * Ensures that a given short value falls within the specified range.
     * If the value is outside the range, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   The value to be checked.
     * @param min     The minimum acceptable value (inclusive).
     * @param max     The maximum acceptable value (inclusive).
     * @param message An optional custom error message to be used if the value is out of range.
     *                If null or blank, a default error message is generated.
     * @throws IndexOutOfBoundsException If the value is outside the specified range.
     */
    public static void enforceRange(final short value, final short min, final short max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value < min || value > max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " is out of range [" + min + ", " + max + "].");
    }

    /**
     * Ensures that a given short value falls within the specified range.
     * If the value is outside the range, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value The value to be checked.
     * @param min   The minimum acceptable value (inclusive).
     * @param max   The maximum acceptable value (inclusive).
     * @throws IndexOutOfBoundsException If the value is outside the specified range.
     */
    public static void enforceRange(final short value, final short min, final short max) throws IndexOutOfBoundsException {
        enforceRange(value, min, max, null);
    }

    /**
     * Ensures that a specified short value is less than or equal to a maximum allowed value.
     * If the specified value is greater than the maximum, an {@link IndexOutOfBoundsException}
     * is thrown, optionally with a custom error message.
     *
     * @param value   the short value to be checked.
     * @param max     the maximum allowed short value.
     * @param message an optional custom error message that will be included in the exception
     *                if the check fails; may be {@code null} or blank.
     * @throws IndexOutOfBoundsException if {@code value} is greater than {@code max}.
     */
    public static void enforceLowerOrEqual(final short value, final short max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value > max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be lower or equal to " + max + ".");
    }

    /**
     * Ensures that a specified short value is less than or equal to a maximum allowed value.
     * If the specified value is greater than the maximum, an {@link IndexOutOfBoundsException}
     * is thrown.
     *
     * @param value the short value to be checked.
     * @param max   the maximum allowed short value.
     * @throws IndexOutOfBoundsException if {@code value} is greater than {@code max}.
     */
    public static void enforceLowerOrEqual(final short value, final short max) throws IndexOutOfBoundsException {
        enforceLowerOrEqual(value, max, null);
    }

    /**
     * Ensures that the specified short value is less than the provided maximum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   the short value to be checked.
     * @param max     the maximum short value that the input value must be less than.
     * @param message an optional custom message for the exception; if null or blank, a default message is used.
     * @throws IndexOutOfBoundsException if the provided value is not less than the specified maximum.
     */
    public static void enforceLowerThan(final short value, final short max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value >= max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be lower than " + max + ".");
    }

    /**
     * Ensures that the specified short value is less than the provided maximum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value the short value to be checked.
     * @param max   the maximum short value that the input value must be less than.
     * @throws IndexOutOfBoundsException if the provided value is not less than the specified maximum.
     */
    public static void enforceLowerThan(final short value, final short max) throws IndexOutOfBoundsException {
        enforceLowerThan(value, max, null);
    }

    /**
     * Ensures that a specified short value is greater than or equal to a minimum allowed value.
     * If the specified value is less than the minimum, an {@link IndexOutOfBoundsException}
     * is thrown, optionally with a custom error message.
     *
     * @param value   the short value to be checked.
     * @param min     the minimum allowed short value.
     * @param message an optional custom error message that will be included in the exception
     *                if the check fails; may be {@code null} or blank.
     * @throws IndexOutOfBoundsException if {@code value} is less than {@code min}.
     */
    public static void enforceGreaterOrEqual(final short value, final short min, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value < min)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be greater or equal to " + min + ".");
    }

    /**
     * Ensures that a specified short value is greater than or equal to a minimum allowed value.
     * If the specified value is less than the minimum, an {@link IndexOutOfBoundsException}
     * is thrown.
     *
     * @param value the short value to be checked.
     * @param min   the minimum allowed short value.
     * @throws IndexOutOfBoundsException if {@code value} is less than {@code min}.
     */
    public static void enforceGreaterOrEqual(final short value, final short min) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(value, min, null);
    }

    /**
     * Ensures that the specified short value is greater than the provided minimum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   the short value to be checked.
     * @param min     the minimum short value that the input value must be greater than.
     * @param message an optional custom message for the exception; if null or blank, a default message is used.
     * @throws IndexOutOfBoundsException if the provided value is not greater than the specified maximum.
     */
    public static void enforceGreaterThan(final short value, final short min, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value <= min)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be greater than " + min + ".");
    }

    /**
     * Ensures that the specified short value is greater than the provided minimum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value the short value to be checked.
     * @param min   the minimum short value that the input value must be greater than.
     * @throws IndexOutOfBoundsException if the provided value is not greater than the specified maximum.
     */
    public static void enforceGreaterThan(final short value, final short min) throws IndexOutOfBoundsException {
        enforceGreaterThan(value, min, null);
    }

    // ==== Integer ==== //

    /**
     * Ensures that the two provided int values are equal.
     * If the values are not equal, an {@link IllegalArgumentException} is thrown with the specified message
     * or a default message if no message is provided.
     *
     * @param v1      the first int value to compare.
     * @param v2      the second int value to compare against the first.
     * @param message the custom exception message to use if the values are not equal; may be null or blank.
     * @throws IllegalArgumentException if the two values are not equal.
     */
    public static void enforceEqual(final int v1, final int v2, final @Nullable String message) throws IllegalArgumentException {
        if (v1 != v2)
            if (message != null && !message.isBlank())
                throw new IllegalArgumentException(message);
            else
                throw new IllegalArgumentException("Value " + v1 + " is not equal to " + v2 + ".");
    }


    /**
     * Ensures that the two provided int values are equal.
     * If the values are not equal, an {@link IllegalArgumentException} is thrown with the default message.
     *
     * @param v1 the first int value to compare.
     * @param v2 the second int value to compare against the first.
     * @throws IllegalArgumentException if the two values are not equal.
     */
    public static void enforceEqual(final int v1, final int v2) throws IllegalArgumentException {
        enforceEqual(v1, v2, null);
    }

    /**
     * Ensures that the two provided int values are not equal.
     * If the values are equal, an {@link IllegalArgumentException} is thrown with the specified message
     * or a default message if no message is provided.
     *
     * @param v1      the first int value to compare.
     * @param v2      the second int value to compare against the first.
     * @param message the custom exception message to use if the values are equal; may be null or blank.
     * @throws IllegalArgumentException if the two values are equal.
     */
    public static void enforceNotEqual(final int v1, final int v2, final @Nullable String message) throws IllegalArgumentException {
        if (v1 == v2)
            if (message != null && !message.isBlank())
                throw new IllegalArgumentException(message);
            else
                throw new IllegalArgumentException("Value " + v1 + " must not be equal to " + v2 + ".");
    }

    /**
     * Ensures that the two provided int values are not equal.
     * If the values are equal, an {@link IllegalArgumentException} is thrown with the default message.
     *
     * @param v1 the first int value to compare.
     * @param v2 the second int value to compare against the first.
     * @throws IllegalArgumentException if the two values are equal.
     */
    public static void enforceNotEqual(final int v1, final int v2) throws IllegalArgumentException {
        enforceNotEqual(v1, v2, null);
    }

    /**
     * Ensures that a given int value falls within the specified range.
     * If the value is outside the range, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   The value to be checked.
     * @param min     The minimum acceptable value (inclusive).
     * @param max     The maximum acceptable value (inclusive).
     * @param message An optional custom error message to be used if the value is out of range.
     *                If null or blank, a default error message is generated.
     * @throws IndexOutOfBoundsException If the value is outside the specified range.
     */
    public static void enforceRange(final int value, final int min, final int max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value < min || value > max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " is out of range [" + min + ", " + max + "].");
    }

    /**
     * Ensures that a given int value falls within the specified range.
     * If the value is outside the range, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value The value to be checked.
     * @param min   The minimum acceptable value (inclusive).
     * @param max   The maximum acceptable value (inclusive).
     * @throws IndexOutOfBoundsException If the value is outside the specified range.
     */
    public static void enforceRange(final int value, final int min, final int max) throws IndexOutOfBoundsException {
        enforceRange(value, min, max, null);
    }

    /**
     * Ensures that a specified int value is less than or equal to a maximum allowed value.
     * If the specified value is greater than the maximum, an {@link IndexOutOfBoundsException}
     * is thrown, optionally with a custom error message.
     *
     * @param value   the int value to be checked.
     * @param max     the maximum allowed int value.
     * @param message an optional custom error message that will be included in the exception
     *                if the check fails; may be {@code null} or blank.
     * @throws IndexOutOfBoundsException if {@code value} is greater than {@code max}.
     */
    public static void enforceLowerOrEqual(final int value, final int max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value > max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be lower or equal to " + max + ".");
    }

    /**
     * Ensures that a specified int value is less than or equal to a maximum allowed value.
     * If the specified value is greater than the maximum, an {@link IndexOutOfBoundsException}
     * is thrown.
     *
     * @param value the int value to be checked.
     * @param max   the maximum allowed int value.
     * @throws IndexOutOfBoundsException if {@code value} is greater than {@code max}.
     */
    public static void enforceLowerOrEqual(final int value, final int max) throws IndexOutOfBoundsException {
        enforceLowerOrEqual(value, max, null);
    }

    /**
     * Ensures that the specified int value is less than the provided maximum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   the int value to be checked.
     * @param max     the maximum int value that the input value must be less than.
     * @param message an optional custom message for the exception; if null or blank, a default message is used.
     * @throws IndexOutOfBoundsException if the provided value is not less than the specified maximum.
     */
    public static void enforceLowerThan(final int value, final int max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value >= max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be lower than " + max + ".");
    }

    /**
     * Ensures that the specified int value is less than the provided maximum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value the int value to be checked.
     * @param max   the maximum int value that the input value must be less than.
     * @throws IndexOutOfBoundsException if the provided value is not less than the specified maximum.
     */
    public static void enforceLowerThan(final int value, final int max) throws IndexOutOfBoundsException {
        enforceLowerThan(value, max, null);
    }

    /**
     * Ensures that a specified int value is greater than or equal to a minimum allowed value.
     * If the specified value is less than the minimum, an {@link IndexOutOfBoundsException}
     * is thrown, optionally with a custom error message.
     *
     * @param value   the int value to be checked.
     * @param min     the minimum allowed int value.
     * @param message an optional custom error message that will be included in the exception
     *                if the check fails; may be {@code null} or blank.
     * @throws IndexOutOfBoundsException if {@code value} is less than {@code min}.
     */
    public static void enforceGreaterOrEqual(final int value, final int min, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value < min)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be greater or equal to " + min + ".");
    }

    /**
     * Ensures that a specified int value is greater than or equal to a minimum allowed value.
     * If the specified value is less than the minimum, an {@link IndexOutOfBoundsException}
     * is thrown.
     *
     * @param value the int value to be checked.
     * @param min   the minimum allowed int value.
     * @throws IndexOutOfBoundsException if {@code value} is less than {@code min}.
     */
    public static void enforceGreaterOrEqual(final int value, final int min) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(value, min, null);
    }

    /**
     * Ensures that the specified int value is greater than the provided minimum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   the int value to be checked.
     * @param min     the minimum int value that the input value must be greater than.
     * @param message an optional custom message for the exception; if null or blank, a default message is used.
     * @throws IndexOutOfBoundsException if the provided value is not greater than the specified maximum.
     */
    public static void enforceGreaterThan(final int value, final int min, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value <= min)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be greater than " + min + ".");
    }

    /**
     * Ensures that the specified int value is greater than the provided minimum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value the int value to be checked.
     * @param min   the minimum int value that the input value must be greater than.
     * @throws IndexOutOfBoundsException if the provided value is not greater than the specified maximum.
     */
    public static void enforceGreaterThan(final int value, final int min) throws IndexOutOfBoundsException {
        enforceGreaterThan(value, min, null);
    }

    // ==== Long ==== //

    /**
     * Ensures that the two provided long values are equal.
     * If the values are not equal, an {@link IllegalArgumentException} is thrown with the specified message
     * or a default message if no message is provided.
     *
     * @param v1      the first long value to compare.
     * @param v2      the second long value to compare against the first.
     * @param message the custom exception message to use if the values are not equal; may be null or blank.
     * @throws IllegalArgumentException if the two values are not equal.
     */
    public static void enforceEqual(final long v1, final long v2, final @Nullable String message) throws IllegalArgumentException {
        if (v1 != v2)
            if (message != null && !message.isBlank())
                throw new IllegalArgumentException(message);
            else
                throw new IllegalArgumentException("Value " + v1 + " is not equal to " + v2 + ".");
    }


    /**
     * Ensures that the two provided long values are equal.
     * If the values are not equal, an {@link IllegalArgumentException} is thrown with the default message.
     *
     * @param v1 the first long value to compare.
     * @param v2 the second long value to compare against the first.
     * @throws IllegalArgumentException if the two values are not equal.
     */
    public static void enforceEqual(final long v1, final long v2) throws IllegalArgumentException {
        enforceEqual(v1, v2, null);
    }

    /**
     * Ensures that the two provided long values are not equal.
     * If the values are equal, an {@link IllegalArgumentException} is thrown with the specified message
     * or a default message if no message is provided.
     *
     * @param v1      the first long value to compare.
     * @param v2      the second long value to compare against the first.
     * @param message the custom exception message to use if the values are equal; may be null or blank.
     * @throws IllegalArgumentException if the two values are equal.
     */
    public static void enforceNotEqual(final long v1, final long v2, final @Nullable String message) throws IllegalArgumentException {
        if (v1 == v2)
            if (message != null && !message.isBlank())
                throw new IllegalArgumentException(message);
            else
                throw new IllegalArgumentException("Value " + v1 + " must not be equal to " + v2 + ".");
    }

    /**
     * Ensures that the two provided long values are not equal.
     * If the values are equal, an {@link IllegalArgumentException} is thrown with the default message.
     *
     * @param v1 the first long value to compare.
     * @param v2 the second long value to compare against the first.
     * @throws IllegalArgumentException if the two values are equal.
     */
    public static void enforceNotEqual(final long v1, final long v2) throws IllegalArgumentException {
        enforceNotEqual(v1, v2, null);
    }

    /**
     * Ensures that a given long value falls within the specified range.
     * If the value is outside the range, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   The value to be checked.
     * @param min     The minimum acceptable value (inclusive).
     * @param max     The maximum acceptable value (inclusive).
     * @param message An optional custom error message to be used if the value is out of range.
     *                If null or blank, a default error message is generated.
     * @throws IndexOutOfBoundsException If the value is outside the specified range.
     */
    public static void enforceRange(final long value, final long min, final long max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value < min || value > max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " is out of range [" + min + ", " + max + "].");
    }

    /**
     * Ensures that a given long value falls within the specified range.
     * If the value is outside the range, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value The value to be checked.
     * @param min   The minimum acceptable value (inclusive).
     * @param max   The maximum acceptable value (inclusive).
     * @throws IndexOutOfBoundsException If the value is outside the specified range.
     */
    public static void enforceRange(final long value, final long min, final long max) throws IndexOutOfBoundsException {
        enforceRange(value, min, max, null);
    }

    /**
     * Ensures that a specified long value is less than or equal to a maximum allowed value.
     * If the specified value is greater than the maximum, an {@link IndexOutOfBoundsException}
     * is thrown, optionally with a custom error message.
     *
     * @param value   the long value to be checked.
     * @param max     the maximum allowed long value.
     * @param message an optional custom error message that will be included in the exception
     *                if the check fails; may be {@code null} or blank.
     * @throws IndexOutOfBoundsException if {@code value} is greater than {@code max}.
     */
    public static void enforceLowerOrEqual(final long value, final long max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value > max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be lower or equal to " + max + ".");
    }

    /**
     * Ensures that a specified long value is less than or equal to a maximum allowed value.
     * If the specified value is greater than the maximum, an {@link IndexOutOfBoundsException}
     * is thrown.
     *
     * @param value the long value to be checked.
     * @param max   the maximum allowed long value.
     * @throws IndexOutOfBoundsException if {@code value} is greater than {@code max}.
     */
    public static void enforceLowerOrEqual(final long value, final long max) throws IndexOutOfBoundsException {
        enforceLowerOrEqual(value, max, null);
    }

    /**
     * Ensures that the specified long value is less than the provided maximum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   the long value to be checked.
     * @param max     the maximum long value that the input value must be less than.
     * @param message an optional custom message for the exception; if null or blank, a default message is used.
     * @throws IndexOutOfBoundsException if the provided value is not less than the specified maximum.
     */
    public static void enforceLowerThan(final long value, final long max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value >= max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be lower than " + max + ".");
    }

    /**
     * Ensures that the specified long value is less than the provided maximum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value the long value to be checked.
     * @param max   the maximum long value that the input value must be less than.
     * @throws IndexOutOfBoundsException if the provided value is not less than the specified maximum.
     */
    public static void enforceLowerThan(final long value, final long max) throws IndexOutOfBoundsException {
        enforceLowerThan(value, max, null);
    }

    /**
     * Ensures that a specified long value is greater than or equal to a minimum allowed value.
     * If the specified value is less than the minimum, an {@link IndexOutOfBoundsException}
     * is thrown, optionally with a custom error message.
     *
     * @param value   the long value to be checked.
     * @param min     the minimum allowed long value.
     * @param message an optional custom error message that will be included in the exception
     *                if the check fails; may be {@code null} or blank.
     * @throws IndexOutOfBoundsException if {@code value} is less than {@code min}.
     */
    public static void enforceGreaterOrEqual(final long value, final long min, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value < min)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be greater or equal to " + min + ".");
    }

    /**
     * Ensures that a specified long value is greater than or equal to a minimum allowed value.
     * If the specified value is less than the minimum, an {@link IndexOutOfBoundsException}
     * is thrown.
     *
     * @param value the long value to be checked.
     * @param min   the minimum allowed long value.
     * @throws IndexOutOfBoundsException if {@code value} is less than {@code min}.
     */
    public static void enforceGreaterOrEqual(final long value, final long min) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(value, min, null);
    }

    /**
     * Ensures that the specified long value is greater than the provided minimum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   the long value to be checked.
     * @param min     the minimum long value that the input value must be greater than.
     * @param message an optional custom message for the exception; if null or blank, a default message is used.
     * @throws IndexOutOfBoundsException if the provided value is not greater than the specified maximum.
     */
    public static void enforceGreaterThan(final long value, final long min, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value <= min)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be greater than " + min + ".");
    }

    /**
     * Ensures that the specified long value is greater than the provided minimum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value the long value to be checked.
     * @param min   the minimum long value that the input value must be greater than.
     * @throws IndexOutOfBoundsException if the provided value is not greater than the specified maximum.
     */
    public static void enforceGreaterThan(final long value, final long min) throws IndexOutOfBoundsException {
        enforceGreaterThan(value, min, null);
    }

    // ==== Float ==== //

    /**
     * Ensures that the two provided float values are equal.
     * If the values are not equal, an {@link IllegalArgumentException} is thrown with the specified message
     * or a default message if no message is provided.
     *
     * @param v1      the first float value to compare.
     * @param v2      the second float value to compare against the first.
     * @param message the custom exception message to use if the values are not equal; may be null or blank.
     * @throws IllegalArgumentException if the two values are not equal.
     */
    public static void enforceEqual(final float v1, final float v2, final @Nullable String message) throws IllegalArgumentException {
        if (v1 != v2)
            if (message != null && !message.isBlank())
                throw new IllegalArgumentException(message);
            else
                throw new IllegalArgumentException("Value " + v1 + " is not equal to " + v2 + ".");
    }


    /**
     * Ensures that the two provided float values are equal.
     * If the values are not equal, an {@link IllegalArgumentException} is thrown with the default message.
     *
     * @param v1 the first float value to compare.
     * @param v2 the second float value to compare against the first.
     * @throws IllegalArgumentException if the two values are not equal.
     */
    public static void enforceEqual(final float v1, final float v2) throws IllegalArgumentException {
        enforceEqual(v1, v2, null);
    }

    /**
     * Ensures that the two provided float values are not equal.
     * If the values are equal, an {@link IllegalArgumentException} is thrown with the specified message
     * or a default message if no message is provided.
     *
     * @param v1      the first float value to compare.
     * @param v2      the second float value to compare against the first.
     * @param message the custom exception message to use if the values are equal; may be null or blank.
     * @throws IllegalArgumentException if the two values are equal.
     */
    public static void enforceNotEqual(final float v1, final float v2, final @Nullable String message) throws IllegalArgumentException {
        if (v1 == v2)
            if (message != null && !message.isBlank())
                throw new IllegalArgumentException(message);
            else
                throw new IllegalArgumentException("Value " + v1 + " must not be equal to " + v2 + ".");
    }

    /**
     * Ensures that the two provided float values are not equal.
     * If the values are equal, an {@link IllegalArgumentException} is thrown with the default message.
     *
     * @param v1 the first float value to compare.
     * @param v2 the second float value to compare against the first.
     * @throws IllegalArgumentException if the two values are equal.
     */
    public static void enforceNotEqual(final float v1, final float v2) throws IllegalArgumentException {
        enforceNotEqual(v1, v2, null);
    }

    /**
     * Ensures that a given float value falls within the specified range.
     * If the value is outside the range, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   The value to be checked.
     * @param min     The minimum acceptable value (inclusive).
     * @param max     The maximum acceptable value (inclusive).
     * @param message An optional custom error message to be used if the value is out of range.
     *                If null or blank, a default error message is generated.
     * @throws IndexOutOfBoundsException If the value is outside the specified range.
     */
    public static void enforceRange(final float value, final float min, final float max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value < min || value > max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " is out of range [" + min + ", " + max + "].");
    }

    /**
     * Ensures that a given float value falls within the specified range.
     * If the value is outside the range, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value The value to be checked.
     * @param min   The minimum acceptable value (inclusive).
     * @param max   The maximum acceptable value (inclusive).
     * @throws IndexOutOfBoundsException If the value is outside the specified range.
     */
    public static void enforceRange(final float value, final float min, final float max) throws IndexOutOfBoundsException {
        enforceRange(value, min, max, null);
    }

    /**
     * Ensures that a specified float value is less than or equal to a maximum allowed value.
     * If the specified value is greater than the maximum, an {@link IndexOutOfBoundsException}
     * is thrown, optionally with a custom error message.
     *
     * @param value   the float value to be checked.
     * @param max     the maximum allowed float value.
     * @param message an optional custom error message that will be included in the exception
     *                if the check fails; may be {@code null} or blank.
     * @throws IndexOutOfBoundsException if {@code value} is greater than {@code max}.
     */
    public static void enforceLowerOrEqual(final float value, final float max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value > max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be lower or equal to " + max + ".");
    }

    /**
     * Ensures that a specified float value is less than or equal to a maximum allowed value.
     * If the specified value is greater than the maximum, an {@link IndexOutOfBoundsException}
     * is thrown.
     *
     * @param value the float value to be checked.
     * @param max   the maximum allowed float value.
     * @throws IndexOutOfBoundsException if {@code value} is greater than {@code max}.
     */
    public static void enforceLowerOrEqual(final float value, final float max) throws IndexOutOfBoundsException {
        enforceLowerOrEqual(value, max, null);
    }

    /**
     * Ensures that the specified float value is less than the provided maximum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   the float value to be checked.
     * @param max     the maximum float value that the input value must be less than.
     * @param message an optional custom message for the exception; if null or blank, a default message is used.
     * @throws IndexOutOfBoundsException if the provided value is not less than the specified maximum.
     */
    public static void enforceLowerThan(final float value, final float max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value >= max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be lower than " + max + ".");
    }

    /**
     * Ensures that the specified float value is less than the provided maximum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value the float value to be checked.
     * @param max   the maximum float value that the input value must be less than.
     * @throws IndexOutOfBoundsException if the provided value is not less than the specified maximum.
     */
    public static void enforceLowerThan(final float value, final float max) throws IndexOutOfBoundsException {
        enforceLowerThan(value, max, null);
    }

    /**
     * Ensures that a specified float value is greater than or equal to a minimum allowed value.
     * If the specified value is less than the minimum, an {@link IndexOutOfBoundsException}
     * is thrown, optionally with a custom error message.
     *
     * @param value   the float value to be checked.
     * @param min     the minimum allowed float value.
     * @param message an optional custom error message that will be included in the exception
     *                if the check fails; may be {@code null} or blank.
     * @throws IndexOutOfBoundsException if {@code value} is less than {@code min}.
     */
    public static void enforceGreaterOrEqual(final float value, final float min, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value < min)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be greater or equal to " + min + ".");
    }

    /**
     * Ensures that a specified float value is greater than or equal to a minimum allowed value.
     * If the specified value is less than the minimum, an {@link IndexOutOfBoundsException}
     * is thrown.
     *
     * @param value the float value to be checked.
     * @param min   the minimum allowed float value.
     * @throws IndexOutOfBoundsException if {@code value} is less than {@code min}.
     */
    public static void enforceGreaterOrEqual(final float value, final float min) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(value, min, null);
    }

    /**
     * Ensures that the specified float value is greater than the provided minimum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   the float value to be checked.
     * @param min     the minimum float value that the input value must be greater than.
     * @param message an optional custom message for the exception; if null or blank, a default message is used.
     * @throws IndexOutOfBoundsException if the provided value is not greater than the specified maximum.
     */
    public static void enforceGreaterThan(final float value, final float min, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value <= min)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be greater than " + min + ".");
    }

    /**
     * Ensures that the specified float value is greater than the provided minimum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value the float value to be checked.
     * @param min   the minimum float value that the input value must be greater than.
     * @throws IndexOutOfBoundsException if the provided value is not greater than the specified maximum.
     */
    public static void enforceGreaterThan(final float value, final float min) throws IndexOutOfBoundsException {
        enforceGreaterThan(value, min, null);
    }

    // ==== Double ==== //

    /**
     * Ensures that the two provided double values are equal.
     * If the values are not equal, an {@link IllegalArgumentException} is thrown with the specified message
     * or a default message if no message is provided.
     *
     * @param v1      the first double value to compare.
     * @param v2      the second double value to compare against the first.
     * @param message the custom exception message to use if the values are not equal; may be null or blank.
     * @throws IllegalArgumentException if the two values are not equal.
     */
    public static void enforceEqual(final double v1, final double v2, final @Nullable String message) throws IllegalArgumentException {
        if (v1 != v2)
            if (message != null && !message.isBlank())
                throw new IllegalArgumentException(message);
            else
                throw new IllegalArgumentException("Value " + v1 + " is not equal to " + v2 + ".");
    }


    /**
     * Ensures that the two provided double values are equal.
     * If the values are not equal, an {@link IllegalArgumentException} is thrown with the default message.
     *
     * @param v1 the first double value to compare.
     * @param v2 the second double value to compare against the first.
     * @throws IllegalArgumentException if the two values are not equal.
     */
    public static void enforceEqual(final double v1, final double v2) throws IllegalArgumentException {
        enforceEqual(v1, v2, null);
    }

    /**
     * Ensures that the two provided double values are not equal.
     * If the values are equal, an {@link IllegalArgumentException} is thrown with the specified message
     * or a default message if no message is provided.
     *
     * @param v1      the first double value to compare.
     * @param v2      the second double value to compare against the first.
     * @param message the custom exception message to use if the values are equal; may be null or blank.
     * @throws IllegalArgumentException if the two values are equal.
     */
    public static void enforceNotEqual(final double v1, final double v2, final @Nullable String message) throws IllegalArgumentException {
        if (v1 == v2)
            if (message != null && !message.isBlank())
                throw new IllegalArgumentException(message);
            else
                throw new IllegalArgumentException("Value " + v1 + " must not be equal to " + v2 + ".");
    }

    /**
     * Ensures that the two provided double values are not equal.
     * If the values are equal, an {@link IllegalArgumentException} is thrown with the default message.
     *
     * @param v1 the first double value to compare.
     * @param v2 the second double value to compare against the first.
     * @throws IllegalArgumentException if the two values are equal.
     */
    public static void enforceNotEqual(final double v1, final double v2) throws IllegalArgumentException {
        enforceNotEqual(v1, v2, null);
    }

    /**
     * Ensures that a given double value falls within the specified range.
     * If the value is outside the range, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   The value to be checked.
     * @param min     The minimum acceptable value (inclusive).
     * @param max     The maximum acceptable value (inclusive).
     * @param message An optional custom error message to be used if the value is out of range.
     *                If null or blank, a default error message is generated.
     * @throws IndexOutOfBoundsException If the value is outside the specified range.
     */
    public static void enforceRange(final double value, final double min, final double max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value < min || value > max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " is out of range [" + min + ", " + max + "].");
    }

    /**
     * Ensures that a given double value falls within the specified range.
     * If the value is outside the range, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value The value to be checked.
     * @param min   The minimum acceptable value (inclusive).
     * @param max   The maximum acceptable value (inclusive).
     * @throws IndexOutOfBoundsException If the value is outside the specified range.
     */
    public static void enforceRange(final double value, final double min, final double max) throws IndexOutOfBoundsException {
        enforceRange(value, min, max, null);
    }

    /**
     * Ensures that a specified double value is less than or equal to a maximum allowed value.
     * If the specified value is greater than the maximum, an {@link IndexOutOfBoundsException}
     * is thrown, optionally with a custom error message.
     *
     * @param value   the double value to be checked.
     * @param max     the maximum allowed double value.
     * @param message an optional custom error message that will be included in the exception
     *                if the check fails; may be {@code null} or blank.
     * @throws IndexOutOfBoundsException if {@code value} is greater than {@code max}.
     */
    public static void enforceLowerOrEqual(final double value, final double max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value > max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be lower or equal to " + max + ".");
    }

    /**
     * Ensures that a specified double value is less than or equal to a maximum allowed value.
     * If the specified value is greater than the maximum, an {@link IndexOutOfBoundsException}
     * is thrown.
     *
     * @param value the double value to be checked.
     * @param max   the maximum allowed double value.
     * @throws IndexOutOfBoundsException if {@code value} is greater than {@code max}.
     */
    public static void enforceLowerOrEqual(final double value, final double max) throws IndexOutOfBoundsException {
        enforceLowerOrEqual(value, max, null);
    }

    /**
     * Ensures that the specified double value is less than the provided maximum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   the double value to be checked.
     * @param max     the maximum double value that the input value must be less than.
     * @param message an optional custom message for the exception; if null or blank, a default message is used.
     * @throws IndexOutOfBoundsException if the provided value is not less than the specified maximum.
     */
    public static void enforceLowerThan(final double value, final double max, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value >= max)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be lower than " + max + ".");
    }

    /**
     * Ensures that the specified double value is less than the provided maximum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value the double value to be checked.
     * @param max   the maximum double value that the input value must be less than.
     * @throws IndexOutOfBoundsException if the provided value is not less than the specified maximum.
     */
    public static void enforceLowerThan(final double value, final double max) throws IndexOutOfBoundsException {
        enforceLowerThan(value, max, null);
    }

    /**
     * Ensures that a specified double value is greater than or equal to a minimum allowed value.
     * If the specified value is less than the minimum, an {@link IndexOutOfBoundsException}
     * is thrown, optionally with a custom error message.
     *
     * @param value   the double value to be checked.
     * @param min     the minimum allowed double value.
     * @param message an optional custom error message that will be included in the exception
     *                if the check fails; may be {@code null} or blank.
     * @throws IndexOutOfBoundsException if {@code value} is less than {@code min}.
     */
    public static void enforceGreaterOrEqual(final double value, final double min, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value < min)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be greater or equal to " + min + ".");
    }

    /**
     * Ensures that a specified double value is greater than or equal to a minimum allowed value.
     * If the specified value is less than the minimum, an {@link IndexOutOfBoundsException}
     * is thrown.
     *
     * @param value the double value to be checked.
     * @param min   the minimum allowed double value.
     * @throws IndexOutOfBoundsException if {@code value} is less than {@code min}.
     */
    public static void enforceGreaterOrEqual(final double value, final double min) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(value, min, null);
    }

    /**
     * Ensures that the specified double value is greater than the provided minimum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value   the double value to be checked.
     * @param min     the minimum double value that the input value must be greater than.
     * @param message an optional custom message for the exception; if null or blank, a default message is used.
     * @throws IndexOutOfBoundsException if the provided value is not greater than the specified maximum.
     */
    public static void enforceGreaterThan(final double value, final double min, final @Nullable String message) throws IndexOutOfBoundsException {
        if (value <= min)
            if (message != null && !message.isBlank())
                throw new IndexOutOfBoundsException(message);
            else
                throw new IndexOutOfBoundsException("Value " + value + " must be greater than " + min + ".");
    }

    /**
     * Ensures that the specified double value is greater than the provided minimum value.
     * If the condition is not met, an {@link IndexOutOfBoundsException} is thrown.
     *
     * @param value the double value to be checked.
     * @param min   the minimum double value that the input value must be greater than.
     * @throws IndexOutOfBoundsException if the provided value is not greater than the specified maximum.
     */
    public static void enforceGreaterThan(final double value, final double min) throws IndexOutOfBoundsException {
        enforceGreaterThan(value, min, null);
    }

    // ==== Object ==== //

    /**
     * Ensures that the given object is non-null. If the object is null, a {@link NullPointerException}
     * is thrown with the specified message. If the message is null or blank, a default message is used.
     *
     * @param value   the object to check for nullity.
     * @param message the custom message to include in the exception if the object is null.
     * @throws NullPointerException if the provided object is null.
     * @deprecated Use {@link Objects#requireNonNull(Object, String)} instead.
     */
    @Deprecated(forRemoval = true)
    public static void enforceNonNull(final @Nullable Object value, final @Nullable String message) throws NullPointerException {
        if (message != null && !message.isBlank())
            Objects.requireNonNull(value, message);
        else
            Objects.requireNonNull(value, "Value must not be null.");
    }

    /**
     * Ensures that the given object is non-null. If the object is null, a {@link NullPointerException}
     * is thrown with the default message.
     *
     * @param value the object to check for nullity.
     * @throws NullPointerException if the provided object is null.
     * @deprecated Use {@link Objects#requireNonNull(Object)} instead.
     */
    @Deprecated(forRemoval = true)
    public static void enforceNonNull(final @Nullable Object value) throws NullPointerException {
        enforceNonNull(value, null);
    }
}
