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
package org.apache.maven.resolver.internal.ant.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.eclipse.aether.artifact.Artifact;

/**
 * Represents the layout type for a Maven repository.
 * <p>
 * This is an Ant {@code DataType} used to configure the repository layout in tasks such as
 * {@code <repository>}. It determines how artifact coordinates are translated into paths
 * when accessing the repository.
 * </p>
 *
 * <h2>Supported Layouts:</h2>
 * <ul>
 *   <li><strong>default</strong> – the standard Maven 2+ repository layout (recommended)</li>
 * </ul>
 * Since default is currently the default layout (and no other types are currently supported), it is safe to omit this.
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * <repositories>
 *   <repository id="central" url="https://repo.maven.apache.org/maven2">
 *      <layout>default</layout>
 *   </repository>
 * </repositories>
 * }</pre>
 *
 * <p>
 * This type is typically used as a nested element inside {@code <repository>} definitions.
 * </p>
 *
 * @see org.apache.maven.resolver.internal.ant.types.RemoteRepository
 */
class Layout {

    public static final String GID = "{groupId}";

    public static final String GID_DIRS = "{groupIdDirs}";

    public static final String AID = "{artifactId}";

    public static final String VER = "{version}";

    public static final String BVER = "{baseVersion}";

    public static final String EXT = "{extension}";

    public static final String CLS = "{classifier}";

    private final String[] tokens;

    Layout(String layout) throws BuildException {
        Collection<String> valid = new HashSet<>(Arrays.asList(GID, GID_DIRS, AID, VER, BVER, EXT, CLS));
        List<String> tokens = new ArrayList<>();
        Matcher m = Pattern.compile("(\\{[^}]*\\})|([^{]+)").matcher(layout);
        while (m.find()) {
            if (m.group(1) != null && !valid.contains(m.group(1))) {
                throw new BuildException("Invalid variable '" + m.group() + "' in layout, supported variables are "
                        + new TreeSet<String>(valid));
            }
            tokens.add(m.group());
        }
        this.tokens = tokens.toArray(new String[0]);
    }

    public String getPath(Artifact artifact) {
        StringBuilder buffer = new StringBuilder(128);

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (GID.equals(token)) {
                buffer.append(artifact.getGroupId());
            } else if (GID_DIRS.equals(token)) {
                buffer.append(artifact.getGroupId().replace('.', '/'));
            } else if (AID.equals(token)) {
                buffer.append(artifact.getArtifactId());
            } else if (VER.equals(token)) {
                buffer.append(artifact.getVersion());
            } else if (BVER.equals(token)) {
                buffer.append(artifact.getBaseVersion());
            } else if (CLS.equals(token)) {
                if (artifact.getClassifier().length() <= 0) {
                    if (i > 0) {
                        String lt = tokens[i - 1];
                        if (!lt.isEmpty() && "-_".indexOf(lt.charAt(lt.length() - 1)) >= 0) {
                            buffer.setLength(buffer.length() - 1);
                        }
                    }
                } else {
                    buffer.append(artifact.getClassifier());
                }
            } else if (EXT.equals(token)) {
                buffer.append(artifact.getExtension());
            } else {
                buffer.append(token);
            }
        }

        return buffer.toString();
    }
}
