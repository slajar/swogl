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

import java.util.*;

import javax.vecmath.*;

/**
 * Implementation of a curve that provides a compound view
 * on several other curves.
 */
class CompoundCurve implements Curve
{
	/**
	 * This class describes a location on this CompoundCurve,
	 * consisting of the index of the sub-curve and the 
	 * relative position (in [0,1]) on the sub-curve.
	 */
	static class Location
	{
		/**
		 * The index of the sub-curve
		 */
		private int index;
		
		/**
		 * The relative position on the sub-curve
		 */
		private float relativePosition;
		
		/**
		 * Creates a new Location with the given index 
		 * and relative position.
		 * 
		 * @param index The index
		 * @param relativePosition The relative position
		 */
		private Location(int index, float relativePosition)
		{
			this.index = index;
			this.relativePosition = relativePosition;
		}
		
		/**
		 * Return the index of the sub-curve this Location
		 * describes
		 * 
		 * @return The index of the sub-curve
		 */
		public int getIndex()
		{
			return index;
		}
		
		/**
		 * Returns the relative position on the sub-curve
		 * with the index of this Location
		 * 
		 * @return The relative position on the sub-curve
		 */
		public float getRelativePosition()
		{
			return relativePosition;
		}
	}
	
	/**
	 * Constant for a floating-point epsilon: Curves
	 * that are shorter than this epsilon will be 
	 * considered as having zero length. 
	 */
    private static final float EPSILON = 1e-5f;

    /**
     * The segments (sub-curves) that this CompoundCurve
     * consists of
     */
    private List<Curve> segments = new ArrayList<Curve>();

    /**
     * Whether the lengths of the sub-curves are cached.
     * The lengths of the sub-curves are required frequently,
     * and computing the lengths may be expensive. So this 
     * flag may be used to compute and store the lengths 
     * only once. This should only be used if the lengths
     * of the sub-curves may not change after they have
     * been added to this CompoundCurve.
     */
    private boolean cacheLengths = false;
    
    /**
     * This list contains the segment lengths if the
     * cacheLengths flag is set to 'true'
     */
    private List<Float> segmentLengths = new ArrayList<Float>();
    
    /**
     * Creates a new CompoundCurve
     */
    public CompoundCurve()
    {
    	this(false);
    }

    /**
     * Creates a new CompundCurve with the given caching
     * flag.  
     * 
     * @param cacheLengths Whether segment lengths are cached.
     */
    CompoundCurve(boolean cacheLengths)
    {
    	this.cacheLengths = cacheLengths;
    }
    
    /**
     * Add the given segment to this CompoundCurve
     * 
     * @param segment The segment to add
     */
    void addSegment(Curve segment)
    {
        segments.add(segment);
        if (cacheLengths)
        {
        	float length = segment.getLength();
        	segmentLengths.add(length);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public float getLength()
    {
    	float length = 0;
    	for (int i=0; i<segments.size(); i++)
    	{
    		length += getLength(i);
    	}
    	return length;
    }
    
    /**
     * Returns the length of the specified segment
     * 
     * @param segmentIndex The index of the segment
     * @return The length of the specified segment
     */
    private float getLength(int segmentIndex)
    {
    	if (cacheLengths)
    	{
    		return segmentLengths.get(segmentIndex);
    	}
    	else
    	{
    		return segments.get(segmentIndex).getLength();
    	}
    }
    
    /**
     * Compute the Location that corresponds to the given relative position 
     * on this CompoundCurve. The given alpha will be clamped to be in [0,1].
     * 
     * @param alpha The relative position 
     * @return The Location on this CompoundCurve
     */
    Location computeLocation(float alpha)
    {
    	alpha = Math.min(1, Math.max(0, alpha));
        float position = alpha * getLength();
        int currentIndex = segments.size()-1;
        for (int i=0; i<segments.size()-1; i++)
        {
        	float length = getLength(i);
            if (position <= length)
            {
                currentIndex = i;
                break;
            }
            position -= length;
        }
        float segmentLength = getLength(currentIndex);
    	float currentRelativePosition = 1.0f;
        if (segmentLength > EPSILON)
        {
        	currentRelativePosition = (position / segmentLength);
        }
        return new Location(currentIndex, currentRelativePosition);
    }
    
    /**
     * Returns the number of segments (sub-curves) this
     * CompoundCurve consists of
     * 
     * @return The number of segments
     */
    int getNumSegments()
    {
    	return segments.size();
    }
    
    /**
     * Returns the segment with the given index
     * 
     * @param index The index of the segment
     * @return The segment with the given index
     */
    Curve getSegment(int index)
    {
    	return segments.get(index);
    }
    
    /**
     * {@inheritDoc}
     */
    public Point3f getPoint(float alpha)
    {
    	if (getNumSegments() == 0)
    	{
    		return new Point3f();
        }
        
        Location location = computeLocation(alpha);
        Curve segment = segments.get(location.getIndex());
        return segment.getPoint(location.getRelativePosition());
    }
    
    /**
     * {@inheritDoc}
     */
    public Vector3f getTangent(float alpha)
    {
        if (getNumSegments() == 0)
        {
        	return new Vector3f();
        }
        
        Location location = computeLocation(alpha);
        Curve segment = segments.get(location.getIndex());
        return segment.getTangent(location.getRelativePosition());
    }
    
}
