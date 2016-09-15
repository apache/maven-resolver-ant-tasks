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

import static org.junit.Assert.assertEquals;

import org.apache.maven.resolver.internal.ant.tasks.Layout;
import org.apache.tools.ant.BuildException;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.junit.Test;

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
