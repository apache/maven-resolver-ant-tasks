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

import org.apache.maven.resolver.internal.ant.types.Exclusion;
import org.junit.Test;

/**
 */
public class ExclusionTest
{

    @Test
    public void testSetCoordsGid()
    {
        Exclusion ex = new Exclusion();
        ex.setCoords( "gid" );

        assertEquals( "gid", ex.getGroupId() );
        assertEquals( "*", ex.getArtifactId() );
        assertEquals( "*", ex.getExtension() );
        assertEquals( "*", ex.getClassifier() );
    }

    @Test
    public void testSetCoordsGidAid()
    {
        Exclusion ex = new Exclusion();
        ex.setCoords( "gid:aid" );

        assertEquals( "gid", ex.getGroupId() );
        assertEquals( "aid", ex.getArtifactId() );
        assertEquals( "*", ex.getExtension() );
        assertEquals( "*", ex.getClassifier() );
    }

    @Test
    public void testSetCoordsGidAidExt()
    {
        Exclusion ex = new Exclusion();
        ex.setCoords( "gid:aid:ext" );

        assertEquals( "gid", ex.getGroupId() );
        assertEquals( "aid", ex.getArtifactId() );
        assertEquals( "ext", ex.getExtension() );
        assertEquals( "*", ex.getClassifier() );
    }

    @Test
    public void testSetCoordsGidAidExtCls()
    {
        Exclusion ex = new Exclusion();
        ex.setCoords( "gid:aid:ext:cls" );

        assertEquals( "gid", ex.getGroupId() );
        assertEquals( "aid", ex.getArtifactId() );
        assertEquals( "ext", ex.getExtension() );
        assertEquals( "cls", ex.getClassifier() );

        ex = new Exclusion();
        ex.setCoords( "gid:aid:ext:" );

        assertEquals( "gid", ex.getGroupId() );
        assertEquals( "aid", ex.getArtifactId() );
        assertEquals( "ext", ex.getExtension() );
        assertEquals( "", ex.getClassifier() );
    }

}
