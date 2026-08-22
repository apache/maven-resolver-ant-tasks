/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.resolver.internal.ant;

import java.io.File;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettingsTest extends AntBuildsTest {
    @Test
    void userSettings() {
        executeTarget("testUserSettings");
        assertEquals(
                "userSettings.xml",
                AntRepoSys.getInstance(getProject()).getUserSettings().getName(),
                "user settings not set");
    }

    @Test
    void globalSettings() {
        executeTarget("testGlobalSettings");
        assertEquals(
                "globalSettings.xml",
                AntRepoSys.getInstance(getProject()).getGlobalSettings().getName(),
                "global settings not set");
    }

    @Test
    void bothSettings() {
        executeTarget("testBothSettings");
        assertEquals(
                "globalSettings.xml",
                AntRepoSys.getInstance(getProject()).getGlobalSettings().getName(),
                "global settings not set");
        assertEquals(
                "userSettings.xml",
                AntRepoSys.getInstance(getProject()).getUserSettings().getName(),
                "user settings not set");
    }

    @Test
    void fallback() throws Exception {
        executeTarget("setUp");
        String userSettings =
                AntRepoSys.getInstance(getProject()).getUserSettings().getAbsolutePath();
        assertTrue(
                userSettings.endsWith(".m2" + File.separator + "settings.xml"),
                "no fallback to local settings, was: " + userSettings);
    }
}
