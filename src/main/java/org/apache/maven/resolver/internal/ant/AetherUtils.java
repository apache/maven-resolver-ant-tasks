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
import java.util.Map;

import org.apache.maven.resolver.internal.ant.types.RemoteRepositories;
import org.apache.tools.ant.Project;

class AetherUtils {

    public static File findGlobalSettings(final Project project) {
        return findGlobalSettings(project, System.getenv());
    }

    /**
     * Finds the global Maven settings file from the Maven home directory or the Ant home directory, in that order.
     *
     * @param project the Ant project to read the {@code ant.home} property from
     * @param environment the environment variables to consult for the Maven home directory
     * @return the global settings file, or {@code null} if none exists
     */
    static File findGlobalSettings(final Project project, final Map<String, String> environment) {
        final String mavenHome = getMavenHome(project, environment);
        if (mavenHome != null) {
            final File mavenSettings = new File(new File(mavenHome, "conf"), Names.SETTINGS_XML);
            if (mavenSettings.isFile()) {
                return mavenSettings;
            }
        }

        final File antSettings = new File(new File(project.getProperty("ant.home"), "etc"), Names.SETTINGS_XML);
        if (antSettings.isFile()) {
            return antSettings;
        }

        return null;
    }

    public static String getMavenHome(final Project project) {
        return getMavenHome(project, System.getenv());
    }

    /**
     * Resolves the Maven home directory from the {@code maven.home} Ant property, the {@code maven.home} system
     * property, the {@code MAVEN_HOME} environment variable, or the {@code M2_HOME} environment variable, in that
     * order.
     *
     * @param project the Ant project to read the {@code maven.home} property from
     * @param environment the environment variables to consult
     * @return the resolved Maven home directory, or {@code null} if none is set
     */
    static String getMavenHome(final Project project, final Map<String, String> environment) {
        String mavenHome = project.getProperty("maven.home");
        if (mavenHome == null) {
            mavenHome = System.getProperty("maven.home");
        }
        if (mavenHome == null) {
            mavenHome = environment.get("MAVEN_HOME");
        }
        if (mavenHome == null) {
            mavenHome = environment.get("M2_HOME");
        }
        return mavenHome;
    }

    public static File findUserSettings(final Project project) {
        final File userHome = new File(project.getProperty("user.home"));
        final File file = new File(new File(userHome, ".ant"), Names.SETTINGS_XML);
        if (file.isFile()) {
            return file;
        } else {
            return new File(new File(userHome, ".m2"), Names.SETTINGS_XML);
        }
    }

    public static RemoteRepositories getDefaultRepositories(final Project project) {
        final Object obj = project.getReference(Names.ID_DEFAULT_REPOS);
        if (obj instanceof RemoteRepositories) {
            return (RemoteRepositories) obj;
        }
        return null;
    }
}
