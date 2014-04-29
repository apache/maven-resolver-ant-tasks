/*******************************************************************************
 * Copyright (c) 2010, 2014 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.aether.internal.ant;

import java.io.FileNotFoundException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.transfer.MetadataNotFoundException;

/**
 * Logs repository events like installed and unresolved artifacts and metadata.
 */
class AntRepositoryListener
    extends AbstractRepositoryListener
{

    private Task task;

    public AntRepositoryListener( Task task )
    {
        this.task = task;
    }

    @Override
    public void artifactInstalling( RepositoryEvent event )
    {
        task.log( "Installing " + event.getArtifact().getFile() + " to " + event.getFile() );
    }

    @Override
    public void metadataInstalling( RepositoryEvent event )
    {
        task.log( "Installing " + event.getMetadata() + " to " + event.getFile() );
    }

    @Override
    public void metadataResolved( RepositoryEvent event )
    {
        Exception e = event.getException();
        if ( e != null )
        {
            if ( e instanceof MetadataNotFoundException )
            {
                task.log( e.getMessage(), Project.MSG_DEBUG );
            }
            else
            {
                task.log( e.getMessage(), e, Project.MSG_WARN );
            }
        }
    }

    @Override
    public void metadataInvalid( RepositoryEvent event )
    {
        Exception exception = event.getException();

        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( "The metadata " );
        if ( event.getMetadata().getFile() != null )
        {
            buffer.append( event.getMetadata().getFile() );
        }
        else
        {
            buffer.append( event.getMetadata() );
        }

        if ( exception instanceof FileNotFoundException )
        {
            buffer.append( " is inaccessible" );
        }
        else
        {
            buffer.append( " is invalid" );
        }

        if ( exception != null )
        {
            buffer.append( ": " );
            buffer.append( exception.getMessage() );
        }

        task.log( buffer.toString(), exception, Project.MSG_WARN );
    }

    @Override
    public void artifactDescriptorInvalid( RepositoryEvent event )
    {
        task.log( "The POM for " + event.getArtifact() + " is invalid"
                      + ", transitive dependencies (if any) will not be available: "
                      + event.getException().getMessage(),
                  event.getException(), Project.MSG_WARN );
    };

    @Override
    public void artifactDescriptorMissing( RepositoryEvent event )
    {
        task.log( "The POM for " + event.getArtifact() + " is missing, no dependency information available",
                  Project.MSG_WARN );
    };

}
