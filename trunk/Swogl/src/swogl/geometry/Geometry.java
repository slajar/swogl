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

/**
 * Interface for all classes that provide geometry information
 */
public interface Geometry
{
    /**
     * Returns the number of triangles in this Geometry
     * 
     * @return The number of triangles in this Geometry
     */
    int getNumTriangles();

    /**
     * Returns the specified vertex index. Three consecutive indices
     * are the indices of the vertices of a single triangle
     * 
     * @param index The number of the index to return
     * @return The index
     */
    int getVertexIndex(int index);

    /**
     * Returns the specified texture coordinate index. Three 
     * consecutive indices are the indices of the texture
     * coordinates of the vertices of a single triangle
     * 
     * @param index The number of the index to return
     * @return The index
     */
    int getTexCoordIndex(int index);
    
    /**
     * Returns the specified normal index. Three 
     * consecutive indices are the indices of the normals of
     * the vertices of a single triangle
     * 
     * @param index The number of the index to return
     * @return The index
     */
    int getNormalIndex(int index);
    
    /**
     * Returns the number of vertices in this Geometry
     * 
     * @return The number of vertices in this Geometry
     */
    int getNumVertices();

    /**
     * Stores the vertex with the given index in the given argument
     * 
     * @param index The index of the vertex
     * @param vertex Will store the vertex
     */
    void getVertex(int index, Tuple3f vertex);

    /**
     * Set the coordinates of the vertex with the specified index
     *  
     * @param index The index of the vertex
     * @param vertex The coordinates the vertex should have
     */
    void setVertex(int index, Tuple3f vertex);

    /**
     * Stores the normal with the given index in the given argument
     * 
     * @param index The index of the vertex whose normal should be obtained
     * @param normal Will store the normal
     */
    void getNormal(int index, Tuple3f normal);

    /**
     * Sets the normal of the vertex with the given index
     * 
     * @param index The index of the vertex whose normal should be set
     * @param normal The normal to set for the vertex
     */
    void setNormal(int index, Tuple3f normal);

    /**
     * Stores the texture coordinates of the vertex with the given
     * index in the given argument
     * 
     * @param index The index of the vertex whose texture coordinates 
     * should be obtained
     * @param texCoord Will store the texture coordinates
     */
    void getTexCoord(int index, Tuple2f texCoord);

    /**
     * Set the texture coordinates of the vertex with the given index
     * 
     * @param index The index of the vertex whose texture coordinates 
     * should be set
     * @param texCoord The texture coordinates to set
     */
    void setTexCoord(int index, Tuple2f texCoord);

    /**
     * Will update this Geometry. This may, for example, compute the 
     * normals according to the current vertex positions.
     */
    void update();
}
