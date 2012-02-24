/**
 * $Id$
 * $URL$
 * TemplateParseUtilTest.java - entity-broker - Apr 10, 2008 10:14:55 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.entitybroker.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.PreProcessedTemplate;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.ProcessedTemplate;

/**
 * Testing the template parsing utility class
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TemplateParseUtilTest extends TestCase {

   /**
    * Test extension finder method
    */
   public void testFindExtension() {
      String input = null;
      String[] output = null;

      input = "noextension";
      output = TemplateParseUtil.findExtension(input);
      assertNotNull(output);
      assertEquals(3, output.length);
      assertEquals(input, output[0]);
      assertEquals(input, output[1]);
      assertEquals(null, output[2]);

      input = "test.xml";
      output = TemplateParseUtil.findExtension(input);
      assertNotNull(output);
      assertEquals(3, output.length);
      assertEquals(input, output[0]);
      assertEquals("test", output[1]);
      assertEquals("xml", output[2]);

      input = "/complex/stuff/test.other.json";
      output = TemplateParseUtil.findExtension(input);
      assertNotNull(output);
      assertEquals(3, output.length);
      assertEquals(input, output[0]);
      assertEquals("/complex/stuff/test.other", output[1]);
      assertEquals("json", output[2]);

      input = "test.";
      output = TemplateParseUtil.findExtension(input);
      assertNotNull(output);
      assertEquals(3, output.length);
      assertEquals(input, output[0]);
      assertEquals("test", output[1]);
      assertEquals(null, output[2]);

      input = "/user/asdf@vt.edu/exists";
      output = TemplateParseUtil.findExtension(input);
      assertNotNull(output);
      assertEquals(3, output.length);
      assertEquals(input, output[0]);
      assertEquals("/user/asdf@vt.edu/exists", output[1]);
      assertEquals(null, output[2]);

      input = "/user/id=asdf@vt.edu.html";
      output = TemplateParseUtil.findExtension(input);
      assertNotNull(output);
      assertEquals(3, output.length);
      assertEquals(input, output[0]);
      assertEquals("/user/id=asdf@vt.edu", output[1]);
      assertEquals("html", output[2]);

   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.util.TemplateParseUtil#validateTemplateKey(java.lang.String)}.
    */
   public void testCheckTemplateKey() {
      for (int i = 0; i < TemplateParseUtil.PARSE_TEMPLATE_KEYS.length; i++) {
         // should simply not throw exception
         TemplateParseUtil.validateTemplateKey(TemplateParseUtil.PARSE_TEMPLATE_KEYS[i]);
      }

      // test invalid key throws exception
      try {
         TemplateParseUtil.validateTemplateKey("xxxxxxxxxxxxxxxxxxxxxxx");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.util.TemplateParseUtil#getDefaultTemplate(java.lang.String)}.
    */
   public void testGetDefaultTemplate() {
      String template = null;

      template = TemplateParseUtil.getDefaultTemplate(TemplateParseUtil.TEMPLATE_LIST);
      assertNotNull(template);
      assertEquals("/{"+TemplateParseUtil.PREFIX+"}", template);

      template = TemplateParseUtil.getDefaultTemplate(TemplateParseUtil.TEMPLATE_SHOW);
      assertNotNull(template);
      assertEquals("/{"+TemplateParseUtil.PREFIX+"}/{"+TemplateParseUtil.ID+"}", template);

      // test invalid keys causes failure
      try {
         template = TemplateParseUtil.getDefaultTemplate("xxxxxxxxxxxxxxxxxxx");
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.util.TemplateParseUtil#validateTemplate(java.lang.String)}.
    */
   public void testValidateTemplate() {
      String template = null;

      // make sure all the default templates are valid
      for (int i = 0; i < TemplateParseUtil.PARSE_TEMPLATE_KEYS.length; i++) {
         // should simply not throw exception
         template = TemplateParseUtil.getDefaultTemplate(TemplateParseUtil.PARSE_TEMPLATE_KEYS[i]);
         TemplateParseUtil.validateTemplate(template);
      }

      // now make sure a few seemingly valid ones are also valid
      template = "/{prefix}/mystuff/other";
      TemplateParseUtil.validateTemplate(template);

      template = "/{prefix}/other/{id}";
      TemplateParseUtil.validateTemplate(template);

      template = "/{prefix}/other/{id}/morestuff";
      TemplateParseUtil.validateTemplate(template);

      template = "/{prefix}/{thing}/{id}";
      TemplateParseUtil.validateTemplate(template);

      template = "/{prefix}/thing={thing}/{id}";
      TemplateParseUtil.validateTemplate(template);

      template = "/{prefix}/{thing}/{id}.xml";
      TemplateParseUtil.validateTemplate(template);

      template = "/{prefix}/{id}/special=.-/:;_";
      TemplateParseUtil.validateTemplate(template);

      template = "/{prefix}stuff";
      TemplateParseUtil.validateTemplate(template);


      // now check ones that invalid
      template = "";
      try {
         TemplateParseUtil.validateTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      template = "/apple/{prefix}";
      try {
         TemplateParseUtil.validateTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      template = "/{prefix} /{id} /";
      try {
         TemplateParseUtil.validateTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      template = "/{prefix}/{id}/";
      try {
         TemplateParseUtil.validateTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      template = "{prefix}/{id}";
      try {
         TemplateParseUtil.validateTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      template = "/{prefix}{id}";
      try {
         TemplateParseUtil.validateTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      template = "/{prefix}/{}/";
      try {
         TemplateParseUtil.validateTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      template = "/{prefix}/thing?auto=true";
      try {
         TemplateParseUtil.validateTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      template = "/{prefix}/{id}/?special=%";
      try {
         TemplateParseUtil.validateTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   public void testValidateOutgoingTemplate() {
      String template = null;

      // make sure all the default templates are valid
      for (int i = 0; i < TemplateParseUtil.PARSE_TEMPLATE_KEYS.length; i++) {
         // should simply not throw exception
         template = TemplateParseUtil.getDefaultTemplate(TemplateParseUtil.PARSE_TEMPLATE_KEYS[i]);
         TemplateParseUtil.validateOutgoingTemplate(template);
      }

      // now make sure a few seemingly valid ones are also valid
      template = "/{prefix}/mystuff/other";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}/other/{id}";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}/other/{id}/morestuff";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}/{thing}/{id}";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}/thing={thing}/{id}";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}/{thing}/{id}.xml";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}/{id}/special=.-/:;_";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}stuff";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}/thing?auto=true";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}/thing?auto={something}";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}/thing?auto=%22AZ%2C1*2%2B3%3D5%2C+OK%3F%22";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}/thing?auto={something}&manual={other}";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}/{id}/";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "{prefix}/{id}";
      TemplateParseUtil.validateOutgoingTemplate(template);

      template = "/{prefix}{id}";
      TemplateParseUtil.validateOutgoingTemplate(template);

      // now check ones that invalid
      template = "";
      try {
         TemplateParseUtil.validateOutgoingTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      template = "/{prefix} /{id} /";
      try {
         TemplateParseUtil.validateOutgoingTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      template = "/{prefix}/{}/";
      try {
         TemplateParseUtil.validateOutgoingTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      template = "/{prefix}/{id}/?special=^^";
      try {
         TemplateParseUtil.validateOutgoingTemplate(template);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }
   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.util.TemplateParseUtil#mergeTemplate(java.lang.String, java.util.Map)}.
    */
   public void testMergeTemplate() {
      String merge = null;
      String template = null;
      Map<String, String> segments = new HashMap<String, String>();
      segments.put(TemplateParseUtil.PREFIX, "myPrefix");
      segments.put(TemplateParseUtil.ID, "myId");

      // make sure all the default templates can be merged
      for (int i = 0; i < TemplateParseUtil.PARSE_TEMPLATE_KEYS.length; i++) {
         template = TemplateParseUtil.getDefaultTemplate(TemplateParseUtil.PARSE_TEMPLATE_KEYS[i]);
         merge = TemplateParseUtil.mergeTemplate(template, segments);
         assertNotNull(merge);
      }

      // test a couple to be sure
      template = TemplateParseUtil.getDefaultTemplate(TemplateParseUtil.TEMPLATE_LIST);
      merge = TemplateParseUtil.mergeTemplate(template, segments);
      assertNotNull(merge);
      assertEquals("/myPrefix", merge);

      template = TemplateParseUtil.getDefaultTemplate(TemplateParseUtil.TEMPLATE_SHOW);
      merge = TemplateParseUtil.mergeTemplate(template, segments);
      assertNotNull(merge);
      assertEquals("/myPrefix/myId", merge);

      // test some others which should work
      template = "/{"+TemplateParseUtil.PREFIX+"}/middle/{"+TemplateParseUtil.ID+"}/end.xml";
      merge = TemplateParseUtil.mergeTemplate(template, segments);
      assertNotNull(merge);
      assertEquals("/myPrefix/middle/myId/end.xml", merge);

      // test case where we do not have enough values in the segments
      template = "/{"+TemplateParseUtil.PREFIX+"}/{"+TemplateParseUtil.ID+"}/{thing}";
      try {
         merge = TemplateParseUtil.mergeTemplate(template, segments);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // test case where we have wrong values in the segments
      template = "/{profix}/{eid}";
      try {
         merge = TemplateParseUtil.mergeTemplate(template, segments);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // test invalid inputs
      try {
         merge = TemplateParseUtil.mergeTemplate("", segments);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      try {
         merge = TemplateParseUtil.mergeTemplate(template, null);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }

   /**
    * Test method for {@link org.sakaiproject.entitybroker.util.TemplateParseUtil#parseTemplate(java.lang.String, java.util.Map)}.
    */
   public void testParseTemplate() {
      String input = null;
      ProcessedTemplate result = null;
      List<PreProcessedTemplate> preprocessedTemplates = TemplateParseUtil.defaultPreprocessedTemplates;

      // try some valid references
      input = "/prefixOnly";
      result = TemplateParseUtil.parseTemplate(input, preprocessedTemplates);
      assertNotNull(result);
      assertEquals(TemplateParseUtil.TEMPLATE_LIST, result.templateKey);
      assertEquals("prefixOnly", result.segmentValues.get(TemplateParseUtil.PREFIX));

      input = "/myPrefix/myId";
      result = TemplateParseUtil.parseTemplate(input, preprocessedTemplates);
      assertNotNull(result);
      assertEquals(TemplateParseUtil.TEMPLATE_SHOW, result.templateKey);
      assertEquals("myPrefix", result.segmentValues.get(TemplateParseUtil.PREFIX));
      assertEquals("myId", result.segmentValues.get(TemplateParseUtil.ID));

      input = "/myPrefix/12345/edit";
      result = TemplateParseUtil.parseTemplate(input, preprocessedTemplates);
      assertNotNull(result);
      assertEquals(TemplateParseUtil.TEMPLATE_EDIT, result.templateKey);
      assertEquals("myPrefix", result.segmentValues.get(TemplateParseUtil.PREFIX));
      assertEquals("12345", result.segmentValues.get(TemplateParseUtil.ID));

      input = "/myPrefix/new";
      result = TemplateParseUtil.parseTemplate(input, preprocessedTemplates);
      assertNotNull(result);
      assertEquals(TemplateParseUtil.TEMPLATE_NEW, result.templateKey);
      assertEquals("myPrefix", result.segmentValues.get(TemplateParseUtil.PREFIX));
      assertEquals(null, result.segmentValues.get(TemplateParseUtil.ID)); // ensure it did not grab the id

      // make sure extra stuff on the end is ok
      input = "/myPrefix/myId.xml";
      result = TemplateParseUtil.parseTemplate(input, preprocessedTemplates);
      assertNotNull(result);
      assertEquals(TemplateParseUtil.TEMPLATE_SHOW, result.templateKey);
      assertEquals("myPrefix", result.segmentValues.get(TemplateParseUtil.PREFIX));
      assertEquals("myId", result.segmentValues.get(TemplateParseUtil.ID));

      input = "/myPrefix/myId/blahblahblah.xml";
      result = TemplateParseUtil.parseTemplate(input, preprocessedTemplates);
      assertNotNull(result);
      assertEquals(TemplateParseUtil.TEMPLATE_SHOW, result.templateKey);
      assertEquals("myPrefix", result.segmentValues.get(TemplateParseUtil.PREFIX));
      assertEquals("myId", result.segmentValues.get(TemplateParseUtil.ID));

      input = "/myPrefix/myId/blahblah/blah/yadda";
      result = TemplateParseUtil.parseTemplate(input, preprocessedTemplates);
      assertNotNull(result);
      assertEquals(TemplateParseUtil.TEMPLATE_SHOW, result.templateKey);
      assertEquals("myPrefix", result.segmentValues.get(TemplateParseUtil.PREFIX));
      assertEquals("myId", result.segmentValues.get(TemplateParseUtil.ID));

      input = "/myPrefix/myId/blahblah/yadda.xml";
      result = TemplateParseUtil.parseTemplate(input, preprocessedTemplates);
      assertNotNull(result);
      assertEquals(TemplateParseUtil.TEMPLATE_SHOW, result.templateKey);
      assertEquals("myPrefix", result.segmentValues.get(TemplateParseUtil.PREFIX));
      assertEquals("myId", result.segmentValues.get(TemplateParseUtil.ID));

      input = "/myPrefix/myId/blahblah/this has spaces.xml";
      result = TemplateParseUtil.parseTemplate(input, preprocessedTemplates);
      assertNotNull(result);
      assertEquals(TemplateParseUtil.TEMPLATE_SHOW, result.templateKey);
      assertEquals("myPrefix", result.segmentValues.get(TemplateParseUtil.PREFIX));
      assertEquals("myId", result.segmentValues.get(TemplateParseUtil.ID));

      // try some that will not match
      input = "myPrefix/stuff/stuff";
      result = TemplateParseUtil.parseTemplate(input, preprocessedTemplates);
      assertNull(result);

      // invalid input is not acceptable
      input = "/myPrefix/stuff/specchars=&%$?{}";
      try {
         result = TemplateParseUtil.parseTemplate(input, preprocessedTemplates);
         fail("Should have thrown exception");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

   }

}
