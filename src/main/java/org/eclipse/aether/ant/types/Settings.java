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

import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;
import org.eclipse.aether.ant.AntRepoSys;

/**
 */
public class Settings
    extends DataType
{

    private File file;

    private File globalFile;

    protected Settings getRef()
    {
        return (Settings) getCheckedRef();
    }

    public void setRefid( Reference ref )
    {
        if ( file != null || globalFile != null )
        {
            throw tooManyAttributes();
        }
        super.setRefid( ref );
    }

    public File getFile()
    {
        if ( isReference() )
        {
            return getRef().getFile();
        }
        return file;
    }

    public void setFile( File file )
    {
        checkAttributesAllowed();
        this.file = file;

        AntRepoSys.getInstance( getProject() ).setUserSettings( file );
    }

    public File getGlobalFile()
    {
        if ( isReference() )
        {
            return getRef().getFile();
        }
        return globalFile;
    }

    public void setGlobalFile( File globalFile )
    {
        checkAttributesAllowed();
        this.globalFile = globalFile;

        AntRepoSys.getInstance( getProject() ).setGlobalSettings( globalFile );
    }

}
