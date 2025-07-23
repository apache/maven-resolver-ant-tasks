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

import junit.framework.JUnit4TestAdapter;
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
import org.junit.Assert;
import org.junit.Test;

public class CreatePomTest extends AntBuildsTest {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(CreatePomTest.class);
    }

    public CreatePomTest() {
        super(new File("target/test-classes/ant/DependencyManagement/build.xml"));
    }

    @Test
    public void testCreatePom() throws IOException, XmlPullParserException {
        executeTarget("setup");
        String pomPath = getProject().getProperty("pomFile");

        Assert.assertNotNull("pomFile property should not be null", pomPath);
        File pomFile = new File(pomPath);

        Model model = readPomFile(pomFile);
        Assert.assertNotNull("Model should not be null", model);
        Assert.assertEquals("modelVersion", "4.0.0", model.getModelVersion());
        Assert.assertEquals("groupId", "test.resolver.dm", model.getGroupId());
        Assert.assertEquals("artifactId", "dependency-management", model.getArtifactId());
        Assert.assertEquals("version", "1.0-SNAPSHOT", model.getVersion());
        DependencyManagement dm = model.getDependencyManagement();
        List<Dependency> dependencies = dm.getDependencies();
        Assert.assertEquals("dependencies should have 2 entries", 2, dependencies.size());
        Dependency matrix = dependencies.get(0);
        Assert.assertEquals("matrix dependency groupId", "se.alipsa.matrix", matrix.getGroupId());
        Assert.assertEquals("matrix dependency artifactId", "matrix-bom", matrix.getArtifactId());
        Assert.assertEquals("matrix dependency version", "2.2.0", matrix.getVersion());
        Assert.assertEquals("matrix dependency type", "pom", matrix.getType());
        Assert.assertEquals("matrix dependency scope", "import", matrix.getScope());

        License license = model.getLicenses().get(0);
        Assert.assertEquals("license name should match", "The Apache Software License, Version 2.0", license.getName());
        Assert.assertEquals(
                "license url should match", "http://www.apache.org/licenses/LICENSE-2.0.txt", license.getUrl());
        Assert.assertEquals("license distribution should match", "repo", license.getDistribution());

        Repository repo = model.getRepositories().get(0);
        Assert.assertEquals("repository id should match", "my-internal-site", repo.getId());
        Assert.assertEquals("repository url should match", "https://myserver/repo", repo.getUrl());
        Assert.assertEquals("repository layout should match", "default", repo.getLayout());
        Assert.assertTrue(
                "repository snapshots should be enabled", repo.getSnapshots().isEnabled());
        Assert.assertTrue(
                "repository releases should be enabled", repo.getReleases().isEnabled());

        List<Developer> developers = model.getDevelopers();
        Assert.assertEquals("developers should have 1 entry", 1, developers.size());
        Developer developer = developers.get(0);
        Assert.assertEquals("developer id should match", "jdoe", developer.getId());
        Assert.assertEquals("developer name should match", "John Doe", developer.getName());
        Assert.assertEquals("developer email should match", "jdoe@example.com", developer.getEmail());
        Assert.assertEquals("developer url should match", "http://www.example.com/jdoe", developer.getUrl());
        Assert.assertEquals("developer organization should match", "ACME", developer.getOrganization());
        Assert.assertEquals(
                "developers organizationUrl should match", "http://www.example.com", developer.getOrganizationUrl());
        Assert.assertEquals(
                "developer should have 2 roles", 2, developer.getRoles().size());
        Assert.assertTrue(
                "developer should have an architect role", developer.getRoles().contains("architect"));
        Assert.assertTrue(
                "developer should have a developer role", developer.getRoles().contains("developer"));
        Assert.assertEquals("developer timezone should match", "America/New_York", developer.getTimezone());

        Scm scm = model.getScm();
        Assert.assertEquals(
                "Scm connection should match", "scm:svn:http://127.0.0.1/svn/my-project", scm.getConnection());
        Assert.assertEquals(
                "Scm developerConnection should match",
                "scm:svn:https://127.0.0.1/svn/my-project",
                scm.getDeveloperConnection());
        Assert.assertEquals("Scm url should match", "http://127.0.0.1/websvn/my-project", scm.getUrl());
    }

    public static Model readPomFile(File pomFile) throws IOException, XmlPullParserException {
        Assert.assertTrue("pomFile (" + pomFile + ") should exist", pomFile.exists());
        try (Reader reader = new FileReader(pomFile)) {
            MavenXpp3Reader pomReader = new MavenXpp3Reader();
            return pomReader.read(reader);
        }
    }
}
