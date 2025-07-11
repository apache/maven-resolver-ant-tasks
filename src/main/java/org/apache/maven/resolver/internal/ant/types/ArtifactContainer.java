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
 * Represents a collection of one or more {@link Artifact} instances in an Ant build context.
 * <p>
 * This interface is implemented by Ant types that logically group or provide access to Maven artifacts,
 * such as {@link Artifact}, {@link Artifacts}, or other custom container types.
 * </p>
 *
 * <p>
 * Implementations must be able to:
 * <ul>
 *   <li>Validate their internal artifact structure for consistency and completeness.</li>
 *   <li>Return all artifacts they contain for further processing (e.g., deployment, installation).</li>
 * </ul>
 *
 * <h2>Known Implementations:</h2>
 * <ul>
 *   <li>{@link Artifact} — represents a single Maven artifact</li>
 *   <li>{@link Artifacts} — represents a group of artifacts or nested artifact containers</li>
 * </ul>
 *
 * <p>This interface is typically used by Ant tasks such as
 * {@link org.apache.maven.resolver.internal.ant.tasks.Install Install} and
 * {@link org.apache.maven.resolver.internal.ant.tasks.Deploy Deploy}.</p>
 *
 * @see Artifact
 * @see Artifacts
 * @see org.apache.maven.resolver.internal.ant.tasks.Install
 * @see org.apache.maven.resolver.internal.ant.tasks.Deploy
 */
public interface ArtifactContainer {

    /**
     * Validates the artifact(s) within this container.
     *
     * @param task the Ant task requesting validation; used for context or error reporting
     * @throws org.apache.tools.ant.BuildException if any artifact is invalid or required attributes are missing
     */
    void validate(Task task);

    /**
     * Returns a flat list of all {@link Artifact} instances in this container.
     *
     * @return a list of artifacts; never {@code null}, but may be empty
     */
    List<Artifact> getArtifacts();
}
