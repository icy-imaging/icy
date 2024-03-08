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

package icy.type;

import icy.math.MathUtil;
import icy.vtk.VtkUtil;
import loci.formats.FormatTools;
import ome.xml.model.enums.PixelType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.DataBuffer;
import java.util.ArrayList;

/**
 * DataType class.<br>
 * This class is used to define the internal native data type of a given object.
 *
 * @author Stephane Dallongeville
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
    // WARNING : double data type loss information here for min/max
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
    ),
    // UNDEFINED (undefined data type)
    /**
     * @deprecated Use <code>null</code> instance instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    UNDEFINED(
            0, true, false,
            0d, 0d,
            null, DataBuffer.TYPE_UNDEFINED, null,
            "Undefined", "Undefined"
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
     * @param javaTypeOnly
     *        Define if we want only java compatible data type (no unsigned integer types)
     * @param longString
     *        Define if we want long string format (bpp information)
     * @param wantUndef
     *        Define if we want the UNDEFINED data type in the list
     */
    public static @NotNull String[] getItems(final boolean javaTypeOnly, final boolean longString, final boolean wantUndef) {
        final ArrayList<String> result = new ArrayList<>();

        for (final DataType dataType : DataType.values())
            if (((!javaTypeOnly) || dataType.isJavaType()) && (wantUndef || (dataType != UNDEFINED)))
                result.add(dataType.toString(longString));

        return result.toArray(new String[0]);
    }

    /**
     * Return a DataType from the specified string.<br>
     * ex : <code>getDataType("byte")</code> will return <code>DataType.BYTE</code>
     */
    public static @Nullable DataType getDataType(final String value) {
        for (final DataType dataType : DataType.values())
            if (dataType.toString(false).equalsIgnoreCase(value) || dataType.toString(true).equalsIgnoreCase(value))
                return dataType;

        return null;
    }

    /**
     * Return a DataType from old dataType.<br>
     * ex : <code>getDataTypeFromOldDataType(TypeUtil.BYTE, false)</code> will return <code>DataType.UBYTE</code>
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static @Nullable DataType getDataType(final int oldDataType, final boolean signed) {
        return switch (oldDataType) {
            case TypeUtil.TYPE_BYTE -> {
                if (signed)
                    yield BYTE;
                yield UBYTE;
            }
            case TypeUtil.TYPE_SHORT -> {
                if (signed)
                    yield SHORT;
                yield USHORT;
            }
            case TypeUtil.TYPE_INT -> {
                if (signed)
                    yield INT;
                yield UINT;
            }
            case TypeUtil.TYPE_FLOAT -> FLOAT;
            case TypeUtil.TYPE_DOUBLE -> DOUBLE;
            default -> null;
        };
    }

    /**
     * Return a DataType from old dataType.<br>
     * ex : <code>getDataType(TypeUtil.BYTE)</code> will return <code>DataType.BYTE</code>
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public static @Nullable DataType getDataType(final int oldDataType) {
        return getDataType(oldDataType, true);
    }

    /**
     * Return a DataType from the specified primitive class type
     */
    public static @Nullable DataType getDataType(final @NotNull Class<?> classType) {
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

        return null;
    }

    /**
     * Return a DataType from the specified VTK type.<br>
     * ex : <code>getDataTypeFromVTKType(VtkUtil.VTK_INT)</code> will return <code>DataType.INT</code>
     */
    public static @Nullable DataType getDataTypeFromVTKType(final int vtkType) {
        return switch (vtkType) {
            case VtkUtil.VTK_UNSIGNED_CHAR -> UBYTE;
            case VtkUtil.VTK_CHAR, VtkUtil.VTK_SIGNED_CHAR -> BYTE;
            case VtkUtil.VTK_UNSIGNED_SHORT -> USHORT;
            case VtkUtil.VTK_SHORT -> SHORT;
            case VtkUtil.VTK_UNSIGNED_INT -> UINT;
            case VtkUtil.VTK_INT -> INT;
            case VtkUtil.VTK_FLOAT -> FLOAT;
            case VtkUtil.VTK_DOUBLE -> DOUBLE;
            case VtkUtil.VTK_UNSIGNED_LONG -> ULONG;
            case VtkUtil.VTK_LONG -> LONG;
            default -> null;
        };
    }

    /**
     * Return a DataType from the specified DataBuffer type.<br>
     * ex : <code>getDataTypeFromDataBufferType(DataBuffer.TYPE_BYTE)</code> will return <code>DataType.UBYTE</code>
     */
    public static @Nullable DataType getDataTypeFromDataBufferType(final int dataBufferType) {
        return switch (dataBufferType) {
            case DataBuffer.TYPE_BYTE -> UBYTE; // consider as unsigned by default
            case DataBuffer.TYPE_SHORT -> SHORT;
            case DataBuffer.TYPE_USHORT -> USHORT;
            case DataBuffer.TYPE_INT -> UINT; // consider as unsigned by default
            case DataBuffer.TYPE_FLOAT -> FLOAT;
            case DataBuffer.TYPE_DOUBLE -> DOUBLE;
            default -> null;
        };
    }

    /**
     * Return a DataType from the specified FormatTools type.<br>
     * ex : <code>getDataTypeFromFormatToolsType(FormatTools.UINT8)</code> will return <code>DataType.UBYTE</code>
     */
    public static @Nullable DataType getDataTypeFromFormatToolsType(final int type) {
        return switch (type) {
            case FormatTools.INT8 -> BYTE;
            case FormatTools.UINT8 -> UBYTE;
            case FormatTools.INT16 -> SHORT;
            case FormatTools.UINT16 -> USHORT;
            case FormatTools.INT32 -> INT;
            case FormatTools.UINT32 -> UINT;
            case FormatTools.FLOAT -> FLOAT;
            case FormatTools.DOUBLE -> DOUBLE;
            default -> null;
        };
    }

    /**
     * Return a DataType from the specified PixelType.<br>
     * ex : <code>getDataTypeFromPixelType(FormatTools.UINT8)</code> will return <code>DataType.UBYTE</code>
     */
    public static @Nullable DataType getDataTypeFromPixelType(final @NotNull PixelType type) {
        return switch (type) {
            case INT8 -> BYTE;
            case UINT8 -> UBYTE;
            case INT16 -> SHORT;
            case UINT16 -> USHORT;
            case INT32 -> INT;
            case UINT32 -> UINT;
            case FLOAT -> FLOAT;
            case DOUBLE -> DOUBLE;
            default -> null;
        };
    }

    /**
     * Check if DataType is undefined.
     */
    public static boolean isUndefined(final @Nullable DataType dataType) {
        if (dataType == null)
            return true;

        return (UNDEFINED.equals(dataType));
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
            final @Nullable Class<?> primitiveClass, final int dataBufferType, final @Nullable PixelType pixelType, // TODO Change Nullable to NotNull once UNDEFINED is removed
            final @NotNull String longString, final @NotNull String string
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
    public @NotNull DataType getJavaType() {
        return switch (this) {
            case UBYTE -> BYTE;
            case USHORT -> SHORT;
            case UINT -> INT;
            case ULONG -> LONG;
            default -> this;
        };
    }

    /**
     * Return the minimum value for current DataType
     */
    public double getMinValue() {
        return min;
    }

    /**
     * Return the maximum value for current DataType
     */
    public double getMaxValue() {
        return max;
    }

    /**
     * Get the default bounds for current DataType.<br>
     * This actually returns <code>[0,1]</code> for Float or Double DataType.
     */
    public double @NotNull [] getDefaultBounds() {
        if (!integer)
            return new double[]{0d, 1d};

        return new double[]{getMinValue(), getMaxValue()};
    }

    /**
     * Get the bounds <code>[min,max]</code> for current DataType.
     */
    public double @NotNull [] getBounds() {
        return new double[]{getMinValue(), getMaxValue()};
    }

    /**
     * Return true if this is a compatible java data type (signed integer type only)
     */
    public boolean isJavaType() {
        return this == getJavaType();
    }

    /**
     * Return true if this is a signed data type
     */
    public boolean isSigned() {
        return signed;
    }

    /**
     * Return true if this is a float data type
     */
    public boolean isFloat() {
        return !isInteger();
    }

    /**
     * Return true if this is an integer data type
     */
    public boolean isInteger() {
        return integer;
    }

    /**
     * Return the size (in byte) of the specified dataType
     */
    public int getSize() {
        return getBitSize() / 8;
    }

    /**
     * Return the size (in bit) of the specified dataType
     */
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
    // TODO Change Nullable to NotNull once UNDEFINED is removed
    public @Nullable Class<?> toPrimitiveClass() {
        return primitiveClass;
    }

    /**
     * Return the DataBuffer type corresponding to current DataType
     */
    public int toDataBufferType() {
        return dataBufferType;
    }

    /**
     * Return the PixelType corresponding to current DataType
     */
    // TODO Change Nullable to NotNull once UNDEFINED is removed
    public @Nullable PixelType toPixelType() {
        return pixelType;
    }

    /**
     * Convert DataType to String.<br>
     *
     * @param longString
     *        Define if we want long description (bpp information)
     */
    public @NotNull String toString(final boolean longString) {
        if (longString)
            return toLongString();

        return toString();
    }

    /**
     * Convert DataType to long String (long description with bpp information)
     */
    public @NotNull String toLongString() {
        return longString;
    }

    @Override
    public @NotNull String toString() {
        return string;
    }
}
