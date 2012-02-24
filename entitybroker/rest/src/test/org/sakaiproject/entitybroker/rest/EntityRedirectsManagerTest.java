/**
 * $Id$
 * $URL$
 * EntityRedirectsManagerTest.java - entity-broker - Jul 31, 2008 1:36:47 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.rest;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.rest.EntityRedirectsManager;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;


/**
 * Testing the redirects manager
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityRedirectsManagerTest extends TestCase {

   protected EntityRedirectsManager entityRedirectsManager;
   private TestData td;

   @Override
   protected void setUp() throws Exception {
      super.setUp();
      // setup things
      td = new TestData();

      entityRedirectsManager = new ServiceTestManager(td).entityRedirectsManager;
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.rest.EntityRedirectsManager#checkForTemplateMatch(org.sakaiproject.entitybroker.entityprovider.EntityProvider, java.lang.String, String)}.
    */
   public void testCheckForTemplateMatch() {
      String targetURL = null;

      // test Redirectable matching
      targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU1, TestData.SPACEU1 + "/123/AZ/go", null);
      assertNotNull(targetURL);
      assertEquals("http://caret.cam.ac.uk/?prefix=" + TestData.PREFIXU1 + "&thing=AZ", targetURL);

      targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU1, TestData.SPACEU1 + "/xml/123", null);
      assertNotNull(targetURL);
      assertEquals(TemplateParseUtil.DIRECT_PREFIX+TestData.SPACEU1+"/123.xml", targetURL);

      targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU1, TestData.SPACEU1 + "/going/nowhere", null);
      assertNotNull(targetURL);
      assertEquals("", targetURL);

      targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU1, TestData.SPACEU1 + "/keep/moving", null);
      assertNull(targetURL);

      // test RedirectDefinable matching
      targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU2, TestData.SPACEU2 + "/site/s1/user/aaronz/junk", null);
      assertNotNull(targetURL);
      assertEquals(TemplateParseUtil.DIRECT_PREFIX+TestData.SPACEU2 + "?siteId=s1&userId=aaronz", targetURL);

      targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU2, TestData.SPACEU2 + "/123/AZ/go/junk", null);
      assertNotNull(targetURL);
      assertEquals(TemplateParseUtil.DIRECT_PREFIX+"/other/stuff?prefix="+TestData.PREFIXU2+"&id=123", targetURL);

      targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU2, TestData.SPACEU2 + "/xml/123/junk", null);
      assertNotNull(targetURL);
      assertEquals(TemplateParseUtil.DIRECT_PREFIX+TestData.SPACEU2+"/123.xml", targetURL);

      targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU2, TestData.SPACEU2 + "/keep/moving", null);
      assertNull(targetURL);

      // test RedirectControllable matching
      targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU3, TestData.SPACEU3 + "/site/s1/user/aaronz/junk", null);
      assertNotNull(targetURL);
      assertEquals(TemplateParseUtil.DIRECT_PREFIX+TestData.SPACEU3 + "/siteuser?site=s1&user=aaronz", targetURL);
      
      targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU3, TestData.SPACEU3 + "/123/AZ/go/junk", null);
      assertNotNull(targetURL);
      assertEquals("http://caret.cam.ac.uk/?prefix="+TestData.PREFIXU3+"&thing=AZ", targetURL);

      targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProviderU3, TestData.SPACEU3 + "/xml/123/junk", null);
      assertNotNull(targetURL);
      assertEquals(TemplateParseUtil.DIRECT_PREFIX+TestData.SPACEU3+"/123.xml", targetURL);

      // test non-matching
      targetURL = entityRedirectsManager.checkForTemplateMatch(td.entityProvider4, TestData.SPACE4 + "/123", null);
      assertNull(targetURL);
   }

}
