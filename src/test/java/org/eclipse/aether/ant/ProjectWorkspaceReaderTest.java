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
package org.eclipse.aether.ant;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.tools.ant.Project;
import org.eclipse.aether.ant.ProjectWorkspaceReader;
import org.eclipse.aether.ant.types.Pom;
import org.junit.Before;
import org.junit.Test;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

public class ProjectWorkspaceReaderTest
{

    private ProjectWorkspaceReader reader;

    private Project project;

    @Before
    public void setUp()
        throws Exception
    {
        this.reader = new ProjectWorkspaceReader();

        this.project = new Project();
        project.setProperty( "user.home", System.getProperty( "user.home" ) );
    }

    private Artifact artifact( String coords )
    {
        return new DefaultArtifact( coords );
    }

    private File getFile( String name )
    {
        return new File( "src/test/resources/ProjectWorkspaceReader", name );
    }

    @Test
    public void testFindPom()
    {
        Pom pom = new Pom();
        pom.setProject( project );
        pom.setFile( getFile( "dummy-pom.xml" ) );

        reader.addPom( pom );

        assertEquals( pom.getFile(), reader.findArtifact( artifact( "test:dummy:pom:0.1-SNAPSHOT" ) ) );
        assertNull( reader.findArtifact( artifact( "unavailable:test:pom:0.1-SNAPSHOT" ) ) );
    }

    @Test
    public void testFindArtifact()
    {
        Pom pom = new Pom();
        pom.setProject( project );
        pom.setFile( getFile( "dummy-pom.xml" ) );

        reader.addPom( pom );

        org.eclipse.aether.ant.types.Artifact artifact = new org.eclipse.aether.ant.types.Artifact();
        artifact.setProject( project );
        artifact.addPom( pom );
        artifact.setFile( getFile( "dummy-file.txt" ) );

        reader.addArtifact( artifact );

        assertEquals( artifact.getFile(), reader.findArtifact( artifact( "test:dummy:txt:0.1-SNAPSHOT" ) ) );
        assertNull( reader.findArtifact( artifact( "unavailable:test:jar:0.1-SNAPSHOT" ) ) );
    }

}
