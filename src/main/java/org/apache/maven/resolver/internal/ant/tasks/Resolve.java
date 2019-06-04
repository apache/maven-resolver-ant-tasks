package org.apache.maven.resolver.internal.ant.tasks;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.maven.resolver.internal.ant.AntRepoSys;
import org.apache.maven.resolver.internal.ant.Names;
import org.apache.maven.resolver.internal.ant.types.Dependencies;
import org.apache.maven.resolver.internal.ant.types.Pom;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Resources;
import org.apache.tools.ant.util.FileUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;

/**
 */
public class Resolve
    extends AbstractResolvingTask
{

    private List<ArtifactConsumer> consumers = new ArrayList<ArtifactConsumer>();

    private boolean failOnMissingAttachments;

    public void setFailOnMissingAttachments( boolean failOnMissingAttachments )
    {
        this.failOnMissingAttachments = failOnMissingAttachments;
    }

    public Path createPath()
    {
        Path path = new Path();
        consumers.add( path );
        return path;
    }

    public Files createFiles()
    {
        Files files = new Files();
        consumers.add( files );
        return files;
    }

    public Props createProperties()
    {
        Props props = new Props();
        consumers.add( props );
        return props;
    }

    private void validate()
    {
        for ( ArtifactConsumer consumer : consumers )
        {
            consumer.validate();
        }

        Pom pom = AntRepoSys.getInstance( getProject() ).getDefaultPom();
        if ( dependencies == null && pom != null )
        {
            log( "Using default pom for dependency resolution (" + pom.toString() + ")", Project.MSG_INFO );
            dependencies = new Dependencies();
            dependencies.setProject( getProject() );
            getProject().addReference( Names.ID_DEFAULT_POM, pom );
            dependencies.setPomRef( new Reference( getProject(), Names.ID_DEFAULT_POM ) );
        }

        if ( dependencies != null )
        {
            dependencies.validate( this );
        }
        else
        {
            throw new BuildException( "No <dependencies> set for resolution" );
        }
    }

    @Override
    public void execute()
        throws BuildException
    {
        validate();


        AntRepoSys sys = AntRepoSys.getInstance( getProject() );

        RepositorySystemSession session = sys.getSession( this, localRepository );
        RepositorySystem system = sys.getSystem();
        log( "Using local repository " + session.getLocalRepository(), Project.MSG_VERBOSE );

        DependencyNode root = collectDependencies().getRoot();
        root.accept( new DependencyGraphLogger( this ) );

        Map<String, Group> groups = new HashMap<String, Group>();
        for ( ArtifactConsumer consumer : consumers )
        {
            String classifier = consumer.getClassifier();
            Group group = groups.get( classifier );
            if ( group == null )
            {
                group = new Group( classifier );
                groups.put( classifier, group );
            }
            group.add( consumer );
        }

        for ( Group group : groups.values() )
        {
            group.createRequests( root );
        }

        log( "Resolving artifacts", Project.MSG_INFO );

        for ( Group group : groups.values() )
        {
            List<ArtifactResult> results;
            try
            {
                results = system.resolveArtifacts( session, group.getRequests() );
            }
            catch ( ArtifactResolutionException e )
            {
                if ( !group.isAttachments() || failOnMissingAttachments )
                {
                    throw new BuildException( "Could not resolve artifacts: " + e.getMessage(), e );
                }
                results = e.getResults();
                for ( ArtifactResult result : results )
                {
                    if ( result.isMissing() )
                    {
                        log( "Ignoring missing attachment " + result.getRequest().getArtifact(), Project.MSG_VERBOSE );
                    }
                    else if ( !result.isResolved() )
                    {
                        throw new BuildException( "Could not resolve artifacts: " + e.getMessage(), e );
                    }
                }
            }

            group.processResults( results, session );
        }
    }

    /**
     */
    public abstract static class ArtifactConsumer
        extends ProjectComponent
    {

        private DependencyFilter filter;

        public boolean accept( org.eclipse.aether.graph.DependencyNode node, List<DependencyNode> parents )
        {
            return filter == null || filter.accept( node, parents );
        }

        public String getClassifier()
        {
            return null;
        }

        public void validate()
        {

        }

        public abstract void process( Artifact artifact, RepositorySystemSession session );

        public void setScopes( String scopes )
        {
            if ( filter != null )
            {
                throw new BuildException( "You must not specify both 'scopes' and 'classpath'" );
            }

            Collection<String> included = new HashSet<String>();
            Collection<String> excluded = new HashSet<String>();

            String[] split = scopes.split( "[, ]" );
            for ( String scope : split )
            {
                scope = scope.trim();
                Collection<String> dst;
                if ( scope.startsWith( "-" ) || scope.startsWith( "!" ) )
                {
                    dst = excluded;
                    scope = scope.substring( 1 );
                }
                else
                {
                    dst = included;
                }
                if ( scope.length() > 0 )
                {
                    dst.add( scope );
                }
            }

            filter = new ScopeDependencyFilter( included, excluded );
        }

        public void setClasspath( String classpath )
        {
            if ( "compile".equals( classpath ) )
            {
                setScopes( "provided,system,compile" );
            }
            else if ( "runtime".equals( classpath ) )
            {
                setScopes( "compile,runtime" );
            }
            else if ( "test".equals( classpath ) )
            {
                setScopes( "provided,system,compile,runtime,test" );
            }
            else
            {
                throw new BuildException( "The classpath '" + classpath + "' is not defined"
                    + ", must be one of 'compile', 'runtime' or 'test'" );
            }
        }

    }

    /**
     */
    public class Path
        extends ArtifactConsumer
    {

        private String refid;

        private org.apache.tools.ant.types.Path path;

        public void setRefId( String refId )
        {
            this.refid = refId;
        }

        public void validate()
        {
            if ( refid == null )
            {
                throw new BuildException( "You must specify the 'refid' for the path" );
            }
        }

        public void process( Artifact artifact, RepositorySystemSession session )
        {
            if ( path == null )
            {
                path = new org.apache.tools.ant.types.Path( getProject() );
                getProject().addReference( refid, path );
            }
            File file = artifact.getFile();
            path.add( new FileResource( file.getParentFile(), file.getName() ) );
        }

    }

    /**
     */
    public class Files
        extends ArtifactConsumer
    {

        private static final String DEFAULT_LAYOUT = Layout.GID_DIRS + "/" + Layout.AID + "/" + Layout.BVER + "/"
            + Layout.AID + "-" + Layout.VER + "-" + Layout.CLS + "." + Layout.EXT;

        private String refid;

        private String classifier;

        private File dir;

        private Layout layout;

        private FileSet fileset;

        private Resources resources;

        public void setRefId( String refId )
        {
            this.refid = refId;
        }

        public String getClassifier()
        {
            return classifier;
        }

        public void setAttachments( String attachments )
        {
            if ( "sources".equals( attachments ) )
            {
                classifier = "*-sources";
            }
            else if ( "javadoc".equals( attachments ) )
            {
                classifier = "*-javadoc";
            }
            else
            {
                throw new BuildException( "The attachment type '" + attachments
                    + "' is not defined, must be one of 'sources' or 'javadoc'" );
            }
        }

        public void setDir( File dir )
        {
            this.dir = dir;
            if ( dir != null && layout == null )
            {
                layout = new Layout( DEFAULT_LAYOUT );
            }
        }

        public void setLayout( String layout )
        {
            this.layout = new Layout( layout );
        }

        public void validate()
        {
            if ( refid == null && dir == null )
            {
                throw new BuildException( "You must either specify the 'refid' for the resource collection"
                    + " or a 'dir' to copy the files to" );
            }
            if ( dir == null && layout != null )
            {
                throw new BuildException( "You must not specify a 'layout' unless 'dir' is also specified" );
            }
        }

        public void process( Artifact artifact, RepositorySystemSession session )
        {
            if ( dir != null )
            {
                if ( refid != null && fileset == null )
                {
                    fileset = new FileSet();
                    fileset.setProject( getProject() );
                    fileset.setDir( dir );
                    getProject().addReference( refid, fileset );
                }

                String path = layout.getPath( artifact );

                if ( fileset != null )
                {
                    fileset.createInclude().setName( path );
                }

                File src = artifact.getFile();
                File dst = new File( dir, path );

                if ( src.lastModified() != dst.lastModified() || src.length() != dst.length() )
                {
                    try
                    {
                        Resolve.this.log( "Copy " + src + " to " + dst, Project.MSG_VERBOSE );
                        FileUtils.getFileUtils().copyFile( src, dst, null, true, true );
                    }
                    catch ( IOException e )
                    {
                        throw new BuildException( "Failed to copy artifact file " + src + " to " + dst + ": "
                            + e.getMessage(), e );
                    }
                }
                else
                {
                    Resolve.this.log( "Omit to copy " + src + " to " + dst + ", seems unchanged", Project.MSG_VERBOSE );
                }
            }
            else
            {
                if ( resources == null )
                {
                    resources = new Resources();
                    resources.setProject( getProject() );
                    getProject().addReference( refid, resources );
                }

                FileResource resource = new FileResource( artifact.getFile() );
                resource.setBaseDir( session.getLocalRepository().getBasedir() );
                resource.setProject( getProject() );
                resources.add( resource );
            }
        }

    }

    /**
     */
    public class Props
        extends ArtifactConsumer
    {

        private String prefix;

        private String classifier;

        public void setPrefix( String prefix )
        {
            this.prefix = prefix;
        }

        public String getClassifier()
        {
            return classifier;
        }

        public void setAttachments( String attachments )
        {
            if ( "sources".equals( attachments ) )
            {
                classifier = "*-sources";
            }
            else if ( "javadoc".equals( attachments ) )
            {
                classifier = "*-javadoc";
            }
            else
            {
                throw new BuildException( "The attachment type '" + attachments
                    + "' is not defined, must be one of 'sources' or 'javadoc'" );
            }
        }

        public void process( Artifact artifact, RepositorySystemSession session )
        {
            StringBuilder buffer = new StringBuilder( 256 );
            if ( prefix != null && prefix.length() > 0 )
            {
                buffer.append( prefix );
                if ( !prefix.endsWith( "." ) )
                {
                    buffer.append( '.' );
                }
            }
            buffer.append( artifact.getGroupId() );
            buffer.append( ':' );
            buffer.append( artifact.getArtifactId() );
            buffer.append( ':' );
            buffer.append( artifact.getExtension() );
            if ( artifact.getClassifier().length() > 0 )
            {
                buffer.append( ':' );
                buffer.append( artifact.getClassifier() );
            }

            String path = artifact.getFile().getAbsolutePath();

            getProject().setProperty( buffer.toString(), path );
        }

    }

    private static class Group
    {

        private String classifier;

        private List<ArtifactConsumer> consumers = new ArrayList<ArtifactConsumer>();

        private List<ArtifactRequest> requests = new ArrayList<ArtifactRequest>();

        Group( String classifier )
        {
            this.classifier = classifier;
        }

        public boolean isAttachments()
        {
            return classifier != null;
        }

        public void add( ArtifactConsumer consumer )
        {
            consumers.add( consumer );
        }

        public void createRequests( DependencyNode node )
        {
            createRequests( node, new LinkedList<DependencyNode>() );
        }

        private void createRequests( DependencyNode node, LinkedList<DependencyNode> parents )
        {
            if ( node.getDependency() != null )
            {
                for ( ArtifactConsumer consumer : consumers )
                {
                    if ( consumer.accept( node, parents ) )
                    {
                        ArtifactRequest request = new ArtifactRequest( node );
                        if ( classifier != null )
                        {
                            request.setArtifact( new SubArtifact( request.getArtifact(), classifier, "jar" ) );
                        }
                        requests.add( request );
                        break;
                    }
                }
            }

            parents.addFirst( node );

            for ( DependencyNode child : node.getChildren() )
            {
                createRequests( child, parents );
            }

            parents.removeFirst();
        }

        public List<ArtifactRequest> getRequests()
        {
            return requests;
        }

        public void processResults( List<ArtifactResult> results, RepositorySystemSession session )
        {
            for ( ArtifactResult result : results )
            {
                if ( !result.isResolved() )
                {
                    continue;
                }
                for ( ArtifactConsumer consumer : consumers )
                {
                    if ( consumer.accept( result.getRequest().getDependencyNode(),
                                          Collections.<DependencyNode>emptyList() ) )
                    {
                        consumer.process( result.getArtifact(), session );
                    }
                }
            }
        }

    }

}
