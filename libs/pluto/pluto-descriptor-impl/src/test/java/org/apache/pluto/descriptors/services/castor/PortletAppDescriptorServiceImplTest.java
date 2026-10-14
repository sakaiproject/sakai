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
package org.apache.pluto.descriptors.services.castor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.TestCase;

import org.apache.pluto.descriptors.portlet.PortletAppDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.portlet.PortletInfoDD;
import org.apache.pluto.descriptors.portlet.PortletPreferencesDD;
import org.apache.pluto.descriptors.portlet.SupportsDD;

/**
 *
 * @version $Id: PortletAppDescriptorServiceImplTest.java 611004 2008-01-11 01:18:36Z esm $
 * @since 1.1.0
 */
public class PortletAppDescriptorServiceImplTest extends TestCase {

    private PortletAppDescriptorServiceImpl service;

    public void setUp() {
        service = new PortletAppDescriptorServiceImpl();
    }

    public void testParseMinimal() throws IOException {
        InputStream in = this.getClass().getResourceAsStream("/portlet-minimal.xml");

        PortletAppDD dd = service.read(in);
        assertEquals("1.0", dd.getVersion());
        assertEquals(0, dd.getCustomPortletModes().size());
        assertEquals(0, dd.getCustomWindowStates().size());
        assertEquals(0, dd.getUserAttributes().size());
        assertEquals(0, dd.getSecurityConstraints().size());
        
        List portlets = dd.getPortlets();
        assertEquals(1, portlets.size());
        PortletDD pd = (PortletDD)portlets.get(0);
        
        assertEquals(0, pd.getDescriptions().size());
        assertEquals(0, pd.getDisplayNames().size());
        assertEquals(PortletDD.EXPIRATION_CACHE_UNSET, pd.getExpirationCache());
        assertEquals(0, pd.getInitParams().size());
        assertEquals("org.apache.pluto.driver.portlets.AboutPortlet", pd.getPortletClass());
        
        PortletInfoDD pi = pd.getPortletInfo();
        assertNotNull(pi);
        assertEquals("About Apache Pluto", pi.getTitle());
        assertNull(pi.getShortTitle());
        assertNull(pi.getKeywords());
        
        assertEquals("AboutPortlet", pd.getPortletName());
        
        PortletPreferencesDD pp = pd.getPortletPreferences();
        assertNotNull(pp);
        assertNull(pp.getPreferencesValidator());
        assertEquals(0, pp.getPortletPreferences().size());
        
        assertNull(pd.getResourceBundle());
        assertEquals(0, pd.getSecurityRoleRefs().size());
        assertEquals(0, pd.getSupportedLocales().size());
        
        List supports = pd.getSupports();
        assertEquals(1, supports.size());
        SupportsDD sd = (SupportsDD)supports.get(0);
        
        assertEquals("text/html", sd.getMimeType());
        assertEquals(0, sd.getPortletModes().size());
    }
    
    public void testRejectUnsupportedDescriptorElements() {
        InputStream input = getClass().getResourceAsStream("/portlet-full.xml");
        org.junit.Assert.assertThrows(IOException.class, () -> service.read(input));
    }
}
