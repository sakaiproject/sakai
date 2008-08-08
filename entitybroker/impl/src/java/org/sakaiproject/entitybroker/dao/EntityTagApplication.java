/**
 * $Id$
 * $URL$
 * AutoRegister.java - entity-broker - 31 May 2007 7:01:11 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 **/

package org.sakaiproject.entitybroker.dao;

/**
 * This is the persistent object for storing entity tag applications,
 * i.e. the combination of an entity and a tag, or the instance of a tag on an entity
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class EntityTagApplication {

   private Long id;
   private String entityRef;
   private String entityPrefix;
   private String tag;

   public EntityTagApplication() {
   }

   public EntityTagApplication(String entityRef, String entityPrefix, String tag) {
      this.entityRef = entityRef;
      this.entityPrefix = entityPrefix;
      this.tag = tag;
   }

   public Long getId() {
      return id;
   }

   public void setId(Long id) {
      this.id = id;
   }

   public String getEntityRef() {
      return entityRef;
   }

   public void setEntityRef(String entityRef) {
      this.entityRef = entityRef;
   }

   public String getEntityPrefix() {
      return entityPrefix;
   }

   public void setEntityPrefix(String entityPrefix) {
      this.entityPrefix = entityPrefix;
   }

   public String getTag() {
      return tag;
   }

   public void setTag(String tag) {
      this.tag = tag;
   }

}
