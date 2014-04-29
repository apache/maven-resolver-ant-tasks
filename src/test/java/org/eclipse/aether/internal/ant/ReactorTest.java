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
import java.io.IOException;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.internal.ant.ProjectWorkspaceReader;

public class ReactorTest
    extends AntBuildsTest
{

    private Artifact artifact( String coords )
    {
        return new DefaultArtifact( coords );
    }

    public void testPom()
        throws IOException
    {
        executeTarget( "testPom" );
        ProjectWorkspaceReader reader = ProjectWorkspaceReader.getInstance();
        File found = reader.findArtifact( artifact( "test:test:pom:0.1-SNAPSHOT" ) );
        assertNotNull( found );
        assertEquals( new File( projectDir, "pom1.xml" ), found.getAbsoluteFile() );
    }

    public void testArtifact()
        throws IOException
    {
        executeTarget( "testArtifact" );
        ProjectWorkspaceReader reader = ProjectWorkspaceReader.getInstance();
        File found = reader.findArtifact( artifact( "test:test:pom:0.1-SNAPSHOT" ) );
        assertNotNull( found );
        assertEquals( new File( projectDir, "pom1.xml" ), found.getAbsoluteFile() );

        found = reader.findArtifact( artifact( "test:test:xml:0.1-SNAPSHOT" ) );
        assertNotNull( found );
        assertEquals( new File( projectDir, "pom1.xml" ), found.getAbsoluteFile() );
    }

    public void testArtifactInMemoryPom()
        throws IOException
    {
        executeTarget( "testArtifactInMemoryPom" );
        ProjectWorkspaceReader reader = ProjectWorkspaceReader.getInstance();
        File found = reader.findArtifact( artifact( "test:test:pom:0.1-SNAPSHOT" ) );
        assertNull( found );

        found = reader.findArtifact( artifact( "test:test:xml:0.1-SNAPSHOT" ) );
        assertNotNull( found );
        assertEquals( new File( projectDir, "pom1.xml" ), found.getAbsoluteFile() );
    }

    public void testResolveArtifact()
        throws IOException
    {
        executeTarget( "testResolveArtifact" );
        String prop = project.getProperty( "resolve.test:test:jar" );
        assertEquals( new File( projectDir, "pom1.xml" ).getAbsolutePath(), prop );
    }

    public void testResolveArtifactInMemoryPom()
        throws IOException
    {
        executeTarget( "testResolveArtifactInMemoryPom" );
        String prop = project.getProperty( "resolve.test:test:jar" );
        assertEquals( new File( projectDir, "pom1.xml" ).getAbsolutePath(), prop );
        assertLogContaining( "The POM for test:test:jar:0.1-SNAPSHOT is missing, no dependency information available" );
    }

    public void testResolveVersionRange()
        throws IOException
    {
        executeTarget( "testResolveVersionRange" );
        String prop = project.getProperty( "resolve.test:test:jar" );
        assertEquals( new File( projectDir, "pom1.xml" ).getAbsolutePath(), prop );
    }

}
