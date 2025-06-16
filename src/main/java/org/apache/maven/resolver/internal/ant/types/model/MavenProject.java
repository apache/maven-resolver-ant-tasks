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

public class MavenProject extends DataType {
    private final Model model;

    public MavenProject() {
        model = new Model();
        model.setModelVersion("4.0.0");
        model.setPackaging("jar");
    }

    public void addLicense(License license) {
        org.apache.maven.model.License lic = new org.apache.maven.model.License();
        lic.setName(license.getName());
        lic.setUrl(license.getUrl());
        lic.setComments(license.getComments());
        lic.setDistribution(license.getDistribution());
        model.getLicenses().add(lic);
    }

    public void setGroupId(String groupId) {
        model.setGroupId(groupId);
    }

    public void setArtifactId(String artifactId) {
        model.setArtifactId(artifactId);
    }

    public void setVersion(String version) {
        model.setVersion(version);
    }

    public void setName(String name) {
        model.setName(name);
    }

    public void setDescription(String description) {
        model.setDescription(description);
    }

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

    public void addToDependencyManagement(Dependency dep) {
        if (model.getDependencyManagement() == null) {
            model.setDependencyManagement(new org.apache.maven.model.DependencyManagement());
        }
        model.getDependencyManagement().addDependency(convert(dep));
    }

    public Model getModel() {
        return model;
    }

    public void toPom(Writer writer) throws IOException {
        MavenXpp3Writer pomWriter = new MavenXpp3Writer();
        pomWriter.write(writer, model);
    }
}
