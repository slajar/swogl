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

import swogl.geometry.Geometry;

/**
 * A class for intersecting a picking ray with a Geometry.
 * An instance of this class is used in each SwoglComponent,
 * to find the intersection position of a picking ray that
 * was sent through the parent SwoglContainer with the 
 * geometry of the respective SwoglComponent. 
 */
class GeometryPicker
{
    private static final float EPSILON = 1e-5f;
    
    // Temporary variables
    private final Point3f tempTriangleResult = new Point3f();

    private final Vector2f tempVector2f0 = new Vector2f();
    private final Vector2f tempVector2f1 = new Vector2f();

    private final Vector3f tempEdge0 = new Vector3f();
    private final Vector3f tempEdge1 = new Vector3f();

    private final Vector3f tempVector3f0 = new Vector3f();
    private final Vector3f tempVector3f1 = new Vector3f();
    private final Vector3f tempVector3f2 = new Vector3f();

    /**
     * Computes the intersection of the given picking ray with this
     * object
     * 
     * @param rayOrigin The origin of the ray
     * @param rayDirection The NORMALIZED direction of the ray
     * @param intersectionPosition The 3D point where the object
     * is hit by the ray
     * @param texCoord The texture coordinates at the position
     * where the ray hits the object
     * @return The distance between the rayOrigin and the intersection
     * point. Float.POSITIVE_INFINITY if no intersection exists
     */
    public synchronized float findIntersectionPosition(
                    Geometry geometry, Point3f rayOrigin, Vector3f rayDirection, 
                    Point3f intersectionPosition, TexCoord2f texCoord)
    {
        intersectionPosition.set(0,0,0);
        texCoord.set(0, 0);

        Point3f p0 = new Point3f();
        Point3f p1 = new Point3f();
        Point3f p2 = new Point3f();

        // Stores the intersection information of the intersection
        // between the ray and the triangle, which has the least
        // distance form the ray origin
        Point3f closestTriangleResult = new Point3f(0,0,Float.POSITIVE_INFINITY);
        
        // Stores the vertex- and texture coordinate indices of the
        // closest intersected triangle
        Point3i closestTriangleVertexIndices = new Point3i();
        Point3i closestTriangleTexCoordIndices = new Point3i();
        
        for (int i = 0; i < geometry.getNumTriangles(); i++)
        {
            int vi0 = geometry.getVertexIndex(i * 3 + 0);
            int vi1 = geometry.getVertexIndex(i * 3 + 1);
            int vi2 = geometry.getVertexIndex(i * 3 + 2);
            
            int ti0 = geometry.getTexCoordIndex(i * 3 + 0);
            int ti1 = geometry.getTexCoordIndex(i * 3 + 1);
            int ti2 = geometry.getTexCoordIndex(i * 3 + 2);

            geometry.getVertex(vi0, p0);
            geometry.getVertex(vi1, p1);
            geometry.getVertex(vi2, p2);

            if (findIntersectionPosition(p0,p1,p2, rayOrigin, rayDirection, tempTriangleResult))
            {
                if (tempTriangleResult.z < closestTriangleResult.z)
                {
                    closestTriangleResult.set(tempTriangleResult);
                    closestTriangleVertexIndices.set(vi0, vi1, vi2);
                    closestTriangleTexCoordIndices.set(ti0, ti1, ti2);
                }
            }
        }

        if (closestTriangleResult.z < Float.POSITIVE_INFINITY)
        {
            // The fields closestTriangleResult.x/y contain the barycentric
            // coordinates of the intersection position. Now, compute 
            // the actual intersection position in 3D space.
            geometry.getVertex(closestTriangleVertexIndices.x, p0);
            geometry.getVertex(closestTriangleVertexIndices.y, p1);
            geometry.getVertex(closestTriangleVertexIndices.z, p2);
            tempVector3f0.sub(p1, p0);
            tempVector3f1.sub(p2, p0);
            intersectionPosition.set(p0);
            intersectionPosition.scaleAdd(closestTriangleResult.x, tempVector3f0, intersectionPosition);
            intersectionPosition.scaleAdd(closestTriangleResult.y, tempVector3f1, intersectionPosition);
            
            // Compute the texture coordinates from the intersection 
            // position
            TexCoord2f t0 = new TexCoord2f();
            TexCoord2f t1 = new TexCoord2f();
            TexCoord2f t2 = new TexCoord2f();
            geometry.getTexCoord(closestTriangleTexCoordIndices.x, t0);
            geometry.getTexCoord(closestTriangleTexCoordIndices.y, t1);
            geometry.getTexCoord(closestTriangleTexCoordIndices.z, t2);
            tempVector2f0.sub(t1, t0);
            tempVector2f1.sub(t2, t0);
            texCoord.set(t0);
            texCoord.scaleAdd(closestTriangleResult.x, tempVector2f0, texCoord);
            texCoord.scaleAdd(closestTriangleResult.y, tempVector2f1, texCoord);
        }
        return closestTriangleResult.z;
    }


    /**
     * Computes the intersection position of a triangle and a ray
     * 
     * @param p0 Point 0 of the triangle
     * @param p1 Point 1 of the triangle
     * @param p2 Point 2 of the triangle
     * @param origin Origin of the ray
     * @param dir Direction of the ray
     * @param result If there is an intersection, this afterwards
     * contains the intersection position (u,v,t) where (u,v) are 
     * barycentric coordinates, and t is the distance of the 
     * intersection along the ray
     * @return Whether there was an intersection
     */
    private boolean findIntersectionPosition(Point3f p0, Point3f p1, Point3f p2, Point3f origin, Vector3f dir, Point3f result)
    {
        result.set(0,0,0);
        tempEdge0.sub(p1, p0);
        tempEdge1.sub(p2, p0);
        tempVector3f0.cross(dir, tempEdge1);
        float a = tempEdge0.dot(tempVector3f0);
        if (a > -EPSILON && a < EPSILON)
        {
            return false;
        }
        float b = 1.0f / a;
        tempVector3f1.sub(origin, p0);
        float u = b * tempVector3f1.dot(tempVector3f0);
        if (u < 0.0f || u > 1.0f)
        {
            return false;
        }
        tempVector3f2.cross(tempVector3f1, tempEdge0);
        float v = b * dir.dot(tempVector3f2);
        if (v < 0.0f || v + u > 1.0f)
        {
            return false;
        }
        float t = b * tempEdge1.dot(tempVector3f2);
        result.set(u,v,t);
        return true;
    }

}
