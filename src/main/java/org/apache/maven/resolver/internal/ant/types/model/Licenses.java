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
 * Represents a collection of licenses in a project model.
 * This class allows adding multiple License objects and retrieving them as a list.
 */
public class Licenses extends DataType {

    private final List<License> licenses = new ArrayList<>();

    /**
     * Default constructor.
     */
    public Licenses() {}

    /**
     * gets the list of licenses.
     *
     * @return a list of License objects, never null
     */
    public List<License> getLicenses() {
        return licenses;
    }

    /**
     * Adds a License to the collection.
     *
     * @param license the License object to add, must not be null
     */
    public void addLicense(License license) {
        licenses.add(license);
    }
}
