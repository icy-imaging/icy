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

package icy.gui.menu;

import icy.action.RoiActions;
import icy.common.listener.ROIToolChangeListener;
import icy.gui.component.menu.IcyMenu;
import icy.gui.component.menu.IcyMenuItem;
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

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Thomas MUSSET
 */
public final class ApplicationMenuROI extends AbstractApplicationMenu {

    private static ApplicationMenuROI instance = null;

    public static synchronized ApplicationMenuROI getInstance() {
        if (instance == null)
            instance = new ApplicationMenuROI();

        return instance;
    }

    private static final Set<ROIToolChangeListener> listeners = new HashSet<>();

    private final IcyMenuItem item2DTo3D;
    private final IcyMenuItem item3DTo2D;
    private final IcyMenuItem itemToEllipse;
    private final IcyMenuItem itemToShape;
    private final IcyMenuItem itemToMask;
    private final IcyMenuItem itemSeparateComponent;
    private final IcyMenuItem itemSeparateWatershed;
    private final IcyMenuItem itemROICutter;
    private final IcyMenuItem itemDilate;
    private final IcyMenuItem itemErode;
    private final IcyMenuItem itemDistanceMap;
    private final IcyMenuItem itemOperationUnion;
    private final IcyMenuItem itemOperationIntersection;
    private final IcyMenuItem itemOperationInversion;
    private final IcyMenuItem itemOperationExclusiveUnion;
    private final IcyMenuItem itemOperationSubstraction;

    private final List<ROI> lastROISelection = new ArrayList<>();

    private ApplicationMenuROI() {
        super("Region of Interest");

        final IcyMenuItem itemLoadROI = new IcyMenuItem("Load ROI...");
        itemLoadROI.addActionListener(RoiActions.loadAction);
        add(itemLoadROI);

        final IcyMenuItem itemSaveROI = new IcyMenuItem("Save ROI As...");
        itemSaveROI.addActionListener(RoiActions.saveAction);
        add(itemSaveROI);

        final IcyMenuItem itemExportExcelROI = new IcyMenuItem("Export ROI As Excel...");
        itemExportExcelROI.addActionListener(RoiActions.xlsExportAction);
        add(itemExportExcelROI);

        addSeparator();

        final IcyMenu menuDraw2D = new IcyMenu("Draw 2D");
        add(menuDraw2D);

        final IcyMenuItem itemDraw2DPoint = new IcyMenuItem("Point");
        addPluginAction(itemDraw2DPoint, new ROI2DPointPlugin());
        menuDraw2D.add(itemDraw2DPoint);

        final IcyMenuItem itemDraw2DLine = new IcyMenuItem("Line");
        addPluginAction(itemDraw2DLine, new ROI2DLinePlugin());
        menuDraw2D.add(itemDraw2DLine);

        final IcyMenuItem itemDraw2DPolyline = new IcyMenuItem("Polyline");
        addPluginAction(itemDraw2DPolyline, new ROI2DPolyLinePlugin());
        menuDraw2D.add(itemDraw2DPolyline);

        menuDraw2D.addSeparator();

        final IcyMenuItem itemDraw2DRectangle = new IcyMenuItem("Rectangle");
        addPluginAction(itemDraw2DRectangle, new ROI2DRectanglePlugin());
        menuDraw2D.add(itemDraw2DRectangle);

        final IcyMenuItem itemDraw2DEllipse = new IcyMenuItem("Ellipse");
        addPluginAction(itemDraw2DEllipse, new ROI2DEllipsePlugin());
        menuDraw2D.add(itemDraw2DEllipse);

        final IcyMenuItem itemDraw2DPolygon = new IcyMenuItem("Polygon");
        addPluginAction(itemDraw2DPolygon, new ROI2DPolygonPlugin());
        menuDraw2D.add(itemDraw2DPolygon);

        menuDraw2D.addSeparator();

        final IcyMenuItem itemDraw2DArea = new IcyMenuItem("Area");
        addPluginAction(itemDraw2DArea, new ROI2DAreaPlugin());
        menuDraw2D.add(itemDraw2DArea);

        final IcyMenu menuDraw3D = new IcyMenu("Draw 3D");
        add(menuDraw3D);

        final IcyMenuItem itemDraw3DPoint = new IcyMenuItem("Dot");
        addPluginAction(itemDraw3DPoint, new ROI3DPointPlugin());
        menuDraw3D.add(itemDraw3DPoint);

        final IcyMenuItem itemDraw3DLine = new IcyMenuItem("Line");
        addPluginAction(itemDraw3DLine, new ROI3DLinePlugin());
        menuDraw3D.add(itemDraw3DLine);

        final IcyMenuItem itemDraw3DPolyline = new IcyMenuItem("Polyline");
        addPluginAction(itemDraw3DPolyline, new ROI3DPolyLinePlugin());
        menuDraw3D.add(itemDraw3DPolyline);

        addSeparator();

        final IcyMenuItem itemMagicWand = new IcyMenuItem("Magic Wand");
        addPluginAction(itemMagicWand, new ROIMagicWandPlugin());
        add(itemMagicWand);

        addSeparator();

        item2DTo3D = new IcyMenuItem("2D to 3D");
        item2DTo3D.addActionListener(RoiActions.convertTo3DAction);
        add(item2DTo3D);

        item3DTo2D = new IcyMenuItem("3D to 2D");
        item3DTo2D.addActionListener(RoiActions.convertTo2DAction);
        add(item3DTo2D);

        itemToEllipse = new IcyMenuItem("Convert to Ellipse");
        itemToEllipse.addActionListener(RoiActions.convertToEllipseAction);
        add(itemToEllipse);

        itemToShape = new IcyMenuItem("Convert to Shape");
        itemToShape.addActionListener(RoiActions.convertToShapeAction);
        add(itemToShape);

        itemToMask = new IcyMenuItem("Convert to Mask");
        itemToMask.addActionListener(RoiActions.convertToMaskAction);
        add(itemToMask);

        addSeparator();

        itemSeparateComponent = new IcyMenuItem("Separate Component");
        itemSeparateComponent.addActionListener(RoiActions.separateObjectsAction);
        add(itemSeparateComponent);

        itemSeparateWatershed = new IcyMenuItem("Separate by Watershed");
        itemSeparateWatershed.addActionListener(RoiActions.computeWatershedSeparation);
        add(itemSeparateWatershed);

        itemROICutter = new IcyMenuItem("ROI Cutter");
        addPluginAction(itemROICutter, new ROILineCutterPlugin());
        add(itemROICutter);

        addSeparator();

        itemDilate = new IcyMenuItem("Dilate");
        itemDilate.addActionListener(RoiActions.dilateObjectsAction);
        add(itemDilate);

        itemErode = new IcyMenuItem("Erode");
        itemErode.addActionListener(RoiActions.erodeObjectsAction);
        add(itemErode);

        itemDistanceMap = new IcyMenuItem("Distance Map");
        itemDistanceMap.addActionListener(RoiActions.computeDistanceMapAction);
        add(itemDistanceMap);

        addSeparator();

        itemOperationUnion = new IcyMenuItem("Union");
        itemOperationUnion.addActionListener(RoiActions.boolOrAction);
        add(itemOperationUnion);

        itemOperationIntersection = new IcyMenuItem("Intersection");
        itemOperationIntersection.addActionListener(RoiActions.boolAndAction);
        add(itemOperationIntersection);

        itemOperationInversion = new IcyMenuItem("Inversion");
        itemOperationInversion.addActionListener(RoiActions.boolNotAction);
        add(itemOperationInversion);

        itemOperationExclusiveUnion = new IcyMenuItem("Exclusive Union");
        itemOperationExclusiveUnion.addActionListener(RoiActions.boolXorAction);
        add(itemOperationExclusiveUnion);

        itemOperationSubstraction = new IcyMenuItem("Substraction");
        itemOperationSubstraction.addActionListener(RoiActions.boolSubtractAction);
        add(itemOperationSubstraction);

        addSeparator();

        // FIXME: 17/01/2023 change fill value
        final IcyMenuItem itemFillInterior = new IcyMenuItem("Fill Interior");
        itemFillInterior.addActionListener(RoiActions.fillInteriorAction);
        add(itemFillInterior);

        final  IcyMenuItem itemFillExterior = new IcyMenuItem("Fill Exterior");
        itemFillExterior.addActionListener(RoiActions.fillExteriorAction);
        add(itemFillExterior);

        reloadROIMenu();

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

    private void addPluginAction(final IcyMenuItem item, final PluginROI plugin) {
        if (item == null || plugin == null)
            return;
        addAction(item, e -> changeTool(plugin));
    }

    private void addAction(final IcyMenuItem item, final ActionListener listener) {
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
