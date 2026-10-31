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

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;

/**
 * Loads an Ant build file into a {@link Project}, runs its targets and records the log. It stands in for Ant's own
 * {@code BuildFileRule}, which is a JUnit 4 rule and would keep JUnit 4 on the test classpath.
 */
public class AntBuildFile {

    private final StringBuffer log = new StringBuffer();

    private Project project;

    /**
     * Configures the project from the given build file.
     *
     * @param filename the path of the build file
     * @param logLevel the most verbose {@link Project} message level to record
     * @throws BuildException if the build file cannot be parsed
     */
    public void configureProject(String filename, int logLevel) throws BuildException {
        log.setLength(0);
        project = new Project();
        project.init();
        project.addBuildListener(new BuildListener() {
            @Override
            public void buildStarted(BuildEvent event) {}

            @Override
            public void buildFinished(BuildEvent event) {}

            @Override
            public void targetStarted(BuildEvent event) {}

            @Override
            public void targetFinished(BuildEvent event) {}

            @Override
            public void taskStarted(BuildEvent event) {}

            @Override
            public void taskFinished(BuildEvent event) {}

            @Override
            public void messageLogged(BuildEvent event) {
                int priority = event.getPriority();
                if (priority <= logLevel && priority <= Project.MSG_INFO) {
                    log.append(event.getMessage());
                }
            }
        });
        ProjectHelper.configureProject(project, new File(filename));
    }

    /**
     * Runs a target of the configured project.
     *
     * @param targetName the target to run
     */
    public void executeTarget(String targetName) {
        log.setLength(0);
        project.executeTarget(targetName);
    }

    /**
     * Returns the messages at info level or above that the last executed target logged.
     *
     * @return the log
     */
    public String getLog() {
        return log.toString();
    }

    /**
     * Returns the configured project.
     *
     * @return the project, or {@code null} before {@link #configureProject(String, int)}
     */
    public Project getProject() {
        return project;
    }

    /**
     * Runs the {@code tearDown} target when the build file has one.
     */
    public void release() {
        if (project != null && project.getTargets().containsKey("tearDown")) {
            project.executeTarget("tearDown");
        }
    }
}
