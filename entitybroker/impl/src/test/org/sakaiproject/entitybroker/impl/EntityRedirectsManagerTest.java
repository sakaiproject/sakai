/**
 * $Id$
 * $URL$
 * EntityRedirectsManagerTest.java - entity-broker - Jul 31, 2008 1:36:47 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.entitybroker.impl;

import java.util.List;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.mocks.data.TestData;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.request.URLRedirect;


/**
 * Testing the redirects manager
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class EntityRedirectsManagerTest extends TestCase {

   protected EntityRedirectsManager entityRedirectsManager;
   private TestData td;

   private URLRedirect valid1 = new URLRedirect("/{prefix}/one");
   private URLRedirect valid2 = new URLRedirect("/{prefix}/two", "/otherprefix/from/{prefix}");
   private URLRedirect valid3 = new URLRedirect("/{prefix}/three", "testMethod", new Class<?>[] {});

   @Override
   protected void setUp() throws Exception {
      super.setUp();
      // setup things
      td = new TestData();

      entityRedirectsManager = new ServiceTestManager(td).entityRedirectsManager;
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityRedirectsManager#checkForTemplateMatch(org.sakaiproject.entitybroker.entityprovider.EntityProvider, java.lang.String, String)}.
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

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityRedirectsManager#findURLRedirectMethods(org.sakaiproject.entitybroker.entityprovider.EntityProvider)}.
    */
   public void testFindURLRedirectMethods() {
      URLRedirect[] redirects = entityRedirectsManager.findURLRedirectMethods(td.entityProviderU1);
      assertNotNull(redirects);
      assertEquals(4, redirects.length);
      assertEquals("/{prefix}/{id}/{thing}/go", redirects[0].template);
      assertEquals("outsideRedirector", redirects[0].methodName);
      assertNotNull(redirects[0].methodArgTypes);
      assertEquals("/{prefix}/xml/{id}", redirects[1].template);
      assertEquals("xmlRedirector", redirects[1].methodName);
      assertNotNull(redirects[1].methodArgTypes);
      assertEquals("/{prefix}/going/nowhere", redirects[2].template);
      assertEquals("returningRedirector", redirects[2].methodName);
      assertNotNull(redirects[2].methodArgTypes);
      assertEquals("/{prefix}/keep/moving", redirects[3].template);
      assertEquals("neverRedirector", redirects[3].methodName);
      assertNotNull(redirects[3].methodArgTypes);

      redirects = entityRedirectsManager.findURLRedirectMethods(td.entityProvider3);
      assertNotNull(redirects);
      assertEquals(0, redirects.length);
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityRedirectsManager#validateDefineableTemplates(org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable)}.
    */
   public void testValidateDefineableTemplates() {
      URLRedirect[] redirects = EntityRedirectsManager.validateDefineableTemplates(td.entityProviderU2);
      assertNotNull(redirects);
      String[] templates = td.entityProviderU2.templates;
      assertEquals(templates[0], redirects[0].template);
      assertEquals(TemplateParseUtil.DIRECT_PREFIX+templates[1], redirects[0].outgoingTemplate);
      assertNotNull(redirects[0].preProcessedTemplate);
      assertNotNull(redirects[0].outgoingPreProcessedTemplate);
      assertEquals(templates[2], redirects[1].template);
      assertEquals(TemplateParseUtil.DIRECT_PREFIX+TemplateParseUtil.SEPARATOR+templates[3], redirects[1].outgoingTemplate);
      assertNotNull(redirects[1].preProcessedTemplate);
      assertNotNull(redirects[1].outgoingPreProcessedTemplate);
      assertEquals(templates[4], redirects[2].template);
      assertEquals(TemplateParseUtil.DIRECT_PREFIX+templates[5], redirects[2].outgoingTemplate);
      assertNotNull(redirects[2].preProcessedTemplate);
      assertNotNull(redirects[2].outgoingPreProcessedTemplate);
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityRedirectsManager#validateControllableTemplates(org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectControllable)}.
    */
   public void testValidateControllableTemplates() {
      URLRedirect[] redirects = EntityRedirectsManager.validateControllableTemplates(td.entityProviderU3);
      assertNotNull(redirects);
      String[] templates = td.entityProviderU3.templates;
      assertEquals(templates[0], redirects[0].template);
      assertTrue(redirects[0].controllable);
      assertEquals(templates[1], redirects[1].template);
      assertTrue(redirects[1].controllable);
      assertEquals(templates[2], redirects[2].template);
      assertTrue(redirects[2].controllable);
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityRedirectsManager#addURLRedirects(java.lang.String, org.sakaiproject.entitybroker.util.request.URLRedirect[])}.
    */
   public void testAddURLRedirects() {
      entityRedirectsManager.addURLRedirects("testing", new URLRedirect[] {valid1, valid2});
      assertEquals(2, entityRedirectsManager.getURLRedirects("testing").size());

      entityRedirectsManager.addURLRedirects("testing", new URLRedirect[] {});
      assertEquals(2, entityRedirectsManager.getURLRedirects("testing").size());

      entityRedirectsManager.addURLRedirects("testing", null);
      assertEquals(2, entityRedirectsManager.getURLRedirects("testing").size());

      entityRedirectsManager.addURLRedirects("testing", new URLRedirect[] {valid3});
      assertEquals(3, entityRedirectsManager.getURLRedirects("testing").size());

      // test adding the same one causes a failure
      try {
         entityRedirectsManager.addURLRedirects("testing", new URLRedirect[] {valid3});
         fail("should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityRedirectsManager#removeURLRedirects(java.lang.String)}.
    */
   public void testRemoveURLRedirects() {
      entityRedirectsManager.addURLRedirects("testing", new URLRedirect[] {valid1, valid2});
      assertEquals(2, entityRedirectsManager.getURLRedirects("testing").size());

      entityRedirectsManager.removeURLRedirects("testing");
      assertEquals(0, entityRedirectsManager.getURLRedirects("testing").size());
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.impl.EntityRedirectsManager#getURLRedirects(java.lang.String)}.
    */
   public void testGetURLRedirects() {
      entityRedirectsManager.addURLRedirects("testing", new URLRedirect[] {valid1, valid2});
      assertEquals(2, entityRedirectsManager.getURLRedirects("testing").size());
      List<URLRedirect> redirects = entityRedirectsManager.getURLRedirects("testing");
      assertEquals(valid1, redirects.get(0));
      assertEquals(valid2, redirects.get(1));
   }

}
