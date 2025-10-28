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

package org.bioimageanalysis.icy.gui.menu;

import org.bioimageanalysis.icy.gui.action.RoiActions;
import org.bioimageanalysis.icy.gui.component.icon.SVGResource;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenu;
import org.bioimageanalysis.icy.gui.component.menu.IcyMenuItem;

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

    private ApplicationMenuROI() {
        super("Region of Interest");

        final IcyMenuItem itemLoadROI = new IcyMenuItem(RoiActions.loadAction, SVGResource.FILE_OPEN);
        add(itemLoadROI);

        final IcyMenuItem itemSaveROI = new IcyMenuItem(RoiActions.saveAction, SVGResource.SAVE_AS);
        add(itemSaveROI);

        final IcyMenuItem itemExportExcelROI = new IcyMenuItem(RoiActions.xlsExportAction, SVGResource.EXPORT_NOTES);
        add(itemExportExcelROI);

        addSeparator();

        final IcyMenu menuConvertion = new IcyMenu("Convertion", SVGResource.CONVERT_SHAPE);
        add(menuConvertion);

        final IcyMenuItem item2DTo3D = new IcyMenuItem(RoiActions.convertTo3DAction, SVGResource.DEPLOYED_CODE);
        menuConvertion.add(item2DTo3D);

        final IcyMenuItem item3DTo2D = new IcyMenuItem(RoiActions.convertTo2DAction, SVGResource.ROI_RECTANGLE);
        menuConvertion.add(item3DTo2D);

        final IcyMenuItem itemToEllipse = new IcyMenuItem(RoiActions.convertToEllipseAction, SVGResource.ROI_ELLIPSE);
        menuConvertion.add(itemToEllipse);

        final IcyMenuItem itemToShape = new IcyMenuItem(RoiActions.convertToShapeAction, SVGResource.ROI_POLYGON);
        menuConvertion.add(itemToShape);

        final IcyMenuItem itemToMask = new IcyMenuItem(RoiActions.convertToMaskAction, SVGResource.GRAIN);
        menuConvertion.add(itemToMask);

        addSeparator();

        final IcyMenuItem itemSeparateComponent = new IcyMenuItem(RoiActions.separateObjectsAction, SVGResource.ROI_SPLIT);
        add(itemSeparateComponent);

        final IcyMenuItem itemSeparateWatershed = new IcyMenuItem(RoiActions.computeWatershedSeparation, SVGResource.IMAGE_BROKEN);
        add(itemSeparateWatershed);

        addSeparator();

        final IcyMenuItem itemDilate = new IcyMenuItem(RoiActions.dilateObjectsAction, SVGResource.ROI_DILATE);
        add(itemDilate);

        final IcyMenuItem itemErode = new IcyMenuItem(RoiActions.erodeObjectsAction, SVGResource.ROI_ERODE);
        add(itemErode);

        final IcyMenuItem itemDistanceMap = new IcyMenuItem(RoiActions.computeDistanceMapAction, SVGResource.ROI_DISTANCE_MAP);
        add(itemDistanceMap);

        addSeparator();

        final IcyMenu menuBoolean = new IcyMenu("Boolean Operation", SVGResource.ROI_BOOLEAN);
        add(menuBoolean);

        final IcyMenuItem itemOperationUnion = new IcyMenuItem(RoiActions.boolOrAction, SVGResource.ROI_BOOLEAN_OR);
        menuBoolean.add(itemOperationUnion);

        final IcyMenuItem itemOperationIntersection = new IcyMenuItem(RoiActions.boolAndAction, SVGResource.ROI_BOOLEAN_AND);
        menuBoolean.add(itemOperationIntersection);

        final IcyMenuItem itemOperationInversion = new IcyMenuItem(RoiActions.boolNotAction, SVGResource.ROI_BOOLEAN_NOT);
        menuBoolean.add(itemOperationInversion);

        final IcyMenuItem itemOperationExclusiveUnion = new IcyMenuItem(RoiActions.boolXorAction, SVGResource.ROI_BOOLEAN_XOR);
        menuBoolean.add(itemOperationExclusiveUnion);

        final IcyMenuItem itemOperationSubstraction = new IcyMenuItem(RoiActions.boolSubtractAction, SVGResource.ROI_BOOLEAN_SUBSTRACT);
        menuBoolean.add(itemOperationSubstraction);

        addSeparator();

        // FIXME: 17/01/2023 change fill value
        final IcyMenuItem itemFillInterior = new IcyMenuItem(RoiActions.fillInteriorAction, SVGResource.ROI_INTERIOR);
        add(itemFillInterior);

        final IcyMenuItem itemFillExterior = new IcyMenuItem(RoiActions.fillExteriorAction, SVGResource.ROI_EXTERIOR);
        add(itemFillExterior);
    }
}
