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

import org.apache.tools.ant.Project;
import org.eclipse.aether.spi.log.Logger;

/**
 */
class AntLogger
    implements Logger
{

    private Project project;

    public AntLogger( Project project )
    {
        this.project = project;
    }

    public void debug( String msg )
    {
        project.log( msg, Project.MSG_DEBUG );
    }

    public void debug( String msg, Throwable error )
    {
        project.log( msg, error, Project.MSG_DEBUG );
    }

    public boolean isDebugEnabled()
    {
        return true;
    }

    public boolean isWarnEnabled()
    {
        return true;
    }

    public void warn( String msg )
    {
        project.log( msg, Project.MSG_WARN );
    }

    public void warn( String msg, Throwable error )
    {
        project.log( msg, error, Project.MSG_WARN );
    }

}
