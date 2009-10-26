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

import java.awt.event.*;

import swogl.SwoglContainer;

/**
 * This is the common interface for all classes that may lay out
 * SwoglComponents in a SwoglContainer
 */
public interface LayoutManager3D extends MouseListener, MouseMotionListener, MouseWheelListener, KeyListener
{
    /**
     * Lays out the SwoglComponents in a SwoglContainer
     */
    void doLayout3D();
    
    /**
     * This method will assign the given SwoglContainer to this 
     * LayoutManager3D. When this LayoutManager3D is assigned to a 
     * SwoglContainer using the 
     * {@link SwoglContainer#setLayout3D(LayoutManager3D)} method, 
     * the SwoglContainer will pass itself to this method. When a 
     * different LayoutManager3D is assigned to the SwoglContainer,
     * then the SwoglContainer will call this method and pass 'null' 
     * as the argument. This allows implementations of this interface 
     * to perform an initialization when this LayoutManager3D becomes
     * assigned to a SwoglContainer, and necessary cleanup operations 
     * when they are detached from a SwoglContainer.
     * 
     * @param swoglContainer The SwoglContainer for this LayoutManager3D
     */
    void setSwoglContainer(SwoglContainer swoglContainer);
    
    /**
     * This method will be called by the SwoglContainer owning this 
     * LayoutManager3D, when the ControlMode is set: This 
     * LayoutManager will be activated when the ControlMode of the 
     * SwoglContainer is set to ControlMode.LAYOUT, and deactivated 
     * otherwise. 
     * 
     * @param active Whether this LayoutManager3D should be active
     */
    void setActive(boolean active);
    
}
