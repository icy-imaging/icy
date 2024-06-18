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
package org.bioimageanalysis.icy.model.overlay.anchor;

import org.bioimageanalysis.icy.io.xml.XMLUtil;
import org.w3c.dom.Node;

import java.awt.*;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

/**
 * Anchor for path type shape.<br>
 * Support extra coordinate to store curve informations.
 *
 * @author Stephane Dallongeville
 * @author Thomas Musset
 */
public class PathAnchor2D extends Anchor2D {
    private static final String ID_POS_CEXT_X = "pos_ext1_x";
    private static final String ID_POS_CEXT_Y = "pos_ext1_y";
    private static final String ID_POS_QEXT_X = "pos_ext2_x";
    private static final String ID_POS_QEXT_Y = "pos_ext2_y";
    private static final String ID_TYPE = "type";

    /**
     * Curve extra coordinates
     */
    private final Point2D.Double posCExt;
    /**
     * Quad extra coordinates
     */
    private final Point2D.Double posQExt;
    /**
     * anchor type (used as PathIterator type)
     */
    private int type;

    public PathAnchor2D(final double x1, final double y1, final double x2, final double y2, final double x3, final double y3, final int ray, final Color color, final Color selectedColor, final int type) {
        super(x3, y3, ray, color, selectedColor);

        posCExt = new Point2D.Double(x1, y1);
        posQExt = new Point2D.Double(x2, y2);
        this.type = type;
    }

    public PathAnchor2D(final double x1, final double y1, final double x2, final double y2, final double x3, final double y3, final int ray, final Color color, final Color selectedColor) {
        this(x1, y1, x2, y2, x3, y3, ray, color, selectedColor, -1);
    }

    public PathAnchor2D(final double x1, final double y1, final double x2, final double y2, final double x3, final double y3, final Color color, final Color selectedColor) {
        this(x1, y1, x2, y2, x3, y3, DEFAULT_RAY, color, selectedColor, PathIterator.SEG_CUBICTO);
    }

    public PathAnchor2D(final double x1, final double y1, final double x2, final double y2, final double x3, final double y3) {
        this(x1, y1, x2, y2, x3, y3, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR, PathIterator.SEG_CUBICTO);
    }

    public PathAnchor2D(final double x1, final double y1, final double x2, final double y2, final Color color, final Color selectedColor) {
        this(0d, 0d, x1, y1, x2, y2, DEFAULT_RAY, color, selectedColor, PathIterator.SEG_QUADTO);
    }

    public PathAnchor2D(final double x1, final double y1, final double x2, final double y2) {
        this(0d, 0d, x1, y1, x2, y2, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR, PathIterator.SEG_QUADTO);
    }

    public PathAnchor2D(final double x1, final double y1, final Color color, final Color selectedColor, final int type) {
        this(0d, 0d, 0d, 0d, x1, y1, DEFAULT_RAY, color, selectedColor, type);
    }

    public PathAnchor2D(final double x1, final double y1, final Color color, final Color selectedColor) {
        this(0d, 0d, 0d, 0d, x1, y1, DEFAULT_RAY, color, selectedColor, PathIterator.SEG_LINETO);
    }

    public PathAnchor2D(final double x1, final double y1, final int type) {
        this(0d, 0d, 0d, 0d, x1, y1, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR, type);
    }

    public PathAnchor2D(final double x1, final double y1) {
        this(0d, 0d, 0d, 0d, x1, y1, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR, PathIterator.SEG_MOVETO);
    }

    public PathAnchor2D() {
        this(0d, 0d, 0d, 0d, 0, 0, DEFAULT_RAY, DEFAULT_NORMAL_COLOR, DEFAULT_SELECTED_COLOR, PathIterator.SEG_MOVETO);
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

    /**
     * Set the type of this segment
     *
     * @see PathIterator
     * @param value
     *        the type to set
     */
    public void setType(final int value) {
        if (type != value) {
            type = value;
            painterChanged();
        }
    }

    @Override
    public boolean isVisible() {
        return switch (getType()) {
            // CLOSE type point are never visible
            case PathIterator.SEG_CLOSE -> false;
            default -> super.isVisible();
        };
    }

    /**
     * @return the posQExt
     */
    public Point2D.Double getPosQExt() {
        return posQExt;
    }

    /**
     * @return the posCExt
     */
    public Point2D.Double getPosCExt() {
        return posCExt;
    }

    public void setPosQExt(final Point2D p) {
        setPosQExt(p.getX(), p.getY());
    }

    public void setPosQExt(final double x, final double y) {
        if ((posQExt.x != x) || (posQExt.y != y)) {
            posQExt.x = x;
            posQExt.y = y;

            positionChanged();
            painterChanged();
        }
    }

    public void setPosCExt(final Point2D p) {
        setPosCExt(p.getX(), p.getY());
    }

    public void setPosCExt(final double x, final double y) {
        if ((posCExt.x != x) || (posCExt.y != y)) {
            posCExt.x = x;
            posCExt.y = y;

            positionChanged();
            painterChanged();
        }
    }

    /**
     * @return the PosQExt.x
     */
    public double getPosQExtX() {
        return posQExt.x;
    }

    /**
     * @param x
     *        the PosQExt.x to set
     */
    public void setPosQExtX(final double x) {
        setPosQExt(x, posQExt.y);
    }

    /**
     * @return the PosQExt.y
     */
    public double getPosQExtY() {
        return posQExt.y;
    }

    /**
     * @param y
     *        the PosQExt.y to set
     */
    public void setPosQExtY(final double y) {
        setPosQExt(posQExt.x, y);
    }

    /**
     * @return the PosCExt.x
     */
    public double getPosCExtX() {
        return posCExt.x;
    }

    /**
     * @param x
     *        the PosCExt.x to set
     */
    public void setPosCExtX(final double x) {
        setPosCExt(x, posCExt.y);
    }

    /**
     * @return the PosCExt.y
     */
    public double getPosCExtY() {
        return posCExt.y;
    }

    /**
     * @param y
     *        the PosCExt.y to set
     */
    public void setPosCExtY(final double y) {
        setPosCExt(posCExt.x, y);
    }

    @Override
    public void translate(final double dx, final double dy) {
        beginUpdate();
        try {
            super.translate(dx, dy);

            setPosCExt(posCExt.x + dx, posCExt.y + dy);
            setPosQExt(posQExt.x + dx, posQExt.y + dy);
        }
        finally {
            endUpdate();
        }
    }

    @Override
    public boolean loadPositionFromXML(final Node node) {
        if (node == null)
            return false;

        beginUpdate();
        try {
            super.loadPositionFromXML(node);

            setPosCExtX(XMLUtil.getElementDoubleValue(node, ID_POS_CEXT_X, 0d));
            setPosCExtY(XMLUtil.getElementDoubleValue(node, ID_POS_CEXT_Y, 0d));
            setPosQExtX(XMLUtil.getElementDoubleValue(node, ID_POS_QEXT_X, 0d));
            setPosQExtY(XMLUtil.getElementDoubleValue(node, ID_POS_QEXT_Y, 0d));
            setType(XMLUtil.getElementIntValue(node, ID_TYPE, -1));
        }
        finally {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean loadFromXML(final Node node) {
        if (node == null)
            return false;

        beginUpdate();
        try {
            super.loadFromXML(node);

            setPosCExtX(XMLUtil.getElementDoubleValue(node, ID_POS_CEXT_X, 0d));
            setPosCExtY(XMLUtil.getElementDoubleValue(node, ID_POS_CEXT_Y, 0d));
            setPosQExtX(XMLUtil.getElementDoubleValue(node, ID_POS_QEXT_X, 0d));
            setPosQExtY(XMLUtil.getElementDoubleValue(node, ID_POS_QEXT_Y, 0d));
            setType(XMLUtil.getElementIntValue(node, ID_TYPE, -1));
        }
        finally {
            endUpdate();
        }

        return true;
    }

    @Override
    public boolean savePositionToXML(final Node node) {
        if (node == null)
            return false;

        super.savePositionToXML(node);

        XMLUtil.setElementDoubleValue(node, ID_POS_CEXT_X, getPosCExtX());
        XMLUtil.setElementDoubleValue(node, ID_POS_CEXT_Y, getPosCExtY());
        XMLUtil.setElementDoubleValue(node, ID_POS_QEXT_X, getPosQExtX());
        XMLUtil.setElementDoubleValue(node, ID_POS_QEXT_Y, getPosQExtY());
        XMLUtil.setElementIntValue(node, ID_TYPE, getType());

        return true;
    }

    @Override
    public boolean saveToXML(final Node node) {
        if (node == null)
            return false;

        super.saveToXML(node);

        XMLUtil.setElementDoubleValue(node, ID_POS_CEXT_X, getPosCExtX());
        XMLUtil.setElementDoubleValue(node, ID_POS_CEXT_Y, getPosCExtY());
        XMLUtil.setElementDoubleValue(node, ID_POS_QEXT_X, getPosQExtX());
        XMLUtil.setElementDoubleValue(node, ID_POS_QEXT_Y, getPosQExtY());
        XMLUtil.setElementIntValue(node, ID_TYPE, getType());

        return true;
    }
}
