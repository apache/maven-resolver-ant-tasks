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
 * Logs up- and downloads.
 */
class AntTransferListener
    extends AbstractTransferListener
{

    private Task task;

    AntTransferListener( Task task )
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
