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

import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.ModelBuilder;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.impl.UpdatePolicyAnalyzer;
import org.eclipse.aether.spi.connector.checksum.ChecksumPolicyProvider;
import org.eclipse.aether.supplier.RepositorySystemSupplier;

/**
 * The Ant modified supplier.
 *
 * @since 1.5.0
 */
public class AntRepositorySystemSupplier extends RepositorySystemSupplier {

    ModelBuilder modelBuilder = new DefaultModelBuilderFactory().newInstance();

    RemoteRepositoryManager remoteRepositoryManager;

    @Override
    protected ModelBuilder getModelBuilder() {
        modelBuilder = super.getModelBuilder();
        return modelBuilder;
    }

    @Override
    protected RemoteRepositoryManager getRemoteRepositoryManager(
            UpdatePolicyAnalyzer updatePolicyAnalyzer, ChecksumPolicyProvider checksumPolicyProvider) {
        remoteRepositoryManager = super.getRemoteRepositoryManager(updatePolicyAnalyzer, checksumPolicyProvider);
        return remoteRepositoryManager;
    }
}
