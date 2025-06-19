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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * Represents the dependency management section in an Ant build file.
 * This class allows you to define and manage dependencies for a project.
 * Note that for this to work, you must produce and register a pom file before calling resolve.
 * The createPom task can be used for this purpose.
 */
public class DependencyManagement extends DataType {
    Dependencies dependencies;

    /**
     * Fetches a DependencyManagement instance from the given project using the specified reference.
     *
     * @param project the project to fetch the DependencyManagement from
     * @param dependencyManagementRef the reference to the DependencyManagement section
     * @return the DependencyManagement instance
     */
    public static DependencyManagement get(Project project, String dependencyManagementRef) {
        DependencyManagement dependencyManagement = new DependencyManagement();
        dependencyManagement.setProject(project);
        Reference ref = new Reference(project, dependencyManagementRef);
        dependencyManagement.setRefid(ref);
        return dependencyManagement.getRef();
    }

    /**
     * Gets the dependencies defined in this DependencyManagement instance.
     *
     * @return the dependencies, or null if none are set
     */
    public Dependencies getDependencies() {
        return dependencies;
    }

    /**
     * Sets the dependencies for this DependencyManagement instance.
     * There can be only one dependencies element.
     *
     * @param dependencies the dependencies to set
     * @throws IllegalStateException if dependencies are already set
     */
    public void addDependencies(Dependencies dependencies) {
        if (this.dependencies != null) {
            throw new IllegalStateException("You must not specify multiple <dependencies> elements");
        }
        this.dependencies = dependencies;
    }

    public DependencyManagement getRef() {
        return getCheckedRef(DependencyManagement.class);
    }
}
