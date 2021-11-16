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

import junit.framework.JUnit4TestAdapter;
import org.apache.maven.resolver.internal.ant.types.Pom;
import org.junit.Test;

/**
 */
public class PomTest
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter( PomTest.class );
    }

    @Test
    public void testSetCoordsGid()
    {
        Pom pom = new Pom();
        pom.setCoords( "gid:aid:ver" );

        assertEquals( "gid", pom.getGroupId() );
        assertEquals( "aid", pom.getArtifactId() );
        assertEquals( "ver", pom.getVersion() );
    }

}
