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

package fr.icy.common.type;

import fr.icy.common.math.MathUtil;
import fr.icy.model.render.vtk.VtkUtil;
import loci.formats.FormatTools;
import ome.xml.model.enums.PixelType;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.Objects;

/**
 * DataType class.<br>
 * This class is used to define the internal native data type of a given object.
 *
 * @author Stéphane Dallongeville
 * @author Thomas Musset
 */
public enum DataType {
    // UBYTE (unsigned 8 bits integer)
    UBYTE(
            Byte.SIZE, true, false,
            0d, MathUtil.POW2_8_DOUBLE - 1d,
            Byte.TYPE, DataBuffer.TYPE_BYTE, PixelType.UINT8,
            "Unsigned Byte (8 bits)", "8 bits"
    ),
    // BYTE (signed 8 bits integer)
    BYTE(
            Byte.SIZE, true, true,
            Byte.MIN_VALUE, Byte.MAX_VALUE,
            Byte.TYPE, DataBuffer.TYPE_BYTE, PixelType.INT8,
            "Signed Byte (8 bits)", "8 bits (signed)"
    ),
    // USHORT (unsigned 16 bits integer)
    USHORT(
            Short.SIZE, true, false,
            0d, MathUtil.POW2_16_DOUBLE - 1d,
            Short.TYPE, DataBuffer.TYPE_USHORT, PixelType.UINT16,
            "Unsigned Short (16 bits)", "16 bits"
    ),
    // SHORT (signed 16 bits integer)
    SHORT(
            Short.SIZE, true, true,
            Short.MIN_VALUE, Short.MAX_VALUE,
            Short.TYPE, DataBuffer.TYPE_SHORT, PixelType.INT16,
            "Signed Short (16 bits)", "16 bits (signed)"
    ),
    // UINT (unsigned 32bits integer)
    UINT(
            Integer.SIZE, true, false,
            0d, MathUtil.POW2_32_DOUBLE - 1d,
            Integer.TYPE, DataBuffer.TYPE_INT, PixelType.UINT32,
            "Unsigned Integer (32 bits)", "32 bits"
    ),
    // INT (signed 32 bits integer)
    INT(
            Integer.SIZE, true, true,
            Integer.MIN_VALUE, Integer.MAX_VALUE,
            Integer.TYPE, DataBuffer.TYPE_INT, PixelType.INT32,
            "Signed Integer (32 bits)", "32 bits (signed)"
    ),
    // ULONG (unsigned 64 bits integer)
    // WARNING : double data type loss information here for min/ma
    ULONG(
            Long.SIZE, true, false,
            0d, MathUtil.POW2_64_DOUBLE - 1d,
            Long.TYPE, DataBuffer.TYPE_UNDEFINED, null,
            "Unsigned Long (64 bits)", "64 bits"
    ),
    // LONG (signed 64 bits integer)
    // WARNING : double data type loss information here for min/max
    LONG(
            Long.SIZE, true, true,
            Long.MIN_VALUE, Long.MAX_VALUE,
            Long.TYPE, DataBuffer.TYPE_UNDEFINED, null,
            "Signed Long (64 bits)", "64 bits (signed)"
    ),
    // FLOAT (signed 32 bits float)
    FLOAT(
            Float.SIZE, false, true,
            -Float.MAX_VALUE, Float.MAX_VALUE,
            Float.TYPE, DataBuffer.TYPE_FLOAT, PixelType.FLOAT,
            "Float (32 bits)", "Float"
    ),
    // DOUBLE (signed 64 bits float)
    DOUBLE(
            Double.SIZE, false, true,
            -Double.MAX_VALUE, Double.MAX_VALUE,
            Double.TYPE, DataBuffer.TYPE_DOUBLE, PixelType.DOUBLE,
            "Double (64 bits)", "Double"
    );

    /**
     * cached
     */
    public static final double UBYTE_MAX_VALUE = MathUtil.POW2_8_DOUBLE - 1;
    public static final double USHORT_MAX_VALUE = MathUtil.POW2_16_DOUBLE - 1;
    public static final double UINT_MAX_VALUE = MathUtil.POW2_32_DOUBLE - 1;
    public static final double ULONG_MAX_VALUE = MathUtil.POW2_64_DOUBLE - 1;
    public static final double INT_MIN_VALUE = Integer.MIN_VALUE;
    public static final double LONG_MIN_VALUE = Long.MIN_VALUE;
    public static final double INT_MAX_VALUE = Integer.MAX_VALUE;
    public static final double LONG_MAX_VALUE = Long.MAX_VALUE;

    public static final float UBYTE_MAX_VALUE_F = MathUtil.POW2_8_FLOAT - 1;
    public static final float USHORT_MAX_VALUE_F = MathUtil.POW2_16_FLOAT - 1;
    public static final float UINT_MAX_VALUE_F = MathUtil.POW2_32_FLOAT - 1;
    public static final float ULONG_MAX_VALUE_F = MathUtil.POW2_64_FLOAT - 1;
    public static final float INT_MIN_VALUE_F = Integer.MIN_VALUE;
    public static final float LONG_MIN_VALUE_F = Long.MIN_VALUE;
    public static final float INT_MAX_VALUE_F = Integer.MAX_VALUE;
    public static final float LONG_MAX_VALUE_F = Long.MAX_VALUE;

    /**
     * Return all dataType as String items array (can be used for ComboBox).<br>
     *
     * @param javaTypeOnly Define if we want only java compatible data type (no unsigned integer types)
     * @param longString   Define if we want long string format (bpp information)
     */
    public static String @NonNull [] getItems(final boolean javaTypeOnly, final boolean longString) {
        final ArrayList<String> result = new ArrayList<>();

        for (final DataType dataType : DataType.values())
            if (((!javaTypeOnly) || dataType.isJavaType()))
                result.add(dataType.toString(longString));

        return result.toArray(new String[0]);
    }

    /**
     * Return all dataType as String items array (can be used for ComboBox).<br>
     *
     * @param javaTypeOnly Define if we want only java compatible data type (no unsigned integer types)
     * @param longString   Define if we want long string format (bpp information)
     * @param wantUndef    Define if we want the UNDEFINED data type in the list
     * @deprecated Use {@link #getItems(boolean, boolean)} instead.
     */
    @Deprecated(since = "3.0.0", forRemoval = true)
    public static String @NonNull [] getItems(final boolean javaTypeOnly, final boolean longString, final boolean wantUndef) {
        final ArrayList<String> result = new ArrayList<>();

        for (final DataType dataType : DataType.values())
            if (((!javaTypeOnly) || dataType.isJavaType()))
                result.add(dataType.toString(longString));

        return result.toArray(new String[0]);
    }

    /**
     * Retrieves the corresponding {@code DataType} for the given string value.
     * The method checks against both case-sensitive and case-insensitive matches
     * of the {@code DataType}'s string representation.
     *
     * @param value the string representation of the data type to be retrieved; must not be null.
     * @return the {@code DataType} corresponding to the specified string value.
     * @throws NullPointerException if the {@code value} is null.
     * @throws IllegalArgumentException if the {@code value} does not correspond to any supported {@code DataType}.
     */
    public static @NonNull DataType getDataType(final @NonNull String value) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(value, "String value cannot be null.");

        for (final DataType dataType : DataType.values())
            if (dataType.toString(false).equalsIgnoreCase(value) || dataType.toString(true).equalsIgnoreCase(value))
                return dataType;

        throw new IllegalArgumentException("Unsupported data type: " + value);
    }

    /**
     * Determines the corresponding {@code DataType} for the provided primitive class type.
     *
     * @param classType the class type to evaluate; must not be null
     * @return the corresponding {@code DataType} of the provided class type
     * @throws NullPointerException if the provided class type is null
     * @throws IllegalArgumentException if the provided class type is unsupported
     */
    public static @NonNull DataType getDataType(final @NonNull Class<?> classType) throws NullPointerException, IllegalArgumentException {
        Objects.requireNonNull(classType, "Class type cannot be null.");

        if (classType.equals(Byte.TYPE))
            return DataType.BYTE;
        if (classType.equals(Short.TYPE))
            return DataType.SHORT;
        if (classType.equals(Integer.TYPE))
            return DataType.INT;
        if (classType.equals(Long.TYPE))
            return DataType.LONG;
        if (classType.equals(Float.TYPE))
            return DataType.FLOAT;
        if (classType.equals(Double.TYPE))
            return DataType.DOUBLE;

        throw new IllegalArgumentException("Unsupported data type: " + classType);
    }

    /**
     * Converts a VTK type constant to its corresponding {@code DataType}.
     *
     * @param vtkType the VTK type constant, which must be one of the predefined values:
     *                {@code VtkUtil.VTK_UNSIGNED_CHAR}, {@code VtkUtil.VTK_CHAR},
     *                {@code VtkUtil.VTK_SIGNED_CHAR}, {@code VtkUtil.VTK_UNSIGNED_SHORT},
     *                {@code VtkUtil.VTK_SHORT}, {@code VtkUtil.VTK_UNSIGNED_INT},
     *                {@code VtkUtil.VTK_INT}, {@code VtkUtil.VTK_FLOAT},
     *                {@code VtkUtil.VTK_DOUBLE}, {@code VtkUtil.VTK_UNSIGNED_LONG},
     *                or {@code VtkUtil.VTK_LONG}.
     * @return the corresponding {@code DataType} for the provided VTK type constant.
     * @throws IllegalArgumentException if the provided {@code vtkType} is unknown
     *         or not one of the recognized VTK type constants.
     */
    @Contract(pure = true)
    public static @NonNull DataType getDataTypeFromVTKType(final @MagicConstant(intValues = {VtkUtil.VTK_UNSIGNED_CHAR, VtkUtil.VTK_CHAR, VtkUtil.VTK_SIGNED_CHAR, VtkUtil.VTK_UNSIGNED_SHORT, VtkUtil.VTK_SHORT, VtkUtil.VTK_UNSIGNED_INT, VtkUtil.VTK_INT, VtkUtil.VTK_FLOAT, VtkUtil.VTK_DOUBLE, VtkUtil.VTK_UNSIGNED_LONG, VtkUtil.VTK_LONG}) int vtkType) throws IllegalArgumentException {
        return switch (vtkType) {
            case VtkUtil.VTK_UNSIGNED_CHAR -> UBYTE;
            case VtkUtil.VTK_CHAR, VtkUtil.VTK_SIGNED_CHAR -> BYTE;
            case VtkUtil.VTK_UNSIGNED_SHORT -> USHORT;
            case VtkUtil.VTK_SHORT -> SHORT;
            case VtkUtil.VTK_UNSIGNED_INT -> UINT;
            case VtkUtil.VTK_INT -> INT;
            case VtkUtil.VTK_UNSIGNED_LONG -> ULONG;
            case VtkUtil.VTK_LONG -> LONG;
            case VtkUtil.VTK_FLOAT -> FLOAT;
            case VtkUtil.VTK_DOUBLE -> DOUBLE;
            default -> throw new IllegalArgumentException("Unknown or unrecognized VTK type: " + vtkType);
        };
    }

    /**
     * Converts a given {@code DataBuffer} type constant to a corresponding {@code DataType}.
     * Maps well-defined {@code DataBuffer} type values to their appropriate {@code DataType}.
     * Throws an exception if the type is undefined or unrecognized.
     *
     * @param dataBufferType a constant value representing a {@code DataBuffer} type.
     *                       Must be one of the following:
     *                       {@code DataBuffer.TYPE_BYTE}, {@code DataBuffer.TYPE_SHORT},
     *                       {@code DataBuffer.TYPE_USHORT}, {@code DataBuffer.TYPE_INT},
     *                       {@code DataBuffer.TYPE_FLOAT}, {@code DataBuffer.TYPE_DOUBLE},
     *                       or {@code DataBuffer.TYPE_UNDEFINED}.
     * @return the corresponding {@code DataType} for the given {@code DataBuffer} type.
     * @throws IllegalArgumentException if the {@code dataBufferType} value is unknown or not recognized.
     * @throws UnsupportedOperationException if {@code dataBufferType} is {@code DataBuffer.TYPE_UNDEFINED}.
     */
    @Contract(pure = true)
    public static @NonNull DataType getDataTypeFromDataBufferType(final @MagicConstant(intValues = {DataBuffer.TYPE_BYTE, DataBuffer.TYPE_SHORT, DataBuffer.TYPE_USHORT, DataBuffer.TYPE_INT, DataBuffer.TYPE_FLOAT, DataBuffer.TYPE_DOUBLE, DataBuffer.TYPE_UNDEFINED}) int dataBufferType) throws IllegalArgumentException, UnsupportedOperationException {
        return switch (dataBufferType) {
            case DataBuffer.TYPE_BYTE -> UBYTE; // consider as unsigned by default
            case DataBuffer.TYPE_SHORT -> SHORT;
            case DataBuffer.TYPE_USHORT -> USHORT;
            case DataBuffer.TYPE_INT -> UINT; // consider as unsigned by default
            case DataBuffer.TYPE_FLOAT -> FLOAT;
            case DataBuffer.TYPE_DOUBLE -> DOUBLE;
            case DataBuffer.TYPE_UNDEFINED -> throw new UnsupportedOperationException("DataBuffer type undefined");
            default -> throw new IllegalArgumentException("Unknown DataBuffer type : " + dataBufferType);
        };
    }

    /**
     * Converts a FormatTools type constant to the corresponding {@code DataType}.
     *
     * @param type The FormatTools type constant to be converted.
     *             Must be one of the following values:
     *             {@code FormatTools.INT8}, {@code FormatTools.UINT8}, {@code FormatTools.INT16},
     *             {@code FormatTools.UINT16}, {@code FormatTools.INT32}, {@code FormatTools.UINT32},
     *             {@code FormatTools.FLOAT}, or {@code FormatTools.DOUBLE}.
     * @return The {@code DataType} matching the specified FormatTools type constant.
     * @throws IllegalArgumentException If the provided type is not a recognized FormatTools type constant.
     */
    @Contract(pure = true)
    public static @NonNull DataType getDataTypeFromFormatToolsType(final @MagicConstant(intValues = {FormatTools.INT8, FormatTools.UINT8, FormatTools.INT16, FormatTools.UINT16, FormatTools.INT32, FormatTools.UINT32, FormatTools.FLOAT, FormatTools.DOUBLE}) int type) throws IllegalArgumentException {
        return switch (type) {
            case FormatTools.INT8 -> BYTE;
            case FormatTools.UINT8 -> UBYTE;
            case FormatTools.INT16 -> SHORT;
            case FormatTools.UINT16 -> USHORT;
            case FormatTools.INT32 -> INT;
            case FormatTools.UINT32 -> UINT;
            case FormatTools.FLOAT -> FLOAT;
            case FormatTools.DOUBLE -> DOUBLE;
            default -> throw new IllegalArgumentException("Unknown FormatTools type : " + type);
        };
    }

    /**
     * Converts a {@link PixelType} to its corresponding {@link DataType}.
     *
     * @param type the pixel type to be converted; must not be null.
     * @return the associated data type for the provided pixel type.
     * @throws NullPointerException if the input {@code type} is null.
     * @throws UnsupportedOperationException if the input {@code type} does not map to a supported data type.
     */
    @Contract(pure = true)
    public static @NonNull DataType getDataTypeFromPixelType(final @NonNull PixelType type) throws NullPointerException, UnsupportedOperationException {
        Objects.requireNonNull(type, "Pixel type cannot be null.");

        return switch (type) {
            case INT8 -> BYTE;
            case UINT8 -> UBYTE;
            case INT16 -> SHORT;
            case UINT16 -> USHORT;
            case INT32 -> INT;
            case UINT32 -> UINT;
            case FLOAT -> FLOAT;
            case DOUBLE -> DOUBLE;
            default -> throw new UnsupportedOperationException("Unsupported PixelType : " + type);
        };
    }

    /**
     * Check if DataType is undefined.
     */
    @Contract(value = "null -> true; !null -> false", pure = true)
    public static boolean isUndefined(final @Nullable DataType dataType) {
        return dataType == null;
    }

    /**
     * internals properties
     */
    private final String longString;
    private final String string;
    private final int bitSize;
    private final boolean integer;
    private final boolean signed;
    private final double min;
    private final double max;
    private final Class<?> primitiveClass;
    private final int dataBufferType;
    private final PixelType pixelType;

    DataType(
            final int bitSize, final boolean integer, final boolean signed,
            final double min, final double max,
            final @NonNull Class<?> primitiveClass, final int dataBufferType, final @Nullable PixelType pixelType, // FIXME LONG and ULONG have PixelType = null
            final @NonNull String longString, final @NonNull String string
    ) {
        this.bitSize = bitSize;
        this.integer = integer;
        this.signed = signed;
        this.min = min;
        this.max = max;
        this.primitiveClass = primitiveClass;
        this.dataBufferType = dataBufferType;
        this.pixelType = pixelType;
        this.longString = longString;
        this.string = string;
    }

    /**
     * Return the java compatible data type (signed integer type only).<br>
     * Can be only one of the following :<br>
     * {@link DataType#BYTE}<br>
     * {@link DataType#SHORT}<br>
     * {@link DataType#INT}<br>
     * {@link DataType#LONG}<br>
     * {@link DataType#FLOAT}<br>
     * {@link DataType#DOUBLE}<br>
     */
    @Contract(pure = true)
    public @NonNull DataType getJavaType() {
        return switch (this) {
            case UBYTE, BYTE -> BYTE;
            case USHORT, SHORT -> SHORT;
            case UINT, INT -> INT;
            case ULONG, LONG -> LONG;
            case FLOAT -> FLOAT;
            case DOUBLE -> DOUBLE;
        };
    }

    /**
     * Return the minimum value for current DataType
     */
    @Contract(pure = true)
    public double getMinValue() {
        return min;
    }

    /**
     * Return the maximum value for current DataType
     */
    @Contract(pure = true)
    public double getMaxValue() {
        return max;
    }

    /**
     * Get the default bounds for current DataType.<br>
     * This actually returns <code>[0,1]</code> for Float or Double DataType.
     */
    @Contract(" -> new")
    public double @NonNull [] getDefaultBounds() {
        if (!integer)
            return new double[]{0d, 1d};

        return new double[]{getMinValue(), getMaxValue()};
    }

    /**
     * Get the bounds <code>[min,max]</code> for current DataType.
     */
    @Contract(" -> new")
    public double @NonNull [] getBounds() {
        return new double[]{getMinValue(), getMaxValue()};
    }

    /**
     * Return true if this is a compatible java data type (signed integer type only)
     */
    @Contract(pure = true)
    public boolean isJavaType() {
        return this == getJavaType();
    }

    /**
     * Return true if this is a signed data type
     */
    @Contract(pure = true)
    public boolean isSigned() {
        return signed;
    }

    /**
     * Return true if this is a float data type
     */
    @Contract(pure = true)
    public boolean isFloat() {
        return !isInteger();
    }

    /**
     * Return true if this is an integer data type
     */
    @Contract(pure = true)
    public boolean isInteger() {
        return integer;
    }

    /**
     * Return the size (in byte) of the specified dataType
     */
    @Contract(pure = true)
    public int getSize() {
        return getBitSize() / 8;
    }

    /**
     * Return the size (in bit) of the specified dataType
     */
    @Contract(pure = true)
    public int getBitSize() {
        return bitSize;
    }

    /**
     * Return true if specified data type has same "basic" type (no sign information) data type
     */
    public boolean isSameJavaType(final @Nullable DataType dataType) {
        if (isUndefined(dataType))
            return false;

        return dataType.getJavaType() == getJavaType();
    }

    /**
     * Return the corresponding primitive class type corresponding to this DataType.
     */
    @Contract(pure = true)
    public @NonNull Class<?> toPrimitiveClass() {
        return primitiveClass;
    }

    /**
     * Return the DataBuffer type corresponding to current DataType
     */
    @Contract(pure = true)
    public int toDataBufferType() {
        return dataBufferType;
    }

    /**
     * Return the PixelType corresponding to current DataType
     */
    // FIXME LONG and ULONG have PixelType = null
    @Contract(pure = true)
    public @Nullable PixelType toPixelType() {
        return pixelType;
    }

    /**
     * Convert DataType to String.<br>
     *
     * @param longString Define if we want long description (bpp information)
     */
    public @NonNull String toString(final boolean longString) {
        if (longString)
            return toLongString();

        return toString();
    }

    /**
     * Convert DataType to long String (long description with bpp information)
     */
    @Contract(pure = true)
    public @NonNull String toLongString() {
        return longString;
    }

    @Contract(pure = true)
    @Override
    public @NonNull String toString() {
        return string;
    }
}
