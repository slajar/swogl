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

import java.awt.AWTEvent;
import java.awt.event.*;

/**
 * This class contains some utility methods for event dispatching
 */
class EventUtils
{
    /**
     * Will pass the given event to the given listener's methods, if the
     * event has the appropriate type
     * 
     * @param event The event to process
     * @param listener The listener to receive the event
     */
    static void dispatchMouseWheelEvent(AWTEvent event, MouseWheelListener mouseWheelListener)
    {
        if (event instanceof MouseEvent)
        {
            MouseEvent mouseEvent = (MouseEvent)event;
            switch (mouseEvent.getID())
            {
                case MouseEvent.MOUSE_WHEEL:
                    mouseWheelListener.mouseWheelMoved((MouseWheelEvent)mouseEvent);
                    break;
            }
        }
    }
    
    /**
     * Will pass the given event to the given listener's methods, if the
     * event has the appropriate type
     * 
     * @param event The event to process
     * @param listener The listener to receive the event
     */
    static void dispatchMouseMotionEvent(AWTEvent event, MouseMotionListener mouseMotionListener)
    {
        if (event instanceof MouseEvent)
        {
            MouseEvent mouseEvent = (MouseEvent)event;
            switch (mouseEvent.getID())
            {
                case MouseEvent.MOUSE_MOVED:
                    mouseMotionListener.mouseMoved(mouseEvent);
                    break;

                case MouseEvent.MOUSE_DRAGGED:
                    mouseMotionListener.mouseDragged(mouseEvent);
                    break;
            }
        }
    }
    
    
    /**
     * Will pass the given event to the given listener's methods, if the
     * event has the appropriate type
     * 
     * @param event The event to process
     * @param listener The listener to receive the event
     */
    static void dispatchMouseEvent(AWTEvent event, MouseListener mouseListener)
    {
        if (event instanceof MouseEvent)
        {
            MouseEvent mouseEvent = (MouseEvent)event;
            switch (mouseEvent.getID())
            {
                case MouseEvent.MOUSE_CLICKED:
                    mouseListener.mouseClicked(mouseEvent);
                    break;

                case MouseEvent.MOUSE_ENTERED:
                    mouseListener.mouseEntered(mouseEvent);
                    break;

                case MouseEvent.MOUSE_EXITED:
                    mouseListener.mouseExited(mouseEvent);
                    break;

                case MouseEvent.MOUSE_PRESSED:
                    mouseListener.mousePressed(mouseEvent);
                    break;

                case MouseEvent.MOUSE_RELEASED:
                    mouseListener.mouseReleased(mouseEvent);
                    break;
            }
        }
    }
    
    /**
     * Will pass the given event to the given listener's methods, if the
     * event has the appropriate type
     * 
     * @param event The event to process
     * @param listener The listener to receive the event
     */
    static void dispatchKeyEvent(AWTEvent event, KeyListener keyListener)
    {
        if (event instanceof KeyEvent)
        {
            KeyEvent keyEvent = (KeyEvent)event;
            switch (keyEvent.getID())
            {
                case KeyEvent.KEY_PRESSED:
                    keyListener.keyPressed(keyEvent);
                    break;

                case KeyEvent.KEY_RELEASED:
                    keyListener.keyReleased(keyEvent);
                    break;
            
                case KeyEvent.KEY_TYPED:
                    keyListener.keyTyped(keyEvent);
                    break;
            }
        }
    }
    
    
    /**
     * Static helper creating a short String for the given event
     * 
     * @param event The event
     * @return A short string describing the event
     */
    static String createInfoString(AWTEvent event)
    {
        StringBuffer str = new StringBuffer(80);
        str.append(event.getClass().getSimpleName()+" ");
        
        if (event instanceof MouseEvent)
        {
            int id = event.getID();
            switch(id) {
                case MouseEvent.MOUSE_PRESSED:
                    str.append("MOUSE_PRESSED");
                    break;
                case MouseEvent.MOUSE_RELEASED:
                    str.append("MOUSE_RELEASED");
                    break;
                case MouseEvent.MOUSE_CLICKED:
                    str.append("MOUSE_CLICKED");
                    break;
                case MouseEvent.MOUSE_ENTERED:
                    str.append("MOUSE_ENTERED");
                    break;
                case MouseEvent.MOUSE_EXITED:
                    str.append("MOUSE_EXITED");
                    break;
                case MouseEvent.MOUSE_MOVED:
                    str.append("MOUSE_MOVED");
                    break;
                case MouseEvent.MOUSE_DRAGGED:
                    str.append("MOUSE_DRAGGED");
                    break;
                case MouseEvent.MOUSE_WHEEL:
                    str.append("MOUSE_WHEEL");
                    break;
                default:
                    str.append("unknown type");
            }
            MouseEvent mouseEvent = (MouseEvent)event;
            str.append(" at "+mouseEvent.getX()+","+mouseEvent.getY());
        }
        if (event instanceof KeyEvent)
        {
            int id = event.getID();
            switch(id) {
                case KeyEvent.KEY_PRESSED:
                    str.append("KEY_PRESSED");
                    break;
                case KeyEvent.KEY_RELEASED:
                    str.append("KEY_RELEASED");
                    break;
                default:
                    str.append("unknown type");
            }
            KeyEvent keyEvent = (KeyEvent)event;
            str.append("  "+InputEvent.getModifiersExText(keyEvent.getModifiersEx()));
        }
        str.append(" on "+event.getSource());
        return str.toString();
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private EventUtils()
    {
        
    }
    
}
