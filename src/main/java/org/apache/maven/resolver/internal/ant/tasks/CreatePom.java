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

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.model.RepositoryPolicy;
import org.apache.maven.resolver.internal.ant.types.*;
import org.apache.maven.resolver.internal.ant.types.model.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

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
    private Boolean skipPomRegistration = false;

    public void setPomTarget(String pomTarget) {
        pomFile = new File(getProject().replaceProperties(pomTarget));
    }

    public void setPomTarget(File pomTarget) {
        pomFile = pomTarget;
    }

    public void setDependencyManagementRef(String dependencyManagementRef) {
        this.dependencyManagementRef = getProject().replaceProperties(dependencyManagementRef);
    }

    public void setDependenciesRef(String dependenciesRef) {
        this.dependenciesRef = getProject().replaceProperties(dependenciesRef);
    }

    public void setGroupId(String groupId) {
        this.groupId = getProject().replaceProperties(groupId);
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = getProject().replaceProperties(artifactId);
    }

    public void setVersion(String version) {
        this.version = getProject().replaceProperties(version);
    }

    public void setName(String name) {
        this.name = getProject().replaceProperties(name);
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = getProject().replaceProperties(description);
    }

    public void setSkipPomRegistration(Boolean skipPomRegistration) {
        this.skipPomRegistration = skipPomRegistration;
    }

    public String getGroupId() {
        if (groupId == null || "null".equals(groupId)) {
            return getProject().getProperty("groupId");
        }
        return groupId;
    }

    public String getArtifactId() {
        if (artifactId == null || "null".equals(artifactId)) {
            return getProject().getProperty("artifactId");
        }
        return artifactId;
    }

    public String getVersion() {
        if (version == null || "null".equals(version)) {
            return getProject().getProperty("version");
        }
        return version;
    }

    public String getDescription() {
        return description == null ? super.getDescription() : description;
    }

    public void addLicenses(Licenses licenses) {
        this.licenses = licenses;
    }

    public void addRepositories(Repositories repositories) {
        this.repositories = repositories;
    }

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
        Dependencies dependencies = new Dependencies();
        dependencies.setProject(getProject());
        org.apache.tools.ant.types.Reference ref =
                new org.apache.tools.ant.types.Reference(getProject(), dependenciesRef);
        dependencies.setRefid(ref);

        appendDependencies(dependencies, pom);

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
        } catch (IOException e) {
            throw new BuildException("Failed to create pom file: ${e.message}", e);
        }

        if (!skipPomRegistration) {
            registerPom();
        } else {
            log("Created pom file ${pomFile.canonicalPath}", Project.MSG_INFO);
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
        try {
            log("Created and registered the pom file " + pomFile.getCanonicalPath(), Project.MSG_INFO);
        } catch (IOException e) {
            log("Failed to get canonical path for pom file: " + e.getMessage(), e, Project.MSG_INFO);
        }
    }

    static void appendDependencies(Dependencies dependencies, MavenProject pom) {
        dependencies.getDependencyContainers().forEach(container -> {
            if (container instanceof Dependency) {
                Dependency dep = (Dependency) container;
                pom.addDependency(dep);
            }
        });
    }

    static void appendManagedDependencies(Dependencies dependencies, MavenProject mavenProject) {
        dependencies.getDependencyContainers().forEach(it -> {
            if (it instanceof Dependency) {
                Dependency dep = (Dependency) it;
                mavenProject.addToDependencyManagement(dep);
            }
        });
    }

    static Map<String, String> toMap(Dependency dep) {
        Map<String, String> params = new HashMap<>();
        if (dep.getGroupId() != null) {
            params.put("groupId", dep.getGroupId());
        }
        if (dep.getArtifactId() != null) {
            params.put("artifactId", dep.getArtifactId());
        }
        if (dep.getVersion() != null) {
            params.put("version", dep.getVersion());
        }
        if (dep.getClassifier() != null) {
            params.put("classifier", dep.getClassifier());
        }
        if (dep.getType() != null) {
            params.put("type", dep.getType());
        }
        if (dep.getScope() != null) {
            params.put("scope", dep.getScope());
        }
        return params;
    }

    static void validatePomContent(String content, String schemaLocation) {
        try {
            URL xsdLocation = new URI(schemaLocation).toURL();

            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(xsdLocation)
                    .newValidator()
                    .validate(new StreamSource(new StringReader(content)));
        } catch (Exception e) {
            throw new BuildException("Failed to validate pom: ${e.message}", e);
        }
    }
}
