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
import java.util.Collections;
import java.util.List;

import org.apache.maven.resolver.internal.ant.ProjectWorkspaceReader;
import org.apache.maven.resolver.internal.ant.tasks.RefTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

/**
 * Represents a Maven artifact in an Ant build script.
 * <p>
 * This type encapsulates the essential coordinates of a Maven artifact — such as
 * {@code groupId}, {@code artifactId}, {@code version}, {@code packaging}, and optional {@code classifier} —
 * along with the actual artifact file to be installed or deployed.
 * </p>
 *
 * <p>
 * {@code <artifact>} elements are typically used as nested elements inside
 * {@link org.apache.maven.resolver.internal.ant.tasks.Install Install} or
 * {@link org.apache.maven.resolver.internal.ant.tasks.Deploy Deploy} tasks.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * <repo:install>
 *   <repo:artifact
 *     file="build/libs/my-lib-1.0.0.jar"
 *     groupId="com.example"
 *     artifactId="my-lib"
 *     version="1.0.0"
 *     packaging="jar"/>
 * </repo:install>
 * }</pre>
 * Alternatively, you can register the artifact after it is created by the jar task and then
 * just refer to it, e.g.:
 * <pre>{@code
 * <target name='jar' depends='compile'>
 *     <property name='jarFile' value='${targetDir}/${artifactId}-${version}.jar'/>
 *     <jar destfile='${jarFile}'>
 *         <fileset dir='${mainBuildDir}'/>
 *     </jar>
 *     <repo:artifact file='${jarFile}' type='jar' id='mainJar'/>
 * </target>
 *
 * <target name='install' depends='jar'>
 *     <repo:artifacts id='localArtifacts'>
 *         <repo:artifact refid='mainJar'/>
 *     </repo:artifacts>
 *     <repo:install artifactsref='localArtifacts'/>
 * </target>
 * }</pre>
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><strong>id</strong> — the refId to use when referring to it, e.g., when combining several <code>artifact</code> with the <code>artifacts</code> tag
 *   <li><strong>file</strong> — the actual artifact file to install or deploy (required)</li>
 *   <li><strong>groupId</strong> — the Maven groupId of the artifact</li>
 *   <li><strong>artifactId</strong> — the Maven artifactId</li>
 *   <li><strong>version</strong> — the artifact version</li>
 *   <li><strong>packaging</strong> — the type/extension of the artifact (e.g., {@code jar}, {@code pom}) (required)</li>
 *   <li><strong>classifier</strong> — optional classifier (e.g., {@code sources}, {@code javadoc})</li>
 * </ul>
 *
 * <h2>Typical Use Cases:</h2>
 * <ul>
 *   <li>Publishing pre-built artifacts from Ant to a Maven repository</li>
 *   <li>Installing locally built binaries into the local Maven repository</li>
 *   <li>Providing coordinates for deployment without a POM</li>
 * </ul>
 *
 * @see org.apache.maven.resolver.internal.ant.tasks.Install
 * @see org.apache.maven.resolver.internal.ant.tasks.Deploy
 */
public class Artifact extends RefTask implements ArtifactContainer {

    private File file;

    private String type;

    private String classifier;

    private Pom pom;

    /**
     * Default constructor for the {@code Artifact} data type.
     */
    public Artifact() {
        // Default constructor
    }

    /**
     * Resolves this object if defined as a reference and verifies that it is a
     * {@code Artifact} instance.
     *
     * @return the referenced {@code Artifact} instance
     * @throws org.apache.tools.ant.BuildException if the reference is invalid
     */
    protected Artifact getRef() {
        return (Artifact) getCheckedRef();
    }

    /**
     * Validates that all required attributes are present and that the file exists.
     *
     * @param task the Ant task that owns this data type (used for context in error messages)
     * @throws BuildException if required attributes are missing or invalid
     */
    @Override
    public void validate(final Task task) {
        if (isReference()) {
            getRef().validate(task);
        } else {
            if (file == null) {
                throw new BuildException("You must specify the 'file' for the artifact");
            } else if (!file.isFile()) {
                throw new BuildException("The artifact file " + file + " does not exist");
            }
            if (type == null || type.length() <= 0) {
                throw new BuildException("You must specify the 'type' for the artifact");
            }
        }
    }

    /**
     * Sets a reference to another {@code Artifact} instance.
     *
     * @param ref the reference ID
     * @throws BuildException if conflicting attributes are already set
     */
    @Override
    public void setRefid(final Reference ref) {
        if (file != null || type != null || classifier != null) {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
    }

    /**
     * Returns the artifact file.
     *
     * @return the artifact file
     */
    public File getFile() {
        if (isReference()) {
            return getRef().getFile();
        }
        return file;
    }

    /**
     * Sets the artifact file to be deployed or installed.
     *
     * @param file the artifact file
     */
    public void setFile(final File file) {
        checkAttributesAllowed();
        this.file = file;

        if (file != null && type == null) {
            final String name = file.getName();
            final int period = name.lastIndexOf('.');
            if (period >= 0) {
                type = name.substring(period + 1);
            }
        }
    }

    /**
     * Returns the type/packaging of the artifact (e.g., {@code jar}, {@code pom}).
     *
     * @return the artifact type
     */
    public String getType() {
        if (isReference()) {
            return getRef().getType();
        }
        return (type != null) ? type : "jar";
    }

    /**
     * Sets the artifact's packaging or type (e.g., {@code jar}, {@code pom}).
     *
     * @param type the artifact type
     */
    public void setType(final String type) {
        checkAttributesAllowed();
        this.type = type;
    }

    /**
     * Returns the optional classifier of the artifact (e.g., {@code sources}, {@code javadoc}).
     *
     * @return the classifier or an empty string if not set
     */
    public String getClassifier() {
        if (isReference()) {
            return getRef().getClassifier();
        }
        return (classifier != null) ? classifier : "";
    }

    /**
     * Sets the optional classifier for the artifact.
     *
     * @param classifier the classifier
     */
    public void setClassifier(final String classifier) {
        checkAttributesAllowed();
        this.classifier = classifier;
    }

    /**
     * Sets a reference to a {@link Pom} element to associate with this artifact.
     *
     * @param ref the reference to the POM
     */
    public void setPomRef(final Reference ref) {
        checkAttributesAllowed();
        final Pom pom = new Pom();
        pom.setProject(getProject());
        pom.setRefid(ref);
        this.pom = pom;
    }

    /**
     * Adds a nested {@link Pom} element to associate with this artifact.
     *
     * @param pom the POM object
     */
    public void addPom(final Pom pom) {
        checkChildrenAllowed();
        this.pom = pom;
    }

    /**
     * Returns the associated POM for this artifact, if any.
     *
     * @return the POM object, or {@code null} if none is set
     */
    public Pom getPom() {
        if (isReference()) {
            return getRef().getPom();
        }
        return pom;
    }

    /**
     * Returns a list containing this single artifact.
     *
     * @return a singleton list with this artifact
     */
    @Override
    public List<Artifact> getArtifacts() {
        return Collections.singletonList(this);
    }

    /**
     * Registers this artifact in the {@link ProjectWorkspaceReader} for internal resolution.
     */
    @Override
    public void execute() throws BuildException {
        ProjectWorkspaceReader.getInstance().addArtifact(this);
    }

    /**
     * Returns a string representation of the artifact, showing its type and classifier.
     *
     * @return a human-readable string describing the artifact
     */
    @Override
    public String toString() {
        final String pomRepr = getPom() != null ? "(" + getPom().toString() + ":)" : "";
        return String.format(pomRepr + "%s:%s", getType(), getClassifier());
    }
}
