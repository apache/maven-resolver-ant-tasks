/*******************************************************************************
 * Copyright (c) 2010, 2014 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.aether.internal.ant.tasks;

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
 */
class Layout
{

    public static final String GID = "{groupId}";

    public static final String GID_DIRS = "{groupIdDirs}";

    public static final String AID = "{artifactId}";

    public static final String VER = "{version}";

    public static final String BVER = "{baseVersion}";

    public static final String EXT = "{extension}";

    public static final String CLS = "{classifier}";

    private String[] tokens;

    public Layout( String layout )
        throws BuildException
    {
        Collection<String> valid = new HashSet<String>( Arrays.asList( GID, GID_DIRS, AID, VER, BVER, EXT, CLS ) );
        List<String> tokens = new ArrayList<String>();
        Matcher m = Pattern.compile( "(\\{[^}]*\\})|([^{]+)" ).matcher( layout );
        while ( m.find() )
        {
            if ( m.group( 1 ) != null && !valid.contains( m.group( 1 ) ) )
            {
                throw new BuildException( "Invalid variable '" + m.group() + "' in layout, supported variables are "
                    + new TreeSet<String>( valid ) );
            }
            tokens.add( m.group() );
        }
        this.tokens = tokens.toArray( new String[tokens.size()] );
    }

    public String getPath( Artifact artifact )
    {
        StringBuilder buffer = new StringBuilder( 128 );

        for ( int i = 0; i < tokens.length; i++ )
        {
            String token = tokens[i];
            if ( GID.equals( token ) )
            {
                buffer.append( artifact.getGroupId() );
            }
            else if ( GID_DIRS.equals( token ) )
            {
                buffer.append( artifact.getGroupId().replace( '.', '/' ) );
            }
            else if ( AID.equals( token ) )
            {
                buffer.append( artifact.getArtifactId() );
            }
            else if ( VER.equals( token ) )
            {
                buffer.append( artifact.getVersion() );
            }
            else if ( BVER.equals( token ) )
            {
                buffer.append( artifact.getBaseVersion() );
            }
            else if ( CLS.equals( token ) )
            {
                if ( artifact.getClassifier().length() <= 0 )
                {
                    if ( i > 0 )
                    {
                        String lt = tokens[i - 1];
                        if ( lt.length() > 0 && "-_".indexOf( lt.charAt( lt.length() - 1 ) ) >= 0 )
                        {
                            buffer.setLength( buffer.length() - 1 );
                        }
                    }
                }
                else
                {
                    buffer.append( artifact.getClassifier() );
                }
            }
            else if ( EXT.equals( token ) )
            {
                buffer.append( artifact.getExtension() );
            }
            else
            {
                buffer.append( token );
            }
        }

        return buffer.toString();
    }

}
