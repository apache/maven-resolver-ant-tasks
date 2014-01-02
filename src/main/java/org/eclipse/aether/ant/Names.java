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
package org.eclipse.aether.ant;

public final class Names
{

    private Names()
    {
        // hide constructor
    }

    public static final String ID = "aether";

    public static final String ID_DEFAULT_REPOS = ID + ".repositories";

    public static final String ID_DEFAULT_POM = ID + ".pom";

    public static final String ID_CENTRAL = "central";

    public static final String PROPERTY_OFFLINE = "aether.offline";

    public static final String SETTINGS_XML = "settings.xml";

}
