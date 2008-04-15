/**
 * $Id$
 * $URL$
 * EBlogic.java - entity-broker - Apr 15, 2008 4:29:18 PM - azeckoski
 **************************************************************************
 * Copyright (c) 2008 Centre for Applied Research in Educational Technologies, University of Cambridge
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 *
 * Aaron Zeckoski (azeckoski@gmail.com) (aaronz@vt.edu) (aaron@caret.cam.ac.uk)
 */

package org.sakaiproject.entitybroker.impl.oldentity;

import java.util.Stack;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entitybroker.impl.EntityHandlerImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class EntityBrokerEntity implements Entity {

   private EntityHandlerImpl entityHandler;

   public void setEntityHandler(EntityHandlerImpl entityHandler) {
      this.entityHandler = entityHandler;
   }

   private ResourceProperties properties;
   // we use only two fields in Reference - type -> parsed.prefix, id -> parsed.id
   private Reference reference;

   void setReference(Reference ref) {
      this.reference = ref;
   }

   public String getId() {
      return reference.getId();
   }

   public ResourceProperties getProperties() {
      return properties;
   }

   public String getReference() {
      return reference.getReference();
   }

   public String getReference(String rootProperty) {
      return getReference();
   }

   public String getUrl() {
      return entityHandler.getEntityURL(reference.getReference(), null, null);
   }

   public String getUrl(String rootProperty) {
      return entityHandler.getEntityURL(reference.getReference(), null, null);
   }

   @SuppressWarnings("unchecked")
   public Element toXml(Document doc, Stack stack) {
      // TODO Auto-generated method stub
      return null;
   }

}
