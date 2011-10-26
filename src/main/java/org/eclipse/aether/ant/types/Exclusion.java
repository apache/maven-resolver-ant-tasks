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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 */
public class Exclusion
    extends DataType
{

    private static final String WILDCARD = "*";

    private String groupId;

    private String artifactId;

    private String classifier;

    private String extension;

    protected Exclusion getRef()
    {
        return (Exclusion) getCheckedRef();
    }

    public void validate( Task task )
    {
        if ( isReference() )
        {
            getRef().validate( task );
        }
        else
        {
            if ( groupId == null && artifactId == null && classifier == null && extension == null )
            {
                throw new BuildException( "You must specify at least one of "
                    + "'groupId', 'artifactId', 'classifier' or 'extension'" );
            }
        }
    }

    public void setRefid( Reference ref )
    {
        if ( groupId != null || artifactId != null || extension != null || classifier != null )
        {
            throw tooManyAttributes();
        }
        super.setRefid( ref );
    }

    public String getGroupId()
    {
        if ( isReference() )
        {
            return getRef().getGroupId();
        }
        return ( groupId != null ) ? groupId : WILDCARD;
    }

    public void setGroupId( String groupId )
    {
        checkAttributesAllowed();
        if ( this.groupId != null )
        {
            throw ambiguousCoords();
        }
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        if ( isReference() )
        {
            return getRef().getArtifactId();
        }
        return ( artifactId != null ) ? artifactId : WILDCARD;
    }

    public void setArtifactId( String artifactId )
    {
        checkAttributesAllowed();
        if ( this.artifactId != null )
        {
            throw ambiguousCoords();
        }
        this.artifactId = artifactId;
    }

    public String getClassifier()
    {
        if ( isReference() )
        {
            return getRef().getClassifier();
        }
        return ( classifier != null ) ? classifier : WILDCARD;
    }

    public void setClassifier( String classifier )
    {
        checkAttributesAllowed();
        if ( this.classifier != null )
        {
            throw ambiguousCoords();
        }
        this.classifier = classifier;
    }

    public String getExtension()
    {
        if ( isReference() )
        {
            return getRef().getExtension();
        }
        return ( extension != null ) ? extension : WILDCARD;
    }

    public void setExtension( String extension )
    {
        checkAttributesAllowed();
        if ( this.extension != null )
        {
            throw ambiguousCoords();
        }
        this.extension = extension;
    }

    public void setCoords( String coords )
    {
        checkAttributesAllowed();
        if ( groupId != null || artifactId != null || extension != null || classifier != null )
        {
            throw ambiguousCoords();
        }
        Pattern p = Pattern.compile( "([^: ]+)(:([^: ]+)(:([^: ]+)(:([^: ]*))?)?)?" );
        Matcher m = p.matcher( coords );
        if ( !m.matches() )
        {
            throw new BuildException( "Bad exclusion coordinates"
                + ", expected format is <groupId>[:<artifactId>[:<extension>[:<classifier>]]]" );
        }
        groupId = m.group( 1 );
        artifactId = m.group( 3 );
        if ( artifactId == null )
        {
            artifactId = "*";
        }
        extension = m.group( 5 );
        if ( extension == null )
        {
            extension = "*";
        }
        classifier = m.group( 7 );
        if ( classifier == null )
        {
            classifier = "*";
        }
    }

    private BuildException ambiguousCoords()
    {
        return new BuildException( "You must not specify both 'coords' and "
            + "('groupId', 'artifactId', 'extension', 'classifier')" );
    }

}
