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
import org.eclipse.aether.internal.test.util.TestFileUtils;

public abstract class AntBuildsTest
    extends BuildFileTest
{

    private static final File BASE_DIR;

    protected static final File BUILD_DIR;

    static
    {
        BASE_DIR = new File( "." ).getAbsoluteFile();
        BUILD_DIR = new File( BASE_DIR, "target/ant" );
    }

    protected File localRepoDir;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        TestFileUtils.deleteFile( BUILD_DIR );

        File projectDir = new File( BASE_DIR, "src/test/ant" );
        localRepoDir = new File( BUILD_DIR, "local-repo" );

        System.setProperty( "project.dir", projectDir.getAbsolutePath() );
        System.setProperty( "build.dir", BUILD_DIR.getAbsolutePath() );
        System.setProperty( "maven.repo.local", localRepoDir.getAbsolutePath() );
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
