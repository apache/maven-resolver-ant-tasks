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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * Container for multiple Maven dependencies in an Ant build script.
 * <p>
 * This Ant {@code DataType} represents a collection of {@link Dependency} elements.
 * It is typically used within tasks like {@link org.apache.maven.resolver.internal.ant.tasks.Resolve Resolve},
 * {@link org.apache.maven.resolver.internal.ant.tasks.CreatePom CreatePom},
 * {@link org.apache.maven.resolver.internal.ant.tasks.Deploy Deploy}, and
 * {@link org.apache.maven.resolver.internal.ant.tasks.Install Install} to provide
 * structured dependency information.
 * </p>
 *
 * <h2>Usage Example (inline):</h2>
 * <pre>{@code
 * <repo:resolve>
 *   <repo:dependencies>
 *     <repo:dependency groupId="org.apache.commons" artifactId="commons-lang3" version="3.12.0"/>
 *     <repo:dependency groupId="com.google.guava" artifactId="guava" version="32.0.2"/>
 *   </repo:dependencies>
 *   <path id="my.classpath"/>
 * </resolve>
 * }</pre>
 *
 * <h2>Usage Example (referenced):</h2>
 * <pre>{@code
 * <dependencies id="compile.deps">
 *   <dependency groupId="org.slf4j" artifactId="slf4j-api" version="2.0.7"/>
 * </dependencies>
 *
 * <resolve dependenciesRef="compile.deps">
 *   <path id="compile.classpath"/>
 * </resolve>
 * }</pre>
 *
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><strong>id</strong> — optional Ant reference ID, allowing reuse in multiple tasks</li>
 * </ul>
 *
 * <h2>Nested Elements:</h2>
 * <ul>
 *   <li>{@code <dependency>} — defines a single Maven dependency (see {@link Dependency})</li>
 * </ul>
 *
 * <h2>Typical Use Cases:</h2>
 * <ul>
 *   <li>Providing dependencies to resolution tasks like Resolve or CreatePom</li>
 *   <li>Reusing a named set of dependencies across tasks via {@code dependenciesRef}</li>
 * </ul>
 *
 * <h2>Behavior:</h2>
 * After being referenced by a task (e.g. via {@code dependenciesRef}), the container is
 * evaluated and each contained {@code <dependency>} is passed to the task’s resolution
 * or POM generation logic.
 * </p>
 *
 * @see Dependency
 * @see org.apache.maven.resolver.internal.ant.tasks.Resolve
 * @see org.apache.maven.resolver.internal.ant.tasks.CreatePom
 * @see org.apache.maven.resolver.internal.ant.tasks.Deploy
 * @see org.apache.maven.resolver.internal.ant.tasks.Install
 */

public class Dependencies extends DataType implements DependencyContainer {

    private File file;

    private Pom pom;

    private final List<DependencyContainer> containers = new ArrayList<>();

    private final List<Exclusion> exclusions = new ArrayList<>();

    private boolean nestedDependencies;

    /**
     * Performs the check for circular references and returns the Dependencies object.
     * This is equivalent to calling getCheckedRef(Dependencies.class)
     *
     * @return The Dependencies
     */
    protected Dependencies getRef() {
        return getCheckedRef(Dependencies.class);
    }

    @Override
    public void validate(Task task) {
        if (isReference()) {
            getRef().validate(task);
        } else {
            if (getPom() != null && getPom().getFile() == null) {
                throw new BuildException("A <pom> used for dependency resolution has to be backed by a pom.xml file");
            }
            Map<String, String> ids = new HashMap<>();
            for (DependencyContainer container : containers) {
                container.validate(task);
                if (container instanceof Dependency) {
                    Dependency dependency = (Dependency) container;
                    String id = dependency.getVersionlessKey();
                    String collision = ids.put(id, dependency.getVersion());
                    if (collision != null) {
                        throw new BuildException("You must not declare multiple <dependency> elements"
                                + " with the same coordinates but got " + id + " -> " + collision + " vs "
                                + dependency.getVersion());
                    }
                }
            }
        }
    }

    @Override
    public void setRefid(Reference ref) {
        if (pom != null || !exclusions.isEmpty() || !containers.isEmpty()) {
            throw noChildrenAllowed();
        }
        super.setRefid(ref);
    }

    /**
     * Set the file attribute.
     *
     * @param file the File to set
     */
    public void setFile(File file) {
        checkAttributesAllowed();
        this.file = file;
        checkExternalSources();
    }

    /**
     * Get the file attribute.
     *
     * @return the File.
     */
    public File getFile() {
        if (isReference()) {
            return getRef().getFile();
        }
        return file;
    }

    /**
     * Allows ant to add the pom element specified in the build file.
     * Only one pom element is allowed.
     *
     * @param pom the Pom element to add.
     */
    public void addPom(Pom pom) {
        checkChildrenAllowed();
        if (this.pom != null) {
            throw new BuildException("You must not specify multiple <pom> elements");
        }
        this.pom = pom;
        checkExternalSources();
    }

    public Pom getPom() {
        if (isReference()) {
            return getRef().getPom();
        }
        return pom;
    }

    public void setPomRef(Reference ref) {
        if (pom == null) {
            pom = new Pom();
            pom.setProject(getProject());
        }
        pom.setRefid(ref);
        checkExternalSources();
    }

    private void checkExternalSources() {
        if (file != null && pom != null) {
            throw new BuildException("You must not specify both a text file and a POM to list dependencies");
        }
        if ((file != null || pom != null) && nestedDependencies) {
            throw new BuildException("You must not specify both a file/POM and nested dependency collections");
        }
    }

    public void addDependency(Dependency dependency) {
        checkChildrenAllowed();
        containers.add(dependency);
    }

    public void addDependencies(Dependencies dependencies) {
        checkChildrenAllowed();
        if (dependencies == this) {
            throw circularReference();
        }
        containers.add(dependencies);
        nestedDependencies = true;
        checkExternalSources();
    }

    public List<DependencyContainer> getDependencyContainers() {
        if (isReference()) {
            return getRef().getDependencyContainers();
        }
        return containers;
    }

    public void addExclusion(Exclusion exclusion) {
        checkChildrenAllowed();
        this.exclusions.add(exclusion);
    }

    public List<Exclusion> getExclusions() {
        if (isReference()) {
            return getRef().getExclusions();
        }
        return exclusions;
    }
}
