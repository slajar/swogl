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
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.*;
import javax.swing.event.EventListenerList;

/**
 * The core class of Swogl, performing all static initializations
 */
class SwoglCore
{
    /**
     * A WeakHashMap containing the SwoglContainers that have been
     * created so far, and not yet been garbage collected
     */
    private static WeakHashMap<SwoglContainer, Object> swoglContainers = 
        new WeakHashMap<SwoglContainer, Object>();

    /**
     * Static initializations
     */
    static
    {
        initMouseEventQueue();
        initRepaintManager();
        initPopupFactory();
    }

    /**
     * Register the given SwoglContainer
     *
     * @param swoglContainer The SwoglContainer to register
     */
    static synchronized void registerSwoglContainer(SwoglContainer swoglContainer)
    {
        WeakHashMap<SwoglContainer, Object> newSwoglContainers = 
            new WeakHashMap<SwoglContainer, Object>(swoglContainers); 
        newSwoglContainers.put(swoglContainer, new Object());
        swoglContainers = newSwoglContainers;
    }


    /**
     * Initializes the EventQueue which catches ALL AWT events and checks
     * how to handle them if they originated from a SwoglContainer.
     *
     * If the event is a MouseEvent, it will be passed to the 
     * handleMouseEvent method of the SwoglContainer under the mouse, 
     * where it may possibly be consumed. 
     * 
     * If the event is no MouseEvent, or is a MouseEvent that did
     * not originate from a SwoglContainer, or is a MouseEvent that
     * has not been consumed by the SwoglContainer, the event is
     * treated normally. 
     */
    private static void initMouseEventQueue()
    {
        EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
        eventQueue.push(new EventQueue()
        {
            public void dispatchEvent(AWTEvent awtEvent)
            {
                //System.out.println("Dispatching on queue: "+EventUtils.createInfoString(awtEvent));
                if (awtEvent instanceof MouseEvent)
                {
                    MouseEvent mouseEvent = (MouseEvent)awtEvent;
                    SwoglContainer swoglContainer = findSwoglContainerFor(mouseEvent);
                    if (swoglContainer != null)
                    {
                        //System.out.println("Handle mouse event on "+swoglContainer);
                        swoglContainer.handleMouseEvent(mouseEvent, mouseEvent.getPoint());

                        if (mouseEvent.isConsumed())
                        {
                            return;
                        }
                    }
                }
                //System.out.println("Normally handling "+EventUtils.createInfoString(awtEvent));
                super.dispatchEvent(awtEvent);
            }
        });
    }

    /**
     * Returns the SwoglContainer that contains the glComponent that the
     * MouseEvent occurred over, or null if the MouseEvent did not happen
     * on a glComponent of a SwoglContainer
     *
     * @param mouseEvent The MouseEvent
     * @return The SwoglComponent that contains the glComponent that
     * the MouseEvent occurred over, or null if the MouseEvent did not
     * happen on a glComponent of a SwoglContainer
     */
    private static SwoglContainer findSwoglContainerFor(MouseEvent mouseEvent)
    {
        return findSwoglContainerFor(mouseEvent.getComponent(),
                                     mouseEvent.getX(),
                                     mouseEvent.getY());
    }

    /**
     * Returns the SwoglContainer that is a descendant of the given sourceComponent
     * and is the deepest component of this sourceComponent, or null if there is no
     * SwoglContainer
     *
     * @param mouseEvent The MouseEvent
     * @return Returns the SwoglContainer that is a descendant of the given sourceComponent
     * and is the deepest component of this sourceComponent, or null if there is no
     * SwoglContainer
     */
    static SwoglContainer findSwoglContainerFor(Component sourceComponent, int x, int y)
    {
        Component component = SwingUtilities.getDeepestComponentAt(sourceComponent, x, y);
        Map<SwoglContainer, Object> currentSwoglContainers = swoglContainers;
        for (SwoglContainer swoglContainer : currentSwoglContainers.keySet())
        {
            if (swoglContainer.hasGlComponent(component))
            {
                //System.out.println("At "+x+" "+y+" for "+component);
                //System.out.println("found "+swoglContainer);

                return swoglContainer;
            }
        }
        return null;
    }

    /**
     * Recursively adds the dirty regions to the SwoglComponents that result from
     * the given dirty region. That is, if a region of a Swing component is marked 
     * "dirty", then the corresponding region of the SwoglComponent will be 
     * marked dirty as well. <br />
     * <br /> 
     * If the SwoglComponent is contained in a SwoglContainer that is contained 
     * in a SwoglComponent, then the dirty region will be propagated up until
     * the topmost SwoglComponent has been reached and marked dirty. 
     * 
     * @param c The component with the given dirty region
     * @param x The x-position of the dirty region
     * @param y The y-position of the dirty region
     * @param w The width of the dirty region
     * @param h The height of the dirty region
     */
    private static void addDirtyRegionRec(Map<SwoglContainer, Object> currentSwoglContainers,
                    JComponent c, int x, int y, int w, int h)
    {
        // Note: The recursion is currently not really required,
        // because recursive SwoglContainers are not officially
        // supported, but this way this method is prepared for
        // recursive SwoglContainers
    	for (SwoglContainer swoglContainer : currentSwoglContainers.keySet())
    	{
    		if (swoglContainer.isAncestorOf(c))
    		{
    			SwoglComponent swoglComponent = swoglContainer.findSwoglComponentOwning(c);
    			if (swoglComponent != null)
    			{
    				swoglComponent.addDirtyRegion(c, x, y, w, h);
    				addDirtyRegionRec(currentSwoglContainers, swoglContainer,
    					swoglContainer.getX(), swoglContainer.getY(),
    					swoglContainer.getWidth(), swoglContainer.getHeight());
    			}
    		}
        }
    }


    /**
     * Will initialize the RepaintManager that causes the SwoglComponents
     * to be refreshed with the contents of the Swing Components they
     * represent in case that a region of the respective swing component
     * has been marked as "dirty"
     */
    private static void initRepaintManager()
    {
        RepaintManager.setCurrentManager(new RepaintManager()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void addDirtyRegion(JComponent c, int x, int y, int w, int h)
            {
                //System.out.println("addDirtyRegion " + x + " " + y + " " + w + " " + h + " on " + c);
                super.addDirtyRegion(c, x, y, w, h);
                Map<SwoglContainer, Object> currentSwoglContainers = swoglContainers;
                addDirtyRegionRec(currentSwoglContainers, c,x,y,w,h);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void paintDirtyRegions()
            {
                super.paintDirtyRegions();

                /**
                 * TODO It should not be necessary to display ALL SwoglContainers,
                 * but in most cases, there will hardly be more than one.
                 */
                Map<SwoglContainer, Object> currentSwoglContainers = swoglContainers;
                for (SwoglContainer swoglContainer : currentSwoglContainers.keySet())
                {
                    swoglContainer.doDisplay();
                }
            }
        });
    }

    /**
     * Initializes the PopupFactory, which creates SwoglPopups instead
     * of usual Popups when the "owner" of the Popup is a descendant
     * of a SwoglContainer
     */
    private static void initPopupFactory()
    {
        PopupFactory.setSharedInstance(new PopupFactory()
        {
            public Popup getPopup(Component owner, Component contents, int x, int y)
            {
                Map<SwoglContainer, Object> currentSwoglContainers = swoglContainers;
                SwoglContainer swoglContainer = 
                    findSwoglContainerOwning(currentSwoglContainers, owner);

                //System.out.println("Popup for SwoglContainer "+swoglContainer);
                //System.out.println("    at "+x+" "+y);
                //System.out.println("    owner    "+owner);
                //System.out.println("    contents "+contents);

                if (swoglContainer == null)
                {
                    return super.getPopup(owner, contents, x, y);
                }
                else
                {
                    return new SwoglPopup(owner, contents, x,y, swoglContainer);
                }
            }

        });
    }


    /**
     * Returns the SwoglContainer that contains the given component,
     * or null if no such SwoglContainer exists
     *
     * @param c The component whose ancestor should be returned
     * @return The SwoglContainer that contains the given component,
     * or null if no such SwoglContainer exists
     */
    private static SwoglContainer findSwoglContainerOwning(Map<SwoglContainer, Object> currentSwoglContainers, Component c)
    {
        for (SwoglContainer swoglContainer : currentSwoglContainers.keySet())
        {
            if (swoglContainer.isAncestorOf(c))
            {
                return swoglContainer;
            }
        }
        return null;
    }


    
    

}
