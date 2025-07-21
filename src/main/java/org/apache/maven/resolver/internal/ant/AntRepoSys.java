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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.resolver.internal.ant.types.Artifact;
import org.apache.maven.resolver.internal.ant.types.Artifacts;
import org.apache.maven.resolver.internal.ant.types.Authentication;
import org.apache.maven.resolver.internal.ant.types.Dependencies;
import org.apache.maven.resolver.internal.ant.types.Dependency;
import org.apache.maven.resolver.internal.ant.types.DependencyContainer;
import org.apache.maven.resolver.internal.ant.types.Exclusion;
import org.apache.maven.resolver.internal.ant.types.LocalRepository;
import org.apache.maven.resolver.internal.ant.types.Mirror;
import org.apache.maven.resolver.internal.ant.types.Pom;
import org.apache.maven.resolver.internal.ant.types.Proxy;
import org.apache.maven.resolver.internal.ant.types.RemoteRepositories;
import org.apache.maven.resolver.internal.ant.types.RemoteRepository;
import org.apache.maven.resolver.internal.ant.types.RemoteRepository.Policy;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.RepositoryPolicy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuilder;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.Reference;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.ConfigurationProperties;
import org.eclipse.aether.DefaultRepositoryCache;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.repository.AuthenticationSelector;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.MirrorSelector;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.repository.ConservativeAuthenticationSelector;
import org.eclipse.aether.util.repository.DefaultAuthenticationSelector;
import org.eclipse.aether.util.repository.DefaultMirrorSelector;
import org.eclipse.aether.util.repository.DefaultProxySelector;

/**
 */
public class AntRepoSys {
    private static final Date STARTED = new Date();

    private static final boolean OS_WINDOWS = Os.isFamily("windows");

    private static final SettingsBuilder SETTINGS_BUILDER = new DefaultSettingsBuilderFactory().newInstance();

    private static final SettingsDecrypter SETTINGS_DECRYPTER = new AntSettingsDecryptorFactory().newInstance();

    private final Project project;

    private final AntRepositorySystemSupplier antRepositorySystemSupplier;

    private final RepositorySystem repoSys;

    private File userSettings;

    private File globalSettings;

    private Settings settings;

    private final List<Mirror> mirrors = new CopyOnWriteArrayList<>();

    private final List<Proxy> proxies = new CopyOnWriteArrayList<>();

    private final List<Authentication> authentications = new CopyOnWriteArrayList<>();

    private LocalRepository localRepository;

    private Pom defaultPom;

    private static <T> boolean eq(T o1, T o2) {
        return Objects.equals(o1, o2);
    }

    public static synchronized AntRepoSys getInstance(Project project) {
        Object obj = project.getReference(Names.ID);
        if (obj instanceof AntRepoSys) {
            return (AntRepoSys) obj;
        }
        AntRepoSys instance = new AntRepoSys(project);
        project.addReference(Names.ID, instance);
        instance.initDefaults();
        return instance;
    }

    private AntRepoSys(Project project) {
        this.project = project;
        this.antRepositorySystemSupplier = new AntRepositorySystemSupplier();
        this.repoSys = antRepositorySystemSupplier.get();
    }

    private void initDefaults() {
        RemoteRepository repo = new RemoteRepository();
        repo.setProject(project);
        repo.setId("central");
        repo.setUrl("https://repo.maven.apache.org/maven2/");
        project.addReference(Names.ID_CENTRAL, repo);

        repo = new RemoteRepository();
        repo.setProject(project);
        repo.setRefid(new Reference(project, Names.ID_CENTRAL));

        RemoteRepositories repos = new RemoteRepositories();
        repos.setProject(project);
        repos.addRemoterepo(repo);
        project.addReference(Names.ID_DEFAULT_REPOS, repos);
    }

    public synchronized RepositorySystem getSystem() {
        return repoSys;
    }

    private synchronized RemoteRepositoryManager getRemoteRepoMan() {
        return antRepositorySystemSupplier.remoteRepositoryManager;
    }

    public RepositorySystemSession getSession(Task task, LocalRepository localRepo) {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        final Map<Object, Object> configProps = new LinkedHashMap<>();
        configProps.put(ConfigurationProperties.USER_AGENT, getUserAgent());
        configProps.put("maven.startTime", STARTED);
        configProps.putAll(getSystemProperties());
        configProps.putAll(getUserProperties());
        processServerConfiguration(configProps);

        session.setConfigProperties(configProps);
        session.setSystemProperties(getSystemProperties());
        session.setUserProperties(getUserProperties());
        session.setOffline(isOffline());

        session.setProxySelector(getProxySelector());
        session.setMirrorSelector(getMirrorSelector());
        session.setAuthenticationSelector(getAuthSelector());

        session.setCache(new DefaultRepositoryCache());

        session.setRepositoryListener(new AntRepositoryListener(task));
        session.setTransferListener(new AntTransferListener(task));

        session.setLocalRepositoryManager(getLocalRepoMan(session, localRepo));

        session.setWorkspaceReader(ProjectWorkspaceReader.getInstance());

        return session;
    }

    private String getUserAgent() {
        return "Apache-Ant/" + project.getProperty("ant.version")
                + " ("
                + "Java " + System.getProperty("java.version")
                + "; "
                + System.getProperty("os.name") + " " + System.getProperty("os.version")
                + ")"
                + " Aether";
    }

    private boolean isOffline() {
        String prop = project.getProperty(Names.PROPERTY_OFFLINE);
        if (prop != null) {
            return Boolean.parseBoolean(prop);
        }
        return getSettings().isOffline();
    }

    private void processServerConfiguration(Map<Object, Object> configProps) {
        Settings settings = getSettings();
        for (Server server : settings.getServers()) {
            if (server.getConfiguration() != null) {
                Xpp3Dom dom = (Xpp3Dom) server.getConfiguration();
                for (int i = dom.getChildCount() - 1; i >= 0; i--) {
                    Xpp3Dom child = dom.getChild(i);
                    if ("wagonProvider".equals(child.getName())) {
                        dom.removeChild(i);
                    } else if ("httpHeaders".equals(child.getName())) {
                        configProps.put(
                                ConfigurationProperties.HTTP_HEADERS + "." + server.getId(), getHttpHeaders(child));
                    }
                }

                configProps.put("aether.connector.wagon.config." + server.getId(), dom);
            }

            configProps.put("aether.connector.perms.fileMode." + server.getId(), server.getFilePermissions());
            configProps.put("aether.connector.perms.dirMode." + server.getId(), server.getDirectoryPermissions());
        }
    }

    private Map<String, String> getHttpHeaders(Xpp3Dom dom) {
        final Map<String, String> headers = new HashMap<>();
        for (int i = 0; i < dom.getChildCount(); i++) {
            Xpp3Dom child = dom.getChild(i);
            Xpp3Dom name = child.getChild("name");
            Xpp3Dom value = child.getChild("value");
            if (name != null && name.getValue() != null) {
                headers.put(name.getValue(), (value != null) ? value.getValue() : null);
            }
        }
        return Collections.unmodifiableMap(headers);
    }

    private File getDefaultLocalRepoDir() {
        String dir = project.getProperty("maven.repo.local");
        if (dir != null) {
            return project.resolveFile(dir);
        }

        Settings settings = getSettings();
        if (settings.getLocalRepository() != null) {
            return new File(settings.getLocalRepository());
        }

        return new File(new File(project.getProperty("user.home"), ".m2"), "repository");
    }

    private LocalRepositoryManager getLocalRepoMan(RepositorySystemSession session, LocalRepository localRepo) {
        if (localRepo == null) {
            localRepo = localRepository;
        }

        File repoDir;
        if (localRepo != null && localRepo.getDir() != null) {
            repoDir = localRepo.getDir();
        } else {
            repoDir = getDefaultLocalRepoDir();
        }

        org.eclipse.aether.repository.LocalRepository repo = new org.eclipse.aether.repository.LocalRepository(repoDir);

        return getSystem().newLocalRepositoryManager(session, repo);
    }

    private synchronized Settings getSettings() {
        if (settings == null) {
            DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
            request.setUserSettingsFile(getUserSettings());
            request.setGlobalSettingsFile(getGlobalSettings());
            request.setSystemProperties(getSystemProperties());
            request.setUserProperties(getUserProperties());

            try {
                settings = SETTINGS_BUILDER.build(request).getEffectiveSettings();
            } catch (SettingsBuildingException e) {
                project.log("Could not process settings.xml: " + e.getMessage(), e, Project.MSG_WARN);
            }

            SettingsDecryptionResult result =
                    SETTINGS_DECRYPTER.decrypt(new DefaultSettingsDecryptionRequest(settings));
            settings.setServers(result.getServers());
            settings.setProxies(result.getProxies());
        }
        return settings;
    }

    private ProxySelector getProxySelector() {
        DefaultProxySelector selector = new DefaultProxySelector();

        for (Proxy proxy : proxies) {
            selector.add(ConverterUtils.toProxy(proxy), proxy.getNonProxyHosts());
        }

        Settings settings = getSettings();
        for (org.apache.maven.settings.Proxy proxy : settings.getProxies()) {
            AuthenticationBuilder auth = new AuthenticationBuilder();
            auth.addUsername(proxy.getUsername()).addPassword(proxy.getPassword());
            selector.add(
                    new org.eclipse.aether.repository.Proxy(
                            proxy.getProtocol(), proxy.getHost(),
                            proxy.getPort(), auth.build()),
                    proxy.getNonProxyHosts());
        }

        return selector;
    }

    private MirrorSelector getMirrorSelector() {
        DefaultMirrorSelector selector = new DefaultMirrorSelector();

        for (Mirror mirror : mirrors) {
            selector.add(mirror.getId(), mirror.getUrl(), mirror.getType(), false, false, mirror.getMirrorOf(), null);
        }

        Settings settings = getSettings();
        for (org.apache.maven.settings.Mirror mirror : settings.getMirrors()) {
            selector.add(
                    String.valueOf(mirror.getId()),
                    mirror.getUrl(),
                    mirror.getLayout(),
                    false,
                    false,
                    mirror.getMirrorOf(),
                    mirror.getMirrorOfLayouts());
        }

        return selector;
    }

    private AuthenticationSelector getAuthSelector() {
        DefaultAuthenticationSelector selector = new DefaultAuthenticationSelector();

        final Collection<String> ids = new HashSet<>();
        for (Authentication auth : authentications) {
            List<String> servers = auth.getServers();
            if (!servers.isEmpty()) {
                org.eclipse.aether.repository.Authentication a = ConverterUtils.toAuthentication(auth);
                for (String server : servers) {
                    if (ids.add(server)) {
                        selector.add(server, a);
                    }
                }
            }
        }

        Settings settings = getSettings();
        for (Server server : settings.getServers()) {
            AuthenticationBuilder auth = new AuthenticationBuilder();
            auth.addUsername(server.getUsername()).addPassword(server.getPassword());
            auth.addPrivateKey(server.getPrivateKey(), server.getPassphrase());
            selector.add(server.getId(), auth.build());
        }

        return new ConservativeAuthenticationSelector(selector);
    }

    private RemoteRepositories getRemoteRepositories() {
        RemoteRepositories remoteRepositories = new RemoteRepositories();
        remoteRepositories.setProject(project);

        Settings settings = getSettings();
        List<String> activeProfiles = settings.getActiveProfiles();
        for (String profileId : activeProfiles) {
            Profile profile = settings.getProfilesAsMap().get(profileId);
            for (Repository repository : profile.getRepositories()) {
                String id = repository.getId();
                RemoteRepository repo = new RemoteRepository();
                repo.setProject(project);
                repo.setId(id);
                repo.setUrl(repository.getUrl());
                if (repository.getReleases() != null) {
                    RepositoryPolicy repositoryPolicy = repository.getReleases();
                    Policy policy = new Policy();
                    policy.setEnabled(repositoryPolicy.isEnabled());
                    if (repositoryPolicy.getChecksumPolicy() != null) {
                        policy.setChecksums(repositoryPolicy.getChecksumPolicy());
                    }
                    if (repositoryPolicy.getUpdatePolicy() != null) {
                        policy.setUpdates(repositoryPolicy.getUpdatePolicy());
                    }
                    repo.addReleases(policy);
                }
                if (repository.getSnapshots() != null) {
                    RepositoryPolicy repositoryPolicy = repository.getSnapshots();
                    Policy policy = new Policy();
                    policy.setEnabled(repositoryPolicy.isEnabled());
                    if (repositoryPolicy.getChecksumPolicy() != null) {
                        policy.setChecksums(repositoryPolicy.getChecksumPolicy());
                    }
                    if (repositoryPolicy.getUpdatePolicy() != null) {
                        policy.setUpdates(repositoryPolicy.getUpdatePolicy());
                    }
                    repo.addSnapshots(policy);
                }
                project.addReference(id, repo);

                repo = new RemoteRepository();
                repo.setProject(project);
                repo.setRefid(new Reference(project, id));
                remoteRepositories.addRemoterepo(repo);
            }
        }

        return remoteRepositories;
    }

    private RemoteRepositories getMergedRepositories() {
        RemoteRepositories defaultRepositories = AetherUtils.getDefaultRepositories(project);
        RemoteRepositories settingsRepositories = getRemoteRepositories();

        RemoteRepositories mergedRepositories = new RemoteRepositories();
        mergedRepositories.setProject(project);
        mergedRepositories.addRemoterepos(defaultRepositories);
        mergedRepositories.addRemoterepos(settingsRepositories);

        return mergedRepositories;
    }

    public synchronized void setUserSettings(File file) {
        if (!eq(this.userSettings, file)) {
            settings = null;
        }
        this.userSettings = file;
    }

    /* UT */ File getUserSettings() {
        if (userSettings == null) {
            userSettings = AetherUtils.findUserSettings(project);
        }
        return userSettings;
    }

    public void setGlobalSettings(File file) {
        if (!eq(this.globalSettings, file)) {
            settings = null;
        }
        this.globalSettings = file;
    }

    /* UT */ File getGlobalSettings() {
        if (globalSettings == null) {
            globalSettings = AetherUtils.findGlobalSettings(project);
        }
        return globalSettings;
    }

    public void addProxy(Proxy proxy) {
        proxies.add(proxy);
    }

    public void addMirror(Mirror mirror) {
        mirrors.add(mirror);
    }

    public void addAuthentication(Authentication authentication) {
        authentications.add(authentication);
    }

    public void setLocalRepository(LocalRepository localRepository) {
        this.localRepository = localRepository;
    }

    public Model loadModel(Task task, File pomFile, boolean local, RemoteRepositories remoteRepositories) {
        RepositorySystemSession session = getSession(task, null);

        remoteRepositories = remoteRepositories == null ? getMergedRepositories() : remoteRepositories;

        List<org.eclipse.aether.repository.RemoteRepository> repositories =
                ConverterUtils.toRepositories(task.getProject(), getSystem(), session, remoteRepositories);

        ModelResolver modelResolver =
                new AntModelResolver(session, "project", getSystem(), getRemoteRepoMan(), repositories);

        Settings settings = getSettings();

        try {
            DefaultModelBuildingRequest request = new DefaultModelBuildingRequest();
            request.setLocationTracking(true);
            request.setProcessPlugins(false);
            if (local) {
                request.setPomFile(pomFile);
                request.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_STRICT);
            } else {
                request.setModelSource(new FileModelSource(pomFile));
                request.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            }
            request.setSystemProperties(getSystemProperties());
            request.setUserProperties(getUserProperties());
            request.setProfiles(SettingsUtils.convert(settings.getProfiles()));
            request.setActiveProfileIds(settings.getActiveProfiles());
            request.setModelResolver(modelResolver);
            return antRepositorySystemSupplier.modelBuilder.build(request).getEffectiveModel();
        } catch (ModelBuildingException e) {
            throw new BuildException("Could not load POM " + pomFile + ": " + e.getMessage(), e);
        }
    }

    private Properties getSystemProperties() {
        Properties props = new Properties();
        getEnvProperties(props);
        props.putAll(System.getProperties());
        ConverterUtils.addProperties(props, project.getProperties());
        return props;
    }

    private void getEnvProperties(Properties props) {
        if (props == null) {
            props = new Properties();
        }
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String key = entry.getKey();
            if (OS_WINDOWS) {
                key = key.toUpperCase(Locale.ENGLISH);
            }
            key = "env." + key;
            props.put(key, entry.getValue());
        }
    }

    private Properties getUserProperties() {
        return ConverterUtils.addProperties(null, project.getUserProperties());
    }

    /**
     * Sets the default POM.
     */
    public void setDefaultPom(Pom pom) {
        this.defaultPom = pom;
    }

    /**
     * Returns the current default POM.
     */
    public Pom getDefaultPom() {
        return defaultPom;
    }

    public CollectResult collectDependencies(
            Task task,
            Dependencies dependencies,
            LocalRepository localRepository,
            RemoteRepositories remoteRepositories) {
        RepositorySystemSession session = getSession(task, localRepository);

        remoteRepositories = remoteRepositories == null ? getMergedRepositories() : remoteRepositories;

        List<org.eclipse.aether.repository.RemoteRepository> repos =
                ConverterUtils.toRepositories(project, getSystem(), session, remoteRepositories);

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRequestContext("project");

        for (org.eclipse.aether.repository.RemoteRepository repo : repos) {
            task.getProject().log("Using remote repository " + repo, Project.MSG_VERBOSE);
            collectRequest.addRepository(repo);
        }

        if (dependencies != null) {
            populateCollectRequest(collectRequest, task, session, dependencies, Collections.emptyList());
        }

        task.getProject().log("Collecting dependencies", Project.MSG_VERBOSE);

        CollectResult result;
        try {
            result = getSystem().collectDependencies(session, collectRequest);
        } catch (DependencyCollectionException e) {
            throw new BuildException("Could not collect dependencies: " + e.getMessage(), e);
        }

        return result;
    }

    private void populateCollectRequest(
            CollectRequest collectRequest,
            Task task,
            RepositorySystemSession session,
            Dependencies dependencies,
            List<Exclusion> exclusions) {
        List<Exclusion> globalExclusions = exclusions;
        if (!dependencies.getExclusions().isEmpty()) {
            globalExclusions = new ArrayList<>(exclusions);
            globalExclusions.addAll(dependencies.getExclusions());
        }

        final Collection<String> ids = new HashSet<>();

        for (DependencyContainer container : dependencies.getDependencyContainers()) {
            if (container instanceof Dependency) {
                Dependency dep = (Dependency) container;
                ids.add(dep.getVersionlessKey());
                collectRequest.addDependency(ConverterUtils.toDependency(dep, globalExclusions, session));
            } else {
                populateCollectRequest(collectRequest, task, session, (Dependencies) container, globalExclusions);
            }
        }

        if (dependencies.getPom() != null) {
            Model model = dependencies.getPom().getModel(task);
            if (model.getDependencyManagement() != null) {
                for (org.apache.maven.model.Dependency manDep :
                        model.getDependencyManagement().getDependencies()) {
                    Dependency dependency = new Dependency();
                    dependency.setArtifactId(manDep.getArtifactId());
                    dependency.setClassifier(manDep.getClassifier());
                    dependency.setGroupId(manDep.getGroupId());
                    dependency.setScope(manDep.getScope());
                    dependency.setType(manDep.getType());
                    dependency.setVersion(manDep.getVersion());
                    if (manDep.getSystemPath() != null
                            && !manDep.getSystemPath().isEmpty()) {
                        dependency.setSystemPath(task.getProject().resolveFile(manDep.getSystemPath()));
                    }
                    for (org.apache.maven.model.Exclusion exc : manDep.getExclusions()) {
                        Exclusion exclusion = new Exclusion();
                        exclusion.setGroupId(exc.getGroupId());
                        exclusion.setArtifactId(exc.getArtifactId());
                        exclusion.setClassifier("*");
                        exclusion.setExtension("*");
                        dependency.addExclusion(exclusion);
                    }
                    collectRequest.addManagedDependency(
                            ConverterUtils.toManagedDependency(dependency, globalExclusions, session));
                }
            }

            for (org.apache.maven.model.Dependency dep : model.getDependencies()) {
                Dependency dependency = new Dependency();
                dependency.setArtifactId(dep.getArtifactId());
                dependency.setClassifier(dep.getClassifier());
                dependency.setGroupId(dep.getGroupId());
                dependency.setScope(dep.getScope());
                dependency.setType(dep.getType());
                dependency.setVersion(dep.getVersion());
                if (ids.contains(dependency.getVersionlessKey())) {
                    project.log(
                            "Ignoring dependency " + dependency.getVersionlessKey() + " from " + model.getId()
                                    + ", already declared locally",
                            Project.MSG_VERBOSE);
                    continue;
                }
                if (dep.getSystemPath() != null && !dep.getSystemPath().isEmpty()) {
                    dependency.setSystemPath(task.getProject().resolveFile(dep.getSystemPath()));
                }
                for (org.apache.maven.model.Exclusion exc : dep.getExclusions()) {
                    Exclusion exclusion = new Exclusion();
                    exclusion.setGroupId(exc.getGroupId());
                    exclusion.setArtifactId(exc.getArtifactId());
                    exclusion.setClassifier("*");
                    exclusion.setExtension("*");
                    dependency.addExclusion(exclusion);
                }
                collectRequest.addDependency(ConverterUtils.toDependency(dependency, globalExclusions, session));
            }
        }

        if (dependencies.getFile() != null) {
            List<Dependency> deps = readDependencies(dependencies.getFile());
            for (Dependency dependency : deps) {
                if (ids.contains(dependency.getVersionlessKey())) {
                    project.log(
                            "Ignoring dependency " + dependency.getVersionlessKey() + " from " + dependencies.getFile()
                                    + ", already declared locally",
                            Project.MSG_VERBOSE);
                    continue;
                }
                collectRequest.addDependency(ConverterUtils.toDependency(dependency, globalExclusions, session));
            }
        }
    }

    private List<Dependency> readDependencies(File file) {
        final List<Dependency> dependencies = new ArrayList<>();
        try {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    int comment = line.indexOf('#');
                    if (comment >= 0) {
                        line = line.substring(0, comment);
                    }
                    line = line.trim();
                    if (line.length() <= 0) {
                        continue;
                    }
                    Dependency dependency = new Dependency();
                    dependency.setCoords(line);
                    dependencies.add(dependency);
                }
            }
        } catch (IOException e) {
            throw new BuildException("Cannot read " + file, e);
        }
        return dependencies;
    }

    public void install(Task task, Pom pom, Artifacts artifacts) {
        RepositorySystemSession session = getSession(task, null);

        InstallRequest request = new InstallRequest();
        request.setArtifacts(toArtifacts(task, session, pom, artifacts));

        try {
            getSystem().install(session, request);
        } catch (InstallationException e) {
            throw new BuildException("Could not install artifacts: " + e.getMessage(), e);
        }
    }

    public void deploy(
            Task task,
            Pom pom,
            Artifacts artifacts,
            RemoteRepository releaseRepository,
            RemoteRepository snapshotRepository) {
        RepositorySystemSession session = getSession(task, null);

        DeployRequest request = new DeployRequest();
        request.setArtifacts(toArtifacts(task, session, pom, artifacts));
        boolean snapshot = request.getArtifacts().iterator().next().isSnapshot();
        RemoteRepository distRepo = (snapshot && snapshotRepository != null) ? snapshotRepository : releaseRepository;
        request.setRepository(ConverterUtils.toDistRepository(distRepo, session));

        try {
            getSystem().deploy(session, request);
        } catch (DeploymentException e) {
            throw new BuildException("Could not deploy artifacts: " + e.getMessage(), e);
        }
    }

    private List<org.eclipse.aether.artifact.Artifact> toArtifacts(
            Task task, RepositorySystemSession session, Pom pom, Artifacts artifacts) {
        Model model = pom.getModel(task);
        File pomFile = pom.getFile();

        final List<org.eclipse.aether.artifact.Artifact> results = new ArrayList<>();

        org.eclipse.aether.artifact.Artifact pomArtifact = new DefaultArtifact(
                        model.getGroupId(), model.getArtifactId(), "pom", model.getVersion())
                .setFile(pomFile);
        results.add(pomArtifact);

        for (Artifact artifact : artifacts.getArtifacts()) {
            org.eclipse.aether.artifact.Artifact buildArtifact = new DefaultArtifact(
                            model.getGroupId(),
                            model.getArtifactId(),
                            artifact.getClassifier(),
                            artifact.getType(),
                            model.getVersion())
                    .setFile(artifact.getFile());
            results.add(buildArtifact);
        }

        return results;
    }
}
