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
package org.sakaiproject.pluto.core;

import java.util.Map;

import javax.portlet.PortalContext;
import javax.portlet.PortletContext;
import javax.portlet.PortletRequest;
import jakarta.servlet.http.HttpServletRequest;

import org.sakaiproject.pluto.OptionalContainerServices;
import org.sakaiproject.pluto.PortletContainer;
import org.sakaiproject.pluto.RequiredContainerServices;
import org.sakaiproject.pluto.internal.InternalPortletWindow;
import org.sakaiproject.pluto.spi.optional.UserInfoService;
import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DefaultRequestAttributeServiceTest extends MockObjectTestCase {
    private Mock mockOptionalServices = null;
    private Mock mockUserInfoService = null;
    private Mock mockPortletRequest = null;

    private InternalPortletWindow window = null;

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp( ) throws Exception
    {
        super.setUp();

        // Create mocks
        mockOptionalServices = mock( OptionalContainerServices.class );
        mockUserInfoService = mock( UserInfoService.class );
        window = (InternalPortletWindow) mock( InternalPortletWindow.class ).proxy();
        mockPortletRequest = mock( PortletRequest.class );
    }
    
    /**
     * Test for PLUTO-477
     */
    public void testUnAuthenticatedCreateUserInfoMap() throws Exception {
        this.mockUserInfoService.expects(once()).method("getUserInfo").will(returnValue(null));
        
        this.mockOptionalServices.expects(once()).method("getUserInfoService").will(returnValue(this.mockUserInfoService.proxy()));
        
        final DefaultRequestAttributeService requestAttributeService = new DefaultRequestAttributeService();
        requestAttributeService.setOptionalContainerServices((OptionalContainerServices)this.mockOptionalServices.proxy());

        final Map userInfoMap = requestAttributeService.createUserInfoMap((PortletRequest)this.mockPortletRequest.proxy(), this.window);
        assertNull(userInfoMap);
    }
}
