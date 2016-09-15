package org.apache.maven.resolver.internal.ant.types;

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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.Reference;

/**
 */
public class Dependency
    extends DataType
    implements DependencyContainer
{

    private String groupId;

    private String artifactId;

    private String version;

    private String classifier;

    private String type;

    private String scope;

    private File systemPath;

    private List<Exclusion> exclusions = new ArrayList<Exclusion>();

    protected Dependency getRef()
    {
        return (Dependency) getCheckedRef();
    }

    public void validate( Task task )
    {
        if ( isReference() )
        {
            getRef().validate( task );
        }
        else
        {
            if ( groupId == null || groupId.length() <= 0 )
            {
                throw new BuildException( "You must specify the 'groupId' for a dependency" );
            }
            if ( artifactId == null || artifactId.length() <= 0 )
            {
                throw new BuildException( "You must specify the 'artifactId' for a dependency" );
            }
            if ( version == null || version.length() <= 0 )
            {
                throw new BuildException( "You must specify the 'version' for a dependency" );
            }

            if ( "system".equals( scope ) )
            {
                if ( systemPath == null )
                {
                    throw new BuildException( "You must specify 'systemPath' for dependencies with scope=system" );
                }
            }
            else if ( systemPath != null )
            {
                throw new BuildException( "You may only specify 'systemPath' for dependencies with scope=system" );
            }

            if ( scope != null && !"compile".equals( scope ) && !"provided".equals( scope ) && !"system".equals( scope )
                && !"runtime".equals( scope ) && !"test".equals( scope ) )
            {
                task.log( "Unknown scope '" + scope + "' for dependency", Project.MSG_WARN );
            }

            for ( Exclusion exclusion : exclusions )
            {
                exclusion.validate( task );
            }
        }
    }

    public void setRefid( Reference ref )
    {
        if ( groupId != null || artifactId != null || type != null || classifier != null || version != null
            || scope != null || systemPath != null )
        {
            throw tooManyAttributes();
        }
        if ( !exclusions.isEmpty() )
        {
            throw noChildrenAllowed();
        }
        super.setRefid( ref );
    }

    public String getGroupId()
    {
        if ( isReference() )
        {
            return getRef().getGroupId();
        }
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        checkAttributesAllowed();
        if ( this.groupId != null )
        {
            throw ambiguousCoords();
        }
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        if ( isReference() )
        {
            return getRef().getArtifactId();
        }
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        checkAttributesAllowed();
        if ( this.artifactId != null )
        {
            throw ambiguousCoords();
        }
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        if ( isReference() )
        {
            return getRef().getVersion();
        }
        return version;
    }

    public void setVersion( String version )
    {
        checkAttributesAllowed();
        if ( this.version != null )
        {
            throw ambiguousCoords();
        }
        this.version = version;
    }

    public String getClassifier()
    {
        if ( isReference() )
        {
            return getRef().getClassifier();
        }
        return classifier;
    }

    public void setClassifier( String classifier )
    {
        checkAttributesAllowed();
        if ( this.classifier != null )
        {
            throw ambiguousCoords();
        }
        this.classifier = classifier;
    }

    public String getType()
    {
        if ( isReference() )
        {
            return getRef().getType();
        }
        return ( type != null ) ? type : "jar";
    }

    public void setType( String type )
    {
        checkAttributesAllowed();
        if ( this.type != null )
        {
            throw ambiguousCoords();
        }
        this.type = type;
    }

    public String getScope()
    {
        if ( isReference() )
        {
            return getRef().getScope();
        }
        return ( scope != null ) ? scope : "compile";
    }

    public void setScope( String scope )
    {
        checkAttributesAllowed();
        if ( this.scope != null )
        {
            throw ambiguousCoords();
        }
        this.scope = scope;
    }

    public void setCoords( String coords )
    {
        checkAttributesAllowed();
        if ( groupId != null || artifactId != null || version != null || type != null || classifier != null
            || scope != null )
        {
            throw ambiguousCoords();
        }
        Pattern p = Pattern.compile( "([^: ]+):([^: ]+):([^: ]+)((:([^: ]+)(:([^: ]+))?)?:([^: ]+))?" );
        Matcher m = p.matcher( coords );
        if ( !m.matches() )
        {
            throw new BuildException( "Bad dependency coordinates '" + coords
                + "', expected format is <groupId>:<artifactId>:<version>[[:<type>[:<classifier>]]:<scope>]" );
        }
        groupId = m.group( 1 );
        artifactId = m.group( 2 );
        version = m.group( 3 );
        type = m.group( 6 );
        if ( type == null || type.length() <= 0 )
        {
            type = "jar";
        }
        classifier = m.group( 8 );
        if ( classifier == null )
        {
            classifier = "";
        }
        scope = m.group( 9 );
    }

    public void setSystemPath( File systemPath )
    {
        checkAttributesAllowed();
        this.systemPath = systemPath;
    }

    public File getSystemPath()
    {
        if ( isReference() )
        {
            return getRef().getSystemPath();
        }
        return systemPath;
    }

    public String getVersionlessKey()
    {
        if ( isReference() )
        {
            return getRef().getVersionlessKey();
        }
        StringBuilder key = new StringBuilder( 128 );
        if ( groupId != null )
        {
            key.append( groupId );
        }
        key.append( ':' );
        if ( artifactId != null )
        {
            key.append( artifactId );
        }
        key.append( ':' );
        key.append( ( type != null ) ? type : "jar" );
        if ( classifier != null && classifier.length() > 0 )
        {
            key.append( ':' );
            key.append( classifier );
        }
        return key.toString();
    }

    public void addExclusion( Exclusion exclusion )
    {
        checkChildrenAllowed();
        this.exclusions.add( exclusion );
    }

    public List<Exclusion> getExclusions()
    {
        if ( isReference() )
        {
            return getRef().getExclusions();
        }
        return exclusions;
    }

    private BuildException ambiguousCoords()
    {
        return new BuildException( "You must not specify both 'coords' and "
            + "('groupId', 'artifactId', 'version', 'extension', 'classifier', 'scope')" );
    }

}
