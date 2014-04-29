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
package org.eclipse.aether.internal.ant.types;

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;
import org.eclipse.aether.internal.ant.AntRepoSys;

/**
 */
public class Authentication
    extends DataType
{

    private String username;

    private String password;

    private String privateKeyFile;

    private String passphrase;

    private List<String> servers = new ArrayList<String>();

    @Override
    public void setProject( Project project )
    {
        super.setProject( project );

        AntRepoSys.getInstance( project ).addAuthentication( this );
    }

    protected Authentication getRef()
    {
        return (Authentication) getCheckedRef();
    }

    public void setRefid( Reference ref )
    {
        if ( username != null || password != null || privateKeyFile != null || passphrase != null )
        {
            throw tooManyAttributes();
        }
        super.setRefid( ref );
    }

    public String getUsername()
    {
        if ( isReference() )
        {
            return getRef().getUsername();
        }
        return username;
    }

    public void setUsername( String username )
    {
        checkAttributesAllowed();
        this.username = username;
    }

    public String getPassword()
    {
        if ( isReference() )
        {
            return getRef().getPassword();
        }
        return password;
    }

    public void setPassword( String password )
    {
        checkAttributesAllowed();
        this.password = password;
    }

    public String getPrivateKeyFile()
    {
        if ( isReference() )
        {
            return getRef().getPrivateKeyFile();
        }
        return privateKeyFile;
    }

    public void setPrivateKeyFile( String privateKeyFile )
    {
        checkAttributesAllowed();
        this.privateKeyFile = privateKeyFile;
    }

    public String getPassphrase()
    {
        if ( isReference() )
        {
            return getRef().getPassphrase();
        }
        return passphrase;
    }

    public void setPassphrase( String passphrase )
    {
        checkAttributesAllowed();
        this.passphrase = passphrase;
    }

    public List<String> getServers()
    {
        if ( isReference() )
        {
            return getRef().getServers();
        }
        return servers;
    }

    public void setServers( String servers )
    {
        checkAttributesAllowed();
        this.servers.clear();
        String[] split = servers.split( "[;:]" );
        for ( String server : split )
        {
            server = server.trim();
            if ( server.length() > 0 )
            {
                this.servers.add( server );
            }
        }
    }

}
