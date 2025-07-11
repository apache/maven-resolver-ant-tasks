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
import org.apache.maven.resolver.internal.ant.types.Dependencies;
import org.apache.maven.resolver.internal.ant.types.LocalRepository;
import org.apache.maven.resolver.internal.ant.types.RemoteRepositories;
import org.apache.maven.resolver.internal.ant.types.RemoteRepository;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;
import org.eclipse.aether.collection.CollectResult;

/**
 * Abstract base class for Ant tasks that perform dependency resolution using Maven Resolver (Aether).
 * <p>
 * This class encapsulates support for specifying and configuring dependencies, remote repositories,
 * and the local repository used during resolution.
 * It provides helper methods to collect dependencies and manage nested configuration elements.
 * </p>
 *
 * <p>This class is intended to be extended by concrete tasks that require dependency resolution,
 * such as retrieving artifacts, resolving transitive dependencies, or analyzing dependency graphs.</p>
 *
 */
public abstract class AbstractResolvingTask extends Task {

    /**
     * The dependency definitions to resolve.
     * Configurable via a nested {@code <dependencies>} element or a reference using {@link #setDependenciesRef(Reference)}.
     */
    protected Dependencies dependencies;

    /**
     * The list of remote repositories to use for resolution.
     * Populated via one or more {@code <remoteRepo>} or {@code <remoteRepos>} nested elements.
     */
    protected RemoteRepositories remoteRepositories;

    /**
     * Optional custom local repository definition.
     * Created using the {@code <localRepo>} nested element.
     */
    protected LocalRepository localRepository;

    /**
     * Default constructor for {@code AbstractResolvingTask}.
     */
    public AbstractResolvingTask() {
        // Default constructor
    }

    /**
     * Adds a {@code <dependencies>} element to define the dependencies to be resolved.
     *
     * @param dependencies the {@link Dependencies} element to add
     * @throws BuildException if multiple {@code <dependencies>} elements are specified
     */
    public void addDependencies(final Dependencies dependencies) {
        if (this.dependencies != null) {
            throw new BuildException("You must not specify multiple <dependencies> elements");
        }
        this.dependencies = dependencies;
    }

    /**
     * Sets a reference to an existing {@link Dependencies} instance using {@code refid}.
     *
     * @param ref the reference to a {@code Dependencies} instance
     */
    public void setDependenciesRef(final Reference ref) {
        if (dependencies == null) {
            dependencies = new Dependencies();
            dependencies.setProject(getProject());
        }
        dependencies.setRefid(ref);
    }

    /**
     * Creates a {@code <localRepo>} element to specify a custom local repository for resolution.
     *
     * @return the created {@link LocalRepository} instance
     * @throws BuildException if multiple {@code <localRepo>} elements are specified
     */
    public LocalRepository createLocalRepo() {
        if (localRepository != null) {
            throw new BuildException("You must not specify multiple <localRepo> elements");
        }
        localRepository = new LocalRepository(this);
        return localRepository;
    }

    /**
     * Returns the {@link RemoteRepositories} instance used for resolution, creating it if necessary.
     *
     * @return the {@code RemoteRepositories} instance
     */
    private RemoteRepositories getRemoteRepos() {
        if (remoteRepositories == null) {
            remoteRepositories = new RemoteRepositories();
            remoteRepositories.setProject(getProject());
        }
        return remoteRepositories;
    }

    /**
     * Adds a single {@code <remoteRepo>} element to the list of remote repositories used for resolution.
     *
     * @param repository the remote repository to add
     */
    public void addRemoteRepo(final RemoteRepository repository) {
        getRemoteRepos().addRemoterepo(repository);
    }

    /**
     * Adds a {@code <remoteRepos>} element, representing a collection of remote repositories.
     *
     * @param repositories the remote repositories to add
     */
    public void addRemoteRepos(final RemoteRepositories repositories) {
        getRemoteRepos().addRemoterepos(repositories);
    }

    /**
     * Sets a reference to an existing {@link RemoteRepositories} instance using {@code refid}.
     *
     * @param ref the reference to a {@code RemoteRepositories} element
     */
    public void setRemoteReposRef(final Reference ref) {
        final RemoteRepositories repos = new RemoteRepositories();
        repos.setProject(getProject());
        repos.setRefid(ref);
        getRemoteRepos().addRemoterepos(repos);
    }

    /**
     * Performs dependency collection using the configured {@link Dependencies},
     * {@link LocalRepository}, and {@link RemoteRepositories}.
     *
     * @return the result of the dependency collection
     * @throws BuildException if dependency collection fails
     */
    protected CollectResult collectDependencies() {
        return AntRepoSys.getInstance(getProject())
                .collectDependencies(this, dependencies, localRepository, remoteRepositories);
    }
}
