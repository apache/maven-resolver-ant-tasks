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
import java.util.List;

import junit.framework.JUnit4TestAdapter;
import org.apache.maven.resolver.internal.ant.types.Dependencies;
import org.apache.maven.resolver.internal.ant.types.Pom;
import org.apache.maven.resolver.internal.ant.types.RemoteRepositories;
import org.apache.maven.resolver.internal.ant.types.RemoteRepository;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.aether.collection.CollectResult;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Checks that the repositories a POM declares end up in the collect request, behind the ones configured
 * on the Ant side.
 */
public class PomRepositoriesTest {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(PomRepositoriesTest.class);
    }

    private static final File FIXTURE_DIR =
            new File(new File("").getAbsoluteFile(), "src/test/resources/ant/PomRepositories");

    private static final String ANT_SIDE_URL = "https://example.invalid/ant";

    private static final String POM_SIDE_URL = "https://example.invalid/repo";

    private static final String SUPER_POM_URL = "https://repo.maven.apache.org/maven2";

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

        // an empty settings.xml: a mirror in the developer's own would rewrite what this test asserts on
        File settings = new File(FIXTURE_DIR, "settings.xml");
        AntRepoSys.getInstance(project).setUserSettings(settings);
        AntRepoSys.getInstance(project).setGlobalSettings(settings);
    }

    @Test
    public void testPomRepositoriesAreCollectedAgainstBehindTheConfiguredOnes() {
        Pom pom = new Pom();
        pom.setProject(project);
        pom.setFile(new File(FIXTURE_DIR, "pom.xml"));

        Dependencies dependencies = new Dependencies();
        dependencies.setProject(project);
        dependencies.addPom(pom);

        CollectResult result =
                AntRepoSys.getInstance(project).collectDependencies(task, dependencies, null, antSideRepository());

        List<org.eclipse.aether.repository.RemoteRepository> repositories =
                result.getRequest().getRepositories();

        assertEquals(
                "expected the Ant repository, the POM repository and the one the super POM adds, got " + repositories,
                3,
                repositories.size());
        assertEquals(
                "the repositories configured on the Ant side must stay dominant",
                ANT_SIDE_URL,
                repositories.get(0).getUrl());
        assertEquals(POM_SIDE_URL, repositories.get(1).getUrl());
        assertEquals(
                "a POM carries Maven Central through the super POM, as it does under Maven",
                SUPER_POM_URL,
                repositories.get(2).getUrl());
    }

    private RemoteRepositories antSideRepository() {
        RemoteRepository antSide = new RemoteRepository();
        antSide.setProject(project);
        antSide.setId("from-the-ant-build");
        antSide.setUrl(ANT_SIDE_URL);

        RemoteRepositories repositories = new RemoteRepositories();
        repositories.setProject(project);
        repositories.addRemoterepo(antSide);
        return repositories;
    }
}
