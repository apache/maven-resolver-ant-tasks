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
 * Represents a releases section in the repository section of the project object model.
 */
public class Releases extends org.apache.tools.ant.types.DataType {
    private Enabled enabled;

    public Releases() {
        super();
    }

    /**
     * Adds an Enabled element to the releases section.
     *
     * @param enabled the Enabled element to add
     */
    public void addEnabled(Enabled enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the Enabled element of the releases section.
     *
     * @return the Enabled element
     */
    public Enabled getEnabled() {
        return enabled;
    }

    /**
     * Returns the enabled value of the releases section.
     * If no Enabled element is present, it returns false.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        if (enabled == null) {
            return false;
        } else {
            return enabled.isEnabled();
        }
    }
}
