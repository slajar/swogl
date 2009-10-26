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

import javax.vecmath.Point3f;

/**
 * Interface for all classes that may transform points.
 * A PointTransformer may, for example, be set for a 
 * DefaultGeometry, so that the vertices of the 
 * DefaultGeometry will be transformed during an update.
 */
public interface PointTransformer
{
    /**
     * Transforms the given point. 
     * 
     * @param point The point that should be transformed. 
     */
    void transformPoint(Point3f point);
}
