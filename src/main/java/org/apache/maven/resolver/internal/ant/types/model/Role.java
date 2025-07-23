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
 * Represents the <code>role</code> element of e.g. a <code>roles</code> section.
 *
 */
public class Role extends DataType {
    private String role;

    /**
     * Default constructor.
     */
    public Role() {}

    /**
     * Allow ant to add text to the <code>role</code> element, replacing any properties in the text.
     *
     * @param role the <code>role</code> value to add
     */
    public void addText(String role) {
        this.role = getProject().replaceProperties(role);
    }

    /**
     * Returns the text of the <code>role</code> element.
     *
     * @return the <code>Role</code> text
     */
    public String getText() {
        return role;
    }
}
