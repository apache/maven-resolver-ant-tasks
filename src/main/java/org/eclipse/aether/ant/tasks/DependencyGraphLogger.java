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
package org.eclipse.aether.ant.tasks;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;

/**
 */
class DependencyGraphLogger
    implements DependencyVisitor
{

    private Task task;

    private String indent = "";

    public DependencyGraphLogger( Task task )
    {
        this.task = task;
    }

    public boolean visitEnter( DependencyNode node )
    {
        StringBuilder buffer = new StringBuilder( 128 );
        buffer.append( indent );
        Dependency dep = node.getDependency();
        if ( dep != null )
        {
            Artifact art = dep.getArtifact();

            buffer.append( art );
            buffer.append( ':' ).append( dep.getScope() );

            if ( node.getPremanagedScope() != null && !node.getPremanagedScope().equals( dep.getScope() ) )
            {
                buffer.append( " (scope managed from " ).append( node.getPremanagedScope() ).append( ")" );
            }

            if ( node.getPremanagedVersion() != null && !node.getPremanagedVersion().equals( art.getVersion() ) )
            {
                buffer.append( " (version managed from " ).append( node.getPremanagedVersion() ).append( ")" );
            }
        }
        else
        {
            buffer.append( "Resolved Dependency Graph:" );
        }

        task.log( buffer.toString(), Project.MSG_VERBOSE );
        indent += "   ";
        return true;
    }

    public boolean visitLeave( DependencyNode node )
    {
        indent = indent.substring( 0, indent.length() - 3 );
        return true;
    }

}
