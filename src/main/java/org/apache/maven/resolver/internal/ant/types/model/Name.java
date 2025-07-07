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
 * Represents a name element used in a license or a repository section in the project model.
 */
public class Name extends DataType {

    private String name;

    /**
     * Default constructor.
     */
    public Name() {}

    /**
     * Adds text to the name element, replacing any properties in the text.
     *
     * @param name the name text to add
     */
    public void addText(String name) {
        this.name = getProject().replaceProperties(name);
    }

    /**
     * Returns the text of the name element.
     *
     * @return the name text
     */
    public String getText() {
        return name;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Name.class.getSimpleName() + "[", "]")
                .add("name='" + name + "'")
                .toString();
    }
}
