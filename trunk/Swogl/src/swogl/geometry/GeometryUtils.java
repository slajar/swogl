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

package swogl.geometry;

import javax.vecmath.*;

import swogl.BoundingBox;

/**
 * This class contains utility methods that deal with Geometry
 * objects
 */
public class GeometryUtils
{
    
    /**
     * Returns the BoundingBox of the given Geometry
     * 
     * @param geometry The Geometry whose BoundingBox should be computed
     * @return The BoundingBox of the given Geometry
     */
    public static BoundingBox computeBoundingBox(Geometry geometry)
    {
        BoundingBox boundingBox = new BoundingBox();
        Point3f point = new Point3f();
        for (int i=0; i<geometry.getNumVertices(); i++)
        {
            geometry.getVertex(i, point);
            boundingBox.combine(point);
        }
        return boundingBox;
    }
    
    /**
     * Scale the given geometry by the given amount
     * 
     * @param geometry The geometry to scale
     * @param scaleX The scaling in x-direction
     * @param scaleY The scaling in y-direction
     * @param scaleZ The scaling in z-direction
     */
    public static void scale(Geometry geometry, float scaleX, float scaleY, float scaleZ)
    {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        matrix.setElement(0,0,scaleX);
        matrix.setElement(1,1,scaleY);
        matrix.setElement(2,2,scaleZ);
        transform(geometry, matrix);
    }

    /**
     * Rotate the given geometry with the given quaternion.
     * 
     * @param geometry The geometry to rotate
     * @param quaternion The rotation
     */
    public static void rotate(Geometry geometry, Quat4f quaternion)
    {
        Matrix4f matrix = new Matrix4f();
        AxisAngle4f axisAngle = new AxisAngle4f();
        axisAngle.set(quaternion);
        matrix.setIdentity();
        matrix.set(axisAngle);
        transform(geometry, matrix);
    }
    
    /**
     * Rotate the given geometry around the given axis about the given
     * angle (in radians).
     * 
     * @param geometry The geometry to rotate
     * @param axis The rotation axis.
     * @param angleRad The rotation angle in radians
     */
    public static void rotate(Geometry geometry, Vector3f axis, float angleRad)
    {
        Matrix4f matrix = new Matrix4f();
        AxisAngle4f axisAngle = new AxisAngle4f(axis, angleRad);
        matrix.setIdentity();
        matrix.set(axisAngle);
        transform(geometry, matrix);
    }

    /**
     * Translate the given Geometry by the given amount.
     * 
     * @param geometry The geometry to translate
     * @param dx The translation in x-direction
     * @param dy The translation in y-direction
     * @param dz The translation in z-direction
     */
    public static void translate(Geometry geometry, float dx, float dy, float dz)
    {
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        matrix.setTranslation(new Vector3f(dx,dy,dz));
        transform(geometry, matrix);
    }
    
    /**
     * Transform the given Geometry with the given matrix.
     * 
     * @param geometry The geometry to transform
     * @param matrix The matrix to transform this geometry with
     */
    public static void transform(Geometry geometry, Matrix4f matrix)
    {
        Point3f vertex = new Point3f();
        for (int i=0; i<geometry.getNumVertices(); i++)
        {
            geometry.getVertex(i, vertex);
            matrix.transform(vertex);
            geometry.setVertex(i, vertex);
        }
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private GeometryUtils()
    {
        
    }
}
