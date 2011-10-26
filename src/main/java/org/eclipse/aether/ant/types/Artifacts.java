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
import java.util.List;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 */
public class Artifacts
    extends DataType
    implements ArtifactContainer
{

    private List<ArtifactContainer> containers = new ArrayList<ArtifactContainer>();

    protected Artifacts getRef()
    {
        return (Artifacts) getCheckedRef();
    }

    public void validate( Task task )
    {
        if ( isReference() )
        {
            getRef().validate( task );
        }
        else
        {
            for ( ArtifactContainer container : containers )
            {
                container.validate( task );
            }
        }
    }

    public void setRefid( Reference ref )
    {
        if ( !containers.isEmpty() )
        {
            throw noChildrenAllowed();
        }
        super.setRefid( ref );
    }

    public void addArtifact( Artifact artifact )
    {
        checkChildrenAllowed();
        containers.add( artifact );
    }

    public void addArtifacts( Artifacts artifacts )
    {
        checkChildrenAllowed();
        if ( artifacts == this )
        {
            throw circularReference();
        }
        containers.add( artifacts );
    }

    public List<Artifact> getArtifacts()
    {
        if ( isReference() )
        {
            return getRef().getArtifacts();
        }
        List<Artifact> artifacts = new ArrayList<Artifact>();
        for ( ArtifactContainer container : containers )
        {
            artifacts.addAll( container.getArtifacts() );
        }
        return artifacts;
    }

}
