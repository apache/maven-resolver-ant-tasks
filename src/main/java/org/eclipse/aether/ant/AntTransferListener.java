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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;

/**
 */
class AntTransferListener
    extends AbstractTransferListener
{

    private Task task;

    public AntTransferListener( Task task )
    {
        this.task = task;
    }

    @Override
    public void transferInitiated( TransferEvent event )
        throws TransferCancelledException
    {
        String msg = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading";
        msg += " " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName();
        task.log( msg );
    }

    @Override
    public void transferCorrupted( TransferEvent event )
        throws TransferCancelledException
    {
        TransferResource resource = event.getResource();

        task.log( event.getException().getMessage() + " for " + resource.getRepositoryUrl()
                      + resource.getResourceName(), Project.MSG_WARN );
    }

    @Override
    public void transferSucceeded( TransferEvent event )
    {
        String msg = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded";
        msg += " " + event.getResource().getRepositoryUrl() + event.getResource().getResourceName();

        long contentLength = event.getTransferredBytes();
        if ( contentLength >= 0 )
        {
            String len = contentLength >= 1024 ? ( ( contentLength + 1023 ) / 1024 ) + " KB" : contentLength + " B";

            String throughput = "";
            long duration = System.currentTimeMillis() - event.getResource().getTransferStartTime();
            if ( duration > 0 )
            {
                DecimalFormat format = new DecimalFormat( "0.0", new DecimalFormatSymbols( Locale.ENGLISH ) );
                double kbPerSec = ( contentLength / 1024.0 ) / ( duration / 1000.0 );
                throughput = " at " + format.format( kbPerSec ) + " KB/sec";
            }

            msg += " (" + len + throughput + ")";
        }
        task.log( msg );
    }

}
