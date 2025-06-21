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
import java.io.PrintStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileRule;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.eclipse.aether.internal.test.util.TestFileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import static org.junit.Assert.assertTrue;

public abstract class AntBuildsTest {

    private static final File BASE_DIR;

    protected static final File BUILD_DIR;
    protected File buildFile;

    static {
        System.setProperty("aether.metadataResolver.threads", "1");
        System.setProperty("aether.connector.basic.threads", "1");
        BASE_DIR = new File("").getAbsoluteFile();
        BUILD_DIR = new File(BASE_DIR, "target/ant");
    }

    public AntBuildsTest() {
        projectDir = new File(new File(BASE_DIR, "src/test/resources/ant"), getProjectDirName());
        buildFile = new File(projectDir, "ant.xml");
    }

    public AntBuildsTest(File projectFile) {
        projectDir = projectFile.getParentFile();
        buildFile = projectFile;
    }

    @Rule
    public final BuildFileRule buildRule = new BuildFileRule();

    protected File projectDir;

    protected File localRepoDir;

    protected File distRepoDir;

    protected String getProjectDirName() {
        String name = getClass().getSimpleName();
        if (name.endsWith("Test")) {
            name = name.substring(0, name.length() - 4);
        }
        return name;
    }

    protected void setUpProperties() throws Exception {
        // hook for subclasses to set further system properties for the project to pick up
    }

    @Before
    public void setUp() throws Exception {
        TestFileUtils.deleteFile(BUILD_DIR);

        localRepoDir = new File(BUILD_DIR, "local-repo");
        distRepoDir = new File(BUILD_DIR, "dist-repo");

        System.setProperty("project.dir", projectDir.getAbsolutePath());
        System.setProperty("build.dir", BUILD_DIR.getAbsolutePath());
        System.setProperty("maven.repo.local", localRepoDir.getAbsolutePath());
        System.setProperty("project.distrepo.url", distRepoDir.toURI().toASCIIString());
        setUpProperties();

        configureProject(buildFile.getAbsolutePath(), Project.MSG_VERBOSE);
    }

    @After
    public void tearDown() throws Exception {
        ProjectWorkspaceReader.dropInstance();
        TestFileUtils.deleteFile(BUILD_DIR);
    }

    public void configureProject(String filename, int logLevel) throws BuildException {
        buildRule.configureProject(filename, logLevel);
        DefaultLogger logger = new DefaultLogger() {
            @Override
            protected void printMessage(String message, PrintStream stream, int priority) {
                message = System.currentTimeMillis() + " " + message;
                super.printMessage(message, stream, priority);
            }
        };
        logger.setMessageOutputLevel(logLevel);
        logger.setOutputPrintStream(System.out);
        logger.setErrorPrintStream(System.err);
        buildRule.getProject().addBuildListener(logger);
    }

    protected void assertLogContaining(String substring) {
        String realLog = getLog();
        assertTrue(
                "expecting log to contain \"" + substring + "\" log was \"" + realLog + "\"",
                realLog.contains(substring));
    }

    protected void executeTarget(String targetName) {
        buildRule.executeTarget(targetName);
    }

    protected String getLog() {
        return buildRule.getLog();
    }

    protected Project getProject() {
        return buildRule.getProject();
    }
}
