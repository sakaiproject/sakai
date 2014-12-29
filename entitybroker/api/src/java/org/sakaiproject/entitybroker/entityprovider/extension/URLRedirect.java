/**
 * $Id$
 * $URL$
 * URLRedirect.java - entity-broker - Jul 29, 2008 5:07:58 PM - azeckoski
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

package org.sakaiproject.entitybroker.entityprovider.extension;

import java.lang.ref.SoftReference;
import java.lang.reflect.Method;

import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.PreProcessedTemplate;


/**
 * Storage for the redirects
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class URLRedirect implements Comparable <URLRedirect> {

   /**
    * The incoming url template
    * the URL pattern to match AFTER the /prefix using {name} to indicate variables <br/>
    * Example: /{thing}/site/{siteId} will match the following URL: <br/>
    * /myprefix/123/site/456, the variables will be {prefix => myprefix, thing => 123, siteId => 456}
    */
   public String template;
   public PreProcessedTemplate preProcessedTemplate;
   /**
    * (optional) the outgoing template to place the variables into,
    * leave null if using the methods
    */
   public String outgoingTemplate;
   public PreProcessedTemplate outgoingPreProcessedTemplate;
   /**
    * (optional) This will be non-null if there is a custom action method which was found or identified
    * by the annotation {@link EntityCustomAction} or if the developer has defined this
    * explicitly
    */
   public String methodName;
   /**
    * (optional) These are the argument types found in the custom action method in order,
    * this should not be populated manually as any value in this will be overwritten<br/>
    * Must be set if the methodName is set
    */
   public Class<?>[] methodArgTypes;
   /**
    * indicates that this is controllable or not, if this is controllable
    * then all the other fields will be ignored and the redirects will be sent to the execute method
    */
   public boolean controllable = false;

   // NOTE: we are holding onto the method here so the reflection is less costly
   private SoftReference<Method> method;
   /**
    * INTERNAL USE ONLY
    */
   public Method getMethod() {
       Method m = null;
       if (method != null) {
           m = method.get(); 
       }
       return m;
   }
   /**
    * INTERNAL USE ONLY
    */
   public void setMethod(Method m) {
       if (m != null) {
           method = new SoftReference<Method>(m);
       } else {
           method = null;
       }
   }

   /**
    * Use this for controllable template matches only
    * @param template
    */
   public URLRedirect(String template) {
      setTemplate(template);
      controllable = true;
   }

   public URLRedirect(String template, String outgoingTemplate) {
      setTemplate(template);
      if (outgoingTemplate == null || "".equals(outgoingTemplate)) {
         throw new IllegalArgumentException("URLRedirect construction failed: outgoingTemplate must not be null or empty string");
      }
      this.outgoingPreProcessedTemplate = TemplateParseUtil.preprocessTemplate( 
            new TemplateParseUtil.Template(null, outgoingTemplate, false) );
      this.outgoingTemplate = this.outgoingPreProcessedTemplate.template;
   }

   public URLRedirect(String template, String methodName, Class<?>[] methodArgTypes) {
      setTemplate(template);
      if (methodName == null || "".equals(methodName)) {
         throw new IllegalArgumentException("URLRedirect construction failed: methodName must not be null or empty string");
      }
      if (methodArgTypes == null) {
         throw new IllegalArgumentException("URLRedirect construction failed: methodArgTypes must not be null");
      }
      this.methodName = methodName;
      this.methodArgTypes = methodArgTypes;
   }

   /**
    * @param template
    */
   public void setTemplate(String template) {
      if (template == null || "".equals(template)) {
         throw new IllegalArgumentException("URLRedirect set template failed: template must not be null or empty string");
      }
      this.template = template;
      this.preProcessedTemplate = TemplateParseUtil.preprocessTemplate( 
            new TemplateParseUtil.Template(null, template, true) );
   }


   /**
    * @return a copy of this object
    */
   public URLRedirect copy() {
      return copy(this);
   }

   /**
    * @param redirect
    * @return a copy of the supplied object
    */
   public static URLRedirect copy(URLRedirect redirect) {
      if (redirect == null) {
         throw new IllegalArgumentException("redirect to copy must not be null");
      }
      URLRedirect togo = new URLRedirect(redirect.template, redirect.outgoingTemplate);
      togo.preProcessedTemplate = redirect.preProcessedTemplate;
      togo.outgoingPreProcessedTemplate = redirect.outgoingPreProcessedTemplate;
      togo.methodName = redirect.methodName;
      togo.methodArgTypes = redirect.methodArgTypes;
      return togo;
   }

   @Override
   public boolean equals(Object obj) {
      if (null == obj)
         return false;
      if (!(obj instanceof URLRedirect))
         return false;
      else {
         URLRedirect castObj = (URLRedirect) obj;
         if (null == this.template || null == castObj.template)
            return false;
         else
            return (this.template.equals(castObj.template));
      }
   }

   @Override
   public int hashCode() {
      String hashStr = this.getClass().getName() + ":" + this.template.hashCode();
      return hashStr.hashCode();
   }

   @Override
   public String toString() {
      return "URLRedirect: template=" + this.template + ": outgoing=" + this.outgoingTemplate + ": method=" + this.methodName + ": control=" + this.controllable;
   }

   @Override 
   public int compareTo(URLRedirect other) {
      return this.template.compareTo(other.template);
   }

}
