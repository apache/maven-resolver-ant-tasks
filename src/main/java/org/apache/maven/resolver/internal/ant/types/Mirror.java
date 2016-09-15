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

import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 */
public class Mirror
    extends DataType
{

    private String id;

    private String url;

    private String type;

    private String mirrorOf;

    private Authentication authentication;

    @Override
    public void setProject( Project project )
    {
        super.setProject( project );

        AntRepoSys.getInstance( project ).addMirror( this );
    }

    protected Mirror getRef()
    {
        return (Mirror) getCheckedRef();
    }

    public void setRefid( Reference ref )
    {
        if ( id != null || url != null || mirrorOf != null || type != null )
        {
            throw tooManyAttributes();
        }
        super.setRefid( ref );
    }

    public String getId()
    {
        if ( isReference() )
        {
            return getRef().getId();
        }
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getUrl()
    {
        if ( isReference() )
        {
            return getRef().getUrl();
        }
        return url;
    }

    public void setUrl( String url )
    {
        checkAttributesAllowed();
        this.url = url;
    }

    public String getType()
    {
        if ( isReference() )
        {
            return getRef().getType();
        }
        return ( type != null ) ? type : "default";
    }

    public void setType( String type )
    {
        checkAttributesAllowed();
        this.type = type;
    }

    public String getMirrorOf()
    {
        if ( isReference() )
        {
            return getRef().getMirrorOf();
        }
        return mirrorOf;
    }

    public void setMirrorOf( String mirrorOf )
    {
        checkAttributesAllowed();
        this.mirrorOf = mirrorOf;
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

    public Authentication getAuthentication()
    {
        if ( isReference() )
        {
            getRef().getAuthentication();
        }
        return authentication;
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
