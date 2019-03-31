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

import org.apache.maven.model.Model;
import org.apache.tools.ant.Project;
import org.codehaus.plexus.interpolation.reflection.ReflectionValueExtractor;

/**
 */
class ModelValueExtractor
{

    private static final String PREFIX_PROPERTIES = "properties.";

    private final String prefix;

    private final Project project;

    private final Model model;

    ModelValueExtractor( String prefix, Model model, Project project )
    {
        if ( model == null )
        {
            throw new IllegalArgumentException( "reference to Maven POM has not been specified" );
        }
        if ( project == null )
        {
            throw new IllegalArgumentException( "reference to Ant project has not been specified" );
        }
        if ( prefix == null || prefix.length() <= 0 )
        {
            prefix = "pom.";
        }
        else if ( !prefix.endsWith( "." ) )
        {
            prefix += '.';
        }
        this.prefix = prefix;
        this.model = model;
        this.project = project;
    }

    public Project getProject()
    {
        return project;
    }

    public boolean isApplicable( String expression )
    {
        return expression.startsWith( prefix );
    }

    public Object getValue( String expression )
    {
        if ( expression.startsWith( prefix ) )
        {
            String expr = expression.substring( prefix.length() );
            try
            {
                if ( expr.startsWith( PREFIX_PROPERTIES ) )
                {
                    String key = expr.substring( PREFIX_PROPERTIES.length() );
                    return model.getProperties().getProperty( key );
                }

                return ReflectionValueExtractor.evaluate( expr, model, false );
            }
            catch ( Exception e )
            {
                project.log( "Could not retrieve '" + expression + "' from POM: " + e.getMessage(), e, Project.MSG_WARN );
                return null;
            }
        }
        else
        {
            return null;
        }
    }

}
