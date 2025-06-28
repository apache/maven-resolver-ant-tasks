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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * Represents a single Maven dependency in an Ant build script.
 * <p>
 * This Ant {@code DataType} defines the coordinates and metadata of a Maven dependency,
 * including its scope, type, classifier, and optional exclusions. It is intended to be used
 * as a nested element within a {@link Dependencies} container, which itself is used by tasks
 * such as {@link org.apache.maven.resolver.internal.ant.tasks.Resolve Resolve},
 * {@link org.apache.maven.resolver.internal.ant.tasks.CreatePom CreatePom},
 * {@link org.apache.maven.resolver.internal.ant.tasks.Install Install}, or
 * {@link org.apache.maven.resolver.internal.ant.tasks.Deploy Deploy}.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * <dependencies>
 *   <dependency
 *     groupId="org.apache.commons"
 *     artifactId="commons-lang3"
 *     version="3.12.0"
 *     scope="compile"
 *     optional="false"
 *     type="jar">
 *     <exclusion groupId="commons-logging" artifactId="commons-logging"/>
 *   </dependency>
 * </dependencies>
 * }</pre>
 *
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><strong>groupId</strong> (required) — the Maven coordinate group ID</li>
 *   <li><strong>artifactId</strong> (required) — the Maven artifact ID</li>
 *   <li><strong>version</strong> (required unless specified by a bom in the dependency management section) — the version of the dependency</li>
 *   <li><strong>scope</strong> — the Maven scope (e.g., {@code compile}, {@code runtime}, {@code test}, {@code system})</li>
 *   <li><strong>type</strong> — the artifact packaging type (default: {@code jar})</li>
 *   <li><strong>classifier</strong> — optional classifier (e.g., {@code sources}, {@code javadoc})</li>
 *   <li><strong>optional</strong> — whether the dependency is marked as optional (default: {@code false})</li>
 *   <li><strong>systemPath</strong> — file path to the artifact for {@code system}-scoped dependencies</li>
 * </ul>
 *
 <h2>Nested Elements:</h2>
 * <ul>
 *   <li>{@code <exclusion>} — zero or more exclusions to omit specific transitive dependencies.
 *     Each exclusion requires {@code groupId} and {@code artifactId} attributes.</li>
 * </ul>
 *
 * <h2>Typical Use Cases:</h2>
 * <ul>
 *   <li>Resolving direct project dependencies for compilation or testing</li>
 *   <li>Including dependencies in a generated POM or deployment descriptor</li>
 *   <li>Installing and deploying artifacts with well-defined transitive behavior</li>
 * </ul>
 *
 * @see Dependencies
 * @see org.apache.maven.resolver.internal.ant.tasks.Resolve
 * @see org.apache.maven.resolver.internal.ant.tasks.CreatePom
 * @see org.apache.maven.resolver.internal.ant.tasks.Install
 * @see org.apache.maven.resolver.internal.ant.tasks.Deploy
 */
public class Dependency extends DataType implements DependencyContainer {

    private String groupId;

    private String artifactId;

    private String version;

    private String classifier;

    private String type;

    private String scope;

    private File systemPath;

    private final List<Exclusion> exclusions = new ArrayList<>();

    /**
     * Default constructor that initializes the dependency with no settings.
     */
    public Dependency() {
        // Default constructor
    }
    /**
     * Returns the Ant reference associated with this dependency, if one is set.
     * <p>
     * This allows the dependency to refer to another predefined {@code <dependency>} or
     * {@code <dependencies>} element using the Ant {@code refid} mechanism.
     * When a reference is set, this object serves as an alias and inherits the configuration
     * of the referenced object.
     * </p>
     *
     * <p>
     * This is typically used to share dependency definitions across multiple tasks or containers.
     * </p>
     *
     * @return the Ant {@link org.apache.tools.ant.types.Reference}, or {@code null} if none is set
     *
     * @see #setRefid(org.apache.tools.ant.types.Reference)
     */
    protected Dependency getRef() {
        return getCheckedRef(Dependency.class);
    }

    @Override
    public void validate(Task task) {
        if (isReference()) {
            getRef().validate(task);
        } else {
            if (groupId == null || groupId.length() <= 0) {
                throw new BuildException("You must specify the 'groupId' for a dependency");
            }
            if (artifactId == null || artifactId.length() <= 0) {
                throw new BuildException("You must specify the 'artifactId' for a dependency");
            }
            if (version == null || version.length() <= 0) {
                throw new BuildException("You must specify the 'version' for a dependency");
            }

            if ("system".equals(scope)) {
                if (systemPath == null) {
                    throw new BuildException("You must specify 'systemPath' for dependencies with scope=system");
                }
            } else if (systemPath != null) {
                throw new BuildException("You may only specify 'systemPath' for dependencies with scope=system");
            }

            if (scope != null
                    && !"compile".equals(scope)
                    && !"provided".equals(scope)
                    && !"system".equals(scope)
                    && !"runtime".equals(scope)
                    && !"test".equals(scope)) {
                task.log("Unknown scope '" + scope + "' for dependency", Project.MSG_WARN);
            }

            for (Exclusion exclusion : exclusions) {
                exclusion.validate(task);
            }
        }
    }

    /**
     * Sets a reference to another dependency or dependencies container.
     * <p>
     * This allows the current {@code <dependency>} element to act as an alias to
     * another dependency or shared container defined elsewhere in the Ant build script.
     * The referenced object must be compatible (e.g., a {@code <dependency>} or {@code <dependencies>} instance),
     * and its configuration will be used in place of this one.
     * </p>
     *
     * <p>
     * This is useful for reusing dependency definitions across multiple tasks
     * without duplicating their configuration.
     * </p>
     *
     * <p>
     * Once a reference is set, all other local attributes on this element are ignored.
     * </p>
     *
     * @param ref the Ant {@link org.apache.tools.ant.types.Reference} to set
     *
     * @throws org.apache.tools.ant.BuildException if this instance is already configured locally
     *         and cannot accept a reference
     *
     * @see #getRef()
     */
    @Override
    public void setRefid(Reference ref) {
        if (groupId != null
                || artifactId != null
                || type != null
                || classifier != null
                || version != null
                || scope != null
                || systemPath != null) {
            throw tooManyAttributes();
        }
        if (!exclusions.isEmpty()) {
            throw noChildrenAllowed();
        }
        super.setRefid(ref);
    }

    /**
     * Returns the groupId of the dependency.
     * <p>
     * The {@code groupId} uniquely identifies the organization or project to which the artifact belongs.
     * It is a required part of the Maven coordinate and typically uses a reverse-domain naming convention
     * (e.g., {@code org.apache.commons}).
     * </p>
     *
     * <p>
     * The {@code groupId} is used in combination with {@code artifactId} and {@code version} to
     * uniquely resolve a dependency.
     * </p>
     *
     * @return the groupId (never {@code null} if properly configured)
     *
     * @see #setGroupId(String)
     * @see #getArtifactId()
     * @see #getVersion()
     */
    public String getGroupId() {
        if (isReference()) {
            return getRef().getGroupId();
        }
        return groupId;
    }

    /**
     * Sets the {@code groupId} of the dependency.
     * <p>
     * The {@code groupId} identifies the organization or project that provides the artifact.
     * It is a required coordinate in Maven and typically follows a reverse domain name pattern
     * (e.g., {@code org.apache.commons}).
     * </p>
     *
     * <p>
     * The value may include Ant properties (e.g., {@code ${project.groupId}}), which will be
     * resolved at runtime using the current {@link org.apache.tools.ant.Project} context.
     * </p>
     *
     * @param groupId the group identifier to set (must not be {@code null} or empty)
     *
     * @see #getGroupId()
     * @see #setArtifactId(String)
     * @see #setVersion(String)
     */
    public void setGroupId(String groupId) {
        checkAttributesAllowed();
        if (this.groupId != null) {
            throw ambiguousCoords();
        }
        this.groupId = groupId;
    }

    /**
     * Returns the artifactId of the dependency.
     * <p>
     * The artifactId uniquely identifies the artifact within a group and typically corresponds
     * to the base name of the artifact’s file. It is required for Maven coordinate resolution.
     * </p>
     *
     * <p>
     * Together with {@code groupId} and {@code version}, the artifactId forms the core identity
     * of a Maven dependency.
     * </p>
     *
     * @return the artifactId (never {@code null} if properly configured)
     *
     * @see #setArtifactId(String)
     * @see #getGroupId()
     * @see #getVersion()
     */
    public String getArtifactId() {
        if (isReference()) {
            return getRef().getArtifactId();
        }
        return artifactId;
    }

    /**
     * Sets the {@code artifactId} of the dependency.
     * <p>
     * The artifactId uniquely identifies an artifact within a {@code groupId}. It is a required
     * part of the Maven coordinate and typically corresponds to the name of the library or module.
     * </p>
     *
     * <p>
     * The value may include Ant properties (e.g., {@code ${lib.name}}), which will be resolved
     * at runtime using the current {@link org.apache.tools.ant.Project} context.
     * </p>
     *
     * @param artifactId the artifact identifier to set (must not be {@code null} or empty)
     *
     * @see #getArtifactId()
     * @see #setGroupId(String)
     * @see #setVersion(String)
     */
    public void setArtifactId(String artifactId) {
        checkAttributesAllowed();
        if (this.artifactId != null) {
            throw ambiguousCoords();
        }
        this.artifactId = artifactId;
    }

    /**
     * Returns the version of the dependency.
     * <p>
     * The version is a required component of the Maven coordinate unless it is being inherited
     * from a {@code <dependencyManagement>} section, such as through the import of a BOM (Bill of Materials).
     * In such cases, the version may be omitted, and the version defined in the imported BOM will apply.
     * </p>
     *
     * <p>
     * The value may contain Ant properties (e.g., {@code ${project.version}}), which will be
     * resolved at runtime using the current Ant project context.
     * </p>
     *
     * @return the explicitly set version of the dependency, or {@code null} if not set and expected to be inherited
     *
     * @see #setVersion(String)
     * @see org.apache.maven.resolver.internal.ant.types.DependencyManagement
     */
    public String getVersion() {
        if (isReference()) {
            return getRef().getVersion();
        }
        return version;
    }

    /**
     * Sets the version of the dependency.
     * <p>
     * The version identifies the specific release of the artifact to be resolved. In most cases,
     * this is a required attribute. However, if the version is provided through an imported BOM
     * in a {@code <dependencyManagement>} section, it may be omitted here and inherited automatically.
     * </p>
     *
     * <p>
     * The value may include Ant properties (e.g., {@code ${project.version}}), which will be resolved
     * at runtime using the current {@link org.apache.tools.ant.Project} context.
     * </p>
     *
     * <p>
     * If neither an explicit version nor an inherited version is available at resolution time,
     * a {@link org.apache.tools.ant.BuildException} will typically be thrown.
     * </p>
     *
     * @param version the version string to set, or {@code null} if it should be inherited
     *
     * @see #getVersion()
     * @see org.apache.maven.resolver.internal.ant.types.DependencyManagement
     */
    public void setVersion(String version) {
        checkAttributesAllowed();
        if (this.version != null) {
            throw ambiguousCoords();
        }
        this.version = version;
    }

    /**
     * Returns the classifier of the dependency, or {@code null} if none is set.
     * <p>
     * The classifier distinguishes artifacts that are built from the same source
     * but differ in content. For example, classifiers like {@code sources}, {@code javadoc},
     * or {@code tests} refer to alternate artifacts attached to the same Maven coordinates.
     * </p>
     *
     * <p>
     * If the classifier is not explicitly set, this method returns {@code null}, indicating
     * that the dependency refers to the main artifact for the given {@code groupId},
     * {@code artifactId}, {@code version}, and {@code type}.
     * </p>
     *
     * @return the classifier, or {@code null} if not specified
     *
     * @see #setClassifier(String)
     */
    public String getClassifier() {
        if (isReference()) {
            return getRef().getClassifier();
        }
        return classifier;
    }

    /**
     * Sets the classifier for the dependency.
     * <p>
     * A classifier distinguishes artifacts that are built from the same source but differ
     * in content or purpose. Examples include:
     * </p>
     * <ul>
     *   <li>{@code sources}</li>
     *   <li>{@code javadoc}</li>
     *   <li>{@code tests}</li>
     * </ul>
     *
     * <p>
     * Classifiers are optional and usually accompany a specific artifact type.
     * </p>
     *
     * @param classifier the classifier to set
     */
    public void setClassifier(String classifier) {
        checkAttributesAllowed();
        if (this.classifier != null) {
            throw ambiguousCoords();
        }
        this.classifier = classifier;
    }

    /**
     * Returns the type (or packaging) of the dependency artifact.
     * <p>
     * The type typically corresponds to the file extension of the artifact and determines
     * how it is resolved and handled. Common values include:
     * </p>
     * <ul>
     *   <li>{@code jar} (default)</li>
     *   <li>{@code pom}</li>
     *   <li>{@code war}</li>
     *   <li>{@code aar}</li>
     *   <li>{@code zip}, {@code tar.gz}, etc.</li>
     * </ul>
     *
     * <p>
     * If not explicitly set, this method returns {@code "jar"} as the default.
     * </p>
     *
     * @return the artifact type, never {@code null}; defaults to {@code "jar"} if unset
     *
     * @see #setType(String)
     */
    public String getType() {
        if (isReference()) {
            return getRef().getType();
        }
        return (type != null) ? type : "jar";
    }

    /**
     * Sets the type (packaging) of the artifact.
     * <p>
     * This corresponds to the Maven artifact type, typically matching the file extension.
     * Common values include:
     * </p>
     * <ul>
     *   <li>{@code jar} (default)</li>
     *   <li>{@code pom}</li>
     *   <li>{@code war}</li>
     *   <li>{@code aar}</li>
     *   <li>{@code zip}, {@code tar.gz}, etc.</li>
     * </ul>
     *
     * <p>
     * This affects both resolution and how the artifact appears in generated POMs.
     * </p>
     *
     * @param type the artifact type (packaging)
     */
    public void setType(String type) {
        checkAttributesAllowed();
        if (this.type != null) {
            throw ambiguousCoords();
        }
        this.type = type;
    }

    /**
     * Returns the Maven dependency scope.
     * <p>
     * The scope controls when and how the dependency is included in the classpath or packaged artifact.
     * Valid values include:
     * </p>
     * <ul>
     *   <li>{@code compile} (default)</li>
     *   <li>{@code provided}</li>
     *   <li>{@code runtime}</li>
     *   <li>{@code test}</li>
     *   <li>{@code system}</li>
     *   <li>{@code import}</li>
     * </ul>
     *
     * <p>
     * If the scope is not explicitly set, this method returns {@code "compile"}.
     * </p>
     *
     * @return the scope of the dependency; defaults to {@code "compile"} if unset
     *
     * @see #setScope(String)
     * @see #getSystemPath()
     */
    public String getScope() {
        if (isReference()) {
            return getRef().getScope();
        }
        return (scope != null) ? scope : "";
    }

    /**
     * Sets the Maven dependency scope.
     * <p>
     * The scope determines the classpath visibility and transitivity of the dependency.
     * Supported values are:
     * </p>
     * <ul>
     *   <li>{@code compile} (default) – available in all classpaths and inherited transitively</li>
     *   <li>{@code provided} – required at compile time but not packaged (e.g., servlet APIs)</li>
     *   <li>{@code runtime} – not required at compile time, but needed at runtime</li>
     *   <li>{@code test} – used only for test compilation and execution</li>
     *   <li>{@code system} – similar to provided but with a fixed local {@code systemPath}</li>
     *   <li>{@code import} – used only within {@code dependencyManagement} (typically unused in Ant)</li>
     * </ul>
     *
     * <p>
     * If omitted, the default scope is {@code compile}.
     * </p>
     *
     * @param scope the Maven scope to set
     *
     * @see #setSystemPath(File)
     */
    public void setScope(String scope) {
        checkAttributesAllowed();
        if (this.scope != null) {
            throw ambiguousCoords();
        }
        this.scope = scope;
    }

    /**
     * Sets the Maven coordinates of the dependency using a colon-separated string.
     * <p>
     * This is a convenience method that allows setting multiple fields at once.
     * The supported formats are:
     * </p>
     * <ul>
     *   <li>{@code groupId:artifactId:version}</li>
     *   <li>{@code groupId:artifactId:packaging:version}</li>
     *   <li>{@code groupId:artifactId:packaging:classifier:version}</li>
     * </ul>
     *
     * <p>
     * For example:
     * </p>
     * <ul>
     *   <li>{@code org.apache.commons:commons-lang3:3.12.0}</li>
     *   <li>{@code com.example:lib:jar:sources:1.0.0}</li>
     * </ul>
     *
     * <p>
     * This method will parse and assign the corresponding fields:
     * {@code groupId}, {@code artifactId}, {@code type} (packaging),
     * {@code classifier}, and {@code version}.
     * </p>
     *
     * <p>
     * The string may include Ant properties (e.g., {@code ${groupId}:my-artifact:${version}}),
     * which will be resolved at runtime.
     * </p>
     *
     * @param coords the colon-separated Maven coordinate string
     *
     * @throws org.apache.tools.ant.BuildException if the coordinate string is malformed
     *
     * @see #setGroupId(String)
     * @see #setArtifactId(String)
     * @see #setVersion(String)
     * @see #setType(String)
     * @see #setClassifier(String)
     */
    public void setCoords(String coords) {
        checkAttributesAllowed();
        if (groupId != null
                || artifactId != null
                || version != null
                || type != null
                || classifier != null
                || scope != null) {
            throw ambiguousCoords();
        }
        Pattern p = Pattern.compile("([^: ]+):([^: ]+):([^: ]+)((:([^: ]+)(:([^: ]+))?)?:([^: ]+))?");
        Matcher m = p.matcher(coords);
        if (!m.matches()) {
            throw new BuildException("Bad dependency coordinates '" + coords
                    + "', expected format is <groupId>:<artifactId>:<version>[[:<type>[:<classifier>]]:<scope>]");
        }
        groupId = m.group(1);
        artifactId = m.group(2);
        version = m.group(3);
        type = m.group(6);
        if (type == null || type.length() <= 0) {
            type = "jar";
        }
        classifier = m.group(8);
        if (classifier == null) {
            classifier = "";
        }
        scope = m.group(9);
    }

    /**
     * Sets the system path to the artifact file for a dependency with {@code scope="system"}.
     * <p>
     * This specifies a local file path to be used as the artifact for this dependency,
     * bypassing repository resolution. This is only applicable if the scope is explicitly
     * set to {@code system}.
     * </p>
     *
     * <p>
     * The path may include Ant properties (e.g., {@code ${basedir}/lib/my-lib.jar}) which will be resolved
     * at runtime using the current {@link org.apache.tools.ant.Project} context.
     * </p>
     *
     * <p><strong>Note:</strong> This attribute is ignored unless {@code scope} is {@code system}.</p>
     *
     * @param systemPath the file path to use for the system-scoped dependency
     *
     * @see #getSystemPath()
     * @see #setScope(String)
     */
    public void setSystemPath(File systemPath) {
        checkAttributesAllowed();
        this.systemPath = systemPath;
    }

    /**
     * Returns the system path file attribute for this dependency, if {@code scope} is set to {@code system}.
     * <p>
     * The system path points to a local file that provides the dependency artifact,
     * bypassing standard repository resolution. This is only valid and meaningful when
     * the dependency scope is set to {@code system}.
     * </p>
     *
     * <p>
     * If the scope is not {@code system}, this value is ignored.
     * </p>
     *
     * @return A File representing the system path to the dependency file, or {@code null} if not set
     *
     * @see #setSystemPath(File)
     * @see #setScope(String)
     */
    public File getSystemPath() {
        if (isReference()) {
            return getRef().getSystemPath();
        }
        return systemPath;
    }

    /**
     * Returns a versionless key for the dependency, composed of {@code groupId:artifactId[:type[:classifier]]}.
     * <p>
     * This is useful for identifying dependencies regardless of version, such as when
     * performing conflict resolution, exclusions, or matching in dependency management sections.
     * </p>
     *
     * <p>
     * The returned string includes:
     * </p>
     * <ul>
     *   <li>{@code groupId}</li>
     *   <li>{@code artifactId}</li>
     *   <li>{@code type} (if not {@code jar})</li>
     *   <li>{@code classifier} (if present)</li>
     * </ul>
     *
     * <p>
     * Example: {@code org.apache.commons:commons-lang3} or {@code com.example:lib:zip:sources}
     * </p>
     *
     * @return a versionless coordinate key representing the dependency
     */
    public String getVersionlessKey() {
        if (isReference()) {
            return getRef().getVersionlessKey();
        }
        StringBuilder key = new StringBuilder(128);
        if (groupId != null) {
            key.append(groupId);
        }
        key.append(':');
        if (artifactId != null) {
            key.append(artifactId);
        }
        key.append(':');
        key.append((type != null) ? type : "jar");
        if (classifier != null && !classifier.isEmpty()) {
            key.append(':');
            key.append(classifier);
        }
        return key.toString();
    }

    /**
     * Allows ant to add a specified exclusion to omit a specific transitive dependency.
     *
     * @param exclusion the {@link Exclusion Exclusion} to omit
     */
    public void addExclusion(Exclusion exclusion) {
        checkChildrenAllowed();
        this.exclusions.add(exclusion);
    }

    /**
     * Returns the list of exclusions declared for this dependency.
     * <p>
     * Exclusions are used to prevent specific transitive dependencies from being included
     * during resolution. Each exclusion specifies a {@code groupId} and {@code artifactId}
     * of a dependency that should be omitted when this dependency is resolved transitively.
     * </p>
     *
     * <p>
     * Exclusions are typically used to avoid version conflicts, remove unwanted libraries,
     * or override defaults inherited through dependency chains.
     * </p>
     *
     * @return a list of {@link Exclusion} elements; may be empty but never {@code null}
     *
     * @see Exclusion
     * @see #addExclusion(Exclusion)
     */
    public List<Exclusion> getExclusions() {
        if (isReference()) {
            return getRef().getExclusions();
        }
        return exclusions;
    }

    private BuildException ambiguousCoords() {
        return new BuildException("You must not specify both 'coords' and "
                + "('groupId', 'artifactId', 'version', 'extension', 'classifier', 'scope')");
    }
}
