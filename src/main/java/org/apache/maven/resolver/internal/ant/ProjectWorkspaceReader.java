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
package org.apache.maven.resolver.internal.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.model.Model;
import org.apache.maven.resolver.internal.ant.types.Pom;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.WorkspaceReader;
import org.eclipse.aether.repository.WorkspaceRepository;
import org.eclipse.aether.util.artifact.ArtifactIdUtils;

/**
 * Workspace reader caching available POMs and artifacts for ant builds.
 * <p>
 * This reader helps Maven Resolver resolve artifacts and POMs from the build workspace
 * without accessing a remote or local repository, useful during dependency resolution
 * and POM inheritance scenarios where temporary or generated models are involved.
 * </p>
 * <p>
 * &lt;pom&gt; elements are cached if they are defined by the 'file'-attribute, as they reference a backing pom.xml file that
 * can be used for resolution with Aether. &lt;artifact&gt; elements are cached if they directly define a 'pom'-attribute
 * or child. The POM may be file-based or in-memory.
 * </p>
 */
public class ProjectWorkspaceReader implements WorkspaceReader {

    private static volatile ProjectWorkspaceReader instance;

    private static final Object LOCK = new Object();

    private final Map<String, Artifact> artifacts = new ConcurrentHashMap<>();

    /**
     * Registers a {@link Pom} into the workspace. Only POMs backed by a file
     * are accepted and cached.
     *
     * @param pom the POM to register
     */
    public void addPom(Pom pom) {
        if (pom.getFile() != null) {
            Model model = pom.getModel(pom);
            Artifact aetherArtifact =
                    new DefaultArtifact(model.getGroupId(), model.getArtifactId(), null, "pom", model.getVersion());
            aetherArtifact = aetherArtifact.setFile(pom.getFile());
            String coords = coords(aetherArtifact);
            artifacts.put(coords, aetherArtifact);
        }
    }

    /**
     * Registers an {@link org.apache.maven.resolver.internal.ant.types.Artifact} along with its
     * associated POM. Only artifacts that reference a POM are accepted.
     *
     * @param artifact the artifact to register
     */
    public void addArtifact(org.apache.maven.resolver.internal.ant.types.Artifact artifact) {
        if (artifact.getPom() != null) {
            Pom pom = artifact.getPom();
            Artifact aetherArtifact;
            if (pom.getFile() != null) {
                Model model = pom.getModel(pom);
                aetherArtifact = new DefaultArtifact(
                        model.getGroupId(),
                        model.getArtifactId(),
                        artifact.getClassifier(),
                        artifact.getType(),
                        model.getVersion());
            } else {
                aetherArtifact = new DefaultArtifact(
                        pom.getGroupId(),
                        pom.getArtifactId(),
                        artifact.getClassifier(),
                        artifact.getType(),
                        pom.getVersion());
            }
            aetherArtifact = aetherArtifact.setFile(artifact.getFile());

            String coords = coords(aetherArtifact);
            artifacts.put(coords, aetherArtifact);
        }
    }

    /**
     * Computes the string coordinate used to uniquely identify an artifact in the workspace.
     *
     * @param artifact the artifact to identify
     * @return a coordinate string (usually {@code groupId:artifactId:type:classifier:version})
     */
    private String coords(Artifact artifact) {
        return ArtifactIdUtils.toId(artifact);
    }

    /**
     * Returns the workspace repository used for this reader. This is always
     * named {@code "ant"}.
     *
     * @return the workspace repository
     */
    @Override
    public WorkspaceRepository getRepository() {
        return new WorkspaceRepository("ant");
    }

    /**
     * Attempts to resolve the artifact file for the given artifact from the workspace.
     *
     * @param artifact the artifact to locate
     * @return the associated file if found, or {@code null} if not registered
     */
    @Override
    public File findArtifact(Artifact artifact) {
        artifact = artifacts.get(coords(artifact));
        return (artifact != null) ? artifact.getFile() : null;
    }

    /**
     * Lists all known versions of the given artifact in the workspace.
     *
     * @param artifact the artifact (ignoring version) to search for
     * @return list of available versions; may be empty but never {@code null}
     */
    @Override
    public List<String> findVersions(Artifact artifact) {
        List<String> versions = new ArrayList<>();
        for (Artifact art : artifacts.values()) {
            if (ArtifactIdUtils.equalsVersionlessId(artifact, art)) {
                versions.add(art.getVersion());
            }
        }
        return versions;
    }

    /**
     * Internal constructor. Use {@link #getInstance()} to retrieve the singleton instance.
     */
    ProjectWorkspaceReader() {}

    /**
     * Returns the singleton instance of this workspace reader.
     * Creates the instance lazily and ensures thread safety.
     *
     * @return the global {@link ProjectWorkspaceReader} instance
     */
    public static ProjectWorkspaceReader getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = new ProjectWorkspaceReader();
                }
            }
        }
        return instance;
    }

    /**
     * Resets the singleton instance. Primarily intended for unit tests.
     */
    static void dropInstance() {
        instance = null;
    }
}
