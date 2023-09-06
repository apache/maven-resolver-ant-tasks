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
 */
public class Settings extends DataType {

    private File file;

    private File globalFile;

    protected Settings getRef() {
        return (Settings) getCheckedRef();
    }

    public void setRefid(Reference ref) {
        if (file != null || globalFile != null) {
            throw tooManyAttributes();
        }
        super.setRefid(ref);
    }

    public File getFile() {
        if (isReference()) {
            return getRef().getFile();
        }
        return file;
    }

    public void setFile(File file) {
        checkAttributesAllowed();
        this.file = file;

        AntRepoSys.getInstance(getProject()).setUserSettings(file);
    }

    public File getGlobalFile() {
        if (isReference()) {
            return getRef().getFile();
        }
        return globalFile;
    }

    public void setGlobalFile(File globalFile) {
        checkAttributesAllowed();
        this.globalFile = globalFile;

        AntRepoSys.getInstance(getProject()).setGlobalSettings(globalFile);
    }
}
