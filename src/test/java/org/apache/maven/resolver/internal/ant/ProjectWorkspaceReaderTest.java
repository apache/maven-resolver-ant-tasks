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

import junit.framework.JUnit4TestAdapter;
import org.apache.maven.resolver.internal.ant.types.Pom;
import org.apache.tools.ant.Project;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ProjectWorkspaceReaderTest {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(ProjectWorkspaceReaderTest.class);
    }

    private ProjectWorkspaceReader reader;

    private Project project;

    @Before
    public void setUp() throws Exception {
        this.reader = new ProjectWorkspaceReader();

        this.project = new Project();
        project.setProperty("user.home", System.getProperty("user.home"));
    }

    private Artifact artifact(String coords) {
        return new DefaultArtifact(coords);
    }

    private File getFile(String name) {
        return new File("src/test/resources/ProjectWorkspaceReader", name);
    }

    @Test
    public void testFindPom() {
        Pom pom = new Pom();
        pom.setProject(project);
        pom.setFile(getFile("dummy-pom.xml"));

        reader.addPom(pom);

        assertEquals(pom.getFile(), reader.findArtifact(artifact("test:dummy:pom:0.1-SNAPSHOT")));
        assertNull(reader.findArtifact(artifact("unavailable:test:pom:0.1-SNAPSHOT")));
    }

    @Test
    public void testFindArtifact() {
        Pom pom = new Pom();
        pom.setProject(project);
        pom.setFile(getFile("dummy-pom.xml"));

        reader.addPom(pom);

        org.apache.maven.resolver.internal.ant.types.Artifact artifact =
                new org.apache.maven.resolver.internal.ant.types.Artifact();
        artifact.setProject(project);
        artifact.addPom(pom);
        artifact.setFile(getFile("dummy-file.txt"));

        reader.addArtifact(artifact);

        assertEquals(artifact.getFile(), reader.findArtifact(artifact("test:dummy:txt:0.1-SNAPSHOT")));
        assertNull(reader.findArtifact(artifact("unavailable:test:jar:0.1-SNAPSHOT")));
    }

    @Test
    public void testFindVersions() {
        Pom pom1 = new Pom();
        pom1.setProject(project);
        pom1.setCoords("test:dummy:1-SNAPSHOT");

        org.apache.maven.resolver.internal.ant.types.Artifact artifact1 =
                new org.apache.maven.resolver.internal.ant.types.Artifact();
        artifact1.setProject(project);
        artifact1.addPom(pom1);
        artifact1.setFile(getFile("dummy-file.txt"));

        reader.addArtifact(artifact1);

        Pom pom2 = new Pom();
        pom2.setProject(project);
        pom2.setCoords("test:dummy:2-SNAPSHOT");

        org.apache.maven.resolver.internal.ant.types.Artifact artifact2 =
                new org.apache.maven.resolver.internal.ant.types.Artifact();
        artifact2.setProject(project);
        artifact2.addPom(pom2);
        artifact2.setFile(getFile("dummy-file.txt"));

        reader.addArtifact(artifact2);

        assertThat(
                reader.findVersions(artifact("test:dummy:txt:[0,)")), containsInAnyOrder("1-SNAPSHOT", "2-SNAPSHOT"));
    }
}
