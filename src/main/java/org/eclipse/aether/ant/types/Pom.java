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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Model;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;
import org.eclipse.aether.ant.AntRepoSys;
import org.eclipse.aether.ant.ProjectWorkspaceReader;
import org.eclipse.aether.ant.tasks.RefTask;

/**
 */
public class Pom
    extends RefTask
{

    private Model model;

    private String id;

    private File file;

    private String groupId;

    private String artifactId;

    private String version;

    private String packaging = "jar";

    private RemoteRepositories remoteRepositories;

    private String coords;

    protected Pom getRef()
    {
        return (Pom) getCheckedRef();
    }

    public void validate()
    {
        if ( isReference() )
        {
            getRef().validate();
        }
        else
        {
            if ( file == null )
            {
                if ( groupId == null )
                {
                    throw new BuildException( "You must specify the 'groupId' for the POM" );
                }
                if ( artifactId == null )
                {
                    throw new BuildException( "You must specify the 'artifactId' for the POM" );
                }
                if ( version == null )
                {
                    throw new BuildException( "You must specify the 'version' for the POM" );
                }
            }
        }

    }

    public void setRefid( Reference ref )
    {
        if ( id != null || file != null || groupId != null || artifactId != null || version != null )
        {
            throw tooManyAttributes();
        }
        if ( remoteRepositories != null )
        {
            throw noChildrenAllowed();
        }
        super.setRefid( ref );
    }

    public void setId( String id )
    {
        checkAttributesAllowed();
        this.id = id;
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
        if ( groupId != null || artifactId != null || version != null )
        {
            throw ambiguousSource();
        }

        this.file = file;

    }

    public String getGroupId()
    {
        if ( isReference() )
        {
            return getRef().getGroupId();
        }
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        checkAttributesAllowed();
        if ( this.groupId != null )
        {
            throw ambiguousCoords();
        }
        if ( file != null )
        {
            throw ambiguousSource();
        }
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        if ( isReference() )
        {
            return getRef().getArtifactId();
        }
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        checkAttributesAllowed();
        if ( this.artifactId != null )
        {
            throw ambiguousCoords();
        }
        if ( file != null )
        {
            throw ambiguousSource();
        }
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        if ( isReference() )
        {
            return getRef().getVersion();
        }
        return version;
    }

    public void setVersion( String version )
    {
        checkAttributesAllowed();
        if ( this.version != null )
        {
            throw ambiguousCoords();
        }
        if ( file != null )
        {
            throw ambiguousSource();
        }
        this.version = version;
    }

    public void setCoords( String coords )
    {
        checkAttributesAllowed();
        if ( file != null )
        {
            throw ambiguousSource();
        }
        if ( groupId != null || artifactId != null || version != null )
        {
            throw ambiguousCoords();
        }
        Pattern p = Pattern.compile( "([^: ]+):([^: ]+):([^: ]+)" );
        Matcher m = p.matcher( coords );
        if ( !m.matches() )
        {
            throw new BuildException( "Bad POM coordinates, expected format is <groupId>:<artifactId>:<version>" );
        }
        groupId = m.group( 1 );
        artifactId = m.group( 2 );
        version = m.group( 3 );
    }

    private BuildException ambiguousCoords()
    {
        return new BuildException( "You must not specify both 'coords' and ('groupId', 'artifactId', 'version')" );
    }

    private BuildException ambiguousSource()
    {
        return new BuildException( "You must not specify both 'file' and "
            + "('coords', 'groupId', 'artifactId', 'version')" );
    }

    public String getPackaging()
    {
        if ( isReference() )
        {
            return getRef().getPackaging();
        }
        return packaging;
    }

    public void setPackaging( String packaging )
    {
        checkAttributesAllowed();
        if ( file != null )
        {
            throw ambiguousSource();
        }
        this.packaging = packaging;
    }

    private RemoteRepositories getRemoteRepos()
    {
        if ( remoteRepositories == null )
        {
            remoteRepositories = new RemoteRepositories();
            remoteRepositories.setProject( getProject() );
        }
        return remoteRepositories;
    }

    public void addRemoteRepo( RemoteRepository repository )
    {
        getRemoteRepos().addRemoterepo( repository );
    }

    public void addRemoteRepos( RemoteRepositories repositories )
    {
        getRemoteRepos().addRemoterepos( repositories );
    }

    public void setRemoteReposRef( Reference ref )
    {
        RemoteRepositories repos = new RemoteRepositories();
        repos.setProject( getProject() );
        repos.setRefid( ref );
        getRemoteRepos().addRemoterepos( repos );
    }

    public Model getModel( Task task )
    {
        if ( isReference() )
        {
            return getRef().getModel( task );
        }
        synchronized ( this )
        {
            if ( model == null )
            {
                if ( file != null )
                {
                    model = AntRepoSys.getInstance( getProject() ).loadModel( task, file, true, remoteRepositories );
                }
            }
            return model;
        }
    }

    @Override
    public void execute()
    {
        validate();

        if ( file != null && ( id == null || AntRepoSys.getInstance( getProject() ).getDefaultPom() == null ) )
        {
            AntRepoSys.getInstance( getProject() ).setDefaultPom( this );
        }

        ProjectWorkspaceReader.getInstance().addPom( this );

        Model model = getModel( this );

        if ( model == null )
        {
            coords = getGroupId() + ":" + getArtifactId() + ":" + getVersion();
            return;
        }

        coords = model.getGroupId() + ":" + model.getArtifactId() + ":" + model.getVersion();

        ModelValueExtractor extractor = new ModelValueExtractor( id, model, getProject() );

        PropertyHelper propHelper = PropertyHelper.getPropertyHelper( getProject() );

        try
        {
            // Ant 1.8.0 delegate
            PomPropertyEvaluator.register( extractor, propHelper );
        }
        catch ( LinkageError e )
        {
            // Ant 1.6 - 1.7.1 interceptor chaining
            PomPropertyHelper.register( extractor, propHelper );
        }

    }

    public String toString()
    {
        return coords + " (" + super.toString() + ")";
    }

    public String getCoords()
    {
        return coords;
    }

}
