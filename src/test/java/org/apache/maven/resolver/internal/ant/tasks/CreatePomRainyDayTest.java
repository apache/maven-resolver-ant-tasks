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
import java.io.PrintStream;

import junit.framework.JUnit4TestAdapter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileRule;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class CreatePomRainyDayTest {

    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(CreatePomRainyDayTest.class);
    }

    @Rule
    public final BuildFileRule buildRule = new BuildFileRule();

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

    /**
     * Test that the task fails if no groupId is specified and the pom task is called (default).
     * Since the failure happens when the task registers the POM, the user can still create the POM
     * if the skipPomRegistration property is set to true.
     */
    @Test
    public void testNoGroupId() {
        configureProject("target/test-classes/ant/createPom/noGroupId.xml", Project.MSG_VERBOSE);
        // Expect a BuildException when trying to execute the target
        Assert.assertThrows(BuildException.class, () -> buildRule.executeTarget("setup"));

        // This should work since the setSkipPomRegistration property is set to true
        configureProject("target/test-classes/ant/createPom/noGroupIdSkipRegistration.xml", Project.MSG_VERBOSE);
        buildRule.executeTarget("setup");
        File pomFile = getPomFile();
        Assert.assertTrue("The pom file should have been created at " + pomFile, pomFile.exists());
    }

    /**
     * Test that the task fails if no artifactId is specified and the pom task is called (default).
     * Since the failure happens when the task registers the POM, the user can still create the POM
     * if the skipPomRegistration property is set to true.
     */
    @Test
    public void testNoArtifactId() {
        configureProject("target/test-classes/ant/createPom/noArtifactId.xml", Project.MSG_VERBOSE);
        // Expect a BuildException when trying to execute the target
        Assert.assertThrows(BuildException.class, () -> buildRule.executeTarget("setup"));

        // This should work since the setSkipPomRegistration property is set to true
        configureProject("target/test-classes/ant/createPom/noArtifactIdSkipRegistration.xml", Project.MSG_VERBOSE);
        buildRule.executeTarget("setup");
        File pomFile = getPomFile();
        Assert.assertTrue("The pom file should have been created at " + pomFile, pomFile.exists());
    }

    private File getPomFile() {
        Project project = buildRule.getProject();
        String pomPath = project.replaceProperties(project.getProperty("pomFile"));
        Assert.assertNotNull("pomFile property should not be null", pomPath);
        return new File(pomPath);
    }
}
