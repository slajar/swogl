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

package swogl.geometry;

import javax.vecmath.TexCoord2f;

/**
 * Interface for all classes that may transform texture coordinates.
 * A TexCoordTransformer may, for example, be set for a 
 * DefaultGeometry, so that the texture coordinates of the 
 * DefaultGeometry will be transformed during an update.
 */
public interface TexCoordTransformer
{
    /**
     * Transforms the given texture coordinate. 
     * 
     * @param texCoord The texture coordinate that should be transformed. 
     */
    void transformTexCoord(TexCoord2f texCoord);

}
