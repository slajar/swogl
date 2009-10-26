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

import javax.media.opengl.GL;

/**
 * Default implementation of a Light.
 */
class DefaultLight implements Light
{
    /**
     * The ambient color of this Light
     */
    private float ambient[] = new float[]{ 0.0f, 0.0f, 0.0f, 1.0f };

    /**
     * The diffuse color of this Light
     */
    private float diffuse[] = new float[]{ 1.0f, 1.0f, 1.0f, 1.0f };

    /**
     * The specular color of this Light
     */
    private float specular[] = new float[]{ 1.0f, 1.0f, 1.0f, 1.0f };

    /**
     * The position of this Light
     */
    private float position[] = new float[]{ 0.0f, 0.0f, 1.0f, 1.0f };
    
    /**
     * The direction of this Light
     */
    private float direction[] = new float[]{ 0.0f, 0.0f, -1.0f, 0.0f };
    
    /**
     * Whether this light is directional
     */
    private boolean directional = true;
    
    /**
     * The constant attenuation
     */
    private float constantAttenuation = 1.0f;

    /**
     * The linear attenuation
     */
    private float linearAttenuation = 0.0f;

    /**
     * The quadratic attenuation
     */
    private float quadraticAttenuation = 0.0f;
    
    /**
     * The spot cutoff angle, in degrees
     */
    private float spotCutoff = 180.0f;
    
    /**
     * The spot attenuation exponent
     */
    private float spotExponent = 0.0f;
    
    /**
     * The GL_LIGHTn constant for this Light
     */
    private int glLightIndexConstant = -1;
    
    /**
     * Creates a new DefaultLight 
     */
    DefaultLight()
    {
        glLightIndexConstant = -1;
    }
    
    /**
     * Set the GL_LIGHTn constant for this Light 
     * 
     * @param constant The GL_LIGHTn constant for this Light
     */
    void setGlLightIndexConstant(int constant)
    {
        this.glLightIndexConstant = constant;
    }
    
    /**
     * Applies this Light to the given GL context
     * 
     * @param gl The GL context to apply this Light to
     */
    void apply(GL gl)
    {
        if (glLightIndexConstant == -1)
        {
            return;
        }
        
        //System.out.println("\nApply light");
        //printInfo();
        
        gl.glLightfv(glLightIndexConstant, GL.GL_AMBIENT, ambient, 0);
        gl.glLightfv(glLightIndexConstant, GL.GL_DIFFUSE, diffuse, 0);
        gl.glLightfv(glLightIndexConstant, GL.GL_SPECULAR, specular, 0);
        
        // When the w-component of the position is 0, then the "position" 
        // is actually the direction of a directional light. In contrast, 
        // the flag whether this Light is directional may be set explicitly.
        // When the light is directional and NO spot light, then the 
        // direction of this light will be passed to OpenGL which will 
        // interpret it as a direction due to the w-component of the 
        // direction being 0.0. If this Light is NOT directional or is 
        // a spot light, then the position will be passed to OpenGL, 
        // which will interpret it as a position (sic) due to the 
        // w-component of the position being 1.0.
        if (directional && spotCutoff == 180)
        {
            gl.glLightfv(glLightIndexConstant, GL.GL_POSITION, direction, 0);
        }
        else
        {
            gl.glLightfv(glLightIndexConstant, GL.GL_POSITION, position, 0);
        }
        gl.glLightf(glLightIndexConstant, GL.GL_SPOT_CUTOFF, spotCutoff);
        gl.glLightfv(glLightIndexConstant, GL.GL_SPOT_DIRECTION, direction, 0);
        gl.glLightf(glLightIndexConstant, GL.GL_SPOT_EXPONENT, spotExponent);
        
        gl.glLightf(glLightIndexConstant, GL.GL_CONSTANT_ATTENUATION, constantAttenuation);
        gl.glLightf(glLightIndexConstant, GL.GL_LINEAR_ATTENUATION, linearAttenuation);
        gl.glLightf(glLightIndexConstant, GL.GL_QUADRATIC_ATTENUATION, quadraticAttenuation);
    }

    /**
     * {@inheritDoc}
     */
    public void setAmbientColor(float r, float g, float b)
    {
        ambient[0] = r;
        ambient[1] = g;
        ambient[2] = b;
        ambient[3] = 1.0f;
    }

    /**
     * {@inheritDoc}
     */
    public void setDiffuseColor(float r, float g, float b)
    {
        diffuse[0] = r;
        diffuse[1] = g;
        diffuse[2] = b;
        diffuse[3] = 1.0f;
    }

    /**
     * {@inheritDoc}
     */
    public void setSpecularColor(float r, float g, float b)
    {
        specular[0] = r;
        specular[1] = g;
        specular[2] = b;
        specular[3] = 1.0f;
    }
    

    /**
     * {@inheritDoc}
     */
    public void setPosition(float x, float y, float z)
    {
        position[0] = x;
        position[1] = y;
        position[2] = z;
        position[3] = 1.0f;
    }

    /**
     * {@inheritDoc}
     */
    public void setDirection(float x, float y, float z)
    {
        direction[0] = x;
        direction[1] = y;
        direction[2] = z;
        direction[3] = 0.0f;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setDirectional(boolean directional)
    {
        this.directional = directional;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setAttenuation(float constant, float linear, float quadratic)
    {
        constantAttenuation = constant;
        linearAttenuation = linear;
        quadraticAttenuation = quadratic;
    }
    
    /**
     * {@inheritDoc}
     */
    public void setSpotCutoff(float cutoff)
    {
        if (cutoff < 0 || cutoff > 90)
        {
            cutoff = 180;
        }
        this.spotCutoff = cutoff;
    }

    /**
     * {@inheritDoc}
     */
    public void setSpotExponent(float spotExponent)
    {
        this.spotExponent = spotExponent;
    }
    
    
    /* 
     * Print some debugging information
     */
    void printInfo()
    {
        System.out.println("ambient "+java.util.Arrays.toString(ambient));
        System.out.println("diffuse "+java.util.Arrays.toString(diffuse));
        System.out.println("specular "+java.util.Arrays.toString(specular));
        System.out.println("position "+java.util.Arrays.toString(position));
        System.out.println("direction "+java.util.Arrays.toString(direction));
        System.out.println("directional "+directional);
        System.out.println("attenuation "+constantAttenuation+" "+linearAttenuation+" "+quadraticAttenuation);
        System.out.println("spotCutoff "+spotCutoff);
        System.out.println("spotExponent "+spotExponent);
        switch (glLightIndexConstant)
        {
            case GL.GL_LIGHT0: System.out.println("glLightIndexConstant GL_LIGHT0"); break;
            case GL.GL_LIGHT1: System.out.println("glLightIndexConstant GL_LIGHT1"); break;
            case GL.GL_LIGHT2: System.out.println("glLightIndexConstant GL_LIGHT2"); break;
            case GL.GL_LIGHT3: System.out.println("glLightIndexConstant GL_LIGHT3"); break;
            case GL.GL_LIGHT4: System.out.println("glLightIndexConstant GL_LIGHT4"); break;
            case GL.GL_LIGHT5: System.out.println("glLightIndexConstant GL_LIGHT5"); break;
            case GL.GL_LIGHT6: System.out.println("glLightIndexConstant GL_LIGHT6"); break;
            case GL.GL_LIGHT7: System.out.println("glLightIndexConstant GL_LIGHT7"); break;
        }
    }
    
    
}
