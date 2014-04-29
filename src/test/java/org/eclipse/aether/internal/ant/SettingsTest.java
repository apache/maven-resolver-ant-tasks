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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;

import org.eclipse.aether.internal.ant.AntRepoSys;

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
