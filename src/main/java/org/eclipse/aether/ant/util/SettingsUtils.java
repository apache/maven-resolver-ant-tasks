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
package org.eclipse.aether.ant.util;

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
 * Uitility methods to read settings from mavens settings.xml.
 */
public class SettingsUtils
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
