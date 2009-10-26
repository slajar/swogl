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

/**
 * This interface describes a Light that was added to a SwoglContainer
 * using {@link SwoglContainer#createLight()}.<br />
 * <br />
 * <b>NOTE</b>: This interface should be considered as being PRELIMINARY and 
 * may change in future releases.
 */
public interface Light
{
    /**
     * Set the ambient color of this Light. 
     * Default value: (0, 0, 0)
     * 
     * @param r The red component
     * @param g The green component
     * @param b The blue component
     */
    void setAmbientColor(float r, float g, float b);

    /**
     * Set the diffuse color of this Light.
     * Default value: (1, 1, 1)
     * 
     * @param r The red component
     * @param g The green component
     * @param b The blue component
     */
    void setDiffuseColor(float r, float g, float b);

    /**
     * Set the specular color of this Light.
     * Default value: (1, 1, 1)
     * 
     * @param r The red component
     * @param g The green component
     * @param b The blue component
     */
    void setSpecularColor(float r, float g, float b);
    
    /**
     * Set the position of this Light. 
     * Default value: (0, 0, 1)
     * 
     * @param x The x position of this light
     * @param y The y position of this light
     * @param z The z position of this light
     */
    void setPosition(float x, float y, float z);
    
    /**
     * Set the direction of this Light.
     * Default value: (0,0,-1)
     * 
     * @param x The x component of the direction
     * @param y The y component of the direction
     * @param z The z component of the direction
     */
    void setDirection(float x, float y, float z);
    
    /**
     * Set whether this is a directional Light or 
     * a point Light. By default, a Light is a 
     * point light.
     * 
     * @param directional Whether this Light is directional
     */
    void setDirectional(boolean directional);
    
    /**
     * Set the constant, linear and quadratic attenuation of this Light.
     * Default value: (1, 0, 0)
     * 
     * @param constant The constant attenuation
     * @param linear The linear attenuation
     * @param quadratic The quadratic attenuation
     */
    void setAttenuation(float constant, float linear, float quadratic);

    /**
     * Set the cutoff angle of this Light, in degrees. If the given 
     * cutoff angle is not in the range of [0...90] degrees, then 
     * this Light is considered to be no spot light. 
     * Default value: 180 (no spot light)
     * 
     * @param spotCutoff The cutoff angle of this Light, in degrees.
     */
    void setSpotCutoff(float spotCutoff);
    
    /**
     * Set the spot light attenuation exponent. The light is 
     * attenuated from the center to the edge of the spot cone,
     * where the intensity of the light is computed as<br />
     * intensity = cos(angleToDirection)^spotExponent<br />
     * Thus higher exponents result in more focused lights.
     * 
     * @param spotExponent The spot attenuation exponent.
     */
    void setSpotExponent(float spotExponent);
}
