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

/**
 * Represents a layout in a repository section.
 * The layout is used to determine how artifacts are stored in the repository.
 */
public class Layout extends org.apache.tools.ant.types.DataType {
    private String layout;

    public Layout() {
        super();
    }

    /**
     * Adds text to the layout element.
     *
     * @param layout the layout to add, which should be a valid layout identifier
     */
    public void addText(String layout) {
        this.layout = getProject().replaceProperties(layout);
    }

    /**
     * Gets the layout text.
     *
     * @return the layout text
     */
    public String getText() {
        return layout;
    }

    @Override
    public String toString() {
        return "Layout: " + layout;
    }
}
