package org.apache.maven.resolver.internal.ant.types;

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

import static org.junit.Assert.*;

import org.apache.maven.resolver.internal.ant.types.Dependency;
import org.junit.Test;

/**
 */
public class DependencyTest
{

    @Test
    public void testSetCoordsGidAidVer()
    {
        Dependency dep = new Dependency();
        dep.setCoords( "gid:aid:ver" );

        assertEquals( "gid", dep.getGroupId() );
        assertEquals( "aid", dep.getArtifactId() );
        assertEquals( "ver", dep.getVersion() );
        assertEquals( "jar", dep.getType() );
        assertEquals( "", dep.getClassifier() );
        assertEquals( "compile", dep.getScope() );
    }

    @Test
    public void testSetCoordsGidAidVerScope()
    {
        Dependency dep = new Dependency();
        dep.setCoords( "gid:aid:ver:scope" );

        assertEquals( "gid", dep.getGroupId() );
        assertEquals( "aid", dep.getArtifactId() );
        assertEquals( "ver", dep.getVersion() );
        assertEquals( "jar", dep.getType() );
        assertEquals( "", dep.getClassifier() );
        assertEquals( "scope", dep.getScope() );
    }

    @Test
    public void testSetCoordsGidAidVerTypeScope()
    {
        Dependency dep = new Dependency();
        dep.setCoords( "gid:aid:ver:type:scope" );

        assertEquals( "gid", dep.getGroupId() );
        assertEquals( "aid", dep.getArtifactId() );
        assertEquals( "ver", dep.getVersion() );
        assertEquals( "type", dep.getType() );
        assertEquals( "", dep.getClassifier() );
        assertEquals( "scope", dep.getScope() );
    }

    @Test
    public void testSetCoordsGidAidVerTypeClsScope()
    {
        Dependency dep = new Dependency();
        dep.setCoords( "gid:aid:ver:type:cls:scope" );

        assertEquals( "gid", dep.getGroupId() );
        assertEquals( "aid", dep.getArtifactId() );
        assertEquals( "ver", dep.getVersion() );
        assertEquals( "type", dep.getType() );
        assertEquals( "cls", dep.getClassifier() );
        assertEquals( "scope", dep.getScope() );
    }

}
