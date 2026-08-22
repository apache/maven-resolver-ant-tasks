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

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReactorTest extends AntBuildsTest {
    private Artifact artifact(String coords) {
        return new DefaultArtifact(coords);
    }

    @Test
    void pom() throws Exception {
        executeTarget("testPom");
        ProjectWorkspaceReader reader = ProjectWorkspaceReader.getInstance();
        File found = reader.findArtifact(artifact("test:test:pom:0.1-SNAPSHOT"));
        assertNotNull(found);
        assertEquals(new File(projectDir, "pom1.xml"), found.getAbsoluteFile());
    }

    @Test
    void testArtifact() throws Exception {
        executeTarget("testArtifact");
        ProjectWorkspaceReader reader = ProjectWorkspaceReader.getInstance();
        File found = reader.findArtifact(artifact("test:test:pom:0.1-SNAPSHOT"));
        assertNotNull(found);
        assertEquals(new File(projectDir, "pom1.xml"), found.getAbsoluteFile());

        found = reader.findArtifact(artifact("test:test:xml:0.1-SNAPSHOT"));
        assertNotNull(found);
        assertEquals(new File(projectDir, "pom1.xml"), found.getAbsoluteFile());
    }

    @Test
    void artifactInMemoryPom() throws Exception {
        executeTarget("testArtifactInMemoryPom");
        ProjectWorkspaceReader reader = ProjectWorkspaceReader.getInstance();
        File found = reader.findArtifact(artifact("test:test:pom:0.1-SNAPSHOT"));
        assertNull(found);

        found = reader.findArtifact(artifact("test:test:xml:0.1-SNAPSHOT"));
        assertNotNull(found);
        assertEquals(new File(projectDir, "pom1.xml"), found.getAbsoluteFile());
    }

    @Test
    void resolveArtifact() throws Exception {
        executeTarget("testResolveArtifact");
        String prop = getProject().getProperty("resolve.test:test:jar");
        assertEquals(new File(projectDir, "pom1.xml").getAbsolutePath(), prop);
    }

    @Test
    void resolveArtifactInMemoryPom() throws Exception {
        executeTarget("testResolveArtifactInMemoryPom");
        String prop = getProject().getProperty("resolve.test:test:jar");
        assertEquals(new File(projectDir, "pom1.xml").getAbsolutePath(), prop);
        assertLogContaining("The POM for test:test:jar:0.1-SNAPSHOT is missing, no dependency information available");
    }

    @Test
    void resolveVersionRange() throws Exception {
        executeTarget("testResolveVersionRange");
        String prop = getProject().getProperty("resolve.test:test:jar");
        assertEquals(new File(projectDir, "pom1.xml").getAbsolutePath(), prop);
    }
}
