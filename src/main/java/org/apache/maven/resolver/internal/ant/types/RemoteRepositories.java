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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * Represents a collection of remote repositories used for dependency resolution or artifact deployment.
 * <p>
 * This container allows combining multiple {@link RemoteRepository} and nested {@code <remoterepositories>} elements
 * into a single referenceable unit. It supports recursive composition and reference resolution through {@code refid}.
 * </p>
 *
 * <p>
 * This type can be used in Ant build scripts to define reusable sets of repositories.
 * </p>
 *
 * @see RemoteRepository
 * @see RemoteRepositoryContainer
 * @see #addRemoterepo(RemoteRepository)
 * @see #addRemoterepos(RemoteRepositories)
 */
public class RemoteRepositories extends DataType implements RemoteRepositoryContainer {

    private final List<RemoteRepositoryContainer> containers = new ArrayList<>();

    /**
     * Default constructor for {@code RemoteRepositories} data type.
     * <p>
     * Initializes an empty collection of remote repositories.
     * </p>
     */
    public RemoteRepositories() {
        // Default constructor
    }

    /**
     * Resolves this object if defined as a reference and verifies that it is a {@code RemoteRepositories} instance.
     *
     * @return the resolved referenced instance
     * @throws org.apache.tools.ant.BuildException if the reference is invalid
     */
    protected RemoteRepositories getRef() {
        return getCheckedRef(RemoteRepositories.class);
    }

    /**
     * Validates the contained repositories.
     * <p>
     * If this object is defined as a reference, validation is delegated to the referenced instance.
     * Otherwise, each nested {@link RemoteRepositoryContainer} is validated individually.
     * </p>
     *
     * @param task the Ant task requesting validation, used for error reporting
     */
    @Override
    public void validate(Task task) {
        if (isReference()) {
            getRef().validate(task);
        } else {
            for (RemoteRepositoryContainer container : containers) {
                container.validate(task);
            }
        }
    }

    /**
     * Sets a reference to another {@code RemoteRepositories} instance.
     * <p>
     * Once a reference is set, this instance must not contain any child elements.
     * </p>
     *
     * @param ref the Ant reference pointing to another {@code RemoteRepositories} object
     * @throws org.apache.tools.ant.BuildException if this object already contains children
     */
    @Override
    public void setRefid(Reference ref) {
        if (!containers.isEmpty()) {
            throw noChildrenAllowed();
        }
        super.setRefid(ref);
    }

    /**
     * Allow ant to add a single remote repository to the list of containers.
     *
     * @param repository the {@link RemoteRepository} to add
     * @throws org.apache.tools.ant.BuildException if this object is defined as a reference
     */
    public void addRemoterepo(RemoteRepository repository) {
        checkChildrenAllowed();
        containers.add(repository);
    }

    /**
     * Allow Ant to add another {@code RemoteRepositories} instance to this container.
     * <p>
     * This allows composition of multiple repository lists.
     * </p>
     *
     * @param repositories the {@code RemoteRepositories} to add
     * @throws org.apache.tools.ant.BuildException if this object is defined as a reference
     * @throws org.apache.tools.ant.BuildException if the specified object is the same as this instance (circular reference)
     */
    public void addRemoterepos(RemoteRepositories repositories) {
        checkChildrenAllowed();
        if (repositories == this) {
            throw circularReference();
        }
        containers.add(repositories);
    }

    /**
     * Returns a flattened list of all {@link RemoteRepository} instances contained in this object.
     * <p>
     * If this is a reference, the call is delegated to the referenced object.
     * </p>
     *
     * @return the list of remote repositories
     */
    @Override
    public List<RemoteRepository> getRepositories() {
        if (isReference()) {
            return getRef().getRepositories();
        }
        List<RemoteRepository> repos = new ArrayList<>();
        for (RemoteRepositoryContainer container : containers) {
            repos.addAll(container.getRepositories());
        }
        return repos;
    }
}
