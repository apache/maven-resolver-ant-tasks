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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 */
public class Dependencies
    extends DataType
{

    private Pom pom;

    private List<Dependency> dependencies = new ArrayList<Dependency>();

    private List<Exclusion> exclusions = new ArrayList<Exclusion>();

    protected Dependencies getRef()
    {
        return (Dependencies) getCheckedRef();
    }

    public void validate( Task task )
    {
        if ( isReference() )
        {
            getRef().validate( task );
        }
        else
        {
            if ( getPom() != null && getPom().getFile() == null )
            {
                throw new BuildException( "A <pom> used for dependency resolution has to be backed by a pom.xml file" );
            }
            Map<String, String> ids = new HashMap<String, String>();
            for ( Dependency dependency : dependencies )
            {
                dependency.validate( task );
                String id = dependency.getVersionlessKey();
                String collision = ids.put( id, dependency.getVersion() );
                if ( collision != null )
                {
                    throw new BuildException( "You must not declare multiple <dependency> elements"
                        + " with the same coordinates but got " + id + " -> " + collision + " vs "
                        + dependency.getVersion() );
                }
            }
        }
    }

    public void setRefid( Reference ref )
    {
        if ( pom != null || !exclusions.isEmpty() || !dependencies.isEmpty() )
        {
            throw noChildrenAllowed();
        }
        super.setRefid( ref );
    }

    public void addPom( Pom pom )
    {
        checkChildrenAllowed();
        if ( this.pom != null )
        {
            throw new BuildException( "You must not specify multiple <pom> elements" );
        }
        this.pom = pom;
    }

    public Pom getPom()
    {
        if ( isReference() )
        {
            return getRef().getPom();
        }
        return pom;
    }

    public void setPomRef( Reference ref )
    {
        if ( pom == null )
        {
            pom = new Pom();
            pom.setProject( getProject() );
        }
        pom.setRefid( ref );
    }

    public void addDependency( Dependency dependency )
    {
        checkChildrenAllowed();
        this.dependencies.add( dependency );
    }

    public List<Dependency> getDependencies()
    {
        if ( isReference() )
        {
            return getRef().getDependencies();
        }
        return dependencies;
    }

    public void addExclusion( Exclusion exclusion )
    {
        checkChildrenAllowed();
        this.exclusions.add( exclusion );
    }

    public List<Exclusion> getExclusions()
    {
        if ( isReference() )
        {
            return getRef().getExclusions();
        }
        return exclusions;
    }

}
