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

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.*;

/**
 * Abstract implementation of a Geometry, containing methods
 * for storing geometry data, and for computing "default" 
 * normals for the vertices by taking the average of the 
 * normals of the adjacent triangles
 */
public abstract class AbstractGeometry implements Geometry
{
    /**
     * The indices of this Geometry
     */
    private List<Integer> vertexIndices = new ArrayList<Integer>();

    /**
     * The texture coordinate indices of this Geometry
     */
    private List<Integer> texCoordIndices = new ArrayList<Integer>();

    /**
     * The normal indices of this Geometry
     */
    private List<Integer> normalIndices = new ArrayList<Integer>();

    /**
     * The vertices of this Geometry
     */
    private List<Point3f> vertices = new ArrayList<Point3f>();
    
    /**
     * The normals of this Geometry
     */
    private List<Vector3f> normals = new ArrayList<Vector3f>();
    
    /**
     * The texture coordinates of this Geometry
     */
    private List<TexCoord2f> texCoords = new ArrayList<TexCoord2f>();

    /**
     * For each vertex, this contains a list of indices of 
     * the triangles that the respective vertex belongs to
     */
    private List<List<Integer>> vertexTriangleIndices = new ArrayList<List<Integer>>();

    /**
     * The triangle normals of this Geometry
     */
    private ArrayList<Vector3f> triangleNormals = new ArrayList<Vector3f>();

    /**
     * Modification counter - will be used to determine whether
     * an update is required.
     */
    private int modCount = 0;
    
    /**
     * Indicates whether default normals should be computed
     */
    private boolean autoComputeNormals = true;
    
    /**
     * Creates a new AbstractGeometry
     * 
     * @param autoComputeNormals Indicates whether default normals should be computed
     */
    protected AbstractGeometry(boolean autoComputeNormals)
    {
        this.autoComputeNormals = autoComputeNormals;
    }
    
    /**
     * Creates a new AbstractGeometry
     */
    protected AbstractGeometry()
    {
    }
    
    /**
     * Whether the vertex normals of this geometry should be computed from
     * the face normals of the adjacent triangles during an update.
     * 
     * @param autoComputeNormals Whether the normals should be computed
     * automatically 
     */
    protected void setAutoComputeNormals(boolean autoComputeNormals)
    {
        this.autoComputeNormals = autoComputeNormals;
    }
    
    /**
     * Returns whether this geometry is automatically computing the
     * vertex normals from the face normals.
     * 
     * @return Whether the normals are computed automatically 
     */
    protected boolean isAutoComputeNormals()
    {
        return autoComputeNormals;
    }
    
    /**
     * Add the specified vertex to this geometry.
     * 
     * @param x The x-coordinate of the vertex
     * @param y The y-coordinate of the vertex
     * @param z The z-coordinate of the vertex
     */
    protected void addVertex(float x, float y, float z)
    {
        vertices.add(new Point3f(x,y,z));
    }
    
    /**
     * Add the specified texture coordinate to this geometry
     * 
     * @param x The x-coordinate
     * @param y The y-coordinate
     */
    protected void addTexCoord(float x, float y)
    {
        texCoords.add(new TexCoord2f(x,y));
    }

    /**
     * Adds the specified normal to this geometry
     * 
     * @param x The x-component of the normal
     * @param y The y-component of the normal
     * @param z The z-component of the normal
     */
    protected void addNormal(float x, float y, float z)
    {
        normals.add(new Vector3f(x,y,z));
    }

    /**
     * Mark this geometry as modified, so that during the
     * next call to 'update' the normals will be computed 
     * (if autoComputeNormals is true)
     */
    protected void setModified()
    {
        modCount++;
    }
    
    /**
     * Add the triangle consisting of the given indices to
     * this geometry. The same indices will be used for
     * vertices, texture coordinates and normals.
     * 
     * @param i0 The first index
     * @param i1 The second index
     * @param i2 The third index
     */
    protected void addTriangle(int i0, int i1, int i2)
    {
        vertexIndices.add(i0);
        vertexIndices.add(i1);
        vertexIndices.add(i2);
        texCoordIndices.add(i0);
        texCoordIndices.add(i1);
        texCoordIndices.add(i2);
        normalIndices.add(i0);
        normalIndices.add(i1);
        normalIndices.add(i2);
    }

    /**
     * Add the triangle consisting of the given indices to
     * this geometry. 
     * 
     * @param vi0 The first vertex index
     * @param ti0 The first texture coordinate index
     * @param ni0 The first normal index
     * @param vi1 The second vertex index
     * @param ti1 The second texture coordinate index
     * @param ni1 The second normal index
     * @param vi2 The third vertex index
     * @param ti2 The third texture coordinate index
     * @param ni2 The third normal index
     */
    protected void addTriangle(int vi0, int ti0, int ni0, int vi1, int ti1, int ni1, int vi2, int ti2, int ni2)
    {
        vertexIndices.add(vi0);
        vertexIndices.add(vi1);
        vertexIndices.add(vi2);
        texCoordIndices.add(ti0);
        texCoordIndices.add(ti1);
        texCoordIndices.add(ti2);
        normalIndices.add(ni0);
        normalIndices.add(ni1);
        normalIndices.add(ni2);
    }

    /**
     * Add the triangle consisting of the given indices to
     * this geometry. 
     * 
     * @param vi0 The first vertex index
     * @param ti0 The first texture coordinate index
     * @param vi1 The second vertex index
     * @param ti1 The second texture coordinate index
     * @param vi2 The third vertex index
     * @param ti2 The third texture coordinate index
     */
    protected void addTriangle(int vi0, int ti0, int vi1, int ti1, int vi2, int ti2)
    {
        vertexIndices.add(vi0);
        vertexIndices.add(vi1);
        vertexIndices.add(vi2);
        texCoordIndices.add(ti0);
        texCoordIndices.add(ti1);
        texCoordIndices.add(ti2);
        normalIndices.add(vi0);
        normalIndices.add(vi1);
        normalIndices.add(vi2);
    }
    

    /**
     * Initialize the normals of this Geometry, so that it
     * contains as many normals as vertices.
     */
    protected void initNormals()
    {
        normalIndices.clear();
        for (int i=0; i<vertexIndices.size(); i++)
        {
            normalIndices.add(vertexIndices.get(i));
        }
        
        vertexTriangleIndices.clear();
        for (int i = 0; i < vertices.size(); i++)
        {
            vertexTriangleIndices.add(new ArrayList<Integer>());
        }
        
        triangleNormals.clear();
        for (int i = 0; i < vertexIndices.size() / 3; i++)
        {
            int i0 = vertexIndices.get(i * 3 + 0);
            int i1 = vertexIndices.get(i * 3 + 1);
            int i2 = vertexIndices.get(i * 3 + 2);
            vertexTriangleIndices.get(i0).add(i);
            vertexTriangleIndices.get(i1).add(i);
            vertexTriangleIndices.get(i2).add(i);

            Vector3f triangleNormal = new Vector3f();
            triangleNormals.add(triangleNormal);
        }
        
        normals.clear();
        for (int i = 0; i < vertices.size(); i++)
        {
            Vector3f vertexNormal = new Vector3f();
            normals.add(vertexNormal);
        }

    }

    /**
     * Update the normals of this Geometry according to 
     * the current vertex positions
     */
    protected synchronized void updateNormals()
    {
        Vector3f edge0 = new Vector3f();
        Vector3f edge1 = new Vector3f();

        for (int i = 0; i < vertexIndices.size() / 3; i++)
        {
            int i0 = vertexIndices.get(i * 3 + 0);
            int i1 = vertexIndices.get(i * 3 + 1);
            int i2 = vertexIndices.get(i * 3 + 2);
            Point3f v0 = vertices.get(i0);
            Point3f v1 = vertices.get(i1);
            Point3f v2 = vertices.get(i2);
            edge0.sub(v1, v0);
            edge1.sub(v2, v0);
            Vector3f triangleNormal = triangleNormals.get(i);
            triangleNormal.cross(edge0, edge1);
            triangleNormal.normalize();
        }

        for (int i = 0; i < vertices.size(); i++)
        {
            Vector3f vertexNormal = normals.get(i);
            vertexNormal.set(0,0,0);
            for (int j = 0; j < vertexTriangleIndices.get(i).size(); j++)
            {
                int index = vertexTriangleIndices.get(i).get(j);
                Vector3f triangleNormal = triangleNormals.get(index);
                vertexNormal.add(triangleNormal);
            }
            vertexNormal.normalize();
        }
    }

    
    // === Implementation of Geometry:

    /**
     * {@inheritDoc}
     */
    public int getNumTriangles()
    {
        return vertexIndices.size() / 3;
    }

    /**
     * {@inheritDoc}
     */
    public int getVertexIndex(int index)
    {
        return vertexIndices.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public int getTexCoordIndex(int index)
    {
        return texCoordIndices.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public int getNormalIndex(int index)
    {
        return normalIndices.get(index);
    }

    /**
     * {@inheritDoc}
     */
    public void getNormal(int index, Tuple3f normal)
    {
        normal.set(normals.get(index));
    }

    /**
     * {@inheritDoc}
     */
    public void setNormal(int index, Tuple3f normal)
    {
        normals.get(index).set(normal);
    }

    /**
     * {@inheritDoc}
     */
    public int getNumVertices()
    {
        return vertices.size();
    }

    /**
     * {@inheritDoc}
     */
    public void getVertex(int index, Tuple3f vertex)
    {
        vertex.set(vertices.get(index));
    }

    /**
     * {@inheritDoc}
     */
    public void setVertex(int index, Tuple3f vertex)
    {
        vertices.get(index).set(vertex);
        modCount++;
    }

    /**
     * {@inheritDoc}
     */
    public void getTexCoord(int index, Tuple2f texCoord)
    {
        texCoord.set(texCoords.get(index));
    }

    /**
     * {@inheritDoc}
     */
    public void setTexCoord(int index, Tuple2f texCoord)
    {
        texCoords.get(index).set(texCoord);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void update()
    {
        if (modCount > 0)
        {
            if (autoComputeNormals)
            {
                updateNormals();
            }
            modCount = 0;
        }
    }
    
    

}
