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
 * A container for one or more {@link Artifact} elements in an Ant build script.
 * <p>
 * This Ant {@link org.apache.tools.ant.types.DataType} is used to group multiple artifacts,
 * allowing them to be referenced collectively by tasks such as
 * {@link org.apache.maven.resolver.internal.ant.tasks.Install Install} or
 * {@link org.apache.maven.resolver.internal.ant.tasks.Deploy Deploy}.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * <repo:artifacts id="deployment.artifacts">
 *   <repo:artifact file="target/my-lib.jar" type="jar" groupId="com.example" artifactId="my-lib" version="1.0.0"/>
 *   <repo:artifact file="target/my-lib.pom" type="pom" groupId="com.example" artifactId="my-lib" version="1.0.0"/>
 * </repo:artifacts>
 *
 * <repo:deploy artifactsRef="deployment.artifacts"/>
 * }</pre>
 *
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><strong>id</strong> — Optional reference ID to reuse the container via {@code artifactsRef}.</li>
 * </ul>
 *
 * <h2>Nested Elements:</h2>
 * <ul>
 *   <li>{@code <artifact>} — A single Maven artifact definition.</li>
 *   <li>{@code <artifacts>} — Another nested {@code Artifacts} element, allowing composition.</li>
 * </ul>
 *
 * <p>This class also supports Ant references via {@code refid}, and prevents combining that
 * with nested artifact declarations.</p>
 *
 * @see Artifact
 * @see ArtifactContainer
 * @see org.apache.maven.resolver.internal.ant.tasks.Install
 * @see org.apache.maven.resolver.internal.ant.tasks.Deploy
 */
public class Artifacts extends DataType implements ArtifactContainer {

    private final List<ArtifactContainer> containers = new ArrayList<>();

    /**
     * Default constructor for the {@code Artifacts} data type.
     */
    public Artifacts() {
        // Default constructor
    }

    /**
     * Resolves this object if defined as a reference and verifies that it is a
     * {@code Artifacts} instance.
     *
     * @return the referenced {@code Artifacts} instance
     * @throws org.apache.tools.ant.BuildException if the reference is invalid
     */
    protected Artifacts getRef() {
        return getCheckedRef(Artifacts.class);
    }

    /**
     * Validates the nested artifact containers.
     * If this is a reference, delegates to the referenced object.
     *
     * @param task the Ant task using this data type
     * @throws org.apache.tools.ant.BuildException if validation fails
     */
    @Override
    public void validate(Task task) {
        if (isReference()) {
            getRef().validate(task);
        } else {
            for (ArtifactContainer container : containers) {
                container.validate(task);
            }
        }
    }

    /**
     * Sets a reference to another {@code Artifacts} instance.
     *
     * @param ref the Ant reference
     * @throws org.apache.tools.ant.BuildException if nested artifacts are already defined
     */
    @Override
    public void setRefid(Reference ref) {
        if (!containers.isEmpty()) {
            throw noChildrenAllowed();
        }
        super.setRefid(ref);
    }

    /**
     * Allow Ant to add a single {@link Artifact} element to this container.
     *
     * @param artifact the artifact to add
     */
    public void addArtifact(Artifact artifact) {
        checkChildrenAllowed();
        containers.add(artifact);
    }

    /**
     * Allow Ant to add another {@link Artifacts} container as a nested element.
     *
     * @param artifacts the nested container to add
     * @throws org.apache.tools.ant.BuildException if a circular reference is detected
     */
    public void addArtifacts(Artifacts artifacts) {
        checkChildrenAllowed();
        if (artifacts == this) {
            throw circularReference();
        }
        containers.add(artifacts);
    }

    /**
     * Collects all {@link Artifact} objects from this container and any nested containers.
     *
     * @return a list of all artifacts
     */
    @Override
    public List<Artifact> getArtifacts() {
        if (isReference()) {
            return getRef().getArtifacts();
        }
        List<Artifact> artifacts = new ArrayList<>();
        for (ArtifactContainer container : containers) {
            artifacts.addAll(container.getArtifacts());
        }
        return artifacts;
    }
}
