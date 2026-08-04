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
package org.apache.pluto.util.assemble;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.util.UtilityException;

/**
 * A base class that assembler implementations should extend.  
 * 
 * The purpose of this class is to ensure consistent behavior
 * when performing assembly.
 */
public abstract class AbstractArchiveAssembler extends WebXmlRewritingAssembler {

    private static final Log LOG = LogFactory.getLog( AbstractArchiveAssembler.class );
    
    /**
     * This implementation throws <code>UtilityException</code> if the source
     * archive doesn't exist or is a directory.  If the destination archive is
     * null, it assumes that in-place assembly is dessired.  It determines
     * if the source archive should be replaced with the destination archive
     * and provisions temporary files as needed.
     */
    public void assemble( AssemblerConfig config ) throws UtilityException {
        File source = config.getSource();
        File dest = config.getDestination();
        
        try {        
            
            if ( source == null || !source.exists() ) {
                throw new UtilityException( "Source archive doesn't exist." );
            }
            
            if ( source.isDirectory() ) {
                throw new UtilityException( "Source archive is a directory." );
            }

            if ( performInPlaceAssembly( config ) ) {
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug( "Performing in-place assembly of " + config.getSource().getAbsolutePath() );
                    }
                    dest = File.createTempFile( source.getName(), ".tmp" );
                    config.setDestination( dest );
                    assembleInternal( config );
                    // renameTo() is impl-specific
                    boolean success = dest.renameTo( source );
                    if (! success ) {
                        // do it the old-fashioned way
                        FileUtils.copyFile( dest, source );
                    }
            } else {
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "Performing assembly of " + config.getSource().getAbsolutePath() + " to " + 
                            config.getDestination().getAbsolutePath() );
                }
                File destFile = dest;
                
                // if the destination is a directory, ensure that parent
                // directories have been created and set the destination
                // file to the file in the direcotory.
                if ( dest.isDirectory() ) {                                
                    dest.mkdirs();
                    destFile = new File( dest, source.getName() );
                }
                
                config.setDestination( destFile );
                assembleInternal( config );
            }
            
        } catch ( IOException e ) {
            LOG.error( "Assembly failed: "+ e.getMessage() );
            throw new UtilityException( e.getMessage(), e );
        }        
    }
    
    /**
     * Performs a series of checks to determine whether or not the assembly should 
     * occur "in-place" - that is, if the source archive will be assembled and then
     * replaced by the destination archive.
     * <p/>
     * <ul>
     *  <li>If the destination is null, then perform in place assembly.</li>
     *  <li>If the destination is equal to the source, then perform in place assembly.</li>
     * </ul>
     * @param config the AssemblerConfig
     * @return true if in-place configuration should occur
     */    
    protected boolean performInPlaceAssembly( final AssemblerConfig config ) {
        // If the destination wasn't provided, in-place assembly is inferred
        if ( config.getDestination() == null ) {
            return true;
        }
        
        // If the source and the destination are the same, in-place
        // assembly has been explicitly requested by the caller
        if ( config.getDestination().equals( config.getSource() ) ) {
            return true;
        }
        
        // If the destination is the parent directory of the source, then
        // in-place assembly is inferred.
        if ( config.getDestination().equals( config.getSource().getParentFile() ) ) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Assemble the source file to the destination file.  The superclass is responsible for ensuring
     * correct and not-null values for the source and destination, and for temporary file handling 
     * used during in-place assembly.
     * 
     * @param config the assembler configuration object
     * @throws UtilityException
     * @throws IOException
     */
    protected abstract void assembleInternal( AssemblerConfig config ) 
        throws UtilityException, IOException;

}
