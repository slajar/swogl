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

package swogl.layout.animation;

import javax.vecmath.*;

/**
 * Interface for all curves. A curve has a certain length and
 * provides points and tangents for arbitrary points on the
 * curve. 
 */
interface Curve 
{
	/**
	 * Returns the length of this curve
	 * 
	 * @return The length of this curve 
	 */
	float getLength();
	
	/**
	 * Returns the point at the given <i>relative</i>
	 * position on this curve. That is, the value of
	 * alpha in general should be in [0,1]
	 * 
	 * @param alpha The relative position
	 * @return The point at the given position
	 */
	Point3f getPoint(float alpha);

	/**
	 * Returns the tangent at the given <i>relative</i>
	 * position on this curve. That is, the value of
	 * alpha in general should be in [0,1]
	 * 
	 * @param alpha The relative position
	 * @return The tangent at the given position
	 */
	Vector3f getTangent(float alpha);
	
}
