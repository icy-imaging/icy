/*
 * Copyright (c) 2010-2023. Institut Pasteur.
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

package icy.vtk;

import icy.preferences.CanvasPreferences;
import icy.system.thread.ThreadUtil;
import icy.util.EventUtil;
import vtk.*;

import java.awt.*;
import java.awt.event.*;

/**
 * Icy custom VTK panel used for VTK rendering.
 *
 * @author stephane dallongeville
 */
public class IcyVtkPanel extends VtkJoglPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, Runnable {
    private enum SlicerPickState {
        PICK_NONE, PICK_VECTOR, PICK_PLANE
    }

    protected Thread renderingMonitor;
    protected vtkCellPicker picker;

    // axis
    protected vtkAxesActor axis;
    protected vtkRenderer axisRenderer;
    protected vtkCamera axisCam;
    protected int[] axisOffset;
    protected double axisScale;

    // plane slicer
    private final VtkArrowObject arrowClip;
    private final VtkPlaneObject planeClip;
    private vtkRenderer slicerRenderer;
    private vtkCamera slicerCam;
    private SlicerPickState slicerPickState;
    protected int[] slicerOffset;
    protected double slicerScale;

    protected boolean lightFollowCamera;
    protected volatile long fineRenderingTime;

    // picked object
    protected vtkProp pickedObject;

    public IcyVtkPanel() {
        super();

        picker = new vtkCellPicker();
        picker.PickFromListOff();
        // important to disable that one as it may cause crash on polydata picking where VTK wrongly assume texture data in some case (--> crash)
        picker.PickTextureDataOff();
        picker.UseVolumeGradientOpacityOff();

        pickedObject = null;

        // set ambient color to white
        lgt.SetAmbientColor(1d, 1d, 1d);
        lightFollowCamera = true;

        // assign default renderer to layer 0 (should be the case by default)
        ren.SetLayer(0);

        // initialize axis renderer
        axisRenderer = new vtkRenderer();
        // BUG: with OpenGL window the global render window viewport is limited to the last layer viewport dimension
        // axisRenderer.SetViewport(0.0, 0.0, 0.2, 0.2);
        axisRenderer.SetLayer(1);
        axisRenderer.InteractiveOff();

        // initialize slicer renderer
        slicerRenderer = new vtkRenderer();
        // use current number of layer as layer index
        slicerRenderer.SetLayer(2);
        slicerRenderer.InteractiveOff();

        // add axis renderer (add slicer renderer only when slicer enable)
        rw.AddRenderer(axisRenderer);
        rw.SetNumberOfLayers(2);

        // initialize axis actor
        axis = new vtkAxesActor();
        // fix caption scaling (we need that since VTK 7)
        axis.GetXAxisCaptionActor2D().GetTextActor().SetTextScaleModeToNone();
        axis.GetYAxisCaptionActor2D().GetTextActor().SetTextScaleModeToNone();
        axis.GetZAxisCaptionActor2D().GetTextActor().SetTextScaleModeToNone();
        axisRenderer.AddActor(axis);

        // init axis camera
        axisCam = axisRenderer.GetActiveCamera();
        axisCam.SetViewUp(0, -1, 0);
        axisCam.Elevation(195);
        axisCam.SetParallelProjection(1);
        axisRenderer.ResetCamera();
        axisRenderer.ResetCameraClippingRange();

        // default axis offset and scale
        axisOffset = new int[]{124, 124};
        axisScale = 1d;

        // initialize slicer actors
        arrowClip = new VtkArrowObject();
        // bring 1,0,0 arrow orientation to 0,-1,0
        arrowClip.getActor().SetOrientation(0d, 0d, -90d);
        // set position to Y=0.5 so arrow cover Y range from -0.5 to 0.5
        arrowClip.getActor().SetPosition(0d, 0.5d, 0d);
        arrowClip.setColor(Color.green);
        planeClip = new VtkPlaneObject();
        planeClip.setNormal(0, -1, 0);
        planeClip.setColor(Color.white);
        planeClip.setEdgeColor(Color.darkGray);
        planeClip.setEdgeVisibile(false);
        planeClip.setWireframeMode();
        planeClip.getActor().GetProperty().SetAmbient(0.8d);
        planeClip.getActor().GetProperty().SetDiffuse(0.2d);
        planeClip.getActor().GetProperty().SetLineWidth(2f);
        slicerRenderer.AddActor(arrowClip.getActor());
        slicerRenderer.AddActor(planeClip.getActor());

        // init slicer camera
        slicerCam = slicerRenderer.GetActiveCamera();
        slicerCam.SetViewUp(0, -1, 0);
        slicerCam.Elevation(195);
        slicerCam.SetParallelProjection(1);
        slicerRenderer.ResetCamera();
        slicerRenderer.ResetCameraClippingRange();

        // default slicer properties
        slicerPickState = SlicerPickState.PICK_NONE;
        // from right border
        slicerOffset = new int[]{124, 124};
        slicerScale = 1d;

        // used for restore quality rendering after a given amount of time
        fineRenderingTime = 0;
        renderingMonitor = new Thread(this, "VTK panel rendering monitor");
        renderingMonitor.start();

        addMouseListener(this);
        addMouseMotionListener(this);
        addMouseWheelListener(this);
        addKeyListener(this);
    }

    @Override
    protected void delete() {
        // stop thread
        fineRenderingTime = 0;
        renderingMonitor.interrupt();

        super.delete();

        lock.lock();
        try {
            // release VTK objects
            arrowClip.release();
            planeClip.release();
            slicerCam.Delete();
            slicerRenderer.Delete();
            axisCam.Delete();
            axis.Delete();
            axisRenderer.Delete();
            picker.Delete();

            slicerCam = null;
            slicerRenderer = null;
            axisCam = null;
            axis = null;
            axisRenderer = null;
            picker = null;

            // call it once in parent as this can take a lot fo time
            // vtkObjectBase.JAVA_OBJECT_MANAGER.gc(false);
        }
        finally {
            // removing the renderWindow is let to the superclass
            // because in the very special case of an AWT component
            // under Linux, destroying renderWindow crashes.
            lock.unlock();
        }
    }

    @Override
    public void removeNotify() {
        // cancel fine rendering request
        fineRenderingTime = 0;

        super.removeNotify();
    }

    @Override
    public void sizeChanged() {
        super.sizeChanged();

        updateAxisView();
        updateSlicerView();
    }

    /**
     * Return picker object.
     */
    public vtkPicker getPicker() {
        return picker;
    }

    /**
     * Returns the picked object on the last mouse move event (can be <code>null</code> if no object was picked).
     *
     * @see #pick(int, int)
     */
    public vtkProp getPickedObject() {
        return pickedObject;
    }

    /**
     * Return the actor for axis orientation display.
     */
    public vtkAxesActor getAxesActor() {
        return axis;
    }

    public boolean getLightFollowCamera() {
        return lightFollowCamera;
    }

    /**
     * Return true if the axis orientation display is enabled
     */
    public boolean isAxisOrientationDisplayEnable() {
        return (axis.GetVisibility() != 0);
    }

    /**
     * Returns the offset from border ({X, Y} format) for the axis orientation display
     */
    public int[] getAxisOrientationDisplayOffset() {
        return axisOffset;
    }

    /**
     * Returns the scale factor (default = 1) for the axis orientation display
     */
    public double getAxisOrientationDisplayScale() {
        return axisScale;
    }

    /**
     * Set to <code>true</code> to automatically update light position to camera position when camera move.
     */
    public void setLightFollowCamera(final boolean value) {
        lightFollowCamera = value;
    }

    /**
     * Enable/Disable the axis orientation display
     */
    public void setAxisOrientationDisplayEnable(final boolean value) {
        axis.SetVisibility(value ? 1 : 0);
        updateAxisView();
    }

    /**
     * Sets the offset from border ({X, Y} format) for the axis orientation display (default = {130, 130})
     */
    public void setAxisOrientationDisplayOffset(final int[] value) {
        axisOffset = value;
        updateAxisView();
    }

    /**
     * Set the scale factor (default = 1) for the axis orientation display
     */
    public void setAxisOrientationDisplayScale(final double value) {
        axisScale = value;
        updateAxisView();
    }

    /**
     * Return true if the slicer is enable
     */
    public boolean isSlicerEnable() {
        return (rw.HasRenderer(slicerRenderer) != 0);
    }

    /**
     * Enable/Disable the slicer
     */
    public void setSlicerEnable(final boolean value) {
        if (!isWindowSet())
            return;

        lock();
        try {
            if (value) {
                if (rw.HasRenderer(slicerRenderer) == 0) {
                    rw.AddRenderer(slicerRenderer);
                    rw.SetNumberOfLayers(3);
                }
            }
            else {
                if (rw.HasRenderer(slicerRenderer) == 1) {
                    rw.RemoveRenderer(slicerRenderer);
                    rw.SetNumberOfLayers(2);
                }
            }
        }
        finally {
            unlock();
        }

        updateSlicerView();

        // call this when plane clip changed
        planeClipChanged();
    }

    /**
     * @deprecated Use {@link #pick(int, int)} instead
     */
    @Deprecated(since = "2.4.3", forRemoval = true)
    public void pickActor(final int x, final int y) {
        pick(x, y);
    }

    /**
     * Pick object at specified position and return it.
     */
    public vtkProp pick(final int x, final int y, final vtkRenderer renderer) {
        lock();
        try {
            final float[] scale = getCurrentSurfaceScale(new float[2]);

            picker.Pick(x * scale[0], rw.GetSize()[1] - (y * scale[1]), 0, renderer);
        }
        finally {
            unlock();
        }

        return picker.GetViewProp();
    }

    /**
     * Pick object at specified position and return it.
     */
    public vtkProp pick(final int x, final int y) {
        return pick(x, y, ren);
    }

    /**
     * Translate specified camera view
     */
    public void translateView(final vtkCamera c, final vtkRenderer r, final double dx, final double dy) {
        // translation mode
        final double[] FPoint;
        final double[] PPoint;
        final double[] APoint = new double[3];
        final double[] RPoint;
        final double focalDepth;

        lock();
        try {
            // get the current focal point and position
            FPoint = c.GetFocalPoint();
            PPoint = c.GetPosition();

            // calculate the focal depth since we'll be using it a lot
            r.SetWorldPoint(FPoint[0], FPoint[1], FPoint[2], 1.0);
            r.WorldToDisplay();
            focalDepth = r.GetDisplayPoint()[2];

            final int[] size = rw.GetSize();
            APoint[0] = (size[0] / 2.0) + dx;
            APoint[1] = (size[1] / 2.0) + dy;
            APoint[2] = focalDepth;
            r.SetDisplayPoint(APoint);
            r.DisplayToWorld();
            RPoint = r.GetWorldPoint();
            if (RPoint[3] != 0.0) {
                RPoint[0] = RPoint[0] / RPoint[3];
                RPoint[1] = RPoint[1] / RPoint[3];
                RPoint[2] = RPoint[2] / RPoint[3];
            }

            /*
             * Compute a translation vector, moving everything 1/2 the distance
             * to the cursor. (Arbitrary scale factor)
             */
            c.SetFocalPoint(
                    (FPoint[0] - RPoint[0]) / 2.0 + FPoint[0],
                    (FPoint[1] - RPoint[1]) / 2.0 + FPoint[1],
                    (FPoint[2] - RPoint[2]) / 2.0 + FPoint[2]
            );
            c.SetPosition(
                    (FPoint[0] - RPoint[0]) / 2.0 + PPoint[0],
                    (FPoint[1] - RPoint[1]) / 2.0 + PPoint[1],
                    (FPoint[2] - RPoint[2]) / 2.0 + PPoint[2]
            );
            r.ResetCameraClippingRange();
        }
        finally {
            unlock();
        }
    }

    /**
     * Rotate specified camera view
     */
    public void rotateView(final vtkCamera c, final vtkRenderer r, final int dx, final int dy) {
        lock();
        try {
            // rotation mode
            c.Azimuth(dx);
            c.Elevation(dy);
            c.OrthogonalizeViewUp();
            r.ResetCameraClippingRange();
        }
        finally {
            unlock();
        }
    }

    /**
     * Zoom current view by specified factor (value &lt; 1d means unzoom while value &gt; 1d mean zoom)
     */
    public void zoomView(final vtkCamera c, final vtkRenderer r, final double factor) {
        lock();
        try {
            if (c.GetParallelProjection() == 1)
                c.SetParallelScale(c.GetParallelScale() / factor);
            else {
                c.Dolly(factor);
                r.ResetCameraClippingRange();
            }
        }
        finally {
            unlock();
        }
    }

    /**
     * Translate current camera view
     */
    public void translateView(final double dx, final double dy) {
        translateView(cam, ren, dx, dy);
        // adjust light position
        if (getLightFollowCamera())
            setLightToCameraPosition(lgt, cam);
    }

    /**
     * Rotate current camera view
     */
    public void rotateView(final int dx, final int dy) {
        // rotate world view
        rotateView(cam, ren, dx, dy);
        // adjust light position
        if (getLightFollowCamera())
            setLightToCameraPosition(lgt, cam);
        // rotate slicer view identically
        rotateSlicerView(dx, dy);
        // update axis camera
        updateAxisView();
    }

    /**
     * Zoom current view by specified factor (negative value means unzoom)
     */
    public void zoomView(final double factor) {
        // zoom world
        zoomView(cam, ren, factor);
        // update axis camera
        updateAxisView();
    }

    /**
     * Rotate slicer camera view
     */
    public void rotateSlicerView(final int dx, final int dy) {
        // reset slicer camera before doing interaction
        slicerRenderer.ResetCamera();
        slicerRenderer.ResetCameraClippingRange();
        // rotate slicer view identically
        rotateView(slicerCam, slicerRenderer, dx, dy);
        // update slicer camera
        updateSlicerView();

        // volume plane clip changed
        planeClipChanged();
    }

    /**
     * Set the specified light at the same position than the specified camera
     */
    public static void setLightToCameraPosition(final vtkLight l, final vtkCamera c) {
        l.SetPosition(c.GetPosition());
        l.SetFocalPoint(c.GetFocalPoint());
    }

    /**
     * Set coarse and fast rendering mode immediately
     *
     * @see #setCoarseRendering(long)
     * @see #setFineRendering()
     */
    public void setCoarseRendering() {
        // cancel pending fine rendering restoration
        fineRenderingTime = 0;

        if (rw.GetDesiredUpdateRate() == 20d)
            return;

        lock();
        try {
            // set fast rendering
            rw.SetDesiredUpdateRate(20d);
        }
        finally {
            unlock();
        }
    }

    /**
     * Set coarse and fast rendering mode <b>for the specified amount of time</b> (in ms).<br>
     * Setting it to 0 means for always.
     *
     * @see #setFineRendering(long)
     */
    public void setCoarseRendering(final long time) {
        // want fast update
        setCoarseRendering();

        if (time > 0)
            fineRenderingTime = System.currentTimeMillis() + time;
    }

    /**
     * Set fine (and possibly slow) rendering mode immediately
     *
     * @see #setFineRendering(long)
     * @see #setCoarseRendering()
     */
    public void setFineRendering() {
        // cancel pending fine rendering restoration
        fineRenderingTime = 0;

        if (rw.GetDesiredUpdateRate() == 0.01)
            return;

        lock();
        try {
            // set quality rendering
            rw.SetDesiredUpdateRate(0.01);
        }
        finally {
            unlock();
        }
    }

    /**
     * Set fine (and possibly slow) rendering <b>after</b> specified time delay (in ms).<br>
     * Using 0 means we want to immediately switch to fine rendering.
     *
     * @see #setCoarseRendering(long)
     */
    public void setFineRendering(final long delay) {
        if (delay > 0)
            fineRenderingTime = System.currentTimeMillis() + delay;
        else
            // set back quality rendering immediately
            setFineRendering();
    }

    public boolean isSlicerPicked() {
        return slicerPickState != SlicerPickState.PICK_NONE;
    }

    protected boolean slicerPick(final MouseEvent e) {
        final SlicerPickState oldPickState = slicerPickState;
        final int[] size = rw.GetSize();
        final int w = size[0];
        final int h = size[1];
        final float[] scale = getCurrentSurfaceScale(new float[2]);
        final int x = (int) (e.getX() * scale[0]);
        final int y = (int) (e.getY() * scale[1]);

        // are we on the volume slicer area ?
        if ((x > (w * (1d - (0.25d * ((double) h / (double) w))))) && (y > (h * (1d - 0.25d)))) {
            // slicer plane picked ?
            if (pick(e.getX(), e.getY(), slicerRenderer) == planeClip.getActor())
                slicerPickState = SlicerPickState.PICK_PLANE;
            else
                slicerPickState = SlicerPickState.PICK_VECTOR;

            // consume on slicer pick so we prevent interaction with objects located under slicer
            e.consume();
        }
        else
            slicerPickState = SlicerPickState.PICK_NONE;

        if (slicerPickState == SlicerPickState.PICK_PLANE) {
            planeClip.setColor(Color.white);
            planeClip.setSurfaceMode();
            planeClip.setEdgeVisibile(true);
        }
        else {
            planeClip.setColor(Color.gray);
            planeClip.setWireframeMode();
            planeClip.setEdgeVisibile(false);
        }

        if (slicerPickState == SlicerPickState.PICK_VECTOR)
            arrowClip.setColor(Color.white);
        else
            arrowClip.setColor(Color.green);

        // pick state changed
        return oldPickState != slicerPickState;
    }

    protected void translateSlicerPlane(final int deltaX, final int deltaY) {
        final double[] pos = planeClip.getActor().GetPosition();
        final double delta;

        // adjust delta depending camera rotation
        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            if (slicerCam.GetOrientation()[2] < 0)
                delta = -deltaX;
            else
                delta = deltaX;
        }
        else {
            if (Math.abs(slicerCam.GetOrientation()[2]) > 90)
                delta = -deltaY;
            else
                delta = deltaY;
        }

        pos[1] += delta / 100d;
        // we shifted from -0.1
        if (pos[1] > 0.7d)
            pos[1] = 0.7d;
        else if (pos[1] < -0.7d)
            pos[1] = -0.7d;

        planeClip.getActor().SetPosition(pos);

        // volume plane clip changed
        planeClipChanged();
    }

    protected void planeClipChanged() {
        // override it
    }

    /**
     * @return clip plane normal
     */
    public double[] getClipNormal() {
        // initial arrow vector
        double[] result = new double[]{0d, -1d, 0d};

        final vtkTransform camTrans = cam.GetModelViewTransformObject();
        final vtkTransform slicerTrans = slicerCam.GetModelViewTransformObject();

        // create new identity transform
        final vtkTransform trans = new vtkTransform();
        trans.Identity();
        // **important to concatenate through the matrix and not the transform object directly !!**
        trans.Concatenate(camTrans.GetMatrix());
        trans.Inverse();
        trans.Concatenate(slicerTrans.GetMatrix());

        // System.out.println("cam =" + ArrayUtil.array1DToString(camTrans.GetOrientation(), true, false, " ", 3)
        // + " slicer =" + ArrayUtil.array1DToString(slicerTrans.GetOrientation(), true, false, " ", 3)
        // + " finale =" + ArrayUtil.array1DToString(trans.GetOrientation(), true, false, " ", 3));

        // transform using merged transform
        result = trans.TransformDoubleNormal(result);

        // System.out.println("clip plane =" + StringUtil.toString(result[0], 3) + "," + StringUtil.toString(result[1], 3)
        // + "," + StringUtil.toString(result[2], 3));

        // invert X/Z axis (not sure why this is needed)
        // result[1] = -result[1];
        // result[2] = -result[2];

        return result;
    }

    /**
     * @return Z clip position in [-0.5..0.5] range
     */
    public double getClipPosition() {
        // System.out.println("pos=" + StringUtil.toString(planeClip.getActor().GetPosition()[1], 3));

        // use plane actor Y position
        return planeClip.getActor().GetPosition()[1];
    }

    /**
     * Update axis display depending the current scene camera view.<br>
     * You should call it after having modified camera settings.
     */
    public void updateAxisView() {
        if (!isWindowSet())
            return;

        lock();
        try {
            final double[] pos = cam.GetPosition();
            final double[] fp = cam.GetFocalPoint();
            final double[] viewup = cam.GetViewUp();

            // mimic axis camera position to scene camera position
            axisCam.SetPosition(pos);
            axisCam.SetFocalPoint(fp);
            axisCam.SetViewUp(viewup);
            axisRenderer.ResetCamera();

            final int[] size = rw.GetSize();
            // adjust scale
            final double scale = size[1] / 512d;
            // adjust offset
            final int w = (int) (size[0] - (axisOffset[0] * scale));
            final int h = (int) (size[1] - (axisOffset[1] * scale));
            // zoom and translate
            zoomView(axisCam, axisRenderer, axisScale * (axisCam.GetDistance() / 17d));
            translateView(axisCam, axisRenderer, -w, -h);
        }
        finally {
            unlock();
        }
    }

    /**
     * Update slicer display (call after any camera reset / change)
     */
    public void updateSlicerView() {
        if (!isWindowSet())
            return;

        lock();
        try {
            final int[] size = rw.GetSize();
            // adjust scale
            final double scale = size[1] / 512d;
            // adjust offset
            final int w = (int) (size[0] - (slicerOffset[0] * scale));
            final int h = (int) (size[1] - (slicerOffset[1] * scale));

            // reset slicer camera for 1:1 ratio view
            slicerRenderer.ResetCamera();
            slicerRenderer.ResetCameraClippingRange();
            // zoom and translate
            zoomView(slicerCam, slicerRenderer, slicerScale * (slicerCam.GetDistance() / 12d));
            translateView(slicerCam, slicerRenderer, w, -h);
        }
        finally {
            unlock();
        }
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            // nothing to do
            if (fineRenderingTime == 0)
                ThreadUtil.sleep(1);
            else {
                // thread used for restoring fine rendering after a certain amount of time
                if (System.currentTimeMillis() >= fineRenderingTime) {
                    // set back quality rendering
                    setFineRendering();
                    // request repaint
                    repaint();
                    // done
                    fineRenderingTime = 0;
                }
                // wait until delay elapsed
                else
                    ThreadUtil.sleep(1);
            }
        }
    }

    @Override
    public void mouseEntered(final MouseEvent e) {
        // nothing to do here
    }

    @Override
    public void mouseExited(final MouseEvent e) {
        // nothing to do here
    }

    @Override
    public void mouseClicked(final MouseEvent e) {
        // nothing to do here
    }

    @Override
    public void mousePressed(final MouseEvent e) {
        // nothing to do here
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        // nothing to do here
    }

    @Override
    public void mouseMoved(final MouseEvent e) {
        // just save mouse position
        lastX = e.getX();
        lastY = e.getY();

        // update pick state for slicer first
        if (isSlicerEnable() && slicerPick(e))
            repaint();

        // slicer picked ? --> cannot pick other object
        if (isSlicerEnable() && isSlicerPicked())
            pickedObject = null;
        else
            // else get picked object (mouse move/drag event)
            pickedObject = pick(lastX, lastY);
    }

    @Override
    public void mouseDragged(final MouseEvent e) {
        // camera not yet defined --> exit
        if (cam == null)
            return;

        if (e.isConsumed())
            return;

        // get current mouse position
        final int x = e.getX();
        final int y = e.getY();
        final float[] scale = getCurrentSurfaceScale(new float[2]);
        int deltaX = (int)((lastX - x) * scale[0]);
        int deltaY = (int)((lastY - y) * scale[1]);

        // consume event
        e.consume();
        // want fast update
        setCoarseRendering();
        // abort current rendering
        rw.SetAbortRender(1);

        // slicer picked ? --> interact with slicer
        if (isSlicerEnable() && isSlicerPicked()) {
            // only accept left button action
            if (EventUtil.isLeftMouseButton(e) && !EventUtil.isShiftDown(e)) {
                // rotate mode ?
                if (slicerPickState == SlicerPickState.PICK_VECTOR)
                    // rotation mode
                    rotateSlicerView(deltaX, -deltaY);
                else
                    // slip plane adjustment
                    translateSlicerPlane(deltaX, deltaY);
            }
        }
        else {
            // faster movement with control modifier
            if (EventUtil.isControlDown(e)) {
                deltaX *= 3;
                deltaY *= 3;
            }

            if (EventUtil.isRightMouseButton(e) || (EventUtil.isLeftMouseButton(e) && EventUtil.isShiftDown(e)))
                // translation mode
                translateView(-deltaX * 2, deltaY * 2);
            else if (EventUtil.isMiddleMouseButton(e))
                // zoom mode
                zoomView(Math.pow(1.02, -deltaY));
            else
                // rotation mode
                rotateView(deltaX, -deltaY);
        }

        // save mouse position
        lastX = x;
        lastY = y;

        // request repaint
        repaint();
        // restore quality rendering in 1 second
        setFineRendering(1000);
    }

    @Override
    public void mouseWheelMoved(final MouseWheelEvent e) {
        // camera not yet defined --> exit
        if (cam == null)
            return;

        if (e.isConsumed())
            return;
        if (ren.VisibleActorCount() == 0)
            return;

        // consume event
        e.consume();

        // want fast update
        setCoarseRendering();
        // abort current rendering
        rw.SetAbortRender(1);

        // get delta
        double delta = e.getWheelRotation() * CanvasPreferences.getMouseWheelSensitivity();
        if (CanvasPreferences.getInvertMouseWheelAxis())
            delta = -delta;

        // faster movement with control modifier
        if (EventUtil.isControlDown(e))
            delta *= 3d;

        zoomView(Math.pow(1.02, delta));

        // request repaint
        repaint();

        // restore quality rendering in 1 second
        setFineRendering(1000);
    }

    @Override
    public void keyTyped(final KeyEvent e) {
        //
    }

    @Override
    public void keyPressed(final KeyEvent e) {
        if (e.isConsumed())
            return;
        if (ren.VisibleActorCount() == 0)
            return;

        final vtkActorCollection ac;
        vtkActor anActor;
        int i;

        switch (e.getKeyChar()) {
            case 'r': // reset camera
                resetCamera();
                repaint();
                // consume event
                e.consume();
                break;

            case 'w': // wireframe mode
                lock();
                try {
                    ac = ren.GetActors();
                    ac.InitTraversal();
                    for (i = 0; i < ac.GetNumberOfItems(); i++) {
                        anActor = ac.GetNextActor();
                        anActor.GetProperty().SetRepresentationToWireframe();
                    }
                }
                finally {
                    unlock();
                }
                repaint();
                // consume event
                e.consume();
                break;

            case 's':
                lock();
                try {
                    ac = ren.GetActors();
                    ac.InitTraversal();
                    for (i = 0; i < ac.GetNumberOfItems(); i++) {
                        anActor = ac.GetNextActor();
                        anActor.GetProperty().SetRepresentationToSurface();
                    }
                }
                finally {
                    unlock();
                }
                repaint();
                // consume event
                e.consume();
                break;
        }
    }

    @Override
    public void keyReleased(final KeyEvent e) {
    }
}
