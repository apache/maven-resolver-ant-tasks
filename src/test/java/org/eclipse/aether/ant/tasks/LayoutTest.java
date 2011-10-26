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

import static org.junit.Assert.assertEquals;

import org.apache.tools.ant.BuildException;
import org.eclipse.aether.ant.tasks.Layout;
import org.junit.Test;
import org.eclipse.aether.util.artifact.DefaultArtifact;

/**
 */
public class LayoutTest
{

    @Test( expected = BuildException.class )
    public void testUnknownVariable()
    {
        new Layout( "{unknown}" );
    }

    @Test
    public void testGetPath()
    {
        Layout layout;

        layout =
            new Layout( "{groupIdDirs}/{artifactId}/{baseVersion}/{artifactId}-{version}-{classifier}.{extension}" );
        assertEquals( "org/apache/maven/maven-model/3.0-SNAPSHOT/maven-model-3.0-20100720.132618-1.jar",
                      layout.getPath( new DefaultArtifact( "org.apache.maven:maven-model:3.0-20100720.132618-1" ) ) );

        layout = new Layout( "{groupId}/{artifactId}-{version}-{classifier}.{extension}" );
        assertEquals( "org.apache.maven/maven-model-3.0-sources.jar",
                      layout.getPath( new DefaultArtifact( "org.apache.maven:maven-model:jar:sources:3.0" ) ) );
    }

}
