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

import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;

import swogl.*;

/**
 * An implementation of the LayoutManager3D interface that arranges
 * the components as a wheel. The wheel of components may then be
 * rotated with the mouse wheel.
 */
public class WheelLayout3D extends AbstractLayout3D implements LayoutManager3D
{

    /**
     * Time in milliseconds between the animation steps
     */
    private static final long ROTATION_DELAY = 30;

    /**
     * Constant for the speed of the wheel rotation
     */
    private static final float ROTATION_SPEED = 0.75f;
    
    /**
     * The rotation angle between two components
     */
    private float angleDelta = 0;

    /**
     * The step size for the wheel rotation.
     */
    private float rotationAngleStepSize = 0.5f;
    
    /**
     * The current rotation angle of the wheel
     */
    private float currentAngleOffset = 0;
    
    /**
     * The rotation angle the wheel animation is heading for
     */
    private float targetAngleOffset = 0;

    /**
     * The WheelAnimator that animates the wheel
     */
    private WheelAnimator wheelAnimator;
    
    /**
     * Creates a new WheelLayout3D
     */
    public WheelLayout3D()
    {
    }

    /**
     * {@inheritDoc}
     */
    public void setSwoglContainer(SwoglContainer swoglContainer)
    {
        super.setSwoglContainer(swoglContainer);
        if (wheelAnimator != null)
        {
            wheelAnimator.stop();
            wheelAnimator = null;
        }
        if (swoglContainer != null)
        {
            wheelAnimator = new WheelAnimator();
            Thread animatorThread = new Thread(wheelAnimator);
            animatorThread.start();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void doLayout3D()
    {
        super.doLayout3D();

        float radius = 0.0f;
        angleDelta = 0;
        int componentCount = getSwoglContainer().getSwoglComponentCount();
        if (componentCount > 1)
        {
        	// Compute the height of the highest SwoglComponent
        	float maxHeight = Float.NEGATIVE_INFINITY;
        	List<BoundingBox> boundingBoxes = new ArrayList<BoundingBox>();
        	for (int i = 0; i < componentCount; i++)
        	{
        		SwoglComponent swoglComponent = getSwoglContainer().getSwoglComponent(i);
        		BoundingBox boundingBox = swoglComponent.getUntransformedBoundingBox();
        		boundingBoxes.add(boundingBox);
        		maxHeight = Math.max(maxHeight, boundingBox.getSizeY());
        	}

        	// Compute the angle between the SwoglComponents and
        	// the wheel radius, and the rotation speed
        	angleDelta = (float) (Math.PI * 2) / componentCount;
        	radius = maxHeight / (2 * (float) Math.tan(angleDelta / 2));
        	rotationAngleStepSize = ROTATION_SPEED / componentCount; 
        }

        // Arrange all SwoglComponents in a wheel shape
        Matrix4f transform = new Matrix4f();
        for (int i = 0; i < componentCount; i++)
        {
        	SwoglComponent swoglComponent = getSwoglContainer().getSwoglComponent(i);
        	BoundingBox boundingBox = swoglComponent.getUntransformedBoundingBox();
        	transform.setIdentity();
        	transform.mul(MatrixUtils.translate(0, 0, -radius));
        	transform.mul(MatrixUtils.rotateX(i * angleDelta + currentAngleOffset));
        	transform.mul(MatrixUtils.translate(0, 0, radius + boundingBox.getSizeZ() + 0.1f));
        	swoglComponent.setTransform(transform);
        }
    }

    /**
     * Rotate the wheel about the given number of steps
     * 
     * @param steps The number of steps to rotate
     */
    private void rotateWheel(int steps)
    {
        while (currentAngleOffset > Math.PI * 2)
        {
            currentAngleOffset -= Math.PI * 2;
        }
        
        // Compute the target angle from the current angle
        // and the number of steps that should be rotated.
        int currentStep = Math.round(currentAngleOffset / angleDelta);
        int targetStep = (currentStep + steps);
        targetAngleOffset = targetStep * angleDelta;

        //System.out.println("rotation " + steps);
        //System.out.println("current " + currentStep + " target " + targetStep);
        //System.out.println("currentAngle " + currentAngleOffset + " targetAngle " + targetAngleOffset);

        synchronized(this)
        {
            // Notify the WheelAnimator
            notifyAll();
        }
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        rotateWheel(e.getWheelRotation());
    }

    
    
    /**
     * The class that performs the wheel rotation animation
     */
    class WheelAnimator implements Runnable
    {
        private boolean stopped = false;
        
        void stop()
        {
            synchronized (WheelLayout3D.this)
            {
                stopped = true;
                WheelLayout3D.this.notifyAll();
            }
        }
        
        public void run()
        {
            while (!stopped)
            {
                if (currentAngleOffset != targetAngleOffset)
                {
                    
                    // Head toward the targetAngleOffset
                    if (currentAngleOffset < targetAngleOffset)
                    {
                        float a = currentAngleOffset + rotationAngleStepSize;
                        currentAngleOffset = Math.min(a, targetAngleOffset);
                    }
                    else if (currentAngleOffset > targetAngleOffset)
                    {
                        float a = currentAngleOffset - rotationAngleStepSize;
                        currentAngleOffset = Math.max(a, targetAngleOffset);
                    }
                    
                    getSwoglContainer().validate3D();
                    try
                    {
                        Thread.sleep(ROTATION_DELAY);
                    }
                    catch (InterruptedException e)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
                else
                {
                    // Wait until this WheelAnimator is triggered by
                    // rotating the mouse wheel
                    synchronized (WheelLayout3D.this)
                    {
                        try
                        {
                            WheelLayout3D.this.wait();
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
