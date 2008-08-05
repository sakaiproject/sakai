/**
 * $Id$
 * $URL$
 * SakaiToolInfo.java - entity-broker - Apr 25, 2008 10:07:43 AM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
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
