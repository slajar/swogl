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
 * Implementation of a simple line segment
 */
class LineSegment implements Curve
{
	/**
	 * The start of this line segment
	 */
	private Point3f start = new Point3f();

	/**
	 * The end of this line segment
	 */
	private Point3f end = new Point3f();
	
	/**
	 * Creates a LineSegment with the given start
	 * and end
	 * 
	 * @param start The start of this line segment
	 * @param end The end of this line segment
	 */
	public LineSegment(Tuple3f start, Tuple3f end)
	{
		this.start.set(start);
		this.end.set(end);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public float getLength()
	{
		return start.distance(end);
	}

	/**
	 * {@inheritDoc}
	 */
	public Point3f getPoint(float alpha)
	{
		Point3f result = new Point3f();
		result.interpolate(start, end, alpha);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public Vector3f getTangent(float alpha)
	{
		Vector3f tangent = new Vector3f();
		tangent.sub(end, start);
		return tangent;
	}
	
}
