/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.resolver.internal.ant;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItemInArray;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.junit.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ResolveTest extends AntBuildsTest {

    @Test
    public void testResolveGlobalPom() {
        executeTarget("testResolveGlobalPom");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-api:jar");
        assertNotNull(prop, "aether-api was not resolved as a property");
        assertThat(
                "aether-api was not resolved to default local repository",
                prop,
                allOf(containsString("aether-api"), endsWith(".jar")));
    }

    @Test
    public void testResolveOverrideGlobalPom() {
        executeTarget("testResolveOverrideGlobalPom");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-api:jar");
        assertNotNull(prop, "aether-api was not resolved as a property");
        assertThat(
                "aether-api was not resolved to default local repository",
                prop,
                allOf(containsString("aether-api"), endsWith(".jar")));
    }

    @Test
    public void testResolveGlobalPomIntoOtherLocalRepo() {
        executeTarget("testResolveGlobalPomIntoOtherLocalRepo");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-api:jar");
        assertNotNull(prop, "aether-api was not resolved as a property");
        assertTrue(
                prop.replace('\\', '/')
                        .endsWith("local-repo-custom/org/eclipse/aether/aether-api/0.9.0.M3/aether-api-0.9.0.M3.jar"),
                "aether-api was not resolved to default local repository");
    }

    @Test
    public void testResolveCustomFileLayout() throws IOException {
        File dir = new File(BUILD_DIR, "resolve-custom-layout");
        executeTarget("testResolveCustomFileLayout");

        assertTrue(
                new File(dir, "org.eclipse.aether/aether-api/org/eclipse/aether/jar").exists(),
                "aether-api was not saved with custom file layout");
    }

    @Test
    public void testResolveAttachments() throws IOException {
        File dir = new File(BUILD_DIR, "resolve-attachments");
        executeTarget("testResolveAttachments");

        File jdocDir = new File(dir, "javadoc");

        assertTrue(
                new File(jdocDir, "org.eclipse.aether-aether-api-javadoc.jar").exists(),
                "aether-api-javadoc was not saved with custom file layout");

        assertThat("found non-javadoc files", Arrays.asList(jdocDir.list()), everyItem(endsWith("javadoc.jar")));

        File sourcesDir = new File(dir, "sources");
        assertTrue(
                new File(sourcesDir, "org.eclipse.aether-aether-api-sources.jar").exists(),
                "aether-api-sources was not saved with custom file layout");
        assertThat("found non-sources files", Arrays.asList(sourcesDir.list()), everyItem(endsWith("sources.jar")));
    }

    @Test
    public void testResolvePath() {
        executeTarget("testResolvePath");
        Map<?, ?> refs = getProject().getReferences();
        Object obj = refs.get("out");
        assertInstanceOf(Path.class, obj, "ref 'out' is no path");
        Path path = (Path) obj;
        String[] elements = path.list();
        assertThat(
                "no aether-api on classpath",
                elements,
                hasItemInArray(allOf(containsString("aether-api"), endsWith(".jar"))));
    }

    @Test
    public void testResolveDepsFromFile() {
        executeTarget("testResolveDepsFromFile");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-spi:jar");
        assertNotNull(prop, "aether-spi was not resolved as a property");
        assertThat(
                "aether-spi was not resolved to default local repository",
                prop,
                allOf(containsString("aether-spi"), endsWith(".jar")));
        prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-api:jar");
        assertNull(prop, "aether-api was resolved as a property");
    }

    @Test
    public void testResolveNestedDependencyCollections() {
        executeTarget("testResolveNestedDependencyCollections");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-spi:jar");
        assertNotNull(prop, "aether-spi was not resolved as a property");
        prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-util:jar");
        assertNotNull(prop, "aether-util was not resolved as a property");
        prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-api:jar");
        assertNull(prop, "aether-api was resolved as a property");
    }

    @Test
    public void testResolveResourceCollectionOnly() {
        executeTarget("testResolveResourceCollectionOnly");

        ResourceCollection resources = (ResourceCollection) getProject().getReference("files");
        assertNotNull(resources);
        assertThat(resources.size(), is(2));
        assertThat(resources.isFilesystemOnly(), is(true));
        Iterator<?> it = resources.iterator();
        FileResource file = (FileResource) it.next();
        assertThat(file.getFile().getName(), is("aether-spi-0.9.0.v20140226.jar"));
        file = (FileResource) it.next();
        assertThat(file.getFile().getName(), is("aether-api-0.9.0.v20140226.jar"));
    }

    @Test
    public void testResolveTransitiveDependencyManagement() {
        executeTarget("testResolveTransitiveDependencyManagement");

        String prop = getProject().getProperty("test.resolve.path.org.slf4j:slf4j-api:jar");
        assertNotNull(prop, "slf4j-api was not resolved as a property");
        assertThat(
                "slf4j-api was not resolved to default local repository",
                prop,
                allOf(containsString("slf4j-api"), endsWith("slf4j-api-2.0.6.jar")));

        prop = getProject().getProperty("test.resolve.path.org.apiguardian:apiguardian-api:jar");
        assertNotNull(prop, "apiguardian-api was not resolved as a property");
        assertThat(
                "apiguardian-api was not resolved to default local repository",
                prop,
                allOf(containsString("apiguardian-api"), endsWith("apiguardian-api-1.1.1.jar")));
    }

    @Test
    public void testResolveTransitiveDependencyManagementTestScope() {
        executeTarget("testResolveTransitiveDependencyManagementTestScope");

        String prop = getProject().getProperty("test.compile.resolve.path.org.slf4j:slf4j-api:jar");
        assertNotNull(prop, "slf4j-api was not resolved as a property");
        assertThat(
                "slf4j-api was not resolved to default local repository",
                prop,
                allOf(containsString("slf4j-api"), endsWith("slf4j-api-2.0.6.jar")));

        prop = getProject().getProperty("test.resolve.path.org.apiguardian:apiguardian-api:jar");
        assertNotNull(prop, "apiguardian-api was not resolved as a property");
        assertThat(
                "apiguardian-api was not resolved to default local repository",
                prop,
                allOf(containsString("apiguardian-api"), endsWith("apiguardian-api-1.1.1.jar")));
    }
}
