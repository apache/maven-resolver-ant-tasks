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
package org.apache.maven.resolver.internal.ant.types.model;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.resolver.internal.ant.types.Dependency;
import org.apache.tools.ant.types.DataType;

/**
 * Represents a Maven project model.
 * This class encapsulates the Maven POM model and provides methods to manipulate it,
 * such as adding licenses, dependencies, and setting project properties.
 */
public class MavenProject extends DataType {
    private final Model model;

    public MavenProject() {
        model = new Model();
        model.setModelVersion("4.0.0");
        model.setPackaging("jar");
    }

    /**
     * Adds a license to the Maven project model.
     *
     * @param license the License object to add, must not be null
     */
    public void addLicense(License license) {
        org.apache.maven.model.License pomLicense = new org.apache.maven.model.License();
        pomLicense.setName(license.getName());
        pomLicense.setUrl(license.getUrl());
        pomLicense.setComments(license.getComments());
        pomLicense.setDistribution(license.getDistribution());
        model.getLicenses().add(pomLicense);
    }

    /**
     * Set the groupId for the Maven project model.
     *
     * @param groupId the groupId to set, must not be null or empty
     */
    public void setGroupId(String groupId) {
        model.setGroupId(groupId);
    }

    /**
     * Set the artifactId for the Maven project model.
     *
     * @param artifactId the artifactId to set, must not be null or empty
     */
    public void setArtifactId(String artifactId) {
        model.setArtifactId(artifactId);
    }

    /**
     * Set the version for the Maven project model.
     *
     * @param version the version to set, must not be null or empty
     */
    public void setVersion(String version) {
        model.setVersion(version);
    }

    /**
     * Set the name for the Maven project model.
     *
     * @param name the name to set, can be null or empty
     */
    public void setName(String name) {
        model.setName(name);
    }

    /**
     * Set the description for the Maven project model.
     *
     * @param description the description to set, can be null or empty
     */
    public void setDescription(String description) {
        model.setDescription(description);
    }

    /**
     * Add a dependency to the dependencies section of the Maven project model.
     * @param dep the Dependency object to add, must not be null
     */
    public void addDependency(Dependency dep) {
        List<org.apache.maven.model.Dependency> dependencies = model.getDependencies();
        if (dependencies == null) {
            dependencies = new ArrayList<>();
            model.setDependencies(dependencies);
        }
        org.apache.maven.model.Dependency dependency = convert(dep);
        dependencies.add(dependency);
    }

    private org.apache.maven.model.Dependency convert(Dependency dep) {
        org.apache.maven.model.Dependency dependency = new org.apache.maven.model.Dependency();
        dependency.setGroupId(dep.getGroupId());
        dependency.setArtifactId(dep.getArtifactId());
        if (dep.getVersion() != null) {
            dependency.setVersion(dep.getVersion());
        }
        if (dep.getType() != null) {
            dependency.setType(dep.getType());
        }
        if (dep.getClassifier() != null) {
            dependency.setClassifier(dep.getClassifier());
        }
        if (dep.getScope() != null && !dep.getScope().trim().isEmpty()) {
            dependency.setScope(dep.getScope());
        }

        if (dep.getExclusions() != null && !dep.getExclusions().isEmpty()) {
            dependency.setExclusions(new ArrayList<>());
            dep.getExclusions().forEach(exclusion -> {
                org.apache.maven.model.Exclusion mavenExclusion = new org.apache.maven.model.Exclusion();
                mavenExclusion.setGroupId(exclusion.getGroupId());
                mavenExclusion.setArtifactId(exclusion.getArtifactId());
                dependency.getExclusions().add(mavenExclusion);
            });
        }
        return dependency;
    }

    /**
     * Adds a dependency to the dependency management section of the Maven project model.
     *
     * @param dep the Dependency object to add, must not be null
     */
    public void addToDependencyManagement(Dependency dep) {
        if (model.getDependencyManagement() == null) {
            model.setDependencyManagement(new org.apache.maven.model.DependencyManagement());
        }
        model.getDependencyManagement().addDependency(convert(dep));
    }

    /**
     * Gets the Maven project model.
     *
     * @return the Model object representing the Maven project, never null
     */
    public Model getModel() {
        return model;
    }

    /**
     * Writes the Maven project model to the specified Writer in POM format.
     *
     * @param writer the Writer to write the POM to, must not be null
     * @throws IOException if an I/O error occurs while writing
     */
    public void toPom(Writer writer) throws IOException {
        MavenXpp3Writer pomWriter = new MavenXpp3Writer();
        pomWriter.write(writer, model);
    }
}
