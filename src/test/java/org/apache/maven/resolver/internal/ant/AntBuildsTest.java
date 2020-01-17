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

import java.io.File;
import java.io.PrintStream;

import org.apache.maven.resolver.internal.ant.ProjectWorkspaceReader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.eclipse.aether.internal.test.util.TestFileUtils;

public abstract class AntBuildsTest
    extends BuildFileTest
{

    private static final File BASE_DIR;

    protected static final File BUILD_DIR;

    static
    {
        BASE_DIR = new File( "" ).getAbsoluteFile();
        BUILD_DIR = new File( BASE_DIR, "target/ant" );
    }

    protected File projectDir;

    protected File localRepoDir;

    protected File distRepoDir;

    protected String getProjectDirName()
    {
        String name = getClass().getSimpleName();
        if ( name.endsWith( "Test" ) )
        {
            name = name.substring( 0, name.length() - 4 );
        }
        return name;
    }

    protected void setUpProperties()
        throws Exception
    {
        // hook for subclasses to set further system properties for the project to pick up
    }

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        TestFileUtils.deleteFile( BUILD_DIR );

        projectDir = new File( new File( BASE_DIR, "src/test/resources/ant" ), getProjectDirName() );
        localRepoDir = new File( BUILD_DIR, "local-repo" );
        distRepoDir = new File( BUILD_DIR, "dist-repo" );

        System.setProperty( "project.dir", projectDir.getAbsolutePath() );
        System.setProperty( "build.dir", BUILD_DIR.getAbsolutePath() );
        System.setProperty( "maven.repo.local", localRepoDir.getAbsolutePath() );
        System.setProperty( "project.distrepo.url", distRepoDir.toURI().toString() );
        if ( "1.7".equals( System.getProperty( "java.specification.version", "1.7" ) ) )
        {
            System.setProperty( "https.protocols", "TLSv1.2" );
        }
        setUpProperties();

        configureProject( new File( projectDir, "ant.xml" ).getAbsolutePath(), Project.MSG_VERBOSE );
    }

    @Override
    protected void tearDown()
        throws Exception
    {
        try
        {
            ProjectWorkspaceReader.dropInstance();
            TestFileUtils.deleteFile( BUILD_DIR );
        }
        finally
        {
            super.tearDown();
        }
    }

    @Override
    public void configureProject( String filename, int logLevel )
        throws BuildException
    {
        super.configureProject( filename, logLevel );
        DefaultLogger logger = new DefaultLogger()
        {
            @Override
            protected void printMessage( String message, PrintStream stream, int priority )
            {
                message = System.currentTimeMillis() + " " + message;
                super.printMessage( message, stream, priority );
            }
        };
        logger.setMessageOutputLevel( logLevel );
        logger.setOutputPrintStream( System.out );
        logger.setErrorPrintStream( System.err );
        getProject().addBuildListener( logger );
    }

}
