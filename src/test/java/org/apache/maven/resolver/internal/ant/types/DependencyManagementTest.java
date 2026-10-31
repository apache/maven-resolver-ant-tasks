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
package org.apache.maven.resolver.internal.ant.types;

import java.io.File;

import org.apache.maven.resolver.internal.ant.AntBuildsTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DependencyManagementTest extends AntBuildsTest {

    public DependencyManagementTest() {
        super(new File("src/test/resources/ant/DependencyManagement/build.xml"));
    }

    @Test
    public void testDependencyManagement() {
        executeTarget("init");
        DependencyManagement dm = getProject().getReference("dm");
        assertNotNull(dm, "Dependency management with id 'dm' should exists");
        assertEquals(2, dm.getDependencies().getDependencyContainers().size(), "Should have 2 dependencies defined");
        Dependency dep1 =
                (Dependency) dm.getDependencies().getDependencyContainers().get(0);
        assertEquals("se.alipsa.matrix", dep1.getGroupId(), "groupId should match");
        assertEquals("matrix-bom", dep1.getArtifactId(), "artifactId should match");
        assertEquals("2.2.0", dep1.getVersion(), "version should match");
        assertEquals("pom", dep1.getType(), "type should match");
        assertEquals("import", dep1.getScope(), "scope should match");
    }
}
