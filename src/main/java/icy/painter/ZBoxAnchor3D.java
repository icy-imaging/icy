package icy.painter;

import java.awt.Color;
import java.awt.event.InputEvent;

import icy.type.point.Point3D;

public class ZBoxAnchor3D extends Anchor3D
{
    public ZBoxAnchor3D(Point3D position, Color color, Color selectedColor)
    {
        super(position.getX(), position.getY(), position.getZ(), color, selectedColor);
    }

    @Override
    protected boolean updateDrag(InputEvent e, double x, double y, double z)
    {
        // not dragging --> exit
        if (startDragMousePosition == null)
            return false;

        // only on z axis
        double dz = z - startDragMousePosition.getZ();
        // set position
        setPosition(new Point3D.Double(getX(), getY(), startDragPainterPosition.getZ() + dz));

        return true;
    }
}
