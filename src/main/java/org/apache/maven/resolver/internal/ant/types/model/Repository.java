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

import org.apache.tools.ant.types.DataType;

/**
 * Represents a repository in a project model.
 */
public class Repository extends DataType {
    Id id;
    Name name;
    Url url;
    Layout layout;
    Releases releases; // actually a boolean
    Snapshots snapshots; // actually a boolean

    public void addId(Id id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        this.id = id;
    }

    public String getId() {
        return id.getText();
    }

    public void addName(Name name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }

    public String getName() {
        return name == null ? null : name.getText();
    }

    public void addUrl(Url url) {
        if (url == null) {
            throw new IllegalArgumentException("Url cannot be null");
        }
        this.url = url;
    }

    public String getUrl() {
        return url == null ? null : url.getText();
    }

    public void addLayout(Layout layout) {
        if (layout == null) {
            throw new IllegalArgumentException("Layout cannot be null");
        }
        this.layout = layout;
    }

    public String getLayout() {
        return layout == null ? null : layout.getText();
    }

    public Releases getReleases() {
        return releases;
    }

    public Snapshots getSnapshots() {
        return snapshots;
    }

    public void addReleases(Releases releases) {
        if (releases == null) {
            throw new IllegalArgumentException("Releases cannot be null");
        }
        this.releases = releases;
    }

    public void addSnapshots(Snapshots snapshots) {
        if (snapshots == null) {
            throw new IllegalArgumentException("Snapshots cannot be null");
        }
        this.snapshots = snapshots;
    }
}
