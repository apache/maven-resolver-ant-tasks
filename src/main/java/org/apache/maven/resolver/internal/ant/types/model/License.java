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

public class License extends DataType {
    Name name;
    Url url;
    Comments comments;
    Distribution distribution;

    public License() {
        super();
    }

    public String getName() {
        return name == null ? null : name.getText();
    }

    public String getUrl() {
        return url == null ? null : url.getText();
    }

    public String getComments() {
        return comments == null ? null : comments.getText();
    }

    public String getDistribution() {
        return distribution == null ? null : distribution.getText();
    }

    public void addName(Name name) {
        this.name = name;
    }

    public void addUrl(Url url) {
        this.url = url;
    }

    public void addComments(Comments comments) {
        this.comments = comments;
    }

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
