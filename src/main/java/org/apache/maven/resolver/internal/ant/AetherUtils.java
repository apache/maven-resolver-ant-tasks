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

import java.io.File;

import org.apache.maven.resolver.internal.ant.types.RemoteRepositories;
import org.apache.tools.ant.Project;

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
