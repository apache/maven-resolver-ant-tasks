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
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Executes Ant targets from a build file and captures the execution details, so tests can assert on them.
 * <p>
 * Ant has no JUnit 5 equivalent to offer: {@code ant-testutil} ships only the JUnit 4 {@code BuildFileRule} and
 * the JUnit 3 {@code BuildFileTest}, and depends on {@code junit:junit} at compile scope. This carries over the
 * part of {@code BuildFileRule} these tests actually use. Register it with {@link
 * org.junit.jupiter.api.extension.RegisterExtension} so that {@link #afterEach} runs the build file's
 * {@code tearDown} target, the way the rule did.
 */
public class AntBuildFileExtension implements AfterEachCallback {

    private Project project;

    private StringBuffer logBuffer;

    /**
     * Runs the configured project's {@code tearDown} target, if it declares one.
     */
    @Override
    public void afterEach(ExtensionContext context) {
        if (project == null) {
            // configureProject has not been called - nothing to clean up
            return;
        }
        String tearDown = "tearDown";
        if (project.getTargets().containsKey(tearDown)) {
            project.executeTarget(tearDown);
        }
    }

    /**
     * Sets up to run the named build file.
     *
     * @param filename the build file to run
     * @param logLevel one of the {@code Project.MSG_*} levels; messages logged above it are discarded
     */
    public void configureProject(String filename, int logLevel) throws BuildException {
        logBuffer = new StringBuffer();
        project = new Project();
        project.init();
        File antFile = new File(filename);
        project.setUserProperty(MagicNames.ANT_FILE, antFile.getAbsolutePath());
        project.addBuildListener(new AntTestListener(logLevel));
        ProjectHelper.configureProject(project, antFile);
    }

    /**
     * Executes a target of the configured build file. Requires {@link #configureProject} to have been called.
     *
     * @param targetName the target to run
     */
    public void executeTarget(String targetName) {
        PrintStream out = new PrintStream(new AntOutputStream());
        PrintStream err = new PrintStream(new AntOutputStream());
        logBuffer = new StringBuffer();

        /* we synchronize to protect our custom output streams from being overridden
         * by other tests executing targets concurrently. Ultimately this would only
         * happen if we ran a multi-threaded test executing multiple targets at once, and
         * this protection doesn't prevent a target from internally modifying the output
         * stream during a test - but at least this scenario is fairly deterministic so
         * easier to troubleshoot.
         */
        synchronized (System.out) {
            PrintStream sysOut = System.out;
            PrintStream sysErr = System.err;
            sysOut.flush();
            sysErr.flush();
            try {
                System.setOut(out);
                System.setErr(err);
                project.executeTarget(targetName);
            } finally {
                System.setOut(sysOut);
                System.setErr(sysErr);
            }
        }
    }

    /**
     * @return the INFO, WARN and ERROR messages logged by the last execution
     */
    public String getLog() {
        return logBuffer.toString();
    }

    /**
     * @return the project configured for the test
     */
    public Project getProject() {
        return project;
    }

    /**
     * Swallows whatever a target writes straight to the console. Assertions read {@link #getLog()}, which the
     * build listener fills, so the bytes themselves are not worth keeping.
     */
    private static class AntOutputStream extends OutputStream {

        @Override
        public void write(int b) {
            // discarded on purpose
        }
    }

    /**
     * Collects the logged messages, ignoring anything above the configured level.
     */
    private class AntTestListener implements BuildListener {

        private final int logLevel;

        AntTestListener(int logLevel) {
            this.logLevel = logLevel;
        }

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
            if (event.getPriority() > logLevel) {
                // ignore event
                return;
            }

            if (event.getPriority() == Project.MSG_INFO
                    || event.getPriority() == Project.MSG_WARN
                    || event.getPriority() == Project.MSG_ERR) {
                logBuffer.append(event.getMessage());
            }
        }
    }
}
