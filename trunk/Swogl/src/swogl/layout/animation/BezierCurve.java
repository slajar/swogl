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
 * Simple implementation of a Bezier curve 
 */
class BezierCurve implements Curve
{
	/**
	 * The number of discretization steps <i>per control
	 * point</i> that is used for approximating the 
	 * length of this curve
	 */
    private static final int LENGTH_APX_STEPS_PER_POINT = 8;
	
	/**
	 * The control points of this bezier curve
	 */
    private Point3f controlPoints[] = new Point3f[0];

    /**
     * The buffer for computing a point at a given
     * relative position
     */
    private Point3f pointBuffer[] = null;

    /**
     * Creates a new bezier curve with the given control points.
     * At least one control point must be given.
     * 
     * @param p The control points
     * @throws IllegalArgumentException If the given point array
     * is null or contains zero elements
     */
    public BezierCurve(Point3f ... p)
    {
    	if (p == null || p.length == 0)
    	{
    		throw new IllegalArgumentException(
    		    "At least one control point is required for "+
    		    "creating a BezierCurve");
    	}
    	controlPoints = new Point3f[p.length];
    	for (int i=0; i<p.length; i++)
    	{
    		controlPoints[i] = new Point3f(p[i]);;
    	}
    }

    /**
     * Add the given control point to this bezier curve
     * 
     * @param controlPoint The control point to add
     */
    void addControlPoint(Point3f controlPoint)
    {
        Point3f newControlPoints[] = new Point3f[controlPoints.length+1];
        System.arraycopy(controlPoints, 0, newControlPoints, 0, controlPoints.length);
        controlPoints = newControlPoints;
        controlPoints[controlPoints.length-1] = controlPoint;
    }

    /**
     * Returns the number of control points in this
     * bezier curve
     * 
     * @return The number of control points in this
     * bezier curve
     */
    int getNumControlPoints()
    {
        return controlPoints.length;
    }

    /**
     * Returns a <strong>reference</strong> of the control 
     * point with the given index.
     * 
     * @param index The index of the control point
     * @return The control point with the given index
     */
    Point3f getControlPointRef(int index)
    {
        return controlPoints[index];
    }

    
    
    /**
     * {@inheritDoc}
     */
    public float getLength()
    {
    	return getLength(LENGTH_APX_STEPS_PER_POINT * getNumControlPoints());
    }
    
    /**
     * Compute and return the length of this curve using
     * the given number of discretization steps
     * 
     * @param steps The number of steps
     * @return The length of this curve
     */
    private float getLength(int steps)
    {
        float length = 0;
        Point3f point = new Point3f();
        Point3f prevPoint = new Point3f();
        computePointAt(0, prevPoint, null);
        for (int i=1; i<steps; i++)
        {
            float t = (float)i / (steps-1);
            computePointAt(t, point, null);
            length += point.distance(prevPoint);
            prevPoint.set(point);
        }
        return length;
    }

    
    /**
     * {@inheritDoc}
     */
    public Point3f getPoint(float t)
    {
    	Point3f result = new Point3f();
    	computePointAt(t, result, null);
    	return result;
    }
    
    /**
     * {@inheritDoc}
     */
	public Vector3f getTangent(float t)
	{
		Vector3f tangent = new Vector3f();
		computePointAt(t, null, tangent);
		return tangent;
	}
    
    
    
    /**
     * Compute the point at the given position on this curve,
     * and store it in the given 'point'. The tangent at this
     * point will be stored in 'tangent'. Both arguments 
     * may be null.
     * 
     * @param t The relative position on this curve. Must be
     * in [0,1]
     * @param point The point which will store the result.
     * @param tangent The tangent which will store the result.
     */
    void computePointAt(float t, Tuple3f point, Tuple3f tangent)
    {
        casteljau(t, point, tangent);
    }

    /**
     * Simple implementation of the deCasteljau algorithm.
     * The point and the tangent may be null. If they are
     * non-null, they will store the resulting point and
     * tangent at this point, respectively.
     *  
     * @param t The location on the curve, between 0 and 1
     * @param result The point which will store the result
     * @param tangent The tangent at the resulting point.
     */
    private void casteljau(float t, Tuple3f result, Tuple3f tangent)
    {
        int n = controlPoints.length - 1;
        if (n==0)
        {
            result.set(controlPoints[0]);
            return;
        }
        if (n==1)
        {
            result.interpolate(controlPoints[0], controlPoints[1], t);
            return;
        }

        if (pointBuffer == null || pointBuffer.length < n+1)
        {
            pointBuffer = new Point3f[n+1];
            for (int i=0; i<pointBuffer.length; i++)
            {
                pointBuffer[i] = new Point3f();
            }
        }

        for (int c=0; c<n; c++)
        {
            pointBuffer[c].interpolate(controlPoints[c], controlPoints[c+1], t);
        }

        for (int r=2; r<n; r++)
        {
            for (int c=0; c<=n-r; c++)
            {
                pointBuffer[c].interpolate(pointBuffer[c], pointBuffer[c+1], t);
            }
        }

        if (result != null)
        {
            result.interpolate(pointBuffer[0], pointBuffer[1], t);
        }

        if (tangent != null)
        {
            tangent.sub(pointBuffer[0], pointBuffer[1]);
            tangent.scale(n);
        }
    }


}
