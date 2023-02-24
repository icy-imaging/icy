/*
 * Copyright 2010-2023 Institut Pasteur.
 *
 * This file is part of Icy.
 *
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
package icy.imagej;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.MenuComponent;
import java.awt.PopupMenu;
import java.lang.reflect.Field;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import icy.common.listener.SkinChangeListener;
import icy.common.listener.weak.WeakSkinChangeListener;
import icy.file.FileUtil;
import icy.gui.util.LookAndFeelUtil;
import icy.system.IcyExceptionHandler;
import icy.system.SystemUtil;
import icy.util.ColorUtil;
import icy.util.ReflectionUtil;
import ij.IJ;
import ij.gui.Toolbar;
import ij.plugin.MacroInstaller;

/**
 * ImageJ ToolBar Wrapper class.
 *
 * @author Stephane
 */
public class ToolbarWrapper extends Toolbar {
    private class CustomToolBar extends JToolBar {
        /**
         * @param orientation
         */
        public CustomToolBar(int orientation) {
            super(orientation);

            setBorder(BorderFactory.createEmptyBorder());
            setFloatable(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            // paint background
            super.paintComponent(g);

            // then just paint parent toolbar buttons as is
            ToolbarWrapper.this.paint(g);
        }

        @Override
        public void removeNotify() {
            try {
                // TODO: remove this temporary hack when the "window dispose" bug will be fixed
                // on the OpenJDK / Sun JVM
                if (SystemUtil.isUnix()) {
                    @SuppressWarnings("rawtypes")
                    Vector pp = (Vector) ReflectionUtil.getFieldObject(this, "popups", true);
                    pp.clear();
                }
            }
            catch (Exception e) {
                System.out.println("Warning: cannot remove toolbar buttons from ImageJ.");
            }

            super.removeNotify();
        }
    }

    protected CustomToolBar swingComponent;

    /**
     * leaked methods and fields
     */
    Field grayField = null;
    Field brighterField = null;
    Field darkerField = null;
    Field evenDarkerField = null;
    Field toolColorField = null;

    public ToolbarWrapper(ImageJWrapper ijw) {
        super();

        // lazy creation (it should already exist here)
        if (swingComponent == null)
            swingComponent = new CustomToolBar(SwingConstants.HORIZONTAL);

        // set component properties
        swingComponent.setMinimumSize(getMinimumSize());
        swingComponent.setPreferredSize(getPreferredSize());
        swingComponent.addKeyListener(ijw);
        swingComponent.addMouseListener(this);
        swingComponent.addMouseMotionListener(this);

        try {
            // get access to private fields
            grayField = ReflectionUtil.getField(this.getClass(), "gray", true);
            brighterField = ReflectionUtil.getField(this.getClass(), "brighter", true);
            darkerField = ReflectionUtil.getField(this.getClass(), "darker", true);
            evenDarkerField = ReflectionUtil.getField(this.getClass(), "evenDarker", true);
            toolColorField = ReflectionUtil.getField(this.getClass(), "toolColor", true);
        }
        catch (Throwable t) {
            System.out.println("Warning: cannot access toolbar buttons from ImageJ");
        }

        // TODO: 17/02/2023 Change skin changed for ImageJ
        SkinChangeListener skinChangeListener = () -> {
            Color bgCol = swingComponent.getBackground();
            Color brighterCol;
            Color darkerCol;
            Color evenDarkerCol;

            if (ColorUtil.getLuminance(bgCol) < 64)
                bgCol = ColorUtil.mix(bgCol, Color.gray);

            brighterCol = ColorUtil.mix(bgCol, Color.white);
            darkerCol = ColorUtil.mix(bgCol, Color.black);
            evenDarkerCol = ColorUtil.mix(darkerCol, Color.black);

            if (ColorUtil.getLuminance(bgCol) < 128)
                brighterCol = ColorUtil.mix(bgCol, brighterCol);
            else {
                darkerCol = ColorUtil.mix(bgCol, darkerCol);
                evenDarkerCol = ColorUtil.mix(darkerCol, evenDarkerCol);
            }

            if (grayField == null)
                System.out.println("Warning: cannot set background color of ImageJ toolbar.");

            try {
                if (grayField != null)
                    grayField.set(ToolbarWrapper.this, bgCol);
                if (brighterField != null)
                    brighterField.set(ToolbarWrapper.this, brighterCol);
                if (darkerField != null)
                    darkerField.set(ToolbarWrapper.this, darkerCol);
                if (evenDarkerField != null)
                    evenDarkerField.set(ToolbarWrapper.this, evenDarkerCol);
                if (toolColorField != null)
                    toolColorField.set(ToolbarWrapper.this, swingComponent.getForeground());

                swingComponent.repaint();
            }
            catch (Exception e) {
                IcyExceptionHandler.showErrorMessage(e, false);
                System.err.println("Cannot hack background color of ImageJ toolbar.");
            }
        };

        // install default tools and macros
        final String file = IJ.getDirectory("macros") + "StartupMacros.txt";
        if (FileUtil.exists(file))
            new MacroInstaller().run(file);

        LookAndFeelUtil.addListener(new WeakSkinChangeListener(skinChangeListener));
    }

    @Override
    public Container getParent() {
        if (swingComponent == null)
            return null;

        return swingComponent.getParent();
    }

    /**
     * @return the swingComponent
     */
    public JToolBar getSwingComponent() {
        return swingComponent;
    }

    @Override
    public synchronized void add(PopupMenu popup) {
        // lazy creation of swing component
        if (swingComponent == null)
            swingComponent = new CustomToolBar(SwingConstants.HORIZONTAL);

        swingComponent.add(popup);
        swingComponent.repaint();
    }

    @Override
    public synchronized void remove(MenuComponent popup) {
        // lazy creation of swing component
        if (swingComponent == null)
            swingComponent = new CustomToolBar(SwingConstants.HORIZONTAL);
        else {
            swingComponent.remove(popup);
            swingComponent.repaint();
        }
    }

    @Override
    public Graphics getGraphics() {
        if (swingComponent == null)
            return null;

        return swingComponent.getGraphics();
    }

    @Override
    public void repaint() {
        super.repaint();

        swingComponent.repaint();
    }
}
