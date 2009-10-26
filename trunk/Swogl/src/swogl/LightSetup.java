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

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;

/**
 * This class encapsulates the Lights of one SwoglContainer
 */
class LightSetup
{
    /**
     * The maximum number of Lights that are supported
     */
    static final int MAX_LIGHTS = 8;
    
    /**
     * The Lights of this LightSetup
     */
    private List<DefaultLight> lights = new ArrayList<DefaultLight>();
    
    /**
     * Creates and returns a new Light. If this LightSetup already
     * contains the maximum number of lights, then no Light is
     * created and 'null' is returned.
     * 
     * @return The light that was created.
     */
    public Light createLight()
    {
        int numLights = lights.size();
        if (numLights >= MAX_LIGHTS)
        {
            return null;
        }
        DefaultLight light = new DefaultLight();
        lights.add(light);
        return light;
    }
    
    /**
     * Returns the number of Lights in this LightSetup
     * 
     * @return The number of Lights in this LightSetup 
     */
    public int getNumLights()
    {
        return lights.size();
    }

    /**
     * Removes the Light with the given Index from this LightSetup.
     * 
     * @param index The index of the Light to remove.
     */
    public void removeLight(int index)
    {
        lights.remove(index);
    }

    /**
     * Removes all lights from this LightSetup
     */
    public void clear()
    {
        lights.clear();
    }
    
    
    /**
     * Applies this LightSetup to the given GL context
     * 
     * @param gl The GL context to apply this LightSetup to.
     */
    void apply(GL gl)
    {
        for (int i=0; i<MAX_LIGHTS; i++)
        {
            int constant = computeGlLightIndexConstant(i);
            gl.glDisable(constant);
        }
        for (int i=0; i<lights.size(); i++)
        {
            DefaultLight light = lights.get(i);
            int constant = computeGlLightIndexConstant(i);
            light.setGlLightIndexConstant(constant);
            gl.glEnable(constant);

            //System.out.println("enable "+i);
        }
        /*
        System.out.println("Light status on "+gl);
        for (int i=0; i<8; i++)
        {
            System.out.println(i+": "+gl.glIsEnabled(computeGlLightIndexConstant(i)));
        }
        */
        for (DefaultLight light : lights)
        {
            light.apply(gl);
        }
    }
    
    /**
     * Returns the GL constant (GL_LIGHTn) for the light with the given
     * number.
     * 
     * @param number The number, may be 0..7
     * @return The GL constant for the light
     */
    private static int computeGlLightIndexConstant(int number)
    {
        switch (number)
        {
            case 0: return GL.GL_LIGHT0;
            case 1: return GL.GL_LIGHT1;
            case 2: return GL.GL_LIGHT2;
            case 3: return GL.GL_LIGHT3;
            case 4: return GL.GL_LIGHT4;
            case 5: return GL.GL_LIGHT5;
            case 6: return GL.GL_LIGHT6;
            case 7: return GL.GL_LIGHT7;
        }
        return -1;
    }
    
}
