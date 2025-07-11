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
 * Represents comments in a license section.
 */
public class Comments extends DataType {
    private String comments;

    /**
     * Default constructor for Comments.
     * Initializes an empty Comments object.
     */
    public Comments() {}

    /**
     * Adds text to the comments element.
     *
     * @param comments the comments text to add
     */
    public void addText(String comments) {
        this.comments = getProject().replaceProperties(comments);
    }

    /**
     * Gets the comments text (the value).
     *
     * @return the comments text
     */
    public String getText() {
        return comments;
    }
}
