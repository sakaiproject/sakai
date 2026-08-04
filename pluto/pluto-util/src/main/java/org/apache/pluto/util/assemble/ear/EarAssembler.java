/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pluto.util.assemble.ear;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.util.UtilityException;
import org.apache.pluto.util.assemble.AbstractArchiveAssembler;
import org.apache.pluto.util.assemble.AssemblerConfig;
import org.apache.pluto.util.assemble.io.JarStreamingAssembly;

/**
 * Assembles war files contained inside of an EAR.  War files that do
 * not contain a portlet.xml are not assembled.  All files are copied
 * into the destination archive.
 */
public class EarAssembler extends AbstractArchiveAssembler {

    private static final Log LOG = LogFactory.getLog( EarAssembler.class );
    private static final int BUFLEN = 1024 * 8; // 8kb
    
    public void assembleInternal( AssemblerConfig config ) throws UtilityException, IOException {

        File source = config.getSource();
        File dest = config.getDestination();

        JarInputStream earIn = new JarInputStream( new FileInputStream( source ) );
        JarOutputStream earOut = new JarOutputStream(
                new BufferedOutputStream( new FileOutputStream( dest ), BUFLEN ) );
        
        try {
            
            JarEntry entry;
            
            // Iterate over entries in the EAR archive
            while ( ( entry = earIn.getNextJarEntry() ) != null ) {
                
                // If a war file is encountered, assemble it into a
                // ByteArrayOutputStream and write the assembled bytes
                // back to the EAR archive.
                if ( entry.getName().toLowerCase().endsWith( ".war" ) ) {                                        
                    
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug( "Assembling war file " + entry.getName() );
                    }
                    
                    // keep a handle to the AssemblySink so we can write out
                    // JarEntry metadata and the bytes later.
                    AssemblySink warBytesOut = getAssemblySink( config, entry );
                    JarOutputStream warOut = new JarOutputStream( warBytesOut );
                    
                    JarStreamingAssembly.assembleStream( new JarInputStream( earIn ), warOut,
                            config.getDispatchServletClass() );
                    
                    JarEntry warEntry = new JarEntry( entry );
                    
                    // Write out the assembled JarEntry metadata
                    warEntry.setSize( warBytesOut.getByteCount() );
                    warEntry.setCrc( warBytesOut.getCrc() );
                    warEntry.setCompressedSize( -1 );                    
                    earOut.putNextEntry( warEntry );

                    // Write out the assembled WAR file to the EAR
                    warBytesOut.writeTo( earOut );

                    earOut.flush();
                    earOut.closeEntry();
                    earIn.closeEntry();
                    
                } else {
                    
                    earOut.putNextEntry( entry );
                    IOUtils.copy( earIn, earOut );
                    
                    earOut.flush();
                    earOut.closeEntry();
                    earIn.closeEntry();
                    
                }
            }            
            
        } finally {
            
            earOut.close();
            earIn.close();
            
        }
    }
    
    /**
     * Obtain a sink used as a temporary container for assembled war bytes.  By default a
     * filesystem based sink is used.
     * 
     * @param config the AssemblerConfig
     * @param entry the JarEntry
     * @return the AssemblySink
     * @throws IOException
     */
    protected AssemblySink getAssemblySink( AssemblerConfig config, JarEntry entry ) throws IOException {
        File f = File.createTempFile( "earAssemblySink", "tmp" );
        f.deleteOnExit();
        return getFileAssemblySink( entry, f );
    }
    
    private AssemblySink getByteArrayAssemblySink( JarEntry entry ) {
        // Create a buffer the size of the warfile, plus a little extra, to 
        // account for the additional bytes added to web.xml as a result of
        // assembly.
        
        ByteArrayAssemblySink warBytesOut = null;
        int defaultBuflen = 1024 * 1024 * 10; // 10Mb
        int assemblyBuflen = 1024 * 32; // 32kb additional bytes for assembly
        
        
        // ByteArrayOutputStream grows the buffer by a left bitshift each time
        // its internal buffer would overflow.  The goal is to prevent the
        // buffer from overflowing, otherwise the exponential growth of
        // the internal buffer can cause OOM errors.
        
        // note that we can only optimize the buffer for file sizes less than 
        // Integer.MAX_VALUE - assemblyBuf
        if ( entry.getSize() > ( Integer.MAX_VALUE - assemblyBuflen ) || entry.getSize() < 1 ) {
            warBytesOut = new ByteArrayAssemblySink( new ByteArrayOutputStream( defaultBuflen ) );
        } else {
            int buflen = (int) entry.getSize() + assemblyBuflen;
            warBytesOut = new ByteArrayAssemblySink( new ByteArrayOutputStream( buflen ) );
        }
        
        return warBytesOut;
    }
    
    private AssemblySink getFileAssemblySink( JarEntry e, File f ) {
        AssemblySink sink = null;    
        try {
            sink = new FileAssemblySink( f );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return sink;
    }

}
