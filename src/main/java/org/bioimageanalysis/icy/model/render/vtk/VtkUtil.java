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
package org.bioimageanalysis.icy.model.render.vtk;

import org.bioimageanalysis.icy.common.collection.array.Array2DUtil;
import org.bioimageanalysis.icy.common.collection.array.ArrayUtil;
import org.bioimageanalysis.icy.common.exception.TooLargeArrayException;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle3D;
import org.bioimageanalysis.icy.common.geom.rectangle.Rectangle5D;
import org.bioimageanalysis.icy.common.math.Scaler;
import org.bioimageanalysis.icy.common.type.DataType;
import org.bioimageanalysis.icy.gui.canvas.Layer;
import org.bioimageanalysis.icy.gui.render.IcyVtkPanel;
import org.bioimageanalysis.icy.model.colormap.IcyColorMap;
import org.bioimageanalysis.icy.model.lut.LUT.LUTChannel;
import org.bioimageanalysis.icy.model.overlay.Overlay;
import org.bioimageanalysis.icy.model.overlay.VtkPainter;
import org.bioimageanalysis.icy.model.roi.ROI;
import org.bioimageanalysis.icy.model.roi.mask.BooleanMask2D;
import org.bioimageanalysis.icy.model.roi.mask.BooleanMask3D;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import org.bioimageanalysis.icy.gui.canvas.VtkCanvas;
import vtk.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class VtkUtil {
    // VTK type
    public final static int VTK_VOID = 0;
    public final static int VTK_BIT = 1;
    public final static int VTK_CHAR = 2;
    public final static int VTK_SIGNED_CHAR = 15;
    public final static int VTK_UNSIGNED_CHAR = 3;
    public final static int VTK_SHORT = 4;
    public final static int VTK_UNSIGNED_SHORT = 5;
    public final static int VTK_INT = 6;
    public final static int VTK_UNSIGNED_INT = 7;
    public final static int VTK_LONG = 8;
    public final static int VTK_UNSIGNED_LONG = 9;
    public final static int VTK_FLOAT = 10;
    public final static int VTK_DOUBLE = 11;
    public final static int VTK_ID = 12;

    // VTK interpolation
    public final static int VTK_NEAREST_INTERPOLATION = 0;
    public final static int VTK_LINEAR_INTERPOLATION = 1;
    public final static int VTK_CUBIC_INTERPOLATION = 2;

    // VTK bounding box
    public final static int VTK_FLY_OUTER_EDGES = 0;
    public final static int VTK_FLY_CLOSEST_TRIAD = 1;
    public final static int VTK_FLY_FURTHEST_TRIAD = 2;
    public final static int VTK_FLY_STATIC_TRIAD = 3;
    public final static int VTK_FLY_STATIC_EDGES = 4;

    public final static int VTK_TICKS_INSIDE = 0;
    public final static int VTK_TICKS_OUTSIDE = 1;
    public final static int VTK_TICKS_BOTH = 2;

    public final static int VTK_GRID_LINES_ALL = 0;
    public final static int VTK_GRID_LINES_CLOSEST = 1;
    public final static int VTK_GRID_LINES_FURTHEST = 2;

    /**
     * Returns the VTK type corresponding to the specified DataType
     */
    public static int getVtkType(final DataType type) {
        return switch (type) {
            default -> VTK_UNSIGNED_CHAR;

            // FIXME: signed char not supported by VTK java wrapper ??
            // case BYTE:
            // return VTK_CHAR;
            // return VTK_SIGNED_CHAR;

            case USHORT -> VTK_UNSIGNED_SHORT;
            case SHORT -> VTK_SHORT;
            case UINT -> VTK_UNSIGNED_INT;
            case INT -> VTK_INT;
            case ULONG -> VTK_UNSIGNED_LONG;
            case LONG -> VTK_LONG;
            case FLOAT -> VTK_FLOAT;
            case DOUBLE -> VTK_DOUBLE;
        };
    }

    public static vtkDataArray getVtkArray(final Object array, final boolean signed) {
        return switch (ArrayUtil.getDataType(array)) {
            case BYTE -> getUCharArray((byte[]) array);
            case SHORT -> {
                if (signed)
                    yield getUShortArray((short[]) array);
                yield getShortArray((short[]) array);
            }
            case INT -> {
                if (signed)
                    yield getUIntArray((int[]) array);
                yield getIntArray((int[]) array);
            }
            case LONG -> {
                if (signed)
                    yield getULongArray((long[]) array);
                yield getLongArray((long[]) array);
            }
            case FLOAT -> getFloatArray((float[]) array);
            case DOUBLE -> getDoubleArray((double[]) array);
            default -> null;
        };
    }

    public static vtkUnsignedCharArray getUCharArray(final byte[] array) {
        final vtkUnsignedCharArray result = new vtkUnsignedCharArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkUnsignedShortArray getUShortArray(final short[] array) {
        final vtkUnsignedShortArray result = new vtkUnsignedShortArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkUnsignedIntArray getUIntArray(final int[] array) {
        final vtkUnsignedIntArray result = new vtkUnsignedIntArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkUnsignedLongArray getULongArray(final long[] array) {
        final vtkUnsignedLongArray result = new vtkUnsignedLongArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkShortArray getShortArray(final short[] array) {
        final vtkShortArray result = new vtkShortArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkIntArray getIntArray(final int[] array) {
        final vtkIntArray result = new vtkIntArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkLongArray getLongArray(final long[] array) {
        final vtkLongArray result = new vtkLongArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkFloatArray getFloatArray(final float[] array) {
        final vtkFloatArray result = new vtkFloatArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkDoubleArray getDoubleArray(final double[] array) {
        final vtkDoubleArray result = new vtkDoubleArray();

        result.SetJavaArray(array);

        return result;
    }

    public static vtkIdTypeArray getIdTypeArray(final int[] array) {
        final vtkIdTypeArray result = new vtkIdTypeArray();
        final vtkIntArray iarray = getIntArray(array);

        result.DeepCopy(iarray);
        iarray.Delete();

        return result;
    }

    /**
     * Returns a native java array from the specified {@link vtkDataArray}.
     */
    public static Object getJavaArray(final vtkDataArray dataArray) {
        return switch (dataArray.GetDataType()) {
            case VTK_UNSIGNED_CHAR -> ((vtkUnsignedCharArray) dataArray).GetJavaArray();
            case VTK_SHORT -> ((vtkShortArray) dataArray).GetJavaArray();
            case VTK_UNSIGNED_SHORT -> ((vtkUnsignedShortArray) dataArray).GetJavaArray();
            case VTK_INT -> ((vtkIntArray) dataArray).GetJavaArray();
            case VTK_UNSIGNED_INT -> ((vtkUnsignedIntArray) dataArray).GetJavaArray();
            case VTK_LONG -> ((vtkLongArray) dataArray).GetJavaArray();
            case VTK_UNSIGNED_LONG -> ((vtkUnsignedLongArray) dataArray).GetJavaArray();
            case VTK_FLOAT -> ((vtkFloatArray) dataArray).GetJavaArray();
            case VTK_DOUBLE -> ((vtkDoubleArray) dataArray).GetJavaArray();
            default -> null;
        };
    }

    public static int[] getJavaArray(final vtkIdTypeArray array) {
        final vtkIntArray iarray = new vtkIntArray();

        iarray.DeepCopy(array);

        final int[] result = iarray.GetJavaArray();
        iarray.Delete();

        return result;
    }

    /**
     * Transforms a vtkCollection to an array
     */
    public static vtkObject[] vtkCollectionToArray(final vtkCollection collection) {
        final vtkObject[] result = new vtkObject[collection.GetNumberOfItems()];

        collection.InitTraversal();
        for (int i = 0; i < result.length; i++)
            result[i] = collection.GetNextItemAsObject();

        return result;
    }

    /**
     * Get vtkPoints from double[]
     */
    public static vtkPoints getPoints(final double[] points) {
        final vtkPoints result = new vtkPoints();
        final vtkDoubleArray array = getDoubleArray(points);

        array.SetNumberOfComponents(3);
        result.SetData(array);

        return result;
    }

    /**
     * Get vtkPoints from double[][3]
     */
    public static vtkPoints getPoints(final double[][] points) {
        return getPoints(Array2DUtil.toDoubleArray1D(points));
    }

    /**
     * Get vtkPoints from float[]
     */
    public static vtkPoints getPoints(final float[] points) {
        final vtkPoints result = new vtkPoints();
        final vtkFloatArray array = getFloatArray(points);

        array.SetNumberOfComponents(3);
        result.SetData(array);

        return result;
    }

    /**
     * Get vtkPoints from float[][3]
     */
    public static vtkPoints getPoints(final float[][] points) {
        return getPoints(Array2DUtil.toFloatArray1D(points));
    }

    /**
     * Get vtkPoints from int[]
     */
    public static vtkPoints getPoints(final int[] points) {
        final vtkPoints result = new vtkPoints();
        final vtkIntArray array = getIntArray(points);

        array.SetNumberOfComponents(3);
        result.SetData(array);

        return result;
    }

    /**
     * Get vtkPoints from int[][3]
     */
    public static vtkPoints getPoints(final int[][] points) {
        return getPoints(Array2DUtil.toIntArray1D(points));
    }

    /**
     * Get vtkCellArray from a 1D prepared cells array ( {n, i1, i2, ..., n, i1, i2,...} )
     */
    public static vtkCellArray getCells(final int numCell, final int[] cells) {
        final vtkCellArray result = new vtkCellArray();

        result.SetCells(numCell, getIdTypeArray(cells));

        return result;
    }

    /**
     * Returns a simple cell {@link vtkPolyData} from {@link vtkPoints}
     */
    public static vtkPolyData getPolyDataFromPoints(final vtkPoints points) {
        final vtkPolyData tmpPolyData = new vtkPolyData();
        final vtkVertexGlyphFilter vertexFilter = new vtkVertexGlyphFilter();

        tmpPolyData.SetPoints(points);
        vertexFilter.SetInputData(tmpPolyData);
        vertexFilter.Update();

        final vtkPolyData result = vertexFilter.GetOutput();

        vertexFilter.Delete();

        return result;
    }

    /**
     * Returns a {@link vtkPolyData} from {@link vtkDataSet}
     */
    public static vtkPolyData getPolyDataFromDataSet(final vtkDataSet dataSet) {
        // transform grid to polydata first
        final vtkDataSetSurfaceFilter surfaceFilter = new vtkDataSetSurfaceFilter();
        surfaceFilter.SetInputData(dataSet);
        surfaceFilter.Update();

        final vtkPolyData result = surfaceFilter.GetOutput();

        surfaceFilter.Delete();

        return result;
    }

    /**
     * Returns the <i>vtkProp</i> from the specified <i>Layer</i> object.<br>
     * Returns a 0 sized array if the specified layer is <code>null</code> or does not contains any vtkProp.
     */
    public static vtkProp[] getLayerProps(final Layer layer) {
        if (layer != null) {
            // add painter actor from the vtk render
            final Overlay overlay = layer.getOverlay();

            if (overlay instanceof VtkPainter)
                return ((VtkPainter) overlay).getProps();
        }

        return new vtkProp[0];
    }

    /**
     * Returns all <i>vtkProp</i> from the specified list of <i>Layer</i> object.<br>
     * Returns a 0 sized array if specified layers does not contains any vtkProp.
     */
    public static vtkProp[] getLayersProps(final List<Layer> layers) {
        final List<vtkProp[]> layersProps = new ArrayList<>();
        int totalSize = 0;

        for (final Layer layer : layers) {
            if (layer != null) {
                // add painter actor from the vtk render
                final Overlay overlay = layer.getOverlay();

                if (overlay instanceof VtkPainter) {
                    final vtkProp[] props = ((VtkPainter) overlay).getProps();

                    if (props.length > 0) {
                        layersProps.add(props);
                        totalSize += props.length;
                    }
                }
            }
        }

        final vtkProp[] result = new vtkProp[totalSize];
        int ind = 0;

        for (final vtkProp[] props : layersProps) {
            final int size = props.length;

            System.arraycopy(props, 0, result, ind, size);
            ind += size;
        }

        return result;
    }

    /**
     * Return all actor / view prop from the specified renderer
     */
    public static vtkProp[] getProps(final vtkRenderer renderer) {
        if (renderer == null)
            return new vtkProp[0];

        final vtkPropCollection collection = renderer.GetViewProps();
        final vtkProp[] result = new vtkProp[collection.GetNumberOfItems()];

        collection.InitTraversal();
        for (int i = 0; i < result.length; i++)
            result[i] = collection.GetNextProp();

        return result;
    }

    /**
     * Return true if the renderer contains the specified actor / view prop
     */
    public static boolean hasProp(final vtkRenderer renderer, final vtkProp actor) {
        if ((renderer == null) || (actor == null))
            return false;

        return renderer.HasViewProp(actor) != 0;
    }

    /**
     * Add an actor (vtkProp) to the specified renderer.<br>
     * If the actor is already existing in the renderer then no operation is done.
     */
    public static void addProp(final vtkRenderer renderer, final vtkProp prop) {
        if ((renderer == null) || (prop == null))
            return;

        // actor not yet present in renderer ? --> add it
        if (renderer.HasViewProp(prop) == 0)
            renderer.AddViewProp(prop);
    }

    /**
     * Add an array of actor (vtkProp) to the specified renderer.<br>
     * If an actor is already existing in the renderer then nothing is done for this actor.
     */
    public static void addProps(final vtkRenderer renderer, final vtkProp[] props) {
        if ((renderer == null) || (props == null))
            return;

        for (final vtkProp prop : props) {
            // actor not yet present in renderer ? --> add it
            if (renderer.HasViewProp(prop) == 0)
                renderer.AddViewProp(prop);
        }
    }

    /**
     * Remove an actor from the specified renderer.
     */
    public static void removeProp(final vtkRenderer renderer, final vtkProp actor) {
        renderer.RemoveViewProp(actor);
    }

    /**
     * Return a 1D cells array from a 2D indexes array
     */
    public static int[] prepareCells(final int[][] indexes) {
        final int len = indexes.length;

        int total_len = 0;
        for (final int[] index : indexes)
            total_len += index.length + 1;

        final int[] result = new int[total_len];

        int offset = 0;
        for (final int[] s_cells : indexes) {
            final int s_len = s_cells.length;

            result[offset++] = s_len;
            for (final int sCell : s_cells)
                result[offset++] = sCell;
        }

        return result;
    }

    /**
     * Return a 1D cells array from a 1D indexes array and num vertex per cell (polygon)
     */
    public static int[] prepareCells(final int numVertexPerCell, final int[] indexes) {
        final int num_cells = indexes.length / numVertexPerCell;
        final int[] result = new int[num_cells * (numVertexPerCell + 1)];

        int off_dst = 0;
        int off_src = 0;
        for (int i = 0; i < num_cells; i++) {
            result[off_dst++] = numVertexPerCell;

            for (int j = 0; j < numVertexPerCell; j++)
                result[off_dst++] = indexes[off_src + j];

            off_src += numVertexPerCell;
        }

        return result;
    }

    /**
     * Returns a {@link BooleanMask3D} from a binary (0/1 values) {@link vtkImageData}.
     */
    public static BooleanMask3D getBooleanMaskFromBinaryImage(final vtkImageData image, final boolean optimizeBounds) {
        final vtkDataArray data = image.GetPointData().GetScalars();
        final double[] origin = image.GetOrigin();
        final int[] dim = image.GetDimensions();
        final int sizeX = dim[0];
        final int sizeY = dim[1];
        final int sizeZ = dim[2];
        final int sizeXY = sizeX * sizeY;
        final Rectangle bounds2D = new Rectangle((int) origin[0], (int) origin[1], sizeX, sizeY);
        final BooleanMask2D[] masks = new BooleanMask2D[sizeZ];

        // more than 200M ? use simple iterator (slower but consume less memory)
        if ((sizeXY * sizeZ) > (200 * 1024 * 1024)) {
            int off = 0;
            for (int z = 0; z < sizeZ; z++) {
                final boolean[] mask = new boolean[sizeXY];

                for (int xy = 0; xy < sizeXY; xy++)
                    mask[xy] = (data.GetTuple1(off++) != 0d);

                masks[z] = new BooleanMask2D(new Rectangle(bounds2D), mask);
            }
        }
        else {
            final Object javaArray = getJavaArray(data);
            int off = 0;

            switch (ArrayUtil.getDataType(javaArray)) {
                case BYTE:
                    final byte[] javaByteArray = (byte[]) javaArray;

                    for (int z = 0; z < sizeZ; z++) {
                        final boolean[] mask = new boolean[sizeXY];

                        for (int xy = 0; xy < mask.length; xy++)
                            mask[xy] = (javaByteArray[off++] != 0);

                        masks[z] = new BooleanMask2D(new Rectangle(bounds2D), mask);
                    }
                    break;

                case SHORT:
                    final short[] javaShortArray = (short[]) javaArray;

                    for (int z = 0; z < sizeZ; z++) {
                        final boolean[] mask = new boolean[sizeXY];

                        for (int xy = 0; xy < mask.length; xy++)
                            mask[xy] = (javaShortArray[off++] != 0);

                        masks[z] = new BooleanMask2D(new Rectangle(bounds2D), mask);
                    }
                    break;

                case INT:
                    final int[] javaIntArray = (int[]) javaArray;

                    for (int z = 0; z < sizeZ; z++) {
                        final boolean[] mask = new boolean[sizeXY];

                        for (int xy = 0; xy < mask.length; xy++)
                            mask[xy] = (javaIntArray[off++] != 0);

                        masks[z] = new BooleanMask2D(new Rectangle(bounds2D), mask);
                    }
                    break;

                case LONG:
                    final long[] javaLongArray = (long[]) javaArray;

                    for (int z = 0; z < sizeZ; z++) {
                        final boolean[] mask = new boolean[sizeXY];

                        for (int xy = 0; xy < mask.length; xy++)
                            mask[xy] = (javaLongArray[off++] != 0L);

                        masks[z] = new BooleanMask2D(new Rectangle(bounds2D), mask);
                    }
                    break;

                case FLOAT:
                    final float[] javaFloatArray = (float[]) javaArray;

                    for (int z = 0; z < sizeZ; z++) {
                        final boolean[] mask = new boolean[sizeXY];

                        for (int xy = 0; xy < mask.length; xy++)
                            mask[xy] = (javaFloatArray[off++] != 0f);

                        masks[z] = new BooleanMask2D(new Rectangle(bounds2D), mask);
                    }
                    break;

                case DOUBLE:
                    final double[] javaDoubleArray = (double[]) javaArray;

                    for (int z = 0; z < sizeZ; z++) {
                        final boolean[] mask = new boolean[sizeXY];

                        for (int xy = 0; xy < mask.length; xy++)
                            mask[xy] = (javaDoubleArray[off++] != 0d);

                        masks[z] = new BooleanMask2D(new Rectangle(bounds2D), mask);
                    }
                    break;

                default:
                    // nothing to do here
                    break;
            }
        }

        final BooleanMask3D result = new BooleanMask3D(new Rectangle3D.Integer(bounds2D.x, bounds2D.y, (int) origin[2], sizeX, sizeY, sizeZ), masks);

        if (optimizeBounds)
            result.optimizeBounds();

        return result;
    }

    /**
     * Build and return volume image data from given Sequence object.
     *
     * @param sequence
     *        the sequence object we want to get volume image data
     * @param posT
     *        frame index
     * @param posC
     *        channel index (-1 for all channel)
     */
    public static vtkImageData getImageData(final Sequence sequence, final int posT, final int posC) throws TooLargeArrayException, OutOfMemoryError {
        if ((sequence == null) || sequence.isEmpty())
            return null;

        final Object data;
        final vtkImageData result;

        if (posC == -1) {
            data = sequence.getDataCopyCXYZ(posT);
            result = getImageData(data, sequence.getDataType(), sequence.getSizeX(), sequence.getSizeY(),
                    sequence.getSizeZ(), sequence.getSizeC());
        }
        else {
            data = sequence.getDataCopyXYZ(posT, posC);
            result = getImageData(data, sequence.getDataType(), sequence.getSizeX(), sequence.getSizeY(),
                    sequence.getSizeZ(), 1);
        }

        return result;
    }

    /**
     * Creates and returns a {@link vtkImageData} object from the specified 1D array data.
     */
    public static vtkImageData getImageData(final Object data, final DataType dataType, final int sizeX, final int sizeY, final int sizeZ, final int sizeC) {
        final vtkImageData result;
        final vtkDataArray array;

        // create a new image data structure
        result = new vtkImageData();
        result.SetDimensions(sizeX, sizeY, sizeZ);
        result.SetExtent(0, sizeX - 1, 0, sizeY - 1, 0, sizeZ - 1);
        // pre-allocate data
        result.AllocateScalars(getVtkType(dataType), sizeC);
        // get array structure
        array = result.GetPointData().GetScalars();

        switch (dataType) {
            case UBYTE:
            case BYTE:
                ((vtkUnsignedCharArray) array).SetJavaArray((byte[]) data);
                break;
            case USHORT:
                ((vtkUnsignedShortArray) array).SetJavaArray((short[]) data);
                break;
            case SHORT:
                ((vtkShortArray) array).SetJavaArray((short[]) data);
                break;
            case UINT:
                ((vtkUnsignedIntArray) array).SetJavaArray((int[]) data);
                break;
            case INT:
                ((vtkIntArray) array).SetJavaArray((int[]) data);
                break;
            case FLOAT:
                ((vtkFloatArray) array).SetJavaArray((float[]) data);
                break;
            case DOUBLE:
                ((vtkDoubleArray) array).SetJavaArray((double[]) data);
                break;
            default:
                break;
        }

        return result;
    }

    /**
     * Create a 3D surface in VTK polygon format from the input VTK image data.
     *
     * @param imageData
     *        the input image to construct surface from
     * @param threshold
     *        the threshold intensity value used to build the surface
     */
    public static vtkPolyData getSurfaceFromImage(final vtkImageData imageData, final double threshold) {
        // TODO: try vtkImageDataGeometryFilter

        final int[] extent = imageData.GetExtent();
        extent[0]--; // min X
        extent[1]++; // max X
        extent[2]--; // min Y
        extent[3]++; // max Y
        extent[4]--; // min Z
        extent[5]++; // max Z

        // pad on all sides to guarantee closed meshes
        final vtkImageConstantPad pad = new vtkImageConstantPad();

        pad.SetOutputWholeExtent(extent);
        pad.SetInputData(imageData);
        pad.Update();

        final vtkImageData out = pad.GetOutput();
        out.SetOrigin(imageData.GetOrigin());
        // do not delete input image
        pad.Delete();

        final vtkContourFilter contourFilter = new vtkContourFilter();
        contourFilter.SetInputData(out);
        contourFilter.SetValue(0, threshold);
        contourFilter.Update();

        final vtkPolyData result = contourFilter.GetOutput();
        contourFilter.GetInput().Delete();
        contourFilter.Delete();

        return result;
    }

    /**
     * Creates and returns a 3D binary (0/1 values) {@link vtkImageData} object corresponding to the ROI 3D boolean mask
     * (C dimension is not considered) at specified T position.
     *
     * @param roi
     *        the roi we want to retrieve the vtkImageData mask
     * @param sz
     *        the Z size to use for ROI with infinite Z dimension (if ROI has a finite Z dimension then ROI Z size is
     *        used).
     * @param t
     *        the T position we want to retrieve the 3D mask data
     */
    public static vtkImageData getBinaryImageData(final ROI roi, final int sz, final int t)
            throws IllegalArgumentException, InterruptedException {
        final vtkImageData result;

        final Rectangle5D bounds5d = roi.getBounds5D();
        final int sizeX;
        final int sizeY;
        final int sizeZ;
        final int x;
        final int y;
        final int z;
        final int c;

        x = (int) bounds5d.getX();
        y = (int) bounds5d.getY();
        sizeX = (int) (bounds5d.getMaxX() - x);
        sizeY = (int) (bounds5d.getMaxY() - y);
        if (bounds5d.isInfiniteZ()) {
            z = 0;
            sizeZ = sz;
        }
        else {
            z = (int) bounds5d.getZ();
            sizeZ = (int) (bounds5d.getMaxZ() - z);
        }
        if (bounds5d.isInfiniteC())
            c = 0;
        else
            c = (int) bounds5d.getC();

        long totalSize = sizeX;
        totalSize *= sizeY;
        totalSize *= sizeZ;

        if (totalSize > Integer.MAX_VALUE)
            throw new IllegalArgumentException("VtkUtil.getBinaryImageData(ROI, ..): Input ROI is too large, can't allocate array (size > 2^31)");

        // build java array
        final int sizeXY = sizeX * sizeY;
        final byte[] array = new byte[(int) totalSize];
        int offset = 0;

        if (bounds5d.isInfiniteZ()) {
            final boolean[] mask = roi.getBooleanMask2D(x, y, sizeX, sizeY, 0, t, c, true);

            for (int curZ = z; curZ < (z + sizeZ); curZ++) {
                for (int i = 0; i < sizeXY; i++)
                    array[offset++] = mask[i] ? (byte) 1 : (byte) 0;
            }
        }
        else {
            for (int curZ = z; curZ < (z + sizeZ); curZ++) {
                final boolean[] mask = roi.getBooleanMask2D(x, y, sizeX, sizeY, curZ, t, c, true);

                for (int i = 0; i < sizeXY; i++)
                    array[offset++] = mask[i] ? (byte) 1 : (byte) 0;
            }
        }

        // create a new image data structure
        result = new vtkImageData();
        result.SetDimensions(sizeX, sizeY, sizeZ);
        result.SetExtent(0, sizeX - 1, 0, sizeY - 1, 0, sizeZ - 1);
        // pre-allocate data
        result.AllocateScalars(VTK_UNSIGNED_CHAR, 1);
        // set data
        ((vtkUnsignedCharArray) result.GetPointData().GetScalars()).SetJavaArray(array);

        return result;
    }

    /**
     * Transforms a {@link vtkPolyData} object to binary (0/1 values) {@link vtkImageData}
     *
     * @param space
     *        spacing between each image point
     */
    public static vtkImageData getBinaryImageData(final vtkPolyData polyData, final double[] space) {
        final vtkImageData whiteImage = new vtkImageData();

        // get poly data bounds
        final double[] bounds = polyData.GetBounds();
        // define spacing
        final double[] spacing = (space == null) ? new double[]{1d, 1d, 1d} : space;
        // define dimensions & origin
        final int[] dim = new int[3];
        final double[] origin = new double[3];

        // compute dimensions
        for (int i = 0; i < dim.length; i++)
            dim[i] = (int) Math.ceil((bounds[(i * 2) + 1] - bounds[(i * 2)/* + 0*/]) / spacing[i]);

        long size = dim[0];
        size *= dim[1];
        size *= dim[2];

        // negative value --> empty poly data
        if (size < 0)
            throw new IllegalArgumentException("VtkUtil.getBinaryImageData(vtkPolyData, ..): Negative object size, cannot do the conversion !");
        // can't allocate more than Integer.MAX_VALUE
        if (size > Integer.MAX_VALUE)
            throw new IllegalArgumentException("VtkUtil.getBinaryImageData(vtkPolyData, ..): Object size is too large, can't allocate array (size > 2^31) !");

        // compute origin (important to add 0.5d to avoid glitches when object edge fall on pixel limit)
        origin[0] = bounds[0] + 0.5d;
        origin[1] = bounds[2] + 0.5d;
        origin[2] = bounds[4] + 0.5d;

        whiteImage.SetSpacing(spacing);
        whiteImage.SetDimensions(dim);
        whiteImage.SetExtent(0, dim[0] - 1, 0, dim[1] - 1, 0, dim[2] - 1);
        whiteImage.SetOrigin(origin);

        // allocate data
        whiteImage.AllocateScalars(VtkUtil.VTK_UNSIGNED_CHAR, 1);

        // fill the image with foreground voxels
        final long len = whiteImage.GetNumberOfPoints();
        // allocate java array
        final byte[] javaArray = new byte[(int) len];
        // get VTK array
        final vtkUnsignedCharArray vtkArray = (vtkUnsignedCharArray) whiteImage.GetPointData().GetScalars();

        // build the java array 1 filled
        Arrays.fill(javaArray, (byte) 1);
        // set to VTK array
        vtkArray.SetJavaArray(javaArray);

        // polygonal data --> image stencil
        final vtkPolyDataToImageStencil polyToImgStencil = new vtkPolyDataToImageStencil();

        polyToImgStencil.SetInputData(polyData);
        polyToImgStencil.SetOutputOrigin(origin);
        polyToImgStencil.SetOutputSpacing(spacing);
        polyToImgStencil.SetOutputWholeExtent(whiteImage.GetExtent());
        // better to set tolerance to 0 (fastest and most permissive miss) for now
        // as more aggressive tolerance (up to 1) can add random points (known issue from VTK 6.3, maybe fixed in VTK 7.0 or >)
        polyToImgStencil.SetTolerance(0.0001);
        polyToImgStencil.Update();

        // cut the corresponding white image and set the background:
        final vtkImageStencil imageStencil = new vtkImageStencil();
        imageStencil.SetInputData(whiteImage);
        imageStencil.SetStencilConnection(polyToImgStencil.GetOutputPort());
        imageStencil.ReverseStencilOff();
        imageStencil.SetBackgroundValue(0d);
        imageStencil.Update();

        final vtkImageData result = imageStencil.GetOutput();

        // release VTK objects
        imageStencil.Delete();
        polyToImgStencil.Delete();
        whiteImage.GetPointData().GetScalars().Delete();
        whiteImage.GetPointData().Delete();
        whiteImage.Delete();

        return result;
    }

    /**
     * Get VTK transform object from specified transform infos
     */
    public static vtkTransform getTransform(final double[] off, final double[] scale, final double[] rot) {
        final double[] offset = (off == null) ? new double[]{0d, 0d, 0d} : off;
        final double[] scaling = (scale == null) ? new double[]{1d, 1d, 1d} : scale;
        final double[] rotation = (rot == null) ? new double[]{0d, 0d, 0d} : rot;

        final vtkTransform result = new vtkTransform();

        result.Translate(offset);
        result.Scale(scaling);
        result.RotateX(rotation[0]);
        result.RotateY(rotation[1]);
        result.RotateZ(rotation[2]);
        result.Update();

        return result;
    }

    /**
     * Transform a polyData using specified rotation, scaling and offset informations
     */
    public static vtkPolyData transformPolyData(final vtkPolyData polyData, final vtkAbstractTransform transform) {
        final vtkTransformPolyDataFilter transformFilter = new vtkTransformPolyDataFilter();
        transformFilter.SetInputData(polyData);
        transformFilter.SetTransform(transform);
        transformFilter.Update();

        final vtkPolyData result = transformFilter.GetOutput();

        transformFilter.Delete();

        return result;
    }

    /**
     * Transform a polyData using specified rotation, scaling and offset informations (3D)
     */
    public static vtkPolyData transformPolyData(final vtkPolyData polyData, final double[] off, final double[] scale, final double[] rot) {
        final vtkTransform transform = getTransform(off, scale, rot);
        final vtkPolyData result = transformPolyData(polyData, transform);
        transform.Delete();
        return result;
    }

    /**
     * Read a OBJ 3D model file and returns it in VTK mesh format ({@link vtkPolyData})
     */
    public static vtkPolyData getSurfaceFromOBJ(final String objPath) {
        final vtkOBJReader reader = new vtkOBJReader();

        reader.SetFileName(objPath);
        reader.Update();

        final vtkPolyData result = reader.GetOutput();

        reader.Delete();

        return result;
    }

    /**
     * Creates and returns the color map in {@link vtkColorTransferFunction} format from the
     * specified {@link LUTChannel}.
     */
    public static vtkColorTransferFunction getColorMap(final LUTChannel lutChannel) {
        final IcyColorMap colorMap = lutChannel.getColorMap();
        final Scaler scaler = lutChannel.getScaler();

        // SCALAR COLOR FUNCTION
        final vtkColorTransferFunction result = new vtkColorTransferFunction();

        result.SetRange(scaler.getLeftIn(), scaler.getRightIn());
        for (int i = 0; i < IcyColorMap.SIZE; i++) {
            result.AddRGBPoint(
                    scaler.unscale(i),
                    colorMap.getNormalizedRed(i),
                    colorMap.getNormalizedGreen(i),
                    colorMap.getNormalizedBlue(i)
            );
        }

        return result;
    }

    /**
     * Creates and returns the opacity map in {@link vtkPiecewiseFunction} format from the specified {@link LUTChannel}.
     */
    public static vtkPiecewiseFunction getOpacityMap(final LUTChannel lutChannel) {
        final IcyColorMap colorMap = lutChannel.getColorMap();
        final Scaler scaler = lutChannel.getScaler();

        // SCALAR OPACITY FUNCTION
        final vtkPiecewiseFunction result = new vtkPiecewiseFunction();

        if (colorMap.isEnabled()) {
            for (int i = 0; i < IcyColorMap.SIZE; i++)
                result.AddPoint(scaler.unscale(i), colorMap.getNormalizedAlpha(i));
        }
        else {
            for (int i = 0; i < IcyColorMap.SIZE; i++)
                result.AddPoint(scaler.unscale(i), 0d);
        }

        return result;
    }

    /**
     * Creates and returns a binary color map in {@link vtkColorTransferFunction} format where 0 value is black and 1 is
     * set to specified color.
     */
    public static vtkColorTransferFunction getBinaryColorMap(final Color color) {
        // SCALAR COLOR FUNCTION
        final vtkColorTransferFunction result = new vtkColorTransferFunction();

        result.SetRange(0, 1);
        result.AddRGBPoint(0d, 0d, 0d, 0d);
        result.AddRGBPoint(1d, color.getRed() / 255d, color.getGreen() / 255d, color.getBlue() / 255d);

        return result;
    }

    /**
     * Creates and returns a binary opacity map in {@link vtkPiecewiseFunction} format where 0 is 100% transparent and 1
     * to the specified opacity value.
     */
    public static vtkPiecewiseFunction getBinaryOpacityMap(final double opacity) {
        // SCALAR OPACITY FUNCTION
        final vtkPiecewiseFunction result = new vtkPiecewiseFunction();

        result.AddPoint(0d, 1d);
        result.AddPoint(1d, opacity);

        return result;
    }

    /**
     * Set the Color of the specified {@link vtkPolyData} object.
     *
     * @param polyData
     *        the vtkPolyData we want to change color
     * @param color
     *        the color to set
     * @param canvas
     *        the VtkCanvas object to lock during the color change operation for safety (can be <code>null</code> if we
     *        don't need to lock the VtkCanvas here)
     */
    public static void setPolyDataColor(final vtkPolyData polyData, final Color color, final VtkCanvas canvas) {
        final long numPts = polyData.GetNumberOfPoints();
        vtkUnsignedCharArray colors = null;

        // try to recover colors object
        if (polyData.GetPointData() != null) {
            final vtkDataArray dataArray = polyData.GetPointData().GetScalars();

            if (dataArray instanceof vtkUnsignedCharArray)
                colors = (vtkUnsignedCharArray) dataArray;
                // delete it
            else if (dataArray != null)
                dataArray.Delete();
        }

        // colors is not correctly defined ? --> reallocate
        if ((colors == null) || (colors.GetNumberOfTuples() != numPts) || (colors.GetNumberOfComponents() != 3)) {
            // delete first
            if (colors != null)
                colors.Delete();

            // and reallocate
            colors = new vtkUnsignedCharArray();
            colors.SetNumberOfComponents(3);
            colors.SetNumberOfTuples(numPts);
            // set colors array
            polyData.GetPointData().SetScalars(colors);
        }

        final int len = (int) (numPts * 3);

        final byte r = (byte) color.getRed();
        final byte g = (byte) color.getGreen();
        final byte b = (byte) color.getBlue();

        final byte[] data = new byte[(int) len];

        for (int i = 0; i < len; i += 3) {
            data[i/* + 0*/] = r;
            data[i + 1] = g;
            data[i + 2] = b;
        }

        final IcyVtkPanel vtkPanel = (canvas != null) ? canvas.getVtkPanel() : null;

        if (vtkPanel != null) {
            vtkPanel.lock();
            try {
                colors.SetJavaArray(data);
                colors.Modified();
            }
            finally {
                vtkPanel.unlock();
            }
        }
        else {
            colors.SetJavaArray(data);
            colors.Modified();
        }
    }

    /**
     * Returns a cube polydata object representing the specified bounding box coordinate
     *
     * @see #setOutlineBounds(vtkPolyData, double, double, double, double, double, double, VtkCanvas)
     */
    public static vtkPolyData getOutline(final double xMin, final double xMax, final double yMin, final double yMax, final double zMin, final double zMax) {
        final double[][] points = new double[8][3];
        final int[][] indexes = {{0, 2, 3, 1}, {4, 5, 7, 6}, {0, 1, 5, 4}, {1, 3, 7, 5}, {0, 4, 6, 2}, {3, 2, 6, 7}};

        for (int i = 0; i < 8; i++) {
            points[i][0] = ((i & 1) == 0) ? xMin : xMax;
            points[i][1] = ((i & 2) == 0) ? yMin : yMax;
            points[i][2] = ((i & 4) == 0) ? zMin : zMax;
        }

        final vtkCellArray vCells = VtkUtil.getCells(6, prepareCells(indexes));
        final vtkPoints vPoints = VtkUtil.getPoints(points);
        final vtkPolyData result = new vtkPolyData();

        result.SetPolys(vCells);
        result.SetPoints(vPoints);

        return result;
    }

    /**
     * Set the bounds of specified outline polydata object (previously created with <i>VtkUtil.getOutline(..)</i>)
     *
     * @param canvas
     *        the VtkCanvas object to lock during the color change operation for safety (can be <code>null</code> if we
     *        don't need to lock the VtkCanvas here)
     * @return <code>false</code> if the specified polydata object is not a valid outline object
     * @see #getOutline(double, double, double, double, double, double)
     */
    public static boolean setOutlineBounds(final vtkPolyData outline, final double xMin, final double xMax, final double yMin, final double yMax, final double zMin, final double zMax, final VtkCanvas canvas) {
        final vtkPoints previousPoints = outline.GetPoints();

        // not valid
        if ((previousPoints != null) && (previousPoints.GetNumberOfPoints() != 8))
            return false;

        final double[][] newPoints = new double[8][3];
        for (int i = 0; i < 8; i++) {
            newPoints[i][0] = ((i & 1) == 0) ? xMin : xMax;
            newPoints[i][1] = ((i & 2) == 0) ? yMin : yMax;
            newPoints[i][2] = ((i & 4) == 0) ? zMin : zMax;
        }

        final vtkPoints points = getPoints(newPoints);
        final IcyVtkPanel vtkPanel = (canvas != null) ? canvas.getVtkPanel() : null;

        if (vtkPanel != null) {
            vtkPanel.lock();
            try {
                // rebuild points
                outline.SetPoints(points);
                // changed
                outline.Modified();
                // delete previous points
                if (previousPoints != null)
                    previousPoints.Delete();
            }
            finally {
                vtkPanel.unlock();
            }
        }
        else {
            // rebuild points
            outline.SetPoints(points);
            // changed
            outline.Modified();
            // delete previous points
            if (previousPoints != null)
                previousPoints.Delete();
        }

        return true;
    }
}
