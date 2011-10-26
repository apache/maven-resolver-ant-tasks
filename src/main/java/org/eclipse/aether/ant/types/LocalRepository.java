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

import java.io.File;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;
import org.eclipse.aether.ant.AntRepoSys;

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
