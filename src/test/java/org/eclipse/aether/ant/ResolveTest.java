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
package org.eclipse.aether.ant;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.eclipse.aether.test.util.TestFileUtils;

public class ResolveTest
    extends AntBuildsTest
{

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();
        configureProject( "src/test/ant/Resolve.xml", Project.MSG_VERBOSE );
    }

    public void testResolveGlobalPom()
    {
        executeTarget( "testResolveGlobalPom" );

        String prop = getProject().getProperty( "test.resolve.path.org.sonatype.aether:aether-api:jar" );
        assertThat( "aether-api was not resolved as a property", prop, notNullValue() );
        assertThat( "aether-api was not resolved to default local repository", prop,
                    allOf( containsString( "aether-api" ), endsWith( ".jar" ) ) );
    }

    public void testResolveOverrideGlobalPom()
    {
        executeTarget( "testResolveOverrideGlobalPom" );

        String prop = getProject().getProperty( "test.resolve.path.org.sonatype.aether:aether-api:jar" );
        assertThat( "aether-api was not resolved as a property", prop, notNullValue() );
        assertThat( "aether-api was not resolved to default local repository", prop,
                    allOf( containsString( "aether-api" ), endsWith( ".jar" ) ) );
    }

    public void testResolveGlobalPomIntoOtherLocalRepo()
    {
        executeTarget( "testResolveGlobalPomIntoOtherLocalRepo" );

        String prop = getProject().getProperty( "test.resolve.path.org.sonatype.aether:aether-api:jar" );
        assertThat( "aether-api was not resolved as a property", prop, notNullValue() );
        assertThat( "aether-api was not resolved to default local repository", prop,
                    containsString( "resolvetest-local-repo" ) );
    }

    public void testResolveCustomFileLayout()
        throws IOException
    {
        File dir = new File( BUILD_DIR, "resolve-custom-layout" );
        TestFileUtils.delete( dir );
        executeTarget( "testResolveCustomFileLayout" );

        assertThat( "aether-api was not saved with custom file layout",
                    new File( dir, "org.sonatype.aether/aether-api/org/sonatype/aether/jar" ).exists() );

        TestFileUtils.delete( dir );
    }

    public void testResolveAttachments()
        throws IOException
    {
        File dir = new File( BUILD_DIR, "resolve-attachments" );
        TestFileUtils.delete( dir );
        executeTarget( "testResolveAttachments" );
        
        File jdocDir = new File(dir, "javadoc");
        
        assertThat( "aether-api-javadoc was not saved with custom file layout",
                    new File( jdocDir, "org.sonatype.aether-aether-api-javadoc.jar" ).exists() );

        assertThat( "found non-javadoc files", Arrays.asList( jdocDir.list() ), everyItem( endsWith( "javadoc.jar" ) ) );

        File sourcesDir = new File( dir, "sources" );
        assertThat( "aether-api-sources was not saved with custom file layout",
                    new File( sourcesDir, "org.sonatype.aether-aether-api-sources.jar" ).exists() );
        assertThat( "found non-sources files", Arrays.asList( sourcesDir.list() ),
                    everyItem( endsWith( "sources.jar" ) ) );


        TestFileUtils.delete( dir );
    }

    public void testResolvePath()
    {
        executeTarget( "testResolvePath" );
        Map<?, ?> refs = getProject().getReferences();
        Object obj = refs.get( "out" );
        assertThat( "ref 'out' is no path", obj, instanceOf( Path.class ) );
        Path path = (Path) obj;
        String[] elements = path.list();
        assertThat( "no aether-api on classpath", elements,
                    hasItemInArray( allOf( containsString( "aether-api" ), endsWith( ".jar" ) ) ) );
    }

}
