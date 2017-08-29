/**
 * Copyright (c) 2007-2010 The Apereo Foundation
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
/**
 * TestTextTemplateLogicUtils.java - evaluation - 2008 Jan 16, 2008 5:12:13 PM - azeckoski
 */

package org.sakaiproject.emailtemplateservice.logic.test;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.sakaiproject.emailtemplateservice.util.TextTemplateLogicUtils;


/**
 * Testing the template processing logic
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class TestTextTemplateLogicUtils extends TestCase {

   private String plainTemplate = "This template has nothing in it that can be replaced and therefore should come out identical \n" +
      "to the one that was input. If it does not come out the same then this is sadly quite broken";

   private String sample1 = "This sample template has information that can be replaced. For example, this sentence:\n" +
   		"Welcome ${name}, Your email address is very special. It is ${email}. We like it so much we would like to hire the " +
   		"company you are working for (${company}) to do something for us.\n Sincerly, Some guy";
   private String result1 = "This sample template has information that can be replaced. For example, this sentence:\n" +
         "Welcome Aaron Zeckoski, Your email address is very special. It is aaronz@vt.edu. We like it so much we would like to hire the " +
         "company you are working for (CARET, University of Cambridge) to do something for us.\n Sincerly, Some guy";

   private String sample2 = "This sample template has information that can be replaced. For example, this sentence:\n" +
         "Welcome ${name}, Your email address is very special. It is ${email}. We like it so much we would like to hire the " +
         "company you are working for (${company}) to do something for us.\n Sincerly, ${author}";

   /**
    * Test method for {@link org.sakaiproject.evaluation.logic.impl.utils.TextTemplateLogicUtils#processTextTemplate(java.lang.String, java.util.Map)}.
    */
   public void testProcessTextTemplate() {
      Map<String, String> replacementValues = null;
      String result = null;

      Map<String, String> rVals = new HashMap<String, String>();
      rVals.put("name", "Aaron Zeckoski");
      rVals.put("email", "aaronz@vt.edu");
      rVals.put("company", "CARET, University of Cambridge");
      rVals.put("extra", "EXTRA");

      // make sure that a plain template remains unchanged
      replacementValues = rVals;
      result = TextTemplateLogicUtils.processTextTemplate(plainTemplate, replacementValues, "test");
      assertNotNull(result);
      assertEquals(plainTemplate, result);

      // make sure that a plain template works with null replacement values
      replacementValues = null;
      result = TextTemplateLogicUtils.processTextTemplate(plainTemplate, replacementValues, "test");
      assertNotNull(result);
      assertEquals(plainTemplate, result);

      // make sure a plain template works ok with empty replacement values
      replacementValues = new HashMap<String, String>();
      result = TextTemplateLogicUtils.processTextTemplate(plainTemplate, replacementValues, "test");
      assertNotNull(result);
      assertEquals(plainTemplate, result);

      // make sure a normal replacement works
      replacementValues = rVals;
      result = TextTemplateLogicUtils.processTextTemplate(sample1, replacementValues, "test");
      assertNotNull(result);
      assertEquals(result1, result);

      // check for expected failures
      try {
         result = TextTemplateLogicUtils.processTextTemplate(null, replacementValues, "test");
         fail("Should not have gotten here");
      } catch (IllegalArgumentException e) {
         assertNotNull(e.getMessage());
      }

      // processing template with a missing replacement value causes failure
      try {
         result = TextTemplateLogicUtils.processTextTemplate(sample2, replacementValues, "test");
         fail("Should not have gotten here");
      } catch (RuntimeException e) {
         assertNotNull(e.getMessage());
      }

   }

}
