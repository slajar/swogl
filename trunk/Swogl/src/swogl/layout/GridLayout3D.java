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

import java.awt.event.MouseEvent;

import javax.vecmath.*;

import swogl.*;

import java.util.*;

/**
 * An implementation of the LayoutManager3D interface that lays out
 * the SwoglComponents in a grid. Double-clicking a component 
 * zooms the component to unit size and centers it. Double-clicking
 * with the right mouse button (or outside of a SwoglComponent)
 * zooms out to make all components visible.
 */
public class GridLayout3D extends AbstractLayout3D implements LayoutManager3D
{
    /**
     * Time in milliseconds between the animation steps
     */
    private static final int ANIMATION_DELAY = 30;

    /**
     * The speed for the zooming animation
     */
    private static final float ZOOMING_SPEED = 0.075f;
    
    /**
     * The number of rows of this GridLayout3D
     */
    private int rows = -1;

    /**
     * The number of columns of this GridLayout3D
     */
    private int columns = -1;

    /**
     * The horizontal gap between components
     */
    private int hGap = -1;

    /**
     * The vertical gap between components
     */
    private int vGap = -1;
    
    /**
     * The scaling factor for the components in the
     * grid overview
     */
    private float overviewScale = 1.0f;
    
    /**
     * The translation in the grid overview
     */
    private Vector3f overviewTranslation = new Vector3f();

    /**
     * The scaling factor where the current animation started
     */
    private float sourceScale = 1.0f;
    
    /**
     * The translation where the current animation started
     */
    private Vector3f sourceTranslation = new Vector3f();
    
    /**
     * The scaling factor that the current animation is heading for
     */
    private float targetScale = 1.0f;
    
    /**
     * The translation that the current animation is heading for
     */
    private Vector3f targetTranslation = new Vector3f();

    /**
     * The current scaling factor
     */
    private float currentScale = 1.0f;
    
    /**
     * The current translation
     */
    private Vector3f currentTranslation = new Vector3f();
    
    /**
     * Whether this layout is currently in the overview mode
     */
    private boolean inOverviewMode = true;
    
    /**
     * The current alpha value between 0 and 1, describing the 
     * relative position between the source- and target scaling 
     * and translation.
     * Initialized with positive infinity to indicate an
     * invalid value.  
     */
    private float currentAlpha = 0;
    
    /**
     * The target alpha value between 0 and 1 that the animation
     * is currently heading for.
     */
    private float targetAlpha = 0;

    /**
     * A map from the current SwoglComponents to their position inside
     * the grid
     */
    private Map<SwoglComponent, Vector3f> gridPositions = 
        new HashMap<SwoglComponent, Vector3f>();
    
    /**
     * The ZoomingAnimator for zooming in and out
     */
    private ZoomingAnimator zoomingAnimator;
    
    /**
     * Creates a new GridLayout3D that lays out the SwoglComponents of 
     * a SwoglContainer in a grid with the given number of rows and
     * columns and with the given spacing between the components.
     * 
     * @param rows The rows of the grid
     * @param columns The columns of the grid.
     * @param hGap The horizontal gap between components
     * @param vGap The vertical gap between components
     */
    public GridLayout3D(int rows, int columns, int hGap, int vGap)
    {
        this.rows = rows;
        this.columns = columns;
        this.hGap = hGap;
        this.vGap = vGap;
    }

    /**
     * Creates a new GridLayout3D that lays out the SwoglComponents of a
     * SwoglContainer in a grid with the given number of rows and
     * columns and with no space between the components.
     * 
     * @param rows The rows of the grid
     * @param columns The columns of the grid.
     */
    public GridLayout3D(SwoglContainer swoglContainer, int rows, int columns)
    {
        this(rows, columns, 0, 0);
    }
    
    /**
     * {@inheritDoc}
     */
    public void setSwoglContainer(SwoglContainer swoglContainer)
    {
        super.setSwoglContainer(swoglContainer);
        if (zoomingAnimator != null)
        {
            zoomingAnimator.stop();
            zoomingAnimator = null;
        }
        if (swoglContainer != null)
        {
            computeOverview();

            sourceTranslation.set(overviewTranslation);
            sourceScale = overviewScale;
            
            targetTranslation.set(overviewTranslation);
            targetScale = overviewScale;
            
            zoomingAnimator = new ZoomingAnimator();
            Thread animatorThread = new Thread(zoomingAnimator,
            	"GridLayout3DAnimator");
            animatorThread.start();
        }
    }
    
    /**
     * Compute the overviewScale, overviewTranslation and the gridPositions.
     * Thus, all information that is required for the layout in the 
     * overview.
     */
    private void computeOverview()
    {
        // Compute the real number of rows and columns
        int numComponents = getSwoglContainer().getSwoglComponentCount();
        int rs = rows;
        int cs = columns;
        if (rs > 0) 
        {
            cs = (numComponents + rs - 1) / rs;
        } 
        else 
        {
            rs = (numComponents + cs - 1) / cs;
        }

        // Compute the maximum size of any SwoglComponent
        float maxW = -Float.MAX_VALUE;
        float maxH = -Float.MAX_VALUE;
        for (int c = 0; c < cs ; c++) 
        {
            for (int r = 0; r < rs ; r++) 
            {
                int i = r * cs + c;
                if (i < numComponents) 
                {
                    SwoglComponent swoglComponent = getSwoglContainer().getSwoglComponent(i);
                    BoundingBox boundingBox = swoglComponent.getUntransformedBoundingBox();
                    maxW = Math.max(maxW, boundingBox.getSizeX());
                    maxH = Math.max(maxH, boundingBox.getSizeY());
                }
            }
        }
        maxW += hGap;
        maxH += vGap;

        // Compute the positions of the SwoglComponents when they
        // are arranged in a grid
        gridPositions.clear();
        for (int r = 0; r < rs ; r++) 
        {
            for (int c = 0; c < cs ; c++) 
            {
                int i = r * cs + c;
                if (i < numComponents) 
                {
                    float x = c * maxW + maxW / 2;
                    float y = - (r * maxH + maxH / 2);
                    
                    //System.out.println("Component "+i+" at "+r+" "+c+" index "+i+" at "+x+" "+y);
                    
                    SwoglComponent swoglComponent = getSwoglContainer().getSwoglComponent(i);
                    gridPositions.put(swoglComponent, new Vector3f(x,y,0));
                }
            }
        }

        // Compute the overviewScale and overviewTranslation, i.e. the
        // scaling and translation that are required to make all 
        // components visible
        float totalW = maxW * cs;
        float totalH = maxH * rs;
        float containerW = getSwoglContainer().getWidth();
        float containerH = getSwoglContainer().getHeight();
        float scaleX = containerW / totalW;
        float scaleY = containerH / totalH;
        overviewScale = Math.min(scaleX, scaleY);
        float dx = - overviewScale * totalW / 2;
        float dy = overviewScale * totalH / 2;
        overviewTranslation = new Vector3f(dx, dy, 0);

        /*
        System.out.println("totalW "+totalW);
        System.out.println("totalH "+totalH);
        System.out.println("swoglContainer.getSize() "+getSwoglContainer().getSize());
        System.out.println("scaleX "+scaleX);
        System.out.println("scaleY "+scaleY);
        //*/
    }

    
    
    /**
     * {@inheritDoc}
     */
    public void doLayout3D()
    {
        super.doLayout3D();

        computeOverview();

        // If the overview is currently shown, adjust the layout
        // to take into account new components that may have
        // been added
        if (inOverviewMode)
        {
        	targetTranslation.set(overviewTranslation);
        	targetScale = overviewScale;
        	targetAlpha = 1.0f;
        	synchronized(this)
        	{
        		// Trigger the ZoomingAnimator
        		notifyAll();
        	}
        }

        // Compute the current scaling and translation according
        // to the currentAlpha, and compute the transformation
        // matrix that is common for all SwoglComponents
        currentScale = sourceScale + (targetScale - sourceScale) * currentAlpha; 
        currentTranslation.interpolate(sourceTranslation, targetTranslation, currentAlpha);
        Matrix4f transform = MatrixUtils.scale(currentScale, currentScale, currentScale);
        transform.setTranslation(currentTranslation);

        //System.out.println("Transform\n"+transform);

        // Arrange the SwoglComponents in a grid accroding to
        // their GridPositions that has been computed above
        int numComponents = getSwoglContainer().getSwoglComponentCount();
        for (int i=0; i<numComponents; i++)
        {
        	SwoglComponent swoglComponent = getSwoglContainer().getSwoglComponent(i);
        	Matrix4f matrix = new Matrix4f(transform);
        	Vector3f translation = gridPositions.get(swoglComponent);
        	matrix.mul(MatrixUtils.translate(translation));
        	swoglComponent.setTransform(matrix);
        }
    }
    
    
    /**
     * {@inheritDoc}
     * 
     * This will cause this GridLayout to zoomTo the double-clicked
     * component, or to 'null' if no component was hit or the right 
     * mouse button was double-clicked.
     */
    public void mouseClicked(MouseEvent event)
    {
        if (event.getClickCount() == 2)
        {
            PickingInfo pickingInfo = getSwoglContainer().computePickingInfo(event.getPoint());
            if (pickingInfo != null)
            {
                SwoglComponent clickedComponent = pickingInfo.getSwoglComponent();
                zoomTo(clickedComponent);
            }
            if (pickingInfo == null || event.getButton() == MouseEvent.BUTTON3)
            {
                zoomTo(null);
            }
            //System.out.println("nextFocussedComponent "+nextFocussedComponent);
        }
        //event.consume();
    }
    
    
    /**
     * Zoom to the given SwoglComponent. If the given SwoglComponent is 
     * null, then this GridLayout3D will zoom to the overview.
     * 
     * @param swoglComponent The SwoglComponent to zoom to.
     */
    private void zoomTo(SwoglComponent swoglComponent)
    {
        sourceScale = currentScale;
        sourceTranslation.set(currentTranslation);
        
        if (swoglComponent == null)
        {
            inOverviewMode = true;
            targetScale = overviewScale;
            targetTranslation.set(overviewTranslation);
        }
        else
        {
            inOverviewMode = false;
            targetScale = 1.0f;
            Vector3f translation = gridPositions.get(swoglComponent);
            targetTranslation.set(translation);
            targetTranslation.negate();
        }
        
        targetAlpha = 1.0f;
        currentAlpha = 0.0f;
        
        synchronized(this)
        {
            // Trigger the ZoomingAnimator
            notifyAll();
        }
    }
    
    /**
     * The class that performs the zooming animation
     */
    private class ZoomingAnimator implements Runnable
    {
        private boolean stopped = false;
        
        void stop()
        {
            synchronized (GridLayout3D.this)
            {
                stopped = true;
                GridLayout3D.this.notifyAll();
            }
        }
        
        public void run()
        {
            while (!stopped)
            {
                if (currentAlpha < targetAlpha)
                {
                    // Just head toward the target alpha
                    float a = currentAlpha + ZOOMING_SPEED;
                    currentAlpha = Math.min(a, targetAlpha);

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
                    // Wait until this ZoomingAnimator is triggered
                    // by a call to zoomTo or a layout operation in
                    // the overview mode
                    synchronized (GridLayout3D.this)
                    {
                        try
                        {
                            GridLayout3D.this.wait();
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
