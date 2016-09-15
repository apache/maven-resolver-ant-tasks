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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;

import org.apache.maven.resolver.internal.ant.AntRepoSys;

public class SettingsTest
    extends AntBuildsTest
{

    public void testUserSettings()
    {
        executeTarget( "testUserSettings" );
        assertThat( "user settings not set", AntRepoSys.getInstance( getProject() ).getUserSettings().getName(),
                    equalTo( "userSettings.xml" ) );
    }

    public void testGlobalSettings()
    {
        executeTarget( "testGlobalSettings" );
        assertThat( "global settings not set", AntRepoSys.getInstance( getProject() ).getGlobalSettings().getName(),
                    equalTo( "globalSettings.xml" ) );
    }

    public void testBothSettings()
    {
        executeTarget( "testBothSettings" );
        assertThat( "global settings not set", AntRepoSys.getInstance( getProject() ).getGlobalSettings().getName(),
                    equalTo( "globalSettings.xml" ) );
        assertThat( "user settings not set", AntRepoSys.getInstance( getProject() ).getUserSettings().getName(),
                    equalTo( "userSettings.xml" ) );
    }

    public void testFallback()
        throws IOException
    {
        executeTarget("setUp");
        assertThat( "no fallback to local settings",
                    AntRepoSys.getInstance( getProject() ).getUserSettings().getAbsolutePath(), endsWith( ".m2"
                        + File.separator + "settings.xml" ) );
    }
}
