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
 * Represents the distribution in a license section.
 */
public class Distribution extends DataType {
    private String distribution;

    public Distribution() {}

    /**
     * Adds text to the distribution element.
     *
     * @param distribution the distribution text to add
     */
    public void addText(String distribution) {
        this.distribution = getProject().replaceProperties(distribution);
    }

    /**
     * Gets the distribution text (the value).
     *
     * @return the distribution text
     */
    public String getText() {
        return distribution;
    }
}
