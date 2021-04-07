package plugins.kernel.roi.tool;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import icy.canvas.Canvas2D;
import icy.canvas.IcyCanvas;
import icy.canvas.IcyCanvas3D;
import icy.gui.lut.LUTViewer;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.sequence.Sequence;
import icy.type.point.Point5D;
import icy.type.point.Point5D.Double;
import icy.util.EventUtil;
import icy.util.GraphicsUtil;
import icy.util.StringUtil;
import plugins.kernel.roi.roi2d.ROI2DArea;
import plugins.kernel.roi.tool.MagicWand.MagicWandSetting;

/**
 * ROI Magic Wand.<br>
 * Used to make Magic Wand interaction easier.<br>
 * Based on smooth tolerance control ideas from Jerome Mutterer.
 * 
 * @author Stephane
 * @author Jerome Mutterer
 */
public class ROIMagicWand extends ROI2DArea
{
    private class MagicWandProcess extends Thread
    {
        final Sequence sequence;
        final int x;
        final int y;
        final int z;
        final int t;
        final int channel;
        final boolean in3D;
        final MagicWandSetting settings;

        public MagicWandProcess(Sequence sequence, int x, int y, int z, int t, int channel, boolean in3D,
                MagicWandSetting mws)
        {
            super("Magic wand");

            this.sequence = sequence;
            this.x = x;
            this.y = y;
            this.z = z;
            this.t = t;
            this.channel = channel;
            this.in3D = in3D;
            settings = mws;

            start();
        }

        @Override
        public void run()
        {
            ROI roi;

            if (in3D)
                roi = MagicWand.doWand3D(sequence, x, y, z, t, channel, settings);
            else
                roi = MagicWand.doWand2D(sequence, x, y, z, t, channel, settings);

            magicWandDone(roi);
        }
    }

    public class ROIMagicWandPainter extends ROI2DAreaPainter
    {
        @Override
        public void mousePressed(MouseEvent e, Double imagePoint, IcyCanvas canvas)
        {
            // we need it
            if (imagePoint == null)
                return;
            // not left button click ? nothing to do..
            if (!EventUtil.isLeftMouseButton(e))
                return;

            start(imagePoint, e.getPoint(), canvas.getSequence(), canvas, EventUtil.isShiftDown(e));

            // consume event
            e.consume();
        }

        @Override
        public void mouseDrag(MouseEvent e, Double imagePoint, IcyCanvas canvas)
        {
            // we need it
            if (imagePoint == null)
                return;
            // not left button click ? nothing to do..
            if (!EventUtil.isLeftMouseButton(e))
                return;

            // currently in process ?
            if (inProcess)
            {
                adjustTolerances(e.getPoint(), canvas, EventUtil.isShiftDown(e));
                e.consume();
            }
        }

        @Override
        public void mouseReleased(MouseEvent e, Double imagePoint, IcyCanvas canvas)
        {
            // not left button click ? nothing to do..
            if (!EventUtil.isLeftMouseButton(e))
                return;

            // no more processing
            inProcess = false;
            // remove the ROI, we don't need it anymore...
            ROIMagicWand.this.remove(false);

            // we have a result ?
            if (result != null)
            {
                // add it to the sequence
                seq.addROI(result, true);
                // store it
                roiAdded = result;
            }
        }

        @Override
        public void paint(Graphics2D g, Sequence sequence, IcyCanvas canvas)
        {
            if (g == null)
                return;

            paintAnchor(g, canvas);

            // paint it
            if (result != null)
                result.getOverlay().paint(g, sequence, canvas);
        }

        void paintAnchor(Graphics2D g, IcyCanvas canvas)
        {
            final MagicWandSetting mws = lastSettings;
            if (mws == null)
                return;

            if (!(canvas instanceof Canvas2D))
                return;

            final Canvas2D cnv2d = (Canvas2D) canvas;
            final Graphics2D g2 = (Graphics2D) g.create();

            try
            {
                // canvas coordinate
                g2.transform(cnv2d.getInverseTransform());
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                final int size = 8;
                final int x = startMousePosition.x - ((size / 2) - 1);
                final int y = startMousePosition.y - ((size / 2) - 1);

                // draw anchor to the starting point
                g2.setColor(Color.darkGray);
                g2.fillOval(x - 1, y - 1, size + 2, size + 2);
                g2.setColor(Color.red);
                g2.fillOval(x, y, size, size);

                String text;

                if (mws.colorSensitivity > 0)
                    text = "Magic Wand - Color mode\n";
                else
                    text = "Magic Wand - Gray mode\n";
                text += "Value tolerance = " + StringUtil.toString(mws.valueTolerance, 2) + "\n";
                text += "Gradient tolerance = " + StringUtil.toString(mws.gradientTolerance, 2);

                // draw hint
                drawText(g2, cnv2d, text, 0.8f);
            }
            finally

            {
                g2.dispose();
            }
        }

        public void drawText(Graphics2D g, Canvas2D canvas2d, String text, float alpha)
        {
            final Rectangle2D rect = GraphicsUtil.getStringBounds(g, text);
            final int w = (int) rect.getWidth();
            final int h = (int) rect.getHeight();
            final int x = (int) (canvas2d.getWidth() - (rect.getWidth() + 20));
            final int y = (int) (canvas2d.getHeight() - (rect.getHeight() + 30));

            GraphicsUtil.drawHint(g, text, x, y, Color.lightGray, Color.black);
        }
    }

    // parameter
    boolean force3d;
    boolean doDebug;

    // internals
    MagicWandProcess processor;

    boolean inProcess;
    Point2D startImagePosition;
    Point startMousePosition;
    Sequence seq;

    double channelDelta;
    double startValueTolerance;
    double startGradientTolerance;
    double valueToleranceStep;
    double gradientToleranceStep;

    ROI result;
    ROI roiAdded;

    MagicWandSetting lastSettings;

    public ROIMagicWand(Point5D pt)
    {
        super(pt);

        force3d = false;

        lastSettings = null;
        processor = null;
        inProcess = false;
        result = null;
        roiAdded = null;
    }

    public ROIMagicWand()
    {
        this(new Point5D.Double());
    }
    
    @Override
    public String getDefaultName()
    {
        return "Magic Wand";
    }

    public void start(Double imagePoint, Point mousePoint, Sequence s, IcyCanvas canvas, boolean rgb)
    {
        // previous task not yet done
        if ((processor != null) && processor.isAlive())
            return;
        // nothing to do
        if (s == null)
            return;

        seq = s;

        // force 3D work on 3 canvas
        if (canvas instanceof IcyCanvas3D)
            force3d = true;

        // save starting position
        startImagePosition = imagePoint.toPoint2D();
        startMousePosition = new Point(mousePoint);

        // define Z, T and C positions
        z = force3d ? (int) imagePoint.getZ() : canvas.getPositionZ();
        t = canvas.getPositionT();

        updateSettings(canvas, rgb, true);
        // get settings
        lastSettings = getSettings(startValueTolerance, startGradientTolerance, (c == -1) ? 90 : -100);
        // create new Magic Wand task
        processor = new MagicWandProcess(seq, (int) imagePoint.getX(), (int) imagePoint.getY(), z, t, c, force3d,
                lastSettings);

        // start new process
        inProcess = true;
        result = null;

        // repaint
        getOverlay().painterChanged();
    }

    @Override
    protected ROI2DAreaPainter createPainter()
    {
        return new ROIMagicWandPainter();
    }

    public void setForce3d(boolean value)
    {
        force3d = value;
    }

    void updateSettings(IcyCanvas canvas, boolean rgb, boolean init)
    {
        // RGB mode
        if (rgb && (seq.getSizeC() == 3))
            c = -1;
        else
        {
            // use active channel tab on LUT viewer to define C position
            final LUTViewer lv = canvas.getViewer().getLutViewer();
            c = (lv != null) ? lv.getActiveChannelIndex() : 0;
        }

        // compute tolerances start/step from min and max
        final double channelBounds[] = (c == -1) ? seq.getChannelsGlobalBounds() : seq.getChannelBounds(c);
        channelDelta = channelBounds[1] - channelBounds[0];

        if (init)
        {
            startValueTolerance = (channelDelta > 0d) ? channelDelta / 10d : 1d;
//            startGradientTolerance = (channelDelta > 0d) ? channelDelta / 20d : 1d;
            startGradientTolerance = -10d;
        }
        valueToleranceStep = (channelDelta > 0d) ? channelDelta / 700d : 0d;
        gradientToleranceStep = (channelDelta > 0d) ? channelDelta / 1500d : 0d;
    }

    static MagicWandSetting getSettings(double valueTolerance, double gradientTolerance, double colorSensitivity)
    {
        final MagicWandSetting settings = new MagicWandSetting();

        settings.colorSensitivity = colorSensitivity;
        settings.connect4 = false;
        settings.valueTolerance = valueTolerance;
        settings.gradientTolerance = gradientTolerance;
        settings.includeHoles = false;

        return settings;
    }

    void adjustTolerances(Point mousePoint, IcyCanvas canvas, boolean rgb)
    {
        // interrupt previous process
        if (processor != null)
            processor.interrupt();

        updateSettings(canvas, rgb, false);

        // compute tolerances from distance from starting point * toleranceStep
        final double deltaX = mousePoint.getX() - startMousePosition.getX();
        final double deltaY = mousePoint.getY() - startMousePosition.getY();

        final double valueTolerance = startValueTolerance + (deltaX * valueToleranceStep);
        final double gradientTolerance = startGradientTolerance + (deltaY * gradientToleranceStep);

        // get settings
        lastSettings = getSettings(valueTolerance, gradientTolerance, (c == -1) ? 90 : -100);
        // create new Magic Wand task
        processor = new MagicWandProcess(seq, (int) startImagePosition.getX(), (int) startImagePosition.getY(), z, t, c,
                force3d, lastSettings);

        // repaint
        getOverlay().painterChanged();
    }

    void magicWandDone(ROI roi)
    {
        if (roi != null)
        {
            result = roi;
            // read only
            // result.setReadOnly(true);
            result.setSelected(false);

            // just need to repaint
            if (inProcess)
                getOverlay().painterChanged();
            // directly set result into the Sequence
            else if (seq != null)
            {
                // already added a ROI ? --> remove it
                if (roiAdded != null)
                    seq.removeROI(roiAdded, false);

                seq.addROI(result, true);
                roiAdded = result;
            }

            // so we all have stats directly from Magic Wand
            if (roi instanceof ROI2D)
                setAsBooleanMask(((ROI2D)roi).getBooleanMask(true));
        }
    }
}
