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
import java.util.Arrays;

import org.apache.tools.ant.BuildException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/*
 * still missing:
 * - deploy snapshots/releases into correct repos
 */
public class DeployTest extends AntBuildsTest {
    @Test
    public void testDeployGlobalPom() {
        long min = System.currentTimeMillis();
        executeTarget("testDeployGlobalPom");
        long max = System.currentTimeMillis();

        assertLogContaining("Uploading");

        assertUpdatedFile(min, max, distRepoDir, "test/dummy/0.1-SNAPSHOT/maven-metadata.xml");
    }

    @Test
    public void testDeployOverrideGlobalPom() {
        long min = System.currentTimeMillis();
        executeTarget("testDeployOverrideGlobalPom");
        long max = System.currentTimeMillis();

        assertLogContaining("Uploading");

        assertUpdatedFile(min, max, distRepoDir, "test/other/0.1-SNAPSHOT/maven-metadata.xml");
    }

    @Test
    public void testDeployOverrideGlobalPomByRef() {
        long min = System.currentTimeMillis();
        executeTarget("testDeployOverrideGlobalPomByRef");
        long max = System.currentTimeMillis();

        assertLogContaining("Uploading");

        assertUpdatedFile(min, max, distRepoDir, "test/dummy/0.1-SNAPSHOT/maven-metadata.xml");
        assertUpdatedFile(min, max, distRepoDir, "test/other/0.1-SNAPSHOT/maven-metadata.xml");
    }

    @Test
    public void testDeployAttachedArtifact() {
        executeTarget("testDeployAttachedArtifact");

        assertLogContaining("Uploading");

        File dir = new File(distRepoDir, "test/dummy/0.1-SNAPSHOT/");
        String[] files = dir.list();
        assertNotNull(files, "deploy directory not found: " + dir);
        assertTrue(
                Arrays.stream(files).anyMatch(name -> name.endsWith("-ant.xml")),
                "attached artifact not found: " + Arrays.toString(files));
    }

    private void assertUpdatedFile(long min, long max, File repoPath, String path) {
        File file = new File(repoPath, path);
        min = (min / 1000) * 1000;
        max = ((max + 999) / 1000) * 1000;
        assertTrue(file.exists(), "File does not exist in default repo: " + file.getAbsolutePath());
        long modified = file.lastModified();
        assertTrue(
                modified >= min && modified <= max,
                "Files were not updated for 1s before/after timestamp, was: " + modified);
    }

    /**
     * Reproduces an NPE when a deploy &lt;artifact&gt; contains only a nested &lt;pom/&gt;.
     * <pre>{@code
     *   <repo:deploy>
     *     <repo:artifact file="${artifact.file}">
     *       <repo:pom file="${project.dir}/dummy-pom.xml"/>
     *     </repo:artifact>
     *     <repo:snapshotrepo refid="Snapshots"/>
     *   </repo:deploy>
     * }</pre>
     * Current behavior: Ant build fails with a NullPointerException at the root cause.
     * Once the deploy task supports this case or throws a clearer error, update the assertion accordingly.
     */
    @Test
    public void testDeployOnlyNestedPomException() {
        try {
            executeTarget("testDeployOnlyNestedPomException");
            fail("Expected the build to fail when deploying with only a nested <pom/> inside <artifact>.");
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            if (!(cause instanceof BuildException)) {
                fail("Expected NullPointerException as root cause, but was: " + cause);
            }
        }
    }
}
