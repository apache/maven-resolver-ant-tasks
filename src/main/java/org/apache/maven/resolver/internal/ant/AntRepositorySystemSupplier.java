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
package org.apache.maven.resolver.internal.ant;

import org.apache.maven.model.building.ModelBuilder;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.impl.UpdatePolicyAnalyzer;
import org.eclipse.aether.spi.connector.checksum.ChecksumPolicyProvider;
import org.eclipse.aether.supplier.RepositorySystemSupplier;

/**
 * The Ant modified supplier, that on repository system creation "remembers" (and exposes) other required components as well.
 * <p>
 * This supplier retains references to the {@link ModelBuilder} and {@link RemoteRepositoryManager}
 * instances created during the initialization of the repository system. These components are used
 * later by the Ant infrastructure (e.g., for resolving POMs or remote repositories).
 * </p>
 *
 * @since 1.5.0
 */
public class AntRepositorySystemSupplier extends RepositorySystemSupplier {

    /**
     * The model builder used to construct Maven models from POM files.
     * Initialized during {@link #getModelBuilder()}.
     */
    ModelBuilder modelBuilder;

    /**
     * The remote repository manager used for managing mirrors, proxies, and authentication
     * for remote repositories. Initialized during {@link #getRemoteRepositoryManager(UpdatePolicyAnalyzer, ChecksumPolicyProvider)}.
     */
    RemoteRepositoryManager remoteRepositoryManager;

    /**
     * Creates a new instance of {@code AntRepositorySystemSupplier}.
     */
    public AntRepositorySystemSupplier() {
        // Default constructor
    }

    /**
     * Returns the {@link ModelBuilder} and stores it in the {@link #modelBuilder} field for later access.
     *
     * @return the {@link ModelBuilder} used for building effective models from POMs
     */
    @Override
    protected ModelBuilder getModelBuilder() {
        modelBuilder = super.getModelBuilder();
        return modelBuilder;
    }

    /**
     * Returns the {@link RemoteRepositoryManager} and stores it in the {@link #remoteRepositoryManager} field for later access.
     *
     * @param updatePolicyAnalyzer the analyzer for update policies
     * @param checksumPolicyProvider the provider for checksum policies
     * @return the {@link RemoteRepositoryManager} used for handling repository-specific configurations
     */
    @Override
    protected RemoteRepositoryManager getRemoteRepositoryManager(
            UpdatePolicyAnalyzer updatePolicyAnalyzer, ChecksumPolicyProvider checksumPolicyProvider) {
        remoteRepositoryManager = super.getRemoteRepositoryManager(updatePolicyAnalyzer, checksumPolicyProvider);
        return remoteRepositoryManager;
    }
}
