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

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;
import org.eclipse.aether.internal.ant.ProjectWorkspaceReader;
import org.eclipse.aether.internal.ant.tasks.RefTask;

/**
 */
public class Artifact
    extends RefTask
    implements ArtifactContainer
{

    private File file;

    private String type;

    private String classifier;

    private Pom pom;

    protected Artifact getRef()
    {
        return (Artifact) getCheckedRef();
    }

    public void validate( Task task )
    {
        if ( isReference() )
        {
            getRef().validate( task );
        }
        else
        {
            if ( file == null )
            {
                throw new BuildException( "You must specify the 'file' for the artifact" );
            }
            else if ( !file.isFile() )
            {
                throw new BuildException( "The artifact file " + file + " does not exist" );
            }
            if ( type == null || type.length() <= 0 )
            {
                throw new BuildException( "You must specify the 'type' for the artifact" );
            }
        }
    }

    public void setRefid( Reference ref )
    {
        if ( file != null || type != null || classifier != null )
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

        if ( file != null && type == null )
        {
            String name = file.getName();
            int period = name.lastIndexOf( '.' );
            if ( period >= 0 )
            {
                type = name.substring( period + 1 );
            }
        }
    }

    public String getType()
    {
        if ( isReference() )
        {
            return getRef().getType();
        }
        return ( type != null ) ? type : "jar";
    }

    public void setType( String type )
    {
        checkAttributesAllowed();
        this.type = type;
    }

    public String getClassifier()
    {
        if ( isReference() )
        {
            return getRef().getClassifier();
        }
        return ( classifier != null ) ? classifier : "";
    }

    public void setClassifier( String classifier )
    {
        checkAttributesAllowed();
        this.classifier = classifier;
    }

    public void setPomRef( Reference ref )
    {
        checkAttributesAllowed();
        Pom pom = new Pom();
        pom.setProject( getProject() );
        pom.setRefid( ref );
        this.pom = pom;
    }

    public void addPom( Pom pom )
    {
        checkChildrenAllowed();
        this.pom = pom;
    }

    public Pom getPom()
    {
        if ( isReference() )
        {
            return getRef().getPom();
        }
        return pom;
    }

    public List<Artifact> getArtifacts()
    {
        return Collections.singletonList( this );
    }

    @Override
    public void execute()
        throws BuildException
    {
        ProjectWorkspaceReader.getInstance().addArtifact( this );
    }

    public String toString()
    {
        String pomRepr = getPom() != null ? "(" + getPom().toString() + ":)" : "";
        return String.format( pomRepr + "%s:%s", getType(), getClassifier() );
    }

}
