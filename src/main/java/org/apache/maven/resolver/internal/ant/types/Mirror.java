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
 * Defines a mirror repository configuration for use in Maven Ant tasks.
 * <p>
 * A mirror allows you to redirect requests for one or more remote repositories
 * to a different repository URL. This is useful for using internal repositories,
 * caching proxies (e.g., Nexus, Artifactory), or selectively replacing external repositories.
 * </p>
 *
 * <p>
 * This type can be used inline or as a referenced {@code <mirror>} data type
 * elsewhere in the build. When included in an Ant project, the mirror will
 * be registered with the {@link org.apache.maven.resolver.internal.ant.AntRepoSys}
 * instance associated with the current project.
 * </p>
 *
 * <h2>Attributes:</h2>
 * <ul>
 *   <li><strong>id</strong> — unique identifier for the mirror (optional)</li>
 *   <li><strong>url</strong> — base URL of the mirror (required)</li>
 *   <li><strong>mirrorOf</strong> — pattern matching the repositories to be mirrored (required)</li>
 *   <li><strong>type</strong> — optional repository layout type (defaults to {@code "default"})</li>
 * </ul>
 *
 * <h2>Nested Elements:</h2>
 * <ul>
 *   <li><strong>{@code <authentication>}</strong> — optional credentials for accessing the mirror</li>
 * </ul>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * <mirror id="company-mirror" url="https://repo.mycompany.com/maven2" mirrorOf="*" />
 * }</pre>
 *
 * <h2>Notes:</h2>
 * <ul>
 *   <li>Only one {@code <authentication>} element is allowed per mirror</li>
 *   <li>If this element is a reference ({@code refid}), attributes and nested elements are disallowed</li>
 *   <li>This mirror will automatically be added to the {@code AntRepoSys} upon being assigned a project</li>
 * </ul>
 *
 * @see #setProject(org.apache.tools.ant.Project)
 * @see #setRefid(org.apache.tools.ant.types.Reference)
 * @see #addAuthentication(Authentication)
 * @see org.apache.maven.resolver.internal.ant.AntRepoSys
 */
public class Mirror extends DataType {

    private String id;

    private String url;

    private String type;

    private String mirrorOf;

    private Authentication authentication;

    /**
     * Default constructor used by Ant to create a {@code Mirror} instance.
     * <p>
     * This constructor is required for Ant to instantiate this type when defined inline
     * or referenced in an Ant build script.
     * </p>
     */
    public Mirror(){
        // Default constructor for Ant task
    }

    /**
     * Sets the Ant project associated with this mirror.
     * <p>
     * In addition to associating the mirror with the current build context,
     * this method registers the mirror with the {@link org.apache.maven.resolver.internal.ant.AntRepoSys}
     * instance for the project. This allows the mirror to influence repository resolution globally.
     * </p>
     *
     * @param project the Ant project
     *
     * @see org.apache.maven.resolver.internal.ant.AntRepoSys#addMirror(Mirror)
     */
    @Override
    public void setProject(Project project) {
        super.setProject(project);

        AntRepoSys.getInstance(project).addMirror(this);
    }

    /**
     * Returns the referenced {@code Mirror} instance if this object is defined as a reference.
     * <p>
     * This method is used internally to delegate property access to the referenced mirror
     * when {@code refid} is set. It also performs type-checking to ensure that the
     * reference is of the expected type.
     * </p>
     *
     * @return the referenced {@code Mirror} instance
     *
     * @throws org.apache.tools.ant.BuildException if the reference is not of type {@code Mirror}
     *
     * @see #setRefid(org.apache.tools.ant.types.Reference)
     * @see #isReference()
     */
    protected Mirror getRef() {
        return getCheckedRef(Mirror.class);
    }

    @Override
    public void setRefid(Reference ref) {
        if (id != null || url != null || mirrorOf != null || type != null) {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
    }

    /**
     * Returns the identifier of this mirror.
     * <p>
     * The ID is typically used for identification and logging purposes. It does not affect
     * repository resolution but may appear in debug output or reports to help distinguish mirrors.
     * </p>
     *
     * <p>
     * If this mirror is defined as a reference ({@code refid}), the ID is retrieved from
     * the referenced {@code Mirror} instance.
     * </p>
     *
     * @return the mirror ID, or {@code null} if not set
     *
     * @see #setId(String)
     * @see #isReference()
     */
    public String getId() {
        if (isReference()) {
            return getRef().getId();
        }
        return id;
    }

    /**
     * Sets the identifier of the mirror.
     * <p>
     * The ID is optional and is primarily used for identification and logging purposes.
     * It does not affect resolution behavior but can help distinguish multiple mirrors.
     * </p>
     *
     * <p>
     * This method must not be used if the mirror is defined via {@code refid}.
     * </p>
     *
     * @param id the identifier for this mirror
     *
     * @see #getId()
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the URL of the mirror repository.
     * <p>
     * This is the base URL where artifacts will be downloaded from when a matched repository
     * is redirected to this mirror. For example: {@code https://repo.example.org/mirror}.
     * </p>
     *
     * <p>
     * If this {@code Mirror} is defined as a reference ({@code refid}), the URL is resolved
     * from the referenced {@code Mirror} instance.
     * </p>
     *
     * @return the mirror repository URL, or {@code null} if not set
     *
     * @see #setUrl(String)
     * @see #isReference()
     */
    public String getUrl() {
        if (isReference()) {
            return getRef().getUrl();
        }
        return url;
    }

    /**
     * Sets the base URL of the mirror repository.
     * <p>
     * This URL must be a valid repository root (e.g., {@code https://repo.example.com/maven2}).
     * It is used to redirect requests from repositories matched by {@link #setMirrorOf(String)}.
     * </p>
     *
     * <p>
     * This method must not be used if the mirror is defined via {@code refid}.
     * </p>
     *
     * @param url the URL of the mirror repository
     *
     * @throws org.apache.tools.ant.BuildException if this instance is a reference
     *
     * @see #getUrl()
     */
    public void setUrl(String url) {
        checkAttributesAllowed();
        this.url = url;
    }

    /**
     * Returns the layout type of the mirror repository.
     * <p>
     * If no type is explicitly set, this method returns {@code "default"},
     * which is the standard Maven 2 layout.
     * </p>
     *
     * <p>
     * If this mirror is defined as a reference, the type is resolved from the referenced instance.
     * </p>
     *
     * @return the repository layout type, or {@code "default"} if not specified
     *
     * @see #setType(String)
     */
    public String getType() {
        if (isReference()) {
            return getRef().getType();
        }
        return (type != null) ? type : "default";
    }

    /**
     * Sets the layout type of the mirror repository.
     * <p>
     * The default value is {@code "default"}, which corresponds to the standard Maven 2 layout.
     * Other values are rarely used and typically not necessary unless working with
     * custom or legacy repository formats.
     * </p>
     *
     * <p>
     * This method must not be called if this mirror is defined via {@code refid}.
     * </p>
     *
     * @param type the layout type of the mirror repository
     *
     * @throws org.apache.tools.ant.BuildException if this instance is a reference
     *
     * @see #getType()
     */
    public void setType(String type) {
        checkAttributesAllowed();
        this.type = type;
    }

    /**
     * Returns the repository pattern that this mirror applies to.
     * <p>
     * The value is typically a pattern or comma-separated list of repository IDs,
     * such as {@code *}, {@code external:*}, or {@code central,!snapshots}.
     * It determines which repositories should be redirected to this mirror.
     * </p>
     *
     * <p>
     * If this {@code Mirror} is defined as a reference ({@code refid}),
     * the value is retrieved from the referenced instance.
     * </p>
     *
     * @return the {@code mirrorOf} pattern, or {@code null} if not set
     *
     * @see #setMirrorOf(String)
     * @see #isReference()
     */
    public String getMirrorOf() {
        if (isReference()) {
            return getRef().getMirrorOf();
        }
        return mirrorOf;
    }

    /**
     * Specifies the repository IDs that this mirror should apply to.
     * <p>
     * The value can be a single repository ID, a comma-separated list,
     * or a pattern (e.g., {@code *}, {@code external:*}, {@code central,!snapshots}).
     * </p>
     *
     * <p>
     * This value is required unless set via a reference. If this {@code Mirror}
     * is defined as a reference, this method must not be called.
     * </p>
     *
     * @param mirrorOf a pattern or list of repository IDs to be mirrored
     *
     * @throws org.apache.tools.ant.BuildException if this instance is a reference
     *
     * @see #getMirrorOf()
     */
    public void setMirrorOf(String mirrorOf) {
        checkAttributesAllowed();
        this.mirrorOf = mirrorOf;
    }

    /**
     * Adds an {@code <authentication>} element to the mirror.
     * <p>
     * This is used to configure credentials for accessing the mirrored repository.
     * Only one {@code <authentication>} child is allowed. Attempting to add multiple
     * will result in a {@link BuildException}.
     * </p>
     *
     * <p>
     * This method must not be used if the mirror is defined via {@code refid}.
     * </p>
     *
     * @param authentication the {@link Authentication} object to associate with this mirror
     *
     * @throws org.apache.tools.ant.BuildException if an authentication has already been set,
     *         or if this instance is a reference
     *
     * @see #getAuthentication()
     */
    public void addAuthentication(Authentication authentication) {
        checkChildrenAllowed();
        if (this.authentication != null) {
            throw new BuildException("You must not specify multiple <authentication> elements");
        }
        this.authentication = authentication;
    }

    /**
     * Returns the authentication configuration for this mirror, if any.
     * <p>
     * If this {@code Mirror} is defined as a reference, the authentication
     * is resolved from the referenced instance.
     * </p>
     *
     * @return the associated {@link Authentication} or {@code null} if none
     */
    public Authentication getAuthentication() {
        if (isReference()) {
            getRef().getAuthentication();
        }
        return authentication;
    }

    /**
     * Sets a reference to a previously defined {@code <authentication>} element.
     * <p>
     * This allows the mirror to reuse shared credentials declared elsewhere in the build script.
     * If no authentication object exists yet, one is created automatically.
     * </p>
     *
     * @param ref a reference to an {@link Authentication} instance
     */
    public void setAuthRef(Reference ref) {
        if (authentication == null) {
            authentication = new Authentication();
            authentication.setProject(getProject());
        }
        authentication.setRefid(ref);
    }
}
