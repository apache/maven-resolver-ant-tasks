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

import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 */
public class LocalRepository
    extends DataType
{

    private final Task task;

    private File dir;

    public LocalRepository()
    {
        this( null );
    }

    public LocalRepository( Task task )
    {
        this.task = task;
    }

    @Override
    public void setProject( Project project )
    {
        super.setProject( project );

        if ( task == null )
        {
            AntRepoSys.getInstance( project ).setLocalRepository( this );
        }
    }

    protected LocalRepository getRef()
    {
        return (LocalRepository) getCheckedRef();
    }

    public void setRefid( Reference ref )
    {
        if ( dir != null )
        {
            throw tooManyAttributes();
        }
        super.setRefid( ref );
    }

    public File getDir()
    {
        if ( isReference() )
        {
            return getRef().getDir();
        }
        return dir;
    }

    public void setDir( File dir )
    {
        checkAttributesAllowed();
        this.dir = dir;
    }

}
