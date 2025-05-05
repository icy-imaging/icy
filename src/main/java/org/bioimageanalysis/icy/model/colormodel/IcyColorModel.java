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

package org.bioimageanalysis.icy.model.colormodel;

import org.bioimageanalysis.icy.common.collection.array.Array1DUtil;
import org.bioimageanalysis.icy.common.collection.array.ArrayUtil;
import org.bioimageanalysis.icy.common.event.CollapsibleEvent;
import org.bioimageanalysis.icy.common.event.UpdateEventHandler;
import org.bioimageanalysis.icy.common.listener.ChangeListener;
import org.bioimageanalysis.icy.common.math.Scaler;
import org.bioimageanalysis.icy.common.math.ScalerEvent;
import org.bioimageanalysis.icy.common.math.ScalerListener;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.model.colormap.IcyColorMap;
import org.bioimageanalysis.icy.model.colormodel.IcyColorModelEvent.IcyColorModelEventType;
import org.bioimageanalysis.icy.model.colorspace.IcyColorSpace;
import org.bioimageanalysis.icy.model.colorspace.IcyColorSpaceEvent;
import org.bioimageanalysis.icy.model.colorspace.IcyColorSpaceListener;
import org.bioimageanalysis.icy.model.lut.LUT;

import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public abstract class IcyColorModel extends ColorModel implements ScalerListener, IcyColorSpaceListener, ChangeListener {
    /**
     * scalers for normalization
     */
    protected final Scaler[] normalScalers;
    /**
     * scalers for colorMap
     */
    protected final Scaler[] colormapScalers;

    /**
     * data type
     */
    protected final DataType dataType;

    /**
     * overridden variables
     */
    protected final int numComponents;

    /**
     * listeners
     */
    private final List<IcyColorModelListener> listeners;

    /**
     * internal updater
     */
    private final UpdateEventHandler updater;

    /**
     * Default constructor
     */
    IcyColorModel(final IcyColorSpace colorSpace, final DataType dataType, final int[] bits) {
        super(dataType.getBitSize(), bits, colorSpace, true, false, TRANSLUCENT, dataType.toDataBufferType());

        if (colorSpace.getNumComponents() == 0)
            throw new IllegalArgumentException("Number of components should be > 0");

        // overridden variable
        this.numComponents = colorSpace.getNumComponents();

        listeners = new ArrayList<>();
        updater = new UpdateEventHandler(this, false);

        // data type information
        this.dataType = dataType;

        // get default min and max for datatype
        final double[] defaultBounds = dataType.getDefaultBounds();
        final double min = defaultBounds[0];
        final double max = defaultBounds[1];
        // float type flag
        final boolean isFloat = dataType.isFloat();

        // allocating scalers
        normalScalers = new Scaler[numComponents];
        colormapScalers = new Scaler[numComponents];
        // defining scalers
        for (int i = 0; i < numComponents; i++) {
            // scale for normalization
            normalScalers[i] = new Scaler(min, max, 0f, 1f, !isFloat);
            // scale for colormap
            colormapScalers[i] = new Scaler(min, max, 0f, IcyColorMap.MAX_INDEX, !isFloat);
            // add listener to the colormap scalers only
            colormapScalers[i].addListener(this);
        }

        // add the listener to colorSpace
        getIcyColorSpace().addListener(this);
    }

    /**
     * Default constructor
     */
    IcyColorModel(final int numComponents, final DataType dataType, final int[] bits) {
        this(new IcyColorSpace(numComponents), dataType, bits);
    }

    /**
     * Creates a new ColorModel from source colorModel
     *
     * @param colorModel      source color model
     * @param shareColorSpace set to <i>true</i> to share the source colorModel.colorSpace instance
     * @return a IcyColorModel object
     */
    static IcyColorModel createInstance(final IcyColorModel colorModel, final boolean shareColorSpace) {
        final int numComponents = colorModel.getNumComponents();
        final DataType dataType = colorModel.getDataType();

        if (!shareColorSpace)
            return createInstance(colorModel.getNumComponents(), dataType);

        // get colorSpace
        final IcyColorSpace colorSpace = colorModel.getIcyColorSpace();
        // define bits size
        final int bits = dataType.getBitSize();
        // we have to fake one more extra component for alpha in ColorModel class
        final int numComponentFixed = numComponents + 1;
        final int[] componentBits = new int[numComponentFixed];

        Arrays.fill(componentBits, bits);

        return switch (dataType) {
            case UBYTE -> new UByteColorModel(colorSpace, componentBits);
            case BYTE -> new ByteColorModel(colorSpace, componentBits);
            case USHORT -> new UShortColorModel(colorSpace, componentBits);
            case SHORT -> new ShortColorModel(colorSpace, componentBits);
            case UINT -> new UIntColorModel(colorSpace, componentBits);
            case INT -> new IntColorModel(colorSpace, componentBits);
            case ULONG -> new ULongColorModel(colorSpace, componentBits);
            case LONG -> new LongColorModel(colorSpace, componentBits);
            case FLOAT -> new FloatColorModel(colorSpace, componentBits);
            case DOUBLE -> new DoubleColorModel(colorSpace, componentBits);
            //default -> throw new IllegalArgumentException("Unsupported data type !");
        };
    }

    /**
     * Creates a new ColorModel with the given color component and image data type
     *
     * @param numComponents number of component
     * @param dataType      the type of image data (see {@link DataType})
     * @return a IcyColorModel object
     */
    public static IcyColorModel createInstance(final int numComponents, final DataType dataType) {
        // define bits size
        final int bits = dataType.getBitSize();
        // we have to fake one more extra component for alpha in ColorModel class
        final int numComponentFixed = numComponents + 1;
        final int[] componentBits = new int[numComponentFixed];

        Arrays.fill(componentBits, bits);

        return switch (dataType) {
            case UBYTE -> new UByteColorModel(numComponents, componentBits);
            case BYTE -> new ByteColorModel(numComponents, componentBits);
            case USHORT -> new UShortColorModel(numComponents, componentBits);
            case SHORT -> new ShortColorModel(numComponents, componentBits);
            case UINT -> new UIntColorModel(numComponents, componentBits);
            case INT -> new IntColorModel(numComponents, componentBits);
            case ULONG -> new ULongColorModel(numComponents, componentBits);
            case LONG -> new LongColorModel(numComponents, componentBits);
            case FLOAT -> new FloatColorModel(numComponents, componentBits);
            case DOUBLE -> new DoubleColorModel(numComponents, componentBits);
            //default -> throw new IllegalArgumentException("Unsupported data type !");
        };
    }

    /**
     * Creates a new ColorModel from a given icyColorModel
     *
     * @param colorModel   icyColorModel
     * @param copyColormap flag to indicate if we want to copy colormaps from the given icyColorModel
     * @param copyBounds   flag to indicate if we want to copy bounds from the given icyColorModel
     * @return a IcyColorModel object
     */
    public static IcyColorModel createInstance(final IcyColorModel colorModel, final boolean copyColormap, final boolean copyBounds) {
        final IcyColorModel result = IcyColorModel.createInstance(colorModel.getNumComponents(),
                colorModel.getDataType());

        result.beginUpdate();
        try {
            // copy colormaps from colorModel ?
            if (copyColormap)
                result.setColorMaps(colorModel);
            // copy bounds from colorModel ?
            if (copyBounds)
                result.setBounds(colorModel);
        }
        finally {
            result.endUpdate();
        }

        return result;
    }

    /**
     * Creates a new ColorModel from a given {@link IcyColorModel} using a shared {@link IcyColorSpace} instance
     *
     * @param colorModel icyColorModel source {@link IcyColorModel}
     * @param copyBounds flag to indicate if we want to copy bounds from the given icyColorModel
     * @return a IcyColorModel object which shared the same {@link ColorSpace} instance than the input {@link IcyColorModel}
     */
    public static IcyColorModel createSharedCSInstance(final IcyColorModel colorModel, final boolean copyBounds) {
        final IcyColorModel result = IcyColorModel.createInstance(colorModel, true);

        // copy bounds from colorModel ?
        if (copyBounds)
            result.setBounds(colorModel);

        return result;
    }

    /**
     * @return a new default ColorModel: 4 components, unsigned byte data type
     */
    public static IcyColorModel createInstance() {
        return createInstance(4, DataType.UBYTE);
    }

    /**
     * @param transferType data type
     * @param w            width
     * @param h            height
     * @param numComponent number of component
     * @return a compatible {@link BandedSampleModel}
     */
    public static BandedSampleModel createCompatibleSampleModel(final int transferType, final int w, final int h, final int numComponent) {
        return new BandedSampleModel(transferType, w, h, numComponent);
    }

    /**
     * @param data input 2D array
     * @param w    width of the raster
     * @param h    height of the raster
     * @return a new writable raster from specified data and size.<br>
     * The Object data is internally a 2D array [][]
     */
    public static WritableRaster createWritableRaster(final Object data, final int w, final int h) {
        if (ArrayUtil.getDim(data) != 2)
            throw new IllegalArgumentException("IcyColorModel.createWritableRaster(..) error: 'data' argument should be a 2D array !");

        final DataType dataType = ArrayUtil.getDataType(data);
        final int sizeC = ArrayUtil.getLength(data);

        final SampleModel sm = createCompatibleSampleModel(dataType.toDataBufferType(), w, h, sizeC);

        return switch (dataType) {
            case UBYTE, BYTE -> Raster.createWritableRaster(sm, new DataBufferByte((byte[][]) data, w * h), null);
            case SHORT -> Raster.createWritableRaster(sm, new DataBufferShort((short[][]) data, w * h), null);
            case USHORT -> Raster.createWritableRaster(sm, new DataBufferUShort((short[][]) data, w * h), null);
            case UINT, INT -> Raster.createWritableRaster(sm, new DataBufferInt((int[][]) data, w * h), null);
            case FLOAT -> Raster.createWritableRaster(sm, new DataBufferFloat((float[][]) data, w * h), null);
            case DOUBLE -> Raster.createWritableRaster(sm, new DataBufferDouble((double[][]) data, w * h), null);
            default -> throw new IllegalArgumentException("IcyColorModel.createWritableRaster(..) error: unsupported data type : " + dataType);
        };
    }

    @Override
    public SampleModel createCompatibleSampleModel(final int w, final int h) {
        return createCompatibleSampleModel(transferType, w, h, getNumComponents());
    }

    @Override
    public WritableRaster createCompatibleWritableRaster(final int w, final int h) {
        final SampleModel sm = createCompatibleSampleModel(w, h);

        return Raster.createWritableRaster(sm, sm.createDataBuffer(), null);
    }

    /**
     * @param data input array
     * @param w    width of the raster
     * @param h    height of the raster
     * @return a new writable raster from specified data and size.<br>
     */
    public WritableRaster createWritableRaster(final Object[] data, final int w, final int h) {
        final SampleModel sm = createCompatibleSampleModel(w, h);

        return switch (dataType) {
            case UBYTE, BYTE -> Raster.createWritableRaster(sm, new DataBufferByte((byte[][]) data, w * h), null);
            case SHORT -> Raster.createWritableRaster(sm, new DataBufferShort((short[][]) data, w * h), null);
            case USHORT -> Raster.createWritableRaster(sm, new DataBufferUShort((short[][]) data, w * h), null);
            case UINT, INT -> Raster.createWritableRaster(sm, new DataBufferInt((int[][]) data, w * h), null);
            case FLOAT -> Raster.createWritableRaster(sm, new DataBufferFloat((float[][]) data, w * h), null);
            case DOUBLE -> Raster.createWritableRaster(sm, new DataBufferDouble((double[][]) data, w * h), null);
            default -> throw new IllegalArgumentException("IcyColorModel.createWritableRaster(..) error : unsupported data type : " + dataType);
        };
    }

    /**
     * @param w width of the raster
     * @param h height of the raster
     * @return a new dummy (empty data) writable raster from specified data and size.<br>
     */
    public WritableRaster createDummyWritableRaster(final int w, final int h) {
        final SampleModel sm = createCompatibleSampleModel(w, h);

        return switch (dataType) {
            case UBYTE, BYTE -> Raster.createWritableRaster(sm, new DataBufferByte(0, numComponents), null);
            case SHORT -> Raster.createWritableRaster(sm, new DataBufferShort(0, numComponents), null);
            case USHORT -> Raster.createWritableRaster(sm, new DataBufferUShort(0, numComponents), null);
            case UINT, INT -> Raster.createWritableRaster(sm, new DataBufferInt(0, numComponents), null);
            case FLOAT -> Raster.createWritableRaster(sm, new DataBufferFloat(0, numComponents), null);
            case DOUBLE -> Raster.createWritableRaster(sm, new DataBufferDouble(0, numComponents), null);
            default -> throw new IllegalArgumentException("IcyColorModel.createWritableRaster(..) error : unsupported data type : " + dataType);
        };
    }

    /**
     * Set bounds from specified {@link IcyColorModel}
     *
     * @param source source colormodel
     */
    public void setBounds(final IcyColorModel source) {
        beginUpdate();
        try {
            for (int i = 0; i < numComponents; i++) {
                final Scaler srcNormalScaler = source.getNormalScalers()[i];
                final Scaler dstNormalScaler = normalScalers[i];
                final Scaler srcColorMapScaler = source.getColormapScalers()[i];
                final Scaler dstColorMapScaler = colormapScalers[i];

                dstNormalScaler.beginUpdate();
                try {
                    dstNormalScaler.setAbsLeftRightIn(srcNormalScaler.getAbsLeftIn(), srcNormalScaler.getAbsRightIn());
                    dstNormalScaler.setLeftRightIn(srcNormalScaler.getLeftIn(), srcNormalScaler.getRightIn());
                    dstNormalScaler.setLeftRightOut(srcNormalScaler.getLeftOut(), srcNormalScaler.getRightOut());
                }
                finally {
                    dstNormalScaler.endUpdate();
                }

                dstColorMapScaler.beginUpdate();
                try {
                    dstColorMapScaler.setAbsLeftRightIn(srcColorMapScaler.getAbsLeftIn(),
                            srcColorMapScaler.getAbsRightIn());
                    dstColorMapScaler.setLeftRightIn(srcColorMapScaler.getLeftIn(), srcColorMapScaler.getRightIn());
                    dstColorMapScaler.setLeftRightOut(srcColorMapScaler.getLeftOut(), srcColorMapScaler.getRightOut());
                }
                finally {
                    dstColorMapScaler.endUpdate();
                }
            }
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Return the toRGB colormap of specified RGB component
     *
     * @param component component index
     * @return colormap
     */
    public IcyColorMap getColorMap(final int component) {
        return getIcyColorSpace().getColorMap(component);
    }

    /**
     * Set the toRGB colormaps from a compatible colorModel.
     *
     * @param source source ColorModel to copy colormap from
     */
    public void setColorMaps(final ColorModel source) {
        getIcyColorSpace().setColorMaps(source);
    }

    /**
     * Set the toRGB colormap of specified component (actually copy the content).
     *
     * @param component component we want to set the colormap
     * @param map       source colormap to copy
     * @param setAlpha  also set the alpha information
     */
    public void setColorMap(final int component, final IcyColorMap map, final boolean setAlpha) {
        getIcyColorSpace().setColorMap(component, map, setAlpha);
    }

    /**
     * @see ColorModel#getAlpha(int)
     */
    @Override
    public int getAlpha(final int pixel) {
        throw new IllegalArgumentException("Argument type not supported for this color model");
    }

    /**
     * @see ColorModel#getBlue(int)
     */
    @Override
    public int getBlue(final int pixel) {
        throw new IllegalArgumentException("Argument type not supported for this color model");
    }

    /**
     * @see ColorModel#getGreen(int)
     */
    @Override
    public int getGreen(final int pixel) {
        throw new IllegalArgumentException("Argument type not supported for this color model");
    }

    /**
     * @see ColorModel#getRed(int)
     */
    @Override
    public int getRed(final int pixel) {
        throw new IllegalArgumentException("Argument type not supported for this color model");
    }

    @Override
    public abstract int getRGB(Object inData);

    /**
     * @param pixel pixel array
     * @param lut   LUT for color conversion
     * @return same as {@link #getRGB(int)} but using a specific {@link LUT}
     */
    public abstract int getRGB(Object pixel, LUT lut);

    /**
     *
     */
    @Override
    public int getBlue(final Object pixel) {
        return getRGB(pixel) & 0xFF;
    }

    /**
     *
     */
    @Override
    public int getGreen(final Object pixel) {
        return (getRGB(pixel) >> 8) & 0xFF;
    }

    /**
     *
     */
    @Override
    public int getRed(final Object pixel) {
        return (getRGB(pixel) >> 16) & 0xFF;
    }

    /**
     *
     */
    @Override
    public int getAlpha(final Object pixel) {
        return (getRGB(pixel) >> 24) & 0xFF;
    }

    /**
     * @see ColorModel#getComponents(int, int[], int)
     */
    @Override
    public int[] getComponents(final int pixel, final int[] components, final int offset) {
        throw new IllegalArgumentException("Not supported in this ColorModel");
    }

    /**
     * @see ColorModel#getComponents(Object, int[], int)
     */
    @Override
    public abstract int[] getComponents(Object pixel, int[] components, int offset);

    /**
     * @see ColorModel#getNormalizedComponents(Object, float[], int)
     */
    @Override
    public abstract float[] getNormalizedComponents(Object pixel, float[] normComponents, int normOffset);

    /**
     * @see ColorModel#getNormalizedComponents(int[], int, float[], int)
     */
    @Override
    public float[] getNormalizedComponents(final int[] components, final int offset, final float[] normComponents, final int normOffset) {
        if ((components.length - offset) < numComponents)
            throw new IllegalArgumentException("Incorrect number of components.  Expecting " + numComponents);

        final float[] result = Array1DUtil.allocIfNull(normComponents, numComponents + normOffset);

        for (int i = 0; i < numComponents; i++)
            result[normOffset + i] = (float) normalScalers[i].scale(components[offset + i]);

        return result;
    }

    /**
     * @see ColorModel#getUnnormalizedComponents(float[], int, int[], int)
     */
    @Override
    public int[] getUnnormalizedComponents(final float[] normComponents, final int normOffset, final int[] components, final int offset) {
        if ((normComponents.length - normOffset) < numComponents)
            throw new IllegalArgumentException("Incorrect number of components.  Expecting " + numComponents);

        final int[] result = Array1DUtil.allocIfNull(components, numComponents + offset);

        for (int i = 0; i < numComponents; i++)
            result[offset + i] = (int) normalScalers[i].unscale(normComponents[normOffset + i]);

        return result;
    }

    /**
     * @see ColorModel#getDataElement(int[], int)
     */
    @Override
    public int getDataElement(final int[] components, final int offset) {
        throw new IllegalArgumentException("Not supported in this ColorModel");
    }

    /**
     * @see ColorModel#getDataElement(float[], int)
     */
    @Override
    public int getDataElement(final float[] normComponents, final int normOffset) {
        throw new IllegalArgumentException("Not supported in this ColorModel");
    }

    /**
     * @see ColorModel#getDataElements(int[], int, Object)
     */
    @Override
    public abstract Object getDataElements(int[] components, int offset, Object obj);

    /**
     * @see ColorModel#getDataElements(int, Object)
     */
    @Override
    public Object getDataElements(final int rgb, final Object pixel) {
        return getDataElements(getIcyColorSpace().fromRGB(rgb), 0, pixel);
    }

    /**
     * @see ColorModel#getDataElements(float[], int, Object)
     */
    @Override
    public abstract Object getDataElements(float[] normComponents, int normOffset, Object obj);

    /**
     *
     */
    @Override
    public ColorModel coerceData(final WritableRaster raster, final boolean isAlphaPremultiplied) {
        // nothing to do
        return this;
    }

    /**
     * Scale input value for colormap indexing
     */
    protected double colormapScale(final int component, final double value) {
        return colormapScalers[component].scale(value);
    }

    /**
     * Tests if the specified <code>Object</code> is an instance of <code>ColorModel</code> and if
     * it equals this <code>ColorModel</code>.
     *
     * @param obj the <code>Object</code> to test for equality
     * @return <code>true</code> if the specified <code>Object</code> is an instance of <code>ColorModel</code> and
     * equals this <code>ColorModel</code>; <code>false</code> otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof IcyColorModel)
            return isCompatible((IcyColorModel) obj);

        return false;
    }

    /**
     * @param cm color model to test
     * @return if specified colormodel is compatible with the current one
     */
    public boolean isCompatible(final IcyColorModel cm) {
        return (getNumComponents() == cm.getNumComponents()) && (getDataType() == cm.getDataType());
    }

    @Override
    public boolean isCompatibleRaster(final Raster raster) {
        final SampleModel sm = raster.getSampleModel();
        final int[] bits = getComponentSize();

        if (sm instanceof ComponentSampleModel) {
            if (sm.getNumBands() != numComponents)
                return false;

            for (int i = 0; i < bits.length; i++)
                if (sm.getSampleSize(i) < bits[i])
                    return false;

            return (raster.getTransferType() == transferType);
        }

        return false;
    }

    @Override
    public boolean isCompatibleSampleModel(final SampleModel sm) {
        // Must have the same number of components
        if (numComponents != sm.getNumBands())
            return false;
        return sm.getTransferType() == transferType;
    }

    /**
     * @return the IcyColorSpace
     */
    public IcyColorSpace getIcyColorSpace() {
        return (IcyColorSpace) getColorSpace();
    }

    /**
     * @return the normalScalers
     */
    public Scaler[] getNormalScalers() {
        return normalScalers;
    }

    /**
     * @return the colormapScalers
     */
    public Scaler[] getColormapScalers() {
        return colormapScalers;
    }

    /**
     * Returns the number of components in this <code>ColorModel</code>.<br>
     * Note that alpha is embedded so we always have NumColorComponent = NumComponent
     *
     * @return the number of components in this <code>ColorModel</code>
     */
    @Override
    public int getNumComponents() {
        return numComponents;
    }

    /**
     * @return data type for this colormodel
     * @see DataType
     */
    public DataType getDataType() {
        return dataType;
    }

    /**
     * @return default component bounds for this colormodel
     */
    public double[] getDefaultComponentBounds() {
        return dataType.getDefaultBounds();
    }

    /**
     * @param component component index
     * @return component absolute minimum value
     */
    public double getComponentAbsMinValue(final int component) {
        // use the normal scaler
        return normalScalers[component].getAbsLeftIn();
    }

    /**
     * @param component component index
     * @return component absolute maximum value
     */
    public double getComponentAbsMaxValue(final int component) {
        // use the normal scaler
        return normalScalers[component].getAbsRightIn();
    }

    /**
     * @param component component index
     * @return component absolute bounds (min and max values)
     */
    public double[] getComponentAbsBounds(final int component) {
        final double[] result = new double[2];

        result[0] = getComponentAbsMinValue(component);
        result[1] = getComponentAbsMaxValue(component);

        return result;
    }

    /**
     * @param component component index
     * @return component user minimum value
     */
    public double getComponentUserMinValue(final int component) {
        // use the normal scaler
        return normalScalers[component].getLeftIn();
    }

    /**
     * @param component component index
     * @return user component user maximum value
     */
    public double getComponentUserMaxValue(final int component) {
        // use the normal scaler
        return normalScalers[component].getRightIn();
    }

    /**
     * @param component component index
     * @return component user bounds (min and max values)
     */
    public double[] getComponentUserBounds(final int component) {
        final double[] result = new double[2];

        result[0] = getComponentUserMinValue(component);
        result[1] = getComponentUserMaxValue(component);

        return result;
    }

    /**
     * @param component component index
     *                  Set component absolute minimum value
     * @param min       minimum value
     */
    public void setComponentAbsMinValue(final int component, final double min) {
        // update both scalers
        normalScalers[component].setAbsLeftIn(min);
        colormapScalers[component].setAbsLeftIn(min);
    }

    /**
     * @param component component index
     *                  Set component absolute maximum value
     * @param max       maximum value
     */
    public void setComponentAbsMaxValue(final int component, final double max) {
        // update both scalers
        normalScalers[component].setAbsRightIn(max);
        colormapScalers[component].setAbsRightIn(max);
    }

    /**
     * @param component component index
     *                  Set component absolute bounds (min and max values)
     * @param bounds    bounds value (min/max)
     */
    public void setComponentAbsBounds(final int component, final double[] bounds) {
        setComponentAbsBounds(component, bounds[0], bounds[1]);
    }

    /**
     * @param component component index
     *                  Set component absolute bounds (min and max values)
     * @param min       minimum value
     * @param max       maximum value
     */
    public void setComponentAbsBounds(final int component, final double min, final double max) {
        // update both scalers
        normalScalers[component].setAbsLeftRightIn(min, max);
        colormapScalers[component].setAbsLeftRightIn(min, max);
    }

    /**
     * Set component user minimum value
     *
     * @param component component index
     * @param min       minimum value
     */
    public void setComponentUserMinValue(final int component, final double min) {
        // update both scalers
        normalScalers[component].setLeftIn(min);
        colormapScalers[component].setLeftIn(min);
    }

    /**
     * Set component user maximum value
     *
     * @param component component index
     * @param max       maximum value
     */
    public void setComponentUserMaxValue(final int component, final double max) {
        // update both scalers
        normalScalers[component].setRightIn(max);
        colormapScalers[component].setRightIn(max);
    }

    /**
     * Set component user bounds (min and max values)
     *
     * @param component component index
     * @param bounds    bounds value (min/max)
     */
    public void setComponentUserBounds(final int component, final double[] bounds) {
        setComponentUserBounds(component, bounds[0], bounds[1]);
    }

    /**
     * Set component user bounds (min and max values)
     *
     * @param component component index
     * @param min       minimum value
     * @param max       maximum value
     */
    public void setComponentUserBounds(final int component, final double min, final double max) {
        // update both scalers
        normalScalers[component].setLeftRightIn(min, max);
        colormapScalers[component].setLeftRightIn(min, max);
    }

    /**
     * Set components absolute bounds (min and max values)
     *
     * @param bounds bounds value (min/max)
     */
    public void setComponentsAbsBounds(final double[][] bounds) {
        final int numComponents = getNumComponents();

        if (bounds.length != numComponents)
            throw new IllegalArgumentException("bounds.length != ColorModel.numComponents");

        for (int component = 0; component < numComponents; component++)
            setComponentAbsBounds(component, bounds[component]);
    }

    /**
     * Set components user bounds (min and max values)
     *
     * @param bounds bounds value (min/max)
     */
    public void setComponentsUserBounds(final double[][] bounds) {
        final int numComponents = getNumComponents();

        if (bounds.length != numComponents)
            throw new IllegalArgumentException("bounds.length != ColorModel.numComponents");

        for (int component = 0; component < numComponents; component++)
            setComponentUserBounds(component, bounds[component]);
    }

    /**
     * @return true if colorModel is float data type
     */
    public boolean isFloatDataType() {
        return dataType.isFloat();
    }

    /**
     * @return true if colorModel data type is signed
     */
    public boolean isSignedDataType() {
        return dataType.isSigned();
    }

    /**
     * @return true if color maps associated to this {@link IcyColorModel} are all linear map.
     * @see IcyColorMap#isLinear()
     */
    public boolean hasLinearColormaps() {
        for (int c = 0; c < numComponents; c++)
            if (!getColorMap(c).isLinear())
                return false;

        return true;
    }

    /**
     * Returns the <code>String</code> representation of the contents of this <code>ColorModel</code>object.
     *
     * @return a <code>String</code> representing the contents of this <code>ColorModel</code> object.
     */
    @Override
    public String toString() {
        return "ColorModel: dataType = " + dataType + " numComponents = " + numComponents + " color space = " + getColorSpace();
    }

    /**
     * Add a listener
     */
    public void addListener(final IcyColorModelListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener
     */
    public void removeListener(final IcyColorModelListener listener) {
        listeners.remove(listener);
    }

    /**
     * fire event
     */
    public void fireEvent(final IcyColorModelEvent e) {
        for (final IcyColorModelListener listener : new ArrayList<>(listeners))
            listener.colorModelChanged(e);
    }

    /**
     * process on colormodel change
     */
    @Override
    public void onChanged(final CollapsibleEvent compare) {
        final IcyColorModelEvent event = (IcyColorModelEvent) compare;

        // notify listener we have changed
        fireEvent(event);
    }

    @Override
    public void scalerChanged(final ScalerEvent e) {
        // only listening colormapScalers
        final int ind = Scaler.indexOf(colormapScalers, e.getScaler());

        // handle changed via updater object
        if (ind != -1)
            updater.changed(new IcyColorModelEvent(this, IcyColorModelEventType.SCALER_CHANGED, ind));
    }

    @Override
    public void colorSpaceChanged(final IcyColorSpaceEvent e) {
        // handle changed via updater object
        updater.changed(new IcyColorModelEvent(this, IcyColorModelEventType.COLORMAP_CHANGED, e.getComponent()));
    }

    /**
     * start updating object
     */
    public void beginUpdate() {
        updater.beginUpdate();
    }

    /**
     * end updating object
     */
    public void endUpdate() {
        updater.endUpdate();
    }

    /**
     * @return updating state
     */
    public boolean isUpdating() {
        return updater.isUpdating();
    }
}
