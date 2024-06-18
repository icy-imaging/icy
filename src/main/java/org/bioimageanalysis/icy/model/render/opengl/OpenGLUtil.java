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

/**
 * 
 */
package org.bioimageanalysis.icy.model.render.opengl;

import com.jogamp.opengl.GLProfile;

/**
 * Utilities class for OpenGL.
 * 
 * @author Stephane
 */
public class OpenGLUtil
{
    /**
     * Returns <code>true</code> is the specified version of OpenGL is supported by the graphics card (or by its
     * driver).<br>
     * Ex: if (isOpenGLSupported(2)) // test for OpenGL 2 compliance
     * 
     * @param version
     *        the version of OpenGL we want to test for (1 to 4)
     */
    public static boolean isOpenGLSupported(int version)
    {
    	return isOpenGLSupported(version, false);
    }
    
    /**
     * Returns <code>true</code> is the specified version of OpenGL is supported by the graphics card
     * (or by its driver).<br>
     * Ex: if (isOpenGLSupported(2, true))   // test if GPU supports OpenGL 2 (hardware support)<br>
     *     if (isOpenGLSupported(3, false))  // test if driver support OpenGL 3 (hardware or software implementation)<br>
     * 
     * @param version
     *        the version of OpenGL we want to test for (1 to 4)
     * @param hard
     *        specify if we query about hardware support (GPU) or not
     */
    public static boolean isOpenGLSupported(int version, boolean hard)
    {
        try
        {
            // get maximum supported GL profile
            final GLProfile glp = GLProfile.getMaximum(hard);
            boolean result = false;
            
            switch (version)
            {
	            case 2:
	            	result |= glp.isGL2();
	            case 3:
	            	result |= glp.isGL3();
	            case 4:
	                result |= glp.isGL4();
            }
            
            if ((version > 0) && (version <= 4)) return result;
        }
        catch (Exception e)
        {
            // OpenGL throwing error --> just report as not supported
        }
        
        return false;
    }
}
