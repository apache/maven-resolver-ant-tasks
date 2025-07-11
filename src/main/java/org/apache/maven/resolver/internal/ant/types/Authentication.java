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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * Defines credentials and authentication settings for remote repositories.
 * <p>
 * This Ant {@link DataType} provides authentication information such as
 * username/password or private key/passphrase credentials. It is used by
 * remote repositories to enable secure access for resolving or deploying artifacts.
 * </p>
 *
 * <p>
 * Authentication settings may be declared inline within repository declarations or
 * referenced by ID to support reuse.
 * </p>
 *
 * <h2>Supported Authentication Mechanisms:</h2>
 * <ul>
 *   <li>Username/password</li>
 *   <li>Private key with optional passphrase (for SSH)</li>
 * </ul>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * <authentication id="repo.auth" username="deployer" password="secret"/>
 *
 * <repository id="private" url="sftp://repo.mycompany.com/releases">
 *   <authentication refid="repo.auth"/>
 * </repository>
 * }</pre>
 *
 * @see org.apache.maven.resolver.internal.ant.types.RemoteRepository
 */
public class Authentication extends DataType {

    private String username;

    private String password;

    private String privateKeyFile;

    private String passphrase;

    private final List<String> servers = new ArrayList<>();

    /**
     * Default constructor for {@code Authentication} data type.
     */
    public Authentication() {
        // Default constructor
    }

    /**
     * Registers this authentication object with the current project.
     *
     * @param project the current Ant project
     */
    @Override
    public void setProject(Project project) {
        super.setProject(project);

        AntRepoSys.getInstance(project).addAuthentication(this);
    }

    /**
     * Resolves this object if defined as a reference and verifies that it is a
     * {@code Authentication} instance.
     *
     * @return the referenced {@code Authentication} instance
     * @throws org.apache.tools.ant.BuildException if the reference is invalid
     */
    protected Authentication getRef() {
        return getCheckedRef(Authentication.class);
    }

    /**
     * Sets a reference to an existing {@code Authentication} object.
     *
     * @param ref the reference ID to use
     * @throws org.apache.tools.ant.BuildException if any attributes have already been set
     */
    @Override
    public void setRefid(Reference ref) {
        if (username != null || password != null || privateKeyFile != null || passphrase != null) {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
    }

    /**
     * Gets the username used for authentication.
     *
     * @return the username, or the referenced value if this is a reference
     */
    public String getUsername() {
        if (isReference()) {
            return getRef().getUsername();
        }
        return username;
    }

    /**
     * Sets the username used for authentication.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        checkAttributesAllowed();
        this.username = username;
    }

    /**
     * Gets the password used for authentication.
     *
     * @return the password, or the referenced value if this is a reference
     */
    public String getPassword() {
        if (isReference()) {
            return getRef().getPassword();
        }
        return password;
    }

    /**
     * Sets the password used for authentication.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        checkAttributesAllowed();
        this.password = password;
    }

    /**
     * Gets the path to the private key file (used for SSH authentication).
     *
     * @return the private key file path, or the referenced value if this is a reference
     */
    public String getPrivateKeyFile() {
        if (isReference()) {
            return getRef().getPrivateKeyFile();
        }
        return privateKeyFile;
    }

    /**
     * Sets the path to the private key file (used for SSH authentication).
     *
     * @param privateKeyFile the private key file path to set
     */
    public void setPrivateKeyFile(String privateKeyFile) {
        checkAttributesAllowed();
        this.privateKeyFile = privateKeyFile;
    }

    /**
     * Gets the passphrase used for the private key file (if required).
     *
     * @return the passphrase, or the referenced value if this is a reference
     */
    public String getPassphrase() {
        if (isReference()) {
            return getRef().getPassphrase();
        }
        return passphrase;
    }

    /**
     * Sets the passphrase used for the private key file (if required).
     *
     * @param passphrase the passphrase to set
     */
    public void setPassphrase(String passphrase) {
        checkAttributesAllowed();
        this.passphrase = passphrase;
    }

    /**
     * Gets the list of server IDs this authentication applies to.
     *
     * @return a list of server ID strings
     */
    public List<String> getServers() {
        if (isReference()) {
            return getRef().getServers();
        }
        return servers;
    }

    /**
     * Sets the list of server IDs this authentication applies to.
     * <p>
     * Multiple server IDs may be specified using {@code ;} or {@code :} as separators.
     * </p>
     *
     * @param servers a semicolon- or colon-separated list of server IDs
     * @throws org.apache.tools.ant.BuildException if attributes are not allowed i.e.
     * it is defined as a reference.
     */
    public void setServers(String servers) {
        checkAttributesAllowed();
        this.servers.clear();
        String[] split = servers.split("[;:]");
        for (String server : split) {
            server = server.trim();
            if (!server.isEmpty()) {
                this.servers.add(server);
            }
        }
    }
}
