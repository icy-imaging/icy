package icy.vtk;

import java.awt.Graphics;
import java.util.concurrent.locks.ReentrantLock;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLContext;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;

import icy.gui.dialog.IdConfirmDialog;
import icy.gui.frame.progress.FailedAnnounceFrame;
import icy.system.IcyExceptionHandler;
import icy.system.IcyHandledException;
import icy.system.thread.ThreadUtil;
import icy.util.OpenGLUtil;
import icy.util.ReflectionUtil;
import jogamp.opengl.GLDrawableHelper;
import vtk.vtkCamera;
import vtk.vtkGenericOpenGLRenderWindow;
import vtk.vtkGenericRenderWindowInteractor;
import vtk.vtkLight;
import vtk.vtkRenderWindow;
import vtk.vtkRenderWindowInteractor;
import vtk.vtkRenderer;

// kind of custom vtkJoglPanelComponent;
public class VtkJoglPanel extends GLJPanel
{
    class GLEventImpl implements GLEventListener
    {
        @Override
        public void init(GLAutoDrawable drawable)
        {
            if (!windowset)
            {
                windowset = true;

                // Make sure the JOGL Context is current
                GLContext ctx = drawable.getContext();
                if (!ctx.isCurrent())
                    ctx.makeCurrent();

                // Init VTK OpenGL RenderWindow
                rw.SetMapped(1);
                rw.SetPosition(0, 0);
                rw.InitializeFromCurrentContext();
                // rw.OpenGLInit();

                // init light
                if (!lightingset)
                {
                    lightingset = true;
                    ren.AddLight(lgt);
                    lgt.SetPosition(cam.GetPosition());
                    lgt.SetFocalPoint(cam.GetFocalPoint());
                }
            }
        }

        @Override
        public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
        {
            if (!windowset) return;
            
            lock();
            try
            {
                // resize renderWindow and windowInteractor
                rw.SetSize(width, height);
                wi.SetSize(width, height);
                sizeChanged();
            }
            finally
            {
                unlock();
            }

//            float[] scale = new float[2];
//            getCurrentSurfaceScale(scale);
//            
//            System.out.println("scale = " + scale[0] + ", " + scale[1]);
//            System.out.println("reshape(" + x + ", " + y + ", " + width + ", " + height + ")");
        }

        @Override
        public void display(GLAutoDrawable drawable)
        {
            render();
        }

        @Override
        public void dispose(GLAutoDrawable drawable)
        {
            delete();
        }
    }

    /**
     * 
     */
    private static final long serialVersionUID = 8821516677188995191L;

    protected vtkGenericOpenGLRenderWindow rw;
    protected vtkRenderer ren;
    protected vtkRenderWindowInteractor wi;
    protected vtkCamera cam;
    protected vtkLight lgt;

    protected ReentrantLock lock;
    protected GLEventImpl glEventImpl;

    protected int lastX;
    protected int lastY;
    protected boolean windowset;
    protected boolean lightingset;
    protected int interactionMode;
    protected boolean rendering;
    private boolean failed;

    public VtkJoglPanel()
    {
        super(new GLCapabilities(GLProfile.getMaximum(true)));

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
        if (!OpenGLUtil.isOpenGLSupported(3))
        {
            if (!IdConfirmDialog.confirm("Warning",
                    "Your graphics card driver does not support OpenGL 3, you may experience issues or crashes with VTK.\nDo you want to try anyway ?",
                    IdConfirmDialog.YES_NO_OPTION, getClass().getName() + ".notCompatibleDialog"))
                throw new IcyHandledException("Your graphics card driver is not compatible with OpenGL 3 !");
        }
    }

    protected void delete()
    {
        if (rendering)
        {
            rw.SetAbortRender(1);
            // wait a bit while rendering
            ThreadUtil.sleep(500);
            // still rendering --> exit
            if (rendering)
                return;
        }

        lock.lock();
        try
        {
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
            if (!rw.GetClassName().equals("vtkXOpenGLRenderWindow"))
            {
                rw = null;
            }
            else
            {
                System.out.println("The renderwindow has been kept arount to prevent a crash");
            }

            // call it only once in parent as this can take a lot of time
            // vtkObjectBase.JAVA_OBJECT_MANAGER.gc(false);
        }
        finally
        {
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
    protected void dispose(Runnable runnable)
    {
        // prevent disposal on removeNotify as window externalization produce remove/add operation.
        // --> don't forget to call disposeInternal when needed
    }

    /**
     * Release VTK and OGL objects.<br>
     * Call it when you know you won't use anymore the VTK OGL panel
     */
    public void disposeInternal()
    {
        super.dispose(null);

        // remove the GL event listener to avoid memory leak
        removeGLEventListener(glEventImpl);

        try
        {
            // hacky fix to avoid the infamous memory leak from ThreadLocal from GLPanel !
            final GLDrawableHelper helper = (GLDrawableHelper) ReflectionUtil.getFieldObject(this, "helper", true);
            final ThreadLocal threadLocal = (ThreadLocal) ReflectionUtil.getFieldObject(helper, "perThreadInitAction",
                    true);
            threadLocal.remove();
        }
        catch (Throwable t)
        {
            // ignore
        }
    }

    public vtkRenderer getRenderer()
    {
        return ren;
    }

    public vtkRenderWindow getRenderWindow()
    {
        return rw;
    }

    public vtkCamera getCamera()
    {
        return cam;
    }

    public vtkLight getLight()
    {
        return lgt;
    }

    public vtkRenderWindowInteractor getInteractor()
    {
        return wi;
    }

    /**
     * return true if currently rendering
     */
    public boolean isRendering()
    {
        return rendering;
    }

    /**
     * Called when window render size changed (helper for this specific event)
     */
    public void sizeChanged()
    {
        // nothing here but can be overridden
    }

    /**
     * Do rendering
     */
    public void render()
    {
        if (rendering)
            return;

        rendering = true;
        lock();
        try
        {
            rw.Render();
        }
        finally
        {
            unlock();
            rendering = false;
        }
    }

    public boolean isWindowSet()
    {
        return windowset;
    }

    public void lock()
    {
        lock.lock();
    }

    public void unlock()
    {
        lock.unlock();
    }

    public void updateLight()
    {
        lgt.SetPosition(cam.GetPosition());
        lgt.SetFocalPoint(cam.GetFocalPoint());
    }

    public void resetCameraClippingRange()
    {
        lock();
        try
        {
            ren.ResetCameraClippingRange();
        }
        finally
        {
            unlock();
        }
    }

    public void resetCamera()
    {
        lock();
        try
        {
            ren.ResetCamera();
        }
        finally
        {
            unlock();
        }
    }

    @Override
    public void paint(Graphics g)
    {
        // previous failed --> do nothing now
        if (failed)
            return;

        try
        {
            super.paint(g);
        }
        catch (Throwable t)
        {
            // it can happen with older video cards
            failed = true;

            new FailedAnnounceFrame("An error occured while initializing OpenGL !\n"
                    + "You may try to update your graphics card driver to fix this issue.", 0);

            IcyExceptionHandler.handleException(t, true);
        }
    }
}
