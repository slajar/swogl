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

package swogl;

import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.media.opengl.*;
import javax.swing.*;
import javax.vecmath.*;

import swogl.layout.LayoutManager3D;
import swogl.layout.SpaceLayout3D;

/**
 * This class is a container for SwoglComponents. It contains the
 * GL area where the SwoglComponents will be rendered to, a Camera
 * to define the view, and an optional LayoutManager3D to arrange
 * the SwoglComponents in space. Additionally, it transforms the
 * mouse events to allow the user to either interact with the 
 * Swing Components that are displayed in the SwoglComponents, or
 * to affect the Camera or layout of this SwoglContainer.
 */
public class SwoglContainer extends JComponent implements GLEventListener
{
    /**
     * Enumeration of the different control modes for this SwoglContainer.
     */
    public enum ControlMode
    {
        /**
         * Default control mode: The mouse events will be transformed and
         * passed to the SwoglComponents that are hit.
         */
        DEFAULT,
        
        /**
         * Layout control mode: The mouse events will be passed to the 
         * LayoutManager3D that is assigned to this SwoglContainer
         */
        LAYOUT,
        
        /**
         * Camera control mode: The mouse events will be passed to 
         * the Camera
         */
        CAMERA
    }
    
    /**
     * Indicates whether the init-method of this GLEventListener has
     * been called and it is ready to execute the display-method
     */
    private boolean initialized = false;
    
    /**
     * The List of SwoglComponents in this SwoglContainer
     */
    private List<SwoglComponent> swoglComponents = 
    	new CopyOnWriteArrayList<SwoglComponent>();

    /**
     * The GL component that actually displays the SwoglComponents
     */
    private GLJPanel glComponent;

    /**
     * The LayoutManager3D that lays out the SwoglComponents in
     * this SwoglContainer 
     */
    private LayoutManager3D layoutManager3D;
    
    /**
     * The Camera that provides the view to the contents of this
     * SwoglContainer
     */
    private Camera camera;

    /**
     * The current controlMode
     */
    private ControlMode currentControlMode = ControlMode.DEFAULT;
    
    /**
     * Used for the computation of the picking ray for this SwoglContainer
     */
    private Picker picker = new Picker();
    
    /**
     * The container for the contents of the individual SwoglComponents.
     * Has to be initialized here, because it may be used early, by 
     * the RepaintManager! 
     */
    private JPanel containerPanel = new JPanel();

    /**
     * The light setup, maintaining the lights that are illuminating
     * the contents of this SwoglContainer
     */
    private LightSetup lightSetup = new LightSetup();
    
    
    /**
     * Creates a new, empty SwoglContainer
     */
    public SwoglContainer()
    {
        SwoglCore.registerSwoglContainer(this);

        setLayout3D(new SpaceLayout3D());

        camera = new Camera();
        
        super.setLayout(new GridLayout(1,1));

        glComponent = new GLJPanel()
        /*// For debugging
        {
            public String toString()
            {
                return "GLPanel for "+SwoglContainer.this;
            }
        }
        //*/
        ;
        
        glComponent.addGLEventListener(this);

        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.X_AXIS));
        final JScrollPane containerScrollPane = new JScrollPane(containerPanel);
        
        final JLayeredPane layeredPane = new JLayeredPane();
        
        // For debugging:
        //layeredPane.setLayout(new GridLayout(2,1));
        
        layeredPane.add(containerScrollPane, Integer.valueOf(0));
        layeredPane.add(glComponent, Integer.valueOf(5000));
        
        super.add(layeredPane, BorderLayout.CENTER);

        layeredPane.addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                containerScrollPane.setBounds(layeredPane.getBounds());
                glComponent.setBounds(layeredPane.getBounds());
                validate();
            }
        });

        // Workaround for disappearing textures on GLJPanel
        addComponentListener(new ComponentAdapter()
        {
            public void componentResized(ComponentEvent e)
            {
                for (SwoglComponent swoglComponent : swoglComponents)
                {
                    swoglComponent.reinitTextures();
                }
            }
        });
        
        createLightSetup();
    }
    
    
    /**
     * Returns whether the given component is the GL component
     * of this SwoglContainer
     * 
     * @param component The component to check
     * @return Whether the given component is the GL component
     */
    boolean hasGlComponent(Component component)
    {
        return glComponent == component;
    }


    /**
     * Returns the SwoglComponent that contains the given component,
     * or null if no such SwoglComponent exists
     * 
     * @param c The component whose containing SwoglComponent should
     * be found
     * @return The SwoglComponent that contains the given component,
     * or null if no such SwoglComponent exists
     */
    SwoglComponent findSwoglComponentOwning(Component c)
    {
        for (SwoglComponent swoglComponent : swoglComponents)
        {
            if (swoglComponent.getContainerPanel().isAncestorOf(c))
            {
                return swoglComponent;
            }
        }
        return null;
    }
    
    /**
     * Causes a repaint of the GL component of this SwoglContainer.
     */
    @Override
    public void repaint()
    {
        glComponent.repaint();
    }
    
    
    /**
     * Calls display() on the GL component
     */
    void doDisplay()
    {
        glComponent.display();
    }
    
    /**
     * Returns the BoundingBox of ALL SwoglComponents inside
     * this SwoglContanier
     * 
     * @return The BoundingBox of this SwoglContainer
     */
    public BoundingBox getBoundingBox()
    {
        BoundingBox boundingBox = new BoundingBox();
        for (int i=0; i<getSwoglComponentCount(); i++)
        {
            boundingBox.combine(getSwoglComponent(i).getBoundingBox());
        }
        return boundingBox;
    }
    
    
    /**
     * Computes the PickingInfo for the given Point. A ray will be sent
     * into this SwoglContainer, from the current eye (camera) position
     * through the point in 3D space that corresponds to the given 
     * Point in screen coordinates. If a SwoglComponent is intersected
     * by the ray, the corresponding PickingInfo will be returned.
     * 
     * @param position A point on the screen
     * @return The PickingInfo - or null if no SwoglComponent was picked
     */
    public PickingInfo computePickingInfo(Point position)
    {
        float minDistance = Float.POSITIVE_INFINITY;

        Point3f rayOrigin = new Point3f();
        Vector3f rayDirection = new Vector3f();
        
        TexCoord2f texCoord = new TexCoord2f();
        Point3f intersectionPosition = new Point3f();

        SwoglComponent closestSwoglComponent = null;
        TexCoord2f closestTexCoord = null;
        Point3f closestIntersectionPosition = null;

        
        for (SwoglComponent swoglComponent : swoglComponents)
        {
            swoglComponent.computePickingRayData(position, rayOrigin, rayDirection);
            float distance = swoglComponent.findIntersectionPosition(
                rayOrigin, rayDirection, intersectionPosition, texCoord);
            //System.out.println("Check "+swoglComponent+" distance "+distance);
            if (distance < minDistance)
            {
                minDistance = distance;
                closestSwoglComponent = swoglComponent;
                closestTexCoord = new TexCoord2f(texCoord);
                closestIntersectionPosition = new Point3f(intersectionPosition);
            }
        }
        //System.out.println("closestSwoglComponent "+closestSwoglComponent);
        
        if (closestSwoglComponent == null)
        {
            return null;
        }
        return new PickingInfo(closestSwoglComponent, 
        	closestIntersectionPosition, closestTexCoord);
    }
    
    
    /**
     * The MouseEvent is first transformed to be relative to "this" and 
     * dispatched to all registered listeners. Then this method will
     * check the current ControlMode: <br />
     * <br />
     * In <code>DEFAULT</code> ControlMode, the event will be transformed
     * to the Component that is located on the SwoglComponent at the 
     * position that is hit by the mouse. If there is no SwoglComponent
     * hit by the mouse, then the event will simply be consumed.<br /> 
     * <br />
     * In <code>CAMERA</code> or <code>LAYOUT</code> ControlMode, the
     * event will be re-dispatched as a camera- or layout event on 'this' 
     * SwoglContainer and then consumed.
     * 
     * @param mouseEvent The event to transform
     */
    void handleMouseEvent(MouseEvent mouseEvent, Point initialPoint)
    {
        //System.out.println("handleMouseEvent on "+this);
        //System.out.println("handleMouseEvent "+EventUtils.createInfoString(mouseEvent));
        //lastMousePosition = mouseEvent.getPoint();
        
        Point originalPoint = mouseEvent.getPoint();

        Point point = SwingUtilities.convertPoint(mouseEvent.getComponent(), 
        	mouseEvent.getPoint(), this);
        mouseEvent.translatePoint(
        	point.x - mouseEvent.getX(), 
        	point.y - mouseEvent.getY());
        
        dispatchToListeners(mouseEvent);
        
        //System.out.println("mouseEvent on "+this);
        //System.out.println("controlMode "+getControlMode()+" button "+mouseEvent.getButton());
        
        
        if (getControlMode() == ControlMode.DEFAULT)
        {
            PickingInfo pickingInfo = computePickingInfo(mouseEvent.getPoint());
            
            //System.out.println("PickingInfo "+pickingInfo);
    
            if (pickingInfo == null)
            {
                mouseEvent.consume();
                return;
            }
            else 
            {
                SwoglComponent swoglComponent = pickingInfo.getSwoglComponent();
    
                // The position where the SwoglComponent was hit is given in 
                // texture coordinates. Now, compute the point in the 
                // SwoglComponent container panel that corresponds to the
                // texture coordinates.
                Component swoglComponentContainer = swoglComponent.getContainerPanel();
                Point inSwoglComponentContainer = 
                	swoglComponent.getPointFor(pickingInfo.getTexCoord());
    
                // Ensure that the respective point is visible - otherwise,
                // the events will not reach the desired target component.
                Point visible = new Point();
                visible.x = swoglComponentContainer.getX() + inSwoglComponentContainer.x;
                visible.y = swoglComponentContainer.getY() + inSwoglComponentContainer.y;
                containerPanel.scrollRectToVisible(new Rectangle(visible.x, visible.y, 1, 1));
    
                // Compute the location of the origin of the SwoglComponent 
                // container panel in the local coordinate system. 
                Point swoglComponentContainerLocation = 
                    SwingUtilities.convertPoint(swoglComponentContainer, 
                    	new Point(0,0), mouseEvent.getComponent());
    
                // Move the location of the mouse event from the local coordinate system
                // to the appropriate location to hit the container of the SwoglComponent
                // at the right place.
                Point real = mouseEvent.getPoint();
                Point faked = new Point();
                faked.x = swoglComponentContainerLocation.x + inSwoglComponentContainer.x;
                faked.y = swoglComponentContainerLocation.y + inSwoglComponentContainer.y;
                mouseEvent.translatePoint(faked.x - real.x, faked.y - real.y);
            }
        }
        else
        {
            // Current ControlMode is CAMERA or LAYOUT
            
            Point pointBefore = mouseEvent.getPoint();
            Object sourceBefore = mouseEvent.getSource();
    
            // Pretend that the event originated from the glComponent
            // of 'this' SwoglContainer
            mouseEvent.translatePoint(
            	initialPoint.x - mouseEvent.getX(), 
            	initialPoint.y - mouseEvent.getY());
            Point location = SwingUtilities.convertPoint(
            	mouseEvent.getComponent(), originalPoint, glComponent);
            mouseEvent.setSource(glComponent);
            mouseEvent.translatePoint(
            	location.x - mouseEvent.getX(), 
            	location.y - mouseEvent.getY());
    
            if (getControlMode() == ControlMode.LAYOUT)
            {
                dispatchLayoutEvent(mouseEvent);
                repaint();
                mouseEvent.consume();
            }
            else if (getControlMode() == ControlMode.CAMERA)
            {
                dispatchCameraEvent(mouseEvent);
                repaint();
                mouseEvent.consume();
            }
    
            // Restore the source and location of the event, so that it may be 
            // handled properly if someone wishes to handle consumed events
            mouseEvent.setSource(sourceBefore);
            mouseEvent.translatePoint(
            	pointBefore.x - mouseEvent.getX(), 
            	pointBefore.y - mouseEvent.getY());
        }
    }


    /**
     * Dispatches the given event to all event listeners that are
     * registered for this component
     * 
     * @param mouseEvent The event to dispatch
     */
    private void dispatchToListeners(MouseEvent mouseEvent)
    {
        for (MouseMotionListener mouseMotionListener : getMouseMotionListeners())
        {
            EventUtils.dispatchMouseMotionEvent(mouseEvent, mouseMotionListener);
        }
        for (MouseListener mouseListener : getMouseListeners())
        {
            EventUtils.dispatchMouseEvent(mouseEvent, mouseListener);
        }
        for (MouseWheelListener mouseWheelListener : getMouseWheelListeners())
        {
            EventUtils.dispatchMouseWheelEvent(mouseEvent, mouseWheelListener);
        }
    }
    
    
    /**
     * Will dispatch the given event to the LayoutManager3D, 
     * if the LayoutManager3D is non-null
     * 
     * @param event The event to dispatch
     */
    private void dispatchLayoutEvent(AWTEvent event)
    {
        //System.out.println("Dispatched layout event "+EventUtils.createInfoString(event));

        if (layoutManager3D != null)
        {
            EventUtils.dispatchMouseEvent(event, layoutManager3D);
            EventUtils.dispatchMouseMotionEvent(event, layoutManager3D);
            EventUtils.dispatchMouseWheelEvent(event, layoutManager3D);
            EventUtils.dispatchKeyEvent(event, layoutManager3D);
        }
    }

    /**
     * Will dispatch the given event to the Camera
     * 
     * @param event The event to dispatch
     */
    private void dispatchCameraEvent(AWTEvent event)
    {
        //System.out.println("Dispatched camera event "+EventUtils.createInfoString(event));

        EventUtils.dispatchMouseEvent(event, camera);
        EventUtils.dispatchMouseMotionEvent(event, camera);
        EventUtils.dispatchMouseWheelEvent(event, camera);
    }
    
    

    /**
     * Set the LayoutManager3D that should be used by this 
     * SwoglContainer to lay out its SwoglComponents
     * 
     * @param newLayoutManager3D The LayoutManager3D to use
     */
    public void setLayout3D(LayoutManager3D newLayoutManager3D)
    {
        //System.out.println("setLayout3D "+layoutManager3D);
        
        if (layoutManager3D != newLayoutManager3D)
        {
            if (layoutManager3D != null)
            {
                layoutManager3D.setSwoglContainer(null);
            }
            layoutManager3D = newLayoutManager3D;
            if (layoutManager3D != null)
            {
                layoutManager3D.setSwoglContainer(this);
                layoutManager3D.setActive(currentControlMode == ControlMode.LAYOUT);
                layoutManager3D.doLayout3D();
            }
        }
    }
    
    /**
     * Returns the current LayoutManager3D
     * 
     * @return The current LayoutManager3D
     */ 
    public LayoutManager3D getLayout3D()
    {
        return layoutManager3D;
    }
    

    /**
     * Adds the given SwoglComponent to this SwoglContainer
     * 
     * @param swoglComponent The SwoglComponent to add
     */
    public void add(SwoglComponent swoglComponent)
    {
        swoglComponents.add(swoglComponent);
        containerPanel.add(swoglComponent.getContainerPanel());
        layoutManager3D.doLayout3D();
        swoglComponent.setOwner(this);
        validate();
        repaint();
    }

    /**
     * Removes the given SwoglComponent from this SwoglContainer
     * 
     * @param swoglComponent The SwoglComponent to remove
     */
    public void remove(SwoglComponent swoglComponent)
    {
        swoglComponents.remove(swoglComponent);
        containerPanel.remove(swoglComponent.getContainerPanel());
        layoutManager3D.doLayout3D();
        swoglComponent.setOwner(null);
        validate();
        repaint();
    }

    /**
     * Overridden method {@link Container#remove(int)}, which removes
     * the SwoglComponent with the given index (instead of the 
     * Component with the given index)
     * 
     * @param index The index of the SwoglComponent to remove
     */
    @Override
    public void remove(int index)
    {
    	remove(swoglComponents.get(index));
    }
    
    /**
     * Overridden method {@link Container#removeAll()}, which removes
     * all SwoglComponents (instead of all Components)
     */
    @Override
    public void removeAll()
    {
    	int n = getSwoglComponentCount();
    	for (int i=0; i<n; i++)
    	{
    		SwoglComponent swoglComponent = swoglComponents.get(0);
            swoglComponents.remove(swoglComponent);
            containerPanel.remove(swoglComponent.getContainerPanel());
            swoglComponent.setOwner(null);
    	}
        layoutManager3D.doLayout3D();
        validate();
        repaint();
    }
    
    
    /**
     * Will cause the LayoutManager3D of this SwoglContainer
     * to be called in order to update the layout of the
     * SwoglComponents. <br />
     * <br />
     * This method is thread-safe, that is, it may be called
     * from any thread, and take care for the synchronization.
     */
    public void validate3D()
    {
    	if (SwingUtilities.isEventDispatchThread())
    	{
			layoutManager3D.doLayout3D();
			repaint();
    	}
    	else
    	{
	    	SwingUtilities.invokeLater(new Runnable()
	    	{
	    		public void run()
	    		{
	    			layoutManager3D.doLayout3D();
	    			repaint();
	    		}
	    	});
    	}
    }
    
    
    
    /**
     * Returns the number of SwoglComponents that are currently
     * present in this SwoglContainer
     * 
     * @return The number of SwoglComponents in this SwoglContainer
     */
    public int getSwoglComponentCount()
    {
        int count = 0;
        for (SwoglComponent swoglComponent : swoglComponents)
        {
            if (!swoglComponent.isPopup())
            {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the SwoglComponent with the given index
     * 
     * @param index The index of the SwoglComponent
     * @return The SwoglComponent with the given index
     */
    public SwoglComponent getSwoglComponent(int index)
    {
        int idx = -1;
        for (SwoglComponent swoglComponent : swoglComponents)
        {
            if (!swoglComponent.isPopup())
            {
                idx++;
            }
            if (idx == index)
            {
                return swoglComponent;
            }
        }
        throw new IndexOutOfBoundsException("Index "+index+" out of bounds");
    }

    /**
     * Sets the current control mode of this SwoglContainer. When the
     * ControlMode is LAYOUT, then the LayoutManager3D of this 
     * SwoglContainer will be activated, and subsequent mouse events
     * will be passed to the LayoutManager3D. 
     * 
     * @param controlMode The current ControlMode of this SwoglContainer
     */
    public void setControlMode(ControlMode controlMode)
    {
        if (controlMode != currentControlMode)
        {
            currentControlMode = controlMode;
            
            //System.out.println("Set to controlMode "+controlMode+" with layoutManager "+layoutManager3D);
            
            if (layoutManager3D != null)
            {
                layoutManager3D.setActive(currentControlMode == ControlMode.LAYOUT);
                layoutManager3D.doLayout3D();
            }
        }
    }
    
    /**
     * Returns the current ControlMode of this SwoglContainer
     * 
     * @return The current ControlMode of this SwoglContainer
     */
    public ControlMode getControlMode()
    {
        return currentControlMode;
    }
    

    /**
     * Computes the picking ray that is implied by sending a ray through
     * the given 'point' (in screen coordinates). The result will be 
     * stored in the given arguments.
     * 
     * @param point The point where the ray should start
     * @param rayOrigin Will contain the origin of the picking ray
     * @param rayDirection Will contain the normalized direction of the picking ray
     */
    public void computePickingRayData(Point point, Point3f rayOrigin, Vector3f rayDirection)
    {
        picker.computePickingRayData(point.x, point.y, rayOrigin, rayDirection);
    }
    
    
    /**
     * Implementation of GLEventListener: Initialize with the given
     * GLAutoDrawable
     * 
     * @param drawable The GLAutoDrawable to use
     */
    public void init(GLAutoDrawable drawable)
    {
        final GL gl = drawable.getGL();
        gl.setSwapInterval(0);

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glClearColor(0, 0, 0, 0);

        gl.glEnable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_NORMALIZE);
        gl.glLightModelf(GL.GL_LIGHT_MODEL_TWO_SIDE, 1);

        initialized = true;
    }

    /**
     * This method will be called during the initialization, to create
     * the lights for this SwoglContainer. <br />
     * <br />
     * This method may be overridden to create own light setups using the 
     * {@link SwoglContainer#createLight()} method.<br />
     * <br />
     * <b>NOTE</b>: This method should be considered as being PRELIMINARY and may 
     * change in future releases.
     */
    protected void createLightSetup()
    {
        Light light0 = createLight();
        light0.setAmbientColor(0.7f, 0.7f, 0.7f);
        light0.setDiffuseColor(1.1f, 1.1f, 1.1f);
        light0.setSpecularColor(1.0f, 1.0f, 1.0f);
        light0.setPosition(-500, 1500, 1000);
        light0.setDirection(500, -1500, -1000);
        light0.setDirectional(true);

        Light light1 = createLight();
        light1.setAmbientColor(0.7f, 0.7f, 0.7f);
        light1.setDiffuseColor(0.9f, 0.9f, 0.9f);
        light1.setSpecularColor(1.0f, 1.0f, 1.0f);
        light1.setPosition(500, -500, -500);
        light1.setDirection(-500, 500, 500);
        light1.setDirectional(true);
    }
    

    /**
     * Creates a new Light in this SwoglContainer and returns it
     * for possible subsequent manipulation by the caller.<br />
     * <br />
     * Note that there is a maximum of 8 lights that may be 
     * created. If this SwoglContainer already contains 8
     * lights, then this method will return 'null'.
     * <br />
     * <b>NOTE</b>: This method should be considered as being PRELIMINARY and may 
     * change in future releases.
     * 
     * @return The Light that was created.
     */
    public Light createLight()
    {
        Light light = lightSetup.createLight();
        repaint();
        return light;
    }
    
    /**
     * Returns the number of lights in this SwoglContainer.
     * <br />
     * <br />
     * <b>NOTE</b>: This method should be considered as being PRELIMINARY and may 
     * change in future releases.
     * 
     * @return The number of lights in this SwoglContainer.
     */
    public int getNumLights()
    {
        return lightSetup.getNumLights();
    }
    
    /**
     * Remove the Light with the given index.
     * <br />
     * <br />
     * <b>NOTE</b>: This method should be considered as being PRELIMINARY and may 
     * change in future releases.
     * 
     * @param index The index of the Light to remove.
     */
    public void removeLight(int index)
    {
        lightSetup.removeLight(index);
    }
    
    /**
     * Removes all lights in this SwoglContainer.
     * <br />
     * <br />
     * <b>NOTE</b>: This method should be considered as being PRELIMINARY and may 
     * change in future releases.
     */
    public void clearLights()
    {
        lightSetup.clear();
    }
    

    /**
     * {@inheritDoc}
     */
    public void display(GLAutoDrawable drawable)
    {
        if (!initialized)
        {
            return;
        }
        GL gl = drawable.getGL();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        camera.apply(drawable);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();

        picker.update(gl);

        lightSetup.apply(gl);
        
        //System.out.println("Display " + swoglComponents.size() + " swoglComponents on "+Thread.currentThread());
        for (SwoglComponent swoglComponent : swoglComponents)
        {
        	swoglComponent.display(drawable);
        }
    }


    /**
     * {@inheritDoc}
     */
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
    {
        camera.setViewportSize(width, height);
        camera.apply(drawable);
    }

    /**
     * {@inheritDoc}
     */
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
    {}

    
    //========================================================================
    // Overridden methods of Container, to restrict the access
    
    /**
     * Overridden method {@link Container#setLayout(LayoutManager)}, which throws an
     * UnsupportedOperationException: The Layout of a SwoglContainer may not be
     * changed. Use {@link SwoglContainer#setLayout3D(LayoutManager3D)} to set 
     * the 3D layout.
     */
    @Override
    public void setLayout(LayoutManager layoutManager)
    {
        throw new UnsupportedOperationException(
        	"May only change the 3D layout of a SwoglContainer");
    }

    /**
     * Overridden method {@link Container#add(Component)}, which throws an
     * UnsupportedOperationException: Only SwoglComponents may be added
     * to a SwoglContainer.
     */
    @Override
    public Component add(Component component)
    {
        throw new UnsupportedOperationException(
        	"May only add SwoglComponents to a SwoglContainer");
    }
    
    /**
     * Overridden method {@link Container#add(Component, Object)}, which throws an
     * UnsupportedOperationException: Only SwoglComponents may be added
     * to a SwoglContainer.
     */
    @Override
    public void add(Component component, Object contraints)
    {
        throw new UnsupportedOperationException(
        	"May only add SwoglComponents to a SwoglContainer");
    }

    /**
     * Overridden method {@link Container#add(Component, int)}, which throws an
     * UnsupportedOperationException: Only SwoglComponents may be added
     * to a SwoglContainer.
     */
    @Override
    public Component add(Component component, int index)
    {
        throw new UnsupportedOperationException(
        	"May only add SwoglComponents to a SwoglContainer");
    }

    /**
     * Overridden method {@link Container#add(Component, Object, int)}, which throws an
     * UnsupportedOperationException: Only SwoglComponents may be added
     * to a SwoglContainer.
     */
    @Override
    public void add(Component component, Object constraints, int index)
    {
        throw new UnsupportedOperationException(
        	"May only add SwoglComponents to a SwoglContainer");
    }

    /**
     * Overridden method {@link Container#add(String, Component)}, which throws an
     * UnsupportedOperationException: Only SwoglComponents may be added
     * to a SwoglContainer.
     */
    @Override
    public Component add(String name, Component component)
    {
        throw new UnsupportedOperationException(
        	"May only add SwoglComponents to a SwoglContainer");
    }
    
    /**
     * Overridden method {@link Container#remove(Component)}, which throws an
     * UnsupportedOperationException: Only SwoglComponents may be removed
     * from a SwoglContainer.
     */
    @Override
    public void remove(Component component)
    {
        throw new UnsupportedOperationException(
        	"May only remove SwoglComponents from a SwoglContainer");
    }

    
    
    

    
    // An attempt to get recursive SwoglContainers working, but this has 
    // many implications concerning the interaction and the rendering,
    // so this functionality is currently omitted: 
    // Recursive SwoglContainers are not officially supported.
    /*
    void handleMouseEventRecursive(MouseEvent mouseEvent, Point initialPoint)
    {
        //System.out.println("handleMouseEvent on "+this);
        //System.out.println("handleMouseEvent "+EventUtils.createInfoString(mouseEvent));
        //lastMousePosition = mouseEvent.getPoint();
        
        Point originalPoint = mouseEvent.getPoint();

        Point point = SwingUtilities.convertPoint(mouseEvent.getComponent(), mouseEvent.getPoint(), this);
        mouseEvent.translatePoint(point.x - mouseEvent.getX(), point.y - mouseEvent.getY());
        
        dispatchToListeners(mouseEvent);
        
        PickingInfo pickingInfo = computePickingInfo(mouseEvent.getPoint());
        
        //System.out.println("PickingInfo "+pickingInfo);

        if (pickingInfo != null)
        {
            SwoglComponent swoglComponent = pickingInfo.getSwoglComponent();

            // The position where the SwoglComponent was hit is given in 
            // texture coordinates. Now, compute the point in the 
            // SwoglComponent container panel that corresponds to the
            // texture coordinates.
            Component swoglComponentContainer = swoglComponent.getContainerPanel();
            Point inSwoglComponentContainer = swoglComponent.getPointFor(pickingInfo.getTexCoord());

            // Ensure that the respective point is visible - otherwise,
            // the events will not reach the desired target component.
            Point visible = new Point();
            visible.x = swoglComponentContainer.getX() + inSwoglComponentContainer.x;
            visible.y = swoglComponentContainer.getY() + inSwoglComponentContainer.y;
            containerPanel.scrollRectToVisible(new Rectangle(visible.x, visible.y, 1, 1));

            // Compute the location of the origin of the SwoglComponent 
            // container panel in the local coordinate system. 
            Point swoglComponentContainerLocation = 
                SwingUtilities.convertPoint(swoglComponentContainer, new Point(0,0), mouseEvent.getComponent());

            // Move the location of the mouse event from the local coordinate system
            // to the appropriate location to hit the container of the SwoglComponent
            // at the right place.
            Point real = mouseEvent.getPoint();
            Point faked = new Point();
            faked.x = swoglComponentContainerLocation.x + inSwoglComponentContainer.x;
            faked.y = swoglComponentContainerLocation.y + inSwoglComponentContainer.y;
            mouseEvent.translatePoint(faked.x - real.x, faked.y - real.y);

            // If an inner SwoglContainer is hit by the mouse event, the
            // event is passed down to the hit SwoglContainer recursively 
            SwoglContainer innerSwoglContainer = 
                SwoglCore.findSwoglContainerFor(swoglComponentContainer, 
                    inSwoglComponentContainer.x, 
                    inSwoglComponentContainer.y);
            if (innerSwoglContainer != null)
            {
                innerSwoglContainer.handleMouseEvent(mouseEvent, initialPoint);                
            }
        }

        //System.out.println("mouseEvent on "+this);
        //System.out.println("mouseEvent is consumed? "+mouseEvent.isConsumed());
        //System.out.println(" controlMode "+getControlMode()+" button "+mouseEvent.getButton());
        
        // If the mouse event has not been consumed yet, it may be an event
        // that affects the layout or camera of 'this' SwoglContainer
        if (!mouseEvent.isConsumed())
        {
            Point pointBefore = mouseEvent.getPoint();
            Object sourceBefore = mouseEvent.getSource();
            
            // Pretend that the event originated from the glComponent
            // of 'this' SwoglContainer
            mouseEvent.translatePoint(initialPoint.x - mouseEvent.getX(), initialPoint.y - mouseEvent.getY());
            Point location = SwingUtilities.convertPoint(mouseEvent.getComponent(), originalPoint, glComponent);
            mouseEvent.setSource(glComponent);
            mouseEvent.translatePoint(location.x - mouseEvent.getX(), location.y - mouseEvent.getY());
            
            if (getControlMode() == ControlMode.LAYOUT)
            {
                dispatchLayoutEvent(mouseEvent);
                repaint();
                mouseEvent.consume();
            }
            else if (getControlMode() == ControlMode.CAMERA)
            {
                dispatchCameraEvent(mouseEvent);
                repaint();
                mouseEvent.consume();
            }

            // Workaround for components reacting although they are not
            // hit by the mouse. Recursion will not work with this!
            if (pickingInfo == null)
            {
                mouseEvent.consume();
                return;
            }
            
            // Restore the source and location of the event, so that it may
            // be handled properly by parent components if it has not been
            // consumed yet
            mouseEvent.setSource(sourceBefore);
            mouseEvent.translatePoint(pointBefore.x - mouseEvent.getX(), pointBefore.y - mouseEvent.getY());
        }
    }
    */
}
