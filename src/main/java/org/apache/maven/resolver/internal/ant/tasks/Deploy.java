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
import org.apache.maven.resolver.internal.ant.types.RemoteRepository;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Reference;

/**
 * Ant task to deploy artifacts to a remote Maven repository.
 * <p>
 * This task uploads artifacts, POM files, and optionally metadata such as licenses or dependencies
 * to a remote repository using Maven Resolver. It mimics the behavior of {@code mvn deploy}
 * but is integrated into Ant build scripts.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * <repo:deploy>
 *   <repo:artifact file='build/libs/my-lib.jar'
 *             groupId='com.example'
 *             artifactId='my-lib'
 *             version='1.0.0'
 *             packaging='jar'/>
 *   <repo:repository id="snapshots" url='https://my.repo.com/snapshots'>
 *     <repo:authentication username='user' password='pass'/>
 *   </repo:repository>
 * </repo:deploy>
 * }</pre>
 * * If you have used the pom task to register an existing POM, or you used the
 * createPom task to generate and register a POM, you can instead do this:
 * <pre>{@code
 * <repo:artifacts id='remoteArtifacts'>
 *       <repo:artifact refid='jar'/>
 *       <repo:artifact refid='sourceJar'/>
 *       <repo:artifact refid='javadocJar'/>
 * </repo:artifacts>
 * <repo:deploy artifactsref='remoteArtifacts'>
 *   <repo:remoteRepo id='localNexus', url='http://localhost:8081/repository/repo/'/>
 * </repo:deploy>
 * }</pre>
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><strong>failOnMissingPom</strong> — whether to fail if no POM information is provided (default: true)</li>
 * </ul>
 *
 * <h2>Nested Elements:</h2>
 * <ul>
 *   <li>{@code <artifact>} — specifies the artifact file and its coordinates to be deployed</li>
 *   <li>{@code <pom>} — (optional) provides the POM file to upload, or one will be generated</li>
 *   <li>{@code <repository>} — defines the target repository for deployment</li>
 * </ul>
 *
 * <h2>Behavior:</h2>
 * <ul>
 *   <li>If no POM is provided, a basic one will be generated using the artifact's metadata</li>
 *   <li>Uploads artifacts using Maven's supported protocols (e.g., HTTP/S with authentication)</li>
 *   <li>Supports deployment to both snapshot and release repositories</li>
 * </ul>
 *
 * <p>
 * This task is typically used in CI/CD pipelines or custom release scripts that need to publish artifacts from Ant.
 * </p>
 *
 * @see org.apache.maven.resolver.internal.ant.tasks.CreatePom
 * @see org.apache.maven.resolver.internal.ant.types.Artifact
 * @see org.apache.maven.resolver.internal.ant.types.Pom
 * @see org.apache.maven.resolver.internal.ant.types.RemoteRepository
 */
public class Deploy extends AbstractDistTask {

    private RemoteRepository repository;

    private RemoteRepository snapshotRepository;

    @Override
    protected void validate() {
        super.validate();

        if (repository == null) {
            throw new BuildException("You must specify the <remoteRepo id=\"...\" url=\"...\"> element"
                    + " to denote the target repository for the deployment");
        } else {
            repository.validate(this);
        }
        if (snapshotRepository != null) {
            snapshotRepository.validate(this);
        }
    }

    public void addRemoteRepo(RemoteRepository repository) {
        if (this.repository != null) {
            throw new BuildException("You must not specify multiple <remoteRepo> elements");
        }
        this.repository = repository;
    }

    public void setRemoteRepoRef(Reference ref) {
        if (repository == null) {
            repository = new RemoteRepository();
            repository.setProject(getProject());
        }
        repository.setRefid(ref);
    }

    public void addSnapshotRepo(RemoteRepository snapshotRepository) {
        if (this.snapshotRepository != null) {
            throw new BuildException("You must not specify multiple <snapshotRepo> elements");
        }
        this.snapshotRepository = snapshotRepository;
    }

    public void setSnapshotRepoRef(Reference ref) {
        if (snapshotRepository == null) {
            snapshotRepository = new RemoteRepository();
            snapshotRepository.setProject(getProject());
        }
        snapshotRepository.setRefid(ref);
    }

    @Override
    public void execute() throws BuildException {
        validate();

        AntRepoSys.getInstance(getProject()).deploy(this, getPom(), getArtifacts(), repository, snapshotRepository);
    }
}
