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

import java.util.Collections;
import java.util.List;

import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;
import org.eclipse.aether.repository.RepositoryPolicy;

/**
 * Represents a single remote repository used for dependency resolution or deployment.
 * <p>
 * Supports configuration of release and snapshot policies, authentication,
 * checksum and update strategies, and type-specific behaviors.
 * </p>
 * <p>
 * May be defined inline in Ant build scripts or referenced via {@code refid}.
 * </p>
 *
 * @see RemoteRepositoryContainer
 * @see RemoteRepositories
 * @see Authentication
 */
public class RemoteRepository extends DataType implements RemoteRepositoryContainer {

    private String id;

    private String url;

    private String type;

    private Policy releasePolicy;

    private Policy snapshotPolicy;

    private boolean releases = true;

    private boolean snapshots = false;

    private String checksums;

    private String updates;

    private Authentication authentication;

    /**
     * Default constructor initializes a new {@code RemoteRepository} instance.
     */
    public RemoteRepository() {
        // Default constructor
    }

    @Override
    public void setProject(Project project) {
        super.setProject(project);

        // NOTE: Just trigger side-effect of default initialization before this type potentially overrides central
        AntRepoSys.getInstance(project);
    }

    /**
     * Resolves this instance as a reference and ensures it is a {@link RemoteRepository}.
     *
     * @return the referenced {@link RemoteRepository} instance
     * @throws BuildException if the reference is not valid
     */
    protected RemoteRepository getRef() {
        return getCheckedRef(RemoteRepository.class);
    }

    /**
     * Validates this repository's configuration.
     * Ensures that both {@code id} and {@code url} are specified unless this is a reference.
     *
     * @param task the Ant task requesting validation, used for error reporting
     * @throws BuildException if required attributes are missing
     */
    @Override
    public void validate(Task task) {
        if (isReference()) {
            getRef().validate(task);
        } else {
            if (url == null || url.length() <= 0) {
                throw new BuildException("You must specify the 'url' for a remote repository");
            }
            if (id == null || id.length() <= 0) {
                throw new BuildException("You must specify the 'id' for a remote repository");
            }
        }
    }

    /**
     * Marks this repository as a reference to another {@code RemoteRepository} instance.
     * <p>
     * Once set, this instance must not define other attributes or child elements.
     * </p>
     *
     * @param ref the Ant reference to another {@code RemoteRepository}
     * @throws BuildException if conflicting attributes or children are already set
     */
    @Override
    public void setRefid(Reference ref) {
        if (id != null || url != null || type != null || checksums != null || updates != null) {
            throw tooManyAttributes();
        }
        if (releasePolicy != null || snapshotPolicy != null || authentication != null) {
            throw noChildrenAllowed();
        }
        super.setRefid(ref);
    }

    /**
     * Gets the unique identifier for this repository.
     *
     * @return the repository ID
     */
    public String getId() {
        if (isReference()) {
            return getRef().getId();
        }
        return id;
    }

    /**
     * Sets the unique identifier for this repository.
     *
     * @param id the repository ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the URL of this repository.
     *
     * @return the base URL of the repository
     */
    public String getUrl() {
        if (isReference()) {
            return getRef().getUrl();
        }
        return url;
    }

    /**
     * Sets the URL of this repository.
     *
     * @param url the base URL of the repository
     * @throws BuildException if attributes are not allowed due to refid
     */
    public void setUrl(String url) {
        checkAttributesAllowed();
        this.url = url;
    }

    /**
     * Gets the layout type of this repository (e.g., {@code default}, {@code legacy}).
     *
     * @return the type of repository layout
     */
    public String getType() {
        if (isReference()) {
            return getRef().getType();
        }
        return (type != null) ? type : "default";
    }

    /**
     * Sets the layout type of this repository.
     *
     * @param type the repository layout type
     */
    public void setType(String type) {
        checkAttributesAllowed();
        this.type = type;
    }

    /**
     * Gets the release policy configured for this repository.
     *
     * @return the release policy or {@code null} if none is set
     */
    public Policy getReleasePolicy() {
        if (isReference()) {
            return getRef().getReleasePolicy();
        }
        return releasePolicy;
    }

    /**
     * Allows Ant to add a release policy to this repository.
     *
     * @param policy the policy to apply to release artifacts
     * @throws BuildException if a release policy was already defined
     */
    public void addReleases(Policy policy) {
        checkChildrenAllowed();
        if (this.releasePolicy != null) {
            throw new BuildException("You must not specify multiple <releases> elements");
        }
        this.releasePolicy = policy;
    }

    /**
     * Gets the snapshot policy configured for this repository.
     *
     * @return the snapshot policy or {@code null} if none is set
     */
    public Policy getSnapshotPolicy() {
        if (isReference()) {
            return getRef().getSnapshotPolicy();
        }
        return snapshotPolicy;
    }

    /**
     * Adds a snapshot policy to this repository.
     *
     * @param policy the policy to apply to snapshot artifacts
     * @throws BuildException if a snapshot policy was already defined
     */
    public void addSnapshots(Policy policy) {
        checkChildrenAllowed();
        if (this.snapshotPolicy != null) {
            throw new BuildException("You must not specify multiple <snapshots> elements");
        }
        this.snapshotPolicy = policy;
    }

    /**
     * Indicates whether releases are enabled for this repository.
     *
     * @return {@code true} if releases are enabled, {@code false} otherwise
     */
    public boolean isReleases() {
        if (isReference()) {
            return getRef().isReleases();
        }
        return releases;
    }

    /**
     * Sets whether releases are enabled for this repository.
     *
     * @param releases {@code true} to enable release artifacts
     */
    public void setReleases(boolean releases) {
        checkAttributesAllowed();
        this.releases = releases;
    }

    /**
     * Indicates whether snapshots are enabled for this repository.
     *
     * @return {@code true} if snapshots are enabled, {@code false} otherwise
     */
    public boolean isSnapshots() {
        if (isReference()) {
            return getRef().isSnapshots();
        }
        return snapshots;
    }

    /**
     * Sets whether snapshots are enabled for this repository.
     *
     * @param snapshots {@code true} to enable snapshot artifacts
     */
    public void setSnapshots(boolean snapshots) {
        checkAttributesAllowed();
        this.snapshots = snapshots;
    }

    /**
     * Gets the update policy for this repository.
     *
     * @return the update policy (e.g., {@code daily}, {@code always}, {@code never})
     */
    public String getUpdates() {
        if (isReference()) {
            return getRef().getUpdates();
        }
        return (updates != null) ? updates : RepositoryPolicy.UPDATE_POLICY_DAILY;
    }

    /**
     * Sets the update policy for this repository.
     *
     * @param updates the update policy string
     * @throws BuildException if the policy is not valid
     */
    public void setUpdates(String updates) {
        checkAttributesAllowed();
        checkUpdates(updates);
        this.updates = updates;
    }

    /**
     * Validates that the given update policy string is one of the permitted values.
     *
     * @param updates the update policy string to validate
     * @throws BuildException if the policy is not permitted
     */
    protected static void checkUpdates(String updates) {
        if (!RepositoryPolicy.UPDATE_POLICY_ALWAYS.equals(updates)
                && !RepositoryPolicy.UPDATE_POLICY_DAILY.equals(updates)
                && !RepositoryPolicy.UPDATE_POLICY_NEVER.equals(updates)
                && !updates.startsWith(RepositoryPolicy.UPDATE_POLICY_INTERVAL)) {
            throw new BuildException("'" + updates + "' is not a permitted update policy");
        }
    }

    /**
     * Gets the checksum policy for this repository.
     *
     * @return the checksum policy (e.g., {@code fail}, {@code warn}, {@code ignore})
     */
    public String getChecksums() {
        if (isReference()) {
            return getRef().getChecksums();
        }
        return (checksums != null) ? checksums : RepositoryPolicy.CHECKSUM_POLICY_WARN;
    }

    /**
     * Sets the checksum policy for this repository.
     *
     * @param checksums the checksum policy
     * @throws BuildException if the policy is not valid
     */
    public void setChecksums(String checksums) {
        checkAttributesAllowed();
        checkChecksums(checksums);
        this.checksums = checksums;
    }

    /**
     * Validates that the given checksum policy string is one of the permitted values.
     *
     * @param checksums the checksum policy string to validate
     * @throws BuildException if the policy is not permitted
     */
    protected static void checkChecksums(String checksums) {
        if (!RepositoryPolicy.CHECKSUM_POLICY_FAIL.equals(checksums)
                && !RepositoryPolicy.CHECKSUM_POLICY_WARN.equals(checksums)
                && !RepositoryPolicy.CHECKSUM_POLICY_IGNORE.equals(checksums)) {
            throw new BuildException("'" + checksums + "' is not a permitted checksum policy");
        }
    }

    /**
     * Gets the {@link Authentication} credentials configured for this repository.
     *
     * @return the authentication element, or {@code null} if none
     */
    public Authentication getAuthentication() {
        if (isReference()) {
            return getRef().getAuthentication();
        }
        return authentication;
    }

    /**
     * Allow Ant to add an {@link Authentication} element to this repository.
     *
     * @param authentication the authentication credentials
     * @throws BuildException if multiple authentications are specified
     */
    public void addAuthentication(Authentication authentication) {
        checkChildrenAllowed();
        if (this.authentication != null) {
            throw new BuildException("You must not specify multiple <authentication> elements");
        }
        this.authentication = authentication;
    }

    /**
     * Sets a reference to an existing {@link Authentication} element.
     *
     * @param ref the reference to an {@code Authentication} definition
     */
    public void setAuthRef(Reference ref) {
        checkAttributesAllowed();
        if (authentication == null) {
            authentication = new Authentication();
            authentication.setProject(getProject());
        }
        authentication.setRefid(ref);
    }

    /**
     * Returns a singleton list containing this repository.
     * This allows {@code RemoteRepository} to be treated as a {@link RemoteRepositoryContainer}.
     *
     * @return a singleton list with this repository
     */
    @Override
    public List<RemoteRepository> getRepositories() {
        return Collections.singletonList(this);
    }

    /**
     * Represents a repository policy configuration for either snapshot or release handling.
     * Includes controls for enabling/disabling access, checksum verification, and update frequency.
     *
     * @see RepositoryPolicy
     */
    public static class Policy {

        private boolean enabled = true;

        private String checksumPolicy;

        private String updatePolicy;

        /**
         * Constructs a new {@code Policy} instance with default settings.
         * The policy is enabled by default, with no specific checksum or update policies set.
         */
        public Policy() {
            // Default constructor
        }

        /**
         * Indicates whether the policy is enabled.
         *
         * @return {@code true} if enabled, {@code false} otherwise
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Enables or disables the policy.
         *
         * @param enabled whether to enable the policy
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Gets the checksum policy string.
         *
         * @return the checksum policy (e.g., {@code warn}, {@code fail})
         */
        public String getChecksums() {
            return checksumPolicy;
        }

        /**
         * Sets the checksum policy for this policy.
         *
         * @param checksumPolicy the checksum policy
         * @throws BuildException if the policy is invalid
         */
        public void setChecksums(String checksumPolicy) {
            checkChecksums(checksumPolicy);
            this.checksumPolicy = checksumPolicy;
        }

        /**
         * Gets the update policy string.
         *
         * @return the update policy (e.g., {@code daily}, {@code never})
         */
        public String getUpdates() {
            return updatePolicy;
        }

        /**
         * Sets the update policy for this policy.
         *
         * @param updatePolicy the update policy
         * @throws BuildException if the policy is invalid
         */
        public void setUpdates(String updatePolicy) {
            checkUpdates(updatePolicy);
            this.updatePolicy = updatePolicy;
        }
    }
}
