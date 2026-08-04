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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.services.PortletAppDescriptorService;
import org.apache.pluto.descriptors.services.WebAppDescriptorService;
import org.apache.pluto.descriptors.services.castor.PortletAppDescriptorServiceImpl;
import org.apache.pluto.descriptors.services.castor.WebAppDescriptorServiceImpl;
import org.apache.pluto.descriptors.servlet.ServletDD;
import org.apache.pluto.descriptors.servlet.WebAppDD;
import org.apache.pluto.util.assemble.Assembler;
import org.apache.pluto.util.assemble.AssemblerConfig;
import org.apache.pluto.util.assemble.ArchiveBasedAssemblyTest;

/**
 * This test assembles an EAR file which contains two portlet
 * web applications for assembly.  There is a third war file
 * which is not a portlet, and therefore shouldn't be assembled, 
 * just copied.
 */
public class ComplexEarAssemblerTest extends ArchiveBasedAssemblyTest {
    
    private static final String earResource = 
        "/org/apache/pluto/util/assemble/ear/ComplexEarDeployerTest.ear";
    
    private static final String[] testPortletNames;
    private static final String[] testWarEntryNames;
    private static final String[] unassembledWarEntryName;
    static {
        testPortletNames = new String[] { "WarTestPortletName", "SecondWarTestPortletName" };
        testWarEntryNames = new String[] { "WarDeployerTestPortlet.war", "SecondWarDeployerTestPortlet.war" };
        unassembledWarEntryName = new String[] { "WarWithNoPortlet.war" };
    }
    
    private File earFile = null;
    
    protected void setUp() throws Exception {
        super.setUp();
        earFile = new File( getClass().getResource( earResource ).getFile() );
    }
    
    public void testMultipleWarFileAssembly() throws Exception {
        File tmpFile = File.createTempFile( "earTest", "tmp" );
        File destDir = new File( tmpFile.getName() + ".dir" );
        destDir.mkdirs();
        
        assertTrue( "Source ear file doesn't exist! [" + earFile + "]", earFile.exists() );
        assertTrue( "Destination directory doesn't exist! [" + destDir + "]", 
                destDir.exists() && destDir.isDirectory() );
        
        AssemblerConfig config = new AssemblerConfig();
        config.setSource( earFile );
        config.setDestination( destDir );
        EarAssembler assembler = new EarAssembler();
        
        assembler.assemble( config );
        
        File destFile = new File( destDir, earFile.getName() );
        
        assertTrue( "Assembled ear file doesn't exist, assembly failed", destFile.exists() );
        
        validateEarAssembly( destFile );
        
        tmpFile.delete();
        destFile.delete();
        destDir.delete();
    }
    
    protected void validateEarAssembly( File earFile ) throws Exception {
        assertTrue( "EAR archive [" + earFile.getAbsolutePath() + "] cannot be found or cannot be read", 
                earFile.exists() && earFile.canRead() );
        
        PortletAppDescriptorService portletSvc = new PortletAppDescriptorServiceImpl();
        WebAppDescriptorService webSvc = new WebAppDescriptorServiceImpl();
        PortletAppDD portletAppDD = null;
        WebAppDD webAppDD = null;
        
        List portletWarEntries = Arrays.asList( testWarEntryNames );
        List unassembledWarEntries = Arrays.asList( unassembledWarEntryName );
        List testPortlets = Arrays.asList( testPortletNames );
        
        int earEntryCount = 0;
        int totalWarEntryCount = 0;
        int portletWarEntryCount = 0;
        
        JarInputStream earIn = new JarInputStream( new FileInputStream( earFile ) );
        
        JarEntry earEntry;
        JarEntry warEntry;
        
        
        while ( ( earEntry = earIn.getNextJarEntry() ) != null ) {            
            earEntryCount++;
            
            if ( earEntry.getName().endsWith( ".war" ) ) {
                totalWarEntryCount++;
                JarInputStream warIn = new JarInputStream( earIn );
                
                while ( ( warEntry = warIn.getNextJarEntry() ) != null ) {
                    if ( Assembler.PORTLET_XML.equals( warEntry.getName() ) ) {
                        portletAppDD = portletSvc.read( 
                                new ByteArrayInputStream( IOUtils.toByteArray( warIn ) ) );
                    }
                    if ( Assembler.SERVLET_XML.equals( warEntry.getName() ) ) {
                        webAppDD = webSvc.read( 
                                new ByteArrayInputStream( IOUtils.toByteArray( warIn ) ) );
                    }
                }
                
                if ( portletWarEntries.contains( earEntry.getName() ) ) {
                    portletWarEntryCount++;
                    assertNotNull( "WAR archive did not contain a portlet.xml", portletAppDD );
                    assertNotNull( "WAR archive did not contain a servlet.xml", webAppDD );
                    assertTrue( "WAR archive did not contain any servlets", webAppDD.getServlets().size() > 0 );
                    assertTrue( "WAR archive did not contain any servlet mappings", webAppDD.getServletMappings().size() > 0 );
                    assertTrue( "WAR archive did not contain any portlets", portletAppDD.getPortlets().size() > 0 );
                    
                    for ( Iterator iter = portletAppDD.getPortlets().iterator(); iter.hasNext(); ) {
                        PortletDD portlet = (PortletDD) iter.next();
                        if (! testPortlets.contains( portlet.getPortletName() ) ) {
                            fail( "Unexpected test portlet name encountered: [" + portlet.getPortletName() + "]" );
                        }
                        ServletDD servlet = webAppDD.getServlet( portlet.getPortletName() );
                        assertNotNull( "web.xml does not contain assembly for test portlet", servlet );
                        assertEquals( "web.xml does not contain correct dispatch servet", Assembler.DISPATCH_SERVLET_CLASS, 
                                servlet.getServletClass() );
                    } 
                    
                }
                
                webAppDD = null;
                portletAppDD = null;
                                
            }
            
        }
        
        assertTrue( "EAR archive did not contain any entries", earEntryCount > 0 );
        assertEquals( "EAR archive did not contain the expected test war entries.", portletWarEntries.size(), portletWarEntryCount );
        assertEquals( "WAR archive did not contain the correct number of entries",
                portletWarEntries.size() + unassembledWarEntries.size(), totalWarEntryCount );
                            
    }

    protected Assembler getAssemblerUnderTest() {
        return new EarAssembler();
    }

    protected File getFileToAssemble() {
        return earFile;
    }

}
