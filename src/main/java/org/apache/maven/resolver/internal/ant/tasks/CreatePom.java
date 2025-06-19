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

import java.io.*;
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
 */
public class CreatePom extends Task {
    private String dependenciesRef;
    private String dependencyManagementRef;
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
     * The target file path for the POM i.e., the path to the file that the pom content should be written to.
     *
     * @param pomTarget the target file for the POM
     */
    public void setPomTarget(String pomTarget) {
        pomFile = new File(getProject().replaceProperties(pomTarget));
    }

    /**
     * The target file for the POM i.e., the path to the file that the pom content should be written to.
     *
     * @param pomTarget the target file for the POM
     */
    public void setPomTarget(File pomTarget) {
        pomFile = pomTarget;
    }

    /**
     * The reference to the dependency management section.
     * This should point to the id attribute of a &lt;dependencyManagement&gt;
     * element defined elsewhere in the build file.
     *
     * @param dependencyManagementRef the reference (id) to the dependency management section
     */
    public void setDependencyManagementRef(String dependencyManagementRef) {
        this.dependencyManagementRef = getProject().replaceProperties(dependencyManagementRef);
    }

    /**
     * The reference to the dependencies section.
     * This should point to the id attribute of a &lt;dependencies&gt;
     * element defined elsewhere in the build file.
     *
     * @param dependenciesRef the reference (id) to the dependencies section
     */
    public void setDependenciesRef(String dependenciesRef) {
        this.dependenciesRef = getProject().replaceProperties(dependenciesRef);
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
     * specifying repositories in the pom is discouraged if you aim to publish to maven central.
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

        if (dependencyManagementRef != null) {
            DependencyManagement dependencyManagement = DependencyManagement.get(getProject(), dependencyManagementRef);
            appendManagedDependencies(dependencyManagement.getDependencies(), pom);
        }
        if (dependenciesRef != null) {
            Dependencies dependencies = new Dependencies();
            dependencies.setProject(getProject());
            org.apache.tools.ant.types.Reference ref =
                    new org.apache.tools.ant.types.Reference(getProject(), dependenciesRef);
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
                    policy.setEnabled(repository.getReleases().getEnabledValue());
                    repo.setReleases(policy);
                }
                if (repository.getSnapshots() != null) {
                    RepositoryPolicy policy = new RepositoryPolicy();
                    policy.setEnabled(repository.getSnapshots().getEnabledValue());
                    repo.setSnapshots(policy);
                }
                pom.getModel().getRepositories().add(repo);
            });
        }

        try (FileWriter fw = new FileWriter(pomFile)) {
            pom.toPom(fw);
            log("Created the pom file " + pomFile.getAbsolutePath(), Project.MSG_VERBOSE);
        } catch (IOException e) {
            throw new BuildException("Failed to create pom file", e);
        }

        if (!skipPomRegistration) {
            registerPom();
            log("Registered the pom file", Project.MSG_VERBOSE);
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
