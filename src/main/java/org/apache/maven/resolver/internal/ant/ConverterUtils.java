package org.apache.maven.resolver.internal.ant;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.resolver.internal.ant.types.Authentication;
import org.apache.maven.resolver.internal.ant.types.Dependency;
import org.apache.maven.resolver.internal.ant.types.Exclusion;
import org.apache.maven.resolver.internal.ant.types.Proxy;
import org.apache.maven.resolver.internal.ant.types.RemoteRepositories;
import org.apache.maven.resolver.internal.ant.types.RemoteRepository;
import org.apache.tools.ant.Project;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.artifact.DefaultArtifactType;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.util.repository.AuthenticationBuilder;

/**
 * Utility methods to convert between Aether and Ant objects.
 */
class ConverterUtils
{

    private static org.eclipse.aether.artifact.Artifact toArtifact( Dependency dependency, ArtifactTypeRegistry types )
    {
        ArtifactType type = types.get( dependency.getType() );
        if ( type == null )
        {
            type = new DefaultArtifactType( dependency.getType() );
        }

        Map<String, String> props = null;
        if ( "system".equals( dependency.getScope() ) && dependency.getSystemPath() != null )
        {
            props = Collections.singletonMap( ArtifactProperties.LOCAL_PATH, dependency.getSystemPath().getPath() );
        }

        Artifact artifact =
            new DefaultArtifact( dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), null,
                                 dependency.getVersion(), props, type );

        return artifact;
    }

    public static org.eclipse.aether.repository.Authentication toAuthentication( Authentication auth )
    {
        if ( auth == null )
        {
            return null;
        }
        AuthenticationBuilder authBuilder = new AuthenticationBuilder();
        authBuilder.addUsername( auth.getUsername() ).addPassword( auth.getPassword() );
        authBuilder.addPrivateKey( auth.getPrivateKeyFile(), auth.getPassphrase() );
        return authBuilder.build();
    }

    public static org.eclipse.aether.graph.Dependency toDependency( Dependency dependency, List<Exclusion> exclusions,
                                                                     RepositorySystemSession session )
    {
        return new org.eclipse.aether.graph.Dependency( toArtifact( dependency, session.getArtifactTypeRegistry() ),
                                                         dependency.getScope(), false,
                                                         toExclusions( dependency.getExclusions(), exclusions ) );
    }

    /**
     * Converts the given ant repository type to an Aether repository instance with authentication and proxy filled in
     * via the sessions' selectors.
     */
    public static org.eclipse.aether.repository.RemoteRepository toDistRepository( RemoteRepository repo,
                                                                       RepositorySystemSession session )
    {
        org.eclipse.aether.repository.RemoteRepository result = toRepository( repo );
        org.eclipse.aether.repository.RemoteRepository.Builder builder =
            new org.eclipse.aether.repository.RemoteRepository.Builder( result );
        builder.setAuthentication( session.getAuthenticationSelector().getAuthentication( result ) );
        builder.setProxy( session.getProxySelector().getProxy( result ) );
        return builder.build();
    }

    private static org.eclipse.aether.graph.Exclusion toExclusion( Exclusion exclusion )
    {
        return new org.eclipse.aether.graph.Exclusion( exclusion.getGroupId(), exclusion.getArtifactId(),
                                                        exclusion.getClassifier(), exclusion.getExtension() );
    }

    private static Collection<org.eclipse.aether.graph.Exclusion> toExclusions( Collection<Exclusion> exclusions1,
                                                                                 Collection<Exclusion> exclusions2 )
    {
        Collection<org.eclipse.aether.graph.Exclusion> results =
            new LinkedHashSet<org.eclipse.aether.graph.Exclusion>();
        if ( exclusions1 != null )
        {
            for ( Exclusion exclusion : exclusions1 )
            {
                results.add( toExclusion( exclusion ) );
            }
        }
        if ( exclusions2 != null )
        {
            for ( Exclusion exclusion : exclusions2 )
            {
                results.add( toExclusion( exclusion ) );
            }
        }
        return results;
    }

    private static RepositoryPolicy toPolicy( RemoteRepository.Policy policy, boolean enabled, String updates,
                                              String checksums )
    {
        if ( policy != null )
        {
            enabled = policy.isEnabled();
            if ( policy.getChecksums() != null )
            {
                checksums = policy.getChecksums();
            }
            if ( policy.getUpdates() != null )
            {
                updates = policy.getUpdates();
            }
        }
        return new RepositoryPolicy( enabled, updates, checksums );
    }

    /**
     * Adds every &lt;String, String>-entry in the map as a property to the given Properties.
     */
    public static Properties addProperties( Properties props, Map<?, ?> map )
    {
        if ( props == null )
        {
            props = new Properties();
        }
        for ( Map.Entry<?, ?> entry : map.entrySet() )
        {
            if ( entry.getKey() instanceof String && entry.getValue() instanceof String )
            {
                props.put( entry.getKey(), entry.getValue() );
            }
        }
        return props;
    }

    public static org.eclipse.aether.repository.Proxy toProxy( Proxy proxy )
    {
        if ( proxy == null )
        {
            return null;
        }
        return new org.eclipse.aether.repository.Proxy( proxy.getType(), proxy.getHost(), proxy.getPort(),
                                                         toAuthentication( proxy.getAuthentication() ) );
    }

    private static org.eclipse.aether.repository.RemoteRepository toRepository( RemoteRepository repo )
    {
        org.eclipse.aether.repository.RemoteRepository.Builder builder =
            new org.eclipse.aether.repository.RemoteRepository.Builder( repo.getId(), repo.getType(), repo.getUrl() );
        builder.setSnapshotPolicy( toPolicy( repo.getSnapshotPolicy(), repo.isSnapshots(), repo.getUpdates(),
                                             repo.getChecksums() ) );
        builder.setReleasePolicy( toPolicy( repo.getReleasePolicy(), repo.isReleases(), repo.getUpdates(),
                                            repo.getChecksums() ) );
        builder.setAuthentication( toAuthentication( repo.getAuthentication() ) );
        return builder.build();
    }

    public static List<org.eclipse.aether.repository.RemoteRepository> toRepositories( Project project,
                                                                          RepositorySystemSession session,
                                                                          RemoteRepositories repos, RemoteRepositoryManager remoteRepositoryManager )
    {
        List<RemoteRepository> repositories;

        if ( repos != null )
        {
            repositories = repos.getRepositories();
        }
        else
        {
            repositories = new ArrayList<RemoteRepository>();
        }

        List<org.eclipse.aether.repository.RemoteRepository> results =
            new ArrayList<org.eclipse.aether.repository.RemoteRepository>();
        for ( RemoteRepository repo : repositories )
        {
            results.add( toRepository( repo ) );
        }

        results =
            remoteRepositoryManager.aggregateRepositories( session,
                                                      Collections.<org.eclipse.aether.repository.RemoteRepository>emptyList(),
                                                      results, true );

        return results;
    }

}
