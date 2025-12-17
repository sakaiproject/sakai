/**
 * $Id$
 * $URL$
 * TemplateMap.java - entity-broker - Jul 31, 2008 10:59:53 PM - azeckoski
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
