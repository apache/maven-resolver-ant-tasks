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

import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.tools.ant.BuildException;

/**
 * Ant task to install artifacts into the local Maven repository.
 * <p>
 * This task allows you to manually install artifacts into the local repository
 * (usually {@code ~/.m2/repository}) by specifying coordinates, files, and optional POM metadata.
 * It mimics the behavior of {@code mvn install:install-file} but is usable in Ant build scripts.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * <repo:install>
 *   <repo:artifact file="build/libs/my-lib.jar"
 *             groupId="com.example"
 *             artifactId="my-lib"
 *             version="1.0.0"
 *             packaging="jar"/>
 * </repo:install>
 * }</pre>
 *
 * If you have used the pom task to register an existing POM, or you used the
 * createPom task to generate and register a POM, you can instead do this:
 * <pre>{@code
 *     <repo:artifacts id='localArtifacts'>
 *       <repo:artifact refid='mainJar'/>
 *       <repo:artifact file='${srcJarFile}'
 *                      type='jar'
 *                      classifier='sources'
 *                      id='srcJar'/>
 *       <repo:artifact file='${javadocJarFile}'
 *                      type='jar'
 *                      classifier='javadocs'
 *                      id='javadocJar'/>
 *     </repo:artifacts>
 *     <repo:install artifactsref='localArtifacts'/>
 * }</pre>
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><strong>failOnMissingPom</strong> — whether to fail if no POM information is provided (default: true)</li>
 * </ul>
 *
 * <h2>Nested Elements:</h2>
 * <ul>
 *   <li>{@code <artifact>} — specifies the artifact file and coordinates to install</li>
 *   <li>{@code <pom>} — (optional) specifies the POM file to install along with the artifact</li>
 * </ul>
 *
 * <h2>Behavior:</h2>
 * <ul>
 *   <li>If a POM is not explicitly provided, a minimal one will be generated</li>
 *   <li>Installs to the local repository used by Maven and compatible tools</li>
 * </ul>
 *
 * <p>This task is useful in custom build pipelines, testing, or deploying non-Maven-built artifacts to the local repo.</p>
 *
 * @see org.apache.maven.resolver.internal.ant.tasks.CreatePom
 * @see org.apache.maven.resolver.internal.ant.types.Artifact
 * @see org.apache.maven.resolver.internal.ant.types.Pom
 */
public class Install extends AbstractDistTask {

    /**
     * Default constructor used by Ant to create an <code>Install</code> task instance.
     */
    public Install() {
        // Default constructor for Ant task
    }

    @Override
    public void execute() throws BuildException {
        validate();

        AntRepoSys.getInstance(getProject()).install(this, getPom(), getArtifacts());
    }
}
