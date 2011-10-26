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

import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;

/**
 */
class AntSecDispatcher
    extends DefaultSecDispatcher
{

    public AntSecDispatcher()
    {
        _configurationFile = "~/.m2/settings-security.xml";
        try
        {
            _cipher = new DefaultPlexusCipher();
        }
        catch ( PlexusCipherException e )
        {
            e.printStackTrace();
        }
    }

}
