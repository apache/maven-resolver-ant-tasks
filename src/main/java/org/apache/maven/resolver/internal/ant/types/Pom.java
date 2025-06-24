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
 * <pre>{@code
 * <pom file="target/generated-pom.xml" id="my.pom" prefix="lib"/>
 * }</pre>
 * </p>
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

    protected Pom getRef() {
        return (Pom) getCheckedRef();
    }

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

    public void setId(String id) {
        checkAttributesAllowed();
        this.id = id;
    }

    public File getFile() {
        if (isReference()) {
            return getRef().getFile();
        }
        return file;
    }

    public void setFile(File file) {
        checkAttributesAllowed();
        if (groupId != null || artifactId != null || version != null) {
            throw ambiguousSource();
        }

        this.file = file;
    }

    public String getGroupId() {
        if (isReference()) {
            return getRef().getGroupId();
        }
        return groupId;
    }

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

    public String getArtifactId() {
        if (isReference()) {
            return getRef().getArtifactId();
        }
        return artifactId;
    }

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

    public String getVersion() {
        if (isReference()) {
            return getRef().getVersion();
        }
        return version;
    }

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

    public String getCoords() {
        if (isReference()) {
            return getRef().getCoords();
        }
        return coords;
    }

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

    public String getPackaging() {
        if (isReference()) {
            return getRef().getPackaging();
        }
        return packaging;
    }

    public void setPackaging(String packaging) {
        checkAttributesAllowed();
        if (file != null) {
            throw ambiguousSource();
        }
        this.packaging = packaging;
    }

    private RemoteRepositories getRemoteRepos() {
        if (remoteRepositories == null) {
            remoteRepositories = new RemoteRepositories();
            remoteRepositories.setProject(getProject());
        }
        return remoteRepositories;
    }

    public void addRemoteRepo(RemoteRepository repository) {
        getRemoteRepos().addRemoterepo(repository);
    }

    public void addRemoteRepos(RemoteRepositories repositories) {
        getRemoteRepos().addRemoterepos(repositories);
    }

    public void setRemoteReposRef(Reference ref) {
        RemoteRepositories repos = new RemoteRepositories();
        repos.setProject(getProject());
        repos.setRefid(ref);
        getRemoteRepos().addRemoterepos(repos);
    }

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
