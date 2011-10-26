/*******************************************************************************
 * Copyright (c) 2010, 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.aether.ant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.eclipse.aether.ant.types.Authentication;
import org.eclipse.aether.ant.types.Dependency;
import org.eclipse.aether.ant.types.Exclusion;
import org.eclipse.aether.ant.types.Proxy;
import org.eclipse.aether.ant.types.RemoteRepositories;
import org.eclipse.aether.ant.types.RemoteRepository;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.ArtifactType;
import org.eclipse.aether.artifact.ArtifactTypeRegistry;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.util.artifact.ArtifactProperties;
import org.eclipse.aether.util.artifact.DefaultArtifact;
import org.eclipse.aether.util.artifact.DefaultArtifactType;

public class ConverterUtils
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

    static org.eclipse.aether.repository.Authentication toAuthentication( Authentication auth )
    {
        if ( auth == null )
        {
            return null;
        }
        return new org.eclipse.aether.repository.Authentication( auth.getUsername(), auth.getPassword(),
                                                                  auth.getPrivateKeyFile(), auth.getPassphrase() );
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
        result.setAuthentication( session.getAuthenticationSelector().getAuthentication( result ) );
        result.setProxy( session.getProxySelector().getProxy( result ) );
        return result;
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
    static Properties addProperties( Properties props, Map<?, ?> map )
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

    static org.eclipse.aether.repository.Proxy toProxy( Proxy proxy )
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
        org.eclipse.aether.repository.RemoteRepository result = new org.eclipse.aether.repository.RemoteRepository();
        result.setId( repo.getId() );
        result.setContentType( repo.getType() );
        result.setUrl( repo.getUrl() );
        result.setPolicy( true,
                          toPolicy( repo.getSnapshotPolicy(), repo.isSnapshots(), repo.getUpdates(),
                                    repo.getChecksums() ) );
        result.setPolicy( false,
                          toPolicy( repo.getReleasePolicy(), repo.isReleases(), repo.getUpdates(), repo.getChecksums() ) );
        result.setAuthentication( toAuthentication( repo.getAuthentication() ) );
        return result;
    }

    static List<org.eclipse.aether.repository.RemoteRepository> toRepositories( Project project,
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
                                                      Collections.<org.eclipse.aether.repository.RemoteRepository> emptyList(),
                                                      results, true );

        return results;
    }

}
