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

        AntRepoSys.getInstance( getProject() ).install( this, getPom(), getArtifacts() );
    }

}
