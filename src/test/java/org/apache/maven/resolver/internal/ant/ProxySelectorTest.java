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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.repository.RemoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProxySelectorTest {
    @TempDir
    File folder;

    private Project project;

    private Task task;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setProperty("user.home", System.getProperty("user.home"));
        project.setProperty(
                "maven.repo.local",
                new File(new File("").getAbsoluteFile(), "target/ant/local-repo").getAbsolutePath());

        task = new Task() {};
        task.setProject(project);
    }

    /**
     * A proxy declared in settings.xml with {@code <active>false</active>} must not be applied to transfers. Only the
     * active proxy (the first one with {@code isActive() == true}) must be added to the selector.
     *
     * @throws Exception in case of problems
     */
    @Test
    void inactiveProxyFromSettingsIsNotApplied() throws Exception {
        File settings = writeSettings(
                "<proxy>",
                "  <id>inactive-proxy</id>",
                "  <active>false</active>",
                "  <protocol>http</protocol>",
                "  <host>inactive-proxy.example.com</host>",
                "  <port>8080</port>",
                "</proxy>",
                "<proxy>",
                "  <id>active-proxy</id>",
                "  <active>true</active>",
                "  <protocol>http</protocol>",
                "  <host>active-proxy.example.com</host>",
                "  <port>8081</port>",
                "</proxy>");

        AntRepoSys repoSys = AntRepoSys.getInstance(project);
        repoSys.setUserSettings(settings);

        try (RepositorySystemSession.CloseableSession session = repoSys.getSession(task, null)) {
            Proxy proxy = proxyForCentral(session);
            assertNotNull(proxy);
            assertEquals("active-proxy.example.com", proxy.getHost());
            assertEquals(8081, proxy.getPort());
        }
    }

    /**
     * When settings.xml declares only inactive proxies, no proxy may be applied to transfers.
     *
     * @throws Exception in case of problems
     */
    @Test
    void onlyInactiveProxiesApplyNoProxy() throws Exception {
        File settings = writeSettings(
                "<proxy>",
                "  <id>inactive-proxy</id>",
                "  <active>false</active>",
                "  <protocol>http</protocol>",
                "  <host>inactive-proxy.example.com</host>",
                "  <port>8080</port>",
                "</proxy>");

        AntRepoSys repoSys = AntRepoSys.getInstance(project);
        repoSys.setUserSettings(settings);

        try (RepositorySystemSession.CloseableSession session = repoSys.getSession(task, null)) {
            assertNull(proxyForCentral(session));
        }
    }

    private Proxy proxyForCentral(RepositorySystemSession session) {
        ProxySelector selector = session.getProxySelector();
        RemoteRepository repo =
                new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build();
        return selector.getProxy(repo);
    }

    private File writeSettings(String... proxyElements) throws Exception {
        File file = newFile(folder, "settings.xml");
        StringBuilder content = new StringBuilder();
        content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        content.append("<settings>\n");
        content.append("  <proxies>\n");
        for (String element : proxyElements) {
            content.append("    ").append(element).append('\n');
        }
        content.append("  </proxies>\n");
        content.append("</settings>\n");
        Files.write(file.toPath(), content.toString().getBytes(StandardCharsets.UTF_8));
        return file;
    }

    private static File newFile(File parent, String child) throws IOException {
        File result = new File(parent, child);
        result.createNewFile();
        return result;
    }
}
