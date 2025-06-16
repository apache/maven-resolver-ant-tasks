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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;

public class Enabled extends DataType {
    private boolean enabled;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addText(String text) {
        String trimmed = getProject().replaceProperties(text).trim();
        if ("true".equalsIgnoreCase(trimmed)) {
            this.enabled = true;
        } else if ("false".equalsIgnoreCase(trimmed)) {
            this.enabled = false;
        } else {
            throw new BuildException("Invalid value for <enabled>: " + text);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
