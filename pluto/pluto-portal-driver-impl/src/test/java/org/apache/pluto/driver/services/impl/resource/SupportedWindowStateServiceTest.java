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
package org.apache.pluto.driver.services.impl.resource;

import javax.portlet.WindowState;
import javax.servlet.ServletContext;

import junit.framework.TestCase;

import org.apache.pluto.driver.config.DriverConfigurationException;
import org.apache.pluto.driver.services.portal.PropertyConfigService;
import org.apache.pluto.driver.services.portal.SupportedWindowStateService;

public class SupportedWindowStateServiceTest extends TestCase
{

    SupportedWindowStateService underTest = null;

    protected void setUp() throws Exception
    {
        super.setUp();
        underTest = new SupportedWindowStateServiceUnitTestImpl(
                new MockPropertyConfigService());
        underTest.init(null);
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
        underTest = null;
    }

    /**
     * Ensure JSR-168 window states are supported by both the portal and the portlet.
     */
    public void testIsWindowStateSupported()
    {
        // We should support all JSR 168 window states
        assertTrue(underTest.isWindowStateSupported("some.id", "maximized"));
        assertTrue(underTest.isWindowStateSupported("some.id", "minimized"));
        assertTrue(underTest.isWindowStateSupported("some.id", "normal"));
        assertTrue(underTest.isWindowStateSupported("some.id", WindowState.MAXIMIZED.toString()));
        assertTrue(underTest.isWindowStateSupported("some.id", WindowState.MINIMIZED.toString()));
        assertTrue(underTest.isWindowStateSupported("some.id", WindowState.NORMAL.toString()));
    }

    /**
     * Ensure a custom window state is supported by both the portal and portlet.
     */
    public void testIsCustomWindowStateSupported()
    {
        assertTrue(underTest.isWindowStateSupported("some.id", "a-custom-state"));
        assertTrue(underTest.isWindowStateSupported("some.id", new WindowState("a-custom-state").toString()));
    }

    /**
     * Ensure a non-existant window state is not supported by either the portal
     * or the portlet.
     */
    public void testIsNonExistantWindowStateSupported()
    {
        assertFalse(underTest.isWindowStateSupported("some.id", "a-nonexistant-state"));
        assertFalse(underTest.isWindowStateSupported("some.id", new WindowState("a-nonexistant-state").toString()));
    }

    /**
     * Ensure the portal supports all JSR-168 mandated window states.
     */
    public void testIsWindowStateSupportedByPortal()
    {
        // We should support all JSR 168 window states
        assertTrue(underTest.isWindowStateSupportedByPortal("maximized"));
        assertTrue(underTest.isWindowStateSupportedByPortal("minimized"));
        assertTrue(underTest.isWindowStateSupportedByPortal("normal"));
        assertTrue(underTest.isWindowStateSupportedByPortal(WindowState.MAXIMIZED.toString()));
        assertTrue(underTest.isWindowStateSupportedByPortal(WindowState.MINIMIZED.toString()));
        assertTrue(underTest.isWindowStateSupportedByPortal(WindowState.NORMAL.toString()));
    }

    /**
     * Ensure custom window states are supported by the portal.
     */
    public void testIsCustomWindowStateSupportedByPortal()
    {
        assertTrue(underTest.isWindowStateSupportedByPortal("a-custom-state"));
        assertTrue(underTest.isWindowStateSupportedByPortal(new WindowState("a-custom-state").toString()));
    }

    /**
     * Ensure non-existant window states are not supported by the portal.
     */
    public void testNonExistantWindowStateSupportedByPortal()
    {
        assertFalse(underTest.isWindowStateSupportedByPortal("a-nonexistant-state"));
        assertFalse(underTest.isWindowStateSupportedByPortal(new WindowState("a-nonexistant-state").toString()));
    }

    /**
     * Ensure the portlet supports all JSR-168 window states.
     */
    public void testIsWindowStateSupportedByPortlet()
    {
        // We should support all JSR 168 window states
        assertTrue(underTest.isWindowStateSupportedByPortlet("some.id", "maximized"));
        assertTrue(underTest.isWindowStateSupportedByPortlet("some.id", "minimized"));
        assertTrue(underTest.isWindowStateSupportedByPortlet("some.id", "normal"));
        assertTrue(underTest.isWindowStateSupportedByPortlet("some.id", WindowState.MAXIMIZED.toString()));
        assertTrue(underTest.isWindowStateSupportedByPortlet("some.id", WindowState.MINIMIZED.toString()));
        assertTrue(underTest.isWindowStateSupportedByPortlet("some.id", WindowState.NORMAL.toString()));
    }

    /**
     * Ensure custom window states are supported in the portlet.
     */
    public void testIsCustomWindowStateSupportedByPortlet()
    {
        assertTrue(underTest.isWindowStateSupportedByPortlet("some.id", "a-custom-state"));
        assertTrue(underTest.isWindowStateSupportedByPortlet("some.id", new WindowState("a-custom-state").toString()));
    }

    /**
     * Ensure non-existant window states are not supported by the portlet.
     */
    public void testIsNonExistantWindowStateSupportedByPortlet()
    {
        assertFalse(underTest.isWindowStateSupportedByPortlet("some.id", "a-nonexistant-state"));
        assertFalse(underTest.isWindowStateSupportedByPortlet("some.id", new WindowState("a-nonexistant-state").toString()));
    }

    /**
     * This class wraps the actual SupportedWindowStateServiceImpl class in
     * order to override the init() method and supply mock service objects.
     *
     * @since Feb 28, 2007
     * @version $Id: SupportedWindowStateServiceTest.java 516329 2007-03-09 08:43:46Z cziegeler $
     */
    private class SupportedWindowStateServiceUnitTestImpl
        extends SupportedWindowStateServiceImpl
    {
        SupportedWindowStateServiceUnitTestImpl(PropertyConfigService svc)
        {
            super(svc);
        }

        public void init(ServletContext ctx) throws DriverConfigurationException
        {
            this.portletRegistry = new MockPortletRegistryService(
                    this.getClass().getResourceAsStream("/portlet.xml"));
            this.portalSupportedWindowStates = propertyService.getSupportedWindowStates();
        }
    }


}
