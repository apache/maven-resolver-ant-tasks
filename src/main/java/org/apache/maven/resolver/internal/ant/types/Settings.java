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
package org.apache.maven.resolver.internal.ant.types;

import java.io.File;

import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 * Represents Maven settings for repository resolution in Ant builds.
 * <p>
 * This data type allows configuration of user-specific and global Maven {@code settings.xml} files,
 * typically used to define mirrors, proxies, authentication, and repository policies.
 * </p>
 *
 * <p>
 * It can be defined inline or as a reference using {@code refid}.
 * Only one of each settings file (user or global) may be set unless using a reference.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * <settings file="${user.home}/.m2/settings.xml"/>
 * }</pre>
 *
 * @see <a href="https://maven.apache.org/settings.html">Maven Settings Reference</a>
 */
public class Settings extends DataType {

    private File file;

    private File globalFile;

    /**
     * Default constructor for {@code Settings} data type.
     */
    public Settings() {
        // Default constructor
    }

    /**
     * Resolves this object if defined as a reference and verifies that it is a
     * {@code Settings} instance.
     *
     * @return the referenced {@code Settings} instance
     * @throws org.apache.tools.ant.BuildException if the reference is invalid
     */
    protected Settings getRef() {
        return getCheckedRef(Settings.class);
    }

    /**
     * Marks this instance as a reference to another {@code Settings} definition.
     * <p>
     * When using a reference, no other attributes (e.g., {@code file}, {@code globalFile}) may be set.
     * </p>
     *
     * @param ref the Ant reference to another {@code Settings} element
     * @throws org.apache.tools.ant.BuildException if attributes are already set
     */
    public void setRefid(Reference ref) {
        if (file != null || globalFile != null) {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
    }

    /**
     * Gets the user-level Maven settings file, usually {@code ~/.m2/settings.xml}.
     *
     * @return the user settings file
     */
    public File getFile() {
        if (isReference()) {
            return getRef().getFile();
        }
        return file;
    }

    /**
     * Sets the user-level Maven settings file.
     * <p>
     * This file is typically located at {@code ~/.m2/settings.xml}.
     * </p>
     *
     * @param file the user settings file
     */
    public void setFile(File file) {
        checkAttributesAllowed();
        this.file = file;

        AntRepoSys.getInstance(getProject()).setUserSettings(file);
    }

    /**
     * Gets the global-level Maven settings file, typically defined by the installation.
     *
     * @return the global settings file
     */
    public File getGlobalFile() {
        if (isReference()) {
            return getRef().getFile();
        }
        return globalFile;
    }

    /**
     * Sets the global-level Maven settings file.
     * <p>
     * This file typically resides in the Maven installation directory under {@code conf/settings.xml}.
     * </p>
     *
     * @param globalFile the global settings file
     */
    public void setGlobalFile(File globalFile) {
        checkAttributesAllowed();
        this.globalFile = globalFile;

        AntRepoSys.getInstance(getProject()).setGlobalSettings(globalFile);
    }
}
