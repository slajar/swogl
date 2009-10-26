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
 * Basic implementation of a Geometry. A DefaultGeometry simply
 * creates a rectangular triangle mesh with a specified number of 
 * points. The default vertex coordinates will range from 
 * (-width/2, -height/2) to (width/2,height/2), and the texture
 * coordinates will be in [0,1]x[0,1]. <br />
 * <br />
 * This class may contain a PointTransformer or a TexCoordTransformer.
 * These transformers will be used to transform each point or 
 * texture coordinate of this Geometry during an update. To cause
 * an update of the vertices or texture coordinates, 
 * triggerVertexUpdate() or triggerTexCoordUpdate() must be
 * called before the next call to update(). 
 */
public class DefaultGeometry extends AbstractGeometry implements Geometry
{
    /** 
     * The number of points in x direction
     */
    private int pointsX;

    /**
     * The number of points in y direction
     */
    private int pointsY;

    /**
     * The width of this geometry
     */
    private int width = 0;
    
    /**
     * The height of this geometry
     */
    private int height = 0;
    
    /**
     * The transformer for the vertices of this geometry.
     */
    private PointTransformer pointTransformer = null;
    
    /**
     * The transformer for the texture coordinates of this geometry.
     */
    private TexCoordTransformer texCoordTransformer = null;
    
    /**
     * A flag indicating whether the vertex positions of this
     * geometry should be recomputed during the next call to
     * 'update'
     */
    private boolean vertexUpdateRequired = false; 

    /**
     * A flag indicating whether the texture coordinates of this
     * geometry should be recomputed during the next call to
     * 'update'
     */
    private boolean texCoordUpdateRequired = false; 
    
    /**
     * Creates a new DefaultGeometry with the given size and number of points
     * 
     * @param width The size in x direction
     * @param height The size in y direction
     * @param pointsX The number of points in x direction (must be >=2)
     * @param pointsY The number of points in y direction (must be >=2)
     */
    public DefaultGeometry(int width, int height, int pointsX, int pointsY)
    {
        super(true);
        
        this.width = width;
        this.height = height;
        
        this.pointsX = pointsX;
        this.pointsY = pointsY;

        initIndices();
        initVertices();
        initNormals();
        initTexCoords();

        updateTexCoords();
        updateVertices(width, height);
        updateNormals();
    }
    
    /**
     * Returns the number of points of this DefaultGeometry in x-direction
     * 
     * @return The number of points of this DefaultGeometry in x-direction
     */
    public int getNumPointsX()
    {
        return pointsX;
    }
    
    
    /**
     * Returns the number of points of this DefaultGeometry in y-direction
     * 
     * @return The number of points of this DefaultGeometry in y-direction
     */
    public int getNumPointsY()
    {
        return pointsY;
    }

    /**
     * Returns the width of this DefaultGeometry
     * 
     * @return The width of this DefaultGeometry
     */
    public int getWidth()
    {
        return width;
    }
    
    /**
     * Returns the height of this DefaultGeometry
     * 
     * @return The height of this DefaultGeometry
     */
    public int getHeight()
    {
        return height;
    }
    

    /**
     * Initialize the indices, so that three consecutive indices
     * are the indices of the vertices of one triangle
     */
    private void initIndices()
    {
        for (int sx = 0; sx < pointsX - 1; sx++)
        {
            for (int sy = 0; sy < pointsY - 1; sy++)
            {
                int i0 = sy + sx * pointsY;
                int i1 = sy + (sx + 1) * pointsY;
                int i2 = (sy + 1) + (sx + 1) * pointsY;
                addTriangle(i0, i1, i2);

                i0 = sy + sx * pointsY;
                i1 = (sy + 1) + (sx + 1) * pointsY;
                i2 = (sy + 1) + sx * pointsY;
                addTriangle(i0, i1, i2);
            }
        }
    }
    
    /**
     * Initialize the vertices and the verticesBuffer with
     * vertices that are all at (0,0,0)
     */
    private void initVertices()
    {
        for (int x = 0; x < pointsX; x++)
        {
            for (int y = 0; y < pointsY; y++)
            {
                addVertex(0,0,0);
            }
        }
    }
    
    /**
     * Initialize the texture coordinates for this
     * geometry, so that they initially are all (0,0)
     */
    private void initTexCoords()
    {
        for (int sx = 0; sx < pointsX; sx++)
        {
            for (int sy = 0; sy < pointsY; sy++)
            {
                addTexCoord(0,0);
            }
        }
    }
    

    /**
     * Update the vertex positions according to the given
     * width and height, so that they form a plane mesh.
     * (But note that if a pointTransformer has been set
     * the mesh may actually have an arbitrary shape
     * after this call) 
     * 
     * @param width The width for the mesh
     * @param height The height of the mesh
     */
    private synchronized void updateVertices(int width, int height)
    {
        Point3f vertex = new Point3f();
        int x = 0;
        int y = 0;
        float stepX = 1.0f / (pointsX - 1);
        float stepY = 1.0f / (pointsY - 1);
        for (int sx = 0; sx < pointsX; sx++)
        {
            for (int sy = 0; sy < pointsY; sy++)
            {
                float relX = sx * stepX;
                float relY = sy * stepY;
                
                float posX = -0.5f + relX;
                float posY = -0.5f + relY;

                int index = sx * pointsY + sy;

                vertex.x = x + posX * width;
                vertex.y = y + posY * height;
                vertex.z = 0;
                
                if (pointTransformer != null)
                {
                    pointTransformer.transformPoint(vertex);
                }

                setVertex(index, vertex);
            }
        }
        setModified();
    }

    /**
     * Trigger a recomputation of the vertex positions for the
     * next time this component is updated and rendered.
     */
    public synchronized void triggerVertexUpdate()
    {
        vertexUpdateRequired = true;
    }

    /**
     * Trigger a recomputation of the texture coordinates for 
     * the next time this component is updated and rendered.
     */
    public synchronized void triggerTexCoordUpdate()
    {
        texCoordUpdateRequired = true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void update()
    {
        if (vertexUpdateRequired)
        {
            updateVertices(width, height);
            vertexUpdateRequired = false;
        }
        if (texCoordUpdateRequired)
        {
            updateTexCoords();
            texCoordUpdateRequired = false;
        }
        super.update();
    }
    
    
    

    /**
     * Initialize the texture coordinates, so that they range
     * from (0,0) to (1,1). (But note that if a TexCoordTransformer
     * has been set, the texture coordinates may be transformed
     * arbitrarily during this call).
     */
    protected void updateTexCoords()
    {
        TexCoord2f texCoord = new TexCoord2f();
        float dx = 1.0f / (pointsX - 1);
        float dy = 1.0f / (pointsY - 1);
        for (int sx = 0; sx < pointsX; sx++)
        {
            for (int sy = 0; sy < pointsY; sy++)
            {
                texCoord.x = sx * dx;
                texCoord.y = 1 - (sy * dy);
                
                int index = sx * pointsY + sy;
                
                if (texCoordTransformer != null)
                {
                    texCoordTransformer.transformTexCoord(texCoord);
                }

                setTexCoord(index, texCoord);
            }
        }
    }

    /**
     * Set the given PointTransformer for this DefaultGeometry.
     * If the given PointTransformer is non-null, all vertices
     * of this geometry will be transformed with the given
     * PointTransformer during the next update.
     * 
     * @param pointTransformer The PointTransformer to set
     */
    public synchronized void setPointTransformer(PointTransformer pointTransformer)
    {
        this.pointTransformer = pointTransformer;
        triggerVertexUpdate();
    }

    /**
     * Set the given TexCoordTransformer for this DefaultGeometry.
     * If the given TexCoordTransformer is non-null, all texture
     * coordinates of this geometry will be transformed with the 
     * given PointTransformer during the next update.
     * 
     * @param texCoordTransformer The TexCoordTransformer to set
     */
    public synchronized void setTexCoordTransformer(TexCoordTransformer texCoordTransformer)
    {
        this.texCoordTransformer = texCoordTransformer;
        triggerTexCoordUpdate();

    }
    
    

}
