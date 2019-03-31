package org.apache.maven.resolver.internal.ant.tasks;

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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;

/**
 */
class DependencyGraphLogger
    implements DependencyVisitor
{

    private Task task;

    private String indent = "";

    DependencyGraphLogger( Task task )
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

            String premanagedScope = DependencyManagerUtils.getPremanagedScope( node );
            if ( premanagedScope != null && !premanagedScope.equals( dep.getScope() ) )
            {
                buffer.append( " (scope managed from " ).append( premanagedScope ).append( ")" );
            }

            String premanagedVersion = DependencyManagerUtils.getPremanagedVersion( node );
            if ( premanagedVersion != null && !premanagedVersion.equals( art.getVersion() ) )
            {
                buffer.append( " (version managed from " ).append( premanagedVersion ).append( ")" );
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
