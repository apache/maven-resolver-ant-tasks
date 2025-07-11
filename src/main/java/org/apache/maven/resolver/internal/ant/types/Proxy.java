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
package org.apache.maven.resolver.internal.ant.types;

import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * Defines a proxy configuration for Maven artifact resolution.
 * <p>
 * This Ant data type allows specification of a proxy host, port, type (e.g., HTTP),
 * non-proxy hosts, and optional authentication credentials.
 * Once declared, it is automatically registered with the project's repository system.
 * </p>
 *
 * <p>
 * A proxy definition can also be referenced by {@code refid}, allowing reuse and modular build files.
 * When defined as a reference, no other attributes or nested elements may be set.
 * </p>
 *
 * @see Authentication
 * @see org.apache.maven.resolver.internal.ant.AntRepoSys
 */
public class Proxy extends DataType {

    private String host;

    private int port;

    private String type;

    private String nonProxyHosts;

    private Authentication authentication;

    /**
     * Default constructor initializes the proxy with no settings.
     * This allows attributes to be set before the project is assigned.
     */
    public Proxy() {
        // Default constructor
    }

    /**
     * Sets the associated Ant {@code Project} and registers this proxy instance
     * with the repository system.
     *
     * @param project the Ant project
     */
    @Override
    public void setProject(Project project) {
        super.setProject(project);

        AntRepoSys.getInstance(project).addProxy(this);
    }

    /**
     * Resolves this object if defined as a reference and verifies that it is a
     * {@code Proxy} instance.
     *
     * @return the referenced {@code Proxy} instance
     * @throws org.apache.tools.ant.BuildException if the reference is invalid
     */
    protected Proxy getRef() {
        return getCheckedRef(Proxy.class);
    }

    /**
     * Sets this proxy as a reference to another defined proxy.
     * <p>
     * This method must not be used in combination with any other attributes
     * or nested {@code <authentication>} elements.
     * </p>
     *
     * @param ref the reference to another {@code Proxy} definition
     *
     * @throws org.apache.tools.ant.BuildException if attributes or children are already set
     */
    @Override
    public void setRefid(Reference ref) {
        if (host != null || port != 0 || type != null || nonProxyHosts != null) {
            throw tooManyAttributes();
        }
        if (authentication != null) {
            throw noChildrenAllowed();
        }
        super.setRefid(ref);
    }

    /**
     * Returns the proxy host name or IP address.
     *
     * @return the proxy host
     */
    public String getHost() {
        if (isReference()) {
            return getRef().getHost();
        }
        return host;
    }

    /**
     * Sets the proxy host name or IP address.
     *
     * @param host the proxy host to use
     * @throws org.apache.tools.ant.BuildException if this object is a reference
     */
    public void setHost(String host) {
        checkAttributesAllowed();
        this.host = host;
    }

    /**
     * Returns the proxy port number.
     *
     * @return the port number
     */
    public int getPort() {
        if (isReference()) {
            return getRef().getPort();
        }
        return port;
    }

    /**
     * Sets the proxy port number.
     * Must be within the range 1 to 65535.
     *
     * @param port the port number
     * @throws org.apache.tools.ant.BuildException if the port is out of range or attributes are not allowed
     */
    public void setPort(int port) {
        checkAttributesAllowed();
        if (port <= 0 || port > 0xFFFF) {
            throw new BuildException("The port number must be within the range 1 - 65535");
        }
        this.port = port;
    }

    /**
     * Returns the proxy type, such as {@code http}.
     *
     * @return the proxy type, or {@code null} if not set
     */
    public String getType() {
        if (isReference()) {
            return getRef().getType();
        }
        return type;
    }

    /**
     * Sets the proxy type (e.g., {@code http}).
     *
     * @param type the proxy protocol type
     * @throws org.apache.tools.ant.BuildException if this object is a reference
     */
    public void setType(String type) {
        checkAttributesAllowed();
        this.type = type;
    }

    /**
     * Returns the non-proxy hosts setting, typically a pipe-delimited list.
     *
     * @return the non-proxy hosts
     */
    public String getNonProxyHosts() {
        if (isReference()) {
            return getRef().getNonProxyHosts();
        }
        return nonProxyHosts;
    }

    /**
     * Sets the non-proxy hosts — hosts that should bypass the proxy.
     * The format is usually a pipe-separated list (e.g., {@code localhost|*.example.com}).
     *
     * @param nonProxyHosts the non-proxy hosts pattern
     * @throws org.apache.tools.ant.BuildException if this object is a reference
     */
    public void setNonProxyHosts(String nonProxyHosts) {
        checkAttributesAllowed();
        this.nonProxyHosts = nonProxyHosts;
    }

    /**
     * Returns the {@link Authentication} settings associated with this proxy.
     *
     * @return the authentication configuration, or {@code null} if none
     */
    public Authentication getAuthentication() {
        if (isReference()) {
            return getRef().getAuthentication();
        }
        return authentication;
    }

    /**
     * Allow Ant to add an {@code <authentication>} child element to configure credentials for the proxy.
     *
     * @param authentication the authentication element
     * @throws org.apache.tools.ant.BuildException if multiple authentications are specified or children are not allowed
     */
    public void addAuthentication(Authentication authentication) {
        checkChildrenAllowed();
        if (this.authentication != null) {
            throw new BuildException("You must not specify multiple <authentication> elements");
        }
        this.authentication = authentication;
    }

    /**
     * Sets a reference to an existing {@link Authentication} element to be used for this proxy.
     * <p>
     * This allows the proxy to reuse a separately defined {@code <authentication>} element
     * via Ant's {@code refid} mechanism, enabling modular and reusable configuration.
     * If no authentication has been previously configured, a new {@code Authentication} instance is created and associated with the project.
     * </p>
     *
     * @param ref the Ant reference pointing to an existing {@code Authentication} instance
     *
     * @see #addAuthentication(Authentication)
     * @see Authentication
     */
    public void setAuthRef(Reference ref) {
        if (authentication == null) {
            authentication = new Authentication();
            authentication.setProject(getProject());
        }
        authentication.setRefid(ref);
    }
}
