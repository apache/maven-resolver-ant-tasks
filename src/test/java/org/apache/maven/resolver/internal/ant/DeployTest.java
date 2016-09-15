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
import java.util.Arrays;

/*
 * still missing:
 * - deploy snapshots/releases into correct repos
 */
public class DeployTest
    extends AntBuildsTest
{

    public void testDeployGlobalPom()
    {
        long min = System.currentTimeMillis();
        executeTarget( "testDeployGlobalPom" );
        long max = System.currentTimeMillis();

        assertLogContaining( "Uploading" );
        
        assertUpdatedFile( min, max, distRepoDir, "test/dummy/0.1-SNAPSHOT/maven-metadata.xml" );
    }

    public void testDeployOverrideGlobalPom()
    {
        long min = System.currentTimeMillis();
        executeTarget( "testDeployOverrideGlobalPom" );
        long max = System.currentTimeMillis();

        assertLogContaining( "Uploading" );

        assertUpdatedFile( min, max, distRepoDir, "test/other/0.1-SNAPSHOT/maven-metadata.xml" );
    }

    public void testDeployOverrideGlobalPomByRef()
    {
        long min = System.currentTimeMillis();
        executeTarget( "testDeployOverrideGlobalPomByRef" );
        long max = System.currentTimeMillis();

        assertLogContaining( "Uploading" );

        assertUpdatedFile( min, max, distRepoDir, "test/dummy/0.1-SNAPSHOT/maven-metadata.xml" );
        assertUpdatedFile( min, max, distRepoDir, "test/other/0.1-SNAPSHOT/maven-metadata.xml" );
    }

    public void testDeployAttachedArtifact()
    {
        executeTarget( "testDeployAttachedArtifact" );

        assertLogContaining( "Uploading" );

        File dir = new File(distRepoDir, "test/dummy/0.1-SNAPSHOT/" );
        String[] files = dir.list();
        assertThat( "attached artifact not found: " + Arrays.toString( files ), files,
                    hasItemInArray( endsWith( "-ant.xml" ) ) );
    }

    private void assertUpdatedFile( long min, long max, File repoPath, String path )
    {
        File file = new File( repoPath, path );
        min = (min / 1000) * 1000;
        max = ((max + 999) / 1000) * 1000;
        assertThat( "File does not exist in default repo: " + file.getAbsolutePath(), file.exists() );
        assertThat( "Files were not updated for 1s before/after timestamp", file.lastModified(),
                    allOf( greaterThanOrEqualTo( min ), lessThanOrEqualTo( max ) ) );
    }
}
