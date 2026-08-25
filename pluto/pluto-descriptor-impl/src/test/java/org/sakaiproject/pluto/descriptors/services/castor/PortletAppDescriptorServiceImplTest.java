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
package org.sakaiproject.pluto.descriptors.services.castor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.sakaiproject.pluto.descriptors.portlet.CustomPortletModeDD;
import org.sakaiproject.pluto.descriptors.portlet.PortletAppDD;
import org.sakaiproject.pluto.descriptors.portlet.PortletDD;
import org.sakaiproject.pluto.descriptors.portlet.PortletInfoDD;
import org.sakaiproject.pluto.descriptors.portlet.PortletPreferenceDD;
import org.sakaiproject.pluto.descriptors.portlet.PortletPreferencesDD;
import org.sakaiproject.pluto.descriptors.portlet.SecurityConstraintDD;
import org.sakaiproject.pluto.descriptors.portlet.SupportsDD;

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
        assertEquals("org.sakaiproject.pluto.driver.portlets.AboutPortlet", pd.getPortletClass());
        
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
    
    public void testParseFull() throws IOException {
        InputStream in = this.getClass().getResourceAsStream("/portlet-full.xml");
        
        PortletAppDD dd = service.read(in);
        assertEquals("1.0", dd.getVersion());
        
        
        List customPortletModes = dd.getCustomPortletModes();
        assertEquals(2, customPortletModes.size());
        
        CustomPortletModeDD cpm1 = (CustomPortletModeDD)customPortletModes.get(0);
        assertEquals("customMode", cpm1.getPortletMode());
        assertEquals(1, cpm1.getDescriptions().size());
        
        CustomPortletModeDD cpm2 = (CustomPortletModeDD)customPortletModes.get(1);
        assertEquals("customMode2", cpm2.getPortletMode());
        assertEquals(1, cpm2.getDescriptions().size());
        
        assertEquals(2, dd.getCustomWindowStates().size());
        assertEquals(1, dd.getUserAttributes().size());

        assertEquals(1, dd.getSecurityConstraints().size());
        SecurityConstraintDD sc = (SecurityConstraintDD)dd.getSecurityConstraints().get(0);
        assertNotNull(sc.getPortletCollection());
        assertEquals(1, sc.getDisplayNames().size());
        assertEquals(3, sc.getPortletCollection().getPortletNames().size());
        assertEquals("a", sc.getPortletCollection().getPortletNames().get(0));
        assertEquals("b", sc.getPortletCollection().getPortletNames().get(1));
        assertEquals("c", sc.getPortletCollection().getPortletNames().get(2));

        assertNotNull(sc.getUserDataConstraint());
        assertEquals(1, sc.getUserDataConstraint().getDescriptions().size());
        assertEquals("NONE", sc.getUserDataConstraint().getTransportGuarantee());
        
        List portlets = dd.getPortlets();
        assertEquals(1, portlets.size());
        PortletDD pd = (PortletDD)portlets.get(0);
        
        assertEquals(1, pd.getDescriptions().size());
        assertEquals(2, pd.getDisplayNames().size());
        assertEquals(30, pd.getExpirationCache());
        assertEquals(1, pd.getInitParams().size());
        assertEquals("org.sakaiproject.pluto.driver.portlets.AboutPortlet", pd.getPortletClass());
        
        PortletInfoDD pi = pd.getPortletInfo();
        assertNotNull(pi);
        assertEquals("About Apache Pluto", pi.getTitle());
        assertNull(pi.getShortTitle());
        assertNull(pi.getKeywords());
        
        assertEquals("AboutPortlet", pd.getPortletName());
        
        PortletPreferencesDD pp = pd.getPortletPreferences();
        assertNotNull(pp);
        assertNull(pp.getPreferencesValidator());
        
        List prefs = pp.getPortletPreferences();
        assertEquals(4, prefs.size());
        
        PortletPreferenceDD pref1 = (PortletPreferenceDD)prefs.get(0);
        assertEquals("noValues", pref1.getName());
        assertNull(pref1.getValues());
        
        PortletPreferenceDD pref2 = (PortletPreferenceDD)prefs.get(1);
        assertEquals("oneEmptyValue", pref2.getName());
        assertEquals(Arrays.asList(new String[] {""}), pref2.getValues());
        
        PortletPreferenceDD pref3 = (PortletPreferenceDD)prefs.get(2);
        assertEquals("oneValue", pref3.getName());
        assertEquals(Arrays.asList(new String[] {"value1"}), pref3.getValues());
        
        PortletPreferenceDD pref4 = (PortletPreferenceDD)prefs.get(3);
        assertEquals("fourValues", pref4.getName());
        assertEquals(Arrays.asList(new String[] {"value1", "", "value3", ""}), pref4.getValues());
        
        
        assertNull(pd.getResourceBundle());
        assertEquals(0, pd.getSecurityRoleRefs().size());
        assertEquals(1, pd.getSupportedLocales().size());
        
        List supports = pd.getSupports();
        assertEquals(1, supports.size());
        SupportsDD sd = (SupportsDD)supports.get(0);
        
        assertEquals("text/html", sd.getMimeType());
        assertEquals(3, sd.getPortletModes().size());
    }
}
