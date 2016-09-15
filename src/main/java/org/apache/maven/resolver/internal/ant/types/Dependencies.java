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

import java.io.File;
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
    implements DependencyContainer
{

    private File file;

    private Pom pom;

    private List<DependencyContainer> containers = new ArrayList<DependencyContainer>();

    private List<Exclusion> exclusions = new ArrayList<Exclusion>();

    private boolean nestedDependencies;

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
            for ( DependencyContainer container : containers )
            {
                container.validate( task );
                if ( container instanceof Dependency )
                {
                    Dependency dependency = (Dependency) container;
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
    }

    public void setRefid( Reference ref )
    {
        if ( pom != null || !exclusions.isEmpty() || !containers.isEmpty() )
        {
            throw noChildrenAllowed();
        }
        super.setRefid( ref );
    }

    public void setFile( File file )
    {
        checkAttributesAllowed();
        this.file = file;
        checkExternalSources();
    }

    public File getFile()
    {
        if ( isReference() )
        {
            return getRef().getFile();
        }
        return file;
    }

    public void addPom( Pom pom )
    {
        checkChildrenAllowed();
        if ( this.pom != null )
        {
            throw new BuildException( "You must not specify multiple <pom> elements" );
        }
        this.pom = pom;
        checkExternalSources();
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
        checkExternalSources();
    }

    private void checkExternalSources()
    {
        if ( file != null && pom != null )
        {
            throw new BuildException( "You must not specify both a text file and a POM to list dependencies" );
        }
        if ( ( file != null || pom != null ) && nestedDependencies )
        {
            throw new BuildException( "You must not specify both a file/POM and nested dependency collections" );
        }
    }

    public void addDependency( Dependency dependency )
    {
        checkChildrenAllowed();
        containers.add( dependency );
    }

    public void addDependencies( Dependencies dependencies )
    {
        checkChildrenAllowed();
        if ( dependencies == this )
        {
            throw circularReference();
        }
        containers.add( dependencies );
        nestedDependencies = true;
        checkExternalSources();
    }

    public List<DependencyContainer> getDependencyContainers()
    {
        if ( isReference() )
        {
            return getRef().getDependencyContainers();
        }
        return containers;
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
