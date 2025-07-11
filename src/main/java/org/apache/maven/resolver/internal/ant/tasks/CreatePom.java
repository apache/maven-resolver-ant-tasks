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
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;

import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.resolver.internal.ant.types.Dependencies;
import org.apache.maven.resolver.internal.ant.types.Dependency;
import org.apache.maven.resolver.internal.ant.types.DependencyManagement;
import org.apache.maven.resolver.internal.ant.types.Pom;
import org.apache.maven.resolver.internal.ant.types.model.License;
import org.apache.maven.resolver.internal.ant.types.model.Licenses;
import org.apache.maven.resolver.internal.ant.types.model.MavenProject;
import org.apache.maven.resolver.internal.ant.types.model.Repositories;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Task to create a Maven POM file.
 * This task allows you to define dependencies, dependency management, licenses, and repositories for the POM.
 * This is useful if you have defined your dependencies in the ant build script instead of in the POM. This task also
 * registers the POM.
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * <repo:createPom pomTarget='${pomFile}'
 *                 dependenciesRef='compile'
 *                 dependencyManagementRef='dm'
 *                 name='mylib'
 *                 description='An useful library'>
 *     <licenses>
 *         <license>
 *             <name>Apache License, Version 2.0</name>
 *             <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
 *         </license>
 *     </licenses>
 * </repo:createPom>
 * }</pre>
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><strong>dependenciesIdRef</strong> - (optional) The reference to the id of the {@code <dependencies>}
 *   section</li>
 *   <li><strong>dependencyManagementRef</strong> - (optional) The reference to the id of the
 *   {@code <dependencyManagement>} section</li>
 *   <li><strong>pomTarget</strong> - The path to the POM file to create</li>
 *   <li><strong>groupId</strong> - The group id for the artifact(s), if a <code>groupId</code> property is set this
 *   can be omitted</li>
 *   <li><strong>artifactId</string> - The artifact id for the artifact(s), if an <code>artifactId</code> property is
 *   set this can be omitted</li>
 *   <li><strong>version</strong> - The version for the artifact(s), if a <code>version</code> property is set this can
 *   be omitted</li>
 *   <li><strong>name</strong> - (optional) The name of the project</li>
 *   <li><strong>description</strong> - (optional) The short description of the project</li>
 *   <li><strong>skipPomRegistration</strong> - (optional) If set to true, the pom task will not be called to register
 *   the pom so must be done explicitly in the build script. Defaults to false.</li>
 * </ul>
 * <h2>Nested Elements:</h2>
 * <ul>
 *   <li>{@code <licenses>} — (optional) specifies the licenses for the artifact.</li>
 *   <li>{@code <repositories>} — (optional) defines the additional repositories used for resolving dependencies</li>
 * </ul>
 * <h2>Behavior:</h2>
 * <ul>
 *  <li>groupId, artifactId and version are required. They could either be defined as properties or passed as
 * attributes to the createPom task.</li>
 * </ul>
 */
public class CreatePom extends Task {
    private String dependenciesIdReference;
    private String dependencyManagementIdReference;
    private File pomFile;
    private String groupId;
    private String artifactId;
    private String version;
    private String name;
    private String description;
    private Licenses licenses;
    private Repositories repositories;
    private boolean skipPomRegistration;

    /**
     * Default constructor.
     */
    public CreatePom() {
        // Default constructor
    }

    /**
     * The path to the file that the POM content should be written to.
     *
     * @param pomTarget the target file for the POM
     */
    public void setPomTarget(String pomTarget) {
        pomFile = new File(getProject().replaceProperties(pomTarget));
    }

    /**
     * The path to the file that the POM content should be written to.
     *
     * @param pomTarget the target file for the POM
     */
    public void setPomTarget(File pomTarget) {
        pomFile = pomTarget;
    }

    /**
     * The reference to the dependency management section.
     * This should point to the id attribute of a dependencyManagement element
     * defined elsewhere in the build file.
     *
     * @param dependencyManagementRefId the reference to the id of the dependency management section
     */
    public void setDependencyManagementRef(String dependencyManagementRefId) {
        this.dependencyManagementIdReference = getProject().replaceProperties(dependencyManagementRefId);
    }

    /**
     * The reference to the dependencies section.
     * This should point to the id attribute of a dependencies
     * element defined elsewhere in the build file.
     *
     * @param dependenciesIdReference the reference to the id of the dependencies section
     */
    public void setDependenciesRef(String dependenciesIdReference) {
        this.dependenciesIdReference = getProject().replaceProperties(dependenciesIdReference);
    }

    /**
     * Set the groupId for the POM.
     * The groupId can include properties that will be replaced at runtime.
     *
     * @param groupId the groupId for the POM
     */
    public void setGroupId(String groupId) {
        this.groupId = getProject().replaceProperties(groupId);
    }

    /**
     * Set the artifactId for the POM.
     * The artifactId can include properties that will be replaced at runtime.
     *
     * @param artifactId the artifactId for the POM
     */
    public void setArtifactId(String artifactId) {
        this.artifactId = getProject().replaceProperties(artifactId);
    }

    /**
     * Set the version for the POM.
     * The version can include properties that will be replaced at runtime.
     *
     * @param version the version for the POM
     */
    public void setVersion(String version) {
        this.version = getProject().replaceProperties(version);
    }

    /**
     * Set the name for the POM.
     * The name can include properties that will be replaced at runtime.
     *
     * @param name the name for the POM
     */
    public void setName(String name) {
        this.name = getProject().replaceProperties(name);
    }

    /**
     * Get the description for the POM.
     *
     * @return the description for the POM
     */
    public String getName() {
        return name;
    }

    /**
     * Set the description for the POM.
     * The description can include properties that will be replaced at runtime.
     *
     * @param description the description for the POM
     */
    public void setDescription(String description) {
        this.description = getProject().replaceProperties(description);
    }

    /**
     * skipPomRegistration is a flag to indicate whether the POM should be registered in the project after creation.
     * Setting this to false is equivalent to doing
     * <pre><code>
     *   &lt;pom file="${pomFile}"/&gt;
     * </code></pre>
     * Default is false, meaning the POM will be registered.
     *
     * @param skipPomRegistration true to skip registration, false to register the POM
     */
    public void setSkipPomRegistration(boolean skipPomRegistration) {
        this.skipPomRegistration = skipPomRegistration;
    }

    /**
     * Get the groupId.
     * If the groupId is not set, it will return a default value based on the project property 'groupId'.
     *
     * @return the groupId for the POM
     */
    public String getGroupId() {
        if (groupId == null) {
            return getProject().getProperty("groupId");
        }
        return groupId;
    }

    /**
     * Get the artifactId.
     * If the artifactId is not set, it will return a default value based on the project property 'artifactId'.
     *
     * @return the artifactId for the POM
     */
    public String getArtifactId() {
        if (artifactId == null) {
            return getProject().getProperty("artifactId");
        }
        return artifactId;
    }

    /**
     * Get the version.
     * If the version is not set, it will return a default value based on the project property 'version'.
     *
     * @return the version for the POM
     */
    public String getVersion() {
        if (version == null) {
            return getProject().getProperty("version");
        }
        return version;
    }

    /**
     * Get the description.
     * If the description is not set, it will return a default value based on the project property 'description'.
     *
     * @return the description for the POM
     */
    public String getDescription() {
        return description == null ? super.getDescription() : description;
    }

    /**
     * Add a licenses element to the POM.
     * This allows you to specify one or more licenses that the project is distributed under.
     *
     * @param licenses the licenses to add to the POM
     */
    public void addLicenses(Licenses licenses) {
        this.licenses = licenses;
    }

    /**
     * Add a repositories element to the POM.
     * This allows you to specify one or more repositories where dependencies can be found. Note that
     * specifying repositories in the POM is discouraged if you aim to publish to maven central.
     *
     * @param repositories the repositories to add to the POM
     */
    public void addRepositories(Repositories repositories) {
        this.repositories = repositories;
    }

    /**
     * Execute the task to create the POM file.
     * This method will create the POM file with the specified properties and dependencies.
     */
    @Override
    public void execute() {
        if (!pomFile.getParentFile().exists()) {
            pomFile.getParentFile().mkdirs();
        }
        MavenProject pom = new MavenProject();
        pom.setGroupId(getGroupId());
        pom.setArtifactId(getArtifactId());
        pom.setVersion(getVersion());
        pom.setName(getName());
        pom.setDescription(getDescription());

        if (dependencyManagementIdReference != null) {
            DependencyManagement dependencyManagement =
                    DependencyManagement.get(getProject(), dependencyManagementIdReference);
            appendManagedDependencies(dependencyManagement.getDependencies(), pom);
        }
        if (dependenciesIdReference != null) {
            Dependencies dependencies = new Dependencies();
            dependencies.setProject(getProject());
            org.apache.tools.ant.types.Reference ref =
                    new org.apache.tools.ant.types.Reference(getProject(), dependenciesIdReference);
            dependencies.setRefid(ref);

            appendDependencies(dependencies, pom);
        }

        if (licenses != null) {
            for (License l : licenses.getLicenses()) {
                pom.addLicense(l);
            }
        }

        if (repositories != null) {
            repositories.getRepositories().forEach(repository -> {
                org.apache.maven.model.Repository repo = new org.apache.maven.model.Repository();
                repo.setId(repository.getId());
                repo.setName(repository.getName());
                repo.setUrl(repository.getUrl());
                repo.setLayout(repository.getLayout());
                if (repository.getReleases() != null) {
                    RepositoryPolicy policy = new RepositoryPolicy();
                    policy.setEnabled(repository.getReleases().isEnabled());
                    repo.setReleases(policy);
                }
                if (repository.getSnapshots() != null) {
                    RepositoryPolicy policy = new RepositoryPolicy();
                    policy.setEnabled(repository.getSnapshots().isEnabled());
                    repo.setSnapshots(policy);
                }
                pom.getModel().getRepositories().add(repo);
            });
        }

        try (OutputStream pomOutputStream = Files.newOutputStream(pomFile.toPath())) {
            pom.toPom(pomOutputStream);
            log("Created the POM file " + pomFile.getAbsolutePath(), Project.MSG_VERBOSE);
        } catch (IOException e) {
            throw new BuildException("Failed to create POM file", e);
        }

        if (!skipPomRegistration) {
            registerPom();
            log("Registered the POM file", Project.MSG_VERBOSE);
        }
    }

    private void registerPom() {
        Map<String, Class<?>> taskDefs = getProject().getTaskDefinitions();
        if (!taskDefs.containsKey("pom")) {
            getProject().addTaskDefinition("pom", Pom.class);
        }
        Pom pom = (Pom) getProject().createTask("pom");
        pom.setProject(getProject());
        pom.setFile(pomFile);
        pom.execute();
    }

    private static void appendDependencies(Dependencies dependencies, MavenProject pom) {

        dependencies.getDependencyContainers().forEach(container -> {
            if (container instanceof Dependency) {
                Dependency dep = (Dependency) container;
                pom.addDependency(dep);
            }
        });
    }

    private static void appendManagedDependencies(Dependencies dependencies, MavenProject mavenProject) {
        dependencies.getDependencyContainers().forEach(it -> {
            if (it instanceof Dependency) {
                Dependency dep = (Dependency) it;
                mavenProject.addToDependencyManagement(dep);
            }
        });
    }
}
