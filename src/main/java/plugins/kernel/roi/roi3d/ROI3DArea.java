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

package plugins.kernel.roi.roi3d;

import icy.canvas.IcyCanvas;
import icy.common.CollapsibleEvent;
import icy.gui.inspector.RoisPanel;
import icy.main.Icy;
import icy.roi.*;
import icy.sequence.Sequence;
import icy.system.logging.IcyLogger;
import icy.system.thread.ThreadUtil;
import icy.type.point.Point3D;
import icy.type.point.Point5D;
import icy.type.rectangle.Rectangle3D;
import icy.util.StringUtil;
import icy.vtk.IcyVtkPanel;
import icy.vtk.VtkUtil;
import plugins.kernel.canvas.VtkCanvas;
import plugins.kernel.roi.roi2d.ROI2DArea;
import vtk.*;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 3D Area ROI.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class ROI3DArea extends ROI3DStack<ROI2DArea> {
    public class ROI3DAreaPainter extends ROI3DStackPainter implements Runnable {
        // VTK 3D objects
        protected vtkPolyData outline;
        protected vtkPolyDataMapper outlineMapper;
        protected vtkActor outlineActor;
        protected vtkInformation vtkInfo;
        protected vtkPolyData polyData;
        protected vtkPolyDataMapper polyMapper;
        protected vtkActor surfaceActor;
        // 3D internal
        protected boolean needRebuild;
        protected double[] scaling;
        protected WeakReference<VtkCanvas> canvas3d;

        public ROI3DAreaPainter() {
            super();

            outline = null;
            outlineMapper = null;
            outlineActor = null;
            vtkInfo = null;
            polyData = null;
            polyMapper = null;
            surfaceActor = null;

            scaling = new double[3];
            Arrays.fill(scaling, 1d);

            needRebuild = true;
            canvas3d = new WeakReference<>(null);
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();

            // release allocated VTK resources
            if (surfaceActor != null)
                surfaceActor.Delete();
            if (polyMapper != null)
                polyMapper.Delete();
            if (polyData != null) {
                polyData.GetPointData().GetScalars().Delete();
                polyData.GetPointData().Delete();
                polyData.Delete();
            }
            if (outlineActor != null) {
                outlineActor.SetPropertyKeys(null);
                outlineActor.Delete();
            }
            if (vtkInfo != null) {
                vtkInfo.Remove(VtkCanvas.visibilityKey);
                vtkInfo.Delete();
            }
            if (outlineMapper != null)
                outlineMapper.Delete();
            if (outline != null) {
                outline.GetPointData().GetScalars().Delete();
                outline.GetPointData().Delete();
                outline.Delete();
            }
        }

        protected void initVtkObjects() {
            outline = VtkUtil.getOutline(0d, 1d, 0d, 1d, 0d, 1d);
            outlineMapper = new vtkPolyDataMapper();
            outlineMapper.SetInputData(outline);
            outlineActor = new vtkActor();
            outlineActor.SetMapper(outlineMapper);
            // disable picking on the outline
            outlineActor.SetPickable(0);
            // and set it to wireframe representation
            outlineActor.GetProperty().SetRepresentationToWireframe();
            // use vtkInformations to store outline visibility state (hacky)
            vtkInfo = new vtkInformation();
            vtkInfo.Set(VtkCanvas.visibilityKey, 0);
            // VtkCanvas use this to restore correctly outline visibility flag
            outlineActor.SetPropertyKeys(vtkInfo);

            polyMapper = new vtkPolyDataMapper();
            surfaceActor = new vtkActor();
            surfaceActor.SetMapper(polyMapper);

            final Color col = getColor();
            final double r = col.getRed() / 255d;
            final double g = col.getGreen() / 255d;
            final double b = col.getBlue() / 255d;

            // set actors color
            outlineActor.GetProperty().SetColor(r, g, b);
            surfaceActor.GetProperty().SetColor(r, g, b);
        }

        /**
         * rebuild VTK objects (called only when VTK canvas is selected).
         */
        protected void rebuildVtkObjects() throws IllegalArgumentException, InterruptedException {
            final VtkCanvas canvas = canvas3d.get();
            // canvas was closed
            if (canvas == null)
                return;

            final IcyVtkPanel vtkPanel = canvas.getVtkPanel();
            // canvas was closed
            if (vtkPanel == null)
                return;

            final Sequence seq = canvas.getSequence();
            // nothing to update
            if (seq == null)
                return;

            // get previous polydata object
            final vtkPolyData previousPolyData = polyData;

            // get VTK binary image from ROI mask
            final vtkImageData imageData = VtkUtil.getBinaryImageData(ROI3DArea.this, seq.getSizeZ(),
                    canvas.getPositionT());
            // adjust spacing
            imageData.SetSpacing(scaling[0], scaling[1], scaling[2]);
            // get VTK polygon data representing the surface of the binary image
            polyData = VtkUtil.getSurfaceFromImage(imageData, 0.5d);

            // get bounds
            final Rectangle3D bounds = getBounds3D();
            // apply scaling on bounds
            bounds.setX(bounds.getX() * scaling[0]);
            bounds.setSizeX(bounds.getSizeX() * scaling[0]);
            bounds.setY(bounds.getY() * scaling[1]);
            bounds.setSizeY(bounds.getSizeY() * scaling[1]);
            if (bounds.isInfiniteZ()) {
                bounds.setZ(0);
                bounds.setSizeZ(seq.getSizeZ() * scaling[2]);
            }
            else {
                bounds.setZ(bounds.getZ() * scaling[2]);
                bounds.setSizeZ(bounds.getSizeZ() * scaling[2]);
            }

            // actor can be accessed in canvas3d for rendering so we need to synchronize access
            vtkPanel.lock();
            try {
                // update outline data
                VtkUtil.setOutlineBounds(outline, bounds.getMinX(), bounds.getMaxX(), bounds.getMinY(),
                        bounds.getMaxY(), bounds.getMinZ(), bounds.getMaxZ(), canvas);
                outlineMapper.Update();
                // update surface polygon data
                polyMapper.SetInputData(polyData);
                polyMapper.Update();

                // update actor position
                surfaceActor.SetPosition(bounds.getX(), bounds.getY(), bounds.getZ());

                // release image data
                imageData.GetPointData().GetScalars().Delete();
                imageData.GetPointData().Delete();
                imageData.Delete();

                // release previous polydata
                if (previousPolyData != null) {
                    previousPolyData.GetPointData().GetScalars().Delete();
                    previousPolyData.GetPointData().Delete();
                    previousPolyData.Delete();
                }
            }
            finally {
                vtkPanel.unlock();
            }

            // update color and others properties
            updateVtkDisplayProperties();
        }

        protected void updateVtkDisplayProperties() {
            if (surfaceActor == null)
                return;

            final VtkCanvas cnv = canvas3d.get();
            final Color col = getDisplayColor();
            final double r = col.getRed() / 255d;
            final double g = col.getGreen() / 255d;
            final double b = col.getBlue() / 255d;
            // final double strk = getStroke();
            // final float opacity = getOpacity();

            final IcyVtkPanel vtkPanel = (cnv != null) ? cnv.getVtkPanel() : null;

            // we need to lock canvas as actor can be accessed during rendering
            if (vtkPanel != null)
                vtkPanel.lock();
            try {
                // set actors color
                outlineActor.GetProperty().SetColor(r, g, b);
                if (isSelected()) {
                    outlineActor.GetProperty().SetRepresentationToWireframe();
                    outlineActor.SetVisibility(1);
                    vtkInfo.Set(VtkCanvas.visibilityKey, 1);
                }
                else {
                    outlineActor.GetProperty().SetRepresentationToPoints();
                    outlineActor.SetVisibility(0);
                    vtkInfo.Set(VtkCanvas.visibilityKey, 0);
                }
                surfaceActor.GetProperty().SetColor(r, g, b);
                // opacity here is about ROI content, global opacity is handled by Layer
                // surfaceActor.GetProperty().SetOpacity(opacity);
                setVtkObjectsColor(col);
            }
            finally {
                if (vtkPanel != null)
                    vtkPanel.unlock();
            }

            // need to repaint
            painterChanged();
        }

        protected void setVtkObjectsColor(final Color color) {
            if (outline != null)
                VtkUtil.setPolyDataColor(outline, color, canvas3d.get());
            if (polyData != null)
                VtkUtil.setPolyDataColor(polyData, color, canvas3d.get());
        }

        @Override
        public void mouseClick(final MouseEvent e, final Point5D.Double imagePoint, final IcyCanvas canvas) {
            // provide backward compatibility
            if (imagePoint != null)
                mouseClick(e, imagePoint.toPoint2D(), canvas);
            else
                mouseClick(e, (Point2D) null, canvas);

            // not yet consumed...
            if (!e.isConsumed()) {
                // and process ROI stuff now
                if (isActiveFor(canvas)) {
                    final int clickCount = e.getClickCount();

                    // double click
                    if (clickCount == 2) {
                        // focused ?
                        if (isFocused()) {
                            // show in ROI panel
                            final RoisPanel roiPanel = Icy.getMainInterface().getRoisPanel();

                            if (roiPanel != null) {
                                roiPanel.scrollTo(ROI3DArea.this);
                                // consume event
                                e.consume();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void paint(final Graphics2D g, final Sequence sequence, final IcyCanvas canvas) {
            super.paint(g, sequence, canvas);

            if (isActiveFor(canvas)) {
                if (canvas instanceof VtkCanvas cnv) {
                    // 3D canvas
                    // update reference if needed
                    if (canvas3d.get() != cnv)
                        canvas3d = new WeakReference<>(cnv);

                    // FIXME : need a better implementation
                    final double[] s = cnv.getVolumeScale();

                    // scaling changed ?
                    if (!Arrays.equals(scaling, s)) {
                        // update scaling
                        scaling = s;
                        // need rebuild
                        needRebuild = true;
                    }

                    // need to rebuild 3D data structures ?
                    if (needRebuild) {
                        // initialize VTK objects if not yet done
                        if (surfaceActor == null)
                            initVtkObjects();

                        // request rebuild 3D objects
                        ThreadUtil.runSingle(this);
                        needRebuild = false;
                    }
                }
            }
        }

        @Override
        protected boolean updateFocus(final InputEvent e, final Point5D imagePoint, final IcyCanvas canvas) {
            // specific VTK canvas processing
            if (canvas instanceof VtkCanvas) {
                // mouse is over the ROI actor ? --> focus the ROI
                final boolean focused = (surfaceActor != null)
                        && (surfaceActor == ((VtkCanvas) canvas).getPickedObject());

                setFocused(focused);

                return focused;
            }

            return super.updateFocus(e, imagePoint, canvas);
        }

        @Override
        public vtkProp[] getProps() {
            // initialize VTK objects if not yet done
            if (surfaceActor == null)
                initVtkObjects();

            return new vtkActor[]{surfaceActor, outlineActor};
        }

        @Override
        public void run() {
            try {
                rebuildVtkObjects();
            }
            catch (final IllegalArgumentException e) {
                IcyLogger.error(ROI3DArea.class, e, "Couldn't rebuild VTK objects.");
            }
            catch (final InterruptedException ie) {
                // ignore
            }
        }
    }

    public ROI3DArea() {
        super(ROI2DArea.class);
    }

    public ROI3DArea(final Point3D pt) {
        this();

        addBrush(pt.toPoint2D(), (int) pt.getZ());
    }

    public ROI3DArea(final Point5D pt) {
        this(pt.toPoint3D());
    }

    /**
     * Create a 3D Area ROI type from the specified {@link BooleanMask3D}.
     *
     * @param mask
     *        3D Mask
     */
    public ROI3DArea(final BooleanMask3D mask) {
        this();

        setAsBooleanMask(mask);
    }

    /**
     * Create a copy of the specified 3D Area ROI.
     *
     * @param area
     *        3D area
     */
    public ROI3DArea(final ROI3DArea area) {
        this();

        // copy the source 3D area ROI
        for (final Entry<Integer, ROI2DArea> entry : area.slices.entrySet())
            slices.put(entry.getKey(), new ROI2DArea(entry.getValue()));

        roiChanged(true);
    }

    /**
     * Create a 3D Area ROI type from the specified {@link BooleanMask3D}.
     *
     * @param mask2d
     *        2D mask
     * @param zMax
     *        int
     * @param zMin
     *        int
     */
    public ROI3DArea(final BooleanMask2D mask2d, final int zMin, final int zMax) {
        this();

        if (zMax < zMin)
            throw new IllegalArgumentException("ROI3DArea: cannot create the ROI (zMax < zMin).");

        beginUpdate();
        try {
            for (int z = zMin; z <= zMax; z++)
                setSlice(z, new ROI2DArea(mask2d));
        }
        finally {
            endUpdate();
        }
    }

    @Override
    public String getDefaultName() {
        return "Area3D";
    }

    @Override
    protected ROIPainter createPainter() {
        return new ROI3DAreaPainter();
    }

    /**
     * Adds the specified point to this ROI
     *
     * @param x
     *        int
     * @param y
     *        int
     * @param z
     *        int
     */
    public void addPoint(final int x, final int y, final int z) {
        setPoint(x, y, z, true);
    }

    /**
     * Remove a point from the mask.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation
     * to refresh the mask bounds.
     *
     * @param x
     *        int
     * @param z
     *        int
     * @param y
     *        int
     */
    public void removePoint(final int x, final int y, final int z) {
        setPoint(x, y, z, false);
    }

    /**
     * Set the value for the specified point in the mask.
     * Don't forget to call optimizeBounds() after consecutive remove point operation
     * to refresh the mask bounds.
     *
     * @param x
     *        int
     * @param y
     *        int
     * @param z
     *        int
     * @param value
     *        boolean
     */
    public void setPoint(final int x, final int y, final int z, final boolean value) {
        final ROI2DArea slice = getSlice(z, value);

        if (slice != null)
            slice.setPoint(x, y, value);
    }

    /**
     * Add brush point at specified position and for specified Z slice.
     *
     * @param z
     *        int
     * @param pos
     *        2D point
     */
    public void addBrush(final Point2D pos, final int z) {
        getSlice(z, true).addBrush(pos);
    }

    /**
     * Remove brush point from the mask at specified position and for specified Z slice.<br>
     * Don't forget to call optimizeBounds() after consecutive remove operation
     * to refresh the mask bounds.
     *
     * @param z
     *        int
     * @param pos
     *        2D point
     */
    public void removeBrush(final Point2D pos, final int z) {
        final ROI2DArea slice = getSlice(z, false);

        if (slice != null)
            slice.removeBrush(pos);
    }

    /**
     * @param mask
     *        Add the specified {@link BooleanMask3D} content to this ROI3DArea
     */
    public void add(final BooleanMask3D mask) {
        beginUpdate();
        try {
            for (final Entry<Integer, BooleanMask2D> entry : mask.mask.entrySet())
                add(entry.getKey().intValue(), entry.getValue());
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Add the specified BooleanMask2D with the existing slice at given Z position.<br>
     * If there is no slice at this Z position then the method is equivalent to setSlice(int, ROI2DArea) with
     * <code>new ROI2DArea(maskSlice)</code>
     *
     * @param z
     *        the position where the slice must be added
     * @param maskSlice
     *        the 2D boolean mask to merge
     */
    public void add(final int z, final BooleanMask2D maskSlice) {
        if (maskSlice == null)
            return;

        final ROI2DArea currentSlice = getSlice(z);

        if (currentSlice != null)
            // merge slices
            currentSlice.add(maskSlice);
        else
            // add new slice
            setSlice(z, new ROI2DArea(maskSlice));
    }

    /**
     * @param mask
     *        Exclusively add the specified {@link BooleanMask3D} content to this ROI3DArea
     */
    public void exclusiveAdd(final BooleanMask3D mask) {
        beginUpdate();
        try {
            for (final Entry<Integer, BooleanMask2D> entry : mask.mask.entrySet())
                exclusiveAdd(entry.getKey().intValue(), entry.getValue());
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Exclusively add the specified BooleanMask2D with the existing slice at given Z position.<br>
     * If there is no slice at this Z position then the method is equivalent to setSlice(int, ROI2DArea) with
     * <code>new ROI2DArea(maskSlice)</code>
     *
     * @param z
     *        the position where the slice must be exclusively added
     * @param maskSlice
     *        the 2D boolean mask to merge
     */
    public void exclusiveAdd(final int z, final BooleanMask2D maskSlice) {
        if (maskSlice == null)
            return;

        final ROI2DArea currentSlice = getSlice(z);

        // merge both slice
        if (currentSlice != null) {
            // process exclusive add
            currentSlice.exclusiveAdd(maskSlice);
            // remove it if empty
            if (currentSlice.isEmpty())
                removeSlice(z);
        }
        // add new slice
        else
            setSlice(z, new ROI2DArea(maskSlice));
    }

    /**
     * @param mask
     *        Intersect the specified {@link BooleanMask3D} content with this ROI3DArea
     */
    public void intersect(final BooleanMask3D mask) throws UnsupportedOperationException, InterruptedException {
        beginUpdate();
        try {
            final Set<Integer> keys = mask.mask.keySet();
            final Set<Integer> toRemove = new HashSet<>();

            // remove slices which are not contained
            for (final Integer key : slices.keySet())
                if (!keys.contains(key))
                    toRemove.add(key);

            // do remove first
            for (final Integer key : toRemove)
                removeSlice(key.intValue());

            // then process intersection
            for (final Entry<Integer, BooleanMask2D> entry : mask.mask.entrySet())
                intersect(entry.getKey().intValue(), entry.getValue());
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Intersect the specified BooleanMask2D with the existing slice at given Z position.
     *
     * @param z
     *        the position where the slice must be set
     * @param maskSlice
     *        the 2D boolean mask to merge
     */
    public void intersect(final int z, final BooleanMask2D maskSlice) throws UnsupportedOperationException, InterruptedException {
        // better to throw an exception here than removing slice
        if (maskSlice == null)
            throw new IllegalArgumentException("Cannot intersect an empty slice in a 3D ROI");

        final ROI2DArea currentSlice = getSlice(z);

        if (currentSlice != null) {
            // build ROI from mask
            final ROI2DArea roi = new ROI2DArea(maskSlice);

            // set same position as slice
            roi.setT(currentSlice.getT());
            roi.setZ(currentSlice.getZ());
            roi.setC(currentSlice.getC());
            // compute intersection
            currentSlice.intersect(roi, false);

            // remove it if empty
            if (currentSlice.isEmpty())
                removeSlice(z);
        }
    }

    /**
     * @param mask
     *        Subtract the specified {@link BooleanMask3D} from this ROI3DArea
     */
    public void subtract(final BooleanMask3D mask) {
        beginUpdate();
        try {
            for (final Entry<Integer, BooleanMask2D> entry : mask.mask.entrySet())
                subtract(entry.getKey().intValue(), entry.getValue());
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Subtract the specified BooleanMask2D from the existing slice at given Z position.
     *
     * @param z
     *        the position where the slice must be subtracted
     * @param maskSlice
     *        the 2D boolean mask to subtract
     */
    public void subtract(final int z, final BooleanMask2D maskSlice) {
        if (maskSlice == null)
            return;

        final ROI2DArea currentSlice = getSlice(z);

        // merge both slice
        if (currentSlice != null) {
            // process exclusive add
            currentSlice.subtract(maskSlice);
            // remove it if empty
            if (currentSlice.isEmpty())
                removeSlice(z);
        }
    }

    @Override
    public ROI add(final ROI roi, final boolean allowCreate) throws UnsupportedOperationException, InterruptedException {
        if (roi instanceof final ROI3D roi3d) {
            // only if on same position
            if ((getT() == roi3d.getT()) && (getC() == roi3d.getC())) {
                if (roi3d instanceof ROI3DArea)
                    add((ROI3DArea) roi3d);
                else
                    add(roi3d.getBooleanMask(true));

                return this;
            }
        }

        return super.add(roi, allowCreate);
    }

    @Override
    public ROI exclusiveAdd(final ROI roi, final boolean allowCreate) throws UnsupportedOperationException, InterruptedException {
        if (roi instanceof final ROI3D roi3d) {
            // only if on same position
            if ((getT() == roi3d.getT()) && (getC() == roi3d.getC())) {
                if (roi3d instanceof ROI3DArea)
                    exclusiveAdd((ROI3DArea) roi3d);
                else
                    exclusiveAdd(roi3d.getBooleanMask(true));

                return this;
            }
        }

        return super.exclusiveAdd(roi, allowCreate);
    }

    @Override
    public ROI intersect(final ROI roi, final boolean allowCreate) throws UnsupportedOperationException, InterruptedException {
        if (roi instanceof final ROI3D roi3d) {
            // only if on same position
            if ((getT() == roi3d.getT()) && (getC() == roi3d.getC())) {
                if (roi3d instanceof ROI3DArea)
                    intersect((ROI3DArea) roi3d);
                else
                    intersect(roi3d.getBooleanMask(true));

                return this;
            }
        }

        return super.intersect(roi, allowCreate);
    }

    @Override
    public ROI subtract(final ROI roi, final boolean allowCreate) throws UnsupportedOperationException, InterruptedException {
        if (roi instanceof final ROI3D roi3d) {
            // only if on same position
            if ((getT() == roi3d.getT()) && (getC() == roi3d.getC())) {
                if (roi3d instanceof ROI3DArea)
                    subtract((ROI3DArea) roi3d);
                else
                    subtract(roi3d.getBooleanMask(true));

                return this;
            }
        }

        return super.subtract(roi, allowCreate);
    }

    /**
     * Sets the BooleanMask2D slice at given Z position to this 3D ROI
     *
     * @param z
     *        the position where the slice must be set
     * @param maskSlice
     *        the BooleanMask2D to set
     */
    public void setSlice(final int z, final BooleanMask2D maskSlice) {
        // empty mask --> just remove previous
        if (maskSlice == null) {
            removeSlice(z);
            return;
        }

        setSlice(z, new ROI2DArea(maskSlice));
    }

    /**
     * @deprecated Use one of these methods instead :<br>
     *             {@link ROI3DStack#setSlice(int, ROI2D)},
     *             {@link ROI3DArea#setSlice(int, BooleanMask2D)},
     *             {@link ROI3DStack#add(int, ROI2D)} or
     *             {@link ROI3DArea#add(BooleanMask3D)}
     * @param z
     *        int
     * @param roiSlice
     *        ROI Slice
     * @param merge
     *        boolean
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public void setSlice(final int z, final ROI2D roiSlice, final boolean merge) throws InterruptedException {
        if (roiSlice == null)
            throw new IllegalArgumentException("Cannot add an empty slice in a 3D ROI");

        final ROI2DArea currentSlice = getSlice(z);
        final ROI newSlice;

        // merge both slice
        if ((currentSlice != null) && merge) {
            // we need to modify the Z, T and C position so we do the merge correctly
            roiSlice.setZ(z);
            roiSlice.setT(getT());
            roiSlice.setC(getC());
            // do ROI union
            newSlice = currentSlice.getUnion(roiSlice);
        }
        else
            newSlice = roiSlice;

        if (newSlice instanceof ROI2DArea)
            setSlice(z, (ROI2DArea) newSlice);
        else if (newSlice instanceof ROI2D)
            setSlice(z, new ROI2DArea(((ROI2D) newSlice).getBooleanMask(true)));
        else
            throw new IllegalArgumentException("Can't add the result of the merge operation on 2D slice " + z + ": " + newSlice.getClassName());
    }

    // /**
    // * Merge the specified ROI with the existing slice at given Z position.<br>
    // * If there is no slice at this Z position then the method is equivalent to {@link #setSlice(int, ROI2DArea)}
    // *
    // * @param z
    // * the position where the slice must be set
    // * @param roiSlice
    // * the 2D ROI to merge
    // */
    // public void addROI2DSlice(int z, ROI2D roiSlice)
    // {
    // if (roiSlice == null)
    // return;
    //
    // final ROI2DArea currentSlice = getSlice(z);
    //
    // // merge both slice
    // if (currentSlice != null)
    // {
    // // we need to modify the Z, T and C position so we do the merge correctly
    // roiSlice.setZ(z);
    // roiSlice.setT(getT());
    // roiSlice.setC(getC());
    // // do ROI union
    // currentSlice.add(roiSlice, true);
    // }
    // else
    // setSlice(z, new ROI2DArea(roiSlice.getBooleanMask(true)));
    // }

    /**
     * @return Returns true if the ROI is empty (the mask does not contains any point).
     */
    @Override
    public boolean isEmpty() {
        for (final ROI2DArea area : slices.values())
            if (!area.isEmpty())
                return false;

        return true;
    }

    /**
     * @deprecated Use {@link #getBooleanMask(boolean)} and {@link BooleanMask3D#getContourPoints()} instead.
     * @return array of 3D points
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public Point3D[] getEdgePoints() throws InterruptedException {
        return getBooleanMask(true).getContourPoints();
    }

    /**
     * @deprecated Use {@link #getBooleanMask(boolean)} and {@link BooleanMask3D#getPoints()} instead.
     * @return array of 3D points
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public Point3D[] getPoints() throws InterruptedException {
        return getBooleanMask(true).getPoints();
    }

    /**
     * @deprecated Use {@link #translate(double, double, double)} instead.
     * @param dy
     *        double
     * @param dx
     *        double
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public void translate(final double dx, final double dy) {
        beginUpdate();
        try {
            for (final ROI2DArea slice : slices.values())
                slice.translate(dx, dy);
        }
        finally {
            endUpdate();
        }
    }

    @Override
    public boolean isOverEdge(final IcyCanvas canvas, final double x, final double y, final double z) {
        final ROI2DArea slice = getSlice((int) z);

        if (slice != null)
            return slice.isOverEdge(canvas, x, y);

        return false;
    }

    /**
     * @param newPosition
     *        Set all 2D slices ROI to same position.
     */
    public void setPosition2D(final Point2D newPosition) {
        beginUpdate();
        try {
            for (final ROI2DArea slice : slices.values())
                slice.setPosition2D(newPosition);
        }
        finally {
            endUpdate();
        }
    }

    /**
     * @param mask
     *        Set the mask from a BooleanMask3D object.<br>
     *        If specified mask is <i>null</i> then ROI is cleared.
     */
    public void setAsBooleanMask(final BooleanMask3D mask) {
        // mask empty ? --> just clear the ROI
        if ((mask == null) || mask.isEmpty())
            clear();
        else {
            final Rectangle3D.Integer bounds3d = mask.bounds;
            final int startZ = bounds3d.z;
            final int sizeZ = bounds3d.sizeZ;
            final BooleanMask2D[] masks2d = new BooleanMask2D[sizeZ];

            for (int z = 0; z < sizeZ; z++)
                masks2d[z] = mask.getMask2D(startZ + z);

            setAsBooleanMask(bounds3d, masks2d);
        }
    }

    /**
     * Set the 3D mask from a 2D boolean mask array
     *
     * @param rect
     *        the 3D region defined by 2D boolean mask array
     * @param mask
     *        the 3D mask data (array length should be equals to rect.sizeZ)
     */
    public void setAsBooleanMask(final Rectangle3D.Integer rect, final BooleanMask2D[] mask) {
        if (rect.isInfiniteZ())
            throw new IllegalArgumentException("Cannot set infinite Z dimension on the 3D Area ROI.");

        beginUpdate();
        try {
            clear();

            for (int z = 0; z < rect.sizeZ; z++)
                setSlice(z + rect.z, new ROI2DArea(mask[z]));
        }
        finally {
            endUpdate();
        }
    }

    /**
     * Fast up scaling by a factor of 2 (each point become a 2x2x2 block points)
     */
    public void upscale() throws InterruptedException {
        setAsBooleanMask(getBooleanMask(true).upscale());
    }

    /**
     * Fast 2x down scaling (each 2x2x2 block points become 1 point).
     *
     * @param nbPointForTrue
     *        the minimum number of <code>true</code>points from a 2x2x2 block to give a <code>true</code> resulting
     *        point.<br>
     *        Accepted value: 1 to 5 (default is 5)
     */
    public void downscale(final int nbPointForTrue) throws InterruptedException {
        setAsBooleanMask(getBooleanMask(true).downscale(nbPointForTrue));
    }

    /**
     * Fast 2x down scaling (each 2x2x2 block points become 1 point).
     */
    public void downscale() throws InterruptedException {
        setAsBooleanMask(getBooleanMask(true).downscale());
    }

    /**
     * Fast up scaling by a factor of 2 (each point become a 2x2 block points)
     * 2D version (down scale is done on XY dimension only).
     */
    public void upscale2D() throws InterruptedException {
        setAsBooleanMask(getBooleanMask(true).upscale2D());
    }

    /**
     * Fast 2x down scaling (each 2x2 block points become 1 point).<br>
     * 2D version (down scale is done on XY dimension only).
     *
     * @param nbPointForTrue
     *        the minimum number of <code>true</code>points from a 2x2 block to give a <code>true</code> resulting
     *        point.<br>
     *        Accepted value: 1 to 4
     */
    public void downscale2D(final int nbPointForTrue) throws InterruptedException {
        setAsBooleanMask(getBooleanMask(true).downscale2D(nbPointForTrue));
    }

    /**
     * Fast 2x down scaling (each 2x2 block points become 1 point).<br>
     * 2D version (down scale is done on XY dimension only).
     */
    public void downscale2D() throws InterruptedException {
        setAsBooleanMask(getBooleanMask(true).downscale2D());
    }

    /**
     * Optimize the bounds size to the minimum surface which still include all mask.<br>
     * You should call it after consecutive remove operations if you directly addressed mask data.
     */
    public void optimizeBounds() {
        final Rectangle3D.Integer bounds = getBounds();

        beginUpdate();
        try {
            for (int z = bounds.z; z < bounds.z + bounds.sizeZ; z++) {
                final ROI2DArea roi = getSlice(z);

                if (roi != null) {
                    if (roi.isEmpty())
                        removeSlice(z);
                    else {
                        if (roi.optimizeBounds())
                            roi.roiChanged(true);
                    }
                }
            }
        }
        finally {
            endUpdate();
        }
    }

    /**
     * roi changed
     */
    @Override
    public void onChanged(final CollapsibleEvent object) {
        final ROIEvent event = (ROIEvent) object;

        // do here global process on ROI change
        switch (event.getType()) {
            case ROI_CHANGED:
                // the painter need to be rebuild
                ((ROI3DAreaPainter) painter).needRebuild = true;
                break;

            case FOCUS_CHANGED:
            case SELECTION_CHANGED:
                ((ROI3DAreaPainter) getOverlay()).updateVtkDisplayProperties();
                break;

            case PROPERTY_CHANGED:
                final String property = event.getPropertyName();

                if (StringUtil.equals(property, PROPERTY_STROKE) || StringUtil.equals(property, PROPERTY_COLOR)
                        || StringUtil.equals(property, PROPERTY_OPACITY))
                    ((ROI3DAreaPainter) getOverlay()).updateVtkDisplayProperties();
                break;

            default:
                break;
        }

        super.onChanged(object);
    }
}
