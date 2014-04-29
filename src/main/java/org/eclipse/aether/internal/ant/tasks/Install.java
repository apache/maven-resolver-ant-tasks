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
package org.eclipse.aether.internal.ant.tasks;

import org.apache.tools.ant.BuildException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.internal.ant.AntRepoSys;

/**
 */
public class Install
    extends AbstractDistTask
{

    @Override
    public void execute()
        throws BuildException
    {
        validate();

        AntRepoSys sys = AntRepoSys.getInstance( getProject() );

        RepositorySystemSession session = sys.getSession( this, null );
        RepositorySystem system = sys.getSystem();

        InstallRequest request = new InstallRequest();
        request.setArtifacts( toArtifacts( session ) );

        try
        {
            system.install( session, request );
        }
        catch ( InstallationException e )
        {
            throw new BuildException( "Could not install artifacts: " + e.getMessage(), e );
        }
    }

}
