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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.maven.resolver.internal.ant.types.Artifact;
import org.apache.maven.resolver.internal.ant.types.Artifacts;
import org.apache.maven.resolver.internal.ant.types.Pom;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

/**
 * Abstract base class for Ant tasks that perform distribution-related operations,
 * such as install or deploy. It handles the configuration and validation of POM and artifact inputs.
 * <p>
 * Subclasses are expected to use {@link #validate()} to ensure input consistency
 * before proceeding with distribution logic.
 *
 * <p>This class ensures:
 * <ul>
 *   <li>Only one {@code <pom>} element is specified</li>
 *   <li>No duplicate artifacts with the same type/classifier are declared</li>
 *   <li>Artifact GAVs (groupId:artifactId:version) match their associated POM</li>
 * </ul>
 *
 */
public abstract class AbstractDistTask extends Task {

    /**
     * Default constructor for {@code AbstractDistTask}.
     */
    public AbstractDistTask() {
        // Default constructor
    }

    /**
     * The POM metadata describing the project and its coordinates.
     */
    private Pom pom;

    /**
     * The artifacts to be distributed.
     */
    private Artifacts artifacts;

    /**
     * Validates the configuration of the task before execution.
     * Ensures there are no duplicate artifacts, that the POM is defined,
     * and that each artifact's associated POM matches the main POM.
     *
     * @throws BuildException if validation fails
     */
    protected void validate() {
        getArtifacts().validate(this);

        final Map<String, File> duplicates = new HashMap<>();
        for (final Artifact artifact : getArtifacts().getArtifacts()) {
            final String key = artifact.getType() + ':' + artifact.getClassifier();
            if ("pom:".equals(key)) {
                throw new BuildException(
                        "You must not specify an <artifact> with type=pom" + ", please use the <pom> element instead.");
            } else if (duplicates.containsKey(key)) {
                throw new BuildException("You must not specify two or more artifacts with the same type ("
                        + artifact.getType() + ") and classifier (" + artifact.getClassifier() + ")");
            } else {
                duplicates.put(key, artifact.getFile());
            }

            validateArtifactGav(artifact);
        }

        final Pom defaultPom = AntRepoSys.getInstance(getProject()).getDefaultPom();
        if (pom == null && defaultPom != null) {
            log("Using default POM (" + defaultPom.getCoords() + ")", Project.MSG_INFO);
            pom = defaultPom;
        }

        if (pom == null) {
            throw new BuildException(
                    "You must specify the <pom file=\"...\"> element" + " to denote the descriptor for the artifacts");
        }
        if (pom.getFile() == null) {
            throw new BuildException("You must specify a <pom> element that has the 'file' attribute set");
        }
    }

    /**
     * Validates that an artifact's groupId, artifactId, and version (GAV) match the main POM's GAV.
     *
     * @param artifact the artifact to validate
     * @throws BuildException if the artifact's GAV does not match the main POM
     */
    private void validateArtifactGav(final Artifact artifact) {
        final Pom artifactPom = artifact.getPom();
        if (artifactPom != null) {
            final String gid;
            final String aid;
            final String version;
            if (artifactPom.getFile() != null) {
                final Model model = artifactPom.getModel(this);
                gid = model.getGroupId();
                aid = model.getArtifactId();
                version = model.getVersion();
            } else {
                gid = artifactPom.getGroupId();
                aid = artifactPom.getArtifactId();
                version = artifactPom.getVersion();
            }

            final Pom pom = getPom();
            if (pom == null) {
                throw new BuildException("You must specify the <pom file=\"...\"> element"
                        + " to denote the descriptor for the artifacts");
            }
            final Model model = pom.getModel(this);

            if (!(model.getGroupId().equals(gid)
                    && model.getArtifactId().equals(aid)
                    && model.getVersion().equals(version))) {
                throw new BuildException(
                        "Artifact references different pom than it would be installed with: " + artifact);
            }
        }
    }

    /**
     * Returns the {@link Artifacts} container, lazily instantiating if necessary.
     *
     * @return the artifact container
     */
    protected Artifacts getArtifacts() {
        if (artifacts == null) {
            artifacts = new Artifacts();
            artifacts.setProject(getProject());
        }
        return artifacts;
    }

    /**
     * Adds a single {@link Artifact} to the task.
     *
     * @param artifact the artifact to add
     */
    public void addArtifact(final Artifact artifact) {
        getArtifacts().addArtifact(artifact);
    }

    /**
     * Adds multiple {@link Artifacts} to the task.
     *
     * @param artifacts the artifacts to add
     */
    public void addArtifacts(final Artifacts artifacts) {
        getArtifacts().addArtifacts(artifacts);
    }

    /**
     * Adds a reference to an existing {@link Artifacts} instance.
     *
     * @param ref the reference to use
     */
    public void setArtifactsRef(final Reference ref) {
        final Artifacts artifacts = new Artifacts();
        artifacts.setProject(getProject());
        artifacts.setRefid(ref);
        getArtifacts().addArtifacts(artifacts);
    }

    /**
     * Returns the current POM, falling back to the default POM if none has been explicitly set.
     *
     * @return the resolved {@link Pom}
     */
    protected Pom getPom() {
        if (pom == null) {
            return AntRepoSys.getInstance(getProject()).getDefaultPom();
        }

        return pom;
    }

    /**
     * Add the POM to use for artifact deployment or installation.
     *
     * @param pom the POM to use
     * @throws BuildException if multiple {@code <pom>} elements are specified
     */
    public void addPom(final Pom pom) {
        if (this.pom != null) {
            throw new BuildException("You must not specify multiple <pom> elements");
        }
        this.pom = pom;
    }

    /**
     * Sets a reference to an existing {@link Pom}.
     *
     * @param ref the reference to set
     * @throws BuildException if multiple {@code <pom>} elements are specified
     */
    public void setPomRef(final Reference ref) {
        if (this.pom != null) {
            throw new BuildException("You must not specify multiple <pom> elements");
        }
        pom = new Pom();
        pom.setProject(getProject());
        pom.setRefid(ref);
    }
}
