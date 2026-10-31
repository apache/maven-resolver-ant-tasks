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

import org.apache.tools.ant.BuildFileRule;

/**
 * Ant's {@link BuildFileRule} for JUnit 5 tests. The rule is a JUnit 4 {@code ExternalResource} whose cleanup is
 * protected, so this subclass exposes it to be called from an {@code @AfterEach} method.
 */
public class AntBuildFile extends BuildFileRule {

    /**
     * Restores the standard streams the rule replaced while a build file was configured and executed.
     */
    public void release() {
        after();
    }
}
