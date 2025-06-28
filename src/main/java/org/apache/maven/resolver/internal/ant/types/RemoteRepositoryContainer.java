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

import java.util.List;

import org.apache.tools.ant.Task;

/**
 * Common interface for Ant data types that hold one or more {@link RemoteRepository} definitions.
 * <p>
 * This abstraction allows reuse of repository containers across tasks such as
 * dependency resolution, deployment, and installation.
 * </p>
 *
 * <h2>Typical Implementations:</h2>
 * <ul>
 *   <li>{@link RemoteRepository} — represents a single remote repository</li>
 *   <li>{@link RemoteRepositories} — represents a collection of repositories</li>
 * </ul>
 *
 * <p>
 * Implementing classes must support validation and retrieval of all contained {@link RemoteRepository} instances.
 * </p>
 *
 * @see RemoteRepository
 * @see RemoteRepositories
 */
public interface RemoteRepositoryContainer {

    /**
     * Validates the repository configuration in the context of the given Ant task.
     *
     * @param task the Ant task requesting validation (typically for logging or error reporting)
     * @throws org.apache.tools.ant.BuildException if the configuration is invalid
     */
    void validate(Task task);

    /**
     * Returns the list of remote repositories represented by this container.
     *
     * @return list of {@link RemoteRepository} objects; never {@code null}
     */
    List<RemoteRepository> getRepositories();
}
