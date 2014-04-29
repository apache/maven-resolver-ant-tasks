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
package org.eclipse.aether.internal.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.model.Model;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.internal.ant.types.Pom;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.eclipse.aether.util.artifact.ArtifactIdUtils;

/**
 * Workspace reader caching available POMs and artifacts for ant builds.
 * <p/>
 * &lt;pom> elements are cached if they are defined by the 'file'-attribute, as they reference a backing pom.xml file that
 * can be used for resolution with Aether. &lt;artifact> elements are cached if they directly define a 'pom'-attribute
 * or child. The POM may be file-based or in-memory.
 */
public class ProjectWorkspaceReader
    implements WorkspaceReader
{

    private static volatile ProjectWorkspaceReader instance;

    private static final Object LOCK = new Object();

    private Map<String, Artifact> artifacts = new ConcurrentHashMap<String, Artifact>();

    public void addPom( Pom pom )
    {
        if ( pom.getFile() != null )
        {
            Model model = pom.getModel( pom );
            Artifact aetherArtifact =
                new DefaultArtifact( model.getGroupId(), model.getArtifactId(), null, "pom", model.getVersion() );
            aetherArtifact = aetherArtifact.setFile( pom.getFile() );
            String coords = coords( aetherArtifact );
            artifacts.put( coords, aetherArtifact );
        }
    }

    public void addArtifact( org.eclipse.aether.internal.ant.types.Artifact artifact )
    {
        if ( artifact.getPom() != null )
        {
            Pom pom = artifact.getPom();
            Artifact aetherArtifact;
            if ( pom.getFile() != null )
            {
                Model model = pom.getModel( pom );
                aetherArtifact =
                    new DefaultArtifact( model.getGroupId(), model.getArtifactId(), artifact.getClassifier(),
                                         artifact.getType(), model.getVersion() );
            }
            else
            {
                aetherArtifact =
                    new DefaultArtifact( pom.getGroupId(), pom.getArtifactId(), artifact.getClassifier(),
                                         artifact.getType(), pom.getVersion() );
            }
            aetherArtifact = aetherArtifact.setFile( artifact.getFile() );

            String coords = coords( aetherArtifact );
            artifacts.put( coords, aetherArtifact );
        }
    }

    private String coords( Artifact artifact )
    {
        return ArtifactIdUtils.toId( artifact );
    }

    public WorkspaceRepository getRepository()
    {
        return new WorkspaceRepository( "ant" );
    }

    public File findArtifact( Artifact artifact )
    {
        artifact = artifacts.get( coords( artifact ) );
        return ( artifact != null ) ? artifact.getFile() : null;
    }

    public List<String> findVersions( Artifact artifact )
    {
        List<String> versions = new ArrayList<String>();
        for ( Artifact art : artifacts.values() )
        {
            if ( ArtifactIdUtils.equalsVersionlessId( artifact, art ) )
            {
                versions.add( art.getVersion() );
            }
        }
        return versions;
    }

    ProjectWorkspaceReader()
    {
    }

    public static ProjectWorkspaceReader getInstance()
    {
        if ( instance == null )
        {
            synchronized ( LOCK )
            {
                if ( instance == null )
                {
                    instance = new ProjectWorkspaceReader();
                }
            }
        }
        return instance;
    }

    static void dropInstance()
    {
        instance = null;
    }
}
