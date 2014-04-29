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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;
import org.eclipse.aether.internal.ant.AntRepoSys;

/**
 */
public class Proxy
    extends DataType
{

    private String host;

    private int port;

    private String type;

    private String nonProxyHosts;

    private Authentication authentication;

    @Override
    public void setProject( Project project )
    {
        super.setProject( project );

        AntRepoSys.getInstance( project ).addProxy( this );
    }

    protected Proxy getRef()
    {
        return (Proxy) getCheckedRef();
    }

    public void setRefid( Reference ref )
    {
        if ( host != null || port != 0 || type != null || nonProxyHosts != null )
        {
            throw tooManyAttributes();
        }
        if ( authentication != null )
        {
            throw noChildrenAllowed();
        }
        super.setRefid( ref );
    }

    public String getHost()
    {
        if ( isReference() )
        {
            return getRef().getHost();
        }
        return host;
    }

    public void setHost( String host )
    {
        checkAttributesAllowed();
        this.host = host;
    }

    public int getPort()
    {
        if ( isReference() )
        {
            return getRef().getPort();
        }
        return port;
    }

    public void setPort( int port )
    {
        checkAttributesAllowed();
        if ( port <= 0 || port > 0xFFFF )
        {
            throw new BuildException( "The port number must be within the range 1 - 65535" );
        }
        this.port = port;
    }

    public String getType()
    {
        if ( isReference() )
        {
            return getRef().getType();
        }
        return type;
    }

    public void setType( String type )
    {
        checkAttributesAllowed();
        this.type = type;
    }

    public String getNonProxyHosts()
    {
        if ( isReference() )
        {
            return getRef().getNonProxyHosts();
        }
        return nonProxyHosts;
    }

    public void setNonProxyHosts( String nonProxyHosts )
    {
        checkAttributesAllowed();
        this.nonProxyHosts = nonProxyHosts;
    }

    public Authentication getAuthentication()
    {
        if ( isReference() )
        {
            return getRef().getAuthentication();
        }
        return authentication;
    }

    public void addAuthentication( Authentication authentication )
    {
        checkChildrenAllowed();
        if ( this.authentication != null )
        {
            throw new BuildException( "You must not specify multiple <authentication> elements" );
        }
        this.authentication = authentication;
    }

    public void setAuthRef( Reference ref )
    {
        if ( authentication == null )
        {
            authentication = new Authentication();
            authentication.setProject( getProject() );
        }
        authentication.setRefid( ref );
    }

}
