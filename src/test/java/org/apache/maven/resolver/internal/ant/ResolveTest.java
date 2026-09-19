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

import junit.framework.JUnit4TestAdapter;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ResolveTest extends AntBuildsTest {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(ResolveTest.class);
    }

    @Test
    public void testResolveGlobalPom() {
        executeTarget("testResolveGlobalPom");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-api:jar");
        assertNotNull("aether-api was not resolved as a property", prop);
        assertTrue(
                "aether-api was not resolved to default local repository",
                prop.contains("aether-api") && prop.endsWith(".jar"));
    }

    @Test
    public void testResolveOverrideGlobalPom() {
        executeTarget("testResolveOverrideGlobalPom");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-api:jar");
        assertNotNull("aether-api was not resolved as a property", prop);
        assertTrue(
                "aether-api was not resolved to default local repository",
                prop.contains("aether-api") && prop.endsWith(".jar"));
    }

    @Test
    public void testResolveGlobalPomIntoOtherLocalRepo() {
        executeTarget("testResolveGlobalPomIntoOtherLocalRepo");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-api:jar");
        assertNotNull("aether-api was not resolved as a property", prop);
        assertTrue(
                "aether-api was not resolved to default local repository",
                prop.replace('\\', '/')
                        .endsWith("local-repo-custom/org/eclipse/aether/aether-api/0.9.0.M3/aether-api-0.9.0.M3.jar"));
    }

    @Test
    public void testResolveCustomFileLayout() throws IOException {
        File dir = new File(BUILD_DIR, "resolve-custom-layout");
        executeTarget("testResolveCustomFileLayout");

        assertTrue(
                "aether-api was not saved with custom file layout",
                new File(dir, "org.eclipse.aether/aether-api/org/eclipse/aether/jar").exists());
    }

    @Test
    public void testResolveAttachments() throws IOException {
        File dir = new File(BUILD_DIR, "resolve-attachments");
        executeTarget("testResolveAttachments");

        File jdocDir = new File(dir, "javadoc");

        assertTrue(
                "aether-api-javadoc was not saved with custom file layout",
                new File(jdocDir, "org.eclipse.aether-aether-api-javadoc.jar").exists());

        assertTrue("found non-javadoc files", Arrays.stream(jdocDir.list()).allMatch(f -> f.endsWith("javadoc.jar")));

        File sourcesDir = new File(dir, "sources");
        assertTrue(
                "aether-api-sources was not saved with custom file layout",
                new File(sourcesDir, "org.eclipse.aether-aether-api-sources.jar").exists());
        assertTrue(
                "found non-sources files", Arrays.stream(sourcesDir.list()).allMatch(f -> f.endsWith("sources.jar")));
    }

    @Test
    public void testResolvePath() {
        executeTarget("testResolvePath");
        Map<?, ?> refs = getProject().getReferences();
        Object obj = refs.get("out");
        assertTrue("ref 'out' is no path", obj instanceof Path);
        Path path = (Path) obj;
        String[] elements = path.list();
        assertTrue(
                "no aether-api on classpath",
                Arrays.stream(elements).anyMatch(e -> e.contains("aether-api") && e.endsWith(".jar")));
    }

    @Test
    public void testResolveDepsFromFile() {
        executeTarget("testResolveDepsFromFile");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-spi:jar");
        assertNotNull("aether-spi was not resolved as a property", prop);
        assertTrue(
                "aether-spi was not resolved to default local repository",
                prop.contains("aether-spi") && prop.endsWith(".jar"));
        prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-api:jar");
        assertNull("aether-api was resolved as a property", prop);
    }

    @Test
    public void testResolveNestedDependencyCollections() {
        executeTarget("testResolveNestedDependencyCollections");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-spi:jar");
        assertNotNull("aether-spi was not resolved as a property", prop);
        prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-util:jar");
        assertNotNull("aether-util was not resolved as a property", prop);
        prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-api:jar");
        assertNull("aether-api was resolved as a property", prop);
    }

    @Test
    public void testResolveResourceCollectionOnly() {
        executeTarget("testResolveResourceCollectionOnly");

        ResourceCollection resources = (ResourceCollection) getProject().getReference("files");
        assertNotNull(resources);
        assertEquals(2, resources.size());
        assertTrue(resources.isFilesystemOnly());
        Iterator<?> it = resources.iterator();
        FileResource file = (FileResource) it.next();
        assertEquals("aether-spi-0.9.0.v20140226.jar", file.getFile().getName());
        file = (FileResource) it.next();
        assertEquals("aether-api-0.9.0.v20140226.jar", file.getFile().getName());
    }

    @Test
    public void testResolveTransitiveDependencyManagement() {
        executeTarget("testResolveTransitiveDependencyManagement");

        String prop = getProject().getProperty("test.resolve.path.org.slf4j:slf4j-api:jar");
        assertNotNull("slf4j-api was not resolved as a property", prop);
        assertTrue(
                "slf4j-api was not resolved to default local repository",
                prop.contains("slf4j-api") && prop.endsWith("slf4j-api-2.0.6.jar"));

        prop = getProject().getProperty("test.resolve.path.org.apiguardian:apiguardian-api:jar");
        assertNotNull("apiguardian-api was not resolved as a property", prop);
        assertTrue(
                "apiguardian-api was not resolved to default local repository",
                prop.contains("apiguardian-api") && prop.endsWith("apiguardian-api-1.1.1.jar"));
    }

    @Test
    public void testResolveTransitiveDependencyManagementTestScope() {
        executeTarget("testResolveTransitiveDependencyManagementTestScope");

        String prop = getProject().getProperty("test.compile.resolve.path.org.slf4j:slf4j-api:jar");
        assertNotNull("slf4j-api was not resolved as a property", prop);
        assertTrue(
                "slf4j-api was not resolved to default local repository",
                prop.contains("slf4j-api") && prop.endsWith("slf4j-api-2.0.6.jar"));

        prop = getProject().getProperty("test.resolve.path.org.apiguardian:apiguardian-api:jar");
        assertNotNull("apiguardian-api was not resolved as a property", prop);
        assertTrue(
                "apiguardian-api was not resolved to default local repository",
                prop.contains("apiguardian-api") && prop.endsWith("apiguardian-api-1.1.1.jar"));
    }
}
