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

import java.io.IOException;
import java.io.InputStream;

import javax.vecmath.*;

import de.javagl.utils.obj.*;
import de.javagl.utils.obj.impl.*;

import swogl.BoundingBox;

/**
 * This is an implementation of the Geometry interface that allows reading
 * simple OBJ data from an InputStream. Currently, only a subset of the 
 * possible contents of an OBJ file is supported: 
 * <ul>
 *     <li>Vertices</li>
 *     <li>Normals</li>
 *     <li>Texture coordinates</li>
 *     <li>Faces</li>
 * </ul>
 * The faces will automatically converted to triangles which will be 
 * stored in this Geometry.<br/>
 * <br/>
 * <strong>This class should be considered as PRELIMINARY and might
 * change in future releases</strong>
 */
public class ObjGeometry extends AbstractGeometry implements Geometry
{
    /**
     * Creates a new ObjGeometry that contains the OBJ Geometry that was
     * read from the given InputStream.
     * 
     * @param objInputStream The OBJ data to read
     * @param autoComputeNormals Whether the normals should be read
     * or computed automatically
     * @throws IOException When an I/O-error occurs.
     */
    public ObjGeometry(InputStream objInputStream, final boolean autoComputeNormals) throws IOException
    {
        super(autoComputeNormals);
        
        ObjReader reader = new DefaultObjReader();
        DefaultObj obj = new DefaultObj();
        reader.read(objInputStream, obj);
        ObjUtils.triangulate(obj, new GeometryObjTarget());
        
        if (isAutoComputeNormals())
        {
            initNormals();
            updateNormals();
        }
    }
    
    
    /**
     * Inner class implementing the ObjTarget interface. An instance
     * of this class will be used to receive the OBJ data and store
     * it in this Geometry
     */
    private class GeometryObjTarget implements ObjTarget
    {
        public void addFace(ObjFace face)
        {
            int vi0 = face.getVertexIndex(0);
            int vi1 = face.getVertexIndex(1);
            int vi2 = face.getVertexIndex(2);
            int ti0 = vi0;
            int ti1 = vi1;
            int ti2 = vi2;
            int ni0 = vi0;
            int ni1 = vi1;
            int ni2 = vi2;
            if (face.containsTexCoordIndices())
            {
                ti0 = face.getTexCoordIndex(0);
                ti1 = face.getTexCoordIndex(1);
                ti2 = face.getTexCoordIndex(2);
            }
            if (!isAutoComputeNormals() && face.containsNormalIndices())
            {
                ni0 = face.getNormalIndex(0);
                ni1 = face.getNormalIndex(1);
                ni2 = face.getNormalIndex(2);
                ObjGeometry.this.setAutoComputeNormals(false);
            }
            ObjGeometry.this.addTriangle(vi0, ti0, ni0, vi1, ti1, ni1, vi2, ti2, ni2);
        }

        public void addGroup(ObjGroup group)
        {
        }

        public void addMaterialGroup(ObjGroup group)
        {
        }

        public void addNormal(FloatTuple normal)
        {
            if (!isAutoComputeNormals())
            {
                ObjGeometry.this.addNormal(normal.getX(), normal.getY(), normal.getZ());
            }
        }

        public void addTexCoord(FloatTuple texCoord)
        {
            ObjGeometry.this.addTexCoord(texCoord.getX(), texCoord.getY());
        }

        public void addVertex(FloatTuple vertex)
        {
            ObjGeometry.this.addVertex(vertex.getX(), vertex.getY(), vertex.getZ());
        }

        public void setMtlFileName(String mtlFileName)
        {
        }
    }
    
    
    
    /**
     * Project the texture coordinates from [0,0] to [1,1] in the given
     * direction onto this Geometry.<br />
     * <br />
     * <strong>This method should be considered as PRELIMINARY and may
     * change in future releases!</strong>
     */
    public void projectTexCoords(Vector3f axis, float angle)
    {
        Vector3f angleAxis = new Vector3f(axis.x, axis.y, -axis.z);
        Vector3f zAxis = new Vector3f(0,0,1);
        float alignmentAngle = angleAxis.angle(zAxis);
        Vector3f alignmentRotationAxis = new Vector3f();
        alignmentRotationAxis.cross(zAxis, angleAxis);
        float epsilon = 1e-4f;
        if (alignmentRotationAxis.length() < epsilon)
        {
            alignmentRotationAxis.set(0,1,0);
        }
        if (alignmentAngle > epsilon)
        {
            GeometryUtils.rotate(this, alignmentRotationAxis, alignmentAngle);
        }
        GeometryUtils.rotate(this, axis, -angle);
        
        //System.out.println("alignmentAngle "+Math.toDegrees(alignmentAngle)+
        //    " rotationAxis "+alignmentRotationAxis);
        
        Point3f vertex = new Point3f();
        TexCoord2f texCoord = new TexCoord2f();
        BoundingBox boundingBox = GeometryUtils.computeBoundingBox(this);
        for (int i=0; i<this.getNumTriangles(); i++)
        {
            int index = i * 3;
            for (int j=0; j<3; j++)
            {
                int vertexIndex = getVertexIndex(index);
                int texCoordIndex = getTexCoordIndex(index);
                
                getVertex(vertexIndex, vertex);

                float x = ((vertex.x - boundingBox.getMinX()) / boundingBox.getSizeX());
                float y = ((vertex.y - boundingBox.getMinY()) / boundingBox.getSizeY());
                texCoord.x = x;
                texCoord.y = 1.0f-y;
                setTexCoord(texCoordIndex, texCoord);

                index++; 
            }
        }
        
        //GeometryUtils.rotate(this, axis, angle);
        if (alignmentAngle > epsilon)
        {
            //GeometryUtils.rotate(this, alignmentRotationAxis, -alignmentAngle);
        }
    }
    
    
}
