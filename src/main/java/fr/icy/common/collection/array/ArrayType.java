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
 * Represents a multidimensional array type, which includes information about
 * the native data type and the number of dimensions of the array.
 * <p>
 * This class provides utilities to inspect and manipulate the properties
 * of array types, such as the data type and dimensionality.
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 * @see DataType
 */
public class ArrayType {
    /**
     * Retrieves information about the given array and encapsulates it in an {@code ArrayType} object.
     *
     * @param array the array object to analyze; must not be {@code null}.
     * @return a non-null {@code ArrayType} instance containing the data type and
     * dimensions of the specified array.
     * @throws NullPointerException     if the array is {@code null}.
     * @throws IllegalArgumentException if the provided object is not an array.
     */
    @Contract("_ -> new")
    public static @NonNull ArrayType getArrayInfo(final @NonNull Object array) throws NullPointerException, IllegalArgumentException {
        return ArrayUtil.getArrayType(array);
    }

    /**
     * Represents the native data type of the array.
     * This variable specifies the fundamental type of elements in the array,
     * such as integer, float, or custom-defined types.
     * It is always guaranteed to be non-null.
     */
    private @NonNull DataType dataType;
    /**
     * Specifies the number of dimensions of a multidimensional array.
     * This value must be a positive integer greater than or equal to 1.
     * It represents the depth or level of nesting of the array structure.
     */
    private @Range(from = 1, to = Integer.MAX_VALUE) int dim;

    /**
     * Constructs an {@code ArrayType} instance with the specified data type and number of dimensions.
     *
     * @param dataType the non-null data type of the array.
     * @param dim      the number of dimensions of the array; must be greater than or equal to 1.
     * @throws NullPointerException      if {@code dataType} is null.
     * @throws IndexOutOfBoundsException if {@code dim} is less than 1.
     */
    @Contract(pure = true)
    public ArrayType(final @NonNull DataType dataType, final @Range(from = 1, to = Integer.MAX_VALUE) int dim) throws NullPointerException, IndexOutOfBoundsException {
        super();

        Objects.requireNonNull(dataType, "Data type cannot be null");
        enforceGreaterOrEqual(dim, 1, "Dimension must be greater than or equal to 1");

        this.dataType = dataType;
        this.dim = dim;
    }

    /**
     * Retrieves the non-null data type of the array.
     *
     * @return the data type associated with the array.
     */
    public @NonNull DataType getDataType() {
        return dataType;
    }

    /**
     * Retrieves the number of dimensions of the array.
     *
     * @return the number of dimensions of the array, guaranteed to be greater than or equal to 1.
     */
    public @Range(from = 1, to = Integer.MAX_VALUE) int getDim() {
        return dim;
    }

    /**
     * Sets the data type of the array.
     *
     * @param dataType the non-null data type to be set.
     * @throws NullPointerException if {@code dataType} is null.
     */
    public void setDataType(final @NonNull DataType dataType) throws NullPointerException {
        Objects.requireNonNull(dataType, "Data type cannot be null");
        this.dataType = dataType;
    }

    /**
     * Sets the number of dimensions for the array. The specified value must be greater than or equal to 1.
     *
     * @param dim the number of dimensions to set; must be greater than or equal to 1.
     * @throws IndexOutOfBoundsException if {@code dim} is less than 1.
     */
    public void setDim(final @Range(from = 1, to = Integer.MAX_VALUE) int dim) throws IndexOutOfBoundsException {
        enforceGreaterOrEqual(dim, 1, "Dimension must be greater than or equal to 1");
        this.dim = dim;
    }

    /**
     * Checks whether the given {@code ArrayType} instance is equivalent to the current instance.
     * Two {@code ArrayType} instances are considered equivalent if they have the same data type
     * and the same number of dimensions.
     *
     * @param arrayType the {@code ArrayType} instance to compare with; may be {@code null}.
     * @return {@code true} if the specified {@code ArrayType} is not {@code null},
     * has the same data type, and the same number of dimensions as the current instance;
     * otherwise {@code false}.
     */
    public boolean isSame(final @Nullable ArrayType arrayType) {
        if (arrayType == null)
            return false;

        return (arrayType.dataType == dataType) && (arrayType.dim == dim);
    }

    /**
     * Determines whether the provided object is equal to the current instance.
     * An object is considered equal if it is an instance of {@code ArrayType} and
     * has the same data type and dimensions as the current instance,
     * or if it satisfies the equality check defined in the superclass.
     *
     * @param obj the object to compare against; may be {@code null}.
     * @return {@code true} if the provided object is equal to this instance,
     * otherwise {@code false}.
     */
    @Contract(value = "null -> false", pure = true)
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ArrayType)
            return isSame((ArrayType) obj);

        return super.equals(obj);
    }
}
