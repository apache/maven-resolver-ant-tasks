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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import junit.framework.JUnit4TestAdapter;
import org.apache.maven.resolver.internal.ant.types.Dependencies;
import org.apache.maven.resolver.internal.ant.types.Pom;
import org.apache.maven.resolver.internal.ant.types.RemoteRepositories;
import org.apache.maven.resolver.internal.ant.types.RemoteRepository;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.graph.visitor.NodeListGenerator;
import org.eclipse.aether.util.graph.visitor.PreorderDependencyNodeConsumerVisitor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Resolves a POM through the Ant tasks and compares the outcome with what {@code mvn dependency:list}
 * reports for the same POM, per classpath scope.
 * <p>
 * The expectations next to the fixture POM are recorded Maven output, not hand-written; their headers carry
 * the Maven version they came from and the command that re-records them. A difference here is a difference
 * between the two tools, which is the thing worth failing a build over.
 */
public class MavenParityTest {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(MavenParityTest.class);
    }

    private static final File FIXTURE_DIR =
            new File(new File("").getAbsoluteFile(), "src/test/resources/ant/MavenParity");

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

    @Test
    public void testCompileClasspathMatchesMaven() throws IOException {
        assertParity("compile");
    }

    @Test
    public void testRuntimeClasspathMatchesMaven() throws IOException {
        assertParity("runtime");
    }

    @Test
    public void testTestClasspathMatchesMaven() throws IOException {
        assertParity("test");
    }

    private void assertParity(String classpath) throws IOException {
        List<String> expected = readExpectation(classpath);
        List<String> actual = resolve(classpath);

        assertEquals(
                "the Ant tasks and Maven disagree about the " + classpath + " classpath of "
                        + new File(FIXTURE_DIR, "pom.xml"),
                String.join("\n", expected),
                String.join("\n", actual));
    }

    private List<String> readExpectation(String classpath) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String line : Files.readAllLines(
                new File(FIXTURE_DIR, "expected-" + classpath + ".txt").toPath(), StandardCharsets.UTF_8)) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                lines.add(line);
            }
        }
        return lines;
    }

    private List<String> resolve(String classpath) {
        Pom pom = new Pom();
        pom.setProject(project);
        pom.setFile(new File(FIXTURE_DIR, "pom.xml"));

        Dependencies dependencies = new Dependencies();
        dependencies.setProject(project);
        dependencies.addPom(pom);

        CollectResult result =
                AntRepoSys.getInstance(project).collectDependencies(task, dependencies, null, centralOnly());

        DependencyFilter filter = DependencyFilterUtils.classpathFilter(classpath);
        NodeListGenerator generator = new NodeListGenerator();
        result.getRoot().accept(new PreorderDependencyNodeConsumerVisitor(generator, filter));

        // a sorted set: dependency:list reports each artifact once, in no order this test should depend on
        TreeSet<String> coordinates = new TreeSet<>();
        for (DependencyNode node : generator.getNodesWithDependencies()) {
            coordinates.add(coordinates(node));
        }
        return new ArrayList<>(coordinates);
    }

    private static String coordinates(DependencyNode node) {
        Artifact artifact = node.getArtifact();
        StringBuilder buffer = new StringBuilder(128);
        buffer.append(artifact.getGroupId())
                .append(':')
                .append(artifact.getArtifactId())
                .append(':')
                .append(artifact.getExtension());
        if (!artifact.getClassifier().isEmpty()) {
            buffer.append(':').append(artifact.getClassifier());
        }
        buffer.append(':')
                .append(artifact.getVersion())
                .append(':')
                .append(node.getDependency().getScope());
        return buffer.toString();
    }

    private RemoteRepositories centralOnly() {
        RemoteRepository central = new RemoteRepository();
        central.setProject(project);
        central.setId("central");
        central.setUrl("https://repo.maven.apache.org/maven2/");
        central.setReleases(true);
        central.setSnapshots(false);

        RemoteRepositories repositories = new RemoteRepositories();
        repositories.setProject(project);
        repositories.addRemoterepo(central);
        return repositories;
    }
}
