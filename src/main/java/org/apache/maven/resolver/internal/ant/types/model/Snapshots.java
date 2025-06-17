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
 * Represents a snapshots section in the repository section of the project object model.
 */
public class Snapshots extends org.apache.tools.ant.types.DataType {
    private Enabled enabled;

    public Snapshots() {
        super();
    }

    /**
     * Adds an enabled element to the snapshots section.
     *
     * @param enabled the enabled element to add, must not be null
     */
    public void addEnabled(Enabled enabled) {
        this.enabled = enabled;
    }

    /**
     * Gets the enabled element of the snapshots section.
     *
     * @return the Enabled object, may be null if not set
     */
    public Enabled getEnabled() {
        return enabled;
    }

    /**
     * Returns the boolean value of the enabled element.
     * If the enabled element is not set, it returns false.
     *
     * @return true if enabled is set and true, false otherwise
     */
    public boolean getEnabledValue() {
        if (enabled == null) {
            return false;
        } else {
            return enabled.isEnabled();
        }
    }
}
