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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.maven.resolver.internal.ant.Names;
import org.apache.maven.resolver.internal.ant.types.Dependencies;
import org.apache.maven.resolver.internal.ant.types.Pom;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Resources;
import org.apache.tools.ant.util.FileUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;

/**
 * Ant task to resolve dependencies using Maven Resolver.
 * <p>
 * This task reads dependency and repository definitions (either inline or via references)
 * and resolves them according to the specified scopes and remote repositories. Resolved artifacts
 * can be stored in Ant references or used for further processing (e.g., setting up classpaths).
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * <resolve>
 *   <dependencies>
 *     <dependency groupId="org.apache.commons" artifactId="commons-lang3" version="3.18.0"/>
 *   </dependencies>
 *   <repositories>
 *     <repository id="central" url="https://repo.maven.apache.org/maven2"/>
 *   </repositories>
 *   <path id="my.classpath"/>
 * </resolve>
 * }</pre>
 *
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><strong>failOnMissingDescriptor</strong> — whether to fail if a POM file cannot be resolved (default: false)</li>
 *   <li><strong>offline</strong> — whether to operate in offline mode (default: false)</li>
 * </ul>
 *
 * <h2>Nested Elements:</h2>
 * <ul>
 *   <li>{@code <dependencies>} — defines one or more dependencies to resolve</li>
 *   <li>{@code <repositories>} — a container for one or more {@code <repository>} elements</li>
 *   <li>{@code <repository>} — specifies a remote Maven repository</li>
 *   <li>{@code <path>} — optionally defines an Ant path to which resolved artifacts are added</li>
 * </ul>
 *
 * <h2>Ant References Created:</h2>
 * <ul>
 *   <li>May register resolved artifacts under a path reference if {@code <path>} is used</li>
 * </ul>
 *
 * <h2>Typical Use Cases:</h2>
 * <ul>
 *   <li>Resolving Maven artifacts for use in compilation, testing, or runtime</li>
 *   <li>Dynamically constructing classpaths using Maven coordinates</li>
 * </ul>
 *
 * @see org.apache.maven.resolver.internal.ant.types.Dependencies
 * @see org.apache.maven.resolver.internal.ant.types.RemoteRepository
 * @see org.apache.tools.ant.types.Path
 */
public class Resolve extends AbstractResolvingTask {

    private final List<ArtifactConsumer> consumers = new ArrayList<>();

    private boolean failOnMissingAttachments;

    /**
     * Default constructor used by Ant to create a {@code Resolve} task instance.
     */
    public Resolve() {
        // Default constructor for Ant task
    }

    /**
     * Sets whether the build should fail if an expected attachment (e.g., sources or javadoc)
     * cannot be resolved.
     * <p>
     * This flag only affects artifact consumers that request classified artifacts,
     * such as sources or javadoc JARs via the {@code attachments} attribute.
     * </p>
     * <p>
     * If {@code false} (default), missing attachments are logged at verbose level and ignored.
     * If {@code true}, any unresolved attachment will cause the build to fail.
     * </p>
     *
     * @param failOnMissingAttachments {@code true} to fail the build on missing attachments;
     *                                  {@code false} to ignore them
     */
    public void setFailOnMissingAttachments(boolean failOnMissingAttachments) {
        this.failOnMissingAttachments = failOnMissingAttachments;
    }

    /**
     * Creates a {@link Path} consumer that collects resolved artifact files into an Ant {@code <path>} reference.
     * <p>
     * This is useful for dynamically constructing classpaths from Maven artifacts. The reference ID must be
     * set via {@link Path#setRefId(String)} so that the resulting path can be used elsewhere in the build.
     * </p>
     *
     * <p>Each resolved artifact is added to the path as a {@link org.apache.tools.ant.types.resources.FileResource}.</p>
     *
     * @return a new {@link Path} consumer instance
     *
     * @see Path#setRefId(String)
     */
    public Path createPath() {
        Path path = new Path();
        consumers.add(path);
        return path;
    }

    /**
     * Creates a {@link Files} consumer to collect resolved artifacts into a directory or resource collection.
     * <p>
     * This element allows resolved artifacts to be copied to a specified directory with an optional layout,
     * or referenced as a {@link org.apache.tools.ant.types.FileSet} or {@link org.apache.tools.ant.types.resources.Resources}
     * depending on the configuration.
     * </p>
     *
     * @return a new {@link Files} consumer instance
     *
     * @see Files#setDir(File)
     * @see Files#setLayout(String)
     * @see Files#setRefId(String)
     */
    public Files createFiles() {
        Files files = new Files();
        consumers.add(files);
        return files;
    }

    /**
     * Creates a {@link Props} consumer that maps resolved artifacts to Ant project properties.
     * <p>
     * Each resolved artifact will result in a property assignment using a key composed of the
     * artifact's Maven coordinates, optionally prefixed. The value will be the absolute path to
     * the artifact file.
     * </p>
     *
     * <p>Example property name format:</p>
     * <pre>{@code
     *   prefix.groupId:artifactId:extension[:classifier]
     * }</pre>
     *
     * @return a new {@link Props} consumer instance
     *
     * @see Props#setPrefix(String)
     * @see Props#setAttachments(String)
     */
    public Props createProperties() {
        Props props = new Props();
        consumers.add(props);
        return props;
    }

    private void validate() {
        for (ArtifactConsumer consumer : consumers) {
            consumer.validate();
        }

        Pom pom = AntRepoSys.getInstance(getProject()).getDefaultPom();
        if (dependencies == null && pom != null) {
            log("Using default pom for dependency resolution (" + pom.toString() + ")", Project.MSG_INFO);
            dependencies = new Dependencies();
            dependencies.setProject(getProject());
            getProject().addReference(Names.ID_DEFAULT_POM, pom);
            dependencies.setPomRef(new Reference(getProject(), Names.ID_DEFAULT_POM));
        }

        if (dependencies != null) {
            dependencies.validate(this);
        } else {
            throw new BuildException("No <dependencies> set for resolution");
        }
    }

    @Override
    public void execute() throws BuildException {
        validate();

        AntRepoSys sys = AntRepoSys.getInstance(getProject());

        RepositorySystemSession session = sys.getSession(this, localRepository);
        RepositorySystem system = sys.getSystem();
        log("Using local repository " + session.getLocalRepository(), Project.MSG_VERBOSE);

        DependencyNode root = collectDependencies().getRoot();
        root.accept(new DependencyGraphLogger(this));

        Map<String, Group> groups = new HashMap<>();
        for (ArtifactConsumer consumer : consumers) {
            String classifier = consumer.getClassifier();
            Group group = groups.get(classifier);
            if (group == null) {
                group = new Group(classifier);
                groups.put(classifier, group);
            }
            group.add(consumer);
        }

        for (Group group : groups.values()) {
            group.createRequests(root);
        }

        log("Resolving artifacts", Project.MSG_INFO);

        for (Group group : groups.values()) {
            List<ArtifactResult> results;
            try {
                results = system.resolveArtifacts(session, group.getRequests());
            } catch (ArtifactResolutionException e) {
                if (!group.isAttachments() || failOnMissingAttachments) {
                    throw new BuildException("Could not resolve artifacts: " + e.getMessage(), e);
                }
                results = e.getResults();
                for (ArtifactResult result : results) {
                    if (result.isMissing()) {
                        log("Ignoring missing attachment " + result.getRequest().getArtifact(), Project.MSG_VERBOSE);
                    } else if (!result.isResolved()) {
                        throw new BuildException("Could not resolve artifacts: " + e.getMessage(), e);
                    }
                }
            }

            group.processResults(results, session);
        }
    }

    /**
     * Abstract base class for consumers of resolved artifacts in the {@link Resolve} task.
     * <p>
     * Subclasses of this class define how resolved artifacts are handled, such as copying them to a directory,
     * adding them to a path, or storing their locations in properties. Each consumer may filter artifacts by
     * Maven scope or classpath profile.
     * </p>
     *
     * <p>
     * ArtifactConsumers are registered by the enclosing {@link Resolve} task and invoked after artifacts are
     * resolved from the dependency graph.
     * </p>
     *
     * <p>
     * Common subclasses include:
     * <ul>
     *   <li>{@link Resolve.Path} – Adds artifacts to an Ant {@code <path>} reference</li>
     *   <li>{@link Resolve.Files} – Copies artifacts to a directory and exposes a fileset or resource collection</li>
     *   <li>{@link Resolve.Props} – Stores artifact file paths as Ant properties</li>
     * </ul>
     *
     * @see Resolve
     */
    public abstract static class ArtifactConsumer extends ProjectComponent {

        private DependencyFilter filter;

        /**
         * Default constructor for Ant task instantiation.
         * <p>
         * This constructor is used by Ant to create instances of {@link ArtifactConsumer} subclasses.
         * </p>
         */
        public ArtifactConsumer() {
            // Default constructor for Ant task
        }

        /**
         * Determines whether the given dependency node should be accepted by this consumer
         * based on its configured dependency filter (e.g., scope or classpath).
         *
         * @param node the dependency node to evaluate
         * @param parents the list of parent nodes leading to this node in the dependency graph
         * @return {@code true} if the node passes the filter (or no filter is set); {@code false} otherwise
         */
        public boolean accept(org.eclipse.aether.graph.DependencyNode node, List<DependencyNode> parents) {
            return filter == null || filter.accept(node, parents);
        }

        /**
         * Returns the classifier this consumer is interested in, if any.
         * <p>
         * This is typically used to distinguish between main artifacts and attachments
         * like {@code sources} or {@code javadoc} jars.
         * </p>
         *
         * @return the classifier string (e.g., {@code "*-sources"}), or {@code null} if none
         */
        public String getClassifier() {
            return null;
        }

        /**
         * Validates the configuration of this {@link ArtifactConsumer}.
         * <p>
         * This default implementation does nothing. Subclasses may override this method
         * to enforce that required attributes (e.g., {@code refid}) or configurations are present
         * before artifact resolution begins.
         * </p>
         *
         * @throws BuildException if the consumer configuration is invalid
         */
        public void validate() {}

        /**
         * Processes a resolved artifact after dependency resolution has completed.
         * <p>
         * This method is invoked for each artifact that has been accepted by the consumer's filter.
         * Implementations may use this hook to copy files, register references, build paths, or store metadata.
         * </p>
         *
         * @param artifact the resolved {@link Artifact}, including a file location
         * @param session the {@link RepositorySystemSession} used during resolution, useful for repository information
         */
        public abstract void process(Artifact artifact, RepositorySystemSession session);

        /**
         * Specifies the scopes of dependencies to include or exclude during resolution.
         * <p>
         * The input string can contain a comma- or space-separated list of scopes.
         * Scopes prefixed with {@code -} or {@code !} will be excluded.
         * For example, {@code compile, -test} will include only compile-scope dependencies,
         * excluding those with scope {@code test}.
         * </p>
         *
         * @param scopes a string defining scopes to include/exclude, e.g., {@code "compile, -test"}
         * @throws BuildException if a scope filter was already set (e.g., via {@link #setClasspath(String)})
         */
        public void setScopes(String scopes) {
            if (filter != null) {
                throw new BuildException("You must not specify both 'scopes' and 'classpath'");
            }

            Collection<String> included = new HashSet<>();
            Collection<String> excluded = new HashSet<>();

            String[] split = scopes.split("[, ]");
            for (String scope : split) {
                scope = scope.trim();
                Collection<String> dst;
                if (scope.startsWith("-") || scope.startsWith("!")) {
                    dst = excluded;
                    scope = scope.substring(1);
                } else {
                    dst = included;
                }
                if (!scope.isEmpty()) {
                    dst.add(scope);
                }
            }

            filter = new ScopeDependencyFilter(included, excluded);
        }

        /**
         * Sets a predefined classpath scope configuration using a shorthand string.
         * <p>Accepted values are:</p>
         * <ul>
         *   <li>{@code compile} — includes {@code provided}, {@code system}, and {@code compile} scopes</li>
         *   <li>{@code runtime} — includes {@code compile} and {@code runtime} scopes</li>
         *   <li>{@code test} — includes {@code provided}, {@code system}, {@code compile}, {@code runtime}, and {@code test} scopes</li>
         * </ul>
         * <p>
         * Internally, this method delegates to {@link #setScopes(String)} with an appropriate scope string.
         * </p>
         *
         * @param classpath the classpath type to use ({@code compile}, {@code runtime}, or {@code test})
         * @throws BuildException if the given classpath is not one of the allowed values
         */
        public void setClasspath(String classpath) {
            if ("compile".equals(classpath)) {
                setScopes("provided,system,compile");
            } else if ("runtime".equals(classpath)) {
                setScopes("compile,runtime");
            } else if ("test".equals(classpath)) {
                setScopes("provided,system,compile,runtime,test");
            } else {
                throw new BuildException("The classpath '" + classpath + "' is not defined"
                        + ", must be one of 'compile', 'runtime' or 'test'");
            }
        }
    }

    /**
     * Artifact consumer that adds resolved artifacts to an Ant {@link org.apache.tools.ant.types.Path}.
     * <p>
     * This is useful for dynamically constructing classpaths from resolved Maven dependencies.
     * Each resolved artifact is wrapped in a {@link org.apache.tools.ant.types.resources.FileResource}
     * and added to a path registered under the specified Ant reference ID.
     * </p>
     *
     * <h2>Usage Example:</h2>
     * <pre>{@code
     * <resolve>
     *   <dependencies>
     *     <dependency groupId="org.example" artifactId="lib" version="1.0"/>
     *   </dependencies>
     *   <path refid="my.classpath"/>
     * </resolve>
     * }</pre>
     *
     * @see org.apache.tools.ant.types.Path
     * @see ArtifactConsumer
     */
    public static class Path extends ArtifactConsumer {

        private String refid;

        private org.apache.tools.ant.types.Path path;

        /**
         * This default constructor is used by Ant to create instances of the {@link Path} consumer.
         */
        public Path() {
            // Default constructor for Ant task
        }

        /**
         * Sets the reference ID for the Ant path to which resolved artifacts will be added.
         *
         * @param refId the Ant reference ID
         */
        public void setRefId(String refId) {
            this.refid = refId;
        }

        /**
         * Validates that the required {@code refid} has been set.
         *
         * @throws BuildException if {@code refid} is not provided
         */
        @Override
        public void validate() {
            if (refid == null) {
                throw new BuildException("You must specify the 'refid' for the path");
            }
        }

        /**
         * Adds the given artifact file to the configured Ant path.
         *
         * @param artifact the resolved artifact
         * @param session the active repository system session
         */
        @Override
        public void process(Artifact artifact, RepositorySystemSession session) {
            if (path == null) {
                path = new org.apache.tools.ant.types.Path(getProject());
                getProject().addReference(refid, path);
            }
            File file = artifact.getFile();
            path.add(new FileResource(file.getParentFile(), file.getName()));
        }
    }

    /**
     * Artifact consumer that copies resolved artifacts to a local directory and optionally registers
     * them as an Ant {@link org.apache.tools.ant.types.FileSet} or {@link org.apache.tools.ant.types.resources.Resources}.
     * <p>
     * This is useful for collecting artifacts into a structured directory or exporting them
     * as a resource collection for further processing in the build.
     * </p>
     *
     * <h2>Usage Examples:</h2>
     * <pre>{@code
     * <resolve>
     *   <dependencies>
     *     <dependency groupId="org.example" artifactId="lib" version="1.0"/>
     *   </dependencies>
     *   <files refid="resolved.files" dir="libs"/>
     * </resolve>
     * }</pre>
     *
     * @see ArtifactConsumer
     * @see org.apache.tools.ant.types.FileSet
     * @see org.apache.tools.ant.types.resources.Resources
     */
    public class Files extends ArtifactConsumer {

        private static final String DEFAULT_LAYOUT = Layout.GID_DIRS + "/" + Layout.AID + "/" + Layout.BVER + "/"
                + Layout.AID + "-" + Layout.VER + "-" + Layout.CLS + "." + Layout.EXT;

        private String refid;

        private String classifier;

        private File dir;

        private Layout layout;

        private FileSet fileset;

        private Resources resources;

        /**
         * Default constructor for Ant task instantiation.
         * <p>
         * This constructor is used by Ant to create instances of the {@link Files} consumer.
         * </p>
         */
        public Files() {
            // Default constructor for Ant task
        }

        /**
         * Sets the Ant reference ID under which the collected fileset or resources will be registered.
         *
         * @param refId the reference ID to assign
         */
        public void setRefId(String refId) {
            this.refid = refId;
        }

        /**
         * Returns the classifier pattern used to match specific artifact attachments, such as sources or javadoc.
         *
         * @return the classifier pattern, or {@code null} if not set
         */
        @Override
        public String getClassifier() {
            return classifier;
        }

        /**
         * Specifies which type of attachment to resolve. Valid values are:
         * <ul>
         *   <li>{@code sources}</li>
         *   <li>{@code javadoc}</li>
         * </ul>
         * Internally, this sets a classifier pattern for filtering resolved artifacts.
         *
         * @param attachments the attachment type
         * @throws BuildException if an invalid type is provided
         */
        public void setAttachments(String attachments) {
            if ("sources".equals(attachments)) {
                classifier = "*-sources";
            } else if ("javadoc".equals(attachments)) {
                classifier = "*-javadoc";
            } else {
                throw new BuildException("The attachment type '" + attachments
                        + "' is not defined, must be one of 'sources' or 'javadoc'");
            }
        }

        /**
         * Sets the output directory to which resolved artifacts will be copied.
         * If this is specified without an explicit layout, a default layout will be used.
         *
         * @param dir the destination directory
         */
        public void setDir(File dir) {
            this.dir = dir;
            if (dir != null && layout == null) {
                layout = new Layout(DEFAULT_LAYOUT);
            }
        }

        /**
         * Sets the layout template used to determine the relative path of each artifact
         * when copying files to the target directory.
         * <p>
         * The layout is a string pattern using variables such as {@code ${gid}}, {@code ${aid}},
         * {@code ${ver}}, {@code ${cls}}, and {@code ${ext}} to define where artifacts
         * should be placed under the specified {@code dir}.
         * </p>
         * <p>
         * This method is only meaningful if a {@code dir} is specified. If used without a directory,
         * it will result in a {@link org.apache.tools.ant.BuildException} during validation.
         * </p>
         *
         * @param layout the path layout pattern to apply for copied artifacts
         *
         * @see #setDir(File)
         */
        public void setLayout(String layout) {
            this.layout = new Layout(layout);
        }

        /**
         * Validates that either a destination directory or a reference ID is set.
         *
         * @throws BuildException if the configuration is invalid
         */
        @Override
        public void validate() {
            if (refid == null && dir == null) {
                throw new BuildException("You must either specify the 'refid' for the resource collection"
                        + " or a 'dir' to copy the files to");
            }
            if (dir == null && layout != null) {
                throw new BuildException("You must not specify a 'layout' unless 'dir' is also specified");
            }
        }

        /**
         * Processes a resolved artifact by copying it to the destination directory or
         * registering it as a resource in the Ant project.
         *
         * @param artifact the resolved artifact
         * @param session  the current repository session
         */
        @Override
        public void process(Artifact artifact, RepositorySystemSession session) {
            if (dir != null) {
                if (refid != null && fileset == null) {
                    fileset = new FileSet();
                    fileset.setProject(getProject());
                    fileset.setDir(dir);
                    getProject().addReference(refid, fileset);
                }

                String path = layout.getPath(artifact);

                if (fileset != null) {
                    fileset.createInclude().setName(path);
                }

                File src = artifact.getFile();
                File dst = new File(dir, path);

                if (src.lastModified() != dst.lastModified() || src.length() != dst.length()) {
                    try {
                        Resolve.this.log("Copy " + src + " to " + dst, Project.MSG_VERBOSE);
                        FileUtils.getFileUtils().copyFile(src, dst, null, true, true);
                    } catch (IOException e) {
                        throw new BuildException(
                                "Failed to copy artifact file " + src + " to " + dst + ": " + e.getMessage(), e);
                    }
                } else {
                    Resolve.this.log("Omit to copy " + src + " to " + dst + ", seems unchanged", Project.MSG_VERBOSE);
                }
            } else {
                if (resources == null) {
                    resources = new Resources();
                    resources.setProject(getProject());
                    getProject().addReference(refid, resources);
                }

                FileResource resource = new FileResource(artifact.getFile());
                resource.setBaseDir(session.getLocalRepository().getBasedir());
                resource.setProject(getProject());
                resources.add(resource);
            }
        }
    }

    /**
     * Artifact consumer that maps resolved artifacts to Ant project properties.
     * <p>
     * Each resolved artifact is converted to a property key-value pair, where the key is
     * based on the artifact coordinates and an optional prefix, and the value is the absolute
     * path to the resolved artifact file.
     * </p>
     *
     * <p>
     * This is useful for referencing artifacts in other tasks or scripts, especially when
     * integration with tools that expect file paths as properties is needed.
     * </p>
     *
     * <p>
     * Example output property format:
     * {@code prefix.groupId:artifactId:extension[:classifier] = /path/to/artifact.jar}
     * </p>
     *
     * <p>Example usage:</p>
     * <pre>{@code
     * <resolve>
     *   <dependencies>
     *     <dependency groupId="org.example" artifactId="lib" version="1.0"/>
     *   </dependencies>
     *   <properties prefix="mydeps"/>
     * </resolve>
     * }</pre>
     *
     * @see ArtifactConsumer
     */
    public static class Props extends ArtifactConsumer {

        private String prefix;

        private String classifier;

        /**
         * This default constructor is used by Ant to create instances of the {@link Props} consumer.
         */
        public Props() {
            // Default constructor for Ant task
        }

        /**
         * Sets a prefix for the generated Ant property keys.
         * For example, if prefix is {@code resolved}, properties will be named like:
         * {@code resolved.groupId:artifactId:type[:classifier]}.
         *
         * @param prefix the property key prefix
         */
        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        /**
         * Returns the classifier pattern used to match specific artifact attachments.
         *
         * @return the classifier pattern, or {@code null} if not set
         */
        @Override
        public String getClassifier() {
            return classifier;
        }

        /**
         * Specifies which type of attachment to resolve. Valid values are:
         * <ul>
         *   <li>{@code sources}</li>
         *   <li>{@code javadoc}</li>
         * </ul>
         * Internally, this sets a classifier pattern for filtering resolved artifacts.
         *
         * @param attachments the attachment type
         * @throws BuildException if an invalid type is provided
         */
        public void setAttachments(String attachments) {
            if ("sources".equals(attachments)) {
                classifier = "*-sources";
            } else if ("javadoc".equals(attachments)) {
                classifier = "*-javadoc";
            } else {
                throw new BuildException("The attachment type '" + attachments
                        + "' is not defined, must be one of 'sources' or 'javadoc'");
            }
        }

        /**
         * Processes a resolved artifact by registering its absolute path as an Ant property.
         * The property name is derived from the artifact coordinates and the optional prefix.
         *
         * @param artifact the resolved artifact
         * @param session  the current repository session
         */
        @Override
        public void process(Artifact artifact, RepositorySystemSession session) {
            StringBuilder buffer = new StringBuilder(256);
            if (prefix != null && !prefix.isEmpty()) {
                buffer.append(prefix);
                if (!prefix.endsWith(".")) {
                    buffer.append('.');
                }
            }
            buffer.append(artifact.getGroupId());
            buffer.append(':');
            buffer.append(artifact.getArtifactId());
            buffer.append(':');
            buffer.append(artifact.getExtension());
            if (!artifact.getClassifier().isEmpty()) {
                buffer.append(':');
                buffer.append(artifact.getClassifier());
            }

            String path = artifact.getFile().getAbsolutePath();

            getProject().setProperty(buffer.toString(), path);
        }
    }

    private static class Group {

        private final String classifier;

        private final List<ArtifactConsumer> consumers = new ArrayList<>();

        private final List<ArtifactRequest> requests = new ArrayList<>();

        Group(String classifier) {
            this.classifier = classifier;
        }

        public boolean isAttachments() {
            return classifier != null;
        }

        public void add(ArtifactConsumer consumer) {
            consumers.add(consumer);
        }

        public void createRequests(DependencyNode node) {
            createRequests(node, new LinkedList<>());
        }

        private void createRequests(DependencyNode node, LinkedList<DependencyNode> parents) {
            if (node.getDependency() != null) {
                for (ArtifactConsumer consumer : consumers) {
                    if (consumer.accept(node, parents)) {
                        ArtifactRequest request = new ArtifactRequest(node);
                        if (classifier != null) {
                            request.setArtifact(new SubArtifact(request.getArtifact(), classifier, "jar"));
                        }
                        requests.add(request);
                        break;
                    }
                }
            }

            parents.addFirst(node);

            for (DependencyNode child : node.getChildren()) {
                createRequests(child, parents);
            }

            parents.removeFirst();
        }

        public List<ArtifactRequest> getRequests() {
            return requests;
        }

        public void processResults(List<ArtifactResult> results, RepositorySystemSession session) {
            for (ArtifactResult result : results) {
                if (!result.isResolved()) {
                    continue;
                }
                for (ArtifactConsumer consumer : consumers) {
                    if (consumer.accept(
                            result.getRequest().getDependencyNode(), Collections.<DependencyNode>emptyList())) {
                        consumer.process(result.getArtifact(), session);
                    }
                }
            }
        }
    }
}
