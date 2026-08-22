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
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.Exclusion;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Checks that a POM contributes to resolution the same way it does under Maven, which is what
 * {@link ConverterUtils#toArtifactDescriptor} delegates to Maven's own code for.
 */
public class ConverterUtilsTest {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(ConverterUtilsTest.class);
    }

    private Project project;

    private Task task;

    @Before
    public void setUp() {
        project = new Project();
        project.setProperty("user.home", System.getProperty("user.home"));
        project.setProperty(
                "maven.repo.local",
                new File(new File("").getAbsoluteFile(), "target/ant/local-repo").getAbsolutePath());

        task = new Task() {};
        task.setProject(project);
    }

    private RepositorySystemSession.CloseableSession newSession() {
        return AntRepoSys.getInstance(project).getSession(task, null);
    }

    private static Model newModel() {
        Model model = new Model();
        model.setModelVersion("4.0.0");
        model.setGroupId("test");
        model.setArtifactId("project");
        model.setVersion("1.0");
        model.setPackaging("jar");
        return model;
    }

    private static org.apache.maven.model.Dependency newDependency(String artifactId) {
        org.apache.maven.model.Dependency dependency = new org.apache.maven.model.Dependency();
        dependency.setGroupId("test");
        dependency.setArtifactId(artifactId);
        dependency.setVersion("1.0");
        dependency.setType("jar");
        dependency.setScope("compile");
        return dependency;
    }

    private static ArtifactDescriptorResult descriptorOf(RepositorySystemSession session, Model model) {
        return ConverterUtils.toArtifactDescriptor(session, model);
    }

    @Test
    public void testDescriptorArtifactIsThePomOfTheModel() {
        Model model = newModel();

        try (RepositorySystemSession.CloseableSession session = newSession()) {
            ArtifactDescriptorResult descriptor = descriptorOf(session, model);

            assertEquals("test", descriptor.getArtifact().getGroupId());
            assertEquals("project", descriptor.getArtifact().getArtifactId());
            assertEquals("pom", descriptor.getArtifact().getExtension());
            assertEquals("1.0", descriptor.getArtifact().getVersion());
        }
    }

    @Test
    public void testOptionalIsCarriedFromTheModel() {
        Model model = newModel();
        org.apache.maven.model.Dependency optional = newDependency("optional-dep");
        optional.setOptional("true");
        model.addDependency(optional);
        model.addDependency(newDependency("plain-dep"));

        try (RepositorySystemSession.CloseableSession session = newSession()) {
            ArtifactDescriptorResult descriptor = descriptorOf(session, model);

            assertEquals(Boolean.TRUE, descriptor.getDependencies().get(0).getOptional());
            assertNull(
                    "a dependency without <optional> must stay unset so that it can still be managed",
                    descriptor.getDependencies().get(1).getOptional());
        }
    }

    @Test
    public void testOptionalIsCarriedFromDependencyManagement() {
        Model model = newModel();
        org.apache.maven.model.Dependency managed = newDependency("managed-dep");
        managed.setOptional("true");
        DependencyManagement dependencyManagement = new DependencyManagement();
        dependencyManagement.addDependency(managed);
        model.setDependencyManagement(dependencyManagement);

        try (RepositorySystemSession.CloseableSession session = newSession()) {
            ArtifactDescriptorResult descriptor = descriptorOf(session, model);

            assertEquals(
                    Boolean.TRUE, descriptor.getManagedDependencies().get(0).getOptional());
        }
    }

    @Test
    public void testTypeIsMappedThroughTheArtifactTypeRegistry() {
        Model model = newModel();
        org.apache.maven.model.Dependency testJar = newDependency("test-jar-dep");
        testJar.setType("test-jar");
        model.addDependency(testJar);

        try (RepositorySystemSession.CloseableSession session = newSession()) {
            Dependency dependency =
                    descriptorOf(session, model).getDependencies().get(0);

            assertEquals("jar", dependency.getArtifact().getExtension());
            assertEquals("tests", dependency.getArtifact().getClassifier());
            assertEquals("compile", dependency.getScope());
        }
    }

    @Test
    public void testExclusionsAreCarriedAsWildcards() {
        Model model = newModel();
        org.apache.maven.model.Dependency dependency = newDependency("excluding-dep");
        org.apache.maven.model.Exclusion exclusion = new org.apache.maven.model.Exclusion();
        exclusion.setGroupId("excluded");
        exclusion.setArtifactId("artifact");
        dependency.addExclusion(exclusion);
        model.addDependency(dependency);

        try (RepositorySystemSession.CloseableSession session = newSession()) {
            Exclusion converted = descriptorOf(session, model)
                    .getDependencies()
                    .get(0)
                    .getExclusions()
                    .iterator()
                    .next();

            assertEquals("excluded", converted.getGroupId());
            assertEquals("artifact", converted.getArtifactId());
            assertEquals("*", converted.getClassifier());
            assertEquals("*", converted.getExtension());
        }
    }

    @Test
    public void testRepositoriesAreCarriedFromTheModel() {
        Model model = newModel();
        Repository repository = new Repository();
        repository.setId("example");
        repository.setUrl("https://example.invalid/repo");
        model.addRepository(repository);

        try (RepositorySystemSession.CloseableSession session = newSession()) {
            RemoteRepository converted =
                    descriptorOf(session, model).getRepositories().get(0);

            assertEquals("example", converted.getId());
            assertEquals("https://example.invalid/repo", converted.getUrl());
        }
    }

    @Test
    public void testVersionlessKeyNormalizesTypeAndClassifier() {
        Model model = newModel();
        org.apache.maven.model.Dependency testJar = newDependency("test-jar-dep");
        testJar.setType("test-jar");
        model.addDependency(testJar);
        model.addDependency(newDependency("plain-dep"));

        try (RepositorySystemSession.CloseableSession session = newSession()) {
            ArtifactDescriptorResult descriptor = descriptorOf(session, model);

            assertEquals(
                    "test:test-jar-dep:jar:tests",
                    ConverterUtils.versionlessKey(
                            descriptor.getDependencies().get(0).getArtifact()));
            assertEquals(
                    "test:plain-dep:jar",
                    ConverterUtils.versionlessKey(
                            descriptor.getDependencies().get(1).getArtifact()));
        }
    }

    @Test
    public void testVersionlessKeyOfAntDependencyMatchesTheModelKey() {
        Model model = newModel();
        org.apache.maven.model.Dependency testJar = newDependency("test-jar-dep");
        testJar.setType("test-jar");
        model.addDependency(testJar);

        org.apache.maven.resolver.internal.ant.types.Dependency antDependency =
                new org.apache.maven.resolver.internal.ant.types.Dependency();
        antDependency.setGroupId("test");
        antDependency.setArtifactId("test-jar-dep");
        antDependency.setVersion("1.0");
        antDependency.setType("test-jar");

        try (RepositorySystemSession.CloseableSession session = newSession()) {
            ArtifactDescriptorResult descriptor = descriptorOf(session, model);

            assertTrue(
                    "an Ant <dependency> must key the same as the POM dependency it overrides",
                    ConverterUtils.versionlessKey(antDependency, session)
                            .equals(ConverterUtils.versionlessKey(
                                    descriptor.getDependencies().get(0).getArtifact())));
        }
    }
}
