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

import javax.portlet.PortalContext;
import javax.portlet.PortletContext;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.pluto.OptionalContainerServices;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.RequiredContainerServices;
import org.apache.pluto.core.PortletContainerImpl;
import org.apache.pluto.internal.InternalPortletRequest;
import org.apache.pluto.internal.InternalPortletWindow;
import org.apache.pluto.spi.optional.PortletEnvironmentService;
import org.apache.pluto.spi.optional.UserInfoService;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

/**
 * Created by IntelliJ IDEA.
 * User: esm
 * Date: Mar 5, 2008
 * Time: 9:50:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class PortletRequestImplTest extends MockObjectTestCase
{
    // Mock Objects
    private Mock mockContainer = null;
    private Mock mockServices = null;
    private Mock mockOptionalServices = null;
    private Mock mockPortalContext = null;
    private Mock mockPortletContext = null;
    private Mock mockHttpServletRequest = null;

    private InternalPortletWindow window = null;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp( ) throws Exception
    {
        super.setUp();

        // Create mocks
        mockServices = mock( RequiredContainerServices.class );
        mockOptionalServices = mock( OptionalContainerServices.class );
        mockPortalContext = mock( PortalContext.class );
        mockPortletContext = mock( PortletContext.class );
        mockContainer = mock( PortletContainerImpl.class,
                new Class[] { String.class, RequiredContainerServices.class, OptionalContainerServices.class },
                new Object[] { "Mock Pluto Container", (RequiredContainerServices) mockServices.proxy(), (OptionalContainerServices) mockOptionalServices.proxy() } );
        window = (InternalPortletWindow) mock( InternalPortletWindow.class ).proxy();
        mockHttpServletRequest = mock( HttpServletRequest.class );

        // Constructor expectations for RenderRequestImpl
        mockContainer.expects( once() ).method( "getRequiredContainerServices" ).will( returnValue( mockServices.proxy() ) );
        mockServices.expects( once() ).method( "getPortalContext" ).will( returnValue( mockPortalContext.proxy() ) );
    }

    /**
     * Test for PLUTO-474.
     */
    public void testInvalidateSessionWithUnititializedLastAccessTime() throws Exception
    {
        // maximum inactive interval of the underlying PortletRequest's HttpSession
        int maxInactiveInterval = 5; // in seconds

        // last accessed time of the underlying PortletRequest's HttpSession
        // A 'lastAccessedTime' of 0 emulates the behavior
        // of a servlet container that doesn't initialize
        // its value.
        long lastAccessedTime = 0L;  // in milliseconds
        
        Mock mockPortletEnvironmentService = mock( PortletEnvironmentService.class );
        
        mockOptionalServices.expects( once() ).method( "getPortletEnvironmentService" ).will( returnValue( mockPortletEnvironmentService.proxy() ));
        
        mockContainer.expects( once() ).method( "getOptionalContainerServices" ).will( returnValue( mockOptionalServices.proxy() ));

        // Create the render request that is under test, and initialize its state
        RenderRequestImpl request = new RenderRequestImpl( (PortletContainer)mockContainer.proxy(), window, (HttpServletRequest)mockHttpServletRequest.proxy() );
        request.init( (PortletContext)mockPortletContext.proxy(), ( HttpServletRequest)mockHttpServletRequest.proxy() );

        // Mock the HttpSession, and set its expectations: it will return 0 for the last accessed time, and 5
        // for the maximum inactive interval
        Mock mockHttpSession = mock( HttpSession.class );
        mockHttpSession.expects( once() ).method( "getLastAccessedTime" ).will( returnValue( lastAccessedTime ) );
        // Prior to applying PLUTO-474, this expectation is invoked exactly twice, not once
        mockHttpSession.expects( once() ).method( "getMaxInactiveInterval" ).will( returnValue( maxInactiveInterval ) );

        // Set the expectation for the servlet request - it will return the mock http session
        // Prior to applying PLUTO-474, this expectation is invoked exactly twice, not once
        mockHttpServletRequest.expects( once() ).method( "getSession" ).will( returnValue( mockHttpSession.proxy() ) );

        // this is the important expectation -
        // Prior to applying PLUTO-474, the HttpSession was
        // incorrectly determined to be invalid, and thus the
        // HttpSession's invalidate() method was invoked.
        //
        // After applying PLUTO-474, invalidate() should never be called
        mockHttpSession.expects( never() ).method( "invalidate" );
        
        Mock mockPortletSession = mock( PortletSession.class );
        mockPortletEnvironmentService.expects( once() ).method( "createPortletSession" ).will( returnValue( mockPortletSession.proxy() ));
        
        PortletSession s = request.getPortletSession( true );
    }
    
    private static class TestPortletRequestImpl extends PortletRequestImpl {
        public TestPortletRequestImpl(InternalPortletRequest internalPortletRequest) {
            super(internalPortletRequest);
        }

        public TestPortletRequestImpl(PortletContainer container, InternalPortletWindow internalPortletWindow, HttpServletRequest servletRequest) {
            super(container, internalPortletWindow, servletRequest);
        }

        public PortletPreferences getPreferences() {
            return null;
        }
    }
}
