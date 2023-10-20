/*
 * Copyright 2010-2015 Institut Pasteur.
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
 * along with Icy. If not, see <http://www.gnu.org/licenses/>.
 */
// LegacyInjector.java
//

/*
 * ImageJ software for multidimensional image processing and analysis.
 * 
 * Copyright (c) 2010, ImageJDev.org.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * Neither the names of the ImageJDev.org developers nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package icy.imagej;

import icy.system.ClassPatcher;
import icy.system.SystemUtil;

/**
 * Overrides class behavior of ImageJ classes using bytecode manipulation. This
 * class uses the {@link ClassPatcher} (which uses Javassist) to inject method
 * hooks, which are implemented in the {@link icy.imagej.patches} package.
 * 
 * @author Curtis Rueden
 * @author Stephane Dallongeville
 */
@Deprecated(since = "3.0.0", forRemoval = true)
public class ImageJPatcher
{
    private static final String PATCH_PKG = "icy.imagej.patches";
    private static final String PATCH_SUFFIX = "Methods";

    /** Overrides class behavior of ImageJ classes by injecting method hooks. */
    public static void applyPatches()
    {
        final ClassPatcher hacker = new ClassPatcher(PATCH_PKG, PATCH_SUFFIX);

        // override behavior of ij.IJ
        hacker.insertAfterMethod("ij.IJ", "public static void showProgress(double progress)");
        hacker.insertAfterMethod("ij.IJ", "public static void showProgress(int currentIndex, int finalIndex)");
        hacker.insertAfterMethod("ij.IJ", "public static void showStatus(java.lang.String s)");

        // override behavior of ij.ImageJ
        hacker.insertAfterMethod("ij.ImageJ", "public void showStatus(java.lang.String s)");
        hacker.replaceMethod("ij.ImageJ", "public void configureProxy()");

        // override behavior of ij.Menus
        hacker.insertAfterMethod("ij.Menus", "public void installUserPlugin(java.lang.String className, boolean force)");
        hacker.insertAfterMethod("ij.Menus", "public static void updateMenus()");
        hacker.insertAfterMethod("ij.Menus", "public static synchronized void updateWindowMenuItem(java.lang.String oldLabel, java.lang.String newLabel)");
        hacker.insertAfterMethod("ij.Menus", "public static synchronized void addOpenRecentItem(java.lang.String path)");
        hacker.insertAfterMethod("ij.Menus",
                "public static int installPlugin(java.lang.String plugin, char menuCode, java.lang.String command, java.lang.String shortcut, ij.ImageJ ij, int result)");

        // override behavior of ij.WindowManager
        hacker.insertAfterMethod("ij.WindowManager", "public static void setCurrentWindow(ij.gui.ImageWindow iw)");
        hacker.insertAfterMethod("ij.WindowManager", "private static void removeImageWindow(ij.gui.ImageWindow iw)");

        // override behavior of ij.ImagePlus
        // hacker.insertAfterMethod("ij.ImagePlus", "public void updateAndDraw()");
        // hacker.insertAfterMethod("ij.ImagePlus", "public void repaintWindow()");
        // hacker.insertAfterMethod("ij.ImagePlus",
        // "public void show(java.lang.String statusMessage)");
        // hacker.insertAfterMethod("ij.ImagePlus", "public void hide()");
        // hacker.insertAfterMethod("ij.ImagePlus", "public void close()");

        // override behavior of ij.gui.ImageWindow
        // hacker.insertMethod("ij.gui.ImageWindow", "public void setVisible(boolean vis)");
        // hacker.insertMethod("ij.gui.ImageWindow", "public void show()");
        // hacker.insertBeforeMethod("ij.gui.ImageWindow", "public void close()");
        // hacker.insertAfterMethod("ij.gui.ImageWindow", "public void windowActivated(java.awt.event.WindowEvent e)");
        // hacker.insertAfterMethod("ij.gui.ImageWindow", "public void windowClosed(java.awt.event.WindowEvent e)");

        // override behavior of MacAdapter
        if (SystemUtil.isMac())
        {
            try
            {
                if (SystemUtil.getJavaVersionAsNumber() >= 9)
                {
                    hacker.replaceMethod("ij.plugin.MacAdapter9", "public void run()");
                    hacker.replaceMethod("ij.plugin.MacAdapter9", "public void run(java.lang.String arg)");
                    hacker.replaceMethod("ij.plugin.MacAdapter9", "public void handleAbout(java.awt.desktop.AboutEvent e)");
                    hacker.replaceMethod("ij.plugin.MacAdapter9", "public void openFiles(java.awt.desktop.OpenFilesEvent e)");
                    hacker.replaceMethod("ij.plugin.MacAdapter9", "public void handleQuitRequestWith(java.awt.desktop.QuitEvent e, java.awt.desktop.QuitResponse r)");
                }
                else
                {
                    hacker.replaceMethod("ij.plugin.MacAdapter", "public void run(java.lang.String arg)");
                    hacker.replaceMethod("ij.plugin.MacAdapter", "public void handleAbout(com.apple.eawt.ApplicationEvent e)");
                    hacker.replaceMethod("ij.plugin.MacAdapter", "public void handleOpenApplication(com.apple.eawt.ApplicationEvent e)");
                    hacker.replaceMethod("ij.plugin.MacAdapter", "public void handleOpenFile(com.apple.eawt.ApplicationEvent e)");
                    hacker.replaceMethod("ij.plugin.MacAdapter", "public void handlePreferences(com.apple.eawt.ApplicationEvent e)");
                    hacker.replaceMethod("ij.plugin.MacAdapter", "public void handlePrintFile(com.apple.eawt.ApplicationEvent e)");
                    hacker.replaceMethod("ij.plugin.MacAdapter", "public void handleReOpenApplication(com.apple.eawt.ApplicationEvent e)");
                    hacker.replaceMethod("ij.plugin.MacAdapter", "public void handleQuit(com.apple.eawt.ApplicationEvent e)");
                }
            }
            catch (Throwable t)
            {
                // not so important...
                System.err.println("Error while patching ImageJ MacAdapter class:" + t.getMessage());
            }
        }

        // load classes at the end
        hacker.loadClass("ij.IJ", ij.IJEventListener.class);
        try
        {
            hacker.loadClass("ij.ImageJ", ij.IJEventListener.class);
        }
        catch (LinkageError le)
        {
            // ignore this one with Java 9 as it will load up at same time than ij.IJ class
            if (SystemUtil.getJavaVersionAsNumber() < 9)
                throw le;
        }
        hacker.loadClass("ij.Menus", ij.IJEventListener.class);
        hacker.loadClass("ij.WindowManager", ij.IJEventListener.class);
        // hacker.loadClass("ij.ImagePlus");
        // try
        // {
        // hacker.loadClass("ij.gui.ImageWindow", ij.gui.DialogListener.class);
        // }
        // catch (LinkageError le)
        // {
        // // ignore this one with Java 9 as it will load up at same time than ij.IJ class
        // if (SystemUtil.getJavaVersionAsNumber() < 9)
        // throw le;
        // }
        if (SystemUtil.isMac())
        {
            try
            {
                if (SystemUtil.getJavaVersionAsNumber() >= 9)
                    hacker.loadClass("ij.plugin.MacAdapter9", ij.plugin.PlugIn.class);
                else
                    hacker.loadClass("ij.plugin.MacAdapter", ij.plugin.PlugIn.class);
            }
            catch (Throwable t)
            {
                // not so important...
                System.err.println("Error while patching ImageJ MacAdapter class:" + t.getMessage());
            }
        }
    }
}
