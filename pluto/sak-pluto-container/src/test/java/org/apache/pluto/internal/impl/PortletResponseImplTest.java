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
package org.apache.pluto.internal.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.RequiredContainerServices;
import org.apache.pluto.core.PortletContainerImpl;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.spi.PortalCallbackService;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * The purpose of this test fixture is to ensure that the PortletResponseImpl correctly
 * determines what is and is not considered an absolute URL, so that the underlying 
 * ResourceURLProvider can provision a valid url according to the contract of
 * ResourceURLProvider.toString().
 * <p/>
 * The example prompting this test are WSRP re-write URLs.  See WSRP 1.0 section 10.2
 * and 10.2.1.
 */
public class PortletResponseImplTest extends MockObjectTestCase
{

    // Expected URLs used for the unit tests.
    private final String wsrpUrl = "wsrp_rewrite?wsrp-urlType=render&amp;wsrp-navigationalState=rO0ABXNyA&amp;wsrp-secureURL=false/wsrp_rewrite";
    private final String httpUrl = "http://portals.apache.org/index.html";
    private final String httpsUrl = "https://portals.apache.org/index.html";
    private final String fileUrl = "file:///absolute/path/to/a/local/resource";
    private final String absPath = "/path/to/an/image.jpg";
    private final String path = "path/to/an/image.jpg";
    
    
    // Mock Objects    
    private Mock mockContainer = null;
    private Mock mockPortalCallback = null;
    private Mock mockServices = null;
    
    private PortletResponseImpl response = null;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp( ) throws Exception
    {
        super.setUp();
        
        // Create mocks
        mockPortalCallback = mock( PortalCallbackService.class );
        mockServices = mock( RequiredContainerServices.class );        
        mockContainer = mock( PortletContainerImpl.class, 
                new Class[] { String.class, RequiredContainerServices.class, OptionalContainerServices.class },
                new Object[] { "Mock Pluto Container", (RequiredContainerServices) mockServices.proxy(), null } );
        InternalPortletWindow window = (InternalPortletWindow) mock( InternalPortletWindow.class ).proxy();
        HttpServletRequest req = (HttpServletRequest) mock( HttpServletRequest.class ).proxy();
        
        // Create Spring mock objects (we need functional HttpServletResponse for encodeURL)
        HttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        
        // Create our RenderResponseImpl object
        response = new RenderResponseImpl( (PortletContainer)mockContainer.proxy(), window, req, mockHttpServletResponse );        
    }        

    public void testEncodeWsrpUrlString( )
    {
        final String expected = wsrpUrl;
 
        // Set expectations and returns.
        mockServices.expects( once() ).method( "getPortalCallbackService" ).will( returnValue( mockPortalCallback.proxy() ) );
        mockContainer.expects( once() ).method( "getRequiredContainerServices" ).will( returnValue( mockServices.proxy() ) );
        mockPortalCallback.expects( once() ).method( "getResourceURLProvider" ).will( returnValue( new MockResourceUrlProviderImpl() ) );
       
        // Execute the test
        assertEquals( "WSRP URL was modified unexpectedly.", expected, response.encodeURL( wsrpUrl ) );         
    }
    
    public void testEncodeHttpUrlString( )
    {
        final String expected = httpUrl;
        
        // Set expectations and returns.
        mockServices.expects( once() ).method( "getPortalCallbackService" ).will( returnValue( mockPortalCallback.proxy() ) );
        mockContainer.expects( once() ).method( "getRequiredContainerServices" ).will( returnValue( mockServices.proxy() ) );
        mockPortalCallback.expects( once() ).method( "getResourceURLProvider" ).will( returnValue( new MockResourceUrlProviderImpl() ) );
        
        // Execute the test
        assertEquals( "http:// URL was modified unexpectedly", expected, response.encodeURL( httpUrl ) );         
    }
    
    public void testEncodeHttpsUrlString( )
    {
        final String expected = httpsUrl;
        
        // Set expectations and returns.
        mockServices.expects( once() ).method( "getPortalCallbackService" ).will( returnValue( mockPortalCallback.proxy() ) );
        mockContainer.expects( once() ).method( "getRequiredContainerServices" ).will( returnValue( mockServices.proxy() ) );
        mockPortalCallback.expects( once() ).method( "getResourceURLProvider" ).will( returnValue( new MockResourceUrlProviderImpl() ) );
        
        // Execute the test
        assertEquals( "https:// URL was modified unexpectedly", expected, response.encodeURL( httpsUrl ) );         
    }
    
    public void testEncodeFileUrlString( )
    {
        final String expected = fileUrl;
        
        // Set expectations and returns.
        mockServices.expects( once() ).method( "getPortalCallbackService" ).will( returnValue( mockPortalCallback.proxy() ) );
        mockContainer.expects( once() ).method( "getRequiredContainerServices" ).will( returnValue( mockServices.proxy() ) );
        mockPortalCallback.expects( once() ).method( "getResourceURLProvider" ).will( returnValue( new MockResourceUrlProviderImpl() ) );
        
        // Execute the test
        assertEquals( "https:// URL was modified unexpectedly", expected, response.encodeURL( fileUrl ) );         
    }

    public void testIsWsrpRewriteUrlAbsolute( )
    {
        // WSRP rewrite URLs should be considered absolute.  ResourceURL
        // providers should not manipulate them, because WSRP consumers
        // expect to see 'wsrp-rewrite?' at the beginning of a URL string,
        // indicating to the WSRP consumer that the URL should be re-written
        // by the consumer.
        final boolean expected = true;        
        assertEquals( "WSRP re-write URLs should be considered absolute.", expected, response.isAbsolute( wsrpUrl ) );        
    }
    
    public void testIsHttpUrlAbsolute( )
    {
        // URLs beginning with 'http://' should be considered absolute.
        final boolean expected = true;
        assertEquals( "URLs beginning with 'http://' should be considered absolute.", expected, response.isAbsolute( httpUrl ) );
    }
    
    public void testIsHttpsUrlAbsolute( )
    {
        // URLs beginning with 'https://' should be considered absolute.
        final boolean expected = true;      
        assertEquals( "URLs beginning with 'https://' should be considered absolute.", expected, response.isAbsolute( httpsUrl ) );
    }
    
    public void testIsFileUrlAbsolute( )
    {
        // URLs beginning with 'file:///' should be considered absolute.
        final boolean expected = true;  
        assertEquals( "URLs beginning with 'file:///' should be considered absolute.", expected, response.isAbsolute( fileUrl ) );
    }
    
    public void testIsPathStartsWithForwardSlashAbsolute( )
    {
        // Paths starting with forward slash should not be considered absolute, since
        // ResourceURLProvider implementations should prepend a scheme and host to the
        // path.
        final boolean expected = false;
        assertEquals( "Paths beginning with a '/' should not be considered absolute.", expected, response.isAbsolute( absPath ) );
    }
    
    public void testIsPathAbsolute( )
    {
        // Paths that do not contain a forward slash should not be considered absolute, since
        // ResourceURLProvider implementations should prepend a scheme and host to the
        // path.
        final boolean expected = false;        
        assertEquals( "Paths should not be considered absolute.", expected, response.isAbsolute( path ) );
    }
    
    public void testIsNullPathAbsolute( )
    {
        // Null paths should not be considered absolute
        final boolean expected = false;       
        assertEquals( "Paths should not be considered absolute.", expected, response.isAbsolute( null ) );
    }
    
    public void testIsEmptyPathAbsolute( )
    {
        // Empty paths should not be considered absolute
        final boolean expected = false;       
        assertEquals( "Paths should not be considered absolute.", expected, response.isAbsolute( "" ) );
    }

}
