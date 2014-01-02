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

import java.io.File;
import java.io.PrintStream;

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
