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

import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

/**
 * This class stores all information relevant for the picking
 * of a SwoglComponent inside a SwoglContainer. A SwoglContainer
 * will return an (immutable) instance of this class for each
 * call to {@link SwoglContainer#computePickingInfo(java.awt.Point)}
 */
public class PickingInfo
{
    /**
     * The SwoglComponent that has been picked
     */
    private SwoglComponent swoglComponent;
    
    /**
     * The position where the picking ray intersected the SwoglComponent
     */
    private Point3f intersectionPosition;
    
    /**
     * The texture coordinates of the SwoglComponent at the 
     * intersectionPosition
     */
    private TexCoord2f texCoord;
    
    /**
     * Creates a new PickingInfo from the given parameters
     * 
     * @param swoglComponent The SwoglComponent that has been picked
     * @param intersectionPosition The position where the picking ray 
     * intersected the SwoglComponent
     * @param texCoord The texture coordinates of the SwoglComponent 
     * at the intersectionPosition
     */
    public PickingInfo(SwoglComponent swoglComponent, 
        Point3f intersectionPosition, TexCoord2f texCoord)
    {
        this.swoglComponent = swoglComponent;
        this.intersectionPosition = new Point3f(intersectionPosition);
        this.texCoord = new TexCoord2f(texCoord);
    }
    
    /**
     * Returns the SwoglComponent that has been picked
     * 
     * @return The SwoglComponent that has been picked
     */
    public SwoglComponent getSwoglComponent()
    {
        return swoglComponent;
    }

    /**
     * Returns a copy of the the position where the picking ray intersected 
     * the SwoglComponent
     * 
     * @return A copy of the position where the picking ray intersected the 
     * SwoglComponent
     */
    public Point3f getIntersectionPosition()
    {
        return new Point3f(intersectionPosition);
    }

    /**
     * Returns a copy of the texture coordinates of the SwoglComponent at 
     * the intersectionPosition
     * 
     * @return A copy of the texture coordinates of the SwoglComponent at 
     * the intersectionPosition
     */
    public TexCoord2f getTexCoord()
    {
        return new TexCoord2f(texCoord);
    }

    /**
     * Returns a String representation of this PickingInfo
     * 
     * @return A String representation of this PickingInfo
     */
    @Override
    public String toString()
    {
        return "PickingInfo[swoglComponent="+swoglComponent+
            ", intersectionPosition="+intersectionPosition+
            ", texCoord="+texCoord+"]";
    }
    
}
