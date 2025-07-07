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

import junit.framework.JUnit4TestAdapter;
import org.apache.maven.resolver.internal.ant.AntBuildsTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DependencyManagementTest extends AntBuildsTest {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(DependencyManagementTest.class);
    }

    public DependencyManagementTest() {
        super(new File("src/test/resources/ant/DependencyManagement/build.xml"));
    }

    @Test
    public void testDependencyManagement() {
        executeTarget("init");
        DependencyManagement dm = getProject().getReference("dm");
        assertNotNull("Dependency management with id 'dm' should exists", dm);
        assertEquals(
                "Should have 2 dependencies defined",
                2,
                dm.getDependencies().getDependencyContainers().size());
        Dependency dep1 =
                (Dependency) dm.getDependencies().getDependencyContainers().get(0);
        assertEquals("groupId should match", "se.alipsa.matrix", dep1.getGroupId());
        assertEquals("artifactId should match", "matrix-bom", dep1.getArtifactId());
        assertEquals("version should match", "2.2.0", dep1.getVersion());
        assertEquals("type should match", "pom", dep1.getType());
        assertEquals("scope should match", "import", dep1.getScope());
    }
}
