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

import junit.framework.JUnit4TestAdapter;
import org.apache.tools.ant.Project;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertTrue;

public class CreatePomNoParentDirTest {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(CreatePomNoParentDirTest.class);
    }

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testPomTargetWithoutParentDirectory() throws Exception {
        File baseDir = tempFolder.newFolder("base");

        Project project = new Project();
        project.setBaseDir(baseDir);
        project.setProperty("groupId", "g");
        project.setProperty("artifactId", "a");
        project.setProperty("version", "1.0");

        CreatePom task = new CreatePom();
        task.setProject(project);
        task.setPomTarget("pom.xml");
        task.setSkipPomRegistration(true);

        task.execute();

        File expected = new File(baseDir, "pom.xml");
        assertTrue("bare pomTarget should write the POM to the project base dir: " + expected, expected.exists());
    }
}
