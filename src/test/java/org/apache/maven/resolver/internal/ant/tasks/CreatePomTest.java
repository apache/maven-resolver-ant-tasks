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
package org.apache.maven.resolver.internal.ant.tasks;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import org.apache.maven.model.*;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.resolver.internal.ant.AntBuildsTest;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.Test;

import static org.junit.Assert.*;

public class CreatePomTest extends AntBuildsTest {
    public CreatePomTest() {
        super(new File("target/test-classes/ant/DependencyManagement/build.xml"));
    }

    @Test
    public void testCreatePom() throws IOException, XmlPullParserException {
        executeTarget("setup");
        String pomPath = getProject().getProperty("pomFile");

        assertNotNull("pomFile property should not be null", pomPath);
        File pomFile = new File(pomPath);

        Model model = readPomFile(pomFile);
        assertNotNull("Model should not be null", model);
        assertEquals("modelVersion", "4.0.0", model.getModelVersion());
        assertEquals("groupId", "test.resolver.dm", model.getGroupId());
        assertEquals("artifactId", "dependency-management", model.getArtifactId());
        assertEquals("version", "1.0-SNAPSHOT", model.getVersion());
        DependencyManagement dm = model.getDependencyManagement();
        List<Dependency> dependencies = dm.getDependencies();
        assertEquals("dependencies should have 2 entries", 2, dependencies.size());
        Dependency matrix = dependencies.get(0);
        assertEquals("matrix dependency groupId", "se.alipsa.matrix", matrix.getGroupId());
        assertEquals("matrix dependency artifactId", "matrix-bom", matrix.getArtifactId());
        assertEquals("matrix dependency version", "2.2.0", matrix.getVersion());
        assertEquals("matrix dependency type", "pom", matrix.getType());
        assertEquals("matrix dependency scope", "import", matrix.getScope());

        License license = model.getLicenses().get(0);
        assertEquals("license name should match", "The Apache Software License, Version 2.0", license.getName());
        assertEquals("license url should match", "http://www.apache.org/licenses/LICENSE-2.0.txt", license.getUrl());
        assertEquals("license distribution should match", "repo", license.getDistribution());

        Repository repo = model.getRepositories().get(0);
        assertEquals("repository id should match", "my-internal-site", repo.getId());
        assertEquals("repository url should match", "https://myserver/repo", repo.getUrl());
        assertEquals("repository layout should match", "default", repo.getLayout());
        assertTrue("repository snapshots should be enabled", repo.getSnapshots().isEnabled());
        assertTrue("repository releases should be enabled", repo.getReleases().isEnabled());
    }

    public static Model readPomFile(File pomFile) throws IOException, XmlPullParserException {
        assertTrue("pomFile (" + pomFile + ") should exist", pomFile.exists());
        try (Reader reader = new FileReader(pomFile)) {
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            return pomReader.read(reader);
        }
    }
}
