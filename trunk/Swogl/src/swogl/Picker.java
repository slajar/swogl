/*
 * Swogl - Swing meets JOGL
 * 
 * Copyright 2007 Marco Hutter - http://swogl.javagl.de
 * 
 * 
 * This file is part of Swogl. 
 * 
 * Swogl is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Swogl is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License
 * along with Swogl.  If not, see <http://www.gnu.org/licenses/>.
 */

package swogl;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.vecmath.*;

/**
 * A class that may compute a picking ray based upon the current
 * GL_MODELVIEW_MATRIX, GL_PROJECTION_MATRIX and GL_VIEWPORT of
 * a GL context
 */
class Picker
{
    /**
     * A GLU helper 
     */
    private final GLU glu = new GLU();

    /**
     * The current GL_MODELVIEW_MATRIX
     */
    private double currentModelviewMatrix[] = new double[16];

    /**
     * The current GL_PROJECTION_MATRIX
     */
    private double currentProjectionMatrix[] = new double[16];
    
    /**
     * The current GL_VIEWPORT
     */
    private int currentViewport[] = new int[4];

    /**
     * Update this picker according to the given GL context, storing
     * the current modelview- and projection matrix and the viewport
     * 
     * @param gl The GL context
     */
    void update(GL gl)
    {
        gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, currentModelviewMatrix, 0);
        gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, currentProjectionMatrix, 0);
        gl.glGetIntegerv(GL.GL_VIEWPORT, currentViewport, 0);
    }


    /**
     * Computes the current direction of a picking ray sent through the
     * pixel with the given screen coordinates
     * 
     * @param x The x coordinate of the pixel
     * @param y The y coordinate of the pixel
     * @param rayOrigin Will contain the origin of the picking ray
     * @param rayDirection Will contain the normalized direction of the picking ray
     */
    void computePickingRayData(int x, int y, Tuple3f rayOrigin, Vector3f rayDirection)
    {
        double result0[] = new double[3];
        double result1[] = new double[3];
        int fy = currentViewport[3] - y;
        glu.gluUnProject(x, fy, 0.0f, currentModelviewMatrix, 0, currentProjectionMatrix, 0, currentViewport, 0, result0, 0);
        glu.gluUnProject(x, fy, 1.0f, currentModelviewMatrix, 0, currentProjectionMatrix, 0, currentViewport, 0, result1, 0);

        float dx = (float)(result1[0] - result0[0]);
        float dy = (float)(result1[1] - result0[1]);
        float dz = (float)(result1[2] - result0[2]);

        rayOrigin.set((float)result0[0], (float)result0[1], (float)result0[2]);

        rayDirection.set(dx, dy, dz);
        rayDirection.normalize();
    }


}
