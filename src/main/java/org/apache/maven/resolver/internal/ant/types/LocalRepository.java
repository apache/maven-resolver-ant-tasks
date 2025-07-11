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

import java.io.File;

import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * Represents a custom local Maven repository location in an Ant build script.
 * <p>
 * This Ant {@code DataType} allows you to override the default local repository
 * (typically {@code ~/.m2/repository}) by specifying a custom directory to be used
 * for dependency resolution, artifact installation, or deployment.
 * </p>
 *
 * <p>
 * The local repository directory is specified using the {@code dir} attribute.
 * This can be declared inline or via a shared reference using {@code refid}.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * <localRepository id="custom.repo" dir="target/local-repo"/>
 *
 * <resolve>
 *   <localRepo refid="custom.repo"/>
 *   ...
 * </resolve>
 * }</pre>
 *
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><strong>dir</strong> — the directory to use as the local repository (required unless using {@code refid})</li>
 * </ul>
 *
 * <p>
 * If this type is declared without a {@link org.apache.tools.ant.Task} context,
 * it will automatically register itself with the current project via {@link AntRepoSys#setLocalRepository(LocalRepository)}.
 * </p>
 *
 * <h2>Typical Use Cases:</h2>
 * <ul>
 *   <li>Using an isolated local repository for reproducible builds</li>
 *   <li>Testing against a temporary or project-scoped repository</li>
 *   <li>Ensuring deployment and resolution do not affect the user's global Maven state</li>
 * </ul>
 *
 * @see #setDir(File)
 * @see #setRefid(org.apache.tools.ant.types.Reference)
 * @see org.apache.maven.resolver.internal.ant.tasks.Resolve
 * @see org.apache.maven.resolver.internal.ant.AntRepoSys
 */
public class LocalRepository extends DataType {

    private final Task task;

    private File dir;

    /**
     * Constructs a new {@code LocalRepository} without an owning Ant task.
     * <p>
     * This constructor is typically used when the local repository is defined inline
     * in the build script, allowing it to be automatically registered with the project.
     * </p>
     */
    public LocalRepository() {
        this(null);
    }

    /**
     * Constructs a new {@code LocalRepository} with an owning Ant task.
     * <p>
     * This constructor is used when the local repository is defined within a specific
     * Ant task context, allowing it to be associated with that task.
     * </p>
     *
     * @param task the Ant task that owns this {@code LocalRepository}, or {@code null} if not associated with a task
     */
    public LocalRepository(Task task) {
        this.task = task;
    }

    /**
     * Sets the Ant project context for this {@code LocalRepository}.
     * <p>
     * In addition to the standard behavior of associating this data type with the Ant project,
     * this method also automatically registers this {@code LocalRepository} with the global
     * {@link org.apache.maven.resolver.internal.ant.AntRepoSys} instance for the project,
     * <strong>but only if no owning {@link org.apache.tools.ant.Task} was provided</strong>.
     * </p>
     *
     * <p>
     * This automatic registration allows this local repository to become the default
     * used for resolution and deployment tasks within the current build context.
     * </p>
     *
     * @param project the Ant project to associate with this data type
     *
     * @see org.apache.maven.resolver.internal.ant.AntRepoSys#setLocalRepository(LocalRepository)
     * @see #LocalRepository(org.apache.tools.ant.Task)
     */
    @Override
    public void setProject(Project project) {
        super.setProject(project);

        if (task == null) {
            AntRepoSys.getInstance(project).setLocalRepository(this);
        }
    }

    /**
     * Resolves this object if defined as a reference and verifies that it is a
     * {@code LocalRepository} instance.
     *
     * @return the referenced {@code LocalRepository} instance
     * @throws org.apache.tools.ant.BuildException if the reference is invalid
     *
     * @see #setRefid(org.apache.tools.ant.types.Reference)
     * @see #isReference()
     */
    protected LocalRepository getRef() {
        return getCheckedRef(LocalRepository.class);
    }

    /**
     * Sets a reference to another {@code <localRepository>} definition.
     * <p>
     * This allows the current {@code LocalRepository} to act as an alias
     * for another local repository definition declared elsewhere in the build script.
     * When a reference is set, all other attributes (such as {@code dir}) must remain unset.
     * </p>
     *
     * <p>
     * Attempting to set this reference after the {@code dir} attribute has already been specified
     * will result in an error.
     * </p>
     *
     * @param ref the reference to another {@code LocalRepository}
     *
     * @throws org.apache.tools.ant.BuildException if {@code dir} is already set
     *
     * @see #getRef()
     * @see #setDir(File)
     */
    @Override
    public void setRefid(Reference ref) {
        if (dir != null) {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
    }

    /**
     * Returns the directory to be used as the local Maven repository.
     * <p>
     * If this {@code LocalRepository} is defined as a reference (via {@code refid}),
     * the method delegates to the referenced instance.
     * </p>
     *
     * @return the local repository directory, or {@code null} if not set
     *
     * @see #setDir(File)
     * @see #setRefid(org.apache.tools.ant.types.Reference)
     */
    public File getDir() {
        if (isReference()) {
            return getRef().getDir();
        }
        return dir;
    }

    /**
     * Sets the directory to be used as the local Maven repository.
     * <p>
     * This directory will be used in place of the default {@code ~/.m2/repository}
     * for tasks such as dependency resolution, artifact installation, and deployment.
     * </p>
     *
     * <p>
     * This method may not be used if this object is defined as a reference (i.e., {@code refid} is set).
     * If the attributes are already configured, an exception will be thrown.
     * </p>
     *
     * @param dir the directory to use as the local repository
     *
     * @throws org.apache.tools.ant.BuildException if this instance is a reference or attributes are not allowed
     *
     * @see #getDir()
     * @see #setRefid(org.apache.tools.ant.types.Reference)
     */
    public void setDir(File dir) {
        checkAttributesAllowed();
        this.dir = dir;
    }
}
