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


import java.awt.event.*;

import javax.vecmath.*;

import swogl.*;



// TODO: The overview should try to achieve a "best fit" 
// arrangement of the available components. Currently, 
// this class contains many hard-coded constants which
// are only appropriate for few components


/**
 * An implementation of the LayoutManager3D interface that allows browsing 
 * through the components with the mouse wheel. It arranges the
 * components of a SwoglContainer along the view direction, and
 * gives a browsable overview over the components when the 
 * layout is activated.
 */
public class BrowseLayout3D extends AbstractLayout3D implements LayoutManager3D
{
    /**
     * Time in milliseconds between the animation steps
     */
    private static final int ANIMATION_DELAY = 30;
    
    /**
     * The index of the frontmost SwoglComponent
     */
    private int currentIndexOffset = 0;
    
    /**
     * The index that is currently animated to be the index of
     * the frontmost SwoglComponent
     */
    private int targetIndexOffset = 0;

    /**
     * The current offset that the sequence of SwoglComponents have
     * along the view direction.
     */
    private float currentOffset = 0;
    
    /**
     * The step size for the animation of the components along
     * the view axis when the next component is moved to the
     * front
     */
    private float offsetStep = 15.0f;

    /**
     * The distance between the components in Z- (view-) direction
     */
    private float componentDistanceZ = -100;

    /**
     * The step size for the interpolation between the browse-
     * view and the default view
     */
    private float alphaStep = 0.15f;
    
    /**
     * The current status of the browse view: 0.0 means default
     * view, and 1.0 means fully in browse view
     */
    private float currentAlpha = 0;
    
    /**
     * The browse view status that the animation is currently
     * heading for. May be 0.0 for deactivating the browse
     * view, or 1.0 for activating the browse view
     */
    private float targetAlpha = 0;

    /**
     * The initial axis along which the SwoglComponents are
     * arranged. By default this is the z-axis, i.e. along
     * the view direction.
     */
    private Vector3f initialAxis = new Vector3f(0,0,1);
    
    /**
     * The target axis for the browsing view
     */
    private Vector3f targetAxis = new Vector3f(2,-1,1);

    /**
     * The position where the row of SwoglComponents
     * starts. By default, the center of the first SwoglComponent
     * is in the origin. When browsing is activated, this point
     * moves to the lower right to give a more centered view
     * of the row of SwoglComponents
     */
    private Point3f initialPosition = new Point3f();
    
    /**
     * The target position where the row of SwoglComponents starts
     * in the browse view
     */
    private Point3f targetPosition = new Point3f(200,-150,-150);

    /**
     * The current angle about which the components are rotated
     * about the x-axis. By default, this is 0.0, but in 
     * the browse mode, the components are slightly rotated
     * to give a better overview.
     */
    private float currentAngleX = 0;
    
    /**
     * The target angle for the component rotation about the 
     * x axis
     */
    private float targetAngleX = -0.3f;

    /**
     * The current angle about which the components are rotated
     * about the y-axis. By default, this is 0.0, but in 
     * the browse mode, the components are slightly rotated
     * to give a better overview.
     */
    private float currentAngleY = 0;

    /**
     * The target angle for the component rotation about the 
     * y axis
     */
    private float targetAngleY = 0.3f;
    
    /**
     * The BrowsAnimator that performs the animation.
     */
    private BrowseAnimator browseAnimator;

    /**
     * Creates a new BrowseLayout 
     */
    public BrowseLayout3D()
    {
    }
    
    
    /**
     * {@inheritDoc}
     * 
     * This will cause the "browsing overview" to be activated.
     */
    @Override
    public void setActive(boolean active)
    {
        super.setActive(active);
        setBrowseModeActivated(active);
    }
    
    /**
     * Set the browse mode (overview mode) activation status
     * 
     * @param activated The browse mode activation status
     */
    private void setBrowseModeActivated(boolean activated)
    {
        if (activated)
        {
            //System.out.println("activated browse layout");
            targetAlpha = 1.0f;
            synchronized(this)
            {
                // Notify the BrowseAnimator
                notifyAll();
            }
        }
        else
        {
            //System.out.println("deactivated browse layout");
            targetAlpha = 0.0f;
            synchronized(this)
            {
                // Notify the BrowseAnimator
                notifyAll();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void setSwoglContainer(SwoglContainer swoglContainer)
    {
        super.setSwoglContainer(swoglContainer);
        if (browseAnimator != null)
        {
            browseAnimator.stop();
            browseAnimator = null;
        }
        if (swoglContainer != null)
        {
            browseAnimator = new BrowseAnimator();
            Thread animatorThread = new Thread(browseAnimator, 
            	"BrowseLayout3DAnimator");
            animatorThread.start();
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void doLayout3D()
    {
        super.doLayout3D();
        
        float currentDistance = 0;
        Vector3f currentAxis = new Vector3f();
        Point3f currentPosition = new Point3f();
        
        int componentCount = getSwoglContainer().getSwoglComponentCount();
        for (int i = 0; i < componentCount; i++)
        {

            // Lay out all SwoglComponents, starting at 
            // the currentIndexOffset
            int index = (i + currentIndexOffset) % componentCount;
            if (index < 0)
            {
                index += componentCount;
            }
            SwoglComponent swoglComponent = getSwoglContainer().getSwoglComponent(index);

            // Interpolate the origin of the row of SwoglComponents,
            // the orientation of the layout axis, and the rotation
            // angles, according to the current alpha. 
            currentPosition.interpolate(initialPosition, targetPosition, currentAlpha);
            currentAxis.interpolate(initialAxis, targetAxis, currentAlpha);
            currentAngleX = currentAlpha * targetAngleX;
            currentAngleY = currentAlpha * targetAngleY;

            Matrix4f transform = new Matrix4f();
            transform.setIdentity();

            // Translate along the layout axis
            Vector3f translation = new Vector3f();
            translation.scale(currentDistance + currentOffset, currentAxis);
            transform.mul(MatrixUtils.translate(translation));

            // Move the start of the layout axis to its current position
            transform.mul(MatrixUtils.translate(new Vector3f(currentPosition)));

            // Rotate the component according to the current angles
            transform.mul(MatrixUtils.rotateY(currentAngleY));
            transform.mul(MatrixUtils.rotateX(currentAngleX));

            swoglComponent.setTransform(transform);

            // Compute the distance for the next component
            float sizeZ = swoglComponent.getUntransformedBoundingBox().getSizeZ();
            currentDistance += sizeZ + componentDistanceZ;
        }
    }
    
    /**
     * Scroll through the components for the given number of steps
     * 
     * @param steps The number of steps
     */
    private void scrollAbout(int steps)
    {
        targetIndexOffset = currentIndexOffset + steps;
        synchronized(this)
        {
            // Notify the BrowseAnimator
            notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     * 
     * Will change the index of the frontmost component according to
     * the wheel rotation
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e)
    {
        scrollAbout(e.getWheelRotation());
    }


    
    /**
     * Inner class that performs the animation
     */
    class BrowseAnimator implements Runnable
    {
        private boolean stopped = false;
        
        void stop()
        {
            synchronized (BrowseLayout3D.this)
            {
                stopped = true;
                BrowseLayout3D.this.notifyAll();
            }
        }
        
        public void run()
        {
            while (!stopped)
            {
                //System.out.println("currentAlpha " + currentAlpha + " targetAlpha " + targetAlpha);
                boolean triggerLayout = false;

                // If the browsing mode has been activated or deactivated
                // (i.e. the target alpha has changed to 1.0 or 0.0), 
                // then let the currentAlpha heat toward the target
                if (currentAlpha < targetAlpha)
                {
                    currentAlpha = Math.min(currentAlpha + alphaStep, targetAlpha);
                    triggerLayout = true;
                }
                else if (currentAlpha > targetAlpha)
                {
                    currentAlpha = Math.max(currentAlpha - alphaStep, targetAlpha);
                    triggerLayout = true;
                }
                else
                {
                    // If the current offset is non-zero, then animate the
                    // row of components until the frontmost component has
                    // offset 0 along the layout axis
                    if (currentOffset > 0)
                    {
                        currentOffset = Math.max(currentOffset - offsetStep, 0);
                        triggerLayout = true;
                    }
                    else if (currentOffset < 0)
                    {
                        currentOffset = Math.min(currentOffset + offsetStep, 0);
                        triggerLayout = true;
                    }
                    else
                    {
                        // The index of the frontmost component has changed -
                        // head toward the target index.
                        if (currentIndexOffset < targetIndexOffset)
                        {
                            currentIndexOffset++;
                            currentOffset = componentDistanceZ;
                            triggerLayout = true;
                        }
                        else if (currentIndexOffset > targetIndexOffset)
                        {
                            currentIndexOffset--;
                            currentOffset = -componentDistanceZ;
                            triggerLayout = true;
                        }
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
                    // Wait until this BrowseAnimator is 
                    // triggered, either by activating or
                    // dectivating this layout, or by 
                    // rotating the mouse wheel
                    synchronized (BrowseLayout3D.this)
                    {
                        try
                        {
                            BrowseLayout3D.this.wait();
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
