package org.apache.maven.resolver.internal.ant;

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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;

public class InstallTest
    extends AntBuildsTest
{

    public void testInstallGlobalPom()
    {
        executeTarget( "testInstallGlobalPom" );
        long tstamp = System.currentTimeMillis();

        assertLogContaining( "Installing" );
        
        assertUpdatedFile( tstamp, localRepoDir, "test/dummy/0.1-SNAPSHOT/dummy-0.1-SNAPSHOT.pom" );
    }

    public void testInstallOverrideGlobalPom()
    {
        executeTarget( "testInstallOverrideGlobalPom" );
        long tstamp = System.currentTimeMillis();

        assertLogContaining( "Installing" );

        assertUpdatedFile( tstamp, localRepoDir, "test/other/0.1-SNAPSHOT/other-0.1-SNAPSHOT.pom" );
    }

    public void testInstallOverrideGlobalPomByRef()
    {
        long tstamp = System.currentTimeMillis();
        executeTarget( "testInstallOverrideGlobalPomByRef" );

        assertLogContaining( "Installing" );

        assertUpdatedFile( tstamp, localRepoDir, "test/dummy/0.1-SNAPSHOT/dummy-0.1-SNAPSHOT.pom" );
        assertUpdatedFile( tstamp, localRepoDir, "test/other/0.1-SNAPSHOT/other-0.1-SNAPSHOT.pom" );
    }

    public void testDefaultRepo()
    {
        executeTarget( "testDefaultRepo" );
        long tstamp = System.currentTimeMillis();

        assertLogContaining( "Installing" );

        assertUpdatedFile( tstamp, localRepoDir, "test/dummy/0.1-SNAPSHOT/dummy-0.1-SNAPSHOT.pom" );
        assertUpdatedFile( tstamp, localRepoDir, "test/dummy/0.1-SNAPSHOT/dummy-0.1-SNAPSHOT-ant.xml" );
    }

    public void testCustomRepo()
        throws IOException
    {
        File repoPath = new File( BUILD_DIR, "local-repo-custom" );

        executeTarget( "testCustomRepo" );
        long tstamp = System.currentTimeMillis();

        System.out.println( getLog() );
        assertLogContaining( "Installing" );

        assertUpdatedFile( tstamp, repoPath, "test/dummy/0.1-SNAPSHOT/dummy-0.1-SNAPSHOT.pom" );
        assertUpdatedFile( tstamp, repoPath, "test/dummy/0.1-SNAPSHOT/dummy-0.1-SNAPSHOT-ant.xml" );
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
