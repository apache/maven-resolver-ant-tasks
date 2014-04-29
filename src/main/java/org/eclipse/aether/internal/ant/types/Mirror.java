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
