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

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLJPanel;
import icy.gui.dialog.IdConfirmDialog;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.system.IcyExceptionHandler;
import icy.system.IcyHandledException;
import icy.system.thread.ThreadUtil;
import icy.util.OpenGLUtil;
import icy.util.ReflectionUtil;
import jogamp.opengl.GLDrawableHelper;
import jogamp.opengl.GLOffscreenAutoDrawableImpl;
import jogamp.opengl.es3.GLES3Impl;
import vtk.*;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

// kind of custom vtkJoglPanelComponent;
public class VtkJoglPanel extends GLJPanel {
    class GLEventImpl implements GLEventListener {
        @Override
        public void init(final GLAutoDrawable drawable) {
            if (!windowset) {
                windowset = true;

                // Make sure the JOGL Context is current
                final GLContext ctx = drawable.getContext();
                if (!ctx.isCurrent())
                    ctx.makeCurrent();

                // Init VTK OpenGL RenderWindow
                rw.SetMapped(1);
                rw.SetPosition(0, 0);
                setSize(drawable.getSurfaceWidth(), drawable.getSurfaceHeight());
                rw.InitializeFromCurrentContext();
                // rw.OpenGLInit();

                // init light
                if (!lightingset) {
                    lightingset = true;
                    ren.AddLight(lgt);
                    lgt.SetPosition(cam.GetPosition());
                    lgt.SetFocalPoint(cam.GetFocalPoint());
                }
            }
        }

        @Override
        public void reshape(final GLAutoDrawable drawable, final int x, final int y, final int width, final int height) {
            setSize(width, height);
        }

        @Override
        public void display(final GLAutoDrawable drawable) {
            render();
        }

        @Override
        public void dispose(final GLAutoDrawable drawable) {
            delete();
        }
    }

    protected vtkGenericOpenGLRenderWindow rw;
    protected vtkRenderer ren;
    protected vtkRenderWindowInteractor wi;
    protected vtkCamera cam;
    protected vtkLight lgt;

    protected ReentrantLock lock;
    GLEventImpl glEventImpl;

    protected int lastX;
    protected int lastY;
    protected boolean windowset;
    protected boolean lightingset;
    protected int interactionMode;
    protected boolean rendering;
    private boolean failed;

    public VtkJoglPanel() {
        super(new GLCapabilities(GLProfile.getMaximum(true)));

        final float[] scales = new float[2];

        // check that requested pixel scale is ok (can be 0 on OSX)
        getRequestedSurfaceScale(scales);
        System.out.println("GLPanel.getRequestedSurfaceScale = " + Arrays.toString(scales));

        getCurrentSurfaceScale(scales);
        System.out.println("GLPanel.getCurrentSurfaceScale = " + Arrays.toString(scales));

        // scale = 0 ? --> set to 1
        /*if ((scales[0] == 0f) || (scales[1] == 0f))
        {
            try
            {
                final float[] reqPixelScale = (float[]) ReflectionUtil.getFieldObject(this, "reqPixelScale", true);
                reqPixelScale[0] = 1f;
                reqPixelScale[1] = 1f;
            }
            catch (Throwable t)
            {
                System.err.println("Couldn't patch GLJPanel.reqPixelScale[] field with Java 17, try using Java 8 instead if you experience some bugs with 3D VTK view.");
            }
        }*/
        /*if ((scales[0] == 0f) || (scales[1] == 0f))
        {
            try
            {
                final float[] hasPixelScale = (float[]) ReflectionUtil.getFieldObject(this, "hasPixelScale", true);
                hasPixelScale[0] = 1f;
                hasPixelScale[1] = 1f;
            }
            catch (final Throwable t)
            {
                System.err.println("Couldn't patch GLJPanel.hasPixelScale[] field with Java 17, try using Java 8 instead if you experience some bugs with 3D VTK view.");
            }
        }*/

        rw = new vtkGenericOpenGLRenderWindow();

        // init render window
        rw.SetIsDirect(1);
        rw.SetSupportsOpenGL(1);
        rw.SetIsCurrent(true);

        // FIXME: smoothing is broken since VTK 6.3
        rw.SetPointSmoothing(1);
        rw.SetLineSmoothing(1);
        rw.SetPolygonSmoothing(1);
        // for use with deeph peeling (allow transparent geometry to intersect with volume)
        rw.SetAlphaBitPlanes(1);
        rw.SetMultiSamples(0);
//        rw.SetMultiSamples(4);

        // Make sure when VTK internally request a Render, the Render get properly triggered
        rw.AddObserver("WindowFrameEvent", this, "render");

        // init window interactor
        wi = new vtkGenericRenderWindowInteractor();
        wi.SetRenderWindow(rw);
        wi.ConfigureEvent();

        // Make sure when VTK internally request a Render, the Render get properly triggered
        wi.AddObserver("RenderEvent", this, "render");
        wi.SetEnableRender(false);

        ren = new vtkRenderer();
        ren.SetLightFollowCamera(1);
        ren.UseDepthPeelingOn();
        ren.UseDepthPeelingForVolumesOn();

        cam = null;

        lgt = new vtkLight();
        // set ambient color to white
        lgt.SetAmbientColor(1d, 1d, 1d);

        lock = new ReentrantLock();
        glEventImpl = new GLEventImpl();

        windowset = false;
        lightingset = false;
        rendering = false;
        failed = false;

        addGLEventListener(glEventImpl);

        rw.AddRenderer(ren);
        cam = ren.GetActiveCamera();

        // not compatible with OpenGL 3 ? (new VTK OpenGL backend require OpenGL 3.2)
        if (!OpenGLUtil.isOpenGLSupported(3)) {
            if (!IdConfirmDialog.confirm("Warning",
                    "Your graphics card driver does not support OpenGL 3, you may experience issues or crashes with VTK.\nDo you want to try anyway ?",
                    IdConfirmDialog.YES_NO_OPTION, getClass().getName() + ".notCompatibleDialog"))
                throw new IcyHandledException("Your graphics card driver is not compatible with OpenGL 3 !");
        }
    }

    protected void delete() {
        if (rendering) {
            rw.SetAbortRender(1);
            // wait a bit while rendering
            ThreadUtil.sleep(500);
            // still rendering --> exit
            if (rendering)
                return;
        }

        lock.lock();
        try {
            // prevent any further rendering
            rendering = true;

            rw.RemoveAllObservers();
            wi.RemoveAllObservers();

            // if (getParent() != null)
            // getParent().remove(this);

            // release internal VTK objects
            ren = null;
            cam = null;
            lgt = null;
            wi = null;

            // On linux we prefer to have a memory leak instead of a crash
            if (!rw.GetClassName().equals("vtkXOpenGLRenderWindow")) {
                rw = null;
            }
            else {
                System.out.println("The renderwindow has been kept arount to prevent a crash");
            }

            // call it only once in parent as this can take a lot of time
            // vtkObjectBase.JAVA_OBJECT_MANAGER.gc(false);
        }
        finally {
            // removing the renderWindow is let to the superclass
            // because in the very special case of an AWT component
            // under Linux, destroying renderWindow crashes.
            lock.unlock();
        }
    }

    /**
     * Disable method, use {@link #disposeInternal()} instead to release VTK and OpenGL resources
     */
    @Override
    protected void dispose(final Runnable runnable) {
        // prevent disposal on removeNotify as window externalization produce remove/add operation.
        // --> don't forget to call disposeInternal when needed
    }

    /**
     * Release VTK and OGL objects.<br>
     * Call it when you know you won't use anymore the VTK OGL panel
     */
    public void disposeInternal() {
        super.dispose(null);

        // remove the GL event listener to avoid memory leak
        removeGLEventListener(glEventImpl);

        try {
            // hacky fix to avoid the infamous memory leak from ThreadLocal from GLPanel !
            final GLDrawableHelper helper = (GLDrawableHelper) ReflectionUtil.getFieldObject(this, "helper", true);
            final ThreadLocal threadLocal = (ThreadLocal) ReflectionUtil.getFieldObject(helper, "perThreadInitAction", true);
            threadLocal.remove();
        }
        catch (Throwable t) {
            // ignore
        }
    }

    public vtkRenderer getRenderer() {
        return ren;
    }

    public vtkRenderWindow getRenderWindow() {
        return rw;
    }

    public vtkCamera getCamera() {
        return cam;
    }

    public vtkLight getLight() {
        return lgt;
    }

    public vtkRenderWindowInteractor getInteractor() {
        return wi;
    }

    /**
     * return true if currently rendering
     */
    public boolean isRendering() {
        return rendering;
    }

    @Override
    public void setBounds(final int x, final int y, final int width, final int height) {
        super.setBounds(x, y, width, height);

        if (windowset) {
            final int[] size = rw.GetSize();

            // set size only if needed
            if ((size[0] != width) || (size[1] != height)) {
                lock();
                try {
                    wi.SetSize(width, height);
                    rw.SetSize(width, height);
                    sizeChanged();
                }
                finally {
                    unlock();
                }
            }
        }
    }

    /**
     * Called when window render size changed (helper for this specific event)
     */
    public void sizeChanged() {
        // nothing here but can be overridden
    }

    /**
     * Do rendering
     */
    public void render() {
        if (rendering)
            return;

        rendering = true;
        lock();
        try {
            rw.Render();
        }
        finally {
            unlock();
            rendering = false;
        }
    }

    public boolean isWindowSet() {
        return windowset;
    }

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public void updateLight() {
        lgt.SetPosition(cam.GetPosition());
        lgt.SetFocalPoint(cam.GetFocalPoint());
    }

    public void resetCameraClippingRange() {
        lock();
        try {
            ren.ResetCameraClippingRange();
        }
        finally {
            unlock();
        }
    }

    public void resetCamera() {
        lock();
        try {
            ren.ResetCamera();
        }
        finally {
            unlock();
        }
    }

    @Override
    public void paint(final Graphics g) {
        // previous failed --> do nothing now
        if (failed)
            return;

        try {
            super.paint(g);
        }
        catch (final Throwable t) {
            // it can happen with older video cards
            failed = true;

            new FailedAnnounceFrame("An error occured while initializing OpenGL !\n"
                    + "You may try to update your graphics card driver to fix this issue.", 0);

            IcyExceptionHandler.handleException(t, true);
        }
    }
}
