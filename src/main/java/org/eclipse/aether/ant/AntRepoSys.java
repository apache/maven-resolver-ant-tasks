/*******************************************************************************
 * Copyright (c) 2010, 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.aether.ant;

import java.io.File;
import java.util.Collection;
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
import org.apache.maven.repository.internal.MavenRepositorySystemSession;
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
import org.eclipse.aether.ant.types.Authentication;
import org.eclipse.aether.ant.types.Dependencies;
import org.eclipse.aether.ant.types.Dependency;
import org.eclipse.aether.ant.types.Exclusion;
import org.eclipse.aether.ant.types.LocalRepository;
import org.eclipse.aether.ant.types.Mirror;
import org.eclipse.aether.ant.types.Pom;
import org.eclipse.aether.ant.types.Proxy;
import org.eclipse.aether.ant.types.RemoteRepositories;
import org.eclipse.aether.ant.types.RemoteRepository;
import org.eclipse.aether.ConfigurationProperties;
import org.eclipse.aether.DefaultRepositoryCache;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.ant.util.AetherUtils;
import org.eclipse.aether.ant.util.ConverterUtils;
import org.eclipse.aether.ant.util.SettingsUtils;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.async.AsyncRepositoryConnectorFactory;
import org.eclipse.aether.connector.file.FileRepositoryConnectorFactory;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.repository.AuthenticationSelector;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.MirrorSelector;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.log.Logger;
import org.eclipse.aether.util.repository.ConservativeAuthenticationSelector;
import org.eclipse.aether.util.repository.DefaultAuthenticationSelector;
import org.eclipse.aether.util.repository.DefaultMirrorSelector;
import org.eclipse.aether.util.repository.DefaultProxySelector;

/**
 */
public class AntRepoSys
{

    private static boolean OS_WINDOWS = Os.isFamily( "windows" );

    private static final ModelBuilder modelBuilder = new DefaultModelBuilderFactory().newInstance();

    private static final SettingsBuilder settingsBuilder = new DefaultSettingsBuilderFactory().newInstance();

    private static final SettingsDecrypter settingsDecrypter = new AntSettingsDecryptorFactory().newInstance();

    private Project project;

    private AntServiceLocator locator;

    private RepositorySystem repoSys;

    private RemoteRepositoryManager remoteRepoMan;

    private File userSettings;

    private File globalSettings;

    private Settings settings;

    private List<Mirror> mirrors = new CopyOnWriteArrayList<Mirror>();

    private List<Proxy> proxies = new CopyOnWriteArrayList<Proxy>();

    private List<Authentication> authentications = new CopyOnWriteArrayList<Authentication>();

    private LocalRepository localRepository;

    private Pom defaultPom;

    private File mavenRepoDirFromProperty;

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

        locator = new AntServiceLocator( project );
        locator.setServices( Logger.class, new AntLogger( project ) );
        locator.setServices( ModelBuilder.class, modelBuilder );
        locator.addService( RepositoryConnectorFactory.class, FileRepositoryConnectorFactory.class );
        locator.addService( RepositoryConnectorFactory.class, AsyncRepositoryConnectorFactory.class );
    }

    private void initDefaults()
    {
        RemoteRepository repo = new RemoteRepository();
        repo.setProject( project );
        repo.setId( "central" );
        repo.setUrl( "http://repo1.maven.org/maven2/" );
        project.addReference( Names.ID_CENTRAL, repo );

        repo = new RemoteRepository();
        repo.setProject( project );
        repo.setRefid( new Reference( project, Names.ID_CENTRAL ) );
        RemoteRepositories repos = new RemoteRepositories();
        repos.setProject( project );
        repos.addRemoterepo( repo );
        project.addReference( Names.ID_DEFAULT_REPOS, repos );

        // resolve maven.repo.local only once relative to project, as the basedir may change for <ant> tasks
        String localRepoResolved = project.getProperty( "maven.repo.local.resolved" );
        if ( localRepoResolved != null )
        {
            mavenRepoDirFromProperty = new File( localRepoResolved );
        }
        else
        {
            String mavenRepoProperty = project.getProperty( "maven.repo.local" );
            if ( mavenRepoProperty != null )
            {
                mavenRepoDirFromProperty = project.resolveFile( mavenRepoProperty );
                project.setProperty( "maven.repo.local.resolved", mavenRepoDirFromProperty.getAbsolutePath() );
            }
        }
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
        DefaultRepositorySystemSession session = new MavenRepositorySystemSession();

        Map<Object, Object> configProps = new LinkedHashMap<Object, Object>();
        configProps.put( ConfigurationProperties.USER_AGENT, getUserAgent() );
        configProps.putAll( System.getProperties() );
        configProps.putAll( (Map<?, ?>) project.getProperties() );
        configProps.putAll( (Map<?, ?>) project.getUserProperties() );
        session.setConfigProps( configProps );

        session.setNotFoundCachingEnabled( false );
        session.setTransferErrorCachingEnabled( false );

        session.setOffline( isOffline() );
        session.setUserProps( project.getUserProperties() );

        session.setLocalRepositoryManager( getLocalRepoMan( localRepo ) );

        session.setProxySelector( getProxySelector() );
        session.setMirrorSelector( getMirrorSelector() );
        session.setAuthenticationSelector( getAuthSelector() );

        session.setCache( new DefaultRepositoryCache() );

        session.setRepositoryListener( new AntRepositoryListener( task ) );
        session.setTransferListener( new AntTransferListener( task ) );

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

    private File getDefaultLocalRepoDir()
    {
        if ( mavenRepoDirFromProperty != null )
        {
            return mavenRepoDirFromProperty;
        }

        Settings settings = getSettings();
        if ( settings.getLocalRepository() != null )
        {
            return new File( settings.getLocalRepository() );
        }

        return new File( new File( project.getProperty( "user.home" ), ".m2" ), "repository" );
    }

    private LocalRepositoryManager getLocalRepoMan( LocalRepository localRepo )
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

        return getSystem().newLocalRepositoryManager( repo );
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
                settings = settingsBuilder.build( request ).getEffectiveSettings();
            }
            catch ( SettingsBuildingException e )
            {
                project.log( "Could not process settings.xml: " + e.getMessage(), e, Project.MSG_WARN );
            }

            SettingsDecryptionResult result =
                settingsDecrypter.decrypt( new DefaultSettingsDecryptionRequest( settings ) );
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
            org.eclipse.aether.repository.Authentication auth = null;
            if ( proxy.getUsername() != null || proxy.getPassword() != null )
            {
                auth = new org.eclipse.aether.repository.Authentication( proxy.getUsername(), proxy.getPassword() );
            }
            selector.add( new org.eclipse.aether.repository.Proxy( proxy.getProtocol(), proxy.getHost(),
                                                                    proxy.getPort(), auth ), proxy.getNonProxyHosts() );
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
            org.eclipse.aether.repository.Authentication auth =
                new org.eclipse.aether.repository.Authentication( server.getUsername(), server.getPassword(),
                                                                   server.getPrivateKey(), server.getPassphrase() );
            selector.add( server.getId(), auth );
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
            return modelBuilder.build( request ).getEffectiveModel();
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
            List<Exclusion> globalExclusions = dependencies.getExclusions();
            Collection<String> ids = new HashSet<String>();

            for ( Dependency dep : dependencies.getDependencies() )
            {
                ids.add( dep.getVersionlessKey() );
                collectRequest.addDependency( ConverterUtils.toDependency( dep, globalExclusions, session ) );
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

}
