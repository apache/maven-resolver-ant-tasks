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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 */
public class RemoteRepositories
    extends DataType
    implements RemoteRepositoryContainer
{

    private List<RemoteRepositoryContainer> containers = new ArrayList<RemoteRepositoryContainer>();

    protected RemoteRepositories getRef()
    {
        return (RemoteRepositories) getCheckedRef();
    }

    public void validate( Task task )
    {
        if ( isReference() )
        {
            getRef().validate( task );
        }
        else
        {
            for ( RemoteRepositoryContainer container : containers )
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

    public void addRemoterepo( RemoteRepository repository )
    {
        checkChildrenAllowed();
        containers.add( repository );
    }

    public void addRemoterepos( RemoteRepositories repositories )
    {
        checkChildrenAllowed();
        if ( repositories == this )
        {
            throw circularReference();
        }
        containers.add( repositories );
    }

    public List<RemoteRepository> getRepositories()
    {
        if ( isReference() )
        {
            return getRef().getRepositories();
        }
        List<RemoteRepository> repos = new ArrayList<RemoteRepository>();
        for ( RemoteRepositoryContainer container : containers )
        {
            repos.addAll( container.getRepositories() );
        }
        return repos;
    }

}
