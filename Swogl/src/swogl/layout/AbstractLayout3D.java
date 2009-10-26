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
 * Abstract base class for a LayoutManager3D. This class offers
 * the functionality of resizing the contents of a SwoglComponent 
 * via mouse drags of the border. In general, if one of the event 
 * listener methods is overridden in subclasses, the subclass 
 * should explicitly call the super implementation for this layout 
 * to work properly.
 */
public abstract class AbstractLayout3D implements LayoutManager3D
{
    /**
     * Returns whether the given inputEvent has the given modifiers set
     * 
     * @param inputEvent The event
     * @param modifiersExMask The modifiers to check
     * @return Whether the given inputEvent has the given modifiers set
     */
    protected static boolean hasModifiersEx(InputEvent inputEvent, int modifiersExMask)
    {
        return (inputEvent.getModifiersEx() & modifiersExMask) == modifiersExMask;
    }

    /**
     * Returns whether the given inputEvent does NOT have the given modifiers set
     * 
     * @param inputEvent The event
     * @param modifiersExMask The modifiers to check
     * @return Whether the given inputEvent does NOT have the given modifiers set
     */
    protected static boolean hasNotModifiersEx(InputEvent inputEvent, int modifiersExMask)
    {
        return (inputEvent.getModifiersEx() & modifiersExMask) == 0;
    }
    
    
    /**
     * The SwoglContainer this LayoutManager3D operates on 
     */
    private SwoglContainer swoglContainer;
    
    /**
     * Whether this LayoutManager3D is currently active, i.e. whether
     * the incoming events should be processed
     */
    private boolean active = false;
    
    /**
     * The PickingInfo that will be used for resizing the contents
     * of a SwoglComponent via mouse drags
     */
    private PickingInfo draggingInfo = null;
    
    /**
     * The current mouse position
     */
    private Point mousePosition = new Point();

    /**
     * The distance that the mouse cursor may have to the border of 
     * a component so that it is considered to be on the border,
     * and will cause in a 'resizing' cursor to be shown and to 
     * cause a resize operation when dragged.
     */
    private int resizingBorderSizePixels = 10;
    
    /**
     * Whether resizing the contents of a SwoglComponent by dragging
     * its border is currently enabled.
     */
    private boolean resizingEnabled = true;

    /**
     * The modifiers which have to be present in order to consider
     * a mouse drag on a border as a resizing operation of the 
     * content of a SwoglComponent
     */
    private int resizingModifiersMask = InputEvent.BUTTON1_DOWN_MASK;
    
    
    /**
     * Creates a new AbstractLayout3D. The layout will support resizing 
     * the contents of the SwoglComponents when pressing the left mouse 
     * button on a border of 10 pixels of the SwoglComponent and 
     * dragging the mouse. This behavior may be modified using
     * {@link AbstractLayout3D#setResizingEnabled(boolean)} and
     * {@link AbstractLayout3D#setResizingBorderSize(int)} 
     */
    protected AbstractLayout3D()
    {
    }

    /**
     * {@inheritDoc}
     */
    public void doLayout3D()
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @param swoglContainer The SwoglContainer for this AbstractLayout3D
     */
    public void setSwoglContainer(SwoglContainer swoglContainer)
    {
        this.swoglContainer = swoglContainer;
    }

    /**
     * Returns the SwoglContainer this layout manager operates on.
     * 
     * @return The SwoglContainer this layout manager operates on.
     */
    protected SwoglContainer getSwoglContainer()
    {
        return swoglContainer;
    }


    /**
     * Set the flag which indicates whether resizing the contents of 
     * a SwoglComponent by dragging its border is currently enabled.
     * 
     * @param enabled Whether resizing is enabled
     */
    public void setResizingEnabled(boolean enabled)
    {
        this.resizingEnabled = enabled;
    }
    
    /**
     * Returns whether resizing the contents of a SwoglComponent by 
     * dragging its border is currently enabled.
     * 
     * @return Whether resizing is enabled
     */
    protected boolean isResizingEnabled()
    {
        return resizingEnabled;
    }
    
    /**
     * Set the modifiers which have to be present in order to consider
     * a mouse drag on a border as a resizing operation of the 
     * content of a SwoglComponent
     * 
     * @param modifiers The modifiers to set
     */
    public void setResizingModifiersMask(int modifiers)
    {
        this.resizingModifiersMask = modifiers;
    }
    
    /**
     * Returns the modifiers which have to be present in order to consider
     * a mouse drag on a border as a resizing operation of the 
     * content of a SwoglComponent
     * 
     * @return The modifiers
     */
    protected int getResizingModifiersMask()
    {
        return resizingModifiersMask;
    }
    
    /**
     * Set the size (in pixels) of the border. When the mouse
     * cursor is on a border of the SwoglComponent, the 
     * contents of the SwoglComponent may be resized via
     * mouse drags.
     *  
     * @param pixels The border size for resizing operations.
     */
    protected void setResizingBorderSize(int pixels)
    {
        resizingBorderSizePixels = pixels;
    }
    

    /**
     * {@inheritDoc}
     */
    public void setActive(boolean active)
    {
        if (active)
        {
            updateCursor(mousePosition);
        }
        else
        {
            draggingInfo = null;
            getSwoglContainer().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
        this.active = active;
    }
    
    /**
     * Returns whether this LayoutManager3D is currently active,
     * i.e. whether the SwoglContainer that owns this 
     * LayoutManager3D is in LAYOUT ControlMode.
     * 
     * @return Whether this LayoutManager3D is currently active.
     */
    boolean isActive()
    {
        return active;
    }
    
    /**
     * {@inheritDoc}
     */
    public void mouseClicked(MouseEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void mousePressed(MouseEvent e)
    {
        mousePosition = e.getPoint();
        draggingInfo = getSwoglContainer().computePickingInfo(e.getPoint());
    }

    /**
     * Returns whether the modifiers which have to be present in order to 
     * consider a mouse drag on a border as a resizing operation of the 
     * content of a SwoglComponent are present in the given event.
     * 
     * @param inputEvent The event to check
     * @return Whether the event is a resizing event
     */
    protected boolean isResizing(InputEvent inputEvent)
    {
        return hasModifiersEx(inputEvent, resizingModifiersMask);
    }


    /**
     * {@inheritDoc}
     */
    public void mouseDragged(MouseEvent e)
    {
        if (resizingEnabled && isResizing(e) && draggingInfo != null && isResizing(draggingInfo))
        {
            //System.out.println("Resize");
            int dx = 0;
            int dy = 0;
            SwoglComponent swoglComponent = draggingInfo.getSwoglComponent(); 
            if (isResizing(swoglComponent.getContentSize().width, draggingInfo.getTexCoord().x))
            {
                dx = e.getX() - mousePosition.x;
            }
            if (isResizing(swoglComponent.getContentSize().height, draggingInfo.getTexCoord().y))
            {
                dy = e.getY() - mousePosition.y;
            }
            resize(swoglComponent, dx, dy);
            getSwoglContainer().repaint();
        }
        mousePosition = e.getPoint();
    }
    
    /**
     * {@inheritDoc}
     */
    public void mouseMoved(MouseEvent e)
    {
        mousePosition = e.getPoint();
        updateCursor();
    }

    
    /**
     * Update the cursor according to the current mouse position.
     * If the cursor is over the border of a SwoglComponent, 
     * and resizing is enabled, a resizing-Cursor will be set.
     */
    protected void updateCursor()
    {
        updateCursor(mousePosition);
    }
    
    
    /**
     * Update the cursor according to the action caused by the mouse
     * at the given point. If the cursor is over the border of a 
     * SwoglComponent, a resizing-Cursor will be set
     *  
     * @param point The current mouse position
     */
    private void updateCursor(Point point)
    {
        PickingInfo pickingInfo = getSwoglContainer().computePickingInfo(point);
        if (resizingEnabled && pickingInfo != null)
        {
            getSwoglContainer().setCursor(computeCursor(pickingInfo));
        }
        else
        {
            getSwoglContainer().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Computes the cursor for the given PickingInfo. If the PickingInfo
     * points to a border, the appropriate resizing cursor will be 
     * returned.
     * 
     * @param pickingInfo The picking info 
     * @return A cursor
     */
    protected Cursor computeCursor(PickingInfo pickingInfo)
    {
        SwoglComponent swoglComponent = pickingInfo.getSwoglComponent();
        Dimension size = swoglComponent.getContentSize();
        float resizingBorderSizeX = (float)resizingBorderSizePixels / size.width;
        float resizingBorderSizeY = (float)resizingBorderSizePixels / size.height;
        TexCoord2f texCoord = pickingInfo.getTexCoord();
        return computeCursor(texCoord.x, texCoord.y, resizingBorderSizeX, resizingBorderSizeY);
    }
    
    /**
     * Computes the cursor for the given texture coordinates. If the 
     * coordinates are in a range less than the given thresholds from 
     * the border, then the appropriate resizing cursor will be 
     * returned.
     * 
     * @param x The x texture coordinate, [0..1]
     * @param y The y texture coordinate, [0..1]
     * @param tx The threshold for the x-resizing cursor
     * @param ty The threshold for the y-resizing cursor
     * @return A cursor
     */
    private Cursor computeCursor(float x, float y, float tx, float ty)
    {
        final float x0 = tx;
        final float x1 = 1.0f - tx;
        final float y0 = ty;
        final float y1 = 1.0f - ty;

        //System.out.println(x + " " + y+" limits "+x0+" "+y0+" "+x1+" "+y1);
        if (x < x0)
        { 
            if (y < y0)
            {
                return Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR);
            }
            else if (y > y1)
            {
                return Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR);
            }
            else
            {
                return Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR);
            }
        }
        else if (x > x1)
        {
            if (y < y0)
            {
                return Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR);
            }
            else if (y > y1)
            {
                return Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR);
            }
            else
            {
                return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
            }
        }
        else
        {
            if (y < y0)
            {
                return Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR);
            }
            else if (y > y1)
            {
                return Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR);
            }
            else
            {
                return Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
            }
        }
    }

    /**
     * Resize the content of the given SwoglComponent by the given amount
     * 
     * @param swoglComponent The SwoglComponent whose content should be resized
     * @param dx The change in size in x-direction
     * @param dy The change in size in y-direction
     */
    private void resize(SwoglComponent swoglComponent, int dx, int dy)
    {
        Dimension size = swoglComponent.getContentSize();
        int w = size.width + dx;
        int h = size.height + dy;
        swoglComponent.setContentSize(w, h);
        getSwoglContainer().validate();
        getSwoglContainer().repaint();
        //System.out.println("resized");
    }



    /**
     * Returns whether the given PickinInfo implies a resizing,
     * that is, whether it is within a border of the SwoglComponent
     * with the current {@link #setResizingBorderSize(int)} pixels
     * size.
     * 
     * @param pickingInfo The PickingInfo to check
     * @return Whether the given PickinInfo implies a resizing
     */
    protected boolean isResizing(PickingInfo pickingInfo)
    {
        SwoglComponent swoglComponent = pickingInfo.getSwoglComponent();
        Dimension size = swoglComponent.getContentSize();
        TexCoord2f texCoord = pickingInfo.getTexCoord();
        boolean resizingH = isResizing(size.width, texCoord.x);
        boolean resizingV = isResizing(size.height, texCoord.y);
        return resizingH || resizingV;
    }

    /**
     * Returns whether the given value is in the threshold that is
     * specified by the current resizingBorderSizePixels and the
     * given totalSize. 
     *    
     * @param totalSize The total size the value refers to
     * @param value The value to check
     * @return Whether this value is in the resizingBorderSizePixels range
     */
    private boolean isResizing(int totalSize, float value)
    {
        float resizingBorderSize = (float)resizingBorderSizePixels / totalSize;
        return (value < resizingBorderSize || value > 1.0f - resizingBorderSize);
    }

    
    /**
     * {@inheritDoc}
     */
    public void mouseReleased(MouseEvent e)
    {
        mousePosition = e.getPoint();
        draggingInfo = null;
        updateCursor();
    }

    /**
     * {@inheritDoc}
     */
    public void mouseEntered(MouseEvent e)
    {
        mousePosition = e.getPoint();
        updateCursor();
    }

    /**
     * {@inheritDoc}
     */
    public void mouseExited(MouseEvent e)
    {
        mousePosition = e.getPoint();
        updateCursor();
    }

    /**
     * {@inheritDoc}
     */
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        mousePosition = e.getPoint();
        updateCursor();
    }


    /**
     * {@inheritDoc}
     */
    public void keyTyped(KeyEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void keyPressed(KeyEvent e)
    {
    }

    /**
     * {@inheritDoc}
     */
    public void keyReleased(KeyEvent e)
    {
    }



}
