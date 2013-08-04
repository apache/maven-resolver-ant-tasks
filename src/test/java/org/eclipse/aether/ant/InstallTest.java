/*******************************************************************************
 * Copyright (c) 2010, 2013 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.aether.ant;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.aether.internal.test.util.TestFileUtils;

public class InstallTest
    extends AntBuildsTest
{

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        TestFileUtils.deleteFile( new File( defaultLocalRepository, "test" ) );

        configureProject( "src/test/ant/Install.xml" );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        TestFileUtils.deleteFile( new File( defaultLocalRepository, "test" ) );
    }

    public void testInstallGlobalPom()
    {
        executeTarget( "testInstallGlobalPom" );
        long tstamp = System.currentTimeMillis();

        assertLogContaining( "Installing" );
        
        assertUpdatedFile( tstamp, defaultLocalRepository, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT.pom" );
    }

    public void testInstallOverrideGlobalPom()
    {
        executeTarget( "testInstallOverrideGlobalPom" );
        long tstamp = System.currentTimeMillis();

        assertLogContaining( "Installing" );

        assertUpdatedFile( tstamp, defaultLocalRepository, "test/other/0.1-SNAPSHOT/other-0.1-SNAPSHOT.pom" );
    }

    public void testInstallOverrideGlobalPomByRef()
    {
        long tstamp = System.currentTimeMillis();
        executeTarget( "testInstallOverrideGlobalPomByRef" );

        assertLogContaining( "Installing" );

        assertUpdatedFile( tstamp, defaultLocalRepository, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT.pom" );
        assertUpdatedFile( tstamp, defaultLocalRepository, "test/other/0.1-SNAPSHOT/other-0.1-SNAPSHOT.pom" );
    }

    public void testDefaultRepo()
    {
        executeTarget( "testDefaultRepo" );
        long tstamp = System.currentTimeMillis();

        assertLogContaining( "Installing" );

        assertUpdatedFile( tstamp, defaultLocalRepository, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT.pom" );
        assertUpdatedFile( tstamp, defaultLocalRepository, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT-ant.xml" );
    }

    public void testCustomRepo()
        throws IOException
    {
        File repoPath = new File( BUILD_DIR, "local-repo-custom" );
        TestFileUtils.deleteFile( repoPath );

        executeTarget( "testCustomRepo" );
        long tstamp = System.currentTimeMillis();

        System.out.println( getLog() );
        assertLogContaining( "Installing" );

        assertUpdatedFile( tstamp, repoPath, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT.pom" );
        assertUpdatedFile( tstamp, repoPath, "test/test/0.1-SNAPSHOT/test-0.1-SNAPSHOT-ant.xml" );
        TestFileUtils.deleteFile( repoPath );
    }

    private void assertUpdatedFile( long tstamp, File repoPath, String path )
    {
        File file = new File( repoPath, path );
        assertThat( "File does not exist in default repo: " + file.getAbsolutePath(), file.exists() );
        assertThat( "Files were not updated for 1s before/after timestamp",
                    file.lastModified(),
                    allOf( greaterThanOrEqualTo( ( ( tstamp - 500 ) / 1000 ) * 1000 ),
                           lessThanOrEqualTo( tstamp + 2000 ) ) );
    }
}
