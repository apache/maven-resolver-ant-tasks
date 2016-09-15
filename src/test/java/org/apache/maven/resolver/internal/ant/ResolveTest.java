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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;

public class ResolveTest
    extends AntBuildsTest
{

    public void testResolveGlobalPom()
    {
        executeTarget( "testResolveGlobalPom" );

        String prop = getProject().getProperty( "test.resolve.path.org.eclipse.aether:aether-api:jar" );
        assertThat( "aether-api was not resolved as a property", prop, notNullValue() );
        assertThat( "aether-api was not resolved to default local repository", prop,
                    allOf( containsString( "aether-api" ), endsWith( ".jar" ) ) );
    }

    public void testResolveOverrideGlobalPom()
    {
        executeTarget( "testResolveOverrideGlobalPom" );

        String prop = getProject().getProperty( "test.resolve.path.org.eclipse.aether:aether-api:jar" );
        assertThat( "aether-api was not resolved as a property", prop, notNullValue() );
        assertThat( "aether-api was not resolved to default local repository", prop,
                    allOf( containsString( "aether-api" ), endsWith( ".jar" ) ) );
    }

    public void testResolveGlobalPomIntoOtherLocalRepo()
    {
        executeTarget( "testResolveGlobalPomIntoOtherLocalRepo" );

        String prop = getProject().getProperty( "test.resolve.path.org.eclipse.aether:aether-api:jar" );
        assertThat( "aether-api was not resolved as a property", prop, notNullValue() );
        assertThat( "aether-api was not resolved to default local repository", prop.replace( '\\', '/' ),
                    endsWith( "local-repo-custom/org/eclipse/aether/aether-api/0.9.0.M3/aether-api-0.9.0.M3.jar" ) );
    }

    public void testResolveCustomFileLayout()
        throws IOException
    {
        File dir = new File( BUILD_DIR, "resolve-custom-layout" );
        executeTarget( "testResolveCustomFileLayout" );

        assertThat( "aether-api was not saved with custom file layout",
                    new File( dir, "org.eclipse.aether/aether-api/org/eclipse/aether/jar" ).exists() );
    }

    public void testResolveAttachments()
        throws IOException
    {
        File dir = new File( BUILD_DIR, "resolve-attachments" );
        executeTarget( "testResolveAttachments" );
        
        File jdocDir = new File(dir, "javadoc");
        
        assertThat( "aether-api-javadoc was not saved with custom file layout",
                    new File( jdocDir, "org.eclipse.aether-aether-api-javadoc.jar" ).exists() );

        assertThat( "found non-javadoc files", Arrays.asList( jdocDir.list() ), everyItem( endsWith( "javadoc.jar" ) ) );

        File sourcesDir = new File( dir, "sources" );
        assertThat( "aether-api-sources was not saved with custom file layout",
                    new File( sourcesDir, "org.eclipse.aether-aether-api-sources.jar" ).exists() );
        assertThat( "found non-sources files", Arrays.asList( sourcesDir.list() ),
                    everyItem( endsWith( "sources.jar" ) ) );
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

    public void testResolveDepsFromFile()
    {
        executeTarget( "testResolveDepsFromFile" );

        String prop = getProject().getProperty( "test.resolve.path.org.eclipse.aether:aether-spi:jar" );
        assertThat( "aether-spi was not resolved as a property", prop, notNullValue() );
        assertThat( "aether-spi was not resolved to default local repository", prop,
                    allOf( containsString( "aether-spi" ), endsWith( ".jar" ) ) );
        prop = getProject().getProperty( "test.resolve.path.org.eclipse.aether:aether-api:jar" );
        assertThat( "aether-api was resolved as a property", prop, nullValue() );
    }

    public void testResolveNestedDependencyCollections()
    {
        executeTarget( "testResolveNestedDependencyCollections" );

        String prop = getProject().getProperty( "test.resolve.path.org.eclipse.aether:aether-spi:jar" );
        assertThat( "aether-spi was not resolved as a property", prop, notNullValue() );
        prop = getProject().getProperty( "test.resolve.path.org.eclipse.aether:aether-util:jar" );
        assertThat( "aether-util was not resolved as a property", prop, notNullValue() );
        prop = getProject().getProperty( "test.resolve.path.org.eclipse.aether:aether-api:jar" );
        assertThat( "aether-api was resolved as a property", prop, nullValue() );
    }

    public void testResolveResourceCollectionOnly()
    {
        executeTarget( "testResolveResourceCollectionOnly" );

        ResourceCollection resources = (ResourceCollection) getProject().getReference( "files" );
        assertThat( resources, is( notNullValue() ) );
        assertThat( resources.size(), is( 2 ) );
        assertThat( resources.isFilesystemOnly(), is( true ) );
        Iterator<?> it = resources.iterator();
        FileResource file = (FileResource) it.next();
        assertThat( file.getFile().getName(), is( "aether-spi-0.9.0.v20140226.jar" ) );
        file = (FileResource) it.next();
        assertThat( file.getFile().getName(), is( "aether-api-0.9.0.v20140226.jar" ) );
    }

}
