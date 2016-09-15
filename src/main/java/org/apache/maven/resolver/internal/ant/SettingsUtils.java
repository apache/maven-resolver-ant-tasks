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
import java.util.List;

import org.apache.maven.settings.Activation;
import org.apache.maven.settings.ActivationFile;
import org.apache.maven.settings.ActivationOS;
import org.apache.maven.settings.ActivationProperty;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryPolicy;

/**
 * Utility methods to read settings from Mavens settings.xml.
 */
class SettingsUtils
{

    public static List<org.apache.maven.model.Profile> convert( List<Profile> profiles )
    {
        if ( profiles == null )
        {
            return null;
        }

        List<org.apache.maven.model.Profile> results = new ArrayList<org.apache.maven.model.Profile>();

        for ( Profile profile : profiles )
        {
            results.add( convert( profile ) );
        }

        return results;
    }

    static org.apache.maven.model.Profile convert( Profile profile )
    {
        if ( profile == null )
        {
            return null;
        }

        org.apache.maven.model.Profile result = new org.apache.maven.model.Profile();

        result.setId( profile.getId() );
        result.setProperties( profile.getProperties() );
        result.setSource( "settings.xml" );
        result.setActivation( convert( profile.getActivation() ) );

        for ( Repository repo : profile.getRepositories() )
        {
            result.addRepository( convert( repo ) );
        }

        for ( Repository repo : profile.getPluginRepositories() )
        {
            result.addPluginRepository( convert( repo ) );
        }

        return result;
    }

    static org.apache.maven.model.Activation convert( Activation activation )
    {
        if ( activation == null )
        {
            return null;
        }

        org.apache.maven.model.Activation result = new org.apache.maven.model.Activation();

        result.setActiveByDefault( activation.isActiveByDefault() );
        result.setJdk( activation.getJdk() );
        result.setFile( convert( activation.getFile() ) );
        result.setProperty( convert( activation.getProperty() ) );
        result.setOs( convert( activation.getOs() ) );

        return result;
    }

    static org.apache.maven.model.ActivationOS convert( ActivationOS activation )
    {
        if ( activation == null )
        {
            return null;
        }

        org.apache.maven.model.ActivationOS result = new org.apache.maven.model.ActivationOS();

        result.setArch( activation.getArch() );
        result.setFamily( activation.getFamily() );
        result.setName( activation.getName() );
        result.setVersion( activation.getVersion() );

        return result;
    }

    static org.apache.maven.model.ActivationProperty convert( ActivationProperty activation )
    {
        if ( activation == null )
        {
            return null;
        }

        org.apache.maven.model.ActivationProperty result = new org.apache.maven.model.ActivationProperty();

        result.setName( activation.getName() );
        result.setValue( activation.getValue() );

        return result;
    }

    static org.apache.maven.model.ActivationFile convert( ActivationFile activation )
    {
        if ( activation == null )
        {
            return null;
        }

        org.apache.maven.model.ActivationFile result = new org.apache.maven.model.ActivationFile();

        result.setExists( activation.getExists() );
        result.setMissing( activation.getMissing() );

        return result;
    }

    static org.apache.maven.model.Repository convert( Repository repo )
    {
        if ( repo == null )
        {
            return null;
        }

        org.apache.maven.model.Repository result = new org.apache.maven.model.Repository();

        result.setId( repo.getId() );
        result.setUrl( repo.getUrl() );
        result.setLayout( repo.getLayout() );
        result.setReleases( convert( repo.getReleases() ) );
        result.setSnapshots( convert( repo.getSnapshots() ) );

        return result;
    }

    static org.apache.maven.model.RepositoryPolicy convert( RepositoryPolicy policy )
    {
        if ( policy == null )
        {
            return null;
        }

        org.apache.maven.model.RepositoryPolicy result = new org.apache.maven.model.RepositoryPolicy();

        result.setEnabled( policy.isEnabled() );
        result.setChecksumPolicy( policy.getChecksumPolicy() );
        result.setUpdatePolicy( policy.getUpdatePolicy() );

        return result;
    }

}
