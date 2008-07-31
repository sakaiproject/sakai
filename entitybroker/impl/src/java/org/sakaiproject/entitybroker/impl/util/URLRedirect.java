/**
 * $Id$
 * $URL$
 * URLRedirect.java - entity-broker - Jul 29, 2008 5:07:58 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.util;

import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;
import org.sakaiproject.entitybroker.util.TemplateParseUtil.PreProcessedTemplate;


/**
 * Storage for the redirects
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class URLRedirect {

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
         throw new IllegalArgumentException("outgoingTemplate must not be null or empty string");
      }
      this.outgoingPreProcessedTemplate = TemplateParseUtil.preprocessTemplate( 
            new TemplateParseUtil.Template(null, outgoingTemplate, false) );
      this.outgoingTemplate = this.outgoingPreProcessedTemplate.template;
   }

   public URLRedirect(String template, String methodName, Class<?>[] methodArgTypes) {
      setTemplate(template);
      if (methodName == null || "".equals(methodName)) {
         throw new IllegalArgumentException("methodName must not be null or empty string");
      }
      if (methodArgTypes == null || "".equals(methodArgTypes)) {
         throw new IllegalArgumentException("methodArgTypes must not be null or empty string");
      }
      this.methodName = methodName;
      this.methodArgTypes = methodArgTypes;
   }

   /**
    * @param template
    */
   public void setTemplate(String template) {
      if (template == null || "".equals(template)) {
         throw new IllegalArgumentException("template must not be null or empty string");
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

}
