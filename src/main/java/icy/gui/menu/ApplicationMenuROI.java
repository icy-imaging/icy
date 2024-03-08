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

package icy.gui.menu;

import icy.action.RoiActions;
import icy.common.listener.ROIToolChangeListener;
import icy.gui.component.menu.IcyMenu;
import icy.gui.component.menu.IcyMenuItem;
import icy.main.Icy;
import icy.plugin.interface_.PluginROI;
import icy.resource.icon.SVGIcon;
import icy.sequence.Sequence;
import icy.sequence.SequenceEvent;
import icy.system.thread.ThreadUtil;
import org.jetbrains.annotations.NotNull;
import plugins.kernel.roi.roi2d.plugin.*;
import plugins.kernel.roi.roi3d.plugin.ROI3DLinePlugin;
import plugins.kernel.roi.roi3d.plugin.ROI3DPointPlugin;
import plugins.kernel.roi.roi3d.plugin.ROI3DPolyLinePlugin;
import plugins.kernel.roi.tool.plugin.ROILineCutterPlugin;
import plugins.kernel.roi.tool.plugin.ROIMagicWandPlugin;

import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Thomas Musset
 */
public final class ApplicationMenuROI extends AbstractApplicationMenu {
    private static ApplicationMenuROI instance = null;

    public static synchronized ApplicationMenuROI getInstance() {
        if (instance == null)
            instance = new ApplicationMenuROI();

        return instance;
    }

    private static final Set<ROIToolChangeListener> listeners = new HashSet<>();

    private final IcyMenu menuDraw2D;
    private final IcyMenu menuDraw3D;

    private final IcyMenuItem itemMagicWand;
    private final IcyMenuItem itemROICutter;

    private ApplicationMenuROI() {
        super("Region of Interest");

        final IcyMenuItem itemLoadROI = new IcyMenuItem(RoiActions.loadAction, SVGIcon.FILE_OPEN);
        add(itemLoadROI);

        final IcyMenuItem itemSaveROI = new IcyMenuItem(RoiActions.saveAction, SVGIcon.SAVE_AS);
        add(itemSaveROI);

        final IcyMenuItem itemExportExcelROI = new IcyMenuItem(RoiActions.xlsExportAction, SVGIcon.EXPORT_NOTES);
        add(itemExportExcelROI);

        addSeparator();

        menuDraw2D = new IcyMenu("Draw 2D", SVGIcon.DRAW_ABSTRACT);
        add(menuDraw2D);

        // FIXME Replace all broken SVG icons
        final IcyMenuItem itemDraw2DPoint = new IcyMenuItem("Point", SVGIcon.BROKEN_IMAGE);
        addPluginAction(itemDraw2DPoint, new ROI2DPointPlugin());
        menuDraw2D.add(itemDraw2DPoint);

        final IcyMenuItem itemDraw2DLine = new IcyMenuItem("Line", SVGIcon.BROKEN_IMAGE);
        addPluginAction(itemDraw2DLine, new ROI2DLinePlugin());
        menuDraw2D.add(itemDraw2DLine);

        final IcyMenuItem itemDraw2DPolyline = new IcyMenuItem("Polyline", SVGIcon.TIMELINE);
        addPluginAction(itemDraw2DPolyline, new ROI2DPolyLinePlugin());
        menuDraw2D.add(itemDraw2DPolyline);

        menuDraw2D.addSeparator();

        final IcyMenuItem itemDraw2DRectangle = new IcyMenuItem("Rectangle", SVGIcon.RECTANGLE);
        addPluginAction(itemDraw2DRectangle, new ROI2DRectanglePlugin());
        menuDraw2D.add(itemDraw2DRectangle);

        final IcyMenuItem itemDraw2DEllipse = new IcyMenuItem("Ellipse", SVGIcon.CIRCLE);
        addPluginAction(itemDraw2DEllipse, new ROI2DEllipsePlugin());
        menuDraw2D.add(itemDraw2DEllipse);

        final IcyMenuItem itemDraw2DPolygon = new IcyMenuItem("Polygon", SVGIcon.PENTAGON);
        addPluginAction(itemDraw2DPolygon, new ROI2DPolygonPlugin());
        menuDraw2D.add(itemDraw2DPolygon);

        menuDraw2D.addSeparator();

        final IcyMenuItem itemDraw2DArea = new IcyMenuItem("Area", SVGIcon.STROKE_FULL);
        addPluginAction(itemDraw2DArea, new ROI2DAreaPlugin());
        menuDraw2D.add(itemDraw2DArea);

        menuDraw3D = new IcyMenu("Draw 3D", SVGIcon.DEPLOYED_CODE);
        add(menuDraw3D);

        final IcyMenuItem itemDraw3DPoint = new IcyMenuItem("Dot", SVGIcon.BROKEN_IMAGE);
        addPluginAction(itemDraw3DPoint, new ROI3DPointPlugin());
        menuDraw3D.add(itemDraw3DPoint);

        final IcyMenuItem itemDraw3DLine = new IcyMenuItem("Line", SVGIcon.BROKEN_IMAGE);
        addPluginAction(itemDraw3DLine, new ROI3DLinePlugin());
        menuDraw3D.add(itemDraw3DLine);

        final IcyMenuItem itemDraw3DPolyline = new IcyMenuItem("Polyline", SVGIcon.TIMELINE);
        addPluginAction(itemDraw3DPolyline, new ROI3DPolyLinePlugin());
        menuDraw3D.add(itemDraw3DPolyline);

        addSeparator();

        itemMagicWand = new IcyMenuItem("Magic Wand", SVGIcon.GESTURE_SELECT);
        addPluginAction(itemMagicWand, new ROIMagicWandPlugin());
        add(itemMagicWand);

        addSeparator();

        final IcyMenuItem item2DTo3D = new IcyMenuItem(RoiActions.convertTo3DAction, SVGIcon.DEPLOYED_CODE);
        add(item2DTo3D);

        final IcyMenuItem item3DTo2D = new IcyMenuItem(RoiActions.convertTo2DAction, SVGIcon.RECTANGLE);
        add(item3DTo2D);

        final IcyMenuItem itemToEllipse = new IcyMenuItem(RoiActions.convertToEllipseAction, SVGIcon.CIRCLE);
        add(itemToEllipse);

        final IcyMenuItem itemToShape = new IcyMenuItem(RoiActions.convertToShapeAction, SVGIcon.PENTAGON);
        add(itemToShape);

        final IcyMenuItem itemToMask = new IcyMenuItem(RoiActions.convertToMaskAction, SVGIcon.GRAIN);
        add(itemToMask);

        addSeparator();

        final IcyMenuItem itemSeparateComponent = new IcyMenuItem(RoiActions.separateObjectsAction, SVGIcon.BROKEN_IMAGE);
        add(itemSeparateComponent);

        final IcyMenuItem itemSeparateWatershed = new IcyMenuItem(RoiActions.computeWatershedSeparation, SVGIcon.BROKEN_IMAGE);
        add(itemSeparateWatershed);

        itemROICutter = new IcyMenuItem("ROI Cutter", SVGIcon.CUT);
        addPluginAction(itemROICutter, new ROILineCutterPlugin());
        add(itemROICutter);

        addSeparator();

        final IcyMenuItem itemDilate = new IcyMenuItem(RoiActions.dilateObjectsAction, SVGIcon.BROKEN_IMAGE);
        add(itemDilate);

        final IcyMenuItem itemErode = new IcyMenuItem(RoiActions.erodeObjectsAction, SVGIcon.BROKEN_IMAGE);
        add(itemErode);

        final IcyMenuItem itemDistanceMap = new IcyMenuItem(RoiActions.computeDistanceMapAction, SVGIcon.BROKEN_IMAGE);
        add(itemDistanceMap);

        addSeparator();

        final IcyMenuItem itemOperationUnion = new IcyMenuItem(RoiActions.boolOrAction, SVGIcon.BROKEN_IMAGE);
        add(itemOperationUnion);

        final IcyMenuItem itemOperationIntersection = new IcyMenuItem(RoiActions.boolAndAction, SVGIcon.BROKEN_IMAGE);
        add(itemOperationIntersection);

        final IcyMenuItem itemOperationInversion = new IcyMenuItem(RoiActions.boolNotAction, SVGIcon.BROKEN_IMAGE);
        add(itemOperationInversion);

        final IcyMenuItem itemOperationExclusiveUnion = new IcyMenuItem(RoiActions.boolXorAction, SVGIcon.BROKEN_IMAGE);
        add(itemOperationExclusiveUnion);

        final IcyMenuItem itemOperationSubstraction = new IcyMenuItem(RoiActions.boolSubtractAction, SVGIcon.BROKEN_IMAGE);
        add(itemOperationSubstraction);

        addSeparator();

        // FIXME: 17/01/2023 change fill value
        final IcyMenuItem itemFillInterior = new IcyMenuItem(RoiActions.fillInteriorAction, SVGIcon.BROKEN_IMAGE);
        add(itemFillInterior);

        final IcyMenuItem itemFillExterior = new IcyMenuItem(RoiActions.fillExteriorAction, SVGIcon.BROKEN_IMAGE);
        add(itemFillExterior);

        reloadROIMenu();

        addActiveSequenceListener();
    }

    private void reloadROIMenu() {
        ThreadUtil.invokeLater(() -> {
            final Sequence active = Icy.getMainInterface().getActiveSequence();

            menuDraw2D.setEnabled((active != null));
            menuDraw3D.setEnabled((active != null));
            itemMagicWand.setEnabled((active != null));
            itemROICutter.setEnabled((active != null));
        });
    }

    private void addPluginAction(final @NotNull IcyMenuItem item, final @NotNull PluginROI plugin) {
        addAction(item, e -> changeTool(plugin));
    }

    private void addAction(final @NotNull IcyMenuItem item, final @NotNull ActionListener listener) {
        for (final ActionListener al : item.getActionListeners())
            item.removeActionListener(al);

        item.addActionListener(listener);
    }

    // TODO: 23/01/2023 Should be global
    public void changeTool(final @NotNull PluginROI plugin) {
        for (final ROIToolChangeListener listener : listeners)
            listener.toolChanged(plugin);
    }

    // TODO: 23/01/2023 Should be global listener
    public static void addListener(final ROIToolChangeListener listener) {
        listeners.add(listener);
    }

    // TODO: 23/01/2023 Should be global listener
    public static void removeListener(final ROIToolChangeListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void sequenceActivated(final Sequence sequence) {
        reloadROIMenu();
    }

    @Override
    public void activeSequenceChanged(final SequenceEvent event) {
        reloadROIMenu();
    }
}
