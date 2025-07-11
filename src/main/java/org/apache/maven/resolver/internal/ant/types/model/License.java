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

import java.util.StringJoiner;

import org.apache.tools.ant.types.DataType;

/**
 * Represents a license in a project model.
 * This class encapsulates the license information including name, URL, comments, and distribution.
 */
public class License extends DataType {
    private Name name;
    private Url url;
    private Comments comments;
    private Distribution distribution;

    /**
     * Default constructor.
     */
    public License() {
        super();
    }

    /**
     * Gets the name of the license.
     *
     * @return the name of the license, or null if not set
     */
    public String getName() {
        return name == null ? null : name.getText();
    }

    /**
     * Gets the URL associated with the license.
     *
     * @return the URL of the license, or null if not set
     */
    public String getUrl() {
        return url == null ? null : url.getText();
    }

    /**
     * Gets the comments associated with the license.
     *
     * @return the comments of the license, or null if not set
     */
    public String getComments() {
        return comments == null ? null : comments.getText();
    }

    /**
     * Gets the distribution type of the license.
     *
     * @return the distribution of the license, or null if not set
     */
    public String getDistribution() {
        return distribution == null ? null : distribution.getText();
    }

    /**
     * Adds a name to the license.
     *
     * @param name the name element to be added
     */
    public void addName(Name name) {
        this.name = name;
    }

    /**
     * Adds a URL to the license.
     *
     * @param url the url element to be added
     */
    public void addUrl(Url url) {
        this.url = url;
    }

    /**
     * Adds comments to the license.
     *
     * @param comments the comments element to be added
     */
    public void addComments(Comments comments) {
        this.comments = comments;
    }

    /**
     * Adds a distribution type to the license.
     *
     * @param distribution the distribution element to be added
     */
    public void addDistribution(Distribution distribution) {
        this.distribution = distribution;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", License.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .add("url='" + url + "'")
                .add("comments='" + comments + "'")
                .add("distribution='" + distribution + "'")
                .toString();
    }
}
