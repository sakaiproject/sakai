/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.rest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.entitybroker.config.EntityRestTestConfiguration;
import org.sakaiproject.entitybroker.entityprovider.EntityProviderManager;
import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@ContextConfiguration(classes = {EntityRestTestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class EntityRedirectsManagerTest {

    @Autowired private EntityRedirectsManager entityRedirectsManager;
    @Autowired private EntityProviderManager entityProviderManager;
    @Autowired private ServerConfigurationService serverConfigurationService;

    private TestData td;

    @Before
    public void setUp() {
        td = new TestData(entityProviderManager);
        Mockito.when(serverConfigurationService.getServerUrl()).thenReturn("http://localhost:8080");
    }

    @After
    public void tearDown() {
        entityProviderManager.unRegistrarAllProvidersAndListeners();
        td = null;
    }

    /**
     * Test method for {@link org.sakaiproject.entitybroker.rest.EntityRedirectsManager#checkForTemplateMatch(org.sakaiproject.entitybroker.entityprovider.EntityProvider, java.lang.String, String)}.
     */
    @Test
    public void testCheckForTemplateMatch() {
        String targetURL;

        // test Redirectable matching
        targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU1, TestData.SPACEU1 + "/123/AZ/go", null);
        assertNotNull(targetURL);
        assertEquals("http://caret.cam.ac.uk/?prefix=" + TestData.PREFIXU1 + "&thing=AZ", targetURL);

        targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU1, TestData.SPACEU1 + "/xml/123", null);
        assertNotNull(targetURL);
        assertEquals(TemplateParseUtil.DIRECT_PREFIX + TestData.SPACEU1 + "/123.xml", targetURL);

        targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU1, TestData.SPACEU1 + "/going/nowhere", null);
        assertNotNull(targetURL);
        assertEquals("", targetURL);

        targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU1, TestData.SPACEU1 + "/keep/moving", null);
        assertNull(targetURL);

        // test RedirectDefinable matching
        targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU2, TestData.SPACEU2 + "/site/s1/user/aaronz/junk", null);
        assertNotNull(targetURL);
        assertEquals(TemplateParseUtil.DIRECT_PREFIX + TestData.SPACEU2 + "?siteId=s1&userId=aaronz", targetURL);

        targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU2, TestData.SPACEU2 + "/123/AZ/go/junk", null);
        assertNotNull(targetURL);
        assertEquals(TemplateParseUtil.DIRECT_PREFIX + "/other/stuff?prefix=" + TestData.PREFIXU2 + "&id=123", targetURL);

        targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU2, TestData.SPACEU2 + "/xml/123/junk", null);
        assertNotNull(targetURL);
        assertEquals(TemplateParseUtil.DIRECT_PREFIX + TestData.SPACEU2 + "/123.xml", targetURL);

        targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU2, TestData.SPACEU2 + "/keep/moving", null);
        assertNull(targetURL);

        // test RedirectControllable matching
        targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU3, TestData.SPACEU3 + "/site/s1/user/aaronz/junk", null);
        assertNotNull(targetURL);
        assertEquals(TemplateParseUtil.DIRECT_PREFIX + TestData.SPACEU3 + "/siteuser?site=s1&user=aaronz", targetURL);

        targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU3, TestData.SPACEU3 + "/123/AZ/go/junk", null);
        assertNotNull(targetURL);
        assertEquals("http://caret.cam.ac.uk/?prefix=" + TestData.PREFIXU3 + "&thing=AZ", targetURL);

        targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU3, TestData.SPACEU3 + "/xml/123/junk", null);
        assertNotNull(targetURL);
        assertEquals(TemplateParseUtil.DIRECT_PREFIX + TestData.SPACEU3 + "/123.xml", targetURL);

        // test non-matching
        targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProvider4, TestData.SPACE4 + "/123", null);
        assertNull(targetURL);
    }

}
