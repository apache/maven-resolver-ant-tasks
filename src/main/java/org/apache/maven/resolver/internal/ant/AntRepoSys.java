package org.apache.maven.resolver.internal.ant;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuilderFactory;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelBuilder;
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
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.installation.InstallRequest;
import org.eclipse.aether.installation.InstallationException;
import org.eclipse.aether.repository.AuthenticationSelector;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.MirrorSelector;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.spi.log.Logger;
import org.eclipse.aether.transport.classpath.ClasspathTransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.eclipse.aether.util.repository.ConservativeAuthenticationSelector;
import org.eclipse.aether.util.repository.DefaultAuthenticationSelector;
import org.eclipse.aether.util.repository.DefaultMirrorSelector;
import org.eclipse.aether.util.repository.DefaultProxySelector;

/**
 */
public class AntRepoSys
{

    private static final boolean OS_WINDOWS = Os.isFamily( "windows" );

    private static final ModelBuilder MODEL_BUILDER = new DefaultModelBuilderFactory().newInstance();

    private static final SettingsBuilder SETTINGS_BUILDER = new DefaultSettingsBuilderFactory().newInstance();

    private static final SettingsDecrypter SETTINGS_DECRYPTER = new AntSettingsDecryptorFactory().newInstance();

    private final Project project;

    private final DefaultServiceLocator locator;

    private RepositorySystem repoSys;

    private RemoteRepositoryManager remoteRepoMan;

    private File userSettings;

    private File globalSettings;

    private Settings settings;

    private final List<Mirror> mirrors = new CopyOnWriteArrayList<Mirror>();

    private final List<Proxy> proxies = new CopyOnWriteArrayList<Proxy>();

    private final List<Authentication> authentications = new CopyOnWriteArrayList<Authentication>();

    private LocalRepository localRepository;

    private Pom defaultPom;

    private static <T> boolean eq( T o1, T o2 )
    {
        return ( o1 == null ) ? o2 == null : o1.equals( o2 );
    }

    public static synchronized AntRepoSys getInstance( Project project )
    {
        Object obj = project.getReference( Names.ID );
        if ( obj instanceof AntRepoSys )
        {
            return (AntRepoSys) obj;
        }
        AntRepoSys instance = new AntRepoSys( project );
        project.addReference( Names.ID, instance );
        instance.initDefaults();
        return instance;
    }

    private AntRepoSys( Project project )
    {
        this.project = project;

        locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.setErrorHandler( new AntServiceLocatorErrorHandler( project ) );
        locator.setServices( Logger.class, new AntLogger( project ) );
        locator.setServices( ModelBuilder.class, MODEL_BUILDER );
        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );
        locator.addService( TransporterFactory.class, ClasspathTransporterFactory.class );
    }

    private void initDefaults()
    {
        RemoteRepository repo = new RemoteRepository();
        repo.setProject( project );
        repo.setId( "central" );
        repo.setUrl( "https://repo1.maven.org/maven2/" );
        project.addReference( Names.ID_CENTRAL, repo );

        repo = new RemoteRepository();
        repo.setProject( project );
        repo.setRefid( new Reference( project, Names.ID_CENTRAL ) );
        RemoteRepositories repos = new RemoteRepositories();
        repos.setProject( project );
        repos.addRemoterepo( repo );
        project.addReference( Names.ID_DEFAULT_REPOS, repos );
    }

    public synchronized RepositorySystem getSystem()
    {
        if ( repoSys == null )
        {
            repoSys = locator.getService( RepositorySystem.class );
            if ( repoSys == null )
            {
                throw new BuildException( "The repository system could not be initialized" );
            }
        }
        return repoSys;
    }

    private synchronized RemoteRepositoryManager getRemoteRepoMan()
    {
        if ( remoteRepoMan == null )
        {
            remoteRepoMan = locator.getService( RemoteRepositoryManager.class );
            if ( remoteRepoMan == null )
            {
                throw new BuildException( "The repository system could not be initialized" );
            }
        }
        return remoteRepoMan;
    }

    public RepositorySystemSession getSession( Task task, LocalRepository localRepo )
    {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        Map<Object, Object> configProps = new LinkedHashMap<Object, Object>();
        configProps.put( ConfigurationProperties.USER_AGENT, getUserAgent() );
        configProps.putAll( (Map<?, ?>) project.getProperties() );
        processServerConfiguration( configProps );
        session.setConfigProperties( configProps );

        session.setOffline( isOffline() );
        session.setUserProperties( project.getUserProperties() );

        session.setProxySelector( getProxySelector() );
        session.setMirrorSelector( getMirrorSelector() );
        session.setAuthenticationSelector( getAuthSelector() );

        session.setCache( new DefaultRepositoryCache() );

        session.setRepositoryListener( new AntRepositoryListener( task ) );
        session.setTransferListener( new AntTransferListener( task ) );

        session.setLocalRepositoryManager( getLocalRepoMan( session, localRepo ) );

        session.setWorkspaceReader( ProjectWorkspaceReader.getInstance() );

        return session;
    }

    private String getUserAgent()
    {
        StringBuilder buffer = new StringBuilder( 128 );

        buffer.append( "Apache-Ant/" ).append( project.getProperty( "ant.version" ) );
        buffer.append( " (" );
        buffer.append( "Java " ).append( System.getProperty( "java.version" ) );
        buffer.append( "; " );
        buffer.append( System.getProperty( "os.name" ) ).append( " " ).append( System.getProperty( "os.version" ) );
        buffer.append( ")" );
        buffer.append( " Aether" );

        return buffer.toString();
    }

    private boolean isOffline()
    {
        String prop = project.getProperty( Names.PROPERTY_OFFLINE );
        if ( prop != null )
        {
            return Boolean.parseBoolean( prop );
        }
        return getSettings().isOffline();
    }

    private void processServerConfiguration( Map<Object, Object> configProps )
    {
        Settings settings = getSettings();
        for ( Server server : settings.getServers() )
        {
            if ( server.getConfiguration() != null )
            {
                Xpp3Dom dom = (Xpp3Dom) server.getConfiguration();
                for ( int i = dom.getChildCount() - 1; i >= 0; i-- )
                {
                    Xpp3Dom child = dom.getChild( i );
                    if ( "wagonProvider".equals( child.getName() ) )
                    {
                        dom.removeChild( i );
                    }
                    else if ( "httpHeaders".equals( child.getName() ) )
                    {
                        configProps.put( ConfigurationProperties.HTTP_HEADERS + "." + server.getId(),
                                         getHttpHeaders( child ) );
                    }
                }

                configProps.put( "aether.connector.wagon.config." + server.getId(), dom );
            }

            configProps.put( "aether.connector.perms.fileMode." + server.getId(), server.getFilePermissions() );
            configProps.put( "aether.connector.perms.dirMode." + server.getId(), server.getDirectoryPermissions() );
        }
    }

    private Map<String, String> getHttpHeaders( Xpp3Dom dom )
    {
        Map<String, String> headers = new HashMap<String, String>();
        for ( int i = 0; i < dom.getChildCount(); i++ )
        {
            Xpp3Dom child = dom.getChild( i );
            Xpp3Dom name = child.getChild( "name" );
            Xpp3Dom value = child.getChild( "value" );
            if ( name != null && name.getValue() != null )
            {
                headers.put( name.getValue(), ( value != null ) ? value.getValue() : null );
            }
        }
        return Collections.unmodifiableMap( headers );
    }

    private File getDefaultLocalRepoDir()
    {
        String dir = project.getProperty( "maven.repo.local" );
        if ( dir != null )
        {
            return project.resolveFile( dir );
        }

        Settings settings = getSettings();
        if ( settings.getLocalRepository() != null )
        {
            return new File( settings.getLocalRepository() );
        }

        return new File( new File( project.getProperty( "user.home" ), ".m2" ), "repository" );
    }

    private LocalRepositoryManager getLocalRepoMan( RepositorySystemSession session, LocalRepository localRepo )
    {
        if ( localRepo == null )
        {
            localRepo = localRepository;
        }

        File repoDir;
        if ( localRepo != null && localRepo.getDir() != null )
        {
            repoDir = localRepo.getDir();
        }
        else
        {
            repoDir = getDefaultLocalRepoDir();
        }

        org.eclipse.aether.repository.LocalRepository repo =
            new org.eclipse.aether.repository.LocalRepository( repoDir );

        return getSystem().newLocalRepositoryManager( session, repo );
    }

    private synchronized Settings getSettings()
    {
        if ( settings == null )
        {
            DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
            request.setUserSettingsFile( getUserSettings() );
            request.setGlobalSettingsFile( getGlobalSettings() );
            request.setSystemProperties( getSystemProperties() );
            request.setUserProperties( getUserProperties() );

            try
            {
                settings = SETTINGS_BUILDER.build( request ).getEffectiveSettings();
            }
            catch ( SettingsBuildingException e )
            {
                project.log( "Could not process settings.xml: " + e.getMessage(), e, Project.MSG_WARN );
            }

            SettingsDecryptionResult result =
                SETTINGS_DECRYPTER.decrypt( new DefaultSettingsDecryptionRequest( settings ) );
            settings.setServers( result.getServers() );
            settings.setProxies( result.getProxies() );
        }
        return settings;
    }

    private ProxySelector getProxySelector()
    {
        DefaultProxySelector selector = new DefaultProxySelector();

        for ( Proxy proxy : proxies )
        {
            selector.add( ConverterUtils.toProxy( proxy ), proxy.getNonProxyHosts() );
        }

        Settings settings = getSettings();
        for ( org.apache.maven.settings.Proxy proxy : settings.getProxies() )
        {
            AuthenticationBuilder auth = new AuthenticationBuilder();
            auth.addUsername( proxy.getUsername() ).addPassword( proxy.getPassword() );
            selector.add( new org.eclipse.aether.repository.Proxy( proxy.getProtocol(), proxy.getHost(),
                                                                   proxy.getPort(), auth.build() ),
                          proxy.getNonProxyHosts() );
        }

        return selector;
    }

    private MirrorSelector getMirrorSelector()
    {
        DefaultMirrorSelector selector = new DefaultMirrorSelector();

        for ( Mirror mirror : mirrors )
        {
            selector.add( mirror.getId(), mirror.getUrl(), mirror.getType(), false, mirror.getMirrorOf(), null );
        }

        Settings settings = getSettings();
        for ( org.apache.maven.settings.Mirror mirror : settings.getMirrors() )
        {
            selector.add( String.valueOf( mirror.getId() ), mirror.getUrl(), mirror.getLayout(), false,
                          mirror.getMirrorOf(), mirror.getMirrorOfLayouts() );
        }

        return selector;
    }

    private AuthenticationSelector getAuthSelector()
    {
        DefaultAuthenticationSelector selector = new DefaultAuthenticationSelector();

        Collection<String> ids = new HashSet<String>();
        for ( Authentication auth : authentications )
        {
            List<String> servers = auth.getServers();
            if ( !servers.isEmpty() )
            {
                org.eclipse.aether.repository.Authentication a = ConverterUtils.toAuthentication( auth );
                for ( String server : servers )
                {
                    if ( ids.add( server ) )
                    {
                        selector.add( server, a );
                    }
                }
            }
        }

        Settings settings = getSettings();
        for ( Server server : settings.getServers() )
        {
            AuthenticationBuilder auth = new AuthenticationBuilder();
            auth.addUsername( server.getUsername() ).addPassword( server.getPassword() );
            auth.addPrivateKey( server.getPrivateKey(), server.getPassphrase() );
            selector.add( server.getId(), auth.build() );
        }

        return new ConservativeAuthenticationSelector( selector );
    }

    public synchronized void setUserSettings( File file )
    {
        if ( !eq( this.userSettings, file ) )
        {
            settings = null;
        }
        this.userSettings = file;
    }

    /* UT */File getUserSettings()
    {
        if ( userSettings == null )
        {
            userSettings = AetherUtils.findUserSettings( project );
        }
        return userSettings;
    }

    public void setGlobalSettings( File file )
    {
        if ( !eq( this.globalSettings, file ) )
        {
            settings = null;
        }
        this.globalSettings = file;
    }

    /* UT */File getGlobalSettings()
    {
        if ( globalSettings == null )
        {
            globalSettings = AetherUtils.findGlobalSettings( project );
        }
        return globalSettings;
    }

    public void addProxy( Proxy proxy )
    {
        proxies.add( proxy );
    }

    public void addMirror( Mirror mirror )
    {
        mirrors.add( mirror );
    }

    public void addAuthentication( Authentication authentication )
    {
        authentications.add( authentication );
    }

    public void setLocalRepository( LocalRepository localRepository )
    {
        this.localRepository = localRepository;
    }

    public Model loadModel( Task task, File pomFile, boolean local, RemoteRepositories remoteRepositories )
    {
        RepositorySystemSession session = getSession( task, null );

        remoteRepositories =
            remoteRepositories == null ? AetherUtils.getDefaultRepositories( project ) : remoteRepositories;

        List<org.eclipse.aether.repository.RemoteRepository> repositories =
            ConverterUtils.toRepositories( task.getProject(), session, remoteRepositories, getRemoteRepoMan() );

        ModelResolver modelResolver =
            new AntModelResolver( session, "project", getSystem(), getRemoteRepoMan(), repositories );

        Settings settings = getSettings();

        try
        {
            DefaultModelBuildingRequest request = new DefaultModelBuildingRequest();
            request.setLocationTracking( true );
            request.setProcessPlugins( false );
            if ( local )
            {
                request.setPomFile( pomFile );
                request.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_STRICT );
            }
            else
            {
                request.setModelSource( new FileModelSource( pomFile ) );
                request.setValidationLevel( ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL );
            }
            request.setSystemProperties( getSystemProperties() );
            request.setUserProperties( getUserProperties() );
            request.setProfiles( SettingsUtils.convert( settings.getProfiles() ) );
            request.setActiveProfileIds( settings.getActiveProfiles() );
            request.setModelResolver( modelResolver );
            return MODEL_BUILDER.build( request ).getEffectiveModel();
        }
        catch ( ModelBuildingException e )
        {
            throw new BuildException( "Could not load POM " + pomFile + ": " + e.getMessage(), e );
        }
    }

    private Properties getSystemProperties()
    {
        Properties props = new Properties();
        getEnvProperties( props );
        props.putAll( System.getProperties() );
        ConverterUtils.addProperties( props, project.getProperties() );
        return props;
    }

    private Properties getEnvProperties( Properties props )
    {
        if ( props == null )
        {
            props = new Properties();
        }
        boolean envCaseInsensitive = OS_WINDOWS;
        for ( Map.Entry<String, String> entry : System.getenv().entrySet() )
        {
            String key = entry.getKey();
            if ( envCaseInsensitive )
            {
                key = key.toUpperCase( Locale.ENGLISH );
            }
            key = "env." + key;
            props.put( key, entry.getValue() );
        }
        return props;
    }

    private Properties getUserProperties()
    {
        return ConverterUtils.addProperties( null, project.getUserProperties() );
    }

    /**
     * Sets the default POM.
     */
    public void setDefaultPom( Pom pom )
    {
        this.defaultPom = pom;
    }

    /**
     * Returns the current default POM.
     */
    public Pom getDefaultPom()
    {
        return defaultPom;
    }

    public CollectResult collectDependencies( Task task, Dependencies dependencies, LocalRepository localRepository,
                                              RemoteRepositories remoteRepositories )
    {
        RepositorySystemSession session = getSession( task, localRepository );

        remoteRepositories =
            remoteRepositories == null ? AetherUtils.getDefaultRepositories( project ) : remoteRepositories;

        List<org.eclipse.aether.repository.RemoteRepository> repos =
            ConverterUtils.toRepositories( project, session, remoteRepositories, getRemoteRepoMan() );

        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRequestContext( "project" );

        for ( org.eclipse.aether.repository.RemoteRepository repo : repos )
        {
            task.getProject().log( "Using remote repository " + repo, Project.MSG_VERBOSE );
            collectRequest.addRepository( repo );
        }

        if ( dependencies != null )
        {
            populateCollectRequest( collectRequest, task, session, dependencies, Collections.<Exclusion>emptyList() );
        }

        task.getProject().log( "Collecting dependencies", Project.MSG_VERBOSE );

        CollectResult result;
        try
        {
            result = getSystem().collectDependencies( session, collectRequest );
        }
        catch ( DependencyCollectionException e )
        {
            throw new BuildException( "Could not collect dependencies: " + e.getMessage(), e );
        }

        return result;
    }

    private void populateCollectRequest( CollectRequest collectRequest, Task task, RepositorySystemSession session,
                                         Dependencies dependencies, List<Exclusion> exclusions )
    {
        List<Exclusion> globalExclusions = exclusions;
        if ( !dependencies.getExclusions().isEmpty() )
        {
            globalExclusions = new ArrayList<Exclusion>( exclusions );
            globalExclusions.addAll( dependencies.getExclusions() );
        }

        Collection<String> ids = new HashSet<String>();

        for ( DependencyContainer container : dependencies.getDependencyContainers() )
        {
            if ( container instanceof Dependency )
            {
                Dependency dep = (Dependency) container;
                ids.add( dep.getVersionlessKey() );
                collectRequest.addDependency( ConverterUtils.toDependency( dep, globalExclusions, session ) );
            }
            else
            {
                populateCollectRequest( collectRequest, task, session, (Dependencies) container, globalExclusions );
            }
        }

        if ( dependencies.getPom() != null )
        {
            Model model = dependencies.getPom().getModel( task );
            for ( org.apache.maven.model.Dependency dep : model.getDependencies() )
            {
                Dependency dependency = new Dependency();
                dependency.setArtifactId( dep.getArtifactId() );
                dependency.setClassifier( dep.getClassifier() );
                dependency.setGroupId( dep.getGroupId() );
                dependency.setScope( dep.getScope() );
                dependency.setType( dep.getType() );
                dependency.setVersion( dep.getVersion() );
                if ( ids.contains( dependency.getVersionlessKey() ) )
                {
                    project.log( "Ignoring dependency " + dependency.getVersionlessKey() + " from " + model.getId()
                        + ", already declared locally", Project.MSG_VERBOSE );
                    continue;
                }
                if ( dep.getSystemPath() != null && dep.getSystemPath().length() > 0 )
                {
                    dependency.setSystemPath( task.getProject().resolveFile( dep.getSystemPath() ) );
                }
                for ( org.apache.maven.model.Exclusion exc : dep.getExclusions() )
                {
                    Exclusion exclusion = new Exclusion();
                    exclusion.setGroupId( exc.getGroupId() );
                    exclusion.setArtifactId( exc.getArtifactId() );
                    exclusion.setClassifier( "*" );
                    exclusion.setExtension( "*" );
                    dependency.addExclusion( exclusion );
                }
                collectRequest.addDependency( ConverterUtils.toDependency( dependency, globalExclusions, session ) );
            }
        }

        if ( dependencies.getFile() != null )
        {
            List<Dependency> deps = readDependencies( dependencies.getFile() );
            for ( Dependency dependency : deps )
            {
                if ( ids.contains( dependency.getVersionlessKey() ) )
                {
                    project.log( "Ignoring dependency " + dependency.getVersionlessKey() + " from "
                                     + dependencies.getFile() + ", already declared locally", Project.MSG_VERBOSE );
                    continue;
                }
                collectRequest.addDependency( ConverterUtils.toDependency( dependency, globalExclusions, session ) );
            }
        }
    }

    private List<Dependency> readDependencies( File file )
    {
        List<Dependency> dependencies = new ArrayList<Dependency>();
        try
        {
            BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file ), "UTF-8" ) );
            try
            {
                for ( String line = reader.readLine(); line != null; line = reader.readLine() )
                {
                    int comment = line.indexOf( '#' );
                    if ( comment >= 0 )
                    {
                        line = line.substring( 0, comment );
                    }
                    line = line.trim();
                    if ( line.length() <= 0 )
                    {
                        continue;
                    }
                    Dependency dependency = new Dependency();
                    dependency.setCoords( line );
                    dependencies.add( dependency );
                }
            }
            finally
            {
                reader.close();
            }
        }
        catch ( IOException e )
        {
            throw new BuildException( "Cannot read " + file, e );
        }
        return dependencies;
    }

    public void install( Task task, Pom pom, Artifacts artifacts )
    {
        RepositorySystemSession session = getSession( task, null );

        InstallRequest request = new InstallRequest();
        request.setArtifacts( toArtifacts( task, session, pom, artifacts ) );

        try
        {
            getSystem().install( session, request );
        }
        catch ( InstallationException e )
        {
            throw new BuildException( "Could not install artifacts: " + e.getMessage(), e );
        }
    }

    public void deploy( Task task, Pom pom, Artifacts artifacts, RemoteRepository releaseRepository,
                        RemoteRepository snapshotRepository )
    {
        RepositorySystemSession session = getSession( task, null );

        DeployRequest request = new DeployRequest();
        request.setArtifacts( toArtifacts( task, session, pom, artifacts ) );
        boolean snapshot = request.getArtifacts().iterator().next().isSnapshot();
        RemoteRepository distRepo = ( snapshot && snapshotRepository != null ) ? snapshotRepository : releaseRepository;
        request.setRepository( ConverterUtils.toDistRepository( distRepo, session ) );

        try
        {
            getSystem().deploy( session, request );
        }
        catch ( DeploymentException e )
        {
            throw new BuildException( "Could not deploy artifacts: " + e.getMessage(), e );
        }
    }

    private List<org.eclipse.aether.artifact.Artifact> toArtifacts( Task task, RepositorySystemSession session,
                                                                    Pom pom, Artifacts artifacts )
    {
        Model model = pom.getModel( task );
        File pomFile = pom.getFile();

        List<org.eclipse.aether.artifact.Artifact> results = new ArrayList<org.eclipse.aether.artifact.Artifact>();

        org.eclipse.aether.artifact.Artifact pomArtifact =
            new DefaultArtifact( model.getGroupId(), model.getArtifactId(), "pom", model.getVersion() ).setFile( pomFile );
        results.add( pomArtifact );

        for ( Artifact artifact : artifacts.getArtifacts() )
        {
            org.eclipse.aether.artifact.Artifact buildArtifact =
                new DefaultArtifact( model.getGroupId(), model.getArtifactId(), artifact.getClassifier(),
                                     artifact.getType(), model.getVersion() ).setFile( artifact.getFile() );
            results.add( buildArtifact );
        }

        return results;
    }

}
