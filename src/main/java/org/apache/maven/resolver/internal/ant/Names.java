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

/**
 * Centralized constants used throughout the Maven Resolver Ant tasks.
 * <p>
 * This class defines symbolic names for commonly used IDs, property keys,
 * and filenames used in the integration with Ant, such as default repository identifiers,
 * offline mode flags, and POM registration keys.
 * </p>
 *
 * <p>
 * This class is not intended to be instantiated.
 * </p>
 */
public final class Names {

    private Names() {
        // hide constructor
    }

    /** ID namespace prefix used by the resolver: {@code "resolver"}. */
    public static final String ID = "resolver";

    /** Default ID for the shared repository list: {@code "resolver.repositories"}. */
    public static final String ID_DEFAULT_REPOS = ID + ".repositories";

    /** Default ID for the shared POM reference: {@code "resolver.pom"}. */
    public static final String ID_DEFAULT_POM = ID + ".pom";

    /** Canonical ID for Maven Central repository: {@code "central"}. */
    public static final String ID_CENTRAL = "central";

    /** Property name for controlling offline mode: {@code "resolver.offline"}. */
    public static final String PROPERTY_OFFLINE = ID + ".offline";

    /** Default filename for the Maven settings file: {@code "settings.xml"}. */
    public static final String SETTINGS_XML = "settings.xml";
}
