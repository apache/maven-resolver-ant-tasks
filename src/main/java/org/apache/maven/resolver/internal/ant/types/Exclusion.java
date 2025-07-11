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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * Represents a Maven dependency exclusion in an Ant build script.
 * <p>
 * This type is used within a {@link org.apache.maven.resolver.internal.ant.types.Dependency}
 * to specify transitive dependencies that should be excluded when the parent dependency is resolved.
 * Exclusions help avoid unwanted or conflicting artifacts that would otherwise be pulled in transitively.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * <dependency groupId="org.example" artifactId="example-lib" version="1.0.0">
 *   <exclusion groupId="commons-logging" artifactId="commons-logging"/>
 * </dependency>
 * }</pre>
 *
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><strong>groupId</strong> — the group ID of the dependency to exclude (required)</li>
 *   <li><strong>artifactId</strong> — the artifact ID of the dependency to exclude (required)</li>
 * </ul>
 *
 * <h2>Typical Use Cases:</h2>
 * <ul>
 *   <li>Preventing known problematic or unwanted transitive dependencies</li>
 *   <li>Ensuring consistency by excluding duplicates or old versions</li>
 *   <li>Controlling the effective dependency graph in complex builds</li>
 * </ul>
 *
 * <p>
 * The exclusion applies only to the specific dependency in which it is declared,
 * and does not affect other dependencies that may bring in the same artifact transitively.
 * </p>
 *
 * @see org.apache.maven.resolver.internal.ant.types.Dependency
 */
public class Exclusion extends DataType {

    private static final String WILDCARD = "*";

    private String groupId;

    private String artifactId;

    private String classifier;

    private String extension;

    /**
     * Default constructor for Exclusion.
     * <p>
     * This constructor is used when the Exclusion is created as a nested element in an Ant task.
     * </p>
     */
    public Exclusion() {
        // Default constructor
    }

    /**
     * Resolves this object if defined as a reference and verifies that it is a
     * {@code Exclusion} instance.
     *
     * @return the referenced {@code Exclusion} instance
     * @throws org.apache.tools.ant.BuildException if the reference is invalid
     *
     * @see #setRefid(org.apache.tools.ant.types.Reference)
     * @see #isReference()
     */
    protected Exclusion getRef() {
        return getCheckedRef(Exclusion.class);
    }

    /**
     * Validates that the required attributes of this exclusion are set.
     * <p>
     * Specifically, both {@code groupId} and {@code artifactId} must be provided
     * for the exclusion to be valid. If either is missing, this method throws
     * a {@link org.apache.tools.ant.BuildException}, indicating a configuration error
     * in the Ant build script.
     * </p>
     *
     * <p>
     * This method is typically called during task execution to ensure that the
     * exclusion is well-formed before it is passed to the underlying Maven model.
     * </p>
     *
     * @param task the Ant task context used for error reporting
     *
     * @throws org.apache.tools.ant.BuildException if {@code groupId} or {@code artifactId} is not set
     *
     * @see #setGroupId(String)
     * @see #setArtifactId(String)
     */
    public void validate(Task task) {
        if (isReference()) {
            getRef().validate(task);
        } else {
            if (groupId == null && artifactId == null && classifier == null && extension == null) {
                throw new BuildException(
                        "You must specify at least one of " + "'groupId', 'artifactId', 'classifier' or 'extension'");
            }
        }
    }

    @Override
    public void setRefid(Reference ref) {
        if (groupId != null || artifactId != null || extension != null || classifier != null) {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
    }

    /**
     * Returns the {@code groupId} of the dependency to exclude.
     * <p>
     * This is a required attribute that identifies the group (organization or project)
     * of the transitive dependency to be excluded. The exclusion will only be valid
     * if both {@code groupId} and {@code artifactId} are specified.
     * </p>
     *
     * @return the group ID of the excluded dependency, or {@code null} if not set
     *
     * @see #setGroupId(String)
     * @see #getArtifactId()
     * @see #validate(org.apache.tools.ant.Task)
     */
    public String getGroupId() {
        if (isReference()) {
            return getRef().getGroupId();
        }
        return (groupId != null) ? groupId : WILDCARD;
    }

    /**
     * Sets the {@code groupId} of the dependency to exclude.
     * <p>
     * The {@code groupId} identifies the group or organization that provides the
     * transitive dependency you want to exclude. This is a required attribute
     * for a valid exclusion.
     * </p>
     *
     * <p>
     * The value may include Ant properties (e.g., {@code ${excluded.groupId}}),
     * which will be resolved at runtime using the current Ant project context.
     * </p>
     *
     * @param groupId the group ID of the excluded dependency
     *
     * @see #getGroupId()
     * @see #setArtifactId(String)
     * @see #validate(org.apache.tools.ant.Task)
     */
    public void setGroupId(String groupId) {
        checkAttributesAllowed();
        if (this.groupId != null) {
            throw ambiguousCoords();
        }
        this.groupId = groupId;
    }

    /**
     * Returns the {@code artifactId} of the dependency to exclude.
     * <p>
     * This is a required attribute that specifies the artifact within the given {@code groupId}
     * that should be excluded from transitive dependency resolution.
     * </p>
     *
     * @return the artifact ID of the excluded dependency, or {@code null} if not set
     *
     * @see #setArtifactId(String)
     * @see #getGroupId()
     * @see #validate(org.apache.tools.ant.Task)
     */
    public String getArtifactId() {
        if (isReference()) {
            return getRef().getArtifactId();
        }
        return (artifactId != null) ? artifactId : WILDCARD;
    }

    /**
     * Sets the {@code artifactId} of the dependency to exclude.
     * <p>
     * The {@code artifactId} identifies the specific artifact (e.g., library or module name)
     * to exclude from the transitive dependency graph. This is a required attribute and must
     * be used in conjunction with {@code groupId}.
     * </p>
     *
     * <p>
     * The value may include Ant properties (e.g., {@code ${excluded.artifactId}}),
     * which will be resolved at runtime.
     * </p>
     *
     * @param artifactId the artifact ID of the excluded dependency
     *
     * @see #getArtifactId()
     * @see #setGroupId(String)
     * @see #validate(org.apache.tools.ant.Task)
     */
    public void setArtifactId(String artifactId) {
        checkAttributesAllowed();
        if (this.artifactId != null) {
            throw ambiguousCoords();
        }
        this.artifactId = artifactId;
    }

    /**
     * Returns the classifier of the dependency to exclude, if specified.
     * <p>
     * The classifier distinguishes artifacts that are built from the same
     * source but differ in content, such as {@code sources}, {@code javadoc},
     * or platform-specific variants.
     * </p>
     *
     * <p>
     * If no classifier is specified, this method returns {@code null}, and the exclusion
     * applies to all classifiers of the specified {@code groupId:artifactId}.
     * </p>
     *
     * @return the classifier of the excluded dependency, or {@code null} if not set
     *
     * @see #setClassifier(String)
     */
    public String getClassifier() {
        if (isReference()) {
            return getRef().getClassifier();
        }
        return (classifier != null) ? classifier : WILDCARD;
    }

    /**
     * Sets the classifier of the dependency to exclude.
     * <p>
     * The classifier distinguishes a variant of the artifact with the same {@code groupId},
     * {@code artifactId}, and {@code type}, such as {@code sources} or {@code javadoc}.
     * This is an optional attribute; if omitted, the exclusion will match all classifiers.
     * </p>
     *
     * <p>
     * The value may include Ant properties (e.g., {@code ${excluded.classifier}}),
     * which will be resolved at runtime using the Ant project context.
     * </p>
     *
     * @param classifier the classifier to match for exclusion
     *
     * @see #getClassifier()
     */
    public void setClassifier(String classifier) {
        checkAttributesAllowed();
        if (this.classifier != null) {
            throw ambiguousCoords();
        }
        this.classifier = classifier;
    }

    /**
     * Returns the extension (file type) of the dependency to exclude.
     * <p>
     * If this exclusion is a reference to another one (via {@code refid}),
     * the extension will be delegated to the referenced exclusion.
     * </p>
     *
     * <p>
     * If no extension has been explicitly set, this method returns a wildcard value
     * (usually {@code "*"}), indicating that the exclusion applies to all extensions
     * for the given coordinates.
     * </p>
     *
     * @return the extension to match for exclusion, or a wildcard if not explicitly set
     *
     * @see #setExtension(String)
     * @see #isReference()
     */
    public String getExtension() {
        if (isReference()) {
            return getRef().getExtension();
        }
        return (extension != null) ? extension : WILDCARD;
    }

    /**
     * Sets the extension (file type) of the dependency to exclude.
     * <p>
     * The extension typically corresponds to the artifact's file type,
     * such as {@code jar}, {@code pom}, {@code zip}, etc.
     * If not set, a wildcard value will be used to match any extension.
     * </p>
     *
     * <p>
     * This method may only be called if the exclusion is not defined as a reference
     * (i.e., {@code refid} is not set). If this exclusion is a reference,
     * or if the extension has already been set, this method will throw an exception.
     * </p>
     *
     * @param extension the file extension to exclude
     *
     * @throws org.apache.tools.ant.BuildException if this exclusion is a reference or the extension is already set
     *
     * @see #getExtension()
     * @see #isReference()
     */
    public void setExtension(String extension) {
        checkAttributesAllowed();
        if (this.extension != null) {
            throw ambiguousCoords();
        }
        this.extension = extension;
    }

    /**
     * Sets the exclusion coordinates using a compact colon-separated format.
     * <p>
     * This is a convenience method that allows you to specify the exclusion
     * as a single string in the format:
     * </p>
     * <pre>
     * groupId[:artifactId[:extension[:classifier]]]
     * </pre>
     *
     * <p>
     * Any omitted segments are automatically set to {@code "*"} (wildcard),
     * which matches all values for that component. For example:
     * </p>
     * <ul>
     *   <li>{@code com.example} → excludes all artifacts from {@code com.example}</li>
     *   <li>{@code com.example:my-lib} → excludes all extensions/classifiers of {@code my-lib}</li>
     *   <li>{@code com.example:my-lib:jar:sources} → excludes only {@code my-lib-*.jar:sources}</li>
     * </ul>
     *
     * <p>
     * This method may only be used if no individual attributes (such as {@code groupId},
     * {@code artifactId}, {@code extension}, or {@code classifier}) have been set,
     * and the exclusion is not a reference (i.e., {@code refid} is not set).
     * </p>
     *
     * @param coords the exclusion coordinates in colon-separated format
     *
     * @throws org.apache.tools.ant.BuildException if the exclusion is a reference,
     *         if any individual coordinate attributes are already set, or if the format is invalid
     *
     * @see #setGroupId(String)
     * @see #setArtifactId(String)
     * @see #setExtension(String)
     * @see #setClassifier(String)
     * @see #isReference()
     */
    public void setCoords(String coords) {
        checkAttributesAllowed();
        if (groupId != null || artifactId != null || extension != null || classifier != null) {
            throw ambiguousCoords();
        }
        Pattern p = Pattern.compile("([^: ]+)(:([^: ]+)(:([^: ]+)(:([^: ]*))?)?)?");
        Matcher m = p.matcher(coords);
        if (!m.matches()) {
            throw new BuildException("Bad exclusion coordinates '" + coords
                    + "', expected format is <groupId>[:<artifactId>[:<extension>[:<classifier>]]]");
        }
        groupId = m.group(1);
        artifactId = m.group(3);
        if (artifactId == null) {
            artifactId = "*";
        }
        extension = m.group(5);
        if (extension == null) {
            extension = "*";
        }
        classifier = m.group(7);
        if (classifier == null) {
            classifier = "*";
        }
    }

    private BuildException ambiguousCoords() {
        return new BuildException(
                "You must not specify both 'coords' and " + "('groupId', 'artifactId', 'extension', 'classifier')");
    }
}
