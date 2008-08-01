/**
 * $Id$
 * $URL$
 * TemplateMap.java - entity-broker - Jul 31, 2008 10:59:53 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Aaron Zeckoski
 * Licensed under the Apache License, Version 2.0
 * 
 * A copy of the Apache License has been included in this 
 * distribution and is available at: http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Aaron Zeckoski (azeckoski @ gmail.com) (aaronz @ vt.edu) (aaron @ caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.entityprovider.extension;

import org.sakaiproject.entitybroker.entityprovider.capabilities.RedirectDefinable;
import org.sakaiproject.entitybroker.util.TemplateParseUtil;


/**
 * An object to hold the incoming and outgoing templates in pairs,
 * used with {@link RedirectDefinable}
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class TemplateMap {

   private String incomingTemplate;
   private String outgoingTemplate;

   /**
    * @param incomingTemplate the URL template pattern to match including the /prefix using {name} to indicate variables <br/>
    * Example: /{prefix}/{thing}/site/{siteId} will match the following URL: <br/>
    * /myprefix/123/site/456, the variables will be {prefix => myprefix, thing => 123, siteId => 456} <br/>
    * NOTE: all incoming URL templates must start with "/{prefix}" ({@link TemplateParseUtil#TEMPLATE_PREFIX}) <br/>
    * @param outgoingTemplate the URL template pattern to fill with values from the incoming pattern,
    * this can start with anything, but will be processed as an external redirect if it starts with "http" or "/" 
    * (unless it starts with "/{prefix}"), otherwise it will be processed as an internal forward
    */
   public TemplateMap(String incomingTemplate, String outgoingTemplate) {
      this.incomingTemplate = incomingTemplate;
      this.outgoingTemplate = outgoingTemplate;
   }
   
   public String getIncomingTemplate() {
      return incomingTemplate;
   }
   
   public void setIncomingTemplate(String incomingTemplate) {
      this.incomingTemplate = incomingTemplate;
   }
   
   public String getOutgoingTemplate() {
      return outgoingTemplate;
   }
   
   public void setOutgoingTemplate(String outgoingTemplate) {
      this.outgoingTemplate = outgoingTemplate;
   }
   
}
