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

import java.awt.Point;
import java.awt.event.*;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.glu.GLU;
import javax.vecmath.*;

/**
 * A basic, preliminary implementation of a Camera, offering methods
 * for arcball rotation, moving and zooming, and for
 * applying the resulting transform matrix to the 
 * GL_PROJECTION matrix of a GL context
 */
class Camera implements MouseListener, MouseMotionListener, MouseWheelListener
{
    /**
     * GLU helper
     */
    private static final GLU glu = new GLU();

    /**
     * The initial eye position
     */
    private Point3f initialEyePoint = new Point3f(0, 0, 0);

    /**
     * The initial view point
     */
    private Point3f initialViewPoint = new Point3f(0, 0, 0);

    /**
     * The initial up vector
     */
    private Vector3f initialUpVector = new Vector3f(0, 1, 0);

    /**
     * The current eye position
     */
    private Point3f eyePoint = new Point3f(initialEyePoint);

    /**
     * The current view point
     */
    private Point3f viewPoint = new Point3f(initialViewPoint);

    /**
     * The current up vector
     */
    private Vector3f upVector = new Vector3f(initialUpVector);

    /**
     * The current FOV in y-direction, in degrees
     */
    private float fovDegree = 60.0f;

    /**
     * The current size of the GL viewport
     */
    private float viewportWidth = 500;

    /**
     * The current size of the GL viewport
     */
    private float viewportHeight = 500;

    /**
     * The previous mouse position
     */
    private Point previousMousePosition = new Point();

    /**
     * The Quaternion describing the rotation when dragging started
     */
    private Quat4f dragStartRotation = new Quat4f(0, 0, 0, 1);

    /**
     * The Quaternion describing the current rotation
     */
    private Quat4f currentRotation = new Quat4f(0, 0, 0, 1);

    /**
     * The position in 3D space where dragging started
     */
    private Vector3f dragStartPosition = new Vector3f();

    /**
     * The current position in 3D space
     */
    private Vector3f currentDragPosition = new Vector3f();

    /**
     * The default eye position
     */
    private Point3f defaultEyePoint = new Point3f(0, 0, 0);
    
    
    /**
     * Creates a new Camera
     */
    Camera()
    {
    }

    
    /**
     * Reset this camera to its initial configuration
     */
    void reset()
    {
        initialEyePoint.set(defaultEyePoint);
        eyePoint.set(initialEyePoint);
        viewPoint.set(initialViewPoint);
        upVector.set(initialUpVector);
        currentRotation.set(0,0,0,1);
    }
    
    /**
     * Set the current size of the GL viewport
     * 
     * @param w Width of the viewport
     * @param h Height of the viewport
     */
    void setViewportSize(int w, int h)
    {
        viewportWidth = w;
        viewportHeight = h;

        initialEyePoint.z = h / (float) (2 * Math.tan(Math.toRadians(fovDegree / 2)));
        defaultEyePoint.z = initialEyePoint.z;
        //System.out.println("Eye distance " + initialEyePoint.z);
        updateRotation();
    }

    /**
     * Apply the current setting of this camera to the given GLAutoDrawable,
     * by setting the GL_PROJECTION matrix of the associated GL context
     * accordingly
     * 
     * @param drawable The GLAutoDrawable to apply this camera to
     */
    void apply(GLAutoDrawable drawable)
    {
        GL gl = drawable.getGL();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        float aspect = (float) drawable.getWidth() / drawable.getHeight();
        glu.gluPerspective(fovDegree, aspect, 1, 10000);

        glu.gluLookAt(eyePoint.x, eyePoint.y, eyePoint.z,
                      viewPoint.x, viewPoint.y, viewPoint.z,
                      upVector.x, upVector.y, upVector.z);


        gl.glMatrixMode(GL.GL_MODELVIEW);
    }

    /**
     * {@inheritDoc}
     */
    public void mousePressed(MouseEvent e)
    {
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
        {
            startArcballRotate(e.getPoint());
        }
        if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK)
        {
            startMovement(e.getPoint());
        }
    }

    /**
     * {@inheritDoc}
     */
    public void mouseDragged(MouseEvent e)
    {
        if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK)
        {
            doArcballRotate(e.getPoint());
        }
        if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK)
        {
            doMovement(e.getPoint());
        }
    }

    /**
     * Called when the arcball rotation starts at the given point
     * 
     * @param point The point where the arcball rotation starts, in screen coordinates
     */
    private void startArcballRotate(Point point)
    {
        mapOnArcball(point.x, point.y, dragStartPosition);
        dragStartRotation = new Quat4f(currentRotation);
        //System.out.println("start " + dragStartPosition);
    }

    /**
     * Called when the arcball rotation continues to the given point
     * 
     * @param point The current mouse position, screen coordinates
     */
    private void doArcballRotate(Point point)
    {
        mapOnArcball(point.x, point.y, currentDragPosition);
        float dot = dragStartPosition.dot(currentDragPosition);
        Vector3f tmp = new Vector3f();
        tmp.cross(dragStartPosition, currentDragPosition);

        //System.out.println("currentRotation "+currentRotation);

        Quat4f q = new Quat4f(tmp.x, tmp.y, tmp.z, dot);
        currentRotation = new Quat4f();
        currentRotation.mul(q, dragStartRotation);

        //System.out.println(currentMatrix);

        updateRotation();
    }

    /**
     * Update the eyePoint and upVector according to the current rotation
     */
    private void updateRotation()
    {
        Matrix4f currentMatrix = new Matrix4f();
        currentMatrix.set(currentRotation);
        currentMatrix.transpose();
        Vector3f temp = new Vector3f();
        temp.sub(initialEyePoint, initialViewPoint);
        currentMatrix.transform(temp);
        currentMatrix.transform(initialUpVector, upVector);
        eyePoint.add(viewPoint, temp);
    }

    /**
     * Maps the given point onto the arcball
     * 
     * @param x The x-screen coordinate of the point
     * @param y The y-screen coordinate of the point
     * @param mappedPoint The point on the arcball in 3D
     */
    private void mapOnArcball(int x, int y, Vector3f mappedPoint)
    {
        Vector2f temp = new Vector2f();
        temp.x = (x / (viewportWidth / 2)) - 1.0f;
        temp.y = -((y / (viewportHeight / 2)) - 1.0f);
        float length = temp.length();
        if (length > 1.0f)
        {
            mappedPoint.x = temp.x / length;
            mappedPoint.y = temp.y / length;
            mappedPoint.z = 0.0f;
        }
        else
        {
            mappedPoint.x = temp.x;
            mappedPoint.y = temp.y;
            mappedPoint.z = (float) Math.sqrt(1.0f - length);
        }
    }

    /**
     * Called when the movement starts at the given point
     * 
     * @param point The point where the movement starts, in screen coordinates
     */
    private void startMovement(Point point)
    {

    }

    /**
     * Called when the movement continues to the given point
     * 
     * @param point The current mouse position, screen coordinates
     */
    private void doMovement(Point point)
    {
        Vector3f delta = new Vector3f();
        delta.x = previousMousePosition.x - point.x;
        delta.y = point.y - previousMousePosition.y;

        Matrix4f currentMatrix = new Matrix4f();
        currentMatrix.setIdentity();
        currentMatrix.set(currentRotation);
        currentMatrix.transpose();
        currentMatrix.transform(delta);

        eyePoint.add(delta);
        viewPoint.add(delta);
        previousMousePosition = point;
    }


    /**
     * {@inheritDoc}
     */
    public void mouseEntered(MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void mouseExited(MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void mouseReleased(MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void mouseClicked(MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON2)
        {
            reset();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void mouseMoved(MouseEvent e)
    {
        previousMousePosition = e.getPoint();
    }

    /**
     * {@inheritDoc}
     */
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        Vector3f delta = new Vector3f();
        delta.z = - e.getWheelRotation() * 100;
        
        delta.z = Math.max(delta.z, - initialEyePoint.z + 1);
        initialEyePoint.add(delta);

        Matrix4f currentMatrix = new Matrix4f();
        currentMatrix.setIdentity();
        currentMatrix.set(currentRotation);
        currentMatrix.transpose();
        currentMatrix.transform(delta);

        eyePoint.add(delta);
    }
}
