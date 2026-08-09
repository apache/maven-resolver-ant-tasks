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
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        assertTrue(
                prop.contains("aether-api") && prop.endsWith(".jar"),
                "aether-api was not resolved to default local repository, was: " + prop);
    }

    @Test
    public void testResolveOverrideGlobalPom() {
        executeTarget("testResolveOverrideGlobalPom");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-api:jar");
        assertNotNull(prop, "aether-api was not resolved as a property");
        assertTrue(
                prop.contains("aether-api") && prop.endsWith(".jar"),
                "aether-api was not resolved to default local repository, was: " + prop);
    }

    @Test
    public void testResolveGlobalPomIntoOtherLocalRepo() {
        executeTarget("testResolveGlobalPomIntoOtherLocalRepo");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-api:jar");
        assertNotNull(prop, "aether-api was not resolved as a property");
        String path = prop.replace('\\', '/');
        assertTrue(
                path.endsWith("local-repo-custom/org/eclipse/aether/aether-api/0.9.0.M3/aether-api-0.9.0.M3.jar"),
                "aether-api was not resolved to default local repository, was: " + path);
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

        List<String> javadocFiles = Arrays.asList(jdocDir.list());
        assertTrue(
                javadocFiles.stream().allMatch(name -> name.endsWith("javadoc.jar")),
                "found non-javadoc files: " + javadocFiles);

        File sourcesDir = new File(dir, "sources");
        assertTrue(
                new File(sourcesDir, "org.eclipse.aether-aether-api-sources.jar").exists(),
                "aether-api-sources was not saved with custom file layout");
        List<String> sourcesFiles = Arrays.asList(sourcesDir.list());
        assertTrue(
                sourcesFiles.stream().allMatch(name -> name.endsWith("sources.jar")),
                "found non-sources files: " + sourcesFiles);
    }

    @Test
    public void testResolvePath() {
        executeTarget("testResolvePath");
        Map<?, ?> refs = getProject().getReferences();
        Object obj = refs.get("out");
        Path path = assertInstanceOf(Path.class, obj, "ref 'out' is no path");
        String[] elements = path.list();
        assertTrue(
                Arrays.stream(elements).anyMatch(e -> e.contains("aether-api") && e.endsWith(".jar")),
                "no aether-api on classpath: " + Arrays.toString(elements));
    }

    @Test
    public void testResolveDepsFromFile() {
        executeTarget("testResolveDepsFromFile");

        String prop = getProject().getProperty("test.resolve.path.org.eclipse.aether:aether-spi:jar");
        assertNotNull(prop, "aether-spi was not resolved as a property");
        assertTrue(
                prop.contains("aether-spi") && prop.endsWith(".jar"),
                "aether-spi was not resolved to default local repository, was: " + prop);
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
        assertNotNull(prop, "slf4j-api was not resolved as a property");
        assertTrue(
                prop.contains("slf4j-api") && prop.endsWith("slf4j-api-2.0.6.jar"),
                "slf4j-api was not resolved to default local repository, was: " + prop);

        prop = getProject().getProperty("test.resolve.path.org.apiguardian:apiguardian-api:jar");
        assertNotNull(prop, "apiguardian-api was not resolved as a property");
        assertTrue(
                prop.contains("apiguardian-api") && prop.endsWith("apiguardian-api-1.1.1.jar"),
                "apiguardian-api was not resolved to default local repository, was: " + prop);
    }

    @Test
    public void testResolveTransitiveDependencyManagementTestScope() {
        executeTarget("testResolveTransitiveDependencyManagementTestScope");

        String prop = getProject().getProperty("test.compile.resolve.path.org.slf4j:slf4j-api:jar");
        assertNotNull(prop, "slf4j-api was not resolved as a property");
        assertTrue(
                prop.contains("slf4j-api") && prop.endsWith("slf4j-api-2.0.6.jar"),
                "slf4j-api was not resolved to default local repository, was: " + prop);

        prop = getProject().getProperty("test.resolve.path.org.apiguardian:apiguardian-api:jar");
        assertNotNull(prop, "apiguardian-api was not resolved as a property");
        assertTrue(
                prop.contains("apiguardian-api") && prop.endsWith("apiguardian-api-1.1.1.jar"),
                "apiguardian-api was not resolved to default local repository, was: " + prop);
    }
}
