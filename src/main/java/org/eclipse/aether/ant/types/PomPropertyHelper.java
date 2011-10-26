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

/**
 */
@SuppressWarnings( "deprecation" )
class PomPropertyHelper
    extends PropertyHelper
{

    private final ModelValueExtractor extractor;

    public static void register( ModelValueExtractor extractor, PropertyHelper propertyHelper )
    {
        PomPropertyHelper helper = new PomPropertyHelper( extractor );
        helper.setNext( propertyHelper.getNext() );
        propertyHelper.setNext( helper );
    }

    public PomPropertyHelper( ModelValueExtractor extractor )
    {
        if ( extractor == null )
        {
            throw new IllegalArgumentException( "no model value exractor specified" );
        }
        this.extractor = extractor;
        setProject( extractor.getProject() );
    }

    @Override
    public Object getPropertyHook( String ns, String name, boolean user )
    {
        Object value = extractor.getValue( name );
        if ( value != null )
        {
            return value;
        }
        else if ( extractor.isApplicable( name ) )
        {
            return null;
        }
        return super.getPropertyHook( ns, name, user );
    }

}
