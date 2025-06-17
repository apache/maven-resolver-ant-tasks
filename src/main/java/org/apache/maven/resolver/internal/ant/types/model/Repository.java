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

    /**
     * Adds an identifier to the repository.
     *
     * @param id the id element to add, must not be null
     */
    public void addId(Id id) {
        if (id == null) {
            throw new IllegalArgumentException("Id cannot be null");
        }
        this.id = id;
    }

    /**
     * Gets the identifier of the repository.
     *
     * @return the id as a String, never null
     */
    public String getId() {
        return id.getText();
    }

    /**
     * Adds a name to the repository.
     *
     * @param name the name element to add, must not be null
     */
    public void addName(Name name) {
        if (name == null) {
            throw new IllegalArgumentException("Name cannot be null");
        }
        this.name = name;
    }

    /**
     * Gets the name of the repository.
     *
     * @return the name as a String, or null if not set
     */
    public String getName() {
        return name == null ? null : name.getText();
    }

    /**
     * Adds a URL to the repository.
     *
     * @param url the url element to add, must not be null
     */
    public void addUrl(Url url) {
        if (url == null) {
            throw new IllegalArgumentException("Url cannot be null");
        }
        this.url = url;
    }

    /**
     * Gets the URL of the repository.
     *
     * @return the URL as a String, or null if not set
     */
    public String getUrl() {
        return url == null ? null : url.getText();
    }

    /**
     * Adds a layout to the repository.
     *
     * @param layout the layout element to add, must not be null
     */
    public void addLayout(Layout layout) {
        if (layout == null) {
            throw new IllegalArgumentException("Layout cannot be null");
        }
        this.layout = layout;
    }

    /**
     * Gets the layout of the repository.
     *
     * @return the layout as a String, or null if not set
     */
    public String getLayout() {
        return layout == null ? null : layout.getText();
    }

    /**
     * Gets the releases configuration of the repository.
     *
     * @return the Releases object, never null
     */
    public Releases getReleases() {
        return releases;
    }

    /**
     * Gets the snapshots configuration of the repository.
     *
     * @return the Snapshots object, never null
     */
    public Snapshots getSnapshots() {
        return snapshots;
    }

    /**
     * Adds releases configuration to the repository.
     *
     * @param releases the Releases object to add, must not be null
     * @throws IllegalArgumentException if releases is null
     */
    public void addReleases(Releases releases) {
        if (releases == null) {
            throw new IllegalArgumentException("Releases cannot be null");
        }
        this.releases = releases;
    }

    /**
     * Adds snapshots configuration to the repository.
     *
     * @param snapshots the Snapshots object to add, must not be null
     * @throws IllegalArgumentException if snapshots is null
     */
    public void addSnapshots(Snapshots snapshots) {
        if (snapshots == null) {
            throw new IllegalArgumentException("Snapshots cannot be null");
        }
        this.snapshots = snapshots;
    }
}
