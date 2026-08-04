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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import junit.framework.TestCase;

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

/**
 * This test class directly tests the Jar streaming assembly,
 * bypassing the file assemblers (EarAssembler, WarAssembler).
 */
public class AssemblyStreamTest extends TestCase {
    
    private InputStream webXmlIn = null;
    private InputStream portletXmlIn = null;    
    private InputStream warIn = null;
    private JarStreamingAssembly jarAssemblerUnderTest = null;
    private WebXmlStreamingAssembly webXmlAssemblerUnderTest = null;
    
    private static final String webXmlToAssembleResource = "/org/apache/pluto/util/assemble/file/web.xml";
    private static final String portletXmlResource = "/org/apache/pluto/util/assemble/file/portlet.xml";
    private static final String warToAssembleResource = "/org/apache/pluto/util/assemble/war/WarDeployerTestPortlet.war";
    
    private static final String testPortletName = "WarTestPortletName";    
    
    protected void setUp() throws Exception {
        webXmlIn = getClass().getResourceAsStream( webXmlToAssembleResource );
        portletXmlIn = getClass().getResourceAsStream( portletXmlResource );
        warIn = getClass().getResourceAsStream( warToAssembleResource );
        jarAssemblerUnderTest = new JarStreamingAssembly();
        webXmlAssemblerUnderTest = new WebXmlStreamingAssembly();
    }
    
    public void testJarStreamingAssembly() throws Exception {
        File warFileOut = File.createTempFile( "streamingAssemblyWarTest", ".war" );
        JarInputStream jarIn = new JarInputStream( warIn );
        JarOutputStream warOut = new JarOutputStream( new FileOutputStream( warFileOut ) );
        jarAssemblerUnderTest.assembleStream( jarIn, warOut, Assembler.DISPATCH_SERVLET_CLASS );
        assertTrue( "Assembled WAR file was not created.", warFileOut.exists() );
        verifyAssembly( warFileOut );
        warFileOut.delete();
    }
    
    public void testWebXmlStreamingAssembly() throws Exception {
        File assembledWebXml = File.createTempFile( "streamingWebXmlTest", ".xml" );
        OutputStream assembledWebXmlOut = new FileOutputStream( assembledWebXml );
        webXmlAssemblerUnderTest.assembleStream(webXmlIn, portletXmlIn, assembledWebXmlOut, Assembler.DISPATCH_SERVLET_CLASS);
        verifyAssembly( new FileInputStream( assembledWebXml ), 
                getClass().getResourceAsStream( portletXmlResource ) );
        assembledWebXml.delete();
    }
    
    protected void verifyAssembly( InputStream webXml, InputStream portletXml ) throws Exception {
        WebAppDescriptorService webSvc = new WebAppDescriptorServiceImpl();
        PortletAppDescriptorService portletSvc = new PortletAppDescriptorServiceImpl();
        WebAppDD webApp = webSvc.read( webXml ) ;
        PortletAppDD portletApp = portletSvc.read( portletXml );
        
        assertNotNull( "Web Application Descripter was null.", webApp );
        assertNotNull( "Portlet Application Descriptor was null.", portletApp );
        assertTrue( "Portlet Application Descriptor doesn't define any portlets.", portletApp.getPortlets().size() > 0 );
        assertTrue( "Web Application Descriptor doesn't define any servlets.", webApp.getServlets().size() > 0 );
        assertTrue( "Web Application Descriptor doesn't define any servlet mappings.", webApp.getServletMappings().size() > 0 );
        
        PortletDD portlet = (PortletDD) portletApp.getPortlets().iterator().next();
        assertTrue( "Unable to retrieve test portlet named [" + testPortletName + "]", portlet.getPortletName().equals( testPortletName ) );
        
        ServletDD servlet = webApp.getServlet( testPortletName );
        assertNotNull( "Unable to retrieve portlet dispatch for portlet named [" + testPortletName + "]", servlet );        
        assertEquals( "Dispatcher servlet incorrect for test portlet [" + testPortletName + "]",  Assembler.DISPATCH_SERVLET_CLASS, servlet.getServletClass() );        
    }

    protected void verifyAssembly( File warFile ) throws Exception {
        WebAppDescriptorService webSvc = new WebAppDescriptorServiceImpl();
        PortletAppDescriptorService portletSvc = new PortletAppDescriptorServiceImpl();
        int entryCount = 0;
        ByteArrayOutputStream portletXmlBytes = new ByteArrayOutputStream();
        ByteArrayOutputStream webXmlBytes = new ByteArrayOutputStream();
        WebAppDD webApp = null;
        PortletAppDD portletApp = null;        
                
        JarInputStream assembledWarIn = new JarInputStream( new FileInputStream( warFile ) );
        JarEntry tempEntry;
        
        while ( ( tempEntry = assembledWarIn.getNextJarEntry() ) != null  ) {
            entryCount++;
            
            if ( Assembler.PORTLET_XML.equals( tempEntry.getName() ) ) {
                IOUtils.copy( assembledWarIn, portletXmlBytes );
                portletApp = portletSvc.read( new ByteArrayInputStream( portletXmlBytes.toByteArray() ) );
            }
            if ( Assembler.SERVLET_XML.equals( tempEntry.getName() ) ) {
                IOUtils.copy( assembledWarIn, webXmlBytes );
                webApp = webSvc.read( new ByteArrayInputStream( webXmlBytes.toByteArray() ) );
            }
        }
        
        assertTrue( "Assembled WAR file was empty.", entryCount > 0 );
        assertNotNull( "Web Application Descripter was null.", webApp );
        assertNotNull( "Portlet Application Descriptor was null.", portletApp );
        assertTrue( "Portlet Application Descriptor doesn't define any portlets.", portletApp.getPortlets().size() > 0 );
        assertTrue( "Web Application Descriptor doesn't define any servlets.", webApp.getServlets().size() > 0 );
        assertTrue( "Web Application Descriptor doesn't define any servlet mappings.", webApp.getServletMappings().size() > 0 );

        PortletDD portlet = (PortletDD) portletApp.getPortlets().iterator().next();
        assertTrue( "Unable to retrieve test portlet named [" + testPortletName + "]", portlet.getPortletName().equals( testPortletName ) );

        ServletDD servlet = webApp.getServlet( testPortletName );
        assertNotNull( "Unable to retrieve portlet dispatch for portlet named [" + testPortletName + "]", servlet );        
        assertEquals( "Dispatcher servlet incorrect for test portlet [" + testPortletName + "]",  Assembler.DISPATCH_SERVLET_CLASS, servlet.getServletClass() );
    }
}
