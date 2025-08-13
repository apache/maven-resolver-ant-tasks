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
 * The Connection element is used in the Scm section.
 * An example is:
 * <pre>{@code
 * <connection>scm:git:https://github.com/Alipsa/matrix.git</connection>
 * }</pre>
 */
public class Connection extends DataType {
    String text;

    /**
     * Default constructor.
     */
    public Connection() {}

    /**
     * Allow ant to add text to the <code>connection</code> element, replacing any properties in the text.
     *
     * @param text the <code>Connection</code> text to add
     */
    public void addText(String text) {
        this.text = getProject().replaceProperties(text).trim();
    }

    /**
     * Returns the text of the <code>Connection</code> element.
     *
     * @return the <code>Connection</code> text
     */
    public String getText() {
        return text;
    }
}
