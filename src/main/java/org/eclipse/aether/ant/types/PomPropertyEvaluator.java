/*******************************************************************************
 * Copyright (c) 2010, 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.aether.ant.types;

import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.PropertyHelper.PropertyEvaluator;
import org.apache.tools.ant.property.NullReturn;

/**
 */
class PomPropertyEvaluator
    implements PropertyEvaluator
{

    private final ModelValueExtractor extractor;

    public static void register( ModelValueExtractor extractor, PropertyHelper propertyHelper )
    {
        propertyHelper.add( new PomPropertyEvaluator( extractor ) );
    }

    private PomPropertyEvaluator( ModelValueExtractor extractor )
    {
        if ( extractor == null )
        {
            throw new IllegalArgumentException( "no model value exractor specified" );
        }
        this.extractor = extractor;
    }

    public Object evaluate( String property, PropertyHelper propertyHelper )
    {
        Object value = extractor.getValue( property );
        if ( value != null )
        {
            return value;
        }
        else if ( extractor.isApplicable( property ) )
        {
            return NullReturn.NULL;
        }
        return null;
    }

}
