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
package org.apache.pluto.util.assemble.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.util.assemble.Assembler;

/** 
 * Utility class responsible for accepting a JarInputStream representing a web application archive,
 * iterating over each JarEntry in the input stream and assembling the WAR web.xml for portlet
 * deployment.
 */
public class JarStreamingAssembly {
    
    private static final Log LOG = LogFactory.getLog( JarStreamingAssembly.class );

    /**
     * Reads the source JarInputStream, copying entries to the destination JarOutputStream. 
     * The web.xml and portlet.xml are cached, and after the entire archive is copied 
     * (minus the web.xml) a re-written web.xml is generated and written to the 
     * destination JAR.
     * 
     * @param source the WAR source input stream
     * @param dest the WAR destination output stream
     * @param dispatchServletClass the name of the dispatch class
     * @throws IOException
     */
    public static void assembleStream(JarInputStream source, JarOutputStream dest, String dispatchServletClass) throws IOException {
        
        try {
            //Need to buffer the web.xml and portlet.xml files for the rewritting
            JarEntry servletXmlEntry = null;
            byte[] servletXmlBuffer = null;
            byte[] portletXmlBuffer = null;

            JarEntry originalJarEntry;                         
            
            //Read the source archive entry by entry
            while ((originalJarEntry = source.getNextJarEntry()) != null) {

                final JarEntry newJarEntry = smartClone(originalJarEntry);
                originalJarEntry = null;

                //Capture the web.xml JarEntry and contents as a byte[], don't write it out now or
                //update the CRC or length of the destEntry.
                if (Assembler.SERVLET_XML.equals(newJarEntry.getName())) {
                    servletXmlEntry = newJarEntry;
                    servletXmlBuffer = IOUtils.toByteArray(source);
                }
                
                //Capture the portlet.xml contents as a byte[]
                else if (Assembler.PORTLET_XML.equals(newJarEntry.getName())) {
                    portletXmlBuffer = IOUtils.toByteArray(source);
                    dest.putNextEntry(newJarEntry);
                    IOUtils.write(portletXmlBuffer, dest);
                }
                
                //Copy all other entries directly to the output archive
                else {
                    dest.putNextEntry(newJarEntry);
                    IOUtils.copy(source, dest);                    
                }      
                    
                dest.closeEntry();
                dest.flush();

            }

            // If no portlet.xml was found in the archive, skip the assembly step.
            if (portletXmlBuffer != null) {
                // container for assembled web.xml bytes
                final byte[] webXmlBytes;
                
                // Checks to make sure the web.xml was found in the archive
                if (servletXmlBuffer == null) {
                    throw new FileNotFoundException("File '" + Assembler.SERVLET_XML + "' could not be found in the source input stream.");
                }

                //Create streams of the byte[] data for the updater method
                final InputStream webXmlIn = new ByteArrayInputStream(servletXmlBuffer);
                final InputStream portletXmlIn = new ByteArrayInputStream(portletXmlBuffer);
                final ByteArrayOutputStream webXmlOut = new ByteArrayOutputStream(servletXmlBuffer.length);

                //Update the web.xml
                WebXmlStreamingAssembly.assembleStream(webXmlIn, portletXmlIn, webXmlOut, dispatchServletClass);
                IOUtils.copy( webXmlIn, webXmlOut );
                webXmlBytes = webXmlOut.toByteArray();
                
                //If no compression is being used (STORED) we have to manually update the size and crc
                if (servletXmlEntry.getMethod() == ZipEntry.STORED) {
                    servletXmlEntry.setSize(webXmlBytes.length);
                    final CRC32 webXmlCrc = new CRC32();
                    webXmlCrc.update(webXmlBytes);
                    servletXmlEntry.setCrc(webXmlCrc.getValue());
                }

                //write out the assembled web.xml entry and contents
                dest.putNextEntry(servletXmlEntry);
                IOUtils.write(webXmlBytes, dest);
                
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "Jar stream " + source + " successfully assembled." );
                }
            } else {                
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "No portlet XML file was found, assembly was not required." );
                }                

                //copy the original, unmodified web.xml entry to the destination
                dest.putNextEntry(servletXmlEntry);
                IOUtils.write(servletXmlBuffer, dest);
                
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "Jar stream " + source + " successfully assembled." );
                }
            }            
            
        } finally {
            
            dest.flush();
            dest.close();
            
        }
    }
    
    private static JarEntry smartClone(JarEntry originalJarEntry) {
        final JarEntry newJarEntry = new JarEntry(originalJarEntry.getName());
        newJarEntry.setComment(originalJarEntry.getComment());
        newJarEntry.setExtra(originalJarEntry.getExtra());
        newJarEntry.setMethod(originalJarEntry.getMethod());
        newJarEntry.setTime(originalJarEntry.getTime());

        //Must set size and CRC for STORED entries
        if (newJarEntry.getMethod() == ZipEntry.STORED) {
            newJarEntry.setSize(originalJarEntry.getSize());
            newJarEntry.setCrc(originalJarEntry.getCrc());
        }

        return newJarEntry;
    }

}
