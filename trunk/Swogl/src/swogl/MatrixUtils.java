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

import javax.vecmath.*;

/**
 * This class contains some utility methods for matrix operations
 */
public class MatrixUtils
{
    /**
     * Returns an identity matrix. 
     */
    public static Matrix4f identity()
    {
        Matrix4f identity = new Matrix4f();
        identity.setIdentity();
        return identity;
    }

    /**
     * Creates a matrix describing the specified translation
     * 
     * @param x Translation in x-direction
     * @param y Translation in y-direction
     * @param z Translation in z-direction
     * @return A matrix describing the specified translation
     */
    public static Matrix4f translate(float x, float y, float z)
    {
        return translate(new Vector3f(x,y,z));
    }

    /**
     * Creates a matrix describing the given translation
     * 
     * @param translation Translation vector
     * @return A matrix describing the given translation
     */
    public static Matrix4f translate(Vector3f translation)
    {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        result.setTranslation(translation);
        return result;
    }

    /**
     * Creates a matrix that describes the given rotation about the
     * x-axis
     * 
     * @param rotX The rotation angle, in radians
     * @return The resulting rotation matrix
     */
    public static Matrix4f rotateX(float rotX)
    {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        result.rotX(rotX);
        return result;
    }

    /**
     * Creates a matrix that describes the given rotation about the
     * y-axis
     * 
     * @param rotY The rotation angle, in radians
     * @return The resulting rotation matrix
     */
    public static Matrix4f rotateY(float rotY)
    {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        result.rotY(rotY);
        return result;
    }

    /**
     * Creates a matrix that describes the given rotation about the
     * z-axis
     * 
     * @param rotZ The rotation angle, in radians
     * @return The resulting rotation matrix
     */
    public static Matrix4f rotateZ(float rotZ)
    {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        result.rotZ(rotZ);
        return result;
    }
    
    /**
     * Creates a matrix that describes a rotation of the given
     * angle around the given axis
     * 
     * @param axis The axis of rotation
     * @param angle The rotation angle, in radians
     * @return The resulting rotation matrix
     */
    public static Matrix4f rotate(Vector3f axis, float angle)
    {
        Matrix4f result = new Matrix4f();
        result.set(new AxisAngle4f(axis, angle));
        return result;
    }
    

    /**
     * Creates a matrix describing the specified scaling
     * 
     * @param x Scaling in x-direction
     * @param y Scaling in y-direction
     * @param z Scaling in z-direction
     * @return A matrix describing the specified scaling
     */
    public static Matrix4f scale(float x, float y, float z)
    {
        Matrix4f result = new Matrix4f();
        result.setIdentity();
        result.setElement(0,0,x);
        result.setElement(1,1,y);
        result.setElement(2,2,z);
        return result;
    }

    /**
     * Private constructor to prevent instantiation
     */
    private MatrixUtils()
    {
        
    }
    
}
