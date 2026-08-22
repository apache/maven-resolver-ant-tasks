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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.util.graph.manager.ClassicDependencyManager;
import org.eclipse.aether.util.graph.manager.TransitiveDependencyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class AntRepoSysTest {
    private Project project;

    private Task task;

    @BeforeEach
    void setUp() {
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

    /**
     * Resolver 2 ships a transitive dependency manager, which Maven 4 enables by default and Maven 3.10 does not.
     * The tasks follow Maven 3 unless asked otherwise, so that the upgrade to resolver 2 does not silently change
     * anyone's dependency tree.
     */
    @Test
    void dependencyManagerIsClassicByDefault() {
        try (RepositorySystemSession.CloseableSession session = newSession()) {
            assertInstanceOf(
                    ClassicDependencyManager.class,
                    session.getDependencyManager(),
                    "expected the classic dependency manager, got " + session.getDependencyManager());
        }
    }

    @Test
    void dependencyManagerTransitivityCanBeEnabled() {
        project.setProperty(Names.PROPERTY_DEPENDENCY_MANAGER_TRANSITIVITY, "true");

        try (RepositorySystemSession.CloseableSession session = newSession()) {
            assertInstanceOf(
                    TransitiveDependencyManager.class,
                    session.getDependencyManager(),
                    "expected the transitive dependency manager, got " + session.getDependencyManager());
        }
    }

    @Test
    void dependencyManagerTransitivityOffIsClassic() {
        project.setProperty(Names.PROPERTY_DEPENDENCY_MANAGER_TRANSITIVITY, "false");

        try (RepositorySystemSession.CloseableSession session = newSession()) {
            assertInstanceOf(
                    ClassicDependencyManager.class,
                    session.getDependencyManager(),
                    "expected the classic dependency manager, got " + session.getDependencyManager());
        }
    }
}
