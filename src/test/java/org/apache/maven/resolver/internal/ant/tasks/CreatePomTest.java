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

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.Scm;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.resolver.internal.ant.AntBuildsTest;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreatePomTest extends AntBuildsTest {

    public CreatePomTest() {
        super(new File("target/test-classes/ant/DependencyManagement/build.xml"));
    }

    @Test
    public void testCreatePom() throws IOException, XmlPullParserException {
        executeTarget("setup");
        String pomPath = getProject().getProperty("pomFile");

        Assertions.assertNotNull(pomPath, "pomFile property should not be null");
        File pomFile = new File(pomPath);

        Model model = readPomFile(pomFile);
        Assertions.assertNotNull(model, "Model should not be null");
        assertEquals("4.0.0", model.getModelVersion(), "modelVersion");
        assertEquals("test.resolver.dm", model.getGroupId(), "groupId");
        assertEquals("dependency-management", model.getArtifactId(), "artifactId");
        assertEquals("1.0-SNAPSHOT", model.getVersion(), "version");
        DependencyManagement dm = model.getDependencyManagement();
        List<Dependency> dependencies = dm.getDependencies();
        assertEquals(2, dependencies.size(), "dependencies should have 2 entries");
        Dependency matrix = dependencies.get(0);
        assertEquals("se.alipsa.matrix", matrix.getGroupId(), "matrix dependency groupId");
        assertEquals("matrix-bom", matrix.getArtifactId(), "matrix dependency artifactId");
        assertEquals("2.2.0", matrix.getVersion(), "matrix dependency version");
        assertEquals("pom", matrix.getType(), "matrix dependency type");
        assertEquals("import", matrix.getScope(), "matrix dependency scope");

        License license = model.getLicenses().get(0);
        assertEquals("The Apache Software License, Version 2.0", license.getName(), "license name should match");
        assertEquals("http://www.apache.org/licenses/LICENSE-2.0.txt", license.getUrl(), "license url should match");
        assertEquals("repo", license.getDistribution(), "license distribution should match");

        Repository repo = model.getRepositories().get(0);
        assertEquals("my-internal-site", repo.getId(), "repository id should match");
        assertEquals("https://myserver/repo", repo.getUrl(), "repository url should match");
        assertEquals("default", repo.getLayout(), "repository layout should match");
        assertTrue(repo.getSnapshots().isEnabled(), "repository snapshots should be enabled");
        assertTrue(repo.getReleases().isEnabled(), "repository releases should be enabled");

        List<Developer> developers = model.getDevelopers();
        assertEquals(1, developers.size(), "developers should have 1 entry");
        Developer developer = developers.get(0);
        assertEquals("jdoe", developer.getId(), "developer id should match");
        assertEquals("John Doe", developer.getName(), "developer name should match");
        assertEquals("jdoe@example.com", developer.getEmail(), "developer email should match");
        assertEquals("http://www.example.com/jdoe", developer.getUrl(), "developer url should match");
        assertEquals("ACME", developer.getOrganization(), "developer organization should match");
        assertEquals(
                "http://www.example.com", developer.getOrganizationUrl(), "developers organizationUrl should match");
        assertEquals(2, developer.getRoles().size(), "developer should have 2 roles");
        assertTrue(developer.getRoles().contains("architect"), "developer should have an architect role");
        assertTrue(developer.getRoles().contains("developer"), "developer should have a developer role");
        assertEquals("America/New_York", developer.getTimezone(), "developer timezone should match");

        Scm scm = model.getScm();
        assertEquals("scm:svn:http://127.0.0.1/svn/my-project", scm.getConnection(), "Scm connection should match");
        assertEquals(
                "scm:svn:https://127.0.0.1/svn/my-project",
                scm.getDeveloperConnection(),
                "Scm developerConnection should match");
        assertEquals("http://127.0.0.1/websvn/my-project", scm.getUrl(), "Scm url should match");
    }

    public static Model readPomFile(File pomFile) throws IOException, XmlPullParserException {
        assertTrue(pomFile.exists(), "pomFile (" + pomFile + ") should exist");
        try (Reader reader = new FileReader(pomFile)) {
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            return pomReader.read(reader);
        }
    }
}
