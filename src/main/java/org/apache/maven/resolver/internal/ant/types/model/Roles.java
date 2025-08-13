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

import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.types.DataType;

/**
 * Represents a collection of <code>role</code> sections in a project model.
 * This class allows adding multiple <code>role</code> objects and retrieving them as a list.
 */
public class Roles extends DataType {
    private final List<Role> roles = new ArrayList<>();

    /**
     * Default constructor.
     */
    public Roles() {}

    /**
     * Allows ant to add a role to the list of roles.
     *
     * @param role the Role object to add, must not be null
     */
    public void addRole(Role role) {
        roles.add(role);
    }

    /**
     * Get the list of roles.
     *
     * @return a list of <code>Role</code> objects, never null
     */
    public List<Role> getRoles() {
        return roles;
    }
}
