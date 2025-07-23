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
 * The SCM (Software Configuration Management) section is required if you
 * aim to publish the POM to Maven Central.
 */
public class Scm extends DataType {
    private Connection connection;
    private DeveloperConnection developerConnection;
    private Url url;

    /**
     * Default constructor.
     */
    public Scm() {}

    /**
     * Allows Ant to add a <code>Connection</code> to the Scm.
     *
     * @param connection the connection to add
     */
    public void addConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Get the <code>Connection</code> of this Scm.
     *
     * @return the <code>Connection</code>, may be null if not specified.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Get the <code>Connection</code> text value or null if not set.
     * This corresponds to the text content of the connection element.
     *
     * @return the text value of the Connection
     */
    public String getConnectionText() {
        return getConnection() == null ? null : getConnection().getText();
    }

    /**
     * Allows Ant to add a <code>DeveloperConnection</code> to the Scm.
     *
     * @param developerConnection the developerConnection to add
     */
    public void addDeveloperConnection(DeveloperConnection developerConnection) {
        this.developerConnection = developerConnection;
    }

    /**
     * Get the DeveloperConnection of this Scm.
     *
     * @return the <code>DeveloperConnection</code>, may be null if not specified.
     */
    public DeveloperConnection getDeveloperConnection() {
        return developerConnection;
    }

    /**
     * Get the <code>DeveloperConnection</code> text value or null if not set.
     * This corresponds to the text content of the developerConnection element.
     *
     * @return the text value of the <code>DeveloperConnection</code>
     */
    public String getDeveloperConnectionText() {
        return getDeveloperConnection() == null
                ? null
                : getDeveloperConnection().getText();
    }

    /**
     * Allows Ant to add a <code>Url</code> to the Scm.
     *
     * @param url the <code>Url</code> to add
     */
    public void addUrl(Url url) {
        this.url = url;
    }

    /**
     * Get the <code>Url</code> of this Scm.
     *
     * @return the <code>Url</code>, may be null if not specified.
     */
    public Url getUrl() {
        return url;
    }

    /**
     * Get the <code>Url</code> text value or null if not set.
     * This corresponds to the text content of the url element.
     *
     * @return the text value of the <code>Url</code>
     */
    public String getUrlText() {
        return getUrl() == null ? null : getUrl().getText();
    }
}
