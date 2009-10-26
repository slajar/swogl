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

package swogl.layout;

import java.awt.*;
import java.awt.event.*;

import javax.vecmath.*;

import swogl.*;

/**
 * An implementation of a LayoutManager3D, does not perform any 
 * specific layout, but allows moving around the SwoglComponents
 * arbitrarily.
 */
public class SpaceLayout3D extends AbstractLayout3D implements LayoutManager3D
{
    /**
     * Constant for the rotation speed when rotating SwoglComponents
     * with the mouse
     */
    private static final float ROTATION_SPEED = 0.5f;
    
    /**
     * Constant for the scaling speed when scaling SwoglComponents
     */
    private static final float SCALING_SPEED = 0.01f;
    
    /**
     * Constant for the mouse wheel movement speed, when moving
     * SwoglComponents in z-direction using the mouse wheel.
     */
    private static final float MOUSE_WHEEL_MOVEMENT_SPEED = 50.0f;
    
    
    /**
     * The PickingInfo that will be used for moving, scaling and
     * rotating SwoglComponents via mouse drags.
     */
    private PickingInfo draggingInfo = null;
    
    /**
     * The current mouse position
     */
    private Point mousePosition = new Point();
    
    /**
     * The intersection position of the current picking ray
     * with the currently dragged SwoglComponent
     */
    private Point3f currentIntersectionPosition = new Point3f();
    
    
    /**
     * The modifiers which must be present during a mouse drag
     * to move a SwoglComponent.
     */
    private int movingModifiersMask = InputEvent.BUTTON1_DOWN_MASK;
    
    /**
     * The modifiers which must be present during a mouse drag
     * to scale a SwoglComponent.
     */
    private int scalingModifiersMask = InputEvent.BUTTON2_DOWN_MASK;
    
    /**
     * The modifiers which must be present during a mouse drag
     * to rotate a SwoglComponent.
     */
    private int rotatingModifiersMask = InputEvent.BUTTON3_DOWN_MASK; 
    
    
    /**
     * Creates a SpaceLayout3D
     */
    public SpaceLayout3D()
    {
    }
    

    /**
     * {@inheritDoc}
     */
    public void doLayout3D()
    {
        super.doLayout3D();
        // Nothing to do here.
    }
    
    
    /**
     * Set the modifiers which must be present during a mouse drag
     * to move a SwoglComponent.
     * 
     * @param movingModifiersMask The modifiers to set
     */
    public void setMovingModifiersMask(int movingModifiersMask)
    {
        this.movingModifiersMask = movingModifiersMask;
    }

    /**
     * Set the modifiers which must be present during a mouse drag
     * to rotate a SwoglComponent.
     * 
     * @param rotatingModifiersMask The modifiers to set
     */
    public void setRotatingModifiersMask(int rotatingModifiersMask)
    {
        this.rotatingModifiersMask = rotatingModifiersMask;
    }

    
    /**
     * Set the modifiers which must be present during a mouse drag
     * to scale a SwoglComponent.
     * 
     * @param scalingModifiersMask The modifiers to set
     */
    public void setScalingModifiersMask(int scalingModifiersMask)
    {
        this.scalingModifiersMask = scalingModifiersMask;
    }

    
    /**
     * Returns whether the given event has only the modifiers 
     * set that are specified by the movingModifiersMask, and
     * not the modifiers that are specified by the 
     * scalingModifiersMask and the rotatingModifiersMask
     * 
     * @param event The event to check
     * @return Whether only the specified modifiers are set
     */
    private boolean isMoving(InputEvent event)
    {
        return 
            hasModifiersEx(event, movingModifiersMask) &&
            hasNotModifiersEx(event, scalingModifiersMask) &&
            hasNotModifiersEx(event, rotatingModifiersMask);
    }

    /**
     * Returns whether the given event has only the modifiers 
     * set that are specified by the rotatingModifiersMask, and
     * not the modifiers that are specified by the 
     * scalingModifiersMask and the movingModifiersMask
     * 
     * @param event The event to check
     * @return Whether only the specified modifiers are set
     */
    private boolean isRotating(InputEvent event)
    {
        return 
            hasModifiersEx(event, rotatingModifiersMask) &&
            hasNotModifiersEx(event, movingModifiersMask) &&
            hasNotModifiersEx(event, scalingModifiersMask);
    }
    
    /**
     * Returns whether the given event has only the modifiers 
     * set that are specified by the scalingModifiersMask, and 
     * not the modifiers that are specified by the 
     * movingModifiersMask and the rotatingModifiersMask
     * 
     * @param event The event to check
     * @return Whether only the specified modifiers are set
     */
    private boolean isScaling(InputEvent event)
    {
        return 
            hasModifiersEx(event, scalingModifiersMask) &&
            hasNotModifiersEx(event, movingModifiersMask) &&
            hasNotModifiersEx(event, rotatingModifiersMask);
    }

    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed(MouseEvent e)
    {
        super.mousePressed(e);
        mousePosition = e.getPoint();
        draggingInfo = getSwoglContainer().computePickingInfo(e.getPoint());
        if (draggingInfo != null)
        {
            currentIntersectionPosition = draggingInfo.getIntersectionPosition();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void mouseReleased(MouseEvent e)
    {
        super.mouseReleased(e);
        mousePosition = e.getPoint();
        draggingInfo = null;
    }
    

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDragged(MouseEvent e)
    {
        //System.out.println("drag "+draggingInfo);
        //System.out.println("drag event "+EventUtils.createInfoString(e));
        
        if (draggingInfo != null)
        {
            if (isResizingEnabled() && isResizing(draggingInfo))
            {
                //System.out.println("isResizing");
                super.mouseDragged(e);
            }
            else
            {
                if (isMoving(e))
                {
                    applyMovement(draggingInfo, e.getX(), e.getY(), 0);
                }
                if (isRotating(e))
                {
                    int dx = e.getX() - mousePosition.x;
                    int dy = e.getY() - mousePosition.y;
                    applyRotation(draggingInfo, dy, dx);
                }
                if (isScaling(e))
                {
                    int dx = e.getX() - mousePosition.x;
                    int dy = e.getY() - mousePosition.y;
                    applyScaling(draggingInfo, dx, dy);
                }
                getSwoglContainer().repaint();
            }
        }
        mousePosition = e.getPoint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoved(MouseEvent e)
    {
        super.mouseMoved(e);
        mousePosition = e.getPoint();
        updateCursor();
    }

    /**
     * If the current mouse position is over a SwoglComponent, and the
     * mouse position does not imply a resizing operation (or resizing
     * is disabled), the a MOVE_CURSOR will be set. Otherwise, the
     * {@link AbstractLayout3D#updateCursor()} method will be called.
     */
    protected void updateCursor()
    {
        PickingInfo pickingInfo = getSwoglContainer().computePickingInfo(mousePosition);
        if (pickingInfo != null && !isResizing(pickingInfo))
        {
            getSwoglContainer().setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
        }
        else
        {
            super.updateCursor();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        super.mouseWheelMoved(e);
        mousePosition = e.getPoint();
        draggingInfo = getSwoglContainer().computePickingInfo(e.getPoint());
        if (draggingInfo != null)
        {
            currentIntersectionPosition = draggingInfo.getIntersectionPosition();
            applyMovement(draggingInfo, e.getX(), e.getY(), -e.getWheelRotation());
            getSwoglContainer().repaint();
        }
    }

    
    /**
     * Apply the movement that is implied by the given PickingInfo referring
     * to the last position where the mouse was pressed.
     * 
     * @param pickingInfo The current pickingInfo
     * @param x The x position of the event that created the pickingInfo
     * @param y The y position of the event that created the pickingInfo
     * @param deltaZ The delta that was caused due to a mouse wheel rotation
     */
    private void applyMovement(PickingInfo pickingInfo, int x, int y, int deltaZ)
    {
        Vector3f delta = computeMovement(pickingInfo, x, y, deltaZ);

        Matrix4f trans = new Matrix4f();
        pickingInfo.getSwoglComponent().getTransform(trans);
        trans.mul(MatrixUtils.translate(delta), trans);
        pickingInfo.getSwoglComponent().setTransform(trans);
    }

    /**
     * Compute the movement that is implied by the given PickingInfo referring
     * to the last position where the mouse was pressed.
     * 
     * @param pickingInfo The current pickingInfo
     * @param x The x position of the event that created the pickingInfo
     * @param y The y position of the event that created the pickingInfo
     * @param deltaZ The delta that was caused due to a mouse wheel rotation
     * @return The movement vector for the given parameters
     */
    private Vector3f computeMovement(PickingInfo pickingInfo, int x, int y, int deltaZ)
    {
        Point3f rayOrigin = new Point3f();
        Vector3f rayDirection = new Vector3f();
        getSwoglContainer().computePickingRayData(new Point(x, y), rayOrigin, rayDirection);
        float dist = -(rayOrigin.z - currentIntersectionPosition.z) / rayDirection.z;
        Point3f newDraggedPoint = new Point3f();
        newDraggedPoint.scaleAdd(dist + deltaZ * MOUSE_WHEEL_MOVEMENT_SPEED, rayDirection, rayOrigin);
        Vector3f delta = new Vector3f();
        delta.sub(newDraggedPoint, currentIntersectionPosition);
        currentIntersectionPosition.add(delta);
        return delta;
    }

    /**
     * Apply the rotation that is caused by the given pickingInfo and
     * the given movement (in pixels)
     * 
     * @param pickingInfo The current pickingInfo
     * @param rotX The mouse movement in pixels
     * @param rotY The mouse movement in pixels
     */
    private void applyRotation(PickingInfo pickingInfo, int rotX, int rotY)
    {
        float rx = (float)Math.toRadians(rotX * ROTATION_SPEED);
        float ry = (float)Math.toRadians(rotY * ROTATION_SPEED);
        Matrix4f trans = new Matrix4f();
        pickingInfo.getSwoglComponent().getTransform(trans);
        Vector4f translation = new Vector4f();
        trans.getColumn(3, translation);
        trans.setColumn(3, 0,0,0,1);
        trans.mul(MatrixUtils.rotateY(ry), trans);
        trans.mul(MatrixUtils.rotateX(rx), trans);
        trans.setColumn(3, translation);
        pickingInfo.getSwoglComponent().setTransform(trans);
    }

    /**
     * Apply the scaling that is caused by the given pickingInfo and
     * the given movement (in pixels)
     * 
     * @param pickingInfo The current pickingInfo
     * @param rotX The mouse movement in pixels
     * @param rotY The mouse movement in pixels
     */
    private void applyScaling(PickingInfo pickingInfo, int dx, int dy)
    {
        float sx = 1.0f + dx * SCALING_SPEED;
        float sy = 1.0f - dy * SCALING_SPEED;
        Matrix4f trans = new Matrix4f();
        pickingInfo.getSwoglComponent().getTransform(trans);
        trans.mul(MatrixUtils.scale(sx, sy, 1));
        pickingInfo.getSwoglComponent().setTransform(trans);
    }





}
