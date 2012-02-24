/**
 * $Id$
 * $URL$
 * SakaiToolInfo.java - entity-broker - Apr 25, 2008 10:07:43 AM - azeckoski
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


/**
 * This contains an abstraction of the information about a tool in Sakai
 * 
 * @author Aaron Zeckoski (aaron@caret.cam.ac.uk)
 */
public class SakaiToolData {

   /**
    * The registered tool id for this tool from the XML file (e.g. sakai.tool)
    */
   private String registrationId;
   /**
    * The id of the placement for this tool 
    * (e.g. 32djkj32j-3e232k3jj3-232j23j)
    */
   private String placementId;
   /**
    * The url to this tool
    * (e.g. http://server:port/portal/site/SITE_ID/page/PAGE_ID) 
    */
   private String toolURL;
   /**
    * The location reference where this tool is being accessed
    * (This is the site id in Sakai)
    */
   private String locationReference;
   /**
    * The known title of this tool 
    */
   private String title;
   /**
    * The known description of this tool
    */
   private String description;

   public String getRegistrationId() {
      return registrationId;
   }
   
   public void setRegistrationId(String registrationId) {
      this.registrationId = registrationId;
   }
   
   public String getPlacementId() {
      return placementId;
   }
   
   public void setPlacementId(String placementId) {
      this.placementId = placementId;
   }
   
   public String getToolURL() {
      return toolURL;
   }
   
   public void setToolURL(String toolURL) {
      this.toolURL = toolURL;
   }
   
   public String getLocationReference() {
      return locationReference;
   }
   
   public void setLocationReference(String locationReference) {
      this.locationReference = locationReference;
   }
   
   public String getTitle() {
      return title;
   }
   
   public void setTitle(String title) {
      this.title = title;
   }
   
   public String getDescription() {
      return description;
   }
   
   public void setDescription(String description) {
      this.description = description;
   }

}
