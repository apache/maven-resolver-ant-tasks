package org.apache.maven.resolver.internal.ant.tasks;

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

import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.maven.resolver.internal.ant.types.RemoteRepository;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Reference;

/**
 */
public class Deploy
    extends AbstractDistTask
{

    private RemoteRepository repository;

    private RemoteRepository snapshotRepository;

    @Override
    protected void validate()
    {
        super.validate();

        if ( repository == null )
        {
            throw new BuildException( "You must specify the <remoteRepo id=\"...\" url=\"...\"> element"
                + " to denote the target repository for the deployment" );
        }
        else
        {
            repository.validate( this );
        }
        if ( snapshotRepository != null )
        {
            snapshotRepository.validate( this );
        }
    }

    public void addRemoteRepo( RemoteRepository repository )
    {
        if ( this.repository != null )
        {
            throw new BuildException( "You must not specify multiple <remoteRepo> elements" );
        }
        this.repository = repository;
    }

    public void setRemoteRepoRef( Reference ref )
    {
        if ( repository == null )
        {
            repository = new RemoteRepository();
            repository.setProject( getProject() );
        }
        repository.setRefid( ref );
    }

    public void addSnapshotRepo( RemoteRepository snapshotRepository )
    {
        if ( this.snapshotRepository != null )
        {
            throw new BuildException( "You must not specify multiple <snapshotRepo> elements" );
        }
        this.snapshotRepository = snapshotRepository;
    }

    public void setSnapshotRepoRef( Reference ref )
    {
        if ( snapshotRepository == null )
        {
            snapshotRepository = new RemoteRepository();
            snapshotRepository.setProject( getProject() );
        }
        snapshotRepository.setRefid( ref );
    }

    @Override
    public void execute()
        throws BuildException
    {
        validate();

        AntRepoSys.getInstance( getProject() ).deploy( this, getPom(), getArtifacts(), repository, snapshotRepository );
    }

}
