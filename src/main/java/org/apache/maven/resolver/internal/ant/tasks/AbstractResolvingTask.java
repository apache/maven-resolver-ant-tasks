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
import org.apache.maven.resolver.internal.ant.types.Dependencies;
import org.apache.maven.resolver.internal.ant.types.LocalRepository;
import org.apache.maven.resolver.internal.ant.types.RemoteRepositories;
import org.apache.maven.resolver.internal.ant.types.RemoteRepository;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;
import org.eclipse.aether.collection.CollectResult;

/**
 */
public abstract class AbstractResolvingTask
    extends Task
{

    protected Dependencies dependencies;

    protected RemoteRepositories remoteRepositories;

    protected LocalRepository localRepository;

    public void addDependencies( Dependencies dependencies )
    {
        if ( this.dependencies != null )
        {
            throw new BuildException( "You must not specify multiple <dependencies> elements" );
        }
        this.dependencies = dependencies;
    }

    public void setDependenciesRef( Reference ref )
    {
        if ( dependencies == null )
        {
            dependencies = new Dependencies();
            dependencies.setProject( getProject() );
        }
        dependencies.setRefid( ref );
    }

    public LocalRepository createLocalRepo()
    {
        if ( localRepository != null )
        {
            throw new BuildException( "You must not specify multiple <localRepo> elements" );
        }
        localRepository = new LocalRepository( this );
        return localRepository;
    }

    private RemoteRepositories getRemoteRepos()
    {
        if ( remoteRepositories == null )
        {
            remoteRepositories = new RemoteRepositories();
            remoteRepositories.setProject( getProject() );
        }
        return remoteRepositories;
    }

    public void addRemoteRepo( RemoteRepository repository )
    {
        getRemoteRepos().addRemoterepo( repository );
    }

    public void addRemoteRepos( RemoteRepositories repositories )
    {
        getRemoteRepos().addRemoterepos( repositories );
    }

    public void setRemoteReposRef( Reference ref )
    {
        RemoteRepositories repos = new RemoteRepositories();
        repos.setProject( getProject() );
        repos.setRefid( ref );
        getRemoteRepos().addRemoterepos( repos );
    }

    protected CollectResult collectDependencies()
    {
        return AntRepoSys.getInstance( getProject() ).collectDependencies( this, dependencies, localRepository,
                                                                           remoteRepositories );
    }

}
