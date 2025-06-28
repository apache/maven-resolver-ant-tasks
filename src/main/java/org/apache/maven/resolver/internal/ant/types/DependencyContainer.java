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

import org.apache.tools.ant.Task;

/**
 * Common interface for Ant types that can contain Maven dependencies.
 * <p>
 * This interface is implemented by types such as {@link Dependency} and {@link Dependencies}
 * to allow uniform handling of dependency validation within tasks like
 * {@code <resolve>}, {@code <install>}, or {@code <createPom>}.
 * </p>
 *
 * <p>
 * Implementations must provide a {@link #validate(Task)} method that verifies
 * their internal state is consistent and suitable for use during build execution.
 * </p>
 *
 * <h2>Typical Implementations:</h2>
 * <ul>
 *   <li>{@link Dependency} — defines a single Maven dependency</li>
 *   <li>{@link Dependencies} — defines a container of dependencies, POMs, or exclusions</li>
 * </ul>
 *
 * @see Dependency
 * @see Dependencies
 */
public interface DependencyContainer {

    /**
     * Validates the container's internal structure and attributes.
     * <p>
     * This method is typically invoked by Ant tasks during execution to ensure
     * that dependency definitions are well-formed, unambiguous, and do not conflict.
     * </p>
     *
     * @param task the Ant task requesting validation, typically used for error context
     *
     * @throws org.apache.tools.ant.BuildException if the container is misconfigured
     */
    void validate(Task task);
}
