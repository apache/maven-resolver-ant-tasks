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

    public ModelValueExtractor( String prefix, Model model, Project project )
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
