package org.apache.maven.resolver.internal.ant;

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

    AntRepositoryListener( Task task )
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
