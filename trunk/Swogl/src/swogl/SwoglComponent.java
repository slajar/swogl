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

import java.awt.*;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JPanel;
import javax.vecmath.*;

import swogl.geometry.*;

import com.sun.opengl.util.j2d.TextureRenderer;
import com.sun.opengl.util.texture.Texture;

/**
 * This class represents a geometric 3D object whose texture is a 
 * Swing Component. Instances of this class may be added to a 
 * SwoglContainer, to display and interact with Swing Components 
 * in 3D.
 */
public class SwoglComponent
{
    /**
     * Indicates whether the GL_ARB_texture_non_power_of_two is available,
     * i.e. whether texture sizes may be integers that are not a power
     * of two.
     */
    private static boolean hasNonPow2Extension = false;
    
    /**
     * Indicates whether the GL_ARB_texture_non_power_of_two extension
     * has already been queried
     */
    private static boolean queriedNonPow2Extension = false;
	
	
    /**
     * The TextureRenderer, which allows rendering a Swing 
     * component into a Texture
     */
    private TextureRenderer textureRenderer;
    
    /**
     * The Texture that is placed on the geometry, and is
     * actually provided by the TextureRenderer
     */
    private Texture texture;

    /**
     * The Geometry representing this SwoglComponent
     */
    private Geometry geometry;
    
    /**
     * The Picker used for picking the geometry of this SwoglComponent
     */
    private GeometryPicker geometryPicker = new GeometryPicker();
    
    /**
     * The picker used for sending picking rays through this SwoglComponent 
     */
    private Picker picker = new Picker();
    
    /**
     * The panel holding all Swing Components that are displayed
     * on this SwoglComponent
     */
    private JPanel containerPanel;

    /**
     * The current transform used to arrange this SwoglComponent
     * in space
     */
    private Matrix4f transform = new Matrix4f();
    
    /**
     * Array for passing the transform to JOGL
     */
    private float transformArray[] = new float[16];

    /**
     * The Rectangle indicating which regions of the Texture have
     * to be updated by the TextureRenderer
     */
    private Rectangle dirtyRectangle = null;

    /**
     * The BoundingBox of the geometry of this SwoglComponent
     * (after it has been transformed with the current transform)
     */
    private BoundingBox boundingBox;

    /**
     * The parent of this SwoglComponent. Another SwoglComponent
     * that provides a transform for the origin of this one
     */
    private SwoglComponent parent = null;
    
    /**
     * The owner of this SwoglComponent
     */
    private SwoglContainer owner = null;

    
    /**
     * Creates a new SwoglComponent with the given size. The size
     * is also the size in pixels that is available for the
     * Swing Components that are displayed in this SwoglComponent
     *  
     * @param width The width of this SwoglComponent
     * @param height The height of this SwoglComponent
     */
    public SwoglComponent(int width, int height)
    {
        this(width, height, (SwoglComponent)null);
    }
    
    /**
     * Creates a new SwoglComponent with the given size and
     * the given parent.
     * 
     * @param width The width of this SwoglComponent
     * @param height The height of this SwoglComponent
     * @param parent The parent of this SwoglComponent
     */
    SwoglComponent(int width, int height, SwoglComponent parent)
    {
        this(width, height, new DefaultGeometry(width, height, 2, 2), parent);
    }

    /**
     * Creates a new SwoglComponent with the given size and the
     * given geometry
     * 
     * @param width The width of this SwoglComponent
     * @param height The height of this SwoglComponent
     * @param geometry The geometry of this SwoglComponent
     */
    public SwoglComponent(int width, int height, Geometry geometry)
    {
        this(width, height, geometry, null);
    }
    
    /**
     * Creates a new SwoglComponent with the given size, geometry and parent
     * @param width The width of this SwoglComponent
     * @param height The height of this SwoglComponent
     * @param geometry The geometry of this SwoglComponent
     * @param parent The parent of this SwoglComponent
     */
    SwoglComponent(int width, int height, Geometry geometry, SwoglComponent parent)
    {
        this.geometry = geometry;
        this.parent = parent;

        containerPanel = new JPanel();
        setContentSize(width, height);
        geometry.update();
        transform.setIdentity();
    }

    /**
     * Set the owner of this SwoglComponent
     * 
     * @param newOwner The owner of this SwoglComponent
     */
    void setOwner(SwoglContainer newOwner)
    {
        this.owner = newOwner;
    }
    
    /**
     * Causes a repaint of the owner of this SwoglComponent, and thus 
     * an implicit repaint of this SwoglComponent
     */
    public void repaint()
    {
        if (owner != null)
        {
            owner.repaint();
        }
    }
    
    
    /**
     * Returns whether this SwoglComponent is a Popup (and thus
     * linked to another SwoglComponent). 
     * 
     * @return Whether this SwogComponent is a Popup
     */
    boolean isPopup()
    {
        return parent != null;
    }
    
    
    /**
     * Set the size of the contents of this SwoglComponent, i.e. the size
     * of the area available for Swing Components, in pixels
     * 
     * @param width The width of the content
     * @param height The height of the content
     */
    public void setContentSize(int width, int height)
    {
        width = Math.max(1, width);
        height = Math.max(1, height);
        Dimension dim = new Dimension(width, height);
        containerPanel.setPreferredSize(dim);
        containerPanel.setMaximumSize(dim);
        containerPanel.setMinimumSize(dim);
        containerPanel.setSize(dim);
        containerPanel.validate();
    }

    /**
     * Returns the size of the contents of this SwoglComponent, i.e. the
     * size of the area a available for Swing Components, in pixels
     * 
     * @return The size of the contents of this SwoglComponent
     */
    public Dimension getContentSize()
    {
        return containerPanel.getPreferredSize();
    }

    /*
     * Workaround for disappearing textures on GLJPanel, used by SwoglContainer
     */
    void reinitTextures()
    {
        textureRenderer = null;
    }

    /**
     * Returns the panel containing all Swing Components that are
     * displayed by this SwoglComponent
     * 
     * @return The panel containing all Swing Components
     */
    JPanel getContainerPanel()
    {
        return containerPanel;
    }

    /**
     * Delegates the call to the Swing Component container. The
     * Swing components in this SwoglComponent will be laid out
     * with the given LayoutManager
     * 
     * @param layoutManager The LayoutManager for this SwoglComponent
     */
    public void setLayout(LayoutManager layoutManager)
    {
        containerPanel.setLayout(layoutManager);
    }

    /**
     * Delegates the call to the Swing Component container. Will
     * add the given Swing component to this SwoglComponent.
     * 
     * @param component The component to add
     */
    public void add(Component component)
    {
        containerPanel.add(component);
    }

    /**
     * Delegates the call to the Swing Component container. Will
     * add the given Swing Component at the given position.
     * 
     * @param component The component to add
     * @param index The position at which to insert the component, 
     * or -1 to append the component to the end
     */
    public void add(Component component, int index)
    {
        containerPanel.add(component, index);
    }

    /**
     * Delegates the call to the Swing Component container. This
     * adds the given component to this SwoglComponent, using
     * the given layout constraints.
     * 
     * @param component The component to add
     * @param constraints An object expressing layout constraints 
     * for this component
     */
    public void add(Component component, Object constraints)
    {
        containerPanel.add(component, constraints);
    }

    /**
     * Set the transform of this SwoglComponent, which positions
     * this SwoglComponent in 3D space
     * 
     * @param trans The transform for this SwoglComponent
     */
    public void setTransform(Matrix4f trans)
    {
        //System.out.println("Transform of "+this+" is\n"+trans);
        this.transform.set(trans);
        boundingBox = null;
    }

    /**
     * Writes the current transform of this SwoglComponent 
     * into the given argument
     * 
     * @param trans Will store the current transform of this
     * SwoglComponent
     */
    public void getTransform(Matrix4f trans)
    {
        trans.set(transform);
    }

    /**
     * Returns the BoundingBox of this SwoglComponent after it has
     * been transformed with the current transform.
     * 
     * @return The BoundingBox of this SwoglComponent
     */
    public BoundingBox getBoundingBox()
    {
        if (boundingBox == null)
        {
            boundingBox = new BoundingBox();
            Point3f point = new Point3f();
            for (int i=0; i<geometry.getNumVertices(); i++)
            {
                geometry.getVertex(i, point);
                transform.transform(point);
                boundingBox.combine(point);
            }
        }
        return new BoundingBox(boundingBox); 
    }

    /**
     * Returns the BoundingBox of this SwoglComponent before
     * the current transform has been applied
     * 
     * @return The BoundingBox of this (untransformed) SwoglComponent
     */
    public BoundingBox getUntransformedBoundingBox()
    {
        BoundingBox bb = new BoundingBox();
        Point3f point = new Point3f();
        for (int i=0; i<geometry.getNumVertices(); i++)
        {
            geometry.getVertex(i, point);
            bb.combine(point);
        }
        return bb;
    }

    /**
     * Marks the specified area on the given component as "dirty", which 
     * causes the TextureRenderer to update this area
     * 
     * @param component The component which may be dirty
     * @param x The x-position of the dirty rectangle
     * @param y The y-position of the dirty rectangle
     * @param w The x-size of the dirty rectangle
     * @param h The y-size of the dirty rectangle
     */
    void addDirtyRegion(Component component, int x, int y, int w, int h)
    {
        Rectangle rectangle = new Rectangle(0, 0, 
        	containerPanel.getWidth(), containerPanel.getHeight());
        
        /** 
         * TODO This should be optimized by taking the given region 
         * into account, but currently this has low priority...
         * (Note that there may be some problems with JScrollPanes)
         * 
        Rectangle converted = null;
        if (component instanceof JScrollPane)
        {
            JScrollPane scrollPane = (JScrollPane)component;
            rectangle = new Rectangle(0, 0, scrollPane.getWidth(), scrollPane.getHeight());
        }
        else
        {
            rectangle = new Rectangle(x, y, w, h);
        }

        System.out.println("Rectangle " + rectangle + " from " + component);
        converted = SwingUtilities.convertRectangle(component, rectangle, containerPanel);
        System.out.println("Converted " + converted);
        rectangle = converted;
        //*/

        if (dirtyRectangle == null)
        {
            dirtyRectangle = rectangle;
        }
        else
        {
            dirtyRectangle = dirtyRectangle.union(rectangle);
        }
        //System.out.println("dirtyRectangle "+dirtyRectangle);
    }

    /**
     * Updates the transformArray according to the current transform,
     * so that the transformArray may be used for glMultMatrixf 
     * calls for JOGL
     */
    private void updateTransformArray()
    {
        Matrix4f trans = computeGlobalTransform();
        
        transformArray[ 0] = trans.m00;
        transformArray[ 1] = trans.m10;
        transformArray[ 2] = trans.m20;
        transformArray[ 3] = trans.m30;
        transformArray[ 4] = trans.m01;
        transformArray[ 5] = trans.m11;
        transformArray[ 6] = trans.m21;
        transformArray[ 7] = trans.m31;
        transformArray[ 8] = trans.m02;
        transformArray[ 9] = trans.m12;
        transformArray[10] = trans.m22;
        transformArray[11] = trans.m32;
        transformArray[12] = trans.m03;
        transformArray[13] = trans.m13;
        transformArray[14] = trans.m23;
        transformArray[15] = trans.m33;
    }

    /**
     * Computes the global transform of this SwoglComponent from
     * its own transform and the global transforms of its parent
     * 
     * @return The global transform of this SwoglComponent
     */
    private Matrix4f computeGlobalTransform()
    {
        if (parent == null)
        {
            return new Matrix4f(transform);
        }
        else
        {
            Matrix4f globalTransform = parent.computeGlobalTransform();
            globalTransform.mul(transform);
            return globalTransform;
        }
    }
    
    /**
     * Will display this SwoglComponent on the given GLAutoDrawable
     * 
     * @param drawable The GLAutoDrawable to display this SwoglComponent on
     */
    void display(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();
        gl.glPushMatrix();

        updateTransformArray();
        gl.glMultMatrixf(transformArray, 0);
        
        picker.update(gl);

        // Query if textures with non-power-of-2-sizes are supported
        if (!queriedNonPow2Extension)
        {
            hasNonPow2Extension = gl.isExtensionAvailable(
            	"GL_ARB_texture_non_power_of_two");
            queriedNonPow2Extension = true;
        }
        
        // Assert that the textureRenderer provides a texture with
        // the preferred size of the containerPanel
        int w = containerPanel.getPreferredSize().width;
        int h = containerPanel.getPreferredSize().height;
        if (textureRenderer == null || 
        	textureRenderer.getWidth() != w || 
        	textureRenderer.getHeight() != h)
        {
            if (texture != null)
            {
                texture.dispose();
                texture = null;
            }
            if (hasNonPow2Extension)
            {
                textureRenderer = new TextureRenderer(w, h, true);
            }
            else
            {
                textureRenderer = 
                	new TextureRenderer(nextPow2(w), nextPow2(h), true);
            }
            if (dirtyRectangle == null)
            {
                dirtyRectangle = new Rectangle(0, 0, 
                	containerPanel.getWidth(), containerPanel.getHeight());
            }
        }

        // Paint the containerPanel into the texture
        if (dirtyRectangle != null)
        {
            Graphics2D g = textureRenderer.createGraphics();

            // Scale the rendering from the power-of-2-size to the
            // actual size, if necessary
            if (!hasNonPow2Extension)
            {
                float scaleX = (float)textureRenderer.getWidth() / w;
                float scaleY = (float)textureRenderer.getHeight() / h;
                g.scale(scaleX, scaleY);
                g.setRenderingHint(
                	RenderingHints.KEY_INTERPOLATION,
                	RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            }

            containerPanel.paint(g);

            textureRenderer.markDirty(
            	dirtyRectangle.x, dirtyRectangle.y, 
            	dirtyRectangle.width, dirtyRectangle.height);
            dirtyRectangle = null;

            texture = textureRenderer.getTexture();
        }
        texture.enable();
        texture.bind();

        geometry.update();

        // Paint the geometry with the current texture
        Point3f vertex = new Point3f();
        Vector3f normal = new Vector3f();
        TexCoord2f texCoord = new TexCoord2f();
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
        gl.glBegin(GL.GL_TRIANGLES);
        for (int i = 0; i < geometry.getNumTriangles(); i++)
        {
            int index = i * 3;
            for (int j=0; j<3; j++)
            {
                int vertexIndex = geometry.getVertexIndex(index);
                int texCoordIndex = geometry.getTexCoordIndex(index);
                int normalIndex = geometry.getNormalIndex(index);
                
                geometry.getVertex(vertexIndex, vertex);
                geometry.getTexCoord(texCoordIndex, texCoord);
                geometry.getNormal(normalIndex, normal);

                gl.glTexCoord2f(texCoord.x, texCoord.y);
                gl.glNormal3f(normal.x, normal.y, normal.z);
                gl.glVertex3f(vertex.x, vertex.y, vertex.z);

                index++; 
            }
        }
        gl.glEnd();

        gl.glPopMatrix();
    }


    /**
     * Computes the intersection between a picking ray and
     * the geometry of this SwoglComponent.
     *  
     * @param rayOrigin The origin of the picking ray
     * @param rayDirection The direction of the picking ray
     * @param intersectionPosition This will contain the position of the 
     * point where the ray intersects the geometry, if such an intersection
     * exists
     * @param texCoord This will contain the texture coordinates of the
     * intersection point, if such an intersection exists
     * @return The distance of the intersection along the picking ray.
     * May be Float.POSITIVE_INFINITY if no intersection exists
     */
    float findIntersectionPosition(
                    Point3f rayOrigin, Vector3f rayDirection, 
                    Point3f intersectionPosition, TexCoord2f texCoord)
    {
        float intersectionDistance = 
            geometryPicker.findIntersectionPosition(
                geometry, rayOrigin, rayDirection, intersectionPosition, texCoord);
        transform.transform(intersectionPosition);
        return intersectionDistance;
    }
    

    /**
     * Computes the picking ray that is implied by sending a ray through
     * the given 'point' (in screen coordinates). The result will be 
     * stored in the given arguments.
     * 
     * @param point The point where the ray should start
     * @param rayOrigin Will contain the origin of the picking ray
     * @param rayDirection Will contain the normalized direction of the picking ray
     */
    void computePickingRayData(Point point, Point3f rayOrigin, Vector3f rayDirection)
    {
        picker.computePickingRayData(point.x, point.y, rayOrigin, rayDirection);
    }
    
    /**
     * Returns the Point on this SwoglComponent that corresponds
     * to the given location, which has to be in the range 
     * (0,0)-(1,1).
     * 
     * @param location The location for the point to return
     * @return The Point on this SwoglComponent that corresponds
     * to the given location
     */
    public Point getPointFor(Tuple2f location)
    {
        int x = (int) (containerPanel.getWidth() * location.x);
        int y = (int) (containerPanel.getHeight() * location.y);
        Point containerPoint = new Point(x,y);
        return containerPoint;
    }

    
    /**
     * Returns the smallest power of 2 that is greater than or equal to n
     * 
     * @param n The value for which the next power of 2 should be computed
     * @return The smallest power of 2 that is greater than or equal to n
     */
    private static int nextPow2(int n)
    {
        for (int i=1; i<32; i++)
        {
            int p = 1<<i;
            if (p >= n)
            {
                return p;
            }
        }
        return 1 << 31;
    }

    
}


