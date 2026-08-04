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

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Common base test class that should be used by assembler implementations that
 * work with files using jar packaging (war, ear, etc.).
 * 
 * This test ensures consistent behaviour across assembler implementations.
 */
public abstract class ArchiveBasedAssemblyTest extends TestCase {

    private static final Log LOG = LogFactory.getLog( ArchiveBasedAssemblyTest.class );
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    /**
     * Obtain the Assembler implementation from the subclass used
     * to assemble the archive.
     * @return the Assembler
     */
    protected abstract Assembler getAssemblerUnderTest();
    
    /**
     * Obtain the archive (war, ear) to assemble from the subclass.
     * @return the archive to assemble.
     */
    protected abstract File getFileToAssemble();
    
    public void testAssembleToNonExistantFile() throws Exception {
        File fileToAssemble = getFileToAssemble();
        Assembler underTest = getAssemblerUnderTest();        
        File destFile = File.createTempFile( "jarAssemblyTest", ".tmp" );
        destFile.delete();

        assertFalse( "Destination file [" + destFile.getAbsolutePath() + "] already exists.", 
                destFile.exists() );
        
        AssemblerConfig config = prepareConfig( fileToAssemble, destFile );
        
        LOG.debug( "Assembling [" + fileToAssemble.getAbsolutePath() + 
                "] to file [" + destFile.getAbsolutePath() + "]" );
        
        underTest.assemble( config );
        
        assertTrue( "Source archive [" + fileToAssemble.getAbsolutePath() + "] doesn't exist! " +  
                "Assembly may have deleted it by accident.", fileToAssemble.exists() );
        assertTrue( "Destination directory [" + destFile.getParentFile().getAbsolutePath() + "] does not exist, " +
                "assembly did not complete properly.", destFile.getParentFile().exists() );
        assertTrue( "Assembled war file [" + destFile + "] does not exist, " +
                "assembly did not complete properly.", destFile.exists() );
        
        destFile.delete();
    }
    
    public void testAssembleToDirectory() throws Exception {
        File fileToAssemble = getFileToAssemble();
        Assembler underTest = getAssemblerUnderTest();
        
        File tmpFile = File.createTempFile( "jarAssemblyTest", ".tmp" );
        File destDir = new File( tmpFile.getParent(), tmpFile.getName() + ".dir" );
        destDir.mkdir();
        
        assertTrue( "Destination directory [" + destDir.getAbsolutePath() + "] doesn't exist.", 
                destDir.exists() );
        
        AssemblerConfig config = prepareConfig( fileToAssemble, destDir );
        
        LOG.debug( "Assembling [" + fileToAssemble.getAbsolutePath() + 
                "] to directory [" + destDir.getAbsolutePath() + "]" );
        
        underTest.assemble( config );
        
        File destFile = new File( destDir, fileToAssemble.getName() );
        
        assertTrue( "Source archive doesn't exist!  Assembly may have deleted it by accident.", fileToAssemble.exists() );
        assertTrue( "Destination directory does not exist, assembly did not complete properly.", destDir.exists() );
        assertTrue( "Assembled war file does not exist, assembly did not complete properly.", destFile.exists() );
        
        tmpFile.delete();
        destFile.delete();
        destDir.delete();
    }
    
    public void testAssembleToExistingFile() throws Exception {
        File fileToAssemble = getFileToAssemble();
        Assembler underTest = getAssemblerUnderTest();
        
        File destFile = File.createTempFile( "jarAssemblyTest", ".tmp" );
        
        assertTrue( "Destination file [" + destFile.getAbsolutePath() + "] should already exist.", 
                destFile.exists() );
        
        AssemblerConfig config = prepareConfig( fileToAssemble, destFile );
        
        LOG.debug( "Assembling [" + fileToAssemble.getAbsolutePath() + "] to file [" + destFile.getAbsolutePath() + "]" );
        
        underTest.assemble( config );
        
        assertTrue( "Source archive doesn't exist!  Assembly may have deleted it by accident.", fileToAssemble.exists() );
        assertTrue( "Assembled war file does not exist, assembly did not complete properly.", destFile.exists() );
        
        destFile.delete();        
    }
    
    public void testAssembleToExistingFileInSubDirectory() throws Exception {
        File fileToAssemble = getFileToAssemble();
        Assembler underTest = getAssemblerUnderTest();
        
        File tmpFile = File.createTempFile( "jarAssemblyTest", ".tmp" );
        File destDir = new File( tmpFile.getName() + ".dir" );
        destDir.mkdirs();
        File destFile = new File( destDir, fileToAssemble.getName() );
        destFile.createNewFile();
        
        assertTrue( "Destination file [" + destFile.getAbsolutePath() + "] should already exist.", 
                destFile.exists() );
        
        AssemblerConfig config = prepareConfig( fileToAssemble, destFile );
        
        LOG.debug( "Assembling [" + fileToAssemble.getAbsolutePath() + "] to file [" + destFile.getAbsolutePath() + "]" );
        
        underTest.assemble( config );
        
        assertTrue( "Source archive doesn't exist!  Assembly may have deleted it by accident.", fileToAssemble.exists() );
        assertTrue( "Assembled war file does not exist, assembly did not complete properly.", destFile.exists() );
        
        destFile.delete();
        destDir.delete();
        tmpFile.delete();
    }
    
    private AssemblerConfig prepareConfig( File source, File dest ) {
        AssemblerConfig config = new AssemblerConfig();
        config.setDestination( dest );
        config.setSource( source );
        return config;
    }
    
}
