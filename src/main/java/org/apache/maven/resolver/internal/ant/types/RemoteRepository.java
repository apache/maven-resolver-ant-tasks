package org.apache.maven.resolver.internal.ant.types;

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
 */
public class RemoteRepository
    extends DataType
    implements RemoteRepositoryContainer
{

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

    @Override
    public void setProject( Project project )
    {
        super.setProject( project );

        // NOTE: Just trigger side-effect of default initialization before this type potentially overrides central
        AntRepoSys.getInstance( project );
    }

    protected RemoteRepository getRef()
    {
        return (RemoteRepository) getCheckedRef();
    }

    public void validate( Task task )
    {
        if ( isReference() )
        {
            getRef().validate( task );
        }
        else
        {
            if ( url == null || url.length() <= 0 )
            {
                throw new BuildException( "You must specify the 'url' for a remote repository" );
            }
            if ( id == null || id.length() <= 0 )
            {
                throw new BuildException( "You must specify the 'id' for a remote repository" );
            }
        }
    }

    public void setRefid( Reference ref )
    {
        if ( id != null || url != null || type != null || checksums != null || updates != null )
        {
            throw tooManyAttributes();
        }
        if ( releasePolicy != null || snapshotPolicy != null || authentication != null )
        {
            throw noChildrenAllowed();
        }
        super.setRefid( ref );
    }

    public String getId()
    {
        if ( isReference() )
        {
            return getRef().getId();
        }
        return id;
    }

    public void setId( String id )
    {
        this.id = id;
    }

    public String getUrl()
    {
        if ( isReference() )
        {
            return getRef().getUrl();
        }
        return url;
    }

    public void setUrl( String url )
    {
        checkAttributesAllowed();
        this.url = url;
    }

    public String getType()
    {
        if ( isReference() )
        {
            return getRef().getType();
        }
        return ( type != null ) ? type : "default";
    }

    public void setType( String type )
    {
        checkAttributesAllowed();
        this.type = type;
    }

    public Policy getReleasePolicy()
    {
        if ( isReference() )
        {
            return getRef().getReleasePolicy();
        }
        return releasePolicy;
    }

    public void addReleases( Policy policy )
    {
        checkChildrenAllowed();
        if ( this.releasePolicy != null )
        {
            throw new BuildException( "You must not specify multiple <releases> elements" );
        }
        this.releasePolicy = policy;
    }

    public Policy getSnapshotPolicy()
    {
        if ( isReference() )
        {
            return getRef().getSnapshotPolicy();
        }
        return snapshotPolicy;
    }

    public void addSnapshots( Policy policy )
    {
        checkChildrenAllowed();
        if ( this.snapshotPolicy != null )
        {
            throw new BuildException( "You must not specify multiple <snapshots> elements" );
        }
        this.snapshotPolicy = policy;
    }

    public boolean isReleases()
    {
        if ( isReference() )
        {
            return getRef().isReleases();
        }
        return releases;
    }

    public void setReleases( boolean releases )
    {
        checkAttributesAllowed();
        this.releases = releases;
    }

    public boolean isSnapshots()
    {
        if ( isReference() )
        {
            return getRef().isSnapshots();
        }
        return snapshots;
    }

    public void setSnapshots( boolean snapshots )
    {
        checkAttributesAllowed();
        this.snapshots = snapshots;
    }

    public String getUpdates()
    {
        if ( isReference() )
        {
            return getRef().getUpdates();
        }
        return ( updates != null ) ? updates : RepositoryPolicy.UPDATE_POLICY_DAILY;
    }

    public void setUpdates( String updates )
    {
        checkAttributesAllowed();
        checkUpdates( updates );
        this.updates = updates;
    }

    protected static void checkUpdates( String updates )
    {
        if ( !RepositoryPolicy.UPDATE_POLICY_ALWAYS.equals( updates )
            && !RepositoryPolicy.UPDATE_POLICY_DAILY.equals( updates )
            && !RepositoryPolicy.UPDATE_POLICY_NEVER.equals( updates )
            && !updates.startsWith( RepositoryPolicy.UPDATE_POLICY_INTERVAL ) )
        {
            throw new BuildException( "'" + updates + "' is not a permitted update policy" );
        }
    }

    public String getChecksums()
    {
        if ( isReference() )
        {
            return getRef().getChecksums();
        }
        return ( checksums != null ) ? checksums : RepositoryPolicy.CHECKSUM_POLICY_WARN;
    }

    public void setChecksums( String checksums )
    {
        checkAttributesAllowed();
        checkChecksums( checksums );
        this.checksums = checksums;
    }

    protected static void checkChecksums( String checksums )
    {
        if ( !RepositoryPolicy.CHECKSUM_POLICY_FAIL.equals( checksums )
            && !RepositoryPolicy.CHECKSUM_POLICY_WARN.equals( checksums )
            && !RepositoryPolicy.CHECKSUM_POLICY_IGNORE.equals( checksums ) )
        {
            throw new BuildException( "'" + checksums + "' is not a permitted checksum policy" );
        }
    }

    public Authentication getAuthentication()
    {
        if ( isReference() )
        {
            return getRef().getAuthentication();
        }
        return authentication;
    }

    public void addAuthentication( Authentication authentication )
    {
        checkChildrenAllowed();
        if ( this.authentication != null )
        {
            throw new BuildException( "You must not specify multiple <authentication> elements" );
        }
        this.authentication = authentication;
    }

    public void setAuthRef( Reference ref )
    {
        checkAttributesAllowed();
        if ( authentication == null )
        {
            authentication = new Authentication();
            authentication.setProject( getProject() );
        }
        authentication.setRefid( ref );
    }

    public List<RemoteRepository> getRepositories()
    {
        return Collections.singletonList( this );
    }

    /**
     */
    public static class Policy
    {

        private boolean enabled = true;

        private String checksumPolicy;

        private String updatePolicy;

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled( boolean enabled )
        {
            this.enabled = enabled;
        }

        public String getChecksums()
        {
            return checksumPolicy;
        }

        public void setChecksums( String checksumPolicy )
        {
            checkChecksums( checksumPolicy );
            this.checksumPolicy = checksumPolicy;
        }

        public String getUpdates()
        {
            return updatePolicy;
        }

        public void setUpdates( String updatePolicy )
        {
            checkUpdates( updatePolicy );
            this.updatePolicy = updatePolicy;
        }

    }

}
