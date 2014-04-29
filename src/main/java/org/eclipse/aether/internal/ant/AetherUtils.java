/*******************************************************************************
 * Copyright (c) 2010, 2014 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.aether.internal.ant;

import java.io.File;

import org.apache.tools.ant.Project;
import org.eclipse.aether.internal.ant.types.RemoteRepositories;

class AetherUtils
{

    public static File findGlobalSettings( Project project )
    {
        File file = new File( new File( project.getProperty( "ant.home" ), "etc" ), Names.SETTINGS_XML );
        if ( file.isFile() )
        {
            return file;
        }
        else
        {
            String mavenHome = getMavenHome( project );
            if ( mavenHome != null )
            {
                return new File( new File( mavenHome, "conf" ), Names.SETTINGS_XML );
            }
        }
    
        return null;
    }

    public static String getMavenHome( Project project )
    {
        String mavenHome = project.getProperty( "maven.home" );
        if ( mavenHome != null )
        {
            return mavenHome;
        }
        return System.getenv( "M2_HOME" );
    }

    public static File findUserSettings( Project project )
    {
        File userHome = new File( project.getProperty( "user.home" ) );
        File file = new File( new File( userHome, ".ant" ), Names.SETTINGS_XML );
        if ( file.isFile() )
        {
            return file;
        }
        else
        {
            return new File( new File( userHome, ".m2" ), Names.SETTINGS_XML );
        }
    }

    public static RemoteRepositories getDefaultRepositories( Project project )
    {
        Object obj = project.getReference( Names.ID_DEFAULT_REPOS );
        if ( obj instanceof RemoteRepositories )
        {
            return (RemoteRepositories) obj;
        }
        return null;
    }

}
