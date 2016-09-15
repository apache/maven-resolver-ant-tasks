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

import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

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
