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

import javax.swing.Popup;
import javax.swing.SwingUtilities;


/**
 * This class is a Popup that may be used to show temporary,
 * "popping-up" SwoglComponents. Instances of this class are
 * returned by the shared PopupFactory instance that is 
 * established in the SwoglCore, if the owner of the Popup 
 * is a descendant of a SwoglContainer
 */
class SwoglPopup extends Popup
{
    /**
     * The owner of the Popup that is created, e.g. a JComboBox
     */
    private Component owner;
    
    /**
     * The contents of the Popup
     */
    private Component contents;
    
    /**
     * The SwoglContainer that contains the owner
     */
    private SwoglContainer swoglContainer;
    
    /**
     * The SwoglComponent that displays the popup contents
     */
    private SwoglComponent swoglPopupComponent;
    
    /**
     * The x coordinate on the screen where the Popup should occur
     */
    private int x;
    
    /**
     * The y coordinate on the screen where the Popup should occur
     */
    private int y;
    
    /**
     * Creates a new SwoglPopup, whose show/hide method will cause
     * a SwoglComponent with the given contents to be placed in the
     * given SwoglContainer
     * 
     * @param owner The owner of the Popup
     * @param contents The contents of the Popup
     * @param swoglContainer The SwoglContainer to which the 
     * SwoglComponent displaying the contents will be added
     */
    public SwoglPopup(Component owner, Component contents, int x, int y, 
                      SwoglContainer swoglContainer)
    {
        this.x = x;
        this.y = y;
        this.owner = owner;
        this.contents = contents;
        this.swoglContainer = swoglContainer;
    }
    
    /**
     * Will show this SwoglPopup. A SwoglComponent will be created,
     * containing the contents of this Popup. This SwoglComponent
     * will be added to the SwoglContainer
     */
    @Override
    public void show()
    {
        //System.out.println("Showing "+this);
        //System.out.println("owner    "+owner);
        //System.out.println("contents "+contents);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                // Find the SwoglComponent that contains the owner of the popup
                SwoglComponent parent = swoglContainer.findSwoglComponentOwning(owner);
                BoundingBox boundingBox = parent.getUntransformedBoundingBox();
                
                // Compute the bounds of the popup owner inside the SwoglComponent
                Rectangle ownerBounds = 
                    SwingUtilities.convertRectangle(owner.getParent(), owner.getBounds(), 
                        parent.getContainerPanel());

                // Compute the location of the popup inside the owner. (Note that
                // the coordinates (x,y) that are given in 
                // PopupFactory#getPopup(Component owner, Component contents, int x, int y)
                // and which are passed here in the constructor are SCREEN coordinates)
                Point locationInOwner = new Point(x,y);
                SwingUtilities.convertPointFromScreen(locationInOwner, owner);

                // Create the SwoglComponent that will display the popup contents
                Dimension contentSize = contents.getPreferredSize();
                swoglPopupComponent = new SwoglComponent(contentSize.width, contentSize.height, parent);
                swoglPopupComponent.setLayout(null);
                swoglPopupComponent.getContainerPanel().setPreferredSize(contentSize);
                swoglPopupComponent.add(contents);
                contents.setBounds(new Rectangle(0,0,contentSize.width,contentSize.height));

                // Compute the translation of the popup-SwoglComponent
                float dx = 
                	- boundingBox.getSizeX() / 2.0f +    // Upper left corner of owner       
                	ownerBounds.x + locationInOwner.x +  // Location referring to owner          
                	contentSize.width / 2.0f;            // Center of content

                float dy = 
                	- boundingBox.getSizeY() / 2.0f +    // Upper left corner of owner
                	ownerBounds.y + locationInOwner.y +  // Location referring to owner
                	contentSize.height / 2.0f;           // Center of content

                /*/
                System.out.println("Point in owner: "+pointInOwner);
                System.out.println("ownerBounds "+ownerBounds);
                System.out.println("x "+x);
                System.out.println("y "+y);

                System.out.println("dx "+dx);
                System.out.println("dy "+dy);
                //*/
                
                // Add the popup-SwoglComponent to the SwoglContainer
                swoglPopupComponent.setTransform(MatrixUtils.translate(dx, -dy, boundingBox.getMaxZ()+1));
                swoglContainer.add(swoglPopupComponent);
            }
        });
    }
    
    /**
     * Will hide this SwoglPopup, effectively by removing the
     * corresponding SwoglComponent from the SwoglContainer
     */
    @Override
    public void hide()
    {
        //System.out.println("Hiding");
        //System.out.println("owner    "+owner);
        //System.out.println("contents "+contents);
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                swoglContainer.remove(swoglPopupComponent);
            }
        });
    }
}
