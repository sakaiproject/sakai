/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006, 2007 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.emailtemplateservice.util;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * This is a weird location but it will have to do for now,
 * this handles processing of text templates
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class TextTemplateLogicUtils {

   /**
    * Handles the replacement of the variable strings within textual templates and
    * also allows the setting of variables for the control of logical branching within
    * the text template as well<br/>
    * Uses and expects freemarker (http://freemarker.org/) style templates 
    * (that is using ${name} as the marker for a replacement)<br/>
    * NOTE: These should be compatible with Velocity (http://velocity.apache.org/) templates
    * 
    * @param textTemplate a freemarker/velocity style text template,
    * cannot be null or empty string
    * @param replacementValues a set of replacement values which are in the map like so:<br/>
    * key => value (String => Object)<br/>
    * username => aaronz<br/>
    * course_title => Math 1001 Differential Equations<br/>
    * @return the processed template
    */
   public static String processTextTemplate(String textTemplate, Map<String, String> replacementValues, String templateName) {
      if (replacementValues == null || replacementValues.size() == 0) {
         return textTemplate;
      }

      if (textTemplate == null || "".equals(textTemplate)) {
         throw new IllegalArgumentException("The textTemplate cannot be null or empty string, " +
         		"please pass in at least something in the template or do not call this method");
      }

      // setup freemarker
      Configuration cfg = new Configuration();

      // Specify how templates will see the data-model
      cfg.setObjectWrapper(new DefaultObjectWrapper()); 

      // get the template
      Template template;
      try {
         template = new Template(templateName, new StringReader(textTemplate), cfg);
      } catch (IOException e) {
         throw new RuntimeException("Failure while creating freemarker template", e);
      }

      Writer output = new StringWriter();
      try {
         template.process(replacementValues, output);
      } catch (TemplateException e) {
         throw new RuntimeException("Failure while processing freemarker template", e);
      } catch (IOException e) {
         throw new RuntimeException("Failure while sending freemarker output to stream", e);
      }

      return output.toString();
   }

/************ commenting out the velocity version for now
   public static String processTextTemplate(String textTemplate, Map<String, String> replacementValues) {
      if (replacementValues == null) {
         return textTemplate;
      }

      // setup velocity
      VelocityEngine ve = null;
      try {
         // trying out creating a new instance of velocity -AZ
         ve = new VelocityEngine();
         ve.init();
      } catch (Exception e) {
         throw new RuntimeException("Could not initialize velocity", e);
      }

      // load in the passed in replacement values
      VelocityContext context = new VelocityContext(replacementValues);

      Writer output = new StringWriter();
      boolean result = false;
      try {
         result = ve.evaluate(context, output, "textProcess", textTemplate);
      } catch (ParseErrorException e) {
         throw new RuntimeException("Velocity parsing error: ", e);
      } catch (MethodInvocationException e) {
         throw new RuntimeException("Velocity method invocation error: ", e);
      } catch (ResourceNotFoundException e) {
         throw new RuntimeException("Velocity resource not found error: ", e);
      } catch (IOException e) {
         throw new RuntimeException("Velocity IO error: ", e);
      }

      if ( result ) {
         return output.toString();
      } else {
         throw new RuntimeException("Failed to process velocity text template");
      }
   }
*********/

}
