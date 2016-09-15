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
import java.util.Collections;
import java.util.List;

import org.apache.maven.resolver.internal.ant.ProjectWorkspaceReader;
import org.apache.maven.resolver.internal.ant.tasks.RefTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

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
