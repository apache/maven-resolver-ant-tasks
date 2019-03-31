package org.apache.maven.resolver.internal.ant.types;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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

    PomPropertyHelper( ModelValueExtractor extractor )
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
