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
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import junit.framework.JUnit4TestAdapter;
import org.apache.tools.ant.Project;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AetherUtilsTest {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(AetherUtilsTest.class);
    }

    private static final String MAVEN_HOME_PROP = "maven.home";

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() {
        System.clearProperty(MAVEN_HOME_PROP);
    }

    @After
    public void tearDown() {
        System.clearProperty(MAVEN_HOME_PROP);
    }

    private Project newProject() {
        Project project = new Project();
        project.setProperty("user.home", System.getProperty("user.home"));
        return project;
    }

    /**
     * The {@code maven.home} Ant property must win over the system property and the environment variables.
     */
    @Test
    public void testGetMavenHomePrefersAntProperty() {
        Project project = newProject();
        project.setProperty(MAVEN_HOME_PROP, "/ant/maven");
        System.setProperty(MAVEN_HOME_PROP, "/system/maven");

        assertEquals("/ant/maven", AetherUtils.getMavenHome(project));
    }

    /**
     * The {@code maven.home} system property must be used when the Ant property is not set.
     */
    @Test
    public void testGetMavenHomeFallsBackToSystemProperty() {
        System.setProperty(MAVEN_HOME_PROP, "/system/maven");

        assertEquals("/system/maven", AetherUtils.getMavenHome(newProject()));
    }

    /**
     * The {@code MAVEN_HOME} environment variable must win over {@code M2_HOME}.
     */
    @Test
    public void testGetMavenHomeFallsBackToMavenHomeEnv() {
        Map<String, String> env = new HashMap<>();
        env.put("MAVEN_HOME", "/env/maven");
        env.put("M2_HOME", "/env/m2");

        assertEquals("/env/maven", AetherUtils.getMavenHome(newProject(), env));
    }

    /**
     * The {@code M2_HOME} environment variable is the last fallback.
     */
    @Test
    public void testGetMavenHomeFallsBackToM2HomeEnv() {
        Map<String, String> env = new HashMap<>();
        env.put("M2_HOME", "/env/m2");

        assertEquals("/env/m2", AetherUtils.getMavenHome(newProject(), env));
    }

    /**
     * The Maven {@code conf} directory must take precedence over the Ant {@code etc} directory.
     *
     * @throws Exception in case of problems
     */
    @Test
    public void testFindGlobalSettingsPrefersMavenConf() throws Exception {
        File mavenSettings = writeSettings(folder.newFolder("maven-home"), "conf");
        File antSettings = writeSettings(folder.newFolder("ant-home"), "etc");

        Project project = newProject();
        project.setProperty(
                "ant.home", antSettings.getParentFile().getParentFile().getAbsolutePath());
        project.setProperty(
                MAVEN_HOME_PROP, mavenSettings.getParentFile().getParentFile().getAbsolutePath());

        assertEquals(
                mavenSettings.getAbsolutePath(),
                AetherUtils.findGlobalSettings(project).getAbsolutePath());
    }

    /**
     * The Ant {@code etc} directory must be used when no Maven home resolves to an existing settings file.
     *
     * @throws Exception in case of problems
     */
    @Test
    public void testFindGlobalSettingsFallsBackToAntEtc() throws Exception {
        File antSettings = writeSettings(folder.newFolder("ant-home"), "etc");

        Project project = newProject();
        project.setProperty(
                "ant.home", antSettings.getParentFile().getParentFile().getAbsolutePath());

        assertEquals(
                antSettings.getAbsolutePath(),
                AetherUtils.findGlobalSettings(project).getAbsolutePath());
    }

    /**
     * No settings file must be returned when neither location holds one.
     */
    @Test
    public void testFindGlobalSettingsReturnsNullWhenNotFound() {
        Project project = newProject();
        project.setProperty("ant.home", folder.getRoot().getAbsolutePath());

        assertNull(AetherUtils.findGlobalSettings(project));
    }

    private File writeSettings(File home, String dirName) throws Exception {
        File dir = new File(home, dirName);
        dir.mkdirs();
        File settings = new File(dir, Names.SETTINGS_XML);
        Files.write(settings.toPath(), new byte[0]);
        return settings;
    }
}
