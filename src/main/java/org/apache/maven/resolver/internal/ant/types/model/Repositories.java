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
 * Represents a collection of repository sections in a project model.
 * This class allows adding multiple repository objects and retrieving them as a list.
 */
public class Repositories extends DataType {

    private final List<Repository> repositories = new ArrayList<>();

    /**
     * Get the list of repositories.
     *
     * @return a list of Repository objects, never null
     */
    public List<Repository> getRepositories() {
        return repositories;
    }

    /**
     * Adds a repository to the list of repositories.
     *
     * @param repository the Repository object to add, must not be null
     * @throws IllegalArgumentException if the repository is null
     */
    public void addRepository(Repository repository) {
        repositories.add(repository);
    }
}
