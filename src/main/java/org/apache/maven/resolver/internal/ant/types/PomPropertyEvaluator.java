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
