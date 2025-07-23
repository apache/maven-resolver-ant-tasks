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
 * The <code>DeveloperConnection</code> element is used in the <code>Scm</code> section.
 * An example is:
 * <pre>{@code
 * <developerConnection>scm:git:git@github.com:Alipsa/matrix.git</developerConnection>
 * }</pre>
 */
public class DeveloperConnection extends DataType {
    String text;

    /**
     * Default constructor.
     */
    public DeveloperConnection() {}

    /**
     * Allow ant to add text to the <code>DeveloperConnection</code> element, replacing any properties in the text.
     *
     * @param text the Connection text to add
     */
    public void addText(String text) {
        this.text = getProject().replaceProperties(text).trim();
    }

    /**
     * Returns the text of the <code>developerConnection</code> element.
     *
     * @return the <code>DeveloperConnection</code> text
     */
    public String getText() {
        return text;
    }
}
