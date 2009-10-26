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

import java.awt.Point;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.*;

import swogl.SwoglComponent;
import swogl.SwoglContainer;
import swogl.layout.animation.*;

// TODO: The layout should adjust the scrolling speed and offsets 
// according to the number of components. Additionally, the size 
// of the displayed components should be taken into account, but 
// this is currently not considered at all.

/**
 * An implementation of the LayoutManager3D interface that lays
 * out components similar to this "iPhone music browsing" style.
 */
public class FlipLayout3D extends AbstractLayout3D implements LayoutManager3D
{
    /**
     * Time in milliseconds between the animation steps
     */
    private static final int ANIMATION_DELAY = 30;

    /**
     * Constant for the z axis
     */
    private static final Vector3f Z_AXIS = new Vector3f(0,0,1);
    
    /**
     * The index of the front (center) SwoglComponent
     */
    private int currentIndexOffset = 0;
    
    /**
     * The index that is currently animated to be the index of
     * the front (center) SwoglComponent
     */
    private int targetIndexOffset = 0;

    /**
     * The real counterpart of the target index offset. The
     * targetIndexOffset will actually be computed by rounding
     * this value to the nearest integer.
     */
    private float realTargetIndexOffset = 0;
    
    /**
     * The interpolator for the path that the SwoglComponents
     * move along
     */
    private PathInterpolator interpolator;

    
    /**
     * An interpolator that modifies the alpha value to distribute the 
     * SwoglComponents nicely along the path: The components should 
     * not be distributed evenly along the path, but instead with a
     * certain distance between the front component and the components
     * on both sides
     */
    private AlphaInterpolator alphaInterpolator = new AlphaInterpolator();

    
    /**
     * This threshold is used to compute the distance between
     * the front component and the other components along the
     * path. The final threshold value that will be passed
     * to the alphaInterpolator is computed from this 
     * alphaThresholdFactor and the number of components: When 
     * there are fewer components, then the threshold should 
     * be higher
     */
    private final float alphaThresholdFactor = 0.6f;
    
    /**
     * The current offset for the alpha value that is used for
     * interpolating along the movement path of the components.
     * During the animation from one front component to the
     * next component, this value decreases to zero.
     */
    private float currentAlphaOffset = 0;

    /**
     * A global factor for the animation speed. The actual step
     * size for the alpha offset value (alphaOffsetStepSize) is
     * computed by dividing this constant by the number of  
     * components. The animation should be (relatively) faster
     * when there are few components.
     */
    private final float alphaOffsetSpeed = 0.075f;
    
    /**
     * The step size for the animation of the components along
     * the movement path.
     */
    private float alphaOffsetStepSize = 0.0f;
    
    /**
     * The step size that is currently used. This may be a 
     * multiple of the basic alphaOffsetStepSize, to support
     * faster scrolling through many components.
     */
    private float currentAlphaOffsetStepSize = 0.0f;
    
    /**
     * This is the value that will be used for the currentAlphaOffset
     * when the front component changes and the alpha has to be
     * animated to bring the next component to the front. 
     */
    private float alphaOffsetSize = 0;

    /**
     * The animator for the flip animation
     */
    private FlipAnimator flipAnimator;

    /**
     * The mouse position of the previous mouse motion event
     */
    private Point previousMousePosition = new Point();
    
    /**
     * A ComponentListener that will be attached to the 
     * SwoglContainer and initialize the movement path
     * depending on the width of the SwoglContainer
     * when the SwoglContainer is resized 
     */
    ComponentListener interpolatorInitializer = new ComponentAdapter()
    {
        public void componentResized(ComponentEvent event)
        {
            initInterpolator();
        }
    };
    
    
    /**
     * Creates a new FlipLayout3D
     */
    public FlipLayout3D()
    {
    }

    
    /**
     * Initialize the path of the interpolator. Seen from the top it will 
     * roughly have the shape of a "{" rotated counterclockwise about 90 
     * degrees. The "tip" of the brace will be at 0,0 and be the position 
     * of the front component. The other components will be distributed 
     * along both sides. 
     */
    private void initInterpolator()
    {
        if (getSwoglContainer() != null)
        {
            interpolator = new PathInterpolator();
            float scaleX = getSwoglContainer().getWidth()*1.2f;
            float scaleZ = 500.0f;
            add(interpolator, scaleX, scaleZ, 1,-1,  0.5f, -0.5f,  0,-1,  0, 0);
            doLayout3D();
        }
    }
    
    /**
     * Add bezier curves to the given interpolator whose control
     * points have the given normalized x- and z-coordinates
     * 
     * @param pi The interpolator
     * @param x0 x-coordinate of point 0
     * @param z0 z-coordinate of point 0
     * @param x1 x-coordinate of point 1
     * @param z1 z-coordinate of point 1
     * @param x2 x-coordinate of point 2
     * @param z2 z-coordinate of point 2
     * @param x3 x-coordinate of point 3
     * @param z3 z-coordinate of point 3
     */
    private void add(PathInterpolator pi, float scaleX, float scaleZ,
                     float x0, float z0,
                     float x1, float z1,
                     float x2, float z2,
                     float x3, float z3)
    {
        Point3f p0 = new Point3f(x0*scaleX, 0, z0*scaleZ);
        Point3f p1 = new Point3f(x1*scaleX, 0, z1*scaleZ);
        Point3f p2 = new Point3f(x2*scaleX, 0, z2*scaleZ);
        Point3f p3 = new Point3f(x3*scaleX, 0, z3*scaleZ);
        pi.addBezierCurve(p0, p1, p2, p3);

        p0.x = -p0.x;
        p1.x = -p1.x;
        p2.x = -p2.x;
        p3.x = -p3.x;
        pi.addBezierCurve(p3, p2, p1, p0);
    }

    
    
    /**
     * {@inheritDoc}
     */
    public void setSwoglContainer(SwoglContainer swoglContainer)
    {
        if (this.getSwoglContainer() != null)
        {
            this.getSwoglContainer().removeComponentListener(interpolatorInitializer);
        }
        super.setSwoglContainer(swoglContainer);
        if (flipAnimator != null)
        {
            flipAnimator.stop();
            flipAnimator = null;
        }
        if (swoglContainer != null)
        {
            flipAnimator = new FlipAnimator();
            Thread animatorThread = new Thread(flipAnimator,
            	"FlipLayout3DAnimator");
            animatorThread.start();
            swoglContainer.addComponentListener(interpolatorInitializer);
            initInterpolator();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doLayout3D()
    {
    	int numComponents = getSwoglContainer().getSwoglComponentCount();
    	if (numComponents == 0)
    	{
    		return;
    	}

    	if (numComponents == 1)
    	{
    		SwoglComponent swoglComponent = getSwoglContainer().getSwoglComponent(0);
    		arrange(swoglComponent, 0.5f);
    		return;
    	}

    	// The values describing the animation using the alpha value that 
    	// depend on the number of components are computed here.

    	int n = numComponents;
    	if (numComponents % 2 == 0)
    	{
    		n++;
    	}

    	// The relative distance between two components on the path
    	alphaOffsetSize = 1.0f / (n - 1);

    	// The step size for the animation of the currentAlphaOffset
    	alphaOffsetStepSize = alphaOffsetSpeed / n;

    	// The threshold that separates the front component from the others
    	alphaInterpolator.setThreshold(alphaThresholdFactor / n);

    	//System.out.println("numComponents "+numComponents);

    	for (int i=0; i<numComponents; i++)
    	{
    		int index = (i + currentIndexOffset) % numComponents;
    		if (index < 0)
    		{
    			index += numComponents;
    		}
    		SwoglComponent swoglComponent = getSwoglContainer().getSwoglComponent(index);

    		float alpha = (float)i/(n-1);

    		alpha += currentAlphaOffset;

    		float a = alphaInterpolator.getInterpolated(alpha);

    		//System.out.println("For component "+i+" alpha "+alpha+" becomes "+a);

    		arrange(swoglComponent, a);
    	}
    }
    
    /**
     * Arrange the given SwoglComponent along the path depending on the
     * given alpha value.
     * 
     * @param swoglComponent The SwoglComponent to arrange
     * @param alpha The alpha value.
     */
    private void arrange(SwoglComponent swoglComponent, float alpha)
    {
    	Point3f position = interpolator.getInterpolated(alpha);
        
    	//System.out.println("Component alpha "+alpha+" gives "+position);
        
        Vector3f tangent = interpolator.getTangent(alpha);
        if (tangent.z < 0)
        {
            tangent.negate();
        }
        
        float angle = Z_AXIS.angle(tangent);
        if (tangent.x < 0)
        {
            angle = -angle;
        }
        Matrix4f matrix = new Matrix4f();
        matrix.setIdentity();
        matrix.rotY(angle);
        matrix.setTranslation(new Vector3f(position));
        swoglComponent.setTransform(matrix);
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void mouseMoved(MouseEvent event)
    {
        super.mouseMoved(event);
    	previousMousePosition = event.getPoint();
    }
    

    /**
     * {@inheritDoc}
     * 
     * Horizontal dragging of the mouse with the right mouse button will 
     * modify the realTargetIndexOffset and the targetIndexOffset.
     */
    public void mouseDragged(MouseEvent event)
    {
        super.mouseDragged(event);
        if (!hasModifiersEx(event, InputEvent.BUTTON3_DOWN_MASK))
        {
            return;
        }
    	int dx = event.getX() - previousMousePosition.x;
    	
    	realTargetIndexOffset += dx / 100.0f;
    	
    	targetIndexOffset = Math.round(realTargetIndexOffset);
    	
    	//System.out.println("realTargetIndexOffset "+realTargetIndexOffset+
    	//                   " targetIndexOffset "+targetIndexOffset);
    	
    	synchronized (this)
    	{
    	    notifyAll();
    	}
    	previousMousePosition = event.getPoint();
    }

    
    /**
     * Inner class used for interpolating the alpha value in order to
     * distribute the components nicely along the path. 
     */
    private static class AlphaInterpolator implements Interpolator<Float>
    {
        /** 
         * The list of 2D points that is used to describe the shape
         * of this function
         */
        private List<Tuple2f> values = new ArrayList<Tuple2f>();

        /**
         * Creates a new AlphaInterpolator
         */
        AlphaInterpolator()
        {
            values.add(new Point2f(0.0f,0.0f ));
            values.add(new Point2f(0.5f,0.3f));
            values.add(new Point2f(0.5f,0.7f));
            values.add(new Point2f(1.0f,1.0f ));
        }
        
        /**
         * Set the threshold for this function: The slope of the function
         * will be low in the range [0,0.5-threshold], high in the range
         * [0.5-threshold,0.5+threshold] and low in the range 
         * [0.5+threshold,1]
         * 
         * @param threshold The threshold
         */
        void setThreshold(float threshold)
        {
            //System.out.println("threshold "+threshold);
            values.get(1).x = 0.5f - threshold;
            values.get(2).x = 0.5f + threshold;
        }
        
        /**
         * {@inheritDoc}
         */
        public Float getInterpolated(float alpha)
        {
            alpha = Math.min(1, Math.max(0, alpha));
            for (int i=0; i<values.size()-1; i++)
            {
                float x0 = values.get(i+0).x;
                float x1 = values.get(i+1).x;
                if (alpha >= x0 && alpha < x1)
                {
                    float local = alpha-x0;
                    float dx = x1-x0;
                    float relative = local / dx;
                    
                    float y0 = values.get(i+0).y;
                    float y1 = values.get(i+1).y;
                    float dy = y1-y0;
                    return y0 + relative * dy;
                }
            }
            return values.get(values.size()-1).y;
        }
        
    }
    
    
    /**
     * Inner class that performs the animation
     */
    class FlipAnimator implements Runnable
    {
        private boolean stopped = false;
        
        void stop()
        {
            synchronized (FlipLayout3D.this)
            {
                stopped = true;
                FlipLayout3D.this.notifyAll();
            }
        }
        
        public void run()
        {
            while (!stopped)
            {
                boolean triggerLayout = false;

                int indexDelta = Math.abs(targetIndexOffset-currentIndexOffset);
                currentAlphaOffsetStepSize = (indexDelta + 1) * alphaOffsetStepSize;
                
                // If the current offset is non-zero, then animate the
                // components until the offset is zero
                if (currentAlphaOffset > 0)
                {
                    float nextAlpha = currentAlphaOffset - currentAlphaOffsetStepSize;
                    currentAlphaOffset = Math.max(nextAlpha, 0);
                    triggerLayout = true;
                }
                else if (currentAlphaOffset < 0)
                {
                    float nextAlpha = currentAlphaOffset + currentAlphaOffsetStepSize;
                    currentAlphaOffset = Math.min(nextAlpha, 0);
                    triggerLayout = true;
                }
                else
                {
                    // The index of the front component has changed -
                    // head toward the target index.
                    if (currentIndexOffset < targetIndexOffset)
                    {
                        currentIndexOffset++;
                        currentAlphaOffset = alphaOffsetSize;
                        triggerLayout = true;
                    }
                    else if (currentIndexOffset > targetIndexOffset)
                    {
                        currentIndexOffset--;
                        currentAlphaOffset = -alphaOffsetSize;
                        triggerLayout = true;
                    }
                }
                
                // Only trigger the layout if something has changed
                if (triggerLayout)
                {
                    getSwoglContainer().validate3D();
                    try
                    {
                        Thread.sleep(ANIMATION_DELAY);
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
                else
                {
                    // Wait until this FlipAnimator is triggered
                    synchronized (FlipLayout3D.this)
                    {
                        try
                        {
                            FlipLayout3D.this.wait();
                        }
                        catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }
    }
    

}
