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

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.*;

/**
 * Implementation of an Interpolator that provides 
 * arrangements. Each arrangement may be specified as
 * a point and a quaternion or a point and a 
 * rotation about a certain angle around an axis.
 * The arrangements are then returned as Matrix4f 
 * objects that describe a location and a rotation
 * in space which is linearly interpolated between
 * the given arrangements.   
 */
public class ArrangementInterpolator extends CompoundCurve 
    implements Interpolator<Matrix4f>
{
	/**
	 * The previous position that has been added to this
	 * ArrangementInterpolator
	 */
    private Point3f previousPosition = null;
    
    /**
     * The rotations at each point
     */
    private List<Quat4f> rotations = new ArrayList<Quat4f>();

    /**
	 * Creates a new ArrangementInterpolator
	 */
    public ArrangementInterpolator()
	{
		super(false);
	}

    /**
     * Add the given arrangement to this ArrangementInterpolator
     * 
     * @param position The position
     * @param rotation The rotation
     */
    public void addArrangement(Point3f position, Quat4f rotation)
    {
    	if (previousPosition == null)
    	{
    		previousPosition = new Point3f(position);
    	}
    	else
    	{
    		Curve segment = new LineSegment(previousPosition, position);
    		addSegment(segment);
    		previousPosition.set(position);
    	}
        rotations.add(new Quat4f(rotation));
    }

    /**
     * Add the given arrangement to this ArrangementInterpolator
     * 
     * @param position The position
     * @param axis The axis of rotation
     * @param angleRad The angle of rotation, in radians
     */
    public void addArrangement(Point3f position, Vector3f axis, float angleRad)
    {
        Quat4f rotation = new Quat4f();
        rotation.set(new AxisAngle4f(axis, angleRad));
        addArrangement(position, rotation);
    }

    /**
     * Returns the interpolated rotation at the given
     * relative position on the path. The given alpha
     * will be clamped to be in [0,1]
     * 
     * @param alpha The relative position
     * @return The rotation at the given position
     */
    public Quat4f getRotation(float alpha)
    {
    	if (getNumSegments() == 0)
    	{
    		return new Quat4f();
    	}
        Location location = computeLocation(alpha);

        Quat4f p0 = rotations.get(location.getIndex());
        Quat4f p1 = rotations.get(location.getIndex()+1);
        Quat4f rotation = new Quat4f();
        rotation.interpolate(p0, p1, location.getRelativePosition());
        return rotation;
    }

    /**
     * Returns the interpolated arrangement for the
     * given relative position. The given alpha
     * will be clamped to be in [0,1]
     *  
     * @param alpha The relative position
     * @return The matrix describing the translation
     * and rotation at the given position
     */
    public Matrix4f getInterpolated(float alpha)
    {
        Quat4f rotation = getRotation(alpha);
        Point3f position = getPoint(alpha);
        Matrix4f result = new Matrix4f();
        result.set(rotation);
        result.setTranslation(new Vector3f(position));
        return result;
    }
}
