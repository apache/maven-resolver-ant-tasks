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
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Reference;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SettingsReferenceTest {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(SettingsReferenceTest.class);
    }

    private Project project;

    private Settings referenced;

    private File userFile;

    private File globalFile;

    @Before
    public void setUp() {
        project = new Project();
        userFile = new File("user-settings.xml");
        globalFile = new File("global-settings.xml");

        referenced = new Settings();
        referenced.setProject(project);
        referenced.setFile(userFile);
        referenced.setGlobalFile(globalFile);

        project.addReference("settings-id", referenced);
    }

    /**
     * A referenced {@code <settings>} must return the referenced instance's global settings file, not its user
     * settings file.
     */
    @Test
    public void testGlobalFileDelegatesToReferencedGlobalFile() {
        Settings ref = new Settings();
        ref.setProject(project);
        ref.setRefid(new Reference(project, "settings-id"));

        assertEquals("global file should come from the referenced settings", globalFile, ref.getGlobalFile());
    }

    /**
     * The user settings file delegation of a referenced {@code <settings>} must keep working.
     */
    @Test
    public void testFileDelegatesToReferencedFile() {
        Settings ref = new Settings();
        ref.setProject(project);
        ref.setRefid(new Reference(project, "settings-id"));

        assertEquals("user file should come from the referenced settings", userFile, ref.getFile());
    }
}
