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

import javax.vecmath.Point3f;

/**
 * An implementation of the Interpolator interface that may be
 * used to interpolate positions along a path. The path may
 * be composed of line segments and cubic bezier curves.
 */
public class PathInterpolator extends CompoundCurve implements Interpolator<Point3f>
{
	/**
	 * Creates a new PathInterpolator
	 */
    public PathInterpolator()
	{
		super(true);
	}
    
    /**
     * Add the given bezier curve to this PathInterpolator.
     * None of the points may be null.
     * 
     * @param p0 The start point of the bezier curve.
     * @param p1 The first control point
     * @param p2 The second control point
     * @param p3 The end point of the bezier curve
     */
    public void addBezierCurve(Point3f p0, Point3f p1, Point3f p2, Point3f p3)
    {
        BezierCurve curve = new BezierCurve(p0, p1, p2, p3);
        addSegment(curve);
    }

    /**
     * Add the given line segment to this PathInterpolator.
     * None of the points may be null.
     * 
     * @param start The start point of the segment
     * @param end The end point of the segment
     */
    public void addLineSegment(Point3f start, Point3f end)
    {
    	Curve segment = new LineSegment(start, end);
    	addSegment(segment);
    }
    
    /**
     * Returns the interpolated point at the given relative
     * position. The alpha value will be clamped to be in
     * [0,1]
     * 
     * @param alpha The relative position
     * @return The point at the given position
     */
	public Point3f getInterpolated(float alpha)
	{
		return getPoint(alpha);
	}
    
}
