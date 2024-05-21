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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 */
public class Artifacts extends DataType implements ArtifactContainer {

    private final List<ArtifactContainer> containers = new ArrayList<>();

    protected Artifacts getRef() {
        return getCheckedRef(Artifacts.class);
    }

    @Override
    public void validate(Task task) {
        if (isReference()) {
            getRef().validate(task);
        } else {
            for (ArtifactContainer container : containers) {
                container.validate(task);
            }
        }
    }

    @Override
    public void setRefid(Reference ref) {
        if (!containers.isEmpty()) {
            throw noChildrenAllowed();
        }
        super.setRefid(ref);
    }

    public void addArtifact(Artifact artifact) {
        checkChildrenAllowed();
        containers.add(artifact);
    }

    public void addArtifacts(Artifacts artifacts) {
        checkChildrenAllowed();
        if (artifacts == this) {
            throw circularReference();
        }
        containers.add(artifacts);
    }

    @Override
    public List<Artifact> getArtifacts() {
        if (isReference()) {
            return getRef().getArtifacts();
        }
        List<Artifact> artifacts = new ArrayList<>();
        for (ArtifactContainer container : containers) {
            artifacts.addAll(container.getArtifacts());
        }
        return artifacts;
    }
}
