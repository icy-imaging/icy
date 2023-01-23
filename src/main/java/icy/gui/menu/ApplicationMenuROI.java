package icy.gui.menu;

import icy.action.RoiActions;
import icy.common.listener.ROIToolChangeListener;
import icy.main.Icy;
import icy.plugin.interface_.PluginROI;
import icy.roi.ROI;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.system.thread.ThreadUtil;
import plugins.kernel.roi.roi2d.plugin.ROI2DAreaPlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DPointPlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DLinePlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DPolyLinePlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DRectanglePlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DEllipsePlugin;
import plugins.kernel.roi.roi2d.plugin.ROI2DPolygonPlugin;
import plugins.kernel.roi.roi3d.plugin.ROI3DLinePlugin;
import plugins.kernel.roi.roi3d.plugin.ROI3DPointPlugin;
import plugins.kernel.roi.roi3d.plugin.ROI3DPolyLinePlugin;
import plugins.kernel.roi.tool.plugin.ROILineCutterPlugin;
import plugins.kernel.roi.tool.plugin.ROIMagicWandPlugin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ApplicationMenuROI extends AbstractApplicationMenu {
    private static final Set<ROIToolChangeListener> listeners = new HashSet<>();

    private final JMenu menuDraw2D;
    private final JMenu menuDraw3D;

    private final JMenuItem itemLoadROI;
    private final JMenuItem itemSaveROI;
    private final JMenuItem itemExportExcelROI;
    private final JMenuItem itemDraw2DArea;
    private final JMenuItem itemDraw2DPoint;
    private final JMenuItem itemDraw2DLine;
    private final JMenuItem itemDraw2DPolyline;
    private final JMenuItem itemDraw2DRectangle;
    private final JMenuItem itemDraw2DEllipse;
    private final JMenuItem itemDraw2DPolygon;
    private final JMenuItem itemDraw3DPoint;
    private final JMenuItem itemDraw3DLine;
    private final JMenuItem itemDraw3DPolyline;
    private final JMenuItem itemMagicWand;
    private final JMenuItem item2DTo3D;
    private final JMenuItem item3DTo2D;
    private final JMenuItem itemToEllipse;
    private final JMenuItem itemToShape;
    private final JMenuItem itemToMask;
    private final JMenuItem itemSeparateComponent;
    private final JMenuItem itemSeparateWatershed;
    private final JMenuItem itemROICutter;
    private final JMenuItem itemDilate;
    private final JMenuItem itemErode;
    private final JMenuItem itemDistanceMap;
    private final JMenuItem itemOperationUnion;
    private final JMenuItem itemOperationIntersection;
    private final JMenuItem itemOperationInversion;
    private final JMenuItem itemOperationExclusiveUnion;
    private final JMenuItem itemOperationSubstraction;
    private final JMenuItem itemFillInterior;
    private final JMenuItem itemFillExterior;

    private final List<ROI> lastROISelection = new ArrayList<>();

    public ApplicationMenuROI() {
        super("Region of Interest");

        itemLoadROI = new JMenuItem("Load ROI...");
        itemLoadROI.addActionListener(RoiActions.loadAction);
        add(itemLoadROI);

        itemSaveROI = new JMenuItem("Save ROI As...");
        itemSaveROI.addActionListener(RoiActions.saveAction);
        add(itemSaveROI);

        itemExportExcelROI = new JMenuItem("Export ROI As Excel...");
        itemExportExcelROI.addActionListener(RoiActions.xlsExportAction);
        add(itemExportExcelROI);

        addSeparator();

        menuDraw2D = new JMenu("Draw 2D");
        add(menuDraw2D);

        itemDraw2DPoint = new JMenuItem("Point");
        addPluginAction(itemDraw2DPoint, new ROI2DPointPlugin());
        menuDraw2D.add(itemDraw2DPoint);

        itemDraw2DLine = new JMenuItem("Line");
        addPluginAction(itemDraw2DLine, new ROI2DLinePlugin());
        menuDraw2D.add(itemDraw2DLine);

        itemDraw2DPolyline = new JMenuItem("Polyline");
        addPluginAction(itemDraw2DPolyline, new ROI2DPolyLinePlugin());
        menuDraw2D.add(itemDraw2DPolyline);

        menuDraw2D.addSeparator();

        itemDraw2DRectangle = new JMenuItem("Rectangle");
        addPluginAction(itemDraw2DRectangle, new ROI2DRectanglePlugin());
        menuDraw2D.add(itemDraw2DRectangle);

        itemDraw2DEllipse = new JMenuItem("Ellipse");
        addPluginAction(itemDraw2DEllipse, new ROI2DEllipsePlugin());
        menuDraw2D.add(itemDraw2DEllipse);

        itemDraw2DPolygon = new JMenuItem("Polygon");
        addPluginAction(itemDraw2DPolygon, new ROI2DPolygonPlugin());
        menuDraw2D.add(itemDraw2DPolygon);

        menuDraw2D.addSeparator();

        itemDraw2DArea = new JMenuItem("Area");
        addPluginAction(itemDraw2DArea, new ROI2DAreaPlugin());
        menuDraw2D.add(itemDraw2DArea);

        menuDraw3D = new JMenu("Draw 3D");
        add(menuDraw3D);

        itemDraw3DPoint = new JMenuItem("Dot");
        addPluginAction(itemDraw3DPoint, new ROI3DPointPlugin());
        menuDraw3D.add(itemDraw3DPoint);

        itemDraw3DLine = new JMenuItem("Line");
        addPluginAction(itemDraw3DLine, new ROI3DLinePlugin());
        menuDraw3D.add(itemDraw3DLine);

        itemDraw3DPolyline = new JMenuItem("Polyline");
        addPluginAction(itemDraw3DPolyline, new ROI3DPolyLinePlugin());
        menuDraw3D.add(itemDraw3DPolyline);

        addSeparator();

        itemMagicWand = new JMenuItem("Magic Wand");
        addPluginAction(itemMagicWand, new ROIMagicWandPlugin());
        add(itemMagicWand);

        addSeparator();

        item2DTo3D = new JMenuItem("2D to 3D");
        item2DTo3D.addActionListener(RoiActions.convertTo3DAction);
        add(item2DTo3D);

        item3DTo2D = new JMenuItem("3D to 2D");
        item3DTo2D.addActionListener(RoiActions.convertTo2DAction);
        add(item3DTo2D);

        itemToEllipse = new JMenuItem("Convert to Ellipse");
        itemToEllipse.addActionListener(RoiActions.convertToEllipseAction);
        add(itemToEllipse);

        itemToShape = new JMenuItem("Convert to Shape");
        itemToShape.addActionListener(RoiActions.convertToShapeAction);
        add(itemToShape);

        itemToMask = new JMenuItem("Convert to Mask");
        itemToMask.addActionListener(RoiActions.convertToMaskAction);
        add(itemToMask);

        addSeparator();

        itemSeparateComponent = new JMenuItem("Separate Component");
        itemSeparateComponent.addActionListener(RoiActions.separateObjectsAction);
        add(itemSeparateComponent);

        itemSeparateWatershed = new JMenuItem("Separate by Watershed");
        itemSeparateWatershed.addActionListener(RoiActions.computeWatershedSeparation);
        add(itemSeparateWatershed);

        itemROICutter = new JMenuItem("ROI Cutter");
        addPluginAction(itemROICutter, new ROILineCutterPlugin());
        add(itemROICutter);

        addSeparator();

        itemDilate = new JMenuItem("Dilate");
        itemDilate.addActionListener(RoiActions.dilateObjectsAction);
        add(itemDilate);

        itemErode = new JMenuItem("Erode");
        itemErode.addActionListener(RoiActions.erodeObjectsAction);
        add(itemErode);

        itemDistanceMap = new JMenuItem("Distance Map");
        itemDistanceMap.addActionListener(RoiActions.computeDistanceMapAction);
        add(itemDistanceMap);

        addSeparator();

        itemOperationUnion = new JMenuItem("Union");
        itemOperationUnion.addActionListener(RoiActions.boolOrAction);
        add(itemOperationUnion);

        itemOperationIntersection = new JMenuItem("Intersection");
        itemOperationIntersection.addActionListener(RoiActions.boolAndAction);
        add(itemOperationIntersection);

        itemOperationInversion = new JMenuItem("Inversion");
        itemOperationInversion.addActionListener(RoiActions.boolNotAction);
        add(itemOperationInversion);

        itemOperationExclusiveUnion = new JMenuItem("Exclusive Union");
        itemOperationExclusiveUnion.addActionListener(RoiActions.boolXorAction);
        add(itemOperationExclusiveUnion);

        itemOperationSubstraction = new JMenuItem("Substraction");
        itemOperationSubstraction.addActionListener(RoiActions.boolSubtractAction);
        add(itemOperationSubstraction);

        addSeparator();

        // FIXME: 17/01/2023 change fill value
        itemFillInterior = new JMenuItem("Fill Interior");
        itemFillInterior.addActionListener(RoiActions.fillInteriorAction);
        add(itemFillInterior);

        itemFillExterior = new JMenuItem("Fill Exterior");
        itemFillExterior.addActionListener(RoiActions.fillExteriorAction);
        add(itemFillExterior);

        reloadROIMenu();

        // TODO: 19/01/2023 Add SkinChangeListener when icons are set
        // addSkinChangeListener();
        addActiveSequenceListener();
        addGlobalROIListener();
    }

    private void reloadROIMenu() {
        ThreadUtil.invokeLater(() -> {
            final Sequence active = Icy.getMainInterface().getActiveSequence();

            if (active != null) {
                final List<ROI> rois = active.getSelectedROIs();

                // Do nothing if last selected ROIs are the same than actual selected ROIs (prevent useless processes)
                if (!lastROISelection.isEmpty() && lastROISelection.equals(rois))
                    return;

                for (final Component c : getMenuComponents())
                    c.setEnabled(true);

                // First of all, clear last saved ROIs list
                lastROISelection.clear();

                if (rois.isEmpty()) {
                    item2DTo3D.setEnabled(false);
                    item3DTo2D.setEnabled(false);
                    itemToEllipse.setEnabled(false);
                    itemToShape.setEnabled(false);
                    itemToMask.setEnabled(false);
                    itemSeparateComponent.setEnabled(false);
                    itemSeparateWatershed.setEnabled(false);
                    itemROICutter.setEnabled(false);
                    itemDilate.setEnabled(false);
                    itemErode.setEnabled(false);
                    itemDistanceMap.setEnabled(false);
                    itemOperationUnion.setEnabled(false);
                    itemOperationIntersection.setEnabled(false);
                    itemOperationInversion.setEnabled(false);
                    itemOperationExclusiveUnion.setEnabled(false);
                    itemOperationSubstraction.setEnabled(false);

                    return;
                }

                // Set last selected ROIs list to actual list
                lastROISelection.addAll(rois);

                boolean contains2D = false;
                boolean contains3D = false;
                boolean containsEllipse = false;
                boolean containsShape = false;

                for (ROI roi : rois) {
                    if (roi.getDimension() == 2)
                        contains2D = true;
                    if (roi.getDimension() == 3)
                        contains3D = true;

                    // TODO: 23/01/2023 Check if can be converted to shape / ellipse / mask
                }

                item2DTo3D.setEnabled(contains2D);
                item3DTo2D.setEnabled(contains3D);
                itemToEllipse.setEnabled(containsShape);
                itemToShape.setEnabled(containsEllipse);



                // TODO: 23/01/2023 Continue reload function
            }
            else {
                // Clear last saved ROIs list
                lastROISelection.clear();

                // Disable all items
                for (final Component c : getMenuComponents())
                    c.setEnabled(false);
            }
        });
    }

    private void addPluginAction(final JMenuItem item, final PluginROI plugin) {
        if (item == null || plugin == null)
            return;
        addAction(item, e -> changeTool(plugin));
    }

    private void addAction(final JMenuItem item, final ActionListener listener) {
        if (item == null)
            return;
        for (ActionListener al : item.getActionListeners())
            item.removeActionListener(al);
        if (listener != null)
            item.addActionListener(listener);
    }

    // TODO: 23/01/2023 Should be global
    private void changeTool(PluginROI plugin) {
        if (plugin == null)
            return;
        for (ROIToolChangeListener listener : listeners)
            listener.toolChanged(plugin.getROIClassName());
    }

    // TODO: 23/01/2023 Should be global listener
    public static void addListener(ROIToolChangeListener listener) {
        listeners.add(listener);
    }

    // TODO: 23/01/2023 Should be global listener
    public static void removeListener(ROIToolChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void sequenceActivated(Sequence sequence) {
        reloadROIMenu();
    }

    @Override
    public void activeSequenceChanged(SequenceEvent event) {
        reloadROIMenu();
    }

    @Override
    public void roiAdded(ROI roi) {
        reloadROIMenu();
    }

    @Override
    public void roiRemoved(ROI roi) {
        reloadROIMenu();
    }
}
