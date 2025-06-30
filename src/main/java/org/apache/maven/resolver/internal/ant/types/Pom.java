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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.model.Model;
import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.maven.resolver.internal.ant.ProjectWorkspaceReader;
import org.apache.maven.resolver.internal.ant.tasks.RefTask;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Reference;

/**
 * Represents a Maven POM file as an Ant {@code DataType}.
 * <p>
 * This type allows an existing {@code pom.xml} file to be loaded into the Ant build environment.
 * The POM's metadata (such as groupId, artifactId, version, etc.) can be accessed directly or reused
 * in other Maven Resolver tasks like {@link org.apache.maven.resolver.internal.ant.tasks.Deploy Deploy},
 * {@link org.apache.maven.resolver.internal.ant.tasks.Install Install}
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * <pom file="target/generated-pom.xml" id="my.pom"/>
 *
 * <echo message="Group ID is ${pom.groupId}"/>
 * <echo message="Version is ${pom.version}"/>
 *
 * <deploy>
 *   <pom refid="my.pom"/>
 *   <repository id="release" url="https://repo.mycompany.com/releases"/>
 * </deploy>
 * }</pre>
 *
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><strong>file</strong> — the path to the {@code pom.xml} file to load</li>
 * </ul>
 *
 * <h2>Exposed Ant Properties:</h2>
 * When a POM is loaded, the following properties become available for use in your build script:
 * <ul>
 *   <li>{@code ${pom.groupId}}</li>
 *   <li>{@code ${pom.artifactId}}</li>
 *   <li>{@code ${pom.version}}</li>
 *   <li>{@code ${pom.packaging}}</li>
 *   <li>{@code ${pom.name}}</li>
 *   <li>{@code ${pom.description}} (if present)</li>
 * </ul>
 *
 * These properties enable consistent reuse of project metadata across Ant targets and tasks.
 * <p>
 * If you want a different property prefix, you can set it when you register the POM, e.g.:
 * </p>
 * <pre>{@code
 * <pom file="target/generated-pom.xml" id="my.pom" prefix="lib"/>
 * }</pre>
 *
 * <h2>Typical Use Cases:</h2>
 * <ul>
 *   <li>Reusing existing Maven coordinates in Ant-based deployments</li>
 *   <li>Injecting POM metadata into artifact creation or reporting tasks</li>
 *   <li>Keeping build logic in sync with Maven configuration</li>
 * </ul>
 *
 * @see org.apache.maven.resolver.internal.ant.tasks.Deploy
 * @see org.apache.maven.resolver.internal.ant.tasks.Install
 * @see org.apache.maven.resolver.internal.ant.tasks.CreatePom
 */
public class Pom extends RefTask {

    private Model model;

    private String id;

    private File file;

    private String groupId;

    private String artifactId;

    private String version;

    private String packaging = "jar";

    private RemoteRepositories remoteRepositories;

    private String coords;

    /**
     * Creates an empty {@code Pom} instance.
     * <p>
     * Attributes such as {@code groupId}, {@code artifactId}, and {@code version} can be set individually,
     * or the POM can be initialized from a {@code file} or via a reference ({@code refid}).
     * </p>
     *
     * <p>
     * Ant uses this constructor when instantiating the datatype from a build script.
     * </p>
     *
     * @see #setGroupId(String)
     * @see #setArtifactId(String)
     * @see #setVersion(String)
     * @see #setFile(java.io.File)
     * @see #setRefid(org.apache.tools.ant.types.Reference)
     */
    public Pom() {
        // Default constructor for Ant task
    }

    /**
     * Returns the referenced {@code Pom} instance if this object is defined as a reference.
     * <p>
     * This method is used internally to delegate property access and behavior to the referenced
     * {@code Pom} when {@link #isReference()} returns {@code true}.
     * It performs type checking to ensure the reference is of type {@code Pom}.
     * </p>
     *
     * @return the referenced {@code Pom} instance
     *
     * @throws org.apache.tools.ant.BuildException if the reference is missing or not of the expected type
     *
     * @see #isReference()
     * @see #getCheckedRef()
     */
    protected Pom getRef() {
        return (Pom) getCheckedRef();
    }

    /**
     * Validates that this POM definition is complete and usable.
     * <p>
     * If this object is a reference, validation is delegated to the referenced {@code Pom}.
     * Otherwise, the method checks that either a {@link #setFile(File) POM file} is provided,
     * or all of the required Maven coordinates — {@code groupId}, {@code artifactId}, and {@code version} —
     * are explicitly set.
     * </p>
     *
     * @throws org.apache.tools.ant.BuildException if required attributes are missing
     *         and no POM file is specified
     *
     * @see #isReference()
     * @see #setFile(File)
     * @see #setGroupId(String)
     * @see #setArtifactId(String)
     * @see #setVersion(String)
     */
    public void validate() {
        if (isReference()) {
            getRef().validate();
        } else {
            if (file == null) {
                if (groupId == null) {
                    throw new BuildException("You must specify the 'groupId' for the POM");
                }
                if (artifactId == null) {
                    throw new BuildException("You must specify the 'artifactId' for the POM");
                }
                if (version == null) {
                    throw new BuildException("You must specify the 'version' for the POM");
                }
            }
        }
    }

    @Override
    public void setRefid(Reference ref) {
        if (id != null || file != null || groupId != null || artifactId != null || version != null) {
            throw tooManyAttributes();
        }
        if (remoteRepositories != null) {
            throw noChildrenAllowed();
        }
        super.setRefid(ref);
    }

    /**
     * Sets the internal identifier for this POM.
     * <p>
     * This {@code id} is not the same as Ant's built-in {@code id} attribute, but may be used
     * internally by the task logic (e.g., for naming, logging, or referencing the POM).
     * </p>
     *
     * <p>
     * This method must not be used if this object is defined as a reference ({@code refid}),
     * or if attributes are otherwise disallowed in the current context.
     * </p>
     *
     * @param id the internal identifier to assign
     *
     * @throws org.apache.tools.ant.BuildException if attribute usage is disallowed
     *
     * @see #isReference()
     */
    public void setId(String id) {
        checkAttributesAllowed();
        this.id = id;
    }

    /**
     * Returns the file that was set via {@link #setFile(File)} to load the POM from.
     * <p>
     * If a file was specified, it is used as the source for reading the POM’s metadata
     * instead of defining coordinates manually. This is useful when you want to reference
     * an existing {@code pom.xml} file directly.
     * </p>
     *
     * <p>
     * If this {@code Pom} is defined as a reference ({@code refid}),
     * the file is retrieved from the referenced {@code Pom} instance.
     * </p>
     *
     * @return the POM file, or {@code null} if not set
     *
     * @see #setFile(File)
     * @see #isReference()
     */
    public File getFile() {
        if (isReference()) {
            return getRef().getFile();
        }
        return file;
    }

    /**
     * Sets the file from which to load the POM.
     * <p>
     * This is an alternative to specifying Maven coordinates manually. When set,
     * the POM's metadata is loaded directly from the given file.
     * </p>
     *
     * <p>This method must not be used in combination with:</p>
     * <ul>
     *   <li>{@link #setGroupId(String)}, {@link #setArtifactId(String)}, {@link #setVersion(String)}, or {@link #setCoords(String)}</li>
     *   <li>or if this object is defined as a {@code refid}</li>
     * </ul>
     * <p>
     * Doing so will raise a {@link org.apache.tools.ant.BuildException}.
     * </p>
     *
     * @param file the file containing the POM
     *
     * @throws org.apache.tools.ant.BuildException if attributes are in conflict or not allowed
     *
     * @see #getFile()
     * @see #setCoords(String)
     * @see #isReference()
     */
    public void setFile(File file) {
        checkAttributesAllowed();
        if (groupId != null || artifactId != null || version != null) {
            throw ambiguousSource();
        }

        this.file = file;
    }

    /**
     * Returns the {@code groupId} of the POM.
     * <p>
     * The group ID uniquely identifies the organization or project to which the artifact belongs,
     * such as {@code org.apache.maven}. This value is typically required when manually specifying
     * Maven coordinates using {@link #setGroupId(String)} or {@link #setCoords(String)}.
     * </p>
     *
     * <p>
     * If this {@code Pom} is defined as a reference ({@code refid}),
     * the value is delegated to the referenced {@code Pom} instance.
     * </p>
     *
     * @return the group ID, or {@code null} if not set
     *
     * @see #setGroupId(String)
     * @see #isReference()
     */
    public String getGroupId() {
        if (isReference()) {
            return getRef().getGroupId();
        }
        return groupId;
    }

    /**
     * Sets the {@code groupId} for the POM.
     * <p>
     * The group ID typically represents the project's organization or domain,
     * such as {@code org.apache.maven} or {@code com.example}.
     * This value is required when specifying coordinates manually.
     * </p>
     *
     * <p>This method must not be called if:</p>
     * <ul>
     *   <li>{@code groupId} has already been set (directly or via {@link #setCoords(String)}),</li>
     *   <li>or if a POM file has been specified via {@link #setFile(java.io.File)},</li>
     *   <li>or if this object is defined via a {@code refid}.</li>
     * </ul>
     * <p>
     * Violations will result in a {@link org.apache.tools.ant.BuildException}.
     * </p>
     *
     * @param groupId the Maven group ID
     *
     * @throws org.apache.tools.ant.BuildException if attributes are in conflict or not allowed
     *
     * @see #getGroupId()
     * @see #setArtifactId(String)
     * @see #setVersion(String)
     * @see #setCoords(String)
     * @see #setFile(java.io.File)
     * @see #isReference()
     */
    public void setGroupId(String groupId) {
        checkAttributesAllowed();
        if (this.groupId != null) {
            throw ambiguousCoords();
        }
        if (file != null) {
            throw ambiguousSource();
        }
        this.groupId = groupId;
    }

    /**
     * Returns the {@code artifactId} of the POM.
     * <p>
     * The artifact ID is the name that uniquely identifies the project within its group,
     * such as {@code maven-resolver-ant-tasks}.
     * </p>
     *
     * <p>
     * If this {@code Pom} is defined as a reference ({@code refid}),
     * the value is retrieved from the referenced instance.
     * </p>
     *
     * @return the artifact ID, or {@code null} if not set
     *
     * @see #setArtifactId(String)
     * @see #isReference()
     */
    public String getArtifactId() {
        if (isReference()) {
            return getRef().getArtifactId();
        }
        return artifactId;
    }

    /**
     * Sets the {@code artifactId} for the POM.
     * <p>
     * The artifact ID identifies the project within its group, for example: {@code guice}, {@code junit}, or {@code my-module}.
     * This value is required when manually specifying the Maven coordinates (groupId, artifactId, version).
     * </p>
     *
     * <p>This method must not be called if:</p>
     * <ul>
     *   <li>{@code artifactId} was already set (directly or via {@link #setCoords(String)}),</li>
     *   <li>or if a {@code file} was specified via {@link #setFile(java.io.File)},</li>
     *   <li>or if this instance is a reference ({@code refid}).</li>
     * </ul>
     * <p>
     * Violating these constraints will result in a {@link org.apache.tools.ant.BuildException}.
     * </p>
     *
     * @param artifactId the Maven artifact ID
     *
     * @throws org.apache.tools.ant.BuildException if attributes are in conflict or not allowed
     *
     * @see #getArtifactId()
     * @see #setGroupId(String)
     * @see #setVersion(String)
     * @see #setCoords(String)
     * @see #setFile(java.io.File)
     * @see #isReference()
     */
    public void setArtifactId(String artifactId) {
        checkAttributesAllowed();
        if (this.artifactId != null) {
            throw ambiguousCoords();
        }
        if (file != null) {
            throw ambiguousSource();
        }
        this.artifactId = artifactId;
    }

    /**
     * Returns the {@code version} of the Maven artifact defined by this POM.
     * <p>
     * The version is one of the required coordinates for uniquely identifying a Maven artifact.
     * If this object is defined as a reference ({@code refid}), the version is retrieved from the referenced instance.
     * </p>
     *
     * <p>
     * If this POM is resolved from a file and uses a {@code dependencyManagement} section (e.g. via a BOM),
     * the version may be inherited and not explicitly required here.
     * </p>
     *
     * @return the version of the artifact, or {@code null} if not set and not resolved
     *
     * @see #setVersion(String)
     * @see #isReference()
     */
    public String getVersion() {
        if (isReference()) {
            return getRef().getVersion();
        }
        return version;
    }

    /**
     * Sets the {@code version} of the POM.
     * <p>
     * This value defines the version of the project artifact being referenced,
     * such as {@code 1.0.0} or {@code 2.5-SNAPSHOT}.
     * </p>
     *
     * <p>This method must not be used if:</p>
     * <ul>
     *   <li>{@code version} is already set (directly or via {@link #setCoords(String)}),</li>
     *   <li>a POM file is specified via {@link #setFile(java.io.File)},</li>
     *   <li>or this instance is defined as a {@code refid}.</li>
     * </ul>
     * <p>
     * Violating these constraints will cause a {@link org.apache.tools.ant.BuildException}.
     * </p>
     *
     * @param version the Maven version
     *
     * @throws org.apache.tools.ant.BuildException if attributes are in conflict or not allowed
     *
     * @see #getVersion()
     * @see #setGroupId(String)
     * @see #setArtifactId(String)
     * @see #setCoords(String)
     * @see #setFile(java.io.File)
     * @see #isReference()
     */
    public void setVersion(String version) {
        checkAttributesAllowed();
        if (this.version != null) {
            throw ambiguousCoords();
        }
        if (file != null) {
            throw ambiguousSource();
        }
        this.version = version;
    }

    /**
     * Returns the raw Maven coordinates string that was set via {@link #setCoords(String)}.
     * <p>
     * This string is typically in the format {@code groupId:artifactId:version}, but may also include
     * optional parts such as {@code :extension} or {@code :classifier} depending on the usage context.
     * </p>
     *
     * <p>
     * If this {@code Pom} is defined as a reference ({@code refid}), the value is retrieved
     * from the referenced instance.
     * </p>
     *
     * @return the raw coordinate string, or {@code null} if not set
     *
     * @see #setCoords(String)
     * @see #isReference()
     */
    public String getCoords() {
        if (isReference()) {
            return getRef().getCoords();
        }
        return coords;
    }

    /**
     * Sets the Maven coordinates for this POM using a compact string format.
     * <p>
     * The expected format is {@code groupId:artifactId:version}, where:
     * </p>
     * <ul>
     *   <li>{@code groupId} is the group ID of the artifact, e.g. {@code org.apache.maven}</li>
     *   <li>{@code artifactId} is the artifact ID, e.g. {@code maven-core}</li>
     *   <li>{@code version} is the version, e.g. {@code 3.9.0}</li>
     * </ul>
     *
     * <p>
     * This method provides a concise alternative to setting each of {@link #setGroupId(String)},
     * {@link #setArtifactId(String)}, and {@link #setVersion(String)} individually.
     * </p>
     *
     * <p><b>Mutual exclusion:</b> This method must not be used in combination with:</p>
     * <ul>
     *   <li>{@link #setGroupId(String)}, {@link #setArtifactId(String)}, or {@link #setVersion(String)}</li>
     *   <li>{@link #setFile(java.io.File)}</li>
     *   <li>{@link #setRefid(org.apache.tools.ant.types.Reference)}</li>
     * </ul>
     * Violating this constraint will result in a {@link org.apache.tools.ant.BuildException}.
     *
     * @param coords the Maven coordinates in the format {@code groupId:artifactId:version}
     *
     * @throws org.apache.tools.ant.BuildException if the format is invalid or attributes conflict
     *
     * @see #getCoords()
     * @see #setGroupId(String)
     * @see #setArtifactId(String)
     * @see #setVersion(String)
     * @see #setFile(java.io.File)
     * @see #setRefid(org.apache.tools.ant.types.Reference)
     */
    public void setCoords(String coords) {
        checkAttributesAllowed();
        if (file != null) {
            throw ambiguousSource();
        }
        if (groupId != null || artifactId != null || version != null) {
            throw ambiguousCoords();
        }
        Pattern p = Pattern.compile("([^: ]+):([^: ]+):([^: ]+)");
        Matcher m = p.matcher(coords);
        if (!m.matches()) {
            throw new BuildException("Bad POM coordinates, expected format is <groupId>:<artifactId>:<version>");
        }
        groupId = m.group(1);
        artifactId = m.group(2);
        version = m.group(3);
    }

    private BuildException ambiguousCoords() {
        return new BuildException("You must not specify both 'coords' and ('groupId', 'artifactId', 'version')");
    }

    private BuildException ambiguousSource() {
        return new BuildException(
                "You must not specify both 'file' and " + "('coords', 'groupId', 'artifactId', 'version')");
    }

    /**
     * Returns the packaging type of the POM, such as {@code jar}, {@code pom}, or {@code war}.
     * <p>
     * This value is typically set via {@link #setPackaging(String)} or resolved from a parsed POM file.
     * If not explicitly set, the default packaging is usually {@code jar}.
     * </p>
     *
     * <p>
     * If this {@code Pom} is defined as a reference ({@code refid}),
     * the packaging is retrieved from the referenced {@code Pom} instance.
     * </p>
     *
     * @return the packaging type, or {@code null} if not set
     *
     * @see #setPackaging(String)
     * @see #isReference()
     */
    public String getPackaging() {
        if (isReference()) {
            return getRef().getPackaging();
        }
        return packaging;
    }

    /**
     * Sets the packaging type of the POM, such as {@code jar}, {@code war}, {@code pom}, etc.
     * <p>
     * This value determines how the artifact is packaged and what build lifecycle it uses.
     * If not specified, Maven assumes a default of {@code jar}.
     * </p>
     *
     * <p>
     * This method must not be used if a POM file has been specified via {@link #setFile(java.io.File)},
     * or if this object is defined as a reference ({@code refid}). Violating these constraints
     * will result in a {@link org.apache.tools.ant.BuildException}.
     * </p>
     *
     * @param packaging the packaging type to assign to the POM
     *
     * @throws org.apache.tools.ant.BuildException if attributes are in conflict or not allowed
     *
     * @see #getPackaging()
     * @see #setFile(java.io.File)
     * @see #isReference()
     */
    public void setPackaging(String packaging) {
        checkAttributesAllowed();
        if (file != null) {
            throw ambiguousSource();
        }
        this.packaging = packaging;
    }

    /**
     * Returns the internal {@link RemoteRepositories} container associated with this POM.
     * <p>
     * This container holds the list of remote repositories that may be used when resolving
     * the POM from a file, including parent POMs, dependencies, and plugin artifacts.
     * </p>
     *
     * <p>
     * If the container has not been initialized yet, this method will lazily create and return it.
     * </p>
     *
     * @return the {@code RemoteRepositories} instance associated with this POM
     *
     * @see #addRemoteRepos(RemoteRepositories)
     * @see RemoteRepository
     */
    private RemoteRepositories getRemoteRepos() {
        if (remoteRepositories == null) {
            remoteRepositories = new RemoteRepositories();
            remoteRepositories.setProject(getProject());
        }
        return remoteRepositories;
    }

    /**
     * Adds a single remote repository to this POM's list of repositories.
     * <p>
     * These repositories are used when resolving artifacts related to this POM,
     * such as parent POMs or dependencies, especially when the POM is loaded from a file.
     * </p>
     *
     * <p>
     * This method delegates to {@link #getRemoteRepos()} and appends the given
     * {@link RemoteRepository} to the internal {@link RemoteRepositories} container.
     * </p>
     *
     * @param repository the remote repository to add
     *
     * @see #getRemoteRepos()
     * @see RemoteRepository
     * @see RemoteRepositories
     */
    public void addRemoteRepo(RemoteRepository repository) {
        getRemoteRepos().addRemoterepo(repository);
    }

    /**
     * Adds a {@link RemoteRepositories} container to this POM, which holds one or more remote repositories
     * used to resolve dependencies and parent POMs when the POM is loaded from a file.
     * <p>
     * This method delegates to {@link #getRemoteRepos()} and appends the given repository container
     * to the list of remote repositories associated with this POM.
     * </p>
     *
     * @param repositories a container of remote repositories to add
     *
     * @see #getRemoteRepos()
     * @see RemoteRepositories#addRemoterepos(RemoteRepositories)
     * @see RemoteRepository
     */
    public void addRemoteRepos(RemoteRepositories repositories) {
        getRemoteRepos().addRemoterepos(repositories);
    }

    /**
     * Adds a reference to a {@link RemoteRepositories} instance to this POM's list of remote repositories.
     * <p>
     * This allows remote repositories to be defined elsewhere in the build script and referenced by ID,
     * promoting reuse and separation of concerns.
     * </p>
     *
     * <p>
     * Internally, this method creates a new {@code RemoteRepositories} object, sets the reference ID,
     * and delegates to {@link #getRemoteRepos()} to append it to the list.
     * </p>
     *
     * @param ref the Ant {@code Reference} to a {@code RemoteRepositories} definition
     *
     * @see RemoteRepositories
     * @see #addRemoteRepos(RemoteRepositories)
     * @see #getRemoteRepos()
     */
    public void setRemoteReposRef(Reference ref) {
        RemoteRepositories repos = new RemoteRepositories();
        repos.setProject(getProject());
        repos.setRefid(ref);
        getRemoteRepos().addRemoterepos(repos);
    }

    /**
     * Returns the parsed Maven {@link org.apache.maven.model.Model} associated with this POM.
     * <p>
     * If a {@code file} has been specified via {@link #setFile(java.io.File)},
     * the model is lazily loaded and cached using the {@link AntRepoSys} instance
     * for the current Ant {@link org.apache.tools.ant.Project}. If no file is set,
     * this method returns {@code null}.
     * </p>
     *
     * <p>
     * If this {@code Pom} is defined as a reference ({@code refid}),
     * the model is retrieved from the referenced {@code Pom} instance.
     * </p>
     *
     * <p>
     * This method is thread-safe and performs lazy loading: the model is loaded only once
     * and reused on subsequent calls.
     * </p>
     *
     * @param task the Ant task context used for logging and error reporting during model loading
     * @return the Maven {@code Model} parsed from the specified POM file, or {@code null} if no file was set
     *
     * @see #setFile(java.io.File)
     * @see #isReference()
     * @see org.apache.maven.model.Model
     */
    public Model getModel(Task task) {
        if (isReference()) {
            return getRef().getModel(task);
        }
        synchronized (this) {
            if (model == null) {
                if (file != null) {
                    model = AntRepoSys.getInstance(getProject()).loadModel(task, file, true, remoteRepositories);
                }
            }
            return model;
        }
    }

    @Override
    public void execute() {
        validate();

        if (file != null && (id == null || AntRepoSys.getInstance(getProject()).getDefaultPom() == null)) {
            AntRepoSys.getInstance(getProject()).setDefaultPom(this);
        }

        ProjectWorkspaceReader.getInstance().addPom(this);

        Model model = getModel(this);

        if (model == null) {
            coords = getGroupId() + ":" + getArtifactId() + ":" + getVersion();
            return;
        }

        coords = model.getGroupId() + ":" + model.getArtifactId() + ":" + model.getVersion();

        ModelValueExtractor extractor = new ModelValueExtractor(id, model, getProject());

        PropertyHelper propHelper = PropertyHelper.getPropertyHelper(getProject());

        try {
            // Ant 1.8.0 delegate
            PomPropertyEvaluator.register(extractor, propHelper);
        } catch (LinkageError e) {
            // Ant 1.6 - 1.7.1 interceptor chaining
            PomPropertyHelper.register(extractor, propHelper);
        }
    }

    @Override
    public String toString() {
        return coords + " (" + super.toString() + ")";
    }
}
